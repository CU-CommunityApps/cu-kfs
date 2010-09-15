package edu.cornell.kfs.module.cg.businessobject;

import java.util.LinkedHashMap;

import org.kuali.rice.kns.bo.Inactivateable;
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;

public class InvoiceFrequency extends PersistableBusinessObjectBase implements Inactivateable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1732078760215418467L;
	private String invoiceFrequencyCode;
	private String invoiceFrequencyName;
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
	 * @return the invoiceFrequencyName
	 */
	public String getInvoiceFrequencyName() {
		return invoiceFrequencyName;
	}

	/**
	 * @param invoiceFrequencyName the invoiceFrequencyName to set
	 */
	public void setInvoiceFrequencyName(String invoiceFrequencyName) {
		this.invoiceFrequencyName = invoiceFrequencyName;
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
	
	@Override
	protected LinkedHashMap toStringMapper() {
		 LinkedHashMap m = new LinkedHashMap();
	     m.put("invoiceFrequencyCode", this.invoiceFrequencyCode);
	     return m;
	}

}
