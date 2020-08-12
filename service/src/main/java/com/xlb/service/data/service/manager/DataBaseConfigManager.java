package com.xlb.service.data.service.manager;

import com.xlb.base.exception.CodeUnCheckException;
import com.xlb.service.data.constant.Constant;
import com.xlb.service.data.core.util.base.ObjectStringSerialUtil;
import com.xlb.service.data.service.entity.DataInfo;
import com.xlb.service.data.service.error.ConfigError;
import com.xlb.service.data.service.service.DataConfigService;
import com.xlb.service.data.service.service.DataInfoService;
import com.xlb.service.data.core.config.ConfigManager;
import com.xlb.service.data.core.config.SingleDataConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Component
public class DataBaseConfigManager implements ConfigManager {
    private final ObjectStringSerialUtil serialUtil = ObjectStringSerialUtil.getSerialUtil();

    private final DataInfoService infoService;
    private final DataConfigService configService;
    private final StringRedisTemplate stringRedisTemplate;

    public DataBaseConfigManager(DataInfoService infoService, DataConfigService configService, StringRedisTemplate stringRedisTemplate) {
        this.infoService = infoService;
        this.configService = configService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public SingleDataConfig getConfig(String name) {
        var configData = stringRedisTemplate.opsForValue().get(Constant.DATA_INFO_PREFIX + name);
        if (StringUtils.isNotBlank(configData)) {
            return serialUtil.parse(configData, SingleDataConfig.class, ObjectStringSerialUtil.SerialType.JSON);
        }
        var dataInfo = infoService.getDataInfoByKeyword(name);
        if (dataInfo != null) {
            var configList = configService.getConfigByDataId(dataInfo.getId());
            var configMap = new HashMap<String, String>();
            configList.parallelStream().forEach((item) -> {
                configMap.put(item.getKeyword(), item.getValue());
            });
            var singleData = new SingleDataConfig(dataInfo.getKeyword(), dataInfo.getType(), configMap);
            stringRedisTemplate.opsForValue().set(Constant.DATA_INFO_PREFIX + name,
                    serialUtil.serial(singleData, ObjectStringSerialUtil.SerialType.JSON));
            return singleData;
        }
        return null;
    }

    @Transactional
    public void saveConfig(SingleDataConfig config) {
        var dataInfo = infoService.getDataInfoByKeyword(config.getName());
        if (dataInfo != null) {
            throw new CodeUnCheckException(ConfigError.CONFIG_KEY_EXPIRE, config.getName());
        }
        saveOrUpdate(config, null);
    }

    public void updateConfig(SingleDataConfig config) {
        var dataInfo = infoService.getDataInfoByKeyword(config.getName());
        if (dataInfo == null) {
            throw new CodeUnCheckException(ConfigError.CONFIG_KEY_NOT_FOUND, config.getName());
        }
        saveOrUpdate(config, dataInfo);
    }

    private void saveOrUpdate(SingleDataConfig config, DataInfo dataInfo) {
        if (dataInfo == null) {
            dataInfo = new DataInfo();
        }
        dataInfo.setKeyword(config.getName());
        dataInfo.setType(config.getType());
        if (dataInfo.getId() == null) {
            infoService.saveDataInfo(dataInfo);
        } else {
            infoService.updateDataInfo(dataInfo);
            configService.cleanOldConfig(dataInfo.getId()); // 清里原有配置
        }
        configService.saveConfig(dataInfo.getId(), config.getConfig());
        stringRedisTemplate.opsForValue().set(Constant.DATA_INFO_PREFIX + config.getName(),
                serialUtil.serial(config, ObjectStringSerialUtil.SerialType.JSON));
    }
}
