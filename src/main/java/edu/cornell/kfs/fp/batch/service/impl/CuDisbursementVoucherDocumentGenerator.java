package edu.cornell.kfs.fp.batch.service.impl;

import java.sql.Date;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.document.service.DisbursementVoucherTravelService;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.springframework.util.AutoPopulatingList;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItemDetail;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherDetailXml;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherPaymentInformationXml;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherPreConferenceRegistrantXml;
import edu.cornell.kfs.fp.batch.xml.DisbursementVoucherPrePaidTravelOverviewXml;
import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherDefaultDueDateService;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherPayeeService;
import edu.cornell.kfs.pdp.CUPdpConstants;

public class CuDisbursementVoucherDocumentGenerator extends AccountingDocumentGeneratorBase<CuDisbursementVoucherDocument> {
	private static final Logger LOG = LogManager.getLogger(CuDisbursementVoucherDocumentGenerator.class);
    
    protected UniversityDateService universityDateService;
    protected DisbursementVoucherTravelService disbursementVoucherTravelService;
    protected VendorService vendorService;
    protected BusinessObjectService businessObjectService;
    protected CuDisbursementVoucherDefaultDueDateService cuDisbursementVoucherDefaultDueDateService;
    protected CuDisbursementVoucherPayeeService cuDisbursementVoucherPayeeService;
    
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
            CuDisbursementVoucherPayeeDetail payeeDetail = (CuDisbursementVoucherPayeeDetail) dvDocument.getDvPayeeDetail();
            payeeDetail.setDisbVchrPaymentReasonCode(paymentInfo.getPaymentReasonCode());
            validateAndPopulatePayeeTypeCodeAndPayeeNumberAndPayeeName(paymentInfo, payeeDetail);
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

    protected void validateAndPopulatePayeeTypeCodeAndPayeeNumberAndPayeeName(DisbursementVoucherPaymentInformationXml paymentInfo, CuDisbursementVoucherPayeeDetail payeeDetail) throws ValidationException {
        String payeeTypeCode = paymentInfo.getPayeeTypeCode();
        String payeeId = paymentInfo.getPayeeId();
        boolean isEmployee = false;
        String convertedPayeeTypeCode = payeeTypeCode;
        String vendorPayeeName = StringUtils.EMPTY;
        if (StringUtils.equalsAnyIgnoreCase(payeeTypeCode, CUPdpConstants.PAYEE_TYPE_CODE_VENDOR)) {
            VendorDetail vendorDetail = vendorService.getByVendorNumber(payeeId);
            if (ObjectUtils.isNull(vendorDetail) || ObjectUtils.isNull(vendorDetail.getVendorHeader())) {
                throwPayeeIdValidationError(CuFPKeyConstants.CREATE_ACCOUNTING_DOCUMENT_VENDOR_ID_BAD, payeeId);
            }
            convertedPayeeTypeCode = cuDisbursementVoucherPayeeService.getPayeeTypeCodeForVendorType(
                    vendorDetail.getVendorHeader().getVendorTypeCode());
            addWarningMessageIfPayeeNameNotAccurate(vendorDetail.getVendorName(), paymentInfo.getPayeeName());
            vendorPayeeName = vendorDetail.getVendorName();
        } else if (StringUtils.equalsAnyIgnoreCase(payeeTypeCode, CUPdpConstants.PAYEE_TYPE_CODE_EMPLOYEE)) {
            Person employee = personService.getPersonByEmployeeId(payeeId);
            if (ObjectUtils.isNull(employee)) {
                throwPayeeIdValidationError(CuFPKeyConstants.CREATE_ACCOUNTING_DOCUMENT_EMPLOYEE_ID_BAD, payeeId);
            } else {
                isEmployee = true;
                addWarningMessageIfPayeeNameNotAccurate(employee.getName(), paymentInfo.getPayeeName());
                vendorPayeeName = employee.getName();
            }
        } else if (StringUtils.equalsAnyIgnoreCase(payeeTypeCode, CUPdpConstants.PAYEE_TYPE_CODE_ALUMNI) || StringUtils.equalsAnyIgnoreCase(payeeTypeCode, CUPdpConstants.PAYEE_TYPE_CODE_STUDENT)) {
            Person person = personService.getPerson(payeeId);
            if (ObjectUtils.isNull(person)) {
                throwPayeeIdValidationError(CuFPKeyConstants.CREATE_ACCOUNTING_DOCUMENT_PRINCIPLE_ID_BAD, payeeId);
            } else {
                addWarningMessageIfPayeeNameNotAccurate(person.getName(), paymentInfo.getPayeeName());
                vendorPayeeName = person.getName();
            }
        } else {
            String payeeTypeCodeErrorMessage = configurationService.getPropertyValueAsString(CuFPKeyConstants.CREATE_ACCOUNTING_DOCUMENT_PAYEE_TYPE_CODE_BAD);
            throw new ValidationException(MessageFormat.format(payeeTypeCodeErrorMessage, payeeTypeCode));
        }
        payeeDetail.setDisbVchrPayeeEmployeeCode(isEmployee);
        payeeDetail.setDisbursementVoucherPayeeTypeCode(convertedPayeeTypeCode);
        payeeDetail.setDisbVchrPayeeIdNumber(payeeId);
        payeeDetail.setDisbVchrPayeePersonName(vendorPayeeName);
    }
    
