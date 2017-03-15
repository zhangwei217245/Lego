/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mr.x.meshwork.edge;

import com.google.common.base.Charsets;

/**
 * 模型：代表一条关系或者一条边
 * 
 * 
 *  private long source_id; //出发点id
 *  private long updated_at; //时间戳，用于表示创建时间
 *  private long destination_id; //目标对象ID
 *  private int state; //状态,分为正常和删除两种。
 *  private int position; // 人工排序位置
 *  private int category; // 分类。例如在具体业务中可以用来标记FEED类型、榜单等。
 *  private String criterion;// 扩展排序字段，字符串型
 *  private long accessory_id; // 可能有些情况下需要记录相关的第三方ID。这种信息认为是附件。例如FEED中可以用来记录餐馆ID
 *  private byte[] ext_info; // 扩展信息。其他在业务中用于展示的信息。长度限制为2000字节。可以存JSON序列化或者是JDK序列化。当然愿意存protobuffer序列化也是可以的。
 *
 *  总体来说，可用于排序的字段有：
 *  1. updated_at       mysql_index: idx_update     redis_index_key=10.${CursorName.value}.${bizName}.${source_id}
 *  2. destination_id   mysql_index: PRIMARY        redis_index_key=10.${CursorName.value}.${bizName}.${source_id}
 *  3. position         mysql_index: idx_position   redis_index_key=10.${CursorName.value}.${bizName}.${source_id}
 *
 *  可用于条件式过滤的字段有：
 *  1. state            mysql_index:idx_state_cate_criterion_accessoryid   redis中直接按对应条件进行过滤。
 *  2. category         mysql_index:idx_state_cate_criterion_accessoryid   redis中直接按对应条件进行过滤。
 *  3. criterion        mysql_index:idx_state_cate_criterion_accessoryid   redis中直接按对应条件进行过滤。
 *  4. accessory_id     mysql_index:idx_state_cate_criterion_accessoryid   redis中直接按对应条件进行过滤。
 *
 * @author zhangwei
 */
public class Edge {
    
    private long source_id; //出发点id
    private long updated_at; //时间戳，用于表示创建时间
    private long destination_id; //目标对象ID
    private int state; //状态,分为正常和删除两种。
    private int position = 0; // 人工排序位置
    private int category = 0; // 分类。例如在具体业务中可以用来标记FEED类型、榜单等。
    private String criterion = "";// 扩展排序字段，字符串型
    private long accessory_id = 0L; // 可能有些情况下需要记录相关的第三方ID。这种信息认为是附件。例如FEED中可以用来记录餐馆ID
    private byte[] ext_info; // 扩展信息。其他在业务中用于展示的信息。长度限制为2000字节。可以存JSON序列化或者是JDK序列化。当然愿意存protobuffer序列化也是可以的。


    public Edge() {
        super();
    }
    
    public Edge(long source_id, long destination_id, int state) {
        this(source_id, System.currentTimeMillis(), destination_id, state);
    }

    public Edge(long source_id, long updated_at, long destination_id, int state) {
        this(source_id, updated_at, destination_id, state, 0, "", 0L);
    }

    public Edge(long source_id, long destination_id, int state, int category, String criterion, long accessory_id, String ext_info) {
        this(source_id, System.currentTimeMillis(), destination_id, state, 0, category, criterion, accessory_id, ext_info);
    }

    public Edge(long source_id, long updated_at, long destination_id, int state, int category, String criterion, long accessory_id) {
        this(source_id, updated_at, destination_id, state, 0, category, criterion, accessory_id, null);
    }
    
    public Edge(long source_id, long updated_at, long destination_id, int state, int position, int category, String criterion, long accessory_id, String ext_info) {
        this.source_id = source_id;
        this.updated_at = updated_at;
        this.destination_id = destination_id;
        this.state = state;
        this.position = position;
        this.category = category;
        this.criterion = criterion;
        this.accessory_id = accessory_id;
        this.ext_info = ext_info == null ? null : ext_info.getBytes(Charsets.UTF_8);
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

    /**
     * @return the destination_id
     */
    public long getDestination_id() {
        return destination_id;
    }

    /**
     * @param destination_id the destination_id to set
     */
    public void setDestination_id(long destination_id) {
        this.destination_id = destination_id;
    }

    /**
     * @return the state
     */
    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }
    
    public String getCriterion() {
        return criterion;
    }

    public void setCriterion(String criterion) {
        this.criterion = criterion;
    }

    public long getAccessory_id() {
        return accessory_id;
    }

    public void setAccessory_id(long accessory_id) {
        this.accessory_id = accessory_id;
    }

    public byte[] getExt_info() {
        return ext_info;
    }

    public void setExt_info(byte[] ext_info) {
        this.ext_info = ext_info;
    }

    public String getExt_info_str() {
        return (ext_info == null ? null :
                new String(ext_info, Charsets.UTF_8));
    }

    public void setExt_info_str(String ext_info_str) {
        this.ext_info = ext_info_str == null ? null : ext_info_str.getBytes(Charsets.UTF_8);
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Edge edge = (Edge) o;

        if (destination_id != edge.destination_id) return false;
        if (source_id != edge.source_id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (source_id ^ (source_id >>> 32));
        result = 31 * result + (int) (destination_id ^ (destination_id >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return String.format("Edge{"
                + "source_id=%s, "
                + "updated_at=%s, "
                + "destination_id=%s, "
                + "state=%s, "
                + "position=%s, "
                + "category=%s, "
                + "criterion=%s, "
                + "accessory_id=%s, "
                + "ext_info=%s", 
                source_id, updated_at, 
                destination_id, state, 
                position, category, 
                criterion, accessory_id, 
                this.getExt_info_str());
    }

}
