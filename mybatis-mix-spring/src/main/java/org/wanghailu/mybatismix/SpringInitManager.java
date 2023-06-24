package org.wanghailu.mybatismix;

import org.wanghailu.mybatismix.common.BaseManager;
import org.wanghailu.mybatismix.common.LogicDeleteTableChecker;
import org.wanghailu.mybatismix.constant.ConfigurationKeyConstant;
import org.wanghailu.mybatismix.mapping.EntityMappedStatementCreator;
import org.wanghailu.mybatismix.mapping.MappedStatementManager;
import org.wanghailu.mybatismix.reflection.FastReflectorFactory;
import org.wanghailu.mybatismix.util.PackageScanner;
import org.wanghailu.mybatismix.util.PrivateStringUtils;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Set;


/**
 * spring相关内容，通过该类初始化
 *
 * @author cdhuang
 * @date 2023/4/13
 */
public class SpringInitManager extends BaseManager {
    
    @Override
    public void setConfiguration(MybatisMixConfiguration configuration) {
        super.setConfiguration(configuration);
        configuration.setReflectorFactory(new FastReflectorFactory());
    }

    @Override
    public void initAfterMybatisInit() {
        initEntityMappedStatement();
        super.initAfterMybatisInit();
    }

    protected void initEntityMappedStatement(){
        EntityMappedStatementCreator entityMappedStatementCreator = configuration
                .getManager(MappedStatementManager.class).getEntityMappedStatementCreator();
        String entityPackages = configuration.getProperty(ConfigurationKeyConstant.entityPackages);
        if (PrivateStringUtils.isNotEmpty(entityPackages)) {
            PackageScanner scanner = new PackageScanner(entityPackages, Entity.class, Table.class);
            Set<Class<?>> entityClassSet = scanner.getClassSet();
            entityMappedStatementCreator.init(entityClassSet);
            new LogicDeleteTableChecker(configuration).checkTableExist(entityClassSet);
        }
    }

    @Override
    public int getOrder() {
        return 10000;
    }
}
