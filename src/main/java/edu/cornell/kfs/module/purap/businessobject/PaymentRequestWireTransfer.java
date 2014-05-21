package edu.cornell.kfs.module.purap.businessobject;

import java.util.LinkedHashMap;

import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

public class PaymentRequestWireTransfer extends PersistableBusinessObjectBase {

    private String documentNumber;
    private String preqBankName;
    private String preqBankRoutingNumber;
    private String preqBankCityName;
    private String preqBankStateCode;
    private String preqBankCountryCode;
    private String preqAttentionLineText;
    private String preqAdditionalWireText;
    private String preqPayeeAccountNumber;
    private String preqCurrencyTypeName;
    private String preqCurrencyTypeCode;
    private boolean preqWireTransferFeeWaiverIndicator;
    private String preqPayeeAccountName;
    private String preqPayeeAccountTypeCode;
    private String preqAutomatedClearingHouseProfileNumber;
    private String preqForeignCurrencyTypeName;
    private String preqForeignCurrencyTypeCode;


    /**
     * Default no-arg constructor.
     */
    public PaymentRequestWireTransfer() {
    	preqWireTransferFeeWaiverIndicator = false;
    }


	protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();
        m.put(KFSPropertyConstants.DOCUMENT_NUMBER, this.documentNumber);
        return m;
	}


	public String getDocumentNumber() {
		return documentNumber;
	}


	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}


	public String getPreqBankName() {
		return preqBankName;
	}


	public void setPreqBankName(String preqBankName) {
		this.preqBankName = preqBankName;
	}


	public String getPreqBankRoutingNumber() {
		return preqBankRoutingNumber;
	}


	public void setPreqBankRoutingNumber(String preqBankRoutingNumber) {
		this.preqBankRoutingNumber = preqBankRoutingNumber;
	}


	public String getPreqBankCityName() {
		return preqBankCityName;
	}


	public void setPreqBankCityName(String preqBankCityName) {
		this.preqBankCityName = preqBankCityName;
	}


	public String getPreqBankStateCode() {
		return preqBankStateCode;
	}


	public void setPreqBankStateCode(String preqBankStateCode) {
		this.preqBankStateCode = preqBankStateCode;
	}


	public String getPreqBankCountryCode() {
		return preqBankCountryCode;
	}


	public void setPreqBankCountryCode(String preqBankCountryCode) {
		this.preqBankCountryCode = preqBankCountryCode;
	}


	public String getPreqAttentionLineText() {
		return preqAttentionLineText;
	}


	public void setPreqAttentionLineText(String preqAttentionLineText) {
		this.preqAttentionLineText = preqAttentionLineText;
	}


	public String getPreqAdditionalWireText() {
		return preqAdditionalWireText;
	}


	public void setPreqAdditionalWireText(String preqAdditionalWireText) {
		this.preqAdditionalWireText = preqAdditionalWireText;
	}


	public String getPreqPayeeAccountNumber() {
		return preqPayeeAccountNumber;
	}


	public void setPreqPayeeAccountNumber(String preqPayeeAccountNumber) {
		this.preqPayeeAccountNumber = preqPayeeAccountNumber;
	}


	public String getPreqCurrencyTypeName() {
		return preqCurrencyTypeName;
	}


	public void setPreqCurrencyTypeName(String preqCurrencyTypeName) {
		this.preqCurrencyTypeName = preqCurrencyTypeName;
	}


	public String getPreqCurrencyTypeCode() {
		return preqCurrencyTypeCode;
	}


	public void setPreqCurrencyTypeCode(String preqCurrencyTypeCode) {
		this.preqCurrencyTypeCode = preqCurrencyTypeCode;
	}


	public boolean isPreqWireTransferFeeWaiverIndicator() {
		return preqWireTransferFeeWaiverIndicator;
	}


	public void setPreqWireTransferFeeWaiverIndicator(
			boolean preqWireTransferFeeWaiverIndicator) {
		this.preqWireTransferFeeWaiverIndicator = preqWireTransferFeeWaiverIndicator;
	}


	public String getPreqPayeeAccountName() {
		return preqPayeeAccountName;
	}


	public void setPreqPayeeAccountName(String preqPayeeAccountName) {
		this.preqPayeeAccountName = preqPayeeAccountName;
	}


	public String getPreqPayeeAccountTypeCode() {
		return preqPayeeAccountTypeCode;
	}


	public void setPreqPayeeAccountTypeCode(String preqPayeeAccountTypeCode) {
		this.preqPayeeAccountTypeCode = preqPayeeAccountTypeCode;
	}


	public String getPreqAutomatedClearingHouseProfileNumber() {
		return preqAutomatedClearingHouseProfileNumber;
	}


	public void setPreqAutomatedClearingHouseProfileNumber(
			String preqAutomatedClearingHouseProfileNumber) {
		this.preqAutomatedClearingHouseProfileNumber = preqAutomatedClearingHouseProfileNumber;
	}


	public String getPreqForeignCurrencyTypeName() {
		return preqForeignCurrencyTypeName;
	}


	public void setPreqForeignCurrencyTypeName(String preqForeignCurrencyTypeName) {
		this.preqForeignCurrencyTypeName = preqForeignCurrencyTypeName;
	}


	public String getPreqForeignCurrencyTypeCode() {
		return preqForeignCurrencyTypeCode;
	}


	public void setPreqForeignCurrencyTypeCode(String preqForeignCurrencyTypeCode) {
		this.preqForeignCurrencyTypeCode = preqForeignCurrencyTypeCode;
	}


}
