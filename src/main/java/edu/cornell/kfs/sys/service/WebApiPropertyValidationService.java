package edu.cornell.kfs.sys.service;

import java.util.List;

import edu.cornell.kfs.sys.util.WebApiProperty;

public interface WebApiPropertyValidationService {

    List<String> validateProperties(final WebApiProperty... properties);

    List<String> validateProperties(final List<WebApiProperty> properties);

}
