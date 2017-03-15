/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mr.x.commons.enums;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author zhangwei
 */
public enum LegoModules {
    
    BASE,
    COUNTER,
    TEST,
    LOGCENTER;

    static final Map<String, LegoModules> nameMapping = new HashMap<>();

    static {
        for (LegoModules module : LegoModules.values()) {
            nameMapping.put(module.name(), module);
        }
    }

    public static LegoModules forName(String name) {
        return nameMapping.get(name);
    }
}
