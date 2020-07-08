package com.xlb.base.constract;

import com.xlb.service.data.core.util.base.CustomEnumValue;

/**
 * 是否枚举类
 */
public enum TrueFalseEnum implements CustomEnumValue<TrueFalseEnum, Boolean> {
    Y(Boolean.TRUE), N(Boolean.FALSE);

    TrueFalseEnum(Boolean code) {
        this.code = code;
    }

    private Boolean code;

    @Override
    public Boolean getCode() {
        return code;
    }

    @Override
    public TrueFalseEnum[] getValues() {
        return TrueFalseEnum.values();
    }
}
