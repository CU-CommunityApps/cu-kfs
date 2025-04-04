package edu.cornell.kfs.tax.batch;

import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;

public class TaxParameterClusionHelper {
    private final String parameterName;
    private final boolean inclusionsOverridePriorExclusions;
    private final TaxStatType taxStatToUpdateForUndeterminedClusion;
    private final TaxStatType taxStatToUpdateIfIncluded;
    private final TaxStatType taxStatToUpdateIfExcluded;

    public TaxParameterClusionHelper(final String parameterName,
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

    public String getParameterName() {
        return parameterName;
    }

    public boolean isInclusionsOverridePriorExclusions() {
        return inclusionsOverridePriorExclusions;
    }

    public TaxStatType getTaxStatToUpdateForUndeterminedClusion() {
        return taxStatToUpdateForUndeterminedClusion;
    }

    public TaxStatType getTaxStatToUpdateIfIncluded() {
        return taxStatToUpdateIfIncluded;
    }

    public TaxStatType getTaxStatToUpdateIfExcluded() {
        return taxStatToUpdateIfExcluded;
    }

}
