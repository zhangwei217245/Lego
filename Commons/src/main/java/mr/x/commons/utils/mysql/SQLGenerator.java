package mr.x.commons.utils.mysql;

/**
 *
 * @author x-spirit
 */
public class SQLGenerator implements QLGenerator {

    private String select_clause;
    private String from_clause;
    private String where_clause;
    private String groupby_clause;
    private String having_clause;
    private String orderby_clause;
    private final boolean isStrictMode;

    @Override
    public final void init() {
        this.select_clause = SELECT;
        this.from_clause = FROM;
        this.where_clause = WHERE;
        this.groupby_clause = GROUP_BY;
        this.having_clause = HAVING;
        this.orderby_clause = ORDER_BY;
    }

    /**
     * 默认的构造器，SQL生成器会工作在Strict模式下，构造出的SQL一定是含有SELECT和FROM分句的
     * 否则调用toString()的时候会返回null
     */
    public SQLGenerator() {
        this.isStrictMode = true;
        init();
    }

    /**
     * 可以指定SQL生成器是否工作在Strict模式下。
     * @param strictMode
     */
    public SQLGenerator(boolean strictMode) {
        this.isStrictMode = strictMode;
        init();
    }

    @Override
    public String getSelect_clause() {
        return select_clause;
    }

    /**
     * 拼接 SELECT 分句。每次调用，就会拼接一个 SELECT 查询项，但多次调用不会造成出现多个 SELECT 关键字。<br/>
     * 如果 select_clause 直接传入 SELECT 这样的关键字，则 SELECT 语句复位。<br/>
     * 复位后 SELECT 分句将不再会拼接到最终的SQL语句中。
     * @param select_clause  传入查询项。
     * @return
     */
    @Override
    public SQLGenerator select(String select_clause) {
        if (!isEmptyString(select_clause)) {
            this.select_clause = (this.select_clause.startsWith(SELECT) ? this.select_clause : SELECT);
            this.select_clause = this.select_clause + (this.select_clause.equals(SELECT) ? " " : ", ")
                    + select_clause;
            if (SELECT.equalsIgnoreCase(select_clause)) {
                this.select_clause = SELECT;
            }
        }
        return this;
    }

    /**
     * 拼接DELETE分句。
     * @return
     */
    @Override
    public SQLGenerator delete() {
        this.select_clause = DELETE;
        return this;
    }

    /**
     * 拼接UPDATE分句。
     * @param update_clause
     * @return
     */
    @Override
    public SQLGenerator update(String update_clause) {
        if (!isEmptyString(update_clause)) {
            this.select_clause = UPDATE + " " + update_clause;
            if (UPDATE.equalsIgnoreCase(update_clause)) {
                this.select_clause = UPDATE;
            }
        }
        return this;
    }

    @Override
    public String getFrom_clause() {
        return from_clause;
    }

    /**
     * 拼接 FROM 分句。每次调用，就会拼接一个 表名，但多次调用不会造成出现多个 FROM 关键字。<br/>
     * 如果 from_clause 直接传入 FROM 这样的关键字，则 FROM 语句复位。<br/>
     * 复位后 FROM 分句将不再会拼接到最终的SQL语句中。
     * @param from_clause 每次传入一个表名，就会自动拼接
     * @return
     */
    @Override
    public SQLGenerator from(String from_clause) {
        if (!isEmptyString(from_clause)) {
            this.from_clause = (this.from_clause.startsWith(FROM) ? this.from_clause : FROM);
            this.from_clause = this.from_clause
                    + (this.from_clause.equals(FROM) ? " " : ", ") + from_clause;
            if (FROM.equalsIgnoreCase(from_clause)) {
                this.from_clause = FROM;
            }
        }
        return this;
    }

