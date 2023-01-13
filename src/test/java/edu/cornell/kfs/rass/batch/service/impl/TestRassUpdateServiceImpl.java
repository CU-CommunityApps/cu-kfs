package edu.cornell.kfs.rass.batch.service.impl;

import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kim.impl.identity.Person;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.module.cg.businessobject.AgencyExtendedAttribute;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;
import edu.cornell.kfs.rass.RassTestConstants;
import edu.cornell.kfs.rass.batch.PendingDocumentTracker;
import edu.cornell.kfs.rass.batch.RassBusinessObjectUpdateResult;
import edu.cornell.kfs.rass.batch.RassObjectTranslationDefinition;
import edu.cornell.kfs.rass.batch.xml.RassXmlObject;
import edu.cornell.kfs.sys.util.MockPersonUtil;

public class TestRassUpdateServiceImpl extends RassUpdateServiceImpl {

    private Consumer<MaintenanceDocument> documentTracker;

    public void setDocumentTracker(Consumer<MaintenanceDocument> documentTracker) {
        this.documentTracker = documentTracker;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public <T extends RassXmlObject, R extends PersistableBusinessObject> RassBusinessObjectUpdateResult<R> processObject(
            T xmlObject, RassObjectTranslationDefinition<T, R> objectDefinition, PendingDocumentTracker documentTracker) {
        if (StringUtils.equals(RassTestConstants.ERROR_OBJECT_KEY, objectDefinition.printPrimaryKeyValues(xmlObject))) {
            throw new RuntimeException("Forced error for " + objectDefinition.printObjectLabelAndKeys(xmlObject));
        }
        return super.processObject(xmlObject, objectDefinition, documentTracker);
    }

    @Override
    protected <R extends PersistableBusinessObject> R createMinimalObject(Class<R> businessObjectClass) {
        R businessObject = super.createMinimalObject(businessObjectClass);
        if (businessObject instanceof Agency) {
            businessObject.setExtension(new AgencyExtendedAttribute());
        } else if (businessObject instanceof Award) {
            businessObject.setExtension(new AwardExtendedAttribute());
        }
        return businessObject;
    }

    @Override
    protected void materializeProxiedCollectionsOnExistingObject(Object existingObject) {
        // Do nothing.
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
