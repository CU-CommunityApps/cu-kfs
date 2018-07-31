package edu.cornell.kfs.fp.batch.service.impl;

import java.sql.Timestamp;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.fp.document.service.DisbursementVoucherTravelService;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherDetail;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherNonEmployeeTravel;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherPaymentInfomration;
import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;

public class CuDisbursementVoucherDocumentGenerator extends AccountingDocumentGeneratorBase<CuDisbursementVoucherDocument> {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuDisbursementVoucherDocumentGenerator.class);
    
    protected UniversityDateService universityDateService;
    protected DisbursementVoucherTravelService disbursementVoucherTravelService;
    
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
            populateDisbursementVouchersGenericSections(dvDocument, documentEntry.getDisbursementVoucherDetail());
            populatePaymentInformation(dvDocument, documentEntry.getDisbursementVoucherDetail());
            populateNonEmployeeTravelExppense(dvDocument, documentEntry.getDisbursementVoucherDetail());
        } else {
            LOG.error("populateCustomAccountingDocumentData, did not find Disbursement Voucher Details");
        }
    }
    
    private void populateDisbursementVouchersGenericSections(CuDisbursementVoucherDocument dvDocument, DisbursementVoucherDetail dvDetail) {
        dvDocument.setDisbVchrBankCode(dvDetail.getBankCode());
        dvDocument.setDisbVchrContactPersonName(dvDetail.getContactName());
        dvDocument.setDisbVchrContactPhoneNumber(dvDetail.getContactPhoneNumber());
        dvDocument.setDisbVchrContactEmailId(dvDetail.getContactEmail());
        dvDocument.setCampusCode(dvDetail.getCampusCode());
    }
    
    private void populatePaymentInformation(CuDisbursementVoucherDocument dvDocument, DisbursementVoucherDetail dvDetail) {
        if (ObjectUtils.isNotNull(dvDetail.getPaymentInformation())) {
            DisbursementVoucherPaymentInfomration paymentInfo = dvDetail.getPaymentInformation();
            CuDisbursementVoucherPayeeDetail payeeDetail = dvDocument.getDvPayeeDetail();
            payeeDetail.setDisbVchrPaymentReasonCode(paymentInfo.getPaymentReasonCode());
            payeeDetail.setDisbursementVoucherPayeeTypeCode(paymentInfo.getPayeeTypeCode());
            payeeDetail.setDisbVchrPayeeIdNumber(paymentInfo.getPayeeId());
            payeeDetail.setDisbVchrPayeePersonName(paymentInfo.getPayeeName());
            payeeDetail.setDisbVchrPayeeLine1Addr(paymentInfo.getAddressLine1());
            payeeDetail.setDisbVchrPayeeLine2Addr(paymentInfo.getAddressLine2());
            payeeDetail.setDisbVchrPayeeCityName(paymentInfo.getCity());
            payeeDetail.setDisbVchrPayeeStateCode(paymentInfo.getState());
            payeeDetail.setDisbVchrPayeeCountryCode(paymentInfo.getCountry());
            payeeDetail.setDisbVchrPayeeZipCode(paymentInfo.getPostalCode());
            payeeDetail.setDisbVchrSpecialHandlingPersonName(paymentInfo.getSpecialHandlingName());
            payeeDetail.setDisbVchrSpecialHandlingLine1Addr(paymentInfo.getSpecialHandlingAddress1());
            payeeDetail.setDisbVchrSpecialHandlingLine2Addr(paymentInfo.getSpecialHandlingAddress2());
            payeeDetail.setDisbVchrSpecialHandlingCityName(paymentInfo.getSpecialHandlingCity());
            payeeDetail.setDisbVchrSpecialHandlingStateCode(paymentInfo.getSpecialHandlingState());
            payeeDetail.setDisbVchrSpecialHandlingZipCode(paymentInfo.getSpecialHandlingZip());
            payeeDetail.setDisbVchrSpecialHandlingCountryCode(paymentInfo.getSpecialHandlingCountry());
            
            dvDocument.setDisbVchrCheckTotalAmount(paymentInfo.getCheckAmount());
            dvDocument.setDisbursementVoucherDueDate(new java.sql.Date(paymentInfo.getDueDate().getTime()));
            dvDocument.setDisbVchrPaymentMethodCode(paymentInfo.getPaymentMethod());
            dvDocument.setDisbVchrCheckStubText(paymentInfo.getCheckStubText());
            dvDocument.setDisbursementVoucherDocumentationLocationCode(paymentInfo.getDocumentationLocationCode());
            dvDocument.setDisbVchrAttachmentCode(convertStringToBoolean(paymentInfo.getAttachmentCode()));
            dvDocument.setDisbVchrSpecialHandlingCode(convertStringToBoolean(paymentInfo.getSpecialHandlingCode()));
            dvDocument.setDisbVchrPayeeW9CompleteCode(convertStringToBoolean(paymentInfo.getW9CompleteCode()));
            dvDocument.setDisbExcptAttachedIndicator(convertStringToBoolean(paymentInfo.getExceptionAttachedCode()));
        } else {
            LOG.error("populatePaymentInformation, did NOT find payment info");
        }
    }
    
    private void populateNonEmployeeTravelExppense(CuDisbursementVoucherDocument dvDocument, DisbursementVoucherDetail dvDetail) {
        if (ObjectUtils.isNotNull(dvDetail.getNonEmployeeTravel())) {
            DisbursementVoucherNonEmployeeTravel nonEmployeeTravel = dvDetail.getNonEmployeeTravel();
            //org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeTravel nonEmployeeTravelDoc = dvDocument.getDvNonEmployeeTravel();
            dvDocument.getDvNonEmployeeTravel().setDisbVchrNonEmpTravelerName(nonEmployeeTravel.getTravelerName());
            dvDocument.getDvNonEmployeeTravel().setDisbVchrServicePerformedDesc(nonEmployeeTravel.getServicePerformed());
            dvDocument.getDvNonEmployeeTravel().setDvServicePerformedLocName(nonEmployeeTravel.getServicePerformedLocationName());
            dvDocument.getDvNonEmployeeTravel().setDvServiceRegularEmprName(nonEmployeeTravel.getServicePerformedRegularEmployeeName());
            dvDocument.getDvNonEmployeeTravel().setDisbVchrTravelToCityName(nonEmployeeTravel.getTravelToCity());
            dvDocument.getDvNonEmployeeTravel().setDisbVchrTravelToStateCode(nonEmployeeTravel.getTravelToState());
            dvDocument.getDvNonEmployeeTravel().setDisbVchrTravelToCountryCode(nonEmployeeTravel.getTravelToCountry());
            dvDocument.getDvNonEmployeeTravel().setDisbVchrTravelFromCityName(nonEmployeeTravel.getTravelFromCity());
            dvDocument.getDvNonEmployeeTravel().setDisbVchrTravelFromStateCode(nonEmployeeTravel.getTravelFromState());
            dvDocument.getDvNonEmployeeTravel().setDvTravelFromCountryCode(nonEmployeeTravel.getTravelFromCountry());
            dvDocument.getDvNonEmployeeTravel().setDvPerdiemStartDttmStamp(new Timestamp(nonEmployeeTravel.getPerdiemStartDate().getTime()));
            dvDocument.getDvNonEmployeeTravel().setDvPerdiemEndDttmStamp(new Timestamp(nonEmployeeTravel.getPerdiemEndDate().getTime()));
            dvDocument.getDvNonEmployeeTravel().setDisbVchrPerdiemCategoryName(nonEmployeeTravel.getPerdiemCategory());
            dvDocument.getDvNonEmployeeTravel().setDisbVchrAutoFromCityName(nonEmployeeTravel.getAutoFromCity());
            dvDocument.getDvNonEmployeeTravel().setDisbVchrAutoFromStateCode(nonEmployeeTravel.getAutoFromState());
            dvDocument.getDvNonEmployeeTravel().setDisbVchrAutoToCityName(nonEmployeeTravel.getAutoToCity());
            dvDocument.getDvNonEmployeeTravel().setDisbVchrAutoToStateCode(nonEmployeeTravel.getAutoToState());
            dvDocument.getDvNonEmployeeTravel().setDisbVchrPerdiemRate(nonEmployeeTravel.getPerdiemRate());
            pupulatePerdiem(dvDocument, nonEmployeeTravel);
            dvDocument.getDvNonEmployeeTravel().setDisbVchrAutoRoundTripCode(convertStringToBoolean(nonEmployeeTravel.getRoundTripCode()));
            populateMilage(dvDocument, nonEmployeeTravel);
        } else {
            LOG.info("populateNonEmployeeTravelExppense, no non employee travel information in XML");
        }
    }

    protected void populateMilage(CuDisbursementVoucherDocument dvDocument, DisbursementVoucherNonEmployeeTravel nonEmployeeTravel) {
        dvDocument.getDvNonEmployeeTravel().setDvPersonalCarMileageAmount(nonEmployeeTravel.getPersonalCarMilageAmount().intValue());
        KualiDecimal caluclatedMilageAmount = disbursementVoucherTravelService.calculateMileageAmount(dvDocument.getDvNonEmployeeTravel().getDvPersonalCarMileageAmount(), 
                dvDocument.getDvNonEmployeeTravel().getDvPerdiemStartDttmStamp());
        dvDocument.getDvNonEmployeeTravel().setDisbVchrMileageCalculatedAmt(caluclatedMilageAmount);
        if (ObjectUtils.isNotNull(nonEmployeeTravel.getPersonalCarAmount())) {
            dvDocument.getDvNonEmployeeTravel().setDisbVchrPersonalCarAmount(nonEmployeeTravel.getPersonalCarAmount());
        } else {
            dvDocument.getDvNonEmployeeTravel().setDisbVchrPersonalCarAmount(caluclatedMilageAmount);
        }
    }

    protected void pupulatePerdiem(CuDisbursementVoucherDocument dvDocument, DisbursementVoucherNonEmployeeTravel nonEmployeeTravel) {
        KualiDecimal caluclatedPerDiemAmount = disbursementVoucherTravelService.calculatePerDiemAmount(dvDocument.getDvNonEmployeeTravel().getDvPerdiemStartDttmStamp(), 
                dvDocument.getDvNonEmployeeTravel().getDvPerdiemEndDttmStamp(), dvDocument.getDvNonEmployeeTravel().getDisbVchrPerdiemRate());
        dvDocument.getDvNonEmployeeTravel().setDisbVchrPerdiemCalculatedAmt(caluclatedPerDiemAmount);
        if (ObjectUtils.isNotNull(nonEmployeeTravel.getPerdiemActualAmount())) {
            dvDocument.getDvNonEmployeeTravel().setDisbVchrPerdiemActualAmount(nonEmployeeTravel.getPerdiemActualAmount());
        } else {
            dvDocument.getDvNonEmployeeTravel().setDisbVchrPerdiemActualAmount(caluclatedPerDiemAmount  );
        }
        dvDocument.getDvNonEmployeeTravel().setDvPerdiemChangeReasonText(nonEmployeeTravel.getPerdiemChangeReasonText());
    }
    
    private boolean convertStringToBoolean(String stringBoolean) {
        return StringUtils.equalsIgnoreCase("T", stringBoolean);
    }
    
    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    public void setDisbursementVoucherTravelService(DisbursementVoucherTravelService disbursementVoucherTravelService) {
        this.disbursementVoucherTravelService = disbursementVoucherTravelService;
    }

}
