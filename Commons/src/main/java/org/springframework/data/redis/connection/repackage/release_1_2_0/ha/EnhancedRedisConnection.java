package org.springframework.data.redis.connection.repackage.release_1_2_0.ha;

import org.springframework.data.redis.connection.RedisConnection;

/**
 * Created by zhangwei on 14-5-22.
 *
 * for locking support only,
 * cause there may be certain delay while GET is issued on slaves after SETNX is issued on master.
 *
 * @author zhangwei
 */
public interface EnhancedRedisConnection extends RedisConnection{

    byte[] getFromMaster(byte[] key);
}
