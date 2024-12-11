package edu.cornell.kfs.tax.batch;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;

public class TaxRowClusionBuilderBase {

    protected final TaxStatisticsHandler statsHandler;
    protected final ParameterService parameterService;
    protected final String parameterComponent;

    protected boolean resultOfPreviousCheck;
    protected boolean cumulativeResult;

    protected TaxRowClusionBuilderBase(final TaxStatisticsHandler statsHandler,
            final ParameterService parameterService, final String parameterComponent) {
        Validate.notNull(statsHandler, "statsHandler cannot be null");
        Validate.notNull(parameterService, "parameterService cannot be null");
        Validate.notBlank(parameterComponent, "parameterComponent cannot be blank");
        this.statsHandler = statsHandler;
        this.parameterService = parameterService;
        this.parameterComponent = parameterComponent;
        this.resultOfPreviousCheck = true;
        this.cumulativeResult = true;
    }

    public boolean getResultOfPreviousCheck() {
        return resultOfPreviousCheck;
    }

    public boolean getCumulativeResult() {
        return cumulativeResult;
    }

    protected void checkAgainstExclusions(final String parameterName, final String value,
            final TaxStatType statToUpdateIfExcluded) {
        handleOutcomeOfExclusionCheck(
                multiValueParameterContains(parameterName, value), statToUpdateIfExcluded);
    }

    protected void checkAgainstExcludedPrefixes(final String parameterName,
            final String value, final TaxStatType statToUpdateIfExcluded) {
        final Collection<String> prefixes = getMultiValueParameter(parameterName);
        handleOutcomeOfExclusionCheck(
                prefixes.stream().anyMatch(prefix -> StringUtils.startsWithIgnoreCase(value, prefix)),
                statToUpdateIfExcluded);
    }

    protected void handleOutcomeOfExclusionCheck(
            final boolean shouldExclude, final TaxStatType statToUpdateIfExcluded) {
        if (shouldExclude) {
            resultOfPreviousCheck = false;
            cumulativeResult = false;
            statsHandler.increment(statToUpdateIfExcluded);
        } else {
            resultOfPreviousCheck = true;
        }
    }

    protected boolean multiValueParameterContains(final String parameterName, final String value) {
        return getMultiValueParameter(parameterName).contains(value);
    }

    protected Collection<String> getMultiValueParameter(final String parameterName) {
        return parameterService.getParameterValuesAsString(CUTaxConstants.TAX_NAMESPACE,
                parameterComponent, parameterName);
    }

}
