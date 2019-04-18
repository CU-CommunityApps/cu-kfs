package edu.cornell.kfs.rass.batch.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.module.cg.businessobject.Agency;

import edu.cornell.kfs.module.cg.businessobject.AgencyExtendedAttribute;
import edu.cornell.kfs.rass.RassTestConstants;
import edu.cornell.kfs.rass.batch.RassXmlObjectResult;
import edu.cornell.kfs.rass.batch.xml.RassObjectTranslationDefinition;

public class TestRassServiceImpl extends RassServiceImpl {

    @Override
    protected <T, R extends PersistableBusinessObject> RassXmlObjectResult processObject(
            T xmlObject, RassObjectTranslationDefinition<T, R> objectDefinition, PendingDocumentTracker documentTracker) {
        if (StringUtils.equals(RassTestConstants.ERROR_OBJECT_KEY, objectDefinition.printPrimaryKeyValues(xmlObject))) {
            throw new RuntimeException("Forced error for " + objectDefinition.printObjectLabelAndKeys(xmlObject));
        }
        return super.processObject(xmlObject, objectDefinition, documentTracker);
    }

    @Override
    protected <R extends PersistableBusinessObject> R createMinimalObject(Class<R> businessObjectClass) {
        R businessObject = super.createMinimalObject(businessObjectClass);
        if (businessObject instanceof Agency) {
            businessObject.setExtension(new AgencyExtendedAttribute());
        }
        return businessObject;
    }

    
    
}