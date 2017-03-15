package mr.x.meshwork.edge;

/**
 * Created by zhangwei on 14-4-2.
 * @author zhangwei
 *
 * value()方法的实现必须返回正确的标志字串
 *
 */
public interface Criterion extends EdgeBizFilter<String> {

    @Override
    public boolean accept(Edge edge);

    @Override
    public String value();
}
