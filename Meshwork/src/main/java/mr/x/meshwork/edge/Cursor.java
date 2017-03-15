/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mr.x.meshwork.edge;


import mr.x.commons.utils.ApiLogger;

/**
 * 游标，
 * cursorName 代表游标名称，也就是某一列的列名
 * start代表起始值
 * end代表结束值
 * cursorDirection代表排序方向，升序或者倒序。默认为倒序
 *
 *
 * page_idx 中的 start 和 end 不会进入SQL，并且不会决定排序索引的选择。
 * 
 * @author zhangwei
 */
public class Cursor {
    
    private CursorName cursorName;
    private Long start;
    private Long end;
    private CursorDirection cursorDirection = CursorDirection.DESC;
    private boolean startInclusive = false;
    private boolean endInclusive = false;

    /**
     * 此构造器用于按起始位置和结束位置翻页
     * @param cursorName
     * @param start
     * @param end 
     */
    public Cursor(CursorName cursorName, Long start, Long end) {
        this(cursorName,start, end, CursorDirection.DESC, false, false);

        //默认只有position需要升序排序
        switch (this.cursorName) {
            case position:
                this.cursorDirection = CursorDirection.ASC;
                break;
            default:
                this.cursorDirection = CursorDirection.DESC;
        }

    }

    /**
     * 此构造器用于按起始地址和页长进行翻页
     * @param cursorName
     * @param start
     * @param count 
     */
    public Cursor(CursorName cursorName, int start, int count) {
        this(cursorName, (long)start, (long)(start+count));
    }


    public Cursor(CursorName cursorName, Long start, Long end, CursorDirection cursorDirection, boolean startInclusive, boolean endInclusive) {
        this.cursorName = cursorName;
        this.start = start;
        this.end = end;
        this.cursorDirection = cursorDirection;
        this.startInclusive = startInclusive;
        this.endInclusive = endInclusive;
    }

    public static Cursor getInstance(CursorName cursorName, Long start, Long end, CursorDirection cursorDirection, boolean startInclusive, boolean endInclusive) {
        return new Cursor(cursorName,start, end, cursorDirection, startInclusive, endInclusive);
    }

    public static Cursor getInstance(CursorName cursorName, int offset, int count, CursorDirection cursorDirection, boolean startInclusive, boolean endInclusive) {
        return new Cursor(cursorName, (long)offset, (long)(offset + count), cursorDirection, startInclusive, endInclusive);
    }

    public CursorName getCursorName() {
        return cursorName;
    }

    public void setCursorName(CursorName cursorName) {
        this.cursorName = cursorName;
    }
    

    /**
     * @return the start
     */
    public Long getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(Long start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public Long getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(Long end) {
        this.end = end;
    }

    public CursorDirection getCursorDirection() {
        return cursorDirection;
    }

    public Cursor setCursorDirection(CursorDirection cursorDirection) {
        this.cursorDirection = cursorDirection;
        return this;
    }

    public boolean isStartInclusive() {
        return startInclusive;
    }

    public Cursor setStartInclusive(boolean startInclusive) {
        this.startInclusive = startInclusive;
        return this;
    }

    public boolean isEndInclusive() {
        return endInclusive;
    }

    public Cursor setEndInclusive(boolean endInclusive) {
        this.endInclusive = endInclusive;
        return this;
    }

    public boolean matched(Edge edge) {
        if (cursorName == CursorName.page_idx) {
            return true;
        }

        boolean matchStart = true;
        boolean matchEnd = true;

        Long val = null;
        switch (cursorName) {
            case updated_at:
                val = edge.getUpdated_at();
                break;
            case destination_id:
                val = edge.getDestination_id();
                break;
            case position:
                val = Integer.valueOf(edge.getPosition()).longValue();
                break;
            default:
                ApiLogger.debug("Graph DB : Cursor " + this.cursorName.name() + " is not relevant to index");
        }

        matchStart = start != null ? (startInclusive? val >= start : val > start) : matchStart;
        matchEnd = end != null ? (endInclusive? val <= end : val < end) : matchEnd;
        return matchStart && matchEnd;
    }

    public static enum CursorName {
        updated_at(0),
        destination_id(1),
        position(2),
        page_idx(3);

        int idx;

        CursorName(int idx) {
            this.idx = idx;
        }

        public int value(){
            return idx;
        }
    }

    public static enum CursorDirection {
        ASC{
//            @Override
//            public String startInequality(boolean inclusive) {
//                return ">" + (inclusive?"=":"");
//            }
//
//            @Override
//            public String endInequality(boolean inclusive) {
//                return "<" + (inclusive?"=":"");
//            }
//
//            @Override
//            public boolean matchStart(long val, long start, boolean inclusive) {
//                return inclusive ? (val >= start) : (val > start);
//            }
//
//            @Override
//            public boolean matchEnd(long val, long end, boolean inclusive) {
//                return inclusive ? (val <= end) : (val < end);
//            }
        },
        DESC {
//            @Override
//            public String startInequality(boolean inclusive) {
//                return "<" + (inclusive?"=":"");
//            }
//
//            @Override
//            public String endInequality(boolean inclusive) {
//                return ">" + (inclusive?"=":"");
//            }
//
//            @Override
//            public boolean matchStart(long val, long start, boolean inclusive) {
//                return inclusive ? (val <= start) : (val < start);
//            }
//
//            @Override
//            public boolean matchEnd(long val, long end, boolean inclusive) {
//                return inclusive ? (val >= end) : (val > end);
//            }
        };
//        //twitter style pagination cursor definition.
//        public abstract String startInequality(boolean inclusive);
//        public abstract String endInequality(boolean inclusive);
//        public abstract boolean matchStart(long val, long start, boolean inclusive);
//        public abstract boolean matchEnd(long val, long end, boolean inclusive);
    }


}
