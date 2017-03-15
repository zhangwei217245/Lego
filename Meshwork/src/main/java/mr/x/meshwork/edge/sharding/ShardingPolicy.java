/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mr.x.meshwork.edge.sharding;


import mr.x.commons.dao.JdbcTemplateFactory;

/**
 * 拆分策略接口
 * 每个业务可以实现自己的ShardingPolicy
 * 截止到2014.3.17之前，jdbcTemplateFactory只具备读写分离的功能，Sharding策略都是单库。
 * @author zhangwei
 */
public interface ShardingPolicy {
    
    /**
     * 获取业务名称，用于按业务拆分。
     * @return 
     */
    public String getBizName();
    
    /**
     * 根据传入的ID对象得到数据的分库及分表名称。
     * @param id
     * @return 
     */
    public String getShardingTable(Object id, String tablePrefix);
    
    /**
     * 根据传入的ID对象得到分库的jdbcTemplateFactory
     * @param id
     * @return 
     */
    public JdbcTemplateFactory getJdbcTemplateFactory(Object id);
    
}
