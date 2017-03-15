package mr.x.meshwork.edge.mysql;


import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import mr.x.commons.dao.JdbcTemplateFactory;
import mr.x.commons.models.PageResult;
import mr.x.commons.utils.ApiLogger;
import mr.x.commons.utils.Arrays2;
import mr.x.commons.utils.mysql.SQLGenerator;
import mr.x.meshwork.edge.*;
import mr.x.meshwork.edge.enums.QueryType;
import mr.x.meshwork.edge.enums.WriteType;
import mr.x.meshwork.edge.exception.GraphDBException;
import mr.x.meshwork.edge.sharding.AbstractShardingPolicy;
import mr.x.meshwork.edge.utils.GraphUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.PreparedStatementSetter;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static mr.x.meshwork.edge.Cursor.CursorName;

/**
 * edge_%s:
 *+----------------+-----------------+------+-----+---------+-------+
 *| Field          | Type            | Null | Key | Default | Extra |
 *+----------------+-----------------+------+-----+---------+-------+
 *| source_id      | bigint(20)      | NO   | PRI | NULL    |       |
 *| updated_at     | bigint(20)      | NO   | MUL | 0       |       |
 *| destination_id | bigint(20)      | NO   | PRI | NULL    |       |
 *| state          | tinyint(4)      | NO   | MUL | 1       |       |
 *| position       | int(11)         | NO   | MUL | 0       |       |
 *| category       | tinyint(4)      | NO   |     | 0       |       |
 *| criterion      | varchar(100)    | NO   |     |         |       |
 *| accessory_id   | bigint(20)      | NO   |     | 0       |       |
 *| ext_info       | varbinary(6000) | YES  |     | NULL    |       |
 *+----------------+-----------------+------+-----+---------+-------+
 * 每条边代表一条由出发点到结束点的有向矢量。 尽管理论上我们可以认为反向边代表反过来的关系，例如正向代表关注，反向代表粉丝。
 * 但处于性能的考虑，推荐用bizName将正向关系和反向关系存放在多个表中，以避免混淆。
 * <p/>
 * &lt;pre&gt;
 * 在涉及到社交关系的时候，我们约定：
 * A ->>关注了->> B, 表示为 Edge(table=edge_following, source_id=A, destination_id = B)，也就是A是B的粉丝
 * B ->>的一个粉丝是->> A， 表示为 Edge(table=edge_fans, source_id=B, destination_id = A)，也就是B拥有一个粉丝。
 * &lt;/pre&gt;
 * <p/>
 * 只有当一条边被标记逻辑删除后，才能进行物理删除操作。
 * <p/>
 * meta_edge_%s:
 * +------------+------------+------+-----+---------+-------+
 * | Field      | Type       | Null | Key | Default | Extra |
 * +------------+------------+------+-----+---------+-------+
 * | source_id  | bigint(20) | NO   | PRI | NULL    |       |
 * | count      | int(11)    | NO   |     | NULL    |       |
 * | state      | tinyint(4) | NO   |     | NULL    |       |
 * | updated_at | bigint(20) | NO   | MUL | NULL    |       |
 * +------------+------------+------+-----+---------+-------+
 * 对于meta表的操作规则：
 * 如果一个边被逻辑删除，则meta中的count发生变化。对于物理删除操作，meta中的计数不发生任何变化。
 * 本系统约定计数的递减已经在逻辑删除过程中进行过。
 *
 * @author zhangwei
 */
//@Transactional
public class MysqlGraphDaoImpl extends GraphStorage {

    public static final String EDGE_PREFIX = "edge";
    public static final String META_PREFIX = "meta_edge";

