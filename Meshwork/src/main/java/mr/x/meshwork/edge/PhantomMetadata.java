package mr.x.meshwork.edge;

/**
 * Created by zhangwei on 14-5-15.
 *
 *
 */
public class PhantomMetadata {

    private long source_id;
    private long count;
    private boolean isReady;

    public PhantomMetadata(long source_id, long count, boolean isReady) {
        this.source_id = source_id;
        this.count = count;
        this.isReady = isReady;
    }

    public long getSource_id() {
        return source_id;
    }

    public void setSource_id(long source_id) {
        this.source_id = source_id;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

}
