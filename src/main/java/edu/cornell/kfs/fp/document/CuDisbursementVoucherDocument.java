package edu.cornell.kfs.fp.document;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.parameter.ParameterEvaluator;
import org.kuali.kfs.core.api.parameter.ParameterEvaluatorService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.fp.FPKeyConstants;
import org.kuali.kfs.fp.FPParameterConstants;
import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService;
import org.kuali.kfs.fp.document.service.DisbursementVoucherTaxService;
import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.engine.RouteContext;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.document.authorization.DocumentAuthorizer;
import org.kuali.kfs.kns.document.authorization.TransactionalDocumentAuthorizer;
import org.kuali.kfs.kns.document.authorization.TransactionalDocumentPresentationController;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KfsAuthorizationConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.WireCharge;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants.COMPONENT;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants.NAMESPACE;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorType;
import org.kuali.kfs.vnd.service.PhoneNumberService;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetailExtension;
import edu.cornell.kfs.fp.businessobject.DisbursementVoucherWireTransferExtendedAttribute;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherDefaultDueDateService;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherTaxService;
import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;
import edu.cornell.kfs.pdp.service.CuCheckStubService;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;


@NAMESPACE(namespace = KFSConstants.CoreModuleNamespaces.FINANCIAL)
@COMPONENT(component = "DisbursementVoucher")
public class CuDisbursementVoucherDocument extends DisbursementVoucherDocument {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LogManager.getLogger();
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

    // TRIP INFORMATION FIELDS
    protected String tripAssociationStatusCode;
    protected String tripId;

    private static CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService;
    private static DisbursementVoucherPayeeService disbursementVoucherPayeeService;
    private static DisbursementVoucherTaxService disbursementVoucherTaxService;
    private static DocumentHelperService documentHelperService;
    private static CuDisbursementVoucherDefaultDueDateService cuDisbursementVoucherDefaultDueDateService;
    private static CuCheckStubService cuCheckStubService;

    public CuDisbursementVoucherDocument() {
        super();
        dvPayeeDetail = new CuDisbursementVoucherPayeeDetail();
        tripAssociationStatusCode = CuFPConstants.IS_NOT_TRIP_DOC;
    }

