package edu.cornell.kfs.krad.service;

import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.DocumentService;

public interface CuDocumentService extends DocumentService {

    Document returnDocumentToPreviousNode(final Document document, final String annotation, final String nodeName);

}
