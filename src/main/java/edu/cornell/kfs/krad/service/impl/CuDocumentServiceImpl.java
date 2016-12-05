package edu.cornell.kfs.krad.service.impl;

import com.thoughtworks.xstream.core.BaseException;
import org.kuali.rice.core.framework.persistence.jta.TransactionalNoValidationExceptionRollback;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.maintenance.MaintenanceDocument;
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
 * This allows for adding the features without overlaying Rice code.
 *
 * There are three get-by-ID service methods that do not save documents
 * prior to triggering a potential XML conversion attempt. (Conversion
 * should theoretically not be needed if the document gets saved
 * just prior to triggering another parsing of the maintenance XML.)
 * These methods have been overridden so that if legacy XML is present
 * and it cannot be parsed as-is, then a conversion of the full XML
 * will be performed with a different conversion service implementation,
 * and the XML-parsing code will be called again.
 *
 * This KFS version is based on its Cynergy counterpart:
 * edu.cornell.cynergy.krad.service.impl.CynergyDocumentServiceImpl
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
    protected Document postProcessDocument(String documentHeaderId, WorkflowDocument workflowDocument, Document document) {
        try {
            return super.postProcessDocument(documentHeaderId, workflowDocument, document);
        } catch (RuntimeException e) {
            if (shouldConvertMaintenanceXML(e, document)) {
                return postProcessDocumentWithLegacyMaintenanceXML(documentHeaderId, workflowDocument, (MaintenanceDocument) document);
            } else {
                throw e;
            }
        }
    }

    protected Document postProcessDocumentWithLegacyMaintenanceXML(String documentHeaderId, WorkflowDocument workflowDocument, MaintenanceDocument document) {
        String oldXml = document.getXmlDocumentContents();
        String newXml = maintainableXMLConversionService.transformMaintainableXML(oldXml);

        try {
            document.setXmlDocumentContents(newXml);
            return super.postProcessDocument(documentHeaderId, workflowDocument, document);
        } catch (RuntimeException e) {
            document.setXmlDocumentContents(oldXml);
            throw e;
        }
    }

    protected boolean shouldConvertMaintenanceXML(RuntimeException e, Document document) {
        return document != null
                && document instanceof MaintenanceDocument
                && BaseException.class.isAssignableFrom(e.getClass());
    }

    public void setMaintainableXMLConversionService(MaintainableXMLConversionService maintainableXMLConversionService) {
        this.maintainableXMLConversionService = maintainableXMLConversionService;
    }

}
