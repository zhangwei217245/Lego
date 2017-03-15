package mr.x.brookside.amqp.sender.gateway;


import mr.x.brookside.amqp.model.LegoMessage;
import mr.x.commons.utils.ApiLogger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.support.RabbitGatewaySupport;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * Created by zhangwei on 14-4-18.
 *
 * @author zhangwei
 */
public class RabbitNormalGateWay extends RabbitGatewaySupport implements NormalGateWay {

    private RabbitTemplate snsRabbitTemplate;

    @Override
    public void sendNormalMessage(LegoMessage legoMessage) {
        if (legoMessage == null) {
            throw new NullPointerException("sendNormalMessage failed, instance LegoMessage should not be null");
        }

        getRabbitTemplate().convertAndSend(legoMessage);
        ApiLogger.debug("[x] LegoMessage(%s) [%s] has been sent to normal exchange.", legoMessage.getUuid(), legoMessage);
    }


    @Override
    public void sendSNSMessage(LegoMessage legoMessage) {
        if (legoMessage == null) {
            throw new NullPointerException("sendSNSMessage failed, instance LegoMessage should not be null");
        }
        getSnsRabbitTemplate().convertAndSend(legoMessage);
        ApiLogger.debug("[x] LegoMessage(%s) [%s] has been sent to SNS exchange.", legoMessage.getUuid(), legoMessage);
    }

    public RabbitTemplate getSnsRabbitTemplate() {
        return snsRabbitTemplate;
    }

    public void setSnsRabbitTemplate(RabbitTemplate snsRabbitTemplate) {
        this.snsRabbitTemplate = snsRabbitTemplate;
    }

    public static void main(String[] args) {
        try {
            InetAddress address = InetAddress.getLocalHost();
            System.out.println("" + address.getHostAddress() + ":");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        System.out.println();
    }

}
