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

}
