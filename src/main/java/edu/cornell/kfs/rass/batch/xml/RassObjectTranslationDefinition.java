package edu.cornell.kfs.rass.batch.xml;

import java.util.Map;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.krad.bo.BusinessObject;

public abstract class RassObjectTranslationDefinition<T, R extends BusinessObject> {

    private String documentTypeName;
    private String rootXmlObjectListPropertyName;
    private Map<String, String> propertyMappings;

    public String getDocumentTypeName() {
        return documentTypeName;
    }

    public void setDocumentTypeName(String documentTypeName) {
        this.documentTypeName = documentTypeName;
    }

    public String getRootXmlObjectListPropertyName() {
        return rootXmlObjectListPropertyName;
    }

    public void setRootXmlObjectListPropertyName(String rootXmlObjectListPropertyName) {
        this.rootXmlObjectListPropertyName = rootXmlObjectListPropertyName;
    }

    public Map<String, String> getPropertyMappings() {
        return propertyMappings;
    }

    public void setPropertyMappings(Map<String, String> propertyMappings) {
        this.propertyMappings = propertyMappings;
    }

    public void processCustomTranslationForBusinessObjectCreate(T xmlObject, R newBusinessObject) {
        // Do nothing by default.
    }

    public void processCustomTranslationForBusinessObjectEdit(T xmlObject, R oldBusinessObject, R newBusinessObject) {
        // Do nothing by default.
    }

    public String printObjectLabelAndKeys(T xmlObject) {
        return getObjectLabel() + KFSConstants.BLANK_SPACE + printPrimaryKeyValues(xmlObject);
    }

    public String printObjectLabelAndKeys(R businessObject) {
        return getObjectLabel() + KFSConstants.BLANK_SPACE + printPrimaryKeyValues(businessObject);
    }

    public String getObjectLabel() {
        return getBusinessObjectClass().getSimpleName();
    }

    public abstract Class<T> getXmlObjectClass();

    public abstract Class<R> getBusinessObjectClass();

    public abstract String printPrimaryKeyValues(T xmlObject);

    public abstract String printPrimaryKeyValues(R businessObject);

    public abstract R findExistingObject(T xmlObject);

}
