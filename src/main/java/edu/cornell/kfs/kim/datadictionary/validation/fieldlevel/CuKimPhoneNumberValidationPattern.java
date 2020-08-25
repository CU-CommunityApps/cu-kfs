package edu.cornell.kfs.kim.datadictionary.validation.fieldlevel;

import org.kuali.kfs.kns.datadictionary.validation.fieldlevel.PhoneNumberValidationPattern;

import edu.cornell.kfs.kim.CuKimConstants;

public class CuKimPhoneNumberValidationPattern extends PhoneNumberValidationPattern {

    @Override
    protected String getPatternTypeName() {
        return CuKimConstants.PHONE_NUMBER_PATTERN_TYPE_NAME;
    }

}
