package com.afeng.live.sms.interfaces.msg;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class MsgCheckDTO implements Serializable {

//    @Serial
//    private static final long serialVersionUID = 3394248744287019717L;
    private boolean checkStatus;
    private String desc;

}
