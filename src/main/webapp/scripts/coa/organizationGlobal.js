// Updates org name sub-collection field on the OrganizationGlobal document.
function loadOrganizationName( orgField ) {
    var orgCodeFieldName = orgField.name;
    var elPrefix = findElPrefix( orgCodeFieldName );
    var coaCodeFieldName = elPrefix + chartCodeSuffix;
    var orgNameFieldName = elPrefix + ".organization.organizationName";
    var chartCode = dwr.util.getValue( coaCodeFieldName );
    var orgCode = dwr.util.getValue( orgCodeFieldName );

    // Setup callbacks to update the org name accordingly, based on success/failure or if-found status.
    var dwrResult = {
        callback:function(data) {
            if ( data != null && typeof data == "object" ) {    
                setRecipientValue( orgNameFieldName, data.organizationName );
            } else {
                setRecipientValue( orgNameFieldName, wrapError( "org not found" ), true );
            }
        },
        errorHandler:function( errorMessage ) { 
            setRecipientValue( orgNameFieldName, wrapError( "error looking up org"), true);
        }
    };

    // Attempt to retrieve the org info.
    OrganizationService.getByPrimaryId( chartCode, orgCode, dwrResult );
}

// Clears out city name and state code if both zip and country are blank; otherwise, calls the appropriate org document script function.
function updateOrgGlobalLocationByPostalCode( postalCodeField, callbackFunction ) {
    var postalCode = getElementValue( postalCodeField.name );
    var postalCountryCode = getElementValue( findElPrefix( postalCodeField.name ) + ".organizationCountryCode" );
    
    if ( postalCode == "" && postalCountryCode == "" ) {
        clearRecipients( "document.newMaintainableObject.organizationCityName" );
        clearRecipients( "document.newMaintainableObject.organizationStateCode" );
    } else {
        updateLocationByPostalCode( postalCodeField, callbackFunction );
    }
}

// Clears out city name and state code if both zip and country are blank; otherwise, calls the appropriate org document script function.
function updateOrgGlobalLocationByCountryCode( countryCodeField, callbackFunction ) {
    var postalCountryCode = getElementValue( countryCodeField.name );
    var postalCode = getElementValue( findElPrefix( countryCodeField.name ) + ".organizationZipCode" );
    
    if ( postalCode == "" && postalCountryCode == "" ) {
        clearRecipients( "document.newMaintainableObject.organizationCityName" );
        clearRecipients( "document.newMaintainableObject.organizationStateCode" );
    } else {
        updateLocationByCountryCode( countryCodeField, callbackFunction );
    }
}
