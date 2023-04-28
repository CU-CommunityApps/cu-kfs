package edu.cornell.kfs.coa.dataaccess.impl;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.cornell.kfs.coa.dataaccess.AccountDelegateGlobalLockDao;
import edu.cornell.kfs.coa.dataaccess.CuAccountDelegateGlobalDao;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class AccountDelegateGlobalLockDaoJdbc extends CuSqlQueryPlatformAwareDaoBaseJdbc
        implements AccountDelegateGlobalLockDao {

    private static final Logger LOG = LogManager.getLogger();

    private CuAccountDelegateGlobalDao accountDelegateGlobalDao;

    @Override
    public String getAnyLockingDocumentNumber(List<String> lockingRepresentations, String documentNumber) {
        LOG.debug("getAnyLockingDocumentNumber, Checking for locking document(s)");
        if (CollectionUtils.isEmpty(lockingRepresentations)) {
            LOG.debug("getAnyLockingDocumentNumber, No lock representations specified, skipping search");
            return null;
        }
        
        CuSqlQuery sqlQuery = createQueryForLockingDocuments(lockingRepresentations, documentNumber);
        List<String> results = queryForValues(sqlQuery, SingleColumnRowMapper.newInstance(String.class));
        String lockingDocumentId = CollectionUtils.isNotEmpty(results) ? results.get(0) : null;
        
        LOG.debug("getAnyLockingDocumentNumber, {}",
                () -> StringUtils.isNotBlank(lockingDocumentId)
                        ? "Found a matching lock owned by document " + lockingDocumentId
                        : "No matching locks found");
        
        return lockingDocumentId;
    }

    private CuSqlQuery createQueryForLockingDocuments(List<String> lockingRepresentations, String documentNumber) {
        return CuSqlQuery.of(
                "SELECT DOC_HDR_ID FROM KFS.KRNS_MAINT_LOCK_T WHERE ",
                createANDedCriteriaForDocumentIdExclusion(documentNumber),
                "(",
                        createORedCriteriaForLockMatching(lockingRepresentations),
                ") AND ROWNUM <= 1"
        );
    }

    private CuSqlChunk createANDedCriteriaForDocumentIdExclusion(String documentNumber) {
        if (StringUtils.isNotBlank(documentNumber)) {
            return CuSqlChunk.of(
                    "DOC_HDR_ID <> ", CuSqlChunk.forParameter(documentNumber), " AND ");
        } else {
            return new CuSqlChunk();
        }
    }

    private CuSqlChunk createORedCriteriaForLockMatching(List<String> lockingRepresentations) {
        List<String> lockRepPatterns = accountDelegateGlobalDao.createSearchPatternsFromLockingRepresentations(
                lockingRepresentations);
        LOG.debug("createOredCriteriaForLockMatching, Performing search with {} lock representations and {} patterns",
                lockingRepresentations::size, lockRepPatterns::size);
        
        CuSqlChunk lockMatchCriteria = CuSqlChunk.asSqlInCondition("MAINT_LOCK_REP_TXT", lockingRepresentations);
        
        for (String lockRepPattern : lockRepPatterns) {
            lockMatchCriteria.append(" OR MAINT_LOCK_REP_TXT LIKE ")
                    .appendAsParameter(lockRepPattern);
        }
        
        return lockMatchCriteria;
    }

    public void setAccountDelegateGlobalDao(CuAccountDelegateGlobalDao accountDelegateGlobalDao) {
        this.accountDelegateGlobalDao = accountDelegateGlobalDao;
    }

}
