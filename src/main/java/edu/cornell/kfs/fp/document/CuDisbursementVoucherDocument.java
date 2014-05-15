package edu.cornell.kfs.fp.document;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonResidentAlienTax;
import org.kuali.kfs.fp.businessobject.WireCharge;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.service.DisbursementVoucherTaxService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AmountTotaling;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.service.PhoneNumberService;
import org.kuali.rice.core.api.parameter.ParameterEvaluator;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.COMPONENT;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.NAMESPACE;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.actiontaken.ActionTakenValue;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kew.engine.RouteContext;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.address.EntityAddress;
import org.kuali.rice.kns.util.KNSGlobalVariables;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetailExtension;
import edu.cornell.kfs.fp.document.interfaces.CULegacyTravelIntegrationInterface;


@NAMESPACE(namespace = KFSConstants.CoreModuleNamespaces.FINANCIAL)
@COMPONENT(component = "DisbursementVoucher")
public class CuDisbursementVoucherDocument extends DisbursementVoucherDocument implements CULegacyTravelIntegrationInterface {

    protected static final String DOCUMENT_REQUIRES_CAMPUS_REVIEW_SPLIT = "RequiresCampusReview";
    protected static final String DOCUMENT_REQUIRES_AWARD_REVIEW_SPLIT = "RequiresAwardReview";
    
    
    protected static final String OBJECT_CODES_REQUIRING_CAMPUS_REVIEW = "OBJECT_CODES_REQUIRING_CAMPUS_REVIEW";
    protected static final String PAYMENT_REASONS_REQUIRING_CAMPUS_REVIEW = "PAYMENT_REASONS_REQUIRING_CAMPUS_REVIEW";
    protected static final String DOLLAR_THRESHOLD_REQUIRING_CAMPUS_REVIEW = "DOLLAR_THRESHOLD_REQUIRING_CAMPUS_REVIEW";
    
    protected static final String DOLLAR_THRESHOLD_REQUIRING_TAX_REVIEW = "DOLLAR_THRESHOLD_REQUIRING_TAX_REVIEW";
    
    protected static final String DOLLAR_THRESHOLD_REQUIRING_AWARD_REVIEW = "DOLLAR_THRESHOLD_REQUIRING_AWARD_REVIEW";
    protected static final String OBJECT_CODES_REQUIRING_AWARD_REVIEW = "OBJECT_CODES_REQUIRING_AWARD_REVIEW";
    
    protected static final String DOLLAR_THRESHOLD_REQUIRING_TRAVEL_REVIEW = "DOLLAR_THRESHOLD_REQUIRING_TRAVEL_REVIEW";
    protected static final String OBJECT_CODES_REQUIRING_TRAVEL_REVIEW = "OBJECT_CODES_REQUIRING_TRAVEL_REVIEW";
    
    protected CuDisbursementVoucherPayeeDetail dvPayeeDetail;
    
    //TRIP INFORMATION FILEDS
    protected String tripAssociationStatusCode;
    protected String tripId;
    
    public CuDisbursementVoucherDocument() {
        super();
        dvPayeeDetail = new CuDisbursementVoucherPayeeDetail();
        
    }

