package org.springframework.data.redis.connection.repackage.release_1_2_0.ha;

import mr.x.commons.utils.ApiLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.repackage.release_1_2_0.ha.commands.GeneralWriteCommands;
import org.springframework.data.redis.connection.repackage.release_1_2_0.ha.commands.MasterReadCommands;
import org.springframework.data.redis.connection.repackage.release_1_2_0.ha.commands.MasterWriteCommands;
import org.springframework.data.redis.connection.repackage.release_1_2_0.ha.commands.SlaveReadCommands;
import org.springframework.data.redis.core.RedisConnectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhangwei on 14-5-7.
 *
 * @author zhangwei
 */
public abstract class RedisHAProxy implements InvocationHandler, InitializingBean, DisposableBean, RedisConnectionFactory {

    static final Set<String> generalWriteCommands = new HashSet<>();
    static final Set<String> slaveReadCommands = new HashSet<>();
    static final Set<String> masterReadCommands = new HashSet<>();
    static final Set<String> masterWriteCommands = new HashSet<>();
    static final Set<String> connectionCommands = new HashSet<>();

    static {
        initCommands(GeneralWriteCommands.class, generalWriteCommands);
        initCommands(SlaveReadCommands.class, slaveReadCommands);
        initCommands(MasterReadCommands.class, masterReadCommands);
        initCommands(MasterWriteCommands.class, masterWriteCommands);
        initCommands(RedisConnection.class, connectionCommands);
    }

    private AtomicInteger slaveCursor = new AtomicInteger(0);

    private static void initCommands(Class clazz, Set<String> commandsMethods) {
        Method[] methods = clazz.getDeclaredMethods();
        System.out.println("[RedisHAProxy] ====== Loading "+clazz.getSimpleName()+" ... =======");
        for (Method m : methods) {
            String sig = getMethodSingnature(m);
            System.out.println(String.format("[RedisHAProxy] Loading %s",sig));
            commandsMethods.add(sig);
        }
    }

    // hostname:port:dbIndex:password:timeoutMillis

    String connectionString = "127.0.0.1:6379:0::10000";

    List<RedisConnectionFactory> connectionFactories = new ArrayList<>();

    boolean debugMode;

    boolean usingMasterForRead;

