package edu.cornell.kfs.coa.dataaccess.impl;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.coa.dataaccess.impl.AccountDelegateGlobalDaoOjb;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;

import edu.cornell.kfs.coa.dataaccess.CuAccountDelegateGlobalDao;
import edu.cornell.kfs.core.framework.persistence.ojb.QueryByPreparedSQL;
import edu.cornell.kfs.krad.util.MaintenanceLockUtils;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;

public class CuAccountDelegateGlobalDaoOjb extends AccountDelegateGlobalDaoOjb implements CuAccountDelegateGlobalDao {

    @Override
    public String getAnyLockingDocumentNumber(List<String> lockingRepresentations, String documentNumber) {
        return MaintenanceLockUtils.doChunkedSearchForAnyLockingDocumentNumber(lockingRepresentations,
                documentNumber, this::queryForAnyLockingDocumentNumber);
    }

    private String queryForAnyLockingDocumentNumber(List<String> lockingRepresentations, String documentNumber) {
        QueryByPreparedSQL lockRepSubquery = createPatternMatchSubquery(lockingRepresentations, documentNumber);
        
        Criteria criteria = new Criteria();
        criteria.addIn(CUKFSPropertyConstants.LOCK_ID, lockRepSubquery);
        
        QueryByCriteria query = QueryFactory.newQuery(MaintenanceLock.class, criteria);
        Collection<?> maintenanceLocks = getPersistenceBrokerTemplate().getCollectionByQuery(query);
        if (CollectionUtils.isNotEmpty(maintenanceLocks)) {
            MaintenanceLock maintenanceLock = (MaintenanceLock) maintenanceLocks.iterator().next();
            return maintenanceLock.getDocumentNumber();
        } else {
            return null;
        }
    }

    private QueryByPreparedSQL createPatternMatchSubquery(List<String> lockingRepresentations, String documentNumber) {
        CuSqlQuery sqlQuery = CuSqlQuery.of(
                createWithClauseForSearchPatterns(lockingRepresentations),
                "SELECT LCK.MAINT_LOCK_ID FROM KFS.KRNS_MAINT_LOCK_T LCK ",
                "WHERE ", createAndedDocIdCriteria(documentNumber),
                "EXISTS (SELECT 1 FROM SEARCH_PATTERNS WHERE LCK.MAINT_LOCK_REP_TXT LIKE SEARCH_PATTERNS.LIKE_EXPR)"
        );
        return new QueryByPreparedSQL(MaintenanceLock.class, sqlQuery);
    }

    private CuSqlChunk createWithClauseForSearchPatterns(List<String> lockingRepresentations) {
        CuSqlChunk caseBlock = new CuSqlChunk();
        caseBlock.append("CASE LEVEL");
        int level = 1;
        for (String lockingRepresentation : lockingRepresentations) {
            String convertedLockingRepresentation = convertForLikeCriteria(lockingRepresentation);
            caseBlock.append(" WHEN ", String.valueOf(level),
                    " THEN ", CuSqlChunk.forParameter(convertedLockingRepresentation));
            level++;
        }
        caseBlock.append(" END ");
        
        return CuSqlChunk.of(
                "WITH SEARCH_PATTERNS AS (",
                        "SELECT ", caseBlock, "\"LIKE_EXPR\" FROM DUAL ",
                        "CONNECT BY LEVEL <= ", String.valueOf(lockingRepresentations.size()),
                ") "
        );
    }

    private CuSqlChunk createAndedDocIdCriteria(String documentNumber) {
        if (StringUtils.isNotBlank(documentNumber)) {
            return CuSqlChunk.of("LCK.DOC_HDR_ID <> ", CuSqlChunk.forParameter(documentNumber), " AND ");
        } else {
            return new CuSqlChunk();
        }
    }

}
