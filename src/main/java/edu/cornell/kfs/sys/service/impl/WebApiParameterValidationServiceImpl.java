package edu.cornell.kfs.sys.service.impl;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.datadictionary.validation.CharacterLevelValidationPattern;
import org.kuali.kfs.krad.datadictionary.validation.ValidationPattern;
import org.kuali.kfs.sys.KFSKeyConstants;

import edu.cornell.kfs.sys.service.WebApiParameterValidationService;
import edu.cornell.kfs.sys.util.WebApiParameter;

@SuppressWarnings("deprecation")
public class WebApiParameterValidationServiceImpl implements WebApiParameterValidationService {

    private DataDictionaryService dataDictionaryService;
    private ConfigurationService configurationService;

    @Override
    public List<String> validateParameters(final WebApiParameter... parameters) {
        Objects.requireNonNull(parameters, "parameters var-arg array cannot be null");
        return validateParameters(List.of(parameters));
    }

    @Override
    public List<String> validateParameters(final List<WebApiParameter> parameters) {
        Objects.requireNonNull(parameters, "parameters list cannot be null");
        Validate.isTrue(!parameters.isEmpty(), "parameters list cannot be empty");
        List<String> errors = parameters.stream()
                .flatMap(this::validateParameterValue)
                .collect(Collectors.toUnmodifiableList());
        return errors;
    }

    private Stream<String> validateParameterValue(final WebApiParameter parameter) {
        final Stream.Builder<String> errors = Stream.builder();
        Objects.requireNonNull(parameter, "Unexpected null WebApiParameter instance detected");
        final AttributeDefinition attribute = dataDictionaryService.getAttributeDefinition(
                parameter.getEntryName(), parameter.getAttributeName());
        Objects.requireNonNull(attribute,
                "Unexpected null data dictionary attribute for " + parameter);
        final String propertyValue = parameter.getValue();

        if (StringUtils.isBlank(propertyValue)) {
            if (parameter.isRequired()) {
                errors.add(createErrorMessage(KFSKeyConstants.ERROR_REQUIRED, attribute.getLabel()));
            }
            return errors.build();
        }

        final ValidationPattern validationPattern = attribute.getValidationPattern();
        Objects.requireNonNull(validationPattern, "Unexpected null validation pattern for " + parameter);
        final boolean patternEnforcesLength = doesValidationPatternEnforceLengthRequirements(validationPattern);
        final Integer maxLength = attribute.getMaxLength();
        Validate.validState(maxLength != null || patternEnforcesLength,
                "No attribute-level or pattern-level length checks exist for " + parameter);

        if (!patternEnforcesLength && propertyValue.length() > attribute.getMaxLength()) {
            errors.add(createErrorMessage(KFSKeyConstants.ERROR_MAX_LENGTH,
                    attribute.getLabel(), attribute.getMaxLength()));
        }

        if (!validationPattern.matches(propertyValue)) {
            final String messageKey = validationPattern.getValidationErrorMessageKey();
            final String[] stringParms = validationPattern.getValidationErrorMessageParameters(attribute.getLabel());
            final Object[] errorParms = Arrays.copyOf(stringParms, stringParms.length, Object[].class);
            errors.add(createErrorMessage(messageKey, errorParms));
        }

        return errors.build();
    }

    private boolean doesValidationPatternEnforceLengthRequirements(final ValidationPattern validationPattern) {
        if (validationPattern instanceof CharacterLevelValidationPattern) {
            final CharacterLevelValidationPattern charPattern = (CharacterLevelValidationPattern) validationPattern;
            return charPattern.getMaxLength() != -1 || charPattern.getExactLength() != -1;
        } else {
            return false;
        }
    }

    private String createErrorMessage(final String patternKey, final Object... arguments) {
        final String pattern = configurationService.getPropertyValueAsString(patternKey);
        return MessageFormat.format(pattern, arguments);
    }

    public void setDataDictionaryService(final DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
