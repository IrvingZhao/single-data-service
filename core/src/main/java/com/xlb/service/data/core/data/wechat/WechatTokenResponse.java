package com.xlb.service.data.core.data.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WechatTokenResponse {
    @JsonProperty("errcode")
    private String errorCode;

    @JsonProperty("errmsg")
    private String errMsg;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private int expires;
}
