package mr.x.meshwork.edge;


import mr.x.commons.models.PageResult;
import mr.x.commons.utils.ApiLogger;
import mr.x.meshwork.edge.enums.QueryType;
import mr.x.meshwork.edge.mysql.MysqlGraphDaoImpl;
import mr.x.meshwork.edge.redis.RedisGraphDaoImpl;
import mr.x.meshwork.edge.utils.GraphUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Created by zhangwei on 14-3-19.
 * <p/>
 * 由于FLUSH REDIS的可能性较大，并且FLUSH以后重建REDIS索引的过程可能受到page cursor的影响，
 * 所以在数据没有充分导入的情况下，默认的read through机制可能在进行PAGE查询的时候，无法加载
 * Mysql对应的全量数据，导致返回结果不一致。
 * <p/>
 * 已经满足的前提条件：
 * 1. Mysql的Metadata中count数为有效Edge数量。
 * 2. Redis的Metadata中count数为有效Edge数量。
 * <p/>
 * 解决办法：
 * <p/>
 * 1. REDIS实现中提供PhantomMetadata。每一个Edge对象对应一个PhantomMetadata.
 * 2. 当某条边的PhantomMetadata的key不存在时，认为数据尚未初始化，或者需要校准（校准功能仅当设置了PhantomMetadata的过期时间时有用）。
 * 3. 如果数据尚未初始化或需要进入校准流程，则首先加载Mysql的Metadata，将其count值写入PhantomMetadata。
 * 4. 异步线程或者队列方式（后期支持）从Mysql中进行数据加载。加载数据的过程就是单纯调用CreateEdges方法。这样对象的Metadata的count值就可以自动增长。数据加载完毕，写PhantomMetadata.ready为true
 * 5. 数据构建过程中，有两种可选的Redis数据响应策略，即：a.返回当前数据 b.返回MySQL数据。 默认是返回当前数据。
 * 6. 如果选择返回当前数据，则直接把查询条件传入Redis实现进行获取。这样在数据构建过程中，每次访问同一页数据，可能得到的结果会随着数据重建的过程而变化。
 * 7. 如果是返回MYSQL数据，则在判断PhantomMetadata存在的情况下，判断PhantomMetadata.isReady==true，
 * 8. PhantomMetadata.isReady==false则判断当前Metadata.count>=PhantomMetadata.count。如果成立，则PhantomMetadata.isReady=true，且从REDIS返回数据。如果不是这种情况，就获取MYSQL数据。这种方式可能导致性能问题，慎用。
 * 9. PhantomMetadata.isReady==true则直接返回Redis数据。
 * <p/>
 * 综上， 整个phantom的生命周期包含：
 * 1. 创建 --》 复制当前MySQL中的Metadata.count计数，同时isReady=false
 * 2. setReady --》 isReady=true
 * 3. 过期（optional) -》自然的key expire。
 *
 * @author zhangwei
 */
public class HybridGraphStorage extends GraphStorage implements InitializingBean {

    public final static ForkJoinPool dataLoadingPool = new ForkJoinPool();

    public final static Executor rebuildingSubmitionPool = Executors.newFixedThreadPool(32);

    private boolean hasMysql = false;
    private boolean hasRedis = false;
    private Long phantomCheckIntervalMillis = null;
    private boolean serveStaleData = false;

    private static boolean hasRemovedCondition(EdgeBizFilter... bizFilters) {
        for (EdgeBizFilter bizFilter : bizFilters) {
            if (State.REMOVED.equals(bizFilter)) {
                return true;
            }
        }
        return false;
    }

    public void init() {
        hasMysql = (getMysqlDao() != null);
//        hasRedis = false;
        hasRedis = (getRedisDao() != null);
        if (!hasMysql) {
            throw new IllegalStateException("[HybridGraphStorage] mysqlDao-ref must be set and initialized!");
        }
    }

    @Override
    public void initiateEdgesTable(String id, boolean dropBeforeCreate) {
        if (hasMysql)
            getMysqlDao().initiateEdgesTable(id, dropBeforeCreate);
    }


    @Override
    public void initiateMetadataTable(String id, boolean dropBeforeCreate) {
        if (hasMysql)
            getMysqlDao().initiateMetadataTable(id, dropBeforeCreate);
    }

