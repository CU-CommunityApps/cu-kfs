package edu.cornell.kfs.krad.dao.impl;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.krad.dao.impl.MaintenanceDocumentDaoOjb;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.krad.dao.CuMaintenanceDocumentDao;

public class CuMaintenanceDocumentDaoOjb extends MaintenanceDocumentDaoOjb implements CuMaintenanceDocumentDao {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public String getAnyLockingDocumentNumber(final List<String> lockingRepresentations, final String documentNumber) {
        LOG.debug("getAnyLockingDocumentNumber, Checking for locking document(s)");
        if (CollectionUtils.isEmpty(lockingRepresentations)) {
            LOG.debug("getAnyLockingDocumentNumber, No lock representations specified, skipping search");
            return null;
        }
        LOG.debug("getAnyLockingDocumentNumber, Performing search with {} lock representations",
                lockingRepresentations::size);
        
        final Criteria criteria = new Criteria();
        criteria.addIn(KFSPropertyConstants.LOCKING_REPRESENTATION, lockingRepresentations);
        if (StringUtils.isNotBlank(documentNumber)) {
            criteria.addNotEqualTo(KRADPropertyConstants.DOCUMENT_NUMBER, documentNumber);
        }
        criteria.addSql(getDbPlatform().applyLimitSql(1));
        
        final QueryByCriteria query = QueryFactory.newQuery(MaintenanceLock.class, criteria);
        final MaintenanceLock maintenanceLock = (MaintenanceLock) getPersistenceBrokerTemplate().getObjectByQuery(query);
        final String lockingDocumentId = ObjectUtils.isNotNull(maintenanceLock) ? maintenanceLock.getDocumentNumber() : null;
        
        LOG.debug("getAnyLockingDocumentNumber, {}",
                () -> StringUtils.isNotBlank(lockingDocumentId)
                        ? "Found a matching lock owned by document " + lockingDocumentId
                        : "No matching locks found");
        
        return lockingDocumentId;
    }

}
