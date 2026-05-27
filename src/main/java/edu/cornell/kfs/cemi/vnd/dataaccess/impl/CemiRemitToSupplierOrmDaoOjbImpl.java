package edu.cornell.kfs.cemi.vnd.dataaccess.impl;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.CemiBasePropertyConstants;
import edu.cornell.kfs.cemi.vnd.CemiVendorPropertyConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiRemitToSupplierOrmDao;
import edu.cornell.kfs.sys.util.CuOjbUtils;

public class CemiRemitToSupplierOrmDaoOjbImpl extends PlatformAwareDaoBaseOjb implements CemiRemitToSupplierOrmDao {

    private static final Pattern WORD_CHARS_PATTERN = Pattern.compile("^\\w+$");

    private ConfigurationService configurationService;

    @Override
    public Stream<CemiSupplierAddressBo> getAddressesForCemiRemitToSupplierExtractAsCloseableStream() {
        String idCondition = "(A0.EXTR_FILE_RUNDATE, A0.ADDRESS_ID) IN ("
                + "SELECT EXTR_FILE_RUNDATE, SUPP_ADDRESS_ID FROM KFS.CU_CEMI_EXTR_RMT_TO_SUPP_RMT_TO_ADDR_T)";
        if (shouldUseLessDataDuringCemiDevelopment()) {
            idCondition += " AND (A0.SUPPLIER_ID <= 'SUPP001000' OR A0.SUPPLIER_ID >= 'SUPP015000')";
        }

        final Criteria criteria = new Criteria();
        criteria.addSql(idCondition);

        final QueryByCriteria query = new QueryByCriteria(CemiSupplierAddressBo.class, criteria);
        query.addOrderByAscending(CemiVendorPropertyConstants.SUPPLIER_ID);
        query.addOrderByAscending(CemiBasePropertyConstants.ROW_INDEX);

        return CuOjbUtils.buildCloseableStreamForQueryResults(CemiSupplierAddressBo.class,
                () -> getPersistenceBrokerTemplate().getIteratorByQuery(query));
    }

    private boolean shouldUseLessDataDuringCemiDevelopment() {
        return configurationService.getPropertyValueAsBoolean(
                CemiBaseConstants.CU_CEMI_DEVELOPMENT_USE_SMALLER_DATA_SET_KEY);
    }
 
    @Override
    public List<VendorAddress> getKfsVendorAddresses(final String supplierId, final String supplierJobRunDate) {
        // OJB plain SQL conditions don't allow for parameterization, so we have to manually clean the supplied values.
        Validate.isTrue(WORD_CHARS_PATTERN.matcher(supplierId).matches(),
                "supplierId must only contain word characters");
        Validate.isTrue(WORD_CHARS_PATTERN.matcher(supplierJobRunDate).matches(),
                "supplierJobRunDate must only contain word characters");

        final String vendorIdCondition = StringUtils.join(
                "(A0.VNDR_HDR_GNRTD_ID, A0.VNDR_DTL_ASND_ID) IN (",
                        "SELECT VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID ",
                        "FROM KFS.CU_CEMI_MAPPING_SPLR_VNDR_EXTR_FILE_T ",
                        "WHERE WKDY_SPLR_ID = '", supplierId, "' ",
                        "AND EXTR_FILE_RUNDATE = '", supplierJobRunDate, "'",
                ")"
        );

        final Criteria criteria = new Criteria();
        criteria.addEqualTo(VendorPropertyConstants.VENDOR_ADDRESS_ACTIVE_INDICATOR, KFSConstants.ACTIVE_INDICATOR);
        criteria.addSql(vendorIdCondition);

        final QueryByCriteria query = new QueryByCriteria(VendorAddress.class, criteria);
        final Collection<?> results = getPersistenceBrokerTemplate().getCollectionByQuery(query);

        return results.stream()
                .map(VendorAddress.class::cast)
                .collect(Collectors.toUnmodifiableList());
    }

    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