    public void templateVendor(VendorDetail vendor, VendorAddress vendorAddress) {
        if (vendor == null) {
            return;
        }

        this.getDvPayeeDetail().setDisbursementVoucherPayeeTypeCode(CuDisbursementVoucherConstants.DV_PAYEE_TYPE_VENDOR);
        this.getDvPayeeDetail().setDisbVchrPayeeIdNumber(vendor.getVendorNumber());
        ((CuDisbursementVoucherPayeeDetailExtension) this.getDvPayeeDetail().getExtension()).setDisbVchrPayeeIdType(
                CuDisbursementVoucherConstants.DV_PAYEE_ID_TYP_VENDOR);
        this.getDvPayeeDetail().setDisbVchrPayeePersonName(vendor.getVendorName());

        this.getDvPayeeDetail().setDisbVchrAlienPaymentCode(vendor.getVendorHeader().getVendorForeignIndicator());


        if (ObjectUtils.isNotNull(vendorAddress) && ObjectUtils.isNotNull(vendorAddress.getVendorAddressGeneratedIdentifier())) {
            this.getDvPayeeDetail().setDisbVchrVendorAddressIdNumber(vendorAddress.getVendorAddressGeneratedIdentifier().toString());
            this.getDvPayeeDetail().setDisbVchrPayeeLine1Addr(vendorAddress.getVendorLine1Address());
            this.getDvPayeeDetail().setDisbVchrPayeeLine2Addr(vendorAddress.getVendorLine2Address());
            this.getDvPayeeDetail().setDisbVchrPayeeCityName(vendorAddress.getVendorCityName());
            this.getDvPayeeDetail().setDisbVchrPayeeStateCode(vendorAddress.getVendorStateCode());
            this.getDvPayeeDetail().setDisbVchrPayeeZipCode(vendorAddress.getVendorZipCode());
            this.getDvPayeeDetail().setDisbVchrPayeeCountryCode(vendorAddress.getVendorCountryCode());
        } else {
            this.getDvPayeeDetail().setDisbVchrVendorAddressIdNumber(StringUtils.EMPTY);
            this.getDvPayeeDetail().setDisbVchrPayeeLine1Addr(StringUtils.EMPTY);
            this.getDvPayeeDetail().setDisbVchrPayeeLine2Addr(StringUtils.EMPTY);
            this.getDvPayeeDetail().setDisbVchrPayeeCityName(StringUtils.EMPTY);
            this.getDvPayeeDetail().setDisbVchrPayeeStateCode(StringUtils.EMPTY);
            this.getDvPayeeDetail().setDisbVchrPayeeZipCode(StringUtils.EMPTY);
            this.getDvPayeeDetail().setDisbVchrPayeeCountryCode(StringUtils.EMPTY);
        }

        this.getDvPayeeDetail().setDisbVchrAlienPaymentCode(vendor.getVendorHeader().getVendorForeignIndicator());
        this.getDvPayeeDetail().setDvPayeeSubjectPaymentCode(VendorConstants.VendorTypes.SUBJECT_PAYMENT.equals(vendor.getVendorHeader().getVendorTypeCode()));
        this.getDvPayeeDetail().setDisbVchrEmployeePaidOutsidePayrollCode(getVendorService()
                .isVendorInstitutionEmployee(vendor.getVendorHeaderGeneratedIdentifier()));

        this.getDvPayeeDetail().setHasMultipleVendorAddresses(1 < vendor.getVendorAddresses().size());


        boolean w9AndW8Checked = false;
        if ((ObjectUtils.isNotNull(vendor.getVendorHeader().getVendorW9ReceivedIndicator())
                && vendor.getVendorHeader().getVendorW9ReceivedIndicator() == true) 
                || (ObjectUtils.isNotNull(vendor.getVendorHeader().getVendorW8BenReceivedIndicator()) 
                        && vendor.getVendorHeader().getVendorW8BenReceivedIndicator() == true)) {

            w9AndW8Checked = true;
        }

        this.disbVchrPayeeW9CompleteCode = w9AndW8Checked;

        Date vendorFederalWithholdingTaxBeginDate = vendor.getVendorHeader().getVendorFederalWithholdingTaxBeginningDate();
        Date vendorFederalWithholdingTaxEndDate = vendor.getVendorHeader().getVendorFederalWithholdingTaxEndDate();
        java.util.Date today = getDateTimeService().getCurrentDate();
        if ((vendorFederalWithholdingTaxBeginDate != null && vendorFederalWithholdingTaxBeginDate.before(today)) 
                && (vendorFederalWithholdingTaxEndDate == null || vendorFederalWithholdingTaxEndDate.after(today))) {
            this.disbVchrPayeeTaxControlCode = CuDisbursementVoucherConstants.TAX_CONTROL_CODE_BEGIN_WITHHOLDING;
        }

        // if vendor is foreign, default alien payment code to true
        if (getVendorService().isVendorForeign(vendor.getVendorHeaderGeneratedIdentifier())) {
            getDvPayeeDetail().setDisbVchrAlienPaymentCode(true);
        }
    }
    
