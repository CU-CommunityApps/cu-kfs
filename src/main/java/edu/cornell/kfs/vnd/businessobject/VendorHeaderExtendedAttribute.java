package edu.cornell.kfs.vnd.businessobject;

import java.sql.Date;

import org.kuali.rice.kns.bo.PersistableBusinessObjectExtensionBase;

public class VendorHeaderExtendedAttribute extends PersistableBusinessObjectExtensionBase {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3727211909556425180L;
	
	private Integer vendorHeaderGeneratedIdentifier;
	private Date w9ReceivedDate;

	public VendorHeaderExtendedAttribute () {
		
	}
	
	public Date getW9ReceivedDate() {
		return w9ReceivedDate;
	}

	public void setW9ReceivedDate(Date w9RecievedDate) {
		this.w9ReceivedDate = w9RecievedDate;
	}

	public Integer getVendorHeaderGeneratedIdentifier() {
		return vendorHeaderGeneratedIdentifier;
	}

	public void setVendorHeaderGeneratedIdentifier(
			Integer vendorHeaderGeneratedIdentifier) {
		this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
	}
	
	

}
