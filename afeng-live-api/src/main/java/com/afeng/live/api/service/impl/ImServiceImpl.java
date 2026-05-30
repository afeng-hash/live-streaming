package com.afeng.live.api.service.impl;

import com.afeng.live.api.service.ImService;
import com.afeng.live.api.vo.resp.ImConfigVo;
import com.afeng.live.im.constants.AppIdEnum;
import com.afeng.live.im.interfaces.ImTokenRpc;
import com.afeng.live.web.starter.thread.AfengRequestContext;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ImServiceImpl implements ImService {

    @DubboReference
    private ImTokenRpc imTokenRpc;
    @Resource
    private DiscoveryClient discoveryClient;

    /**
     * 获取im服务配置
     *
     * @return
     */
    @Override
    public ImConfigVo getImConfig() {
        ImConfigVo imConfigVo = new ImConfigVo();
        imConfigVo.setToken(imTokenRpc.createImLoginToken(AfengRequestContext.getUserId(), AppIdEnum.AFENG_LIVE_BIZ.getCode()));
        buildImServerAddress(imConfigVo);
        return imConfigVo;
    }

    /**
     * 从nacos中拉取im服务，然后获取服务的ip和端口
     * 构建im服务地址
     *
     * @param imConfigVo
     */
    private void buildImServerAddress(ImConfigVo imConfigVo) {
        List<ServiceInstance> instances = discoveryClient.getInstances("afeng-live-im-core-server");
        Collections.shuffle(instances);
        ServiceInstance serviceInstance = instances.get(0);
        imConfigVo.setWsImServerAddress(serviceInstance.getHost() + ":" + "8092");
        imConfigVo.setTcpImServerAddress(serviceInstance.getHost() + ":" + "8087");
    }
}
