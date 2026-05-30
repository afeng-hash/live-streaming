package com.afeng.live.im.core.server.imclient.handler;

import com.afeng.live.im.constants.AppIdEnum;
import com.afeng.live.im.core.server.common.ImMsg;
import com.afeng.live.im.core.server.common.ImMsgDecoder;
import com.afeng.live.im.core.server.common.ImMsgEncoder;
import com.afeng.live.im.dto.ImMsgBody;
import com.afeng.live.im.enums.ImMsgCodeEnum;
import com.afeng.live.im.interfaces.ImTokenRpc;
import com.alibaba.fastjson.JSON;
import com.afeng.live.im.core.server.imclient.ClientHandler;
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

                Long userId = 100023L;
                String token = imTokenRpc.createImLoginToken(userId,AppIdEnum.AFENG_LIVE_BIZ.getCode());
                for (int i = 0; i < 1; i++){
                    ImMsgBody imMsgBody = new ImMsgBody();
                    imMsgBody.setData("login");
                    imMsgBody.setAppId(AppIdEnum.AFENG_LIVE_BIZ.getCode());
                    imMsgBody.setToken(token);
                    channel.writeAndFlush(ImMsg.build(ImMsgCodeEnum.IM_LOGIN_MSG.getCode(), JSON.toJSONString(imMsgBody) ));
                    logger.info("发送消息成功");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        chientThread.start();
    }
}
