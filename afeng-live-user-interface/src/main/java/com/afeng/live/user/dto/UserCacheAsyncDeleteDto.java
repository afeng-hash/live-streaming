package com.afeng.live.user.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserCacheAsyncDeleteDto implements Serializable {

    /**
     * 不同业务场景的code，区分不同的延迟消息
     */
    private int code;
    private String json;
}