    @Override
    public int createEdges(Edge... edges) {
        int dbrst = hasMysql ? getMysqlDao().createEdges(edges) : getRedisDao().createEdges(edges);
        dbrst = hasRedis ? (hasMysql ? getRedisDao().createEdges(edges) : dbrst) : dbrst;
        return dbrst;
    }

    @Override
    public int removeEdges(Edge... edges) {
        int dbrst = hasMysql ? getMysqlDao().removeEdges(edges) : getRedisDao().removeEdges(edges);
        dbrst = hasRedis ? (hasMysql ? getRedisDao().removeEdges(edges) : dbrst) : dbrst;
        return dbrst;
    }

    @Override
    public int purgeEdges(Edge... edges) {
        int dbrst = hasMysql ? getMysqlDao().purgeEdges(edges) : getRedisDao().purgeEdges(edges);
        dbrst = hasRedis ? (hasMysql ? getRedisDao().purgeEdges(edges) : dbrst) : dbrst;
        return dbrst;
    }

    private boolean hasPhantom(long source_id) {
        return ((RedisGraphDaoImpl) getRedisDao()).hasPhantomMetadata(source_id);
    }

    private PhantomMetadata getPhantom(long source_id) {
        return ((RedisGraphDaoImpl) getRedisDao()).getPhantomMetadata(source_id);
    }


    /**
     * 如果设置了redis并且redis的精准获取开关是关闭的，则考虑从MYSQL校准总数
     * 在这种情况下，如果有Mysql可用，并且方法要求校准的情况下，从MYSQL校准。
     *
     * @param source_id
     * @param needAccurateTotalCount 是否需要精确的总数。
     * @param cursors
     * @param bizFilters
     * @return
     */
    @Override
    public PageResult<Edge> getEdgesBySource(final long source_id, boolean needAccurateTotalCount, Cursor[] cursors, EdgeBizFilter... bizFilters) {
        PageResult<Edge> rst = null;
        GraphDao mainDataSource = getMysqlDao();
        if (hasRedis) {
            boolean isRedisReady = trySubmittingAsyncEdgesLoading(source_id);

            if (isRedisReady) {
                mainDataSource = getRedisDao();
            }
            //if loading stale data, set main data source to redis datasource.
            if (isServeStaleData()) {
                mainDataSource = getRedisDao();
            }
            if (hasRemovedCondition(bizFilters)) {
                mainDataSource = getMysqlDao();
            }

        }

        if (mainDataSource instanceof RedisGraphDaoImpl) {
            ApiLogger.debug("getEdgesBySource loading from redis for %s %s",
                    ((RedisGraphDaoImpl) mainDataSource).getShardingPolicy().getBizName(),
                    source_id);
        }
        if (mainDataSource instanceof MysqlGraphDaoImpl) {
            ApiLogger.debug("getEdgesBySource loading from redis for %s %s",
                    ((MysqlGraphDaoImpl) mainDataSource).getShardingPolicy().getBizName(),
                    source_id);
        }
        rst = mainDataSource.getEdgesBySource(source_id, needAccurateTotalCount, cursors, bizFilters);

        if (rst != null && rst.isNotEmpty()) {
            if (hasRedis && mainDataSource instanceof MysqlGraphDaoImpl) {
                setRedisAfterFilteringNormal(rst.getCurrent_page());
            }
        }

        return rst;
    }

    @Override
    public Set<Edge> getEdgesBySourceAndDestinations(long source_id, long[] destination_ids, State state) {
        Set<Edge> rst = null;
        GraphDao mainDataSource = getMysqlDao();
        if (hasRedis) {
            boolean isRedisReady = trySubmittingAsyncEdgesLoading(source_id);

            if (isRedisReady) {
                mainDataSource = getRedisDao();
            }
            //if loading stale data, set main data source to redis datasource.
            if (isServeStaleData()) {
                mainDataSource = getRedisDao();
            }
            if (hasRemovedCondition(state)) {
                mainDataSource = getMysqlDao();
            }

        }

        if (mainDataSource instanceof RedisGraphDaoImpl) {
            ApiLogger.debug("getEdgesBySourceAndDestinations loading from redis for %s %s %s %s",
                    ((RedisGraphDaoImpl) mainDataSource).getShardingPolicy().getBizName(),
                    source_id, Arrays.toString(destination_ids), state);
        }
        if (mainDataSource instanceof MysqlGraphDaoImpl) {
            ApiLogger.debug("getEdgesBySourceAndDestinations loading from mysql for %s %s %s %s",
                    ((MysqlGraphDaoImpl) mainDataSource).getShardingPolicy().getBizName(),
                    source_id, Arrays.toString(destination_ids), state);
        }
        rst = mainDataSource.getEdgesBySourceAndDestinations(source_id, destination_ids, state);

        if (CollectionUtils.isEmpty(rst) && mainDataSource instanceof RedisGraphDaoImpl) {
            rst = loadMysqlAndSetRedis(source_id, destination_ids, state);
        }
        return rst;
    }


