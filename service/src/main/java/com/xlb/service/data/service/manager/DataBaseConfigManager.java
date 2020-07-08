package com.xlb.service.data.service.manager;

import com.xlb.service.data.service.service.DataConfigService;
import com.xlb.service.data.service.service.DataInfoService;
import com.xlb.service.data.core.config.ConfigManager;
import com.xlb.service.data.core.config.SingleDataConfig;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class DataBaseConfigManager implements ConfigManager {

    private final DataInfoService infoService;
    private final DataConfigService configService;

    public DataBaseConfigManager(DataInfoService infoService, DataConfigService configService) {
        this.infoService = infoService;
        this.configService = configService;
    }

    @Override
    public SingleDataConfig getConfig(String name) {
        // TODO 读取逻辑调整
        // 在redis中读取，redis中不存在时，读取数据库
        // 数据保存在redis中，不使用数据库中的值
        var dataInfo = infoService.getDataInfoByKeyword(name);
        if (dataInfo != null) {
            var configList = configService.getConfigByDataId(dataInfo.getId());
            var configMap = new HashMap<String, String>();
            configList.parallelStream().forEach((item) -> {
                configMap.put(item.getKeyword(), item.getValue());
            });
            return new SingleDataConfig(dataInfo.getKeyword(), dataInfo.getType(), configMap, dataInfo.getData());
        }
        return null;
    }
}
