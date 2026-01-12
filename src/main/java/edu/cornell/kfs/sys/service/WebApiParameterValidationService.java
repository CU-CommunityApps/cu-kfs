package edu.cornell.kfs.sys.service;

import java.util.List;

import edu.cornell.kfs.sys.util.WebApiParameter;

/**
 * Shared helper service for validating simple data inputs on web service endpoints,
 * returning populated error message lists when the inputs don't adhere to the data requirements.
 * 
 * The default implementation uses the Data Dictionary to validate the inputs.
 */
public interface WebApiParameterValidationService {

    List<String> validateParameters(final WebApiParameter... parameters);

    List<String> validateParameters(final List<WebApiParameter> parameters);

}
