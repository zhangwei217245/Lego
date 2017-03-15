package org.springframework.data.redis.connection.repackage.release_1_2_0.ha;

import mr.x.commons.utils.ApiLogger;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.repackage.release_1_2_0.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhangwei on 14-5-8.
 *
 * @author zhangwei
 */
public class RedisConnectionFactoryBuilder {


    public static List<RedisConnectionFactory> buildJedisConnectionFactories(String connectionString, Boolean usePool, JedisPoolConfig jedisPoolConfig) {

        if (StringUtils.isBlank(connectionString)) {
            ApiLogger.error("[JedisConnectionFactoryBuilder] ConnectionString should not be blank!");
            throw new IllegalStateException("[JedisConnectionFactoryBuilder] ConnectionString should not be blank!");
        }


        List<RedisConnectionFactory> jedisConnectionFactories = new ArrayList<>();

        String[] portTokens = connectionString.split("\\|");


        int i = 0;
        String hostName = "127.0.0.1";
        String port = "6379";
        String dbIndex = "0";
        String password = null;
        String timeoutMillis = "10000";
        String[] connStrTokens = new String[5];
        connStrTokens[0] = hostName; connStrTokens[1] = port; connStrTokens[2] = dbIndex; connStrTokens[3] = password; connStrTokens[4] = timeoutMillis;


        for (String portToken : portTokens){
            if (StringUtils.isBlank(portToken)) {
                ApiLogger.warn("[JedisConnectionFactoryBuilder] " +
                        "Connection String for PORT %s is blank, " +
                        "using '%s:%s:%s:%s:%s' as default.", i, connStrTokens);
            } else {
                String[] detailTokens = portToken.split("\\:");
                if (detailTokens.length > connStrTokens.length) {
                    throw new IllegalStateException("[JedisConnectionFactoryBuilder] Connection String in Segment " + i + "is not in valid format!");
                }
                for (int j = 0; j < detailTokens.length; j++) {
                    String token = detailTokens[j];
                    if (StringUtils.isNotBlank(token)) {
                        connStrTokens[j] = token;
                    } else {
                        ApiLogger.info("[WARN] token %s in port %s is blank, using %s as default value", j, i, connStrTokens[j]);
                    }
                }
            }
            System.out.println(Arrays.toString(connStrTokens));
            JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
            jedisConnectionFactory.setHostName(connStrTokens[0]);
            jedisConnectionFactory.setPort(Integer.valueOf(connStrTokens[1]));
            jedisConnectionFactory.setDatabase(Integer.valueOf(connStrTokens[2]));
            jedisConnectionFactory.setPassword(StringUtils.isBlank(connStrTokens[3])?null:connStrTokens[3]);
            jedisConnectionFactory.setTimeout(Integer.valueOf(connStrTokens[4]));
            if (usePool != null) {
                jedisConnectionFactory.setUsePool(usePool);
            }
            if (jedisPoolConfig != null) {
                jedisConnectionFactory.setPoolConfig(jedisPoolConfig);
            }
            jedisConnectionFactories.add(jedisConnectionFactory);
            i++;
        }
        return jedisConnectionFactories;
    }

}
