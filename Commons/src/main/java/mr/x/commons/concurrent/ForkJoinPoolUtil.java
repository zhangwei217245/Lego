package mr.x.commons.concurrent;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.concurrent.ForkJoinPoolFactoryBean;

import java.util.concurrent.ForkJoinPool;

/**
 * Created by zhangwei on 14-4-7.
 *
 * @author zhangwei
 */
public class ForkJoinPoolUtil implements ApplicationContextAware {

    private static ApplicationContext context;


    public static ForkJoinPool getCommonPool() {
        return context.getBean("forkJoinPoolFactory", ForkJoinPoolFactoryBean.class).getObject();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
