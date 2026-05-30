package com.afeng.live.bank.provider.rpc;

import com.afeng.live.bank.dto.PayOrderDTO;
import com.afeng.live.bank.interfaces.IPayOrderRpc;
import com.afeng.live.bank.provider.dao.po.PayOrderPO;
import com.afeng.live.bank.provider.service.IPayOrderService;
import com.afeng.live.common.interfaces.ConvertBeanUtils;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;


/**
 * 订单服务rpc实现
 * @Author idea
 * @Date: Created in 21:02 2023/8/19
 * @Description
 */
@DubboService
public class PayOrderRpcImpl implements IPayOrderRpc {

    @Resource
    private IPayOrderService iPayOrderService;

    /**
     * 插入一条订单
     *
     * @param payOrderDTO
     * @return
     */
    @Override
    public String insertOne(PayOrderDTO payOrderDTO) {
        return iPayOrderService.insertOne(ConvertBeanUtils.convert(payOrderDTO, PayOrderPO.class));
    }

    /**
     * 更新订单状态
     *
     * @param id
     * @param status
     * @return
     */
    @Override
    public boolean updateOrderStatus(Long id, Integer status) {
        return iPayOrderService.updateOrderStatus(id, status);
    }

    /**
     * 根据订单号更新订单状态
     * @param orderId
     * @param status
     * @return
     */
    @Override
    public boolean updateOrderStatus(String orderId, Integer status) {
        return iPayOrderService.updateOrderStatus(orderId, status);
    }

    /**
     * 支付回调接口
     * @param payOrderDTO
     * @return
     */
    @Override
    public boolean payNotify(PayOrderDTO payOrderDTO) {
        return iPayOrderService.payNotify(payOrderDTO);
    }
}
