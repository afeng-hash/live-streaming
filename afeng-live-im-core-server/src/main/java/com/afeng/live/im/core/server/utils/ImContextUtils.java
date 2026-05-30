package com.afeng.live.im.core.server.utils;

import com.afeng.live.im.core.server.common.ImContextAttr;
import io.netty.channel.ChannelHandlerContext;


/**
 * 利用ChannelHandlerContext的attr方法去绑定/获取一些业务属性
 */
public class ImContextUtils {

    public static Integer getRoomId(ChannelHandlerContext ctx) {
        return ctx.attr(ImContextAttr.ROOM_ID).get();
    }

    public static void setRoomId(ChannelHandlerContext ctx, int roomId) {
        ctx.attr(ImContextAttr.ROOM_ID).set(roomId);
    }
    public static void setAppId(ChannelHandlerContext ctx,Integer appId){
        ctx.attr(ImContextAttr.APP_ID).set(appId);
    }

    public static Integer getAppId(ChannelHandlerContext ctx){
        return ctx.attr(ImContextAttr.APP_ID).get();
    }

    public static void setUserId(ChannelHandlerContext ctx,Long userId){
        ctx.attr(ImContextAttr.USER_ID).set(userId);
    }

    public static Long getUserId(ChannelHandlerContext ctx){
        return ctx.attr(ImContextAttr.USER_ID).get();
    }

    public static void romeveUserId(ChannelHandlerContext ctx) {
        ctx.attr(ImContextAttr.USER_ID).remove();
    }

    public static void romeveAppId(ChannelHandlerContext ctx) {
        ctx.attr(ImContextAttr.APP_ID).remove();
    }

    public static void romeveRoomId(ChannelHandlerContext ctx) {
        ctx.attr(ImContextAttr.ROOM_ID).remove();
    }
}
