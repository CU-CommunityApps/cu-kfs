package edu.cornell.kfs.rass.batch;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.rass.batch.xml.RassXmlObject;

public abstract class RassBaseObjectTranslationDefinition <T extends RassXmlObject, R extends PersistableBusinessObject> {
    private List<RassPropertyDefinition> propertyMappings;  

    public List<RassPropertyDefinition> getPropertyMappings() {
        return propertyMappings;
    }

    public void setPropertyMappings(List<RassPropertyDefinition> propertyMappings) {
        this.propertyMappings = propertyMappings;
    }
    
    public List<RassPropertyDefinition> getPropertyMappingsApplicableForAction(String maintenanceAction) {
        if (StringUtils.equalsIgnoreCase(maintenanceAction, KRADConstants.MAINTENANCE_NEW_ACTION)) {
            return propertyMappings;
        } else if (StringUtils.equalsIgnoreCase(maintenanceAction, KRADConstants.MAINTENANCE_EDIT_ACTION)) {
            return propertyMappings.stream()
                    .filter(propertyMapping -> !propertyMapping.isSkipForObjectEdit())
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("Action '" + maintenanceAction + "' is not supported by the RASS Job");
        }
    }
    
    public abstract Class<T> getXmlObjectClass();

    public abstract Class<R> getBusinessObjectClass();
}
