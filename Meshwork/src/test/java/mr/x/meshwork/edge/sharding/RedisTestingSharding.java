package mr.x.meshwork.edge.sharding;

import mr.x.commons.dao.JdbcTemplateFactory;

/**
 * Created by zhangwei on 14-3-19.
 */
public class RedisTestingSharding extends AbstractShardingPolicy {

    @Override
    public String getTableId(Object id) {
        return null;
    }

    @Override
    public String getSubstantialBaseId(Object id) {
        return null;
    }

    @Override
    public String getVirtualBaseId(Object id) {
        return null;
    }

    @Override
    public String getShardingTable(Object id, String tablePrefix) {
        return null;
    }

    @Override
    public JdbcTemplateFactory getJdbcTemplateFactory(Object id) {
        return null;
    }
}
