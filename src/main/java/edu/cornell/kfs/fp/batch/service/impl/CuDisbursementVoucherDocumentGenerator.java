package edu.cornell.kfs.fp.batch.service.impl;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeExpense;
import org.kuali.kfs.fp.document.service.DisbursementVoucherTravelService;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherDetailXml;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherNonEmployeeExpenseXml;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherNonEmployeeTravelXml;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherPaymentInformationXml;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherPreConferenceRegistrantXml;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherPrePaidTravelOverviewXml;
import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherDefaultDueDateService;
import edu.cornell.kfs.pdp.CUPdpConstants;

public class CuDisbursementVoucherDocumentGenerator extends AccountingDocumentGeneratorBase<CuDisbursementVoucherDocument> {
	private static final Logger LOG = LogManager.getLogger(CuDisbursementVoucherDocumentGenerator.class);
    
    protected UniversityDateService universityDateService;
    protected DisbursementVoucherTravelService disbursementVoucherTravelService;
    protected VendorService vendorService;
    protected BusinessObjectService businessObjectService;
    protected CuDisbursementVoucherDefaultDueDateService cuDisbursementVoucherDefaultDueDateService;
    
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
            DisbursementVoucherPaymentInformationXml paymentInfo = dvDetail.getPaymentInformation();
            CuDisbursementVoucherPayeeDetail payeeDetail = dvDocument.getDvPayeeDetail();
            payeeDetail.setDisbVchrPaymentReasonCode(paymentInfo.getPaymentReasonCode());
            validateAndPopulatePayeeTypeCodeAndPayeeNumber(paymentInfo, payeeDetail);
            payeeDetail.setDisbVchrPayeePersonName(paymentInfo.getPayeeName());
            populateAddressFields(paymentInfo, dvDocument);
            payeeDetail.setDisbVchrSpecialHandlingPersonName(paymentInfo.getSpecialHandlingName());
            payeeDetail.setDisbVchrSpecialHandlingLine1Addr(paymentInfo.getSpecialHandlingAddress1());
            payeeDetail.setDisbVchrSpecialHandlingLine2Addr(paymentInfo.getSpecialHandlingAddress2());
            payeeDetail.setDisbVchrSpecialHandlingCityName(paymentInfo.getSpecialHandlingCity());
            payeeDetail.setDisbVchrSpecialHandlingStateCode(paymentInfo.getSpecialHandlingState());
            payeeDetail.setDisbVchrSpecialHandlingZipCode(paymentInfo.getSpecialHandlingZip());
            payeeDetail.setDisbVchrSpecialHandlingCountryCode(paymentInfo.getSpecialHandlingCountry());
            dvDocument.setDisbVchrCheckTotalAmount(paymentInfo.getCheckAmount());
            if (paymentInfo.getInvoiceDate() != null) {
                dvDocument.setInvoiceDate(buildSqlDateFromUtilDate(paymentInfo.getInvoiceDate()));
            }
            dvDocument.setInvoiceNumber(paymentInfo.getInvoiceNumber());
            
            if (paymentInfo.getDueDate() == null) {
                LOG.info("populatePaymentInformation, no due date defined on the XML, calculating default due date.");
                dvDocument.setDisbursementVoucherDueDate(cuDisbursementVoucherDefaultDueDateService.findDefaultDueDate());
            } else {
                dvDocument.setDisbursementVoucherDueDate(buildSqlDateFromUtilDate(paymentInfo.getDueDate()));
            }
            
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

    protected void validateAndPopulatePayeeTypeCodeAndPayeeNumber(DisbursementVoucherPaymentInformationXml paymentInfo, CuDisbursementVoucherPayeeDetail payeeDetail) throws ValidationException {
        String payeeTypeCode = paymentInfo.getPayeeTypeCode();
        String payeeId = paymentInfo.getPayeeId();
        boolean isEmployee = false;
        if (StringUtils.equalsAnyIgnoreCase(payeeTypeCode, CUPdpConstants.PAYEE_TYPE_CODE_VENDOR)) {
            if (ObjectUtils.isNull(vendorService.getByVendorNumber(payeeId))) {
                String vendorErrorMessage = configurationService.getPropertyValueAsString(CuFPKeyConstants.CREATE_ACCOUNTING_DOCUMENT_VENDOR_ID_BAD);
                throw new ValidationException(MessageFormat.format(vendorErrorMessage,  payeeId));
            }
        } else if (StringUtils.equalsAnyIgnoreCase(payeeTypeCode, CUPdpConstants.PAYEE_TYPE_CODE_EMPLOYEE)) {
            if (ObjectUtils.isNull(personService.getPersonByEmployeeId(payeeId))) {
                String employeeErrorMessage = configurationService.getPropertyValueAsString(CuFPKeyConstants.CREATE_ACCOUNTING_DOCUMENT_EMPLOYEE_ID_BAD);
                throw new ValidationException(MessageFormat.format(employeeErrorMessage, payeeId));
            } else {
                isEmployee = true;
            }
        } else if (StringUtils.equalsAnyIgnoreCase(payeeTypeCode, CUPdpConstants.PAYEE_TYPE_CODE_ALUMNI) || StringUtils.equalsAnyIgnoreCase(payeeTypeCode, CUPdpConstants.PAYEE_TYPE_CODE_STUDENT)) {
            if (ObjectUtils.isNull(personService.getPerson(payeeId))) {
                String personError = configurationService.getPropertyValueAsString(CuFPKeyConstants.CREATE_ACCOUNTING_DOCUMENT_PRINCIPLE_ID_BAD);
                throw new ValidationException(MessageFormat.format(personError, payeeId));
            }
        } else {
            String payeeTypeCodeErrorMessage = configurationService.getPropertyValueAsString(CuFPKeyConstants.CREATE_ACCOUNTING_DOCUMENT_PAYEE_TYPE_CODE_BAD);
            throw new ValidationException(MessageFormat.format(payeeTypeCodeErrorMessage, payeeTypeCode));
        }
        payeeDetail.setDisbVchrPayeeEmployeeCode(isEmployee);
        payeeDetail.setDisbursementVoucherPayeeTypeCode(payeeTypeCode);
        payeeDetail.setDisbVchrPayeeIdNumber(payeeId);
    }
    
