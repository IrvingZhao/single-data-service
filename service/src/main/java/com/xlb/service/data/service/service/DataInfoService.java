package com.xlb.service.data.service.service;

import com.xlb.base.constract.TrueFalseEnum;
import com.xlb.service.data.service.entity.DataInfo;
import com.xlb.service.data.service.mapper.DataInfoMapper;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

@Service
public class DataInfoService extends BaseService<DataInfoMapper, DataInfo> {

    public DataInfo getDataInfoByKeyword(String keyword) {
        var example = new Example(DataInfo.class);
        example.createCriteria().andEqualTo("keyword", keyword)
                .andEqualTo(DataInfo.COLUMN_D_FLAG, TrueFalseEnum.N);
        return mapper.selectOneByExample(example);
    }

    public void updateDataByKeyword(String keyword, String data) {
        var record = new DataInfo();
        record.setData(data);
        var example = new Example(DataInfo.class);
        example.createCriteria().andEqualTo("keyword", keyword);
        mapper.updateByExampleSelective(record, example);
    }

}
