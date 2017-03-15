package mr.x.brookside.amqp.receiver.consumer;


import mr.x.brookside.amqp.model.Probe;
import mr.x.commons.concurrent.BlockingQueueHolder;
import mr.x.commons.utils.ApiLogger;

/**
 * Created by zhangwei on 14-6-5.
 *
 * This is the basic framework of a message consumer.
 *
 * The method "readMessage" and "processMessageBeforeQueue" are two critical actions which should be implemented
 * in its sub-class.
 *
 * You could simply create a sub-class of this class, and write the code to "readMessage" from the message source you've
 * chosen. And do things like check message format or convert message in "processMessageBeforeQueue" method.
 *
 * However, as a generic message consumer abstraction,
 * it doesn't care the type of the message and the source of the message.
 *
 * @author zhangwei
 */
public abstract class AbstractMessageConsumer<T> implements MessageConsumer<T> {

    protected Probe probe;

    protected BlockingQueueHolder<T> blockingQueueHolder;

    protected String queueName;


    public BlockingQueueHolder<T> getBlockingQueueHolder() {
        return blockingQueueHolder;
    }

    @Override
    public MessageConsumer setBlockingQueueHolder(BlockingQueueHolder<T> blockingQueueHolder) {
        this.blockingQueueHolder = blockingQueueHolder;
        return this;
    }


    public String getQueueName() {
        return queueName;
    }

    @Override
    public MessageConsumer setQueueName(String queueName) {
        this.queueName = queueName;
        return this;
    }

    @Override
    public MessageConsumer registerProbe(Probe probe) {
        this.probe = probe;
        return this;
    }

    @Override
    public void run() {
        while (!this.probe.getShutdownTriggered().get()) {
            if (this.probe.getRunning().get()) {
                try {

                    T message = readMessage();

                    if (message == null) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                        }
                        continue;
                    }
                    if (ApiLogger.isDebugEnabled()) {
                        ApiLogger.debug("[x] Message : %s Received!", message);
                    }

                    processMessageBeforeQueue(message);

                    getBlockingQueueHolder().put(message);
                    probe.getWorkCount().incrementAndGet();
                } catch (Throwable t) {
                    probe.getFailCount().incrementAndGet();
                    ApiLogger.error(t, "[x] Message Acquire Failed due to the following reason : %s" , t.getMessage());
                }
            } else {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
            }
        }
        ApiLogger.info("[x] Consumer shutdown successfully.");
    }
}
