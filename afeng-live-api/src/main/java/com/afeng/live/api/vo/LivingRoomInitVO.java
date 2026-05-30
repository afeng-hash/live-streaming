package com.afeng.live.api.vo;

import lombok.Data;

/**
 * @Author idea
 * @Date: Created in 14:33 2023/7/20
 * @Description
 */
@Data
public class LivingRoomInitVO {

    private Long anchorId;
    private Long userId;
    private String anchorImg;
    private String roomName;
    private boolean isAnchor;
    //红包配置码
    private String redPacketConfigCode;
    private String avatar;
    private Integer roomId;
    private String watcherNickName;
    private String anchorNickName;
    //观众头像
    private String watcherAvatar;
    //默认背景图，为了方便讲解使用
    private String defaultBgImg;
    private Long pkObjId;



    @Override
    public String toString() {
        return "LivingRoomInitVO{" +
                "anchorId=" + anchorId +
                ", userId=" + userId +
                ", anchorImg='" + anchorImg + '\'' +
                ", roomName='" + roomName + '\'' +
                ", isAnchor=" + isAnchor +
                ", avatar='" + avatar + '\'' +
                ", roomId=" + roomId +
                ", watcherNickName='" + watcherNickName + '\'' +
                ", anchorNickName='" + anchorNickName + '\'' +
                ", watcherAvatar='" + watcherAvatar + '\'' +
                ", defaultBgImg='" + defaultBgImg + '\'' +
                ", pkObjId=" + pkObjId +
                '}';
    }
}
