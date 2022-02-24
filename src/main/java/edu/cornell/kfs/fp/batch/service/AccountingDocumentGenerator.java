package edu.cornell.kfs.fp.batch.service;

import java.util.function.Function;

import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.document.AccountingDocument;

import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItemDetail;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;

public interface AccountingDocumentGenerator<T extends AccountingDocument> {
    Class<? extends T> getDocumentClass();

    T createDocument(Function<Class<? extends Document>, Document> emptyDocumentGenerator, AccountingXmlDocumentEntry documentEntry);
    
    void handleDocumentWarningMessage(CreateAccountingDocumentReportItemDetail reportDetail);
}
