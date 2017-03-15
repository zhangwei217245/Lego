package mr.x.commons.enums;

import com.google.common.base.Objects;

/**
 * Created with IntelliJ IDEA.
 * User: Reilost
 * Date: 10/17/13
 * Time: 12:23
 * To change this template use File | Settings | File Templates.
 */
public enum OpenPlatformType {


    RICEBOOK("ricebook", 1), SINA("sina", 2), QQ_CONNECT("qq_connect", 3);

    private String name;

    private int index;

    private OpenPlatformType(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public static String getName(int index) {
        for (OpenPlatformType c : OpenPlatformType.values()) {
            if (c.getIndex() == index) {
                return c.name;
            }
        }
        return null;
    }

    public static OpenPlatformType valueByIndex(int index) {
        if (index > OpenPlatformType.values().length) return null;
        return OpenPlatformType.values()[index - 1];
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }


    @Override
    public String toString() {
        return Objects.toStringHelper(this).
                add("name", name).
                add("index", index).
                toString();
    }
}
