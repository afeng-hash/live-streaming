package com.afeng.live.common.interfaces.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

    /**
     * 将一个大的list集合拆解为多个子list集合
     * @param list
     * @param subNum
     * @return
     * @param <T>
     */
    public static <T> List<List<T>> splistList(List<T> list, int subNum){
        List<List<T>> resultList = new ArrayList<>();
        int priIndex = 0;
        int lastIndex = 0;
        int insertTimes = list.size() / subNum;
        List<T> subList ;
        for (int i = 0; i<=insertTimes;i++){
            priIndex = subNum + 1;
            lastIndex = priIndex + subNum;
            if (i != insertTimes){
                subList = list.subList(priIndex,lastIndex);
            }else {
                subList = list.subList(priIndex,list.size());
            }
            if (!subList.isEmpty()){
                resultList.add(subList);
            }
        }
        return resultList;
    }
}
