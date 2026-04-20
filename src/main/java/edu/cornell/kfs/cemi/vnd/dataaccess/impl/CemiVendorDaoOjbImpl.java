package edu.cornell.kfs.cemi.vnd.dataaccess.impl;

import java.util.stream.Stream;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;

import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiVendorDaoOjb;
import edu.cornell.kfs.sys.util.CuOjbUtils;
import edu.cornell.kfs.vnd.dataaccess.impl.CuVendorDaoOjb;

public class CemiVendorDaoOjbImpl extends CuVendorDaoOjb implements CemiVendorDaoOjb {
    
    @Override
    public Stream<VendorDetail> getVendorsForCemiSupplierExtractAsCloseableStream() {
        final String vendorIdCondition;

        if (shouldUseLessDataDuringCemiDevelopment()) {
            // This conditional was added to reduce processing time for local development during CEMI project work.
            // The values were chosen for the WHERE clause to restrict the result set to roughly 1000 rows as
            // well as provide both old and new vendors that had a variety of attributes for local verification. 
            vendorIdCondition = "(A0.VNDR_HDR_GNRTD_ID, A0.VNDR_DTL_ASND_ID) IN ("
                    + "SELECT VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID FROM KFS.CU_CEMI_SPLR_EXTR_VNDR_T"
                    + " WHERE VNDR_HDR_GNRTD_ID <= 5000 OR VNDR_HDR_GNRTD_ID >= 160000)";
        } else {
            vendorIdCondition = "(A0.VNDR_HDR_GNRTD_ID, A0.VNDR_DTL_ASND_ID) IN ("
                    + "SELECT VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID FROM KFS.CU_CEMI_SPLR_EXTR_VNDR_T)";
        }
        final Criteria criteria = new Criteria();
        criteria.addSql(vendorIdCondition);

        /*
         * NOTE: The sort order below is crucial to simplify processing the Vendors in a streaming manner.
         * When iterating over the Vendors below, a parent Vendor will be immediately followed
         * by its children BEFORE the next parent Vendor is encountered. That way, when the processing code,
         * iterates over the data but needs to populate child Vendor data based on what's in its parent,
         * only a single parent Vendor needs its reference kept short-term.
         */
        final QueryByCriteria query = new QueryByCriteria(VendorDetail.class, criteria);
        query.addOrderByAscending(KFSPropertyConstants.VENDOR_HEADER_GENERATED_ID);
        query.addOrderByDescending(VendorPropertyConstants.VENDOR_PARENT_INDICATOR);
        query.addOrderByAscending(KFSPropertyConstants.VENDOR_DETAIL_ASSIGNED_ID);

        return CuOjbUtils.buildCloseableStreamForQueryResults(
                VendorDetail.class,
                () -> getPersistenceBrokerTemplate().getIteratorByQuery(query));
    }

    // This was added to reduce processing time for local development during CEMI project work.
    private static boolean shouldUseLessDataDuringCemiDevelopment() {
        return getBooleanProperty(CemiBaseConstants.CU_CEMI_DEVELOPMENT_USE_SMALLER_DATA_SET_KEY);
    }

    // This was added to reduce processing time for local development during CEMI project work.
    private static boolean getBooleanProperty(String propertyName) {
        return KRADServiceLocator.getKualiConfigurationService().getPropertyValueAsBoolean(propertyName);
    }
    
}
