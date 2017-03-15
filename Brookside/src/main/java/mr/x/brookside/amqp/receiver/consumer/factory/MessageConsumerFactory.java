package mr.x.brookside.amqp.receiver.consumer.factory;


import mr.x.brookside.amqp.model.Probe;
import mr.x.brookside.amqp.receiver.consumer.MessageConsumer;
import mr.x.commons.concurrent.BlockingQueueHolder;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by zhangwei on 14-6-5.
 *
 * MessageConsumer can be build by any factory of its concrete class.
 * By providing this factory, any MessageConsumer instance could be build up to apply with any message middleware,
 * like RibbitMQ, JBoss MQ, MemcacheQ, even some Nosql storages as a queue.
 *
 * @author zhangwei
 */
public interface MessageConsumerFactory extends ApplicationContextAware {

    MessageConsumer buildMessageConsumer(Probe consumerProbe, BlockingQueueHolder blockingQueueHolder, String queueName);

    /**
     * 此方法在ReceiverControlPanel执行初始化方法时执行。
     * 主要用于判断构造MessageConsumer子类的条件是否已经完备。
     * @return
     * @throws Exception
     */
    boolean preCheck() throws Exception;

}
