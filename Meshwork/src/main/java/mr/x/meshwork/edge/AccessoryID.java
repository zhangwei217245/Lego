package mr.x.meshwork.edge;

/**
 * Created by zhangwei on 14-4-2.
 * @author zhangwei
 *
 * value()方法的实现必须返回有效的SQL查询条件
 */
public interface AccessoryID extends EdgeBizFilter<String> {

    @Override
    public boolean accept(Edge edge);

    /**
     * accessoryId filter support sql condition in value() method, please return the sql inequality directly, e.g. "in (1,2,3)" or " > 2000" or "is <not> null"
     * @return
     */
    @Override
    public String value();
}
