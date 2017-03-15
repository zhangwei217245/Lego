package mr.x.meshwork.edge.sharding;

import mr.x.commons.dao.JdbcTemplateFactory;
import org.apache.commons.lang.StringUtils;

/**
 * Created by zhangwei on 14-3-19.
 */
public class MysqlTestingSharding extends AbstractShardingPolicy {

    @Override
    public String getTableId(Object id) {
        return "";//当前不分表
    }

    @Override
    public String getSubstantialBaseId(Object id) {
        return "";//当前不分物理数据库
    }

    @Override
    public String getVirtualBaseId(Object id) {
        return "matrix";//当前不分虚拟库
    }

    @Override
    public String getShardingTable(Object id, String tablePrefix) {
        String tableId = getTableId(id);
        return tablePrefix + "_" + getBizName() + (StringUtils.isBlank(tableId)?"":"_" + tableId);
    }

    @Override
    public JdbcTemplateFactory getJdbcTemplateFactory(Object id) {
        return null;
    }
}
