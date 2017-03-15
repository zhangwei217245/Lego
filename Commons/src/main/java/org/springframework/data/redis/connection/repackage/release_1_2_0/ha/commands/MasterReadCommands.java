package org.springframework.data.redis.connection.repackage.release_1_2_0.ha.commands;

import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.repackage.release_1_2_0.ha.EnhancedRedisConnection;

import java.util.Set;

/**
 * Created by zhangwei on 14-5-7.
 * This is an interface holding all the read commands for only master.
 * @author zhangwei
 */
public interface MasterReadCommands extends EnhancedRedisConnection {

    // master read for redis key
    @Override
    DataType type(byte[] key);

    @Override
    Boolean exists(byte[] key);

    @Override
    Set<byte[]> keys(byte[] pattern);

    @Override
    Long ttl(byte[] key);

    @Override
    Long pTtl(byte[] key);

    //master read for redis hashes

    @Override
    Boolean hExists(byte[] key, byte[] field);

    // master read for redis sets
    @Override
    Boolean sIsMember(byte[] key, byte[] value);


    @Override
    byte[] getFromMaster(byte[] key);
}
