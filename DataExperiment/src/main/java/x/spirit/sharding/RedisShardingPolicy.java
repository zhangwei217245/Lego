package x.spirit.sharding;

import mr.x.commons.dao.JdbcTemplateFactory;
import mr.x.meshwork.edge.sharding.AbstractShardingPolicy;

/**
 * Created by zhangwei on 5/13/16.
 */
public class RedisShardingPolicy extends AbstractShardingPolicy{
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
