package mr.x.brookside.amqp.receiver.processor;


import mr.x.brookside.amqp.model.Probe;
import mr.x.brookside.amqp.receiver.processor.strategy.ProcessingStrategy;
import mr.x.brookside.amqp.receiver.processor.strategy.abstraction.BatchProcessingStrategy;
import mr.x.commons.concurrent.BlockingQueueHolder;
import mr.x.commons.utils.ApiLogger;
import org.apache.commons.collections.CollectionUtils;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhangwei on 14-4-23.
 *
 *
 * MessageProcess should ignore the type of the message it will process.
 *
 * So we turn to generic programming here.
 *
 * @author zhangwei
 */
public class DefaultMessageProcessor<T> implements MessageProcessor<T> {

    public static ExecutorService strategyPool;

    // ensure flush once
    protected AtomicBoolean flushed = new AtomicBoolean(false);

    protected Probe probe;

    protected BlockingQueueHolder<T> blockingQueueHolder;

    protected Set<ProcessingStrategy<T>> processingStrategies;

    public DefaultMessageProcessor(Probe probe, BlockingQueueHolder<T> blockingQueueHolder, Set<ProcessingStrategy<T>> processingStrategies) {
        this.probe = probe;
        this.blockingQueueHolder = blockingQueueHolder;
        this.processingStrategies = processingStrategies;
    }

    public Probe getProbe() {
        return probe;
    }


    public BlockingQueueHolder<T> getBlockingQueueHolder() {
        return blockingQueueHolder;
    }


    @Override
    public MessageProcessor<T> setBlockingQueueHolder(BlockingQueueHolder<T> blockingQueueHolder) {
        this.blockingQueueHolder = blockingQueueHolder;
        return this;
    }


    @Override
    public MessageProcessor<T> registeProbe(Probe probe) {
        this.probe = probe;
        return this;
    }

    public Set<ProcessingStrategy<T>> getProcessingStrategies() {
        return processingStrategies;
    }

    @Override
    public MessageProcessor<T> setProcessingStrategies(Set<ProcessingStrategy<T>> processingStrategies) {
        this.processingStrategies = processingStrategies;
        return this;
    }

    @Override
    public void run() {

        this.probe.getStopWatch().start();

        while (!this.probe.getShutdownTriggered().get()) {
            if (this.probe.getRunning().get()) {
                try {
                    final T message = blockingQueueHolder.pollWithTimeout();

                    if (message == null) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                        }
                        continue;
                    }

                    if (ApiLogger.isDebugEnabled()) {
                        ApiLogger.debug("[x] Processing Message : %s Received!", message);
                    }



                    if (CollectionUtils.isNotEmpty(getProcessingStrategies())) {
                        for (final ProcessingStrategy processingStrategy : getProcessingStrategies()) {
                            try {
                                if (processingStrategy.isApplicable(message)) {
                                    if (processingStrategy.isAsync()) {
                                        strategyPool.submit(new Runnable() {
                                            @Override
                                            public void run() {
                                                processingStrategy.onMessage(message);
                                            }
                                        });
                                    } else {
                                        processingStrategy.onMessage(message);
                                    }

                                    if (ApiLogger.isDebugEnabled()) {
                                        ApiLogger.debug("[x] message %s processed by %s ", message, processingStrategy.getBeanName());
                                    }
                                }


                            } catch (Throwable t) {
                                ApiLogger.error(t, "[x] FAILED TO PROCEED STRATEGY %s", processingStrategy);
                            }

                        }
                    }

                    probe.getWorkCount().incrementAndGet();

                } catch (Throwable t) {
                    probe.getFailCount().incrementAndGet();
                    ApiLogger.error(t, "[x] Message Acquire Failed due to the following reason : %s" , t.getMessage());
                } finally {
                }
            } else {
                // check all BatchProcessingStrategy and invoke the flush method to flush any pending tasks
                if (!flushed.get()) {
                    flushResidual();
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
            }
        }
        ApiLogger.info("[x] Processor shutdown successfully.");

    }

    private void flushResidual() {
        if (CollectionUtils.isNotEmpty(getProcessingStrategies())) {
            for (final ProcessingStrategy processingStrategy : getProcessingStrategies()) {
                try {
                    if (processingStrategy instanceof BatchProcessingStrategy) {
                        if (((BatchProcessingStrategy) processingStrategy).getBatchCache().size() > 0) {
                            ((BatchProcessingStrategy) processingStrategy).flush();
                        }
                    }

                    if (ApiLogger.isDebugEnabled()) {
                        ApiLogger.debug("[x] Residual message flushed!");
                    }
                } catch (Throwable t) {
                    ApiLogger.error(t, "[x] FAILED TO FLUSH RESIDUAL MESSAGE IN STRATEGY %s", processingStrategy);
                }
            }
        }
        flushed.compareAndSet(false, true);
    }
}
