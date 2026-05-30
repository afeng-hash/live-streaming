package com.afeng.live.im.router.provider.cluster;

import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.cluster.Directory;
import org.apache.dubbo.rpc.cluster.LoadBalance;
import org.apache.dubbo.rpc.cluster.support.AbstractClusterInvoker;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 继承了 Dubbo 的 AbstractClusterInvoker 抽象类
 * 负责在多个服务提供者实例之间进行 RPC 调用的路由和负载均衡
 * 实际的集群调用执行器
 */
public class ImRouterClusterInvoker<T> extends AbstractClusterInvoker<T> {

    public ImRouterClusterInvoker(Directory<T> directory) {
        super(directory);
    }

    /**
     * invocation：RPC 调用信息（包含方法名、参数等）
     * list：可用的服务提供者列表（Invoker 列表）
     * loadbalance：负载均衡策略
     * @param invocation
     * @param list
     * @param loadbalance
     * @return
     * @throws RpcException
     */
    @Override
    protected Result doInvoke(Invocation invocation, List list, LoadBalance loadbalance) throws RpcException {
        //父类提供的方法，防止在系统关闭过程中继续处理新的请求
        checkWhetherDestroyed();
        String ip = RpcContext.getContext().get("ip").toString();
        if (StringUtils.isEmpty(ip)){
            throw new RuntimeException("ip can not be null");
        }

        //获取到指定rpc服务提供者的所有地址信息
        List<Invoker<T>> invokers = list(invocation);
        Invoker<T> matchInvoker = invokers.stream().filter(invoker -> {
            //拿到我们服务者提供的暴露地址（ip：端口的格式）
            String serverIp = invoker.getUrl().getHost() + ":" + invoker.getUrl().getPort();
            return serverIp.equals(ip);
        }).findFirst().orElse(null);

        if (matchInvoker == null){
            throw new RuntimeException("no match invoker");
        }

        return matchInvoker.invoke(invocation);
    }
}
