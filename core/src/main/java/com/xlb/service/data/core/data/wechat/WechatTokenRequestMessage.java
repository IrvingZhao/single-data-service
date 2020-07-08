package com.xlb.service.data.core.data.wechat;

import com.xlb.service.data.core.util.base.ObjectStringSerialUtil;
import com.xlb.service.data.core.util.http.enums.HttpMethod;
import com.xlb.service.data.core.util.http.message.HttpMessage;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class WechatTokenRequestMessage implements HttpMessage {

    private final static ObjectStringSerialUtil SERIAL_UTIL = ObjectStringSerialUtil.getSerialUtil();

    public WechatTokenRequestMessage(String appId, String appSecurity) {
        requestParams = new HashMap<>();
        requestParams.put("grant_type", "client_credential");
        requestParams.put("appid", appId);
        requestParams.put("secret", appSecurity);
    }

    @Getter
    private final Map<String, Object> requestParams;

    @Getter
    private WechatTokenResponse tokenResponse;

    @Setter
    @Getter
    private int responseCode;

    @Override
    public String getRequestUrl() {
        return "https://api.weixin.qq.com/cgi-bin/token";
    }

    @Override
    public HttpMethod getRequestMethod() {
        return HttpMethod.GET;
    }

    @Override
    public void setResponseStream(InputStream inputStream) {
        if (this.responseCode == 200) {
            this.tokenResponse = SERIAL_UTIL.parse(inputStream, WechatTokenResponse.class, ObjectStringSerialUtil.SerialType.JSON);
        }
    }
}
