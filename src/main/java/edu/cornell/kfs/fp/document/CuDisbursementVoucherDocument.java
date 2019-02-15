package edu.cornell.kfs.fp.document;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService;
import org.kuali.kfs.fp.document.service.DisbursementVoucherTaxService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KfsAuthorizationConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.WireCharge;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AmountTotaling;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorType;
import org.kuali.kfs.vnd.service.PhoneNumberService;
import org.kuali.rice.core.api.parameter.ParameterEvaluator;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterConstants.COMPONENT;
import org.kuali.kfs.coreservice.framework.parameter.ParameterConstants.NAMESPACE;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.actiontaken.ActionTakenValue;
import org.kuali.rice.kew.api.action.ActionTaken;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kew.engine.RouteContext;
import org.kuali.rice.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.address.EntityAddress;
import org.kuali.kfs.kns.document.authorization.DocumentAuthorizer;
import org.kuali.kfs.kns.document.authorization.TransactionalDocumentAuthorizer;
import org.kuali.kfs.kns.document.authorization.TransactionalDocumentPresentationController;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetailExtension;
import edu.cornell.kfs.fp.businessobject.DisbursementVoucherWireTransferExtendedAttribute;
import edu.cornell.kfs.fp.document.interfaces.CULegacyTravelIntegrationInterface;
import edu.cornell.kfs.fp.document.service.CULegacyTravelService;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherTaxService;
import edu.cornell.kfs.fp.document.service.impl.CULegacyTravelServiceImpl;
import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;


@NAMESPACE(namespace = KFSConstants.CoreModuleNamespaces.FINANCIAL)
@COMPONENT(component = "DisbursementVoucher")
public class CuDisbursementVoucherDocument extends DisbursementVoucherDocument implements CULegacyTravelIntegrationInterface {

    private static final long serialVersionUID = 1L;
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
    
    protected static final String DISAPPROVE_ANNOTATION_REASON_STARTER = "Disapproval reason - ";
    
    protected CuDisbursementVoucherPayeeDetail dvPayeeDetail;

    // TRIP INFORMATION FIELDS
    protected String tripAssociationStatusCode;
    protected String tripId;

    private static CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService;
    private static DisbursementVoucherPayeeService disbursementVoucherPayeeService;
    private static DisbursementVoucherTaxService disbursementVoucherTaxService;
    private static DocumentHelperService documentHelperService;

    public CuDisbursementVoucherDocument() {
        super();
        dvPayeeDetail = new CuDisbursementVoucherPayeeDetail();
        tripAssociationStatusCode = CULegacyTravelServiceImpl.TRIP_ASSOCIATIONS.IS_NOT_TRIP_DOC;
    }

    /**
     * Overridden to interact with the Legacy Travel service
     * @see <a href="https://jira.cornell.edu/browse/KFSPTS-2715">https://jira.cornell.edu/browse/KFSPTS-2715</a>
     */
    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
      // If the DV is Canceled or Disapproved, we need to reopen the trip in the Legacy Travel service.
      if (getDocumentHeader().getWorkflowDocument().isCanceled() ||
          getDocumentHeader().getWorkflowDocument().isDisapproved()) {        
        boolean tripReOpened = false;
        boolean isTravelDoc = false;
        List<ActionTaken> actionsTaken = this.getDocumentHeader().getWorkflowDocument().getActionsTaken();
        String disapprovalReason = findDissapprovalReason(actionsTaken);

        try {
          CULegacyTravelService cuLegacyTravelService = SpringContext.getBean(CULegacyTravelService.class);
          isTravelDoc = cuLegacyTravelService.isCULegacyTravelIntegrationInterfaceAssociatedWithTrip(this);
          if(isTravelDoc) {
            // This means the DV is a Travel DV
            tripReOpened = cuLegacyTravelService.reopenLegacyTrip(this.getDocumentNumber(), disapprovalReason);
            LOG.info("Trip successfully reopened : "+ tripReOpened);
          } else {
            LOG.info("DV is not a travel DV");
          }
        } catch (Exception ex) {
          LOG.error("Exception occurred while trying to cancel a trip.", ex);
        }
        
      }

