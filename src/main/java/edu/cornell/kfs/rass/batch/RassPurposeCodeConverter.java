package edu.cornell.kfs.rass.batch;

import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

import edu.cornell.kfs.module.cg.CuCGParameterConstants;

public class RassPurposeCodeConverter extends RassValueConverterBase {

    private ParameterService parameterService;

    @Override
    public Object convert(Class<? extends PersistableBusinessObject> businessObjectClass, String propertyName, Object propertyValue) {
        return getMappedProposalPurposeValue((String) propertyValue);
    }

    protected String getMappedProposalPurposeValue(String value) {
        return parameterService.getSubParameterValueAsString(KFSConstants.OptionalModuleNamespaces.CONTRACTS_AND_GRANTS, KfsParameterConstants.BATCH_COMPONENT,
                CuCGParameterConstants.PROPOSAL_PURPOSE_MAPPINGS, value);
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
}
