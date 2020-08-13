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
import org.kuali.rice.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.springframework.jdbc.core.RowMapper;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksSupplierDiversityMapDatabaseRow;
import edu.cornell.kfs.pmw.batch.dataaccess.KfsSupplierDiversityDao;

import org.kuali.kfs.vnd.businessobject.SupplierDiversity;

public class KfsSupplierDiversityDaoJdbc extends PlatformAwareDaoBaseJdbc implements KfsSupplierDiversityDao {

	private static final Logger LOG = LogManager.getLogger(KfsSupplierDiversityDaoJdbc.class);
    private static final String KFS_SUPP_DVRST_CD_COL = "kfs_supp_dvrst_cd";
    private static final String KFS_SUPP_DVRST_DESC_COL = "kfs_supp_dvrst_desc";
    private static final String PMW_SUPP_DVRST_DESC_COL = "pmw_supp_dvrst_desc";
    private static final String KFS_SUPPLIER_DIVERSITY_SQL = "SELECT DVRST.VNDR_SUPP_DVRST_CD AS KFS_SUPP_DVRST_CD, DVRST.VNDR_SUPP_DVRST_DESC KFS_SUPP_DVRST_DESC, PMW_MAP.PMW_SUPP_DVRST_DESC PMW_SUPP_DVRST_DESC FROM KFS.PUR_SUPP_DVRST_T DVRST, KFS.CU_PMW_SUPP_DVRST_MAP_T PMW_MAP WHERE DVRST.VNDR_SUPP_DVRST_DESC = PMW_MAP.VNDR_SUPP_DVRST_DESC AND DVRST.DOBJ_MAINT_CD_ACTV_IND = 'Y' ORDER BY DVRST.VNDR_SUPP_DVRST_DESC";

    @Override
    public Map<String, SupplierDiversity> buildPmwToKfsSupplierDiversityMap() {
        Map<String, SupplierDiversity> pmwToKfsDiversityMap = new HashMap<String, SupplierDiversity>();
        List<PaymentWorksSupplierDiversityMapDatabaseRow> pmwToKfsDiversityDatabaseRows = findActiveSupplierDiversitiesForMap();
        
        if (ObjectUtils.isNotNull(pmwToKfsDiversityDatabaseRows) && !pmwToKfsDiversityDatabaseRows.isEmpty()) {
            ListIterator <PaymentWorksSupplierDiversityMapDatabaseRow> kfsDiversityValueDatabaseRowsIterator =  pmwToKfsDiversityDatabaseRows.listIterator();
            while (kfsDiversityValueDatabaseRowsIterator.hasNext()) {
                PaymentWorksSupplierDiversityMapDatabaseRow pmwToKfsSupplierDiversityRow = kfsDiversityValueDatabaseRowsIterator.next();
                String mapKey = pmwToKfsSupplierDiversityRow.getPmwSupplierDiversityDescription();
                SupplierDiversity mapValue = new SupplierDiversity();
                mapValue.setVendorSupplierDiversityCode(pmwToKfsSupplierDiversityRow.getKfsSupplierDiversityCode());
                mapValue.setVendorSupplierDiversityDescription(pmwToKfsSupplierDiversityRow.getKfsSupplierDiversityDescription());
                mapValue.setActive(true);
                pmwToKfsDiversityMap.put(mapKey, mapValue);
            }
        }
        for (String key : pmwToKfsDiversityMap.keySet()) {
            SupplierDiversity value = pmwToKfsDiversityMap.get(key);
            LOG.info("buildPmwToKfsSupplierDiversityMap: key: pmwDiversityDescription = " + key + 
                     "  value: kfsDiversityCode = " + value.getVendorSupplierDiversityCode() + 
                     " kfsDiversityDescription = " + value.getVendorSupplierDiversityDescription());
        }
        return pmwToKfsDiversityMap;
    }
    
    private List<PaymentWorksSupplierDiversityMapDatabaseRow> findActiveSupplierDiversitiesForMap() {
        try {
            RowMapper<PaymentWorksSupplierDiversityMapDatabaseRow> mapRow = new RowMapper<PaymentWorksSupplierDiversityMapDatabaseRow>() {
                public PaymentWorksSupplierDiversityMapDatabaseRow mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
                    PaymentWorksSupplierDiversityMapDatabaseRow pmwToKfsSupplierDiversity = new PaymentWorksSupplierDiversityMapDatabaseRow();
                    pmwToKfsSupplierDiversity.setKfsSupplierDiversityCode(resultSet.getString(KFS_SUPP_DVRST_CD_COL));
                    pmwToKfsSupplierDiversity.setKfsSupplierDiversityDescription(resultSet.getString(KFS_SUPP_DVRST_DESC_COL));
                    pmwToKfsSupplierDiversity.setPmwSupplierDiversityDescription(resultSet.getString(PMW_SUPP_DVRST_DESC_COL));
                    return pmwToKfsSupplierDiversity;
                }
            };
            return this.getJdbcTemplate().query(KFS_SUPPLIER_DIVERSITY_SQL, mapRow);
        } catch (Exception e) {
            LOG.info("findActiveSupplierDiversitiesForMap Exception: " + e.getMessage());
            return null;
        }
    }

}
