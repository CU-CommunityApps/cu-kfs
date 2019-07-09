package edu.cornell.kfs.rass.batch.service;

import org.kuali.kfs.krad.bo.PersistableBusinessObject;

import edu.cornell.kfs.rass.batch.PendingDocumentTracker;
import edu.cornell.kfs.rass.batch.RassBusinessObjectUpdateResult;
import edu.cornell.kfs.rass.batch.RassObjectTranslationDefinition;
import edu.cornell.kfs.rass.batch.xml.RassXmlObject;

public interface RassUpdateService {

    <T extends RassXmlObject, R extends PersistableBusinessObject> RassBusinessObjectUpdateResult<R> processObject(
            T xmlObject, RassObjectTranslationDefinition<T, R> objectDefinition, PendingDocumentTracker documentTracker);

    void waitForRemainingGeneratedDocumentsToFinish(PendingDocumentTracker documentTracker);

}
