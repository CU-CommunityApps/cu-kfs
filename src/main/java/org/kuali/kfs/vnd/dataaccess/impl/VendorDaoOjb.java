/*
 * Copyright 2007-2009 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.vnd.dataaccess.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.util.collections.RemovalAwareCollection;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorContract;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.dataaccess.VendorDao;
import org.kuali.rice.kns.bo.BusinessObject;
import org.kuali.rice.kns.dao.impl.PlatformAwareDaoBaseOjb;
import org.kuali.rice.kns.lookup.CollectionIncomplete;
import org.kuali.rice.kns.lookup.LookupUtils;
import org.kuali.rice.kns.service.DateTimeService;

/**
 * OJB implementation of VendorDao.
 */
public class VendorDaoOjb extends PlatformAwareDaoBaseOjb implements VendorDao {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(VendorDaoOjb.class);

    private DateTimeService dateTimeService;
    private static final String activeIndicator = "activeIndicator";

    public VendorContract getVendorB2BContract(VendorDetail vendorDetail, String campus) {
        Date now = dateTimeService.getCurrentSqlDate();

        Criteria header = new Criteria();
        Criteria detail = new Criteria();
        Criteria campusCode = new Criteria();
        Criteria beginDate = new Criteria();
       // Criteria endDate = new Criteria();
        Criteria b2b = new Criteria();

        header.addEqualTo("VNDR_HDR_GNRTD_ID", vendorDetail.getVendorHeaderGeneratedIdentifier());
        detail.addEqualTo("VNDR_DTL_ASND_ID", vendorDetail.getVendorDetailAssignedIdentifier());
        campusCode.addEqualTo("VNDR_CMP_CD", campus);
        beginDate.addLessOrEqualThan("VNDR_CONTR_BEG_DT", now);
       //endDate.addGreaterOrEqualThan("VNDR_CONTR_END_DT", now);
        b2b.addEqualTo("VNDR_B2B_IND", "Y");

        header.addAndCriteria(detail);
        header.addAndCriteria(campusCode);
        header.addAndCriteria(beginDate);
        //header.addAndCriteria(endDate);
        header.addAndCriteria(b2b);

        VendorContract contract = (VendorContract) getPersistenceBrokerTemplate().getObjectByQuery(new QueryByCriteria(VendorContract.class, header));
        return contract;
    }        
    
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public List<BusinessObject> getSearchResults(Map<String,String> fieldValues) {
        ArrayList results = new ArrayList();
        HashMap<String, VendorDetail> nonDupResults = new HashMap<String, VendorDetail>();
        RemovalAwareCollection rac = new RemovalAwareCollection();
        
        Criteria header = new Criteria();
        Criteria detail = new Criteria();
        Criteria taxNum = new Criteria();
        Criteria name = new Criteria();
        Criteria alias = new Criteria();
        Criteria active = new Criteria();
        Criteria type = new Criteria();
        Criteria state = new Criteria();
        Criteria commodityCode = new Criteria();
        Criteria supplierDiversity = new Criteria();
            	
        String headerVal = fieldValues.get(VendorPropertyConstants.VENDOR_HEADER_GENERATED_ID);
        String detailVal = fieldValues.get(VendorPropertyConstants.VENDOR_DETAIL_ASSIGNED_ID);
        String taxNumVal = fieldValues.get(VendorPropertyConstants.VENDOR_TAX_NUMBER);
        String activeVal = fieldValues.get(activeIndicator);
        String nameVal = fieldValues.get(VendorPropertyConstants.VENDOR_NAME);
        String typeVal = fieldValues.get(VendorPropertyConstants.VENDOR_TYPE_CODE);
        String stateVal = fieldValues.get(VendorPropertyConstants.VENDOR_ADDRESS + "." + VendorPropertyConstants.VENDOR_ADDRESS_STATE);
        String supplierDiversityVal = fieldValues.get(VendorPropertyConstants.VENDOR_HEADER_PREFIX + VendorPropertyConstants.VENDOR_SUPPLIER_DIVERSITY_CODE);
        String commodityCodeVal = fieldValues.get(VendorPropertyConstants.VENDOR_COMMODITIES_CODE_PURCHASING_COMMODITY_CODE);
        //KFSPTS-1891
        String defaultPaymentMethod = fieldValues.get("extension.defaultB2BPaymentMethodCode");
     
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
            active.addEqualTo("activeIndicator", activeVal);  //VNDR_DTL
            header.addAndCriteria(active);            
        }
        if (StringUtils.isNotBlank(nameVal) && nameVal.contains("*")) {
    		String upperVendorName = getDbPlatform().getUpperCaseFunction() + "( vendorName )";
    		String upperNameVal = nameVal.toUpperCase();
        	name.addLike(upperVendorName, upperNameVal);
        	alias.addLike(getDbPlatform().getUpperCaseFunction() + "( " + VendorPropertyConstants.VENDOR_ALIAS_NAME_FULL_PATH + " )", upperNameVal);
        	alias.addEqualTo(VendorPropertyConstants.VENDOR_ALIAS_ACTIVE, "Y");
        	Criteria t = new Criteria();
        	t.addOrCriteria(name);
        	t.addOrCriteria(alias);
        	header.addAndCriteria(t);
        }
        if (StringUtils.isNotBlank(nameVal) && !nameVal.contains("*")) {
        	name.addEqualTo("vendorName", nameVal);
        	alias.addEqualTo(VendorPropertyConstants.VENDOR_ALIAS_NAME_FULL_PATH, nameVal);
        	alias.addEqualTo(VendorPropertyConstants.VENDOR_ALIAS_ACTIVE, "Y");
        	Criteria t = new Criteria();
        	t.addOrCriteria(name);
        	t.addOrCriteria(alias);
        	header.addAndCriteria(t);
        }
        if (StringUtils.isNotBlank(typeVal)) {
        	type.addEqualTo("vendorHeader.vendorTypeCode", typeVal); //VNDR_HDR
            header.addAndCriteria(type);
        }
        if (StringUtils.isNotBlank(stateVal)) {
        	state.addEqualTo("vendorAddresses.vendorStateCode", stateVal); //THIS COMES OUT OF PUR_VND_ADDR_T
            header.addAndCriteria(state);
        }
        if (StringUtils.isNotBlank(commodityCodeVal)) {
        	commodityCode.addEqualTo("vendorCommodities.purchasingCommodityCode", commodityCodeVal);  //THIS COMES OUT OF PUR_VNDR_COMM_T
            header.addAndCriteria(commodityCode); 
        }
        if (StringUtils.isNotBlank(supplierDiversityVal)) {
        	supplierDiversity.addEqualTo("vendorHeader.vendorSupplierDiversities.vendorSupplierDiversityCode", supplierDiversityVal); //THIS COMES OUT OF PUR_VNDR_SUPP_DVRST_T
            header.addAndCriteria(supplierDiversity);        
        }  
        //KFSPTS-1891
        if (StringUtils.isNotBlank(defaultPaymentMethod)) {
        	header.addEqualTo("extension.defaultB2BPaymentMethodCode", defaultPaymentMethod);  //
        }