    protected void throwPayeeIdValidationError(String errorMessageKey, String payeeId) { 
        String employeeErrorMessage = configurationService.getPropertyValueAsString(errorMessageKey);
        throw new ValidationException(MessageFormat.format(employeeErrorMessage, payeeId));
    }
    
    protected void addWarningMessageIfPayeeNameNotAccurate(String vendorPayeeName, String dvPayeeName) {
        if (StringUtils.isNotBlank(dvPayeeName) && !StringUtils.equalsIgnoreCase(vendorPayeeName, dvPayeeName)) {
            AutoPopulatingList<ErrorMessage> warningMessages = new AutoPopulatingList<>(ErrorMessage.class);
            String messageString = MessageFormat.format(
                    configurationService.getPropertyValueAsString(
                            CuFPKeyConstants.CREATE_ACCOUNTING_DOCUMENT_PAYEE_NAME_NOT_SAME_AS_VENDOR),
                    vendorPayeeName, dvPayeeName);
            warningMessages.add(new ErrorMessage(CuFPConstants.CreateAccountingDocumentValidatedDataElements.PAYEE_NAME, messageString));

            LOG.warn("addWarningMessageIfPayeeNameNotAccurate, " + messageString);

            GlobalVariables.getMessageMap().getWarningMessages()
                    .put(CuFPConstants.CreateAccountingDocumentValidatedDataElements.PAYEE_NAME, warningMessages);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addWarningMessageIfPayeeNameNotAccurate, the vendorPayeeName: '" + vendorPayeeName + "' dvPayeeName: '" + dvPayeeName + "'");
            }
        }
    }
    
    private void populateAddressFields(DisbursementVoucherPaymentInformationXml paymentInfo, CuDisbursementVoucherDocument dvDocument) {
        if (StringUtils.isNotBlank(paymentInfo.getPayeeAddressId())) {
            VendorAddress vendorAddress = findVendorAddress(paymentInfo);
            dvDocument.templateVendor(vendorAddress.getVendorDetail(), vendorAddress);
        } else {
            CuDisbursementVoucherPayeeDetail payeeDetail = (CuDisbursementVoucherPayeeDetail) dvDocument.getDvPayeeDetail();
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
    
    @Override
    public void handleDocumentWarningMessage(CreateAccountingDocumentReportItemDetail reportDetail) {
        if (!GlobalVariables.getMessageMap().getWarningMessages().isEmpty()) {
            GlobalVariables.getMessageMap().getWarningMessages().entrySet().forEach(message -> processWarning(message, reportDetail));
        }
    }
    
    protected void processWarning(Entry<String, AutoPopulatingList<ErrorMessage>> message, CreateAccountingDocumentReportItemDetail reportDetail) {
        if (StringUtils.equalsIgnoreCase(CuFPConstants.CreateAccountingDocumentValidatedDataElements.PAYEE_NAME, message.getKey())) {
            message.getValue().stream().forEach(em -> reportDetail.appendWarningMessageToExistingWarningMessage(em.getMessageParameters()[0]));
        } else {
            LOG.warn("processWarning, unexpected warning message with a key of " + message.getKey());
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

    public void setCuDisbursementVoucherPayeeService(CuDisbursementVoucherPayeeService cuDisbursementVoucherPayeeService) {
        this.cuDisbursementVoucherPayeeService = cuDisbursementVoucherPayeeService;
    }

}
