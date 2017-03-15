package x.spirit.queryexec;

import mr.x.commons.models.PageResult;
import mr.x.commons.utils.ApiLogger;
import mr.x.meshwork.edge.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32;

/**
 * Created by zhangwei on 4/13/15.
 */
//@Service(value = "queryExecutor")
public class MeshworkQueryExecutor implements QueryExecutor {

    ScheduledExecutorService executor = null;

    

    @Resource(name = "hybridGraphStorage")
    private GraphDao graphDao;

    public void queryEdgeCount(long sourceId) {
        long startMillis = System.currentTimeMillis();
        EdgeMetadata emeta = graphDao.getEdgeMetadata(sourceId);
        long endMillis = System.currentTimeMillis();
        System.out.println(String.format("Time elapsed: %d ms, " + emeta.getCount() +
                " edges are incident from " + sourceId  , endMillis - startMillis));
    }

    @Override
    public void randomHiDegree() {
        Random random = new Random();
        CRC32 crc32 = new CRC32();
        Integer[] seed = new Integer[high_degree_nodes.length];
        int size = 0;
        //shuffling
        while (size < high_degree_nodes.length) {
            int r = random.nextInt();
            crc32.update(r);
            int i = Long.valueOf(crc32.getValue()).intValue() / high_degree_nodes.length % high_degree_nodes.length;
            i = i < 0 ? 0-i : i;
            if (seed[i] == null) {
                seed[i] = high_degree_nodes[size];
                size ++;
            }
        }
        //testing
        for (Integer sourceId : seed){
            long startMillis = System.currentTimeMillis();
            PageResult<Edge> pageResult = graphDao.getEdgesBySource(sourceId, true,
                    new Cursor[]{new Cursor(Cursor.CursorName.destination_id, 0L, Long.MAX_VALUE,
                            Cursor.CursorDirection.ASC, true, true)});
            long endMillis = System.currentTimeMillis();
            System.out.println(String.format("Time elapsed: %d ms, " + pageResult.getCurrent_page().size() +
                    " records acquired for sourceId " + sourceId, (endMillis - startMillis)));

        }

    }

    @Override
    public void queryAllEdges(long sourceId) {
        long startMillis = System.currentTimeMillis();
        PageResult<Edge> pageResult = graphDao.getEdgesBySource(sourceId, true,
                new Cursor[]{new Cursor(Cursor.CursorName.destination_id, 0L, Long.MAX_VALUE,
                        Cursor.CursorDirection.ASC, true, true)});
        long endMillis = System.currentTimeMillis();
        System.out.println(String.format("Time elapsed: %d ms, "+ pageResult.getCurrent_page().size() +
                " records acquired.", endMillis - startMillis));
    }

    @Override
    public void queryOneEdge(long sourceId, long destinationId) {
        long startMillis = System.currentTimeMillis();
        Set<Edge> edges = graphDao.getEdgesBySourceAndDestinations(sourceId, new long[]{destinationId}, State.NORMAL);
        List<Edge> rstlst = new ArrayList<>();
        rstlst.addAll(edges);
        PageResult<Edge> pageResult = new PageResult<>(rstlst);
        long endMillis = System.currentTimeMillis();
        System.out.println(String.format("Time elapsed: %d ms", endMillis - startMillis));
        System.out.println(pageResult.toJSONString());
    }

    @Override
    public void bfsSearch(final long sourceId, int depth) {
        long startMillis = System.currentTimeMillis();
        final Map<Long, PageResult> sdMap = new ConcurrentHashMap<>();
        final Set<Long> sIds = new ConcurrentSkipListSet<>();
        final List<Long> durations = new LinkedList<>();
        sIds.add(sourceId);
        for (int i = 0 ; i<depth; i++) {
            executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2 + 4);
            List<Runnable> tasks = new LinkedList<>();
            //making tasks
            for (final Long source_id : sIds) {
                tasks.add(new Runnable() {
                    @Override
                    public void run() {
                        PageResult<Edge> pageResult = graphDao.getEdgesBySource(source_id, true,
                                new Cursor[]{new Cursor(Cursor.CursorName.destination_id, 0L, Long.MAX_VALUE,
                                        Cursor.CursorDirection.ASC, true, true)});
                        sdMap.put(source_id, pageResult);
                    }
                });
            }

            try {

                Thread.sleep(5000L);
                //submit tasks
                for (Runnable task : tasks) {
                    executor.submit(task);
                }
                // waiting for result
                Thread.sleep(5000L);
                executor.shutdown();
                executor.awaitTermination(30L, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // collecting the result
            sIds.clear();
            for (PageResult<Edge> page: sdMap.values()) {
                for (Edge e : page.getCurrent_page()) {
                    sIds.add(e.getDestination_id());
                }
            }
            long stageDuration = System.currentTimeMillis() - startMillis - 10000L;
            durations.add(stageDuration);
            ApiLogger.info("[Stage " + (i+1) + "]: " + sIds.size() +
                    " acquired in " + stageDuration + " Milliseconds.");
        }

        // print time duration.
        String str = "[BFS] ";
        for (Long dur : durations) {
            str = str + dur + " ";
        }
        System.out.println(str.trim());

    }

    public GraphDao getGraphDao() {
        return graphDao;
    }

    public void setGraphDao(GraphDao graphDao) {
        this.graphDao = graphDao;
    }

    public static void main(String[] args) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
        final AtomicInteger count = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            final int id = i;
            tasks.add(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setName("Thread-" + id);
                    for (int i = 0; i < 10; i++) {
                        int current = count.addAndGet(2);
                        try {
                            Thread.sleep(5L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println(Thread.currentThread().getName() + " : " + current);
                    }
                }
            });
        }

        for (Runnable run : tasks) {
            executor.submit(run);
        }
        try {
            Thread.sleep(3000L);
            System.out.println(count.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Integer[] a = new Integer[2];
        System.out.println(Arrays.toString(a));
    }
}
