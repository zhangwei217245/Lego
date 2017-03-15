package org.springframework.data.redis.connection.repackage.release_1_2_0.ha.commands;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.repackage.release_1_2_0.ha.EnhancedRedisConnection;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhangwei on 14-5-7.
 * This is an interface for redis read commands which should be a request to the slaves in redis HA architecture.
 *
 * @author zhangwei
 */
public interface SlaveReadCommands extends EnhancedRedisConnection {

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
}
