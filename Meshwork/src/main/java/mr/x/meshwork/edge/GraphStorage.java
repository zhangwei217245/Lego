/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mr.x.meshwork.edge;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import mr.x.commons.models.PageResult;
import mr.x.commons.utils.ApiLogger;
import mr.x.meshwork.edge.enums.QueryType;
import mr.x.meshwork.edge.exception.GraphDBException;
import mr.x.meshwork.edge.mysql.MysqlGraphDaoImpl;
import mr.x.meshwork.edge.sharding.AbstractShardingPolicy;
import mr.x.meshwork.edge.redis.RedisGraphDaoImpl;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * 用来封装一些通用的查询，例如集合运算
 * 同时也为具体实现提供一些便利。
 * 
 * 提供redisDao和mysqlDao的注入是为了能够将mysql实现中集合运算的结果在redis实现中进行短暂的缓存
 * 
 * @author zhangwei
 */
public abstract class GraphStorage implements GraphDao, InitializingBean{


    public static final long DBWRITE_TIMEOUT_MS = 10000L;
    public static final long DBQUERY_TIMEOUT_MS = 10000L;
    public static final long REDIS_QUERY_TIMEOUT_MS = 150L;


    protected static final ListeningExecutorService writeExecutorService = MoreExecutors
            .listeningDecorator(
                    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4));

    protected static final ListeningExecutorService readExecutorService = MoreExecutors
            .listeningDecorator(
                    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 8));



    /**
     * 初始化某业务所需的edges表
     * @param id
     */
    public abstract void initiateEdgesTable(String id, boolean dropBeforeCreate);

    /**
     * 初始化某业务所需的Metadata表
     * @param id
     */
    public abstract void initiateMetadataTable(String id, boolean dropBeforeCreate);

    /**
     * 在两个起点的出边中寻找各种集合
     * queryType 有四种取值:
     * intersection获取交集。
     * union获取并集。
     * diff获取A减去B。
     * symmetricDiff获取两边独有的元素。
     *
     * @param sourceA_id
     * @param sourceB_id
     * @param queryType
     * @param start_idx
     * @param count
     * @return
     */
    @Override
    public PageResult advancedQuery(final long sourceA_id, final long sourceB_id, final QueryType queryType, final int start_idx, final int count) {
        List<Long> rst = null;

        rst = preFetchAdvancedQueryResult(sourceA_id, sourceB_id, queryType);

        if (CollectionUtils.isEmpty(rst)) {
            try {
                final CountDownLatch countdown = new CountDownLatch(2);
                // 首先尝试从redisDao获取，如果redisDao可用的话。如果不可用，则从mysqlDao实例获取
                List<ListenableFuture<Set<Long>>> asyncQueries
                        = submitAsyncEdgeQueries(countdown, redisDao == null ? mysqlDao : redisDao
                                , sourceA_id, sourceB_id);

                countdown.await(REDIS_QUERY_TIMEOUT_MS, TimeUnit.MILLISECONDS);

                Set<Long> rstForA = Futures.get(asyncQueries.get(0), GraphDBException.class);
                Set<Long> rstForB = Futures.get(asyncQueries.get(1), GraphDBException.class);

                boolean isRstAEmpty = CollectionUtils.isEmpty(rstForA);
                boolean isRstBEmpty = CollectionUtils.isEmpty(rstForB);

                List<Long> reFetchIds = new ArrayList<>();

                if (isRstAEmpty) {
                    reFetchIds.add(sourceA_id);
                }

                if (isRstBEmpty) {
                    reFetchIds.add(sourceB_id);
                }
                //如果首次获取不成功，则尝试从mySql实例再获取一次
                if (reFetchIds.size() > 0) {
                    final CountDownLatch redoCountdown = new CountDownLatch(reFetchIds.size());

                    asyncQueries = submitAsyncEdgeQueries(redoCountdown, mysqlDao, reFetchIds.toArray(new Long[reFetchIds.size()]));

                    countdown.await(REDIS_QUERY_TIMEOUT_MS, TimeUnit.MILLISECONDS);

                    if (isRstAEmpty && isRstBEmpty) {
                        rstForA = Futures.get(asyncQueries.get(0), GraphDBException.class);
                        rstForB = Futures.get(asyncQueries.get(1), GraphDBException.class);
                    } else if (!isRstAEmpty && isRstBEmpty) {
                        rstForB = Futures.get(asyncQueries.get(0), GraphDBException.class);
                    } else if (!isRstBEmpty && isRstAEmpty) {
                        rstForA = Futures.get(asyncQueries.get(0), GraphDBException.class);
                    }
                }

                rst = queryType.computing(rstForA, rstForB);

                Collections.sort(rst);

                storeAdvancedQueryResult(sourceA_id, sourceB_id, queryType, rst);

            } catch (GraphDBException | InterruptedException ex) {
                Logger.getLogger(MysqlGraphDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
                ApiLogger.error(ex, "[%s] calculating intersection between edges!", MysqlGraphDaoImpl.class.getName());
            }
        }


        return PageResult.paginate(start_idx, count, rst);
    }


    private List<ListenableFuture<Set<Long>>> submitAsyncEdgeQueries(final CountDownLatch countdown, final GraphDao daoImpl, Long... source_ids) {
        List<ListenableFuture<Set<Long>>> futureList = new LinkedList<>();

        GraphDao dao = daoImpl;
        if (daoImpl == null) {
            if (this instanceof MysqlGraphDaoImpl || this instanceof RedisGraphDaoImpl) {
                dao = this;
            }
        }
        final GraphDao graphDao = dao;

        for (final Long source_id : source_ids) {
            futureList.add(
                    readExecutorService.submit(new Callable<Set<Long>>() {

                        @Override
                        public Set<Long> call() throws Exception {
                            Set<Long> rst = new LinkedHashSet<>(Collections2
                                    .transform(graphDao.getEdgesBySource(source_id, false, null, new State[]{State.NORMAL}).getCurrent_page(),
                                            new Function<Edge, Long>() {

                                                @Override
                                                public Long apply(Edge edge) {
//                                                    System.out.println("async query edge : "+ edge);
                                                    return edge.getDestination_id();
                                                }
                                            }));
                            countdown.countDown();
                            return rst;
                        }
                    })
            );
        }

        return futureList;
    }

    private GraphStorage mysqlDao;

    private GraphStorage redisDao;

    private boolean needAccurateTotalCount = false;


    AbstractShardingPolicy shardingPolicy;

    public void setShardingPolicy(AbstractShardingPolicy shardingPolicy) {
        this.shardingPolicy = shardingPolicy;
    }

    public AbstractShardingPolicy getShardingPolicy() {
        return shardingPolicy;
    }


    public GraphStorage getMysqlDao() {
        return mysqlDao;
    }

    public void setMysqlDao(GraphStorage mysqlDao) {
        this.mysqlDao = mysqlDao;
    }

    public GraphStorage getRedisDao() {
        return redisDao;
    }

    public void setRedisDao(GraphStorage redisDao) {
        this.redisDao = redisDao;
    }

    public abstract List<Long> preFetchAdvancedQueryResult(final long sourceA_id, final long sourceB_id, final QueryType queryType);

    public abstract void storeAdvancedQueryResult(final long sourceA_id, final long sourceB_id, final QueryType queryType, List<Long> queryRst);

    public void setNeedAccurateTotalCount(boolean needAccurateTotalCount) {
        this.needAccurateTotalCount = needAccurateTotalCount;
    }

    public boolean isNeedAccurateTotalCount() {
        return needAccurateTotalCount;
    }
}
