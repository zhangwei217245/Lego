package mr.x.commons.redis.jedis;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhangwei on 14-4-15.
 *
 * @author zhangwei
 */
public class JedisStructuralDataHelper {



    public static <T> void resolveScoreConflictsForTupleSet(Set<ZSetOperations.TypedTuple<T>> tupleSet) {

        if (CollectionUtils.isEmpty(tupleSet)) {
            return;
        }
        int totalCount = tupleSet.size();
        Set<ZSetOperations.TypedTuple<T>> rstSet = new HashSet<>(totalCount);
        Set<Double> scores = new HashSet<>();
        int i = 0;
        for (ZSetOperations.TypedTuple<T> tuple : tupleSet) {
            Double score = tuple.getScore();
            if (scores.contains(score)) {
                score = score + ((double)i%totalCount/1000);
            }

            rstSet.add(new DefaultTypedTuple<T>(tuple.getValue(), score));
            scores.add(score);
            i++;
        }

        tupleSet.clear();
        tupleSet.addAll(rstSet);
    }


    public static void main(String[] args) {
        double rst = 7;
        for(int i=0;i<7;i++){
            rst = rst + ((double)i%7/1000000);

            System.out.println("rst="+rst);
        }

    }
}
