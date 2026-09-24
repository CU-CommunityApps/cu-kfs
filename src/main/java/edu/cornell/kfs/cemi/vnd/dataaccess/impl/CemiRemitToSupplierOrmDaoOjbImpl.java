package edu.cornell.kfs.cemi.vnd.dataaccess.impl;

import java.util.stream.Stream;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.CemiBasePropertyConstants;
import edu.cornell.kfs.cemi.vnd.CemiVendorPropertyConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileAddressesTabRowBo;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiRemitToSupplierOrmDao;
import edu.cornell.kfs.sys.util.CuOjbUtils;

public class CemiRemitToSupplierOrmDaoOjbImpl extends PlatformAwareDaoBaseOjb implements CemiRemitToSupplierOrmDao {

    private ConfigurationService configurationService;

    @Override
    public Stream<CemiSupplierFileAddressesTabRowBo> getAddressesForCemiRemitToSupplierExtractAsCloseableStream() {
        String idCondition = "(A0.EXTR_FILE_RUNDATE, A0.ADDRESS_ID) IN ("
                + "SELECT EXTR_FILE_RUNDATE, SUPP_ADDRESS_ID FROM CEMI.CU_CEMI_EXTR_RMT_TO_SUPP_RMT_TO_ADDR_T)";
        if (shouldUseLessDataDuringCemiDevelopment()) {
            idCondition += " AND (A0.SUPPLIER_ID <= 'SUPP001000' OR A0.SUPPLIER_ID >= 'SUPP015000')";
        }

        final Criteria criteria = new Criteria();
        criteria.addSql(idCondition);

        final QueryByCriteria query = new QueryByCriteria(CemiSupplierFileAddressesTabRowBo.class, criteria);
        query.addOrderByAscending(CemiVendorPropertyConstants.SUPPLIER_ID);
        query.addOrderByAscending(CemiBasePropertyConstants.JOB_RUN_ROW_INDEX);

        return CuOjbUtils.buildCloseableStreamForQueryResults(CemiSupplierFileAddressesTabRowBo.class,
                () -> getPersistenceBrokerTemplate().getIteratorByQuery(query));
    }

    private boolean shouldUseLessDataDuringCemiDevelopment() {
        return configurationService.getPropertyValueAsBoolean(
                CemiBaseConstants.CU_CEMI_DEVELOPMENT_USE_SMALLER_DATA_SET_KEY);
    }

    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
