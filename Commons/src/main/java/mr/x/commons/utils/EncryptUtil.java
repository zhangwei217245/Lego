package mr.x.commons.utils;


import org.apache.commons.codec.digest.DigestUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Reilost on 14-2-20.
 */
public class EncryptUtil {

    private static final String PASSWORD_SALT = "Jb!oFryRnqMVZm+thsDxeI:;|-+\"8zwPBKN7cpu,Gg*EfX4aUL";
    static SimpleDateFormat regDateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

//    public static String encryptPassword(String password, Date date) {
//        StringBuffer sb = new StringBuffer();
//        sb.append(regDateFormate.format(date));
//        sb.append(password);
//        sb.append(PASSWORD_SALT);
//        System.out.println(sb.toString());
//        return DigestUtils.md5Hex(sb.toString());
//
//    }

    public static String encryptPassword(String password, String ts_string) {

        String ts = ts_string;
        int mcIndex = ts.indexOf(".");
        if (mcIndex != -1) {
            int needZeroCount = 6 - (ts.length() - mcIndex - 1);
            if (needZeroCount > 0) {
                StringBuffer sb = new StringBuffer(ts);
                for (int i = 0; i < needZeroCount; i++) {
                    sb.append("0");
                }
                ts = sb.toString();
            }
        } else {
            ts = ts + ".000000";
        }
        StringBuffer sb = new StringBuffer();
        sb.append(ts);
        sb.append(password);
        sb.append(PASSWORD_SALT);
        return DigestUtils.md5Hex(sb.toString());
    }


    public static String getRegFormatTs(Date date) {
        return regDateFormate.format(date);
    }

    public static String getRegFormatTs(long datetime) {
        Date date = new Date(datetime);
        return regDateFormate.format(date);
    }

}
