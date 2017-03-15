package mr.x.commons.utils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.util.Collection;
import java.util.List;

/**
 * Created by zhangwei on 14-4-7.
 *
 * @author zhangwei
 */
public class Arrays2 {


    public static String[] toStringArray(long[] objArrays, Function<Long, String> function) {
        if (ArrayUtils.isEmpty(objArrays)) {
            return new String[0];
        }
        return toStringArray(ArrayUtils.toObject(objArrays), function);
    }

    public static String[] toStringArray(int[] objArrays, Function<Integer, String> function) {
        if (ArrayUtils.isEmpty(objArrays)) {
            return new String[0];
        }
        return toStringArray(ArrayUtils.toObject(objArrays), function);
    }

    public static String[] toStringArray(byte[] objArrays, Function<Byte, String> function) {
        if (ArrayUtils.isEmpty(objArrays)) {
            return new String[0];
        }
        return toStringArray(ArrayUtils.toObject(objArrays), function);
    }

    public static String[] toStringArray(short[] objArrays, Function<Short, String> function) {
        if (ArrayUtils.isEmpty(objArrays)) {
            return new String[0];
        }
        return toStringArray(ArrayUtils.toObject(objArrays), function);
    }

    public static String[] toStringArray(char[] objArrays, Function<Character, String> function) {
        if (ArrayUtils.isEmpty(objArrays)) {
            return new String[0];
        }
        return toStringArray(ArrayUtils.toObject(objArrays), function);
    }

    public static String[] toStringArray(boolean[] objArrays, Function<Boolean, String> function) {
        if (ArrayUtils.isEmpty(objArrays)) {
            return new String[0];
        }
        return toStringArray(ArrayUtils.toObject(objArrays), function);
    }

    public static <F> String[] toStringArray(F[] objArrays, Function<F, String> function) {
        if (ArrayUtils.isEmpty(objArrays)) {
            return new String[0];
        }

        String[] rst = new String[objArrays.length];
        for (int i = 0; i < objArrays.length; i++) {
            if (function == null) {
                rst[i] = String.valueOf(objArrays[i]);
            } else {
                rst[i] = function.apply(objArrays[i]);
            }
        }
        return rst;
    }

    public static long[] toLongsArray(String[] strings) {
        return toLongsArray(strings, false);
    }

    public static long[] toLongsArray(String[] strings, boolean nullToZero) {
        if (ArrayUtils.isEmpty(strings)) {
            return new long[0];
        }
        long[] rst = new long[strings.length];
        for (int i = 0; i < strings.length; i++) {
            String s = strings[i];

            if (s == null) {
                if (nullToZero) {
                    rst[i] = 0L;
                }
            } else {
                s = s.trim();
                if (NumberUtils.isNumber(s)) {
                    rst[i] = Long.valueOf(s);
                }
            }

        }
        return rst;
    }

    public static long[] toPrimitiveLongArray(Collection<Long> numbers) {
        if (CollectionUtils.isEmpty(numbers)) {
            return new long[0];
        }

        int i = 0;
        long[] rst = new long[numbers.size()];
        for (Long n : numbers) {
            rst[i] = n.longValue();
            i++;
        }
        return rst;
    }

    public static int[] toPrimitiveIntArray(Collection<Integer> numbers) {
        if (CollectionUtils.isEmpty(numbers)) {
            return new int[0];
        }

        int i = 0;
        int[] rst = new int[numbers.size()];
        for (Integer n : numbers) {
            rst[i] = n.intValue();
            i++;
        }
        return rst;
    }

    public static short[] toPrimitiveShortArray(Collection<Short> numbers) {
        if (CollectionUtils.isEmpty(numbers)) {
            return new short[0];
        }

        int i = 0;
        short[] rst = new short[numbers.size()];
        for (Short n : numbers) {
            rst[i] = n.shortValue();
            i++;
        }
        return rst;
    }

    public static byte[] toPrimitiveByteArray(Collection<Byte> numbers) {
        if (CollectionUtils.isEmpty(numbers)) {
            return new byte[0];
        }

        int i = 0;
        byte[] rst = new byte[numbers.size()];
        for (Byte n : numbers) {
            rst[i] = n.byteValue();
            i++;
        }
        return rst;
    }

    public static float[] toPrimitiveFloatArray(Collection<Float> numbers) {
        if (CollectionUtils.isEmpty(numbers)) {
            return new float[0];
        }

        int i = 0;
        float[] rst = new float[numbers.size()];
        for (Float n : numbers) {
            rst[i] = n.floatValue();
            i++;
        }
        return rst;
    }

    public static double[] toPrimitiveDoubleArray(Collection<Double> numbers) {
        if (CollectionUtils.isEmpty(numbers)) {
            return new double[0];
        }

        int i = 0;
        double[] rst = new double[numbers.size()];
        for (Double n : numbers) {
            rst[i] = n.doubleValue();
            i++;
        }
        return rst;
    }

    public static char[] toPrimitiveCharArray(Collection<Character> elements) {
        if (CollectionUtils.isEmpty(elements)) {
            return new char[0];
        }

        int i = 0;
        char[] rst = new char[elements.size()];
        for (Character n : elements) {
            rst[i] = n.charValue();
            i++;
        }
        return rst;
    }

    public static long[] trimZero(long[] photoIds) {
        if (ArrayUtils.isEmpty(photoIds)) {
            return new long[0];
        }
        List<Long> list = Lists.newArrayList(ArrayUtils.toObject(photoIds));
        Predicate<Long> trim = new Predicate<Long>() {
            @Override
            public boolean apply(Long input) {
                if (input == null) {
                    return false;
                }
                if (Longs.compare(0, input.longValue()) == 0) {
                    return false;
                }
                return true;
            }
        };
        Collection c = Collections2.filter(list, trim);
        if (c.isEmpty()) {
            return new long[0];
        }
        return toPrimitiveLongArray(c);

    }

    public static String joinPrimitiveToString(long[] numbers, String on) {
        if (ArrayUtils.isEmpty(numbers)) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < numbers.length; i++) {
            sb.append(numbers[i]).append(on);
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
