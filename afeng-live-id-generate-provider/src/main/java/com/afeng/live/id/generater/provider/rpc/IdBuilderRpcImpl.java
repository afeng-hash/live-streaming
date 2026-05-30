package com.afeng.live.id.generater.provider.rpc;

import com.afeng.live.id.generater.interfaces.IdBuilderRpc;
import com.afeng.live.id.generater.provider.service.IdGenerateService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 自增id服务实现类
 */
@DubboService
public class IdBuilderRpcImpl implements IdBuilderRpc {

    @Resource
    private IdGenerateService idGenerateService;

    @Override
    public Long increaseSeqId(Long code) {
        try {
            return idGenerateService.getSeqId(code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Long increaseUnSeqId(Long code) {
        try {
            return idGenerateService.getUnSeqId(code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String increaseSeqStrId(int code) {
        return "";
    }
}
