package edu.cornell.kfs.vnd.service;

import java.util.HashMap;
import java.util.Map;

import javax.jws.WebService;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.DistributionOfIncomeAndExpenseDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.bo.entity.KimEntityAffiliation;
import org.kuali.rice.kim.bo.impl.PersonImpl;
import org.kuali.rice.kim.service.PersonService;
import org.kuali.rice.kns.UserSession;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.document.authorization.DocumentAuthorizer;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.service.DocumentHelperService;
import org.kuali.rice.kns.service.DocumentService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.MessageMap;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;
import edu.cornell.kfs.vnd.document.service.CUVendorService;


/**
 *
 * <p>Title: KFSVendorWebServiceImpl</p>
 * <p>Description: Implements the webservice for KFS vendor management</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Cornell University: Kuali Financial Systems</p>
 * @author Dennis Friends
 * @version 1.0
 */
@WebService(endpointInterface = "edu.cornell.kfs.vnd.service.KFSVendorWebService")
public class KFSVendorWebServiceImpl implements KFSVendorWebService {

	/**
	 * 
	 */
	public String addVendor(String vendorType) throws Exception {
        UserSession actualUserSession = GlobalVariables.getUserSession();
        MessageMap globalErrorMap = GlobalVariables.getMessageMap();
        
        try {
        	return "";
        } finally {
            GlobalVariables.setUserSession(actualUserSession);
            GlobalVariables.setMessageMap(globalErrorMap);
		}        
	}
  
	/**
	 * 
	 */
	public boolean updateVendor(String documentName) throws Exception {

		return true;
	}

	/**
	 * Return caret (^) delineated string of Vendor values
	 */
	public String retrieveKfsVendor(String vendorId, String vendorIdType) throws Exception {
		VendorDetail vendor = retrieveVendor(vendorId, vendorIdType);
				
		return buildVendorString(vendor);
	}

	/**
	 * 
	 * @param vendorId
	 * @param vendorIdType - DUNS, VENDORID, SSN, FEIN
	 * @return
	 * @throws Exception
	 */
	public boolean vendorExists(String vendorId, String vendorIdType) throws Exception {
		VendorDetail vendor = retrieveVendor(vendorId, vendorIdType);
		return ObjectUtils.isNotNull(vendor);
	}

	/**
	 * 
	 * @param vendorId
	 * @param vendorIdType
	 * @return
	 * @throws Exception
	 */
	private VendorDetail retrieveVendor(String vendorId, String vendorIdType) throws Exception {
		VendorDetail vendor = null;
		VendorService vendorService = SpringContext.getBean(VendorService.class);
		if(StringUtils.equalsIgnoreCase(vendorIdType, "DUNS")) {
			vendor = vendorService.getVendorByDunsNumber(vendorId);
		} else if(StringUtils.equalsIgnoreCase(vendorIdType, "VENDORID")) {
			vendor = vendorService.getByVendorNumber(vendorId);
		} else if(StringUtils.equalsIgnoreCase(vendorIdType, "VENDORNAME")) {
			vendor = SpringContext.getBean(CUVendorService.class).getVendorByVendorName(vendorId);
		} else if(StringUtils.equalsIgnoreCase(vendorIdType, "SSN")) {
			// not implemented yet
		} else if(StringUtils.equalsIgnoreCase(vendorIdType, "FEIN")) {
			// not implemented yet
		}
		return vendor;
	}
	
	/**
	 * 
	 * @param vendor
	 * @return
	 */
	private String buildVendorString(VendorDetail vendor) {
		StringBuffer vendorValues = new StringBuffer();
		String CARET = "^";

		if(ObjectUtils.isNotNull(vendor)) {
			VendorDetailExtension vdExtension = (VendorDetailExtension)vendor.getExtension();
			
			vendorValues.append(vendor.getVendorNumber()).append(CARET);
			vendorValues.append(vendor.getVendorName()).append(CARET);
			vendorValues.append(vendor.getVendorHeader().getVendorTaxNumber()).append(CARET);
			vendorValues.append(vendor.getVendorHeader().getVendorTaxTypeCode()).append(CARET);
			vendorValues.append(vendor.getDefaultAddressLine1()).append(CARET);
			vendorValues.append(vendor.getDefaultAddressLine2()).append(CARET);
			vendorValues.append(vendor.getDefaultAddressCity()).append(CARET);
			vendorValues.append(vendor.getDefaultAddressStateCode()).append(CARET);
			vendorValues.append(vendor.getDefaultAddressPostalCode()).append(CARET);
			vendorValues.append(vendor.getDefaultAddressCountryCode()).append(CARET);
			vendorValues.append(vendor.getDefaultFaxNumber()).append(CARET);
			vendorValues.append(vdExtension.isEinvoiceVendorIndicator());
		}		
		return vendorValues.toString();
	}
	
	
}