    @Override
    public void templateVendor(final VendorDetail vendor, final VendorAddress vendorAddress) {
        if (vendor == null) {
            return;
        }

        getDvPayeeDetail().setDisbursementVoucherPayeeTypeCode(KFSConstants.PaymentPayeeTypes.VENDOR);
        getDvPayeeDetail().setDisbVchrPayeeIdNumber(vendor.getVendorNumber());
        ((CuDisbursementVoucherPayeeDetailExtension) getDvPayeeDetail().getExtension()).setDisbVchrPayeeIdType(
                CuDisbursementVoucherConstants.DV_PAYEE_ID_TYP_VENDOR);
        ((CuDisbursementVoucherPayeeDetailExtension) getDvPayeeDetail().getExtension()).setPayeeTypeSuffix(
                createVendorPayeeTypeSuffix(vendor.getVendorHeader().getVendorType()));
        getDvPayeeDetail().setDisbVchrPayeePersonName(vendor.getVendorName());

        getDvPayeeDetail().setDisbVchrNonresidentPaymentCode(vendor.getVendorHeader().getVendorForeignIndicator());

        if (ObjectUtils.isNotNull(vendorAddress) && ObjectUtils.isNotNull(vendorAddress.getVendorAddressGeneratedIdentifier())) {
            getDvPayeeDetail().setDisbVchrVendorAddressIdNumber(vendorAddress.getVendorAddressGeneratedIdentifier().toString());
            getDvPayeeDetail().setDisbVchrPayeeLine1Addr(vendorAddress.getVendorLine1Address());
            getDvPayeeDetail().setDisbVchrPayeeLine2Addr(vendorAddress.getVendorLine2Address());
            getDvPayeeDetail().setDisbVchrPayeeCityName(vendorAddress.getVendorCityName());
            getDvPayeeDetail().setDisbVchrPayeeStateCode(vendorAddress.getVendorStateCode());
            getDvPayeeDetail().setDisbVchrPayeeZipCode(vendorAddress.getVendorZipCode());
            getDvPayeeDetail().setDisbVchrPayeeCountryCode(vendorAddress.getVendorCountryCode());
        } else {
            getDvPayeeDetail().setDisbVchrVendorAddressIdNumber(StringUtils.EMPTY);
            getDvPayeeDetail().setDisbVchrPayeeLine1Addr(StringUtils.EMPTY);
            getDvPayeeDetail().setDisbVchrPayeeLine2Addr(StringUtils.EMPTY);
            getDvPayeeDetail().setDisbVchrPayeeCityName(StringUtils.EMPTY);
            getDvPayeeDetail().setDisbVchrPayeeStateCode(StringUtils.EMPTY);
            getDvPayeeDetail().setDisbVchrPayeeZipCode(StringUtils.EMPTY);
            getDvPayeeDetail().setDisbVchrPayeeCountryCode(StringUtils.EMPTY);
        }

        getDvPayeeDetail().setDisbVchrNonresidentPaymentCode(vendor.getVendorHeader().getVendorForeignIndicator());
        getDvPayeeDetail().setDvPayeeSubjectPaymentCode(
                VendorConstants.VendorTypes.SUBJECT_PAYMENT.equals(vendor.getVendorHeader().getVendorTypeCode()));
        getDvPayeeDetail().setDisbVchrEmployeePaidOutsidePayrollCode(getVendorService()
                .isVendorInstitutionEmployee(vendor.getVendorHeaderGeneratedIdentifier()));

        getDvPayeeDetail().setHasMultipleVendorAddresses(1 < vendor.getVendorAddresses().size());


        boolean w9AndW8Checked = false;
        if (ObjectUtils.isNotNull(vendor.getVendorHeader().getVendorW9ReceivedIndicator())
            && vendor.getVendorHeader().getVendorW9ReceivedIndicator()
            || ObjectUtils.isNotNull(vendor.getVendorHeader().getVendorW8BenReceivedIndicator())
            && vendor.getVendorHeader().getVendorW8BenReceivedIndicator()) {
            w9AndW8Checked = true;
        }

        disbVchrPayeeW9CompleteCode = w9AndW8Checked;

        final Date vendorFederalWithholdingTaxBeginDate = vendor.getVendorHeader().getVendorFederalWithholdingTaxBeginningDate();
        final Date vendorFederalWithholdingTaxEndDate = vendor.getVendorHeader().getVendorFederalWithholdingTaxEndDate();
        final java.util.Date today = getDateTimeService().getCurrentDate();
        if (vendorFederalWithholdingTaxBeginDate != null && vendorFederalWithholdingTaxBeginDate
                .before(today) && (vendorFederalWithholdingTaxEndDate == null || vendorFederalWithholdingTaxEndDate
                .after(today))) {
            disbVchrPayeeTaxControlCode = DisbursementVoucherConstants.TAX_CONTROL_CODE_BEGIN_WITHHOLDING;
        }

        // if vendor is foreign, default nonresident payment code to true
        if (getVendorService().isVendorForeign(vendor.getVendorHeaderGeneratedIdentifier())) {
            getDvPayeeDetail().setDisbVchrNonresidentPaymentCode(true);
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

    @Override
    public void templateEmployee(final Person employee) {
        if (employee == null) {
            return;
        }

        getDvPayeeDetail().setDisbursementVoucherPayeeTypeCode(KFSConstants.PaymentPayeeTypes.EMPLOYEE);
        if (StringUtils.isNotBlank(employee.getEmployeeId())) {
            getDvPayeeDetail().setDisbVchrPayeeIdNumber(employee.getEmployeeId());
            ((CuDisbursementVoucherPayeeDetailExtension) getDvPayeeDetail().getExtension()).setDisbVchrPayeeIdType(
                    CuDisbursementVoucherConstants.DV_PAYEE_ID_TYP_EMPL);
        } else {
            getDvPayeeDetail().setDisbVchrPayeeIdNumber(employee.getPrincipalId());
            ((CuDisbursementVoucherPayeeDetailExtension) getDvPayeeDetail().getExtension()).setDisbVchrPayeeIdType(
                    CuDisbursementVoucherConstants.DV_PAYEE_ID_TYP_ENTITY);
        }
        ((CuDisbursementVoucherPayeeDetailExtension) getDvPayeeDetail().getExtension()).setPayeeTypeSuffix(StringUtils.EMPTY);
        // Changed this from employee.getName to employee.getNameUnmasked() otherwise "Xxxxxx" appears on the DV!
        getDvPayeeDetail().setDisbVchrPayeePersonName(employee.getNameUnmasked());

        getDvPayeeDetail().setDisbVchrPayeeLine1Addr(employee.getAddressLine1Unmasked());
        getDvPayeeDetail().setDisbVchrPayeeLine2Addr(employee.getAddressLine2Unmasked());
        getDvPayeeDetail().setDisbVchrPayeeCityName(employee.getAddressCityUnmasked());
        getDvPayeeDetail().setDisbVchrPayeeStateCode(employee.getAddressStateProvinceCodeUnmasked());
        getDvPayeeDetail().setDisbVchrPayeeZipCode(employee.getAddressPostalCodeUnmasked());
        getDvPayeeDetail().setDisbVchrPayeeCountryCode(employee.getAddressCountryCodeUnmasked());

        //KFSMI-8935: When an employee is inactive, the Payment Type field on DV documents should display the message
        // "Is this payee an employee" = No
        if (employee.isActive()) {
            getDvPayeeDetail().setDisbVchrPayeeEmployeeCode(true);
        } else {
            getDvPayeeDetail().setDisbVchrPayeeEmployeeCode(false);
        }

        // I'm assuming that if a tax id type code other than 'TAX' is present, then the employee must be foreign
        for (final String externalIdentifierTypeCode : employee.getExternalIdentifiers().keySet()) {
            if (KimConstants.PersonExternalIdentifierTypes.TAX.equals(externalIdentifierTypeCode)) {
                getDvPayeeDetail().setDisbVchrNonresidentPaymentCode(false);
            }
        }
        // Determine if employee is a research subject
        final ParameterEvaluator researchPaymentReasonCodeEvaluator = SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(
                DisbursementVoucherDocument.class, FPParameterConstants.RESEARCH_PAYMENT_REASONS,
                getDvPayeeDetail().getDisbVchrPaymentReasonCode());
        if (researchPaymentReasonCodeEvaluator.evaluationSucceeds()) {
            if (getParameterService().parameterExists(DisbursementVoucherDocument.class,
                    FPParameterConstants.RESEARCH_PAYMENT_LIMIT)) {
                final String researchPayLimit = getParameterService().getParameterValueAsString(
                        DisbursementVoucherDocument.class, FPParameterConstants.RESEARCH_PAYMENT_LIMIT);
                if (StringUtils.isNotBlank(researchPayLimit)) {
                    final KualiDecimal payLimit = new KualiDecimal(researchPayLimit);

                    if (getDisbVchrCheckTotalAmount().isLessThan(payLimit)) {
                        getDvPayeeDetail().setDvPayeeSubjectPaymentCode(true);
                    }
                }
            }
        }

        disbVchrPayeeTaxControlCode = "";
        disbVchrPayeeW9CompleteCode = true;
    }

    /**
     * Convenience method to set dv payee detail fields based on a given student.
     *
     * @param student
     */
    public void templateStudent(final Person student) {
        if (student == null) {
            return;
        }

        getDvPayeeDetail().setDisbursementVoucherPayeeTypeCode(CuDisbursementVoucherConstants.DV_PAYEE_TYPE_STUDENT);

        getDvPayeeDetail().setDisbVchrPayeeIdNumber(student.getPrincipalId());
        ((CuDisbursementVoucherPayeeDetailExtension) getDvPayeeDetail().getExtension()).setDisbVchrPayeeIdType(
                CuDisbursementVoucherConstants.DV_PAYEE_ID_TYP_ENTITY);
        ((CuDisbursementVoucherPayeeDetailExtension) getDvPayeeDetail().getExtension()).setPayeeTypeSuffix(StringUtils.EMPTY);

        getDvPayeeDetail().setDisbVchrPayeePersonName(student.getNameUnmasked());

        getDvPayeeDetail().setDisbVchrPayeeLine1Addr(student.getAddressLine1Unmasked());
        getDvPayeeDetail().setDisbVchrPayeeLine2Addr(student.getAddressLine2Unmasked());
        getDvPayeeDetail().setDisbVchrPayeeCityName(student.getAddressCityUnmasked());
        getDvPayeeDetail().setDisbVchrPayeeStateCode(student.getAddressStateProvinceCodeUnmasked());
        getDvPayeeDetail().setDisbVchrPayeeZipCode(student.getAddressPostalCodeUnmasked());
        getDvPayeeDetail().setDisbVchrPayeeCountryCode(student.getAddressCountryCodeUnmasked());

        // I'm assuming that if a tax id type code other than 'TAX' is present, then the student must be foreign
        for (final String externalIdentifierTypeCode : student.getExternalIdentifiers().keySet()) {
            if (KimConstants.PersonExternalIdentifierTypes.TAX.equals(externalIdentifierTypeCode)) {
                getDvPayeeDetail().setDisbVchrNonresidentPaymentCode(false);
            }
        }
        // Determine if student is a research subject


        final ParameterEvaluator researchPaymentReasonCodeEvaluator = SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(
                DisbursementVoucherDocument.class, FPParameterConstants.RESEARCH_PAYMENT_REASONS);
        if (researchPaymentReasonCodeEvaluator.evaluationSucceeds()) {
            if (getParameterService().parameterExists(DisbursementVoucherDocument.class,
                    FPParameterConstants.RESEARCH_PAYMENT_LIMIT)) {
                final String researchPayLimit = getParameterService().getParameterValueAsString(DisbursementVoucherDocument.class,
                        FPParameterConstants.RESEARCH_PAYMENT_LIMIT);
                if (StringUtils.isNotBlank(researchPayLimit)) {
                    final KualiDecimal payLimit = new KualiDecimal(researchPayLimit);

                    if (getDisbVchrCheckTotalAmount().isLessThan(payLimit)) {
                        getDvPayeeDetail().setDvPayeeSubjectPaymentCode(true);
                    }
                }
            }
        }

        disbVchrPayeeTaxControlCode = "";
        disbVchrPayeeW9CompleteCode = true;
    }

    /**
     * Convenience method to set dv payee detail fields based on a given Alumnus.
     *
     * @param alumni
     */
    public void templateAlumni(final Person alumni) {
        if (alumni == null) {
            return;
        }

        getDvPayeeDetail().setDisbursementVoucherPayeeTypeCode(CuDisbursementVoucherConstants.DV_PAYEE_TYPE_ALUMNI);

        getDvPayeeDetail().setDisbVchrPayeeIdNumber(alumni.getPrincipalId());
        ((CuDisbursementVoucherPayeeDetailExtension) getDvPayeeDetail().getExtension()).setDisbVchrPayeeIdType(
                CuDisbursementVoucherConstants.DV_PAYEE_ID_TYP_ENTITY);
        ((CuDisbursementVoucherPayeeDetailExtension) getDvPayeeDetail().getExtension()).setPayeeTypeSuffix(StringUtils.EMPTY);

        // Changed this from employee.getName to employee.getNameUnmasked() otherwise "Xxxxxx" appears on the DV!
        getDvPayeeDetail().setDisbVchrPayeePersonName(alumni.getNameUnmasked());

        getDvPayeeDetail().setDisbVchrPayeeLine1Addr(alumni.getAddressLine1Unmasked());
        getDvPayeeDetail().setDisbVchrPayeeLine2Addr(alumni.getAddressLine2Unmasked());
        getDvPayeeDetail().setDisbVchrPayeeCityName(alumni.getAddressCityUnmasked());
        getDvPayeeDetail().setDisbVchrPayeeStateCode(alumni.getAddressStateProvinceCodeUnmasked());
        getDvPayeeDetail().setDisbVchrPayeeZipCode(alumni.getAddressPostalCodeUnmasked());
        getDvPayeeDetail().setDisbVchrPayeeCountryCode(alumni.getAddressCountryCodeUnmasked());

        // I'm assuming that if a tax id type code other than 'TAX' is present, then the alumni must be foreign
        for (final String externalIdentifierTypeCode : alumni.getExternalIdentifiers().keySet()) {
            if (KimConstants.PersonExternalIdentifierTypes.TAX.equals(externalIdentifierTypeCode)) {
                getDvPayeeDetail().setDisbVchrNonresidentPaymentCode(false);
            }
        }
        // Determine if alumni is a research subject
        final ParameterEvaluator researchPaymentReasonCodeEvaluator = SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(
                DisbursementVoucherDocument.class, FPParameterConstants.RESEARCH_PAYMENT_REASONS,
                getDvPayeeDetail().getDisbVchrPaymentReasonCode());
        if (researchPaymentReasonCodeEvaluator.evaluationSucceeds()) {
            if (getParameterService().parameterExists(DisbursementVoucherDocument.class,
                    FPParameterConstants.RESEARCH_PAYMENT_LIMIT)) {
                final String researchPayLimit = getParameterService().getParameterValueAsString(DisbursementVoucherDocument.class,
                        FPParameterConstants.RESEARCH_PAYMENT_LIMIT);
                if (StringUtils.isNotBlank(researchPayLimit)) {
                    final KualiDecimal payLimit = new KualiDecimal(researchPayLimit);

                    if (getDisbVchrCheckTotalAmount().isLessThan(payLimit)) {
                        getDvPayeeDetail().setDvPayeeSubjectPaymentCode(true);
                    }
                }
            }
        }

        disbVchrPayeeTaxControlCode = "";
        disbVchrPayeeW9CompleteCode = true;
    }

    @Override
    public void prepareForSave() {

		if (getDocumentHeader().getFinancialDocumentStatusCode()
				.equals(KFSConstants.DocumentStatusCodes.ENROUTE)
				&& !getDocumentHeader().getWorkflowDocument().isCompletionRequested()) {
			if (getParameterService().parameterExists(KfsParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class,
					UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_PARAMETER_NAME)
					&& getParameterService().getParameterValueAsBoolean(
							KfsParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class,
							UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_PARAMETER_NAME)) {
				getDocumentHeader().setFinancialDocumentTotalAmount(getTotalDollarAmount());
			}
		} else {
			getDocumentHeader().setFinancialDocumentTotalAmount(getTotalDollarAmount());
		}

        captureWorkflowHeaderInformation();

        if (wireTransfer != null) {
            wireTransfer.setDocumentNumber(documentNumber);
            ((DisbursementVoucherWireTransferExtendedAttribute) wireTransfer.getExtension()).setDocumentNumber(documentNumber);
        }

        if (dvNonresidentTax != null) {
            dvNonresidentTax.setDocumentNumber(documentNumber);
        }

        dvPayeeDetail.setDocumentNumber(documentNumber);
        ((CuDisbursementVoucherPayeeDetailExtension)dvPayeeDetail.getExtension()).setDocumentNumber(documentNumber);

        if (dvNonEmployeeTravel != null) {
            dvNonEmployeeTravel.setDocumentNumber(documentNumber);
            dvNonEmployeeTravel.setTotalTravelAmount(dvNonEmployeeTravel.getTotalTravelAmount());
        }

        if (dvPreConferenceDetail != null) {
            dvPreConferenceDetail.setDocumentNumber(documentNumber);
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
                    KFSConstants.CoreModuleNamespaces.KFS,
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

    @Override
    public void populateDocumentForRouting() {
        final CuDisbursementVoucherPayeeDetail payeeDetail = (CuDisbursementVoucherPayeeDetail) getDvPayeeDetail();

        if (payeeDetail.isVendor()) {
            payeeDetail.setDisbVchrPayeeEmployeeCode(
                getVendorService().isVendorInstitutionEmployee(payeeDetail.getDisbVchrVendorHeaderIdNumberAsInteger()));
            payeeDetail.setDvPayeeSubjectPaymentCode(
                getVendorService().isSubjectPaymentVendor(payeeDetail.getDisbVchrVendorHeaderIdNumberAsInteger()));
        } else if (payeeDetail.isEmployee() || payeeDetail.isStudent() || payeeDetail.isAlumni()) {

            // Determine if employee student or alumni is a research subject
            final ParameterEvaluator researchPaymentReasonCodeEvaluator = /*REFACTORME*/SpringContext
                .getBean(ParameterEvaluatorService.class).getParameterEvaluator(DisbursementVoucherDocument.class,
                    FPParameterConstants.RESEARCH_PAYMENT_REASONS,
                    payeeDetail.getDisbVchrPaymentReasonCode());
            if (researchPaymentReasonCodeEvaluator.evaluationSucceeds()
                    && getParameterService().parameterExists(DisbursementVoucherDocument.class,
                            FPParameterConstants.RESEARCH_PAYMENT_LIMIT)) {
                final String researchPayLimit = getParameterService()
                        .getParameterValueAsString(DisbursementVoucherDocument.class,
                                FPParameterConstants.RESEARCH_PAYMENT_LIMIT);
                if (StringUtils.isNotBlank(researchPayLimit)) {
                    final KualiDecimal payLimit = new KualiDecimal(researchPayLimit);

                    if (getDisbVchrCheckTotalAmount().isLessThan(payLimit)) {
                        payeeDetail.setDvPayeeSubjectPaymentCode(true);
                    }
                }
            }
        }

        // Call last, serializes to XML
        super.populateDocumentForRouting();
    }

    @Override
    public void toCopy() {
        String payeeidNumber = getDvPayeeDetail().getDisbVchrPayeeIdNumber();

        super.toCopy();

        getDvPayeeDetail().setDisbVchrPayeeIdNumber(payeeidNumber);
    }

    /**
     * Clear fields that shouldn't be copied to to the new DV.
     */
    @Override
    protected void clearFieldsThatShouldNotBeCopied() {
        super.clearFieldsThatShouldNotBeCopied();
        setTripAssociationStatusCode(CuFPConstants.IS_NOT_TRIP_DOC);
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
            final VendorDetail vendorDetail = getVendorService().getVendorDetail(getDvPayeeDetail().getDisbVchrVendorHeaderIdNumberAsInteger(), getDvPayeeDetail().getDisbVchrVendorDetailAssignedIdNumberAsInteger());
            if (vendorDetail == null) {
                clearPayee(FPKeyConstants.WARNING_DV_PAYEE_NON_EXISTENT_CLEARED);
            } else {
                final DisbursementPayee payee = getDisbursementVoucherPayeeService().getPayeeFromVendor(vendorDetail);
                if (!getDisbursementVoucherPaymentReasonService().isPayeeQualifiedForPayment(payee, getDvPayeeDetail().getDisbVchrPaymentReasonCode())) {
                    clearPayee(FPKeyConstants.MESSAGE_DV_PAYEE_INVALID_PAYMENT_TYPE_CLEARED);
                }
            }
        }
    }

    @Override
    protected void clearPayee(final String messageKey) {
        dvPayeeDetail = new CuDisbursementVoucherPayeeDetail();
        getDvPayeeDetail().setDisbVchrPayeeIdNumber(StringUtils.EMPTY);
        clearDvPayeeIdType();
        KNSGlobalVariables.getMessageList().add(messageKey);
    }

    protected void clearDvPayeeIdType() {
        ((CuDisbursementVoucherPayeeDetailExtension)getDvPayeeDetail().getExtension()).setDisbVchrPayeeIdType(StringUtils.EMPTY);
        ((CuDisbursementVoucherPayeeDetailExtension)getDvPayeeDetail().getExtension()).setPayeeTypeSuffix(StringUtils.EMPTY);
    }

    @Override
    public void initiateDocument() {
        final PhoneNumberService phoneNumberService = SpringContext.getBean(PhoneNumberService.class);
        final Person currentUser = GlobalVariables.getUserSession().getPerson();
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
        final ChartOrgHolder chartOrg = SpringContext.getBean(org.kuali.kfs.sys.service.FinancialSystemUserService.class).getPrimaryOrganization(currentUser, KFSConstants.CoreModuleNamespaces.FINANCIAL);

        // Does a valid campus code exist for this person?  If so, simply grab
        // the campus code via the business object service.
        if (chartOrg != null && chartOrg.getOrganization() != null) {
            setCampusCode(chartOrg.getOrganization().getOrganizationPhysicalCampusCode());
        } else {
            // A valid campus code was not found; therefore, use the default affiliated  campus code.
            final String affiliatedCampusCode = currentUser.getCampusCode();
            setCampusCode(affiliatedCampusCode);
        }

        setDisbursementVoucherDueDate(getCuDisbursementVoucherDefaultDueDateService().findDefaultDueDate());

        // default doc location
        if (StringUtils.isBlank(getDisbursementVoucherDocumentationLocationCode())) {
            setDisbursementVoucherDocumentationLocationCode(getParameterService().getParameterValueAsString(DisbursementVoucherDocument.class, FPParameterConstants.DOCUMENTATION_LOCATION));
        }

        updateBankBasedOnPaymentMethodCode();
    }

    @Override
    public boolean generateDocumentGeneralLedgerPendingEntries(final GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
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
                final WireCharge wireCharge = getPaymentSourceHelperService().retrieveCurrentYearWireCharge();
                //KFSPTS-764: Added if check to eliminate zero dollar wire charge generating zero dollar accounting entries
                if (!isZeroDollarWireCharge(wireCharge)) {

                //KFSPTS-764: only generate GLPE entries when wire charges are NOT zero dollars.
                // generate debits
                final GeneralLedgerPendingEntry chargeEntry = getPaymentSourceHelperService().processWireChargeDebitEntries(this, sequenceHelper, wireCharge);

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
    public boolean answerSplitNodeQuestion(final String nodeName) throws UnsupportedOperationException {
        if (nodeName.equals(DOCUMENT_REQUIRES_AWARD_REVIEW_SPLIT))
            return isCAndGReviewRequired();
        if (nodeName.equals(DOCUMENT_REQUIRES_CAMPUS_REVIEW_SPLIT))
            return isCampusReviewRequired();
        if (nodeName.equals(DOCUMENT_REQUIRES_TAX_REVIEW_SPLIT)) {
            return isTaxReviewRequired();
        }
        if (nodeName.equals(DOCUMENT_REQUIRES_TRAVEL_REVIEW_SPLIT)) {
            return isTravelReviewRequired();
        }
        if (nodeName.equals(DOCUMENT_REQUIRES_SEPARATION_OF_DUTIES)) {
            return false;
        }
        throw new UnsupportedOperationException("Cannot answer split question for this node you call \""+nodeName+"\"");
    }

    @Override
    public boolean isTravelReviewRequired() {
        List<AccountingLine> theList = (List<AccountingLine>) sourceAccountingLines;

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
        if (disbVchrCheckTotalAmount.isGreaterEqual(dollarThresholdDecimal)) {
            overDollarThreshold = true;
        }

        final String paymentReasonCode = getDvPayeeDetail().getDisbVchrPaymentReasonCode();

        return (getDisbursementVoucherPaymentReasonService().isPrepaidTravelPaymentReason(paymentReasonCode) || getDisbursementVoucherPaymentReasonService().isNonEmployeeTravelPaymentReason(paymentReasonCode) && overDollarThreshold);
    }

    protected boolean isCAndGReviewRequired() {

        String awardThreshold = getParameterService().getParameterValueAsString("KFS-FP", "DisbursementVoucher", DOLLAR_THRESHOLD_REQUIRING_AWARD_REVIEW);
        KualiDecimal dollarThresholdDecimal = new KualiDecimal(awardThreshold);
        if ( disbVchrCheckTotalAmount.isGreaterEqual(dollarThresholdDecimal)) {
            return true;
        }

        List<AccountingLine> theList = (List<AccountingLine>) sourceAccountingLines;
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

        List<ActionTaken> actions = RouteContext.getCurrentRouteContext().getDocument().getActionsTaken();
        List<String> people = new ArrayList<String>();
        for(ActionTaken atv: actions) {
            if( !people.contains(atv.getPrincipalId())) {
                people.add(atv.getPrincipalId());
            }
        }
        if (people.size()<2)
        {
            return true;
        }

        List<AccountingLine> theList = (List<AccountingLine>) sourceAccountingLines;

        for (AccountingLine alb : theList )
        {
            ParameterEvaluator objectCodes = SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator("KFS-FP", "DisbursementVoucher", OBJECT_CODES_REQUIRING_CAMPUS_REVIEW, alb.getFinancialObjectCode());
            if (objectCodes.evaluationSucceeds())
            {
                LOG.info("Object Code " + alb.getFinancialObjectCode() + " requires this document to undergo Campus review.");
                return true;
            }
        }

        ParameterEvaluator paymentReasons = SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator("KFS-FP", "DisbursementVoucher", PAYMENT_REASONS_REQUIRING_CAMPUS_REVIEW, dvPayeeDetail.getDisbVchrPaymentReasonCode());
        if (paymentReasons.evaluationSucceeds()) {
            return true;
        }

        String dollarThreshold = getParameterService().getParameterValueAsString("KFS-FP", "DisbursementVoucher", DOLLAR_THRESHOLD_REQUIRING_CAMPUS_REVIEW);
        KualiDecimal dollarThresholdDecimal = new KualiDecimal(dollarThreshold);
        if ( disbVchrCheckTotalAmount.isGreaterEqual(dollarThresholdDecimal)) {
            return true;
        }

        return false;
    }

    @Override
    protected boolean isTaxReviewRequired() {
        if (isPayeePurchaseOrderVendorHasWithholding()) {
            return true;
        }

        final String payeeTypeCode = getDvPayeeDetail().getDisbursementVoucherPayeeTypeCode();
        if (payeeTypeCode.equals(KFSConstants.PaymentPayeeTypes.EMPLOYEE)) {
            return false;
        } else if (payeeTypeCode.equals(KFSConstants.PaymentPayeeTypes.VENDOR)) {
            if(getVendorService().isVendorInstitutionEmployee(getDvPayeeDetail().getDisbVchrVendorHeaderIdNumberAsInteger())){
                return true;
            }
        }

        final String paymentReasonCode = getDvPayeeDetail().getDisbVchrPaymentReasonCode();
        Integer vendorHeaderId = getDvPayeeDetail().getDisbVchrVendorHeaderIdNumberAsInteger();
        if (getCuDisbursementVoucherTaxService().isForeignVendorAndTaxReviewRequired(payeeTypeCode, paymentReasonCode, vendorHeaderId)) {
        	return true;
        }

        final String taxControlCode = getDisbVchrPayeeTaxControlCode();
        if (StringUtils.equals(taxControlCode, TAX_CONTROL_BACKUP_HOLDING) || StringUtils.equals(taxControlCode, TAX_CONTROL_HOLD_PAYMENTS)) {
            return true;
        }


        if (getDisbursementVoucherPaymentReasonService().isDecedentCompensationPaymentReason(paymentReasonCode)) {
            return true;
        }

        if (getDisbursementVoucherPaymentReasonService().isMovingPaymentReason(paymentReasonCode) && taxedCampusForMovingReimbursements()) {
            return true;
        }

        if (getParameterEvaluatorService().getParameterEvaluator(DisbursementVoucherDocument.class, PAYMENT_REASONS_REQUIRING_TAX_REVIEW_PARAMETER_NAME, paymentReasonCode).evaluationSucceeds()) {
            return true;
        }

        return false;
    }

    public boolean isLegacyTrip() {
        return StringUtils.equals(getTripAssociationStatusCode(), CuFPConstants.IS_TRIP_DOC);
    }

    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        if (getDocumentHeader().getWorkflowDocument().isProcessed()) {
            if (getCuCheckStubService().doesCheckStubNeedTruncatingForIso20022(this)) {
                getCuCheckStubService().addNoteToDocumentRegardingCheckStubIso20022MaxLength(this);
            }
        }
        super.doRouteStatusChange(statusChangeEvent);
    }

    protected CuDisbursementVoucherTaxService getCuDisbursementVoucherTaxService() {
        return SpringContext.getBean(CuDisbursementVoucherTaxService.class);
    }

    protected ParameterEvaluatorService getParameterEvaluatorService(){
    	return SpringContext.getBean(ParameterEvaluatorService.class);
    }

    @Override
    public void setDvPayeeDetail(final DisbursementVoucherPayeeDetail dvPayeeDetail) {
        this.dvPayeeDetail = (CuDisbursementVoucherPayeeDetail) dvPayeeDetail;
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

    public static CuDisbursementVoucherDefaultDueDateService getCuDisbursementVoucherDefaultDueDateService() {
        if (cuDisbursementVoucherDefaultDueDateService == null) {
            cuDisbursementVoucherDefaultDueDateService = SpringContext.getBean(CuDisbursementVoucherDefaultDueDateService.class);
        }
        return cuDisbursementVoucherDefaultDueDateService;
    }

    public static void setCuDisbursementVoucherDefaultDueDateService(CuDisbursementVoucherDefaultDueDateService cuDisbursementVoucherDefaultDueDateService) {
        CuDisbursementVoucherDocument.cuDisbursementVoucherDefaultDueDateService = cuDisbursementVoucherDefaultDueDateService;
    }

    public static CuCheckStubService getCuCheckStubService() {
        if (cuCheckStubService == null) {
            cuCheckStubService = SpringContext.getBean(CuCheckStubService.class);
        }
        return cuCheckStubService;
    }

}

