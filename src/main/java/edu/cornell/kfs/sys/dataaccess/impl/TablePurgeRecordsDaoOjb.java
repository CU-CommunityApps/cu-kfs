package edu.cornell.kfs.sys.dataaccess.impl;

import java.sql.Date;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.joda.time.DateTime;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.util.KfsDateUtils;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.sys.dataaccess.TablePurgeRecordsDao;

public abstract class TablePurgeRecordsDaoOjb extends PlatformAwareDaoBaseOjb implements TablePurgeRecordsDao {
    private static final Logger LOG = LogManager.getLogger(TablePurgeRecordsDaoOjb.class); 
    private BusinessObjectService businessObjectService;
    
    public abstract void purgeRecords(java.util.Date jobRunDate, int daysOld);
    
    protected abstract Criteria buildLookupCriteria(Date dateForPurge);
    
    protected void identifyAndRequestRecordsDeletion(Class classForDeleteQuery, Criteria lookupCriteria) {
        int numberOfRecordsBeingPurged = getPersistenceBrokerTemplate().getCount(QueryFactory.newQuery(classForDeleteQuery, lookupCriteria));
        LOG.info("identifyAndRequestRecordsDeletion: numberOfRecordsBeingPurged = " + numberOfRecordsBeingPurged);
        Collection toBePurgedRecords = getPersistenceBrokerTemplate().getCollectionByQuery(QueryFactory.newQuery(classForDeleteQuery, lookupCriteria));
        for (Object recordAsObject : toBePurgedRecords) {
            deleteRecordBasedOnBusinessObject((PersistableBusinessObjectBase)recordAsObject, recordAsObject.toString());
        }
    }
    
    @Transactional
    protected void deleteRecordBasedOnBusinessObject(PersistableBusinessObjectBase businessObjectClassToDelete, String purgeRecordingString) {
        getBusinessObjectService().delete(businessObjectClassToDelete);
        LOG.info("deleteRecordBasedOnBusinessObject: Purged record :: " + purgeRecordingString);
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

}
