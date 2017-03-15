package mr.x.commons.httpclient;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhangwei on 14-6-16.
 *
 * @author zhangwei
 */
public class HttpManager {
    private static Set<String> blockResources = new HashSet<String>();


    public static void addBlockResource(String r){
        if(r == null || r.length()<6){//http://
            return;
        }
        blockResources.add(r);
    }
    public static void removeBlockResource(String r){
        blockResources.remove(r);
    }
    public static boolean isBlockResource(String url){
        if(url == null){
            return true;
        }
        for(String br : blockResources){
            if(url.startsWith(br)){
                return true;
            }
        }
        return false;
    }
    public static Set<String> getBlockResources(){
        return blockResources;
    }


}
