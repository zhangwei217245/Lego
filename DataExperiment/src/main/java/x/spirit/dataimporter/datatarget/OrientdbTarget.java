/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package x.spirit.dataimporter.datatarget;

import java.util.Collection;
import javax.annotation.Resource;
import x.spirit.graphdbbenchmark.Edge;
import x.spirit.graphdbbenchmark.GraphDataSource;
import x.spirit.sandglass.DataTarget;
import x.spirit.sandglass.PostProcessException;
import x.spirit.sandglass.PreProcessException;

/**
 *
 * @author zhangwei
 */
public class OrientdbTarget extends DataTarget<Edge>{
    
    @Resource(name="orientdb")
    private GraphDataSource gds;

    @Override
    public void preAction(Collection<Edge> lst) throws PreProcessException {
        
    }

    @Override
    public void writeData(Collection<Edge> lst) {
        //gds.openTx();
        for (Edge e : lst) {
            gds.insertEdge(e.getSourceV(), e.getDestinationV(), e.getValue());
        }
        //gds.commit();
    }

    @Override
    public void postAction(Collection<Edge> lst) throws PostProcessException {
        
    }

    /**
     * @return the gds
     */
    public GraphDataSource getGds() {
        return gds;
    }

    /**
     * @param gds the gds to set
     */
    public void setGds(GraphDataSource gds) {
        this.gds = gds;
    }
    
    
    
}
