package edu.cornell.kfs.krad.datadictionary.validation.fieldlevel;

import org.kuali.kfs.krad.datadictionary.validation.FieldLevelValidationPattern;

import edu.cornell.kfs.sys.CUKFSConstants;

/**
 * Validation pattern that allows for integer values only.
 * Unlike the "NumericValidation" helper bean, this implementation
 * allows for negative integers.
 */
@SuppressWarnings("deprecation")
public class IntegerValidationPattern extends FieldLevelValidationPattern {

    private static final long serialVersionUID = 8850022886274526658L;

    @Override
    protected String getPatternTypeName() {
        return CUKFSConstants.INTEGER_VALIDATION_PATTERN_TYPE;
    }

}
