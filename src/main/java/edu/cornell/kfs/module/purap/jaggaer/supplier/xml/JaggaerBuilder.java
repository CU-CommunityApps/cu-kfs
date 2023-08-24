package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import edu.cornell.kfs.module.purap.JaggaerConstants.JaggaerBooleanToStringType;

public class JaggaerBuilder {
    
    public static ErpNumber buildErpNumber(String value) {
        ErpNumber erp = new ErpNumber();
        erp.setValue(value);
        return erp;
    }
    
    public static Name buildName(String nameString) {
        Name name = new Name();
        name.setValue(nameString);
        return name;
    }
    
    public static Active buildActive(String activeString) {
        Active active = new Active();
        active.setValue(activeString);
        return active;
    }
    
    public static Active buildActive(boolean isActive, JaggaerBooleanToStringType jaggaerBooleanToStringType) {
        return buildActive(buildActiveStringFromBoolean(isActive, jaggaerBooleanToStringType));
    }

    protected static String buildActiveStringFromBoolean(boolean isActive, JaggaerBooleanToStringType jaggaerBooleanToStringType) {
        String activeString = isActive ? jaggaerBooleanToStringType.true_string : jaggaerBooleanToStringType.false_string;
        return activeString;
    }
    
    public static AddressLine buildAddressLine(String addressString) {
        AddressLine line = new AddressLine();
        line.setValue(addressString);
        return line;
    }
    
    public static City buildCity(String cityString) {
        City city = new City();
        city.setValue(cityString);
        return city;
    }
    
    public static State buildState(String stateString) {
        State state = new State();
        state.setValue(stateString);
        return state;
    }
    
    public static PostalCode buildPostalCode(String postalString) {
        PostalCode postalCode = new PostalCode();
        postalCode.setValue(postalString);
        return postalCode;
    }
    
    public static IsoCountryCode buildIsoCountryCode(String countryCode) {
        IsoCountryCode country = new IsoCountryCode();
        country.setValue(countryCode);
        return country;
    }
    
    public static JaggaerBasicValue buildJaggaerBasicValue(String value) {
        JaggaerBasicValue basic = new JaggaerBasicValue();
        basic.setValue(value);
        return basic;
    }
    
    public static JaggaerBasicValue buildJaggaerBasicValue(boolean value, JaggaerBooleanToStringType stringType) {
        return buildJaggaerBasicValue(buildActiveStringFromBoolean(value, stringType));
    }
    
}
