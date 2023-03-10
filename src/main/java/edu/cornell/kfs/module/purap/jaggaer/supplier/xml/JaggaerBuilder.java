package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

public class JaggaerBuilder {
    
    public static ERPNumber buildERPNumber(String value) {
        return buildERPNumber(value, null);
    }
    
    public static ERPNumber buildERPNumber(String value, String isChanged) {
        ERPNumber erp = new ERPNumber();
        erp.setValue(value);
        erp.setIsChanged(isChanged);
        return erp;
    }
    
    public static SQIntegrationNumber buildSQIntegrationNumber(String value) {
        return buildSQIntegrationNumber(value, null);
    }
    
    public static SQIntegrationNumber buildSQIntegrationNumber(String value, String isChanged) {
        SQIntegrationNumber number = new SQIntegrationNumber();
        number.setValue(value);
        number.setIsChanged(isChanged);
        return number;
    }
    
    public static ThirdPartyRefNumber buildThirdPartyRefNumber(String value) {
        return buildThirdPartyRefNumber(value, null);
    }
    
    public static ThirdPartyRefNumber buildThirdPartyRefNumber(String value, String isChanged) {
        ThirdPartyRefNumber number = new ThirdPartyRefNumber();
        number.setValue(value);
        number.setIsChanged(isChanged);
        return number;
    }
    
    public static Name buildName(String nameString) {
        return buildName(nameString, null);
    }
    
    public static Name buildName(String nameString, String isChanged) {
        Name name = new Name();
        name.setValue(nameString);
        name.setIsChanged(isChanged);
        return name;
    }
    
    public static Active buildActive(String activeString) {
        return buildActive(activeString, null);
    }
    
    public static Active buildActive(String activeString, String isChanged) {
        Active active = new Active();
        active.setValue(activeString);
        active.setIsChanged(isChanged);
        return active;
    }
    
    public static BusinessUnitVendorNumber buildBusinessUnitVendorNumber(String businessUnitInternalName, String value) {
        return buildBusinessUnitVendorNumber(businessUnitInternalName, value, null);
    }
    
    public static BusinessUnitVendorNumber buildBusinessUnitVendorNumber(String businessUnitInternalName, String value, String isChanged) {
        BusinessUnitVendorNumber number = new BusinessUnitVendorNumber();
        number.setBusinessUnitInternalName(businessUnitInternalName);
        number.setValue(value);
        number.setIsChanged(isChanged);
        return number;
    }
    
    public static Amount buildAmount(String amountString) {
        return buildAmount(amountString, null);
    }
    
    public static Amount buildAmount(String amountString, String isChanged) {
        Amount amount = new Amount();
        amount.setValue(amountString);
        amount.setIsChanged(isChanged);
        return amount;
    }
    
    public static IsoCurrencyCode buildIsoCurrencyCode(String currencyCode) {
        return buildIsoCurrencyCode(currencyCode, null);
    }
    
    public static IsoCurrencyCode buildIsoCurrencyCode(String currencyCode, String isChanged) {
        IsoCurrencyCode code = new IsoCurrencyCode();
        code.setIsChanged(isChanged);
        code.setValue(currencyCode);
        return code;
    }
    
    public static StateServiceAreaInternalName buildStateServiceAreaInternalName(String nameString) {
        return buildStateServiceAreaInternalName(nameString, null);
    }
    
    public static StateServiceAreaInternalName buildStateServiceAreaInternalName(String nameString, String isChanged) {
        StateServiceAreaInternalName name = new StateServiceAreaInternalName();
        name.setValue(nameString);
        name.setIsChanged(isChanged);
        return name;
    }
    
    public static PrimaryNaics buildPrimaryNaics(String code) {
        return buildPrimaryNaics(code, null);
    }
    
    public static PrimaryNaics buildPrimaryNaics(String code, String isChanged) {
        PrimaryNaics naic = new PrimaryNaics();
        naic.setValue(code);
        naic.setIsChanged(isChanged);
        return naic;
    }
    
    public static SecondaryNaics buildSecondaryNaics(String code) {
        return buildSecondaryNaics(code, null);
    }
    
    public static SecondaryNaics buildSecondaryNaics(String code, String isChanged) {
        SecondaryNaics naic = new SecondaryNaics();
        naic.setValue(code);
        naic.setIsChanged(isChanged);
        return naic;
    }
    

}
