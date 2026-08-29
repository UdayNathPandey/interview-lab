package com.interviewlab.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class H2DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.datasource.h2")
    public DataSource h2DataSource()
    {
        return DataSourceBuilder
                .create()
                .build();
    }
}
