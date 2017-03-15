package mr.x.brookside.amqp.receiver.processor.strategy;

import com.google.common.base.Predicate;

/**
 * Created by zhangwei on 14-6-5.
 *
 * "Whether a certain ProcessingStrategy is applicable according to the business logic",
 * This algorithm is abstracted as an abstract class here.
 *
 * @author zhangwei
 */
public abstract class AbstractProcessStrategyFilter implements Predicate<ProcessingStrategy> {


    @Override
    public boolean apply(ProcessingStrategy strategy) {
        return matched(strategy);
    }

    protected abstract boolean matched(ProcessingStrategy strategy);
}
