package edu.cornell.kfs.fp.batch.service.impl;

import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.document.PreEncumbranceDocument;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;

public class PreEncumbranceDocumentGenerator extends AccountingDocumentGeneratorBase<PreEncumbranceDocument> {
    private static final Logger LOG = LogManager.getLogger();
    
    public PreEncumbranceDocumentGenerator() {
        super();
    }

    public PreEncumbranceDocumentGenerator(Supplier<Note> emptyNoteGenerator, Supplier<AdHocRoutePerson> emptyAdHocRoutePersonGenerator) {
        super(emptyNoteGenerator, emptyAdHocRoutePersonGenerator);
    }

    @Override
    public Class<? extends PreEncumbranceDocument> getDocumentClass() {
        return PreEncumbranceDocument.class;
    }
    
    @Override
    protected void populateCustomAccountingDocumentData(PreEncumbranceDocument document, AccountingXmlDocumentEntry documentEntry) {
        super.populateCustomAccountingDocumentData(document, documentEntry);
        document.setReversalDate(parseReversalDate(documentEntry));
    }
    
    private java.sql.Date parseReversalDate(AccountingXmlDocumentEntry documentEntry) {
        if (documentEntry.getReversalDate() != null) {
            java.sql.Date reversalDate = new java.sql.Date(documentEntry.getReversalDate().getTime());
            LOG.debug("parseReversalDate, reversal date is {}", reversalDate);
            return reversalDate;
        } else {
            LOG.debug("parseReversalDate, no reversal date found");
            return null;
        }
    }
    
    @Override
    protected <A extends AccountingLine> A buildAccountingLine(
            Class<A> accountingLineClass, String documentNumber, AccountingXmlDocumentAccountingLine xmlLine) {
        A line = super.buildAccountingLine(accountingLineClass, documentNumber, xmlLine);
        
        if (line instanceof TargetAccountingLine) {
            TargetAccountingLine targetLine = (TargetAccountingLine) line;
            LOG.debug("buildAccountingLine, setting reference number in target line to {}", xmlLine.getReferenceNumber());
            targetLine.setReferenceNumber(xmlLine.getReferenceNumber());
            targetLine.setFinancialDocumentLineDescription(StringUtils.EMPTY);
        }
        return line;
    }

}