    /**
     * 拼接 SET 分句。每次调用，就会拼接一个 表名，但多次调用不会造成出现多个 SET 关键字。<br/>
     * 如果 set_clause 直接传入 SET 这样的关键字，则 SET 语句复位。<br/>
     * 复位后 SET 分句将不再会拼接到最终的SQL语句中。
     * @param set_clause 每次传入一个表名，就会自动拼接
     * @return
     */
    @Override
    public SQLGenerator set(String set_clause) {
        if (!isEmptyString(set_clause)) {
            this.from_clause = (this.from_clause.startsWith(SET) ? this.from_clause : SET);
            this.from_clause = this.from_clause
                    + (this.from_clause.equals(SET) ? " " : ", ") + set_clause;
            if (SET.equalsIgnoreCase(set_clause)) {
                this.from_clause = SET;
            }
        }
        return this;
    }

    @Override
    public String getWhere_clause() {
        return where_clause;
    }

    /**
     * 拼接 WHERE 分句。每次调用，就会拼接一个 WHERE 条件，但多次调用不会造成出现多个 WHERE 关键字。<br/>
     * 如果 where_clause 直接传入 WHERE 这样的关键字，则 WHERE 语句复位。<br/>
     * 复位后 WHERE 分句将不再会拼接到最终的SQL语句中。
     * @param logical_operator 逻辑运算符，比如AND 或者OR。
     * @param where_clause WHERE查询分句，每传入一个查询分句，则向后拼接一个用逗号分隔的查询标准
     * @return
     */
    @Override
    public SQLGenerator where(String logical_operator, String where_clause) {
        if (!isEmptyString(where_clause)) {
            this.where_clause = this.where_clause + (this.where_clause.equals(WHERE) ? " " : " " + (isEmptyString(logical_operator) ? "AND" : logical_operator) + " ") + where_clause;
            if (WHERE.equalsIgnoreCase(where_clause)) {
                this.where_clause = WHERE;
            }
        }
        return this;
    }

    public String getGroupby_clause() {
        return groupby_clause;
    }

    /**
     * 拼接 GROUP BY 分句。每次调用，就会拼接一个 GROUP BY 条件，但多次调用不会造成出现多个 GROUP BY 关键字。<br/>
     * 如果groupby_clause直接传入 GROUP BY 这样的关键字，则 GROUP BY 语句复位。<br/>
     * 复位后 GROUP BY 分句将不再会拼接到最终的SQL语句中。
     * @param groupby_clause 分组条件
     * @return
     */
    @Override
    public SQLGenerator groupBy(String groupby_clause) {
        if (!isEmptyString(groupby_clause)) {
            this.groupby_clause = this.groupby_clause + (this.groupby_clause.equals(GROUP_BY) ? " " : ", ") + groupby_clause;
            if (GROUP_BY.equalsIgnoreCase(groupby_clause)) {
                this.groupby_clause = GROUP_BY;
            }
        }

        return this;
    }

    @Override
    public String getHaving_clause() {
        return having_clause;
    }

    /**
     * 拼接 HAVING 分句。每次调用，就会拼接一个HAVING条件，但多次调用不会造成出现多个HAVING关键字。<br/>
     * 如果having_clause直接传入HAVING这样的关键字，则HAVING语句复位。<br/>
     * 复位后HAVING分句将不再会拼接到最终的SQL语句中。
     * @param logical_operator 逻辑运算符，比如AND 或者OR。
     * @param having_clause HAVING分句的相关条件
     * @return
     */
    @Override
    public SQLGenerator having(String logical_operator, String having_clause) {
        if (!isEmptyString(having_clause)) {
            this.having_clause = this.having_clause + (this.having_clause.equals(HAVING) ? " " : " " + (isEmptyString(logical_operator) ? "AND" : logical_operator) + " ") + having_clause;
            if (HAVING.equalsIgnoreCase(having_clause)) {
                this.having_clause = HAVING;
            }
        }
        return this;
    }

    @Override
    public String getOrderby_clause() {
        return orderby_clause;
    }

