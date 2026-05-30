package com.afeng.live.id.generater.provider.service.impl;

import com.afeng.live.id.generater.provider.dao.mapper.IdGenerateMapper;
import com.afeng.live.id.generater.provider.dao.po.IdGeneratePO;
import com.afeng.live.id.generater.provider.service.IdGenerateService;
import com.afeng.live.id.generater.provider.service.bo.LocalSeqIdBO;
import com.afeng.live.id.generater.provider.service.bo.LocalUnSeqIdBO;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class IdGenerateServiceImpl implements IdGenerateService, InitializingBean {

    @Resource
    private IdGenerateMapper idGenerateMapper;

    private final static Logger LOGGER = LoggerFactory.getLogger(IdGenerateServiceImpl.class);

    private static Map<Long, LocalSeqIdBO> localSeqIdMap = new ConcurrentHashMap<>();
    private static Map<Long, LocalUnSeqIdBO> localUnSeqIdMap = new ConcurrentHashMap<>();

    private static final float UPDATE_RATE = 0.75f;
    private static final int SEQ_ID = 1;

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(8, 16, 3, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("id-generate-thread-" + ThreadLocalRandom.current().nextInt(1000));
            return thread;
        }
    });

    private static Map<Long,Semaphore> semaphoreMap = new ConcurrentHashMap<>();



    /**
     * 根据本地步长度来生成唯一 id(区间性递增)
     *
     * @param id
     * @return
     */
    @Override
    public Long getSeqId(Long id) throws Exception {
        if(id == null){
            LOGGER.error("getSeqId id 不能为空");
            return null;
        }
        LocalSeqIdBO localSeqIdBO = localSeqIdMap.get(id);
        if(localSeqIdBO == null){
            LOGGER.error("[getSeqId] localSeqIdBO is null,id:{}",id);
            return null;
        }
        this.refreshLocalSeqId(localSeqIdBO);
        long returnId = localSeqIdBO.getCurrentNum().incrementAndGet();
        if (returnId > localSeqIdBO.getNextThreadshold()){
            LOGGER.error("[getSeqId] id is over limit,id:{}",id);
            return null;
        }
        return returnId;
    }



    /**
     * 获取非连续性id
     * @param id
     * @return
     */
    @Override
    public Long getUnSeqId(Long id) throws Exception {
        if(id == null){
            LOGGER.error("getSeqId id 不能为空");
            return null;
        }
        LocalUnSeqIdBO localUnSeqIdBO = localUnSeqIdMap.get(id);
        if(localUnSeqIdBO == null){
            LOGGER.error("[getSeqId] localUnSeqIdBO is null,id:{}",id);
            return null;
        }
        Long returnId = localUnSeqIdBO.getIdQueue().poll();
        if (returnId == null){
            LOGGER.error("[getSeqId] id is over limit,id:{}",id);
            return null;
        }
        this.refreshLocalUnSeqId(localUnSeqIdBO);
        return returnId;
    }


    /**
     * 根据本地步长度来生成唯一 id(区间性递增)
     *
     * @param id
     * @return
     */
    @Override
    public String increaseSeqStrId(Integer id) {
        return "";
    }


    /**
     * bean初始化回回到这里
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<IdGeneratePO> idGeneratePOS = idGenerateMapper.selectAll();
        LOGGER.info("开始初始化id段：{}",idGeneratePOS);
        for (IdGeneratePO idGeneratePO : idGeneratePOS) {
            tryUpdateMysqlRecord(idGeneratePO);
            semaphoreMap.put(idGeneratePO.getId(),new Semaphore(1));
        }
    }


    /**
     * 刷新本地有序id段
     * @param localSeqIdBO
     */
    private void refreshLocalSeqId(LocalSeqIdBO localSeqIdBO) throws Exception {
        long step = localSeqIdBO.getNextThreadshold() - localSeqIdBO.getCurrentStart();
        if(localSeqIdBO.getCurrentNum().get() - localSeqIdBO.getCurrentStart() > step * UPDATE_RATE){
            Semaphore semaphore = semaphoreMap.get(localSeqIdBO.getId());
            if (semaphore == null){
                LOGGER.error("semaphore is null,id:{}",localSeqIdBO.getId());
                return;
            }
            boolean acquireStatus = semaphore.tryAcquire();
            if (acquireStatus){
                //异步同步id段操作
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            IdGeneratePO idGeneratePO = idGenerateMapper.selectById(localSeqIdBO.getId());
                            tryUpdateMysqlRecord(idGeneratePO);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }finally {
                            semaphore.release();
                        }
                    }
                });
            }

        }
    }

    /**
     * 刷新本地非连续性id段
     * @param localUnSeqIdBO
     */
    private void refreshLocalUnSeqId(LocalUnSeqIdBO localUnSeqIdBO) throws Exception {
        long step = localUnSeqIdBO.getNextThreadshold() - localUnSeqIdBO.getCurrentStart();
        int remainSize = localUnSeqIdBO.getIdQueue().size();
        if (remainSize < step * (1 - UPDATE_RATE)){
            Semaphore semaphore = semaphoreMap.get(localUnSeqIdBO.getId());
            if (semaphore == null){
                LOGGER.error("semaphore is null,id:{}",localUnSeqIdBO.getId());
                return;
            }
            boolean acquireStatus = semaphore.tryAcquire();
            if (acquireStatus){
                //异步同步id段操作
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            IdGeneratePO idGeneratePO = idGenerateMapper.selectById(localUnSeqIdBO.getId());
                            tryUpdateMysqlRecord(idGeneratePO);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }finally {
                            semaphore.release();
                        }
                    }
                });
            }
        }
    }



    /**
     * 尝试更新mysql记录
     * @param idGeneratePO
     * @throws Exception
     */
    private void tryUpdateMysqlRecord(IdGeneratePO idGeneratePO) throws Exception {

        int updateResult = idGenerateMapper.updateNewIdCountAndVersion(idGeneratePO.getId(), idGeneratePO.getVersion());
        if (updateResult > 0){
            localIdBOHandle(idGeneratePO);
            return;
        }

        for (int i = 0; i < 3; i++) {
            idGeneratePO = idGenerateMapper.selectById(idGeneratePO.getId());
            updateResult = idGenerateMapper.updateNewIdCountAndVersion(idGeneratePO.getId(), idGeneratePO.getVersion());
            if (updateResult > 0){
                localIdBOHandle(idGeneratePO);
                return;
            }
        }

        throw new Exception("表id段占用失败，竞争过于激烈，id is :" + idGeneratePO.getId());
    }

    /**
     * 处理将本地id对象放入map中，并且进行初始化
     * @param idGeneratePO
     */
    private void localIdBOHandle(IdGeneratePO idGeneratePO){
        Long currentStart = idGeneratePO.getCurrentStart();
        Long nextThreshold = idGeneratePO.getNextThreshold();
        if (idGeneratePO.getIsSeq() == SEQ_ID){
            LocalSeqIdBO localSeqIdBO = new LocalSeqIdBO();
            localSeqIdBO.setId(idGeneratePO.getId());
            localSeqIdBO.setCurrentNum(new AtomicLong(idGeneratePO.getCurrentStart()));
            localSeqIdBO.setCurrentStart(currentStart);
            localSeqIdBO.setNextThreadshold(nextThreshold);
            localSeqIdMap.put(idGeneratePO.getId(),localSeqIdBO);
        }else{
            LocalUnSeqIdBO localUnSeqIdBO = new LocalUnSeqIdBO();
            localUnSeqIdBO.setCurrentStart(currentStart);
            localUnSeqIdBO.setNextThreadshold(nextThreshold);
            localUnSeqIdBO.setId(idGeneratePO.getId());
            Long begin = localUnSeqIdBO.getCurrentStart();
            Long end = localUnSeqIdBO.getNextThreadshold();
            List<Long> idList = new ArrayList<>();
            for (Long i = begin; i < end; i++) {
                idList.add(i);
            }
            //将本地id段随机打乱放入队列中
            Collections.shuffle(idList);
            ConcurrentLinkedQueue<Long> idQueue = new ConcurrentLinkedQueue<>(idList);
            localUnSeqIdBO.setIdQueue(idQueue);
            localUnSeqIdMap.put(idGeneratePO.getId(),localUnSeqIdBO);
        }
    }
}
