package edu.cornell.kfs.fp.batch.service;

import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItem;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItemDetail;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;

public interface CreateAccountingDocumentValidationService {
    
    boolean isValidXmlFileHeaderData(AccountingXmlDocumentListWrapper accountingXmlDocuments, CreateAccountingDocumentReportItem reportItem);
    
    boolean isAllRequiredDataValid(AccountingXmlDocumentEntry accountingXmlDocument, CreateAccountingDocumentReportItemDetail reportItemDetail);
    
}
