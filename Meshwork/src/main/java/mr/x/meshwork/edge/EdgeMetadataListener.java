/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mr.x.meshwork.edge;

/**
 * Metadata更新的时候，用来处理关联的更新
 * @author zhangwei
 */
public interface EdgeMetadataListener {
    
    
    public void onUpdated(long source_id, int count, int state, long updated_at);
    
}
