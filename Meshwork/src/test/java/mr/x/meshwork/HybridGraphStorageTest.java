/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mr.x.meshwork;

import mr.x.commons.models.PageResult;
import mr.x.meshwork.edge.*;
import mr.x.meshwork.edge.enums.QueryType;
import mr.x.meshwork.edge.redis.RedisGraphDaoImplTest;
import org.apache.commons.collections.CollectionUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author zhangwei
 */
//@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/test-mr-meshwork-context.xml")
public class HybridGraphStorageTest {


    public static final char[] alphabeta = new char[]{'a','b','c','d','e','f','g','h','i','j','k','l','m','n',
    'o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N',
    'O','P','Q','R','S','T','U','V','W','X','Y','Z'};

    @Resource
    GraphStorage hybridFolloweesStorage;

    public HybridGraphStorageTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of initiateEdgesTable method, of class MysqlGraphDaoImpl.
     */
    @Test
    public void a1_testInitiateEdgesTable() {
        System.out.println("### a1_testInitiateEdgesTable");
        hybridFolloweesStorage.initiateEdgesTable("1", true);
    }

    /**
     * Test of initiateMetadataTable method, of class MysqlGraphDaoImpl.
     */
    @Test
    public void a2_testInitiateMetadataTable() {
        System.out.println("### a2_testInitiateMetadataTable");
        hybridFolloweesStorage.initiateMetadataTable("1", true);
    }

    /**
     * Test of createEdges method, of class MysqlGraphDaoImpl.
     */
    @Test
    public void a3_testCreateEdges() throws InterruptedException {
        System.out.println("### a3_testCreateEdges");
        long now = System.currentTimeMillis();
        List<Edge> edges = new ArrayList<>();
        for (long destination = 2L; destination <= 10L; destination+=1L) {
            long sourceId = 1000L;
            long destinationId = 1000L + destination;
            long accessoryId = 2000L + destination;
            String criterion = String.valueOf(alphabeta, 0, (int) destination);
            String extinfo = "{result:\""+criterion+"\"}";
            Edge e = new Edge(sourceId, System.currentTimeMillis(), destinationId, State.NORMAL.idx(), (int)destination, (int)(100L-destination), criterion, accessoryId, extinfo);
            edges.add(e);
            Thread.sleep(1L);
        }
        for (long destination = 5L; destination <= 12L; destination+=1L) {
            long sourceId = 2000L;
            long destinationId = 1000L + destination;
            long accessoryId = 3000L + destination;
            String criterion = String.valueOf(alphabeta, 0, (int) destination);
            String extinfo = "{result:\""+criterion+"\"}";
            Edge e = new Edge(sourceId, System.currentTimeMillis(), destinationId, State.NORMAL.idx(), (int)destination, (int)(100L-destination), criterion, accessoryId, extinfo);
            edges.add(e);
            Thread.sleep(1L);
        }
        String criterion = String.valueOf(alphabeta, 0, Long.valueOf(10L).intValue());
        String ext_info = "{result:\""+criterion+"\"}";
        
        edges.add(new Edge(1000L, System.currentTimeMillis(), 1010L, State.NORMAL.idx(), (int)10L, (int)(100L-10L), criterion, 2010L, ext_info));

        int dbrst = hybridFolloweesStorage.createEdges(edges.toArray(new Edge[edges.size()]));
        System.out.println(dbrst + " records updated!");
        int totalCount = hybridFolloweesStorage.getEdgeMetadata(1000L).getCount();
        int count = 1;
        Cursor cur = new Cursor(Cursor.CursorName.page_idx, 0, 0);
        for (int i = 0; i < totalCount ; i+=count) {
            cur.setStart((long)i);
            cur.setEnd((long)i+count);
            PageResult<Edge> rst = hybridFolloweesStorage.getEdgesBySource(1000L, false, new Cursor[]{cur}, new State[]{State.NORMAL});
            System.out.println("Show it for 1000!" + rst);
        }
        
        
        totalCount = hybridFolloweesStorage.getEdgeMetadata(1000L).getCount();
        count = 1;
        cur = new Cursor(Cursor.CursorName.page_idx, 0, 0);
        for (int i = 0; i < totalCount ; i+=count) {
            cur.setStart((long)i);
            cur.setEnd((long)i+count);
            PageResult<Edge> rst = hybridFolloweesStorage.getEdgesBySource(2000L, false, new Cursor[]{cur}, new State[]{State.NORMAL});
            System.out.println("Show it for 2000!" + rst);
        }
    }

