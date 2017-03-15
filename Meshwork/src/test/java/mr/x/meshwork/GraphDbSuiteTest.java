package mr.x.meshwork;

import mr.x.meshwork.edge.mysql.MysqlGraphDaoImplTest;
import mr.x.meshwork.edge.redis.RedisGraphDaoImplTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by zhangwei on 14-3-20.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ MysqlGraphDaoImplTest.class,
        RedisGraphDaoImplTest.class,
        HybridGraphStorageTest.class})
public class GraphDbSuiteTest {
}
