/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mr.x.commons.jedis;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import mr.x.commons.redis.jedis.LegoRedisHandlerFactory;
import mr.x.commons.redis.jedis.LegoSpringRedisHandler;
import mr.x.commons.utils.ApiLogger;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.perf4j.StopWatch;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static mr.x.commons.enums.LegoModules.*;

/**
 *
 * @author zhangwei
 */
//@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/test-mr-commons-context.xml"})
public class JedisSpringTest {
    
    @Resource(name = "testRedisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Resource(name = "testRedisTemplate")
    private RedisTemplate<String, Integer> atomicRedisTemplate;

    @Resource(name = "testRedisTemplate")
    private HashOperations<String, String, Object> hashOperations;

    @Resource(name = "testRedisTemplate")
    private ValueOperations<String, Long> valueOperations;

    private RedisAtomicInteger atomicInteger;

    private LegoSpringRedisHandler handler;

    

    
    public JedisSpringTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        handler = LegoRedisHandlerFactory.getRedisHandler(TEST);
    }
    
    @After
    public void tearDown() {
    }


    //@Test
    public void a0_initTest(){
        atomicInteger = new RedisAtomicInteger("redisCounter", atomicRedisTemplate);
        
        System.out.println(handler.toString());
        System.out.println(handler.commands().toString());
        System.out.println(handler.commands().forStrings().toString());
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    //@Test
    public void a1_basicOrderTest() {
        String value = (String)redisTemplate.execute(new RedisCallback<String>() {

            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                connection.set("abc".getBytes(), "10".getBytes());
                connection.incrBy("abc".getBytes(), -3);
                return new String(connection.get("abc".getBytes()));
            }
        });
        System.out.println("###### "+value);


    }

    //@Test
    public void a2_hashOrderTest() {
        hashOperations.put("person1","name","zhang");
        hashOperations.put("person1","birth",new java.util.Date());

        for (Object o : hashOperations.multiGet("person1", Sets.newHashSet("name", "birth", "gender"))) {
            System.out.println("for-each o:" + o);
        }
        ;
    }

    //@Test
    public void a3_testAtomicLong() throws InterruptedException {
        a0_initTest();
        for (int i=0; i < 10 ; i++) {
            final int base = i;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j<10; j++) {
                        atomicInteger.addAndGet(base * 10 + j + 1);

                    }
                }
            });
            t.start();

        }
        System.out.println("#### sleep for 5000 seconds");
        Thread.sleep(5000);

        System.out.println("########"+atomicInteger.get());


    }

    @Test
    public void a4_testRedisCommands() throws Exception {
        System.out.println("### a4_testRedisCommands");
        StopWatch sw = new StopWatch();
        byte[] key = "a".getBytes(Charsets.UTF_8);
        for (int i = 0; i < 10000; i++) {
            RedisStringCommands stringcommand = handler.commands().forStrings();
            stringcommand.set(key, String.valueOf(i).getBytes(Charsets.UTF_8));
            System.out.println(new String(stringcommand.get(key), Charsets.UTF_8));
        }
        handler.commands().forKeys().del(key);
        Assert.assertNull(handler.commands().forStrings().get(key));
        System.out.println("ElapsedTime: " + sw.getElapsedTime());
    }

    @Test
    public void a5_testApiLogger() throws Exception {
        System.out.println("### a5_testApiLogger");
        for (int i = 0; i < 50; i++) {
            ApiLogger.debug("www%s", "878");
            ApiLogger.info("www%s","878");
            ApiLogger.error("www%s","878");
            ApiLogger.warn("www%s","878");
            ApiLogger.logRequest("www%s", "878");
            ApiLogger.infoForTest("www%s","878");
        }
    }



    
    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public HashOperations<String, String, Object> getHashOperations() {
        return hashOperations;
    }

    public void setHashOperations(HashOperations<String, String, Object> hashOperations) {
        this.hashOperations = hashOperations;
    }
}
