package edu.cornell.kfs.cemi.vnd.dataaccess.impl;

import java.util.stream.Stream;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.cemi.sys.CemiBasePropertyConstants;
import edu.cornell.kfs.cemi.vnd.CemiVendorPropertyConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiSupplierOrderFromOrmDao;
import edu.cornell.kfs.sys.util.CuOjbUtils;

public class CemiSupplierOrderFromOrmDaoOjbImpl extends PlatformAwareDaoBaseOjb implements CemiSupplierOrderFromOrmDao {

    @Override
    public Stream<VendorAddress> getKfsVendorAddressesForExtractedSuppliers() {
        final Criteria criteria = new Criteria();
        criteria.addSql("(A0.VNDR_HDR_GNRTD_ID, A0.VNDR_DTL_ASND_ID) IN ("
                + "SELECT VMP.VNDR_HDR_GNRTD_ID, VMP.VNDR_DTL_ASND_ID "
                + "FROM KFS.CU_CEMI_MAPPING_SPLR_VNDR_EXTR_FILE_T VMP "
                + "JOIN KFS.CU_CEMI_SUPP_ORD_FRM_QUERY_SETTINGS_T QST "
                + "ON VMP.EXTR_FILE_RUNDATE = QST.SUPP_EXTR_FILE_RUNDATE)");

        final QueryByCriteria query = new QueryByCriteria(VendorAddress.class, criteria);
        query.addOrderByAscending(KFSPropertyConstants.VENDOR_ADDRESS_GENERATED_ID);

        return CuOjbUtils.buildCloseableStreamForQueryResults(VendorAddress.class,
                () -> getPersistenceBrokerTemplate().getIteratorByQuery(query));
    }

    @Override
    public Stream<CemiSupplierAddressBo> getSupplierAddressesForExtractedSuppliers() {
        final Criteria criteria = new Criteria();
        criteria.addSql("(A0.EXTR_FILE_RUNDATE, A0.SUPPLIER_ID) IN ("
                + "SELECT VMP.EXTR_FILE_RUNDATE, VMP.WKDY_SPLR_ID \"SUPPLIER_ID\" "
                + "FROM KFS.CU_CEMI_MAPPING_SPLR_VNDR_EXTR_FILE_T VMP "
                + "JOIN KFS.CU_CEMI_SUPP_ORD_FRM_QUERY_SETTINGS_T QST "
                + "ON VMP.EXTR_FILE_RUNDATE = QST.SUPP_EXTR_FILE_RUNDATE)");

        final QueryByCriteria query = new QueryByCriteria(CemiSupplierAddressBo.class, criteria);
        query.addOrderByAscending(CemiBasePropertyConstants.ROW_INDEX);

        return CuOjbUtils.buildCloseableStreamForQueryResults(CemiSupplierAddressBo.class,
                () -> getPersistenceBrokerTemplate().getIteratorByQuery(query));
    }

    @Override
    public Stream<CemiSupplierAddressBo> getSupplierAddressesForSupplierOrderFromExtract() {
        final Criteria criteria = new Criteria();
        criteria.addSql("(A0.EXTR_FILE_RUNDATE, A0.ADDRESS_ID) IN ("
                + "SELECT FRM.SUPP_EXTR_FILE_RUNDATE \"EXTR_FILE_RUNDATE\", FRM.SUPP_ADDRESS_ID \"ADDRESS_ID\" "
                + "FROM KFS.CU_CEMI_SUPP_ORD_FRM_ADDR_T FRM "
                + "JOIN KFS.CU_CEMI_SUPP_ORD_FRM_QUERY_SETTINGS_T QST "
                + "ON FRM.SUPP_EXTR_FILE_RUNDATE = QST.SUPP_EXTR_FILE_RUNDATE)");

        final QueryByCriteria query = new QueryByCriteria(CemiSupplierAddressBo.class, criteria);
        query.addOrderByAscending(CemiVendorPropertyConstants.SUPPLIER_ID);
        query.addOrderByAscending(CemiBasePropertyConstants.ROW_INDEX);

        return CuOjbUtils.buildCloseableStreamForQueryResults(CemiSupplierAddressBo.class,
                () -> getPersistenceBrokerTemplate().getIteratorByQuery(query));
    }

}
