package mr.x.brookside.amqp.receiver;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import mr.x.brookside.amqp.model.Probe;
import mr.x.brookside.amqp.receiver.consumer.MessageConsumer;
import mr.x.brookside.amqp.receiver.consumer.factory.MessageConsumerFactory;
import mr.x.brookside.amqp.receiver.processor.DefaultMessageProcessor;
import mr.x.brookside.amqp.receiver.processor.MessageProcessor;
import mr.x.brookside.amqp.receiver.processor.strategy.AbstractProcessStrategyFilter;
import mr.x.brookside.amqp.receiver.processor.strategy.ProcessingStrategy;
import mr.x.commons.concurrent.BlockingQueueHolder;
import mr.x.commons.concurrent.StandardThreadExecutor;
import mr.x.commons.utils.ApiLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhangwei on 14-4-23.
 *
 * The implementation of a ReceiverControlPanel interface.
 * A receiver contains tree part:
 * 1. MessageConsumer which receive the message from the source and put it into a blocking queue.
 * 2. MessageProcessor which poll a message from the blocking queue and process it according to certain strategy.
 * 3. The control panel which initiate the consumer and processor, controls the behavior of both.
 *
 * @author zhangwei
 */
public class ReceiverControlPanelImpl implements ReceiverControlPanel {

    // TODO : make it configurable;
    private static final int DEFAULT_SHUTDOWN_WAIT_TIMEOUT = 5000;

    private ExecutorService consumerExecutor;
    private ExecutorService processorExecutor;

    private int consumerCount = 10;
    private int processorCount = 10;
    private int concurrencyFactor = 1;
    private int blockingQueueSize = 5000;
    private int blockingQueueTimeout = 2000;

    private int strategyPoolSize = Runtime.getRuntime().availableProcessors() * 10;

    private ApplicationContext context;

    private MessageConsumerFactory consumerFactory;

    private Set<String> queueNames = new LinkedHashSet<>();

    private Map<String, BlockingQueueHolder> blockingQueueHolderMap = new ConcurrentHashMap<>();

    private Map<String, Probe> consumerProbeMap = new ConcurrentHashMap<>();

    private Map<String, Probe> processorProbeMap = new ConcurrentHashMap<>();

    private ArrayList<MessageConsumer> consumerArrayList = new ArrayList<>();

    private ArrayList<MessageProcessor> messageProcessorArrayList = new ArrayList<>();

    private Set<ProcessingStrategy> strategies = new LinkedHashSet<>();

    private AbstractProcessStrategyFilter strategyFilter;

    private AtomicBoolean isReady = new AtomicBoolean(false);


    @Override
    public void initialize() {

        DefaultMessageProcessor.strategyPool = Executors.newFixedThreadPool(this.getStrategyPoolSize());

        // submit consumers to read amqp
        consumerExecutor = new StandardThreadExecutor(consumerCount, calculateMaxThreadCount(consumerCount, concurrencyFactor));

        for (String qName : this.queueNames) {
            for (int i = 0; i < consumerCount; i++) {
                tryAddNewConsumer(qName);
            }
        }

        // submit processors to wait for BlockingQueue messages.

        processorExecutor = new StandardThreadExecutor(processorCount, calculateMaxThreadCount(processorCount, concurrencyFactor * 2));

        for (String qName : this.queueNames) {
            for (int i = 0; i < processorCount; i++) {
                tryAddNewProcessor(qName);
            }
        }

        startProcessor();

        // wait for RESTAPI to start the consumers
        //startConsumer();

    }

    private int calculateMaxThreadCount(int consumerCount, int queueCount) {
        int base = consumerCount;
        int cpuCount = Runtime.getRuntime().availableProcessors();
        if (consumerCount > cpuCount) {
            base = cpuCount;
        }
        return base * queueCount * 4 + 1;
    }

    private void startProcessor() {
        for (Map.Entry<String, Probe> entry : this.processorProbeMap.entrySet()) {
            setRunning(entry.getValue().getRunning(), true);
            entry.getValue().getStopWatch().start(entry.getKey());
        }
    }

    private void stopProcessor() {
        for (Map.Entry<String, Probe> entry : this.processorProbeMap.entrySet()) {
            setRunning(entry.getValue().getRunning(), false);
            entry.getValue().getStopWatch().stop(entry.getKey());
        }
    }

    @Override
    public void startConsumer() {
        for (Map.Entry<String, Probe> entry : this.consumerProbeMap.entrySet()) {
            setRunning(entry.getValue().getRunning(), true);
            entry.getValue().getStopWatch().start(entry.getKey());
        }
    }

    @Override
    public void stopConsumer() {
        for (Map.Entry<String, Probe> entry : this.consumerProbeMap.entrySet()) {
            setRunning(entry.getValue().getRunning(), false);
            entry.getValue().getStopWatch().stop(entry.getKey());
        }
    }

