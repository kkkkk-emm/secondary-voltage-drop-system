package com.straykun.svd.svdsys;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 系统启动类。
 */
@SpringBootApplication
@MapperScan("com.straykun.svd.svdsys.mapper")
public class SvdSysApplication {

    /**
     * 应用程序启动入口。
     *
     * @param args 参数 args。
     */
    public static void main(String[] args) {
        SpringApplication.run(SvdSysApplication.class, args);
    }

}
