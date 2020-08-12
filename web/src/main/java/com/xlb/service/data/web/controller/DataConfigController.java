package com.xlb.service.data.web.controller;

import com.xlb.base.annotation.SuccessMessage;
import com.xlb.base.controller.ResponseBodyHandleController;
import com.xlb.service.data.core.config.SingleDataConfig;
import com.xlb.service.data.service.manager.DataBaseConfigManager;
import com.xlb.service.data.web.vo.config.ConfigAddReq;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "数据配置接口", tags = "数据配置接口")
@RestController
@RequestMapping("/config")
public class DataConfigController implements ResponseBodyHandleController {

    private final DataBaseConfigManager configManager;

    public DataConfigController(DataBaseConfigManager configManager) {
        this.configManager = configManager;
    }

    @ApiOperation(value = "保存配置", notes = "如果存在name相同的配置，则抛出异常")
    @RequestMapping(value = "", method = RequestMethod.POST)
    @SuccessMessage(msg = "配置新增成功")
    public void addConfig(@RequestBody ConfigAddReq config, BindingResult error) {
        SingleDataConfig configInfo = new SingleDataConfig(config.getName(), config.getType(), config.getParams());
        configManager.saveConfig(configInfo);
    }

    @ApiOperation(value = "更新配置", notes = "根据name更新配置，如果name不存在，则抛出异常")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    @SuccessMessage(msg = "配置更新成功")
    public void updateConfig(@RequestBody ConfigAddReq config, BindingResult error) {
        SingleDataConfig configInfo = new SingleDataConfig(config.getName(), config.getType(), config.getParams());
        configManager.updateConfig(configInfo);
    }
}
