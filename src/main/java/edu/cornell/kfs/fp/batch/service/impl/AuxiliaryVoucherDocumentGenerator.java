package edu.cornell.kfs.fp.batch.service.impl;

import java.util.function.Supplier;

import org.kuali.kfs.fp.document.AuxiliaryVoucherDocument;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;

public class AuxiliaryVoucherDocumentGenerator
        extends AccountingDocumentGeneratorBase<AuxiliaryVoucherDocument> {

    public AuxiliaryVoucherDocumentGenerator() {
        super();
    }

    public AuxiliaryVoucherDocumentGenerator(
            Supplier<Note> emptyNoteGenerator, Supplier<AdHocRoutePerson> emptyAdHocRoutePersonGenerator) {
        super(emptyNoteGenerator, emptyAdHocRoutePersonGenerator);
    }

    @Override
    public Class<? extends AuxiliaryVoucherDocument> getDocumentClass() {
        return AuxiliaryVoucherDocument.class;
    }

    @Override
    protected <A extends AccountingLine> A buildAccountingLine(
            Class<A> accountingLineClass, String documentNumber, AccountingXmlDocumentAccountingLine xmlLine) {
        A accountingLine = super.buildAccountingLine(accountingLineClass, documentNumber, xmlLine);
        validateAndPopulateAccountingLineWithDebitCreditAmounts(accountingLine, xmlLine);
        return accountingLine;
    }

    protected <A extends AccountingLine> void validateAndPopulateAccountingLineWithDebitCreditAmounts(
            A accountingLine, AccountingXmlDocumentAccountingLine xmlLine) {
        if (hasAmountBeenSpecified(xmlLine.getAmount())) {
            throw new ValidationException(
                    "AV accounting line cannot use the 'amount' element to specify an amount; use the relevant debit/credit amount element instead");
        }
        
        KualiDecimal debitAmount = xmlLine.getDebitAmount();
        KualiDecimal creditAmount = xmlLine.getCreditAmount();
        if (hasAmountBeenSpecified(debitAmount)) {
            if (hasAmountBeenSpecified(creditAmount)) {
                throw new ValidationException("AV accounting line cannot specify both a debit amount and a credit amount");
            }
            accountingLine.setDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            accountingLine.setAmount(debitAmount);
        } else if (hasAmountBeenSpecified(creditAmount)) {
            accountingLine.setDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            accountingLine.setAmount(creditAmount);
        } else {
            throw new ValidationException("AV accounting line did not specify a debit or credit amount");
        }
    }

    protected boolean hasAmountBeenSpecified(KualiDecimal amount) {
        return amount != null && !KualiDecimal.ZERO.equals(amount);
    }

    @Override
    protected void populateCustomAccountingDocumentData(AuxiliaryVoucherDocument document, AccountingXmlDocumentEntry documentEntry) {
        
    }

}
