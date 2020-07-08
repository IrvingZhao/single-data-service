package com.xlb.service.data.core.data;

import java.util.Map;

public interface SingleDataInfo {

    /**
     * 初始化配置
     *
     * @param config 配置信息
     * @param data   数据
     */
    void init(Map<String, String> config, String data);

    /**
     * 获取数据
     */
    String getData();

    /**
     * 刷新数据，外部控制同步机制
     *
     * @return 下次刷新执行间隔，单位秒，-1 不自动执行下次刷新
     */
    int refresh();

    /**
     * 设置全部信息
     */
    void setAll(SingleDataInfo dataInfo);

    boolean equals(Object o);

    int hashCode();

}
