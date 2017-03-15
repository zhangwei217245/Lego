package mr.x.commons.httpclient;

import mr.x.commons.utils.ApiLogger;
import org.apache.commons.lang.StringUtils;

/**
 * Created by zhangwei on 14-6-16.
 *
 * @author zhangwei
 */
public class DefaultHttpClientLog implements LegoHttpClient.AccessLog{


    public void accessLog(long time, String method, int status, int len,
                          String url, String post, String ret) {
        if (post != null && post.length() > 200) {
            post = post.substring(0, 200);
            post.replaceAll("\n", "");
            post = post + "...";
        }
        if (ret != null) {
            ret = ret.trim();
            ret = ret.replaceAll("\n", "");
        }
        if(!StringUtils.isBlank(post)&&url.startsWith("http://ilogin.sina.com.cn")){
            post = replacePwd(post);
        }
        ApiLogger.httpInfo("[HTTPCLI] %s %s %s %s %s %s %s", time, method, status, len, url,
                StringUtils.isEmpty(post) ? "-" : post, StringUtils.isEmpty(ret) ? "-" : ret);
    }

    static String replacePwd(String text){
        text = text.replaceFirst("pw=[^&]*", "pw=***");
        return text;
    }
}
