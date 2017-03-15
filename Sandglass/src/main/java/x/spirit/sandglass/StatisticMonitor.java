package x.spirit.sandglass;


import mr.x.commons.utils.ApiLogger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhangwei
 */
public class StatisticMonitor<T> implements Runnable {

    private volatile AtomicLong sendCount = new AtomicLong(0);
    private volatile AtomicLong successCount = new AtomicLong(0);
    private volatile AtomicBoolean sourceOver = new AtomicBoolean(false);
    private volatile AtomicBoolean targetOver = new AtomicBoolean(false);
    private static double minWriteSpeed = 0;
    private static double maxWriteSpeed = 0;
    private static double minReadSpeed = 0;
    private static double maxReadSpeed = 0;
    private static final long startMills = System.currentTimeMillis();
    private static long lastMills = startMills;
    private static long stopMills = startMills + 1000L;
    private static long lastRCount = 0;
    private static long lastWCount = 0;
    private static int canShutdown = 0;

    @Override
    public void run() {

        if (canShutdown >= 5) {
            System.exit(0);
        }
        try{
            long currMills = System.currentTimeMillis();
            if (!(sourceOver.get() && targetOver.get())) {
                stopMills = currMills;
            }
            long durationSecs = (stopMills - startMills) / 1000L;
            long currSecs = (currMills - lastMills) / 1000L;
            long rCount = sendCount.get();
            long wCount = successCount.get();
            long currRCount = rCount - lastRCount;
            long currWCount = wCount - lastWCount;
            lastRCount = rCount; lastWCount = wCount;lastMills = currMills;
            long currRSpeed = currRCount / currSecs;
            long currWSpeed = currWCount / currSecs;
            long readSpeedAVG = rCount / durationSecs;
            long writeSpeedAVG = wCount / durationSecs;
            long failCount = rCount - wCount;
            if ((currWSpeed > 0.0d && currWSpeed < minWriteSpeed) || minWriteSpeed == 0.0d) minWriteSpeed = currWSpeed;
            if (currWSpeed > maxWriteSpeed) maxWriteSpeed = currWSpeed;
            if ((currRSpeed > 0.0d && currRSpeed < minReadSpeed) || minReadSpeed == 0.0d) minReadSpeed = currRSpeed;
            if (currRSpeed > maxReadSpeed) maxReadSpeed = currRSpeed;

            String taskFinishSign = "";
            if (sourceOver.get() && targetOver.get()) {
                taskFinishSign = "[Task Finished]";
                Thread.sleep(5000L);
                canShutdown++;
            }
            ApiLogger.info("%s time statistic:Speed:{readMin:"+minReadSpeed+",readCurr:"
                    +currRSpeed+",readAVG:"+readSpeedAVG+",readMax:"+maxReadSpeed+","
                    +"writeMin:"+minWriteSpeed+",writeCurr:"+ currWSpeed
                    +",writeAVG:"+writeSpeedAVG+",writeMax:"+maxWriteSpeed+"}," +
                    "Count:{read:"+rCount+",written:"+wCount+",unwritten:"+failCount+"}", taskFinishSign);
        } catch (Throwable t) {

        }

    }


    public StatisticMonitor(AtomicLong sendCount, AtomicLong successCount, AtomicBoolean sourceOver, AtomicBoolean targetOver) {
        super();
        this.sendCount = sendCount;
        this.successCount = successCount;
        this.sourceOver = sourceOver;
        this.targetOver = targetOver;

    }


    public AtomicLong getSendCount() {
        return sendCount;
    }

    public void setSendCount(AtomicLong sendCount) {
        this.sendCount = sendCount;
    }

    public AtomicLong getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(AtomicLong successCount) {
        this.successCount = successCount;
    }

    public AtomicBoolean getSourceOver() {
        return sourceOver;
    }

    public void setSourceOver(AtomicBoolean sourceOver) {
        this.sourceOver = sourceOver;
    }

    public AtomicBoolean getTargetOver() {
        return targetOver;
    }

    public void setTargetOver(AtomicBoolean targetOver) {
        this.targetOver = targetOver;
    }

    public static void main(String[] args){

    }


}
