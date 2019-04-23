package edu.cornell.kfs.rass.batch.service;

import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.maintenance.MaintenanceDocument;

import edu.cornell.kfs.rass.batch.RassObjectTranslationDefinition;
import edu.cornell.kfs.rass.batch.xml.RassXmlObject;

public interface RassRoutingService {

    <T extends RassXmlObject, R extends PersistableBusinessObject> MaintenanceDocument createAndRouteMaintenanceDocument(
            Pair<R, R> businessObjects, String maintenanceAction, RassObjectTranslationDefinition<T, R> objectDefinition);

}
