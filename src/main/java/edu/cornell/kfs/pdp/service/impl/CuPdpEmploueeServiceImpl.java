package edu.cornell.kfs.pdp.service.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.pdp.businessobject.PaymentFileLoad;

import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.pdp.service.CuPdpEmploueeService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class CuPdpEmploueeServiceImpl implements CuPdpEmploueeService {
    
    protected ParameterService parameterService;
    
    @Override
    public boolean shouldProcessPayeeAsEmployee(PaymentFileLoad paymentFile) {
        String chartCode = getConcurParameterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_LOCATION);
        String subUnitCode = getConcurParameterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_SUB_UNIT);
        String unitCode = getConcurParameterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_UNIT);
        return StringUtils.equalsIgnoreCase(paymentFile.getCustomer().getUnitCode(), unitCode) &&
                StringUtils.equalsIgnoreCase(paymentFile.getCustomer().getSubUnitCode(), subUnitCode) &&
                StringUtils.equalsIgnoreCase(paymentFile.getCustomer().getChartCode(), chartCode);
    }
    
    public String getConcurParameterValue(String parameterName) {
        String parameterValue = parameterService.getParameterValueAsString(CUKFSConstants.ParameterNamespaces.CONCUR, 
                CUKFSParameterKeyConstants.ALL_COMPONENTS, parameterName);
        return parameterValue;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
