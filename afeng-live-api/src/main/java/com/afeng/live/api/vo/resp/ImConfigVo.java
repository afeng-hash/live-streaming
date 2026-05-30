package com.afeng.live.api.vo.resp;

import lombok.Data;

/**
 * 返回给客户端的im地址与认证token
 */
@Data
public class ImConfigVo {
    private String token;
    private String wsImServerAddress;
    private String tcpImServerAddress;
}