    /**
     * Test of removeEdges method, of class MysqlGraphDaoImpl.
     */
    @Test
    public void a4_testRemoveEdges() {
        System.out.println("### a4_testRemoveEdges");
        
        Edge[] edges = new Edge[]{new Edge(1000L, 1002L, State.NORMAL.idx()),
                new Edge(1000L, 1003L, State.NORMAL.idx()),
                new Edge(1000L, 1004L, State.NORMAL.idx()),
                new Edge(1000L, 1010L, State.NORMAL.idx()),
                new Edge(1000L, 1010L, State.NORMAL.idx()),
                new Edge(2000L, 1005L, State.NORMAL.idx())};
        int dbrst = hybridFolloweesStorage.removeEdges(edges);
        System.out.println(dbrst + " records updated!");
        int totalCount = hybridFolloweesStorage.getEdgeMetadata(1000L).getCount();
        int count = 1;
        Cursor cur = new Cursor(Cursor.CursorName.page_idx, 0, 0);
        for (int i = 0; i < totalCount ; i+=count) {
            cur.setStart((long)i);
            cur.setEnd((long)i+count);
            PageResult<Edge> rst = hybridFolloweesStorage.getEdgesBySource(1000L, false, new Cursor[]{cur}, new State[]{State.NORMAL});
            System.out.println("Show it!" + rst);
        }
        //TODO: test 1-2,1-3,1-4,1-10 is logically removed but not purged.
    }

    /**
     * Test of purgeEdges method, of class MysqlGraphDaoImpl.
     */
    @Test
    public void a5_testPurgeEdges() {
        System.out.println("### a5_testPurgeEdges");
        long now = System.currentTimeMillis();
        Edge[] edges = new Edge[]{new Edge(1000L, 1002L, State.NORMAL.idx()),
                new Edge(1000L, 1004L, State.NORMAL.idx()),
                new Edge(1000L, 1010L, State.NORMAL.idx()),
                new Edge(1000L, now + 1000L, 1010L, State.NORMAL.idx()),
                };
        int dbrst = hybridFolloweesStorage.purgeEdges(edges);
        System.out.println(dbrst + " records updated!");
        int totalCount = hybridFolloweesStorage.getEdgeMetadata(1000L).getCount();
        int count = 1;
        Cursor cur = new Cursor(Cursor.CursorName.page_idx, 0, 0);
        for (int i = 0; i < totalCount ; i+=count) {
            cur.setStart((long)i);
            cur.setEnd((long)i+count);
            PageResult<Edge> rst = hybridFolloweesStorage.getEdgesBySource(1000L, false, new Cursor[]{cur}, new State[]{State.NORMAL});
            System.out.println("Show it!" + rst);
        }
        //TODO: test 1-3 is logically removed but not purged.
        //TODO: test 1-2, 1-4, 1-10 is already purged
    }

    /**
     * Test of getEdgesBySource method, of class MysqlGraphDaoImpl.
     */
    @Test
    public void a6_testGetEdgesBySource() {
        System.out.println("### a6_testGetEdgesBySource");
        int totalCount = hybridFolloweesStorage.getEdgeMetadata(1000L).getCount();
        int count = 1;
        Cursor cur = new Cursor(Cursor.CursorName.page_idx, 0, 0);
        for (int i = 0; i < totalCount ; i+=count) {
            cur.setStart((long)i);
            cur.setEnd((long)i+count);
            PageResult<Edge> rst = hybridFolloweesStorage.getEdgesBySource(1000L, false, new Cursor[]{cur}, new State[]{State.NORMAL});
            System.out.println("Show it!" + rst);
        }
        
        PageResult<Edge> rst = hybridFolloweesStorage.getEdgesBySource(1000L, false, null, new State[]{State.NORMAL});
        System.out.println("rst="+rst);
    }

