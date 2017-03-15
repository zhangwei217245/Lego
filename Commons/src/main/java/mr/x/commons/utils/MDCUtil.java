package mr.x.commons.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.MDC;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Reilost on 3/21/14.
 */
public class MDCUtil {


    public static String startTraceMDC() {
        String traceId = randomTraceId();
        MDC.put("trace_id", traceId);
        MDC.put("event_id", new AtomicInteger(0));
        String startTime = String.valueOf(System.currentTimeMillis());
        MDC.put("start_time", startTime);
        return traceId;
    }

    public static void addUrl(String url) {

        MDC.put("url", url);
    }


    public static String getUrl() {
        Object o = MDC.get("url");
        if (o == null) {
            return "";
        }
        return String.valueOf(o);
    }

    public static void endTraceMDC() {
        MDC.clear();
    }


    public static void addHttpStatus(int status) {
        MDC.put("http_status", String.valueOf(status));
    }


    public static long getHttpStartTime() {
        return Long.valueOf(MDC.get("start_time").toString());
    }

    public static String getHttpStatus() {
        return String.valueOf(MDC.get("http_status"));
    }

    public static String getTraceId() {
        Object traceId = MDC.get("trace_id");
        if (traceId == null) {
            return null;
        }
        return String.valueOf(MDC.get("trace_id"));
    }


    public static String formatLogString(String format) {
        boolean notHttp = false;
        if (StringUtils.isEmpty(getTraceId())) {
            startTraceMDC();
            notHttp = true;
        }
        addEventId();

        StackTraceElement stack[] = Thread.currentThread().getStackTrace();
        StackTraceElement s = null;
        for (int i = 0; i < stack.length; i++) {
            s = stack[i];
            if (s.getClassName().indexOf("ApiLogger") != -1) {
                s = stack[i + 1];
                break;
            }
        }
        String[] classNames = s.getClassName().split("\\.");
        String className = classNames[classNames.length - 1];
        String methodName = s.getMethodName();
        int lineNumber = s.getLineNumber();
        String wrapFormat = "%s [%s.java:%d -> %s(...)]";
        if (notHttp) {
            endTraceMDC();
        }
        return String.format(wrapFormat, format, className, lineNumber, methodName);
    }

    private static void addEventId() {
        ((AtomicInteger) (MDC.get("event_id"))).incrementAndGet();
    }

    private static Integer tomcatPort = null;

    private static AtomicInteger tomcatPortRetryCount = new AtomicInteger(0);

    private static String ip = null;
    private static Integer ipIntMod = null;
    private static Integer portMod = null;

    private static AtomicInteger traceIdSeed = new AtomicInteger(0);

    private static AtomicLong lastTimer = new AtomicLong(0L);


    public static int getShardMod() {
        if (ipIntMod == null) {
            ipIntMod = ipToInt(getIp()) % 999;
        }
        return ipIntMod;

    }

    public static String getIp() {
        if (StringUtils.isNotBlank(ip)) {
            return ip;
        }
        try {
            InetAddress ia = InetAddress.getLocalHost();
            ip = ia.getHostAddress();
            ipIntMod = ipToInt(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return ip;
    }


    public static String randomTraceId() {
        long timer = System.currentTimeMillis();
        int seq = 0;
        long lt = lastTimer.get();
        if (lt == timer) {
            seq = traceIdSeed.incrementAndGet();
            if (seq > 9999) {
                traceIdSeed.set(0);
                seq = traceIdSeed.incrementAndGet();
            }
        }
        lastTimer.compareAndSet(lt, timer);
        return "T" + getShardMod() + timer + String.format("%04d", seq);
    }


    private static final String IP_SEPARATOR = "\\.";

    public static String intToIp(int i) {
        return ((i >> 24) & 0xFF) + "." + ((i >> 16) & 0xFF) + "."
                + ((i >> 8) & 0xFF) + "." + (i & 0xFF);
    }

    public static int ipToInt(final String addr) {
        final String[] addressBytes = addr.split(IP_SEPARATOR);

        int ip = 0;
        for (int i = 0; i < 4; i++) {
            ip <<= 8;
            ip |= Integer.parseInt(addressBytes[i]);
        }
        return ip;
    }

}
