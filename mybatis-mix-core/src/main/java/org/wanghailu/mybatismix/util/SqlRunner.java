package org.wanghailu.mybatismix.util;

import org.apache.ibatis.session.Configuration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * 简单运行sql语句
 * @author cdhuang
 * @date 2023/3/15
 */
public class SqlRunner {
    
    public static void executeSelectSql(String sql, Consumer<ResultSet> resultSetConsumer){
        executeSelectSql(MybatisContext.getConfiguration(),sql,resultSetConsumer);
    }
    
    public static void executeSelectSql(Configuration configuration,String sql, Consumer<ResultSet> resultSetConsumer){
        try(Connection connection = configuration.getEnvironment().getDataSource().getConnection()){
            executeSelectSql(connection, sql, resultSetConsumer);
        }catch (SQLException e) {
            ExceptionUtils.throwException(e);
        }
    }
    
    
    public static void executeSelectSql(Connection connection,String sql, Consumer<ResultSet> resultSetConsumer){
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            resultSetConsumer.accept(resultSet);
        } catch (SQLException e) {
            ExceptionUtils.throwException(e);
        }
    }

    public static int executeUpdateSql(String sql, Consumer<ResultSet> resultSetConsumer){
        return executeUpdateSql(MybatisContext.getConfiguration(),sql,resultSetConsumer);
    }

    public static int executeUpdateSql(Configuration configuration,String sql, Consumer<ResultSet> resultSetConsumer){
        try(Connection connection = configuration.getEnvironment().getDataSource().getConnection()){
            return executeUpdateSql(connection, sql, resultSetConsumer);
        }catch (SQLException e) {
            ExceptionUtils.throwException(e);
        }
        return 0;
    }


    public static int executeUpdateSql(Connection connection,String sql, Consumer<ResultSet> resultSetConsumer){
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.execute();
            return preparedStatement.getUpdateCount();
        } catch (SQLException e) {
            ExceptionUtils.throwException(e);
        }
        return 0;
    }
}
