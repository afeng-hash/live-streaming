package com.afeng.live.api.service.impl;

import com.afeng.live.api.service.IBankService;
import com.afeng.live.api.vo.req.PayProductReqVO;
import com.afeng.live.api.vo.resp.PayProductItemVO;
import com.afeng.live.api.vo.resp.PayProductRespVO;
import com.afeng.live.api.vo.resp.PayProductVO;
import com.afeng.live.bank.constants.OrderStatusEnum;
import com.afeng.live.bank.constants.PaySourceEnum;
import com.afeng.live.bank.dto.PayOrderDTO;
import com.afeng.live.bank.dto.PayProductDTO;
import com.afeng.live.bank.interfaces.IPayOrderRpc;
import com.afeng.live.bank.interfaces.IPayProductRpc;
import com.afeng.live.bank.interfaces.IAfengCurrencyAccountRpc;
import com.afeng.live.web.starter.error.BizBaseErrorEnum;
import com.afeng.live.web.starter.error.ErrorAssert;
import com.afeng.live.web.starter.thread.AfengRequestContext;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class BankServiceImpl implements IBankService {

    @DubboReference
    private IPayProductRpc payProductRpc;
    @DubboReference
    private IAfengCurrencyAccountRpc afengCurrencyAccountRpc;
    @DubboReference
    private IPayOrderRpc payOrderRpc;
    @Resource
    private RestTemplate restTemplate;

    /**
     * 获取商品列表
     *
     * @param type
     * @return
     */
    @Override
    public PayProductVO products(Integer type) {
        List<PayProductDTO> payProductDTOS = payProductRpc.products(type);
        PayProductVO payProductVO = new PayProductVO();
        List<PayProductItemVO> itemList = new ArrayList<>();
        for (PayProductDTO payProductDTO : payProductDTOS) {
            PayProductItemVO itemVO = new PayProductItemVO();
            itemVO.setName(payProductDTO.getName());
            itemVO.setId(payProductDTO.getId());
            itemVO.setCoinNum(JSON.parseObject(payProductDTO.getExtra()).getInteger("coin"));
            itemList.add(itemVO);
        }
        payProductVO.setPayProductItemVOList(itemList);
        payProductVO.setCurrentBalance(Optional.ofNullable(afengCurrencyAccountRpc.getBalance(AfengRequestContext.getUserId())).orElse(0));
        return payProductVO;
    }


    /**
     * 购买商品
     *
     * @param payProductReqVO
     * @return
     */
    @Override
    public PayProductRespVO payProduct(PayProductReqVO payProductReqVO) {
        //参数校验
        ErrorAssert.isTure(payProductReqVO != null && payProductReqVO.getProductId() != null && payProductReqVO.getPaySource() != null, BizBaseErrorEnum.PARAM_ERROR);
        ErrorAssert.isNotNull(PaySourceEnum.find(payProductReqVO.getPaySource()), BizBaseErrorEnum.PARAM_ERROR);
        PayProductDTO payProductDTO = payProductRpc.getByProductId(payProductReqVO.getProductId());
        ErrorAssert.isNotNull(payProductDTO, BizBaseErrorEnum.PARAM_ERROR);

        //插入一条订单，待支付状态
        PayOrderDTO payOrderDTO = new PayOrderDTO();
        payOrderDTO.setProductId(payProductReqVO.getProductId());
        payOrderDTO.setUserId(AfengRequestContext.getUserId());
        payOrderDTO.setSource(payProductReqVO.getPaySource());
        payOrderDTO.setPayChannel(payProductReqVO.getPayChannel());
        String orderId = payOrderRpc.insertOne(payOrderDTO);

        //更新订单为支付中状态
        payOrderRpc.updateOrderStatus(orderId, OrderStatusEnum.PAYING.getCode());
        PayProductRespVO payProductRespVO = new PayProductRespVO();
        payProductRespVO.setOrderId(orderId);

        //todo 远程http请求 resttemplate-》支付回调接口
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("orderId", orderId);
        jsonObject.put("userId", AfengRequestContext.getUserId());
        jsonObject.put("bizCode", 10001);
        HashMap<String,String> paramMap = new HashMap<>();
        paramMap.put("param",jsonObject.toJSONString());
        ResponseEntity<String> resultEntity = restTemplate.postForEntity("http://localhost:8095/live/bank/payNotify/wxNotify?param={param}", null, String.class,paramMap);
        System.out.println(resultEntity.getBody());
        return payProductRespVO;
    }
}
