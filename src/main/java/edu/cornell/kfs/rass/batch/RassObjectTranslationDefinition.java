package edu.cornell.kfs.rass.batch;

import java.util.List;

import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;

import edu.cornell.kfs.rass.batch.xml.RassXmlObject;

public abstract class RassObjectTranslationDefinition<T extends RassXmlObject, R extends PersistableBusinessObject> extends RassBaseObjectTranslationDefinition<T,R> {

    private String documentTypeName;
    private String rootXmlObjectListPropertyName;

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

    public <T extends RassXmlObject, R extends PersistableBusinessObject> void makeBusinessObjectActiveIfApplicable(T xmlObject, R newBusinessObject) {
        if (newBusinessObject instanceof MutableInactivatable) {
            ((MutableInactivatable) newBusinessObject).setActive(true);
        }
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
    
    public boolean businessObjectCreateIsPermitted(T xmlObject) {
        return true;
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

    public abstract String printPrimaryKeyValues(T xmlObject);

    public abstract String printPrimaryKeyValues(R businessObject);

    public abstract String getKeyOfPrimaryObjectUpdateToWaitFor(T xmlObject);

    public abstract List<String> getKeysOfUpstreamObjectUpdatesToWaitFor(T xmlObject);

    public abstract R findExistingObject(T xmlObject);

    protected void refreshReferenceObject(PersistableBusinessObject businessObject, String propertyName) {
        businessObject.refreshReferenceObject(propertyName);
    }

}
