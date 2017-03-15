package mr.x.brookside.amqp.model;

import org.perf4j.StopWatch;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhangwei on 14-4-23.
 *
 * @author zhangwei
 */
public class Probe {

    public StopWatch stopWatch = new StopWatch();

    public AtomicBoolean running = new AtomicBoolean(false);

    public AtomicBoolean shutdownTriggered = new AtomicBoolean(false);

    public AtomicLong workCount = new AtomicLong(0L);

    public AtomicLong failCount = new AtomicLong(0L);


    public StopWatch getStopWatch() {
        return stopWatch;
    }

    public void setStopWatch(StopWatch stopWatch) {
        this.stopWatch = stopWatch;
    }

    public AtomicLong getFailCount() {
        return failCount;
    }

    public void setFailCount(AtomicLong failCount) {
        this.failCount = failCount;
    }

    public AtomicLong getWorkCount() {
        return workCount;
    }

    public void setWorkCount(AtomicLong workCount) {
        this.workCount = workCount;
    }

    public AtomicBoolean getRunning() {
        return running;
    }

    public void setRunning(AtomicBoolean running) {
        this.running = running;
    }

    public AtomicBoolean getShutdownTriggered() {
        return shutdownTriggered;
    }

    public void setShutdownTriggered(AtomicBoolean shutdownTriggered) {
        this.shutdownTriggered = shutdownTriggered;
    }
}
