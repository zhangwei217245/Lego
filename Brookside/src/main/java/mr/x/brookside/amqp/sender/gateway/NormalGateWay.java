package mr.x.brookside.amqp.sender.gateway;


import mr.x.brookside.amqp.model.LegoMessage;

/**
 * Created by zhangwei on 14-4-18.
 *
 * @author zhangwei
 */
public interface NormalGateWay {

    void sendNormalMessage(LegoMessage legoMessage);

    void sendSNSMessage(LegoMessage legoMessage);
}
