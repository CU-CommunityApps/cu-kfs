package edu.cornell.kfs.vnd.businessobject;


import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.PaymentTermType;
import org.kuali.kfs.vnd.businessobject.ShippingPaymentTerms;
import org.kuali.kfs.vnd.businessobject.ShippingTitle;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorInactiveReason;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.COMPONENT;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.NAMESPACE;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.util.ObjectUtils;

@NAMESPACE(namespace = KFSConstants.CoreModuleNamespaces.VENDOR)
@COMPONENT(component = "VendorDetail")
public class CuVendorDetail extends VendorDetail {

    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(CuVendorDetail.class);
    private CuVendorHeader vendorHeader;
    // vendoraddress not in the scope of upgrade-280
   // private List<CuVendorAddress> vendorAddresses;
    private VendorInactiveReason vendorInactiveReason;
    private PaymentTermType vendorPaymentTerms;
    private ShippingTitle vendorShippingTitle;
    private ShippingPaymentTerms vendorShippingPaymentTerms;
    private CuVendorDetail soldToVendorDetail;
    private Person vendorRestrictedPerson;

    /* BEGIN CORNELL SPECIFIC MODIFICATIONS */
    
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
    // END CU mod
    
    public CuVendorDetail() {
    	super();
        vendorCreditCardMerchants = new ArrayList<CuVendorCreditCardMerchant>();
//        vendorAddresses = new ArrayList<CuVendorAddress>();
        vendorHeader = new CuVendorHeader();

    }

