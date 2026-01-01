package com.example.demo.commom;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus配置类
 * 核心用途：配置MyBatis Plus分页插件，支持Oracle数据库分页查询
 */
@Configuration
@MapperScan("com.example.demo.mapper")
public class MybatisPlusConfig {

    /**
     * 配置MyBatis Plus拦截器（分页插件）
     * 核心逻辑：添加Oracle数据库的分页拦截器，支持分页查询
     * @return MybatisPlusInterceptor 配置好的拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.ORACLE));
        return interceptor;
    }

}