        Long val = new Long( getPersistenceBrokerTemplate().getCount(QueryFactory.newQuery(VendorDetail.class, header)));
		Integer searchResultsLimit = LookupUtils.getSearchResultsLimit(VendorDetail.class);
		if (val.intValue() > searchResultsLimit.intValue()) {
			LookupUtils.applySearchResultsLimit(VendorDetail.class, header, getDbPlatform());
		}
        QueryByCriteria qbc = new QueryByCriteria(VendorDetail.class, header);
		rac = (RemovalAwareCollection) getPersistenceBrokerTemplate().getCollectionByQuery( qbc );
        
        Criteria children = new Criteria();
        
        Iterator it = rac.iterator();
        while (it.hasNext()) {
        	VendorDetail vd = (VendorDetail) it.next();
        	String key = vd.getVendorNumber();
        	if (! nonDupResults.containsKey(key)) {
       			Criteria c = new Criteria();
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
	        	VendorDetail vd = (VendorDetail) it.next();
	        	String key = vd.getVendorNumber();
	        	if (! nonDupResults.containsKey(key)) {
	        		nonDupResults.put(key, vd);
	        		results.add(vd);
	        	}
	        }
        }
        CollectionIncomplete resultsTruncated = new CollectionIncomplete((Collection) results, new Long(results.size()));
		if (val.intValue() > searchResultsLimit.intValue()) {
        	resultsTruncated.setActualSizeIfTruncated(new Long(-1));
        }
        return resultsTruncated;
    }
    
    
}
