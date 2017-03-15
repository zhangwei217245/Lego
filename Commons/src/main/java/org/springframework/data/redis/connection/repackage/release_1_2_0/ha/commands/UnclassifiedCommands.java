package org.springframework.data.redis.connection.repackage.release_1_2_0.ha.commands;

import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.repackage.release_1_2_0.ha.EnhancedRedisConnection;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhangwei on 14-5-9.
 *
 * @author zhangwei
 */
@Deprecated
public interface UnclassifiedCommands extends EnhancedRedisConnection {

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

    @Override
    Boolean move(byte[] key, int dbIndex);

    @Override
    Boolean persist(byte[] key);

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
    Double zIncrBy(byte[] key, double increment, byte[] value);

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

    //masterReadCommands

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

    // master write commands

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


    //slave read commands


    // read commands for hashes

    @Override
    byte[] hGet(byte[] key, byte[] field);

    @Override
    List<byte[]> hMGet(byte[] key, byte[]... fields);

    @Override
    Set<byte[]> hKeys(byte[] key);

    @Override
    List<byte[]> hVals(byte[] key);

    @Override
    Long hLen(byte[] key);

    @Override
    Map<byte[], byte[]> hGetAll(byte[] key);

    // read commands for lists

    @Override
    List<byte[]> lRange(byte[] key, long begin, long end);

    @Override
    Long lLen(byte[] key);

    @Override
    byte[] lIndex(byte[] key, long index);

    //read commands for sets

    @Override
    Long sCard(byte[] key);

    @Override
    Set<byte[]> sInter(byte[]... keys);

    @Override
    Set<byte[]> sUnion(byte[]... keys);

    @Override
    Set<byte[]> sDiff(byte[]... keys);

    @Override
    Set<byte[]> sMembers(byte[] key);

    @Override
    byte[] sRandMember(byte[] key);

    @Override
    List<byte[]> sRandMember(byte[] key, long count);

    //read commands for strings

    @Override
    byte[] get(byte[] key);

    @Override
    List<byte[]> mGet(byte[]... keys);

    @Override
    byte[] getRange(byte[] key, long begin, long end);

    @Override
    Boolean getBit(byte[] key, long offset);

    @Override
    Long bitCount(byte[] key);

    @Override
    Long bitCount(byte[] key, long begin, long end);

    @Override
    Long strLen(byte[] key);

    //redis commands for zsets

    @Override
    Long zRevRank(byte[] key, byte[] value);

    @Override
    Long zRank(byte[] key, byte[] value);

    @Override
    Set<byte[]> zRange(byte[] key, long begin, long end);

    @Override
    Set<Tuple> zRangeWithScores(byte[] key, long begin, long end);

    @Override
    Set<byte[]> zRangeByScore(byte[] key, double min, double max);

    @Override
    Set<Tuple> zRangeByScoreWithScores(byte[] key, double min, double max);

    @Override
    Set<byte[]> zRangeByScore(byte[] key, double min, double max, long offset, long count);

    @Override
    Set<Tuple> zRangeByScoreWithScores(byte[] key, double min, double max, long offset, long count);

    @Override
    Set<byte[]> zRevRange(byte[] key, long begin, long end);

    @Override
    Set<Tuple> zRevRangeWithScores(byte[] key, long begin, long end);

    @Override
    Set<byte[]> zRevRangeByScore(byte[] key, double min, double max);

    @Override
    Set<Tuple> zRevRangeByScoreWithScores(byte[] key, double min, double max);

    @Override
    Set<byte[]> zRevRangeByScore(byte[] key, double min, double max, long offset, long count);

    @Override
    Set<Tuple> zRevRangeByScoreWithScores(byte[] key, double min, double max, long offset, long count);

    @Override
    Long zCount(byte[] key, double min, double max);

    @Override
    Long zCard(byte[] key);

    @Override
    Double zScore(byte[] key, byte[] value);



    //unclassifiedCommands




}
