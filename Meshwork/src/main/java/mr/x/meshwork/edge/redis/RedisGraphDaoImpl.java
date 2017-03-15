package mr.x.meshwork.edge.redis;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import mr.x.commons.models.PageResult;
import mr.x.commons.redis.jedis.LegoRedisHandlerFactory;
import mr.x.commons.redis.jedis.LegoSpringRedisHandler;
import mr.x.commons.redis.jedis.JedisStructuralDataHelper;
import mr.x.commons.utils.ApiLogger;
import mr.x.meshwork.edge.*;
import mr.x.meshwork.edge.enums.QueryType;
import mr.x.meshwork.edge.enums.WriteType;
import mr.x.meshwork.edge.sharding.AbstractShardingPolicy;
import mr.x.meshwork.edge.utils.GraphUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.support.collections.RedisMap;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static mr.x.commons.redis.jedis.LegoSpringRedisHandler.DEFAULT_LOCKING_TIMEOUT_MILLIS;
import static mr.x.meshwork.edge.Cursor.CursorDirection;
import static mr.x.meshwork.edge.Cursor.CursorName;

/**
 * Created by zhangwei on 14-3-18.
 * <p/>
 * <p/>
 * 0. key for edges object stored as hash or value(json):
 * key=10.${bizName}.${source_id}.${destination_id} value using redis value,
 * using fastjson for serialization: source_id updated_at destination_id state
 * position category criterion accessory_id ext_info
 * <p/>
 * 1. key for edges index stored as zset in redis:
 * key=10.${CursorName.value}.${bizName}.${source_id} score = ${CursorName.name}
 * , member=destination_id
 * <p/>
 * <p/>
 * 2. key for edge meta stored as hash in redis: key=20.${bizName}.${source_id}
 * fields: state updated_at key for edge meta stored as value in redis:
 * key=20.${bizName}.${source_id}.count value: ${count}
 * <p/>
 * <p/>
 * 3. key for edge advanced query result stored as list in redis;
 * key=3x.${bizName}.${source_idA}.${source_idB} value= ${elements in a list}
 *
 * @author zhangwei
 */
public class RedisGraphDaoImpl extends GraphStorage {

    private static final int DEFAULT_MAX_RETRIEVAL = 5;
    //暂时设定为1分钟
    private final long advancedQueryCacheExpire_Seconds = 60L;
    private LegoSpringRedisHandler redisHandler;
    private int maxIndexRetrieval = DEFAULT_MAX_RETRIEVAL;

    public static String generateKeyForEdgeObj(String bizName, long source_id, long destination_id) {
        return Joiner.on(".").join(GraphRedisNameSpace.edge.getNamespace(),
                bizName, source_id, destination_id);
    }

    public static String generateKeyForEdgeIdx(String bizName, CursorName cn, long source_id) {
        return Joiner.on(".").join(GraphRedisNameSpace.edge.getNamespace(), cn.value(),
                bizName, source_id);
    }

    public static String generateKeyForMetadata(String bizName, long source_id) {
        return Joiner.on('.').join(GraphRedisNameSpace.metadata.getNamespace(),
                bizName, source_id);
    }

    public static String generateKeyForAdvancedQuery(QueryType queryType, String bizName, long sourceA_id, long sourceB_id) {
        return Joiner.on('.').join(GraphRedisNameSpace.advancedQuery.getNamespace(queryType),
                bizName, sourceA_id, sourceB_id);
    }

    public static String generateKeyForPhantomMetadata(String bizName, long source_id) {
        return Joiner.on('.').join(GraphRedisNameSpace.phantom.getNamespace(),
                bizName, source_id);
    }



    public void init() {
        redisHandler = LegoRedisHandlerFactory.getRedisHandler(this.getShardingPolicy().getLegoModule());
    }

    @Override
    public void initiateEdgesTable(String id, boolean dropBeforeCreate) {
        if (dropBeforeCreate) {
            redisHandler.commands().forServer().flushAll();
        }
        ApiLogger.info("[%s.%s]No table to be initiated in redis!", this.getClass().getName(), "initiateEdgesTable");
    }

