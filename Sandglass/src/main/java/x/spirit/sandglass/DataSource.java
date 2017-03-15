package x.spirit.sandglass;

import java.util.Map;

/**
 * @author zhangwei
 */
public abstract class DataSource<T> {


    protected Map<String, String> otherParams;

    public abstract void preAction(long startPosPerThread, long endPosPerThread) throws PreProcessException;

    public abstract ResultModel<T> readData(long startPos, long endPos);

    public abstract void postAction(long startPosPerThread, long endPosPerThread) throws PostProcessException;

    public Map<String, String> getOtherParams() {
        return otherParams;
    }

    public void setOtherParams(Map<String, String> otherParams) {
        this.otherParams = otherParams;
    }
}
