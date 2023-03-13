package edu.cornell.kfs.fp.document.service.impl;


import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.parameter.ParameterEvaluator;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.fp.FPParameterConstants;
import org.kuali.kfs.fp.batch.DvToPdpExtractStep;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeExpense;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeTravel;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPreConferenceDetail;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPreConferenceRegistrant;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.service.impl.DisbursementVoucherExtractionHelperServiceImpl;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpParameterConstants;
import org.kuali.kfs.pdp.businessobject.PaymentAccountDetail;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentNoteText;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.document.service.PaymentSourceHelperService;
import org.kuali.kfs.sys.service.XmlUtilService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherConstants;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherExtractionHelperService;
import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;


public class CuDisbursementVoucherExtractionHelperServiceImpl extends DisbursementVoucherExtractionHelperServiceImpl implements CuDisbursementVoucherExtractionHelperService{
    private static final Logger LOG = LogManager.getLogger(CuDisbursementVoucherExtractionHelperServiceImpl.class);
    
    protected CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService;
    private XmlUtilService xmlUtilService;

    @Override
    public PaymentGroup createPaymentGroup(DisbursementVoucherDocument document, Date processRunDate) {
        LOG.debug("createPaymentGroupForDisbursementVoucher() started");

        PaymentGroup pg = new PaymentGroup();
        pg.setCombineGroups(Boolean.TRUE);
        pg.setCampusAddress(Boolean.FALSE);

        CuDisbursementVoucherPayeeDetail pd = 
        		businessObjectService.findBySinglePrimaryKey(CuDisbursementVoucherPayeeDetail.class, document.getDocumentNumber());
        String rc = pd.getDisbVchrPaymentReasonCode();

        if (KFSConstants.PaymentPayeeTypes.CUSTOMER.equals(document.getDvPayeeDetail().getDisbursementVoucherPayeeTypeCode())) {
            pg.setPayeeIdTypeCd(PdpConstants.PayeeIdTypeCodes.CUSTOMER);
            pg.setTaxablePayment(Boolean.FALSE);
        } else if ((pd.isVendor() && vendorService.isVendorInstitutionEmployee(pd.getDisbVchrVendorHeaderIdNumberAsInteger()))
                    || document.getDvPayeeDetail().isEmployee()) {
        		// If the payee is an employee, set these flags accordingly
            pg.setEmployeeIndicator(Boolean.TRUE);
            pg.setPayeeIdTypeCd(PdpConstants.PayeeIdTypeCodes.EMPLOYEE);
            pg.setTaxablePayment(parameterEvaluatorService.getParameterEvaluator(DisbursementVoucherDocument.class,
                            FPParameterConstants.RESEARCH_PAYMENT_REASONS, rc).evaluationSucceeds()
                        && !parameterService.getParameterValueAsString(DisbursementVoucherDocument.class,
                            FPParameterConstants.PAYMENT_REASON_CODE_RENTAL_PAYMENT).equals(rc)
                        && !parameterService.getParameterValueAsString(DisbursementVoucherDocument.class,
                            FPParameterConstants.PAYMENT_REASON_CODE_ROYALTIES).equals(rc));
        }
        // KFSUPGRADE-973 : Cu mods
        // If the payee is an alumni or student, set these flags accordingly
        else if(pd.isStudent() || pd.isAlumni()) {
            pg.setPayeeIdTypeCd(PdpConstants.PayeeIdTypeCodes.ENTITY);

            // All payments are taxable except research participant, rental & royalties
            pg.setTaxablePayment(
                    !parameterEvaluatorService.getParameterEvaluator(CuDisbursementVoucherDocument.class,
                            FPParameterConstants.RESEARCH_PAYMENT_REASONS, rc).evaluationSucceeds()
                        && !CuDisbursementVoucherConstants.PaymentReasonCodes.RENTAL_PAYMENT.equals(rc)
                        && !CuDisbursementVoucherConstants.PaymentReasonCodes.ROYALTIES.equals(rc));
        }
        else {

            // These are taxable
            VendorDetail vendDetail = vendorService.getVendorDetail(pd.getDisbVchrVendorHeaderIdNumberAsInteger(),
                    pd.getDisbVchrVendorDetailAssignedIdNumberAsInteger());
            String vendorOwnerCode = vendDetail.getVendorHeader().getVendorOwnershipCode();
            String vendorOwnerCategoryCode = vendDetail.getVendorHeader().getVendorOwnershipCategoryCode();
            String payReasonCode = pd.getDisbVchrPaymentReasonCode();

            pg.setPayeeIdTypeCd(PdpConstants.PayeeIdTypeCodes.VENDOR_ID);

            // Assume it is not taxable until proven otherwise
            pg.setTaxablePayment(Boolean.FALSE);
            pg.setPayeeOwnerCd(vendorOwnerCode);

            ParameterEvaluator parameterEvaluator1 = /*REFACTORME*/parameterEvaluatorService.getParameterEvaluator(
                    DvToPdpExtractStep.class,
                    FPParameterConstants.TAXABLE_PAYMENT_REASON_CODES_BY_OWNERSHIP_CODES,
                    FPParameterConstants.NON_TAXABLE_PAYMENT_REASON_CODES_BY_OWNERSHIP_CODES,
                    vendorOwnerCode, payReasonCode);
            ParameterEvaluator parameterEvaluator2 = /*REFACTORME*/parameterEvaluatorService.getParameterEvaluator(
                    DvToPdpExtractStep.class,
                    FPParameterConstants.TAXABLE_PAYMENT_REASON_CODES_BY_CORPORATION_OWNERSHIP_TYPE_CATEGORY,
                    FPParameterConstants.NON_TAXABLE_PAYMENT_REASON_CODES_BY_CORPORATION_OWNERSHIP_TYPE_CATEGORY,
                    vendorOwnerCategoryCode, payReasonCode);
            
            if ( parameterEvaluator1.evaluationSucceeds() ) {
                pg.setTaxablePayment(Boolean.TRUE);
            } else if (parameterService.getParameterValueAsString(DvToPdpExtractStep.class,
                    FPParameterConstants.CORPORATION_OWNERSHIP_TYPE).equals("CP") &&
                StringUtils.isEmpty(vendorOwnerCategoryCode) &&
                      /*REFACTORME*/parameterEvaluatorService.getParameterEvaluator(DvToPdpExtractStep.class,
                              FPParameterConstants.TAXABLE_PAYMENT_REASON_CODES_FOR_BLANK_CORPORATION_OWNERSHIP_TYPE_CATEGORIES,
                    payReasonCode).evaluationSucceeds()) {
                pg.setTaxablePayment(Boolean.TRUE);
            } else if (parameterService.getParameterValueAsString(DvToPdpExtractStep.class,
                    FPParameterConstants.CORPORATION_OWNERSHIP_TYPE).equals("CP")
                && !StringUtils.isEmpty(vendorOwnerCategoryCode)
                && parameterEvaluator2.evaluationSucceeds()) {
                pg.setTaxablePayment(Boolean.TRUE);
            }
        }

        pg.setCity(pd.getDisbVchrPayeeCityName());
        pg.setCountry(pd.getDisbVchrPayeeCountryCode());
        pg.setLine1Address(pd.getDisbVchrPayeeLine1Addr());
        pg.setLine2Address(pd.getDisbVchrPayeeLine2Addr());
        pg.setPayeeName(pd.getDisbVchrPayeePersonName());
        pg.setPayeeId(pd.getDisbVchrPayeeIdNumber());
        pg.setState(pd.getDisbVchrPayeeStateCode());
        pg.setZipCd(pd.getDisbVchrPayeeZipCode());
        pg.setPaymentDate(document.getDisbursementVoucherDueDate());
        pg.setProcessImmediate(document.isImmediatePaymentIndicator());
        pg.setPymtAttachment(document.isDisbVchrAttachmentCode());
        pg.setPymtSpecialHandling(document.isDisbVchrSpecialHandlingCode());
        pg.setNonresidentPayment(pd.isDisbVchrNonresidentPaymentCode());

        pg.setBankCode(document.getDisbVchrBankCode());
        pg.setPaymentStatusCode(PdpConstants.PaymentStatusCodes.OPEN);

        // now add the payment detail
        final PaymentDetail paymentDetail = buildPaymentDetail(document, processRunDate);
        pg.addPaymentDetails(paymentDetail);
        paymentDetail.setPaymentGroup(pg);

        return pg;
    }

