package mr.x.commons.concurrent;

import mr.x.commons.utils.ApiLogger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author zhangwei .
 */
public class BlockingQueueHolder<T> {

    private ArrayBlockingQueue<T> blockingQueue;
    private int queueSize = 1000;
    private int queueTimeout = 5000;

    public BlockingQueueHolder() {
        init();
    }

    public BlockingQueueHolder(final int size, final int timeout) {
        if (queueSize != 0) {
            queueSize = size;
        }
        if (queueTimeout != 0) {
            queueTimeout = timeout;
        }
        init();
    }

    public void init() {
        ApiLogger.info("init queue with size " + queueSize);
        blockingQueue = new ArrayBlockingQueue<T>(queueSize);
    }

    public void put(T msg) {
        try {
            blockingQueue.put(msg);
        } catch (InterruptedException ex) {
            ApiLogger.debug("InterruptedException:" + ex.getMessage());
        }
    }

    public boolean offer(final T msg) {
        return blockingQueue.offer(msg);
    }

    public boolean offerWithTimeout(final T msg) {
        boolean rst = false;
        try {
            rst = blockingQueue.offer(msg, queueTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            ApiLogger.debug("InterruptedException:" + e.getMessage());
        }
        return rst;
    }

    public T poll() {
        try {
            return blockingQueue.poll(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }

    public T pollWithTimeout() {
        T rst = null;
        try {
            rst = blockingQueue.poll(queueTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            ApiLogger.debug("InterruptedException:" + ex.getMessage());
        }
        return rst;
    }

    public T take() {
        T rst = null;
        try {
            rst = blockingQueue.take();
        } catch (InterruptedException ex) {
            ApiLogger.debug("InterruptedException:" + ex.getMessage());
        }
        return rst;
    }

    public int getCapacity() {
        return queueSize;
    }

    public int getSize() {
        return this.blockingQueue.size();
    }

    public ArrayBlockingQueue<T> getBlockingQueue() {
        return this.blockingQueue;
    }
}
