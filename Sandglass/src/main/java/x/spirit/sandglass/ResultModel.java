package x.spirit.sandglass;

import java.util.Collection;

/**
 * @author zhangwei
 */
public class ResultModel<V> {

    private Collection<V> result;
    private long lastPos;
    public Collection<V> getResult() {
        return result;
    }
    public void setResult(Collection<V> result) {
        this.result = result;
    }
    public long getLastPos() {
        return lastPos;
    }
    public void setLastPos(long lastPos) {
        this.lastPos = lastPos;
    }
}