    /**
     * 拼接ORDER BY 语句。每次调用，就会拼接一个ORDER BY条件，但多次调用不会造成出现多个ORDER BY关键字。<br/>
     * 如果orderby_clause直接传入ORDER BY这样的关键字，则ORDER BY语句复位。<br/>
     * 复位后ORDER BY分句将不再会拼接到最终的SQL语句中。
     * @param orderby_clause  ORDER BY条件。
     * @param ascOrDesc 排序标准，递增递减
     * @return
     */
    @Override
    public SQLGenerator orderBy(String orderby_clause, String ascOrDesc) {
        if (!isEmptyString(orderby_clause)) {
            this.orderby_clause = this.orderby_clause + (this.orderby_clause.equals(ORDER_BY) ? " " : ", ") + orderby_clause + " " + (isEmptyString(ascOrDesc) ? "" : ascOrDesc);
            if (ORDER_BY.equalsIgnoreCase(orderby_clause)) {
                this.orderby_clause = ORDER_BY;
            }
        }
        return this;
    }

    private boolean isEmptyString(String s) {
        return s == null || s.length() <= 0;
    }

    /**
     * 获取IN分句。格式：id IN (sps[0],sps[1],....,sps[n])
     * @param id
     * @param sps
     * @return
     */
    @Override
    public String getIn_clause(String id, String... sps) {
        String in = IN;
        if (sps != null && sps.length > 0) {
            for (String s : sps) {
                if (isEmptyString(s) || s.equalsIgnoreCase("null")) {
                    continue;
                }
                if (in.equals(IN)) {
                    in = in + " (" + s;
                } else {
                    in = in + "," + s;
                }
            }
            in += ")";
            return id + " " + in;
        }
        return "";
    }

