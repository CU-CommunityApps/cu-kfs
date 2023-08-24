package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

public class JaggaerBuilderTest extends JaggaerBuilder {
    
    public static ErpNumber buildErpNumber(String value, String isChanged) {
        ErpNumber erp = buildErpNumber(value);
        erp.setIsChanged(isChanged);
        return erp;
    }
    
    public static SQIntegrationNumber buildSQIntegrationNumber(String value) {
        SQIntegrationNumber number = new SQIntegrationNumber();
        number.setValue(value);
        return number;
    }
    
    public static ThirdPartyRefNumber buildThirdPartyRefNumber(String value) {
        ThirdPartyRefNumber number = new ThirdPartyRefNumber();
        number.setValue(value);
        return number;
    }
    
    public static Name buildName(String nameString, String isChanged) {
        Name name = buildName(nameString);
        name.setIsChanged(isChanged);
        return name;
    }
    
    public static BusinessUnitVendorNumber buildBusinessUnitVendorNumber(String businessUnitInternalName, String value) {
        BusinessUnitVendorNumber number = new BusinessUnitVendorNumber();
        number.setBusinessUnitInternalName(businessUnitInternalName);
        number.setValue(value);
        return number;
    }
    
    public static BusinessUnitVendorNumber buildBusinessUnitVendorNumber(String businessUnitInternalName, String value, String isChanged) {
        BusinessUnitVendorNumber number = buildBusinessUnitVendorNumber(businessUnitInternalName, value);
        number.setIsChanged(isChanged);
        return number;
    }
    
    public static Amount buildAmount(String amountString) {
        Amount amount = new Amount();
        amount.setValue(amountString);
        return amount;
    }
    
    public static Amount buildAmount(String amountString, String isChanged) {
        Amount amount = buildAmount(amountString);
        amount.setIsChanged(isChanged);
        return amount;
    }
    
    public static IsoCurrencyCode buildIsoCurrencyCode(String currencyCode) {
        IsoCurrencyCode code = new IsoCurrencyCode();
        code.setValue(currencyCode);
        return code;
    }
    
    public static IsoCurrencyCode buildIsoCurrencyCode(String currencyCode, String isChanged) {
        IsoCurrencyCode code = buildIsoCurrencyCode(currencyCode);
        code.setIsChanged(isChanged);
        return code;
    }
    
    public static StateServiceAreaInternalName buildStateServiceAreaInternalName(String nameString) {
        StateServiceAreaInternalName name = new StateServiceAreaInternalName();
        name.setValue(nameString);
        return name;
    }
    
    public static StateServiceAreaInternalName buildStateServiceAreaInternalName(String nameString, String isChanged) {
        StateServiceAreaInternalName name = buildStateServiceAreaInternalName(nameString);
        name.setIsChanged(isChanged);
        return name;
    }
    
    public static PrimaryNaicsItem buildPrimaryNaicsItem(String code) {
        PrimaryNaicsItem naic = new PrimaryNaicsItem();
        naic.setValue(code);
        return naic;
    }
    
    public static PrimaryNaicsItem buildPrimaryNaicsItem(String code, String isChanged) {
        PrimaryNaicsItem naic = buildPrimaryNaicsItem(code);
        naic.setIsChanged(isChanged);
        return naic;
    }
    
    public static SecondaryNaicsItem buildSecondaryNaicsItem(String code) {
        SecondaryNaicsItem naic = new SecondaryNaicsItem();
        naic.setValue(code);
        return naic;
    }
    
    public static SecondaryNaicsItem buildSecondaryNaicsItem(String code, String isChanged) {
        SecondaryNaicsItem naic = buildSecondaryNaicsItem(code);
        naic.setIsChanged(isChanged);
        return naic;
    }
    
    public static Email buildEmail(String emailAddress) {
        Email email = new Email();
        email.setValue(emailAddress);
        return email;
    }
    
    public static AddressLine buildAddressLine(String addressString, String isChanged) {
        AddressLine line = buildAddressLine(addressString);
        line.setIsChanged(isChanged);
        return line;
    }
    
    public static IsoCountryCode buildIsoCountryCode(String countryCode, String isChanged) {
        IsoCountryCode country = buildIsoCountryCode(countryCode);
        country.setIsChanged(isChanged);
        return country;
    }
    
    public static TelephoneNumber buildTelephoneNumber(String countryCode, String areaCode, String number, String extension) {
        TelephoneNumber phone = new TelephoneNumber();
        phone.setAreaCode(buildJaggaerBasicValue(areaCode));
        phone.setCountryCode(buildJaggaerBasicValue(countryCode));
        phone.setExtension(buildJaggaerBasicValue(extension));
        phone.setNumber(buildJaggaerBasicValue(number));
        return phone;
    }
    
    public static TelephoneNumber buildTelephoneNumber(String countryCode, String areaCode, String number, String extension, String isChanged) {
        TelephoneNumber phone = buildTelephoneNumber(countryCode, areaCode, number, extension);
        phone.setIsChanged(isChanged);
        phone.getAreaCode().setIsChanged(isChanged);
        phone.getCountryCode().setIsChanged(isChanged);
        phone.getExtension().setIsChanged(isChanged);
        phone.getNumber().setIsChanged(isChanged);
        return phone;
    }
    
    public static JaggaerBasicValue buildJaggaerBasicValue(String value, String isChanged) {
        JaggaerBasicValue basic = buildJaggaerBasicValue(value);
        basic.setIsChanged(isChanged);
        return basic;
    }
    
    public static BusinessUnitInternalName buildBusinessUnitInternalName(String internalName, String preferredForThisBusinessUnit) {
        BusinessUnitInternalName name = new BusinessUnitInternalName();
        name.setPreferredForThisBusinessUnit(preferredForThisBusinessUnit);
        name.setValue(internalName);
        return name;
    }
    
    public static BusinessUnitInternalName buildBusinessUnitInternalName(String internalName, String preferredForThisBusinessUnit, String isChanged) {
        BusinessUnitInternalName name = buildBusinessUnitInternalName(internalName, preferredForThisBusinessUnit);
        name.setIsChanged(isChanged);
        return name;
    }
    
    public static Attachment buildAttachment(String id, String type, String name, String size, String attachmentURL) {
        Attachment attachment = new Attachment();
        attachment.setId(id);
        attachment.setType(type);
        attachment.setAttachmentName(name);
        attachment.setAttachmentSize(size);
        attachment.setAttachmentURL(attachmentURL);
        return attachment;
    }
    
    public static DisplayName buildDisplayName(String name) {
        DisplayName displayName = new DisplayName();
        displayName.setValue(name);
        return displayName;
    }
    
    public static DisplayName buildDisplayName(String name, String isChanged) {
        DisplayName displayName = buildDisplayName(name);
        displayName.setIsChanged(isChanged);
        return displayName;
    }
}
