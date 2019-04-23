package edu.cornell.kfs.rass.batch;

import java.util.List;

import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.rass.batch.xml.RassXmlObject;

public abstract class RassObjectTranslationDefinition<T extends RassXmlObject, R extends PersistableBusinessObject> {

    private String documentTypeName;
    private String rootXmlObjectListPropertyName;
    private List<RassPropertyDefinition> propertyMappings;

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

    public List<RassPropertyDefinition> getPropertyMappings() {
        return propertyMappings;
    }

    public void setPropertyMappings(List<RassPropertyDefinition> propertyMappings) {
        this.propertyMappings = propertyMappings;
    }

    public void processCustomTranslationForBusinessObjectCreate(T xmlObject, R newBusinessObject) {
        // Do nothing by default.
    }

    public void processCustomTranslationForBusinessObjectEdit(T xmlObject, R oldBusinessObject, R newBusinessObject) {
        // Do nothing by default.
    }

    public boolean businessObjectEditIsPermitted(T xmlObject, R oldBusinessObject) {
        return true;
    }

    public boolean otherCustomObjectPropertiesHaveDifferences(R oldBusinessObject, R newBusinessObject) {
        return false;
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

    public abstract List<String> getKeysOfObjectUpdatesToWaitFor(T xmlObject);

    public abstract R findExistingObject(T xmlObject);

    protected void refreshReferenceObject(PersistableBusinessObject businessObject, String propertyName) {
        businessObject.refreshReferenceObject(propertyName);
    }

}
