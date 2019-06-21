package edu.cornell.kfs.module.purap.dataaccess.impl;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.dataaccess.CuEinvoiceDao;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.Query;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import java.util.List;

public class CuCuEinvoiceDaoOjb extends PlatformAwareDaoBaseOjb implements CuEinvoiceDao {

    public List<VendorDetail> getVendors(List<String> vendorNumbers) {
        Criteria criteria = new Criteria();
        criteria.addColumnIn(CUPurapConstants.COMBINED_VENDOR_PK_SQL, vendorNumbers);
        Query query = QueryFactory.newQuery(VendorDetail.class, criteria);
        return (List<VendorDetail>) getPersistenceBrokerTemplate().getCollectionByQuery(query);
    }

}
