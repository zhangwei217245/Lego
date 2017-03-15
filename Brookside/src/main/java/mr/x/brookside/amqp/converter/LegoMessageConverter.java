package mr.x.brookside.amqp.converter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import mr.x.brookside.amqp.model.LegoMessage;
import mr.x.commons.enums.LegoModules;
import mr.x.commons.utils.ApiLogger;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.SimpleMessageConverter;

import java.util.Date;
import java.util.Map;



/**
 * Created by zhangwei on 14-4-22.
 *
 *
 *
 * @author zhangwei
 */
public class LegoMessageConverter extends SimpleMessageConverter {

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        if (message == null) {
            return null;
        }

        MessageProperties msgProperties = message.getMessageProperties();

        String msgType = msgProperties.getType();

        if (StringUtils.isBlank(msgType)) {
            ApiLogger.warn("[x] message type is not acceptable");
        }
        if ("LegoMessage".equals(msgType)) {
            Object object = super.fromMessage(message);
            if (object instanceof String) {
                try {
                    JSONObject body = JSON.parseObject((String) object);
                    Map<String, Object> headers = msgProperties.getHeaders();
                    return LegoMessage.buildFromMap(
                            /**
                             * Module
                             */
                            LegoModules.forName(headers.get("LegoModule") + ""),
                            /**
                             * Action
                             */
                            LegoMessage.Action.forName(headers.get("action") + ""),
                            /**
                             * queueName
                             */
                            headers.get("queueName") == null ? null : String.valueOf(headers.get("queueName")),
                            /**
                             * timestamp
                             */
                            msgProperties.getTimestamp().getTime(),
                            /**
                             * UUID
                             */
                            msgProperties.getMessageId(),
                            /**
                             * body (Map<String, Object>)
                             */
                            body);
                } catch (Throwable t) {
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    protected Message createMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
        if (object == null || (!(object instanceof LegoMessage))) {
            return null;
        }

        LegoMessage legoMessage = (LegoMessage)object;

        messageProperties.setType("LegoMessage");
        messageProperties.setMessageId(legoMessage.getUuid());
        messageProperties.setTimestamp(new Date(legoMessage.getTimestamp()));
        messageProperties.setHeader("queueName", legoMessage.getQueueName());
        messageProperties.setHeader("action", legoMessage.getAction().name());
        messageProperties.setHeader("LegoModule", legoMessage.getModule().name());

        // 消息格式变更为：只序列化LegoMessage中的info
        return super.createMessage(legoMessage.getBodyJsonString(), messageProperties);
    }
}
