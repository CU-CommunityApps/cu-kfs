package edu.cornell.kfs.module.purap.businessobject.lookup;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractPartyType;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerLegalStructure;

public class JaggaerContractPartyUploadDto extends JaggaerContractUploadBaseDto {

    private String overrideDupError;
    private String contractPartyName;
    private String doingBusinessAs;
    private String otherNames;
    private String countryOfOrigin;
    private JaggaerContractPartyType contractPartyType;
    private String primary;
    private JaggaerLegalStructure legalStructure;
    private String taxIDType;
    private String taxIdentificationNumber;
    private String VATRegistrationNumber;
    private String websiteURL;

    public String getOverrideDupError() {
        return overrideDupError;
    }

    public void setOverrideDupError(String overrideDupError) {
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

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
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
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE);
        builder.setExcludeFieldNames("taxIdentificationNumber");
        builder.append("taxIdentificationNumber", StringUtils.isBlank(taxIdentificationNumber) ? StringUtils.EMPTY : "restricted tax id number");
        return builder.build();
    }
    
    @Override
    public boolean equals(Object o) {
      return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
