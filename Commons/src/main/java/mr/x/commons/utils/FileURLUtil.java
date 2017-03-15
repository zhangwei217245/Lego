package mr.x.commons.utils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import mr.x.commons.enums.ImageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * Created by Reilost on 14-3-6.
 */
@Component
public class FileURLUtil {

    static String imageDomain;
    static long defaultImageId = 12345l;


    @Value("${upyun_image_domain}")
    public void setImageDomain(String imageDomain) {
        FileURLUtil.imageDomain = imageDomain;
    }

    public static String getAvatarURL(String imageStr, boolean withSuffix) {
        return getImageRemoteURL(imageStr, ImageType.AVATAR_IMAGE, withSuffix);
    }

    public static String getImageRemoteURL(long imageId, ImageType imageType, boolean withSuffix) {
        if (imageType != ImageType.FEED_IMAGE) {
            if (imageId <= 1) {
                return "";
            }
        }

        if (imageId <= 0) {
            return "";
        }

        return getImageRemoteURL(imageId + "", imageType, withSuffix);
    }


    public static String getImageRemoteURL(String imageId, ImageType imageType, boolean withSuffix) {
        if (imageId == null || imageId.trim().length() == 0) {
            return "";
        }
        if (withSuffix) {
            return imageDomain + "/" + imageType.getPath() + "/" + imageId + "?.jpg";
        }
        return imageDomain + "/" + imageType.getPath() + "/" + imageId;
    }

    public static List<String> getImageRemoteURLs(Collection<String> imageIds, final ImageType imageType, final boolean withSuffix) {
        Function<String, String> coverFunction = new Function<String, String>() {
            @Override
            public String apply(String input) {
                return getImageRemoteURL(input, imageType, withSuffix);
            }
        };
        return Lists.newArrayList(Collections2.transform(imageIds, coverFunction));
    }
}
