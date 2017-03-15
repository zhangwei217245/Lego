package mr.x.meshwork.edge.enums;


import mr.x.meshwork.edge.Edge;
import mr.x.meshwork.edge.mysql.MysqlGraphDaoImpl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Created by zhangwei on 14-3-19.
 *
 */
public enum WriteType {


    CREATE {

        @Override
        void setPs(PreparedStatement ps, Edge edge) throws SQLException {
            ps.setLong(1, edge.getSource_id());
            ps.setLong(2, edge.getUpdated_at());
            ps.setLong(3, edge.getDestination_id());
            ps.setInt(4, edge.getPosition());
            ps.setInt(5, edge.getCategory());
            ps.setString(6, edge.getCriterion());
            ps.setLong(7, edge.getAccessory_id());

            if (edge.getExt_info() == null) {
                ps.setNull(8, Types.VARBINARY);
            } else {
                ps.setBytes(8, edge.getExt_info());
            }
            
        }

        @Override
        public String getSQLForWrite() {
            return MysqlGraphDaoImpl.SQL_CREATE_EDGE;
        }
    },
    REMOVE {

        @Override
        void setPs(PreparedStatement ps, Edge edge) throws SQLException {
            ps.setLong(1, edge.getUpdated_at());
            ps.setLong(2, edge.getSource_id());
            ps.setLong(3, edge.getDestination_id());
            ps.setLong(4, edge.getUpdated_at());
        }

        @Override
        public String getSQLForWrite() {
            return MysqlGraphDaoImpl.SQL_REMOVE_EDGE;
        }

    },
    PURGE {

        @Override
        void setPs(PreparedStatement ps, Edge edge) throws SQLException {
            ps.setLong(1, edge.getSource_id());
            ps.setLong(2, edge.getDestination_id());
        }

        @Override
        public String getSQLForWrite() {
            return MysqlGraphDaoImpl.SQL_PURGE_EDGE;
        }

    };

    public void setArgsForPreparedStatements(PreparedStatement ps, Edge edge) throws SQLException{
        setPs(ps, edge);
        //TODO: logging here.
    }
    abstract void setPs(PreparedStatement ps, Edge edge) throws SQLException;

    public abstract String getSQLForWrite();
}
