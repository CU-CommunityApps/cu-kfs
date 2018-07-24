package edu.cornell.kfs.fp.batch.service.impl;

import java.util.function.Supplier;

import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.PaymentInformation;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;

public class CuDisbursementVoucherDocumentGenerator extends AccountingDocumentGeneratorBase<CuDisbursementVoucherDocument> {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuDisbursementVoucherDocumentGenerator.class);
    
    public CuDisbursementVoucherDocumentGenerator() {
        super();
    }
    
    public CuDisbursementVoucherDocumentGenerator(Supplier<Note> emptyNoteGenerator, Supplier<AdHocRoutePerson> emptyAdHocRoutePersonGenerator) {
        super(emptyNoteGenerator, emptyAdHocRoutePersonGenerator);
    }
    
    @Override
    public Class<? extends CuDisbursementVoucherDocument> getDocumentClass() {
        return CuDisbursementVoucherDocument.class;
    }
    
    @Override
    protected void populateCustomAccountingDocumentData(CuDisbursementVoucherDocument dvDocument, AccountingXmlDocumentEntry documentEntry) {
        super.populateCustomAccountingDocumentData(dvDocument, documentEntry);
        populatePaymentInformation(dvDocument, documentEntry);
    }
    
    private void populatePaymentInformation(CuDisbursementVoucherDocument dvDocument, AccountingXmlDocumentEntry documentEntry) {
        if (ObjectUtils.isNotNull(documentEntry.getPaymentInformation())) {
            LOG.info("populatePaymentInformation, found payment info");
            PaymentInformation paymentInfo = documentEntry.getPaymentInformation();
            dvDocument.getDvPayeeDetail().setDisbVchrPaymentReasonCode(paymentInfo.getPaymentReasonCode());
            dvDocument.getDvPayeeDetail().setDisbVchrPayeeIdNumber(paymentInfo.getPayeeId());
            dvDocument.getDvPayeeDetail().setDisbVchrPayeeLine1Addr(paymentInfo.getAddressLine1());
            dvDocument.getDvPayeeDetail().setDisbVchrPayeeLine2Addr(paymentInfo.getAddressLine2());
            dvDocument.getDvPayeeDetail().setDisbVchrPayeeCityName(paymentInfo.getCity());
            dvDocument.getDvPayeeDetail().setDisbVchrPayeeStateCode(paymentInfo.getState());
            dvDocument.getDvPayeeDetail().setDisbVchrPayeeCountryCode(paymentInfo.getCountry());
            dvDocument.getDvPayeeDetail().setDisbVchrPayeeZipCode(paymentInfo.getPostalCode());
            dvDocument.setDisbVchrCheckTotalAmount(paymentInfo.getCheckAmount());
            dvDocument.setDisbursementVoucherDueDate(new java.sql.Date(paymentInfo.getDueDate().getTime()));
            dvDocument.setDisbVchrCheckStubText(paymentInfo.getCheckStubText());
        } else {
            LOG.info("populatePaymentInformation, did NOT fin payment info");
        }
        
        dvDocument.setDisbVchrContactPersonName("Salino, Catherine C.");
        dvDocument.setDisbVchrContactPhoneNumber("607-255-9466");
        dvDocument.setDisbVchrContactEmailId("ccs1@cornell.edu");
        dvDocument.setCampusCode("IT");
    }

}
