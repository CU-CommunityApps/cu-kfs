/**
 * 
 */
package edu.cornell.kfs.fp.businessobject;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kns.bo.PersistableBusinessObjectExtensionBase;

/**
 * @author Admin-dwf5
 *
 */
public class DisbursementVoucherPayeeDetailExtension extends PersistableBusinessObjectExtensionBase {

    private String documentNumber;

    private String disbVchrPayeeIdType;
	
    /**
     * Gets the documentNumber attribute.
     * 
     * @return Returns the documentNumber
     */
    public String getDocumentNumber() {
        return documentNumber;
    }


    /**
     * Sets the documentNumber attribute.
     * 
     * @param documentNumber The documentNumber to set.
     */
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    /**
	 * @return the disbVchrPayeeIdType
	 */
	public String getDisbVchrPayeeIdTypeDisplay() {
		if(StringUtils.isBlank(disbVchrPayeeIdType)) {
			return "";
		}
		return " - ("+disbVchrPayeeIdType+")";
	}

    /**
	 * @return the disbVchrPayeeIdType
	 */
	public String getDisbVchrPayeeIdType() {
		return disbVchrPayeeIdType;
	}

	/**
	 * @param disbVchrPayeeIdType the disbVchrPayeeIdType to set
	 */
	public void setDisbVchrPayeeIdType(String disbVchrPayeeIdType) {
		this.disbVchrPayeeIdType = disbVchrPayeeIdType;
	}

	
}
