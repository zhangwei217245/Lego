package mr.x.meshwork.edge.utils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import mr.x.commons.utils.ApiLogger;
import mr.x.meshwork.edge.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.zip.CRC32;

/**
 * Created by zhangwei on 14-4-3.
 * @author zhangwei
 */
public class GraphUtils {

    private static ThreadLocal<CRC32> crc32Provider = new ThreadLocal<CRC32>(){
        @Override
        protected CRC32 initialValue() {
            return new CRC32();
        }
    };

    public static long getCrc32(byte[] b) {
        CRC32 crc = crc32Provider.get();
        crc.reset();
        crc.update(b);
        return crc.getValue();
    }

    public static long getCrc32(String str) {
        try {
            return getCrc32(str.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            ApiLogger.warn(e, new StringBuilder(64).append("Error: getCrc32, str=").append(str)
                    .toString());
            return -1;
        }
    }

    public static int getHash4split(Object id, int splitCount){
        try {
            long h = getCrc32(String.valueOf(id).getBytes("utf-8"));
            if(h < 0){
                h = -1 * h;
            }
            int hash = (int)(h / splitCount % splitCount);
            return hash;
        } catch (UnsupportedEncodingException e) {
            ApiLogger.warn(e, new StringBuilder(64).append("Error: when hash4split, id=").append(id)
                    .append(", splitCount=").append(splitCount).toString());
            return -1;
        }
    }

    public static int getHash4split(long id, int splitCount){
        try {
            long h = getCrc32(String.valueOf(id).getBytes("utf-8"));
            if(h < 0){
                h = -1 * h;
            }
            int hash = (int)(h / splitCount % splitCount);
            return hash;
        } catch (UnsupportedEncodingException e) {
            ApiLogger.warn(e, new StringBuilder(64).append("Error: when hash4split, id=").append(id)
                    .append(", splitCount=").append(splitCount).toString());
            return -1;
        }
    }

    public static Edge[] buildEdgesArray(long source_id,long[] destination_ids, Function<Long, Edge> function) {
        if (ArrayUtils.isEmpty(destination_ids)) {
            return new Edge[0];
        }
        Edge[] edges = new Edge[destination_ids.length];
        for (int i = 0; i < destination_ids.length; i++) {
            edges[i] = function.apply(destination_ids[i]);
        }
        return edges;
    }

    public static long[] getSourceIds(Collection<Edge> edges) {
        if (CollectionUtils.isEmpty(edges)) {
            return new long[0];
        }

        long[] source_ids = new long[edges.size()];
        int i = 0;
        for (Edge e : edges) {
            source_ids[i] = e.getSource_id();
            i++;
        }
        return source_ids;
    }

    public static long[] getDestinationIds(Collection<Edge> edges) {
        if (CollectionUtils.isEmpty(edges)) {
            return new long[0];
        }

        long[] destination_ids = new long[edges.size()];
        int i = 0;
        for (Edge e : edges) {
            destination_ids[i] = e.getDestination_id();
            i++;
        }
        return destination_ids;
    }
    
    public static void removeEdgesWithBizFilters(Collection<Edge> edges, final EdgeBizFilter bizFilter) {
        CollectionUtils.filter(edges, new org.apache.commons.collections.Predicate() {

            @Override
            public boolean evaluate(Object object) {
                if (object == null) {
                    return false;
                }
                Edge edge = (Edge)object;
                return !bizFilter.accept(edge);
            }
        });
    }
    
    public static void filterEdgesWithBizFilters(Collection<Edge> edges, final EdgeBizFilter bizFilter) {
        CollectionUtils.filter(edges, new org.apache.commons.collections.Predicate() {

            @Override
            public boolean evaluate(Object object) {
                if (object == null) {
                    return false;
                }
                Edge edge = (Edge)object;
                return bizFilter.accept(edge);
            }
        });
    }

    public static Collection<Edge> getBizFilteredEdges(Collection<Edge> edges, final EdgeBizFilter bizFilters) {
        return Collections2.filter(edges, new Predicate<Edge>() {
            @Override
            public boolean apply(Edge input) {
                return bizFilters.accept(input);
            }
        });
    }

    public static Map<Class, List<EdgeBizFilter>> classifyBizFilters(EdgeBizFilter[] bizFilters) {
        Map<Class, List<EdgeBizFilter>> filterMap = new HashMap<>();
        if (ArrayUtils.isNotEmpty(bizFilters)) {

            for (EdgeBizFilter bizFilter: bizFilters) {
                Class clazz = null;
                if (bizFilter instanceof State) {
                    clazz = State.class;
                } else if (bizFilter instanceof Category){
                    clazz = Category.class;
                } else if (bizFilter instanceof Criterion) {
                    clazz = Criterion.class;
                } else if (bizFilter instanceof AccessoryID) {
                    clazz = AccessoryID.class;
                }
                List<EdgeBizFilter> stateList = filterMap.get(clazz);
                if (stateList == null) {
                    stateList = new ArrayList<>();
                    filterMap.put(clazz, stateList);
                }
                stateList.add(bizFilter);
            }
        }
        return filterMap;
    }

    public static Integer[] getStateFilterValues(List<EdgeBizFilter> filter) {
        if (CollectionUtils.isEmpty(filter)) {
            return null;
        }
        Collection<Integer> valueCollection = Collections2.transform(filter, new Function<EdgeBizFilter, Integer>() {
            @Override
            public Integer apply(EdgeBizFilter input) {
                return ((State)input).value();
            }
        });

        return valueCollection.toArray(new Integer[valueCollection.size()]);
    }

    public static Integer[] getCategoryFilterValues(List<EdgeBizFilter> filter) {
        if (CollectionUtils.isEmpty(filter)) {
            return null;
        }
        Collection<Integer> valueCollection = Collections2.transform(filter, new Function<EdgeBizFilter, Integer>() {
            @Override
            public Integer apply(EdgeBizFilter input) {
                return ((Category)input).value();
            }
        });
        return valueCollection.toArray(new Integer[valueCollection.size()]);
    }

    public static String[] getCriterionFilterValues(List<EdgeBizFilter> filter) {
        if (CollectionUtils.isEmpty(filter)) {
            return null;
        }
        Collection<String> valueCollection = Collections2.transform(filter, new Function<EdgeBizFilter, String>() {
            @Override
            public String apply(EdgeBizFilter input) {
                return ((Criterion)input).value();
            }
        });
        return valueCollection.toArray(new String[valueCollection.size()]);
    }

    public static String[] getAccessoryIDFilterValues(List<EdgeBizFilter> filter) {
        if (CollectionUtils.isEmpty(filter)) {
            return null;
        }
        Collection<String> valueCollection = Collections2.transform(filter, new Function<EdgeBizFilter, String>() {
            @Override
            public String apply(EdgeBizFilter input) {
                return ((AccessoryID)input).value();
            }
        });
        return valueCollection.toArray(new String[valueCollection.size()]);
    }



}
