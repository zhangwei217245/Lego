package x.spirit.sandglass;


import mr.x.commons.concurrent.BlockingQueueHolder;
import mr.x.commons.utils.ApiLogger;
import org.apache.commons.collections.CollectionUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhangwei
 */
public class DataImporter<T> {
    // DataSource ref and DataTarget ref
    private DataSource<T> dataSource;
    private DataTarget<T> dataTarget;
    private List<Runnable> sourceRunners;
    private List<Runnable> targetRunners;
    // param for the global (both source and target)
    protected int threadPoolSize = Runtime.getRuntime().availableProcessors();
    protected int sourceCount;
    protected int targetCount;
    protected int queueSize;
    protected int queueTimeout;
    protected AtomicBoolean sourceOver = new AtomicBoolean(false);
    protected AtomicBoolean targetOver = new AtomicBoolean(false);
    protected ScheduledExecutorService executor;
    protected BlockingQueueHolder<T> queue;
    // param for the DataSource
    protected long startPos; // should start from 0
    protected long pageCount;
    protected long endPos;
    protected AtomicLong sendCount = new AtomicLong(0);
    // param for the DataTarget
    protected AtomicLong successCount = new AtomicLong(0);
    // other params;
    public static Map<String, String> otherParams = new HashMap<String, String>();

    private File stopMark = new File("/data1/stopread");


    public List<Runnable> getSourceRunners() {
        return sourceRunners;
    }

    public void setSourceRunners(List<Runnable> sourceRunners) {
        this.sourceRunners = sourceRunners;
    }

    public List<Runnable> getTargetRunners() {
        return targetRunners;
    }

    public void setTargetRunners(List<Runnable> targetRunners) {
        this.targetRunners = targetRunners;
    }

    public void init() {
        String process_home = System.getProperty("process.home");
        if (process_home != null) {
            String stopMarkFilePath = System.getProperty("process.home") + File.pathSeparator + "stopread";
            stopMark = new File(stopMarkFilePath);
            System.out.print("stopMark: " + stopMarkFilePath);
        }
        dataSource.setOtherParams(otherParams);
        dataTarget.setOtherParams(otherParams);
        int poolSize = (sourceCount != 0 && targetCount != 0) ? sourceCount + targetCount : threadPoolSize * 2;
        executor = Executors.newScheduledThreadPool(poolSize * 2 + 4);
        queue = new BlockingQueueHolder<T>(queueSize, queueTimeout);
        sourceCount = sourceCount == 0 ? threadPoolSize : sourceCount;
        targetCount = targetCount == 0 ? threadPoolSize : targetCount;
        sourceRunners = initSource();
        targetRunners = initTarget();
    }

