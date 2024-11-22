package edu.cornell.kfs.vnd.businessobject;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.vnd.CUVendorConstants;

public class VendorDetailExtension extends PersistableBusinessObjectBase
        implements PersistableBusinessObjectExtension {
	private static final long serialVersionUID = 2L;

	protected Integer vendorHeaderGeneratedIdentifier;
    protected Integer vendorDetailAssignedIdentifier;
    protected String einvoiceVendorIndicator;
    
    protected boolean insuranceRequiredIndicator;
    protected Boolean insuranceRequirementsCompleteIndicator;
    protected Boolean cornellAdditionalInsuredIndicator;
    protected KualiDecimal generalLiabilityCoverageAmount;
    protected Date generalLiabilityExpiration;
    protected KualiDecimal automobileLiabilityCoverageAmount;
    protected Date automobileLiabilityExpiration;
    protected KualiDecimal workmansCompCoverageAmount;
    protected Date workmansCompExpiration;
    protected KualiDecimal excessLiabilityUmbrellaAmount;
    protected Date excessLiabilityUmbExpiration;
    protected Boolean healthOffSiteCateringLicenseReq;
    protected Date healthOffSiteLicenseExpirationDate;
    protected String insuranceNotes;
    protected String merchantNotes;
    protected List<CuVendorCreditCardMerchant> vendorCreditCardMerchants;
    protected String procurementMethods;

    protected boolean paymentWorksOriginatingIndicator;
    protected Timestamp paymentWorksLastActivityTimestamp;
    
    public VendorDetailExtension() {
        vendorCreditCardMerchants = new ArrayList<CuVendorCreditCardMerchant>();

    }

    public Integer getVendorHeaderGeneratedIdentifier() {
        return vendorHeaderGeneratedIdentifier;
    }

    public void setVendorHeaderGeneratedIdentifier(Integer vendorHeaderGeneratedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
    }

    public Integer getVendorDetailAssignedIdentifier() {
        return vendorDetailAssignedIdentifier;
    }

    public void setVendorDetailAssignedIdentifier(Integer vendorDetailAssignedIdentifier) {
        this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
    }

    public String getEinvoiceVendorIndicator() {
        return einvoiceVendorIndicator;
    }

    public void setEinvoiceVendorIndicator(String eInvoiceVendorIndicator) {
        this.einvoiceVendorIndicator = eInvoiceVendorIndicator;
    }
    
	public boolean isInsuranceRequiredIndicator() {
		return insuranceRequiredIndicator;
	}

	public void setInsuranceRequiredIndicator(boolean insuranceRequiredIndicator) {
		this.insuranceRequiredIndicator = insuranceRequiredIndicator;
	}

	public Boolean getInsuranceRequirementsCompleteIndicator() {
		return insuranceRequirementsCompleteIndicator;
	}

	public void setInsuranceRequirementsCompleteIndicator(
			Boolean insuranceRequirementsCompleteIndicator) {
		this.insuranceRequirementsCompleteIndicator = insuranceRequirementsCompleteIndicator;
	}

	public Boolean getCornellAdditionalInsuredIndicator() {
		return cornellAdditionalInsuredIndicator;
	}

	public void setCornellAdditionalInsuredIndicator(
			Boolean cornellAdditionalInsuredIndicator) {
		this.cornellAdditionalInsuredIndicator = cornellAdditionalInsuredIndicator;
	}

	public KualiDecimal getGeneralLiabilityCoverageAmount() {
		return generalLiabilityCoverageAmount;
	}

	public void setGeneralLiabilityCoverageAmount(
			KualiDecimal generalLiabilityCoverageAmount) {
		this.generalLiabilityCoverageAmount = generalLiabilityCoverageAmount;
	}

	public Date getGeneralLiabilityExpiration() {
		return generalLiabilityExpiration;
	}

	public void setGeneralLiabilityExpiration(Date generalLiabilityExpiration) {
		this.generalLiabilityExpiration = generalLiabilityExpiration;
	}

	public KualiDecimal getAutomobileLiabilityCoverageAmount() {
		return automobileLiabilityCoverageAmount;
	}

	public void setAutomobileLiabilityCoverageAmount(
			KualiDecimal automobileLiabilityCoverageAmount) {
		this.automobileLiabilityCoverageAmount = automobileLiabilityCoverageAmount;
	}

	public Date getAutomobileLiabilityExpiration() {
		return automobileLiabilityExpiration;
	}

	public void setAutomobileLiabilityExpiration(Date automobileLiabilityExpiration) {
		this.automobileLiabilityExpiration = automobileLiabilityExpiration;
	}

	public KualiDecimal getWorkmansCompCoverageAmount() {
		return workmansCompCoverageAmount;
	}

	public void setWorkmansCompCoverageAmount(
			KualiDecimal workmansCompCoverageAmount) {
		this.workmansCompCoverageAmount = workmansCompCoverageAmount;
	}

	public Date getWorkmansCompExpiration() {
		return workmansCompExpiration;
	}

	public void setWorkmansCompExpiration(Date workmansCompExpiration) {
		this.workmansCompExpiration = workmansCompExpiration;
	}

	public KualiDecimal getExcessLiabilityUmbrellaAmount() {
		return excessLiabilityUmbrellaAmount;
	}

	public void setExcessLiabilityUmbrellaAmount(
			KualiDecimal excessLiabilityUmbrellaAmount) {
		this.excessLiabilityUmbrellaAmount = excessLiabilityUmbrellaAmount;
	}

	public Date getExcessLiabilityUmbExpiration() {
		return excessLiabilityUmbExpiration;
	}

	public void setExcessLiabilityUmbExpiration(Date excessLiabilityUmbExpiration) {
		this.excessLiabilityUmbExpiration = excessLiabilityUmbExpiration;
	}

	public Boolean getHealthOffSiteCateringLicenseReq() {
		return healthOffSiteCateringLicenseReq;
	}

	public void setHealthOffSiteCateringLicenseReq(
			Boolean healthOffSiteCateringLicenseReq) {
		this.healthOffSiteCateringLicenseReq = healthOffSiteCateringLicenseReq;
	}

	public Date getHealthOffSiteLicenseExpirationDate() {
		return healthOffSiteLicenseExpirationDate;
	}

	public void setHealthOffSiteLicenseExpirationDate(
			Date healthOffSiteLicenseExpirationDate) {
		this.healthOffSiteLicenseExpirationDate = healthOffSiteLicenseExpirationDate;
	}

	public String getInsuranceNotes() {
		return insuranceNotes;
	}

	public void setInsuranceNotes(String insuranceNotes) {
		this.insuranceNotes = insuranceNotes;
	}

	public String getMerchantNotes() {
		return merchantNotes;
	}

	public void setMerchantNotes(String merchantNotes) {
		this.merchantNotes = merchantNotes;
	}

	public List<CuVendorCreditCardMerchant> getVendorCreditCardMerchants() {
		return vendorCreditCardMerchants;
	}

	public void setVendorCreditCardMerchants(
			List<CuVendorCreditCardMerchant> vendorCreditCardMerchants) {
		this.vendorCreditCardMerchants = vendorCreditCardMerchants;
	}

	public String getProcurementMethods() {
		return procurementMethods;
	}

	public void setProcurementMethods(String procurementMethods) {
		this.procurementMethods = procurementMethods;
	}

	// This getter is for the multiselect version of the procurementMethods field.
	public Object getProcurementMethodsArray() {
		return StringUtils.split(procurementMethods, ','); 
	}

	// This setter is for the multiselect version of the procurementMethods field.
	public void setProcurementMethodsArray(Object procurementMethodsArray) {
		// Only set property if null or an array, in the event of a string value being passed in instead.
		if (procurementMethodsArray == null) {
			this.procurementMethods = null;
		} else if (procurementMethodsArray instanceof String[]) {
			this.procurementMethods = StringUtils.join((String[]) procurementMethodsArray, ',');
		}
	}

	// This getter handles read-only UI display of the procurementMethods field.
	public String getProcurementMethodsForDisplay() {
		if (StringUtils.isBlank(procurementMethods)) {
			return KFSConstants.EMPTY_STRING;
		}
		
		// Get the results as comma-plus-space-delimited labels instead of comma-delimited keys.
		StringBuilder displayValue = new StringBuilder(50);
		for (String procurementMethod : StringUtils.split(procurementMethods, ',')) {
			displayValue.append(CUVendorConstants.PROC_METHODS_LABEL_MAP.get(procurementMethod)).append(',').append(' ');
		}
		
		// Remove trailing ", " from the returned result.
		return displayValue.substring(0, displayValue.length() - 2);
	}

    public boolean isPaymentWorksOriginatingIndicator() {
        return paymentWorksOriginatingIndicator;
    }

    public void setPaymentWorksOriginatingIndicator(boolean paymentWorksOriginatingIndicator) {
        this.paymentWorksOriginatingIndicator = paymentWorksOriginatingIndicator;
    }

    public Timestamp getPaymentWorksLastActivityTimestamp() {
        return paymentWorksLastActivityTimestamp;
    }

    public void setPaymentWorksLastActivityTimestamp(Timestamp paymentWorksLastActivityTimestamp) {
        this.paymentWorksLastActivityTimestamp = paymentWorksLastActivityTimestamp;
    }
}