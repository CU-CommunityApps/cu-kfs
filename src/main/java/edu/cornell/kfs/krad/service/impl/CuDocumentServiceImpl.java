package edu.cornell.kfs.krad.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.core.framework.persistence.jta.TransactionalNoValidationExceptionRollback;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.UserSessionUtils;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.AdHocRouteWorkgroup;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.MaintainableXMLConversionService;
import org.kuali.kfs.krad.service.impl.DocumentServiceImpl;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.NoteType;

import com.thoughtworks.xstream.core.BaseException;

import edu.cornell.kfs.krad.service.CuDocumentService;

/**
 * Custom DocumentServiceImpl subclass that performs its own handling
 * of just-in-time legacy maintenance XML conversion.
 *
 * Maintenance documents ordinarily convert the old/new maintenance XML
 * sections separately if they need it, and are not capable of converting
 * legacy BO notes XML. To support better conversion of such content,
 * the default MaintainableXMLConversionService has been switched over
 * to a no-op implementation, and instead this class will perform
 * the XML conversion on the entire maintenance XML payload if necessary.
 * This allows for adding the features without overlaying KFS code.
 */
@TransactionalNoValidationExceptionRollback
public class CuDocumentServiceImpl extends DocumentServiceImpl implements CuDocumentService {

    protected MaintainableXMLConversionService maintainableXMLConversionService;

    /**
     * Overridden to perform just-in-time legacy maintenance XML conversion if necessary.
     *
     * @param documentHeaderId
     * @param workflowDocument
     * @param document
     * @return
     */
    @Override
    protected Document postProcessDocument(
            final String documentHeaderId, final WorkflowDocument workflowDocument, 
            final Document document) {
        try {
            return super.postProcessDocument(documentHeaderId, workflowDocument, document);
        } catch (final RuntimeException e) {
            if (shouldConvertMaintenanceXML(e, document)) {
                return postProcessDocumentWithLegacyMaintenanceXML(documentHeaderId, workflowDocument, (MaintenanceDocument) document);
            } else {
                throw e;
            }
        }
    }

    protected Document postProcessDocumentWithLegacyMaintenanceXML(
            final String documentHeaderId, final WorkflowDocument workflowDocument, 
            final MaintenanceDocument document) {
        final String oldXml = document.getXmlDocumentContents();
        final String newXml = maintainableXMLConversionService.transformMaintainableXML(oldXml);

        try {
            document.setXmlDocumentContents(newXml);
            return super.postProcessDocument(documentHeaderId, workflowDocument, document);
        } catch (final RuntimeException e) {
            document.setXmlDocumentContents(oldXml);
            throw e;
        }
    }

    protected boolean shouldConvertMaintenanceXML(final RuntimeException e, final Document document) {
        return document != null
                && document instanceof MaintenanceDocument
                && BaseException.class.isAssignableFrom(e.getClass());
    }

    /*
     * This method is modeled after base code's implementations of "disapprove" and "recall" action handling.
     */
    @Override
    public Document returnDocumentToPreviousNode(final Document document, final String annotation,
            final String nodeName) {
        checkForNulls(document);

        final Note note = createNoteFromDocument(document, annotation);
        if (document.getNoteType() == NoteType.BUSINESS_OBJECT) {
            note.setNoteTypeCode(NoteType.DOCUMENT_HEADER.getCode());
            note.setRemoteObjectIdentifier(document.getDocumentHeader().getObjectId());
        }
        document.addNote(note);
        getNoteService().save(note);

        prepareWorkflowDocument(document);
        document.getDocumentHeader().getWorkflowDocument().returnToPreviousNode(annotation, nodeName);
        final UserSession userSession = GlobalVariables.getUserSession();
        if (userSession != null) {
            UserSessionUtils.addWorkflowDocument(userSession, document.getDocumentHeader().getWorkflowDocument());
        }
        removeAdHocPersonsAndWorkgroups(document);
        return document;
    }

    // Copied this private method from the superclass.
    private void removeAdHocPersonsAndWorkgroups(final Document document) {
        final List<AdHocRoutePerson> adHocRoutePersons = new ArrayList<>();
        final List<AdHocRouteWorkgroup> adHocRouteWorkgroups = new ArrayList<>();
        getBusinessObjectService().delete(document.getAdHocRoutePersons());
        getBusinessObjectService().delete(document.getAdHocRouteWorkgroups());
        document.setAdHocRoutePersons(adHocRoutePersons);
        document.setAdHocRouteWorkgroups(adHocRouteWorkgroups);
    }
    

    public void setMaintainableXMLConversionService(final MaintainableXMLConversionService maintainableXMLConversionService) {
        this.maintainableXMLConversionService = maintainableXMLConversionService;
    }

}
