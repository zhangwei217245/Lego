package x.spirit.graphdbbenchmark;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;


/**
 *
 * @author zhangwei
 */
public interface GraphDataSource {
    
    public Vertex insertVertex(String vertex);
    public Edge insertEdge(String srcV, String dstV, String value);
    public Vertex readVertex(String vertex);
    public Iterable<Edge> scanVertexForEdges(String vertex);
    public Iterable<Vertex> traversal(String srcVertex, int steps);
    public void openTx();
    public void commit();
    
}
