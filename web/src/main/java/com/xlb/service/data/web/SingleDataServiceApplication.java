package com.xlb.service.data.web;

import com.xlb.base.mapper.BaseMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableTransactionManagement
@ComponentScan(basePackages = {"com.xlb.service.data", "com.xlb.base.config"})
@MapperScan(basePackages = "com.xlb.service.data.**.mapper", markerInterface = BaseMapper.class)
public class SingleDataServiceApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(SingleDataServiceApplication.class);
    }
}
