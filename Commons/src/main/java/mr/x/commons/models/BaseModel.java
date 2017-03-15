package mr.x.commons.models;

import com.alibaba.fastjson.JSON;
import mr.x.commons.utils.ApiLogger;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BaseModel implements Serializable {
    private static final long serialVersionUID = 5819826420430806522L;
    private Map<String, Object> properties;

    public Object getProperty(String key) {
        if (this.properties == null || this.properties.isEmpty()) {
            return null;
        }
        if (properties.containsKey(key)) {
            return this.properties.get(key);
        }
        return null;
    }

    public Object addProperty(String key, Object value) {
        if (this.properties == null) {
            synchronized (this) {
                this.properties = new ConcurrentHashMap();
            }
        }
        if (value == null) {
            return null;
        }
        return this.properties.put(key, value);
    }

    public Map<String, Object> getProperties() {
        if (this.properties == null) {
            synchronized (this) {
                this.properties = new ConcurrentHashMap();
            }
        }
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String toJSONString() {

        return JSON.toJSONString(this);
    }


    public int getIntProperty(String key) {
        Object o = getProperty(key);
        if (o == null) {
            return 0;
        }
        try {
            return Integer.parseInt(o.toString());
        } catch (Exception e) {
            ApiLogger.error("error:{}", e);
        }
        return 0;


    }
}