    public static final String EDGE_DROP = "DROP TABLE IF EXISTS %s";
    public static final String EDGE_DDL = "CREATE TABLE IF NOT EXISTS %s (\n" +
            "  `source_id` bigint(20) NOT NULL COMMENT 'user_id',\n" +
            "  `updated_at` bigint(20) NOT NULL DEFAULT '0',\n" +
            "  `destination_id` bigint(20) NOT NULL COMMENT 'feed_id',\n" +
            "  `state` tinyint(4) NOT NULL DEFAULT '1' COMMENT '0 deleted\\n1 normal',\n" +
            "  `position` int(11) NOT NULL DEFAULT '0',\n" +
            "  `category` tinyint(4) NOT NULL DEFAULT '0' COMMENT 'feed_type',\n" +
            "  `criterion` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',\n" +
            "  `accessory_id` bigint(20) NOT NULL DEFAULT '0',\n" +
            "  `ext_info` varbinary(6000) DEFAULT NULL,\n" +
            "  PRIMARY KEY (`source_id`,`destination_id`),\n" +
            "  KEY `idx_state_cate_criterion_accessoryid` (`state`,`category`,`criterion`,`accessory_id`),\n" +
            "  KEY `idx_update` (`updated_at`),\n" +
            "  KEY `idx_position` (`position`)\n" +
            ") ENGINE=MYISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    private static final String METADATA_DROP = "DROP TABLE IF EXISTS %s";
    private static final String METADATA_DDL = "CREATE TABLE IF NOT EXISTS %s (\n" +
            "  `source_id` bigint(20) NOT NULL,\n" +
            "  `count` int(11) NOT NULL,\n" +
            "  `state` tinyint(4) NOT NULL,\n" +
            "  `updated_at` bigint(20) NOT NULL DEFAULT '0',\n" +
            "  PRIMARY KEY (`source_id`),\n" +
            "  KEY `idx_updated_at_state_count` (`updated_at`,`state`,`count`)\n" +
            ") ENGINE=MYISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

    private boolean forceIndex = false;


    @Override
    public void init() {
        // CURRENTLY, NOTHING TO DO
    }
    
    /**
     * 初始化Edge表
     *
     * @param id
     */
    @Override
    public void initiateEdgesTable(String id, boolean dropBeforeCreate) {
        //currently, pass null due to no sharding policy is substantially activated.
        AbstractShardingPolicy shard = getShardingPolicy();
        final String EDGE_TABLE_NAME = shard.getShardingTable(id, EDGE_PREFIX);
        if (dropBeforeCreate) {
            String ddl = String.format(EDGE_DROP, EDGE_TABLE_NAME);
            shard.getJdbcTemplateFactory(id).getWriteJdbcTemplate().execute(ddl);
        }

        String ddl = String.format(EDGE_DDL, EDGE_TABLE_NAME);
        shard.getJdbcTemplateFactory(id)
                .getWriteJdbcTemplate().execute(ddl);
    }

    /**
     * 初始化MetaData表
     *
     * @param id
     */
    @Override
    public void initiateMetadataTable(String id, boolean dropBeforeCreate) {
        //currently, pass null due to no sharding policy is substantially activated.
        AbstractShardingPolicy shard = getShardingPolicy();
        final String META_TABLE_NAME = shard.getShardingTable(id, META_PREFIX);
        if (dropBeforeCreate) {
            String ddl = String.format(METADATA_DROP, META_TABLE_NAME);
            shard.getJdbcTemplateFactory(id).getWriteJdbcTemplate().execute(ddl);
        }

        String ddl = String.format(METADATA_DDL, META_TABLE_NAME);
        shard.getJdbcTemplateFactory(id)
                .getWriteJdbcTemplate().execute(ddl);
    }

    /**
     * 添加指定的边
     *
     * @param edges
     * @return
     */
    @Override
    public int createEdges(Edge... edges) {
        int rstcount = writeEdgeTables(WriteType.CREATE, edges);
        // TODO: maybe use aspect oriented programming?
        writeMetadata(WriteType.CREATE, edges);
        return rstcount;
    }

    /**
     * 逻辑删除边
     *
     * @param edges
     * @return
     */
    @Override
    public int removeEdges(Edge... edges) {
        int rst = writeEdgeTables(WriteType.REMOVE, edges);
        writeMetadata(WriteType.REMOVE, edges);
        return rst;
    }

    /**
     * 物理删除边
     *
     * @param edges
     * @return
     */
    @Override
    public int purgeEdges(Edge... edges) {
        int rst = writeEdgeTables(WriteType.PURGE, edges);
        writeMetadata(WriteType.PURGE, edges);
        return rst;
    }

