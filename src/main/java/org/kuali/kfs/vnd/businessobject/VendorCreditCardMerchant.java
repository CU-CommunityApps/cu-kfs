package org.kuali.kfs.vnd.businessobject;

import java.util.LinkedHashMap;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.krad.util.ObjectUtils;

public class VendorCreditCardMerchant extends PersistableBusinessObjectBase implements VendorRoutingComparable, MutableInactivatable {
	
	private Integer vendorHeaderGeneratedIdentifier;
	private Integer vendorDetailAssignedIdentifier;
	
	private String creditMerchantName;
	private String merchantCategoryCodeOne;
	private String merchantCategoryCodeTwo;
	private String merchantCategoryCodeThree;
	private String merchantCategoryCodeFour;
	
	private boolean active;
		
	private VendorDetail vendorDetail;
	
	public VendorDetail getVendorDetail() {
		return vendorDetail;
	}
	
	public void setVendorDetail(VendorDetail vendorDetail) {
		this.vendorDetail = vendorDetail;
	}
	
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public Integer getVendorHeaderGeneratedIdentifier() {
		return vendorHeaderGeneratedIdentifier;
	}
	public void setVendorHeaderGeneratedIdentifier(
			Integer vendorHeaderGeneratedIdentifier) {
		this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
	}	

	public Integer getVendorDetailAssignedIdentifier() {
		return vendorDetailAssignedIdentifier;
	}
	public void setVendorDetailAssignedIdentifier(
			Integer vendorDetailAssignedIdentifier) {
		this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
	}
	public String getCreditMerchantName() {
		return creditMerchantName;
	}
	public void setCreditMerchantName(String creditMerchantName) {
		this.creditMerchantName = creditMerchantName;
	}
	public String getMerchantCategoryCodeOne() {
		return merchantCategoryCodeOne;
	}
	public void setMerchantCategoryCodeOne(String merchantCategoryCodeOne) {
		this.merchantCategoryCodeOne = merchantCategoryCodeOne;
	}
	public String getMerchantCategoryCodeTwo() {
		return merchantCategoryCodeTwo;
	}
	public void setMerchantCategoryCodeTwo(String merchantCategoryCodeTwo) {
		this.merchantCategoryCodeTwo = merchantCategoryCodeTwo;
	}
	public String getMerchantCategoryCodeThree() {
		return merchantCategoryCodeThree;
	}
	public void setMerchantCategoryCodeThree(String merchantCategoryCodeThree) {
		this.merchantCategoryCodeThree = merchantCategoryCodeThree;
	}
	public String getMerchantCategoryCodeFour() {
		return merchantCategoryCodeFour;
	}
	public void setMerchantCategoryCodeFour(String merchantCategoryCodeFour) {
		this.merchantCategoryCodeFour = merchantCategoryCodeFour;
	}

	protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();

        if (this.vendorHeaderGeneratedIdentifier != null) {
            m.put("vendorHeaderGeneratedIdentifier", this.vendorHeaderGeneratedIdentifier.toString());
        }
        if (this.vendorDetailAssignedIdentifier != null) {
            m.put("vendorDetailAssignedIdentifier", this.vendorDetailAssignedIdentifier.toString());
        }
        return m;
	}

	public boolean isEqualForRouting(Object toCompare) {
		// KFSPTS-2055
        if ((ObjectUtils.isNull(toCompare)) || !(toCompare instanceof VendorCreditCardMerchant)) {

            return false;
        }
        else {
        	VendorCreditCardMerchant vndCardMerchant = (VendorCreditCardMerchant) toCompare;

            return new EqualsBuilder().append(
            		this.getCreditMerchantName(), vndCardMerchant.getCreditMerchantName()).append(
                    this.getMerchantCategoryCodeOne(), vndCardMerchant.getMerchantCategoryCodeOne()).append(
                    this.getMerchantCategoryCodeTwo(), vndCardMerchant.getMerchantCategoryCodeTwo()).append(
                    this.getMerchantCategoryCodeThree(), vndCardMerchant.getMerchantCategoryCodeThree()).append(
                    this.getMerchantCategoryCodeFour(), vndCardMerchant.getMerchantCategoryCodeFour()).append(
            		this.isActive(), vndCardMerchant.isActive()).isEquals();
        }
	}

}
