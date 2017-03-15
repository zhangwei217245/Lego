/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mr.x.meshwork.edge;

/**
 *
 * 表示边的状态，有删除和正常两种。
 * 如果边注释为REMOVED状态，则表示边目前处于逻辑删除状态
 * 关系数据库中可以存储这条边的记录，但是查询时不展示。
 * 如果某条边彻底从数据库中删除后，则METADATA的状态设置为REMOVED。
 * 交由后端队列或者处理机进行垃圾回收
 * 
 * @author zhangwei
 */
public enum State implements EdgeBizFilter<Integer>{
    
    
    REMOVED(0),
    NORMAL(1);
    
    int idx; // CAUTION: idx of state must be restricted to the result of ordinal()
    
    State(int idx) {
        this.idx = idx;
    }
    
    public int idx(){
        return idx;
    }



    @Override
    public String toString() {
        return String.valueOf(idx);
    }


    @Override
    public boolean accept(Edge edge) {
        if (edge == null) {
            return false;
        }
        return this.value().equals(edge.getState());
    }

    @Override
    public Integer value() {
        return Integer.valueOf(idx);
    }


}
