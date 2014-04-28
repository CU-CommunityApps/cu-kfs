package org.kuali.kfs.module.purap.businessobject;

import java.util.LinkedHashMap;

import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;

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
    
    //KFSPTS-3166
    protected String preqBankStreetAddress;
    protected String preqBankProvince;
    protected String preqBankSWIFTCode;
    protected String preqBankIBAN;
    protected String preqSortOrTransitCode;
    protected String preqCorrespondentBankName;
    protected String preqCorrespondentBankAddress;
    protected String preqCorrespondentBankRoutingNumber;
    protected String preqCorrespondentBankAccountNumber;
    protected String preqCorrespondentBankSwiftCode;


    /**
     * Default no-arg constructor.
     */
    public PaymentRequestWireTransfer() {
    	preqWireTransferFeeWaiverIndicator = false;
    }


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


    public String getPreqBankStreetAddress() {
        return preqBankStreetAddress;
    }


    public void setPreqBankStreetAddress(String preqBankStreetAddress) {
        this.preqBankStreetAddress = preqBankStreetAddress;
    }


    public String getPreqBankProvince() {
        return preqBankProvince;
    }


    public void setPreqBankProvince(String preqBankProvince) {
        this.preqBankProvince = preqBankProvince;
    }


    public String getPreqBankSWIFTCode() {
        return preqBankSWIFTCode;
    }


    public void setPreqBankSWIFTCode(String preqBankSWIFTCode) {
        this.preqBankSWIFTCode = preqBankSWIFTCode;
    }


    public String getPreqBankIBAN() {
        return preqBankIBAN;
    }


    public void setPreqBankIBAN(String preqBankIBAN) {
        this.preqBankIBAN = preqBankIBAN;
    }


    public String getPreqSortOrTransitCode() {
        return preqSortOrTransitCode;
    }


    public void setPreqSortOrTransitCode(String preqSortOrTransitCode) {
        this.preqSortOrTransitCode = preqSortOrTransitCode;
    }


    public String getPreqCorrespondentBankName() {
        return preqCorrespondentBankName;
    }


    public void setPreqCorrespondentBankName(String preqCorrespondentBankName) {
        this.preqCorrespondentBankName = preqCorrespondentBankName;
    }


    public String getPreqCorrespondentBankAddress() {
        return preqCorrespondentBankAddress;
    }


    public void setPreqCorrespondentBankAddress(String preqCorrespondentBankAddress) {
        this.preqCorrespondentBankAddress = preqCorrespondentBankAddress;
    }


    public String getPreqCorrespondentBankRoutingNumber() {
        return preqCorrespondentBankRoutingNumber;
    }


    public void setPreqCorrespondentBankRoutingNumber(String preqCorrespondentBankRoutingNumber) {
        this.preqCorrespondentBankRoutingNumber = preqCorrespondentBankRoutingNumber;
    }


    public String getPreqCorrespondentBankAccountNumber() {
        return preqCorrespondentBankAccountNumber;
    }


    public void setPreqCorrespondentBankAccountNumber(String preqCorrespondentBankAccountNumber) {
        this.preqCorrespondentBankAccountNumber = preqCorrespondentBankAccountNumber;
    }


    public String getPreqCorrespondentBankSwiftCode() {
        return preqCorrespondentBankSwiftCode;
    }


    public void setPreqCorrespondentBankSwiftCode(String preqCorrespondentBankSwiftCode) {
        this.preqCorrespondentBankSwiftCode = preqCorrespondentBankSwiftCode;
    }


}
