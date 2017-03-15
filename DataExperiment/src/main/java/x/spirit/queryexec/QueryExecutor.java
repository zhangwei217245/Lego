/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package x.spirit.queryexec;

/**
 *
 * @author zhangwei
 */
public interface QueryExecutor {
    
    int[] high_degree_nodes = new int[]{0, 15, 1953, 31250, 31, 61, 244, 977, 3906, 8, 4, 7812, 503, 122, 488, 15625, 2, 125000, 250000, 1};

    void bfsSearch(final long sourceId, int depth);

    void queryAllEdges(long sourceId);

    void queryOneEdge(long sourceId, long destinationId);

    void randomHiDegree();
    
}
