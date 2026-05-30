package com.afeng.live.bank.provider.service.impl;

import com.afeng.live.bank.constants.TradeTypeEnum;
import com.afeng.live.bank.dto.AccountTradeReqDTO;
import com.afeng.live.bank.dto.AccountTradeRespDTO;
import com.afeng.live.bank.dto.AfengCurrencyAccountDTO;
import com.afeng.live.bank.provider.dao.mapper.IAfengCurrencyAccountMapper;
import com.afeng.live.bank.provider.dao.po.AfengCurrencyAccountPO;
import com.afeng.live.bank.provider.service.IAfengCurrencyAccountService;
import com.afeng.live.bank.provider.service.IAfengCurrencyTradeService;
import com.afeng.live.common.interfaces.ConvertBeanUtils;
import com.afeng.live.common.interfaces.enums.CommonStatusEum;
import com.afeng.live.framework.redis.starter.keys.BankProviderCacheKeyBuilder;
import jakarta.annotation.Resource;

import org.springframework.aop.framework.AopContext;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author idea
 * @Date: Created in 10:24 2023/8/6
 * @Description
 */
@Service
public class AfengCurrencyAccountServiceImpl implements IAfengCurrencyAccountService {

    @Resource
    private IAfengCurrencyAccountMapper afengCurrencyAccountMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private BankProviderCacheKeyBuilder cacheKeyBuilder;
    @Resource
    private IAfengCurrencyTradeService currencyTradeService;
//    @Resource
//    private AopContext aopContext;
    @Resource
    private ApplicationContext applicationContext;

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 4, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));

    /**
     * 插入一条用户账户信息
     *
     * @param userId
     * @return
     */
    @Override
    public boolean insertOne(long userId) {
        try {
            AfengCurrencyAccountPO accountPO = new AfengCurrencyAccountPO();
            accountPO.setUserId(userId);
            afengCurrencyAccountMapper.insert(accountPO);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 递增用户余额
     *
     * @param userId
     * @param num
     */
    @Override
    public void incr(long userId, int num) {
        afengCurrencyAccountMapper.incr(userId, num);
        String cacheKey = cacheKeyBuilder.buildUserBalance(userId);
        if (redisTemplate.hasKey(cacheKey)) {
            redisTemplate.opsForValue().increment(cacheKey, num);
            redisTemplate.expire(cacheKey, 5, TimeUnit.MINUTES);
        }
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                //分布式架构下，cap理论，可用性和性能，强一致性，柔弱的一致性处理
                //在异步线程池中完成数据库层的扣减和流水记录插入操作，带有事务
                IAfengCurrencyAccountService proxy = applicationContext.getBean(IAfengCurrencyAccountService.class);
                consumeIncrDBHandler(userId, num);
            }
        });

    }

    /**
     * 扣减用户余额
     * 先在redis中做扣减,再在数据库中做扣减
     * @param userId
     * @param num
     */
    @Override
    public void decr(long userId, int num) {
        //扣减余额
        String cacheKey = cacheKeyBuilder.buildUserBalance(userId);
        if (redisTemplate.hasKey(cacheKey)) {
            //基于redis的扣减操作
            redisTemplate.opsForValue().decrement(cacheKey, num);
            redisTemplate.expire(cacheKey, 5, TimeUnit.MINUTES);
        }
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                //分布式架构下，cap理论，可用性和性能，强一致性，柔弱的一致性处理
                //在异步线程池中完成数据库层的扣减和流水记录插入操作，带有事务
//                IAfengCurrencyAccountService proxy = (IAfengCurrencyAccountService) AopContext.currentProxy();
                IAfengCurrencyAccountService proxy = applicationContext.getBean(IAfengCurrencyAccountService.class);
                proxy.consumeDBHandler(userId, num);
            }
        });
    }

    /**
     * 根据用户id查询用户账户信息
     *
     * @param userId
     * @return
     */
    @Override
    public AfengCurrencyAccountDTO getByUserId(long userId) {
        return ConvertBeanUtils.convert(afengCurrencyAccountMapper.queryBalance(userId), AfengCurrencyAccountDTO.class);
    }

    /**
     * 底层判断用户余额是否充足，充足则扣减，不足则拦截
     * @param accountTradeReqDTO
     * @return
     */
    @Override
    public AccountTradeRespDTO consume(AccountTradeReqDTO accountTradeReqDTO) {
        long userId = accountTradeReqDTO.getUserId();
        int num = accountTradeReqDTO.getNum();
        //判断账号余额是否充足，考虑无记录情况
        AfengCurrencyAccountDTO accountDto = this.getByUserId(userId);
        if (accountDto == null){
            return AccountTradeRespDTO.buildFail(userId, "用户未初始化", 1);
        }
        if (accountDto.getStatus() == CommonStatusEum.INVALID_STATUS.getCode()){
            return AccountTradeRespDTO.buildFail(userId, "账号异常", 2);
        }
        if (accountDto.getCurrentBalance() - num < 0){
            return AccountTradeRespDTO.buildFail(userId, "余额不足", 3);
        }

        //大并发请求场景，1000个直播间，500人，50w人在线，20%的人送礼，10w在线触发送礼行为
        //db扛不住
        //mysql换成写入性能相对较高的数据库
        //我们能不能从业务上去进行优化，用户送礼都在直播间，大家都连上了im服务器，router层扩容（50台），im-core-provider层（100台），rocketmq削峰
        //消费端也做水平扩容
        //我们客户端发送送礼行为的时候，同步校验（校验账户余额是否足够，余额放入到redis中）
        //拦截下大部分的请求，如果余额不足，（接口还得做防止重复点击，客户端也要防重复）
        //同步送礼接口，只完成简单的余额校验，发送mq，在mq的异步操作里面，完成二次余额校验，余额扣减，礼物发送
        //如果余额不足，是不是可以利用im，反向通知发送方

        //扣减余额
        this.decr(userId, num);
        return AccountTradeRespDTO.buildSuccess(userId, "扣减成功");
    }

    /**
     * 送礼的时候，同步校验余额是否充足
     * @param accountTradeReqDTO
     * @return
     */
    @Override
    public AccountTradeRespDTO consumeForSendGift(AccountTradeReqDTO accountTradeReqDTO) {
        //余额判断
        long userId = accountTradeReqDTO.getUserId();
        int num = accountTradeReqDTO.getNum();
        Integer balance = this.getBalance(userId);
        if (balance == null || balance < num){
            return AccountTradeRespDTO.buildFail(userId, "余额不足", 1);
        }

        this.decr(userId, num);
        return AccountTradeRespDTO.buildSuccess(userId, "扣减成功");
    }


    /**
     * 扣减用户余额并记录流水
     * @param userId
     * @param num
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void consumeDBHandler(long userId,int num){
        //更新db,插入db
        afengCurrencyAccountMapper.decr(userId, num);
        //流水记录
        currencyTradeService.insertOne(userId, num*-1, TradeTypeEnum.SEND_GIFT_TRADE.getCode());
    }

    /**
     * 增加用户余额并记录流水
     * @param userId
     * @param num
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void consumeIncrDBHandler(long userId, int num) {
        //更新db，插入db
        afengCurrencyAccountMapper.incr(userId, num);
        //流水记录
        currencyTradeService.insertOne(userId, num, TradeTypeEnum.SEND_GIFT_TRADE.getCode());
    }


    /**
     * 获取用户余额
     * @param userId
     * @return
     */
    @Override
    public Integer getBalance(long userId) {
        String cacheKey = cacheKeyBuilder.buildUserBalance(userId);
        Object cacheBalance = redisTemplate.opsForValue().get(cacheKey);
        if (cacheBalance != null) {
            if ((Integer) cacheBalance == -1) {
                return null;
            }
            return (Integer) cacheBalance;
        }
        Integer currentBalance = afengCurrencyAccountMapper.queryBalance(userId);
        if (currentBalance == null) {
            redisTemplate.opsForValue().set(cacheKey, -1, 5, TimeUnit.MINUTES);
            return null;
        }
        redisTemplate.opsForValue().set(cacheKey, currentBalance, 30, TimeUnit.MINUTES);
        return currentBalance;
    }


