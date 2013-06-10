package edu.cornell.kfs.vnd.service;

import java.util.ArrayList;

import javax.jws.WebService;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorContact;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.document.VendorMaintainableImpl;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.kns.UserSession;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.service.DocumentService;
import org.kuali.rice.kns.util.GlobalVariables;
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
	public String addVendor(String vendorName, String vendorTypeCode, boolean isForeign, String taxNumber, String taxNumberType, String ownershipTypeCode, boolean isTaxable, boolean isEInvoice,
			                String vendorAddressTypeCode, String vendorLine1Address, String vendorCityName, String vendorStateCode, String vendorPostalCode, String vendorCountryCode) throws Exception {
        UserSession actualUserSession = GlobalVariables.getUserSession();
        MessageMap globalErrorMap = GlobalVariables.getMessageMap();
        
        // create and route doc as system user
        GlobalVariables.setUserSession(new UserSession("kfs"));
        
        try {
        	DocumentService docService = SpringContext.getBean(DocumentService.class);
        	
            MaintenanceDocument vendorDoc = (MaintenanceDocument)docService.getNewDocument("PVEN");
            
            vendorDoc.getDocumentHeader().setDocumentDescription("New vendor from Procurement tool");
            
            if(vendorExists(taxNumber, taxNumberType)) {
            	VendorDetail vendor = retrieveVendor(taxNumber, taxNumberType);
            	VendorMaintainableImpl oldVendorImpl = (VendorMaintainableImpl)vendorDoc.getOldMaintainableObject();
            	oldVendorImpl.setBusinessObject(vendor);
            }
            
        	VendorMaintainableImpl vImpl = (VendorMaintainableImpl)vendorDoc.getNewMaintainableObject();

        	VendorDetail vDetail = (VendorDetail)vImpl.getBusinessObject();
        	
        	vDetail.setVendorName(vendorName);
        	vDetail.setActiveIndicator(true);
        	vDetail.setTaxableIndicator(isTaxable);

        	((VendorDetailExtension)vDetail.getExtension()).setEinvoiceVendorIndicator(isEInvoice);

        	VendorAddress vendorAddr = new VendorAddress();
        	vendorAddr.setVendorAddressTypeCode(vendorAddressTypeCode);
        	vendorAddr.setVendorLine1Address(vendorLine1Address);
        	vendorAddr.setVendorCityName(vendorCityName);
        	vendorAddr.setVendorStateCode(vendorStateCode);
        	vendorAddr.setVendorZipCode(vendorPostalCode);
        	vendorAddr.setVendorCountryCode(vendorCountryCode);
        	vendorAddr.setVendorDefaultAddressIndicator(true);

        	ArrayList<VendorAddress> vAddrs = new ArrayList<VendorAddress>();
        	vAddrs.add(vendorAddr);
        	
        	vDetail.setVendorAddresses(vAddrs);

        	VendorContact vContact = new VendorContact();
        	vContact.setVendorContactTypeCode("VF");
        	
        	ArrayList<VendorContact> vendorContacts = new ArrayList<VendorContact>();
        	vendorContacts.add(vContact);
        	
        	vDetail.setVendorContacts(vendorContacts);

        	vDetail.setDefaultAddressLine1(vendorLine1Address);
        	vDetail.setDefaultAddressCity(vendorCityName);
        	vDetail.setDefaultAddressStateCode(vendorStateCode);
        	vDetail.setDefaultAddressPostalCode(vendorPostalCode);
        	vDetail.setDefaultAddressCountryCode(vendorCountryCode);

        	
        	VendorHeader vHeader = vDetail.getVendorHeader();
        	
        	vHeader.setVendorTypeCode(vendorTypeCode);
        	vHeader.setVendorTaxNumber(taxNumber);
        	vHeader.setVendorTaxTypeCode(taxNumberType);
        	vHeader.setVendorForeignIndicator(isForeign);
        	vHeader.setVendorOwnershipCode(ownershipTypeCode);

        	vDetail.setVendorHeader(vHeader);
        	vImpl.setBusinessObject(vDetail);
        	vendorDoc.setNewMaintainableObject(vImpl);

        	docService.routeDocument(vendorDoc, "", null);
        	
            return vendorDoc.getDocumentNumber();
        } finally {
            GlobalVariables.setUserSession(actualUserSession);
            GlobalVariables.setMessageMap(globalErrorMap);
		}        
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
	 * @param vendorName
	 * @param lastFour
	 * @return
	 * @throws Exception
	 */
	public String retrieveKfsVendorByNamePlusLastFour(String vendorName, String lastFour) throws Exception {
		VendorDetail vendor = SpringContext.getBean(CUVendorService.class).getVendorByNamePlusLastFourOfTaxID(vendorName, lastFour);
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
		CUVendorService vendorService = SpringContext.getBean(CUVendorService.class);
		if(StringUtils.equalsIgnoreCase(vendorIdType, "DUNS")) {
			vendor = vendorService.getVendorByDunsNumber(vendorId);
		} else if(StringUtils.equalsIgnoreCase(vendorIdType, "VENDORID")) {
			vendor = vendorService.getByVendorNumber(vendorId);
		} else if(StringUtils.equalsIgnoreCase(vendorIdType, "VENDORNAME")) {
			vendor = vendorService.getVendorByVendorName(vendorId);
		} else if(StringUtils.equalsIgnoreCase(vendorIdType, "SSN")) {
			vendor = vendorService.getVendorByNamePlusLastFourOfTaxID(vendorId, vendorIdType);
//		} else if(StringUtils.equalsIgnoreCase(vendorIdType, "FEIN")) {
//			// not implemented yet
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
