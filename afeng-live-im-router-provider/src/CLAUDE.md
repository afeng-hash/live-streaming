路由层

A用户发送消息给B用户

这个链路是 消息先到

afeng-live-core-server模块 -> mq -> afeng-live-msg-provider模块【进行业务处理】-> afeng-live-core-server模块 -> B用户

在业务处理完之后，这个要把消息重新发送给用户绑定通道的im服务机器上，有这台im服务机器去反馈给B用户



需要记录每个用户连接的im服务器地址，然后根据im服务器的连接地址去做具体的机器的调用

基于mq广播思路去做，可能会有消息风暴发生，100台im机器，99的mq消息都是无效的

router中转的设计，router就是一个dubbo的rpc层