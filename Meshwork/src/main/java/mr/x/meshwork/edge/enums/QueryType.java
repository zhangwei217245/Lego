package mr.x.meshwork.edge.enums;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

/**
 *
 * @author zhangwei
 */
public enum QueryType {
    Intersection{

        @Override
        public <T> List<T> computing(Set<T> setA, Set<T> setB) {
            return Lists.newArrayList(Sets.intersection(setA, setB));
        }

        
    }, Union{

        @Override
        public <T> List<T> computing(Set<T> setA, Set<T> setB) {
            return Lists.newArrayList(Sets.union(setA, setB));
        }

    }, Diff{

        @Override
        public <T> List<T> computing(Set<T> setA, Set<T> setB) {
            return Lists.newArrayList(Sets.difference(setA, setB));
        }

    }, SymmetricDiff{

        @Override
        public <T> List<T> computing(Set<T> setA, Set<T> setB) {
            return Lists.newArrayList(Sets.symmetricDifference(setA, setB));
        }
    };
    
    
    public abstract <T> List<T> computing(Set<T> setA, Set<T> setB);
}
