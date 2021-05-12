package edu.cornell.kfs.module.cg.businessobject;

import java.util.LinkedHashMap;

import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class InvoiceFrequency extends PersistableBusinessObjectBase implements MutableInactivatable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1732078760215418467L;
	private String invoiceFrequencyCode;
	private String invoiceFrequencyDescription;
	
    private boolean active;

	
	 /**
	 * @return the invoiceFrequencyCode
	 */
	public String getInvoiceFrequencyCode() {
		return invoiceFrequencyCode;
	}

	/**
	 * @param invoiceFrequencyCode the invoiceFrequencyCode to set
	 */
	public void setInvoiceFrequencyCode(String invoiceFrequencyCode) {
		this.invoiceFrequencyCode = invoiceFrequencyCode;
	}

	/**
	 * @return the invoiceFrequencyDescription
	 */
	public String getInvoiceFrequencyDescription() {
		return invoiceFrequencyDescription;
	}

	/**
	 * @param invoiceFrequencyDescription the invoiceFrequencyDescription to set
	 */
	public void setInvoiceFrequencyDescription(String invoiceFrequencyDescription) {
		this.invoiceFrequencyDescription = invoiceFrequencyDescription;
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
	     m.put("invoiceFrequencyCode", this.invoiceFrequencyCode);
	     return m;
	}

}
