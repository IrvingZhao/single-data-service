package com.xlb.service.data.web.controller;

import com.xlb.base.controller.SingleResponse;
import com.xlb.service.data.core.manager.SingleDataManager;
import com.xlb.base.annotation.SuccessMessage;
import com.xlb.base.controller.ResponseBodyHandleController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(value = "数据信息接口", tags = "数据信息接口")
@RestController
@RequestMapping("/info")
public class DataInfoController implements ResponseBodyHandleController {

    @Resource
    private SingleDataManager dataManager;

    @ApiOperation("数据获取")
    @RequestMapping(value = "/{keyword}", method = RequestMethod.GET)
    @SuccessMessage(msg = "获取成功")
    public SingleResponse<String> getDataByKeyword(@PathVariable String keyword) {
        return wrapResponse(dataManager.getData(keyword));
    }


    @ApiOperation("数据获取")
    @RequestMapping(value = "/{keyword}/{old}", method = RequestMethod.GET)
    @SuccessMessage(msg = "获取成功")
    public SingleResponse<String> getDataByKeyword(@PathVariable String keyword,
                                                   @PathVariable String old) {
        String newData = dataManager.getData(keyword);
        if (newData.equals(old)) {
            dataManager.refresh(keyword, old);
            newData = dataManager.getData(keyword);
        }
//        return wapData(newData);
        return wrapResponse(newData);
    }

}