//    @Override
//    public AccountTradeRespDTO consumeForSendGift(AccountTradeReqDTO accountTradeReqDTO) {
//        //余额判断
//        long userId = accountTradeReqDTO.getUserId();
//        int num = accountTradeReqDTO.getNum();
//        Integer balance = this.getBalance(userId);
//        if (balance == null || balance < num) {
//            return AccountTradeRespDTO.buildFail(userId, "账户余额不足", 1);
//        }
//        this.decr(userId, num);
//        return AccountTradeRespDTO.buildSuccess(userId, "消费成功");
//    }

//    @Transactional(rollbackFor = Exception.class)
//    public void consumeIncrDBHandler(long userId, int num) {
//        //更新db，插入db
//        afengCurrencyAccountMapper.incr(userId, num);
//        //流水记录
//        currencyTradeService.insertOne(userId, num, TradeTypeEnum.SEND_GIFT_TRADE.getCode());
//    }
//
//    @Transactional(rollbackFor = Exception.class)
//    public void consumeDecrDBHandler(long userId, int num) {
//        //更新db，插入db
//        afengCurrencyAccountMapper.decr(userId, num);
//        //流水记录
//        currencyTradeService.insertOne(userId, num * -1, TradeTypeEnum.SEND_GIFT_TRADE.getCode());
//    }

//
//    @Override
//    public AccountTradeRespDTO consume(AccountTradeReqDTO accountTradeReqDTO) {
////        long userId = accountTradeReqDTO.getUserId();
////        int num = accountTradeReqDTO.getNum();
////        //首先判断账户余额是否充足，考虑无记录的情况
////        AfengCurrencyAccountDTO accountDTO = this.getByUserId(userId);
////        if (accountDTO == null) {
////            return AccountTradeRespDTO.buildFail(userId, "账户未有初始化", 1);
////        }
////        if (!accountDTO.getStatus().equals(CommonStatusEum.VALID_STATUS.getCode())) {
////            return AccountTradeRespDTO.buildFail(userId, "账号异常", 2);
////        }
////        if (accountDTO.getCurrentBalance() - num < 0) {
////            return AccountTradeRespDTO.buildFail(userId, "余额不足", 3);
////        }
//        //todo 流水记录？
//        //大并发请求场景，1000个直播间，500人，50w人在线，20%的人送礼，10w人在线触发送礼行为，
//        //DB扛不住
//        //1.MySQL换成写入性能相对较高的数据库
//        //2.我们能不能从业务上去进行优化，用户送礼都在直播间，大家都连接上了im服务器，router层扩容（50台），im-core-server层（100台），RocketMQ削峰，
//        // 消费端也可以水平扩容
//        //3.我们客户端发起送礼行为的时候，同步校验（校验账户余额是否足够，余额放入到redis中），
//        //4.拦截下大部分的请求，如果余额不足，（接口还得做防止重复点击，客户端也要防重复）
//        //5.同步送礼接口，只完成简单的余额校验，发送mq，在mq的异步操作里面，完成二次余额校验，余额扣减，礼物发送
//        //6.如果余额不足，是不是可以利用im，反向通知发送方
//        // todo 性能问题
//        //扣减余额
////        this.decr(userId, num);
//        return AccountTradeRespDTO.buildSuccess(-1L, "扣费成功");
//    }
}
