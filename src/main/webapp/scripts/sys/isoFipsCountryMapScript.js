/**
 * CU Generic ISO/FIPS Country modification
 */
const ISO_COUNTRY_CODE_FIELD = 'document.newMaintainableObject.isoCountryCode';
const ISO_COUNTRY_NAME_FIELD = 'document.newMaintainableObject.isoCountry.name.div';
const FIPS_COUNTRY_CODE_FIELD = 'document.newMaintainableObject.fipsCountryCode';
const FIPS_COUNTRY_NAME_FIELD = 'document.newMaintainableObject.fipsCountry.name.div';
const EMPTY_STRING = '';
var previousFipsCountryCode = "";
var previousIsoCountryCode = "";

function onblur_lookupFipsCountry(fipsCountryCodeField) {
    var primaryKeyValue = ensurePrimaryKeyIsUppercase(fipsCountryCodeField);
    if (primaryKeyValue === null || primaryKeyValue === '') {
        clearCountryName(FIPS_COUNTRY_NAME_FIELD);
    } else {
        setFieldToSpecifiedValue(fipsCountryCodeField, primaryKeyValue);
        previousFipsCountryCode = primaryKeyValue;
        updateCountryName(primaryKeyValue, FIPS_COUNTRY_NAME_FIELD);
    }
}

function clearCountryName(countryNameFieldName) {
    dwr.util.setValue(countryNameFieldName, EMPTY_STRING)
}

function setFieldToSpecifiedValue(fieldName, fieldValue) {
    dwr.util.setValue(fieldName, fieldValue)
}

function ensurePrimaryKeyIsUppercase(countryCodeField) {
    return dwr.util.getValue(countryCodeField).trim().toUpperCase();
}

function updateCountryName(primaryKeyValue, targetFieldName) {   
    if (primaryKeyValue === '') {
        clearCountryName(targetFieldName);
    } else {
        var dwrReply = {
                 callback:function(data) {
                    if (data === null || data.includes("NOT FOUND")) {
                        setRecipientValue(targetFieldName, wrapError("Country Name not found"), true);
                    } else {
                        setFieldToSpecifiedValue(targetFieldName, data);
                    } },
                    errorHandler:function(errorMessage ) {
                        setRecipientValue(targetFieldName, wrapError("Country Name errorhandler"), true); 
                    }
        };
        CountryService.findCountryNameByCountryCode(primaryKeyValue, dwrReply);
    }
}

function onblur_lookupIsoCountry(isoCountryCodeField) {
    var primaryKeyValue = ensurePrimaryKeyIsUppercase(isoCountryCodeField);
    if (primaryKeyValue === null || primaryKeyValue === '') {
        clearCountryName(ISO_COUNTRY_NAME_FIELD);
    } else {
        setFieldToSpecifiedValue(isoCountryCodeField, primaryKeyValue);
        previousIsoCountryCode = primaryKeyValue;
        updateIsoCountryName(primaryKeyValue, ISO_COUNTRY_NAME_FIELD);
    }
}

function updateIsoCountryName(primaryKeyValue, targetFieldName) {   
    if (primaryKeyValue === '') {
        clearCountryName(targetFieldName);
    } else {
        var dwrReply = {
                 callback:function(data) {
                    if (data === null|| data.includes("NOT FOUND")) {
                        setRecipientValue(targetFieldName, wrapError("ISOCountry Name not found"), true);
                    } else {
                        setFieldToSpecifiedValue(targetFieldName, data);
                    } },
                    errorHandler:function(errorMessage ) {
                        setRecipientValue(targetFieldName, wrapError("ISOCountry Name errorhandler"), true); 
                    }
        };
        ISOCountryService.findISOCountryNameByCountryCode(primaryKeyValue, dwrReply);
    }
}
