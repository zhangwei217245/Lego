package org.springframework.data.redis.connection.repackage.release_1_2_0.ha;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConverters;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

/**
 * Created by zhangwei on 14-5-8.
 *
 * @author zhangwei
 */
public class JedisHAProxy extends RedisHAProxy {


    private boolean usePool = true;
    private JedisPoolConfig poolConfig = new JedisPoolConfig();

    @Override
    List<RedisConnectionFactory> buildConnectionFactories(String connectionString) {
        List<RedisConnectionFactory> redisConnectionFactories = RedisConnectionFactoryBuilder
                .buildJedisConnectionFactories(connectionString, this.isUsePool(), this.getPoolConfig());
        return redisConnectionFactories;
    }

    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
        return JedisConverters.toDataAccessException(ex);
    }

    public boolean isUsePool() {
        return usePool;
    }

    public void setUsePool(boolean usePool) {
        this.usePool = usePool;
    }

    public JedisPoolConfig getPoolConfig() {
        return poolConfig;
    }

    public void setPoolConfig(JedisPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }


}
