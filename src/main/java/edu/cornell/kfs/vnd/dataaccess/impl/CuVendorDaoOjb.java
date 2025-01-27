package edu.cornell.kfs.vnd.dataaccess.impl;

import java.sql.Date;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.apache.ojb.broker.util.collections.RemovalAwareCollection;
import org.kuali.kfs.kns.lookup.CollectionIncomplete;
import org.kuali.kfs.kns.lookup.LookupUtils;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorContract;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.dataaccess.impl.VendorDaoOjb;

import edu.cornell.kfs.sys.util.CuOjbUtils;
import edu.cornell.kfs.vnd.CUVendorConstants.VendorOwnershipCodes;
import edu.cornell.kfs.vnd.CUVendorPropertyConstants;
import edu.cornell.kfs.vnd.businessobject.VendorWithTaxId;
import edu.cornell.kfs.vnd.dataaccess.CuVendorDao;

public class CuVendorDaoOjb extends VendorDaoOjb implements CuVendorDao {
    private static final Logger LOG = LogManager.getLogger(CuVendorDaoOjb.class);

    private static final String ACTIVE_INDICATOR = "activeIndicator";
    
    @Override
    public VendorContract getVendorB2BContract(final VendorDetail vendorDetail, final String campus, final Date currentSqlDate) {
        final Criteria header = new Criteria();
        final Criteria detail = new Criteria();
        final Criteria campusCode = new Criteria();
        final Criteria beginDate = new Criteria();
        // Criteria endDate = new Criteria();
        final Criteria b2b = new Criteria();

        header.addEqualTo("VNDR_HDR_GNRTD_ID", vendorDetail.getVendorHeaderGeneratedIdentifier());
        detail.addEqualTo("VNDR_DTL_ASND_ID", vendorDetail.getVendorDetailAssignedIdentifier());
        campusCode.addEqualTo("VNDR_CMP_CD", campus);
        beginDate.addLessOrEqualThan("VNDR_CONTR_BEG_DT", currentSqlDate);
       //endDate.addGreaterOrEqualThan("VNDR_CONTR_END_DT", currentSqlDate);
        b2b.addEqualTo("VNDR_B2B_IND", "Y");

        header.addAndCriteria(detail);
        header.addAndCriteria(campusCode);
        header.addAndCriteria(beginDate);
        //header.addAndCriteria(endDate);
        header.addAndCriteria(b2b);

        return (VendorContract) getPersistenceBrokerTemplate()
                .getObjectByQuery(new QueryByCriteria(VendorContract.class, header));
    }        
    
