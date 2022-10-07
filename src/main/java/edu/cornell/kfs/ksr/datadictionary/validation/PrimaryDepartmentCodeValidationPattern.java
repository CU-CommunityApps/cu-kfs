package edu.cornell.kfs.ksr.datadictionary.validation;

import org.kuali.kfs.krad.datadictionary.validation.FieldLevelValidationPattern;

@SuppressWarnings("deprecation")
public class PrimaryDepartmentCodeValidationPattern extends FieldLevelValidationPattern {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getPatternTypeName() {
        return "primaryDepartmentCode";
    }

}
