package edu.cornell.kfs.tax.batch;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.coreservice.api.parameter.EvaluationOperator;
import org.kuali.kfs.coreservice.impl.parameter.Parameter;

import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;
import edu.cornell.kfs.tax.service.TaxParameterService;

public abstract class TaxRowClusionBuilderBase {

    protected final TaxStatisticsHandler statsHandler;
    protected final TaxParameterService taxParameterService;
    protected final String parameterComponent;
    protected final String transactionDocType;

    protected TaxRowClusionResult resultOfPreviousCheck;
    protected TaxRowClusionResult cumulativeResult;

    protected TaxRowClusionBuilderBase(final TaxStatisticsHandler statsHandler,
            final TaxParameterService taxParameterService, final String parameterComponent,
            final String transactionDocType) {
        Validate.notNull(statsHandler, "statsHandler cannot be null");
        Validate.notNull(taxParameterService, "taxParameterService cannot be null");
        Validate.notBlank(parameterComponent, "parameterComponent cannot be blank");
        this.statsHandler = statsHandler;
        this.taxParameterService = taxParameterService;
        this.parameterComponent = parameterComponent;
        this.transactionDocType = transactionDocType;
        this.resultOfPreviousCheck = TaxRowClusionResult.UNKNOWN;
        this.cumulativeResult = TaxRowClusionResult.UNKNOWN;
    }

    public TaxRowClusionResult getResultOfPreviousCheck() {
        return resultOfPreviousCheck;
    }

    public TaxRowClusionResult getCumulativeResult() {
        return cumulativeResult;
    }

    protected void checkAgainstExclusions(final String parameterName, final String value,
            final TaxStatType statToUpdateIfExcluded) {
        handleOutcomeOfExclusionCheck(
                multiValueParameterContains(parameterName, value), statToUpdateIfExcluded);
    }

    protected void checkAgainstExcludedPrefixes(final String parameterName,
            final String value, final TaxStatType statToUpdateIfExcluded) {
        final Set<String> prefixes = getMultiValueParameter(parameterName);
        handleOutcomeOfExclusionCheck(startsWithAnyIgnoreCase(value, prefixes), statToUpdateIfExcluded);
    }

    protected void checkAgainstExcludedPrefixes(final String parameterName,
            final Stream<String> values, final TaxStatType statToUpdateIfExcluded) {
        final Set<String> prefixes = getMultiValueParameter(parameterName);
        handleOutcomeOfExclusionCheck(
                values.anyMatch(value -> startsWithAnyIgnoreCase(value, prefixes)),
                statToUpdateIfExcluded);
    }

    protected boolean startsWithAnyIgnoreCase(final String value, final Set<String> prefixes) {
        return prefixes.stream().anyMatch(prefix -> StringUtils.startsWithIgnoreCase(value, prefix));
    }

    protected void handleOutcomeOfExclusionCheck(
            final boolean shouldExclude, final TaxStatType statToUpdateIfExcluded) {
        if (shouldExclude) {
            resultOfPreviousCheck = TaxRowClusionResult.EXCLUDE;
            cumulativeResult = TaxRowClusionResult.EXCLUDE;
            statsHandler.increment(statToUpdateIfExcluded, transactionDocType);
        } else {
            resultOfPreviousCheck = TaxRowClusionResult.INCLUDE;
        }
    }

    protected void checkAgainstSubValues(final TaxParameterClusionHelper helper, final String key,
            final String value) {
        final boolean treatAsInclusions = getParameterAllowDisallowFlag(helper.getParameterName());
        final Map<String, Set<String>> subValuesMap =
                taxParameterService.getValuesMapFromParameterContainingDuplicateSubParameterKeys(
                        parameterComponent, helper.getParameterName());
        final Set<String> subValues = subValuesMap.get(StringUtils.defaultString(key));
        final TaxRowClusionResult result;
        if (subValues == null) {
            result = TaxRowClusionResult.UNKNOWN;
        } else {
            final boolean matchFound = subValues.contains(value);
            result = (matchFound == treatAsInclusions) ? TaxRowClusionResult.INCLUDE : TaxRowClusionResult.EXCLUDE;
        }
        handleOutcomeOfClusionCheck(helper, result);
    }

    protected void checkAgainstPatterns(final TaxParameterClusionHelper helper, final String key, final String value) {
        final boolean treatAsInclusions = getParameterAllowDisallowFlag(helper.getParameterName());
        final Map<String, List<Pattern>> patternsMap =
                taxParameterService.getRegexMapFromParameterContainingDuplicateSubParameterKeys(
                        parameterComponent, helper.getParameterName());
        final List<Pattern> patterns = patternsMap.get(StringUtils.defaultString(key));
        final TaxRowClusionResult result;
        if (patterns == null) {
            result = TaxRowClusionResult.UNKNOWN;
        } else {
            final boolean matchFound = patterns.stream().anyMatch(pattern -> pattern.matcher(value).matches());
            result = (matchFound == treatAsInclusions) ? TaxRowClusionResult.INCLUDE : TaxRowClusionResult.EXCLUDE;
        }
        handleOutcomeOfClusionCheck(helper, result);
    }

    protected void handleOutcomeOfClusionCheck(final TaxParameterClusionHelper helper,
            final TaxRowClusionResult result) {
        if (result == TaxRowClusionResult.INCLUDE) {
            if (helper.isInclusionsOverridePriorExclusions()) {
                cumulativeResult = TaxRowClusionResult.INCLUDE;
            }
            statsHandler.increment(helper.getTaxStatToUpdateIfIncluded(), transactionDocType);
        } else if (result == TaxRowClusionResult.EXCLUDE) {
            cumulativeResult = TaxRowClusionResult.EXCLUDE;
            statsHandler.increment(helper.getTaxStatToUpdateIfExcluded(), transactionDocType);
        } else {
            statsHandler.increment(helper.getTaxStatToUpdateForUndeterminedClusion(), transactionDocType);
        }
    }

    protected boolean multiValueParameterContains(final String parameterName, final String value) {
        return getMultiValueParameter(parameterName).contains(value);
    }

    protected Set<String> getMultiValueParameter(final String parameterName) {
        return taxParameterService.getParameterValuesSetAsString(parameterComponent, parameterName);
    }

    protected boolean getParameterAllowDisallowFlag(final String parameterName) {
        final Parameter parameter = taxParameterService.getParameter(parameterComponent, parameterName);
        return parameter.getEvaluationOperator() == EvaluationOperator.ALLOW;
    }

    protected String getParameter(final String parameterName) {
        return taxParameterService.getParameterValueAsString(parameterComponent, parameterName);
    }

}
