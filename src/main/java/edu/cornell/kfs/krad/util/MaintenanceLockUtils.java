package edu.cornell.kfs.krad.util;

import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.PersistenceBrokerFactory;
import org.apache.ojb.broker.core.PersistenceBrokerConfiguration;
import org.apache.ojb.broker.util.configuration.ConfigurationException;
import org.kuali.kfs.sys.KFSConstants;

public final class MaintenanceLockUtils {

    private static final Logger LOG = LogManager.getLogger();

    private static volatile int ojbInListLimit;

    private MaintenanceLockUtils() {
        throw new UnsupportedOperationException("do not call");
    }

    public static String doChunkedSearchForAnyLockingDocumentNumber(List<String> lockingRepresentations,
            String documentNumber, BiFunction<List<String>, String, String> actualQueryOperation) {
        if (CollectionUtils.isEmpty(lockingRepresentations)) {
            LOG.debug("doChunkedSearchForAnyLockingDocumentNumber, No locking reps specified, skipping search");
            return null;
        }
        LOG.debug("doChunkedSearchForAnyLockingDocumentNumber, Searching with {} lock reps{}",
                lockingRepresentations::size,
                () -> StringUtils.isNotBlank(documentNumber)
                        ? " and excluding results owned by document " + documentNumber
                        : KFSConstants.EMPTY_STRING);
        
        int queryChunkSize = getOjbSqlInLimit();
        LOG.debug("doChunkedSearchForAnyLockingDocumentNumber, Searching in chunk sizes of {}", queryChunkSize);
        
        String lockingDocumentNumber = null;
        int startIndex = 0;
        
        do {
            int endIndex = Math.min(startIndex + queryChunkSize, lockingRepresentations.size());
            List<String> locksSubList = lockingRepresentations.subList(startIndex, endIndex);
            lockingDocumentNumber = actualQueryOperation.apply(locksSubList, documentNumber);
            startIndex = endIndex;
        } while (StringUtils.isBlank(lockingDocumentNumber) && startIndex < lockingRepresentations.size());
        
        final String matchingDocNumber = lockingDocumentNumber;
        LOG.debug("doChunkedSearchForAnyLockingDocumentNumber, {}",
                () -> StringUtils.isNotBlank(matchingDocNumber)
                        ? "Found a matching lock owned by document " + matchingDocNumber
                        : "No matching locks found");
        return lockingDocumentNumber;
    }

    /*
     * This is a modified copy of OJB's Criteria.getSqlInLimit() method, adjusted to lazy-load
     * the value and to take advantage of the double-checked lock idiom.
     */
    private static int getOjbSqlInLimit() {
        int limit = ojbInListLimit;
        if (limit == 0) {
            synchronized (MaintenanceLockUtils.class) {
                limit = ojbInListLimit;
                if (limit == 0) {
                    try {
                        PersistenceBrokerConfiguration config = (PersistenceBrokerConfiguration)
                                PersistenceBrokerFactory.getConfigurator().getConfigurationFor(null);
                        limit = config.getSqlInLimit();
                    } catch (ConfigurationException e) {
                        limit = 200;
                    }
                    ojbInListLimit = limit;
                }
            }
        }
        
        return limit;
    }

}