    public List<BusinessObject> getSearchResults(final Map<String,String> fieldValues) {
        final List results = new ArrayList();
        final Map<String, VendorDetail> nonDupResults = new HashMap<String, VendorDetail>();
        RemovalAwareCollection rac = new RemovalAwareCollection();
        
        final Criteria header = new Criteria();
        final Criteria detail = new Criteria();
        final Criteria taxNum = new Criteria();
        final Criteria name = new Criteria();
        final Criteria alias = new Criteria();
        final Criteria active = new Criteria();
        final Criteria type = new Criteria();
        final Criteria state = new Criteria();
        final Criteria commodityCode = new Criteria();
        final Criteria supplierDiversity = new Criteria();
        final Criteria vendorOwnershipCode = new Criteria();
        final Criteria certificationExpirationDate = new Criteria();
        
        String headerVal = fieldValues.get(VendorPropertyConstants.VENDOR_HEADER_GENERATED_ID);
        String detailVal = fieldValues.get(VendorPropertyConstants.VENDOR_DETAIL_ASSIGNED_ID);
        String taxNumVal = fieldValues.get(VendorPropertyConstants.VENDOR_TAX_NUMBER);
        String activeVal = fieldValues.get(ACTIVE_INDICATOR);
        String nameVal = fieldValues.get(VendorPropertyConstants.VENDOR_NAME);
        String typeVal = fieldValues.get(VendorPropertyConstants.VENDOR_TYPE_CODE);
        String stateVal = fieldValues.get(VendorPropertyConstants.VENDOR_ADDRESS + "." + VendorPropertyConstants.VENDOR_ADDRESS_STATE);
        String supplierDiversityVal = fieldValues.get(CUVendorPropertyConstants.VENDOR_HEADER_PREFIX + CUVendorPropertyConstants.VENDOR_SUPPLIER_DIVERSITY_CODE);
        String vendorOwnershipCodeVal = fieldValues.get(VendorPropertyConstants.VENDOR_OWNERSHIP_CODE);
        String commodityCodeVal = fieldValues.get(VendorPropertyConstants.VENDOR_COMMODITIES_CODE_PURCHASING_COMMODITY_CODE);
        String certificationExpirationDateVal = fieldValues.get(CUVendorPropertyConstants.VENDOR_HEADER_PREFIX  +
                VendorPropertyConstants.VENDOR_SUPPLIER_DIVERSITIES + "." + CUVendorPropertyConstants.CERTIFICATION_EXPRIATION_DATE);
        
        final String defaultPaymentMethodCodeVal = fieldValues.get("defaultPaymentMethodCode");
        
        if (StringUtils.isNotBlank(headerVal)) {
        	header.addEqualTo("vendorHeaderGeneratedIdentifier", headerVal);  //
        }
        if (StringUtils.isNotBlank(detailVal)) {
        	detail.addEqualTo("vendorDetailAssignedIdentifier", detailVal);
        	header.addAndCriteria(detail);
        }
        if (StringUtils.isNotBlank(taxNumVal)) {
        	taxNum.addEqualTo("vendorHeader.vendorTaxNumber", taxNumVal);  //THIS IS ENCRYPTED IN THE DB, COMES OFF HDR TABLE
        	header.addAndCriteria(taxNum);
        }
        if (StringUtils.isNotBlank(activeVal)) {
            active.addEqualTo(ACTIVE_INDICATOR, activeVal);  //VNDR_DTL
            header.addAndCriteria(active);            
        }
        if (StringUtils.isNotBlank(nameVal) && nameVal.contains("*")) {
            final String upperVendorName = getDbPlatform().getUpperCaseFunction() + "( vendorName )";
            final String upperNameVal = nameVal.toUpperCase(Locale.US);
        	name.addLike(upperVendorName, upperNameVal);
        	alias.addLike(getDbPlatform().getUpperCaseFunction() + "( " + VendorPropertyConstants.VENDOR_ALIAS_NAME_FULL_PATH + " )", upperNameVal);
        	alias.addEqualTo(VendorPropertyConstants.VENDOR_ALIAS_ACTIVE, "Y");
        	final Criteria t = new Criteria();
        	t.addOrCriteria(name);
        	t.addOrCriteria(alias);
        	header.addAndCriteria(t);
        }
        if (StringUtils.isNotBlank(nameVal) && !nameVal.contains("*")) {
        	name.addEqualTo(VendorPropertyConstants.VENDOR_NAME, nameVal);
        	alias.addEqualTo(VendorPropertyConstants.VENDOR_ALIAS_NAME_FULL_PATH, nameVal);
        	alias.addEqualTo(VendorPropertyConstants.VENDOR_ALIAS_ACTIVE, "Y");
        	final Criteria t = new Criteria();
        	t.addOrCriteria(name);
        	t.addOrCriteria(alias);
        	header.addAndCriteria(t);
        }
        if (StringUtils.isNotBlank(typeVal)) {
        	type.addEqualTo(VendorPropertyConstants.VENDOR_TYPE_CODE, typeVal); //VNDR_HDR
            header.addAndCriteria(type);
        }
        if (StringUtils.isNotBlank(stateVal)) {
        	state.addEqualTo(CUVendorPropertyConstants.VENDOR_ADDRESS_STATE_CODE, stateVal); //THIS COMES OUT OF PUR_VND_ADDR_T
            header.addAndCriteria(state);
        }
        if (StringUtils.isNotBlank(commodityCodeVal)) {
        	commodityCode.addEqualTo(VendorPropertyConstants.VENDOR_COMMODITIES_CODE_PURCHASING_COMMODITY_CODE, commodityCodeVal);  //THIS COMES OUT OF PUR_VNDR_COMM_T
            header.addAndCriteria(commodityCode); 
        }
        if (StringUtils.isNotBlank(supplierDiversityVal)) {
        	supplierDiversity.addEqualTo(CUVendorPropertyConstants.VENDOR_HEADER_SUPPLIER_DIVERSITY_CODE, supplierDiversityVal); //THIS COMES OUT OF PUR_VNDR_SUPP_DVRST_T
            header.addAndCriteria(supplierDiversity);        
        }     
        if (StringUtils.isNotBlank(vendorOwnershipCodeVal)) {
            vendorOwnershipCode.addEqualTo(VendorPropertyConstants.VENDOR_OWNERSHIP_CODE, vendorOwnershipCodeVal); //VNDR_HDR
            header.addAndCriteria(vendorOwnershipCode);
        }
        if (StringUtils.isNotBlank(certificationExpirationDateVal)) {
            try {
                final Date date = new java.sql.Date(new SimpleDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT, Locale.US).parse(certificationExpirationDateVal).getTime());
                certificationExpirationDate.addGreaterOrEqualThan(CUVendorPropertyConstants.VENDOR_HEADER_PREFIX +
                        VendorPropertyConstants.VENDOR_SUPPLIER_DIVERSITIES + "." + CUVendorPropertyConstants.CERTIFICATION_EXPRIATION_DATE, date);
                header.addAndCriteria(certificationExpirationDate);
            } catch (final ParseException e) {
                LOG.error("unable to parse date");
            }
           
        }
        
