package mr.x.commons.redis.jedis;

import mr.x.commons.enums.LegoModules;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by zhangwei on 14-3-26.
 *
 * commons工程下的spring/redis下的Lego-common-redis-context.xml
 * 是自动随spring/Lego-commons-context.xml加载的。
 *
 * 其中定义了这个类。
 *
 * 所有使用这个工厂类获取RedisHandler及其包装类的代码，都应当托管于spring context中，
 * 并且在spring完成bean的构建后配置自动初始化init-method或者@PostConstruct，从而确保
 * 一定可以拿到需要的RedisHandler。
 *
 * RedisHandler可以根据传入的模块枚举获得具体的redisTemplate或者其封装功能。
 *
 *
 *
 * @author zhangwei
 */
public class LegoRedisHandlerFactory implements ApplicationContextAware{

    private static ApplicationContext context;

    public static <HK, HV> LegoSpringRedisHandler<HK, HV> getRedisHandler(LegoModules legoModules) {
        RedisTemplate<String, HV> redisTemplate = getRedisTemplate(legoModules);
        return new DefaultLegoSpringRedisHandler<>(redisTemplate);
    }

    private static <String, HV> RedisTemplate<String, HV> getRedisTemplate(LegoModules legoModules) {
        return context.getBean(legoModules.name().toLowerCase() + "RedisTemplate", RedisTemplate.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }


}
