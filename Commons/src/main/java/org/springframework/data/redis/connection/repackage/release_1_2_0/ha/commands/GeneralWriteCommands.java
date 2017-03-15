package org.springframework.data.redis.connection.repackage.release_1_2_0.ha.commands;

import org.springframework.data.redis.connection.repackage.release_1_2_0.ha.EnhancedRedisConnection;

import java.util.Map;
import java.util.Set;

/**
 * Created by zhangwei on 14-5-7.
 *
 * This is an interface holding all write commands that should be invoked on master and all its slaves.
 * @author zhangwei
 */
public interface GeneralWriteCommands extends EnhancedRedisConnection{

    // general write commands for hashes

    @Override
    Boolean hSet(byte[] key, byte[] field, byte[] value);

    @Override
    void hMSet(byte[] key, Map<byte[], byte[]> hashes);

    @Override
    Long hDel(byte[] key, byte[]... fields);

    // general write commands for redis key


    @Override
    Long del(byte[]... keys);

    @Override
    Boolean expire(byte[] key, long seconds);

    @Override
    Boolean pExpire(byte[] key, long millis);

    @Override
    Boolean expireAt(byte[] key, long unixTime);

    @Override
    Boolean pExpireAt(byte[] key, long unixTimeInMillis);

    // general write commands for lists

    @Override
    Long rPush(byte[] key, byte[]... values);

    @Override
    Long lPush(byte[] key, byte[]... value);

    @Override
    void lTrim(byte[] key, long begin, long end);

    @Override
    Long lInsert(byte[] key, Position where, byte[] pivot, byte[] value);

    @Override
    void lSet(byte[] key, long index, byte[] value);

    @Override
    Long lRem(byte[] key, long count, byte[] value);

    @Override
    byte[] lPop(byte[] key);

    @Override
    byte[] rPop(byte[] key);

    @Override
    byte[] rPopLPush(byte[] srcKey, byte[] dstKey);


    //general write commands for sets

    @Override
    Long sAdd(byte[] key, byte[]... values);

    @Override
    Long sRem(byte[] key, byte[]... values);

    @Override
    byte[] sPop(byte[] key);

    @Override
    Boolean sMove(byte[] srcKey, byte[] destKey, byte[] value);

    @Override
    Long sInterStore(byte[] destKey, byte[]... keys);

    @Override
    Long sUnionStore(byte[] destKey, byte[]... keys);

    @Override
    Long sDiffStore(byte[] destKey, byte[]... keys);

    //general write commands for strings

    @Override
    void set(byte[] key, byte[] value);

    @Override
    void mSet(Map<byte[], byte[]> tuple);

    @Override
    void setRange(byte[] key, byte[] value, long offset);

    @Override
    void setBit(byte[] key, long offset, boolean value);

    @Override
    Long bitOp(BitOperation op, byte[] destination, byte[]... keys);

    @Override
    Long append(byte[] key, byte[] value);

    //general write commands for zsets

    @Override
    Boolean zAdd(byte[] key, double score, byte[] value);

    @Override
    Long zAdd(byte[] key, Set<Tuple> tuples);

    @Override
    Long zRem(byte[] key, byte[]... values);

    @Override
    Long zRemRange(byte[] key, long begin, long end);

    @Override
    Long zRemRangeByScore(byte[] key, double min, double max);

    @Override
    Long zUnionStore(byte[] destKey, byte[]... sets);

    @Override
    Long zUnionStore(byte[] destKey, Aggregate aggregate, int[] weights, byte[]... sets);

    @Override
    Long zInterStore(byte[] destKey, byte[]... sets);

    @Override
    Long zInterStore(byte[] destKey, Aggregate aggregate, int[] weights, byte[]... sets);
}
