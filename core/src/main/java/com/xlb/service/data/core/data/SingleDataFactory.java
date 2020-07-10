package com.xlb.service.data.core.data;

import com.xlb.service.data.core.config.ConfigManager;
import com.xlb.service.data.core.config.SingleDataConfig;
import com.xlb.service.data.core.data.wechat.WechatTokenInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.function.Supplier;

/**
 * 单数据信息工厂
 */
public class SingleDataFactory {

    @Getter
    @Setter
    private ConfigManager configManager;

    /**
     * 获取数据信息
     *
     * @param name 配置名
     */
    public SingleDataInfo getSingleData(String name) {
        SingleDataConfig config = configManager.getConfig(name);
        if (config == null) {
            return null;
        }
        SingleDataInfo dataInfo = Type.valueOf(config.getType()).getDataInfo();
        if (config.getConfig() != null) {
            dataInfo.init(config.getConfig());
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
