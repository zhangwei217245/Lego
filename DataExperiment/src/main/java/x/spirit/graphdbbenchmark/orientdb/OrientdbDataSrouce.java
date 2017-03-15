/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package x.spirit.graphdbbenchmark.orientdb;

import com.google.common.collect.Iterables;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mr.x.commons.utils.ApiLogger;
import x.spirit.graphdbbenchmark.GraphDataSource;

/**
 *
 * @author zhangwei
 */
public class OrientdbDataSrouce implements GraphDataSource {
    
    private OrientGraphFactory factory;
    
    private OrientBaseGraph graph;
    
    private String url;
    
    private String username;
    
    private String password;
    
    public void init(){
        
        try {
//            String adminUrl = url.replace("/test", "");
//            OServerAdmin serverAdmin = new OServerAdmin(adminUrl)
//                    .connect(username, password);
//            if (!serverAdmin.existsDatabase()) {
//                serverAdmin.createDatabase("test", "graph", "plocal");
//            }
//            Thread.sleep(3000);
            this.factory = new OrientGraphFactory(url, username, password).setupPool(1,10);
            
            // EVERY TIME YOU NEED A GRAPH INSTANCE
            this.graph = this.factory.getNoTx();

        } catch (Exception ex) {
            Logger.getLogger(OrientdbDataSrouce.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void openTx(){
        
    }

    public OrientdbDataSrouce() {
        
        
    }

    public OrientBaseGraph getGraph() {
        return graph;
    }

    public void setGraph(OrientBaseGraph graph) {
        this.graph = graph;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    
    @Override
    public void commit (){
        graph.commit();
    }
    
    public void close(){
        try {
            graph.shutdown();
//            OServerAdmin serverAdmin = new OServerAdmin(url)
//                    .connect(username, password);
//            serverAdmin.dropDatabase("plocal");
            
        } catch (Exception ex) {
            Logger.getLogger(OrientdbDataSrouce.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Vertex insertVertex(String vertex) {
        return graph.addVertex(vertex, "nickname", vertex);
    }

    @Override
    public Edge insertEdge(String srcV, String dstV, String value) {
        long start = System.currentTimeMillis();
        Vertex src = this.insertVertex(srcV);
        long insertSrc = System.currentTimeMillis();
        Vertex dst = this.insertVertex(dstV);
        long insertDst = System.currentTimeMillis();
        Edge e = graph.addEdge(srcV+"-"+dstV, src, dst, value);
        long addEdge = System.currentTimeMillis();
        ApiLogger.info("insert srcV: %d, insert dstV: %d, insert edge: %d\n", insertSrc - start, 
                insertDst - insertSrc, addEdge - insertDst);
        return e;
    }

    @Override
    public Vertex readVertex(String vertex) {
        long start = System.currentTimeMillis();
        Vertex v = graph.getVertex(vertex); 
        long end = System.currentTimeMillis();
        ApiLogger.info("Read vertex %s in %d ms\n", vertex, end-start);
        return v;
    }

    @Override
    public Iterable<Edge> scanVertexForEdges(String vertex) {
        long start = System.currentTimeMillis();
        
        Iterable<Edge> edges = graph.getVertex(vertex).getEdges(Direction.OUT);
        
        long end = System.currentTimeMillis();
        
        ApiLogger.info("Scanned %d for vertex %s in %d ms\n", Iterables.size(edges), vertex, end -start);
        return edges;
    }

    @Override
    public Iterable<Vertex> traversal(String srcVertex, int steps) {
        int i = 0;
        LinkedList<Vertex> srcIds = new LinkedList<>();
        LinkedList<Long> durations = new LinkedList<>();
        Vertex v = graph.getVertex(srcVertex);
        srcIds.add(v);
        while (i < steps) {
            LinkedList<Vertex> dstVs = new LinkedList<>();
            long start = System.currentTimeMillis();
            for (Vertex vtx : srcIds){
                Iterable<Vertex> partialResult = vtx.getVertices(Direction.OUT);
                for (Vertex dstV : partialResult) {
                    dstVs.add(dstV);
                }
            }
            srcIds.clear();
            srcIds.addAll(dstVs);
            long end = System.currentTimeMillis();
            durations.add(end - start);
            dstVs.clear();
            i++;
        }
        ApiLogger.info("BFS Search from %s : %s\n", srcVertex, durations.toString());
        return srcIds;
    }
    
}
