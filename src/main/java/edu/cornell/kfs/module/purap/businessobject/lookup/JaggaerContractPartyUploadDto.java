package edu.cornell.kfs.module.purap.businessobject.lookup;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractPartyType;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerLegalStructure;

public class JaggaerContractPartyUploadDto extends JaggaerContractUploadBaseDto {

    private boolean overrideDupError;
    private String contractPartyName;
    private String doingBusinessAs;
    private String otherNames;
    private String countryOfOrigin;
    private JaggaerContractPartyType contractPartyType;
    private String Pprimary;
    private JaggaerLegalStructure legalStructure;
    private String taxIDType;
    private String taxIdentificationNumber;
    private String VATRegistrationNumber;
    private String websiteURL;

    public boolean isOverrideDupError() {
        return overrideDupError;
    }

    public void setOverrideDupError(boolean overrideDupError) {
        this.overrideDupError = overrideDupError;
    }

    public String getContractPartyName() {
        return contractPartyName;
    }

    public void setContractPartyName(String contractPartyName) {
        this.contractPartyName = contractPartyName;
    }

    public String getDoingBusinessAs() {
        return doingBusinessAs;
    }

    public void setDoingBusinessAs(String doingBusinessAs) {
        this.doingBusinessAs = doingBusinessAs;
    }

    public String getOtherNames() {
        return otherNames;
    }

    public void setOtherNames(String otherNames) {
        this.otherNames = otherNames;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public void setCountryOfOrigin(String countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin;
    }

    public JaggaerContractPartyType getContractPartyType() {
        return contractPartyType;
    }

    public void setContractPartyType(JaggaerContractPartyType contractPartyType) {
        this.contractPartyType = contractPartyType;
    }

    public String getPprimary() {
        return Pprimary;
    }

    public void setPprimary(String pprimary) {
        Pprimary = pprimary;
    }

    public JaggaerLegalStructure getLegalStructure() {
        return legalStructure;
    }

    public void setLegalStructure(JaggaerLegalStructure legalStructure) {
        this.legalStructure = legalStructure;
    }

    public String getTaxIDType() {
        return taxIDType;
    }

    public void setTaxIDType(String taxIDType) {
        this.taxIDType = taxIDType;
    }

    public String getTaxIdentificationNumber() {
        return taxIdentificationNumber;
    }

    public void setTaxIdentificationNumber(String taxIdentificationNumber) {
        this.taxIdentificationNumber = taxIdentificationNumber;
    }

    public String getVATRegistrationNumber() {
        return VATRegistrationNumber;
    }

    public void setVATRegistrationNumber(String vATRegistrationNumber) {
        VATRegistrationNumber = vATRegistrationNumber;
    }

    public String getWebsiteURL() {
        return websiteURL;
    }

    public void setWebsiteURL(String websiteURL) {
        this.websiteURL = websiteURL;
    }
    
    @Override
    public String toString() {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this);
        builder.setExcludeFieldNames("taxIdentificationNumber");
        builder.append("taxIdentificationNumber", StringUtils.isBlank(taxIdentificationNumber) ? StringUtils.EMPTY : "restrctived tax id number");
        return builder.build();
    }

}
