package com.xlb.service.data.web.vo.config;

import com.xlb.service.data.core.data.SingleDataFactory;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@ApiModel("配置新增")
public class ConfigAddReq {

    @ApiModelProperty("配置名称，在获取数据时传入，保持唯一")
    private String name;

    @ApiModelProperty("配置类型")
    private SingleDataFactory.Type type;

    @ApiModelProperty("配置参数")
    private Map<String, String> params;
}