    @Override
    public void initiateMetadataTable(String id, boolean dropBeforeCreate) {
        if (dropBeforeCreate) {
            redisHandler.commands().forServer().flushAll();
        }
        ApiLogger.info("[%s.%s]No table to be initiated in redis!", this.getClass().getName(), "initiateMetadataTable");
    }

    /**
     * Initialize the PhantomMetadata, set count to the specified count, usually from mysql metadata, and isReady = false;
     * @param source_id
     * @return
     */
    public PhantomMetadata initPhantom(final long source_id, final long count, final Long expireMillis) {
        AbstractShardingPolicy shard = this.getShardingPolicy();
        String phantomKey = generateKeyForPhantomMetadata(shard.getBizName(), source_id);
        // locking
        if (!redisHandler.lockingSupport().lock(phantomKey, DEFAULT_LOCKING_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
            return null;
        }
        if (hasPhantomMetadata(source_id)) {
            return null;
        }
        Map<String, Long> phantomParam = new HashMap<String, Long>(){
            {
                put("count", count);
                put("isReady", 0L);
            }
        };
        RedisMap<String, Long> phantomMap = redisHandler.collections().redisMap(phantomKey);
        phantomMap.putAll(phantomParam);
        if (expireMillis!=null) {
            phantomMap.expire(expireMillis, TimeUnit.MILLISECONDS);
        }
        // unlock
        redisHandler.lockingSupport().unlock(phantomKey);
        return new PhantomMetadata(source_id, phantomMap.get("count"), phantomMap.get("isReady") == 1L);
    }

    /**
     * determine if there is a phantom.
     * @param source_id
     * @return
     */
    public boolean hasPhantomMetadata(long source_id) {
        AbstractShardingPolicy shard = this.getShardingPolicy();
        String phantomKey = generateKeyForPhantomMetadata(shard.getBizName(), source_id);

        return redisHandler.operations().hasKey(phantomKey);
    }
    /**
     * load PhantomMetadata from a specified source_id, only used when determine mainDataSource in HybridStorage.
     * @param source_id
     * @return
     */
    public PhantomMetadata getPhantomMetadata(long source_id) {
        AbstractShardingPolicy shard = this.getShardingPolicy();
        String phantomKey = generateKeyForPhantomMetadata(shard.getBizName(), source_id);
        RedisMap<String, Long> phantomMap = redisHandler.collections().redisMap(phantomKey);
        Long count = phantomMap.get("count");
        boolean isReady = phantomMap.get("isReady") == 1L;
        return new PhantomMetadata(source_id, count, isReady);
    }

    /**
     * set isReady to true
     * @param source_id
     * @return
     */
    public PhantomMetadata setPhantomReady(long source_id) {
        AbstractShardingPolicy shard = this.getShardingPolicy();
        String phantomKey = generateKeyForPhantomMetadata(shard.getBizName(), source_id);
        if (!redisHandler.lockingSupport().lock(phantomKey, DEFAULT_LOCKING_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
            return getPhantomMetadata(source_id);
        }

        RedisMap<String, Long> phantomMap = redisHandler.collections().redisMap(phantomKey);
        phantomMap.put("isReady", 1L);

        redisHandler.lockingSupport().unlock(phantomKey);

        return getPhantomMetadata(source_id);
    }


    public int createEdges(Edge[] edges, boolean ignoreMeta) {

        AbstractShardingPolicy shard = this.getShardingPolicy();
        // 1. create edges objects: using redis value and fastjson serialization
        Map<byte[], byte[]> kvPairsForObj = new HashMap<>(edges.length);
        for (Edge e : edges) {
            byte[] keyForObj = generateKeyForEdgeObj(shard.getBizName(), e.getSource_id(), e.getDestination_id())
                    .getBytes(Charsets.UTF_8);
            e.setState(State.NORMAL.idx());
            byte[] jsonObj = JSON.toJSONString(e).getBytes(Charsets.UTF_8);
            kvPairsForObj.put(keyForObj, jsonObj);
        }
        redisHandler.commands().forStrings().mSet(kvPairsForObj);
        // TODO: maybe set expire or expire for these keys. otherwise, the redis storage could expand rapidly.

        Long createdRecord = 0L;
        // 2. create edges index : destination_id, updated_at, position
        for (CursorName cn : CursorName.values()) {
            if (cn != CursorName.page_idx) {
                Map<Long, Set<ZSetOperations.TypedTuple<Long>>> index = getTypedTupleMap(WriteType.CREATE, cn, edges);
                for (Map.Entry<Long, Set<ZSetOperations.TypedTuple<Long>>> v : index.entrySet()) {
                    Long source_id = v.getKey();
                    Set<ZSetOperations.TypedTuple<Long>> valueSet = v.getValue();
                    String keyForIndex = generateKeyForEdgeIdx(shard.getBizName(), cn, source_id);
                    //ApiLogger.error("well ,this is for test --> values:{}",JSON.toJSON(valueSet));
                    Long inserted = redisHandler.operations().ZSET().add(keyForIndex, valueSet);

                    if (cn == CursorName.updated_at ) {
                        if (!ignoreMeta) {
                            // 3. update metadata if not ignored
                            if (inserted != null && inserted > 0L) {
                                this.updateEdgeMetadata(source_id, 1, inserted.intValue() , false);// increase only
                                createdRecord += inserted;
                            } else {
                                Long zcard = redisHandler.operations().ZSET().size(keyForIndex);
                                if (zcard != null) {
                                    this.updateEdgeMetadata(source_id, 1, zcard.intValue(), true);// reset as zcard
                                } else {
                                    ApiLogger.warn("[createEdges] Ignore update to the count of metadata for %s due to failed zcard command ", source_id);
                                }
                            }
                        }
                    }
                }
            }
        }

        return BigDecimal.valueOf(createdRecord).intValue();

    }
    /**
     * CAUTION:
     * <p/>
     * the returning result of "zadd" order represents for the score of a member
     * may be replaced if the member already exists in which case the result
     * doesn't include the number of members existing for which the score was
     * updated.
     * <p/>
     * while in MySQL, the affected row of "INSERT ON DUPLICATE KEY" could be 1
     * if a new row was inserted, or 2 if a certain originally existing row was
     * updated.
     * <p/>
     * So the returning result may be counter-intuitive and quite confusing.
     * <p/>
     * It is highly recommended that we should only use this returning result
     * when deciding whether the operation is successful, rather than counting
     * the numbers of affected records.
     *
     * @param edges
     * @return
     */
    @Override
    public int createEdges(Edge... edges) {
        return createEdges(edges, false);
    }

    /**
     * 在redis存储中，remove 与purge有着相同的语义，即：逻辑删除不支持
     *
     * @param edges
     * @return
     */
    @Override
    public int removeEdges(Edge... edges) {
        return purgeEdges(edges);
    }

    @Override
    public int purgeEdges(Edge... edges) {
        AbstractShardingPolicy shard = this.getShardingPolicy();

        // 1. delete edges objects
        for (Edge e : edges) {
            byte[] keyForObj = generateKeyForEdgeObj(shard.getBizName(), e.getSource_id(), e.getDestination_id())
                    .getBytes(Charsets.UTF_8);
            redisHandler.commands().forKeys().del(keyForObj);
        }

        // 2. delete in edges index : destination_id, updated_at, position
        Long purgedRecord = 0L;
        for (CursorName cn : CursorName.values()) {
            if (cn != CursorName.page_idx) {
                Map<Long, Set<ZSetOperations.TypedTuple<Long>>> values = getTypedTupleMap(WriteType.PURGE, cn, edges);

                for (Map.Entry<Long, Set<ZSetOperations.TypedTuple<Long>>> v : values.entrySet()) {
                    Long source_id = v.getKey();
                    Set<ZSetOperations.TypedTuple<Long>> valueSet = v.getValue();
                    Collection<Long> d_ids = Collections2.transform(valueSet, new Function<ZSetOperations.TypedTuple<Long>, Long>() {

                        @Override
                        public Long apply(ZSetOperations.TypedTuple<Long> input) {
                            return input.getValue();
                        }
                    });

                    Long[] destination_ids = d_ids.toArray(new Long[d_ids.size()]);

                    //update edges index
                    String keyForIndex = generateKeyForEdgeIdx(shard.getBizName(), cn, source_id);

                    Long removed = redisHandler.operations().ZSET()
                            .remove(keyForIndex, destination_ids);
                    if (cn == CursorName.updated_at) {
                        if (removed != null && removed > 0L) {
                            this.updateEdgeMetadata(source_id, 1, 0 - removed.intValue() , false);// decrease only
                            purgedRecord += removed;
                        } else {
                            Long zcard = redisHandler.operations().ZSET().size(keyForIndex);
                            if (zcard != null) {
                                this.updateEdgeMetadata(source_id, 1, zcard.intValue(), true);// reset as zcard
                            } else {
                                ApiLogger.warn("[purgeEdges] Ignore update to the count of metadata for %s due to failed zcard command ", source_id);
                            }
                        }
                    }
                }
            }
        }

        return BigDecimal.valueOf(purgedRecord).intValue();
    }

    /**
     * In redis storage, we just omit the state, only returns all corresponding
     * edges that exists in redis.
     *
     * @param source_id
     * @param needAccurateTotalCount
     * @param bizFilters
     * @param cursors                in redis, we don't return updated_at, state fields.
     * @return
     */
    @Override
    public PageResult<Edge> getEdgesBySource(long source_id, boolean needAccurateTotalCount, Cursor[] cursors, EdgeBizFilter[] bizFilters) {
        return fullConditionQuery(source_id, null, needAccurateTotalCount, bizFilters, cursors);
    }

    @Override
    public Set<Edge> getEdgesBySourceAndDestinations(long source_id, long[] destination_ids, State state) {
        PageResult<Edge> rst = fullConditionQuery(source_id, destination_ids, false, null, null);
        if (rst == null) {
            return null;
        }
        if (CollectionUtils.isEmpty(rst.getCurrent_page())) {
            return null;
        }
        return new LinkedHashSet<>(rst.getCurrent_page());
    }


    @Override
    public EdgeMetadata getEdgeMetadata(long source_id) {

        AbstractShardingPolicy shard = this.getShardingPolicy();

        final String key = generateKeyForMetadata(shard.getBizName(), source_id);

        if (!redisHandler.getRedisTemplate().hasKey(key)) {
            return null;
        }

        final HashOperations<String, String, Long> hashOperations = redisHandler.operations().HASH();
        List<String> hashKeys = new ArrayList<String>() {
            {
                add("state");
                add("updated_at");
            }
        };
        List<Long> multiValues = hashOperations.multiGet(key, hashKeys);
        Long _state = multiValues.get(0);
        Long _updated_at = multiValues.get(1);
        Long _count = hashOperations.increment(key, "count", 0L);

        long now = System.currentTimeMillis();

        // prepared for zcard
        String keyForIndex = generateKeyForEdgeIdx(shard.getBizName(), CursorName.updated_at, source_id);
        Long zcard = redisHandler.operations().ZSET().size(keyForIndex);


        int count = _count != null ? _count.intValue(): (zcard != null? zcard.intValue() : 0);
        int state = _state != null ? _state.intValue(): 1;
        long updated_at = _updated_at != null ? _updated_at.longValue() : now;


        return new EdgeMetadata(source_id,
                count,
                state,
                updated_at);
    }

    private int updateEdgeMetadata(long source_id, final int state, final int count, boolean isReset) {
        int rst = 0;
        AbstractShardingPolicy shard = this.getShardingPolicy();
        String key = generateKeyForMetadata(shard.getBizName(), source_id);


        HashOperations<String, String, Long> hashOperations = redisHandler.operations().HASH();
        if (!isReset) {
            rst = hashOperations.increment(key, "count", count).intValue();
        } else {
            String keyForDefaultIdx = generateKeyForEdgeIdx(getShardingPolicy().getBizName(), CursorName.updated_at, source_id);
            hashOperations.delete(key, "count");
            rst = hashOperations.increment(key, "count", redisHandler.operations().ZSET().size(keyForDefaultIdx)).intValue();
        }


        Map<String, Long> metaParam = new HashMap<String, Long>() {
            {
                put("state", Long.valueOf(state));
                put("updated_at", System.currentTimeMillis());
            }
        };
        hashOperations.putAll(key, metaParam);

        if (!isReset) {
            //disseminate the count to any listener that has been registered.
            if (shard.getEdgeMetadataListener() != null) {
                shard.getEdgeMetadataListener().onUpdated(source_id, count, state, source_id);
            }
        }
        return rst;

    }

    @Override
    public int updateEdgeMetadata(final EdgeMetadata emeta) {
        int rst = 0;
        if (emeta == null) {
            return rst;
        }
        PhantomMetadata phantom = getPhantomMetadata(emeta.getSource_id());
        if (phantom != null) {
            if (phantom.isReady()) {
                rst = updateEdgeMetadata(emeta.getSource_id(), emeta.getState(), emeta.getCount(), false);
            } else {
                rst = updateEdgeMetadata(emeta.getSource_id(), emeta.getState(), emeta.getCount(), true);
            }
        }
        return rst;
    }


    @Override
    public List<Long> preFetchAdvancedQueryResult(long sourceA_id, long sourceB_id, QueryType queryType) {
        AbstractShardingPolicy shard = this.getShardingPolicy();
        String key = generateKeyForAdvancedQuery(queryType, shard.getBizName(), sourceA_id, sourceB_id);

        return new ArrayList<Long>(redisHandler.collections().redisList(key));
    }

    @Override
    public void storeAdvancedQueryResult(long sourceA_id, long sourceB_id, QueryType queryType, List<Long> queryRst) {
        AbstractShardingPolicy shard = this.getShardingPolicy();
        String key = generateKeyForAdvancedQuery(queryType, shard.getBizName(), sourceA_id, sourceB_id);

        redisHandler.collections().redisList(key).addAll(queryRst);

        redisHandler.collections().redisList(key).expire(advancedQueryCacheExpire_Seconds, TimeUnit.SECONDS);

    }

    private Map<Long, Set<ZSetOperations.TypedTuple<Long>>> getTypedTupleMap(WriteType writeType, CursorName cursorName, Edge... edges) {
        if (ArrayUtils.isEmpty(edges)) {
            return Collections.emptyMap();
        }
        //  source   destination_id-updated_at
        Map<Long, Set<ZSetOperations.TypedTuple<Long>>> edgesMap = new HashMap<>();
        for (Edge e : edges) {
            Set<ZSetOperations.TypedTuple<Long>> v = edgesMap.get(e.getSource_id());
            if (v == null) {
                v = new HashSet<>();
                edgesMap.put(e.getSource_id(), v);
            }

            long score_src = e.getUpdated_at();

            switch (cursorName) {
                case destination_id:
                    score_src = e.getDestination_id();
                    break;
                case position:
                    score_src = e.getPosition();
                    break;
                case updated_at:
                    score_src = e.getUpdated_at();
                    break;
            }
            // TODO: test if this would result in unpredictable loss in terms of precision on the score field
            Double score = BigDecimal.valueOf(score_src).doubleValue();

            DefaultTypedTuple<Long> tt = new DefaultTypedTuple<>(e.getDestination_id(), score);
            v.add(tt);
        }

        for (Set<ZSetOperations.TypedTuple<Long>> tupleSet : edgesMap.values()) {
            JedisStructuralDataHelper.resolveScoreConflictsForTupleSet(tupleSet);
        }
        return edgesMap;
    }

    /**
     * @param source_id
     * @param destination_ids
     * @param filters
     * @param cursors
     * @return
     */
    private PageResult<Edge> fullConditionQuery(long source_id, long[] destination_ids, boolean needAccurateTotalCount, EdgeBizFilter[] filters, Cursor[] cursors) {
        List<Edge> rstEdges = null;

        int page_start = 0;
        int page_count = -1;
        int page_end = -1;
        int totalCount = 0;

        AbstractShardingPolicy shard = this.getShardingPolicy();
        //1. 如果给定了destination_ids数组则认为是查询不连续的记录。

        if (ArrayUtils.isNotEmpty(destination_ids)) {

            rstEdges = multiGetEdges(source_id, Arrays.asList(ArrayUtils.toObject(destination_ids)), shard.getBizName());

            page_start = 0;
            page_count = rstEdges.size();
            totalCount = rstEdges.size();
        } else { //2. 如果没有指定destination_ids，则说明查询的是source_id下所有的边。
            // 首先根据cursors中最后传入的cursor进行选取排序索引，然后再获取Edge对象，并根据其他cursors条件和bizFilters条件进行过滤
            // 当过滤后的结果数小于page_count时，进行增量数据提取。

            Cursor order_cur = null, page_cur = null;
            //2.1) 首先获取排序Cursor, 进而选定索引

            if (ArrayUtils.isNotEmpty(cursors)) {
                for (Cursor c : cursors) {
                    if (c.getCursorName() != CursorName.page_idx) {
                        order_cur = c;
                    } else if (c.getCursorName() == CursorName.page_idx) {
                        page_cur = c;
                    }
                }
            }

            if (totalCount == 0) {
                EdgeMetadata emeta = getEdgeMetadata(source_id);
                if (emeta != null) {
                    totalCount = emeta.getCount();
                }
            }

            if (page_cur != null) {
                page_start = BigDecimal.valueOf(page_cur.getStart()).intValue();
                page_end = BigDecimal.valueOf(page_cur.getEnd()).intValue();
                page_count = page_end - page_start;
            } else {
                page_count = page_count < 0 ? totalCount : page_count;
                page_end = page_start + page_count;
            }

            if (isNeedAccurateTotalCount() || needAccurateTotalCount) {
                // pass "null" to page_start and page_count in order to load All destinationIds
                PageResult<Long> destinationIds = getDestinationIdsByIndex(source_id, order_cur, shard.getBizName(), null, null);
                // load All Edge Objects by multiget order.
                List<Edge> indexEdges = multiGetEdges(source_id, destinationIds.getCurrent_page(), shard.getBizName());
                //根据bizFilter和所有的cursors进行过滤
                rstEdges = filteringEdges(indexEdges, filters, cursors);
                // totalCount is the count after filtering.
                totalCount = rstEdges.size();
                // pagination
                int fromIndex = page_start > rstEdges.size() ? rstEdges.size() : page_start;
                int endIndex = page_end > totalCount ? totalCount : page_end;
                rstEdges = rstEdges.subList(fromIndex, endIndex);
            } else {

                Object[] rst = fetchEdgesGradually(source_id, order_cur, shard.getBizName(), page_start,
                        page_count,
                        cursors, filters);
                totalCount = (Integer) rst[0];
                rstEdges = (List<Edge>) rst[1];
                if (CollectionUtils.isNotEmpty(rstEdges)) {
                    rstEdges = rstEdges.subList(0, page_end > rstEdges.size() ? rstEdges.size() : page_end);
                }
            }
        }
        return new PageResult<>(page_start, page_count, rstEdges, totalCount);
    }

    @Deprecated
    private Object[] fetchEdgesGradually(long source_id, Cursor order_cur, String bizName,
                                         int page_start, int page_count, Cursor[] cursors, EdgeBizFilter... bizFilters) {
        int totalCount = 0;
        int retried = 0;
        List<Edge> rstEdges = new ArrayList<>();
        int offset = page_start;
        int count = page_count;
        while (rstEdges.size() < page_count && retried < maxIndexRetrieval) {

            if (retried > 0) {
                offset = offset + count;
                count = count * (retried + 1);
            }
            //按选定的索引获取指定数目的destination_ids
            PageResult<Long> destinationIds = getDestinationIdsByIndex(source_id, order_cur, bizName, offset, count);
            // 为了防止取不到数据导致的死循环，可以直接跳出。
            if (destinationIds.isEmpty()) {
                break;
            }
            //通过MultiGET命令获取所有边对象
            List<Edge> indexEdges = multiGetEdges(source_id, destinationIds.getCurrent_page(), bizName);
            //根据bizFilter和所有的cursors进行过滤
            rstEdges.addAll(filteringEdges(indexEdges, bizFilters, cursors));
            totalCount = destinationIds.getTotalCount();
            ApiLogger.debug("[XXX] fetchEdgesGradually , retried: " + retried);
            retried++;
        }

        return new Object[]{totalCount, rstEdges};
    }

    private List<Edge> filteringEdges(List<Edge> input, EdgeBizFilter[] bizFilters, Cursor[] cursors) {
        if (CollectionUtils.isEmpty(input)) {
            return Collections.emptyList();
        }

        if (ArrayUtils.isEmpty(bizFilters) && ArrayUtils.isEmpty(cursors)) {
            return input;
        }

        List<Edge> rstEdges = new ArrayList<>();
        for (Edge e : input) {
            boolean filterMatched = ArrayUtils.isNotEmpty(bizFilters) ? matchBizFilters(e, bizFilters) : true;
            boolean cursorMatched = ArrayUtils.isNotEmpty(cursors) ? matchCursor(e, cursors) : true;
            if (filterMatched && cursorMatched) {
                rstEdges.add(e);
            }
        }
        return rstEdges;
    }

    private boolean matchCursor(Edge e, Cursor[] cursors) {
        boolean cursorMatched = true;
        for (Cursor c : cursors) {
            cursorMatched = (c.matched(e) && cursorMatched);
        }
        return cursorMatched;
    }

    private boolean matchBizFilters(Edge e, EdgeBizFilter[] bizFilters) {
        boolean stateMatched = true;
        boolean categoryMatched = true;
        boolean criterionMatched = true;
        boolean accessoryIdMatched = true;

        /**
         * 发现了一些过滤的问题,先临时加了temp变量试试,如果后续发现过滤有问题,可以从这里考虑
         */
        Map<Class, List<EdgeBizFilter>> filterMap = GraphUtils.classifyBizFilters(bizFilters);

        if (MapUtils.isNotEmpty(filterMap)) {
            List<EdgeBizFilter> stateFilters = filterMap.get(State.class);
            if (CollectionUtils.isNotEmpty(stateFilters)) {
                boolean tempStateMatched = false;
                for (EdgeBizFilter<Integer> stateFilter : stateFilters) {
                    tempStateMatched = (stateFilter.accept(e) || tempStateMatched);
                }
                stateMatched = tempStateMatched;
            }
            List<EdgeBizFilter> categoryFilters = filterMap.get(Category.class);
            if (CollectionUtils.isNotEmpty(categoryFilters)) {
                boolean tempCategoryMatched = false;
                for (EdgeBizFilter<Integer> categoryFilter : categoryFilters) {
                    tempCategoryMatched = (categoryFilter.accept(e) || tempCategoryMatched);
                }
                categoryMatched = tempCategoryMatched;

            }
            List<EdgeBizFilter> criterionFilters = filterMap.get(Criterion.class);
            if (CollectionUtils.isNotEmpty(criterionFilters)) {
                boolean tempCriterionMatched = false;
                for (EdgeBizFilter<String> criterionFilter : criterionFilters) {
                    tempCriterionMatched = (criterionFilter.accept(e) || tempCriterionMatched);
                }

                criterionMatched = tempCriterionMatched;
            }
            List<EdgeBizFilter> accessoryIDFilters = filterMap.get(AccessoryID.class);
            if (CollectionUtils.isNotEmpty(accessoryIDFilters)) {
                boolean tempAccessoryIdMatched = false;
                for (EdgeBizFilter<Long> accessoryIdFilter : accessoryIDFilters) {
                    tempAccessoryIdMatched = (accessoryIdFilter.accept(e) || tempAccessoryIdMatched);
                }
                accessoryIdMatched = tempAccessoryIdMatched;
            }
        }
        return stateMatched && categoryMatched && criterionMatched && accessoryIdMatched;
    }

    private PageResult<Long> getDestinationIdsByIndex(long source_id, Cursor order_cur, String bizName, Integer page_start, Integer page_count) {
        // 默认按照时间顺序倒排
        CursorName orderCursorName = CursorName.updated_at;
        CursorDirection orderCursorDirection = CursorDirection.DESC;

        Double orderMin = Double.NEGATIVE_INFINITY;
        Double orderMax = Double.POSITIVE_INFINITY;

        if (order_cur != null) {
            orderCursorName = order_cur.getCursorName();
            if (order_cur.getStart() != null) {
                orderMin = BigDecimal.valueOf(order_cur.getStart()).doubleValue();
            }
            if (order_cur.getEnd() != null) {
                orderMax = BigDecimal.valueOf(order_cur.getEnd()).doubleValue();
            }
            orderCursorDirection = order_cur.getCursorDirection();
        }

        String keyForIndex = generateKeyForEdgeIdx(bizName, orderCursorName, source_id);

        if (page_start != null && page_count != null) {
            Set<Long> orderedDestinationIds = orderCursorDirection == Cursor.CursorDirection.DESC
                    ? redisHandler.operations().ZSET().reverseRangeByScore(keyForIndex, orderMin, orderMax, page_start, page_count)
                    : redisHandler.operations().ZSET().rangeByScore(keyForIndex, orderMin, orderMax, page_start, page_count);

            Long totalCount = redisHandler.operations().ZSET().count(keyForIndex, orderMin, orderMax);

            return new PageResult<>(page_start, page_count, new ArrayList<>(orderedDestinationIds), totalCount == null
                    ? 0 : totalCount.intValue());
        } else {
            Set<Long> orderedDestinationIds = orderCursorDirection == Cursor.CursorDirection.DESC
                    ? redisHandler.operations().ZSET().reverseRangeByScore(keyForIndex, orderMin, orderMax)
                    : redisHandler.operations().ZSET().rangeByScore(keyForIndex, orderMin, orderMax);

            return new PageResult<>(0, orderedDestinationIds.size(), new ArrayList<>(orderedDestinationIds), orderedDestinationIds.size());
        }
    }

    private List<Edge> multiGetEdges(long source_id, Collection<Long> multipleIds, String bizName) {
        List<Edge> rstEdges = Collections.emptyList();
        if (CollectionUtils.isEmpty(multipleIds)) {
            return rstEdges;
        }

        byte[][] keys = new byte[multipleIds.size()][];
        int i = 0;
        for (Long des_id : multipleIds) {
            keys[i] = generateKeyForEdgeObj(bizName, source_id, des_id).getBytes(Charsets.UTF_8);
            i++;
        }

        List<byte[]> multiEdgesBytes = redisHandler.commands().forStrings().mGet(keys);

        rstEdges = new ArrayList<>();
        for (byte[] rstBytes : multiEdgesBytes) {
            if (rstBytes != null) {
                rstEdges.add(JSON.parseObject(new String(rstBytes, Charsets.UTF_8), Edge.class));
            }
        }

        return rstEdges;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    public static enum GraphRedisNameSpace {

        edge(1), metadata(2), advancedQuery(3), phantom(4);

        int namespace;

        GraphRedisNameSpace(int namespace) {
            this.namespace = namespace;
        }

        public int getNamespace() {
            return getNamespace(null);
        }

        public int getNamespace(QueryType queryType) {
            return queryType == null ? namespace * 10 + 0 : namespace * 10 + queryType.ordinal();
        }
    }

    public int getMaxIndexRetrieval() {
        return maxIndexRetrieval;
    }

    public void setMaxIndexRetrieval(int maxIndexRetrieval) {
        this.maxIndexRetrieval = maxIndexRetrieval;
    }

    public static void main(String[] args) {

        Edge edge = new Edge(1L, System.currentTimeMillis(), 2L, 1, 0, 0, "m", 3L, "{a:\"abc\"}");

        String json = JSON.toJSONString(edge);

        Edge e = JSON.parseObject(json, Edge.class);
        System.out.println(json);
        System.out.println(e.toString());
    }
}
