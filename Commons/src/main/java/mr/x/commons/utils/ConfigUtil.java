package mr.x.commons.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by Reilost on 14-2-20.
 */
@Component
public class ConfigUtil {


    public static final String SYS_PROP_UNIT_TEST_KEY = "mr.x.commons.isunittest";

    public static boolean isUnitTesting() {
        return Boolean.valueOf(System.getProperty(SYS_PROP_UNIT_TEST_KEY));
    }




}
