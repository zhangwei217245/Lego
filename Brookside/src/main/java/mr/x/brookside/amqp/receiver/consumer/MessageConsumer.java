package mr.x.brookside.amqp.receiver.consumer;


import mr.x.brookside.amqp.model.Probe;
import mr.x.commons.concurrent.BlockingQueueHolder;

/**
 * Created by zhangwei on 14-4-23.
 *
 * An instance of MessageConsumer only cares about five things:
 * 1. The blocking queue which holds the message and play the role of an internal buffer.
 * 2. queueName. The identification of a queue.
 * 3. A probe that collect data for monitoring.
 * 4. how to read a message/ where to read a message
 * 5. What to do when a message is received and before it is enqueue.
 *
 * @author zhangwei
 */
public interface MessageConsumer<T> extends Runnable {

    MessageConsumer setBlockingQueueHolder(BlockingQueueHolder<T> blockingQueueHolder);

    MessageConsumer setQueueName(String queueName);

    MessageConsumer registerProbe(Probe probe);

    T readMessage();

    void processMessageBeforeQueue(T message);
}
