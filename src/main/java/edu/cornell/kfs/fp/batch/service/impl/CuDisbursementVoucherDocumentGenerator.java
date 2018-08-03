package edu.cornell.kfs.fp.batch.service.impl;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.function.Supplier;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeExpense;
import org.kuali.kfs.fp.document.service.DisbursementVoucherTravelService;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherDetailXml;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherNonEmployeeExpenseXml;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherNonEmployeeTravelXml;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherPaymentInfomrationXml;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherPreConferenceRegistrantXml;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherPrePaidTravelOverviewXml;
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
            populateNonEmployeeTravelExpense(dvDocument, documentEntry.getDisbursementVoucherDetail());
            populatePreConferenceDetail(dvDocument, documentEntry.getDisbursementVoucherDetail());
        } else {
            LOG.error("populateCustomAccountingDocumentData, did not find Disbursement Voucher Details");
        }
    }
    
    protected void populateDisbursementVouchersGenericSections(CuDisbursementVoucherDocument dvDocument, DisbursementVoucherDetailXml dvDetail) {
        dvDocument.setDisbVchrBankCode(dvDetail.getBankCode());
        dvDocument.setDisbVchrContactPersonName(dvDetail.getContactName());
        dvDocument.setDisbVchrContactPhoneNumber(dvDetail.getContactPhoneNumber());
        dvDocument.setDisbVchrContactEmailId(dvDetail.getContactEmail());
        dvDocument.setCampusCode(dvDetail.getCampusCode());
    }
    
    protected void populatePaymentInformation(CuDisbursementVoucherDocument dvDocument, DisbursementVoucherDetailXml dvDetail) {
        if (ObjectUtils.isNotNull(dvDetail.getPaymentInformation())) {
            DisbursementVoucherPaymentInfomrationXml paymentInfo = dvDetail.getPaymentInformation();
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
            dvDocument.setDisbursementVoucherDueDate(buildSqlDateFromUtilDate(paymentInfo.getDueDate()));
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
    
    private void populateNonEmployeeTravelExpense(CuDisbursementVoucherDocument dvDocument, DisbursementVoucherDetailXml dvDetail) {
        if (ObjectUtils.isNotNull(dvDetail.getNonEmployeeTravel())) {
            DisbursementVoucherNonEmployeeTravelXml nonEmployeeTravel = dvDetail.getNonEmployeeTravel();
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
            dvDocument.getDvNonEmployeeTravel().setDvPerdiemStartDttmStamp(buildTimestampFromUtilDate(nonEmployeeTravel.getPerdiemStartDate()));
            dvDocument.getDvNonEmployeeTravel().setDvPerdiemEndDttmStamp(buildTimestampFromUtilDate(nonEmployeeTravel.getPerdiemEndDate()));
            dvDocument.getDvNonEmployeeTravel().setDisbVchrPerdiemCategoryName(nonEmployeeTravel.getPerdiemCategory());
            dvDocument.getDvNonEmployeeTravel().setDisbVchrAutoFromCityName(nonEmployeeTravel.getAutoFromCity());
            dvDocument.getDvNonEmployeeTravel().setDisbVchrAutoFromStateCode(nonEmployeeTravel.getAutoFromState());
            dvDocument.getDvNonEmployeeTravel().setDisbVchrAutoToCityName(nonEmployeeTravel.getAutoToCity());
            dvDocument.getDvNonEmployeeTravel().setDisbVchrAutoToStateCode(nonEmployeeTravel.getAutoToState());
            dvDocument.getDvNonEmployeeTravel().setDisbVchrPerdiemRate(nonEmployeeTravel.getPerdiemRate());
            populatePerdiem(dvDocument, nonEmployeeTravel);
            populateMileage(dvDocument, nonEmployeeTravel);
            populateNonEmployeeTravelExpenses(dvDocument, nonEmployeeTravel);
            populateNonEmployeeTravelPrePaidExpenses(dvDocument, nonEmployeeTravel);
        } else {
            LOG.info("populateNonEmployeeTravelExpense, no non employee travel information in XML");
        }
    }
    
    protected void populatePerdiem(CuDisbursementVoucherDocument dvDocument, DisbursementVoucherNonEmployeeTravelXml nonEmployeeTravel) {
        KualiDecimal calculatedPerDiemAmount = disbursementVoucherTravelService.calculatePerDiemAmount(dvDocument.getDvNonEmployeeTravel().getDvPerdiemStartDttmStamp(), 
                dvDocument.getDvNonEmployeeTravel().getDvPerdiemEndDttmStamp(), dvDocument.getDvNonEmployeeTravel().getDisbVchrPerdiemRate());
        dvDocument.getDvNonEmployeeTravel().setDisbVchrPerdiemCalculatedAmt(calculatedPerDiemAmount);
        if (ObjectUtils.isNotNull(nonEmployeeTravel.getPerdiemActualAmount())) {
            dvDocument.getDvNonEmployeeTravel().setDisbVchrPerdiemActualAmount(nonEmployeeTravel.getPerdiemActualAmount());
        } else {
            dvDocument.getDvNonEmployeeTravel().setDisbVchrPerdiemActualAmount(calculatedPerDiemAmount  );
        }
        dvDocument.getDvNonEmployeeTravel().setDvPerdiemChangeReasonText(nonEmployeeTravel.getPerdiemChangeReasonText());
    }
    
    protected void populateMileage(CuDisbursementVoucherDocument dvDocument, DisbursementVoucherNonEmployeeTravelXml nonEmployeeTravel) {
        dvDocument.getDvNonEmployeeTravel().setDisbVchrAutoRoundTripCode(convertStringToBoolean(nonEmployeeTravel.getRoundTripCode()));
        dvDocument.getDvNonEmployeeTravel().setDvPersonalCarMileageAmount(nonEmployeeTravel.getPersonalCarMileageAmount().intValue());
        KualiDecimal caluclatedMilageAmount = disbursementVoucherTravelService.calculateMileageAmount(dvDocument.getDvNonEmployeeTravel().getDvPersonalCarMileageAmount(), 
                dvDocument.getDvNonEmployeeTravel().getDvPerdiemStartDttmStamp());
        dvDocument.getDvNonEmployeeTravel().setDisbVchrMileageCalculatedAmt(caluclatedMilageAmount);
        if (ObjectUtils.isNotNull(nonEmployeeTravel.getPersonalCarAmount())) {
            dvDocument.getDvNonEmployeeTravel().setDisbVchrPersonalCarAmount(nonEmployeeTravel.getPersonalCarAmount());
        } else {
            dvDocument.getDvNonEmployeeTravel().setDisbVchrPersonalCarAmount(caluclatedMilageAmount);
        }
    }
    
    protected void populateNonEmployeeTravelExpenses(CuDisbursementVoucherDocument dvDocument, DisbursementVoucherNonEmployeeTravelXml nonEmployeeTravel) {
        if (CollectionUtils.isNotEmpty(nonEmployeeTravel.getTravelerExpenses())) {
            for (DisbursementVoucherNonEmployeeExpenseXml expenseXml : nonEmployeeTravel.getTravelerExpenses()) {
                org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeExpense dvExpense = buildNonEmployeeExpense();
                dvExpense.setDisbVchrExpenseCode(expenseXml.getExpenseType());
                dvExpense.setDisbVchrExpenseCompanyName(expenseXml.getCompnayName());
                dvExpense.setDisbVchrExpenseAmount(expenseXml.getAmount());
                dvExpense.setDocumentNumber(dvDocument.getDocumentNumber());
                dvExpense.setNewCollectionRecord(true);
                dvDocument.getDvNonEmployeeTravel().addDvNonEmployeeExpenseLine(dvExpense);
            }
        }
    }

    protected void populateNonEmployeeTravelPrePaidExpenses(CuDisbursementVoucherDocument dvDocument, DisbursementVoucherNonEmployeeTravelXml nonEmployeeTravel) {
        if (CollectionUtils.isNotEmpty(nonEmployeeTravel.getPrepaidExpenses())) {
            for (DisbursementVoucherNonEmployeeExpenseXml expenseXml : nonEmployeeTravel.getPrepaidExpenses()) {
                org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeExpense dvExpense = buildNonEmployeeExpense();
                dvExpense.setDisbVchrPrePaidExpenseCode(expenseXml.getExpenseType());
                dvExpense.setDisbVchrPrePaidExpenseCompanyName(expenseXml.getCompnayName());
                dvExpense.setDisbVchrExpenseAmount(expenseXml.getAmount());
                dvExpense.setDocumentNumber(dvDocument.getDocumentNumber());
                dvExpense.setNewCollectionRecord(true);
                dvDocument.getDvNonEmployeeTravel().addDvPrePaidEmployeeExpenseLine(dvExpense);
            }
        }
    }
    
    protected DisbursementVoucherNonEmployeeExpense buildNonEmployeeExpense() {
        return new DisbursementVoucherNonEmployeeExpense();
    }
    
    protected void populatePreConferenceDetail(CuDisbursementVoucherDocument dvDocument, DisbursementVoucherDetailXml dvDetail) {
        if (ObjectUtils.isNotNull(dvDetail.getPrePaidTravelOverview())) {
            DisbursementVoucherPrePaidTravelOverviewXml overView = dvDetail.getPrePaidTravelOverview();
            dvDocument.getDvPreConferenceDetail().setDvConferenceDestinationName(overView.getLocation());
            dvDocument.getDvPreConferenceDetail().setDisbVchrExpenseCode(overView.getType());
            dvDocument.getDvPreConferenceDetail().setDisbVchrConferenceStartDate(buildSqlDateFromUtilDate(overView.getStartDate()));
            dvDocument.getDvPreConferenceDetail().setDisbVchrConferenceEndDate(buildSqlDateFromUtilDate(overView.getEndDate()));
            if (CollectionUtils.isNotEmpty(overView.getRegistrants())) {
                for (DisbursementVoucherPreConferenceRegistrantXml registrantXml : overView.getRegistrants()) {
                    org.kuali.kfs.fp.businessobject.DisbursementVoucherPreConferenceRegistrant dvRegistrant = new org.kuali.kfs.fp.businessobject.DisbursementVoucherPreConferenceRegistrant();
                    dvRegistrant.setDocumentNumber(dvDocument.getDocumentNumber());
                    dvRegistrant.setDvConferenceRegistrantName(registrantXml.getName());
                    dvRegistrant.setDisbVchrPreConfDepartmentCd(registrantXml.getDepartmentCode());
                    dvRegistrant.setDvPreConferenceRequestNumber(registrantXml.getPreConferenceRequestNuumber());
                    dvRegistrant.setDisbVchrExpenseAmount(registrantXml.getAmount());
                    dvDocument.addDvPrePaidRegistrantLine(dvRegistrant);
                }
            }
        } else {
           LOG.info("populatePreConferenceDetail, no pre paid expenses found"); 
        }
    }

    protected boolean convertStringToBoolean(String stringBoolean) {
        return StringUtils.equalsIgnoreCase("T", stringBoolean);
    }
    
    private Date buildSqlDateFromUtilDate(java.util.Date utilDate) {
        if (ObjectUtils.isNotNull(utilDate)) {
            return new Date(utilDate.getTime());
        } else {
            return null;
        }
    }
    
    private Timestamp buildTimestampFromUtilDate(java.util.Date utilDate) {
        if (ObjectUtils.isNotNull(utilDate)) {
            return new Timestamp(utilDate.getTime());
        } else {
            return null;
        }
    }
    
    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    public void setDisbursementVoucherTravelService(DisbursementVoucherTravelService disbursementVoucherTravelService) {
        this.disbursementVoucherTravelService = disbursementVoucherTravelService;
    }

}
