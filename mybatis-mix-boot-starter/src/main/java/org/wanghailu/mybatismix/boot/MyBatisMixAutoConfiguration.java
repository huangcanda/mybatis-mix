package org.wanghailu.mybatismix.boot;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wanghailu.mybatismix.MybatisMixConfiguration;
import org.wanghailu.mybatismix.boot.properties.MybatisMixProperties;
import org.wanghailu.mybatismix.mapper.MapperManager;

/**
 * @author cdhuang
 * @date 2023/1/6
 */
@Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties({MybatisMixProperties.class})
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
public class MyBatisMixAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MybatisMixConfiguration mybatisMixConfiguration(SqlSessionTemplate sqlSessionTemplate) {
        MybatisMixConfiguration mybatisMixConfiguration = (MybatisMixConfiguration) sqlSessionTemplate
                .getConfiguration();
        return mybatisMixConfiguration;
    }

    @Bean
    @ConditionalOnMissingBean
    public MapperManager mybatisSpringAutoMapper(SqlSessionTemplate sqlSessionTemplate) {
        MybatisMixConfiguration mybatisMixConfiguration = (MybatisMixConfiguration) sqlSessionTemplate
                .getConfiguration();
        MapperManager mapperManager = mybatisMixConfiguration.getManager(MapperManager.class);
        return mapperManager;
    }
}
