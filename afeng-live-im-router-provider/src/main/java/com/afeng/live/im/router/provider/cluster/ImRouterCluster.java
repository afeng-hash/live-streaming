package com.afeng.live.im.router.provider.cluster;

import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Cluster;
import org.apache.dubbo.rpc.cluster.Directory;

/**
 * 集群策略工厂，负责创建集群调用器
 * 实现了 Dubbo 的 Cluster 接口，负责创建自定义的集群调用器实例。
 */
public class ImRouterCluster implements Cluster {

    /**
     * ① 服务启动阶段
     *    ┌─────────────────────────────────────┐
     *    │  Dubbo 框架初始化                    │
     *    │  1. 加载 Cluster 扩展                │
     *    │  2. 发现 ImRouterCluster             │
     *    │  3. 注册到集群策略管理器              │
     *    └─────────────────────────────────────┘
     *                     ↓
     * ② 服务引用阶段（Consumer 端）
     *    ┌─────────────────────────────────────┐
     *    │  @DubboReference                     │
     *    │  private ImRouterRpc imRouterRpc;    │
     *    │                                     │
     *    │  Dubbo 框架：                        │
     *    │  1. 创建 Directory 对象              │
     *    │  2. 从注册中心拉取服务列表            │
     *    │  3. 调用 ImRouterCluster.join()     │
     *    │  4. 获取 ImRouterClusterInvoker     │
     *    │  5. 包装成代理对象返回给用户          │
     *    └─────────────────────────────────────┘
     *                     ↓
     * ③ RPC 调用阶段
     *    ┌─────────────────────────────────────┐
     *    │  imRouterRpc.sendMsg(userId, msg)   │
     *    │           ↓                         │
     *    │  代理对象 → ImRouterClusterInvoker  │
     *    │           ↓                         │
     *    │  doInvoke() 执行精准路由             │
     *    │           ↓                         │
     *    │  找到目标 IP 对应的 Invoker          │
     *    │           ↓                         │
     *    │  发起网络调用                        │
     *    └─────────────────────────────────────┘
     */


    /**
     * 创建集群调用器
     * Directory
     *     ├── 监听注册中心（Nacos/Zookeeper）
     *     ├── 当有新服务上线时，自动添加到列表
     *     ├── 当有服务下线时，自动从列表移除
     *     └── 提供 list() 方法获取当前的 Invoker 列表
     *
     * @param directory 服务目录
     * @param buildFilterChain 是否构建过滤链
     * @return 集群调用器实例
     * @throws RpcException 创建集群调用器时发生的异常
     */
    @Override
    public <T> Invoker<T> join(Directory<T> directory, boolean buildFilterChain) throws RpcException {
        return new ImRouterClusterInvoker<>(directory);
    }
}
