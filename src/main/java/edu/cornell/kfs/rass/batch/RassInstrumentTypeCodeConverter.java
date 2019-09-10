package edu.cornell.kfs.rass.batch;

import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

import edu.cornell.kfs.rass.RassConstants;
import edu.cornell.kfs.rass.RassParameterConstants;

public class RassInstrumentTypeCodeConverter extends RassValueConverterBase {

    private ParameterService parameterService;

    @Override
    public Object convert(Class<? extends PersistableBusinessObject> businessObjectClass, RassPropertyDefinition propertyMapping, Object propertyValue) {
        return getMappedInstrumentTypeCodeValue((String) propertyValue);
    }

    protected String getMappedInstrumentTypeCodeValue(String value) {
        return parameterService.getSubParameterValueAsString(RassConstants.RASS_MODULE, KfsParameterConstants.BATCH_COMPONENT,
                RassParameterConstants.INSTRUMENT_TYPE_CODE_MAPPINGS, value);
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
}
