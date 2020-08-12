package com.xlb.service.data.test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogTest {
    public static void main(String[] args) {
        log.error("error");
        log.info("info");
        log.debug("debug");
        log.warn("warn");
        log.trace("trace");
    }
}
