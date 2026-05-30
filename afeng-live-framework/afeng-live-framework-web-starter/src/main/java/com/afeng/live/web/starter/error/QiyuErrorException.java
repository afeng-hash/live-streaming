package com.afeng.live.web.starter.error;

public class AfengErrorException extends RuntimeException{

    private int errorCode;
    private String errorMsg;

    public AfengErrorException(int errorCode,String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public AfengErrorException(AfengBaseError afengBaseError) {
        this.errorCode = afengBaseError.getErrorCode();
        this.errorMsg = afengBaseError.getErrorMsg();
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
