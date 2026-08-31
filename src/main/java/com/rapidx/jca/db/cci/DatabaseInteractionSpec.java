package com.rapidx.jca.db.cci;

import jakarta.resource.cci.InteractionSpec;

/**
 * JCA CCI InteractionSpec defining database operations (CREATE_TABLE, INSERT, FIND_BY_ID, FIND_ALL, UPDATE, DELETE).
 */
public class DatabaseInteractionSpec implements InteractionSpec {
    public static final String OP_CREATE_TABLE = "CREATE_TABLE";
    public static final String OP_INSERT = "INSERT";
    public static final String OP_FIND_BY_ID = "FIND_BY_ID";
    public static final String OP_FIND_ALL = "FIND_ALL";
    public static final String OP_UPDATE = "UPDATE";
    public static final String OP_DELETE = "DELETE";

    private String functionName;
    private String tableName;
    private String sqlQuery;

    public DatabaseInteractionSpec() {
    }

    public DatabaseInteractionSpec(String functionName, String tableName) {
        this.functionName = functionName;
        this.tableName = tableName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    @Override
    public String toString() {
        return "DatabaseInteractionSpec{" +
                "functionName='" + functionName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", sqlQuery='" + sqlQuery + '\'' +
                '}';
    }
}
