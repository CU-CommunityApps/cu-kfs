package edu.cornell.kfs.fp.businessobject;

import org.kuali.rice.krad.bo.PersistableBusinessObjectExtensionBase;

public class ProcurementCardTransactionExtendedAttribute extends PersistableBusinessObjectExtensionBase{
	private Integer transactionSequenceRowNumber;
	private String transactionType;
	
	/**
	 * Gets the transactionSequenceRowNumber.
	 * 
	 * @return transactionSequenceRowNumber
	 */
	public Integer getTransactionSequenceRowNumber() {
		return transactionSequenceRowNumber;
	}

	/**
	 * Sets the transactionSequenceRowNumber.
	 * 
	 * @param transactionSequenceRowNumber
	 */
	public void setTransactionSequenceRowNumber(Integer transactionSequenceRowNumber) {
		this.transactionSequenceRowNumber = transactionSequenceRowNumber;
	}

	/**
	 * Gets the transactionType.
	 * 
	 * @return transactionType
	 */
	public String getTransactionType() {
		return transactionType;
	}

	/**
	 * Sets the transactionType.
	 * 
	 * @param transactionType
	 */
	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}



}