    protected PaymentDetail buildPaymentDetail(DisbursementVoucherDocument document, Date processRunDate) {
        LOG.debug("buildPaymentDetail() started");
        final String maxNoteLinesParam = parameterService.getParameterValueAsString(
                KfsParameterConstants.PRE_DISBURSEMENT_ALL.class, PdpParameterConstants.MAX_NOTE_LINES);

        int maxNoteLines;
        try {
            maxNoteLines = Integer.parseInt(maxNoteLinesParam);
        }
        catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Invalid Max Notes Lines parameter, value: " + maxNoteLinesParam +
                    " cannot be converted to an integer");
        }

        PaymentDetail pd = new PaymentDetail();
        if (StringUtils.isNotEmpty(document.getDocumentHeader().getOrganizationDocumentNumber())) {
            pd.setOrganizationDocNbr(document.getDocumentHeader().getOrganizationDocumentNumber());
        }
        pd.setCustPaymentDocNbr(document.getDocumentNumber());
        pd.setInvoiceNbr(xmlUtilService.filterOutIllegalXmlCharacters(document.getInvoiceNumber()));
        if (ObjectUtils.isNull(document.getInvoiceDate())) {
            pd.setInvoiceDate(new java.sql.Date(processRunDate.getTime()));
        } else {
            pd.setInvoiceDate(document.getInvoiceDate());
        }
        pd.setOrigInvoiceAmount(document.getDisbVchrCheckTotalAmount());
        pd.setInvTotDiscountAmount(KualiDecimal.ZERO);
        pd.setInvTotOtherCreditAmount(KualiDecimal.ZERO);
        pd.setInvTotOtherDebitAmount(KualiDecimal.ZERO);
        pd.setInvTotShipAmount(KualiDecimal.ZERO);
        pd.setNetPaymentAmount(document.getDisbVchrCheckTotalAmount());
        pd.setPrimaryCancelledPayment(Boolean.FALSE);
        pd.setFinancialDocumentTypeCode(DisbursementVoucherConstants.DOCUMENT_TYPE_CHECKACH);
        pd.setFinancialSystemOriginCode(KFSConstants.ORIGIN_CODE_KUALI);

        // Handle accounts
        for (SourceAccountingLine sal : (List<? extends SourceAccountingLine>)document.getSourceAccountingLines()) {
            PaymentAccountDetail pad = new PaymentAccountDetail();
            pad.setFinChartCode(sal.getChartOfAccountsCode());
            pad.setAccountNbr(sal.getAccountNumber());
            if (StringUtils.isNotEmpty(sal.getSubAccountNumber())) {
                pad.setSubAccountNbr(sal.getSubAccountNumber());
            }
            else {
                pad.setSubAccountNbr(KFSConstants.getDashSubAccountNumber());
            }
            pad.setFinObjectCode(sal.getFinancialObjectCode());
            if (StringUtils.isNotEmpty(sal.getFinancialSubObjectCode())) {
                pad.setFinSubObjectCode(sal.getFinancialSubObjectCode());
            }
            else {
                pad.setFinSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            }
            if (StringUtils.isNotEmpty(sal.getOrganizationReferenceId())) {
                pad.setOrgReferenceId(sal.getOrganizationReferenceId());
            }
            if (StringUtils.isNotEmpty(sal.getProjectCode())) {
                pad.setProjectCode(sal.getProjectCode());
            }
            else {
                pad.setProjectCode(KFSConstants.getDashProjectCode());
            }
            pad.setAccountNetAmount(sal.getAmount());
            pd.addAccountDetail(pad);
        }

        // Handle notes
        DisbursementVoucherPayeeDetail dvpd = document.getDvPayeeDetail();

        int line = 0;
        PaymentNoteText pnt = new PaymentNoteText();
        pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
        pnt.setCustomerNoteText(CuDisbursementVoucherConstants.DV_EXTRACT_NOTE_PREFIX_PREPARER + document.getDisbVchrContactPersonName() + " " + 
                document.getDisbVchrContactPhoneNumber());
        pd.addNote(pnt);

        String dvSpecialHandlingPersonName = dvpd.getDisbVchrSpecialHandlingPersonName();
        String dvSpecialHandlingLine1Address = dvpd.getDisbVchrSpecialHandlingLine1Addr();
        String dvSpecialHandlingLine2Address = dvpd.getDisbVchrSpecialHandlingLine2Addr();
        String dvSpecialHandlingCity = dvpd.getDisbVchrSpecialHandlingCityName();
        String dvSpecialHandlingState = dvpd.getDisbVchrSpecialHandlingStateCode();
        String dvSpecialHandlingZip = dvpd.getDisbVchrSpecialHandlingZipCode();

        if (StringUtils.isNotEmpty(dvSpecialHandlingPersonName)) {
            pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
            pnt.setCustomerNoteText("Send Check To: " + dvSpecialHandlingPersonName);
            LOG.debug("Creating special handling person name note: {}", pnt::getCustomerNoteText);
            pd.addNote(pnt);
        }
        if (StringUtils.isNotEmpty(dvSpecialHandlingLine1Address)) {
            pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
            pnt.setCustomerNoteText(CuDisbursementVoucherConstants.DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_ADDRESS1 + dvSpecialHandlingLine1Address);
            LOG.debug("Creating special handling address 1 note: {}", pnt::getCustomerNoteText);
            pd.addNote(pnt);
        }
        if (StringUtils.isNotEmpty(dvSpecialHandlingLine2Address)) {
            pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
            pnt.setCustomerNoteText(CuDisbursementVoucherConstants.DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_ADDRESS2 + dvSpecialHandlingLine2Address);
            LOG.debug("Creating special handling address 2 note: {}", pnt::getCustomerNoteText);
            pd.addNote(pnt);
        }
        if (StringUtils.isNotEmpty(dvSpecialHandlingCity)) {
            pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
            pnt.setCustomerNoteText(CuDisbursementVoucherConstants.DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_ADDRESS3 + dvSpecialHandlingCity + ", " + dvSpecialHandlingState + " " + dvSpecialHandlingZip);
            LOG.debug("Creating special handling city note: {}", pnt::getCustomerNoteText);
            pd.addNote(pnt);
        }
        if (document.isDisbVchrAttachmentCode()) {
            pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
            pnt.setCustomerNoteText("Attachment Included");
            LOG.debug("create attachment note: {}", pnt::getCustomerNoteText);
            pd.addNote(pnt);
        }

        String paymentReasonCode = dvpd.getDisbVchrPaymentReasonCode();
        if (/*REFACTORME*/parameterEvaluatorService.getParameterEvaluator(DisbursementVoucherDocument.class,
                FPParameterConstants.NON_EMPLOYEE_TRAVEL_PAY_REASONS, paymentReasonCode).evaluationSucceeds()) {
            DisbursementVoucherNonEmployeeTravel dvnet = document.getDvNonEmployeeTravel();

            pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
            pnt.setCustomerNoteText("Reimbursement associated with " + dvnet.getDisbVchrServicePerformedDesc());
            LOG.debug("Creating non employee travel notes: {}", pnt::getCustomerNoteText);
            pd.addNote(pnt);

            pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
            pnt.setCustomerNoteText("The total per diem amount for your daily expenses is " +
                    dvnet.getDisbVchrPerdiemActualAmount());
            LOG.debug("Creating non employee travel notes: {}", pnt::getCustomerNoteText);
            pd.addNote(pnt);

            if (dvnet.getDisbVchrPersonalCarAmount() != null && dvnet.getDisbVchrPersonalCarAmount().compareTo(KualiDecimal.ZERO) != 0) {
                pnt = new PaymentNoteText();
                pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
                pnt.setCustomerNoteText("The total dollar amount for your vehicle mileage is " +
                        dvnet.getDisbVchrPersonalCarAmount());
                LOG.debug("Creating non employee travel vehicle note: {}", pnt::getCustomerNoteText);
                pd.addNote(pnt);

                for (DisbursementVoucherNonEmployeeExpense exp : (List<DisbursementVoucherNonEmployeeExpense>)dvnet.getDvNonEmployeeExpenses()) {
                    if (line < maxNoteLines - 8) {
                        pnt = new PaymentNoteText();
                        pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
                        pnt.setCustomerNoteText(exp.getDisbVchrExpenseCompanyName() + " " + exp.getDisbVchrExpenseAmount());
                        LOG.debug("Creating non employee travel expense note: {}", pnt::getCustomerNoteText);
                        pd.addNote(pnt);
                    }
                }
            }
        } else if (/*REFACTORME*/parameterEvaluatorService.getParameterEvaluator(DisbursementVoucherDocument.class,
                FPParameterConstants.PREPAID_TRAVEL_PAYMENT_REASONS, paymentReasonCode).evaluationSucceeds()) {
            pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
            pnt.setCustomerNoteText("Payment is for the following individuals/charges:");
            pd.addNote(pnt);
            LOG.info("Creating prepaid travel note note: {}", pnt::getCustomerNoteText);

            DisbursementVoucherPreConferenceDetail dvpcd = document.getDvPreConferenceDetail();

            for (DisbursementVoucherPreConferenceRegistrant dvpcr : 
                    (List<DisbursementVoucherPreConferenceRegistrant>)dvpcd.getDvPreConferenceRegistrants()) {
                if (line < maxNoteLines - 8) {
                    pnt = new PaymentNoteText();
                    pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
                    pnt.setCustomerNoteText(dvpcr.getDvConferenceRegistrantName() + " " + dvpcr.getDisbVchrExpenseAmount());
                    LOG.debug("Creating pre-paid conference registrants note: {}", pnt::getCustomerNoteText);
                    pd.addNote(pnt);
                }
            }
        }

        final String text = xmlUtilService.filterOutIllegalXmlCharacters(document.getDisbVchrCheckStubText());
        if (!StringUtils.isBlank(text)) {
            pd.addNotes(paymentSourceHelperService.buildNotesForCheckStubText(text, line));
        }

        return pd;
    }

    public Map<String, List<DisbursementVoucherDocument>> retrievePaymentSourcesByCampus(boolean immediatesOnly) {
        LOG.debug("retrievePaymentSourcesByCampus() started");

        if (immediatesOnly) {
            throw new UnsupportedOperationException("DisbursementVoucher PDP does immediates extraction through normal " +
                    "document processing; immediates for DisbursementVoucher should not be run through batch.");
        }

        Map<String, List<DisbursementVoucherDocument>> documentsByCampus = new HashMap<>();

        Collection<DisbursementVoucherDocument> docs = disbursementVoucherDao.getDocumentsByHeaderStatus(
                KFSConstants.DocumentStatusCodes.APPROVED, false);
        for (DisbursementVoucherDocument element : docs) {
            String dvdCampusCode = element.getCampusCode();
            if (StringUtils.isNotBlank(dvdCampusCode)) {
                if (documentsByCampus.containsKey(dvdCampusCode) 
                		// KFSUPGRADE-973
                		&& getPaymentMethodGeneralLedgerPendingEntryService().isPaymentMethodProcessedUsingPdp(element.getDisbVchrPaymentMethodCode())) {

                    List<DisbursementVoucherDocument> documents = documentsByCampus.get(dvdCampusCode);
                    documents.add(element);
                }
                else {
                    List<DisbursementVoucherDocument> documents = new ArrayList<>();
                    documents.add(element);
                    documentsByCampus.put(dvdCampusCode, documents);
                }
            }
        }

        return documentsByCampus;
    }

    // KFSPTS-1891 : calling PaymentMethodGeneralLedgerPendingEntryService().isPaymentMethodProcessedUsingPdp
    // TODO :  this method does not seem to be referenced.  
