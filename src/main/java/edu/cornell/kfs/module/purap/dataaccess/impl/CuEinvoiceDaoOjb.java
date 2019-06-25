package edu.cornell.kfs.module.purap.dataaccess.impl;

import edu.cornell.kfs.module.purap.dataaccess.CuEinvoiceDao;
import org.apache.commons.lang3.StringUtils;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.Query;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import java.util.List;

public class CuEinvoiceDaoOjb extends PlatformAwareDaoBaseOjb implements CuEinvoiceDao {

    public List<VendorDetail> getVendors(List<String> vendorNumbers) {
        Criteria criteria = new Criteria();
        for (String vendorNumber : vendorNumbers) {
            Criteria criteriaVendorPk = new Criteria();
            criteria.addEqualTo("VNDR_HDR_GNRTD_ID", getVendorHeaderId(vendorNumber));
            criteria.addEqualTo("VNDR_DTL_ASND_ID", getVendorDetailId(vendorNumber));
            criteria.addOrCriteria(criteriaVendorPk);
        }

        Query query = QueryFactory.newQuery(VendorDetail.class, criteria);
        return (List<VendorDetail>) getPersistenceBrokerTemplate().getCollectionByQuery(query);
    }

    private String getVendorHeaderId(String vendorNumber) {
        return StringUtils.substringBeforeLast(vendorNumber, KFSConstants.DASH);
    }

    private String getVendorDetailId(String vendorNumber) {
        return StringUtils.substringBeforeLast(vendorNumber, KFSConstants.DASH);
    }

}
