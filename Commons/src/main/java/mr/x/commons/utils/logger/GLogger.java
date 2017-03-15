package mr.x.commons.utils.logger;

import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

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
public class GLogger {

    private static final Logger debug = LoggerFactory.getLogger("debug");
    private static final Logger infoLog = LoggerFactory.getLogger("info");
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
        return debug.isTraceEnabled();
    }

    public static boolean isDebugEnabled() {
        return debug.isDebugEnabled();
    }

    public static void trace(String format, Object... arguments) {
        format = formatLogString(format);
        debug.trace(String.format(format, arguments), arguments);
    }

    public static void debug(String format, Object... arguments) {
        if (debug.isDebugEnabled()) {
            format = formatLogString(format);
            debug.debug(String.format(format, arguments), arguments);
        }
    }

    public static void info(String format, Object... arguments) {

        if (infoLog.isInfoEnabled()) {
            format = formatLogString(format);
            infoLog.info(String.format(format, arguments), arguments);
        }
    }

    public static void warn(Throwable t, String format, Object... arguments) {
        format = formatLogString(format);
        warnLog.warn(String.format(format, arguments), t);
    }

    public static void warn(String format, Object... arguments) {
        format = formatLogString(format);
        warnLog.warn(String.format(format, arguments), arguments);
    }


    public static void error(Throwable t, String format, Object... arguments) {
        format = formatLogString(format);
        errorLog.error(String.format(format, arguments), t);
    }

    public static void error(String format, Object... arguments) {
        format = formatLogString(format);
        errorLog.error(String.format(format, arguments), arguments);
    }

    public static void errorForTest(Throwable t, String format, Object... arguments) {
        format = formatLogString(format);
        testLog.error(String.format(format, arguments), t);
    }

    public static void infoForTest(String format, Object... arguments) {
        format = formatLogString(format);
        testLog.info(String.format(format, arguments), arguments);
    }


    public static String formatLogString(String format){
        StackTraceElement stack[] = Thread.currentThread().getStackTrace();
        System.out.println(Arrays.toString(stack));
        StackTraceElement s = null;
        for (int i = 0; i < stack.length; i++) {
            s = stack[i];
            if (s.getClassName().indexOf("GLogger") != -1) {
                s = stack[i + 1];
                break;
            }
        }
        String[] classNames = s.getClassName().split("\\.");
        String className = classNames[classNames.length - 1];
        String methodName = s.getMethodName();
        int lineNumber = s.getLineNumber();
        String wrapFormat = "%s [%s.java:%d -> %s(...)]";
        return String.format(wrapFormat, format, className, lineNumber, methodName);
    }

}
