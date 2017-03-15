/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mr.x.commons.redis.jedis;

import mr.x.commons.utils.ApiLogger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.repackage.release_1_2_0.ha.EnhancedRedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.support.atomic.RedisAtomicDouble;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.data.redis.support.collections.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author zhangwei
 */
public class DefaultLegoSpringRedisHandler<HK, HV> implements LegoSpringRedisHandler<HK, HV> {

    RedisTemplate<String, HV> redisTemplate;
    private final OperationsFactory operationsFactory = new DefaultRedisOperationsFactory();
    private final StringOperationsFactory stringOperationsFactory = new DefaultStringRedisOperationFactory();
    private final RedisCollectionsFactory redisCollectionsFactory = new DefaultRedisCollectionsFactory();
    private final RedisAtomicFactory redisAtomicFactory = new DefaultRedisAtomicFactory();
    private final RedisCommandsFactory redisCommandsFactory = new DefaultRedisCommandsFactory();
    private final Lock lock = new DefaultDistributionLock();

    public DefaultLegoSpringRedisHandler(RedisTemplate<String, HV> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RedisTemplate<String, HV> getRedisTemplate() {
        return this.redisTemplate;
    }

    @Override
    public StringRedisTemplate getStringRedisTemplate() {
        return new StringRedisTemplate(this.redisTemplate.getConnectionFactory());
    }

    @Override
    public OperationsFactory operations(){
        return this.operationsFactory;
    }

    @Override
    public StringOperationsFactory stringOperations() {
        return this.stringOperationsFactory;
    }

    @Override
    public RedisCollectionsFactory collections() {
        return this.redisCollectionsFactory;
    }

    @Override
    public RedisAtomicFactory atomics() {
        return this.redisAtomicFactory;
    }

    @Override
    public RedisCommandsFactory commands() {
        return this.redisCommandsFactory;
    }

    @Override
    public Lock lockingSupport() {
        return this.lock;
    }

    class DefaultRedisCommandsFactory implements RedisCommandsFactory {
        private final RedisCommands redisCommands = (RedisCommands)Proxy.newProxyInstance(RedisCommands.class.getClassLoader(), new Class<?>[]{RedisCommands.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        return redisTemplate.execute(new RedisCallback<Object>() {
                            @Override
                            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                                try {
                                    return method.invoke(connection, args);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    ApiLogger.warn(e, "%s occurred! method={%s}.%s, args=%s",
                                            e.getClass().getSimpleName(), connection.toString(), method.getName(), Arrays.toString(args));
                                    return null;
                                }
                            }
                        });
                    }
                });



        public RedisCommands getCommands() {
            return this.redisCommands;
        }

        @Override
        public RedisKeyCommands forKeys() {
            return getCommands();
        }

        @Override
        public RedisStringCommands forStrings() {
            return getCommands();
        }

        @Override
        public RedisListCommands forList() {
            return getCommands();
        }

        @Override
        public RedisHashCommands forHash() {
            return getCommands();
        }

        @Override
        public RedisSetCommands forSet() {
            return getCommands();
        }

        @Override
        public RedisZSetCommands forZSet() {
            return getCommands();
        }

        @Override
        public RedisServerCommands forServer() {
            return getCommands();
        }

        @Override
        public RedisPubSubCommands forPubSub() {
            return getCommands();
        }

        @Override
        public RedisConnectionCommands forConnection() {
            return getCommands();
        }

        @Override
        public RedisTxCommands forTransaction() {
            return getCommands();
        }

