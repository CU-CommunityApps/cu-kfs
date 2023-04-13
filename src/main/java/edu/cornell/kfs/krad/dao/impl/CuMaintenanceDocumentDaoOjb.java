package edu.cornell.kfs.krad.dao.impl;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.krad.dao.impl.MaintenanceDocumentDaoOjb;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.krad.dao.CuMaintenanceDocumentDao;
import edu.cornell.kfs.krad.util.MaintenanceLockUtils;

public class CuMaintenanceDocumentDaoOjb extends MaintenanceDocumentDaoOjb implements CuMaintenanceDocumentDao {

    @Override
    public String getAnyLockingDocumentNumber(List<String> lockingRepresentations, String documentNumber) {
        return MaintenanceLockUtils.doChunkedSearchForAnyLockingDocumentNumber(lockingRepresentations,
                documentNumber, this::queryForAnyLockingDocumentNumber);
    }

    private String queryForAnyLockingDocumentNumber(List<String> lockingRepresentations, String documentNumber) {
        Criteria criteria = new Criteria();
        criteria.addIn(KFSPropertyConstants.LOCKING_REPRESENTATION, lockingRepresentations);
        if (StringUtils.isNotBlank(documentNumber)) {
            criteria.addNotEqualTo(KRADPropertyConstants.DOCUMENT_NUMBER, documentNumber);
        }
        
        QueryByCriteria query = QueryFactory.newQuery(MaintenanceLock.class, criteria);
        Collection<?> maintenanceLocks = getPersistenceBrokerTemplate().getCollectionByQuery(query);
        if (CollectionUtils.isNotEmpty(maintenanceLocks)) {
            MaintenanceLock maintenanceLock = (MaintenanceLock) maintenanceLocks.iterator().next();
            return maintenanceLock.getDocumentNumber();
        } else {
            return null;
        }
    }

}
