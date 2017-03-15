package mr.x.commons.utils;

import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 统一日志。
 * <p/>
 * 使用slf4j API，方便日后切换
 * <p/>
 * trace/debug/info三个级别只提供format, arguments方式, 强制程序员将信息描述写入这类日志。
 * warn/error级别提供format, arguments和message, throwable两种方式，要求程序员仅将抛出异常的情况记录在此。
 * <p/>
 * <p/>
 * slf4j使用的formatter接受{}参数替换。为了能够使用JDK提供的formatter,本Logger在内部对format和arguments
 * 进行了封装
 *
 * @author zhangwei
 */
public class ApiLogger {

    private static final Logger Lego = LoggerFactory.getLogger("debug");
    private static final Logger infoLog = LoggerFactory.getLogger("info");
    private static final Logger apiRequestLog = LoggerFactory.getLogger("apiRequest");
    private static final Logger httpClientLog = LoggerFactory.getLogger("httpClient");
    private static final Logger warnLog = LoggerFactory.getLogger("warn");
    private static final Logger errorLog = LoggerFactory.getLogger("error");
    private static final Logger testLog = LoggerFactory.getLogger("testlog");

    static {
        //TODO: to test if this code below is necessary
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LogManager.shutdown();
            }
        });
    }

    public static boolean isTraceEnabled() {
        return Lego.isTraceEnabled();
    }

    public static boolean isDebugEnabled() {
        return Lego.isDebugEnabled();
    }

    public static void trace(String format, Object... arguments) {
        format = MDCUtil.formatLogString(format);
        Lego.trace(String.format(format, arguments), arguments);
    }

    public static void debug(String format, Object... arguments) {
        if (Lego.isDebugEnabled()) {
            format = MDCUtil.formatLogString(format);
            Lego.debug(String.format(format, arguments), arguments);
        }
    }

    public static void info(String format, Object... arguments) {

        if (infoLog.isInfoEnabled()) {
            format = MDCUtil.formatLogString(format);
            infoLog.info(String.format(format, arguments), arguments);
        }
    }

    public static void warn(Throwable t, String format, Object... arguments) {
        format = MDCUtil.formatLogString(format);
        warnLog.warn(String.format(format, arguments), t);
    }

    public static void warn(String format, Object... arguments) {
        format = MDCUtil.formatLogString(format);
        warnLog.warn(String.format(format, arguments), arguments);
    }


    public static void error(Throwable t, String format, Object... arguments) {
        format = MDCUtil.formatLogString(format);
        errorLog.error(String.format(format, arguments), t);
    }

    public static void error(String format, Object... arguments) {
        format = MDCUtil.formatLogString(format);
        errorLog.error(String.format(format, arguments), arguments);
    }

    public static void errorForTest(Throwable t, String format, Object... arguments) {
        format = MDCUtil.formatLogString(format);
        testLog.error(String.format(format, arguments), t);
    }

    public static void infoForTest(String format, Object... arguments) {
        format = MDCUtil.formatLogString(format);
        testLog.info(String.format(format, arguments), arguments);
    }

    public static void logRequest(String format, Object... arguments) {
        format = MDCUtil.formatLogString(format);
        apiRequestLog.info(String.format(format, arguments), arguments);
    }


    public static void httpDebug(String format, Object... arguments) {
        format = MDCUtil.formatLogString(format);
        httpClientLog.debug(String.format(format, arguments), arguments);
    }

    public static void httpInfo(String format, Object... arguments) {
        format = MDCUtil.formatLogString(format);
        httpClientLog.info(String.format(format, arguments), arguments);
    }

    public static void httpWarn(String format, Object... arguments) {
        format = MDCUtil.formatLogString(format);
        httpClientLog.warn(String.format(format, arguments), arguments);
    }

    public static void httpError(String format, Object... arguments) {
        format = MDCUtil.formatLogString(format);
        httpClientLog.error(String.format(format, arguments), arguments);
    }

    public static void httpWarn(Throwable t, String format, Object... arguments) {
        format = MDCUtil.formatLogString(format);
        httpClientLog.warn(String.format(format, arguments), arguments, t);
    }

    public static void httpError(Throwable t, String format, Object... arguments) {
        format = MDCUtil.formatLogString(format);
        httpClientLog.error(String.format(format, arguments), arguments, t);
    }

}
