package com.xlb.service.data.service.service;

import com.xlb.base.mapper.CustomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public abstract class BaseService<M extends CustomMapper<R>, R> {
    @Autowired
    protected M mapper;
}