    /**
     * 获取某source_id为起始点的所有边
     * 默认按照updated_at倒序排序。
     * Cursors可以传入多个游标。
     *    关于游标：
     *    cursorName 可以是page_idx，这个主要是用来翻页的，也就是某固定查询条件下的结果集分页
     *    destination_id/updated_at/position分别是按目标id、更新时间、人工排序值进行范围确认。同时会按该cursorName所指示的字段
     *    进行相应的排序。
     *    当有多个游标传入的时候，以最后一个游标的cursorName所指示的字段名和排序方向进行排序，不支持多列多标准排序。
     *
     * bizFilter: 可以传入State以及Category、Criterion、AccessoryID的实现类。过滤器仅支持等号过滤
     * @param source_id
     * @param needAccurateTotalCount
     * @param cursors
     * @param bizFilters
     * @return
     */
    @Override
    public PageResult<Edge> getEdgesBySource(long source_id, boolean needAccurateTotalCount, Cursor[] cursors, EdgeBizFilter... bizFilters) {
        //为了避免每页返回的内容一样，不传入count值，否则会导致SQL始终后缀LIMIT1，使得翻页失去效果。
        return fullConditionQuery(source_id, null, bizFilters, cursors);
    }

    /**
     * 获取以起始点和结束点确定的一条边 据说即使查询的记录数已经可以确定为1，但是加上LIMIT
     * 1仍然可以帮助MySQL在匹配到一条符合的记录后立即返回，提升性能。
     *
     * @param source_id
     * @param destination_ids
     * @param state
     * @return
     */
    @Override
    public Set<Edge> getEdgesBySourceAndDestinations(long source_id, long[] destination_ids, State state) {
        PageResult<Edge> rst = fullConditionQuery(source_id, destination_ids, new EdgeBizFilter[]{state}, null);
        if (rst == null) {
            return null;
        }
        if (CollectionUtils.isEmpty(rst.getCurrent_page())) {
            return null;
        }
        return new LinkedHashSet<>(rst.getCurrent_page());
    }




