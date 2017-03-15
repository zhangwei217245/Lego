package mr.x.meshwork.edge;

/**
 * Created by zhangwei on 14-4-2.
 * @author zhangwei
 */
public interface EdgeBizFilter<V> {

    public boolean accept(Edge edge);
    public V value();

}
