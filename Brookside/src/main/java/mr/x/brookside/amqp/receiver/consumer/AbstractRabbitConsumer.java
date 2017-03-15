package mr.x.brookside.amqp.receiver.consumer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Created by zhangwei on 14-6-5.
 *
 * This is the MessageConsumer abstraction for reading messages from rabbitTemplate.
 * But it is still a generic class which omit the type of the message, 'cause you could
 * convert the message format as you want.
 *
 * @author zhangwei
 */
public abstract class AbstractRabbitConsumer<T> extends AbstractMessageConsumer<T>{

    protected RabbitTemplate rabbitTemplate;

    public RabbitTemplate getRabbitTemplate() {
        return rabbitTemplate;
    }

    public MessageConsumer setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        return this;
    }
}
