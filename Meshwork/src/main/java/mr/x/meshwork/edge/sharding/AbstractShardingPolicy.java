package mr.x.meshwork.edge.sharding;


import org.apache.commons.lang.StringUtils;
import mr.x.commons.dao.JdbcTemplateFactory;
import mr.x.commons.enums.LegoModules;
import mr.x.meshwork.edge.Edge;
import mr.x.meshwork.edge.EdgeMetadataListener;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import java.util.*;

/**
 * sharding策略，每个业务可以实现自己的sharding Policy
 *
 *
 *
 * @author zhangwei
 */
public abstract class AbstractShardingPolicy implements ShardingPolicy{

    /**
     * 根据传入的ID对象得到分表编号
     * @param id
     * @return
     */
    public abstract String getTableId(Object id);

    /**
     * 根据传入的ID对象得到实体库编号
     * @param id
     * @return
     */
    public abstract String getSubstantialBaseId(Object id);

    /**
     * 根据传入的id对象获得虚拟库编号
     * @param id
     * @return
     */
    public abstract String getVirtualBaseId(Object id);



    public final Map<JdbcTemplateFactory, List<Edge>> projectToJts(List<Edge> edges) {
        if (CollectionUtils.isEmpty(edges)) {
            return Collections.emptyMap();
        }
        Map<JdbcTemplateFactory, List<Edge>> rstmap = new HashMap<>();
        for (Edge e : edges) {
            JdbcTemplateFactory jtf = this.getJdbcTemplateFactory(String.valueOf(e.getSource_id()));
            List<Edge> es;
            if (!rstmap.containsKey(jtf)) {
                es = new ArrayList<>();
                rstmap.put(jtf, es);
            } else {
                es = rstmap.get(jtf);
            }
            es.add(e);
        }
        return rstmap;
    }

    public final Map<String, List<Edge>> projectToTables(String prefix, Edge... edges) {
        if (ArrayUtils.isEmpty(edges)) {
            return Collections.emptyMap();
        }
        Map<String, List<Edge>> rstmap = new HashMap<>();
        for (Edge e : edges) {
            String vir_tab_name = this.getShardingTable(e.getSource_id(),prefix);
            String sub_jt_id = this.getSubstantialBaseId(e.getSource_id());
            String rawPartitionId = StringUtils.isBlank(sub_jt_id) ? ("~,"+vir_tab_name)
                    : (sub_jt_id + "," + vir_tab_name);
            List<Edge> es;
            if (!rstmap.containsKey(rawPartitionId)) {
                es = new ArrayList<>();
                rstmap.put(rawPartitionId, es);
            } else {
                es = rstmap.get(rawPartitionId);
            }
            es.add(e);
        }

        return rstmap;
    }

    private LegoModules LegoModule;
            
    private String bizName;


    
    private EdgeMetadataListener edgeMetadataListener;

    public void setBizName(String bizName) {
        this.bizName = bizName;
    }

    @Override
    public String getBizName() {
        return this.bizName;
    }



    public void setLegoModule(LegoModules LegoModule) {
        this.LegoModule = LegoModule;
    }

    public LegoModules getLegoModule() {
        return LegoModule;
    }

    public EdgeMetadataListener getEdgeMetadataListener() {
        return edgeMetadataListener;
    }

    public void setEdgeMetadataListener(EdgeMetadataListener edgeMetadataListener) {
        this.edgeMetadataListener = edgeMetadataListener;
    }
    
}
