package com.afeng.live.consumer.common;

import com.afeng.live.im.constants.ImConstants;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class ImMsg implements Serializable {
    //魔数，用于做基本的校验
    private short magic;

    //消息类型，后续交给不同的handler处理
    private int code;

    //用于记录body的长度
    private int length;

    //存储消息体的内容，一般会按照字节数组的方式去存放
    private byte[] body;

    public static ImMsg build(int code, String data) {
        ImMsg imMsg = new ImMsg();
        imMsg.setCode(code);
        imMsg.setBody(data.getBytes());
        imMsg.setLength(data.getBytes().length);
        imMsg.setMagic(ImConstants.DEFAULT_MAGIC);
        return imMsg;
    }
}
