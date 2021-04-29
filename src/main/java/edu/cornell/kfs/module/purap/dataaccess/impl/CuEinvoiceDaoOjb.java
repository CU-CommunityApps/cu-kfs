package edu.cornell.kfs.module.purap.dataaccess.impl;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.dataaccess.CuEinvoiceDao;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.Query;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CuEinvoiceDaoOjb extends PlatformAwareDaoBaseOjb implements CuEinvoiceDao {

    private static final Logger LOG = LogManager.getLogger(CuEinvoiceDaoOjb.class);

    public List<VendorDetail> getVendors(List<String> vendorNumbers) {
        HashMap<Integer, List<Integer>> vendorDetailToHeaderMap = buildVendorDetailMap(vendorNumbers);
        Criteria criteria = buildQueryCriteria(vendorDetailToHeaderMap);
        Query query = QueryFactory.newQuery(VendorDetail.class, criteria);
        return (List<VendorDetail>) getPersistenceBrokerTemplate().getCollectionByQuery(query);
    }

    private HashMap<Integer, List<Integer>> buildVendorDetailMap(List<String> vendorNumbers) {
        HashMap<Integer, List<Integer>> vendorDetailToHeaderMap = new HashMap<>();
        vendorNumbers.stream().forEach(vn -> addVendorToMap(vendorDetailToHeaderMap, vn));
        return vendorDetailToHeaderMap;
    }

    private Criteria buildQueryCriteria(HashMap<Integer, List<Integer>> vendorDetailToHeaderMap) {
        Criteria criteria = new Criteria();
        vendorDetailToHeaderMap.entrySet().stream().forEach(m -> criteria.addOrCriteria(getQueryCriteria(m.getKey(), m.getValue())));
        return criteria;
    }

    private Criteria getQueryCriteria(Integer detailId, List<Integer> headerIds) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(CUPurapConstants.Einvoice.VENDOR_DETAIL_ASSIGNED_ID, detailId);
        criteria.addIn(CUPurapConstants.Einvoice.VENDOR_GENERATED_HEADER_ID, headerIds);
        return criteria;
    }

    private void addVendorToMap(HashMap<Integer, List<Integer>> vendorDetailToHeaderMap, String vendorNumber) {
        try {
            Integer vendorDetailAssignedId = getVendorDetailId(vendorNumber);
            Integer vendorHeaderGeneratedId = getVendorHeaderId(vendorNumber);
            List<Integer> vendorHeaderNumbers = vendorDetailToHeaderMap.get(vendorDetailAssignedId);
            if (ObjectUtils.isNull(vendorHeaderNumbers)) {
                vendorHeaderNumbers = new ArrayList<>();
                vendorDetailToHeaderMap.put(vendorDetailAssignedId, vendorHeaderNumbers);
            }
            vendorHeaderNumbers.add(vendorHeaderGeneratedId);
        }
        catch (NumberFormatException ex) {
            LOG.debug("addVendorToMap, invalid vendorNumber " + StringUtils.defaultIfBlank(vendorNumber, "[blank]"));
        }
    }

    private Integer getVendorHeaderId(String vendorNumber) throws NumberFormatException {
        return Integer.parseInt(StringUtils.substringBeforeLast(vendorNumber, KFSConstants.DASH));
    }

    private Integer getVendorDetailId(String vendorNumber) throws NumberFormatException {
        return Integer.parseInt(StringUtils.substringAfterLast(vendorNumber, KFSConstants.DASH));
    }

}
