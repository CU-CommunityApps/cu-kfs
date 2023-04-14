package edu.cornell.kfs.krad.dao.impl;

import java.util.List;

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
import edu.cornell.kfs.krad.util.MaintenanceLockUtils;

public class CuMaintenanceDocumentDaoOjb extends MaintenanceDocumentDaoOjb implements CuMaintenanceDocumentDao {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public String getAnyLockingDocumentNumber(List<String> lockingRepresentations, String documentNumber) {
        LOG.debug("getAnyLockingDocumentNumber, Checking for locking document(s)");
        return MaintenanceLockUtils.doChunkedSearchForAnyLockingDocumentNumber(lockingRepresentations,
                documentNumber, this::queryForAnyLockingDocumentNumber);
    }

    private String queryForAnyLockingDocumentNumber(List<String> lockingRepresentations, String documentNumber) {
        Criteria criteria = new Criteria();
        criteria.addIn(KFSPropertyConstants.LOCKING_REPRESENTATION, lockingRepresentations);
        if (StringUtils.isNotBlank(documentNumber)) {
            criteria.addNotEqualTo(KRADPropertyConstants.DOCUMENT_NUMBER, documentNumber);
        }
        criteria.addSql(getDbPlatform().applyLimitSql(1));
        
        QueryByCriteria query = QueryFactory.newQuery(MaintenanceLock.class, criteria);
        MaintenanceLock maintenanceLock = (MaintenanceLock) getPersistenceBrokerTemplate().getObjectByQuery(query);
        return ObjectUtils.isNotNull(maintenanceLock)
                ? maintenanceLock.getDocumentNumber()
                : null;
    }

}
