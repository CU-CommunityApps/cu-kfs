package edu.cornell.kfs.cemi.sys.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import edu.cornell.kfs.cemi.sys.dataaccess.CemiIsoCountryDao;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiIsoCountryDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CemiIsoCountryDao {

    @Override
    public String getIso3CharCountryCode(final String iso2CharCountryCode) {
        final CuSqlQuery query = new CuSqlChunk()
                .append("SELECT ISO_3_CHR_CNTRY_CD FROM CEMI.CU_CEMI_ISO_CNTRY_MAPPING_T ")
                .append("WHERE ISO_2_CHR_CNTRY_CD = ").appendAsParameter(iso2CharCountryCode)
                .toQuery();
        return queryForResults(query, this::getFirstColumnValueFromFirstRowIfPresent);
    }

    @Override
    public String getIso2CharCountryCode(final String iso3CharCountryCode) {
        final CuSqlQuery query = new CuSqlChunk()
                .append("SELECT ISO_2_CHR_CNTRY_CD FROM CEMI.CU_CEMI_ISO_CNTRY_MAPPING_T ")
                .append("WHERE ISO_3_CHR_CNTRY_CD = ").appendAsParameter(iso3CharCountryCode)
                .toQuery();
        return queryForResults(query, this::getFirstColumnValueFromFirstRowIfPresent);
    }

    private String getFirstColumnValueFromFirstRowIfPresent(final ResultSet resultSet) throws SQLException {
        return resultSet.next() ? resultSet.getString(1) : null;
    }

}
