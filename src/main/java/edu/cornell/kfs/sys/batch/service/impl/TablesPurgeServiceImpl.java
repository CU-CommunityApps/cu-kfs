package edu.cornell.kfs.sys.batch.service.impl;

import java.util.Date;

import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.sys.batch.service.TablesPurgeService;

public abstract class TablesPurgeServiceImpl implements TablesPurgeService {
    
    protected static final String DEFAULT_NAME_SPACE_CODE = "KFS-SYS";
    protected static final String DEFAULT_COMPONENT = "PurgeTablesStep";
    protected static final String DEFAULT_PARAMETER_NAME = "DEFAULT_NUMBER_OF_DAYS_OLD";
    
    protected ParameterService parameterService;
    
    public abstract void purgeRecords(Date jobRunDate); 
    
    protected int retrieveDefaultDaysBeforePurgeParameterValue() {
        return retrieveDaysBeforePurgeParameterValue(DEFAULT_NAME_SPACE_CODE, DEFAULT_COMPONENT, DEFAULT_PARAMETER_NAME);
    }
    
    protected int retrieveDaysBeforePurgeParameterValue(String nameSpaceCode, String component, String parameterName) {
        final String parameterValue = getParameterService().getParameterValueAsString(nameSpaceCode, component, parameterName);
        return new Integer(parameterValue);
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
