package edu.cornell.kfs.krad.util;

import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;

public final class MaintenanceLockUtils {

    private static final Logger LOG = LogManager.getLogger();

    private static final int BULK_LOCK_QUERY_LIMIT = 500;

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
        
        String lockingDocumentNumber = null;
        int startIndex = 0;
        
        do {
            int endIndex = Math.min(startIndex + BULK_LOCK_QUERY_LIMIT, lockingRepresentations.size());
            List<String> locksSubList = lockingRepresentations.subList(startIndex, endIndex);
            lockingDocumentNumber = actualQueryOperation.apply(locksSubList, documentNumber);
            startIndex += BULK_LOCK_QUERY_LIMIT;
        } while (StringUtils.isBlank(lockingDocumentNumber) && startIndex < lockingRepresentations.size());
        
        final String matchingDocNumber = lockingDocumentNumber;
        LOG.debug("doChunkedSearchForAnyLockingDocumentNumber, {}",
                () -> StringUtils.isNotBlank(matchingDocNumber)
                        ? "Found a matching lock owned by document " + matchingDocNumber
                        : "No matching locks found");
        return lockingDocumentNumber;
    }

}
