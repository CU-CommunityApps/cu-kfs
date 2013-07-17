package edu.cornell.kfs.vnd.service;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
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
import edu.cornell.kfs.vnd.service.params.VendorAddressParam;


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

	private static final String VENDOR_NOT_FOUND ="Vendor Not Found";
	/**
	 * 
	 */
	// TODO : need to add poTransmissionMethodCode in web service params. 'name' in contact is also required
	public String addVendor(String vendorName, String vendorTypeCode, boolean isForeign, String taxNumber, String taxNumberType, String ownershipTypeCode, boolean isTaxable, boolean isEInvoice,
			                String contactName,  List<VendorAddressParam> addresses) throws Exception {
        UserSession actualUserSession = GlobalVariables.getUserSession();
        MessageMap globalErrorMap = GlobalVariables.getMessageMap();
        
        // create and route doc as system user
        GlobalVariables.setUserSession(new UserSession("kfs"));
        
        try {
        	DocumentService docService = SpringContext.getBean(DocumentService.class);
        	
            MaintenanceDocument vendorDoc = (MaintenanceDocument)docService.getNewDocument("PVEN");
            
            vendorDoc.getDocumentHeader().setDocumentDescription("New vendor from Procurement tool");
                        
        	VendorMaintainableImpl vImpl = (VendorMaintainableImpl)vendorDoc.getNewMaintainableObject();

        	VendorDetail vDetail = (VendorDetail)vImpl.getBusinessObject();
        	
        	vDetail.setVendorName(vendorName);
        	vDetail.setActiveIndicator(true);
        	vDetail.setTaxableIndicator(isTaxable);

        	((VendorDetailExtension)vDetail.getExtension()).setEinvoiceVendorIndicator(isEInvoice);

        	// how should address be handled.  If no addres type code matched, then create, otherwise change ?
        	// how do we know that this is just to change the address type code.  should there is another filed, say 'oldAddressTypeCode' ?
        	ArrayList<VendorAddress> vAddrs = new ArrayList<VendorAddress>();
			for (VendorAddressParam address : addresses) {
				VendorAddress vendorAddr = new VendorAddress();
				vendorAddr.setVendorAddressTypeCode(address.getVendorAddressTypeCode());
				vendorAddr.setVendorLine1Address(address.getVendorLine1Address());
				vendorAddr.setVendorCityName(address.getVendorCityName());
				vendorAddr.setVendorStateCode(address.getVendorStateCode());
				vendorAddr.setVendorZipCode(address.getVendorZipCode());
				vendorAddr.setVendorCountryCode(address.getVendorCountryCode());
				vendorAddr.setVendorDefaultAddressIndicator(true);
				vendorAddr.setVendorDefaultAddressIndicator(address.isVendorDefaultAddressIndicator());
				if (address.isVendorDefaultAddressIndicator()) {
		        	vDetail.setDefaultAddressLine1(address.getVendorLine1Address());
		        	vDetail.setDefaultAddressCity(address.getVendorCityName());
		        	vDetail.setDefaultAddressStateCode(address.getVendorStateCode());
		        	vDetail.setDefaultAddressPostalCode(address.getVendorZipCode());
		        	vDetail.setDefaultAddressCountryCode(address.getVendorCountryCode());
					
				}
				// TODO : need to add poTransmissionMethodCode because it is
				// required if PO type
				vendorAddr.setPurchaseOrderTransmissionMethodCode(address.getPurchaseOrderTransmissionMethodCode());
				vendorAddr.setVendorAddressEmailAddress(address.getVendorAddressEmailAddress());						
				vendorAddr.setVendorFaxNumber(address.getVendorFaxNumber());
				
				vAddrs.add(vendorAddr);
			}        	
        	
        	vDetail.setVendorAddresses(vAddrs);

        	// also, question for contact, are we assume, the contact type is always "VI" ?
        	// should we handle like address ?
        	VendorContact vContact = new VendorContact();
        	// Contact type does not have "VT" which is originally set up
        	vContact.setVendorContactTypeCode("VI");
        	vContact.setVendorContactName(contactName);
        	ArrayList<VendorContact> vendorContacts = new ArrayList<VendorContact>();
        	vendorContacts.add(vContact);
        	
        	vDetail.setVendorContacts(vendorContacts);


        	
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
  
	public String updateVendor(String vendorName, String vendorTypeCode, boolean isForeign, String vendorNumber, 
			String ownershipTypeCode, boolean isTaxable, boolean isEInvoice,
			String oldVendorAddressTypeCode, String vendorAddressTypeCode, String vendorLine1Address,
			String vendorCityName, String vendorStateCode,
			String vendorPostalCode, String vendorCountryCode,
			String contactName, String poTransmissionMethodCode,
			String emailOrFaxNumber) throws Exception {
		UserSession actualUserSession = GlobalVariables.getUserSession();
		MessageMap globalErrorMap = GlobalVariables.getMessageMap();

		// create and route doc as system user
		GlobalVariables.setUserSession(new UserSession("kfs"));

		try {
			DocumentService docService = SpringContext.getBean(DocumentService.class);

			MaintenanceDocument vendorDoc = (MaintenanceDocument) docService.getNewDocument("PVEN");

			vendorDoc.getDocumentHeader().setDocumentDescription("Update vendor from Procurement tool");

				VendorDetail vendor = retrieveVendor(vendorNumber, "VENDORID");
				if (vendor != null) {
					// Vendor does not eist
				VendorMaintainableImpl oldVendorImpl = (VendorMaintainableImpl) vendorDoc.getOldMaintainableObject();
				oldVendorImpl.setBusinessObject(vendor);

				} else {
					// Vendor does not eist
					return "Vendor " + vendorNumber + " Not Found.";
				}
				
			VendorMaintainableImpl vImpl = (VendorMaintainableImpl) vendorDoc.getNewMaintainableObject();

			vImpl.setMaintenanceAction(KFSConstants.MAINTENANCE_EDIT_ACTION);
//			VendorDetail vendorCopy = (VendorDetail)ObjectUtils.deepCopy(vendor);
			vImpl.setBusinessObject((VendorDetail)ObjectUtils.deepCopy(vendor));
			VendorDetail vDetail = (VendorDetail) vImpl.getBusinessObject();

			vDetail.setVendorName(vendorName);
			vDetail.setActiveIndicator(true);
			vDetail.setTaxableIndicator(isTaxable);

			((VendorDetailExtension) vDetail.getExtension()).setEinvoiceVendorIndicator(isEInvoice);

			VendorAddress vendorAddr = new VendorAddress();
			if (StringUtils.isNotBlank(oldVendorAddressTypeCode)) {
				vendorAddr = getVendorAddress(vDetail, oldVendorAddressTypeCode);
			}
			vendorAddr.setVendorAddressTypeCode(vendorAddressTypeCode);
			vendorAddr.setVendorLine1Address(vendorLine1Address);
			vendorAddr.setVendorCityName(vendorCityName);
			vendorAddr.setVendorStateCode(vendorStateCode);
			vendorAddr.setVendorZipCode(vendorPostalCode);
			vendorAddr.setVendorCountryCode(vendorCountryCode);
			vendorAddr.setVendorDefaultAddressIndicator(true);
			// TODO : need to add poTransmissionMethodCode because it is
			// required if PO type
			vendorAddr.setPurchaseOrderTransmissionMethodCode(poTransmissionMethodCode);
			if (StringUtils.equals("PO", vendorAddressTypeCode)) {
				if (StringUtils.equals("EMAL", poTransmissionMethodCode)) {
					vendorAddr.setVendorAddressEmailAddress(emailOrFaxNumber);
				} else if (StringUtils.equals("FAX", poTransmissionMethodCode)) {
					vendorAddr.setVendorFaxNumber(emailOrFaxNumber);
				}

			}

			if (vendorAddr.getVendorHeaderGeneratedIdentifier() == null) {
				vDetail.getVendorAddresses().add(vendorAddr);
			}

//			vDetail.setVendorAddresses(vAddrs);

			VendorContact vContact = getVendorContact(vDetail);
			// Contact type does not have "VT" which is originally set up
			vContact.setVendorContactTypeCode("VI");
			vContact.setVendorContactName(contactName);
			ArrayList<VendorContact> vendorContacts = new ArrayList<VendorContact>();
			vendorContacts.add(vContact);
			if (vContact.getVendorHeaderGeneratedIdentifier() == null) {
				vDetail.getVendorContacts().add(vContact);
			}

			vDetail.setVendorContacts(vendorContacts);

			vDetail.setDefaultAddressLine1(vendorLine1Address);
			vDetail.setDefaultAddressCity(vendorCityName);
			vDetail.setDefaultAddressStateCode(vendorStateCode);
			vDetail.setDefaultAddressPostalCode(vendorPostalCode);
			vDetail.setDefaultAddressCountryCode(vendorCountryCode);

			VendorHeader vHeader = vDetail.getVendorHeader();

			vHeader.setVendorTypeCode(vendorTypeCode);
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
				
		// TODO : this is not quite right because vendor may not be found
		//return vendor.getVendorNumber();
		return vendor != null ? vendor.getVendorNumber() : VENDOR_NOT_FOUND;
	}

	private VendorAddress getVendorAddress(VendorDetail vDetail, String oldVendorAddressTypeCode) {
		for (VendorAddress vAddress : vDetail.getVendorAddresses()) {
			if (StringUtils.equals(oldVendorAddressTypeCode, vAddress.getVendorAddressTypeCode())) {
				return vAddress;
			}
		}
		return new VendorAddress();
	}

	private VendorContact getVendorContact(VendorDetail vDetail) {
		for (VendorContact vContact : vDetail.getVendorContacts()) {
			if (StringUtils.equals("VI", vContact.getVendorContactTypeCode())) {
				return vContact;
			}
		}
		return new VendorContact();
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
		// TODO : this is not quite right because vendor may not be found
		//return vendor.getVendorNumber();
		return vendor != null ? vendor.getVendorNumber() : VENDOR_NOT_FOUND;
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
			String taxID = vendor.getVendorHeader().getVendorTaxNumber();
			String maskedTaxID = "*****"+taxID.substring(taxID.length()-4, taxID.length());
			vendorValues.append(maskedTaxID).append(CARET);
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

	public String retrieveKfsVendorByEin(String vendorEin) throws Exception {
		VendorHeader vendor = SpringContext.getBean(CUVendorService.class).getVendorByEin(vendorEin);
		if (vendor != null) {
			return vendor.getVendorHeaderGeneratedIdentifier() + "-0";
		} 
		return VENDOR_NOT_FOUND;
	}
	
}
