package edu.cornell.kfs.rass.batch;

import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

public abstract class RassParameterMapingValueConverterBase extends RassValueConverterBase {
    private ParameterService parameterService;

    @Override
    public Object convert(Class<? extends PersistableBusinessObject> businessObjectClass, RassPropertyDefinition propertyMapping, Object propertyValue) {
        return getMappedProposalPurposeValue((String) propertyValue);
    }

    protected String getMappedProposalPurposeValue(String value) {
        String parameterName = getParameterName();
        String mappedValue = parameterService.getSubParameterValueAsString(KFSConstants.OptionalModuleNamespaces.CONTRACTS_AND_GRANTS, KfsParameterConstants.BATCH_COMPONENT,
                parameterName, value);
        
        if (getLog().isDebugEnabled()) {
            getLog().debug("getMappedProposalPurposeValue, value from RASS: " + value + " paremter name: " + parameterName + " mapped value to return: " + mappedValue);
        }
        
        if (mappedValue == null) {
            getLog().error("getMappedProposalPurposeValue, there is no oarameter mapping for the value " + value + " so returning original value");
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
