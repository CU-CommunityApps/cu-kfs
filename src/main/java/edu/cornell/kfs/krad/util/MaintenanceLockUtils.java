package edu.cornell.kfs.krad.util;

import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public final class MaintenanceLockUtils {

    private static final int BULK_LOCK_QUERY_LIMIT = 500;

    private MaintenanceLockUtils() {
        throw new UnsupportedOperationException("do not call");
    }

    public static String doChunkedSearchForAnyLockingDocumentNumber(List<String> lockingRepresentations,
            String documentNumber, BiFunction<List<String>, String, String> actualQueryOperation) {
        if (CollectionUtils.isEmpty(lockingRepresentations)) {
            return null;
        }
        
        String lockingDocumentNumber = null;
        int startIndex = 0;
        
        do {
            int endIndex = Math.min(startIndex + BULK_LOCK_QUERY_LIMIT, lockingRepresentations.size());
            List<String> locksSubList = lockingRepresentations.subList(startIndex, endIndex);
            lockingDocumentNumber = actualQueryOperation.apply(locksSubList, documentNumber);
            startIndex += BULK_LOCK_QUERY_LIMIT;
        } while (StringUtils.isBlank(lockingDocumentNumber) && startIndex < lockingRepresentations.size());
        
        return lockingDocumentNumber;
    }

}
