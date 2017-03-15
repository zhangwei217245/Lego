package mr.x.brookside.amqp.receiver.consumer.factory;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * Created by zhangwei on 14-6-5.
 *
 * This is a basic template for building upRabbitMqConsumer instance.
 *
 * Notice that it has been injected with rabbitTemplate instance.
 *
 * @author zhangwei
 */
public abstract class AbstractRabbitConsumerFactory implements MessageConsumerFactory{

    protected ApplicationContext context;

    protected RabbitTemplate rabbitTemplate;

    public RabbitTemplate getRabbitTemplate() {
        return rabbitTemplate;
    }

    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public boolean preCheck() throws Exception {
        // check for RabbitTemplate instance named 'normalRabbitTemplate'
        if ((this.rabbitTemplate == null)) {
            RabbitTemplate ref = this.context.getBean("normalRabbitTemplate", RabbitTemplate.class);
            if (ref == null) {
                throw new Exception("Cannot find any bean of type {RabbitTemplate} named 'normalRabbitTemplate' in spring context!");
            }
            this.rabbitTemplate = ref;
        }
        return true;
    }
}
