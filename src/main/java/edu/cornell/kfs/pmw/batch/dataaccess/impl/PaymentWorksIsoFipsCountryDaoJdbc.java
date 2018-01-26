package edu.cornell.kfs.pmw.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.rice.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.springframework.jdbc.core.RowMapper;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksIsoFipsCountryItem;
import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksIsoFipsCountryDao;

public class PaymentWorksIsoFipsCountryDaoJdbc extends PlatformAwareDaoBaseJdbc implements PaymentWorksIsoFipsCountryDao {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksIsoFipsCountryDaoJdbc.class);
    private static final String ISO_CNTRY_CD_COL = "iso_cntry_cd";
    private static final String ISO_CNTRY_NM_COL = "iso_cntry_nm";
    private static final String FIPS_CNTRY_CD_COL = "fips_cntry_cd";
    private static final String FIPS_CNTRY_NM_COL = "fips_cntry_nm";

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
            return this.getJdbcTemplate().query(buildIsoToFipsCountryTranslationsSql(), mapRow);
        } catch (Exception e) {
            LOG.info("PaymentWorksIsoFipsCountryDaoJdbc Exception: " + e.getMessage());
            return null;
        }
    }
    
    private String buildIsoToFipsCountryTranslationsSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("select iso.iso_cntry_cd, iso.iso_cntry_nm, fips.postal_cntry_cd as fips_cntry_cd, fips.postal_cntry_nm as fips_cntry_nm ");
        sql.append("from kfs.cu_pmw_iso_cntry_t iso, kfs.cu_pmw_iso_fips_cntry_map_t map_table, cynergy.krlc_cntry_t fips ");
        sql.append("where iso.actv_ind = 'Y' and iso.iso_cntry_cd = map_table.iso_cntry_cd and fips.postal_cntry_cd = map_table.fips_cntry_cd and fips.actv_ind = 'Y' ");
        sql.append("order by iso.iso_cntry_cd");
        return sql.toString();
    }
    
}