    private void shutdownProcessor() {
        for (Map.Entry<String, Probe> entry : this.processorProbeMap.entrySet()) {
            setRunning(entry.getValue().getShutdownTriggered(), true);
            entry.getValue().getStopWatch().stop(entry.getKey());
        }
    }

    private void shutdownConsumer() {
        for (Map.Entry<String, Probe> entry : this.consumerProbeMap.entrySet()) {
            setRunning(entry.getValue().getShutdownTriggered(), true);
            entry.getValue().getStopWatch().stop(entry.getKey());
        }
    }

    private void setRunning(AtomicBoolean running, boolean isRunning) {
        running.compareAndSet(!isRunning, isRunning);
    }

    @Override
    public boolean shutdown() {
        int retried = 0;
        boolean consumerTerminated = false;
        boolean processorTerminated = false;
        boolean strategyTerminated = false;
        // retry until all three thread pools are terminated.
        while ((!(consumerTerminated
                && processorTerminated
                && strategyTerminated)) && retried < 10) {
            retried++;
            //try to shutdown consumer pool
            stopConsumer();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {

            }
            shutdownConsumer();
            consumerExecutor.shutdown();

            try {
                consumerTerminated = consumerExecutor.awaitTermination(DEFAULT_SHUTDOWN_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                //TODO: logging here?
                ApiLogger.warn("[x] COULD NOT SHUTDOWN THE consumerExecutor INSTANCE! ");
                continue;
            }
            if (consumerTerminated) {
                ApiLogger.info("[x] consumerExecutor SHUTDOWN SUCCESSFULLY");
            }

            //try to shutdown processor pool
            stopProcessor();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {

            }
            shutdownProcessor();
            processorExecutor.shutdown();

            try {
                processorTerminated = processorExecutor.awaitTermination(DEFAULT_SHUTDOWN_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                //TODO: logging here?
                ApiLogger.warn("[x] COULD NOT SHUTDOWN THE processorExecutor INSTANCE! ");
                continue;
            }
            if (processorTerminated) {
                ApiLogger.info("[x] processorExecutor SHUTDOWN SUCCESSFULLY");
            }

            int bQueueShutdownRetried = 0;
            while (!isAllBlockQueueEmpty() && bQueueShutdownRetried < 10) {
                try {
                    ApiLogger.info("[x] WAITING FOR BLOCKING QUEUE TO BE EMPTY");
                    Thread.sleep(DEFAULT_SHUTDOWN_WAIT_TIMEOUT);
                } catch (InterruptedException e) {
                    //TODO: logging here?
                    ApiLogger.warn("[x] BlockingQueue WAITING FAILED DURING SHUTDOWN PROCESSING");
                }
                bQueueShutdownRetried++;
            }
            // shutdown strategy pool.
            try {
                DefaultMessageProcessor.strategyPool.shutdown();
                strategyTerminated = DefaultMessageProcessor.strategyPool.awaitTermination(DEFAULT_SHUTDOWN_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                //TODO: logging here?
                ApiLogger.warn("[x] COULD NOT SHUTDOWN THE strategyPool INSTANCE! ");
                continue;
            }
            if (strategyTerminated) {
                ApiLogger.info("[x] strategyPool SHUTDOWN SUCCESSFULLY");
            }
        }
        ApiLogger.info("[x] MessageProcessor shutdown successfully after %d times of retry", retried);
//        ApiLogger.info("[x] MessageProcessor exit now! ");
//        System.exit(0);
        return true;
    }

    private boolean isAllBlockQueueEmpty() {
        if (MapUtils.isEmpty(blockingQueueHolderMap)) {
            return true;
        }
        boolean rst = true;
        for (Map.Entry<String, BlockingQueueHolder> entry : blockingQueueHolderMap.entrySet()) {
            rst = rst && entry.getValue().getSize() <= 0;
        }
        return rst;
    }

    @Override
    public void setConcurrencyFactor(int concurrencyFactor) {
        this.concurrencyFactor = concurrencyFactor;
    }

    @Override
    public void setConsumerCount(int consumerCount) {
        this.consumerCount = consumerCount;
    }

    @Override
    public void setProcessorCount(int processorCount) {
        this.processorCount = processorCount;
    }

    @Override
    public void setBlockingQueueSize(int blockingQueueSize) {
        this.blockingQueueSize = blockingQueueSize;
    }

    @Override
    public void setBlockingQueueTimeout(int blockingQueueTimeout) {
        this.blockingQueueTimeout = blockingQueueTimeout;
    }

    @Override
    public void setMessageConsumerFactory(MessageConsumerFactory messageConsumerFactory){
        this.consumerFactory = messageConsumerFactory;
    }


    @Override
    public void setQueueNames(Set<String> queueNames) {
        if (CollectionUtils.isNotEmpty(queueNames)) {
            this.queueNames.addAll(queueNames);
        }
    }

    @Override
    public void tryAddNewConsumer(String queueName) {
        BlockingQueueHolder blockingQueueHolder = blockingQueueHolderMap.get(queueName);
        if (blockingQueueHolder == null) {
            blockingQueueHolder = new BlockingQueueHolder<>(blockingQueueSize, blockingQueueTimeout);
            blockingQueueHolderMap.put(queueName, blockingQueueHolder);
        }

        Probe consumerProbe = consumerProbeMap.get(queueName);
        if (consumerProbe == null) {
            consumerProbe = new Probe();
            consumerProbeMap.put(queueName, consumerProbe);
        }
        MessageConsumer consumer = consumerFactory.buildMessageConsumer(consumerProbe, blockingQueueHolder, queueName);
        consumerExecutor.submit(consumer);
        consumerArrayList.add(consumer);
    }

    @Override
    public void tryAddNewProcessor(final String queueName) {
        if (queueName == null) {
            return;
        }
        BlockingQueueHolder blockingQueueHolder = blockingQueueHolderMap.get(queueName);
        if (blockingQueueHolder == null) {
            ApiLogger.warn("[x] No BlockingQueueHolder found for queue[%s], failed to submit new Processor.", queueName);
            return;
        }

        if (processorExecutor == null) {
            ApiLogger.warn("[x] ProcessorExecutor wasn't initialized, failed to submit new Processor. ");
            return;
        }

        Probe processorProbe = processorProbeMap.get(queueName);
        if (processorProbe == null) {
            processorProbe = new Probe();
            processorProbeMap.put(queueName, processorProbe);
        }

        MessageProcessor messageProcessor = new DefaultMessageProcessor(processorProbe, blockingQueueHolder,
                Sets.filter(strategies, strategyFilter)
        );
        processorExecutor.submit(messageProcessor);
        messageProcessorArrayList.add(messageProcessor);
    }

    @Override
    public void setStrategyPoolSize(int poolSize) {
        this.strategyPoolSize = poolSize;
    }

    public int getStrategyPoolSize() {
        return this.strategyPoolSize;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void destroy() throws Exception {
        while (!this.shutdown()) {
            Thread.sleep(DEFAULT_SHUTDOWN_WAIT_TIMEOUT / 2);
        }
    }

    @Override
    public String printTPS() {
        Map<String, Long> tpsRst = new LinkedHashMap<>();
        for (Map.Entry<String, Probe> entry : consumerProbeMap.entrySet()) {
            String queueName = entry.getKey();
            Probe probe = entry.getValue();
            long workCount = probe.getWorkCount().incrementAndGet();
            long duration = probe.getStopWatch().getElapsedTime() / 1000L;
            if (duration == 0) {
                ApiLogger.info("[x] Current Consumer TPS of Queue : just started");
            }else{
                long currentTPS = workCount / duration;
                ApiLogger.info("[x] Current Consumer TPS of Queue[%s] : %d", queueName, currentTPS);
                tpsRst.put("consumer_" + queueName, currentTPS);
            }
        }

        for (Map.Entry<String, Probe> entry : processorProbeMap.entrySet()) {
            String queueName = entry.getKey();
            Probe probe = entry.getValue();
            long workCount = probe.getWorkCount().incrementAndGet();
            long duration = probe.getStopWatch().getElapsedTime() / 1000L;
            if (duration == 0) {
                ApiLogger.info("[x] Current Processor TPS of Queue : just started");
            }else{
                long currentTPS = workCount / duration;
                ApiLogger.info("[x] Current Processor TPS of Queue[%s] : %d", queueName, currentTPS);
                tpsRst.put("processor_" + queueName, currentTPS);
            }

        }

        return JSON.toJSONString(tpsRst);
    }

    @Override
    public boolean isReady() {
        return this.isReady.get();
    }

    @Override
    public void setProcessingStrategies(Collection<ProcessingStrategy> strategies) {
        this.strategies.addAll(strategies);
    }

    public AbstractProcessStrategyFilter getStrategyFilter() {
        return strategyFilter;
    }

    public void setStrategyFilter(AbstractProcessStrategyFilter strategyFilter) {
        this.strategyFilter = strategyFilter;
    }

    @Override
    public boolean isTerminated() {
        return consumerExecutor.isTerminated() && processorExecutor.isTerminated() && DefaultMessageProcessor.strategyPool.isTerminated();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // pre-check on consumer factories:
        if (!consumerFactory.preCheck()) {
            return;
        }

        // check if queueNames is not empty
        if (CollectionUtils.isEmpty(this.queueNames)) {
            throw new Exception("queueNames is empty!");
        }

        // load all ProcessingStrategy instances
        Map<String, ProcessingStrategy> strategyMap = context.getBeansOfType(ProcessingStrategy.class);
        if (MapUtils.isNotEmpty(strategyMap)) {
            setProcessingStrategies(strategyMap.values());
        }
        initialize();
        this.isReady.compareAndSet(false, true);
    }
}
