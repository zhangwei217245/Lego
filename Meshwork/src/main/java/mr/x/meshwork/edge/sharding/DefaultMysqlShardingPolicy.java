package mr.x.meshwork.edge.sharding;

import mr.x.commons.dao.JdbcTemplateFactory;
import mr.x.meshwork.edge.utils.GraphUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Created by zhangwei on 14-4-10.
 *
 * By default, for a particular business,
 * sharding in MySQL is carried out by different substantial instances(jdbcTemplateFactories),
 * different virtual schemas, and different tables.
 *
 * If the number of substantial instance/virtual bases/tables is less or equals to 1,
 * then sharding won't happen in the corresponding level.
 *
 * @author zhangwei
 */
public class DefaultMysqlShardingPolicy extends AbstractShardingPolicy {

    private int tableCount;

    private int substantialBaseCount;

    private int virtualBaseCount;

    /**
     * key : bizname_#
     * value: jtf
     */
    private Map<String, JdbcTemplateFactory> jdbcTemplateFactories;


    @Override
    public String getTableId(Object id) {
        if (this.getTableCount() <= 1) {
            return "";
        }
        return String.valueOf(GraphUtils.getHash4split(id, this.getTableCount()));
    }

    @Override
    public String getSubstantialBaseId(Object id) {
        return getBaseId(id, this.getSubstantialBaseCount());
    }

    @Override
    public String getVirtualBaseId(Object id) {
        if (this.getVirtualBaseCount() <= 1) {
            return "";
        }
        return getBaseId(id, this.getVirtualBaseCount());
    }

    private String getBaseId(Object id, int baseCount) {
        if (baseCount <= 1) {
            return this.getBizName() + "_0";
        }
        return this.getBizName() + "_" + GraphUtils.getHash4split(id, baseCount);
    }

    @Override
    public String getShardingTable(Object id, String prefix) {
        String virtualBaseId = getVirtualBaseId(id);

        virtualBaseId = (StringUtils.isBlank(virtualBaseId) ? "" : "`" + virtualBaseId + "`.");

        String tableId = getTableId(id);

        tableId = (StringUtils.isBlank(tableId) ? "" : "_" + tableId);

        return  virtualBaseId + //(`biz(_1)`.)
                "`" + prefix + "_" + getBizName() + tableId + "`"; //`pre_biz(_2)`

    }

    public int getSubstantialBaseCount() {
        return substantialBaseCount;
    }

    public void setSubstantialBaseCount(int substantialBaseCount) {
        this.substantialBaseCount = substantialBaseCount;
    }

    public int getTableCount() {
        return tableCount;
    }

    public void setTableCount(int tableCount) {
        this.tableCount = tableCount;
    }

    public int getVirtualBaseCount() {
        return virtualBaseCount;
    }

    public void setVirtualBaseCount(int virtualBaseCount) {
        this.virtualBaseCount = virtualBaseCount;
    }

    @Override
    public JdbcTemplateFactory getJdbcTemplateFactory(Object id) {
        return jdbcTemplateFactories.get(getSubstantialBaseId(id));
    }

    public Map<String, JdbcTemplateFactory> getJdbcTemplateFactories() {
        return jdbcTemplateFactories;
    }

    public void setJdbcTemplateFactories(Map<String, JdbcTemplateFactory> jdbcTemplateFactories) {
        this.jdbcTemplateFactories = jdbcTemplateFactories;
    }
}
