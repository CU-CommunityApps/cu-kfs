package edu.cornell.kfs.fp.batch.service.impl;

import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.kuali.kfs.fp.batch.DvToPdpExtractStep;
import org.kuali.kfs.fp.batch.service.impl.DisbursementVoucherExtractServiceImpl;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeExpense;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeTravel;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPreConferenceDetail;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPreConferenceRegistrant;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpParameterConstants;
import org.kuali.kfs.pdp.businessobject.Batch;
import org.kuali.kfs.pdp.businessobject.PaymentAccountDetail;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentNoteText;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.core.api.parameter.ParameterEvaluator;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.api.util.type.KualiInteger;

import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherConstants;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;

public class CuDisbursementVoucherExtractServiceImpl extends DisbursementVoucherExtractServiceImpl {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DisbursementVoucherExtractServiceImpl.class);
    private String CP = "CP";
    
    @Override
    protected PaymentGroup buildPaymentGroup(DisbursementVoucherDocument document, Batch batch) {
        LOG.debug("buildPaymentGroup() started");

        PaymentGroup pg = new PaymentGroup();
        pg.setBatch(batch);
        pg.setCombineGroups(Boolean.TRUE);
        pg.setCampusAddress(Boolean.FALSE);

        document.refreshReferenceObject(KFSPropertyConstants.DV_PAYEE_DETAIL);
        CuDisbursementVoucherPayeeDetail pd = (CuDisbursementVoucherPayeeDetail) document.getDvPayeeDetail();
        String rc = pd.getDisbVchrPaymentReasonCode();

        // If the payee is an employee, set these flags accordingly
        if ((pd.isVendor() && SpringContext.getBean(VendorService.class).isVendorInstitutionEmployee(pd.getDisbVchrVendorHeaderIdNumberAsInteger()))
                || document.getDvPayeeDetail().isEmployee()) {
            pg.setEmployeeIndicator(Boolean.TRUE);
            pg.setPayeeIdTypeCd(PdpConstants.PayeeIdTypeCodes.EMPLOYEE);
            pg.setTaxablePayment(
                    !/*REFACTORME*/SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(CuDisbursementVoucherDocument.class,
                            DisbursementVoucherConstants.RESEARCH_PAYMENT_REASONS_PARM_NM, rc).evaluationSucceeds()
                        && !parameterService.getParameterValueAsString(CuDisbursementVoucherDocument.class,
                                DisbursementVoucherConstants.PAYMENT_REASON_CODE_RENTAL_PAYMENT_PARM_NM).equals(rc)
                        && !parameterService.getParameterValueAsString(CuDisbursementVoucherDocument.class,
                                DisbursementVoucherConstants.PAYMENT_REASON_CODE_ROYALTIES_PARM_NM).equals(rc));
        } else if (pd.isStudent() || pd.isAlumni()) {
            pg.setPayeeIdTypeCd(PdpConstants.PayeeIdTypeCodes.ENTITY);

            // All payments are taxable except research participant, rental & royalties
            pg.setTaxablePayment(
                    !SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(CuDisbursementVoucherDocument.class,
                            DisbursementVoucherConstants.RESEARCH_PAYMENT_REASONS_PARM_NM, rc).evaluationSucceeds()
                        && !CuDisbursementVoucherConstants.PaymentReasonCodes.RENTAL_PAYMENT.equals(rc)
                        && !CuDisbursementVoucherConstants.PaymentReasonCodes.ROYALTIES.equals(rc));
        } else {

            // These are taxable
            VendorDetail vendDetail = SpringContext.getBean(VendorService.class).getVendorDetail(pd.getDisbVchrVendorHeaderIdNumberAsInteger(), 
                    pd.getDisbVchrVendorDetailAssignedIdNumberAsInteger());
            String vendorOwnerCode = vendDetail.getVendorHeader().getVendorOwnershipCode();
            String vendorOwnerCategoryCode = vendDetail.getVendorHeader().getVendorOwnershipCategoryCode();
            String payReasonCode = pd.getDisbVchrPaymentReasonCode();

            pg.setPayeeIdTypeCd(PdpConstants.PayeeIdTypeCodes.VENDOR_ID);

            // Assume it is not taxable until proven otherwise
            pg.setTaxablePayment(Boolean.FALSE);
            pg.setPayeeOwnerCd(vendorOwnerCode);

            ParameterEvaluator parameterEvaluator1 = /*REFACTORME*/SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(
                    DvToPdpExtractStep.class, PdpParameterConstants.TAXABLE_PAYMENT_REASON_CODES_BY_OWNERSHIP_CODES_PARAMETER_NAME, 
                       PdpParameterConstants.NON_TAXABLE_PAYMENT_REASON_CODES_BY_OWNERSHIP_CODES_PARAMETER_NAME, vendorOwnerCode, payReasonCode);
            ParameterEvaluator parameterEvaluator2 = /*REFACTORME*/SpringContext.getBean(
                    ParameterEvaluatorService.class).getParameterEvaluator(DvToPdpExtractStep.class,
                            PdpParameterConstants.TAXABLE_PAYMENT_REASON_CODES_BY_CORPORATION_OWNERSHIP_TYPE_CATEGORY_PARAMETER_NAME, 
                            PdpParameterConstants.NON_TAXABLE_PAYMENT_REASON_CODES_BY_CORPORATION_OWNERSHIP_TYPE_CATEGORY_PARAMETER_NAME,
                            vendorOwnerCategoryCode, payReasonCode);

            if (parameterEvaluator1.evaluationSucceeds()) {
                pg.setTaxablePayment(Boolean.TRUE);
            } else if (this.parameterService.getParameterValueAsString(DvToPdpExtractStep.class,
                    PdpParameterConstants.CORPORATION_OWNERSHIP_TYPE_PARAMETER_NAME).equals(CP) 
                    && StringUtils.isEmpty(vendorOwnerCategoryCode) 
                    && /*REFACTORME*/SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(DvToPdpExtractStep.class,
                            PdpParameterConstants.TAXABLE_PAYMENT_REASON_CODES_FOR_BLANK_CORPORATION_OWNERSHIP_TYPE_CATEGORIES_PARAMETER_NAME,
                            payReasonCode).evaluationSucceeds()) {
                pg.setTaxablePayment(Boolean.TRUE);
            } else if (this.parameterService.getParameterValueAsString(DvToPdpExtractStep.class,
                    PdpParameterConstants.CORPORATION_OWNERSHIP_TYPE_PARAMETER_NAME).equals(CP)
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

        // It doesn't look like the DV has a way to do immediate processes
        pg.setProcessImmediate(Boolean.FALSE);
        pg.setPymtAttachment(document.isDisbVchrAttachmentCode());
        pg.setPymtSpecialHandling(document.isDisbVchrSpecialHandlingCode());
        pg.setNraPayment(pd.isDisbVchrAlienPaymentCode());

        pg.setBankCode(document.getDisbVchrBankCode());
        pg.setPaymentStatusCode(KFSConstants.PdpConstants.PAYMENT_OPEN_STATUS_CODE);

        if (pg.isPayableByACH()) {
            pg.setDisbursementTypeCode(PdpConstants.DisbursementTypeCodes.ACH);
        } else {
            pg.setDisbursementTypeCode(PdpConstants.DisbursementTypeCodes.CHECK);
        }
        
        return pg;
    }
    
    /**
     * This method builds a payment detail object from the disbursement voucher document provided and links that detail file to the
     * batch and process run date given.
     *
     * @param document The disbursement voucher document to retrieve payment information from to populate the PaymentDetail.
     * @param batch The batch file associated with the payment.
     * @param processRunDate The date of the payment detail invoice.
     * @return A fully populated PaymentDetail instance.
     */
    protected PaymentDetail buildPaymentDetail(DisbursementVoucherDocument document, Batch batch, Date processRunDate) {
        LOG.debug("buildPaymentDetail() started");

        PaymentDetail pd = new PaymentDetail();
        if (StringUtils.isNotEmpty(document.getDocumentHeader().getOrganizationDocumentNumber())) {
            pd.setOrganizationDocNbr(document.getDocumentHeader().getOrganizationDocumentNumber());
        }
        pd.setCustPaymentDocNbr(document.getDocumentNumber());
        pd.setInvoiceDate(new java.sql.Date(processRunDate.getTime()));
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
        for (Iterator iter = document.getSourceAccountingLines().iterator(); iter.hasNext();) {
            SourceAccountingLine sal = (SourceAccountingLine) iter.next();

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
        pnt.setCustomerNoteText(CuDisbursementVoucherConstants.DV_EXTRACT_NOTE_PREFIX_PREPARER + document.getDisbVchrContactPersonName() + " " + document.getDisbVchrContactPhoneNumber());
        pd.addNote(pnt);

        String dvSpecialHandlingPersonName = null;
        String dvSpecialHandlingLine1Address = null;
        String dvSpecialHandlingLine2Address = null;
        String dvSpecialHandlingCity = null;
        String dvSpecialHandlingState = null;
        String dvSpecialHandlingZip = null;

        dvSpecialHandlingPersonName = dvpd.getDisbVchrSpecialHandlingPersonName();
        dvSpecialHandlingLine1Address = dvpd.getDisbVchrSpecialHandlingLine1Addr();
        dvSpecialHandlingLine2Address = dvpd.getDisbVchrSpecialHandlingLine2Addr();
        dvSpecialHandlingCity = dvpd.getDisbVchrSpecialHandlingCityName();
        dvSpecialHandlingState = dvpd.getDisbVchrSpecialHandlingStateCode();
        dvSpecialHandlingZip = dvpd.getDisbVchrSpecialHandlingZipCode();

        if (StringUtils.isNotEmpty(dvSpecialHandlingPersonName)) {
            // Split into different lines of length customerNoteText. That is because DV allows for higher length then PDP.
            // Using chopWord to keep consistent with the rest of this code. Per functional specification not required to split
            // on words. It may be nice for future cleanup to centralize some of this instead of doing it for each field
            String tempCustomerNoteText = "Send Check To: " + dvSpecialHandlingPersonName;
            for (String choppedWord : chopWord(tempCustomerNoteText, DisbursementVoucherConstants.MAX_NOTE_LINE_SIZE)) {
 
                // Make sure we're still under the maximum number of note lines. 15 represents the maximum number of lines that may still be
                // added after this point
                if (line < (maxNoteLines - 15) && !StringUtils.isEmpty(choppedWord)) {
                    pnt = new PaymentNoteText();
                    pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
                    pnt.setCustomerNoteText(choppedWord.replaceAll("\\n", "").trim());
 
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Creating special handling person name note, line #" + pnt.getCustomerNoteLineNbr() + ": " + pnt.getCustomerNoteText());
                    }
 
                    pd.addNote(pnt);
                }
                // We can't add any additional note lines, or we'll exceed the maximum, therefore
                // just break out of the loop early - there's nothing left to do.
                else {
                    LOG.warn("Can't add any more special handling person name note.");
                    break;
                }
            }
        }
        if (StringUtils.isNotEmpty(dvSpecialHandlingLine1Address)) {
            pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
            pnt.setCustomerNoteText(CuDisbursementVoucherConstants.DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_ADDRESS1 + dvSpecialHandlingLine1Address);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating special handling address 1 note: "+pnt.getCustomerNoteText());
            }
            pd.addNote(pnt);
        }
        if (StringUtils.isNotEmpty(dvSpecialHandlingLine2Address)) {
            pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
            pnt.setCustomerNoteText(CuDisbursementVoucherConstants.DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_ADDRESS2 + dvSpecialHandlingLine2Address);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating special handling address 2 note: "+pnt.getCustomerNoteText());
            }
            pd.addNote(pnt);
        }
        if (StringUtils.isNotEmpty(dvSpecialHandlingCity)) {
            pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
            pnt.setCustomerNoteText(CuDisbursementVoucherConstants.DV_EXTRACT_NOTE_PREFIX_SPECIAL_HANDLING_ADDRESS3 + dvSpecialHandlingCity + ", " + dvSpecialHandlingState + " " + dvSpecialHandlingZip);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating special handling city note: "+pnt.getCustomerNoteText());
            }
            pd.addNote(pnt);
        }
        if (document.isDisbVchrAttachmentCode()) {
            pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
            pnt.setCustomerNoteText("Attachment Included");
            if (LOG.isDebugEnabled()) {
                LOG.debug("create attachment note: "+pnt.getCustomerNoteText());
            }
            pd.addNote(pnt);
        }

        String paymentReasonCode = dvpd.getDisbVchrPaymentReasonCode();
        if (/*REFACTORME*/SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(DisbursementVoucherDocument.class, DisbursementVoucherConstants.NONEMPLOYEE_TRAVEL_PAY_REASONS_PARM_NM, paymentReasonCode).evaluationSucceeds()) {
            DisbursementVoucherNonEmployeeTravel dvnet = document.getDvNonEmployeeTravel();

            pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
            pnt.setCustomerNoteText("Reimbursement associated with " + dvnet.getDisbVchrServicePerformedDesc());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating non employee travel notes: "+pnt.getCustomerNoteText());
            }
            pd.addNote(pnt);

            pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
            pnt.setCustomerNoteText("The total per diem amount for your daily expenses is " + dvnet.getDisbVchrPerdiemCalculatedAmt());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating non employee travel notes: "+pnt.getCustomerNoteText());
            }
            pd.addNote(pnt);

            if (dvnet.getDisbVchrPersonalCarAmount() != null && dvnet.getDisbVchrPersonalCarAmount().compareTo(KualiDecimal.ZERO) != 0) {
                pnt = new PaymentNoteText();
                pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
                pnt.setCustomerNoteText("The total dollar amount for your vehicle mileage is " + dvnet.getDisbVchrPersonalCarAmount());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Creating non employee travel vehicle note: "+pnt.getCustomerNoteText());
                }
                pd.addNote(pnt);

                for (Iterator iter = dvnet.getDvNonEmployeeExpenses().iterator(); iter.hasNext();) {
                    DisbursementVoucherNonEmployeeExpense exp = (DisbursementVoucherNonEmployeeExpense) iter.next();

                    if (line < (maxNoteLines - 8)) {
                        pnt = new PaymentNoteText();
                        pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
                        pnt.setCustomerNoteText(exp.getDisbVchrExpenseCompanyName() + " " + exp.getDisbVchrExpenseAmount());
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Creating non employee travel expense note: "+pnt.getCustomerNoteText());
                        }
                        pd.addNote(pnt);
                    }
                }
            }
        }
        else if (/*REFACTORME*/SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(DisbursementVoucherDocument.class, DisbursementVoucherConstants.PREPAID_TRAVEL_PAYMENT_REASONS_PARM_NM, paymentReasonCode).evaluationSucceeds()) {
            pnt = new PaymentNoteText();
            pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
            pnt.setCustomerNoteText("Payment is for the following individuals/charges:");
            pd.addNote(pnt);
            if (LOG.isDebugEnabled()) {
                LOG.info("Creating prepaid travel note note: "+pnt.getCustomerNoteText());
            }

            DisbursementVoucherPreConferenceDetail dvpcd = document.getDvPreConferenceDetail();

            for (Iterator iter = dvpcd.getDvPreConferenceRegistrants().iterator(); iter.hasNext();) {
                DisbursementVoucherPreConferenceRegistrant dvpcr = (DisbursementVoucherPreConferenceRegistrant) iter.next();

                if (line < (maxNoteLines - 8)) {
                    pnt = new PaymentNoteText();
                    pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
                    pnt.setCustomerNoteText(dvpcr.getDvConferenceRegistrantName() + " " + dvpcr.getDisbVchrExpenseAmount());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Creating pre-paid conference registrants note: "+pnt.getCustomerNoteText());
                    }
                    pd.addNote(pnt);
                }
            }
        }

        // Get the original, raw form, note text from the DV document.
        String text = document.getDisbVchrCheckStubText();
        if (text != null && text.length() > 0) {

            // The WordUtils should be sufficient for the majority of cases.  This method will
            // word wrap the whole string based on the MAX_NOTE_LINE_SIZE, separating each wrapped
            // word by a newline character.  The 'wrap' method adds line feeds to the end causing
            // the character length to exceed the max length by 1, hence the need for the replace
            // method before splitting.
            String   wrappedText = WordUtils.wrap(text, DisbursementVoucherConstants.MAX_NOTE_LINE_SIZE);
            String[] noteLines   = wrappedText.replaceAll("[\r]", "").split("\\n");

            // Loop through all the note lines.
            for (String noteLine : noteLines) {
                if (line < (maxNoteLines - 3) && !StringUtils.isEmpty(noteLine)) {

                    // This should only happen if we encounter a word that is greater than the max length.
                    // The only concern I have for this occurring is with URLs/email addresses.
                    if (noteLine.length() > DisbursementVoucherConstants.MAX_NOTE_LINE_SIZE) {
                        for (String choppedWord : chopWord(noteLine, DisbursementVoucherConstants.MAX_NOTE_LINE_SIZE)) {

                            // Make sure we're still under the maximum number of note lines.
                            if (line < (maxNoteLines - 3) && !StringUtils.isEmpty(choppedWord)) {
                                pnt = new PaymentNoteText();
                                pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
                                pnt.setCustomerNoteText(choppedWord.replaceAll("\\n", "").trim());
                            }
                            // We can't add any additional note lines, or we'll exceed the maximum, therefore
                            // just break out of the loop early - there's nothing left to do.
                            else {
                                break;
                            }
                        }
                    }
                    // This should be the most common case.  Simply create a new PaymentNoteText,
                    // add the line at the correct line location.
                    else {
                        pnt = new PaymentNoteText();
                        pnt.setCustomerNoteLineNbr(new KualiInteger(line++));
                        //  We need to add the :: in front of each line that is generated in order to determine which notes being
                        //   generated are those typed by the user in the eDoc as we can only move 3 lines to the remittance lines
                        //   on the check.  These codes will help us identify the user's notes and as such ensure that they will
                        //   make it to their expected destination on the remittance stub.
                        pnt.setCustomerNoteText(CuDisbursementVoucherConstants.DV_EXTRACT_TYPED_NOTE_PREFIX_IDENTIFIER + noteLine.replaceAll("\\n", "").trim());
                    }

                    // Logging...
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Creating check stub text note: " + pnt.getCustomerNoteText());
                    }
                    pd.addNote(pnt);
                }
            }
        }

        return pd;
    }

}
