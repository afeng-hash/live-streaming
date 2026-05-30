package com.afeng.live.consumer.handler;

import com.afeng.live.consumer.ClientHandler;
import com.afeng.live.consumer.common.ImMsg;
import com.afeng.live.consumer.common.ImMsgDecoder;
import com.afeng.live.consumer.common.ImMsgEncoder;
import com.afeng.live.im.constants.AppIdEnum;

import com.afeng.live.im.dto.ImMsgBody;
import com.afeng.live.im.enums.ImMsgCodeEnum;
import com.afeng.live.im.interfaces.ImTokenRpc;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class ImClientHandler implements InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(ImClientHandler.class);

    @DubboReference
    private ImTokenRpc imTokenRpc;

    @Override
    public void afterPropertiesSet() throws Exception {
        Thread chientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                EventLoopGroup clientGroup = new NioEventLoopGroup();
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(clientGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<>() {
                            @Override
                            protected void initChannel(Channel channel) throws Exception {
                                System.out.println("建立连接");
                                channel.pipeline().addLast(new ImMsgEncoder());
                                channel.pipeline().addLast(new ImMsgDecoder());
                                channel.pipeline().addLast(new ClientHandler());
                            }
                        });

                ChannelFuture channelFuture = bootstrap.connect("localhost", Integer.parseInt("8087"));
                Channel channel = channelFuture.channel();

                Scanner sc = new Scanner(System.in);
                System.out.println("请输入userId");
                long userId = sc.nextLong();
                System.out.println("请输入objectId:");
                long objectId = sc.nextLong();
                String token = imTokenRpc.createImLoginToken(userId, AppIdEnum.AFENG_LIVE_BIZ.getCode());
                ImMsgBody imMsgBody = new ImMsgBody();
                imMsgBody.setData("login");
                imMsgBody.setAppId(AppIdEnum.AFENG_LIVE_BIZ.getCode());
                imMsgBody.setToken(token);
                imMsgBody.setUserId(userId);
                channel.writeAndFlush(ImMsg.build(ImMsgCodeEnum.IM_LOGIN_MSG.getCode(), JSON.toJSONString(imMsgBody)));
                logger.info("发送消息成功,Token:{}", token);
                sendHeartBeat(userId, channel);
                logger.info("发送心跳消息成功");
//
                while (true) {
                    System.out.println("请输入聊天内容：");
                    String content = sc.next();
                    ImMsgBody bizBody = new ImMsgBody();
                    bizBody.setBizCode(5555);
                    bizBody.setAppId(AppIdEnum.AFENG_LIVE_BIZ.getCode());
                    bizBody.setUserId(userId);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("userId", userId);
                    jsonObject.put("objectId", objectId);
                    jsonObject.put("content", content);
                    bizBody.setData(jsonObject.toJSONString());
                    ImMsg msg = ImMsg.build(ImMsgCodeEnum.IM_BIZ_MSG.getCode(), JSON.toJSONString(bizBody));
                    channel.writeAndFlush(msg);
                }


            }
        });
        chientThread.start();
    }


    private void sendHeartBeat(Long userId, Channel channel) {
        new Thread(() -> {
            while (true) {
                logger.info("开始发送心跳消息");
                ImMsgBody imMsgBody = new ImMsgBody();
                imMsgBody.setAppId(AppIdEnum.AFENG_LIVE_BIZ.getCode());
                imMsgBody.setUserId(userId);
                ImMsg msg = ImMsg.build(ImMsgCodeEnum.IM_HEARTBEAT_MSG.getCode(), JSON.toJSONString(imMsgBody));
                channel.writeAndFlush(msg);

                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