    @Override
    public EdgeMetadata getEdgeMetadata(long source_id) {
        EdgeMetadata rst = null;
        GraphDao mainDataSource = getMysqlDao();
        if (hasRedis) {
            boolean isRedisReady = trySubmittingAsyncEdgesLoading(source_id);
            if (isRedisReady) {
                mainDataSource = getRedisDao();
            }
            if (isServeStaleData()) {
                mainDataSource = getRedisDao();
            }
        }

        if (mainDataSource instanceof RedisGraphDaoImpl) {
            ApiLogger.debug("getEdgeMetadata loading from redis for %s %s",
                    ((RedisGraphDaoImpl) mainDataSource).getShardingPolicy().getBizName(), source_id);
        }
        if (mainDataSource instanceof MysqlGraphDaoImpl) {
            ApiLogger.debug("getEdgeMetadata loading from mysql for %s %s",
                    ((MysqlGraphDaoImpl) mainDataSource).getShardingPolicy().getBizName(), source_id);
        }
        rst = mainDataSource.getEdgeMetadata(source_id);
        //FIXME: temporarily disable reading through due to phantom loading feature, to avoid extraneous increase to the count of metadata.
        //TODO: however, you may enable the code below with conditional check whenever phantom loading could be implemented as a plugin.

//        if (rst == null && mainDataSource instanceof RedisGraphDaoImpl) {
//            rst = getMysqlDao().getEdgeMetadata(source_id);
//            if ((rst != null) && hasRedis) {
//                getRedisDao().updateEdgeMetadata(rst);
//            }
//        }
        return rst;
    }

    @Override
    public int updateEdgeMetadata(EdgeMetadata emeta) {
        int dbrst = hasMysql ? getMysqlDao().updateEdgeMetadata(emeta) : getRedisDao().updateEdgeMetadata(emeta);
        dbrst = hasRedis ? (hasMysql ? getRedisDao().updateEdgeMetadata(emeta) : dbrst) : dbrst;
        return dbrst;
    }

    @Override
    public List<Long> preFetchAdvancedQueryResult(long sourceA_id, long sourceB_id, QueryType queryType) {
        return hasRedis ? getRedisDao().preFetchAdvancedQueryResult(sourceA_id, sourceB_id, queryType) : null;
    }

    @Override
    public void storeAdvancedQueryResult(long sourceA_id, long sourceB_id, QueryType queryType, List<Long> queryRst) {
        if (hasRedis)
            getRedisDao().storeAdvancedQueryResult(sourceA_id, sourceB_id, queryType, queryRst);
    }

