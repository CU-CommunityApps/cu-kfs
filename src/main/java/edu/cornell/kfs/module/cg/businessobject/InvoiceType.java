package edu.cornell.kfs.module.cg.businessobject;

import java.util.LinkedHashMap;

import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class InvoiceType extends PersistableBusinessObjectBase implements MutableInactivatable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6297347189341686751L;
	private String invoiceTypeCode;
	private String invoiceTypeDescription;
	
    private boolean active;

	
	 /**
	 * @return the invoiceTypeCode
	 */
	public String getInvoiceTypeCode() {
		return invoiceTypeCode;
	}

	/**
	 * @param invoiceTypeCode the invoiceTypeCode to set
	 */
	public void setInvoiceTypeCode(String invoiceTypeCode) {
		this.invoiceTypeCode = invoiceTypeCode;
	}

	/**
	 * @return the invoiceTypeDescription
	 */
	public String getInvoiceTypeDescription() {
		return invoiceTypeDescription;
	}

	/**
	 * @param invoiceTypeDescription the invoiceTypeDescription to set
	 */
	public void setInvoiceTypeDescription(String invoiceTypeDescription) {
		this.invoiceTypeDescription = invoiceTypeDescription;
	}

	/**
     * Gets the active attribute.
     * 
     * @return Returns the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active attribute.
     * 
     * @param active The active to set.
     */
    public void setActive(boolean active) {
        this.active = active;
    }
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
  protected LinkedHashMap toStringMapper() {
		LinkedHashMap m = new LinkedHashMap();
	    m.put("invoiceTypeCode", this.invoiceTypeCode);
	    return m;
	}

}