    @Override
    public EdgeMetadata getEdgeMetadata(final long source_id) {
        final AbstractShardingPolicy shard = this.getShardingPolicy();
        String META_TABLE_NAME = shard.getShardingTable(source_id, META_PREFIX);
        JdbcTemplateFactory jtf = shard.getJdbcTemplateFactory(source_id);
        List<EdgeMetadata> metas = jtf.getReadJdbcTemplate().query(
                String.format(SQL_SELECT_METADATA_FOR_EDGE, META_TABLE_NAME),
                new PreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setLong(1, source_id);
            }
        }, new BeanPropertyRowMapper<>(EdgeMetadata.class));
        if (CollectionUtils.isNotEmpty(metas)) {
            return metas.get(0);
        }
        return null;
    }

    /**
     * In MySQL, we don't cache advanced QueryResult
     * @param sourceA_id
     * @param sourceB_id
     * @param queryType
     * @return
     */
    @Override
    public List<Long> preFetchAdvancedQueryResult(long sourceA_id, long sourceB_id, QueryType queryType) {
        ApiLogger.info("[%s.%s] Not supported!", this.getClass().getName(), "preFetchAdvancedQueryResult");
        return Collections.emptyList();
    }

    /**
     * In MySQL, we don't cache advanced QueryResult
     * @param sourceA_id
     * @param sourceB_id
     * @param queryType
     * @param queryRst
     */
    @Override
    public void storeAdvancedQueryResult(long sourceA_id, long sourceB_id, QueryType queryType, List<Long> queryRst) {
        ApiLogger.info("[%s.%s] Not supported!", this.getClass().getName(), "storeAdvancedQueryResult");
        return;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    private class BatchEdgePreparedStatementSetter implements BatchPreparedStatementSetter {

        private final Map.Entry<String, List<Edge>> entry;

        private final WriteType writeType;

        public BatchEdgePreparedStatementSetter(Map.Entry<String, List<Edge>> entry, WriteType writeType) {
            this.entry = entry;
            this.writeType = writeType;
        }

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            Edge edge = entry.getValue().get(i);
            this.writeType.setArgsForPreparedStatements(ps, edge);
        }

        @Override
        public int getBatchSize() {
            return entry.getValue().size();
        }

    }

    private int writeEdgeTables(final WriteType writeType, final Edge... edges) {
        if (ArrayUtils.isEmpty(edges)) {
            return 0;
        }
        final AbstractShardingPolicy shard = this.getShardingPolicy();
        Map<String, List<Edge>> projection = shard.projectToTables(EDGE_PREFIX, edges);
        // 根据source_id分到各个具体的表。由于可以定位到具体的某一张表，也就意味着该表下的待插入数据位于同一jt中。
        Collection<Integer> allUpdateCount = Collections2.transform(projection.entrySet(), new Function<Map.Entry<String, List<Edge>>, Integer>() {

            @Override
            public Integer apply(final Map.Entry<String, List<Edge>> entry) {
                try {
                    final String EDGE_TABLE_INDEX = entry.getKey();
                    final String EDGE_TABLE_NAME = EDGE_TABLE_INDEX.split(",")[1];
                    List<Edge> es = entry.getValue();

                    if (CollectionUtils.isEmpty(es)) {
                        return 0;
                    }

                    final JdbcTemplateFactory jtf = shard.getJdbcTemplateFactory(es.get(0).getSource_id());

                    ListenableFuture<Integer> future = writeExecutorService.submit(new Callable<Integer>() {

                        @Override
                        public Integer call() throws Exception {
                            int[] rstcount = jtf.getWriteJdbcTemplate().batchUpdate(String.format(writeType.getSQLForWrite(), EDGE_TABLE_NAME),
                                    new BatchEdgePreparedStatementSetter(entry, writeType));
                            return sumInt(ArrayUtils.toObject(rstcount));
                        }
                    });
                    return Futures.get(future, DBWRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS, GraphDBException.class);
                } catch (GraphDBException ex) {
                    Logger.getLogger(MysqlGraphDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
                    ApiLogger.error(ex, "[%s]failed to insert tables", MysqlGraphDaoImpl.class.getName());
                    return 0;
                }
            }
        });
        int update_count = sumInt(allUpdateCount.toArray(new Integer[allUpdateCount.size()]));
        if (edges.length != update_count) {
            ApiLogger.warn("batch insert abnormally, %d expected but %d inserted indeed.", edges.length, update_count);
        }
        return update_count;
    }

    private PageResult<Edge> fullConditionQuery(long source_id, long[] destination_ids, EdgeBizFilter[] bizFilters, Cursor[] cursors) {
        int totalCount = 0;

        int page_start = 0;
        int page_count = -1;

        if (ArrayUtils.isNotEmpty(destination_ids)) {
            page_count = destination_ids.length;
            totalCount = destination_ids.length;
        } else {
            //获取翻页值
            if (ArrayUtils.isNotEmpty(cursors)) {
                for (Cursor c : cursors) {
                    if (c != null && c.getCursorName() == CursorName.page_idx) {
                        page_start = BigDecimal.valueOf(c.getStart()).intValue();
                        int page_end = BigDecimal.valueOf(c.getEnd()).intValue();
                        page_count = page_end - page_start;
                    }
                }
            }
        }
        
        String condition = getSqlConditions(source_id, destination_ids, bizFilters, cursors);
        
        if (ArrayUtils.isEmpty(cursors) && ArrayUtils.isEmpty(bizFilters)) {
            //获取总数
            EdgeMetadata emeta = getEdgeMetadata(source_id);
            if (emeta != null) {
                totalCount = emeta.getCount();
            }
        } else {
            totalCount = getTotalCountByConditions(source_id, condition);
        }

        if (totalCount <= 0) {
            return PageResult.emptyPage();
        }
        
        List<Edge> rawRst = getEdgesByConditions(source_id, condition, page_start,
                (page_count < 0 ? totalCount : page_count));

        return new PageResult<>(page_start, page_count, rawRst, totalCount);
    }

    private List<Edge> getEdgesByConditions(long source_id, String condition, int page_start, int page_count) {

        final AbstractShardingPolicy shard = this.getShardingPolicy();

        String EDGE_TABLE_NAME = shard.getShardingTable(source_id, EDGE_PREFIX);

        String selectFrom = String.format(SQL_SELECT_EDGES, EDGE_TABLE_NAME);

        //通过MYSQL LIMIT来确定返回页面大小
        String pagination = " LIMIT " + page_start + "," + page_count;

        String sqlForRst = selectFrom + condition + pagination;
        
//        System.out.println("SQL FOR RST:" + sqlForRst);

        JdbcTemplateFactory jtf = shard.getJdbcTemplateFactory(source_id);

        List<Edge> rawRst = jtf.getReadJdbcTemplate().query(sqlForRst, new BeanPropertyRowMapper<>(Edge.class));

        return rawRst;
    }

    private int getTotalCountByConditions(long source_id, String condition) {
        final AbstractShardingPolicy shard = this.getShardingPolicy();

        String EDGE_TABLE_NAME = shard.getShardingTable(source_id, EDGE_PREFIX);

        String selectCountFrom = String.format(SQL_SELECT_EDGES_COUNT, EDGE_TABLE_NAME);

        String sqlForCount = selectCountFrom + condition;
        
//        System.out.println("SQL FOR COUNT:" + sqlForCount);

        JdbcTemplateFactory jtf = shard.getJdbcTemplateFactory(source_id);

        Integer count = jtf.getReadJdbcTemplate().queryForObject(sqlForCount, Integer.class);
        return count == null ? 0 : count;
    }

    public int calibrateTotalCount(long source_id, long[] destination_ids, EdgeBizFilter[] bizFilters, Cursor[] cursors) {
        String sqlCondition = getSqlConditions(source_id, destination_ids, bizFilters, cursors);
        return getTotalCountByConditions(source_id, sqlCondition);
    }

    private String getSqlConditions(long source_id, long[] destination_ids, EdgeBizFilter[] bizFilters, Cursor[] cursors) {

        SQLGenerator sql_gen = new SQLGenerator(false);
        sql_gen.where(null, "source_id = " + source_id);

        String[] des_ids = Arrays2.toStringArray(destination_ids, null);

        if (ArrayUtils.isNotEmpty(destination_ids)) {
            sql_gen.where(null, sql_gen.getIn_clause("destination_id", des_ids));
        }
        String indexForSearch = null;
        Map<Class, List<EdgeBizFilter>> filterMap = GraphUtils.classifyBizFilters(bizFilters);

        if (MapUtils.isNotEmpty(filterMap)) {
            List<EdgeBizFilter> stateFilters = filterMap.get(State.class);
            if (CollectionUtils.isNotEmpty(stateFilters)) {
                Integer[] states = GraphUtils.getStateFilterValues(stateFilters);
                sql_gen.where(null, " state in (" + Joiner.on(", ").skipNulls().join(states) + ")");
                // 只有传入了state的查询才认为是使用了索引idx_state_cate_criterion_accessoryid；
                indexForSearch = "idx_state_cate_criterion_accessoryid";
            }
            List<EdgeBizFilter> categoryFilters = filterMap.get(Category.class);
            if (CollectionUtils.isNotEmpty(categoryFilters)) {
                Integer[] categories = GraphUtils.getCategoryFilterValues(categoryFilters);
                sql_gen.where(null, " category in (" + Joiner.on(", ").skipNulls().join(categories) + ")");
            }
            List<EdgeBizFilter> criterionFilters = filterMap.get(Criterion.class);
            if (CollectionUtils.isNotEmpty(criterionFilters)) {
                String[] criterion = GraphUtils.getCriterionFilterValues(criterionFilters);
                if (ArrayUtils.isNotEmpty(criterion)) {
                    for (int i = 0 ; i < criterion.length; i++) {
                        criterion[i] = "'" + criterion[i]+"'";
                    }
                }
                sql_gen.where(null, " criterion in (" + Joiner.on(", ").skipNulls().join(criterion) + ")");
            }
            List<EdgeBizFilter> accessoryIDFilters = filterMap.get(AccessoryID.class);
            if (CollectionUtils.isNotEmpty(accessoryIDFilters)) {
                String[] accessory_ids = GraphUtils.getAccessoryIDFilterValues(accessoryIDFilters);
                for (String acc_ids_condition : accessory_ids) {
                    if (StringUtils.isNotBlank(acc_ids_condition)) {
                        sql_gen.where(null, " acc_ids_condition " + acc_ids_condition);
                    }
                }

            }
        }

        //默认按照 updated_at 进行排序
        String orderByField = "updated_at";
        String orderByDirection = "DESC";
        String order_idx = "idx_update";

        if (ArrayUtils.isNotEmpty(cursors)) {
            for (Cursor c : cursors) {
                if (c != null) {
                    boolean startInclusive = c.isStartInclusive();
                    boolean endInclusive = c.isEndInclusive();
                    if (c.getCursorName() == CursorName.destination_id) {
                        if (c.getStart() != null) {
                            sql_gen.where(null, "destination_id >" + (startInclusive ? "= " : " ") + c.getStart()); // 相当于设置了since_id
                        }
                        if (c.getEnd() != null) {
                            sql_gen.where(null, "destination_id <" + (endInclusive ? "= " : " ") + c.getEnd()); // 相当于设置了max_id
                        }
                        if (c.getStart() != null || c.getEnd() != null) {
                            order_idx = "PRIMARY";
                        }
                    } else if (c.getCursorName() == CursorName.updated_at) {
                        if (c.getStart() != null) {
                            sql_gen.where(null, "updated_at >" + (startInclusive ? "= " : " ") + c.getStart());
                        }
                        if (c.getEnd() != null) {
                            sql_gen.where(null, "updated_at <" + (endInclusive ? "= " : " ") + c.getEnd());
                        }
                        if (c.getStart() != null || c.getEnd() != null) {
                            order_idx = "idx_update";
                        }
                    } else if (c.getCursorName() == CursorName.position) {
                        if (c.getStart() != null) {
                            sql_gen.where(null, "position >" + (startInclusive ? "= " : " ") + c.getStart());
                        }
                        if (c.getEnd() != null) {
                            sql_gen.where(null, "position <" + (endInclusive ? "= " : " ") + c.getEnd());
                        }
                        if (c.getStart() != null || c.getEnd() != null) {
                            order_idx = "idx_position";
                        }
                    } else if (c.getCursorName() == CursorName.page_idx) {
                        continue;
                    }
                    orderByField = c.getCursorName().name();
                    orderByDirection = c.getCursorDirection().name();
                }
            }
        }


        String useIndexForSearch = indexForSearch == null?"":String.format(" USE INDEX (%s) \n", indexForSearch);
        String useIndexForOrder = String.format(" USE INDEX FOR ORDER BY (%s) \n", order_idx);
        sql_gen.orderBy(orderByField, orderByDirection);;

        return (this.isForceIndex()?(useIndexForSearch + useIndexForOrder):" ") +sql_gen.toString();
    }

    /**
     * 根据writeType来写Metadata，维持数据库中的计数
     *
     * @param writeType
     * @param edges
     */
    private void writeMetadata(final WriteType writeType, Edge... edges) {
        if (ArrayUtils.isEmpty(edges)) {
            return;
        }
        Set<Edge> uniqueEdges = Sets.newHashSet(edges);
        Edge[] edgesArray = uniqueEdges.toArray(new Edge[uniqueEdges.size()]);
        Map<Long, Integer> sourceIdCount = new HashMap<>();
        //整理SourceId和计数
        for (Edge e : edgesArray) {
            Integer count = sourceIdCount.get(e.getSource_id());
            if (count == null) {
                count = 0;
            }
            sourceIdCount.put(e.getSource_id(), ++count);
        }
        
        for (Map.Entry<Long, Integer> entry : sourceIdCount.entrySet()) {
            int count;
            int state = 1;
            switch (writeType) {
                case CREATE:
                    count = entry.getValue();
                    break;
                case REMOVE:
                    count = 0 - entry.getValue();
                    break;
                case PURGE:
                    count = 0;//remove已经减掉过了，purge不用再减掉了。
                    state = 1;
                    break;
                default:
                    count = 0;
                    state = 1;
            }

            EdgeMetadata emeta = new EdgeMetadata(entry.getKey(),count,state,System.nanoTime());
            this.updateEdgeMetadata(emeta);
        }
    }

    @Override
    public int updateEdgeMetadata(EdgeMetadata emeta) {
        final AbstractShardingPolicy shard = this.getShardingPolicy();
        String META_TABLE_NAME = shard.getShardingTable(emeta.getSource_id(), META_PREFIX);
        String EDGE_TABLE_NAME = shard.getShardingTable(emeta.getSource_id(),EDGE_PREFIX);
        JdbcTemplateFactory jtf = shard.getJdbcTemplateFactory(emeta.getSource_id());
        int rst = jtf.getWriteJdbcTemplate().update(String.format(SQL_INSERT_METADATA_FOR_EDGE, META_TABLE_NAME, EDGE_TABLE_NAME)
                , emeta.getSource_id(), emeta.getCount(), emeta.getState(), emeta.getUpdated_at(), emeta.getSource_id());
        
        if (shard.getEdgeMetadataListener() != null) {
            shard.getEdgeMetadataListener().onUpdated(emeta.getSource_id(), emeta.getCount(), emeta.getState(), emeta.getUpdated_at());
        }
        return rst;
    }

    private static int sumInt(Integer[] ints) {
        int rst = 0;
        if (ArrayUtils.isEmpty(ints)) {
            return rst;
        }
        for (Integer i : ints) {
            if (i != null) {
                rst = rst + i;
            }
        }
        return rst;
    }


    public boolean isForceIndex() {
        return forceIndex;
    }

    public void setForceIndex(boolean forceIndex) {
        this.forceIndex = forceIndex;
    }

    // create edges by inserting a record or update a record
    public static final String SQL_CREATE_EDGE = "INSERT INTO %s (source_id, updated_at, "
            + "destination_id, state, position, category, criterion, accessory_id, ext_info) "
            + "VALUES (?, ?, ?, 1, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
            + "updated_at=VALUES(updated_at), "
            + "state=1, "
            + "position=VALUES(position), "
            + "category=VALUES(category), "
            + "criterion=VALUES(criterion), "
            + "accessory_id=VALUES(accessory_id), "
            + "ext_info=VALUES(ext_info)";

    // remove edges logically by updating the state to zero, which respresents the state of REMOVED.
    public static final String SQL_REMOVE_EDGE = "UPDATE %s SET updated_at = ?, "
            + "state = 0 WHERE source_id = ? AND destination_id = ? "
            + "AND updated_at <= ?";

    // purge an edge record from table, only when the record is marked with REMOVED state 物理删除一条边，仅删除已经被逻辑删除的边
    public static final String SQL_PURGE_EDGE = "DELETE FROM %s WHERE source_id = ? AND destination_id = ? AND state = 0";


    private static final String SQL_SELECT_EDGES = "SELECT "
            + "source_id, updated_at, destination_id, state, position, category, criterion, accessory_id, ext_info "
            + "FROM %s ";

    private static final String SQL_SELECT_EDGES_COUNT = "SELECT count(1) FROM %s";

    private static final String SQL_SELECT_METADATA_FOR_EDGE = "SELECT "
            + "source_id, count, state, updated_at FROM %s "
            + "WHERE source_id = ?";


    private static final String SQL_INSERT_METADATA_FOR_EDGE = "INSERT INTO %s"
            + "(source_id, count, state, updated_at) "
            + "VALUES (?, ?, ?, ?) "
            + "ON DUPLICATE KEY "
            + "UPDATE count=(SELECT count(1) FROM %s where source_id = ? AND state = 1), "
            + "state=VALUES(state), updated_at=VALUES(updated_at)";


    public static void main(String[] args) {

        System.out.println(GraphUtils.getHash4split(0L, 4));
        System.out.println(EDGE_DDL);
        System.out.println(EDGE_DROP);
        System.out.println(METADATA_DDL);
        System.out.println(METADATA_DROP);


    }
}