    boolean convertPipelineAndTxResults = true;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (isConnectionCommand(method)) {
            return doConnectionOperation(method, args);
        } else if (isGeneralWriteCommand(method)) {
            return doGeneralWrite(method, args);
        } else if (isSlaveReadCommand(method)) {
            return doSlaveRead(method, args);
        } else if (isMasterWriteCommand(method)) {
            return doMasterOperation(method, args);
        } else if (isMasterReadCommand(method)) {
            return doMasterOperation(method, args);
        } else {
            return doMasterOperation(method, args);
        }
    }

    private Object doGeneralWrite(Method method, Object[] args) throws Throwable {
        Object rst = doMasterOperation(method, args);

        Map<Integer, Object> rstMap = null;

        if (isDebugMode()) {
            rstMap = new HashMap<>();
        }

        for (int i = 1; i < connectionFactories.size(); i++) {
            RedisConnectionFactory slaveConnFactory = connectionFactories.get(i);
            RedisConnection slaveConn = null;
            Object slaveRst = null;
            try {
                slaveConn = slaveConnFactory.getConnection();
                slaveRst = method.invoke(slaveConn, args);
            } catch (Throwable t) {
                ApiLogger.warn(t, "[Error on slave item %s] port %s failed, original exception is %s",
                        i, slaveConn == null ? null : slaveConn.info("Server").get("tcp_port"), t);
            } finally {
                if (slaveConn != null) {
                    RedisConnectionUtils.releaseConnection(slaveConn, slaveConnFactory);
                }
                // record the result of each slave
                if (rstMap != null) {
                    rstMap.put(i, slaveRst);
                }
            }
        }

        // add log for comparison in debug mode.
        if (isDebugMode()) {
            Iterator<Map.Entry<Integer, Object>> entryIterator = rstMap.entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry<Integer, Object> entry = entryIterator.next();
                if (rst != null && rst.equals(entry.getValue())) {
                    entryIterator.remove();
                }
            }
            ApiLogger.debug("[Warning] some slave port may got different result comparing to master : slaveRst - %s, masterRst - %s", rstMap, rst);
        }

        return rst;
    }

    private Object doSlaveRead(Method method, Object[] args) throws Throwable {
        int v = slaveCursor.incrementAndGet();

        if (v > 1000000000) {
            slaveCursor.set(0);
        }

        int len = connectionFactories.size();

        v = v % len;

        if (!isUsingMasterForRead()) {
            if (v == 0) {
                v = slaveCursor.incrementAndGet();
                v = v % len;
            }
        }

        RedisConnectionFactory slaveConnFactory = connectionFactories.get(v);
        RedisConnection slaveConn = null;

        Object rst = null;
        try {
            slaveConn = slaveConnFactory.getConnection();
            rst = method.invoke(slaveConn, args);
            return rst;
        } catch (Throwable t) {
            ApiLogger.warn(t, "[Error on slave item %s] port %s failed, original exception is %s",
            v, slaveConn == null ? null : slaveConn.info("Server").get("tcp_port"), t);
            throw t;
        } finally {
            if (slaveConn != null) {
                RedisConnectionUtils.releaseConnection(slaveConn, slaveConnFactory);
            }
        }

    }

    private Object doMasterOperation(Method method, Object[] args) throws Throwable {
        RedisConnectionFactory masterConnFactory = connectionFactories.get(0);
        RedisConnection masterConn = null;

        try {
            masterConn = masterConnFactory.getConnection();
            Object rst = method.invoke((EnhancedRedisConnection)masterConn, args);
            return rst;
        } catch (Throwable t) {
            ApiLogger.warn(t, "[Error on master item] port %s failed, original exception is %s",
                    masterConn == null ? null : masterConn.info("Server").get("tcp_port"), t);
            throw t;
        } finally {
            if (masterConn != null) {
                RedisConnectionUtils.releaseConnection(masterConn, masterConnFactory);
            }
        }
    }

    private Object doConnectionOperation(Method method, Object[] args) throws Throwable {
        String mtSig = getMethodSingnature(method);
        if (mtSig.contains("void ")) {
            return null;
        } else if (mtSig.contains("boolean ")) {
            return false;
        } else if (mtSig.contains("Object ")) {
            return new Object();
        } else if (mtSig.contains("List ")) {
            return Collections.emptyList();
        } else {
            return null;
        }
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    private static boolean isGeneralWriteCommand(Method m) {
        return generalWriteCommands.contains(getMethodSingnature(m));
    }

    private static boolean isSlaveReadCommand(Method m) {
        return slaveReadCommands.contains(getMethodSingnature(m));
    }

    private static boolean isMasterWriteCommand(Method m) {
        return masterWriteCommands.contains(getMethodSingnature(m));
    }

    private static boolean isMasterReadCommand(Method m) {
        return masterReadCommands.contains(getMethodSingnature(m));
    }

    private static boolean isConnectionCommand(Method m) {
        return connectionCommands.contains(getMethodSingnature(m));
    }

    private static String getMethodSingnature(Method m) {
        StringBuffer paramSb = new StringBuffer();
        Class[] classes = m.getParameterTypes();
        for (int i = 0; i < classes.length; i++) {
            paramSb.append(classes[i].getSimpleName());
            if (i != classes.length - 1) {
                paramSb.append(',');
            }
        }
        return String.format("%s %s(%s)", m.getReturnType().getSimpleName(), m.getName(), paramSb.toString());
    }

    @Override
    public RedisConnection getConnection() {
        return (RedisConnection) Proxy.newProxyInstance(EnhancedRedisConnection.class.getClassLoader(), new Class[]{EnhancedRedisConnection.class}, this);
    }

    public boolean isUsingMasterForRead() {
        return usingMasterForRead;
    }

    public void setUsingMasterForRead(boolean usingMasterForRead) {
        this.usingMasterForRead = usingMasterForRead;
    }

    @Override
    public boolean getConvertPipelineAndTxResults() {
        return convertPipelineAndTxResults;
    }

    public void setConvertPipelineAndTxResults(boolean convertPipelineAndTxResults) {
        this.convertPipelineAndTxResults = convertPipelineAndTxResults;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        this.checkConnectionString();
        this.connectionFactories = this.buildAndInitConnectionFactory(this.getConnectionString());
    }

    @Override
    public void destroy() throws Exception {
        if (CollectionUtils.isNotEmpty(connectionFactories)) {
            int i = 0; String connName = "Master";
            for (RedisConnectionFactory slaveConnFactory : connectionFactories) {
                if (i > 0) {
                    connName = "Slave " + i;
                }
                destroyConnectionFactory(slaveConnFactory, connName);
                i++;
            }
        }
    }



    void checkConnectionString() throws Exception {
        // previous check
        if (StringUtils.isBlank(this.getConnectionString())) {
            throw new IllegalStateException("[RedisHAProxy] connectionString has not been initialized!");
        }

    }

    List<RedisConnectionFactory> buildAndInitConnectionFactory(String connectionString) {
        List<RedisConnectionFactory> connectionFactories = buildConnectionFactories(connectionString);

        if (CollectionUtils.isNotEmpty(connectionFactories)) {
            int i = 0; String connName = "Master";
            for (RedisConnectionFactory rcf : connectionFactories) {
                if (i > 0) {
                    connName = "Slave " + i;
                }
                initConnectionFactory(rcf, connName);
                i++;
            }

        }
        return connectionFactories;
    }

    abstract List<RedisConnectionFactory> buildConnectionFactories(String connectionString);

    void initConnectionFactory(RedisConnectionFactory RedisConnectionFactory, String connName) {
        if (RedisConnectionFactory instanceof InitializingBean) {
            try {
                Method initMethod = RedisConnectionFactory.getClass().getDeclaredMethod("afterPropertiesSet");
                initMethod.invoke(RedisConnectionFactory);
            } catch (NoSuchMethodException e) {
                ApiLogger.warn("ConnectionFactory %s has no method called 'afterPropertiesSet', ignore init action!", connName);
            } catch (InvocationTargetException | IllegalAccessException e) {
                ApiLogger.warn(e,"Failed to call 'afterPropertiesSet' method on ConnectionFactory %s due to %s", connName, e);
            }
        } else {
            ApiLogger.warn("ConnectionFactory %s is not a InitializingBean, ignore init action!", connName);
        }
    }
    void destroyConnectionFactory(RedisConnectionFactory RedisConnectionFactory, String connName) {
        if (RedisConnectionFactory instanceof DisposableBean) {
            try {
                Method destroyMethod = RedisConnectionFactory.getClass().getDeclaredMethod("destroy");
                destroyMethod.invoke(RedisConnectionFactory);
            } catch (NoSuchMethodException e) {
                ApiLogger.warn("ConnectionFactory %s has no method called 'destroy', ignore destroy action!", connName);
            } catch (InvocationTargetException | IllegalAccessException e) {
                ApiLogger.warn(e,"Failed to call 'destroy' method on ConnectionFactory %s due to %s", connName, e);
            }
        } else {
            ApiLogger.warn("ConnectionFactory %s is not a DisposableBean, ignore destroy action!", connName);
        }
    }

}
