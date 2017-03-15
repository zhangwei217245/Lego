package mr.x.meshwork.edge;

/**
 * Created by zhangwei on 14-4-2.
 * @author zhangwei
 *
 * value()方法的实现必须返回正确的整数，切不能为空
 */
public interface Category extends EdgeBizFilter<Integer> {

    @Override
    boolean accept(Edge edge);

    @Override
    public Integer value();
}
