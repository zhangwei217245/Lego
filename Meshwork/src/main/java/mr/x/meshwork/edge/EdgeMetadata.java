/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mr.x.meshwork.edge;

/**
 * 模型：以某条边的起点为准的Metadata。
 * @author zhangwei
 */
public class EdgeMetadata {
    
    private long source_id;
    private int count;
    private int state;
    private long updated_at;

    public EdgeMetadata() {
        super();
    }

    public EdgeMetadata(long source_id, int count, int state, long updated_at) {
        this.source_id = source_id;
        this.count = count;
        this.state = state;
        this.updated_at = updated_at;
    }

    /**
     * @return the source_id
     */
    public long getSource_id() {
        return source_id;
    }

    /**
     * @param source_id the source_id to set
     */
    public void setSource_id(long source_id) {
        this.source_id = source_id;
    }

    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * @param count the count to set
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * @return the state
     */
    public int getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * @return the updated_at
     */
    public long getUpdated_at() {
        return updated_at;
    }

    /**
     * @param updated_at the updated_at to set
     */
    public void setUpdated_at(long updated_at) {
        this.updated_at = updated_at;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EdgeMetadata that = (EdgeMetadata) o;

        if (source_id != that.source_id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (source_id ^ (source_id >>> 32));
    }


    @Override
    public String toString() {
        return "EdgeMetadata{" +
                "source_id=" + source_id +
                ", count=" + count +
                ", state=" + state +
                ", updated_at=" + updated_at +
                '}';
    }
}
