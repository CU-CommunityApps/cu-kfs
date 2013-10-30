package edu.cornell.kfs.vnd.businessobject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorRoutingComparable;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.bo.PersistableBusinessObjectExtensionBase;
import org.kuali.rice.krad.util.ObjectUtils;

public class VendorDetailExtension extends PersistableBusinessObjectExtensionBase implements VendorRoutingComparable {
	private static final long serialVersionUID = 2L;

    private Integer vendorHeaderGeneratedIdentifier;
    private Integer vendorDetailAssignedIdentifier;
    private boolean einvoiceVendorIndicator;

    private boolean insuranceRequiredIndicator;
    private Boolean insuranceRequirementsCompleteIndicator;
    private Boolean cornellAdditionalInsuredIndicator;
    private KualiDecimal generalLiabilityCoverageAmount;
    private Date generalLiabilityExpiration;
    private KualiDecimal automobileLiabilityCoverageAmount;
    private Date automobileLiabilityExpiration;
    private KualiDecimal workmansCompCoverageAmount;
    private Date workmansCompExpiration;
    private KualiDecimal excessLiabilityUmbrellaAmount;
    private Date excessLiabilityUmbExpiration;
    private Boolean healthOffSiteCateringLicenseReq;
    private Date healthOffSiteLicenseExpirationDate;
    private String insuranceNotes;
    private String merchantNotes;
    private List<CuVendorCreditCardMerchant> vendorCreditCardMerchants;

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

    public boolean isEinvoiceVendorIndicator() {
        return einvoiceVendorIndicator;
    }

    public void setEinvoiceVendorIndicator(boolean eInvoiceVendorIndicator) {
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

	@Override
	public boolean isEqualForRouting(Object toCompare) {

      if ((ObjectUtils.isNull(toCompare)) || !(toCompare instanceof VendorDetailExtension)) {
          return false;
      }
      else {
          VendorDetailExtension detailExt = (VendorDetailExtension) toCompare;
          boolean eq = new EqualsBuilder().append(
                          // KFSPTS-2055 also include CU insurance enhancement
                  this.isInsuranceRequiredIndicator(),detailExt.isInsuranceRequiredIndicator()).append(
                  this.getInsuranceRequirementsCompleteIndicator(),detailExt.getInsuranceRequirementsCompleteIndicator()).append(
                  this.getCornellAdditionalInsuredIndicator(),detailExt.getCornellAdditionalInsuredIndicator()).append(
                  this.getGeneralLiabilityCoverageAmount(),detailExt.getGeneralLiabilityCoverageAmount()).append(
                  this.getGeneralLiabilityExpiration(),detailExt.getGeneralLiabilityExpiration()).append(
                  this.getAutomobileLiabilityCoverageAmount(),detailExt.getAutomobileLiabilityCoverageAmount()).append(
                  this.getAutomobileLiabilityExpiration(),detailExt.getAutomobileLiabilityExpiration()).append(
                  this.getWorkmansCompCoverageAmount(),detailExt.getWorkmansCompCoverageAmount()).append(
                  this.getWorkmansCompExpiration(),detailExt.getWorkmansCompExpiration()).append(
                  this.getExcessLiabilityUmbExpiration(),detailExt.getExcessLiabilityUmbExpiration()).append(
                  this.getExcessLiabilityUmbrellaAmount(),detailExt.getExcessLiabilityUmbrellaAmount()).append(
                  this.getHealthOffSiteCateringLicenseReq(),detailExt.getHealthOffSiteCateringLicenseReq()).append(
                  this.getHealthOffSiteLicenseExpirationDate(),detailExt.getHealthOffSiteLicenseExpirationDate()).append(
                  this.getInsuranceNotes(),detailExt.getInsuranceNotes()).append(
                  this.getMerchantNotes(),detailExt.getMerchantNotes()).isEquals();
          eq &= SpringContext.getBean(VendorService.class).equalMemberLists(this.getVendorCreditCardMerchants(),detailExt.getVendorCreditCardMerchants());
          return eq;

      }
	}

}