        @Override
        public RedisScriptingCommands forScripting() {
            return getCommands();
        }
    }

    class DefaultRedisAtomicFactory implements RedisAtomicFactory {

        @Override
        public RedisAtomicInteger atomicInteger(String key) {
            return new RedisAtomicInteger(key, redisTemplate.getConnectionFactory());
        }

        @Override
        public RedisAtomicLong atomicLong(String key) {
            return new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        }

        @Override
        public RedisAtomicDouble atomicDouble(String key) {
            return new RedisAtomicDouble(key, redisTemplate.getConnectionFactory());
        }

        @Override
        public void deleteKey(String key) {
            redisTemplate.delete(key);
        }

        @Override
        public void deleteMultiKeys (Collection<String> keys){
            redisTemplate.delete(keys);
        }

        @Override
        public Boolean expire(String key, long timeout, TimeUnit timeUnit) {
            return redisTemplate.expire(key, timeout, timeUnit);
        }

        @Override
        public Boolean expireAt(String key, Date date) {
            return redisTemplate.expireAt(key ,date);
        }

        @Override
        public byte[] dump(String key) {
            return redisTemplate.dump(key);
        }

        @Override
        public void restore(String key, byte[] data, long timeToLive, TimeUnit timeUnit) {
            redisTemplate.restore(key, data, timeToLive, timeUnit);
        }

        @Override
        public Long getExpire(String key) {
            return redisTemplate.getExpire(key);
        }

        @Override
        public Long getExpire(String key, TimeUnit timeUnit) {
            return redisTemplate.getExpire(key, timeUnit);
        }

        @Override
        public Boolean hasKey(String key) {
            return redisTemplate.hasKey(key);
        }

        @Override
        public Set<String> keys(String pattern) {
            return redisTemplate.keys(pattern);
        }

        @Override
        public Boolean move(String key, int dbIndex) {
            return redisTemplate.move(key, dbIndex);
        }

        @Override
        public Boolean persist(String key) {
            return redisTemplate.persist(key);
        }

        @Override
        public void rename(String oldKey, String newKey) {
            redisTemplate.rename(oldKey, newKey);
        }

        @Override
        public Boolean renameIfAbsent(String oldKey, String newKey) {
            return redisTemplate.renameIfAbsent(oldKey, newKey);
        }

        @Override
        public String randomKey() {
            return redisTemplate.randomKey();
        }

        @Override
        public DataType type(String key) {
            return redisTemplate.type(key);
        }
    }

    class DefaultRedisCollectionsFactory implements RedisCollectionsFactory<HK, HV> {
        @Override
        public RedisList<HV> redisList(String key) {
            return new DefaultRedisList<>(redisTemplate.boundListOps(key));
        }

        @Override
        public RedisSet<HV> redisSet(String key) {
            return new DefaultRedisSet<>(redisTemplate.boundSetOps(key));
        }

        @Override
        public RedisZSet<HV> redisSortedSet(String key) {
            return new DefaultRedisZSet<>(redisTemplate.boundZSetOps(key));
        }

        @Override
        public RedisMap<HK, HV> redisMap(String key) {
            return new DefaultRedisMap<>(key, redisTemplate);
        }

        @Override
        public void deleteKey(String key) {
            redisTemplate.delete(key);
        }

        @Override
        public void deleteMultiKeys (Collection<String> keys){
            redisTemplate.delete(keys);
        }

        @Override
        public Boolean expire(String key, long timeout, TimeUnit timeUnit) {
            return redisTemplate.expire(key, timeout, timeUnit);
        }

        @Override
        public Boolean expireAt(String key, Date date) {
            return redisTemplate.expireAt(key ,date);
        }

        @Override
        public byte[] dump(String key) {
            return redisTemplate.dump(key);
        }

        @Override
        public void restore(String key, byte[] data, long timeToLive, TimeUnit timeUnit) {
            redisTemplate.restore(key, data, timeToLive, timeUnit);
        }

        @Override
        public Long getExpire(String key) {
            return redisTemplate.getExpire(key);
        }

        @Override
        public Long getExpire(String key, TimeUnit timeUnit) {
            return redisTemplate.getExpire(key, timeUnit);
        }

        @Override
        public Boolean hasKey(String key) {
            return redisTemplate.hasKey(key);
        }

        @Override
        public Set<String> keys(String pattern) {
            return redisTemplate.keys(pattern);
        }

        @Override
        public Boolean move(String key, int dbIndex) {
            return redisTemplate.move(key, dbIndex);
        }

        @Override
        public Boolean persist(String key) {
            return redisTemplate.persist(key);
        }

        @Override
        public void rename(String oldKey, String newKey) {
            redisTemplate.rename(oldKey, newKey);
        }

        @Override
        public Boolean renameIfAbsent(String oldKey, String newKey) {
            return redisTemplate.renameIfAbsent(oldKey, newKey);
        }

        @Override
        public String randomKey() {
            return redisTemplate.randomKey();
        }

        @Override
        public DataType type(String key) {
            return redisTemplate.type(key);
        }
    }

    class DefaultRedisOperationsFactory implements OperationsFactory<HK, HV>{
        @Override
        public ValueOperations<String, HV> VALUES(){
            return redisTemplate.opsForValue();
        }
        @Override
        public ListOperations<String, HV> LIST() {
            return redisTemplate.opsForList();
        }
        @Override
        public SetOperations<String, HV> SET() {
            return redisTemplate.opsForSet();
        }
        @Override
        public ZSetOperations<String, HV> ZSET() {
            return redisTemplate.opsForZSet();
        }
        @Override
        public HashOperations<String, HK, HV> HASH() {
            return redisTemplate.opsForHash();
        }

        @Override
        public void deleteKey(String key) {
            redisTemplate.delete(key);
        }

        @Override
        public void deleteMultiKeys (Collection<String> keys){
            redisTemplate.delete(keys);
        }

        @Override
        public Boolean expire(String key, long timeout, TimeUnit timeUnit) {
            return redisTemplate.expire(key, timeout, timeUnit);
        }

        @Override
        public Boolean expireAt(String key, Date date) {
            return redisTemplate.expireAt(key ,date);
        }

        @Override
        public byte[] dump(String key) {
            return redisTemplate.dump(key);
        }

        @Override
        public void restore(String key, byte[] data, long timeToLive, TimeUnit timeUnit) {
            redisTemplate.restore(key, data, timeToLive, timeUnit);
        }

        @Override
        public Long getExpire(String key) {
            return redisTemplate.getExpire(key);
        }

        @Override
        public Long getExpire(String key, TimeUnit timeUnit) {
            return redisTemplate.getExpire(key, timeUnit);
        }

        @Override
        public Boolean hasKey(String key) {
            return redisTemplate.hasKey(key);
        }

        @Override
        public Set<String> keys(String pattern) {
            return redisTemplate.keys(pattern);
        }

        @Override
        public Boolean move(String key, int dbIndex) {
            return redisTemplate.move(key, dbIndex);
        }

        @Override
        public Boolean persist(String key) {
            return redisTemplate.persist(key);
        }

        @Override
        public void rename(String oldKey, String newKey) {
            redisTemplate.rename(oldKey, newKey);
        }

        @Override
        public Boolean renameIfAbsent(String oldKey, String newKey) {
            return redisTemplate.renameIfAbsent(oldKey, newKey);
        }

        @Override
        public String randomKey() {
            return redisTemplate.randomKey();
        }

        @Override
        public DataType type(String key) {
            return redisTemplate.type(key);
        }
    }

    class DefaultStringRedisOperationFactory implements StringOperationsFactory {

        @Override
        public ValueOperations<String, String> VALUES() {
            return getStringRedisTemplate().opsForValue();
        }

        @Override
        public ListOperations<String, String> LIST() {
            return getStringRedisTemplate().opsForList();
        }

        @Override
        public SetOperations<String, String> SET() {
            return getStringRedisTemplate().opsForSet();
        }

        @Override
        public ZSetOperations<String, String> ZSET() {
            return getStringRedisTemplate().opsForZSet();
        }

        @Override
        public HashOperations<String, String, String> HASH() {
            return getStringRedisTemplate().opsForHash();
        }

        @Override
        public void deleteKey(String key) {
            getStringRedisTemplate().delete(key);
        }

        @Override
        public void deleteMultiKeys (Collection<String> keys){
            getStringRedisTemplate().delete(keys);
        }

        @Override
        public Boolean expire(String key, long timeout, TimeUnit timeUnit) {
            return getStringRedisTemplate().expire(key, timeout, timeUnit);
        }

        @Override
        public Boolean expireAt(String key, Date date) {
            return getStringRedisTemplate().expireAt(key, date);
        }

        @Override
        public byte[] dump(String key) {
            return getStringRedisTemplate().dump(key);
        }

        @Override
        public void restore(String key, byte[] data, long timeToLive, TimeUnit timeUnit) {
            getStringRedisTemplate().restore(key, data, timeToLive, timeUnit);
        }

        @Override
        public Long getExpire(String key) {
            return getStringRedisTemplate().getExpire(key);
        }

        @Override
        public Long getExpire(String key, TimeUnit timeUnit) {
            return getStringRedisTemplate().getExpire(key, timeUnit);
        }

        @Override
        public Boolean hasKey(String key) {
            return getStringRedisTemplate().hasKey(key);
        }

        @Override
        public Set<String> keys(String pattern) {
            return getStringRedisTemplate().keys(pattern);
        }

        @Override
        public Boolean move(String key, int dbIndex) {
            return getStringRedisTemplate().move(key, dbIndex);
        }

        @Override
        public Boolean persist(String key) {
            return getStringRedisTemplate().persist(key);
        }

        @Override
        public void rename(String oldKey, String newKey) {
            getStringRedisTemplate().rename(oldKey, newKey);
        }

        @Override
        public Boolean renameIfAbsent(String oldKey, String newKey) {
            return getStringRedisTemplate().renameIfAbsent(oldKey, newKey);
        }

        @Override
        public String randomKey() {
            return getStringRedisTemplate().randomKey();
        }

        @Override
        public DataType type(String key) {
            return getStringRedisTemplate().type(key);
        }
    }

    class DefaultDistributionLock implements Lock {



        String getLockKey(String originalKey) {
            return originalKey + ".lock";
        }
        @Override
        public boolean lock(String key, long timeout, TimeUnit unit) {
            boolean lockAcqired = false;
            int retried = 0;
            while (retried < DEFAULT_LOCKING_ACQUIRE_RETRIES) {
                lockAcqired = acquireLock(key, timeout, unit);
                if (lockAcqired) {
                    break;
                }
                try {
                    Thread.sleep(DEFAULT_LOCKING_TIMEOUT_MILLIS);
                } catch (InterruptedException e) {
                    ApiLogger.warn(e, "Interrupted Exception Occurred While trying to lock a key: '%s' in redis", key);
                } finally {
                    retried++;
                }
            }
            return lockAcqired;
        }

        private boolean acquireLock(String key, long timeout, TimeUnit unit) {
            boolean holdsLock = false;
            long currMillis = System.currentTimeMillis();
            long millis = unit.toMillis(timeout);

            final ValueOperations<String, Long> ops = operations().VALUES();

            long nextTimeout = (currMillis + millis + 1L);
            try {
                final String lockKey = getLockKey(key);
                holdsLock = ops.setIfAbsent(lockKey, nextTimeout);

                if (!holdsLock) {

                    final RedisSerializer<String> keySerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
                    final RedisSerializer<Long> valueSerializer = (RedisSerializer<Long>)redisTemplate.getValueSerializer();
                    Long lockValidTimestamp = (Long)redisTemplate.execute(new RedisCallback<Object>() {
                        @Override
                        public Object doInRedis(RedisConnection connection) throws DataAccessException {
                            if (connection instanceof EnhancedRedisConnection) {

                                byte[] val = ((EnhancedRedisConnection)connection).getFromMaster(keySerializer.serialize(lockKey));
                                return valueSerializer.deserialize(val);
                            }
                            return null;
                        }
                    }, true);
                    if (lockValidTimestamp == null) {
                        return false;
                    }
                    if (currMillis > lockValidTimestamp) {// lock is not valid, try to modify timestamp and acquire it!
                        if (currMillis > ops.getAndSet(lockKey, nextTimeout)) {// test if the lock is still invalid
                            holdsLock = true;
                        }
                    }
                } else {
                    holdsLock = false;
                }

            } catch (Throwable t) {
                ApiLogger.warn(t, "redis lock for key:%s has failed due to exception", key);
                holdsLock = false;
            }
            return holdsLock;
        }


        @Override
        public boolean unlock(String key) {
            boolean lockReleased = false;
            long currMillis = System.currentTimeMillis();
            try {
                final String lockKey = getLockKey(key);
                final RedisSerializer<String> keySerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
                final RedisSerializer<Long> valueSerializer = (RedisSerializer<Long>)redisTemplate.getValueSerializer();
                Long lockValidTimestamp = (Long)redisTemplate.execute(new RedisCallback<Object>() {
                    @Override
                    public Object doInRedis(RedisConnection connection) throws DataAccessException {
                        if (connection instanceof EnhancedRedisConnection) {

                            byte[] val = ((EnhancedRedisConnection)connection).getFromMaster(keySerializer.serialize(lockKey));
                            return valueSerializer.deserialize(val);
                        }
                        return null;
                    }
                }, true);
                if (lockValidTimestamp == null) {
                    operations().expire(lockKey, 2000, TimeUnit.MILLISECONDS);
                    return false;
                }
                if (currMillis < lockValidTimestamp) {
                    operations().deleteKey(lockKey);
                    lockReleased = true;
                }
            } catch (Throwable t) {
                ApiLogger.warn(t, "redis lock for key:%s has failed due to exception", key);
                lockReleased = false;
            }
            return lockReleased;
        }
    }

}
