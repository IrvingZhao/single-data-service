package com.xlb.service.data.core.data.wechat;

import com.xlb.service.data.core.data.SingleDataInfo;
import com.xlb.service.data.core.exception.RefreshParamException;
import com.xlb.service.data.core.util.http.HttpClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 微信 token 信息
 */
@Slf4j
public class WechatTokenInfo implements SingleDataInfo {

    private final AtomicInteger refreshTime = new AtomicInteger(0);
    private final HttpClient client = new HttpClient();

    private String appId;
    private String appSecurity;

    @Getter
    private String data;

    @Override
    public void init(Map<String, String> config) {
        this.appId = config.get("appId");
        this.appSecurity = config.get("appSecurity");
    }

    @Override
    public int refresh() {
        int nowTime = refreshTime.addAndGet(1);
        if (nowTime > 5) {
            refreshTime.set(0);
            String msg = MessageFormat.format("[{0}] refresh has 5 times timeout", this.appId);
            log.error(msg);
            return -1;
            // 刷新超过5次后，直接返回，停止定时任务
        }
        WechatTokenRequestMessage requestMessage = new WechatTokenRequestMessage(this.appId, this.appSecurity);
        client.sendMessage(requestMessage);
        WechatTokenResponse response = requestMessage.getTokenResponse();
        if (response == null) {
            log.error("http request error, [{}]", this.appId);
            return this.refresh();
        }
        String errorCode = response.getErrorCode();
        if (StringUtils.isBlank(errorCode) || "0".equals(errorCode)) {
            this.data = response.getAccessToken();
            refreshTime.set(0);
            return response.getExpires();
        } else if ("-1".equals(errorCode)) {
            return this.refresh();
        } else {
            String msg = MessageFormat.format("Wechat [{0}] refresh token failed.[{1}]:{2}",
                    this.appId, response.getErrorCode(), response.getErrMsg());
            log.error(msg);
            refreshTime.set(0);
            throw new RefreshParamException(msg);
        }
    }

    @Override
    public void setAll(SingleDataInfo dataInfo) {
        if (dataInfo instanceof WechatTokenInfo) {
            var newData = (WechatTokenInfo) dataInfo;
            this.appId = newData.appId;
            this.appSecurity = newData.appSecurity;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        WechatTokenInfo that = (WechatTokenInfo) o;

        return new EqualsBuilder()
                .append(appId, that.appId)
                .append(appSecurity, that.appSecurity)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(appId)
                .append(appSecurity)
                .toHashCode();
    }
}
