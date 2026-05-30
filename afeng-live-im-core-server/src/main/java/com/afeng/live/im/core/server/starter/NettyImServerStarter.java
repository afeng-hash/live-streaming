package com.afeng.live.im.core.server.starter;

import com.afeng.live.im.core.server.ImCoreServerApplication;
import com.afeng.live.im.core.server.common.ChannelHandlerContextCache;
import com.afeng.live.im.core.server.common.ImMsgDecoder;
import com.afeng.live.im.core.server.common.ImMsgEncoder;
import com.afeng.live.im.core.server.handler.ImServerCoreHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 启动netty服务
 */
@Component
public class NettyImServerStarter implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyImServerStarter.class);

    @Resource
    private Environment environment;

    @Resource
    private ImServerCoreHandler imServerCoreHandler;

    @Value("${afeng.im.tcp.port}")
    private int port;

    //基于netty去启动一个java进程，绑定监听的端口
    public void startApplication() throws InterruptedException {
        //处理accept事件
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        //处理read/write事件
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        LOGGER.info("初始化连接通道");
                        //增加编解码器
                        channel.pipeline().addLast(new ImMsgDecoder());
                        channel.pipeline().addLast(new ImMsgEncoder());
                        channel.pipeline().addLast(imServerCoreHandler);
                    }
                });

        //基于jvm的钩子函数去实现优雅的关闭
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("关闭服务");
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }));

        //获取im的服务注册ip和暴露端口
        String registryIp = environment.getProperty("DUBBO_IP_TO_REGISTRY");
        String registryPort = environment.getProperty("DUBBO_PORT_TO_REGISTRY");
        if (StringUtils.isEmpty(registryPort) || StringUtils.isEmpty(registryIp)) {
            throw new IllegalArgumentException("启动参数中的注册端口和注册ip不能为空");
        }

        ChannelHandlerContextCache.setServerIpAddress(registryIp + ":" + registryPort);

        ChannelFuture channelFuture = bootstrap.bind(port).sync();
        LOGGER.info("服务启动成功，端口：{}",port);
        //这里会同步阻塞主线程，实现服务长期开启的效果
        channelFuture.channel().closeFuture().sync();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Thread nettyServerThread = new Thread(() -> {
            try {
                startApplication();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        nettyServerThread.setName("afeng-live-im-server");
        nettyServerThread.start();
    }
}
