package edu.cornell.kfs.sys.service;

import java.util.List;

import edu.cornell.kfs.sys.util.WebApiProperty;

/**
 * Shared helper service for validating simple data inputs on web service endpoints,
 * returning populated error message lists when the inputs don't adhere to the data requirements.
 * 
 * The default implementation uses the Data Dictionary to validate the inputs.
 */
public interface WebApiPropertyValidationService {

    List<String> validateProperties(final WebApiProperty... properties);

    List<String> validateProperties(final List<WebApiProperty> properties);

}
