package com.xlb.service.data.core.data.huawei;

import com.xlb.service.data.core.data.wechat.WechatTokenResponse;
import com.xlb.service.data.core.util.base.ObjectStringSerialUtil;
import com.xlb.service.data.core.util.http.enums.HttpMethod;
import com.xlb.service.data.core.util.http.message.HttpMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HuaweiTokenRequest implements HttpMessage {
    private final static ObjectStringSerialUtil SERIAL_UTIL = ObjectStringSerialUtil.getSerialUtil();

    public HuaweiTokenRequest(String clientId, String clientSecret) {
        requestParams = new HashMap<>();
        requestParams.put("grant_type", "client_credentials");
        requestParams.put("client_id", clientId);
        requestParams.put("client_secret", clientSecret);
    }

    @Getter
    private final Map<String, Object> requestParams;

    @Getter
    private HuaweiTokenResponse tokenResponse;

    @Setter
    @Getter
    private int responseCode;

    @Setter
    private Map<String, String> responseHead;

    @Override
    public String getRequestUrl() {
        return "https://oauth-login.cloud.huawei.com/oauth2/v3/token";
    }

    @Override
    public HttpMethod getRequestMethod() {
        return HttpMethod.GET;
    }

    @Override
    public void setResponseStream(InputStream inputStream) {
        if (this.responseCode == 200) {
            var nspStatus = responseHead.get("NSP_STATUS");
            if ("0".equals(nspStatus)) {
                this.tokenResponse = SERIAL_UTIL.parse(inputStream, HuaweiTokenResponse.class, ObjectStringSerialUtil.SerialType.JSON);
            } else {
                this.tokenResponse = new HuaweiTokenResponse();
            }
            this.tokenResponse.setNspStatus(NumberUtils.toInt(nspStatus, -1));
        } else {
            log.error("get huawei token error {}", streamToString(inputStream));
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
