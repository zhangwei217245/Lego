package mr.x.brookside.amqp.receiver.processor.strategy;

import org.springframework.beans.factory.BeanNameAware;

/**
 * Created by zhangwei on 14-4-23.
 *
 *
 * As the interface of ProcessingStrategy, it just omit the type of the message it will process.
 *
 * But the message type should be determined by its certain implementation
 *
 * @author zhangwei
 */
public interface ProcessingStrategy<T> extends BeanNameAware {

    public void onMessage(T message);

    boolean isApplicable(T message);

    String getBeanName();

    /**
     * 判断是否任务处理是否为异步模式。true异步，false同步
     *
     * @return
     */
    boolean isAsync();
}
