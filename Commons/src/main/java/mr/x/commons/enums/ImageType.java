package mr.x.commons.enums;

import com.google.common.base.Objects;

/**
 * Created by Reilost on 14-3-6.
 */
public enum ImageType {


    FEED_IMAGE("feed", 1),
    AVATAR_IMAGE("avatar", 2),
    COVER_IMAGE("skin", 3),
    RANKING_LIST_COVER("rankinglist", 4),
    SYSTEM_IMAGE("ricebook", 5);

    private String path;

    private int index;

    private ImageType(String path, int index) {
        this.path = path;
        this.index = index;
    }

    public static String getPath(int index) {
        for (ImageType c : ImageType.values()) {
            if (c.getIndex() == index) {
                return c.path;
            }
        }
        return null;
    }

    public static ImageType valueByIndex(int index) {
        if (index > OpenPlatformType.values().length) return null;
        return ImageType.values()[index - 1];
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
                add("path", path).
                add("index", index).
                toString();
    }
}
