package edu.cornell.kfs.rass.batch;

import org.kuali.kfs.krad.bo.PersistableBusinessObject;

public class TestAgencyTranslationDefinition extends AgencyTranslationDefinition {

    @Override
    protected void refreshReferenceObject(PersistableBusinessObject businessObject, String propertyName) {
        // Do nothing.
    }

}
