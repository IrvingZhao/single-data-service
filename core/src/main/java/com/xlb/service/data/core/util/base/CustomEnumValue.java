package com.xlb.service.data.core.util.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 自定义枚举序列化值
 */
public interface CustomEnumValue<T extends CustomEnumValue, C> {
    @JsonValue
    C getCode();

    @JsonCreator
    default T getItem(C code) {
        for (T item : this.getValues()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        return null;
    }

    T[] getValues();
}
