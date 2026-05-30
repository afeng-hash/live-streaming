package com.afeng.live.web.starter.thread;

import com.afeng.live.web.starter.constants.RequestConstants;
import org.apache.dubbo.common.threadlocal.InternalThreadLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 请求上下文
 */
public class AfengRequestContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfengRequestContext.class);

    private static final ThreadLocal<Map<Object,Object>> resouces = new InternalThreadLocalMap();

    public static void set(Object key,Object value) throws Exception {
        if (key == null){
            throw new Exception("key is null");
        }
        if (value == null){
            resouces.get().remove(key);
        }
        resouces.get().put(key,value);
    }

    public static Object get(Object key) {
        if (key == null){
            LOGGER.error("key is null");
            return null;
        }
        return resouces.get().get(key);
    }


    public static void clear(){
        resouces.remove();
    }

    public static Long getUserId() {
        Object userId = get(RequestConstants.AFENG_USER_ID);
        return userId == null ? null : Long.valueOf(userId.toString());
    }

    //实现父子线程之间的线程本地变量传递
    // A-> threadLocal("userId",1)
    //A-> new Thread(B) -> B线程属于A的子线程
    private static class InternalThreadLocalMap extends InheritableThreadLocal<Map<Object,Object>> {
        @Override
        protected Map<Object,Object> initialValue() {
            return new HashMap<>();
        }

        @Override
        protected Map<Object, Object> childValue(Map<Object, Object> parentValue) {
            if (parentValue != null){
                return (Map<Object, Object>) ((HashMap<Object,Object>)parentValue).clone();
            }else {
                return null;
            }
        }
    }
}
