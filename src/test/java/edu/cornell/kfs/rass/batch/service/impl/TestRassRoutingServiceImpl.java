package edu.cornell.kfs.rass.batch.service.impl;

import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.maintenance.MaintenanceDocument;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;

import edu.cornell.kfs.rass.batch.RassObjectTranslationDefinition;
import edu.cornell.kfs.rass.batch.xml.RassXmlObject;
import edu.cornell.kfs.sys.util.MockPersonUtil;

public class TestRassRoutingServiceImpl extends RassRoutingServiceImpl {

    private Consumer<MaintenanceDocument> documentTracker;

    public void setDocumentTracker(Consumer<MaintenanceDocument> documentTracker) {
        this.documentTracker = documentTracker;
    }

    @Override
    protected <T extends RassXmlObject, R extends PersistableBusinessObject> MaintenanceDocument createAndRouteMaintenanceDocumentInternal(
            Pair<R, R> businessObjects, String maintenanceAction, RassObjectTranslationDefinition<T, R> objectDefinition)
            throws WorkflowException {
        MaintenanceDocument maintenanceDocument = super.createAndRouteMaintenanceDocumentInternal(
                businessObjects, maintenanceAction, objectDefinition);
        documentTracker.accept(maintenanceDocument);
        return maintenanceDocument;
    }

    @Override
    protected UserSession buildSessionForSystemUser() {
        Person mockSystemUser = MockPersonUtil.createMockPerson(UserNameFixture.kfs);
        return MockPersonUtil.createMockUserSession(mockSystemUser);
    }

}
