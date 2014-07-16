package edu.cornell.kfs.vnd.businessobject;

import java.sql.Date;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

/**
 *  non-persistable to hold vendor insurance tracking converted from input data file
 **/
public class VendorBatchInsuranceTracking {
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
    
    public VendorBatchInsuranceTracking(String[] insuranceTracking) {
    	insuranceRequiredIndicator = StringUtils.equalsIgnoreCase("Y", insuranceTracking[0]);
    	if (StringUtils.isNotBlank(insuranceTracking[1])) {
        	insuranceRequirementsCompleteIndicator = StringUtils.equalsIgnoreCase("Y", insuranceTracking[1]);
    	}
    	if (StringUtils.isNotBlank(insuranceTracking[2])) {
        	cornellAdditionalInsuredIndicator = StringUtils.equalsIgnoreCase("Y", insuranceTracking[2]);
    	}
    	generalLiabilityCoverageAmount = getAmount(insuranceTracking[3]);
    	generalLiabilityExpiration = getFormatDate(insuranceTracking[4]);
    	automobileLiabilityCoverageAmount = getAmount(insuranceTracking[5]);
    	automobileLiabilityExpiration = getFormatDate(insuranceTracking[6]);
    	workmansCompCoverageAmount = getAmount(insuranceTracking[7]);
    	workmansCompExpiration = getFormatDate(insuranceTracking[8]);
    	excessLiabilityUmbrellaAmount = getAmount(insuranceTracking[9]);
    	excessLiabilityUmbExpiration = getFormatDate(insuranceTracking[10]);
    	if (StringUtils.isNotBlank(insuranceTracking[11])) {
        	healthOffSiteCateringLicenseReq = StringUtils.equalsIgnoreCase("Y", insuranceTracking[11]);
    	}
    	healthOffSiteLicenseExpirationDate = getFormatDate(insuranceTracking[12]);
    	insuranceNotes = insuranceTracking[13];
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

	private KualiDecimal getAmount(String amount) {
		if (StringUtils.isNotBlank(amount)) {
			return new KualiDecimal(amount);
		}
		return null;
	}
	
	private Date getFormatDate(String stringDate) {
        SimpleDateFormat format = new SimpleDateFormat("MM.dd.yyyy");
        if (stringDate.contains("/")) {
        	format = new SimpleDateFormat("MM/dd/yyyy");
        }
        Date date = null;
        if (StringUtils.isNotBlank(stringDate)) {
            try {
                date = new Date (format.parse(stringDate).getTime());
            } catch (Exception e) {
            }
        }
        return date;
	}

}
