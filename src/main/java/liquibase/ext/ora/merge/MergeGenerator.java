package liquibase.ext.ora.merge;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.structure.core.Table;


public class MergeGenerator extends AbstractSqlGenerator<MergeStatement> {

    private String[] insertColumnsName;
    private String[] insertColumnsValue;
    private String[] updateList;
    private boolean isNullUpdate;
    private boolean isNullColumnName;
    private boolean isNullColumnValue;

    public Sql[] generateSql(MergeStatement statement, Database database,
                             SqlGeneratorChain sqlGeneratorChain) {

        StringBuilder sql = new StringBuilder();

        sql.append("MERGE INTO ").append(database.escapeTableName(null, statement.getTargetSchemaName(), statement.getTargetTableName()));
        sql.append(" USING ").append(database.escapeTableName(null, statement.getSourceSchemaName(), statement.getSourceTableName()));
        sql.append(" ON (").append(statement.getOnCondition()).append(") ");

        if (!isNullUpdate) {
            sql.append("WHEN MATCHED THEN UPDATE SET ");
            for (String list : updateList) {
                sql.append(list).append(",");
            }
            sql.deleteCharAt(sql.lastIndexOf(","));
            if (statement.getUpdateCondition() != null)
                sql.append(" WHERE (").append(database.escapeObjectName(statement.getUpdateCondition(), Table.class)).append(")");
            if (statement.getDeleteCondition() != null)
                sql.append(" DELETE WHERE (").append(database.escapeObjectName(statement.getDeleteCondition(), Table.class)).append(")");
        }

        if (!isNullColumnValue) {
            sql.append(" WHEN NOT MATCHED THEN INSERT ");
            if (!isNullColumnName) {
                for (String list : insertColumnsName) {
                    sql.append(list).append(",");
                }
                sql.deleteCharAt(sql.lastIndexOf(",")).append(") ");
            }

            sql.append("VALUES(");
            for (String list : insertColumnsValue) {
                sql.append(list).append(",");
            }
            sql.deleteCharAt(sql.lastIndexOf(",")).append(")");
            if (statement.getInsertCondition() != null)
                sql.append("WHERE (").append(database.escapeObjectName(statement.getInsertCondition(), Table.class)).append(")");
        }
        return new Sql[]{new UnparsedSql(sql.toString())};
    }


    public boolean supports(MergeStatement statement, Database database) {

        return database instanceof OracleDatabase;
    }


    public ValidationErrors validate(MergeStatement statement,
                                     Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors valid = new ValidationErrors();
        valid.checkRequiredField("sourceTableName", statement.getSourceTableName());
        valid.checkRequiredField("targetTableName", statement.getTargetTableName());
        valid.checkRequiredField("onCondition", statement.getOnCondition());


        if (statement.getInsertColumnsNameList() != null)
            insertColumnsName = statement.getInsertColumnsNameList().split(",");
        else isNullColumnName = true;

        if (statement.getInsertColumnsValueList() != null)
            insertColumnsValue = statement.getInsertColumnsValueList().split(",");
        else isNullColumnValue = true;
        if (statement.getUpdateList() != null) updateList = statement.getUpdateList().split(",");
        else isNullUpdate = true;

        return valid;
    }


}
