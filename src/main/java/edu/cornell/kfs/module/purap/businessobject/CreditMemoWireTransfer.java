package edu.cornell.kfs.module.purap.businessobject;

import java.util.LinkedHashMap;

import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;

public class CreditMemoWireTransfer extends PersistableBusinessObjectBase {

	private String documentNumber;
    private String cmBankName;
    private String cmBankRoutingNumber;
    private String cmBankCityName;
    private String cmBankStateCode;
    private String cmBankCountryCode;
    private String cmAttentionLineText;
    private String cmAdditionalWireText;
    private String cmPayeeAccountNumber;
    private String cmCurrencyTypeName;
    private String cmCurrencyTypeCode;
    private boolean cmWireTransferFeeWaiverIndicator;
    private String cmPayeeAccountName;
    private String cmPayeeAccountTypeCode;
    private String cmAutomatedClearingHouseProfileNumber;
    private String cmForeignCurrencyTypeName;
    private String cmForeignCurrencyTypeCode;

    public CreditMemoWireTransfer() {
    	cmWireTransferFeeWaiverIndicator = false;
    }


	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
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


	public String getCmBankName() {
		return cmBankName;
	}


	public void setCmBankName(String cmBankName) {
		this.cmBankName = cmBankName;
	}


	public String getCmBankRoutingNumber() {
		return cmBankRoutingNumber;
	}


	public void setCmBankRoutingNumber(String cmBankRoutingNumber) {
		this.cmBankRoutingNumber = cmBankRoutingNumber;
	}


	public String getCmBankCityName() {
		return cmBankCityName;
	}


	public void setCmBankCityName(String cmBankCityName) {
		this.cmBankCityName = cmBankCityName;
	}


	public String getCmBankStateCode() {
		return cmBankStateCode;
	}


	public void setCmBankStateCode(String cmBankStateCode) {
		this.cmBankStateCode = cmBankStateCode;
	}


	public String getCmBankCountryCode() {
		return cmBankCountryCode;
	}


	public void setCmBankCountryCode(String cmBankCountryCode) {
		this.cmBankCountryCode = cmBankCountryCode;
	}


	public String getCmAttentionLineText() {
		return cmAttentionLineText;
	}


	public void setCmAttentionLineText(String cmAttentionLineText) {
		this.cmAttentionLineText = cmAttentionLineText;
	}


	public String getCmAdditionalWireText() {
		return cmAdditionalWireText;
	}


	public void setCmAdditionalWireText(String cmAdditionalWireText) {
		this.cmAdditionalWireText = cmAdditionalWireText;
	}


	public String getCmPayeeAccountNumber() {
		return cmPayeeAccountNumber;
	}


	public void setCmPayeeAccountNumber(String cmPayeeAccountNumber) {
		this.cmPayeeAccountNumber = cmPayeeAccountNumber;
	}


	public String getCmCurrencyTypeName() {
		return cmCurrencyTypeName;
	}


	public void setCmCurrencyTypeName(String cmCurrencyTypeName) {
		this.cmCurrencyTypeName = cmCurrencyTypeName;
	}


	public String getCmCurrencyTypeCode() {
		return cmCurrencyTypeCode;
	}


	public void setCmCurrencyTypeCode(String cmCurrencyTypeCode) {
		this.cmCurrencyTypeCode = cmCurrencyTypeCode;
	}


	public boolean isCmWireTransferFeeWaiverIndicator() {
		return cmWireTransferFeeWaiverIndicator;
	}


	public void setCmWireTransferFeeWaiverIndicator(
			boolean cmWireTransferFeeWaiverIndicator) {
		this.cmWireTransferFeeWaiverIndicator = cmWireTransferFeeWaiverIndicator;
	}


	public String getCmPayeeAccountName() {
		return cmPayeeAccountName;
	}


	public void setCmPayeeAccountName(String cmPayeeAccountName) {
		this.cmPayeeAccountName = cmPayeeAccountName;
	}


	public String getCmPayeeAccountTypeCode() {
		return cmPayeeAccountTypeCode;
	}


	public void setCmPayeeAccountTypeCode(String cmPayeeAccountTypeCode) {
		this.cmPayeeAccountTypeCode = cmPayeeAccountTypeCode;
	}


	public String getCmAutomatedClearingHouseProfileNumber() {
		return cmAutomatedClearingHouseProfileNumber;
	}


	public void setCmAutomatedClearingHouseProfileNumber(
			String cmAutomatedClearingHouseProfileNumber) {
		this.cmAutomatedClearingHouseProfileNumber = cmAutomatedClearingHouseProfileNumber;
	}


	public String getCmForeignCurrencyTypeName() {
		return cmForeignCurrencyTypeName;
	}


	public void setCmForeignCurrencyTypeName(String cmForeignCurrencyTypeName) {
		this.cmForeignCurrencyTypeName = cmForeignCurrencyTypeName;
	}


	public String getCmForeignCurrencyTypeCode() {
		return cmForeignCurrencyTypeCode;
	}


	public void setCmForeignCurrencyTypeCode(String cmForeignCurrencyTypeCode) {
		this.cmForeignCurrencyTypeCode = cmForeignCurrencyTypeCode;
	}

}