//  @Override
    protected Set<String> getCampusListByDocumentStatusCode(String statusCode) {
        LOG.debug("getCampusListByDocumentStatusCode() started");

        Set<String> campusSet = new HashSet<String>();

        Collection<DisbursementVoucherDocument> docs = disbursementVoucherDao.getDocumentsByHeaderStatus(statusCode, false);
        for (DisbursementVoucherDocument doc : docs) {
            if ( getPaymentMethodGeneralLedgerPendingEntryService().isPaymentMethodProcessedUsingPdp(doc.getDisbVchrPaymentMethodCode()) ) {
                campusSet.add(doc.getCampusCode());
            }
        }

        return campusSet;
    }

	public CUPaymentMethodGeneralLedgerPendingEntryService getPaymentMethodGeneralLedgerPendingEntryService() {
		return paymentMethodGeneralLedgerPendingEntryService;
	}

	public void setPaymentMethodGeneralLedgerPendingEntryService(
			CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService) {
		this.paymentMethodGeneralLedgerPendingEntryService = paymentMethodGeneralLedgerPendingEntryService;
	}
	   
	   @Override
	   public boolean shouldExtractPayment(DisbursementVoucherDocument paymentSource) {
	       LOG.debug("paymentSource: " + paymentSource.getClass());
	       if (isRecurringDV(paymentSource)) {
	           LOG.debug("shouldExtractPayment: found a recurring DV, returning false for " + paymentSource.getDocumentNumber());
	           return false;
	       }
	       boolean shouldExtract = super.shouldExtractPayment(paymentSource);
	       LOG.debug("shouldExtractPayment: Found a non recurring DV with a document number of " + paymentSource.getDocumentNumber() + " and returning " + shouldExtract);
	       return shouldExtract;
	   }
	   
	   private boolean isRecurringDV(DisbursementVoucherDocument paymentSource) {
	       boolean isRecurringDV = false;
	       if (CuFPConstants.RecurringDisbursementVoucherDocumentConstants.RECURRING_DV_DOCUMENT_TYPE_NAME.equalsIgnoreCase(
                   paymentSource.getDocumentHeader().getWorkflowDocument().getDocumentTypeName())) {
               isRecurringDV = true;
           }
	       return isRecurringDV;
	   }
	   
	   public PaymentSourceHelperService getPaymentSourceHelperService() {
	             return paymentSourceHelperService;
	   }

    @Override
    public void setXmlUtilService(XmlUtilService xmlUtilService) {
        super.setXmlUtilService(xmlUtilService);
        this.xmlUtilService = xmlUtilService;
    }
}
