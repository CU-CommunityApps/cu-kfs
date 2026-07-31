package edu.cornell.kfs.cemi.vnd.dataaccess.impl;

import java.util.stream.Stream;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorContact;

import edu.cornell.kfs.cemi.sys.dataaccess.impl.CemiOrmDaoOjbImplBase;
import edu.cornell.kfs.cemi.vnd.CemiVendorPropertyConstants;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiEntityContactExtractOrmDao;
import edu.cornell.kfs.sys.util.CuOjbUtils;

public class CemiEntityContactExtractOrmDaoOjbImpl extends CemiOrmDaoOjbImplBase implements CemiEntityContactExtractOrmDao {

    @Override
    public Stream<VendorContact> getVendorContactsForCemiEntityContactExtractAsCloseableStream() {
        final String vendorContactIdCondition;

        if (shouldUseLessDataDuringCemiDevelopment()) {
            // This conditional was added to reduce processing time for local development during CEMI project work.
            vendorContactIdCondition = "(A0.VNDR_CNTCT_GNRTD_ID) IN ("
                    + "SELECT VNDR_CNTCT_GNRTD_ID FROM CEMI.CU_CEMI_EXTR_ENT_CNTCT_VNDR_CNTCT_T"
                    + " WHERE VNDR_CNTCT_GNRTD_ID <= 4000 OR VNDR_CNTCT_GNRTD_ID >= 125000)";
        } else {
            vendorContactIdCondition = "(A0.VNDR_CNTCT_GNRTD_ID) IN ("
                    + "SELECT VNDR_CNTCT_GNRTD_ID FROM CEMI.CU_CEMI_EXTR_ENT_CNTCT_VNDR_CNTCT_T)";
        }
        final Criteria criteria = new Criteria();
        criteria.addSql(vendorContactIdCondition);

        final QueryByCriteria query = new QueryByCriteria(VendorContact.class, criteria);
        query.addOrderByAscending(KFSPropertyConstants.VENDOR_HEADER_GENERATED_ID);
        query.addOrderByAscending(KFSPropertyConstants.VENDOR_DETAIL_ASSIGNED_ID);
        query.addOrderByAscending(CemiVendorPropertyConstants.VENDOR_CONTACT_GENERATED_ID);

        return CuOjbUtils.buildCloseableStreamForQueryResults(
                VendorContact.class,
                () -> getPersistenceBrokerTemplate().getIteratorByQuery(query));
    }

}
