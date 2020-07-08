package com.xlb.service.data.core.data;

import com.xlb.service.data.core.config.ConfigManager;
import com.xlb.service.data.core.config.SingleDataConfig;
import com.xlb.service.data.core.data.wechat.WechatTokenInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.function.Supplier;

public class SingleDataFactory {

    @Getter
    @Setter
    private ConfigManager configManager;

    public SingleDataInfo getSingleData(String name) {
        SingleDataConfig config = configManager.getConfig(name);
        if (config == null) {
            return null;
        }
        SingleDataInfo dataInfo = Type.valueOf(config.getType()).getDataInfo();
        if (config.getConfig() != null) {
            dataInfo.init(config.getConfig(), config.getData());
        }
        return dataInfo;
    }

    @RequiredArgsConstructor
    public enum Type {
        WECHAT(WechatTokenInfo::new),
        ;
        private final Supplier<SingleDataInfo> dataGetter;

        public SingleDataInfo getDataInfo() {
            return dataGetter.get();
        }
    }

}
