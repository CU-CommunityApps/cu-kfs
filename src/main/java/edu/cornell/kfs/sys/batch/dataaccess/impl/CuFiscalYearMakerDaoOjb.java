package edu.cornell.kfs.sys.batch.dataaccess.impl;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.batch.dataaccess.FiscalYearMaker;
import org.kuali.kfs.sys.batch.dataaccess.impl.FiscalYearMakersDaoOjb;
import org.kuali.kfs.krad.bo.DataObjectRelationship;
import org.kuali.kfs.krad.service.PersistenceStructureService;

public class CuFiscalYearMakerDaoOjb extends FiscalYearMakersDaoOjb {
	private static final Logger LOG = LogManager.getLogger(CuFiscalYearMakerDaoOjb.class);

    protected PersistenceStructureService persistenceStructureService;

    /**
     * Deletes the new year records for the given fiscal year maker. If the object has an extension it first deletes the data
     * in the extension table.
     * 
     * @see org.kuali.kfs.sys.batch.dataaccess.impl.FiscalYearMakersDaoOjb#deleteNewYearRows(java.lang.Integer,
     *      org.kuali.kfs.sys.batch.dataaccess.FiscalYearMaker)
     */
    @Override
    public void deleteNewYearRows(Integer baseYear, FiscalYearMaker objectFiscalYearMaker) {
        // Check if an extension exists and delete extension records first
        if (persistenceStructureService.hasReference(objectFiscalYearMaker.getBusinessObjectClass(), KFSPropertyConstants.EXTENSION)) {
            Map<String, DataObjectRelationship> relationships = persistenceStructureService.getRelationshipMetadata(objectFiscalYearMaker.getBusinessObjectClass(), KFSPropertyConstants.EXTENSION);
            Class extensionClass = relationships.get(KFSPropertyConstants.EXTENSION).getRelatedClass();

            LOG.info(String.format("\ndeleting %s for %d", extensionClass.getName(), baseYear + 1));

            QueryByCriteria queryExtension = new QueryByCriteria(extensionClass, objectFiscalYearMaker.createDeleteCriteria(baseYear));

            getPersistenceBrokerTemplate().deleteByQuery(queryExtension);
            getPersistenceBrokerTemplate().clearCache();
        }

        // Delete the main objects now
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("\ndeleting %s for target year(s)", objectFiscalYearMaker.getBusinessObjectClass().getName()));
        }

        QueryByCriteria queryID = new QueryByCriteria(objectFiscalYearMaker.getBusinessObjectClass(), objectFiscalYearMaker.createDeleteCriteria(baseYear));
        getPersistenceBrokerTemplate().deleteByQuery(queryID);

        getPersistenceBrokerTemplate().clearCache();
    }

    /**
     * Gets the persistenceStructureService.
     * 
     * @return persistenceStructureService
     */
    public PersistenceStructureService getPersistenceStructureService() {
        return persistenceStructureService;
    }

    /**
     * Sets the persistenceStructureService.
     * 
     * @param persistenceStructureService
     */
    public void setPersistenceStructureService(PersistenceStructureService persistenceStructureService) {
        this.persistenceStructureService = persistenceStructureService;
    }

}
