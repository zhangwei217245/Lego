/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package x.spirit.queryexec;

import com.google.common.collect.Iterables;
import com.tinkerpop.blueprints.Edge;
import java.util.Random;
import java.util.zip.CRC32;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import x.spirit.graphdbbenchmark.GraphDataSource;
import static x.spirit.queryexec.QueryExecutor.high_degree_nodes;

/**
 *
 * @author zhangwei
 */
@Service(value = "queryExecutor")
public class OrientdbQueryExecutor implements QueryExecutor{

    @Resource(name="orientdb")
    private GraphDataSource gds = null;
    
    //read
    @Override
    public void queryOneEdge(long sourceId, long destinationId) {
        gds.readVertex(String.valueOf(sourceId));
    }

    //scan
    @Override
    public void queryAllEdges(long sourceId) {
        gds.scanVertexForEdges(String.valueOf(sourceId));
    }

    //bfsSearch
    @Override
    public void bfsSearch(long sourceId, int depth) {
        gds.traversal(String.valueOf(sourceId), depth);
    }

    @Override
    public void randomHiDegree() {
        Random random = new Random();
        CRC32 crc32 = new CRC32();
        Integer[] seed = new Integer[high_degree_nodes.length];
        int size = 0;
        //shuffling
        while (size < high_degree_nodes.length) {
            int r = random.nextInt();
            crc32.update(r);
            int i = Long.valueOf(crc32.getValue()).intValue() / high_degree_nodes.length % high_degree_nodes.length;
            i = i < 0 ? 0-i : i;
            if (seed[i] == null) {
                seed[i] = high_degree_nodes[size];
                size ++;
            }
        }
        //testing
        for (Integer sourceId : seed){
            long startMillis = System.currentTimeMillis();
            Iterable<Edge> pageResult = gds.scanVertexForEdges(String.valueOf(sourceId));
            long endMillis = System.currentTimeMillis();
            System.out.println(String.format("Time elapsed: %d ms, " + Iterables.size(pageResult) +
                    " records acquired for sourceId " + sourceId, (endMillis - startMillis)));

        }
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
