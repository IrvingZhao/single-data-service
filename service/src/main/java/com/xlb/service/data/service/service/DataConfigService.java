package com.xlb.service.data.service.service;

import com.xlb.service.data.service.entity.DataConfig;
import com.xlb.service.data.service.mapper.DataConfigMapper;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@Service
public class DataConfigService extends BaseService<DataConfigMapper, DataConfig> {

    public List<DataConfig> getConfigByDataId(int dataId) {
        Example example = new Example(DataConfig.class);
        example.createCriteria().andEqualTo("dataId", dataId);
        return mapper.selectByExample(example);
    }

    public void saveConfig(int dataId, Map<String, String> configs) {
        if (configs == null) {
            return;
        }
        configs.entrySet().parallelStream().map((entity) -> {
            DataConfig result = new DataConfig();
            result.setDataId(dataId);
            result.setKeyword(entity.getKey());
            result.setValue(entity.getValue());
            return result;
        }).forEach(mapper::insertSelective);
//        configs.entrySet().parallelStream().forEachOrdered((entity) -> {
//            DataConfig result = new DataConfig();
//            result.setDataId(dataId);
//            result.setKeyword(entity.getKey());
//            result.setValue(entity.getValue());
//            mapper.insertSelective(result);
////            return result;
//        });
    }

    public void cleanOldConfig(int dataId) {
        var example = new Example(DataConfig.class);
        example.createCriteria().andEqualTo("dataId", dataId);
        mapper.deleteByExample(example);
    }

}
