package com.afeng.live.im.core.server.starter;

import com.afeng.live.im.core.server.common.ChannelHandlerContextCache;
import com.afeng.live.im.core.server.common.WebsocketEncoder;
import com.afeng.live.im.core.server.handler.ws.WsImServerCoreHandler;
import com.afeng.live.im.core.server.handler.ws.WsSharkHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * @Author idea
 * @Date: Created in 20:35 2023/7/9
 * @Description
 */
@Configuration
public class WsNettyImServerStarter implements InitializingBean {

    private static Logger LOGGER = LoggerFactory.getLogger(WsNettyImServerStarter.class);

    //指定监听的端口
    @Value("${afeng.im.ws.port}")
    private int port;
    @Resource
    private WsSharkHandler wsSharkHandler;
    @Resource
    private WsImServerCoreHandler wsImServerCoreHandler;
    @Resource
    private Environment environment;

    //基于netty去启动一个java进程，绑定监听的端口
    public void startApplication() throws InterruptedException {
        //处理accept事件
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        //处理read&write事件
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        //netty初始化相关的handler
        bootstrap.childHandler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                //打印日志，方便观察
                LOGGER.info("初始化连接渠道");
                //因为基于http协议 使用http的编码和解码器
                // 1. HTTP 编解码器：将字节流解码为 HttpRequest，或将 HttpResponse 编码为字节流
                ch.pipeline().addLast(new HttpServerCodec());
                //是以块方式写 添加处理器
                // 2. 分块写处理器：支持发送大数据流（如文件），防止内存溢出
                ch.pipeline().addLast(new ChunkedWriteHandler());
                //http数据在传输过程中是分段 就是可以将多个段聚合 这就是为什么当浏览器发生大量数据时 就会发生多次http请求
                // 3. HTTP 聚合器：HTTP 请求可能是分段发送的，这个 Handler 将多个片段聚合成一个完整的 FullHttpRequest
                // 握手阶段必须拿到完整的请求对象才能处理
                ch.pipeline().addLast(new HttpObjectAggregator(8192));
                // 4. 自定义 WS 编码器：将业务对象 ImMsg 包装成 TextWebSocketFrame 发送给前端
                ch.pipeline().addLast(new WebsocketEncoder());
                // 5. 握手处理器 (SharkHandler)：负责校验 Token、处理 HTTP 升级为 WS 的逻辑
                ch.pipeline().addLast(wsSharkHandler);
                // 6. 业务核心处理器：握手成功后，后续所有的 WS 消息（TextWebSocketFrame）都由它处理
                ch.pipeline().addLast(wsImServerCoreHandler);
            }
        });

        //基于JVM的钩子函数去实现优雅关闭
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }));

        //获取im的服务注册ip和暴露端口
        String registryIp = environment.getProperty("DUBBO_IP_TO_REGISTRY");
        String registryPort = environment.getProperty("DUBBO_PORT_TO_REGISTRY");
        if (StringUtils.isEmpty(registryPort) || StringUtils.isEmpty(registryIp)) {
            throw new IllegalArgumentException("启动参数中的注册端口和注册ip不能为空");
        }
        ChannelHandlerContextCache.setServerIpAddress(registryIp + ":" + registryPort);
        ChannelFuture channelFuture = bootstrap.bind(port).sync();
        LOGGER.info("服务启动成功，监听端口为{}", port);
        //这里会阻塞掉主线程，实现服务长期开启的效果
        channelFuture.channel().closeFuture().sync();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Thread nettyServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startApplication();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        nettyServerThread.setName("afeng-live-im-server-ws");
        nettyServerThread.start();
    }
}
