package com.afeng.live.framework.redis.starter.keys;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;


/**
 * @Author idea
 * @Date: Created in 10:23 2023/6/20
 * @Description
 */
@Configuration
@Conditional(RedisKeyLoadMatch.class)
public class GiftProviderCacheKeyBuilder extends RedisKeyBuilder {

    private static String GIFT_CONFIG_CACHE = "gift_config_cache";
    private static String GIFT_LIST_CACHE = "gift_list_cache";
    private static String GIFT_CONSUME_KEY = "gift_consume_key";
    private static String GIFT_LIST_LOCK = "gift_list_lock";
    private static String LIVING_PK_KEY = "living_pk_key";
    private static String LIVING_PK_SEND_SEQ = "living_pk_send_seq";
    private static String LIVING_PK_IS_OVER = "living_pk_is_over";
    private static String RED_PACKET_LIST = "red_packet_list";
    private static String RED_PACKET_INIT_LOCK = "red_packet_init_lock";
    private static String RED_PACKET_TOTAL_GET_CACHE = "red_packet_total_get_cache";
    private static String RED_PACKET_TOTAL_GET_PRICE_CACHE = "red_packet_total_get_price_cache";
    private static String MAX_GET_PRICE_CACHE = "max_get_price_cache";
    private static String USER_TOTAL_GET_PRICE_CACHE = "user_total_get_price_cache";
    private static String RED_PACKET_PREPARE_SUCCESS = "red_packet_prepare_success";
    private static String RED_PACKET_NOITFY = "red_packet_notify";
    //定时任务加锁
    private static String SKU_STOCK_SYN_LOCK = "sku_stock_syn_lock";
    //库存缓存
    private static String SKU_STOCK = "sku_stock";
    //用户购物车
    private static String SHOP_CAR = "shop_car";
    //商品详情
    private static String SKU_DETAIL_KEY = "sku_detail_key";

    public String buildSkuStockLock() {
        return super.getPrefix() + SKU_STOCK_SYN_LOCK;
    }

    public String buildRedPacketNotifyCache(String code) {
        return super.getPrefix() + RED_PACKET_NOITFY + super.getSplitItem() + code;
    }

    public String buildSkuStockCache(Long skuId) {
        return super.getPrefix() + SKU_STOCK + super.getSplitItem() + skuId;
    }

    public String buildUserShopCarCache(Long userId,Integer roomId) {
        return super.getPrefix() + SHOP_CAR + super.getSplitItem() + userId + super.getSplitItem() + roomId;
    }

    public String buildSkuDetailCache(Long skuId) {
        return super.getPrefix() + SKU_DETAIL_KEY + super.getSplitItem() + skuId;
    }

    public String buildRedPacketPrepareSuccessCache(String code) {
        return super.getPrefix() + RED_PACKET_PREPARE_SUCCESS + super.getSplitItem() + code;
    }

    public String buildUserTotalGetPriceCache(Long uid) {
        return super.getPrefix() + USER_TOTAL_GET_PRICE_CACHE + super.getSplitItem() + uid;
    }

    public String buildRedPacketInitLock(String code) {
        return super.getPrefix() + RED_PACKET_INIT_LOCK + super.getSplitItem() + code;
    }
    public String buildRedPacketTotalGetCache(String code) {
        return super.getPrefix() + RED_PACKET_TOTAL_GET_CACHE + super.getSplitItem() + code;
    }
    public String buildRedPacketTotalGetPriceCache(String code) {
        return super.getPrefix() + RED_PACKET_TOTAL_GET_PRICE_CACHE + super.getSplitItem() + code;
    }
    public String buildRedPacketMaxGetPriceCache(String code) {
        return super.getPrefix() + MAX_GET_PRICE_CACHE + super.getSplitItem() + code;
    }


    public String buildLivingPkIsOver(Integer roomId) {
        return super.getPrefix() + LIVING_PK_IS_OVER + super.getSplitItem() + roomId;
    }

    public String buildLivingPkSendSeq(Integer roomId) {
        return super.getPrefix() + LIVING_PK_SEND_SEQ + super.getSplitItem() + roomId;
    }

    public String buildLivingPkKey(Integer roomId) {
        return super.getPrefix() + LIVING_PK_KEY + super.getSplitItem() + roomId;
    }

    public String buildGiftConsumeKey(String uuid) {
        return super.getPrefix() + GIFT_CONSUME_KEY + super.getSplitItem() + uuid;
    }

    public String buildGiftConfigCacheKey(int giftId) {
        return super.getPrefix() + GIFT_CONFIG_CACHE + super.getSplitItem() + giftId;
    }

    public String buildGiftListCacheKey() {
        return super.getPrefix() + GIFT_LIST_CACHE;
    }

    public String buildGiftListLockCacheKey() {
        return super.getPrefix() + GIFT_LIST_LOCK;
    }
    public String buildRedPacketListCacheKey(String code) {
        return super.getPrefix() + RED_PACKET_LIST + super.getSplitItem() + code;
    }
}
