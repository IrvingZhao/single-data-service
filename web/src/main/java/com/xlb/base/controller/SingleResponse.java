package com.xlb.base.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SingleResponse<T> {
    private final T data;
}