    private void populateAddressFields(DisbursementVoucherPaymentInformationXml paymentInfo, CuDisbursementVoucherDocument dvDocument) {
        if (StringUtils.isNotBlank(paymentInfo.getPayeeAddressId())) {
            VendorAddress vendorAddress = findVendorAddress(paymentInfo);
            dvDocument.templateVendor(vendorAddress.getVendorDetail(), vendorAddress);
        } else {
            CuDisbursementVoucherPayeeDetail payeeDetail = dvDocument.getDvPayeeDetail();
            LOG.info("populateAddressFields, no payee address ID provided, so using the address fields from the XML");
            payeeDetail.setDisbVchrPayeeLine1Addr(paymentInfo.getAddressLine1());
            payeeDetail.setDisbVchrPayeeLine2Addr(paymentInfo.getAddressLine2());
            payeeDetail.setDisbVchrPayeeCityName(paymentInfo.getCity());
            payeeDetail.setDisbVchrPayeeStateCode(paymentInfo.getState());
            payeeDetail.setDisbVchrPayeeCountryCode(paymentInfo.getCountry());
            payeeDetail.setDisbVchrPayeeZipCode(paymentInfo.getPostalCode());
        }
    }

    private VendorAddress findVendorAddress(DisbursementVoucherPaymentInformationXml paymentInfo) {
        Collection<VendorAddress> vendorAddresses;
        try {
            String[] payeeArray = StringUtils.split(paymentInfo.getPayeeId(), KFSConstants.DASH);
            if (LOG.isInfoEnabled()) {
                LOG.info("findVendorAddress, vendorHeaderGeneratedIdentifier: " + payeeArray[0] + 
                        " vendorDetailAssignedIdentifier: " + payeeArray[1] + 
                        ", payee address id: " + paymentInfo.getPayeeAddressId());
            }
            Map<String, String> fieldValues = new HashMap<String, String>();
            fieldValues.put(KFSPropertyConstants.VENDOR_HEADER_GENERATED_ID, payeeArray[0]);
            fieldValues.put(KFSPropertyConstants.VENDOR_DETAIL_ASSIGNED_ID, payeeArray[1]);
            fieldValues.put(KFSPropertyConstants.VENDOR_ADDRESS_GENERATED_ID, paymentInfo.getPayeeAddressId());
            fieldValues.put(KFSPropertyConstants.ACCOUNT_ACTIVE_INDICATOR,  KFSConstants.ACTIVE_INDICATOR);
            vendorAddresses = businessObjectService.findMatching(VendorAddress.class, fieldValues);
        } catch (Exception e) {
            LOG.error("findVendorAddress, there was an error attempting to find the vendor address", e);
            throw new ValidationException("Unable to find the vendor address: " + e.getMessage());
        }
        
        if (CollectionUtils.isEmpty(vendorAddresses)) {
            throw new ValidationException("Unable to find a vendor address for vendor number " + paymentInfo.getPayeeId() + 
                    " and vendor address id " + paymentInfo.getPayeeAddressId());
        }
        
        VendorAddress address = vendorAddresses.iterator().next();
        return address;
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
        KualiDecimal calculatedMileageAmount = disbursementVoucherTravelService.calculateMileageAmount(dvDocument.getDvNonEmployeeTravel().getDvPersonalCarMileageAmount(), 
                dvDocument.getDvNonEmployeeTravel().getDvPerdiemStartDttmStamp());
        dvDocument.getDvNonEmployeeTravel().setDisbVchrMileageCalculatedAmt(calculatedMileageAmount);
        if (ObjectUtils.isNotNull(nonEmployeeTravel.getPersonalCarAmount())) {
            dvDocument.getDvNonEmployeeTravel().setDisbVchrPersonalCarAmount(nonEmployeeTravel.getPersonalCarAmount());
        } else {
            dvDocument.getDvNonEmployeeTravel().setDisbVchrPersonalCarAmount(calculatedMileageAmount);
        }
    }
    
    protected void populateNonEmployeeTravelExpenses(CuDisbursementVoucherDocument dvDocument, DisbursementVoucherNonEmployeeTravelXml nonEmployeeTravel) {
        if (CollectionUtils.isNotEmpty(nonEmployeeTravel.getTravelerExpenses())) {
            for (DisbursementVoucherNonEmployeeExpenseXml expenseXml : nonEmployeeTravel.getTravelerExpenses()) {
                org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeExpense dvExpense = buildNonEmployeeExpense();
                dvExpense.setDisbVchrExpenseCode(expenseXml.getExpenseType());
                dvExpense.setDisbVchrExpenseCompanyName(expenseXml.getCompanyName());
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
                dvExpense.setDisbVchrPrePaidExpenseCompanyName(expenseXml.getCompanyName());
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
                    dvRegistrant.setDvPreConferenceRequestNumber(registrantXml.getPreConferenceRequestNumber());
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

    public void setVendorService(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
    
    public void setCuDisbursementVoucherDefaultDueDateService(CuDisbursementVoucherDefaultDueDateService cuDisbursementVoucherDefaultDueDateService) {
        this.cuDisbursementVoucherDefaultDueDateService = cuDisbursementVoucherDefaultDueDateService;
    }

}
