package com.xlb.service.data.core.data.huawei;

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

@Slf4j
public class HuaweiTokenInfo implements SingleDataInfo {

    private final AtomicInteger refreshTime = new AtomicInteger(0);
    private final HttpClient client = new HttpClient();

    private String clientId;
    private String clientSecret;

    @Getter
    private String data;

    @Override
    public void init(Map<String, String> config) {
        this.clientId = config.get("client_id");
        this.clientSecret = config.get("client_secret");
    }

    @Override
    public int refresh() {
        int nowTime = refreshTime.addAndGet(1);
        if (nowTime > 5) {
            refreshTime.set(0);
            String msg = MessageFormat.format("[{0}] refresh has 5 times timeout", this.clientId);
            log.error(msg);
            return -1;
            // 刷新超过5次后，直接返回，停止定时任务
        }
        HuaweiTokenRequest requestMessage = new HuaweiTokenRequest(this.clientId, this.clientSecret);
        client.sendMessage(requestMessage);
        HuaweiTokenResponse response = requestMessage.getTokenResponse();
        if (response == null) {
            log.error("http request error, [{}]", this.clientId);
            return this.refresh();
        }
        if (response.getNspStatus() == 0) { // 状态为0
            this.data = response.getAccessToken();
            refreshTime.set(0);
            return response.getExpiresIn();
        } else if (response.getNspStatus() == -1) {
            var token = response.getAccessToken();
            if (StringUtils.isNotEmpty(token)) {
                this.data = token;
                refreshTime.set(0);
                return response.getExpiresIn();
            } else {
                log.error("huawei token request cannot find response, [{}]", this.clientId);
                return this.refresh();
            }
        } else {
            String msg = MessageFormat.format("huawei [{0}] token request has exception [{1}]",
                    this.clientId, response.getNspStatus());
            log.error(msg);
            refreshTime.set(0);
            throw new RefreshParamException(msg);
        }
    }

    @Override
    public void setAll(SingleDataInfo dataInfo) {
        if (dataInfo instanceof HuaweiTokenInfo) {
            var newData = (HuaweiTokenInfo) dataInfo;
            this.clientId = newData.clientId;
            this.clientSecret = newData.clientSecret;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(clientId)
                .append(clientSecret)
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        HuaweiTokenInfo that = (HuaweiTokenInfo) o;

        return new EqualsBuilder()
                .append(clientId, that.clientId)
                .append(clientSecret, that.clientSecret)
                .isEquals();
    }
}
