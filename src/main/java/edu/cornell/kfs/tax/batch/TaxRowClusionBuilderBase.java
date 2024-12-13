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

public class TaxRowClusionBuilderBase {

    public enum ClusionResult {
        INCLUDE,
        EXCLUDE,
        UNKNOWN;
    }

    protected final TaxStatisticsHandler statsHandler;
    protected final TaxParameterService taxParameterService;
    protected final String parameterComponent;

    protected ClusionResult resultOfPreviousCheck;
    protected ClusionResult cumulativeResult;

    protected TaxRowClusionBuilderBase(final TaxStatisticsHandler statsHandler,
            final TaxParameterService taxParameterService, final String parameterComponent) {
        Validate.notNull(statsHandler, "statsHandler cannot be null");
        Validate.notNull(taxParameterService, "taxParameterService cannot be null");
        Validate.notBlank(parameterComponent, "parameterComponent cannot be blank");
        this.statsHandler = statsHandler;
        this.taxParameterService = taxParameterService;
        this.parameterComponent = parameterComponent;
        this.resultOfPreviousCheck = ClusionResult.UNKNOWN;
        this.cumulativeResult = ClusionResult.UNKNOWN;
    }

    public ClusionResult getResultOfPreviousCheck() {
        return resultOfPreviousCheck;
    }

    public ClusionResult getCumulativeResult() {
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
            resultOfPreviousCheck = ClusionResult.EXCLUDE;
            cumulativeResult = ClusionResult.EXCLUDE;
            statsHandler.increment(statToUpdateIfExcluded);
        } else {
            resultOfPreviousCheck = ClusionResult.INCLUDE;
        }
    }



    protected void checkAgainstSubValues(final ParameterCheckHelper helper, final String key, final String value) {
        final boolean treatAsInclusions = getParameterAllowDisallowFlag(helper.parameterName);
        final Map<String, Set<String>> subValuesMap =
                taxParameterService.getValuesMapFromParameterContainingDuplicateSubParameterKeys(
                        parameterComponent, helper.parameterName);
        final Set<String> subValues = subValuesMap.get(StringUtils.defaultString(key));
        final ClusionResult result;
        if (subValues == null) {
            result = ClusionResult.UNKNOWN;
        } else {
            final boolean matchFound = subValues.contains(value);
            result = (matchFound == treatAsInclusions) ? ClusionResult.INCLUDE : ClusionResult.EXCLUDE;
        }
        handleOutcomeOfClusionCheck(helper, result);
    }

    protected void checkAgainstPatterns(final ParameterCheckHelper helper, final String key, final String value) {
        final boolean treatAsInclusions = getParameterAllowDisallowFlag(helper.parameterName);
        final Map<String, List<Pattern>> patternsMap =
                taxParameterService.getRegexMapFromParameterContainingDuplicateSubParameterKeys(
                        parameterComponent, helper.parameterName);
        final List<Pattern> patterns = patternsMap.get(StringUtils.defaultString(key));
        final ClusionResult result;
        if (patterns == null) {
            result = ClusionResult.UNKNOWN;
        } else {
            final boolean matchFound = patterns.stream().anyMatch(pattern -> pattern.matcher(value).matches());
            result = (matchFound == treatAsInclusions) ? ClusionResult.INCLUDE : ClusionResult.EXCLUDE;
        }
        handleOutcomeOfClusionCheck(helper, result);
    }

    protected void handleOutcomeOfClusionCheck(final ParameterCheckHelper helper, final ClusionResult result) {
        if (result == ClusionResult.INCLUDE) {
            if (helper.inclusionsOverridePriorExclusions) {
                cumulativeResult = ClusionResult.INCLUDE;
            }
            statsHandler.increment(helper.taxStatToUpdateIfIncluded);
        } else if (result == ClusionResult.EXCLUDE) {
            cumulativeResult = ClusionResult.EXCLUDE;
            statsHandler.increment(helper.taxStatToUpdateIfExcluded);
        } else {
            statsHandler.increment(helper.taxStatToUpdateForUndeterminedClusion);
        }
    }



    protected boolean multiValueParameterContains(final String parameterName, final String value) {
        return getMultiValueParameter(parameterName).contains(value);
    }

    protected Set<String> getMultiValueParameter(final String parameterName) {
        return taxParameterService.getParameterValuesAsString(parameterComponent, parameterName);
    }

    protected boolean getParameterAllowDisallowFlag(final String parameterName) {
        final Parameter parameter = taxParameterService.getParameter(parameterComponent, parameterName);
        return parameter.getEvaluationOperator() == EvaluationOperator.ALLOW;
    }

    protected String getParameter(final String parameterName) {
        return taxParameterService.getParameterValueAsString(parameterComponent, parameterName);
    }



    public static final class ParameterCheckHelper {
        public final String parameterName;
        public final boolean inclusionsOverridePriorExclusions;
        public final TaxStatType taxStatToUpdateForUndeterminedClusion;
        public final TaxStatType taxStatToUpdateIfIncluded;
        public final TaxStatType taxStatToUpdateIfExcluded;

        public ParameterCheckHelper(final String parameterName,
                final boolean inclusionsOverridePriorExclusions,
                final TaxStatType taxStatToUpdateForUndeterminedClusion,
                final TaxStatType taxStatToUpdateIfIncluded, final TaxStatType taxStatToUpdateIfExcluded) {
            Validate.notBlank(parameterName, "parameterName cannot be blank");
            Validate.notNull(taxStatToUpdateForUndeterminedClusion, "Unknown-clusion tax stat cannot be null");
            Validate.notNull(taxStatToUpdateIfIncluded, "Inclusion tax stat cannot be null");
            Validate.notNull(taxStatToUpdateIfExcluded, "Exclusion tax stat cannot be null");
            this.parameterName = parameterName;
            this.inclusionsOverridePriorExclusions = inclusionsOverridePriorExclusions;
            this.taxStatToUpdateForUndeterminedClusion = taxStatToUpdateForUndeterminedClusion;
            this.taxStatToUpdateIfIncluded = taxStatToUpdateIfIncluded;
            this.taxStatToUpdateIfExcluded = taxStatToUpdateIfExcluded;
        }
    }

}
