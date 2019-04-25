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
        return findParameterValue(CUPdpParameterConstants.CuPayeeAddressService.PAYER_NAME);
    }

    @Override
    public String findPayerAddressLine1() {
        return findParameterValue(CUPdpParameterConstants.CuPayeeAddressService.PAYER_ADDRESS_LINE1);
    }

    @Override
    public String findPayerAddressLine2() {
        return findParameterValue(CUPdpParameterConstants.CuPayeeAddressService.PAYER_ADDRESS_LINE2);
    }

    @Override
    public String findPayerCity() {
        return findParameterValue(CUPdpParameterConstants.CuPayeeAddressService.PAYER_CITY);
    }

    @Override
    public String findPayerState() {
        return findParameterValue(CUPdpParameterConstants.CuPayeeAddressService.PAYER_STATE);
    }

    @Override
    public String findPayerZipCode() {
        return findParameterValue(CUPdpParameterConstants.CuPayeeAddressService.PAYER_ZIP_CODE);
    }
    
    protected String findParameterValue(String parameterName) {
        String parameterValue = parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.PDP, 
                CUPdpParameterConstants.CuPayeeAddressService.COMPONENT_CODE, parameterName);
        if (LOG.isDebugEnabled()) {
            LOG.debug("findParameterValue, for parameter " + parameterName + " the value is " + parameterValue);
        }
        return parameterValue;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
