package mr.x.meshwork.edge.sharding;


import mr.x.commons.dao.JdbcTemplateFactory;

/**
 * Created by zhangwei on 14-4-10.
 *
 * @author zhangwei
 */
public class DefaultRedisShardingPolicy extends AbstractShardingPolicy {


    @Override
    public String getBizName() {
        return super.getBizName();
    }

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
    public String getShardingTable(Object id, String prefix) {
        return null;
    }

    @Override
    public JdbcTemplateFactory getJdbcTemplateFactory(Object id) {
        return null;
    }
}
