package edu.cornell.kfs.pmw.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.springframework.jdbc.core.RowMapper;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksIsoFipsCountryItem;
import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksIsoFipsCountryDao;

public class PaymentWorksIsoFipsCountryDaoJdbc extends PlatformAwareDaoBaseJdbc implements PaymentWorksIsoFipsCountryDao {

	private static final Logger LOG = LogManager.getLogger(PaymentWorksIsoFipsCountryDaoJdbc.class);
    private static final String ISO_CNTRY_CD_COL = "iso_cntry_cd";
    private static final String ISO_CNTRY_NM_COL = "iso_cntry_nm";
    private static final String FIPS_CNTRY_CD_COL = "fips_cntry_cd";
    private static final String FIPS_CNTRY_NM_COL = "fips_cntry_nm";
    private static final String ISO_COUNTRY_TO_FIPS_COUNTRIES_SQL = "SELECT ISO.ISO_CNTRY_CD, ISO.ISO_CNTRY_NM, FIPS.POSTAL_CNTRY_CD AS FIPS_CNTRY_CD, FIPS.POSTAL_CNTRY_NM AS FIPS_CNTRY_NM FROM KFS.CU_PMW_ISO_CNTRY_T ISO, KFS.CU_PMW_ISO_FIPS_CNTRY_MAP_T MAP_TABLE, KFS.SH_CNTRY_T FIPS WHERE ISO.ACTV_IND = 'Y' AND ISO.ISO_CNTRY_CD = MAP_TABLE.ISO_CNTRY_CD AND FIPS.POSTAL_CNTRY_CD = MAP_TABLE.FIPS_CNTRY_CD AND FIPS.ACTV_IND = 'Y' ORDER BY ISO.ISO_CNTRY_CD";


    @Override
    public Map<String, List<PaymentWorksIsoFipsCountryItem>> buildIsoToFipsMapFromDatabase() {
        Map<String, List<PaymentWorksIsoFipsCountryItem>> isoToFipsMap = new HashMap<String, List<PaymentWorksIsoFipsCountryItem>>();
        List<PaymentWorksIsoFipsCountryItem> isoToFipsValueDatabaseRows = findIsoToFipsCountryTranslations();
        
        if (ObjectUtils.isNotNull(isoToFipsValueDatabaseRows) && !isoToFipsValueDatabaseRows.isEmpty()) {
            ListIterator <PaymentWorksIsoFipsCountryItem> isoToFipsValueDatabaseRowsIterator =  isoToFipsValueDatabaseRows.listIterator();
            String isoKey = isoToFipsValueDatabaseRows.get(0).getIsoCountryCode();
            List<PaymentWorksIsoFipsCountryItem> isoFipsValues = new ArrayList<PaymentWorksIsoFipsCountryItem>();
            
            while (isoToFipsValueDatabaseRowsIterator.hasNext()) {
                PaymentWorksIsoFipsCountryItem currentCountryValue = isoToFipsValueDatabaseRowsIterator.next();
                if (isoKey.equalsIgnoreCase(currentCountryValue.getIsoCountryCode())) {
                    isoFipsValues.add(currentCountryValue);
                }
                else {
                    isoToFipsMap.put(isoKey, isoFipsValues);
                    isoKey = currentCountryValue.getIsoCountryCode();
                    isoFipsValues = new ArrayList<PaymentWorksIsoFipsCountryItem>();
                    isoFipsValues.add(currentCountryValue);
                }
            }
            isoToFipsMap.put(isoKey, isoFipsValues);
        }
        for (String key : isoToFipsMap.keySet()) {
            List<PaymentWorksIsoFipsCountryItem> valuesList = isoToFipsMap.get(key);
            LOG.info("buildIsoToFipsMapFromDatabase: key: " + key);
            for (PaymentWorksIsoFipsCountryItem countryItem : valuesList) {
               LOG.info("buildIsoToFipsMapFromDatabase: value(s): " + countryItem.toString());
            }
        }
        return isoToFipsMap;
    }
    
    private List<PaymentWorksIsoFipsCountryItem> findIsoToFipsCountryTranslations() {
        try {
            RowMapper<PaymentWorksIsoFipsCountryItem> mapRow = new RowMapper<PaymentWorksIsoFipsCountryItem>() {
                public PaymentWorksIsoFipsCountryItem mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
                    PaymentWorksIsoFipsCountryItem isoToFipsCountryItem = 
                            new PaymentWorksIsoFipsCountryItem(resultSet.getString(ISO_CNTRY_CD_COL), resultSet.getString(ISO_CNTRY_NM_COL), 
                                                               resultSet.getString(FIPS_CNTRY_CD_COL), resultSet.getString(FIPS_CNTRY_NM_COL));
                    return isoToFipsCountryItem;
                }
            };
            return this.getJdbcTemplate().query(ISO_COUNTRY_TO_FIPS_COUNTRIES_SQL, mapRow);
        } catch (Exception e) {
            LOG.info("PaymentWorksIsoFipsCountryDaoJdbc Exception: " + e.getMessage());
            return null;
        }
    }

}
