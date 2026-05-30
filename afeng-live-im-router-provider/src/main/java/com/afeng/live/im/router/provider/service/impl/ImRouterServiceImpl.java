package com.afeng.live.im.router.provider.service.impl;

import com.afeng.live.im.core.server.interfaces.constants.ImCoreServerConstants;
import com.afeng.live.im.core.server.interfaces.rpc.IRouterHandlerRpc;
import com.afeng.live.im.dto.ImMsgBody;
import com.afeng.live.im.router.provider.service.ImRouterService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ImRouterServiceImpl implements ImRouterService {

    @DubboReference
    private IRouterHandlerRpc routerHandlerRpc;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean sendMsg(ImMsgBody imMsgBody) {
        log.info("路由层进行路由：{}",imMsgBody);
        String bingAddress = redisTemplate.opsForValue().get(ImCoreServerConstants.IM_BIND_IP_KEY + imMsgBody.getAppId()+ ":" + imMsgBody.getUserId());
        if (bingAddress == null){
            return false;
        }
        //binAddress: ip:port%userId
        bingAddress = bingAddress.substring(bingAddress.lastIndexOf("%"));
        RpcContext.getContext().set("ip", bingAddress);
        log.info("路由层进行路由：ip:{}",bingAddress);
        routerHandlerRpc.sendMesg(imMsgBody);
        return true;
    }

    /**
     * 批量发送消息
     * @param imMsgBodyList
     */
    @Override
    public void batchSendMsg(List<ImMsgBody> imMsgBodyList) {
        log.info("批量发送消息：{}",imMsgBodyList);
        List<Long> userIdList = imMsgBodyList.stream().map(ImMsgBody::getUserId).collect(Collectors.toList());

        //根据userId 将不同的userId的immsgbody分类存入map
        Map<Long, ImMsgBody> userIdMsgMap = imMsgBodyList.stream().collect(Collectors.toMap(ImMsgBody::getUserId, x -> x));

        //保证整个list集合的appId得是同一个
        Integer appId = imMsgBodyList.get(0).getAppId();
        List<String> cacheKeyList = new ArrayList<>();
        userIdList.forEach(userId -> {
            String cacheKey = ImCoreServerConstants.IM_BIND_IP_KEY + appId + ":" + userId;
            cacheKeyList.add(cacheKey);
        });

        //批量取出每个用户绑定的ip地址
        List<String> ipList = redisTemplate.opsForValue().multiGet(cacheKeyList).stream().filter(x -> x != null).collect(Collectors.toList());

        Map<String, List<ImMsgBody>> imMsgBodyMap = new HashMap<>();
        ipList.forEach(ip -> {
            String currentIp = ip.substring(0, ip.indexOf("%"));
            Long userId = Long.valueOf(ip.substring(ip.indexOf("%") + 1));
            List<ImMsgBody> currentUserIdList = imMsgBodyMap.get(currentIp);
            if (currentUserIdList == null) {
                currentUserIdList = new ArrayList<>();
            }
            currentUserIdList.add(userIdMsgMap.get(userId));
            imMsgBodyMap.put(currentIp, currentUserIdList);
        });

        //将连接同一台ip地址的imMsgBody组装到同一个list集合中，然后进行统一的发送
        for (String currentIp : imMsgBodyMap.keySet()) {
            RpcContext.getContext().set("ip", currentIp);
            List<ImMsgBody> batchSendMsgGroupByIpList = imMsgBodyMap.get(currentIp);
            routerHandlerRpc.batchSendMsg(batchSendMsgGroupByIpList);
        }
    }
}