    /**
     * try to submit the async loading process if the phantom key is not exists or has expired.
     * returns whether the edges under a certain source_id is ready in redis.
     *
     * @param source_id
     * @return
     */
    private boolean trySubmittingAsyncEdgesLoading(final long source_id) {
        boolean isRedisReady = false;
        if (!hasPhantom(source_id)) {
            final EdgeMetadata meta = getMysqlDao().getEdgeMetadata(source_id);
            if (meta != null) {
                rebuildingSubmitionPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        final EdgeInitTask task = new EdgeInitTask(source_id, 0L, meta.getCount(), meta.getCount());
                        //set expire time to indicate PhantomCheckIntervals
                        PhantomMetadata phantom = ((RedisGraphDaoImpl) getRedisDao()).initPhantom(source_id, meta.getCount(), getPhantomCheckIntervalMillis());

                        if (phantom == null) {
                            return;
                        }
                        long startMillis = System.currentTimeMillis();
                        ApiLogger.debug("start Edge rebuilding process for %s %s", getMysqlDao().getShardingPolicy().getBizName()
                                , source_id);

                        dataLoadingPool.invoke(task);

                        ApiLogger.debug("end Edge rebuilding process for %s %s in %s ms.", getMysqlDao().getShardingPolicy().getBizName()
                                , source_id, (System.currentTimeMillis() - startMillis));

                        ((RedisGraphDaoImpl) getRedisDao()).setPhantomReady(source_id);
                    }
                });
            }
        } else {
            PhantomMetadata phantomMeta = getPhantom(source_id);
            if (phantomMeta != null) {
                isRedisReady = phantomMeta.isReady();
            }
        }
        return isRedisReady;
    }

    private Set<Edge> loadMysqlAndSetRedis(long source_id, long[] destination_ids, State state) {
        Set<Edge> edges = hasMysql ? getMysqlDao().getEdgesBySourceAndDestinations(source_id, destination_ids, state) : null;
        if ((CollectionUtils.isNotEmpty(edges)) && hasRedis) {
            setRedisAfterFilteringNormal(edges);
        }
        return edges;
    }

    private void setRedisAfterFilteringNormal(Collection<Edge> edges) {
        Collection<Edge> filteredEdges = GraphUtils.getBizFilteredEdges(edges, State.NORMAL);
        if (CollectionUtils.isNotEmpty(filteredEdges)) {
            ((RedisGraphDaoImpl) getRedisDao()).createEdges(filteredEdges.toArray(new Edge[filteredEdges.size()]), true);
        }
    }

    public Long getPhantomCheckIntervalMillis() {
        return phantomCheckIntervalMillis;
    }

    public void setPhantomCheckIntervalMillis(Long phantomCheckIntervalMillis) {
        this.phantomCheckIntervalMillis = phantomCheckIntervalMillis;
    }

    public boolean isServeStaleData() {
        return serveStaleData;
    }

    public void setServeStaleData(boolean serveStaleData) {
        this.serveStaleData = serveStaleData;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
        if (getPhantomCheckIntervalMillis() != null && getPhantomCheckIntervalMillis() < 900000L) {
            setPhantomCheckIntervalMillis(900000L);
        }
    }


    protected class EdgeInitTask extends RecursiveAction {

        private long MAX_PAGE_SIZE = 500L;

        private long source_id;

        private long low;

        private long high;

        private long max_end;

        public EdgeInitTask(long source_id, long low, long high, long max_end) {
            this.source_id = source_id;
            this.low = low;
            this.high = high;
            this.max_end = max_end;
        }

        @Override
        protected void compute() {

            ApiLogger.debug("compute : %s, %s, %s, %s, %s", source_id, low, high, max_end, getRedisDao().getShardingPolicy().getBizName());
            if (high - low > MAX_PAGE_SIZE) {
                long mid = (low + high) >>> 1L;
                EdgeInitTask leftTask = new EdgeInitTask(source_id, low, mid, max_end);
                EdgeInitTask rightTask = new EdgeInitTask(source_id, mid, high, max_end);
                invokeAll(leftTask, rightTask);
            } else {
                try {
                    loadDirectly(source_id, low, high);
                } catch (Throwable t) {
                    ApiLogger.error(t, "Error Occurred!");
//                    ((RedisGraphDaoImpl)getRedisDao()).removePhantomMetadata(source_id);
                }
            }
        }


        private void loadDirectly(long source_id, long low, long high) {
            PageResult<Edge> page = getMysqlDao().getEdgesBySource
                    (source_id, true, new Cursor[]{new Cursor(Cursor.CursorName.page_idx, low, high)}, State.NORMAL);
            if (page != null && page.isNotEmpty()) {
                if (hasRedis) {
                    getRedisDao().createEdges(page.getCurrent_page().toArray(new Edge[page.getCurrent_page().size()]));
                    ApiLogger.debug("[GraphDB] rebuild data in redis for " + getMysqlDao().getShardingPolicy().getBizName()
                            + "." + source_id + ", from " + low + " to " + high);
                }
            }
        }

        public long getSource_id() {
            return source_id;
        }

        public void setSource_id(long source_id) {
            this.source_id = source_id;
        }

        public long getLow() {
            return low;
        }

        public void setLow(long low) {
            this.low = low;
        }

        public long getHigh() {
            return high;
        }

        public void setHigh(long high) {
            this.high = high;
        }
    }
}
