package edu.cornell.kfs.rass.batch;

import org.kuali.kfs.krad.bo.PersistableBusinessObject;

public class TestAwardTranslationDefinition extends AwardTranslationDefinition {

    @Override
    protected void refreshReferenceObject(PersistableBusinessObject businessObject, String propertyName) {
        // Do nothing.
    }

}
