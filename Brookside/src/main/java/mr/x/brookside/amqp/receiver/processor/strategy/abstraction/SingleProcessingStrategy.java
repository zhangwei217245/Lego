package mr.x.brookside.amqp.receiver.processor.strategy.abstraction;


import mr.x.brookside.amqp.receiver.processor.strategy.ProcessingStrategy;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by zhangwei on 14-6-4.
 *
 * Strategy aiming at process single message.
 *
 * @author zhangwei
 */
public abstract class SingleProcessingStrategy<T> implements ProcessingStrategy<T>, InitializingBean {

    protected String beanName;

    @Override
    public String getBeanName() {
        return this.beanName;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

}
