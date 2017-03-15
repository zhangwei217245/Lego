package mr.x.commons.dao;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableList;
import mr.x.commons.utils.ApiLogger;
import mr.x.commons.utils.ConfigUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Reilost on 14-3-4.
 */
public class BaseDAO {


    @Resource
    JdbcTemplateFactory jdbcTemplateFactory;

    public void setJdbcTemplateFactory(JdbcTemplateFactory jdbcTemplateFactory) {
        this.jdbcTemplateFactory = jdbcTemplateFactory;
    }

    public JdbcTemplate getReadJdbcTemplate() {
        return jdbcTemplateFactory.getReadJdbcTemplate();
    }

    public JdbcTemplate getWriteJdbcTemplate() {
        return jdbcTemplateFactory.getWriteJdbcTemplate();
    }

    public NamedParameterJdbcTemplate getReadNamedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(jdbcTemplateFactory.getReadJdbcTemplate());
    }

    public NamedParameterJdbcTemplate getWriteNamedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(jdbcTemplateFactory.getWriteJdbcTemplate());
    }


    /**
     * 目前只有单元测试情况下才生效,防止对线上误操作.
     *
     * @param createDdl
     * @param dropDDL
     * @param dropBeforeCreate
     */
    public void initTable(String createDdl, String dropDDL, boolean dropBeforeCreate) {
        if (ConfigUtil.isUnitTesting()) {
            if (dropBeforeCreate) {
                getWriteJdbcTemplate().execute(dropDDL);

            }
        }
        try {
            getWriteJdbcTemplate().execute(createDdl);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    protected <T> T queryForObject(JdbcTemplate jdbcTemplate, String sql, RowMapper<T> rowMapper, Object... args) {
        if (ApiLogger.isDebugEnabled()) {

        }
        ApiLogger.debug("class:{} ,queryForObject sql: {} ,params:{} ,rowmapper:{}", this.getClass().getSimpleName(), sql, JSON.toJSONString(args), rowMapper.getClass().getSimpleName());
        try {
            T result = jdbcTemplate.queryForObject(sql,
                    rowMapper, args);
            if (result != null) {
                return result;
            }
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            ApiLogger.debug("class:{} ,queryForObject sql: {} ,params:{} ,rowmapper:{} , no_result", this.getClass().getSimpleName(), sql, JSON.toJSONString(args), rowMapper.getClass().getSimpleName());
        }
        return null;
    }

    protected <T> ImmutableList<T> queryForList(JdbcTemplate jdbcTemplate, String sql, RowMapper<T> rowMapper, Object... args) {
        if (ApiLogger.isDebugEnabled())
            ApiLogger.debug("class:{} ,queryForList sql: {} ,params:{} ,rowmapper:{}", this.getClass().getSimpleName(), sql, JSON.toJSONString(args), rowMapper.getClass().getSimpleName());
        try {
            List<T> result = jdbcTemplate.query(sql,
                    rowMapper, args);
            if (CollectionUtils.isNotEmpty(result)) {
                return ImmutableList.copyOf(result);
            }
        } catch (Exception e) {
            ApiLogger.debug("class:{} ,queryForList sql: {} ,params:{} ,rowmapper:{} , no_result", this.getClass().getSimpleName(), sql, JSON.toJSONString(args), rowMapper.getClass().getSimpleName());
        }
        return null;
    }

}
