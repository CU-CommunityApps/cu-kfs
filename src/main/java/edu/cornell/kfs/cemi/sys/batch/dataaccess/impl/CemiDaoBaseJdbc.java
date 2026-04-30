package edu.cornell.kfs.cemi.sys.batch.dataaccess.impl;

import java.util.stream.Stream;

import org.springframework.jdbc.core.RowMapper;

import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiDaoBaseJdbc extends CuSqlQueryPlatformAwareDaoBaseJdbc {

    protected <T> Stream<T> queryForStream(final CuSqlQuery query, final RowMapper<T> rowMapper) {
        return queryForStream(query, rowMapper, true);
    }

    protected <T> Stream<T> queryForStream(final CuSqlQuery query, final RowMapper<T> rowMapper,
            final boolean logSqlOnError) {
        return runQuery(query, logSqlOnError,
                () -> getJdbcTemplate().queryForStream(query.getQueryString(), rowMapper, query.getParametersArray()));
    }

}
