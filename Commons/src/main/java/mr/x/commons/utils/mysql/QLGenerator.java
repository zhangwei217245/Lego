/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mr.x.commons.utils.mysql;

/**
 *
 * @author x-spirit
 */
public interface QLGenerator {
    String FROM = "FROM";
    String GROUP_BY = "GROUP BY";
    String HAVING = "HAVING";
    String ORDER_BY = "ORDER BY";
    String SELECT = "SELECT";
    String WHERE = "WHERE";
    String IN = "IN";
    String DELETE = "DELETE";
    String UPDATE = "UPDATE";
    String SET = "SET";

    String getFrom_clause();

    String getGroupby_clause();

    String getHaving_clause();

    String getOrderby_clause();

    String getSelect_clause();

    String getWhere_clause();

    String getIn_clause(String id,String ...sps);

    void init();

    QLGenerator from(String from_clause);

    QLGenerator set(String set_clause);

    QLGenerator groupBy(String groupby_clause);

    QLGenerator having(String logical_operator, String having_clause);

    QLGenerator orderBy(String orderby_clause, String ascOrDesc);

    QLGenerator select(String select_clause);

    QLGenerator delete();

    QLGenerator update(String select_clause);

    QLGenerator where(String logical_operator, String where_clause);

    @Override
    String toString();

}
