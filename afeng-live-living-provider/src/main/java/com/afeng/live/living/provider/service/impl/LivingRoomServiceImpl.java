package com.afeng.live.living.provider.service.impl;

import com.afeng.live.common.interfaces.ConvertBeanUtils;
import com.afeng.live.common.interfaces.dto.PageWrapper;
import com.afeng.live.common.interfaces.enums.CommonStatusEum;
import com.afeng.live.framework.redis.starter.keys.LivingProviderCacheKeyBuilder;
import com.afeng.live.im.constants.AppIdEnum;
import com.afeng.live.im.core.server.interfaces.dto.ImOfflineDto;
import com.afeng.live.im.core.server.interfaces.dto.ImOnlineDto;
import com.afeng.live.im.dto.ImMsgBody;
import com.afeng.live.im.router.interfaces.constants.ImMsgBizCodeEnum;
import com.afeng.live.im.router.interfaces.rpc.ImRouterRpc;
import com.afeng.live.living.provider.dao.mapper.LivingRoomMapper;
import com.afeng.live.living.provider.dao.po.LivingRoomPO;
import com.afeng.live.living.provider.service.ILivingRoomService;
import com.afeng.live.living.provider.service.ILivingRoomTxService;
import com.afeng.living.interfaces.dto.LivingPkRespDTO;
import com.afeng.living.interfaces.dto.LivingRoomReqDTO;
import com.afeng.living.interfaces.dto.LivingRoomRespDTO;
import com.afeng.living.interfaces.enums.LivingRoomTypeEnum;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LivingRoomServiceImpl implements ILivingRoomService {

    @Resource
    private LivingRoomMapper livingRoomMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private LivingProviderCacheKeyBuilder cacheKeyBuilder;
    @DubboReference
    private ImRouterRpc imRouterRpc;
    @Resource
    private ILivingRoomTxService iLivingRoomTxService;

    /**
     * 开启直播间
     *
     * @param livingRoomReqDTO
     * @return
     */
    @Override
    public Integer startLivingRoom(LivingRoomReqDTO livingRoomReqDTO) {
        LivingRoomPO livingRoomPO = ConvertBeanUtils.convert(livingRoomReqDTO, LivingRoomPO.class);
        livingRoomPO.setStatus(CommonStatusEum.VALID_STATUS.getCode());
        livingRoomPO.setStartTime(new Date());
        livingRoomMapper.insert(livingRoomPO);
        String cacheKey = cacheKeyBuilder.buildLivingRoomObj(livingRoomPO.getId());
        //防止之前有空值缓存，这里做移除操作
        redisTemplate.delete(cacheKey);
        return livingRoomPO.getId();
    }


    /**
     * 查询直播间
     *
     * @param roomId
     * @return
     */
    @Override
    public LivingRoomRespDTO queryByRoomId(Integer roomId) {
        String cacheKey = cacheKeyBuilder.buildLivingRoomObj(roomId);
        LivingRoomRespDTO queryResult = (LivingRoomRespDTO) redisTemplate.opsForValue().get(cacheKey);
        if (queryResult != null) {
            //空值缓存
            if (queryResult.getId() == null) {
                return null;
            }
            return queryResult;
        }
        LambdaQueryWrapper<LivingRoomPO> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(LivingRoomPO::getId, roomId);
        queryWrapper.eq(LivingRoomPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        queryWrapper.last("limit 1");
        queryResult = ConvertBeanUtils.convert(livingRoomMapper.selectOne(queryWrapper), LivingRoomRespDTO.class);
        if (queryResult == null) {
            //防止缓存击穿
            redisTemplate.opsForValue().set(cacheKey, new LivingRoomRespDTO(), 1, TimeUnit.MINUTES);
            return null;
        }
//        if (LivingRoomTypeEnum.PK_LIVING_ROOM.getCode().equals(queryResult.getType())) {
//            queryResult.setPkObjId(this.queryOnlinePkUserId(roomId));
//        }
        redisTemplate.opsForValue().set(cacheKey, queryResult, 30, TimeUnit.MINUTES);
        return queryResult;
    }


    /**
     * 直播间列表
     *
     * @param livingRoomReqDTO
     * @return
     */
    @Override
    public PageWrapper<LivingRoomRespDTO> list(LivingRoomReqDTO livingRoomReqDTO) {
        log.info("[LivingRoomServiceImpl] 分页查询直播间");
//        PageWrapper<LivingRoomRespDTO> pageWrapper = new PageWrapper<>();
//        LambdaQueryWrapper<LivingRoomPO> queryWrapper = new LambdaQueryWrapper();
//        queryWrapper.eq(LivingRoomPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
//        queryWrapper.eq(LivingRoomPO::getType, livingRoomReqDTO.getType());
//        queryWrapper.orderByDesc(LivingRoomPO::getStartTime);
//        Page<LivingRoomPO> pageResult = livingRoomMapper.selectPage(new Page<>((long) livingRoomReqDTO.getPage(), (long) livingRoomReqDTO.getPageSize()), queryWrapper);
//        pageWrapper.setList(ConvertBeanUtils.convertList(pageResult.getRecords(), LivingRoomRespDTO.class));
//        pageWrapper.setHasNext((long) livingRoomReqDTO.getPage() *livingRoomReqDTO.getPageSize() < pageResult.getTotal());
//        return pageWrapper;

        String cacheKey = cacheKeyBuilder.buildLivingRoomList(livingRoomReqDTO.getType());
        int page = livingRoomReqDTO.getPage();
        int pageSize = livingRoomReqDTO.getPageSize();
        long total = redisTemplate.opsForList().size(cacheKey);
        List<Object> resultList = redisTemplate.opsForList().range(cacheKey, (page - 1) * pageSize, (page * pageSize));
        PageWrapper<LivingRoomRespDTO> pageWrapper = new PageWrapper<>();
        if (CollectionUtils.isEmpty(resultList)) {
            pageWrapper.setList(Collections.emptyList());
            pageWrapper.setHasNext(false);
            return pageWrapper;
        } else {
            List<LivingRoomRespDTO> livingRoomRespDTOS = ConvertBeanUtils.convertList(resultList, LivingRoomRespDTO.class);
            pageWrapper.setList(livingRoomRespDTOS);
            pageWrapper.setHasNext(page * pageSize < total);
            return pageWrapper;
        }
    }


    /**
     * 获取所有直播间
     *
     * @param type
     * @return
     */
    @Override
    public List<LivingRoomRespDTO> listAllLivingRoomFromDB(Integer type) {
        LambdaQueryWrapper<LivingRoomPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LivingRoomPO::getStatus, CommonStatusEum.VALID_STATUS.getCode());
        queryWrapper.eq(LivingRoomPO::getType, type);
        //按照时间倒序展示
        queryWrapper.orderByDesc(LivingRoomPO::getId);
        queryWrapper.last("limit 1000");
        return ConvertBeanUtils.convertList(livingRoomMapper.selectList(queryWrapper), LivingRoomRespDTO.class);
    }

    /**
     * 用户上线处理
     *
     * @param imOnlineDto
     */
    @Override
    public void userOnlineHandler(ImOnlineDto imOnlineDto) {
        log.info("[LivingRoomServiceImpl] 用户上线处理");
        Long userId = imOnlineDto.getUserId();
        Integer roomId = imOnlineDto.getRoomId();
        Integer appId = imOnlineDto.getAppId();
        String key = cacheKeyBuilder.buildLivingRoomUserSet(roomId, appId);
        redisTemplate.opsForSet().add(key, userId);
        redisTemplate.expire(key, 12, TimeUnit.HOURS);
    }




    /**
     * 根据roomId批量查询userId
     * @param livingRoomReqDTO
     * @return
     */
    @Override
    public List<Long> queryUserIdsByRoomId(LivingRoomReqDTO livingRoomReqDTO) {
        Integer roomId = livingRoomReqDTO.getRoomId();
        Integer appId = livingRoomReqDTO.getAppId();
        String key = cacheKeyBuilder.buildLivingRoomUserSet(roomId, appId);
        //0-100 101-200 201-300 300-末尾
        Cursor<Object> scan = redisTemplate.opsForSet().scan(key, ScanOptions.scanOptions().match("*").count(100).build());
        List<Long> result = new ArrayList<>();
        while (scan.hasNext()){
            result.add(Long.parseLong(scan.next().toString()));
        }
        return result;
    }

    /**
     * 根据roomId查询当前pk人是谁
     *
     * @param roomId
     * @return
     */
    @Override
    public Long queryOnlinePkUserId(Integer roomId) {
        String cacheKey = cacheKeyBuilder.buildLivingOnlinePk(roomId);
        Object userId = redisTemplate.opsForValue().get(cacheKey);
        return userId != null ? Long.valueOf((int) userId) : null;
    }


    /**
     * 用户在pk直播间中，连上线请求
     *
     * @param livingRoomReqDTO
     * @return
     */
    @Override
    public LivingPkRespDTO onlinePk(LivingRoomReqDTO livingRoomReqDTO) {
        LivingRoomRespDTO currentLivingRoom = this.queryByRoomId(livingRoomReqDTO.getRoomId());
        LivingPkRespDTO respDTO = new LivingPkRespDTO();
        respDTO.setOnlineStatus(false);
        if (currentLivingRoom.getAnchorId().equals(livingRoomReqDTO.getPkObjId())) {
            respDTO.setMsg("主播不可以连线参与pk");
            return respDTO;
        }
        String cacheKey = cacheKeyBuilder.buildLivingOnlinePk(livingRoomReqDTO.getRoomId());
        boolean tryOnline = redisTemplate.opsForValue().setIfAbsent(cacheKey, livingRoomReqDTO.getPkObjId(), 30, TimeUnit.HOURS);
        if (tryOnline) {
            List<Long> userIdList = this.queryUserIdsByRoomId(livingRoomReqDTO);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("pkObjId", livingRoomReqDTO.getPkObjId());
            jsonObject.put("pkObjAvatar", "https://picdm.sunbangyan.cn/2023/08/29/w2qq1k.jpeg");
            batchSendImMsg(userIdList, ImMsgBizCodeEnum.LIVING_ROOM_PK_ONLINE.getCode(), jsonObject);
            respDTO.setMsg("连线成功");
            respDTO.setOnlineStatus(false);
        } else {
            respDTO.setMsg("目前有人在线，请稍后再试");
        }
        return respDTO;
    }

    /**
     * 用户下线处理
     * 清除缓存中用户与roomId的关系
     * @param imOfflineDto
     */
    @Override
    public void userOfflineHandler(ImOfflineDto imOfflineDto) {
        log.info("offline handler,imOfflineDTO is {}", imOfflineDto);
        Long userId = imOfflineDto.getUserId();
        Integer roomId = imOfflineDto.getRoomId();
        Integer appId = imOfflineDto.getAppId();
        String cacheKey = cacheKeyBuilder.buildLivingRoomUserSet(roomId, appId);
        redisTemplate.opsForSet().remove(cacheKey, userId);
        //监听pk主播下线行为
        LivingRoomReqDTO roomReqDTO = new LivingRoomReqDTO();
        roomReqDTO.setRoomId(imOfflineDto.getRoomId());
        roomReqDTO.setPkObjId(imOfflineDto.getUserId());
        roomReqDTO.setAnchorId(imOfflineDto.getUserId());
        this.offlinePk(roomReqDTO);
        //当主播断开im服务器的时候，也要监听它的动作，然后将直播间的状态修改为关闭状态
        iLivingRoomTxService.closeLiving(roomReqDTO);
    }

    /**
     * 批量发送IM消息
     *
     * @param userIdList
     * @param bizCode
     * @param jsonObject
     */
    private void batchSendImMsg(List<Long> userIdList, int bizCode, JSONObject jsonObject) {
        List<ImMsgBody> imMsgBodies = userIdList.stream().map(userId -> {
            ImMsgBody imMsgBody = new ImMsgBody();
            imMsgBody.setAppId(AppIdEnum.AFENG_LIVE_BIZ.getCode());
            imMsgBody.setBizCode(bizCode);
            imMsgBody.setUserId(userId);
            imMsgBody.setData(jsonObject.toJSONString());
            return imMsgBody;
        }).collect(Collectors.toList());
        imRouterRpc.batchSendMsg(imMsgBodies);
    }


    /**
     * 用户在pk直播间中，下线请求
     *
     * @param livingRoomReqDTO
     * @return
     */
    @Override
    public boolean offlinePk(LivingRoomReqDTO livingRoomReqDTO) {
        String cacheKey = cacheKeyBuilder.buildLivingOnlinePk(livingRoomReqDTO.getRoomId());
        return redisTemplate.delete(cacheKey);
    }

    /**
     * 根据主播id查询直播间信息
     * @param anchorId
     * @return
     */
    @Override
    public LivingRoomRespDTO queryByAnchorId(Long anchorId) {
        LambdaQueryWrapper<LivingRoomPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LivingRoomPO::getAnchorId,anchorId);
        queryWrapper.eq(LivingRoomPO::getStatus,CommonStatusEum.VALID_STATUS);
        queryWrapper.last("limit 1");
        return ConvertBeanUtils.convert(livingRoomMapper.selectOne(queryWrapper),LivingRoomRespDTO.class);
    }
}
