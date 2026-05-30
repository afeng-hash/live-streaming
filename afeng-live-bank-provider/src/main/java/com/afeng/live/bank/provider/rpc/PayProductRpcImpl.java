package com.afeng.live.bank.provider.rpc;

import com.afeng.live.bank.dto.PayProductDTO;
import com.afeng.live.bank.interfaces.IPayProductRpc;
import com.afeng.live.bank.provider.service.IPayProductService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;


import java.util.List;

/**
 * @Author idea
 * @Date: Created in 08:07 2023/8/17
 * @Description
 */
@DubboService
public class PayProductRpcImpl implements IPayProductRpc {

    @Resource
    private IPayProductService payProductService;

    /**
     * 返回批量的商品信息
     *
     * @param type 不同的业务场景所使用的产品
     */
    @Override
    public List<PayProductDTO> products(Integer type) {
        return payProductService.products(type);
    }

    @Override
    public PayProductDTO getByProductId(Integer productId) {
        return payProductService.getByProductId(productId);
    }
}
