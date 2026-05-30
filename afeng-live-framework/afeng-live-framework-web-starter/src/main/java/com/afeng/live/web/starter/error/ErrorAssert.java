package com.afeng.live.web.starter.error;

public class ErrorAssert {


    /**
     * 判断参数不能为空
     *
     * @param obj
     * @param afengBaseError
     */
    public static void isNotNull(Object obj, AfengBaseError afengBaseError) {
        if (obj == null) {
            throw new AfengErrorException(afengBaseError);
        }
    }

    /**
     * 判断字符串不能为空
     *
     * @param str
     * @param afengBaseError
     */
    public static void isNotBlank(String str, AfengBaseError afengBaseError) {
        if (str == null || str.trim().length() == 0) {
            throw new AfengErrorException(afengBaseError);
        }
    }

    /**
     * flag == true
     *
     * @param flag
     * @param afengBaseError
     */
    public static void isTure(boolean flag, AfengBaseError afengBaseError) {
        if (!flag) {
            throw new AfengErrorException(afengBaseError);
        }
    }

    /**
     * flag == true
     *
     * @param flag
     * @param afengErrorException
     */
    public static void isTure(boolean flag, AfengErrorException afengErrorException) {
        if (!flag) {
            throw afengErrorException;
        }
    }
}

