package edu.cornell.kfs.vnd.businessobject;

import java.util.HashMap;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderTransmissionMethod;
import org.kuali.kfs.sys.context.SpringContext;

public class CuVendorAddressExtension extends PersistableBusinessObjectBase
        implements PersistableBusinessObjectExtension {

    private Integer vendorAddressGeneratedIdentifier;
    private String purchaseOrderTransmissionMethodCode;
    private PurchaseOrderTransmissionMethod purchaseOrderTransmissionMethod;

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

	public Integer getVendorAddressGeneratedIdentifier() {
		return vendorAddressGeneratedIdentifier;
	}

	public void setVendorAddressGeneratedIdentifier(
			Integer vendorAddressGeneratedIdentifier) {
		this.vendorAddressGeneratedIdentifier = vendorAddressGeneratedIdentifier;
	}    


}
