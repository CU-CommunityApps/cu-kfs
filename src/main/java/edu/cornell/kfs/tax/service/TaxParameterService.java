package edu.cornell.kfs.tax.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.kuali.kfs.coreservice.impl.parameter.Parameter;

public interface TaxParameterService {

    Parameter getParameter(final String componentCode, final String parameterName);

    String getParameterValueAsString(final String componentCode, final String parameterName);

    Set<String> getParameterValuesAsString(final String componentCode, final String parameterName);

    Map<String, String> getSubParameters(final String componentCode, final String parameterName);

    Map<String, String> getValueToKeyMapFromParameterContainingMultiValueEntries(
            final String componentCode, final String parameterName);

    Map<String, Set<String>> getValuesMapFromParameterContainingDuplicateSubParameterKeys(
            final String componentCode, final String parameterName);

    Map<String, List<Pattern>> getRegexMapFromParameterContainingDuplicateSubParameterKeys(
            final String componentCode, final String parameterName);

}
