package edu.cornell.kfs.rass.batch.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.maintenance.MaintenanceDocument;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.MaintenanceDocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.rass.RassConstants;
import edu.cornell.kfs.rass.batch.service.RassRoutingService;
import edu.cornell.kfs.rass.batch.xml.RassObjectTranslationDefinition;
import edu.cornell.kfs.sys.CUKFSConstants;

public class RassRoutingServiceImpl implements RassRoutingService {

    protected MaintenanceDocumentService maintenanceDocumentService;
    protected DocumentService documentService;
    protected DataDictionaryService dataDictionaryService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public <R extends PersistableBusinessObject> MaintenanceDocument createAndRouteMaintenanceDocument(
            Pair<R, R> businessObjects, String maintenanceAction, RassObjectTranslationDefinition<?, R> objectDefinition) {
        try {
            return GlobalVariables.doInNewGlobalVariables(
                    buildSessionForSystemUser(),
                    () -> createAndRouteMaintenanceDocumentInternal(businessObjects, maintenanceAction, objectDefinition));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected UserSession buildSessionForSystemUser() {
        return new UserSession(KFSConstants.SYSTEM_USER);
    }

    protected <R extends PersistableBusinessObject> MaintenanceDocument createAndRouteMaintenanceDocumentInternal(
            Pair<R, R> businessObjects, String maintenanceAction, RassObjectTranslationDefinition<?, R> objectDefinition)
            throws WorkflowException {
        R newBo = businessObjects.getRight();
        MaintenanceDocument maintenanceDocument = maintenanceDocumentService.setupNewMaintenanceDocument(
                objectDefinition.getBusinessObjectClass().getName(), objectDefinition.getDocumentTypeName(), maintenanceAction);
        if (StringUtils.equals(KRADConstants.MAINTENANCE_EDIT_ACTION, maintenanceAction)) {
            maintenanceDocument.getOldMaintainableObject().setDataObject(businessObjects.getLeft());
        }
        maintenanceDocument.getNewMaintainableObject().setDataObject(newBo);
        maintenanceDocument.getNewMaintainableObject().setMaintenanceAction(maintenanceAction);
        maintenanceDocument.getDocumentHeader().setDocumentDescription(
                buildDocumentDescription(newBo, maintenanceAction, objectDefinition));
        
        maintenanceDocument = (MaintenanceDocument) documentService.routeDocument(
                maintenanceDocument, RassConstants.RASS_ROUTE_ACTION_ANNOTATION, null);
        
        return maintenanceDocument;
    }

    protected <R extends PersistableBusinessObject> String buildDocumentDescription(
            R newBo, String maintenanceAction, RassObjectTranslationDefinition<?, R> objectDefinition) {
        String actionLabel = getLabelForAction(maintenanceAction);
        String formattedDescription = String.format(RassConstants.RASS_MAINTENANCE_DOCUMENT_DESCRIPTION_FORMAT,
                actionLabel, objectDefinition.printObjectLabelAndKeys(newBo));
        int maxLength = getDocumentDescriptionMaxLength();
        if (formattedDescription.length() > maxLength) {
            return StringUtils.left(formattedDescription, maxLength - CUKFSConstants.ELLIPSIS.length())
                    + CUKFSConstants.ELLIPSIS;
        } else {
            return formattedDescription;
        }
    }

    protected String getLabelForAction(String maintenanceAction) {
        return StringUtils.equals(KRADConstants.MAINTENANCE_NEW_ACTION, maintenanceAction)
                ? RassConstants.RASS_MAINTENANCE_NEW_ACTION_DESCRIPTION : maintenanceAction;
    }

    protected int getDocumentDescriptionMaxLength() {
        return dataDictionaryService
                .getAttributeMaxLength(FinancialSystemDocumentHeader.class, KFSPropertyConstants.DOCUMENT_DESCRIPTION)
                .intValue();
    }

    public void setMaintenanceDocumentService(MaintenanceDocumentService maintenanceDocumentService) {
        this.maintenanceDocumentService = maintenanceDocumentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

}
