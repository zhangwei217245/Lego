package x.spirit.sandglass;

import java.util.Collection;
import java.util.Map;

/**
 * @author zhangwei
 */
public abstract class DataTarget<T>{
    
    protected Map<String, String> otherParams;

    public abstract void preAction(Collection<T> lst) throws PreProcessException;
    
    public abstract void writeData(Collection<T> lst);

    public abstract void postAction(Collection<T> lst) throws PostProcessException;
    
    public Map<String, String> getOtherParams() {
        return otherParams;
    }
    
    public void setOtherParams(Map<String, String> otherParams) {
        this.otherParams = otherParams;
    }
    
}
