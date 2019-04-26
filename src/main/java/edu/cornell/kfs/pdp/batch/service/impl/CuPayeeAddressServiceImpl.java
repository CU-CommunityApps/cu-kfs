package edu.cornell.kfs.pdp.batch.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.pdp.CUPdpParameterConstants;
import edu.cornell.kfs.pdp.batch.service.CuPayeeAddressService;

public class CuPayeeAddressServiceImpl implements CuPayeeAddressService {
    private static final Logger LOG = LogManager.getLogger(CuPayeeAddressServiceImpl.class);
    
    protected ParameterService parameterService;

    @Override
    public String findPayerName() {
        return findParameterValue(CUPdpParameterConstants.CuPayeeAddressService.PAYER_NAME_PARAMETER);
    }

    @Override
    public String findPayerAddressLine1() {
        return findParameterValue(CUPdpParameterConstants.CuPayeeAddressService.PAYER_ADDRESS_LINE1_PARAMETER);
    }

    @Override
    public String findPayerAddressLine2() {
        return findParameterValue(CUPdpParameterConstants.CuPayeeAddressService.PAYER_ADDRESS_LINE2_PARAMETER);
    }

    @Override
    public String findPayerCity() {
        return findParameterValue(CUPdpParameterConstants.CuPayeeAddressService.PAYER_CITY_PARAMETER);
    }

    @Override
    public String findPayerState() {
        return findParameterValue(CUPdpParameterConstants.CuPayeeAddressService.PAYER_STATE_PARAMETER);
    }

    @Override
    public String findPayerZipCode() {
        return findParameterValue(CUPdpParameterConstants.CuPayeeAddressService.PAYER_ZIP_CODE_PARAMETER);
    }
    
    protected String findParameterValue(String parameterName) {
        String parameterValue = parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.PDP, 
                CUPdpParameterConstants.CuPayeeAddressService.CU_PAYEE_ADDRESS_SERVICE_COMPONENT, parameterName);
        if (LOG.isDebugEnabled()) {
            LOG.debug("findParameterValue, for parameter " + parameterName + " the value is " + parameterValue);
        }
        return parameterValue;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
