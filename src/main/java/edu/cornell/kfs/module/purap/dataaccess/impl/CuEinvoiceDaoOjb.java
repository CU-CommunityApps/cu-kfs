package edu.cornell.kfs.module.purap.dataaccess.impl;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.dataaccess.CuEinvoiceDao;
import edu.cornell.kfs.vnd.businessobject.options.EinvoiceIndicatorValuesFinder;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.accesslayer.LookupException;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.Query;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.springframework.dao.DataAccessResourceFailureException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CuEinvoiceDaoOjb extends PlatformAwareDaoBaseOjb implements CuEinvoiceDao {
    private static final Logger LOG = LogManager.getLogger(CuEinvoiceDaoOjb.class);
    
    @Override
    public List<VendorDetail> getVendors(List<String> vendorNumbers) {
        HashMap<Integer, List<Integer>> vendorDetailToHeaderMap = buildVendorDetailMap(vendorNumbers);
        Criteria criteria = buildQueryCriteria(vendorDetailToHeaderMap);
        Query query = QueryFactory.newQuery(VendorDetail.class, criteria);
        return (List<VendorDetail>) getPersistenceBrokerTemplate().getCollectionByQuery(query);
    }
    
    @Override
    public List<String> getFilteredVendorNumbers(String searchCritiera) {
        List<String> vendorNumbers = new ArrayList<>();
        ResultSet results = queryVendors(searchCritiera);
        
        try {
            while(results.next()) {
                String vendorNumber = results.getString(1) + KFSConstants.DASH + results.getString(2);
                if (!vendorNumbers.contains(vendorNumber)) {
                    vendorNumbers.add(vendorNumber);
                }
            }
        } catch (SQLException e) {
            LOG.error("getFilteredVendorNumbers, had an error processing the query results.", e);
            throw new RuntimeException(e);
        }
        
        return vendorNumbers;
    }

    protected ResultSet queryVendors(String searchCritiera) {
        ResultSet queryResults;
        try {
            Connection connection = getPersistenceBroker(true).serviceConnectionManager().getConnection();
            PreparedStatement query = connection.prepareCall(buildSQl());
            String formattedSearchCriteria = KFSConstants.PERCENTAGE_SIGN +  StringUtils.upperCase(searchCritiera).trim() + KFSConstants.PERCENTAGE_SIGN;
            query.setString(1, formattedSearchCriteria);
            query.setString(2, formattedSearchCriteria);
            queryResults = query.executeQuery();
        } catch (DataAccessResourceFailureException | LookupException | IllegalStateException | SQLException e) {
            LOG.error("queryVendors, there was an error querying the database: ", e);
            throw new RuntimeException(e);
        }
        return queryResults;
    }
    
    protected String buildSQl() {
        StringBuilder sb = new StringBuilder("SELECT DETAIL.VNDR_HDR_GNRTD_ID, DETAIL.VNDR_DTL_ASND_ID ");
        sb.append("FROM KFS.PUR_VNDR_DTL_T DETAIL, KFS.PUR_VNDR_DTL_TX EXTENSION ");
        sb.append("WHERE DETAIL.VNDR_HDR_GNRTD_ID = EXTENSION.VNDR_HDR_GNRTD_ID ");
        sb.append("AND DETAIL.VNDR_DTL_ASND_ID = EXTENSION.VNDR_DTL_ASND_ID ");
        sb.append("AND EXTENSION.EINV_IND = '").append(EinvoiceIndicatorValuesFinder.EinvoiceIndicator.WEB.code).append("' ");
        sb.append("AND (UPPER(DETAIL.VNDR_NM) LIKE ? ");
        sb.append("OR UPPER(DETAIL.VNDR_DUNS_NBR) LIKE ?)");
        String sql = sb.toString();
        LOG.debug("buildSQl, generated SQL: " + sql);
        return sql;
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
