package edu.cornell.kfs.rass.batch;

import java.util.List;

import org.kuali.kfs.krad.bo.PersistableBusinessObject;

import edu.cornell.kfs.rass.batch.xml.RassXmlObject;

public abstract class RassBaseObjectTranslationDefinition <T extends RassXmlObject, R extends PersistableBusinessObject> {
    private List<RassPropertyDefinition> propertyMappings;  

    public List<RassPropertyDefinition> getPropertyMappings() {
        return propertyMappings;
    }

    public void setPropertyMappings(List<RassPropertyDefinition> propertyMappings) {
        this.propertyMappings = propertyMappings;
    }
    
    public abstract Class<T> getXmlObjectClass();

    public abstract Class<R> getBusinessObjectClass();
}