      super.doRouteStatusChange(statusChangeEvent);
    }

    private String findDissapprovalReason(List<ActionTaken> actionsTaken) {
        String disapprovalReason = "";
        if(actionsTaken.size() > 0) {
          String annotation = actionsTaken.get(actionsTaken.size() - 1).getAnnotation();
          if(StringUtils.isNotEmpty(annotation)) {
              if(StringUtils.contains(annotation, DISAPPROVE_ANNOTATION_REASON_STARTER)) {
                  disapprovalReason = annotation.substring(DISAPPROVE_ANNOTATION_REASON_STARTER.length());  
              } else {
                  disapprovalReason = annotation;
              }
          }
        }
        return disapprovalReason;
    }

    public void templateVendor(VendorDetail vendor, VendorAddress vendorAddress) {
        if (vendor == null) {
            return;
        }

        this.getDvPayeeDetail().setDisbursementVoucherPayeeTypeCode(KFSConstants.PaymentPayeeTypes.VENDOR);
        this.getDvPayeeDetail().setDisbVchrPayeeIdNumber(vendor.getVendorNumber());
        ((CuDisbursementVoucherPayeeDetailExtension) this.getDvPayeeDetail().getExtension()).setDisbVchrPayeeIdType(
                CuDisbursementVoucherConstants.DV_PAYEE_ID_TYP_VENDOR);
        ((CuDisbursementVoucherPayeeDetailExtension) this.getDvPayeeDetail().getExtension()).setPayeeTypeSuffix(
                createVendorPayeeTypeSuffix(vendor.getVendorHeader().getVendorType()));
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
        this.getDvPayeeDetail().setDvPayeeSubjectPaymentCode(
                VendorConstants.VendorTypes.SUBJECT_PAYMENT.equals(vendor.getVendorHeader().getVendorTypeCode()));
        this.getDvPayeeDetail().setDisbVchrEmployeePaidOutsidePayrollCode(getVendorService()
                .isVendorInstitutionEmployee(vendor.getVendorHeaderGeneratedIdentifier()));

        this.getDvPayeeDetail().setHasMultipleVendorAddresses(1 < vendor.getVendorAddresses().size());


        boolean w9AndW8Checked = false;
        if ((ObjectUtils.isNotNull(vendor.getVendorHeader().getVendorW9ReceivedIndicator())
                && vendor.getVendorHeader().getVendorW9ReceivedIndicator())
                || (ObjectUtils.isNotNull(vendor.getVendorHeader().getVendorW8BenReceivedIndicator())
                && vendor.getVendorHeader().getVendorW8BenReceivedIndicator())) {
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

        // KFSPTS-1891
        if ( vendor != null ) {
                      if ( ObjectUtils.isNotNull( vendor.getExtension() )
                              && vendor.getExtension() instanceof VendorDetailExtension ) {
                          if ( StringUtils.isNotBlank(((VendorDetailExtension)vendor.getExtension()).getDefaultB2BPaymentMethodCode())) {
                              disbVchrPaymentMethodCode = ((VendorDetailExtension)vendor.getExtension()).getDefaultB2BPaymentMethodCode();
                          }
                      }
                  }
    }

    public void templateEmployee(Person employee) {
        if (employee == null) {
            return;
        }

        this.getDvPayeeDetail().setDisbursementVoucherPayeeTypeCode(KFSConstants.PaymentPayeeTypes.EMPLOYEE);
        if (StringUtils.isNotBlank(employee.getEmployeeId())) {
            this.getDvPayeeDetail().setDisbVchrPayeeIdNumber(employee.getEmployeeId());
            ((CuDisbursementVoucherPayeeDetailExtension) this.getDvPayeeDetail().getExtension()).setDisbVchrPayeeIdType(
                    CuDisbursementVoucherConstants.DV_PAYEE_ID_TYP_EMPL);
        } else {
            this.getDvPayeeDetail().setDisbVchrPayeeIdNumber(employee.getPrincipalId());
            ((CuDisbursementVoucherPayeeDetailExtension) this.getDvPayeeDetail().getExtension()).setDisbVchrPayeeIdType(
                    CuDisbursementVoucherConstants.DV_PAYEE_ID_TYP_ENTITY);
        }
        ((CuDisbursementVoucherPayeeDetailExtension) this.getDvPayeeDetail().getExtension()).setPayeeTypeSuffix(StringUtils.EMPTY);
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

        //KFSMI-8935: When an employee is inactive, the Payment Type field on DV documents should display the message
        // "Is this payee an employee" = No
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
        ((CuDisbursementVoucherPayeeDetailExtension) this.getDvPayeeDetail().getExtension()).setPayeeTypeSuffix(StringUtils.EMPTY);

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
        ((CuDisbursementVoucherPayeeDetailExtension) this.getDvPayeeDetail().getExtension()).setPayeeTypeSuffix(StringUtils.EMPTY);

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

		if (getFinancialSystemDocumentHeader().getFinancialDocumentStatusCode()
				.equals(KFSConstants.DocumentStatusCodes.ENROUTE)
				&& !getFinancialSystemDocumentHeader().getWorkflowDocument().isCompletionRequested()) {
			if (getParameterService().parameterExists(KfsParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class,
					UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_PARAMETER_NAME)
					&& getParameterService().getParameterValueAsBoolean(
							KfsParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class,
							UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_PARAMETER_NAME)) {
				getFinancialSystemDocumentHeader()
						.setFinancialDocumentTotalAmount(((AmountTotaling) this).getTotalDollarAmount());
			}
		} else {
			getFinancialSystemDocumentHeader()
					.setFinancialDocumentTotalAmount(((AmountTotaling) this).getTotalDollarAmount());
		}

        captureWorkflowHeaderInformation();

        if (wireTransfer != null) {
            wireTransfer.setDocumentNumber(this.documentNumber);
            ((DisbursementVoucherWireTransferExtendedAttribute) wireTransfer.getExtension()).setDocumentNumber(this.documentNumber);
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
        // KFSPTS-1891.  This is from uadisbvdocument.
        // TODO : need to check again to see if cornell need this
        // CU Customization: Also perform check below when an ENROUTE doc is at the initial "AdHoc" node.
        if ( getDocumentHeader().getWorkflowDocument().isInitiated() ||  getDocumentHeader().getWorkflowDocument().isSaved()
                || (getDocumentHeader().getWorkflowDocument().isEnroute()
                        && getDocumentHeader().getWorkflowDocument().getCurrentNodeNames().contains("AdHoc")) ) {
            // need to check whether the user has the permission to edit the bank code
            // if so, don't synchronize since we can't tell whether the value coming in
            // was entered by the user or not.
            DocumentAuthorizer docAuth = getDocumentHelperService().getDocumentAuthorizer(this);
            if ( !docAuth.isAuthorizedByTemplate(this,
                    KFSConstants.ParameterNamespaces.KFS,
                    KFSConstants.PermissionTemplate.EDIT_BANK_CODE.name,
                    GlobalVariables.getUserSession().getPrincipalId()  ) ) {
                synchronizeBankCodeWithPaymentMethod();
            } else {
                refreshReferenceObject( "bank" );
            }
            refreshPayeeTypeSuffixIfPaymentIsEditable();
        }
    }

    // KFSPTS-1891
    protected void synchronizeBankCodeWithPaymentMethod() {
        Bank bank = getPaymentMethodGeneralLedgerPendingEntryService().getBankForPaymentMethod( getDisbVchrPaymentMethodCode() );
        if ( bank != null ) {
            if ( !StringUtils.equals(bank.getBankCode(), getDisbVchrBankCode()) ) {
                setDisbVchrBankCode(bank.getBankCode());
                refreshReferenceObject( "bank" );
            }
        } else {
            // CU Customization: Load default bank if no payment method is given, or set to null if no default bank could be found.
            Bank defaultBank = SpringContext.getBean(BankService.class).getDefaultBankByDocType(this.getClass());
            if (defaultBank != null) {
                setDisbVchrBankCode(defaultBank.getBankCode());
                setBank(defaultBank);
            } else {
                setDisbVchrBankCode(null);
                setBank(null);
            }
        }
    }

    public void refreshPayeeTypeSuffixIfPaymentIsEditable() {
        TransactionalDocumentAuthorizer docAuthorizer = getDvDocumentAuthorizer();
        if (docAuthorizer.canEdit(this, GlobalVariables.getUserSession().getPerson())) {
            Set<String> editModes = getDvDocumentPresentationController().getEditModes(this);
            editModes = docAuthorizer.getEditModes(this, GlobalVariables.getUserSession().getPerson(), editModes);
            if (editModes.contains(KfsAuthorizationConstants.DisbursementVoucherEditMode.PAYEE_ENTRY)) {
                String newSuffix;
                if (getDvPayeeDetail().isVendor()) {
                    VendorDetail vendor = getVendorService().getByVendorNumber(getDvPayeeDetail().getDisbVchrPayeeIdNumber());
                    if (ObjectUtils.isNotNull(vendor)) {
                        newSuffix = createVendorPayeeTypeSuffix(vendor.getVendorHeader().getVendorType());
                    } else {
                        newSuffix = StringUtils.EMPTY;
                    }
                } else {
                    newSuffix = StringUtils.EMPTY;
                }
                ((CuDisbursementVoucherPayeeDetailExtension) getDvPayeeDetail().getExtension()).setPayeeTypeSuffix(newSuffix);
            }
        }
    }

    public String createVendorPayeeTypeSuffix(VendorType vendorType) {
        return vendorType.getVendorTypeCode() + " - " + vendorType.getVendorTypeDescription();
    }

    protected TransactionalDocumentPresentationController getDvDocumentPresentationController() {
        return (TransactionalDocumentPresentationController) getDocumentHelperService().getDocumentPresentationController(this);
    }

    protected TransactionalDocumentAuthorizer getDvDocumentAuthorizer() {
        return (TransactionalDocumentAuthorizer) getDocumentHelperService().getDocumentAuthorizer(this);
    }

    protected CUPaymentMethodGeneralLedgerPendingEntryService getPaymentMethodGeneralLedgerPendingEntryService() {
        if ( paymentMethodGeneralLedgerPendingEntryService == null ) {
            paymentMethodGeneralLedgerPendingEntryService = SpringContext.getBean(CUPaymentMethodGeneralLedgerPendingEntryService.class);
        }
        return paymentMethodGeneralLedgerPendingEntryService;
    }

    protected DocumentHelperService getDocumentHelperService() {
        if (documentHelperService == null) {
            documentHelperService = SpringContext.getBean(DocumentHelperService.class);
        }
        return documentHelperService;
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

        getDvPayeeDetail().setDisbVchrPayeeIdNumber(payeeidNumber);
    }

    /**
     * Clear fields that shouldn't be copied to to the new DV.
     */
    protected void clearFieldsThatShouldNotBeCopied() {
        super.clearFieldsThatShouldNotBeCopied();
        setTripAssociationStatusCode(CULegacyTravelServiceImpl.TRIP_ASSOCIATIONS.IS_NOT_TRIP_DOC);
        setTripId(null);
    }

    /**
     * This overrides the code in the parent base class because of an issue where vendorDetail is null when it shouldn't be.
     * Might be related to OJB and proxy objects or something like that.
     * Hopefully we can contribute a fix to base code and eliminate this duplicate method in the future.
     */
    @Override
    protected void clearInvalidPayee() {
        // check vendor id number to see if still valid, if not, clear dvPayeeDetail; otherwise, use the current
        // dvPayeeDetail as is
        if (!StringUtils.isBlank(getDvPayeeDetail().getDisbVchrPayeeIdNumber())) {
            VendorDetail vendorDetail = getVendorService().getVendorDetail(getDvPayeeDetail().getDisbVchrVendorHeaderIdNumberAsInteger(), getDvPayeeDetail().getDisbVchrVendorDetailAssignedIdNumberAsInteger());
            if (vendorDetail == null) {
                clearPayee(KFSKeyConstants.WARNING_DV_PAYEE_NONEXISTANT_CLEARED);
            } else {
                DisbursementPayee payee = getDisbursementVoucherPayeeService().getPayeeFromVendor(vendorDetail);
                if (!getDisbursementVoucherPaymentReasonService().isPayeeQualifiedForPayment(payee, getDvPayeeDetail().getDisbVchrPaymentReasonCode())) {
                    clearPayee(KFSKeyConstants.MESSAGE_DV_PAYEE_INVALID_PAYMENT_TYPE_CLEARED);
                }
            }
        }
    }

    protected void clearPayee(String messageKey) {
        dvPayeeDetail = new CuDisbursementVoucherPayeeDetail();
        getDvPayeeDetail().setDisbVchrPayeeIdNumber(StringUtils.EMPTY);
        clearDvPayeeIdType();
        KNSGlobalVariables.getMessageList().add(messageKey);
    }

    protected void clearDvPayeeIdType() {
        ((CuDisbursementVoucherPayeeDetailExtension)getDvPayeeDetail().getExtension()).setDisbVchrPayeeIdType(StringUtils.EMPTY);
        ((CuDisbursementVoucherPayeeDetailExtension)getDvPayeeDetail().getExtension()).setPayeeTypeSuffix(StringUtils.EMPTY);
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
        } else {
            // A valid campus code was not found; therefore, use the default affiliated  campus code.
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
            if (KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE.equals(getDisbVchrPaymentMethodCode()) && !getWireTransfer().isWireTransferFeeWaiverIndicator()) {
                LOG.debug("generating wire charge gl pending entries.");

                // retrieve wire charge
                WireCharge wireCharge = getPaymentSourceHelperService().retrieveCurrentYearWireCharge();
                //KFSPTS-764: Added if check to eliminate zero dollar wire charge generating zero dollar accounting entries
                if (!isZeroDollarWireCharge(wireCharge)) {

                //KFSPTS-764: only generate GLPE entries when wire charges are NOT zero dollars.
                // generate debits
                GeneralLedgerPendingEntry chargeEntry = getPaymentSourceHelperService().processWireChargeDebitEntries(this, sequenceHelper, wireCharge);

                // generate credits
                getPaymentSourceHelperService().processWireChargeCreditEntries(this, sequenceHelper, wireCharge, chargeEntry);
            }
        }

        // for wire or drafts generate bank offset entry (if enabled), for ACH and checks offset will be generated by PDP
        if (KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE.equals(getDisbVchrPaymentMethodCode()) || KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_DRAFT.equals(getDisbVchrPaymentMethodCode())) {
            getPaymentSourceHelperService().generateDocumentBankOffsetEntries(this, sequenceHelper, DisbursementVoucherConstants.DOCUMENT_TYPE_WTFD);
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

        if ( (KFSConstants.COUNTRY_CODE_UNITED_STATES.equals(getWireTransfer().getBankCountryCode()) && wireCharge.getDomesticChargeAmt().isZero()) ||
             (!KFSConstants.COUNTRY_CODE_UNITED_STATES.equals(getWireTransfer().getBankCountryCode()) && wireCharge.getForeignChargeAmt().isZero()) ){
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

        String paymentReasonCode = this.getDvPayeeDetail().getDisbVchrPaymentReasonCode();

        return (this.getDisbursementVoucherPaymentReasonService().isPrepaidTravelPaymentReason(paymentReasonCode) || this.getDisbursementVoucherPaymentReasonService().isNonEmployeeTravelPaymentReason(paymentReasonCode) && overDollarThreshold);
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

    @Override
    protected boolean isTaxReviewRequired() {
        if (isPayeePurchaseOrderVendorHasWithholding()) {
            return true;
        }

        String payeeTypeCode = this.getDvPayeeDetail().getDisbursementVoucherPayeeTypeCode();
        if (payeeTypeCode.equals(KFSConstants.PaymentPayeeTypes.EMPLOYEE)) {
            return false;
        } else if (payeeTypeCode.equals(KFSConstants.PaymentPayeeTypes.VENDOR)) {
            if(getVendorService().isVendorInstitutionEmployee(this.getDvPayeeDetail().getDisbVchrVendorHeaderIdNumberAsInteger())){
                return true;
            }
        }

        String paymentReasonCode = this.getDvPayeeDetail().getDisbVchrPaymentReasonCode();
        Integer vendorHeaderId = getDvPayeeDetail().getDisbVchrVendorHeaderIdNumberAsInteger();
        if (getCuDisbursementVoucherTaxService().isForeignVendorAndTaxReviewRequired(payeeTypeCode, paymentReasonCode, vendorHeaderId)) {
        	return true;
        }

        String taxControlCode = this.getDisbVchrPayeeTaxControlCode();
        if (StringUtils.equals(taxControlCode, DisbursementVoucherDocument.TAX_CONTROL_BACKUP_HOLDING) || StringUtils.equals(taxControlCode,DisbursementVoucherDocument.TAX_CONTROL_HOLD_PAYMENTS)) {
            return true;
        }


        if (this.getDisbursementVoucherPaymentReasonService().isDecedentCompensationPaymentReason(paymentReasonCode)) {
            return true;
        }

        if (this.getDisbursementVoucherPaymentReasonService().isMovingPaymentReason(paymentReasonCode) && taxedCampusForMovingReimbursements()) {
            return true;
        }

        if (getParameterEvaluatorService().getParameterEvaluator(DisbursementVoucherDocument.class, DisbursementVoucherDocument.PAYMENT_REASONS_REQUIRING_TAX_REVIEW_PARAMETER_NAME, paymentReasonCode).evaluationSucceeds()) {
            return true;
        }

        return false;
    }

    protected CuDisbursementVoucherTaxService getCuDisbursementVoucherTaxService() {
        return SpringContext.getBean(CuDisbursementVoucherTaxService.class);
    }

    protected ParameterEvaluatorService getParameterEvaluatorService(){
    	return SpringContext.getBean(ParameterEvaluatorService.class);
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

    public static void setPaymentMethodGeneralLedgerPendingEntryService(CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService) {
        CuDisbursementVoucherDocument.paymentMethodGeneralLedgerPendingEntryService = paymentMethodGeneralLedgerPendingEntryService;
    }

    public static void setDocumentHelperService(DocumentHelperService documentHelperService) {
        CuDisbursementVoucherDocument.documentHelperService = documentHelperService;
    }

}

