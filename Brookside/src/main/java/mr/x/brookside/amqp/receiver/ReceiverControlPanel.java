package mr.x.brookside.amqp.receiver;


import mr.x.brookside.amqp.receiver.consumer.factory.MessageConsumerFactory;
import mr.x.brookside.amqp.receiver.processor.strategy.ProcessingStrategy;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;
import java.util.Set;

/**
 * Created by zhangwei on 14-4-22.
 *
 *
 * This is a control panel interface which defines some actions and commands that controls both the consumer and
 * processor.
 *
 * @author zhangwei
 */
public interface ReceiverControlPanel extends ApplicationContextAware, InitializingBean, DisposableBean{

    void initialize();

    void startConsumer();

    void stopConsumer();

    boolean shutdown();

    void setConcurrencyFactor(int concurrencyFactor);

    void setConsumerCount(int consumerCount);

    void setProcessorCount(int processorCount);

    void setBlockingQueueSize(int blockingQueueSize);

    void setBlockingQueueTimeout(int blockingQueueTimeout);

    void setMessageConsumerFactory(MessageConsumerFactory messageConsumerFactory);

    void setQueueNames(Set<String> queueNames);

    void tryAddNewConsumer(String queueName);

    void tryAddNewProcessor(String queueName);

    void setStrategyPoolSize(int poolSize);

    String printTPS();

    boolean isReady();

    void setProcessingStrategies(Collection<ProcessingStrategy> strategies);

    boolean isTerminated();
}
