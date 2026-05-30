package com.afeng.live.user.provider.utis;

public class TagInfoUtils {

    /**
     * 是否包含某个标签
     * @param tagInfo 用户标签
     * @param matchTag 需要匹配的标签
     * @return
     */
    public static boolean isContain(Long tagInfo,Long matchTag){
        return tagInfo!=null && matchTag != null && matchTag > 0 && (tagInfo & matchTag) == matchTag;
    }
}
