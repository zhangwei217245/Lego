package mr.x.commons.redis;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;

/**
 * Created by Reilost on 14-3-4.
 */
public class BaseRedisService {

    protected static final int NP_BASE_ALL_CITY_TOKEN = 201;
    protected static final int NP_BASE_CITY_AREA_TOKEN = 202;
    protected static final int NP_BASE_FIX_GEO_TOKEN = 203;
    protected static final int NP_BASE_AREA_OBJECT = 204;
    protected static final int NP_BASE_CATEGORY_OBJECT = 205;
    protected static final int NP_USER_NICKNAME = 302;
    protected static final int NP_USER_OBJECT = 303;
    protected static final int NP_USER_ID = 304;


    protected static final int NP_OAUTH2_ACCESS_TOKEN = 404;
    protected static final int NP_OAUTH2_USER_PASSWORD_BY_EMAIL = 405;
    protected static final int NP_OAUTH2_USER_ID_BY_EMAIL = 406;
    protected static final int NP_OAUTH2_RICEBOOK_ACCOUNT_BY_USER_ID = 407;
    protected static final int NP_OAUTH2_OPENPLATFORM_ACCOUNT_BY_USER_ID = 408;
    protected static final int NP_OAUTH2_OPENPLATFORM_ACCOUNT_BY_OPENPLATFORM_ID = 409;
    protected static final int NP_OAUTH2_REST_PASSWORD = 410;
    protected static final int NP_OAUTH2_USER_OPENLATFORM_ACCOUNT = 411;

    protected static final int NP_BASE_RESTAURANT_OBJECT = 501;


    protected static final int NP_TAG_FANQIE_UNSPOORT_OBJECT = 601;
    protected static final int NP_TAG_FANQIE_OBJECT = 602;
    protected static final int NP_TAG_DP_UNSPOORT_OBJECT = 603;
    protected static final int NP_TAG_DP_OBJECT = 604;

    protected static final int NP_TAG_RB_OBJECT = 605;
    protected static final int NP_TAG_RB_USER_TAGS = 606;


    protected static final int NP_TAG_KAHUI_OBJECT = 607;

    protected static final int NP_TAG_KA_HUI_UNSPOORT_OBJECT = 608;



    protected String getKey(int nameSpace, Object key) {
        return nameSpace + "_" + key;
    }


    protected List<String> getKeys(final int nameSpace, Collection<? extends Object> keys) {
        Function<Object, String> getKeyFunction = new Function<Object, String>() {
            @Override
            public String apply(Object key) {
                return getKey(nameSpace, key);
            }
        };
        return ImmutableList.copyOf(Collections2.transform(keys, getKeyFunction));
    }
}
