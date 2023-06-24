package org.wanghailu.mybatismix.test.init;

import org.wanghailu.mybatismix.util.ExceptionUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * @author cdhuang
 * @date 2023/3/23
 */
public class DbInitializer {
    
    public static String initTable(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
                Statement stat = connection.createStatement()) {
            stat.execute("create table t_user(id BIGINT primary key, user_name varchar(255)"
                    + ", password varchar(255), address varchar(255), create_User varchar(255)"
                    + ", create_Time timestamp(6), update_User varchar(255), update_Time timestamp(6))");
            stat.execute("create table t_user_deleted(id BIGINT primary key, user_name varchar(255)"
                    + ", password varchar(255), address varchar(255), create_User varchar(255)"
                    + ", create_Time timestamp(6), update_User varchar(255), update_Time timestamp(6))");
            stat.execute("create table t_vip_user(id BIGINT primary key, user_name varchar(255)"
                    + ", password varchar(255),vip_level int ,vip_expiration_time timestamp(6), address varchar(255), create_User varchar(255)"
                    + ", create_Time timestamp(6), update_User varchar(255), update_Time timestamp(6),lock int)");
            stat.execute("create table t_vip_user_deleted(id BIGINT primary key, user_name varchar(255)"
                    + ", password varchar(255),vip_level int ,vip_expiration_time timestamp(6), address varchar(255), create_User varchar(255)"
                    + ", create_Time timestamp(6), update_User varchar(255), update_Time timestamp(6),lock int)");
            for (int index = 1; index < 1000; index++) {
                stat.execute("insert into t_user(id,user_name"
                        + ",password, address) values ("+index+",'admin"+index+"','111111"+index+"','望海路" + index + "号楼')");
            }
            
        } catch (Exception e) {
            ExceptionUtils.throwException(e);
        }
        return "";
    }
}
