package mr.x.meshwork.edge;


import mr.x.commons.models.PageResult;
import mr.x.meshwork.edge.enums.QueryType;

import java.util.Set;

/**
 * 基于RDBMS的图形数据访问
 * 每个边代表一条从source通往destination的关系
 * 每一个source&lt;--&gt;destination的对应关系都唯一的确定一条边
 * <p/>
 * 不提供查询入边的功能，因为分表分库策略是根据source_id进行的。
 *
 * @author zhangwei
 */
public interface GraphDao {


    public void init();

    /**
     * 根据给定的Edges列表创建一组边
     *
     * @param edges
     * @return
     */
    public int createEdges(Edge... edges);


    /**
     * 根据给定的Edges列表删除一组边
     *
     * @param edges
     * @return
     */
    public int removeEdges(Edge... edges);


    /**
     * 给定的Edges列表清除一组边
     *
     * @param edges
     * @return
     */
    public int purgeEdges(Edge... edges);

    /**
     * 按起始点查询出边，查询结果按照cursors数组最后提供的cursor进行排序
     * 由于REDIS实现只能获取到排序cursor指定的范围内的记录数作为总数，所以如果有精准的总数需求，
     * 需要在性能方面进行折损。目前有两种方案
     * 1. 在redis中获取符合排序cursor所指定的范围的所有记录，求的总数以及过滤后的总数，然后再分页。
     * 2. hybridGraphDaoImpl中穿透到MySql实现进行查询。
     *
     * @param source_id
     * @param needAccurateTotalCount 是否需要精确的总数。
     * @param cursors
     * @param bizFilters
     * @return
     */
    public PageResult<Edge> getEdgesBySource(long source_id, boolean needAccurateTotalCount, Cursor[] cursors, EdgeBizFilter... bizFilters);


    /**
     * 按起始点和结束点查询某几条边, 结果按照s->d的边进行排重
     *
     * @param source_id
     * @param destination_ids
     * @param state
     * @return
     */
    public Set<Edge> getEdgesBySourceAndDestinations(long source_id, long[] destination_ids, State state);


    /**
     * 更新metadata
     *
     * @param emeta
     * @return
     */
    public int updateEdgeMetadata(EdgeMetadata emeta);

    /**
     * 在两个起点的出边中寻找各种集合
     * queryType 有四种取值:
     * intersection获取交集。
     * union获取并集。
     * diff获取A减去B。
     * symmetricDiff获取两边独有的元素。
     * 目前查询结果只支持
     *
     * @param sourceA_id
     * @param sourceB_id
     * @param queryType
     * @param start_idx
     * @param count
     * @return
     */
    public PageResult<Long> advancedQuery(long sourceA_id, long sourceB_id, QueryType queryType, int start_idx, int count);

    /**
     * 根据source_id获取元数据，包括出边的条数，当前起始点的状态等
     *
     * @param source_id
     * @return
     */
    public EdgeMetadata getEdgeMetadata(long source_id);
}