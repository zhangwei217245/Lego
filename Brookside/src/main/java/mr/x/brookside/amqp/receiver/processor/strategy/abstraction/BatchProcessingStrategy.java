package mr.x.brookside.amqp.receiver.processor.strategy.abstraction;

import mr.x.brookside.amqp.receiver.processor.strategy.ProcessingStrategy;
import mr.x.commons.utils.ApiLogger;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zhangwei on 14-4-23.
 *
 * Strategy aiming at processing a bunch of messages.
 *
 * @author zhangwei
 */
public abstract class BatchProcessingStrategy<T> implements ProcessingStrategy<T>, InitializingBean {

    protected String beanName;

    protected int flushThreshold;

    protected List<T> batchCache = new LinkedList<>();

    @Override
    public void onMessage(T messages) {
        if (messages == null) {
            return;
        }
        batchCache.add(messages);
        if (batchCache.size() >= flushThreshold) {
            try{
                flush(batchCache);
            } catch (Throwable t) {
                ApiLogger.error(t, "[x] unpredictable error occurred, msg : %s", t.getMessage());
                //TODO: logging batchCatch items here
            } finally {
                batchCache.clear();
            }
        }
    }

    @Override
    public String getBeanName() {
        return this.beanName;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    public void setFlushThreshold(int flushThreshold) {
        this.flushThreshold = flushThreshold;
    }

    public int getFlushThreshold() {
        return flushThreshold;
    }

    public List<T> getBatchCache() {
        return batchCache;
    }

    public void flush() {
        flush(batchCache);
    }

    public abstract void flush(Collection<T> messages);
}
