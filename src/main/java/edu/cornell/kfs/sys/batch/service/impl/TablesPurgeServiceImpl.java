package edu.cornell.kfs.sys.batch.service.impl;

import java.util.Date;

import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.concur.dataaccess.ConcurEventNotificationDao;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.batch.service.TablesPurgeService;

public class TablesPurgeServiceImpl implements TablesPurgeService {
    protected ParameterService parameterService;
    protected ConcurEventNotificationDao concurEventNotificationDao;
    
    public void purgeRecords(Date jobRunDate) {
        getConcurEventNotificationDao().purgeRecords(jobRunDate, retrieveDefaultDaysBeforePurgeParameterValue());
    }
    
    protected int retrieveDefaultDaysBeforePurgeParameterValue() {
        return retrieveDaysBeforePurgeParameterValue(
                CUKFSParameterKeyConstants.PurgeTablesParameterConstants.DEFAULT_NAME_SPACE_CODE, 
                CUKFSParameterKeyConstants.PurgeTablesParameterConstants.DEFAULT_COMPONENT, 
                CUKFSParameterKeyConstants.PurgeTablesParameterConstants.DEFAULT_PARAMETER_NAME);
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

    public ConcurEventNotificationDao getConcurEventNotificationDao() {
        return concurEventNotificationDao;
    }

    public void setConcurEventNotificationDao(ConcurEventNotificationDao concurEventNotificationDao) {
        this.concurEventNotificationDao = concurEventNotificationDao;
    }

}