    public void templateEmployee(Person employee) {
        if (employee == null) {
            return;
        }

        this.getDvPayeeDetail().setDisbursementVoucherPayeeTypeCode(DisbursementVoucherConstants.DV_PAYEE_TYPE_EMPLOYEE);
        if (StringUtils.isNotBlank(employee.getEmployeeId())) {
            this.getDvPayeeDetail().setDisbVchrPayeeIdNumber(employee.getEmployeeId());
            ((CuDisbursementVoucherPayeeDetailExtension) this.getDvPayeeDetail().getExtension()).setDisbVchrPayeeIdType(
                    CuDisbursementVoucherConstants.DV_PAYEE_ID_TYP_EMPL);
        } else {
            this.getDvPayeeDetail().setDisbVchrPayeeIdNumber(employee.getPrincipalId());
            ((CuDisbursementVoucherPayeeDetailExtension) this.getDvPayeeDetail().getExtension()).setDisbVchrPayeeIdType(
                    CuDisbursementVoucherConstants.DV_PAYEE_ID_TYP_ENTITY);
        }
        // Changed this from employee.getName to employee.getNameUnmasked() otherwise "Xxxxxx" appears on the DV!
        this.getDvPayeeDetail().setDisbVchrPayeePersonName(employee.getNameUnmasked());

        final ParameterService parameterService = this.getParameterService();

        if (parameterService.parameterExists(DisbursementVoucherDocument.class, DisbursementVoucherDocument.USE_DEFAULT_EMPLOYEE_ADDRESS_PARAMETER_NAME) 
                && parameterService.getParameterValueAsBoolean(
                        DisbursementVoucherDocument.class, DisbursementVoucherDocument.USE_DEFAULT_EMPLOYEE_ADDRESS_PARAMETER_NAME)) {
            this.getDvPayeeDetail().setDisbVchrPayeeLine1Addr(employee.getAddressLine1Unmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeLine2Addr(employee.getAddressLine2Unmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeCityName(employee.getAddressCityUnmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeStateCode(employee.getAddressStateProvinceCodeUnmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeZipCode(employee.getAddressPostalCodeUnmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeCountryCode(employee.getAddressCountryCodeUnmasked());
        } else {
            final EntityAddress address = getNonDefaultAddress(employee);
            if (address != null) {
                this.getDvPayeeDetail().setDisbVchrPayeeLine1Addr(address.getLine1Unmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeLine2Addr(address.getLine2Unmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeCityName(address.getCityUnmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeStateCode(address.getStateProvinceCodeUnmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeZipCode(address.getPostalCodeUnmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeCountryCode(address.getCountryCodeUnmasked());
            } else {
                this.getDvPayeeDetail().setDisbVchrPayeeLine1Addr("");
                this.getDvPayeeDetail().setDisbVchrPayeeLine2Addr("");
                this.getDvPayeeDetail().setDisbVchrPayeeCityName("");
                this.getDvPayeeDetail().setDisbVchrPayeeStateCode("");
                this.getDvPayeeDetail().setDisbVchrPayeeZipCode("");
                this.getDvPayeeDetail().setDisbVchrPayeeCountryCode("");
            }
        }

        //KFSMI-8935: When an employee is inactive, the Payment Type field on DV documents should display the message "Is this payee an employee" = No
        if (employee.isActive()) {
            this.getDvPayeeDetail().setDisbVchrPayeeEmployeeCode(true);
        } else {
            this.getDvPayeeDetail().setDisbVchrPayeeEmployeeCode(false);
        }

        // I'm assuming that if a tax id type code other than 'TAX' is present, then the employee must be foreign
        for (String externalIdentifierTypeCode : employee.getExternalIdentifiers().keySet()) {
            if (KimConstants.PersonExternalIdentifierTypes.TAX.equals(externalIdentifierTypeCode)) {
                this.getDvPayeeDetail().setDisbVchrAlienPaymentCode(false);
            }
        }
        // Determine if employee is a research subject
        ParameterEvaluator researchPaymentReasonCodeEvaluator = SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(
                DisbursementVoucherDocument.class, DisbursementVoucherConstants.RESEARCH_PAYMENT_REASONS_PARM_NM,
                this.getDvPayeeDetail().getDisbVchrPaymentReasonCode());
        if (researchPaymentReasonCodeEvaluator.evaluationSucceeds()) {
            if (getParameterService().parameterExists(DisbursementVoucherDocument.class,
                    DisbursementVoucherConstants.RESEARCH_NON_VENDOR_PAY_LIMIT_AMOUNT_PARM_NM)) {
                String researchPayLimit = getParameterService().getParameterValueAsString(
                        DisbursementVoucherDocument.class, DisbursementVoucherConstants.RESEARCH_NON_VENDOR_PAY_LIMIT_AMOUNT_PARM_NM);
                if (StringUtils.isNotBlank(researchPayLimit)) {
                    KualiDecimal payLimit = new KualiDecimal(researchPayLimit);

                    if (getDisbVchrCheckTotalAmount().isLessThan(payLimit)) {
                        this.getDvPayeeDetail().setDvPayeeSubjectPaymentCode(true);
                    }
                }
            }
        }

        this.disbVchrPayeeTaxControlCode = "";
        this.disbVchrPayeeW9CompleteCode = true;
    }
    
    /**
     * Convenience method to set dv payee detail fields based on a given student.
     * 
     * @param student
     */
    public void templateStudent(Person student) {
        if (student == null) {
            return;
        }

        this.getDvPayeeDetail().setDisbursementVoucherPayeeTypeCode(CuDisbursementVoucherConstants.DV_PAYEE_TYPE_STUDENT);
        
        this.getDvPayeeDetail().setDisbVchrPayeeIdNumber(student.getPrincipalId());
        ((CuDisbursementVoucherPayeeDetailExtension) this.getDvPayeeDetail().getExtension()).setDisbVchrPayeeIdType(
                CuDisbursementVoucherConstants.DV_PAYEE_ID_TYP_ENTITY);

        this.getDvPayeeDetail().setDisbVchrPayeePersonName(student.getNameUnmasked());

        final ParameterService parameterService = this.getParameterService();
        
        // Use the same parameter as for employees even though this is a student as basic intention is the same
        if (parameterService.parameterExists(DisbursementVoucherDocument.class, DisbursementVoucherDocument.USE_DEFAULT_EMPLOYEE_ADDRESS_PARAMETER_NAME)
                && parameterService.getParameterValueAsBoolean(DisbursementVoucherDocument.class,
                        DisbursementVoucherDocument.USE_DEFAULT_EMPLOYEE_ADDRESS_PARAMETER_NAME)) {
            this.getDvPayeeDetail().setDisbVchrPayeeLine1Addr(student.getAddressLine1Unmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeLine2Addr(student.getAddressLine2Unmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeCityName(student.getAddressCityUnmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeStateCode(student.getAddressStateProvinceCodeUnmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeZipCode(student.getAddressPostalCodeUnmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeCountryCode(student.getAddressCountryCodeUnmasked());
        } else {
            final EntityAddress address = getNonDefaultAddress(student);
            if (address != null) {
                this.getDvPayeeDetail().setDisbVchrPayeeLine1Addr(address.getLine1Unmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeLine2Addr(address.getLine2Unmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeCityName(address.getCityUnmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeStateCode(address.getStateProvinceCodeUnmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeZipCode(student.getAddressPostalCodeUnmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeCountryCode(address.getCountryCodeUnmasked());
            } else {
                this.getDvPayeeDetail().setDisbVchrPayeeLine1Addr("");
                this.getDvPayeeDetail().setDisbVchrPayeeLine2Addr("");
                this.getDvPayeeDetail().setDisbVchrPayeeCityName("");
                this.getDvPayeeDetail().setDisbVchrPayeeStateCode("");
                this.getDvPayeeDetail().setDisbVchrPayeeZipCode("");
                this.getDvPayeeDetail().setDisbVchrPayeeCountryCode("");            
            }
        }

        // I'm assuming that if a tax id type code other than 'TAX' is present, then the student must be foreign
        for (String externalIdentifierTypeCode : student.getExternalIdentifiers().keySet()) {
            if (KimConstants.PersonExternalIdentifierTypes.TAX.equals(externalIdentifierTypeCode)) {
                this.getDvPayeeDetail().setDisbVchrAlienPaymentCode(false);
            }
        }
        // Determine if student is a research subject
        
        
        ParameterEvaluator researchPaymentReasonCodeEvaluator = SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(
                DisbursementVoucherDocument.class, DisbursementVoucherConstants.RESEARCH_PAYMENT_REASONS_PARM_NM);
        if (researchPaymentReasonCodeEvaluator.evaluationSucceeds()) {
            if (getParameterService().parameterExists(DisbursementVoucherDocument.class,
                    DisbursementVoucherConstants.RESEARCH_NON_VENDOR_PAY_LIMIT_AMOUNT_PARM_NM)) {
                String researchPayLimit = getParameterService().getParameterValueAsString(DisbursementVoucherDocument.class,
                        DisbursementVoucherConstants.RESEARCH_NON_VENDOR_PAY_LIMIT_AMOUNT_PARM_NM);
                if (StringUtils.isNotBlank(researchPayLimit)) {
                    KualiDecimal payLimit = new KualiDecimal(researchPayLimit);

                    if (getDisbVchrCheckTotalAmount().isLessThan(payLimit)) {
                        this.getDvPayeeDetail().setDvPayeeSubjectPaymentCode(true);
                    }
                }
            }
        }

        this.disbVchrPayeeTaxControlCode = "";
        this.disbVchrPayeeW9CompleteCode = true;
    }
    
    /**
     * Convenience method to set dv payee detail fields based on a given Alumnus.
     * 
     * @param alumni
     */
    public void templateAlumni(Person alumni) {
        if (alumni == null) {
            return;
        }

        this.getDvPayeeDetail().setDisbursementVoucherPayeeTypeCode(CuDisbursementVoucherConstants.DV_PAYEE_TYPE_ALUMNI);
        
        this.getDvPayeeDetail().setDisbVchrPayeeIdNumber(alumni.getPrincipalId());
        ((CuDisbursementVoucherPayeeDetailExtension) this.getDvPayeeDetail().getExtension()).setDisbVchrPayeeIdType(
                CuDisbursementVoucherConstants.DV_PAYEE_ID_TYP_ENTITY);

        // Changed this from employee.getName to employee.getNameUnmasked() otherwise "Xxxxxx" appears on the DV!
        this.getDvPayeeDetail().setDisbVchrPayeePersonName(alumni.getNameUnmasked());

        final ParameterService parameterService = this.getParameterService();
        
        // Use the same parameter as for employees even though this is a alumni as basic intention is the same
        if (parameterService.parameterExists(DisbursementVoucherDocument.class, DisbursementVoucherDocument.USE_DEFAULT_EMPLOYEE_ADDRESS_PARAMETER_NAME) 
                && parameterService.getParameterValueAsBoolean(DisbursementVoucherDocument.class,
                        DisbursementVoucherDocument.USE_DEFAULT_EMPLOYEE_ADDRESS_PARAMETER_NAME)) {
            this.getDvPayeeDetail().setDisbVchrPayeeLine1Addr(alumni.getAddressLine1Unmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeLine2Addr(alumni.getAddressLine2Unmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeCityName(alumni.getAddressCityUnmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeStateCode(alumni.getAddressStateProvinceCodeUnmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeZipCode(alumni.getAddressPostalCodeUnmasked());
            this.getDvPayeeDetail().setDisbVchrPayeeCountryCode(alumni.getAddressCountryCodeUnmasked());
        } else {
            final EntityAddress address = getNonDefaultAddress(alumni);
            if (address != null) {
                this.getDvPayeeDetail().setDisbVchrPayeeLine1Addr(address.getLine1Unmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeLine2Addr(address.getLine2Unmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeCityName(address.getCityUnmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeStateCode(address.getStateProvinceCodeUnmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeZipCode(alumni.getAddressPostalCodeUnmasked());
                this.getDvPayeeDetail().setDisbVchrPayeeCountryCode(address.getCountryCodeUnmasked());
            } else {
                this.getDvPayeeDetail().setDisbVchrPayeeLine1Addr("");
                this.getDvPayeeDetail().setDisbVchrPayeeLine2Addr("");
                this.getDvPayeeDetail().setDisbVchrPayeeCityName("");
                this.getDvPayeeDetail().setDisbVchrPayeeStateCode("");
                this.getDvPayeeDetail().setDisbVchrPayeeZipCode("");
                this.getDvPayeeDetail().setDisbVchrPayeeCountryCode("");            
            }
        }

        // I'm assuming that if a tax id type code other than 'TAX' is present, then the alumni must be foreign
        for (String externalIdentifierTypeCode : alumni.getExternalIdentifiers().keySet()) {
            if (KimConstants.PersonExternalIdentifierTypes.TAX.equals(externalIdentifierTypeCode)) {
                this.getDvPayeeDetail().setDisbVchrAlienPaymentCode(false);
            }
        }
        // Determine if alumni is a research subject
        ParameterEvaluator researchPaymentReasonCodeEvaluator = SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(
                DisbursementVoucherDocument.class, DisbursementVoucherConstants.RESEARCH_PAYMENT_REASONS_PARM_NM,
                this.getDvPayeeDetail().getDisbVchrPaymentReasonCode());
        if (researchPaymentReasonCodeEvaluator.evaluationSucceeds()) {
            if (getParameterService().parameterExists(DisbursementVoucherDocument.class,
                    DisbursementVoucherConstants.RESEARCH_NON_VENDOR_PAY_LIMIT_AMOUNT_PARM_NM)) {
                String researchPayLimit = getParameterService().getParameterValueAsString(DisbursementVoucherDocument.class,
                        DisbursementVoucherConstants.RESEARCH_NON_VENDOR_PAY_LIMIT_AMOUNT_PARM_NM);
                if (StringUtils.isNotBlank(researchPayLimit)) {
                    KualiDecimal payLimit = new KualiDecimal(researchPayLimit);

                    if (getDisbVchrCheckTotalAmount().isLessThan(payLimit)) {
                        this.getDvPayeeDetail().setDvPayeeSubjectPaymentCode(true);
                    }
                }
            }
        }

        this.disbVchrPayeeTaxControlCode = "";
        this.disbVchrPayeeW9CompleteCode = true;
    }
    
    @Override
    public void prepareForSave() {
        if (this instanceof AmountTotaling) {
            if (getFinancialSystemDocumentHeader().getFinancialDocumentStatusCode().equals(KFSConstants.DocumentStatusCodes.ENROUTE) && !getFinancialSystemDocumentHeader().getWorkflowDocument().isCompletionRequested()) {
                if (getParameterService().parameterExists(KfsParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class, UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_PARAMETER_NAME)
                        && getParameterService().getParameterValueAsBoolean(KfsParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class, UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_PARAMETER_NAME)) {
                    getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(((AmountTotaling) this).getTotalDollarAmount());
                }
            } else {
                getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(((AmountTotaling) this).getTotalDollarAmount());
            }
        }

        if (dvWireTransfer != null) {
            dvWireTransfer.setDocumentNumber(this.documentNumber);
        }

        if (dvNonResidentAlienTax != null) {
            dvNonResidentAlienTax.setDocumentNumber(this.documentNumber);
        }

        dvPayeeDetail.setDocumentNumber(this.documentNumber);
        ((CuDisbursementVoucherPayeeDetailExtension)dvPayeeDetail.getExtension()).setDocumentNumber(this.documentNumber);
        
        if (dvNonEmployeeTravel != null) {
            dvNonEmployeeTravel.setDocumentNumber(this.documentNumber);
            dvNonEmployeeTravel.setTotalTravelAmount(dvNonEmployeeTravel.getTotalTravelAmount());
        }

        if (dvPreConferenceDetail != null) {
            dvPreConferenceDetail.setDocumentNumber(this.documentNumber);
            dvPreConferenceDetail.setDisbVchrConferenceTotalAmt(dvPreConferenceDetail.getDisbVchrConferenceTotalAmt());
        }

        if (shouldClearSpecialHandling()) {
            clearSpecialHandling();
        }
    }
    
    public CuDisbursementVoucherPayeeDetail getDvPayeeDetail() {
        return dvPayeeDetail;
    }

    @Override
    public void populateDocumentForRouting() {
        CuDisbursementVoucherPayeeDetail payeeDetail =   getDvPayeeDetail();

        if (payeeDetail.isVendor()) {
            payeeDetail.setDisbVchrPayeeEmployeeCode(getVendorService().isVendorInstitutionEmployee(payeeDetail.getDisbVchrVendorHeaderIdNumberAsInteger()));
            payeeDetail.setDvPayeeSubjectPaymentCode(getVendorService().isSubjectPaymentVendor(payeeDetail.getDisbVchrVendorHeaderIdNumberAsInteger()));
        }
        else if (payeeDetail.isEmployee()|| payeeDetail.isStudent() || payeeDetail.isAlumni()) {

            // Determine if employee student or alumni is a research subject
            ParameterEvaluator researchPaymentReasonCodeEvaluator = /*REFACTORME*/SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(DisbursementVoucherDocument.class, DisbursementVoucherConstants.RESEARCH_PAYMENT_REASONS_PARM_NM, payeeDetail.getDisbVchrPaymentReasonCode());
            if (researchPaymentReasonCodeEvaluator.evaluationSucceeds()) {
                if (getParameterService().parameterExists(DisbursementVoucherDocument.class, DisbursementVoucherConstants.RESEARCH_NON_VENDOR_PAY_LIMIT_AMOUNT_PARM_NM)) {
                    String researchPayLimit = getParameterService().getParameterValueAsString(DisbursementVoucherDocument.class, DisbursementVoucherConstants.RESEARCH_NON_VENDOR_PAY_LIMIT_AMOUNT_PARM_NM);
                    if (StringUtils.isNotBlank(researchPayLimit)) {
                        KualiDecimal payLimit = new KualiDecimal(researchPayLimit);

                        if (getDisbVchrCheckTotalAmount().isLessThan(payLimit)) {
                            payeeDetail.setDvPayeeSubjectPaymentCode(true);
                        }
                    }
                }
            }
        }

        super.populateDocumentForRouting(); // Call last, serializes to XML
    }
    
    @Override
    public void toCopy() throws WorkflowException {
        String payeeidNumber = getDvPayeeDetail().getDisbVchrPayeeIdNumber();
        
        super.toCopy();
        
        KNSGlobalVariables.getMessageList().clear();
        getDvPayeeDetail().setDisbVchrPayeeIdNumber(payeeidNumber);
        initiateDocument();

        // clear fields
        setDisbVchrContactPhoneNumber(StringUtils.EMPTY);
        setDisbVchrContactEmailId(StringUtils.EMPTY);
        setDisbVchrPayeeTaxControlCode(StringUtils.EMPTY);
        setTripAssociationStatusCode(null);
        setTripId(null);

        // clear nra
        SpringContext.getBean(DisbursementVoucherTaxService.class).clearNRATaxLines(this);
        setDvNonResidentAlienTax(new DisbursementVoucherNonResidentAlienTax());

        // clear waive wire
        getDvWireTransfer().setDisbursementVoucherWireTransferFeeWaiverIndicator(false);

        // check vendor id number to see if still valid, if not, clear dvPayeeDetail; otherwise, use the current dvPayeeDetail as is
        if (!StringUtils.isBlank(getDvPayeeDetail().getDisbVchrPayeeIdNumber())) {
            VendorDetail vendorDetail = getVendorService().getVendorDetail(dvPayeeDetail.getDisbVchrVendorHeaderIdNumberAsInteger(), dvPayeeDetail.getDisbVchrVendorDetailAssignedIdNumberAsInteger());
            if (vendorDetail == null) {
                dvPayeeDetail = new CuDisbursementVoucherPayeeDetail();;
                getDvPayeeDetail().setDisbVchrPayeeIdNumber(StringUtils.EMPTY);
                ((CuDisbursementVoucherPayeeDetailExtension)getDvPayeeDetail().getExtension()).setDisbVchrPayeeIdType(StringUtils.EMPTY);
                KNSGlobalVariables.getMessageList().add(KFSKeyConstants.WARNING_DV_PAYEE_NONEXISTANT_CLEARED);
            }
        }

        // this copied DV has not been extracted
        this.extractDate = null;
        this.paidDate = null;
        this.cancelDate = null;
        getFinancialSystemDocumentHeader().setFinancialDocumentStatusCode(KFSConstants.DocumentStatusCodes.INITIATED);
    }
    
    public void initiateDocument() {
        PhoneNumberService phoneNumberService = SpringContext.getBean(PhoneNumberService.class);
        Person currentUser = GlobalVariables.getUserSession().getPerson();
        setDisbVchrContactPersonName(currentUser.getName());
        setDisbVchrContactEmailId(currentUser.getEmailAddressUnmasked());
        String phoneNumber = currentUser.getPhoneNumber();
        
        if(StringUtils.isNotBlank(phoneNumber) && !StringUtils.equalsIgnoreCase("null", phoneNumber)) {
            if(!phoneNumberService.isDefaultFormatPhoneNumber(currentUser.getPhoneNumber())) {
                setDisbVchrContactPhoneNumber(phoneNumberService.formatNumberIfPossible(currentUser.getPhoneNumber()));
            } else if(StringUtils.equalsIgnoreCase(phoneNumber, "null")) {
                // do nothing... we don't want phone number set to invalid value
            } else {
                setDisbVchrContactPhoneNumber(phoneNumber);
            }
        }        

        if(!phoneNumberService.isDefaultFormatPhoneNumber(currentUser.getPhoneNumber())) {
            setDisbVchrContactPhoneNumber(phoneNumberService.formatNumberIfPossible(currentUser.getPhoneNumber()));
        }

        setDisbVchrContactEmailId(currentUser.getEmailAddress());
        ChartOrgHolder chartOrg = SpringContext.getBean(org.kuali.kfs.sys.service.FinancialSystemUserService.class).getPrimaryOrganization(currentUser, KFSConstants.ParameterNamespaces.FINANCIAL);

        // Does a valid campus code exist for this person?  If so, simply grab
        // the campus code via the business object service.
        if (chartOrg != null && chartOrg.getOrganization() != null) {
            setCampusCode(chartOrg.getOrganization().getOrganizationPhysicalCampusCode());
        }
        // A valid campus code was not found; therefore, use the default affiliated
        // campus code.
        else {
            String affiliatedCampusCode = currentUser.getCampusCode();
            setCampusCode(affiliatedCampusCode);
        }

        // due date
        Calendar calendar = getDateTimeService().getCurrentCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        setDisbursementVoucherDueDate(new Date(calendar.getTimeInMillis()));

        // default doc location
        if (StringUtils.isBlank(getDisbursementVoucherDocumentationLocationCode())) {
            setDisbursementVoucherDocumentationLocationCode(getParameterService().getParameterValueAsString(DisbursementVoucherDocument.class, DisbursementVoucherConstants.DEFAULT_DOC_LOCATION_PARM_NM));
        }

        // default bank code
        Bank defaultBank = SpringContext.getBean(BankService.class).getDefaultBankByDocType(this.getClass());
        if (defaultBank != null) {
            this.disbVchrBankCode = defaultBank.getBankCode();
            this.bank = defaultBank;
        }
    }
    
    @Override
    public boolean generateDocumentGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        if (getGeneralLedgerPendingEntries() == null || getGeneralLedgerPendingEntries().size() < 2) {
            LOG.warn("No gl entries for accounting lines.");
            return true;
        }

        /*
         * only generate additional charge entries for payment method wire charge, and if the fee has not been waived
         */
        if (DisbursementVoucherConstants.PAYMENT_METHOD_WIRE.equals(getDisbVchrPaymentMethodCode()) && !getDvWireTransfer().isDisbursementVoucherWireTransferFeeWaiverIndicator()) {
            LOG.debug("generating wire charge gl pending entries.");

            // retrieve wire charge
            WireCharge wireCharge = retrieveWireCharge();
            //KFSPTS-764: Added if check to eliminate zero dollar wire charge generating zero dollar accounting entries
            if (!isZeroDollarWireCharge(wireCharge)) {
                
                //KFSPTS-764: only generate GLPE entries when wire charges are NOT zero dollars.
            // generate debits
            GeneralLedgerPendingEntry chargeEntry = processWireChargeDebitEntries(sequenceHelper, wireCharge);

            // generate credits
            processWireChargeCreditEntries(sequenceHelper, wireCharge, chargeEntry);
        }
        }

        // for wire or drafts generate bank offset entry (if enabled), for ACH and checks offset will be generated by PDP
        if (DisbursementVoucherConstants.PAYMENT_METHOD_WIRE.equals(getDisbVchrPaymentMethodCode()) || DisbursementVoucherConstants.PAYMENT_METHOD_DRAFT.equals(getDisbVchrPaymentMethodCode())) {
            generateDocumentBankOffsetEntries(sequenceHelper);
        }

        return true;
    }
        
        /**
         * KFSPTS-764
         * Returns true when the wire charge amount is found to be zero dollars based on the bank country code
         * otherwise, returns false.
         * 
         * @param wireCharge
         * @return true when wire charge for DV bank is zero dollars.
         */
       private boolean isZeroDollarWireCharge(WireCharge wireCharge) {
            
            if ( (KFSConstants.COUNTRY_CODE_UNITED_STATES.equals(getDvWireTransfer().getDisbVchrBankCountryCode()) && wireCharge.getDomesticChargeAmt().isZero()) ||
                 (!KFSConstants.COUNTRY_CODE_UNITED_STATES.equals(getDvWireTransfer().getDisbVchrBankCountryCode()) && wireCharge.getForeignChargeAmt().isZero()) ){
                //DV is for a US bank and wire charge value is zero dollars OR DV is for a foreign bank and wire charge is zero dollars.
                return true;
            }
            return false;
        }
        
        @Override
        public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
            if (nodeName.equals(CuDisbursementVoucherDocument.DOCUMENT_REQUIRES_AWARD_REVIEW_SPLIT))
                return isCAndGReviewRequired();
            if (nodeName.equals(CuDisbursementVoucherDocument.DOCUMENT_REQUIRES_CAMPUS_REVIEW_SPLIT))
                return isCampusReviewRequired();
            if (nodeName.equals(DisbursementVoucherDocument.DOCUMENT_REQUIRES_TAX_REVIEW_SPLIT)) {
                return isTaxReviewRequired();
            }
            if (nodeName.equals(DisbursementVoucherDocument.DOCUMENT_REQUIRES_TRAVEL_REVIEW_SPLIT)) {
                return isTravelReviewRequired();
            }
            if (nodeName.equals(DOCUMENT_REQUIRES_SEPARATION_OF_DUTIES)) {
                return isSeparationOfDutiesReviewRequired();
            }
            throw new UnsupportedOperationException("Cannot answer split question for this node you call \""+nodeName+"\"");
        }
        
        public boolean isTravelReviewRequired() {
            List<AccountingLine> theList = (List<AccountingLine>) this.sourceAccountingLines;
            
            for (AccountingLine alb : theList )
            {
                ParameterEvaluator objectCodes = SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator("KFS-FP", "DisbursementVoucher", OBJECT_CODES_REQUIRING_TRAVEL_REVIEW, alb.getFinancialObjectCode());
                if (objectCodes.evaluationSucceeds())
                {
                    LOG.info("Object Code " + alb.getFinancialObjectCode() + " requires this document to undergo Travel review.");              
                    return true;
                }
            }
            
            
            boolean overDollarThreshold = false;
            String dollarThreshold = getParameterService().getParameterValueAsString("KFS-FP", "DisbursementVoucher", DOLLAR_THRESHOLD_REQUIRING_TRAVEL_REVIEW);
            KualiDecimal dollarThresholdDecimal = new KualiDecimal(dollarThreshold);
            if ( this.disbVchrCheckTotalAmount.isGreaterEqual(dollarThresholdDecimal)) {
                overDollarThreshold = true;
            }

            
            boolean paymentReasonCodeIsNorP = false;
            String paymentReasonCode = this.getDvPayeeDetail().getDisbVchrPaymentReasonCode();
            paymentReasonCodeIsNorP = this.getDvPymentReasonService().isPrepaidTravelPaymentReason(paymentReasonCode) || this.getDvPymentReasonService().isNonEmployeeTravelPaymentReason(paymentReasonCode);

            
            return (this.getDvPymentReasonService().isPrepaidTravelPaymentReason(paymentReasonCode) || this.getDvPymentReasonService().isNonEmployeeTravelPaymentReason(paymentReasonCode) && overDollarThreshold);
            }
        
        protected boolean isCAndGReviewRequired() {
            
            String awardThreshold = getParameterService().getParameterValueAsString("KFS-FP", "DisbursementVoucher", DOLLAR_THRESHOLD_REQUIRING_AWARD_REVIEW);
            KualiDecimal dollarThresholdDecimal = new KualiDecimal(awardThreshold);
            if ( this.disbVchrCheckTotalAmount.isGreaterEqual(dollarThresholdDecimal)) {
                return true;
            }
            
            List<AccountingLine> theList = (List<AccountingLine>) this.sourceAccountingLines;       
            for (AccountingLine alb : theList )
            {
                ParameterEvaluator objectCodes = SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator("KFS-FP", "DisbursementVoucher", OBJECT_CODES_REQUIRING_AWARD_REVIEW, alb.getFinancialObjectCode());
                if (objectCodes.evaluationSucceeds()) {
                    LOG.info("Object Code " + alb.getFinancialObjectCode() + " requires this document to undergo Award review.");
                    return true;
                }
            }
            
            return false;
        }
        
        protected boolean isCampusReviewRequired() {

            List<ActionTakenValue> actions = RouteContext.getCurrentRouteContext().getDocument().getActionsTaken();
            List<String> people = new ArrayList<String>();
            for(ActionTakenValue atv: actions) {
                if( !people.contains(atv.getPrincipalId())) {
                    people.add(atv.getPrincipalId());
                }
            }
            if (people.size()<2)
            {
                return true;
            }               
                        
            List<AccountingLine> theList = (List<AccountingLine>) this.sourceAccountingLines;
            
            for (AccountingLine alb : theList )
            {
                ParameterEvaluator objectCodes = SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator("KFS-FP", "DisbursementVoucher", OBJECT_CODES_REQUIRING_CAMPUS_REVIEW, alb.getFinancialObjectCode());
                if (objectCodes.evaluationSucceeds())
                {
                    LOG.info("Object Code " + alb.getFinancialObjectCode() + " requires this document to undergo Campus review.");
                    return true;
                }
            }
            
            ParameterEvaluator paymentReasons = SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator("KFS-FP", "DisbursementVoucher", PAYMENT_REASONS_REQUIRING_CAMPUS_REVIEW, this.dvPayeeDetail.getDisbVchrPaymentReasonCode());
            if (paymentReasons.evaluationSucceeds()) {
                return true;
            }

            String dollarThreshold = getParameterService().getParameterValueAsString("KFS-FP", "DisbursementVoucher", DOLLAR_THRESHOLD_REQUIRING_CAMPUS_REVIEW);
            KualiDecimal dollarThresholdDecimal = new KualiDecimal(dollarThreshold);
            if ( this.disbVchrCheckTotalAmount.isGreaterEqual(dollarThresholdDecimal)) {
                return true;
            }               
            
            return false;
        }
        
        
        public void setDvPayeeDetail(CuDisbursementVoucherPayeeDetail dvPayeeDetail) {
            this.dvPayeeDetail = dvPayeeDetail;
        }
        
        public String getTripAssociationStatusCode() {
            return tripAssociationStatusCode;
        }


        public void setTripAssociationStatusCode(String tripAssociationStatusCode) {
            this.tripAssociationStatusCode = tripAssociationStatusCode;
        }


        public String getTripId() {
            return tripId;
        }


        public void setTripId(String tripId) {
            this.tripId = tripId;
        }
        
        
}