    /**
     * 拼接最终的 SQL 语句
     * @return
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (this.select_clause.equalsIgnoreCase(SELECT) || this.select_clause.equalsIgnoreCase(UPDATE)) {
            if (isStrictMode) {
                return null;
            } else {
                sb.append("");
            }
        } else {
            sb.append(this.select_clause).append(" ");
        }

        if (this.from_clause.equalsIgnoreCase(FROM) || this.from_clause.equalsIgnoreCase(SET)) {
            if (isStrictMode) {
                return null;
            } else {
                sb.append("");
            }
        } else {
            sb.append(this.from_clause).append(" ");
        }

        if (!this.where_clause.equals(WHERE)) {
            sb.append(this.where_clause).append(" ");
        }
        
        if (this.select_clause.startsWith(SELECT)) {
            if (!this.groupby_clause.equals(GROUP_BY)) {
                sb.append(this.groupby_clause).append(" ");
            }

            if (!this.having_clause.equals(HAVING)) {
                sb.append(this.having_clause).append(" ");
            }

            if (!this.orderby_clause.equals(ORDER_BY)) {
                sb.append(this.orderby_clause).append(" ");
            }
        }
        //System.out.println(sb.toString());
        return sb.toString().trim();
    }

    public static void main(String[] args) {
        //演示如何拼接一个嵌套查询

        SQLGenerator mainquery = new SQLGenerator();
        SQLGenerator subquery = new SQLGenerator();
        String mainsql = "";

        //首先演示如何拼接多个查询条件。
        //传统的字符串拼接：
        mainsql = "select *　from table1 t, table2 s where t.id=s.tid";
        //如果此时需要更改查询条件，或者查询条件异常复杂，那么我们很可能需要这样做：
        String descstr = "输油管道";//此处的descstr可能是我们从某个服务返回的一个字串，用于查询相似的内容
        String where_clause = " and t.pid >1000 "
                + "and s.desc like '%" + descstr + "%'"
                + " and s.name like '%title%'";
        mainsql = mainsql + where_clause;

        //一：如果我们需要动态的拼接多个查询条件，那么这种写法会带来Java代码中夹杂大量的SQL语句。
        //代码的可读性被大量的+号分隔的字符串所降低。并且大段的SQL语句拼接中，一旦由于失误
        //造成了某些字符漏掉，则在后期的调试和维护中将带来很大的不便。

        //二：如果此时我执行了一个SQL查询之后，我希望后面的查询语句不便，而前面的查询项变成select count(*)，
        //一个用来查询符合条件的所有记录，一个用来统计符合条件的
        //记录条数。那么可能按照传统的方法，我需要拼接两个SQL字符串。
        //这也是非常令人头疼的问题，随着程序动态化程度越来越高，传统的使用字符串拼接SQL的方法
        //很难适应程序动态化对SQL查询语句多变性的要求。



        //来看使用SQLGenerator如何避免这两个问题：
        //可以首先关注数据来源表，不必关心select项。当然，先写select分句也是可以的。
        mainquery.from("table1 t").from("table2 s");
        //然后我可以关注select
        mainquery.select("t.id, t,name, s.desc");
        //之后就可以关注where查询条件。
        //第一个查询条件可以不加逻辑运算符
        mainquery.where(null, "t.id=s.tid");
        mainquery.where("and", "s.desc like '%php%'").where("and", "s.age > 25");
        mainquery.where("and", mainquery.getIn_clause("t.id", "1001", "1002", "1003"));

        //现在我们看到代码具有很好的Java语义，并且，更酷的是，我们可以不必太担心SQL语句中某个括号丢失
        //或者混进了某个用于拼接Java字符串的+号之类的问题

        //加入现在需要变换查询项，那么只需要将拼接的SQL存入一个字符串变量中，然后对select分句进行初始化：
        String query1 = mainquery.toString();
        mainquery.select("select");
        //然后重写你的select分句。
        mainquery.select("count(*)");
        //现在再次保存你的查询语句到另外一个变量中：
        String query2 = mainquery.toString();

        System.out.println("query1:" + query1);
        System.out.println("query2:" + query2);


        //演示如何初始化一个SQL QUERY，然后创建一个加排序的分组查询
        mainquery.init();
        mainquery.select("region,SUM(population),SUM(area)").from("bbc").groupBy("region").having(null, "SUM(area)>100000 AND SUM(population)<1000000");
        System.out.println(mainquery.toString());

        //当然，任何事物都不是完美的。使用该类虽然能够在较大的程度上对SQL拼接的正确性和灵活性进行把握，但是有时候，当你
        //从SQL查询分析器中COPY出一个查询语句之后，直接粘贴到代码中更为方便。而要把代码改成这样，似乎就有点得不偿失。
        //不过,你仍然可以用这个类来辅助你构造某个多变的分句：
        String basequery = "from table4 v, table5 u where v.vid = u.uid";
        //指定SQL生成器工作在非Strict模式下，仅用于生成分句片段。
        SQLGenerator queryclip = new SQLGenerator(false);
        queryclip.select("v.name,u.desc");
        String query3 = queryclip.toString() + " " + basequery;
        queryclip.select("select").select("count(*)");
        String query4 = queryclip.toString() + " " + basequery;

        System.out.println("query3: " + query3);
        System.out.println("query4: " + query4);

        QLGenerator delete = new SQLGenerator();
        delete.delete().from("publisher p").where(null, "p.numpid=1");

        System.out.println(delete.toString());
        delete.select("p").select("a").from("Abc a").groupBy("a.numtickage");
        System.out.println(delete.toString());
        delete.update("publisher p").set("p.vc2name='abc'").set("p.numage=2");
        System.out.println(delete.toString());
        
        //又一个拼接查询条件部分的案例：
        
        String baseQuery = "SELECT %s FROM edge_%s ";
        
        QLGenerator conditions = new SQLGenerator(false);
        conditions.where(null, "source_id > 1").where("AND", "destination_id < 1").orderBy("updated_at","ASC");

        System.out.println(baseQuery + conditions.toString());
    }
}
