package edu.cornell.kfs.fp.batch.service;

import java.util.function.Function;

import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.document.AccountingDocument;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;

public interface AccountingDocumentGenerator<T extends AccountingDocument> {
    T createDocument(Function<Class<? extends Document>, Document> bareDocumentGenerator, AccountingXmlDocumentEntry documentEntry);
}
