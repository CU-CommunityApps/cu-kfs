/**
 * 
 */
package edu.cornell.kfs.fp.businessobject;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;


/**
 * @author Admin-dwf5
 *
 */
public class CuDisbursementVoucherPayeeDetailExtension extends PersistableBusinessObjectBase
        implements PersistableBusinessObjectExtension {

    private String documentNumber;

    private String disbVchrPayeeIdType;

    private String payeeTypeSuffix;

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

	public String getPayeeTypeSuffix() {
		return payeeTypeSuffix;
	}

	public void setPayeeTypeSuffix(String payeeTypeSuffix) {
		this.payeeTypeSuffix = payeeTypeSuffix;
	}

	public String getPayeeTypeSuffixForDisplay() {
		if (StringUtils.isBlank(getPayeeTypeSuffix())) {
			return StringUtils.EMPTY;
		}
		return " (" + getPayeeTypeSuffix() + ")";
	}

}
