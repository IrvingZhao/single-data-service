package com.xlb.base.mapper;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.IdsMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * 基于 tkMapper 进行扩展，添加分页方法
 */
@Component
public interface CustomMapper<T> extends Mapper<T>, IdsMapper<T>, BaseMapper {

    default PageInfo<T> selectPage(int pageIndex, int pageSize) {
        Page<T> result = PageHelper.startPage(pageIndex, pageSize);
        this.selectAll();
        return result.toPageInfo();
    }

    default PageInfo<T> selectPageByExample(int pageIndex, int pageSize, Object example) {
        Page<T> result = PageHelper.startPage(pageIndex, pageSize);
        this.selectByExample(example);
        return result.toPageInfo();
    }

}