    // for importer instance to indicate the dataSource, and do other initialization.
    public List<Runnable> initSource() {

        List<Runnable> sourceRunners = new ArrayList<Runnable>();
        // the task numbers of each thread.
        long countPerThread = (endPos - startPos + 1L) / sourceCount;
        for (int i = 0; i < sourceCount; i++) {
            // each thread should start at i * countPerThread + startPos;
            final long startPosPerThread = i * countPerThread + startPos;
            // each thread should stop at startPosPerThread + countPerThread - 1
            long tempend = startPosPerThread + countPerThread - 1L;
            // if preforced stopPos is larger than endPos, stopPos should be endPos.
            if (tempend > endPos) {
                tempend = endPos;
            }
            final long endPosPerThread = tempend;
            sourceRunners.add(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setName("SourceRunner " + startPosPerThread + " ~ " + endPosPerThread);
                    boolean threadFinish = false;
                    long pageStartPos = startPosPerThread;
                    try {
                        dataSource.preAction(startPosPerThread, endPosPerThread);
                        long lastMillisWhileData = System.currentTimeMillis();
                        long hungaryTime = 0L;
                        while (!threadFinish) {
                            // to test whether the file exists
                            if (stopMark.exists()) {
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    ApiLogger.warn("stopMark exists, but thread cannot wait due to InterruptedException: ", e);
                                }
                                continue;
                            }
                            // read one page in a loop
                            long pageEndPos = pageStartPos + pageCount - 1L;
                            try {

                                ResultModel<T> rstModel = dataSource.readData(pageStartPos, pageEndPos);

                                Collection<T> coll = rstModel.getResult();
                                if (CollectionUtils.isNotEmpty(coll)) {
                                    for (T t : coll) {
                                        queue.put(t);
                                        sendCount.incrementAndGet();
                                    }
                                    lastMillisWhileData = System.currentTimeMillis();
                                } else {
                                    Thread.sleep(100);
                                    hungaryTime = System.currentTimeMillis() - lastMillisWhileData;
                                }

                                // if endPos == startPos and pageCount == 0,
                                // the source thread should not stop, for myTriggerQ Processor
                                if ((endPos - startPos) == 0L && pageCount == 0L) {
                                    continue;
                                } else {
                                    if (hungaryTime >= 5000L) {
                                        sourceOver.compareAndSet(false, true);
                                        break;
                                    }
                                }

                                String threadName = Thread.currentThread().getName();
                                ApiLogger.debug("["+threadName+"][READING DATA] " + pageStartPos + " ~ " + pageEndPos);
                                // to test whether the thread should stop, and whether it reads at the end of the whole range.
                                if (pageEndPos >= endPosPerThread) {
                                    threadFinish = true;
                                    if (pageEndPos >= endPos) {
                                        sourceOver.compareAndSet(false, true);
                                    }
                                }
                            } catch (Throwable t) {
                                sourceOver.compareAndSet(false, true);
                                ApiLogger.error("[DataSource FAILED in Loop]" + dataSource.getClass().getName() + " from " + pageStartPos + " to " + pageEndPos);
                                t.printStackTrace();
                            }
                            // the next page start pos
                            pageStartPos = pageEndPos + 1L;
                        }
                        dataSource.postAction(startPosPerThread, endPosPerThread);
                    } catch (PreProcessException | PostProcessException e) {
                        sourceOver.compareAndSet(false, true);
                        ApiLogger.error(e, "[DataSource FAILED]["+e.getClass().getName()+"]" + dataSource.getClass().getName() + " from " + startPosPerThread + " to " + endPosPerThread);
                    }

                }
            });
        }
        return sourceRunners;
    }

    // for importer instance to indicate the dataTarget, and do other initialization.
    public List<Runnable> initTarget() {
        List<Runnable> targetRunners = new ArrayList<Runnable>();
        for (int i = 0; i < targetCount; i++) {
            final int threadId = i;
            targetRunners.add(new Runnable() {

                private void flushData(List<T> dataList) {
                    dataTarget.writeData(dataList);
                    successCount.addAndGet(dataList.size());
                    dataList.clear();
                }

                @Override
                public void run() {
                    Thread.currentThread().setName("TargetRunner-" + threadId);
                    List<T> dataList = new ArrayList<T>();

                    try {
                        dataTarget.preAction(dataList);
                        long lastMillisWhileData = System.currentTimeMillis();
                        long hungaryTime = 0L;
                        while (true) {
                            try {
                                T t = queue.pollWithTimeout();
                                if (t == null) {
                                    if (sourceOver.get()) {
                                        hungaryTime = System.currentTimeMillis() - lastMillisWhileData;
                                        if (hungaryTime > 10000L) {
                                            ApiLogger.warn("[TARGET ABORTED] No Data Receieved for 5 min after sources finish work.");
                                            flushData(dataList);
                                            targetOver.compareAndSet(false, true);
                                            break;
                                        }
                                    }
                                    Thread.sleep(100);
                                    continue;
                                }
                                lastMillisWhileData = System.currentTimeMillis();
                                dataList.add(t);
                                int dataLstSize = dataList.size();
                                String threadName = Thread.currentThread().getName();
                                ApiLogger.debug("[" + threadName + "][WRITING DATA] " + dataLstSize);

                                if (dataLstSize >= (pageCount / targetCount * 2)) {
                                    flushData(dataList);
                                }

                            } catch (Exception t) {
                                ApiLogger.error("Error: ", t);
                            }
                        }
                        dataTarget.postAction(dataList);
                    } catch (PreProcessException |PostProcessException e) {
                        ApiLogger.error("Error: ", e);
                    }
                }
            });
        }
        return targetRunners;
    }

    public void execute() {
        init();
        StatisticMonitor<T> task = new StatisticMonitor<T>(sendCount, successCount, sourceOver, targetOver);
        executor.scheduleAtFixedRate(task, 5, 5, TimeUnit.SECONDS);
        for (Runnable target : targetRunners) {
            executor.submit(target);
        }
        for (Runnable source : sourceRunners) {
            executor.submit(source);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                executor.shutdown();
                try {
                    executor.awaitTermination(10000L, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public long getStartPos() {
        return startPos;
    }

    public void setStartPos(long startPos) {
        this.startPos = startPos;
    }

    public long getPageCount() {
        return pageCount;
    }

    public void setPageCount(long pageCount) {
        this.pageCount = pageCount;
    }

    public long getEndPos() {
        return endPos;
    }

    public void setEndPos(long endPos) {
        this.endPos = endPos;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public int getQueueTimeout() {
        return queueTimeout;
    }

    public void setQueueTimeout(int queueTimeout) {
        this.queueTimeout = queueTimeout;
    }

    public Map<String, String> getOtherParams() {
        return otherParams;
    }

    public AtomicLong getSuccessCount() {
        return successCount;
    }

    public DataSource<T> getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource<T> dataSource) {
        this.dataSource = dataSource;
    }

    public DataTarget<T> getDataTarget() {
        return dataTarget;
    }

    public void setDataTarget(DataTarget<T> dataTarget) {
        this.dataTarget = dataTarget;
    }

    public int getSourceCount() {
        return sourceCount;
    }

    public void setSourceCount(int sourceCount) {
        this.sourceCount = sourceCount;
    }

    public int getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(int targetCount) {
        this.targetCount = targetCount;
    }


}