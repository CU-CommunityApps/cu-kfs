package edu.cornell.kfs.fp.batch.service.impl;

import java.util.function.Supplier;

import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.service.UniversityDateService;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherDetail;
import edu.cornell.kfs.fp.batch.xml.PaymentInformation;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;

public class CuDisbursementVoucherDocumentGenerator extends AccountingDocumentGeneratorBase<CuDisbursementVoucherDocument> {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuDisbursementVoucherDocumentGenerator.class);
    
    protected UniversityDateService universityDateService;
    
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
    protected <A extends AccountingLine> A buildAccountingLine(
            Class<A> accountingLineClass, String documentNumber, AccountingXmlDocumentAccountingLine xmlLine) {
        A accountingLine = super.buildAccountingLine(accountingLineClass, documentNumber, xmlLine);
        accountingLine.setPostingYear(universityDateService.getCurrentFiscalYear());
        return accountingLine;
    }
    
    @Override
    protected void populateCustomAccountingDocumentData(CuDisbursementVoucherDocument dvDocument, AccountingXmlDocumentEntry documentEntry) {
        super.populateCustomAccountingDocumentData(dvDocument, documentEntry);
        if (ObjectUtils.isNotNull(documentEntry.getDisbursementVoucherDetail())) {
            populatePaymentInformation(dvDocument, documentEntry.getDisbursementVoucherDetail());
            populateContactInformation(dvDocument, documentEntry.getDisbursementVoucherDetail());
        } else {
            LOG.error("populateCustomAccountingDocumentData, did not find Disbursement Voucher Details");
        }
    }
    
    private void populatePaymentInformation(CuDisbursementVoucherDocument dvDocument, DisbursementVoucherDetail dvDetail) {
        if (ObjectUtils.isNotNull(dvDetail.getPaymentInformation())) {
            PaymentInformation paymentInfo = dvDetail.getPaymentInformation();
            dvDocument.getDvPayeeDetail().setDisbVchrPaymentReasonCode(paymentInfo.getPaymentReasonCode());
            dvDocument.getDvPayeeDetail().setDisbursementVoucherPayeeTypeCode(paymentInfo.getPayeeTypeCode());
            dvDocument.getDvPayeeDetail().setDisbVchrPayeeIdNumber(paymentInfo.getPayeeId());
            dvDocument.getDvPayeeDetail().setDisbVchrPayeePersonName(paymentInfo.getPayeeName());
            dvDocument.getDvPayeeDetail().setDisbVchrPayeeLine1Addr(paymentInfo.getAddressLine1());
            dvDocument.getDvPayeeDetail().setDisbVchrPayeeLine2Addr(paymentInfo.getAddressLine2());
            dvDocument.getDvPayeeDetail().setDisbVchrPayeeCityName(paymentInfo.getCity());
            dvDocument.getDvPayeeDetail().setDisbVchrPayeeStateCode(paymentInfo.getState());
            dvDocument.getDvPayeeDetail().setDisbVchrPayeeCountryCode(paymentInfo.getCountry());
            dvDocument.getDvPayeeDetail().setDisbVchrPayeeZipCode(paymentInfo.getPostalCode());
            dvDocument.setDisbVchrCheckTotalAmount(paymentInfo.getCheckAmount());
            dvDocument.setDisbursementVoucherDueDate(new java.sql.Date(paymentInfo.getDueDate().getTime()));
            dvDocument.setDisbVchrPaymentMethodCode(paymentInfo.getPaymentMethod());
            dvDocument.setDisbVchrCheckStubText(paymentInfo.getCheckStubText());
            dvDocument.setDisbursementVoucherDocumentationLocationCode(paymentInfo.getDocumentationLocationCode());
        } else {
            LOG.error("populatePaymentInformation, did NOT find payment info");
        }
    }
    
    private void populateContactInformation(CuDisbursementVoucherDocument dvDocument, DisbursementVoucherDetail dvDetail) {
        dvDocument.setDisbVchrContactPersonName("Salino, Catherine C.");
        dvDocument.setDisbVchrContactPhoneNumber("607-255-9466");
        dvDocument.setDisbVchrContactEmailId("ccs1@cornell.edu");
        dvDocument.setCampusCode("IT");
    }

    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

}
