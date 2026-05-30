package com.afeng.live.consumer.common;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelHandlerContextCache {

    private static Map<Long, ChannelHandlerContext> channelHandlerContextMap = new ConcurrentHashMap<>();

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
