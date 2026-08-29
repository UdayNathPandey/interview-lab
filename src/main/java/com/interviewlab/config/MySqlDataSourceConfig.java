package com.interviewlab.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class MySqlDataSourceConfig {
    // datasource -> repository -> entitymanagerfactory -> transaction manager

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource mysqlDataSource()
    {
        return DataSourceBuilder
                .create()
                .build();
    }

}
