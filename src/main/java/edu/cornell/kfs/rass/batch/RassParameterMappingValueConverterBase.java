package edu.cornell.kfs.rass.batch;

import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

public abstract class RassParameterMappingValueConverterBase extends RassValueConverterBase {
    private ParameterService parameterService;

    @Override
    public Object convert(Class<? extends PersistableBusinessObject> businessObjectClass, RassPropertyDefinition propertyMapping, Object propertyValue) {
        return getMappedValue((String) propertyValue);
    }

    protected String getMappedValue(String value) {
        String parameterName = getParameterName();
        String mappedValue = parameterService.getSubParameterValueAsString(KFSConstants.OptionalModuleNamespaces.CONTRACTS_AND_GRANTS, KfsParameterConstants.BATCH_COMPONENT,
                parameterName, value);
        
        if (getLog().isDebugEnabled()) {
            getLog().debug("getMappedValue, value from RASS: " + value + " parameter name: " + parameterName + " mapped value to return: " + mappedValue);
        }
        
        if (mappedValue == null) {
            getLog().error("getMappedValue, there is no parameter mapping for the value " + value + " so returning original value");
            mappedValue = value;
        }
        
        return mappedValue;
    }
    
    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
    
    public abstract String getParameterName(); 
    
    public abstract Logger getLog();
}
