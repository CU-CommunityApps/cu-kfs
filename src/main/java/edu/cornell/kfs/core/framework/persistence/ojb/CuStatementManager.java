package edu.cornell.kfs.core.framework.persistence.ojb;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.accesslayer.StatementManager;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.query.Query;

/**
 * Custom StatementManager subclass that supports param binding for parameterized SQL queries/sub-queries.
 */
public class CuStatementManager extends StatementManager {

    public CuStatementManager(final PersistenceBroker pBroker) {
        super(pBroker);
    }

    @Override
    public int bindStatement(PreparedStatement stmt, Query query, ClassDescriptor cld, int param) throws SQLException {
        int newIndex = param;
        if (query instanceof QueryByPreparedSQL) {
            QueryByPreparedSQL preparedQuery = (QueryByPreparedSQL) query;
            newIndex = bindValues(stmt, preparedQuery.getParameters(), newIndex);
        }
        return super.bindStatement(stmt, query, cld, newIndex);
    }

}
