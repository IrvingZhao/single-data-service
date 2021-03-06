package com.xlb.service.data.core.data.wechat;

import com.xlb.service.data.core.util.base.ObjectStringSerialUtil;
import com.xlb.service.data.core.util.http.enums.HttpMethod;
import com.xlb.service.data.core.util.http.message.HttpMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Slf4j
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
        } else {
            log.error("get wechat token error {}", streamToString(inputStream));
        }
    }

    private String streamToString(InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            var builder = new StringBuilder();
            var temp = "";
            while ((temp = reader.readLine()) != null) {
                builder.append(temp);
            }
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
