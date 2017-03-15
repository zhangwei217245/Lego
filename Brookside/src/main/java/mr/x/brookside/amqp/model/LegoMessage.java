/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mr.x.brookside.amqp.model;

import com.alibaba.fastjson.JSON;
import mr.x.commons.enums.LegoModules;
import mr.x.commons.utils.ApiLogger;
import org.apache.commons.lang.ArrayUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author zhangwei
 */
public class LegoMessage implements Serializable {

    private static final long serialVersionUID = 3596387523849296070L;

    private LegoModules module;

    private String uuid;

    private Action action;

    private long timestamp;

    private String queueName;

    private Map<String, Object> messageBody;

    private LegoMessage() {
        uuid = UUID.randomUUID().toString();//
    }

//    public static LegoMessage parseFromJsonString(String json) {
//        return JSON.parseObject(json, LegoMessage.class);
//    }

//    public static String buildJsonString(LegoModules module, Action action, String queueName, Object... bodyEntries){
//        return LegoMessage.buildFromMap(module, action, queueName, bodyEntries).toJsonString();
//    }

    public static LegoMessage buildFromMap(LegoModules module, Action action, String queueName, long timestamp, String uuid, Map<String, Object> messageBody) {
        LegoMessage instance = new LegoMessage();
        return instance.setModule(module).setAction(action).setQueueName(queueName).setTimestamp(timestamp).setUuid(uuid).setMessageBody(messageBody);
    }

    public static LegoMessage build(LegoModules module, Action action, String queueName, Object... bodyEntries) {
        return buildWithUUID(module, action, queueName, UUID.randomUUID().toString(), bodyEntries);
    }

    public static LegoMessage buildWithUUID(LegoModules module, Action action, String queueName, String uuid, Object[] bodyEntries) {
        if (ArrayUtils.isEmpty(bodyEntries) || module == null || action == null) {
            ApiLogger.warn("[x] 'null' shouldn't be an element of arguments.");
            return null;
        }
        if (bodyEntries.length % 2 != 0) {
            ApiLogger.warn("[x] Wrong number of arguments.");
            return null;
        }
        Map<String, Object> bodyEntryMap = new HashMap<>(bodyEntries.length / 2);
        for (int i = 0; i < bodyEntries.length; i += 2) {
            bodyEntryMap.put(bodyEntries[i].toString(), bodyEntries[i + 1]);
        }

        LegoMessage instance = new LegoMessage();
        return instance.setModule(module).setAction(action)
                .setQueueName(queueName).setUuid(uuid).setMessageBody(bodyEntryMap).setTimestamp(System.currentTimeMillis());

    }

    public LegoModules getModule() {
        return module;
    }

    public LegoMessage setModule(LegoModules module) {
        this.module = module;
        return this;
    }

    public Action getAction() {
        return action;
    }

    public LegoMessage setAction(Action action) {
        this.action = action;
        return this;
    }

    public Map<String, Object> getMessageBody() {
        return messageBody;
    }

    public LegoMessage setMessageBody(Map<String, Object> messageBody) {
        this.messageBody = messageBody;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public LegoMessage setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getQueueName() {
        return queueName;
    }

    public LegoMessage setQueueName(String queueName) {
        this.queueName = queueName;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public LegoMessage setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getBodyJsonString() {
        return JSON.toJSONString(this.getMessageBody());
    }

//    public String toJsonString() {
//        return JSON.toJSONString(this);
//    }

    @Override
    public String toString() {
        return "LegoMessage{" +
                "module=" + module +
                ", action=" + action +
                ", timestamp=" + timestamp +
                ", queueName=" + queueName +
                ", uuid='" + uuid + '\'' +
                ", messageBody='" + messageBody + '\'' +
                '}';
    }

    public static enum Action {
        CREATE, READ, UPDATE, DELETE, JOIN;

        static final Map<String, Action> nameMapping = new HashMap<>();

        static {
            for (Action action : Action.values()) {
                nameMapping.put(action.name(), action);
            }
        }

        public static Action forName(String name) {
            return nameMapping.get(name);
        }
    }


}
