package com.afeng.live.bank.provider.service.impl;

import com.afeng.live.bank.constants.OrderStatusEnum;
import com.afeng.live.bank.constants.PayProductTypeEnum;
import com.afeng.live.bank.dto.PayOrderDTO;
import com.afeng.live.bank.dto.PayProductDTO;
import com.afeng.live.bank.provider.dao.mapper.IPayOrderMapper;
import com.afeng.live.bank.provider.dao.po.PayOrderPO;
import com.afeng.live.bank.provider.dao.po.PayTopicPO;
import com.afeng.live.bank.provider.service.*;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Slf4j
@Service
public class PayOrderServiceImpl implements IPayOrderService {

    @Resource
    private IPayOrderMapper payOrderMapper;
    @Resource
    private IPayTopicService payTopicService;
    @Resource
    private MQProducer mqProducer;
    @Resource
    private IAfengCurrencyAccountService afengCurrencyAccountService;
    @Autowired
    private IPayProductService iPayProductService;

    /**
     * 根据订单号查询订单
     *
     * @param orderId
     * @return
     */
    @Override
    public PayOrderPO queryByOrderId(String orderId) {
        LambdaQueryWrapper<PayOrderPO> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(PayOrderPO::getOrderId, orderId);
        queryWrapper.last("limit 1");
        return payOrderMapper.selectOne(queryWrapper);
    }

    /**
     * 插入一条订单
     *
     * @param payOrderPO
     * @return
     */
    @Override
    public String insertOne(PayOrderPO payOrderPO) {
        String orderId = UUID.randomUUID().toString();
        payOrderPO.setOrderId(orderId);
        payOrderMapper.insert(payOrderPO);
        return orderId;
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
        PayOrderPO payOrderPO = new PayOrderPO();
        payOrderPO.setId(id);
        payOrderPO.setStatus(status);
        payOrderMapper.updateById(payOrderPO);
        return true;
    }

    /**
     * 根据订单号更新订单状态
     *
     * @param orderId
     * @param status
     * @return
     */
    @Override
    public boolean updateOrderStatus(String orderId, Integer status) {
        PayOrderPO payOrderPO = new PayOrderPO();
        payOrderPO.setStatus(status);
        LambdaUpdateWrapper<PayOrderPO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PayOrderPO::getOrderId, orderId);
        payOrderMapper.update(payOrderPO, updateWrapper);
        return true;
    }

    /**
     * 支付回调
     *
     * @param payOrderDTO
     * @return
     */
    @Override
    public boolean payNotify(PayOrderDTO payOrderDTO) {
        PayOrderPO payOrderPO = this.queryByOrderId(payOrderDTO.getOrderId());
        if (payOrderPO == null){
            log.error("订单不存在,error payOrderPO, payOrderDTO is {}",payOrderDTO);
            return false;
        }
        //根据业务码获取对应的topic
        PayTopicPO payTopicPO = payTopicService.getByCode(payOrderDTO.getBizCode());
        if (payTopicPO == null || StringUtils.isEmpty(payTopicPO.getTopic())) {
            log.error("error payTopicPO, payOrderDTO is {}", payOrderDTO);
            return false;
        }

        this.payNotifyHandler(payOrderPO);

        //假设 支付成功后，要发送消息通知 -》 msg-provider
        //假设 支付成功后，要修改用户的vip经验值
        //发mq
        //中台服务，支付的对接方 10几种服务，pay-notify-topic
        Message message = new Message();
        message.setTopic(payTopicPO.getTopic());
        message.setBody(JSON.toJSONBytes(payOrderPO));
        SendResult sendResult = null;
        try {
            sendResult = mqProducer.send(message);
            log.info("[payNotify] sendResult is {} ", sendResult);
        } catch (Exception e) {
            log.error("[payNotify] sendResult is {}, error is ", sendResult, e);
        }
        return true;
    }


    /**
     * 增加用户余额
     *
     * @param payOrderPO
     */
    private void payNotifyHandler(PayOrderPO payOrderPO) {
        this.updateOrderStatus(payOrderPO.getOrderId(), OrderStatusEnum.PAYED.getCode());
        Integer productId = payOrderPO.getProductId();
        PayProductDTO payProductDTO = iPayProductService.getByProductId(productId);
        if (payProductDTO != null && PayProductTypeEnum.AFENG_COIN.getCode().equals(payProductDTO.getType())) {
            Long userId = payOrderPO.getUserId();
            JSONObject jsonObject = JSON.parseObject(payProductDTO.getExtra());
            Integer num = jsonObject.getInteger("coin");
            afengCurrencyAccountService.incr(userId,num);
        }
    }
}
