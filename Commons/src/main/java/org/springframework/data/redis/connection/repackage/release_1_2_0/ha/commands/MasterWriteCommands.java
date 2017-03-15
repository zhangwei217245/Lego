package org.springframework.data.redis.connection.repackage.release_1_2_0.ha.commands;

import org.springframework.data.redis.connection.repackage.release_1_2_0.ha.EnhancedRedisConnection;

import java.util.List;
import java.util.Map;

/**
 * Created by zhangwei on 14-5-7.
 *
 * This is an interface holding all writing commands for only master.
 * @author zhangwei
 */
public interface MasterWriteCommands extends EnhancedRedisConnection {

    @Override
    Long hIncrBy(byte[] key, byte[] field, long delta);

    @Override
    Double hIncrBy(byte[] key, byte[] field, double delta);

    @Override
    Boolean hSetNX(byte[] key, byte[] field, byte[] value);

    @Override
    Boolean renameNX(byte[] oldName, byte[] newName);

    @Override
    void rename(byte[] oldName, byte[] newName);

    @Override
    Long rPushX(byte[] key, byte[] value);

    @Override
    Long lPushX(byte[] key, byte[] value);

    @Override
    List<byte[]> bLPop(int timeout, byte[]... keys);

    @Override
    List<byte[]> bRPop(int timeout, byte[]... keys);

    @Override
    byte[] bRPopLPush(int timeout, byte[] srcKey, byte[] dstKey);

    @Override
    Boolean setNX(byte[] key, byte[] value);

    @Override
    void setEx(byte[] key, long seconds, byte[] value);

    @Override
    byte[] getSet(byte[] key, byte[] value);

    @Override
    Boolean mSetNX(Map<byte[], byte[]> tuple);

    @Override
    Long incr(byte[] key);

    @Override
    Long incrBy(byte[] key, long value);

    @Override
    Double incrBy(byte[] key, double value);

    @Override
    Long decrBy(byte[] key, long value);

    @Override
    Long decr(byte[] key);

    @Override
    Double zIncrBy(byte[] key, double increment, byte[] value);

    @Override
    Boolean move(byte[] key, int dbIndex);

    @Override
    Boolean persist(byte[] key);

}
