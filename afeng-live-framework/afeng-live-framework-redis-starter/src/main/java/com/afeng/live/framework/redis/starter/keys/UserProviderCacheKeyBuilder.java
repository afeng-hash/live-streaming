package com.afeng.live.framework.redis.starter.keys;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Conditional;

/**
 * 用户中台专属的 keyBuiler
 *
 * @author afeng
 * @date 2023/5/14 20:05
 **/
@Configurable
@Conditional(RedisKeyLoadMatch.class)
public class UserProviderCacheKeyBuilder extends RedisKeyBuilder {
    private static String USER_INFO_KEY = "userInfo";
    private static String USER_TAG_KEY = "userTag";
    private static String USER_PHONE_KEY = "userPhone";
    private static String USER_TOKEN_KEY = "userLoginToken";
    private static String USER_PHONE_OBJ_KEY = "userPhoneObj";
    private static String USER_PHONE_LIST_KEY = "userPhoneList";
    public String buildUserInfoKey(Long userId) {
        return super.getPrefix() +USER_INFO_KEY + super.getSplitItem() +userId;
    }

    public String buildUserTagKey(Long userId) {
        return super.getPrefix() +USER_TAG_KEY + super.getSplitItem() +userId;
    }

    public String buildUserPhoneKey(Long userId) {
        return super.getPrefix() +USER_PHONE_KEY + super.getSplitItem() +userId;
    }

    public String buildUserLoginTokenKey(String tokenKey){
        return super.getPrefix() +USER_TOKEN_KEY + super.getSplitItem() +tokenKey;
    }

    public String buildUserPhoneObjKey(String phone){
        return super.getPrefix() +USER_PHONE_OBJ_KEY + super.getSplitItem() +phone;
    }

    public String buildUserPhoneListKey(Long userId) {
        return super.getPrefix() +USER_PHONE_LIST_KEY + super.getSplitItem() +userId;
    }
}
