package edu.cornell.kfs.rass.batch.xml;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.sys.KFSConstants;

public abstract class RassObjectTranslationDefinition<T, R extends PersistableBusinessObject> {

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

    public abstract List<Pair<Class<?>, String>> getListOfObjectUpdatesToWaitFor(T xmlObject);

    public abstract R findExistingObject(T xmlObject);

}