    /**
     * Test of getEdgesBySourceAndDestinations method, of class MysqlGraphDaoImpl.
     */
    @Test
    public void a7_testGetEdgeBySourceAndDestination() {
        System.out.println("### a7_testGetEdgeBySourceAndDestination");
        int totalCount = hybridFolloweesStorage.getEdgeMetadata(1000L).getCount();
        int count = 1;
        Cursor cur = new Cursor(Cursor.CursorName.page_idx, 0, 0);
        for (int i = 0; i < totalCount ; i+=count) {
            cur.setStart((long)i);
            cur.setEnd((long)i+count);
            PageResult<Edge> rst = hybridFolloweesStorage.getEdgesBySource(1000L, true, new Cursor[]{cur}, new State[]{State.NORMAL});
            
            Iterator<Edge> it = rst.getCurrent_page().iterator();
            if (it.hasNext()) {
                long destination_id = it.next().getDestination_id();
                assertTrue(CollectionUtils.isNotEmpty(hybridFolloweesStorage.getEdgesBySourceAndDestinations(1000L, new long[]{destination_id}, State.NORMAL)));
            }
        }
        assertTrue(CollectionUtils.isEmpty(hybridFolloweesStorage.getEdgesBySourceAndDestinations(1000L, new long[]{1002L}, State.REMOVED)));
        assertTrue(CollectionUtils.isEmpty(hybridFolloweesStorage.getEdgesBySourceAndDestinations(1000L, new long[]{1002L}, State.NORMAL)));
        assertTrue(CollectionUtils.isNotEmpty(hybridFolloweesStorage.getEdgesBySourceAndDestinations(1000L, new long[]{1003L}, State.REMOVED)));
        assertTrue(CollectionUtils.isEmpty(hybridFolloweesStorage.getEdgesBySourceAndDestinations(1000L, new long[]{1003L}, State.NORMAL)));
    }

    /**
     * Test of getEdgeMetadata method, of class MysqlGraphDaoImpl.
     */
    @Test
    public void a8_testGetEdgeMetadata() {
        System.out.println("### a8_testGetEdgeMetadata");
        EdgeMetadata emeta = hybridFolloweesStorage.getEdgeMetadata(1000L);
        System.out.println(emeta.toString());
    }


    @Test
    public void a9_testAdvancedQuery() {
        System.out.println("### a9_testAdvancedQuery");
        PageResult rst = hybridFolloweesStorage.advancedQuery(1000L, 2000L, QueryType.Intersection, 0, 10);
        System.out.println("intersection:" + rst.toString());
        rst = hybridFolloweesStorage.advancedQuery(1000L, 2000L, QueryType.Union, 0, 10);
        System.out.println("union:" + rst.toString());
        rst = hybridFolloweesStorage.advancedQuery(1000L, 2000L, QueryType.Diff, 0, 10);
        System.out.println("diff 1-2:"+rst.toString());
        rst = hybridFolloweesStorage.advancedQuery(1000L, 2000L, QueryType.Diff, 0, 10);
        System.out.println("diff 2-1:"+rst.toString());
        rst = hybridFolloweesStorage.advancedQuery(1000L, 2000L, QueryType.SymmetricDiff, 0, 10);
        System.out.println("symmetricDiff"+rst.toString());

    }


    /**
     * Test of preFetchAdvancedQueryResult method, of class MysqlGraphDaoImpl.
     */
    @Test
    public void b1_testPreFetchAdvancedQueryResult() {
        System.out.println("### b1_testPreFetchAdvancedQueryResult");
        List<Long> result = hybridFolloweesStorage.preFetchAdvancedQueryResult(1000L, 2000L, QueryType.Intersection);
        System.out.println(result.toString());
    }

    /**
     * Test of storeAdvancedQueryResult method, of class MysqlGraphDaoImpl.
     */
    @Test
    public void b2_testStoreAdvancedQueryResult() {
        System.out.println("### b2_testStoreAdvancedQueryResult");
        hybridFolloweesStorage.storeAdvancedQueryResult(1000L, 2000L, QueryType.Intersection, Arrays.asList(1015L, 1016L));
    }

