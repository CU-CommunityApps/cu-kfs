package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import edu.cornell.kfs.module.purap.JaggaerConstants.JaggaerBooleanToStringType;

public class JaggaerBuilder {
    
    public static ErpNumber buildErpNumber(String value) {
        return buildErpNumber(value, null);
    }
    
    public static ErpNumber buildErpNumber(String value, String isChanged) {
        ErpNumber erp = new ErpNumber();
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
    
    public static Active buildActive(boolean isActive, JaggaerBooleanToStringType jaggaerBooleanToStringType) {
        return buildActive(isActive, null, jaggaerBooleanToStringType);
    }
    
    public static Active buildActive(String activeString, String isChanged) {
        Active active = new Active();
        active.setValue(activeString);
        active.setIsChanged(isChanged);
        return active;
    }
    
    public static Active buildActive(boolean isActive, String isChanged, JaggaerBooleanToStringType jaggaerBooleanToStringType) {
        Active active = new Active();
        active.setValue(isActive ? jaggaerBooleanToStringType.true_string : jaggaerBooleanToStringType.false_string);
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
    
    public static PrimaryNaicsItem buildPrimaryNaicsItem(String code) {
        return buildPrimaryNaicsItem(code, null);
    }
    
    public static PrimaryNaicsItem buildPrimaryNaicsItem(String code, String isChanged) {
        PrimaryNaicsItem naic = new PrimaryNaicsItem();
        naic.setValue(code);
        naic.setIsChanged(isChanged);
        return naic;
    }
    
    public static SecondaryNaicsItem buildSecondaryNaicsItem(String code) {
        return buildSecondaryNaicsItem(code, null);
    }
    
    public static SecondaryNaicsItem buildSecondaryNaicsItem(String code, String isChanged) {
        SecondaryNaicsItem naic = new SecondaryNaicsItem();
        naic.setValue(code);
        naic.setIsChanged(isChanged);
        return naic;
    }
    
    public static Email buildEmail(String emailAddress) {
        return buildEmail(emailAddress, null);
    }
    
    public static Email buildEmail(String emailAddress, String isChanged) {    
        Email email = new Email();
        email.setValue(emailAddress);
        email.setIsChanged(isChanged);
        return email;
    }
    
    public static AddressLine buildAddressLine(String addressString) {
        return buildAddressLine(addressString, null);
    }
    
    public static AddressLine buildAddressLine(String addressString, String isChanged) {
        AddressLine line = new AddressLine();
        line.setIsChanged(isChanged);
        line.setValue(addressString);
        return line;
    }
    
    public static City buildCity(String cityString) {
        return buildCity(cityString, null);
    }
    
    public static City buildCity(String cityString, String isChanged) {
        City city = new City();
        city.setValue(cityString);
        city.setIsChanged(isChanged);
        return city;
    }
    
    public static State buildState(String stateString) {
        return buildState(stateString, null);
    }
    
    public static State buildState(String stateString, String isChanged) {
        State state = new State();
        state.setValue(stateString);
        state.setIsChanged(isChanged);
        return state;
    }
    
    public static PostalCode buildPostalCode(String postalString) {
        return buildPostalCode(postalString, null);
    }
    
    public static PostalCode buildPostalCode(String postalString, String isChanged) {
        PostalCode postalCode = new PostalCode();
        postalCode.setValue(postalString);
        postalCode.setIsChanged(isChanged);
        return postalCode;
    }
    
    public static IsoCountryCode buildIsoCountryCode(String countryCode) {
        return buildIsoCountryCode(countryCode, null);
    }
    
    public static IsoCountryCode buildIsoCountryCode(String countryCode, String isChanged) {
        IsoCountryCode country = new IsoCountryCode();
        country.setValue(countryCode);
        country.setIsChanged(isChanged);
        return country;
    }
    
    public static TelephoneNumber buildTelephoneNumber(String countryCode, String areaCode, String number, String extension) {
        return buildTelephoneNumber(countryCode, areaCode, number, extension, null);
    }
    
    public static TelephoneNumber buildTelephoneNumber(String countryCode, String areaCode, String number, String extensione, String isChanged) {
        TelephoneNumber phone = new TelephoneNumber();
        phone.setIsChanged(isChanged);
        phone.setAreaCode(buildJaggaerBasicValue(areaCode, isChanged));
        phone.setCountryCode(buildJaggaerBasicValue(countryCode, isChanged));
        phone.setExtension(buildJaggaerBasicValue(extensione, isChanged));
        phone.setNumber(buildJaggaerBasicValue(number, isChanged));
        return phone;
    }
    
    public static JaggaerBasicValue buildJaggaerBasicValue(String value) {
        return buildJaggaerBasicValue(value, null);
    }
    
    public static JaggaerBasicValue buildJaggaerBasicValue(boolean value, JaggaerBooleanToStringType stringType) {
        return buildJaggaerBasicValue(value, null, stringType);
    }
    
    public static JaggaerBasicValue buildJaggaerBasicValue(String value, String isChanged) {
        JaggaerBasicValue basic = new JaggaerBasicValue();
        basic.setValue(value);
        basic.setIsChanged(isChanged);
        return basic;
    }
    
    public static JaggaerBasicValue buildJaggaerBasicValue(boolean value, String isChanged, JaggaerBooleanToStringType stringType) {
        JaggaerBasicValue basic = new JaggaerBasicValue();
        basic.setValue(value ? stringType.true_string : stringType.false_string);
        basic.setIsChanged(isChanged);
        return basic;
    }
    
    public static BusinessUnitInternalName buildBusinessUnitInternalName(String internalName, String preferredForThisBusinessUnit) {
        return buildBusinessUnitInternalName(internalName, preferredForThisBusinessUnit, null);
    }
    
    public static BusinessUnitInternalName buildBusinessUnitInternalName(String internalName, String preferredForThisBusinessUnit, String isChanged) {
        BusinessUnitInternalName name = new BusinessUnitInternalName();
        name.setIsChanged(isChanged);
        name.setPreferredForThisBusinessUnit(preferredForThisBusinessUnit);
        name.setValue(internalName);
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
        return buildDisplayName(name, null);
    }
    
    public static DisplayName buildDisplayName(String name, String isChanged) {
        DisplayName displayName = new DisplayName();
        displayName.setIsChanged(isChanged);
        displayName.setValue(name);
        return displayName;
    }

}
