package com.afeng.live.sms.interfaces.enums;

public enum SmsTemplateIdEnum {
    SMS_LOGIN_TEMPLATE("1", "登录验证码模板");

    String templateId;
    String desc;

    SmsTemplateIdEnum(String templateId, String desc) {
        this.templateId = templateId;
        this.desc = desc;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getDesc() {
        return desc;
    }
}
