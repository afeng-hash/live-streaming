package com.afeng.live.im.core.server.common;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存im连接通道
 */
public class ChannelHandlerContextCache {

    private static Map<Long, ChannelHandlerContext> channelHandlerContextMap = new ConcurrentHashMap<>();

    private static String serverIpAddress = "";

    public static String getServerIpAddress() {
        return serverIpAddress;
    }

    public static void setServerIpAddress(String serverIpAddress) {
        ChannelHandlerContextCache.serverIpAddress = serverIpAddress;
    }

    public static ChannelHandlerContext get(Long userId){
        return channelHandlerContextMap.get(userId);
    }

    public static void put(Long userId,ChannelHandlerContext channelHandlerContext){
        channelHandlerContextMap.put(userId,channelHandlerContext);
    }

    public static void remove(Long userId){
        channelHandlerContextMap.remove(userId);
    }
}