    /**
     * Test of updateEdgeMetadata method, of class MysqlGraphDaoImpl.
     */
    @Test
    public void b3_testUpdateEdgeMetadata() {
        System.out.println("### b3_testUpdateEdgeMetadata");
        System.out.println("updateEdgeMetadata");
        EdgeMetadata emeta = new EdgeMetadata(1000L, 250, State.NORMAL.ordinal(), System.currentTimeMillis());
        int result = hybridFolloweesStorage.updateEdgeMetadata(emeta);
        System.out.println("updateEdgeMetadata : " + result);
        System.out.println(hybridFolloweesStorage.getEdgeMetadata(1000L).toString());

        emeta.setCount(-250);
        result = hybridFolloweesStorage.updateEdgeMetadata(emeta);
        System.out.println("updateEdgeMetadata : " + result);
        System.out.println(hybridFolloweesStorage.getEdgeMetadata(1000L).toString());

    }


    @Test
    public void b4_testCreateAndRemoveEdgeRepeatedly() {
        System.out.println("### b4_testCreateAndRemoveEdgeRepeatedly");
        long now = System.currentTimeMillis();

        for (int ticker = 0; ticker < 5; ticker++) {
            try {
                // create
                Thread.sleep(50L);
            } catch (InterruptedException ex) {
                Logger.getLogger(RedisGraphDaoImplTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            now = System.currentTimeMillis();
            System.out.println("ticker[create] = " + ticker);
            Edge[] edges = new Edge[]{new Edge(11L, now+ new Random().nextLong() , 2L, State.NORMAL.idx()),
            new Edge(11L, now+new Random().nextLong(), 3L, State.NORMAL.idx())};
            int dbrst = hybridFolloweesStorage.createEdges(edges);
            System.out.println(dbrst + " records updated!");
            int totalCount = hybridFolloweesStorage.getEdgeMetadata(11L).getCount();
            assertEquals(edges.length, totalCount);
            int count = 1;
            Cursor cur = new Cursor(Cursor.CursorName.page_idx, 0, 0);
            for (int i = 0; i < totalCount; i += count) {
                cur.setStart((long) i);
                cur.setEnd((long) i + count);
                PageResult<Edge> rst = hybridFolloweesStorage.getEdgesBySource(11L, false, new Cursor[]{cur}, new State[]{State.NORMAL});
                System.out.println("Show it!" + rst);
            }
            // remove
            System.out.println("ticker[remove] = " + ticker);
            Edge[] edges_rm = new Edge[]{new Edge(11L, now + ticker, 2L, State.NORMAL.idx())};
            dbrst = hybridFolloweesStorage.removeEdges(edges_rm);
            System.out.println(dbrst + " records updated!");
            totalCount = hybridFolloweesStorage.getEdgeMetadata(11L).getCount();
            assertEquals(1, totalCount);
            count = 1;
            cur = new Cursor(Cursor.CursorName.page_idx, 0, 0);
            for (int i = 0; i < totalCount; i += count) {
                cur.setStart((long) i);
                cur.setEnd((long) i + count);
                PageResult<Edge> rst = hybridFolloweesStorage.getEdgesBySource(11L, false, new Cursor[]{cur}, new State[]{State.NORMAL});
                System.out.println("Show it!" + rst);
            }
        }
    }
    
    @Test
    public void b5_testConditionalQuery() {
        int totalCount = hybridFolloweesStorage.getEdgeMetadata(1000L).getCount();
        int count = 2;
        Cursor cur = new Cursor(Cursor.CursorName.page_idx, 0, 0);
        for (int i = 0; i < totalCount ; i+=count) {
            cur.setStart((long)i);
            cur.setEnd((long)i+count);
            PageResult<Edge> rst = hybridFolloweesStorage.getEdgesBySource(1000L, false, new Cursor[]{cur,
            new Cursor(Cursor.CursorName.destination_id, 1005L, 1008L)}, new State[]{State.NORMAL});
            System.out.println("Show it for 1000!" + rst);
        }
        System.out.println("### b5_testConditionalQuery");
        
        for (int i = 0; i < totalCount ; i+=count) {
            cur.setStart((long)i);
            cur.setEnd((long)i+count);
            PageResult<Edge> rst = hybridFolloweesStorage.getEdgesBySource(1000L, false, new Cursor[]{cur,
            new Cursor(Cursor.CursorName.position, 3L, 8L)}, State.NORMAL, State.REMOVED, new Category() {

                @Override
                public boolean accept(Edge edge) {
                    return edge.getCategory() == 95;
                }

                @Override
                public Integer value() {
                    return 95;
                }
            });
            System.out.println("Show it!" + rst);
        }
    }
    
}
