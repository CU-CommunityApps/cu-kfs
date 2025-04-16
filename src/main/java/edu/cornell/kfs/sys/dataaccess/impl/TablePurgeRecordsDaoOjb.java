package edu.cornell.kfs.sys.dataaccess.impl;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.lang.String;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.batch.service.TableLookupCriteriaPurgeService;
import edu.cornell.kfs.sys.businessobject.PurgeableBusinessObjectInterface;
import edu.cornell.kfs.sys.businessobject.TableDetailsForPurge;
import edu.cornell.kfs.sys.dataaccess.TablePurgeRecordsDao;

public class TablePurgeRecordsDaoOjb extends PlatformAwareDaoBaseOjb implements TablePurgeRecordsDao {
    private static final Logger LOG = LogManager.getLogger(TablePurgeRecordsDaoOjb.class); 
    private BusinessObjectService businessObjectService;
    protected ParameterService parameterService;
    protected DateTimeService dateTimeService;
    
    @Override
    public void purgeRecords(LocalDateTime jobRunLocalDateTime, List<TableDetailsForPurge> tableDetails) {
        int daysOldToUse;
        Date dateForPurge;
        Criteria lookupCriteria;
        int defaultDaysOld = retrieveDefaultDaysBeforePurgeParameterValue();
        for (TableDetailsForPurge details : tableDetails) {
            daysOldToUse = details.isUseDefaultDaysBeforePurgeParameter() ? defaultDaysOld : retrieveDaysBeforePurgeParameterValue(details.getNameSpaceCode(), details.getComponent(), details.getParameterName());
            dateForPurge = getPurgeDate(jobRunLocalDateTime, daysOldToUse);
            lookupCriteria = buildTablePurgeCriteria(details.getServiceImplForPurgeTableLookupCriteria(), dateForPurge);
            identifyAndRequestRecordsDeletion(details, lookupCriteria);
        }
    }

    protected Criteria buildTablePurgeCriteria(TableLookupCriteriaPurgeService serviceImplClassForPurgeTableLookupCriteria, Date dateForPurge) {
        return serviceImplClassForPurgeTableLookupCriteria.buildLookupCriteria(dateForPurge);
    }
    
    protected void identifyAndRequestRecordsDeletion(TableDetailsForPurge details, Criteria lookupCriteria) {
        Class<?> classForDeleteQuery = details.getBusinessObjectForRecordsTablePurge();
        int numberOfRecordsBeingPurged = getPersistenceBrokerTemplate().getCount(QueryFactory.newQuery(classForDeleteQuery, lookupCriteria));
        LOG.info("identifyAndRequestRecordsDeletion: numberOfRecordsBeingPurged from table " + details.getTableToPurge() + " = " + numberOfRecordsBeingPurged);
        Collection<?> toBePurgedRecords = getPersistenceBrokerTemplate().getCollectionByQuery(QueryFactory.newQuery(classForDeleteQuery, lookupCriteria));
        for (Object recordAsObject : toBePurgedRecords) {
            deleteRecordBasedOnBusinessObject((PersistableBusinessObjectBase)recordAsObject, buildPurgeRecordingString(recordAsObject));
        }
    }
    
    protected String buildPurgeRecordingString(Object recordAsObject) {
        String purgeRecordingString = ToStringBuilder.reflectionToString(recordAsObject, ToStringStyle.MULTI_LINE_STYLE);
        if (recordAsObject instanceof PurgeableBusinessObjectInterface) {
            PurgeableBusinessObjectInterface purgeable = (PurgeableBusinessObjectInterface) recordAsObject;
            purgeRecordingString = purgeable.buildPurgeableRecordingString();
        }
        return purgeRecordingString;
    }
    
    @Transactional
    protected void deleteRecordBasedOnBusinessObject(PersistableBusinessObjectBase businessObjectClassToDelete, String purgeRecordingString) {
        getBusinessObjectService().delete(businessObjectClassToDelete);
        LOG.info("deleteRecordBasedOnBusinessObject: Purged record :: " + purgeRecordingString);
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
    
    protected Date getPurgeDate(LocalDateTime jobRunLocalDateTime, int daysOld) {
        LocalDateTime computedPurgeLocalDateTime = computeDateTimeAsDaysOldFromJobRunDateTime(jobRunLocalDateTime, daysOld);
        return getDateTimeService().getSqlDate(computedPurgeLocalDateTime);
    }
    
    protected LocalDateTime computeDateTimeAsDaysOldFromJobRunDateTime(LocalDateTime jobRunLocalDateTime, int daysOld) {
        LOG.info("computeDateAsDaysOldFromJobRunDate: jobRunLocalDateTime = " + jobRunLocalDateTime.toString() + " daysOld = " + daysOld);
        LocalDateTime dateForPurgeAsLocalDateTime = jobRunLocalDateTime.minusDays(daysOld);
        LOG.info("computeDateAsDaysOldFromJobRunDate: dateForPurgeAsDateTime = " + dateForPurgeAsLocalDateTime.toString());
        return dateForPurgeAsLocalDateTime;
    }
    
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
    
    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
