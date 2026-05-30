package com.afeng.live.sms.provider.service.impl;

import com.afeng.live.framework.redis.starter.keys.MsgProviderCacheKeyBuilder;

import com.afeng.live.sms.interfaces.enums.MsgSendResultEnum;
import com.afeng.live.sms.interfaces.enums.SmsTemplateIdEnum;
import com.afeng.live.sms.interfaces.msg.MsgCheckDTO;
import com.afeng.live.sms.provider.config.ApplicationProperties;
import com.afeng.live.sms.provider.config.ThreadPoolManager;
import com.afeng.live.sms.provider.dao.mapper.SmsMapper;
import com.afeng.live.sms.provider.dao.po.SmsPO;
import com.afeng.live.sms.provider.service.ISmsService;
import com.cloopen.rest.sdk.BodyType;
import com.cloopen.rest.sdk.CCPRestSmsSDK;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SmsServiceImpl implements ISmsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private SmsMapper smsMapper;
    @Autowired
    private MsgProviderCacheKeyBuilder msgProviderCacheKeyBuilder;
    @Autowired
    private ApplicationProperties applicationProperties;


    /**
     * 发送短信验证码
     *
     * @param phone
     * @return
     */
    @Override
    public MsgSendResultEnum sendLoginCode(String phone) {
        if (StringUtils.isEmpty(phone)) {
            return MsgSendResultEnum.MSG_PARAM_ERROR;
        }

        //生成验证码，有效期30s，同一个手机号码不能重复发，redis存储
        String key = msgProviderCacheKeyBuilder.buildLoginCodeKey(phone);
        if (redisTemplate.hasKey( key)){
            LOGGER.warn("该手机号码发送过于频繁,{}",phone);
            return MsgSendResultEnum.SEND_FAIL;
        }

        int code = RandomUtils.nextInt(10000, 99999);
        redisTemplate.opsForValue().set(key,code,1, TimeUnit.MINUTES);

        //异步发送验证码
        ThreadPoolManager.commonAsyncPool.execute(() -> {
            boolean result = sendSms(phone,code);
            if (result){
                //插入数据库
                insertOne(phone,code);
            }
        });

        return MsgSendResultEnum.SEND_SUCCESS;
    }


    /**
     * 校验登录验证码
     *
     * @param phone
     * @param code
     * @return
     */
    @Override
    public MsgCheckDTO checkLoginCode(String phone, Integer code) {
        if (StringUtils.isEmpty(phone) || code == null) {
            return new MsgCheckDTO(false,"参数错误");
        }
        String key = msgProviderCacheKeyBuilder.buildLoginCodeKey(phone);
        Integer redisCode = (Integer) redisTemplate.opsForValue().get(key);
        if (redisCode == null){
            return new MsgCheckDTO(false,"验证码已过期");
        }
        if (redisCode.equals(code)){
            redisTemplate.delete(key);
            return new MsgCheckDTO(true,"验证码正确");
        }
        return new MsgCheckDTO(false,"验证码错误");
    }

    /**
     * 插入一条短信验证码记录
     *
     * @param phone
     * @param code
     */
    @Override
    public void insertOne(String phone, Integer code) {
        SmsPO smsPO = new SmsPO();
        smsPO.setPhone(phone);
        smsPO.setCode(code);
        smsPO.setSendTime(new Date());
        smsPO.setUpdateTime(new Date());
        smsMapper.insert(smsPO);
    }

    private boolean mockSendSms(String phone, int code)  {
        LOGGER.info("发送短信验证码，手机号码：{}，验证码：{}",phone,code);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("发送短信验证码成功，手机号码：{}，验证码：{}",phone,code);
        return true;
    }


    /**
     * 发送短信验证码
     *
     * @param phone
     * @param code
     * @return
     */
    private boolean sendSms(String phone, int code){
        LOGGER.info("发送短信验证码，手机号码：{}，验证码：{}",phone,code);
        return true;
//        try {
//            //生产环境请求地址：app.cloopen.com
//            String serverIp = applicationProperties.getSmsServerIp();
//            //请求端口
//            String serverPort = applicationProperties.getSmsServerPort();
//            //主账号,登陆云通讯网站后,可在控制台首页看到开发者主账号ACCOUNT SID和主账号令牌AUTH TOKEN
//            String accountSId = applicationProperties.getAccountSId();
//            String accountToken = applicationProperties.getAccountToken();
//            //请使用管理控制台中已创建应用的APPID
//            String appId = applicationProperties.getAppId();
//            CCPRestSmsSDK sdk = new CCPRestSmsSDK();
//            sdk.init(serverIp, serverPort);
//            sdk.setAccount(accountSId, accountToken);
//            sdk.setAppId(appId);
//            sdk.setBodyType(BodyType.Type_JSON);
//            String to = "17280269489";
//            String templateId= SmsTemplateIdEnum.SMS_LOGIN_TEMPLATE.getTemplateId();
//            String[] datas = {String.valueOf(code),"1"};
//            String subAppend="123";  //可选 扩展码，四位数字 0~9999
//            String reqId= UUID.randomUUID().toString();  //可选 第三方自定义消息id，最大支持32位英文数字，同账号下同一自然天内不允许重复
//            //HashMap<String, Object> result = sdk.sendTemplateSMS(to,templateId,datas);
//            HashMap<String, Object> result = sdk.sendTemplateSMS(to,templateId,datas,subAppend,reqId);
//            if("000000".equals(result.get("statusCode"))){
//                //正常返回输出data包体信息（map）
//                HashMap<String,Object> data = (HashMap<String, Object>) result.get("data");
//                Set<String> keySet = data.keySet();
//                for(String key:keySet){
//                    Object object = data.get(key);
//                    System.out.println(key +" = "+object);
//                }
//                LOGGER.info("发送短信验证码成功，手机号码：{}，验证码：{}",phone,code);
//                return true;
//            }else{
//                LOGGER.error("发送短信验证码失败，手机号码：{}，验证码：{},错误信息：{}",phone,code,result.get("statusMsg"));
//                //todo
//               return true;
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//            return true;
//        }
    }




    public static void main(String[] args) {
        //生产环境请求地址：app.cloopen.com
        String serverIp = "app.cloopen.com";
        //请求端口
        String serverPort = "8883";
        //主账号,登陆云通讯网站后,可在控制台首页看到开发者主账号ACCOUNT SID和主账号令牌AUTH TOKEN
        String accountSId = "2c94811c9ac8c114019dc8b1ffd076e5";
        String accountToken = "60ee68a592ce482baca10dc80fb05e4f";
        //请使用管理控制台中已创建应用的APPID
        String appId = "2c94811c9ac8c114019dc8b2005776ec";
        CCPRestSmsSDK sdk = new CCPRestSmsSDK();
        sdk.init(serverIp, serverPort);
        sdk.setAccount(accountSId, accountToken);
        sdk.setAppId(appId);
        sdk.setBodyType(BodyType.Type_JSON);
        String to = "17280269489";
        String templateId= "1";
        int code = RandomUtils.nextInt(1000, 9999);
        System.out.println("短信验证码："+ code);
        String[] datas = {String.valueOf(code),"1"};
        String subAppend="1234";  //可选 扩展码，四位数字 0~9999
        String reqId= UUID.randomUUID().toString();  //可选 第三方自定义消息id，最大支持32位英文数字，同账号下同一自然天内不允许重复
        //HashMap<String, Object> result = sdk.sendTemplateSMS(to,templateId,datas);
        HashMap<String, Object> result = sdk.sendTemplateSMS(to,templateId,datas,subAppend,reqId);
        if("000000".equals(result.get("statusCode"))){
            //正常返回输出data包体信息（map）
            HashMap<String,Object> data = (HashMap<String, Object>) result.get("data");
            Set<String> keySet = data.keySet();
            for(String key:keySet){
                Object object = data.get(key);
                System.out.println(key +" = "+object);
            }
        }else{
            //异常返回输出错误码和错误信息
            System.out.println("错误码=" + result.get("statusCode") +" 错误信息= "+result.get("statusMsg"));
        }
    }
}
