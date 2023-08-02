package org.wanghailu.mybatismix.page.countsql;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlOrderingExpr;
import com.alibaba.druid.sql.parser.SQLParserUtils;

public class CountSqlByDruid {

    private static SQLSelectItem countItem;

    static {
        SQLIdentifierExpr expr = new SQLIdentifierExpr("count(1)");
        countItem = new SQLSelectItem(expr, "countNum");
    }

    public static String getCountSql(String baseSql, String dbType) {
        SQLStatement statement = SQLParserUtils.createSQLStatementParser(baseSql, dbType).parseStatementList().get(0);
        if (statement instanceof SQLSelectStatement) {
            SQLSelectStatement selectStatement = (SQLSelectStatement) statement;
            SQLSelect select = selectStatement.getSelect();
            String countSql = null;
            SQLSelectQuery query = select.getQuery();
            if (query instanceof SQLSelectQueryBlock) {
                SQLSelectQueryBlock sqlSelectQuery = (SQLSelectQueryBlock) query;
                if (sqlSelectQuery.getDistionOption() == 0) {
                    if (sqlSelectQuery.getGroupBy() != null) {
                        select.setOrderBy(null);
                        replaceSelectItemToGroupItem(sqlSelectQuery);
                        removeOrderBy(sqlSelectQuery);
                        countSql = getSimpleCountSql(statement.toString());
                    } else {
                        select.setOrderBy(null);
                        replaceSelectItemToCountItem(sqlSelectQuery);
                        removeOrderBy(sqlSelectQuery);
                        countSql = statement.toString();
                    }
                }
            }
            return countSql;
        } else {
            throw new IllegalArgumentException("must be select sql :" + baseSql);
        }
    }

    private static String getSimpleCountSql(String baseSql) {
        return "select count(1) as countNum from (" + baseSql + ") countSql";
    }

    private static void replaceSelectItemToGroupItem(SQLSelectQueryBlock sqlSelectQuery) {
        SQLSelectGroupByClause sqlSelectGroupByClause = sqlSelectQuery.getGroupBy();
        if (sqlSelectGroupByClause != null) {
            sqlSelectQuery.getSelectList().clear();
            for (SQLExpr item : sqlSelectGroupByClause.getItems()) {
                if(item instanceof MySqlOrderingExpr){
                    item = ((MySqlOrderingExpr) item).getExpr();
                }
                SQLSelectItem sqlSelectItem = new SQLSelectItem(item);
                sqlSelectQuery.getSelectList().add(sqlSelectItem);
            }
        }
    }

    private static void replaceSelectItemToCountItem(SQLSelectQueryBlock sqlSelectQuery) {
        sqlSelectQuery.getSelectList().clear();
        sqlSelectQuery.getSelectList().add(countItem);
    }

    private static void removeOrderBy(SQLSelectQueryBlock sqlSelectQuery) {
        if (sqlSelectQuery.getOrderBy() != null) {
            sqlSelectQuery.setOrderBy(null);
        }
        removeOrderBy(sqlSelectQuery.getFrom());
    }

    private static void removeOrderBy(SQLTableSource sqlTableSource) {
        if (sqlTableSource instanceof SQLSubqueryTableSource) {
            SQLSubqueryTableSource subqueryTableSource = (SQLSubqueryTableSource) sqlTableSource;
            SQLSelect select = subqueryTableSource.getSelect();
            SQLSelectQuery sqlSelectQuery2 = select.getQuery();
            if (sqlSelectQuery2 instanceof SQLSelectQueryBlock) {
                removeOrderBy((SQLSelectQueryBlock) sqlSelectQuery2);
            }
        } else if (sqlTableSource instanceof SQLJoinTableSource) {
            SQLJoinTableSource sqlJoinTableSource = (SQLJoinTableSource) sqlTableSource;
            removeOrderBy(sqlJoinTableSource.getLeft());
            removeOrderBy(sqlJoinTableSource.getRight());
        }
    }

}
