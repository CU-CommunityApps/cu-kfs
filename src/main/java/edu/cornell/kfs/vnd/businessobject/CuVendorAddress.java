package edu.cornell.kfs.vnd.businessobject;

import java.util.HashMap;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.log4j.Logger;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderTransmissionMethod;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.ObjectUtils;

public class CuVendorAddress extends VendorAddress {
    private static Logger LOG = Logger.getLogger(CuVendorAddress.class);

    private String purchaseOrderTransmissionMethodCode;
    private PurchaseOrderTransmissionMethod purchaseOrderTransmissionMethod;

    public boolean isEqualForRouting(Object toCompare) {
        LOG.debug("Entering isEqualForRouting.");
        if ((ObjectUtils.isNull(toCompare)) || !(toCompare instanceof VendorAddress)) {
            LOG.debug("Exiting isEqualForRouting");
            return false;
        }
        else {
            CuVendorAddress va = (CuVendorAddress) toCompare;
            boolean eq = new EqualsBuilder().append(this.getVendorAddressGeneratedIdentifier(), va.getVendorAddressGeneratedIdentifier()).append(this.getVendorHeaderGeneratedIdentifier(), va.getVendorHeaderGeneratedIdentifier()).append(this.getVendorDetailAssignedIdentifier(), va.getVendorDetailAssignedIdentifier()).append(this.getVendorAddressTypeCode(), va.getVendorAddressTypeCode()).append(this.getVendorLine1Address(), va.getVendorLine1Address()).append(this.getVendorLine2Address(), va.getVendorLine2Address()).append(this.getVendorCityName(), va.getVendorCityName()).append(this.getVendorStateCode(), va.getVendorStateCode()).append(this.getVendorZipCode(), va.getVendorZipCode()).append(this.getVendorCountryCode(), va.getVendorCountryCode()).append(this.getVendorAttentionName(), va.getVendorAttentionName()).append(this.getVendorAddressInternationalProvinceName(), va.getVendorAddressInternationalProvinceName()).append(this.getVendorAddressEmailAddress(),
                    va.getVendorAddressEmailAddress()).append(this.getPurchaseOrderTransmissionMethodCode(), va.getPurchaseOrderTransmissionMethodCode()).append(this.getVendorBusinessToBusinessUrlAddress(), va.getVendorBusinessToBusinessUrlAddress()).append(this.getVendorFaxNumber(), va.getVendorFaxNumber()).append(this.isVendorDefaultAddressIndicator(), va.isVendorDefaultAddressIndicator()).
            // KFSPTS-2055
                    append(this.isActive(),va.isActive()).isEquals();
            eq &= SpringContext.getBean(VendorService.class).equalMemberLists(this.getVendorDefaultAddresses(), va.getVendorDefaultAddresses());
            LOG.debug("Exiting isEqualForRouting.");
            return eq;
        }
    }
    
	/**
	 * @return the purchaseOrderTransmissionMethodCode
	 */
	public String getPurchaseOrderTransmissionMethodCode() {
		return purchaseOrderTransmissionMethodCode;
	}

	/**
	 * @param purchaseOrderTransmissionMethodCode the purchaseOrderTransmissionMethodCode to set
	 */
	public void setPurchaseOrderTransmissionMethodCode(String purchaseOrderTransmissionMethodCode) {
		this.purchaseOrderTransmissionMethodCode = purchaseOrderTransmissionMethodCode;
		BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
		HashMap<String,String> keys = new HashMap<String,String>();
		keys.put("purchaseOrderTransmissionMethodCode", purchaseOrderTransmissionMethodCode);
		purchaseOrderTransmissionMethod = (PurchaseOrderTransmissionMethod) bos.findByPrimaryKey(PurchaseOrderTransmissionMethod.class, keys);
	}

	/**
	 * @return the purchaseOrderTransmissionMethod
	 */
	public PurchaseOrderTransmissionMethod getPurchaseOrderTransmissionMethod() {
		return purchaseOrderTransmissionMethod;
	}

	/**
	 * @param purchaseOrderTransmissionMethod the purchaseOrderTransmissionMethod to set
	 */
	public void setPurchaseOrderTransmissionMethod(PurchaseOrderTransmissionMethod purchaseOrderTransmissionMethod) {
		this.purchaseOrderTransmissionMethod = purchaseOrderTransmissionMethod;
	}    

}
