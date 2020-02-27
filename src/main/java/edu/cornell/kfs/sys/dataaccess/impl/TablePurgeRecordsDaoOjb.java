package edu.cornell.kfs.sys.dataaccess.impl;

import java.sql.Date;
import java.util.Collection;
import java.util.List;
import java.lang.String;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.joda.time.DateTime;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.util.KfsDateUtils;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.batch.service.TableLookupCriteriaPurgeService;
import edu.cornell.kfs.sys.businessobject.TableDetailsForPurge;
import edu.cornell.kfs.sys.dataaccess.TablePurgeRecordsDao;

public class TablePurgeRecordsDaoOjb extends PlatformAwareDaoBaseOjb implements TablePurgeRecordsDao {
    private static final Logger LOG = LogManager.getLogger(TablePurgeRecordsDaoOjb.class); 
    private BusinessObjectService businessObjectService;
    protected ParameterService parameterService;
    
    @Override
    public void purgeRecords(java.util.Date jobRunDate, List<TableDetailsForPurge> tableDetails) {
        int daysOldToUse;
        Date dateForPurge;
        Criteria lookupCriteria;
        int defaultDaysOld = retrieveDefaultDaysBeforePurgeParameterValue();
        for (TableDetailsForPurge details : tableDetails) {
            daysOldToUse = details.isUseDefaultDaysBeforePurgeParameter() ? defaultDaysOld : retrieveDaysBeforePurgeParameterValue(details.getNameSpaceCode(), details.getComponent(), details.getParameterName());
            dateForPurge = getPurgeDate(jobRunDate, daysOldToUse);
            lookupCriteria = requestTablePurgeCriteria(details.getServiceImplForPurgeTableLookupCriteria(), dateForPurge);
            identifyAndRequestRecordsDeletion(details.getBusinessObjectForRecordsTablePurge(), lookupCriteria);
        }
    }

    protected Criteria requestTablePurgeCriteria(String serviceImplClassForPurgeTableLookupCriteria, Date dateForPurge) {
        Criteria lookupQueryCriteria = null;
        try {
            Class<?> computedClass = Class.forName(serviceImplClassForPurgeTableLookupCriteria);
            Constructor<?> computedClassConstrctor = computedClass.getConstructor();
            Object serviceAsObject =  computedClassConstrctor.newInstance();
            TableLookupCriteriaPurgeService criteriaService = (TableLookupCriteriaPurgeService) serviceAsObject;
            lookupQueryCriteria =  criteriaService.buildLookupCriteria(dateForPurge);
        } catch (NoSuchMethodException nsme) {
            String errorMessage = new String("requestTablePurgeCriteria: Caught NoSuchMethodException attempting to construct lookup perge query criteria.");
            LOG.error(errorMessage + nsme.toString());
            throw new RuntimeException(errorMessage + nsme.toString());
            
        } catch (ClassNotFoundException cnfe) {
            String errorMessage = new String("requestTablePurgeCriteria: Caught ClassNotFoundException attempting to construct lookup perge query criteria.");
            LOG.error(errorMessage + cnfe.toString());
            throw new RuntimeException(errorMessage + cnfe.toString());
            
        } catch (InvocationTargetException ite) {
            String errorMessage = new String("requestTablePurgeCriteria: Caught InvocationTargetException attempting to construct lookup perge query criteria.");
            LOG.error(errorMessage + ite.toString());
            throw new RuntimeException(errorMessage + ite.toString());
            
        } catch (InstantiationException ite) {
            String errorMessage = new String("requestTablePurgeCriteria: Caught InstantiationException attempting to construct lookup perge query criteria.");
            LOG.error(errorMessage + ite.toString());
            throw new RuntimeException(errorMessage + ite.toString());
            
        } catch (IllegalAccessException iae) {
            String errorMessage = new String("requestTablePurgeCriteria: Caught IllegalAccessException attempting to construct lookup perge query criteria.");
            LOG.error(errorMessage + iae.toString());
            throw new RuntimeException(errorMessage + iae.toString());
            
        }
        return lookupQueryCriteria;
    }
    
    protected void identifyAndRequestRecordsDeletion(Class<?> classForDeleteQuery, Criteria lookupCriteria) {
        int numberOfRecordsBeingPurged = getPersistenceBrokerTemplate().getCount(QueryFactory.newQuery(classForDeleteQuery, lookupCriteria));
        LOG.info("identifyAndRequestRecordsDeletion: numberOfRecordsBeingPurged = " + numberOfRecordsBeingPurged);
        Collection<?> toBePurgedRecords = getPersistenceBrokerTemplate().getCollectionByQuery(QueryFactory.newQuery(classForDeleteQuery, lookupCriteria));
        for (Object recordAsObject : toBePurgedRecords) {
            deleteRecordBasedOnBusinessObject((PersistableBusinessObjectBase)recordAsObject, recordAsObject.toString());
        }
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
    
    protected Date getPurgeDate(java.util.Date jobRunDate, int daysOld) {
        java.util.Date computedPurgeDate = computeDateAsDaysOldFromJobRunDate(jobRunDate, daysOld);
        return KfsDateUtils.convertToSqlDate(computedPurgeDate);
    }
    
    protected java.util.Date computeDateAsDaysOldFromJobRunDate(java.util.Date jobRunDate, int daysOld) {
        LOG.info("computeDateAsDaysOldFromJobRunDate: jobRunDate = " + jobRunDate.toString() + " daysOld = " + daysOld);
        DateTime jobRunDateAsDateTime = new DateTime(jobRunDate.getTime());
        DateTime dateForPurgeAsDateTime = jobRunDateAsDateTime.minusDays(daysOld);
        LOG.info("computeDateAsDaysOldFromJobRunDate: dateForPurgeAsDateTime = " + dateForPurgeAsDateTime.toLocalDate().toString());
        return dateForPurgeAsDateTime.toDate();
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

}
