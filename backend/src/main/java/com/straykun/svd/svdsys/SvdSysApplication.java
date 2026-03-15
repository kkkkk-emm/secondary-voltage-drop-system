package com.straykun.svd.svdsys;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.straykun.svd.svdsys.mapper")
public class SvdSysApplication {

    public static void main(String[] args) {
        SpringApplication.run(SvdSysApplication.class, args);
    }

}
