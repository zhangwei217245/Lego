/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mr.x.commons.redis.jedis;


import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.atomic.RedisAtomicDouble;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.data.redis.support.collections.RedisSet;
import org.springframework.data.redis.support.collections.RedisZSet;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 该类用于创建各种SpringRedis工具类实例。
 * 
 * 涵盖spring-data-redis提供的所有特性：
 * 1. redisTemplate
 * 2. stringRedisTemplate
 * 3. operations (including operations for redis data structures like k-v, list, set, zset, hash)
 * 4. collections (including list, set, sortedset/zset, map/hash)
 * 5. atomics (including atomicInteger, atomicLong and atomicDouble)
 * 6. commands (including commands for keys, strings, list, set, zset, hash, pubsub, scripting, server, connection, etc.)
 * 
 * @author zhangwei
 */
public interface LegoSpringRedisHandler<HK, HV> {

    public static final Long DEFAULT_LOCKING_TIMEOUT_MILLIS = 1000L;

    public static final int DEFAULT_LOCKING_ACQUIRE_RETRIES = 10;
    
    public RedisTemplate<String, HV> getRedisTemplate();
    
    public StringRedisTemplate getStringRedisTemplate();

    public OperationsFactory operations();

    public StringOperationsFactory stringOperations();

    public RedisCollectionsFactory collections();

    public RedisAtomicFactory atomics();

    public RedisCommandsFactory commands();

    public Lock lockingSupport();

    interface RedisCommandsFactory {

        public RedisKeyCommands forKeys();

        public RedisStringCommands forStrings();

        public RedisListCommands forList();

        public RedisHashCommands forHash();

        public RedisSetCommands forSet();

        public RedisZSetCommands forZSet();

        public RedisServerCommands forServer();

        public RedisPubSubCommands forPubSub();

        public RedisConnectionCommands forConnection();

        public RedisTxCommands forTransaction();

        public RedisScriptingCommands forScripting();
    }

    interface RedisAtomicFactory {
        public RedisAtomicInteger atomicInteger(String key);
        public RedisAtomicLong atomicLong(String key);
        public RedisAtomicDouble atomicDouble(String key);
        public void deleteKey(String key);
        public void deleteMultiKeys (Collection<String> keys);
        public Boolean expire(String key, long timeout, TimeUnit timeUnit);
        public Boolean expireAt(String key, Date date);
        public byte[] dump(String key);
        public void restore(String key, byte[] data, long timeToLive, TimeUnit timeUnit);
        public Long getExpire(String key);
        public Long getExpire(String key, TimeUnit timeUnit);
        public Boolean hasKey(String key);
        public Set<String> keys(String pattern);
        public Boolean move(String key, int dbIndex);
        public Boolean persist(String key);
        public void rename(String oldKey, String newKey);
        public Boolean renameIfAbsent(String oldKey, String newKey);
        public String randomKey();
        public DataType type(String key);
    }

    interface RedisCollectionsFactory<HK, HV> {
        public RedisList<HV> redisList(String key);
        public RedisSet<HV> redisSet(String key);
        public RedisZSet<HV> redisSortedSet(String key);
        public RedisMap<HK, HV> redisMap(String key);
        public void deleteKey(String key);
        public void deleteMultiKeys (Collection<String> keys);
        public Boolean expire(String key, long timeout, TimeUnit timeUnit);
        public Boolean expireAt(String key, Date date);
        public byte[] dump(String key);
        public void restore(String key, byte[] data, long timeToLive, TimeUnit timeUnit);
        public Long getExpire(String key);
        public Long getExpire(String key, TimeUnit timeUnit);
        public Boolean hasKey(String key);
        public Set<String> keys(String pattern);
        public Boolean move(String key, int dbIndex);
        public Boolean persist(String key);
        public void rename(String oldKey, String newKey);
        public Boolean renameIfAbsent(String oldKey, String newKey);
        public String randomKey();
        public DataType type(String key);
    }

    interface StringOperationsFactory {
        public ValueOperations<java.lang.String, String> VALUES();
        public ListOperations<java.lang.String, String> LIST();
        public SetOperations<java.lang.String, String> SET();
        public ZSetOperations<java.lang.String, String> ZSET();
        public HashOperations<java.lang.String, String, String> HASH();
        public void deleteKey(String key);
        public void deleteMultiKeys (Collection<String> keys);
        public Boolean expire(String key, long timeout, TimeUnit timeUnit);
        public Boolean expireAt(String key, Date date);
        public byte[] dump(String key);
        public void restore(String key, byte[] data, long timeToLive, TimeUnit timeUnit);
        public Long getExpire(String key);
        public Long getExpire(String key, TimeUnit timeUnit);
        public Boolean hasKey(String key);
        public Set<String> keys(String pattern);
        public Boolean move(String key, int dbIndex);
        public Boolean persist(String key);
        public void rename(String oldKey, String newKey);
        public Boolean renameIfAbsent(String oldKey, String newKey);
        public String randomKey();
        public DataType type(String key);
    }

    interface OperationsFactory<HK, HV> {
        public ValueOperations<String, HV> VALUES();
        public ListOperations<String, HV> LIST();
        public SetOperations<String, HV> SET();
        public ZSetOperations<String, HV> ZSET();
        public HashOperations<String, HK, HV> HASH();
        public void deleteKey(String key);
        public void deleteMultiKeys (Collection<String> keys);
        public Boolean expire(String key, long timeout, TimeUnit timeUnit);
        public Boolean expireAt(String key, Date date);
        public byte[] dump(String key);
        public void restore(String key, byte[] data, long timeToLive, TimeUnit timeUnit);
        public Long getExpire(String key);
        public Long getExpire(String key, TimeUnit timeUnit);
        public Boolean hasKey(String key);
        public Set<String> keys(String pattern);
        public Boolean move(String key, int dbIndex);
        public Boolean persist(String key);
        public void rename(String oldKey, String newKey);
        public Boolean renameIfAbsent(String oldKey, String newKey);
        public String randomKey();
        public DataType type(String key);
    }

    interface Lock {
        public boolean lock(String key, long timeout, TimeUnit unit);
        public boolean unlock(String key);
    }
    
}
