package edu.cornell.kfs.tax.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.coreservice.impl.parameter.Parameter;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.cache.annotation.Cacheable;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.service.TaxParameterService;

public class TaxParameterServiceImpl implements TaxParameterService {

    private ParameterService parameterService;

    @Override
    public Parameter getParameter(final String componentCode, final String parameterName) {
        validateParams(componentCode, parameterName);
        return parameterService.getParameter(CUTaxConstants.TAX_NAMESPACE, componentCode, parameterName);
    }

    @Override
    public String getParameterValueAsString(final String componentCode, final String parameterName) {
        validateParams(componentCode, parameterName);
        return parameterService.getParameterValueAsString(CUTaxConstants.TAX_NAMESPACE, componentCode, parameterName);
    }

    @Override
    @Cacheable(cacheNames = Parameter.CACHE_NAME, key = "'{CU_getParameterValuesAsString}'+#p0+','+#p1")
    public Set<String> getParameterValuesAsString(final String componentCode, final String parameterName) {
        validateParams(componentCode, parameterName);
        final Collection<String> parameterValues = parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, componentCode, parameterName);
        return Set.copyOf(parameterValues);
    }

    @Override
    @Cacheable(cacheNames = Parameter.CACHE_NAME, key = "'{CU_getSubParameters}'+#p0+','+#p1")
    public Map<String, String> getSubParameters(final String componentCode, final String parameterName) {
        validateParams(componentCode, parameterName);
        final Collection<String> parameterValues = parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, componentCode, parameterName);
        final Stream.Builder<Map.Entry<String, String>> subParameters = Stream.builder();
        for (final String keyValuePair : parameterValues) {
            final String key = StringUtils.substringBefore(keyValuePair, CUKFSConstants.EQUALS_SIGN);
            final String value = StringUtils.substringAfter(keyValuePair, CUKFSConstants.EQUALS_SIGN);
            subParameters.add(Map.entry(key, value));
        }
        return subParameters.build().collect(
                Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    @Cacheable(cacheNames = Parameter.CACHE_NAME,
            key = "'{CU_getValueToKeyMapFromParameterContainingMultiValueEntries}'+#p0+','+#p1")
    public Map<String, String> getValueToKeyMapFromParameterContainingMultiValueEntries(
            final String componentCode, final String parameterName) {
        validateParams(componentCode, parameterName);
        final Collection<String> parameterValues = parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, componentCode, parameterName);
        final Stream.Builder<Map.Entry<String, String>> valueToKeyEntries = Stream.builder();
        for (final String keyAndMultiValuePair : parameterValues) {
            final String key = StringUtils.substringBefore(keyAndMultiValuePair, CUKFSConstants.EQUALS_SIGN);
            final String commaDelimitedValues = StringUtils.substringAfter(
                    keyAndMultiValuePair, CUKFSConstants.EQUALS_SIGN);
            final String[] values = StringUtils.split(commaDelimitedValues, KFSConstants.COMMA);
            for (final String value : values) {
                valueToKeyEntries.add(Map.entry(value, key));
            }
        }
        return valueToKeyEntries.build()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    

    @Override
    @Cacheable(cacheNames = Parameter.CACHE_NAME,
            key = "'{CU_getValuesMapFromParameterContainingDuplicateSubParameterKeys}'+#p0+','+#p1")
    public Map<String, Set<String>> getValuesMapFromParameterContainingDuplicateSubParameterKeys(
            final String componentCode, final String parameterName) {
        validateParams(componentCode, parameterName);
        final Collection<String> parameterValues = parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, componentCode, parameterName);
        final Map<String, Stream.Builder<String>> mappedValueSets = new HashMap<>();
        for (final String keyValuePair : parameterValues) {
            final String key = StringUtils.substringBefore(keyValuePair, CUKFSConstants.EQUALS_SIGN);
            final String value = StringUtils.substringAfter(keyValuePair, CUKFSConstants.EQUALS_SIGN);
            mappedValueSets.computeIfAbsent(key, sameKey -> Stream.builder())
                    .add(value);
        }
        return mappedValueSets.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().build().collect(Collectors.toUnmodifiableSet())));
    }

    @Override
    @Cacheable(cacheNames = Parameter.CACHE_NAME,
            key = "'{CU_getRegexMapFromParameterContainingDuplicateSubParameterKeys}'+#p0+','+#p1")
    public Map<String, List<Pattern>> getRegexMapFromParameterContainingDuplicateSubParameterKeys(
            final String componentCode, final String parameterName) {
        validateParams(componentCode, parameterName);
        final Collection<String> parameterValues = parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, componentCode, parameterName);
        final Map<String, Stream.Builder<Pattern>> mappedRegexes = new HashMap<>();
        for (final String keyAndRegexPair : parameterValues) {
            final String key = StringUtils.substringBefore(keyAndRegexPair, CUKFSConstants.EQUALS_SIGN);
            final String regex = StringUtils.substringAfter(keyAndRegexPair, CUKFSConstants.EQUALS_SIGN);
            final String convertedRegex = convertRegex(regex);
            final Pattern pattern = Pattern.compile(convertedRegex, Pattern.CASE_INSENSITIVE);
            mappedRegexes.computeIfAbsent(key, sameKey -> Stream.builder())
                    .add(pattern);
        }
        return mappedRegexes.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().build().collect(Collectors.toUnmodifiableList())));
    }

    private String convertRegex(final String regex) {
        String convertedRegex = regex;
        if (!StringUtils.startsWithAny(regex, CUKFSConstants.CARET, KFSConstants.PERCENTAGE_SIGN)) {
            convertedRegex = CUKFSConstants.CARET + convertedRegex;
        }
        if (!StringUtils.endsWithAny(regex, CUKFSConstants.DOLLAR_SIGN, KFSConstants.PERCENTAGE_SIGN)) {
            convertedRegex += CUKFSConstants.DOLLAR_SIGN;
        }
        return StringUtils.replaceEach(convertedRegex,
                new String[] {KFSConstants.PERCENTAGE_SIGN,
                        CUKFSConstants.UNDERSCORE},
                new String[] {CUKFSConstants.REGEX_WILDCARD + CUKFSConstants.REGEX_ZERO_OR_MORE_SYMBOL,
                        CUKFSConstants.REGEX_WILDCARD});
    }

    private void validateParams(final String componentCode, final String parameterName) {
        Validate.notBlank(componentCode, "component cannot be blank");
        Validate.notBlank(parameterName, "parameterName cannot be blank");
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
