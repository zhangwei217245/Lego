/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mr.x.commons.models;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

/**
 * 代表高级查询的结果。rstlist是分页后的结果，sumCount是分页前的总数
 *
 * @param <T>
 * @author zhangwei
 */
public class PageResult<T> extends BaseModel {

    private int curr_start;

    private int count;

    private List<T> current_page = Collections.emptyList();

    private int totalCount;

    private static final int defaultPageSize = 20;


    public PageResult() {
    }

    public PageResult(List<T> wholePage) {
        this.curr_start = 0;
        this.current_page = wholePage;
        this.count = this.current_page.size();
        this.totalCount = this.count;
    }

    public PageResult(int curr_start, int count, List<T> current_page, int totalCount) {
        this.curr_start = curr_start;
        this.count = count;
        this.current_page = current_page;
        this.totalCount = totalCount;
    }

    public <S> PageResult<S> transformTo(Function<T, S> function) {
        List<S> newrst = Lists.transform(this.getCurrent_page(), function);
        return new PageResult<S>(this.curr_start, this.count, newrst, this.totalCount);
    }

    /**
     * @param <S>
     * @param <T>
     * @param curr_start
     * @param count
     * @param totalCount
     * @param rst
     * @param function
     * @return
     */
    public static final <S, T> PageResult<T> transform(int curr_start, int count, int totalCount, List<S> rst, Function<S, T> function) {
        return new PageResult<>(curr_start, count, Lists.transform(rst, function), totalCount);
    }


    public static <F, T> PageResult<T> batchTransForm(PageResult<F> src, BatchFunction<F, T> batchFunction) {
        if (CollectionUtils.isEmpty(src.getCurrent_page())) {
            List<T> rstlst = Collections.emptyList();
            return new PageResult<>(src.getCurr_start(), src.getCount(), rstlst, src.getTotalCount());
        }
        return new PageResult<>(src.getCurr_start(), src.getCount(), batchFunction.apply(src.getCurrent_page()), src.getTotalCount());
    }

    public static <F, T> PageResult<T> transform(PageResult<F> src, Function<F, T> function) {
        return new PageResult<>(src.getCurr_start(), src.getCount(), Lists.transform(src.getCurrent_page(), function), src.getTotalCount());
    }


    public static interface BatchFunction<F, T> {

        public List<T> apply(List<F> src);
    }

    /**
     * get paginated result, from start (inclusive) to start + count(exclusive)
     * 如果start为负数，或者超过给定集合的长度，则start复位为0；
     * 如果count为负数，则修正为默认的defaultPageSize＝20；
     * 如果start+count为负数，或者超过指定集合的长度，则表示去到结合的最后一个元素
     *
     * @param <S>
     * @param <T>
     * @param start
     * @param count
     * @param totalList
     * @param function
     * @return
     */
    public static <S, T> PageResult<T> paginateWithTransform(int start, int count, Collection<S> totalList, Function<S, T> function) {
        int totalCount = totalList.size();
        if (start < 0 || start >= totalCount) start = 0;
        if (count <= 0) count = defaultPageSize;
        int end = start + count;
        if (end <= 0 || end >= totalCount) {
            end = totalCount;
        }
        List<T> list = new ArrayList<>();
        Iterator<S> it = totalList.iterator();

        int i = 0;

        while (it.hasNext()) {
            S source = it.next();
            if (i >= start && i < end) {
                list.add(function.apply(source));
            }
            i++;
        }
        return new PageResult<>(start, count, list, totalCount);
    }

    public static <S> PageResult<S> paginate(int start, int count, Collection<S> totalList) {
        int totalCount = totalList.size();


        if (start < 0) start = 0;

        if (start >= totalCount) start = totalCount;

        if (count <= 0) count = defaultPageSize;
        int end = start + count;
        if (end <= 0 || end >= totalCount) {
            end = totalCount;
        }
        List<S> list = new ArrayList<>();
        Iterator<S> it = totalList.iterator();

        int i = 0;

        while (it.hasNext()) {
            S source = it.next();
            if (i >= start && i < end) {
                list.add(source);
            }
            i++;
        }
        return new PageResult<>(start, count, list, totalCount);
    }

    public int getCurr_start() {
        return curr_start;
    }

    public void setCurr_start(int curr_start) {
        this.curr_start = curr_start;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<T> getCurrent_page() {
        return current_page;
    }

    public void setCurrent_page(List<T> current_page) {
        this.current_page = current_page;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(this.current_page);
    }

    public boolean isNotEmpty() {
        return !this.isEmpty();
    }

    public void merge(PageResult<T> edges) {
        this.getCurrent_page().addAll(edges.getCurrent_page());
        this.count+=edges.getCount();
        this.totalCount+=edges.getTotalCount();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PageResult that = (PageResult) o;

        if (count != that.count) return false;
        if (curr_start != that.curr_start) return false;
        if (totalCount != that.totalCount) return false;
        if (!current_page.equals(that.current_page)) return false;

        return true;
    }

    public static <T> PageResult<T> emptyPage(){
        List<T> emptylist = Collections.emptyList();
        return new PageResult<>(emptylist);
    }

    @Override
    public int hashCode() {
        int result = curr_start;
        result = 31 * result + count;
        result = 31 * result + current_page.hashCode();
        result = 31 * result + totalCount;
        return result;
    }

    @Override
    public String toString() {
        return "PageResult{" + "current_page=" + current_page + ",curr_start=" + curr_start + ", count=" + count + ", totalCount=" + totalCount + '}';
    }

}