    public boolean isEqualForRouting(Object toCompare) {
        LOG.debug("Entering isEqualForRouting.");
        if ((ObjectUtils.isNull(toCompare)) || !(toCompare instanceof VendorDetail)) {
            return false;
        }
        else {
            CuVendorDetail detail = (CuVendorDetail) toCompare;
            return new EqualsBuilder().append(
                    this.getVendorHeaderGeneratedIdentifier(), detail.getVendorHeaderGeneratedIdentifier()).append(
                    this.getVendorDetailAssignedIdentifier(), detail.getVendorDetailAssignedIdentifier()).append(
                    this.isVendorParentIndicator(), detail.isVendorParentIndicator()).append(
                    this.getVendorName(), detail.getVendorName()).append(
                    this.getVendorFirstName(), detail.getVendorFirstName()).append( // ==== KFSPTS-1422: Added vendorFirstName to the comparison. ====
                    this.getVendorLastName(), detail.getVendorLastName()).append( // ==== KFSPTS-1422: Added vendorLastName to the comparison. ====
                    this.isActiveIndicator(), detail.isActiveIndicator()).append(
                    this.getVendorInactiveReasonCode(), detail.getVendorInactiveReasonCode()).append(
                    this.getVendorDunsNumber(), detail.getVendorDunsNumber()).append(
                    this.getVendorPaymentTermsCode(), detail.getVendorPaymentTermsCode()).append(
                    this.getVendorShippingTitleCode(), detail.getVendorShippingTitleCode()).append(
                    this.getVendorShippingPaymentTermsCode(), detail.getVendorShippingPaymentTermsCode()).append(
                    this.getVendorConfirmationIndicator(), detail.getVendorConfirmationIndicator()).append(
                    this.getVendorPrepaymentIndicator(), detail.getVendorPrepaymentIndicator()).append(
                    this.getVendorCreditCardIndicator(), detail.getVendorCreditCardIndicator()).append(
                    this.getVendorMinimumOrderAmount(), detail.getVendorMinimumOrderAmount()).append(
                    this.getVendorUrlAddress(), detail.getVendorUrlAddress()).append(
                    this.getVendorRemitName(), detail.getVendorRemitName()).append(
                    this.getVendorRestrictedIndicator(), detail.getVendorRestrictedIndicator()).append(
                    this.getVendorRestrictedReasonText(), detail.getVendorRestrictedReasonText()).append(
                    this.getVendorRestrictedDate(), detail.getVendorRestrictedDate()).append(
                    this.getVendorRestrictedPersonIdentifier(), detail.getVendorRestrictedPersonIdentifier()).append(
                    this.getVendorSoldToGeneratedIdentifier(), detail.getVendorSoldToGeneratedIdentifier()).append(
                    this.getVendorSoldToAssignedIdentifier(), detail.getVendorSoldToAssignedIdentifier()).append(
                    this.getVendorSoldToName(), detail.getVendorSoldToName()).append(
                    this.isTaxableIndicator(), detail.isTaxableIndicator()).append( // KFSPTS-2055
                            // KFSPTS-2055 also include CU insurance enhancement
                    this.isInsuranceRequiredIndicator(),detail.isInsuranceRequiredIndicator()).append(
                    this.getInsuranceRequirementsCompleteIndicator(),detail.getInsuranceRequirementsCompleteIndicator()).append(
                    this.getCornellAdditionalInsuredIndicator(),detail.getCornellAdditionalInsuredIndicator()).append(
                    this.getGeneralLiabilityCoverageAmount(),detail.getGeneralLiabilityCoverageAmount()).append(
                    this.getGeneralLiabilityExpiration(),detail.getGeneralLiabilityExpiration()).append(
                    this.getAutomobileLiabilityCoverageAmount(),detail.getAutomobileLiabilityCoverageAmount()).append(
                    this.getAutomobileLiabilityExpiration(),detail.getAutomobileLiabilityExpiration()).append(
                    this.getWorkmansCompCoverageAmount(),detail.getWorkmansCompCoverageAmount()).append(
                    this.getWorkmansCompExpiration(),detail.getWorkmansCompExpiration()).append(
                    this.getExcessLiabilityUmbExpiration(),detail.getExcessLiabilityUmbExpiration()).append(
                    this.getExcessLiabilityUmbrellaAmount(),detail.getExcessLiabilityUmbrellaAmount()).append(
                    this.getHealthOffSiteCateringLicenseReq(),detail.getHealthOffSiteCateringLicenseReq()).append(
                    this.getHealthOffSiteLicenseExpirationDate(),detail.getHealthOffSiteLicenseExpirationDate()).append(
                    this.getInsuranceNotes(),detail.getInsuranceNotes()).append(
                    this.getMerchantNotes(),detail.getMerchantNotes()).append( // end CU insurance fields
                    this.isVendorFirstLastNameIndicator(), detail.isVendorFirstLastNameIndicator()).isEquals();
        }
    }
	
    
    /* BEGIN CORNELL SPECIFIC MODIFICATIONS */
    
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

	public CuVendorHeader getVendorHeader() {
		return vendorHeader;
	}

	public void setVendorHeader(CuVendorHeader vendorHeader) {
		this.vendorHeader = vendorHeader;
	}

//	public List<VendorAddress> getVendorAddresses() {
//		// TODO : can't cast List<CuVendorAddress> to List<VendorAddress>
//		// need further work to resolve this
//		List<VendorAddress> addresses = new ArrayList<VendorAddress>();
//		if (CollectionUtils.isNotEmpty(vendorAddresses)) {
//			for (CuVendorAddress address : vendorAddresses) {
//				addresses.add(address);
//			}
//		}
//		return addresses;
//	}
//
//	public void setVendorAddresses(List<VendorAddress> vendorAddresses) {
//		this.vendorAddresses = new ArrayList<CuVendorAddress>();
//		if (CollectionUtils.isNotEmpty(vendorAddresses)) {
//			for (VendorAddress address : vendorAddresses) {
//				this.vendorAddresses.add((CuVendorAddress) address);
//			}
//		}
//	}

	public CuVendorDetail getSoldToVendorDetail() {
		return soldToVendorDetail;
	}

	public void setSoldToVendorDetail(CuVendorDetail soldToVendorDetail) {
		this.soldToVendorDetail = soldToVendorDetail;
	}
	
    /* END CORNELL SPECIFIC MODIFICATIONS */

}
