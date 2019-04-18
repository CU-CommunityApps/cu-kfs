package edu.cornell.kfs.rass.batch.service;

import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.maintenance.MaintenanceDocument;

import edu.cornell.kfs.rass.batch.xml.RassObjectTranslationDefinition;

public interface RassRoutingService {

    <R extends PersistableBusinessObject> MaintenanceDocument createAndRouteMaintenanceDocument(
            Pair<R, R> businessObjects, String maintenanceAction, RassObjectTranslationDefinition<?, R> objectDefinition);

}
