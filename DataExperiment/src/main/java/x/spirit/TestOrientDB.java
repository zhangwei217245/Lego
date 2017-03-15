/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package x.spirit;

import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections.CollectionUtils;

/**
 *
 * @author zhangwei
 */
public class TestOrientDB {
    
    
    public static List<String> readServerList(String severFileName){
        List<String> serverNames = new ArrayList<>();
        BufferedReader bw = null;
        try {
            File serverFile = new File(severFileName);
            bw = new BufferedReader(new FileReader(serverFile));
            String srvName = null;
            while ((srvName = bw.readLine()) != null) {
                serverNames.add(srvName);
            }
        } catch (IOException ex) {
            Logger.getLogger(TestOrientDB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(TestOrientDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return serverNames;
    }
    
    public static List<Vertex> insertVertex(String url, int... ids){
        Vertex v = null;
        List<Vertex> vlst = new LinkedList<>();
        OrientGraph graph = new OrientGraph(url, "root", "root");
        try {
            for (int id : ids) {
                v = graph.addVertex(Integer.valueOf(id));
                vlst.add(v);
            }
            graph.commit();
        } catch (Throwable t) {
            Logger.getLogger(TestOrientDB.class.getName()).log(Level.SEVERE, null, t);
            graph.rollback();
        } finally {
            graph.shutdown();
            return vlst;
        }
    }
    public static Edge insertEdge(String url, int id, Vertex src, Vertex dst, String lable){
        Edge e = null;
        OrientGraph graph = new OrientGraph(url, "root", "root");
        try {
            e = graph.addEdge(Integer.valueOf(id), src, dst, lable);
            graph.commit();
        } catch (Throwable t) {
            Logger.getLogger(TestOrientDB.class.getName()).log(Level.SEVERE, null, t);
            graph.rollback();
        } finally {
            graph.shutdown();
            return e;
        }
    }
    
    public static void main(String[] args) {
        
        String serverFileName = args[0];
        List<String> serverNames = readServerList(serverFileName);
        if (CollectionUtils.isNotEmpty(serverNames)) {
            for (int i = 0; i < serverNames.size(); i++) {
                String srvName = serverNames.get(i);
                String adminUrl = "remote:"+srvName;
                try{
                    OServerAdmin admin = new OServerAdmin(adminUrl).connect("root", "root");
                    System.out.println(srvName+" connected!");
                    if (i == 0){
                        admin.createDatabase("test", "graph", "plocal");
                        System.out.println("DB 'test' created successfully on " + srvName);
                    }
                }catch (Throwable e) {
                    Logger.getLogger(TestOrientDB.class.getName()).log(Level.SEVERE, null, e);
                }
                
                System.out.println("Waiting for 30 secs...");
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TestOrientDB.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                    
                String url = "remote:" + srvName + "/test";
                //OrientGraph graph = new OrientGraph(url, "root", "root");
//                OrientGraphFactory factory = new OrientGraphFactory(url, "root", "root")
//                        .setupPool(1, 10);
//                OrientGraph graph = factory.getTx();
                System.out.println("url : " + url);
                long currMillis = System.currentTimeMillis();
                int v_num = (int)(currMillis % 1000L);
                List<Vertex> vlst = insertVertex(url, v_num, v_num+1);
                System.out.println("===== insertVertex Done!");
                Edge e = insertEdge(url, i, vlst.get(0), vlst.get(1), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                System.out.println("===== insertEdge Done!");
                
            }
        }
        
    }
}

