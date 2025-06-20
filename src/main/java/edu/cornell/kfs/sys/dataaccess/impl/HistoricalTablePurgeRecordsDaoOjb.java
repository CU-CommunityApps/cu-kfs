package edu.cornell.kfs.sys.dataaccess.impl;

import java.sql.Date;
import java.time.LocalDate;
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
import org.joda.time.DateTime;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.util.KfsDateUtils;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.batch.service.HistoricalTableLookupCriteriaPurgeService;
import edu.cornell.kfs.sys.businessobject.HistoricalTableDetailsForPurge;
import edu.cornell.kfs.sys.businessobject.PurgeableBusinessObjectInterface;
import edu.cornell.kfs.sys.dataaccess.HistoricalTablePurgeRecordIdentifiersDao;
import edu.cornell.kfs.sys.dataaccess.HistoricalTablePurgeRecordsDao;

/**
 * CU Customization: Historical Purge Table Job : short lived batch job to clear backlog of Initiated documents.
 *
 */

public class HistoricalTablePurgeRecordsDaoOjb extends PlatformAwareDaoBaseOjb implements HistoricalTablePurgeRecordsDao {
    private static final Logger LOG = LogManager.getLogger(); 
    private BusinessObjectService businessObjectService;
    protected ParameterService parameterService;
    private HistoricalTablePurgeRecordIdentifiersDao historicalTablePurgeRecordIdentifiersDao;
    
    @Override
    public void purgeRecords(LocalDateTime jobRunDate, List<HistoricalTableDetailsForPurge> tableDetails) {
        int defaultDaysOld = retrieveDefaultDaysBeforePurgeParameterValue();
        
        for (HistoricalTableDetailsForPurge details : tableDetails) {
            int daysOldToUse = details.isUseDefaultDaysBeforePurgeParameter() ? defaultDaysOld : retrieveDaysBeforePurgeParameterValue(details);
            int databaseRowFetchCount = retrieveDatabaseRowFetchCountParameterValue(details);
            Date dateForPurge = getPurgeDate(jobRunDate, daysOldToUse);
            LOG.info("purgeRecords: daysOldToUse = {}    databaseRowFetchCount = {}    dateForPurge = {}", daysOldToUse, databaseRowFetchCount, dateForPurge);
            
            List<String> initiatedDocIdsToPurge = historicalTablePurgeRecordIdentifiersDao.obtainInitiatedDocumentIdsToPurge(dateForPurge, databaseRowFetchCount);
            
            for (String documentIdToPurge : initiatedDocIdsToPurge) {
                Criteria lookupCriteria = buildTablePurgeCriteria(details.getServiceImplForPurgeTableLookupCriteria(), documentIdToPurge);
                identifyAndRequestRecordsDeletion(details, lookupCriteria);
            }
        }
    }

    protected Criteria buildTablePurgeCriteria(HistoricalTableLookupCriteriaPurgeService serviceImplClassForPurgeTableLookupCriteria, String documentIdToPurge) {
        return serviceImplClassForPurgeTableLookupCriteria.buildLookupCriteria(documentIdToPurge);
    }
    
    protected void identifyAndRequestRecordsDeletion(HistoricalTableDetailsForPurge details, Criteria lookupCriteria) {
        Class<?> classForDeleteQuery = details.getBusinessObjectForRecordsTablePurge();
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
        LOG.info("deleteRecordBasedOnBusinessObject: Purged record :: {}", purgeRecordingString);
    }
    
    protected int retrieveDefaultDaysBeforePurgeParameterValue() {
        return retrieveParameterValue(
                CUKFSParameterKeyConstants.PurgeTablesParameterConstants.DEFAULT_NAME_SPACE_CODE, 
                CUKFSParameterKeyConstants.PurgeTablesParameterConstants.DEFAULT_COMPONENT, 
                CUKFSParameterKeyConstants.PurgeTablesParameterConstants.DEFAULT_PARAMETER_NAME);
    }
    
    protected int retrieveDaysBeforePurgeParameterValue(HistoricalTableDetailsForPurge details) {
        return retrieveParameterValue(details.getNameSpaceCode(), details.getComponent(), details.getParameterName());
    }
    
    protected int retrieveDatabaseRowFetchCountParameterValue(HistoricalTableDetailsForPurge details) {
        return retrieveParameterValue(details.getNameSpaceCode(), details.getComponent(), details.getFetchRowCountToPurgeParameterName());
    }
    
    protected int retrieveParameterValue(String nameSpaceCode, String component, String parameterName) {
        final String parameterValue = getParameterService().getParameterValueAsString(nameSpaceCode, component, parameterName);
        return new Integer(parameterValue);
    }
    
    protected Date getPurgeDate(LocalDateTime jobRunDate, int daysOld) {
        LocalDateTime computedPurgeDate = computeDateAsDaysOldFromJobRunDate(jobRunDate, daysOld);
        return KfsDateUtils.newDate(computedPurgeDate.getYear(), computedPurgeDate.getMonthValue(), computedPurgeDate.getDayOfMonth(), computedPurgeDate.getHour(), computedPurgeDate.getMinute(), computedPurgeDate.getSecond());
    }
    
    protected LocalDateTime computeDateAsDaysOldFromJobRunDate(LocalDateTime jobRunDate, int daysOld) {
        LOG.info("computeDateAsDaysOldFromJobRunDate: jobRunDate = {}   daysOld = {}", jobRunDate.toString(), daysOld);
       
        LocalDateTime dateForPurgeAsDateTime = jobRunDate.minusDays(daysOld);
        LOG.info("computeDateAsDaysOldFromJobRunDate: dateForPurgeAsDateTime = {}", dateForPurgeAsDateTime.toLocalDate().toString());
        return dateForPurgeAsDateTime;
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

    public HistoricalTablePurgeRecordIdentifiersDao getHistoricalTablePurgeRecordIdentifiersDao() {
        return historicalTablePurgeRecordIdentifiersDao;
    }

    public void setHistoricalTablePurgeRecordIdentifiersDao(
            HistoricalTablePurgeRecordIdentifiersDao historicalTablePurgeRecordIdentifiersDao) {
        this.historicalTablePurgeRecordIdentifiersDao = historicalTablePurgeRecordIdentifiersDao;
    }

}
