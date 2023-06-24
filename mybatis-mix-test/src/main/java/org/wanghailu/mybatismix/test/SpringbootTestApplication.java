package org.wanghailu.mybatismix.test;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.wanghailu.mybatismix.test.init.DbInitializer;

import javax.sql.DataSource;

/**
 * @author cdhuang
 * @date 2023/1/11
 */
@SpringBootApplication
public class SpringbootTestApplication {
    
    @Bean
    public String dbInitializer(DataSource dataSource) {
        DbInitializer.initTable(dataSource);
        return "";
    }
}
