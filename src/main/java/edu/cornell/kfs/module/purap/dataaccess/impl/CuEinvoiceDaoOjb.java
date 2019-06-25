package edu.cornell.kfs.module.purap.dataaccess.impl;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.dataaccess.CuEinvoiceDao;
import org.apache.commons.lang3.StringUtils;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.Query;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import javax.ws.rs.BadRequestException;
import java.util.List;

public class CuEinvoiceDaoOjb extends PlatformAwareDaoBaseOjb implements CuEinvoiceDao {

    public List<VendorDetail> getVendors(List<String> vendorNumbers) throws BadRequestException {
        Criteria criteria = new Criteria();
        for (String vendorNumber : vendorNumbers) {
            Criteria criteriaVendorPk = new Criteria();
            criteriaVendorPk.addEqualTo(CUPurapConstants.Einvoice.VENDOR_GENERATED_HEADER_ID, getVendorHeaderId(vendorNumber));
            criteriaVendorPk.addEqualTo(CUPurapConstants.Einvoice.VENDOR_DETAIL_ASSIGNED_ID, getVendorDetailId(vendorNumber));
            criteria.addOrCriteria(criteriaVendorPk);
        }

        Query query = QueryFactory.newQuery(VendorDetail.class, criteria);
        return (List<VendorDetail>) getPersistenceBrokerTemplate().getCollectionByQuery(query);
    }

    private Integer getVendorHeaderId(String vendorNumber) throws BadRequestException {
        try {
            return Integer.parseInt(StringUtils.substringBeforeLast(vendorNumber, KFSConstants.DASH));
        } catch (NumberFormatException ex) {
            throw new BadRequestException();
        }
    }

    private Integer getVendorDetailId(String vendorNumber) throws BadRequestException {
        try {
            return Integer.parseInt(StringUtils.substringAfterLast(vendorNumber, KFSConstants.DASH));
        } catch (NumberFormatException ex) {
            throw new BadRequestException();
        }
    }

}
