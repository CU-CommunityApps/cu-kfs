package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kuali.kfs.krad.bo.PersistableBusinessObject;

import edu.cornell.kfs.rass.RassConstants.RassObjectUpdateResultCode;
import edu.cornell.kfs.rass.util.RassUtil;

public class PendingDocumentTracker {
    private Map<String, String> objectKeysToDocumentIdsMap = new HashMap<>();
    private Set<String> objectsWithFailedUpdates = new HashSet<>();

    public <R extends PersistableBusinessObject> void addDocumentIdToTrack(RassBusinessObjectUpdateResult<R> objectResult) {
        objectKeysToDocumentIdsMap.put(
                RassUtil.buildClassAndKeyIdentifier(objectResult), objectResult.getDocumentId());
    }

    public String getTrackedDocumentId(String classAndKeyIdentifier) {
        return objectKeysToDocumentIdsMap.get(classAndKeyIdentifier);
    }

    public List<String> getIdsForAllObjectsWithTrackedDocuments() {
        return new ArrayList<>(objectKeysToDocumentIdsMap.keySet());
    }

    public void stopTrackingDocumentForObject(String classAndKeyIdentifier) {
        objectKeysToDocumentIdsMap.remove(classAndKeyIdentifier);
    }

    public <R extends PersistableBusinessObject> void addObjectUpdateFailureToTrack(RassBusinessObjectUpdateResult<R> objectResult) {
        if (!RassObjectUpdateResultCode.ERROR.equals(objectResult.getResultCode())) {
            throw new IllegalArgumentException(
                    "processingResult should have had a status of ERROR, but instead had " + objectResult.getResultCode());
        }
        objectsWithFailedUpdates.add(RassUtil.buildClassAndKeyIdentifier(objectResult));
    }

    public boolean didObjectFailToUpdate(String classAndKeyIdentifier) {
        return objectsWithFailedUpdates.contains(classAndKeyIdentifier);
    }

    public void stopTrackingFailedUpdateForObject(String classAndKeyIdentifier) {
        objectsWithFailedUpdates.remove(classAndKeyIdentifier);
    }

}
