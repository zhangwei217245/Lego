package mr.x.brookside.amqp.receiver.processor;


import mr.x.brookside.amqp.model.Probe;
import mr.x.brookside.amqp.receiver.processor.strategy.ProcessingStrategy;
import mr.x.commons.concurrent.BlockingQueueHolder;

import java.util.Set;

/**
 * Created by zhangwei on 14-4-23.
 *
 * MessageProcessor doesn't bound to any queue. Only the ProcessingStrategy will bound to
 * a certain queue.
 *
 * @author zhangwei
 */
public interface MessageProcessor<T> extends Runnable{


    MessageProcessor setBlockingQueueHolder(BlockingQueueHolder<T> blockingQueueHolder);

    MessageProcessor registeProbe(Probe probe);

    MessageProcessor setProcessingStrategies(Set<ProcessingStrategy<T>> strategies);

}
