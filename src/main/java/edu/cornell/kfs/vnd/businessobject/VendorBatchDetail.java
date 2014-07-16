package edu.cornell.kfs.vnd.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * Non-persistable BO to hold data loaded from vendor batch csv file.
 *
 */
public class VendorBatchDetail {

    private static final String DATA_DELIMITER = ";";
	private static final String COLLECTION_ITEM_DELIMITER = "::";
	private static final String COLLECTION_FIELD_DELIMITER = "\\|";
	private String vendorNumber;
	private String vendorName;
	private String legalFirstName;
	private String legalLastName;
	private String vendorTypeCode;
	private String foreignVendor;
	private String taxNumber;
	private String taxNumberType;
	private String ownershipTypeCode;
	private String defaultB2BPaymentMethodCode;
	private String taxable;
	private String eInvoice;
	private String addresses;
	private String contacts;
	private String phoneNumbers;
	private String supplierDiversities;
	private String insuranceTracking;
	private String notes;
	private String attachmentFiles;
	
	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	public String getVendorTypeCode() {
		return vendorTypeCode;
	}
	public void setVendorTypeCode(String vendorTypeCode) {
		this.vendorTypeCode = vendorTypeCode;
	}
	public String getForeignVendor() {
		return foreignVendor;
	}
	public void setForeignVendor(String foreignVendor) {
		this.foreignVendor = foreignVendor;
	}
	public String getTaxNumber() {
		return taxNumber;
	}
	public void setTaxNumber(String taxNumber) {
		this.taxNumber = taxNumber;
	}
	public String getTaxNumberType() {
		return taxNumberType;
	}
	public void setTaxNumberType(String taxNumberType) {
		this.taxNumberType = taxNumberType;
	}
	public String getOwnershipTypeCode() {
		return ownershipTypeCode;
	}
	public void setOwnershipTypeCode(String ownershipTypeCode) {
		this.ownershipTypeCode = ownershipTypeCode;
	}
	public String getTaxable() {
		return taxable;
	}
	public void setTaxable(String taxable) {
		this.taxable = taxable;
	}
	public String geteInvoice() {
		return eInvoice;
	}
	public void seteInvoice(String eInvoice) {
		this.eInvoice = eInvoice;
	}

	public String getAddresses() {
		return addresses;
	}
	public void setAddresses(String addresses) {
		this.addresses = addresses;
	}
	
	public List<VendorBatchAddress> getVendorAddresses() {
		List<VendorBatchAddress> vendorAddresses = new ArrayList<VendorBatchAddress>();
		String[] addressLines = getAddresses().split(COLLECTION_ITEM_DELIMITER);
		for (String addressLine : addressLines) {
			if (StringUtils.isNotBlank(addressLine)) {
		    	vendorAddresses.add(new VendorBatchAddress(addressLine.split(COLLECTION_FIELD_DELIMITER, -1)));
			}
		}
		return vendorAddresses;
	}

	public String getDefaultB2BPaymentMethodCode() {
		return defaultB2BPaymentMethodCode;
	}
	public void setDefaultB2BPaymentMethodCode(String defaultB2BPaymentMethodCode) {
		this.defaultB2BPaymentMethodCode = defaultB2BPaymentMethodCode;
	}
	public String getVendorNumber() {
		return vendorNumber;
	}
	public void setVendorNumber(String vendorNumber) {
		this.vendorNumber = vendorNumber;
	}

	public String getLogData() {
	    StringBuilder sb = new StringBuilder();
	    if (StringUtils.isNotBlank(getVendorNumber())) {
	        sb.append(getVendorNumber()).append(DATA_DELIMITER);
	    }
	    sb.append(getVendorName()).append(DATA_DELIMITER)
        .append(getLegalFirstName()).append(DATA_DELIMITER)
        .append(getLegalLastName()).append(DATA_DELIMITER)
        .append(getVendorTypeCode()).append(DATA_DELIMITER)
        .append(getOwnershipTypeCode()).append(DATA_DELIMITER)
        .append(getTaxNumberType());
	    return sb.toString();
	}
	public String getContacts() {
		return contacts;
	}
	public void setContacts(String contacts) {
		this.contacts = contacts;
	}
	
	public List<VendorBatchContact> getVendorContacts() {
		List<VendorBatchContact> vendorContacts = new ArrayList<VendorBatchContact>();
		String[] contactLines = getContacts().split(COLLECTION_ITEM_DELIMITER);
		for (String contactLine : contactLines) {
			if (StringUtils.isNotBlank(contactLine)) {
			    vendorContacts.add(new VendorBatchContact(contactLine.split(COLLECTION_FIELD_DELIMITER, -1)));
			}
		}
		return vendorContacts;
	}
	public String getPhoneNumbers() {
		return phoneNumbers;
	}
	public void setPhoneNumbers(String phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}
	public String getSupplierDiversities() {
		return supplierDiversities;
	}
	public void setSupplierDiversities(String supplierDiversities) {
		this.supplierDiversities = supplierDiversities;
	}
	public List<VendorBatchPhoneNumber> getVendorPhoneNumbers() {
		List<VendorBatchPhoneNumber> vendorPhoneNumbers = new ArrayList<VendorBatchPhoneNumber>();
		String[] phoneNumberLines = getPhoneNumbers().split(COLLECTION_ITEM_DELIMITER);
		for (String phoneNumberLine : phoneNumberLines) {
			if (StringUtils.isNotBlank(phoneNumberLine)) {
				vendorPhoneNumbers.add(new VendorBatchPhoneNumber(phoneNumberLine.split(COLLECTION_FIELD_DELIMITER, -1)));
			}
		}
		return vendorPhoneNumbers;
	}

	public List<VendorBatchSupplierDiversity> getVendorSupplierDiversities() {
		List<VendorBatchSupplierDiversity> vendorSupplierDiversities = new ArrayList<VendorBatchSupplierDiversity>();
		String[] supplierDiversityLines = getSupplierDiversities().split(COLLECTION_ITEM_DELIMITER);
		for (String supplierDiversityLine : supplierDiversityLines) {
			if (StringUtils.isNotBlank(supplierDiversityLine)) {
				vendorSupplierDiversities.add(new VendorBatchSupplierDiversity(supplierDiversityLine.split(COLLECTION_FIELD_DELIMITER, -1)));
			}
		}
		return vendorSupplierDiversities;
	}
	public String getAttachmentFiles() {
		return attachmentFiles;
	}
	public void setAttachmentFiles(String attachmentFiles) {
		this.attachmentFiles = attachmentFiles;
	}
	public String getInsuranceTracking() {
		return insuranceTracking;
	}
	public void setInsuranceTracking(String insuranceTracking) {
		this.insuranceTracking = insuranceTracking;
	}

	public VendorBatchInsuranceTracking getVendorInsuranceTracking() {
		if (StringUtils.isNotBlank(insuranceTracking)) {
			return new VendorBatchInsuranceTracking(insuranceTracking.split(COLLECTION_FIELD_DELIMITER, -1));
		}
		return null;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}

	public VendorBatchAdditionalNote getVendorAdditionalNote() {
		if (StringUtils.isNotBlank(notes)) {
			return new VendorBatchAdditionalNote(notes.split(COLLECTION_FIELD_DELIMITER, -1));
		}
		return null;
	}
	public String getLegalFirstName() {
		return legalFirstName;
	}
	public void setLegalFirstName(String legalFirstName) {
		this.legalFirstName = legalFirstName;
	}
	public String getLegalLastName() {
		return legalLastName;
	}
	public void setLegalLastName(String legalLastName) {
		this.legalLastName = legalLastName;
	}

}
