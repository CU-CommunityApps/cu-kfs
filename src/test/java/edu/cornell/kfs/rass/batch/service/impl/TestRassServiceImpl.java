package edu.cornell.kfs.rass.batch.service.impl;

import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.maintenance.Maintainable;
import org.kuali.kfs.krad.maintenance.MaintenanceDocument;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;

import edu.cornell.kfs.module.cg.businessobject.AgencyExtendedAttribute;
import edu.cornell.kfs.rass.RassTestConstants;
import edu.cornell.kfs.rass.batch.RassXmlObjectResult;
import edu.cornell.kfs.rass.batch.xml.RassObjectTranslationDefinition;
import edu.cornell.kfs.sys.util.MockPersonUtil;

public class TestRassServiceImpl extends RassServiceImpl {

    private Consumer<Maintainable> agencyUpdateTracker;

    public void setAgencyUpdateTracker(Consumer<Maintainable> agencyUpdateTracker) {
        this.agencyUpdateTracker = agencyUpdateTracker;
    }

    @Override
    protected <T, R extends PersistableBusinessObject> RassXmlObjectResult processObject(
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
        }
        return businessObject;
    }

    @Override
    protected <R extends PersistableBusinessObject> MaintenanceDocument createAndRouteMaintenanceDocument(
            Pair<R, R> businessObjects, String maintenanceAction, RassObjectTranslationDefinition<?, R> objectDefinition)
            throws WorkflowException {
        MaintenanceDocument maintenanceDocument = super.createAndRouteMaintenanceDocument(businessObjects, maintenanceAction, objectDefinition);
        Maintainable maintainable = maintenanceDocument.getNewMaintainableObject();
        Object newBusinessObject = maintenanceDocument.getNewMaintainableObject().getDataObject();
        if (newBusinessObject instanceof Agency) {
            agencyUpdateTracker.accept(maintainable);
        }
        return maintenanceDocument;
    }

    @Override
    protected UserSession buildSessionForSystemUser() {
        Person mockSystemUser = MockPersonUtil.createMockPerson(UserNameFixture.kfs);
        return MockPersonUtil.createMockUserSession(mockSystemUser);
    }
    
}