        if (StringUtils.isNotBlank(defaultPaymentMethodCodeVal)) {
        	header.addEqualTo("defaultPaymentMethodCode", defaultPaymentMethodCodeVal);
        }
        
        final Long val = new Long( getPersistenceBrokerTemplate().getCount(QueryFactory.newQuery(VendorDetail.class, header)));
        final Integer searchResultsLimit = LookupUtils.getSearchResultsLimit(VendorDetail.class);
		if (val.intValue() > searchResultsLimit.intValue()) {
			LookupUtils.applySearchResultsLimit(VendorDetail.class, header, getDbPlatform());
		}
		QueryByCriteria qbc = new QueryByCriteria(VendorDetail.class, header);
		rac = (RemovalAwareCollection) getPersistenceBrokerTemplate().getCollectionByQuery( qbc );
        
		final Criteria children = new Criteria();
        
        Iterator it = rac.iterator();
        while (it.hasNext()) {
            final VendorDetail vd = (VendorDetail) it.next();
            final String key = vd.getVendorNumber();
        	if (! nonDupResults.containsKey(key)) {
        	    final Criteria c = new Criteria();
       			c.addEqualTo("vendorHeaderGeneratedIdentifier", vd.getVendorHeaderGeneratedIdentifier());
       			children.addOrCriteria(c);
        		nonDupResults.put(key, vd);
        		results.add(vd);
        	}
        }
//		LookupUtils.applySearchResultsLimit(VendorDetail.class, children, getDbPlatform());
//        children.addAndCriteria(resultLimitingCriteria);
        
        //ONLY NEED TO DO THE BELOW IF THE USER HAS ENTERED A VALUE INTO THE NAME FIELD, IN WHICH CASE
        //THE CHILDREN OF ANY MATCHING VENDOR DETAIL OBJECT MUST BE FOUND AND ADDED TO THE RESULTS LIST
        if (nonDupResults.size()>0) {
	        qbc = new QueryByCriteria(VendorDetail.class, children);
	        rac = (RemovalAwareCollection) getPersistenceBrokerTemplate().getCollectionByQuery( qbc );
	        it = rac.iterator();
	        while (it.hasNext()) {
	            final VendorDetail vd = (VendorDetail) it.next();
	            final String key = vd.getVendorNumber();
	        	if (! nonDupResults.containsKey(key)) {
	        		nonDupResults.put(key, vd);
	        		results.add(vd);
	        	}
	        }
        }
        final CollectionIncomplete resultsTruncated = new CollectionIncomplete((Collection) results, new Long(results.size()));
		if (val.intValue() > searchResultsLimit.intValue()) {
        	resultsTruncated.setActualSizeIfTruncated(new Long(-1));
        }
        return resultsTruncated;
    }

    @Override
    public Stream<VendorWithTaxId> getPotentialEmployeeVendorsAsCloseableStream() {
        final Criteria criteria = new Criteria();
        criteria.addEqualTo(VendorPropertyConstants.VENDOR_OWNERSHIP_CODE,
                VendorOwnershipCodes.INDIVIDUAL_OR_SOLE_PROPRIETOR_OR_SMLLC);
        criteria.addEqualTo(VendorPropertyConstants.VENDOR_TAX_TYPE_CODE, VendorConstants.TAX_TYPE_SSN);
        criteria.addNotNull(VendorPropertyConstants.VENDOR_TAX_NUMBER);
        criteria.addEqualTo(VendorPropertyConstants.VENDOR_PARENT_INDICATOR, KRADConstants.YES_INDICATOR_VALUE);
        criteria.addEqualTo(KFSPropertyConstants.ACTIVE_INDICATOR, KRADConstants.YES_INDICATOR_VALUE);

        final ReportQueryByCriteria reportQuery = QueryFactory.newReportQuery(VendorDetail.class, criteria);
        reportQuery.setAttributes(new String[] {
                VendorPropertyConstants.VENDOR_HEADER_GENERATED_ID,
                VendorPropertyConstants.VENDOR_DETAIL_ASSIGNED_ID,
                VendorPropertyConstants.VENDOR_TAX_NUMBER
        });
        reportQuery.setJdbcTypes(new int[] {
                Types.INTEGER, Types.INTEGER, Types.VARCHAR
        });

        return CuOjbUtils.buildCloseableStreamForReportQueryResults(
                () -> getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(reportQuery),
                this::mapToVendorWithTaxId);
    }

    private VendorWithTaxId mapToVendorWithTaxId(final Object[] queryResultRow) {
        final String vendorId = StringUtils.join(
                (Integer) queryResultRow[0], KFSConstants.DASH, (Integer) queryResultRow[1]);
        final VendorWithTaxId vendor = new VendorWithTaxId();
        vendor.setVendorId(vendorId);
        vendor.setVendorTaxNumber((String) queryResultRow[2]);
        return vendor;
    }

}
