package edu.cornell.kfs.krad.service.impl;

import com.thoughtworks.xstream.core.BaseException;
import org.kuali.kfs.core.framework.persistence.jta.TransactionalNoValidationExceptionRollback;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.MaintainableXMLConversionService;
import org.kuali.kfs.krad.service.impl.DocumentServiceImpl;

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
public class CuDocumentServiceImpl extends DocumentServiceImpl {

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

    public void setMaintainableXMLConversionService(final MaintainableXMLConversionService maintainableXMLConversionService) {
        this.maintainableXMLConversionService = maintainableXMLConversionService;
    }

}
