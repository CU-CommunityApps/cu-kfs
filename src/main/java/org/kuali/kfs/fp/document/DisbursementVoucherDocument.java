/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.fp.document;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.service.ObjectTypeService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.parameter.ParameterEvaluator;
import org.kuali.kfs.core.api.parameter.ParameterEvaluatorService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.FPKeyConstants;
import org.kuali.kfs.fp.FPParameterConstants;
import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeTravel;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonresidentTax;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPreConferenceDetail;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPreConferenceRegistrant;
import org.kuali.kfs.fp.businessobject.PaymentReasonCode;
import org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService;
import org.kuali.kfs.fp.document.service.DisbursementVoucherPaymentReasonService;
import org.kuali.kfs.fp.document.service.DisbursementVoucherTaxService;
import org.kuali.kfs.integration.ar.AccountsReceivableCustomer;
import org.kuali.kfs.integration.ar.AccountsReceivableCustomerAddress;
import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kew.engine.node.RouteNodeInstance;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.identity.IdentityService;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.identity.address.EntityAddress;
import org.kuali.kfs.kim.impl.identity.entity.Entity;
import org.kuali.kfs.kim.impl.identity.type.EntityTypeContactInfo;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.document.Copyable;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSConstants.AdHocPaymentIndicator;
import org.kuali.kfs.sys.batch.service.PaymentSourceExtractionService;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.businessobject.PaymentDocumentationLocation;
import org.kuali.kfs.sys.businessobject.PaymentSourceWireTransfer;
import org.kuali.kfs.sys.businessobject.WireCharge;
import org.kuali.kfs.sys.businessobject.options.PaymentDocumentationLocationValuesFinder;
import org.kuali.kfs.sys.businessobject.options.PaymentMethodValuesFinder;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentBase;
import org.kuali.kfs.sys.document.AmountTotaling;
import org.kuali.kfs.sys.document.PaymentSource;
import org.kuali.kfs.sys.document.service.DebitDeterminerService;
import org.kuali.kfs.sys.document.service.PaymentSourceHelperService;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.kfs.sys.service.FlexibleOffsetAccountService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.vnd.service.PhoneNumberService;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is the business object that represents the DisbursementVoucher document in Kuali.
 */
/*
 * CU Customization: Backported this file from the 2021-05-06 financials patch to bring in the FINP-7506 changes.
 * This overlay should be removed when we upgrade to the 2021-05-06 financials patch.
 */
public class DisbursementVoucherDocument extends AccountingDocumentBase implements Copyable, AmountTotaling,
        PaymentSource {

    protected static final String PAYEE_IS_PURCHASE_ORDER_VENDOR_SPLIT = "PayeeIsPurchaseOrderVendor";
    protected static final String PURCHASE_ORDER_VENDOR_TYPE = "PO";
    protected static final String DOCUMENT_REQUIRES_TAX_REVIEW_SPLIT = "RequiresTaxReview";
    protected static final String DOCUMENT_REQUIRES_TRAVEL_REVIEW_SPLIT = "RequiresTravelReview";
    protected static final String DOCUMENT_REQUIRES_SEPARATION_OF_DUTIES = "RequiresSeparationOfDutiesReview";
    protected static final String DISBURSEMENT_VOUCHER_TYPE = "DisbursementVoucher";

    protected static final String PAYMENT_REASONS_REQUIRING_TAX_REVIEW_PARAMETER_NAME = "PAYMENT_REASONS_REQUIRING_TAX_REVIEW";

    protected static final String TAX_CONTROL_BACKUP_HOLDING = "B";
    protected static final String TAX_CONTROL_HOLD_PAYMENTS = "H";

    protected static transient PersonService personService;
    protected static transient ParameterService parameterService;
    protected static transient VendorService vendorService;
    protected static transient BusinessObjectService businessObjectService;
    protected static transient DateTimeService dateTimeService;
    protected static transient DisbursementVoucherPayeeService disbursementVoucherPayeeService;
    protected static transient DisbursementVoucherPaymentReasonService disbursementVoucherPaymentReasonService;
    protected static transient DisbursementVoucherTaxService disbursementVoucherTaxService;
    protected static transient IdentityService identityService;
    protected static transient PaymentSourceExtractionService paymentSourceExtractionService;
    protected static transient volatile PaymentSourceHelperService paymentSourceHelperService;

    private static final Logger LOG = LogManager.getLogger();

    protected Integer finDocNextRegistrantLineNbr;
    protected String disbVchrContactPersonName;
    protected String disbVchrContactPhoneNumber;
    protected String disbVchrContactEmailId;
    protected Date disbursementVoucherDueDate;
    protected boolean disbVchrAttachmentCode;
    protected boolean disbVchrSpecialHandlingCode;
    protected KualiDecimal disbVchrCheckTotalAmount;
    protected boolean disbVchrForeignCurrencyInd;
    protected String disbursementVoucherDocumentationLocationCode;
    protected String disbVchrCheckStubText;
    protected boolean dvCheckStubOverflowCode;
    protected String campusCode;
    protected String disbVchrPayeeTaxControlCode;
    protected boolean disbVchrPayeeChangedInd;
    protected String disbursementVoucherCheckNbr;
    protected Timestamp disbursementVoucherCheckDate;
    protected boolean disbVchrPayeeW9CompleteCode;
    protected String disbVchrPaymentMethodCode;
    protected boolean exceptionIndicator;
    protected boolean disbExcptAttachedIndicator;
    protected Date extractDate;
    protected Date paidDate;
    protected Date cancelDate;
    protected String disbVchrBankCode;
    protected String disbVchrPdpBankCode;
    protected Date invoiceDate;
    protected String invoiceNumber;

    protected boolean payeeAssigned = false;
    protected boolean editW9W8BENbox = false;
    protected boolean immediatePaymentIndicator = false;
    protected boolean achSignUpStatusFlag;

    protected DocumentHeader financialDocument;
    protected PaymentDocumentationLocation disbVchrDocumentationLoc;
    protected DisbursementVoucherNonEmployeeTravel dvNonEmployeeTravel;
    protected DisbursementVoucherNonresidentTax dvNonresidentTax;
    protected DisbursementVoucherPayeeDetail dvPayeeDetail;
    protected DisbursementVoucherPreConferenceDetail dvPreConferenceDetail;
    protected PaymentSourceWireTransfer wireTransfer;

    protected Bank bank;

    public DisbursementVoucherDocument() {
        super();
        exceptionIndicator = false;
        finDocNextRegistrantLineNbr = 1;
        dvNonEmployeeTravel = new DisbursementVoucherNonEmployeeTravel();
        dvNonresidentTax = new DisbursementVoucherNonresidentTax();
        dvPayeeDetail = new DisbursementVoucherPayeeDetail();
        dvPreConferenceDetail = new DisbursementVoucherPreConferenceDetail();
        wireTransfer = new PaymentSourceWireTransfer();
        disbVchrCheckTotalAmount = KualiDecimal.ZERO;
        bank = new Bank();
    }

    @Override
    public List<GeneralLedgerPendingEntry> getPendingLedgerEntriesForSufficientFundsChecking() {
        List<GeneralLedgerPendingEntry> ples = new ArrayList<>();

        FlexibleOffsetAccountService flexibleOffsetAccountService = SpringContext.getBean(FlexibleOffsetAccountService.class);

        ObjectTypeService objectTypeService = SpringContext.getBean(ObjectTypeService.class);

        for (GeneralLedgerPendingEntry ple : this.getGeneralLedgerPendingEntries()) {
            List<String> expenseObjectTypes = objectTypeService.getExpenseObjectTypes(ple.getUniversityFiscalYear());
            if (expenseObjectTypes.contains(ple.getFinancialObjectTypeCode())) {
                // is an expense object type, keep checking
                ple.refreshNonUpdateableReferences();
                if (ple.getAccount().isPendingAcctSufficientFundsIndicator() && ple.getAccount()
                    .getAccountSufficientFundsCode().equals(KFSConstants.SF_TYPE_CASH_AT_ACCOUNT)) {
                    // is a cash account
                    if (flexibleOffsetAccountService
                        .getByPrimaryIdIfEnabled(ple.getChartOfAccountsCode(), ple.getAccountNumber(),
                            ple.getChart().getFinancialCashObjectCode()) == null && flexibleOffsetAccountService
                        .getByPrimaryIdIfEnabled(ple.getChartOfAccountsCode(), ple.getAccountNumber(),
                            ple.getChart().getFinAccountsPayableObjectCode()) == null) {
                        // does not have a flexible offset for cash or liability, set the object code to cash and add
                        // to list of PLEs to check for SF

                        GeneralLedgerPendingEntry newPle = new GeneralLedgerPendingEntry(ple);
                        newPle.setFinancialObjectCode(newPle.getChart().getFinancialCashObjectCode());
                        newPle.setTransactionDebitCreditCode(newPle.getTransactionDebitCreditCode().equals(
                            KFSConstants.GL_DEBIT_CODE) ? KFSConstants.GL_CREDIT_CODE : KFSConstants.GL_DEBIT_CODE);
                        ples.add(newPle);
                    }
                } else {
                    // is not a cash account, process as normal
                    ples.add(ple);
                }
            }
        }

        return ples;
    }

    public Integer getFinDocNextRegistrantLineNbr() {
        return finDocNextRegistrantLineNbr;
    }

    public void setFinDocNextRegistrantLineNbr(Integer finDocNextRegistrantLineNbr) {
        this.finDocNextRegistrantLineNbr = finDocNextRegistrantLineNbr;
    }

    public String getDisbVchrContactPersonName() {
        return disbVchrContactPersonName;
    }

    public void setDisbVchrContactPersonName(String disbVchrContactPersonName) {
        this.disbVchrContactPersonName = disbVchrContactPersonName;
    }

    public String getDisbVchrContactPhoneNumber() {
        return disbVchrContactPhoneNumber;
    }

    public void setDisbVchrContactPhoneNumber(String disbVchrContactPhoneNumber) {
        this.disbVchrContactPhoneNumber = disbVchrContactPhoneNumber;
    }

    public String getDisbVchrContactEmailId() {
        return disbVchrContactEmailId;
    }

    public void setDisbVchrContactEmailId(String disbVchrContactEmailId) {
        this.disbVchrContactEmailId = disbVchrContactEmailId;
    }

    public Date getDisbursementVoucherDueDate() {
        return disbursementVoucherDueDate;
    }

    public void setDisbursementVoucherDueDate(Date disbursementVoucherDueDate) {
        this.disbursementVoucherDueDate = disbursementVoucherDueDate;
    }

    public boolean isDisbVchrAttachmentCode() {
        return disbVchrAttachmentCode;
    }

    @Override
    public boolean hasAttachment() {
        return isDisbVchrAttachmentCode();
    }

    public void setDisbVchrAttachmentCode(boolean disbVchrAttachmentCode) {
        this.disbVchrAttachmentCode = disbVchrAttachmentCode;
    }

    public boolean isDisbVchrSpecialHandlingCode() {
        return disbVchrSpecialHandlingCode;
    }

    public void setDisbVchrSpecialHandlingCode(boolean disbVchrSpecialHandlingCode) {
        this.disbVchrSpecialHandlingCode = disbVchrSpecialHandlingCode;
    }

    public KualiDecimal getDisbVchrCheckTotalAmount() {
        return disbVchrCheckTotalAmount;
    }

    public void setDisbVchrCheckTotalAmount(KualiDecimal disbVchrCheckTotalAmount) {
        if (disbVchrCheckTotalAmount != null) {
            this.disbVchrCheckTotalAmount = disbVchrCheckTotalAmount;
        }
    }

    public boolean isDisbVchrForeignCurrencyInd() {
        return disbVchrForeignCurrencyInd;
    }

    public void setDisbVchrForeignCurrencyInd(boolean disbVchrForeignCurrencyInd) {
        this.disbVchrForeignCurrencyInd = disbVchrForeignCurrencyInd;
    }

    public String getDisbursementVoucherDocumentationLocationCode() {
        return disbursementVoucherDocumentationLocationCode;
    }

    public void setDisbursementVoucherDocumentationLocationCode(String disbursementVoucherDocumentationLocationCode) {
        this.disbursementVoucherDocumentationLocationCode = disbursementVoucherDocumentationLocationCode;
    }

    public String getDisbVchrCheckStubText() {
        return disbVchrCheckStubText;
    }

    public void setDisbVchrCheckStubText(String disbVchrCheckStubText) {
        this.disbVchrCheckStubText = disbVchrCheckStubText;
    }

    public boolean getDvCheckStubOverflowCode() {
        return dvCheckStubOverflowCode;
    }

    public void setDvCheckStubOverflowCode(boolean dvCheckStubOverflowCode) {
        this.dvCheckStubOverflowCode = dvCheckStubOverflowCode;
    }

    @Override
    public String getCampusCode() {
        return campusCode;
    }

    public void setCampusCode(String campusCode) {
        this.campusCode = campusCode;
    }

    public String getDisbVchrPayeeTaxControlCode() {
        return disbVchrPayeeTaxControlCode;
    }

    public void setDisbVchrPayeeTaxControlCode(String disbVchrPayeeTaxControlCode) {
        this.disbVchrPayeeTaxControlCode = disbVchrPayeeTaxControlCode;
    }

    public boolean isDisbVchrPayeeChangedInd() {
        return disbVchrPayeeChangedInd;
    }

    public void setDisbVchrPayeeChangedInd(boolean disbVchrPayeeChangedInd) {
        this.disbVchrPayeeChangedInd = disbVchrPayeeChangedInd;
    }

    public String getDisbursementVoucherCheckNbr() {
        return disbursementVoucherCheckNbr;
    }

    public void setDisbursementVoucherCheckNbr(String disbursementVoucherCheckNbr) {
        this.disbursementVoucherCheckNbr = disbursementVoucherCheckNbr;
    }

    public Timestamp getDisbursementVoucherCheckDate() {
        return disbursementVoucherCheckDate;
    }

    public void setDisbursementVoucherCheckDate(Timestamp disbursementVoucherCheckDate) {
        this.disbursementVoucherCheckDate = disbursementVoucherCheckDate;
    }

    public boolean getDisbVchrPayeeW9CompleteCode() {
        return disbVchrPayeeW9CompleteCode;
    }

    public void setDisbVchrPayeeW9CompleteCode(boolean disbVchrPayeeW9CompleteCode) {
        this.disbVchrPayeeW9CompleteCode = disbVchrPayeeW9CompleteCode;
    }

    public String getDisbVchrPaymentMethodCode() {
        return disbVchrPaymentMethodCode;
    }

    @Override
    public String getPaymentMethodCode() {
        return getDisbVchrPaymentMethodCode();
    }

    public void setDisbVchrPaymentMethodCode(String disbVchrPaymentMethodCode) {
        this.disbVchrPaymentMethodCode = disbVchrPaymentMethodCode;
    }

    public DocumentHeader getFinancialDocument() {
        return financialDocument;
    }

    @Deprecated
    public void setFinancialDocument(DocumentHeader financialDocument) {
        this.financialDocument = financialDocument;
    }

    public PaymentDocumentationLocation getDisbVchrDocumentationLoc() {
        return disbVchrDocumentationLoc;
    }

    @Deprecated
    public void setDisbVchrDocumentationLoc(PaymentDocumentationLocation disbVchrDocumentationLoc) {
        this.disbVchrDocumentationLoc = disbVchrDocumentationLoc;
    }

    public DisbursementVoucherNonEmployeeTravel getDvNonEmployeeTravel() {
        return dvNonEmployeeTravel;
    }

    public void setDvNonEmployeeTravel(DisbursementVoucherNonEmployeeTravel dvNonEmployeeTravel) {
        this.dvNonEmployeeTravel = dvNonEmployeeTravel;
    }

    public DisbursementVoucherNonresidentTax getDvNonresidentTax() {
        return dvNonresidentTax;
    }

    public void setDvNonresidentTax(DisbursementVoucherNonresidentTax dvNonresidentTax) {
        this.dvNonresidentTax = dvNonresidentTax;
    }

    public DisbursementVoucherPayeeDetail getDvPayeeDetail() {
        return dvPayeeDetail;
    }

    public void setDvPayeeDetail(DisbursementVoucherPayeeDetail dvPayeeDetail) {
        this.dvPayeeDetail = dvPayeeDetail;
    }

    public DisbursementVoucherPreConferenceDetail getDvPreConferenceDetail() {
        return dvPreConferenceDetail;
    }

    public void setDvPreConferenceDetail(DisbursementVoucherPreConferenceDetail dvPreConferenceDetail) {
        this.dvPreConferenceDetail = dvPreConferenceDetail;
    }

    @Override
    public PaymentSourceWireTransfer getWireTransfer() {
        return wireTransfer;
    }

    public void setWireTransfer(PaymentSourceWireTransfer dvWireTransfer) {
        this.wireTransfer = dvWireTransfer;
    }

    public boolean isExceptionIndicator() {
        return exceptionIndicator;
    }

    public void setExceptionIndicator(boolean exceptionIndicator) {
        this.exceptionIndicator = exceptionIndicator;
    }

    public Date getCancelDate() {
        return cancelDate;
    }

    public void setCancelDate(Date cancelDate) {
        this.cancelDate = cancelDate;
    }

    public Date getExtractDate() {
        return extractDate;
    }

    public void setExtractDate(Date extractDate) {
        this.extractDate = extractDate;
    }

    public Date getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(Date paidDate) {
        this.paidDate = paidDate;
    }

    /**
     * Based on which pdp dates are present (extract, paid, canceled), determines a String for the status
     *
     * @return a String representation of the status
     */
    public String getDisbursementVoucherPdpStatus() {
        if (cancelDate != null) {
            return "Canceled";
        } else if (paidDate != null) {
            return "Paid";
        } else if (extractDate != null) {
            return "Extracted";
        } else {
            return "Pre-Extraction";
        }
    }

    /**
     * Pretends to set the PDP status for this document
     *
     * @param status the status to pretend to set
     */
    public void setDisbursementVoucherPdpStatus(String status) {
        // don't do nothing, 'cause this ain't a real field
    }

    /**
     * Adds a dv pre-paid registrant line
     *
     * @param line
     */
    public void addDvPrePaidRegistrantLine(DisbursementVoucherPreConferenceRegistrant line) {
        line.setFinancialDocumentLineNumber(getFinDocNextRegistrantLineNbr());
        this.getDvPreConferenceDetail().getDvPreConferenceRegistrants().add(line);
        this.finDocNextRegistrantLineNbr = getFinDocNextRegistrantLineNbr() + 1;
    }

    /**
     * @return the name associated with the payment method code
     */
    public String getDisbVchrPaymentMethodName() {
        return new PaymentMethodValuesFinder().getKeyLabel(disbVchrPaymentMethodCode);
    }

    /**
     * @deprecated This method should not be used. There is no protected attribute to store this value. The associated
     * getter retrieves the value remotely.
     */
    @Deprecated
    public void setDisbVchrPaymentMethodName(String method) {
    }

    /**
     * @return the name associated with the documentation location name
     */
    public String getDisbursementVoucherDocumentationLocationName() {
        return getDataDictionaryService()
                .getDDBean(PaymentDocumentationLocationValuesFinder.class, "paymentDocumentationLocationValuesFinder")
                .getKeyLabel(disbursementVoucherDocumentationLocationCode);
    }

    /**
     * @deprecated This method should not be used. There is no protected attribute to store this value. The associated
     * getter retrieves the value remotely.
     */
    @Deprecated
    public void setDisbursementVoucherDocumentationLocationName(String name) {
    }

    public String getDisbVchrBankCode() {
        return disbVchrBankCode;
    }

    public void setDisbVchrBankCode(String disbVchrBankCode) {
        this.disbVchrBankCode = disbVchrBankCode;
    }

    @Override
    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    /**
     * Convenience method to set dv payee detail fields based on a given vendor.
     *
     * @param vendor
     */
    public void templateVendor(VendorDetail vendor, VendorAddress vendorAddress) {
        if (vendor == null) {
            return;
        }

        this.getDvPayeeDetail().setDisbursementVoucherPayeeTypeCode(KFSConstants.PaymentPayeeTypes.VENDOR);
        this.getDvPayeeDetail().setDisbVchrPayeeIdNumber(vendor.getVendorNumber());
        this.getDvPayeeDetail().setDisbVchrPayeePersonName(vendor.getVendorName());

        this.getDvPayeeDetail().setDisbVchrNonresidentPaymentCode(vendor.getVendorHeader().getVendorForeignIndicator());

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

        this.getDvPayeeDetail().setDisbVchrNonresidentPaymentCode(vendor.getVendorHeader().getVendorForeignIndicator());
        this.getDvPayeeDetail().setDvPayeeSubjectPaymentCode(
            VendorConstants.VendorTypes.SUBJECT_PAYMENT.equals(vendor.getVendorHeader().getVendorTypeCode()));
        this.getDvPayeeDetail().setDisbVchrEmployeePaidOutsidePayrollCode(
            getVendorService().isVendorInstitutionEmployee(vendor.getVendorHeaderGeneratedIdentifier()));

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
        if ((vendorFederalWithholdingTaxBeginDate != null && vendorFederalWithholdingTaxBeginDate
            .before(today)) && (vendorFederalWithholdingTaxEndDate == null || vendorFederalWithholdingTaxEndDate
            .after(today))) {
            this.disbVchrPayeeTaxControlCode = DisbursementVoucherConstants.TAX_CONTROL_CODE_BEGIN_WITHHOLDING;
        }

        // if vendor is foreign, default nonresident payment code to true
        if (getVendorService().isVendorForeign(vendor.getVendorHeaderGeneratedIdentifier())) {
            getDvPayeeDetail().setDisbVchrNonresidentPaymentCode(true);
        }
    }

    /**
     * Convenience method to set dv payee detail fields based on a given Employee.
     *
     * @param employee
     */
    public void templateEmployee(Person employee) {
        if (employee == null) {
            return;
        }

        this.getDvPayeeDetail().setDisbursementVoucherPayeeTypeCode(KFSConstants.PaymentPayeeTypes.EMPLOYEE);
        this.getDvPayeeDetail().setDisbVchrPayeeIdNumber(employee.getEmployeeId());
        this.getDvPayeeDetail().setDisbVchrPayeePersonName(employee.getName());

        final ParameterService parameterService = this.getParameterService();

        if (parameterService.parameterExists(DisbursementVoucherDocument.class,
            FPParameterConstants.USE_DEFAULT_EMPLOYEE_ADDRESS) && parameterService
            .getParameterValueAsBoolean(DisbursementVoucherDocument.class,
                FPParameterConstants.USE_DEFAULT_EMPLOYEE_ADDRESS)) {
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
                this.getDvPayeeDetail().setDisbVchrNonresidentPaymentCode(false);
            }
        }
        // Determine if employee is a research subject
        ParameterEvaluator researchPaymentReasonCodeEvaluator = /*REFACTORME*/SpringContext
            .getBean(ParameterEvaluatorService.class).getParameterEvaluator(DisbursementVoucherDocument.class,
                FPParameterConstants.RESEARCH_PAYMENT_REASONS,
                this.getDvPayeeDetail().getDisbVchrPaymentReasonCode());
        if (researchPaymentReasonCodeEvaluator.evaluationSucceeds()) {
            if (getParameterService().parameterExists(DisbursementVoucherDocument.class,
                FPParameterConstants.RESEARCH_NON_VENDOR_PAY_LIMIT_AMOUNT)) {
                String researchPayLimit = getParameterService()
                    .getParameterValueAsString(DisbursementVoucherDocument.class,
                        FPParameterConstants.RESEARCH_NON_VENDOR_PAY_LIMIT_AMOUNT);
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
     * Convenience method to set dv payee detail fields based on a given customer
     *
     * @param customer        customer to use as payee
     * @param customerAddress customer address to use for payee address
     */
    public void templateCustomer(AccountsReceivableCustomer customer, AccountsReceivableCustomerAddress customerAddress) {
        if (customer == null) {
            return;
        }

        this.getDvPayeeDetail().setDisbursementVoucherPayeeTypeCode(KFSConstants.PaymentPayeeTypes.CUSTOMER);
        this.getDvPayeeDetail().setDisbVchrPayeeIdNumber(customer.getCustomerNumber());
        this.getDvPayeeDetail().setDisbVchrPayeePersonName(customer.getCustomerName());
        this.getDvPayeeDetail().setDisbVchrNonresidentPaymentCode(false);

        if (ObjectUtils.isNotNull(customerAddress) && ObjectUtils.isNotNull(customerAddress.getCustomerAddressIdentifier())) {
            this.getDvPayeeDetail().setDisbVchrVendorAddressIdNumber(customerAddress.getCustomerAddressIdentifier().toString());
            this.getDvPayeeDetail().setDisbVchrPayeeLine1Addr(customerAddress.getCustomerLine1StreetAddress());
            this.getDvPayeeDetail().setDisbVchrPayeeLine2Addr(customerAddress.getCustomerLine2StreetAddress());
            this.getDvPayeeDetail().setDisbVchrPayeeCityName(customerAddress.getCustomerCityName());
            this.getDvPayeeDetail().setDisbVchrPayeeStateCode(customerAddress.getCustomerStateCode());
            this.getDvPayeeDetail().setDisbVchrPayeeZipCode(customerAddress.getCustomerZipCode());
            this.getDvPayeeDetail().setDisbVchrPayeeCountryCode(customerAddress.getCustomerCountryCode());
        } else {
            this.getDvPayeeDetail().setDisbVchrVendorAddressIdNumber("");
            this.getDvPayeeDetail().setDisbVchrPayeeLine1Addr("");
            this.getDvPayeeDetail().setDisbVchrPayeeLine2Addr("");
            this.getDvPayeeDetail().setDisbVchrPayeeCityName("");
            this.getDvPayeeDetail().setDisbVchrPayeeStateCode("");
            this.getDvPayeeDetail().setDisbVchrPayeeZipCode("");
            this.getDvPayeeDetail().setDisbVchrPayeeCountryCode("");
        }
    }

    /**
     * Finds the address for the given employee, matching the type in the
     * KFS-FP / Disbursement Voucher/ DEFAULT_EMPLOYEE_ADDRESS_TYPE parameter, to use as the address for the employee
     *
     * @param employee the employee to find a non-default address for
     * @return the non-default address, or null if not found
     */
    protected EntityAddress getNonDefaultAddress(Person employee) {
        final String addressType = getParameterService().getParameterValueAsString(DisbursementVoucherDocument.class,
            FPParameterConstants.DEFAULT_EMPLOYEE_ADDRESS_TYPE);
        final Entity entity = getIdentityService().getEntityByPrincipalId(employee.getPrincipalId());
        if (entity != null) {
            final EntityTypeContactInfo entityEntityType = getPersonEntityEntityType(entity);
            if (entityEntityType != null) {
                final List<EntityAddress> addresses = entityEntityType.getAddresses();

                return findAddressByType(addresses, addressType);
            }
        }
        return null;
    }

    /**
     * Lazy loop through the entity entity types in the given KimEntityInfo and return the one who has the type of
     * "PERSON"
     *
     * @param entity the entity info to loop through entity entity types of
     * @return a found entity entity type or null if a PERSON entity entity type is not associated with the given
     *         KimEntityInfo record
     */
    protected EntityTypeContactInfo getPersonEntityEntityType(Entity entity) {
        final List<EntityTypeContactInfo> entityEntityTypes = entity.getEntityTypeContactInfos();
        int count = 0;
        EntityTypeContactInfo foundInfo = null;

        while (count < entityEntityTypes.size() && foundInfo == null) {
            if (entityEntityTypes.get(count).getEntityTypeCode().equals(KimConstants.EntityTypes.PERSON)) {
                foundInfo = entityEntityTypes.get(count);
            }
            count += 1;
        }

        return foundInfo;
    }

    /**
     * Given a List of KimEntityAddress and an address type, finds the address in the List with the given type (or null
     * if no matching KimEntityAddress is found)
     *
     * @param addresses   the List of KimEntityAddress records to search
     * @param addressType the address type of the address to return
     * @return the found KimEntityAddress, or null if not found
     */
    protected EntityAddress findAddressByType(List<EntityAddress> addresses, String addressType) {
        EntityAddress foundAddress = null;
        int count = 0;

        while (count < addresses.size() && foundAddress == null) {
            final EntityAddress currentAddress = addresses.get(count);
            if (currentAddress.isActive() && currentAddress.getAddressType().getCode().equals(addressType)) {
                foundAddress = currentAddress;
            }
            count += 1;
        }

        return foundAddress;
    }

    @Override
    public void prepareForSave() {
        if (getFinancialSystemDocumentHeader().getFinancialDocumentStatusCode()
            .equals(KFSConstants.DocumentStatusCodes.ENROUTE)) {
            if (getParameterService().parameterExists(KfsParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class,
                UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_PARAMETER_NAME)
                && getParameterService()
                .getParameterValueAsBoolean(KfsParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class,
                    UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_PARAMETER_NAME)) {
                getFinancialSystemDocumentHeader()
                    .setFinancialDocumentTotalAmount(this.getTotalDollarAmount());
            }
        } else {
            getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(this.getTotalDollarAmount());
        }
        captureWorkflowHeaderInformation();

        if (wireTransfer != null) {
            wireTransfer.setDocumentNumber(this.documentNumber);
        }

        if (dvNonresidentTax != null) {
            dvNonresidentTax.setDocumentNumber(this.documentNumber);
        }

        dvPayeeDetail.setDocumentNumber(this.documentNumber);

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

    /**
     * Determines if the special handling fields should be cleared, based on whether the special handling has been
     * turned off and whether the current node is CAMPUS
     *
     * @return true if special handling should be cleared, false otherwise
     */
    protected boolean shouldClearSpecialHandling() {
        if (!isDisbVchrSpecialHandlingCode()) {
            // are we at the campus route node?
        	Set<String> currentNodes = getDocumentHeader().getWorkflowDocument().getCurrentNodeNames();
            return currentNodes.contains(DisbursementVoucherConstants.RouteLevelNames.CAMPUS);
        }
        return false;
    }

    /**
     * Clears all set special handling fields
     */
    protected void clearSpecialHandling() {
        DisbursementVoucherPayeeDetail payeeDetail = getDvPayeeDetail();

        if (StringUtils.isNotBlank(payeeDetail.getDisbVchrSpecialHandlingPersonName())) {
            payeeDetail.setDisbVchrSpecialHandlingPersonName(null);
        }
        if (StringUtils.isNotBlank(payeeDetail.getDisbVchrSpecialHandlingLine1Addr())) {
            payeeDetail.setDisbVchrSpecialHandlingLine1Addr(null);
        }
        if (StringUtils.isNotBlank(payeeDetail.getDisbVchrSpecialHandlingLine2Addr())) {
            payeeDetail.setDisbVchrSpecialHandlingLine2Addr(null);
        }
        if (StringUtils.isNotBlank(payeeDetail.getDisbVchrSpecialHandlingCityName())) {
            payeeDetail.setDisbVchrSpecialHandlingCityName(null);
        }
        if (StringUtils.isNotBlank(payeeDetail.getDisbVchrSpecialHandlingStateCode())) {
            payeeDetail.setDisbVchrSpecialHandlingStateCode(null);
        }
        if (StringUtils.isNotBlank(payeeDetail.getDisbVchrSpecialHandlingZipCode())) {
            payeeDetail.setDisbVchrSpecialHandlingZipCode(null);
        }
        if (StringUtils.isNotBlank(payeeDetail.getDisbVchrSpecialHandlingCountryCode())) {
            payeeDetail.setDisbVchrSpecialHandlingCountryCode(null);
        }
    }

    /**
     * This method is overridden to populate some local variables that are not persisted to the database. These values need to be
     * computed and saved to the DV Payee Detail BO so they can be serialized to XML for routing. Some of the routing rules rely on
     * these variables.
     */
    @Override
    public void populateDocumentForRouting() {
        DisbursementVoucherPayeeDetail payeeDetail = getDvPayeeDetail();

        if (payeeDetail.isVendor()) {
            payeeDetail.setDisbVchrPayeeEmployeeCode(
                getVendorService().isVendorInstitutionEmployee(payeeDetail.getDisbVchrVendorHeaderIdNumberAsInteger()));
            payeeDetail.setDvPayeeSubjectPaymentCode(
                getVendorService().isSubjectPaymentVendor(payeeDetail.getDisbVchrVendorHeaderIdNumberAsInteger()));
        } else if (payeeDetail.isEmployee()) {

            // Determine if employee is a research subject
            ParameterEvaluator researchPaymentReasonCodeEvaluator = /*REFACTORME*/SpringContext
                .getBean(ParameterEvaluatorService.class).getParameterEvaluator(DisbursementVoucherDocument.class,
                    FPParameterConstants.RESEARCH_PAYMENT_REASONS,
                    payeeDetail.getDisbVchrPaymentReasonCode());
            if (researchPaymentReasonCodeEvaluator.evaluationSucceeds()
                    && getParameterService().parameterExists(DisbursementVoucherDocument.class,
                            FPParameterConstants.RESEARCH_NON_VENDOR_PAY_LIMIT_AMOUNT)) {
                String researchPayLimit = getParameterService()
                        .getParameterValueAsString(DisbursementVoucherDocument.class,
                                FPParameterConstants.RESEARCH_NON_VENDOR_PAY_LIMIT_AMOUNT);
                if (StringUtils.isNotBlank(researchPayLimit)) {
                    KualiDecimal payLimit = new KualiDecimal(researchPayLimit);

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
        super.toCopy();

        clearFieldsThatShouldNotBeCopied();

        initiateDocument();

        getDisbursementVoucherTaxService().clearNonresidentTaxLines(this);
        setDvNonresidentTax(new DisbursementVoucherNonresidentTax());

        getWireTransfer().setWireTransferFeeWaiverIndicator(false);
        clearInvalidPayee();

        // this copied DV has not been extracted
        this.extractDate = null;
        this.paidDate = null;
        this.cancelDate = null;
        getFinancialSystemDocumentHeader().setFinancialDocumentStatusCode(KFSConstants.DocumentStatusCodes.INITIATED);
    }

    /**
     * generic, shared logic used to initiate a dv document
     */
    public void initiateDocument() {
        PhoneNumberService phoneNumberService = SpringContext.getBean(PhoneNumberService.class);
        Person currentUser = GlobalVariables.getUserSession().getPerson();
        setDisbVchrContactPersonName(currentUser.getName());

        if (!phoneNumberService.isDefaultFormatPhoneNumber(currentUser.getPhoneNumber())) {
            setDisbVchrContactPhoneNumber(phoneNumberService.formatNumberIfPossible(currentUser.getPhoneNumber()));
        } else {
            setDisbVchrContactPhoneNumber(currentUser.getPhoneNumber());
        }

        setDisbVchrContactEmailId(currentUser.getEmailAddress());
        ChartOrgHolder chartOrg = SpringContext.getBean(org.kuali.kfs.sys.service.FinancialSystemUserService.class)
            .getPrimaryOrganization(currentUser, KFSConstants.CoreModuleNamespaces.FINANCIAL);

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
            setDisbursementVoucherDocumentationLocationCode(getParameterService()
                .getParameterValueAsString(DisbursementVoucherDocument.class,
                    FPParameterConstants.DEFAULT_DOC_LOCATION));
        }

        // default bank code
        Bank defaultBank = SpringContext.getBean(BankService.class).getDefaultBankByDocType(this.getClass());
        if (defaultBank != null) {
            this.disbVchrBankCode = defaultBank.getBankCode();
            this.bank = defaultBank;
        }
    }

    protected void clearFieldsThatShouldNotBeCopied() {
        setDisbVchrContactPhoneNumber(StringUtils.EMPTY);
        setDisbVchrContactEmailId(StringUtils.EMPTY);
        setDisbVchrPayeeTaxControlCode(StringUtils.EMPTY);
        setInvoiceNumber(StringUtils.EMPTY);
        setInvoiceDate(null);
        setImmediatePaymentIndicator(false);
    }

    protected void clearInvalidPayee() {
        // check vendor id number to see if still valid, if not, clear dvPayeeDetail; otherwise, use the current
        // dvPayeeDetail as is
        if (StringUtils.isNotBlank(getDvPayeeDetail().getDisbVchrPayeeIdNumber())) {
            VendorDetail vendorDetail = getVendorService()
                .getVendorDetail(dvPayeeDetail.getDisbVchrVendorHeaderIdNumberAsInteger(),
                    dvPayeeDetail.getDisbVchrVendorDetailAssignedIdNumberAsInteger());
            if (vendorDetail == null) {
                clearPayee(FPKeyConstants.WARNING_DV_PAYEE_NON_EXISTENT_CLEARED);
            } else {
                DisbursementPayee payee = getDisbursementVoucherPayeeService().getPayeeFromVendor(vendorDetail);
                if (!getDisbursementVoucherPaymentReasonService()
                    .isPayeeQualifiedForPayment(payee, dvPayeeDetail.getDisbVchrPaymentReasonCode())) {
                    clearPayee(FPKeyConstants.MESSAGE_DV_PAYEE_INVALID_PAYMENT_TYPE_CLEARED);
                }
            }
        }
    }

    protected void clearPayee(String messageKey) {
        dvPayeeDetail = new DisbursementVoucherPayeeDetail();
        getDvPayeeDetail().setDisbVchrPayeeIdNumber(StringUtils.EMPTY);
        KNSGlobalVariables.getMessageList().add(messageKey);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List buildListOfDeletionAwareLists() {
        List managedLists = super.buildListOfDeletionAwareLists();

        if (dvNonEmployeeTravel != null) {
            managedLists.add(dvNonEmployeeTravel.getDvNonEmployeeExpenses());
            managedLists.add(dvNonEmployeeTravel.getDvPrePaidEmployeeExpenses());
        }

        if (dvPreConferenceDetail != null) {
            managedLists.add(dvPreConferenceDetail.getDvPreConferenceRegistrants());
        }

        return managedLists;
    }

    @Override
    public KualiDecimal getTotalDollarAmount() {
        return this.getDisbVchrCheckTotalAmount();
    }

    @Override
    public boolean isDebit(GeneralLedgerPendingEntrySourceDetail postable) {
        // disallow error corrections
        DebitDeterminerService isDebitUtils = SpringContext.getBean(DebitDeterminerService.class);
        isDebitUtils.disallowErrorCorrectionDocumentCheck(this);

        if (getDvNonresidentTax() != null && getDvNonresidentTax()
            .getFinancialDocumentAccountingLineText() != null && getDvNonresidentTax()
            .getFinancialDocumentAccountingLineText()
            .contains(((AccountingLine) postable).getSequenceNumber().toString())) {
            return postable.getAmount().isPositive();
        }
        if (SpringContext.getBean(ParameterService.class).getParameterValueAsBoolean(DisbursementVoucherDocument.class,
            FPParameterConstants.NEGATIVE_ACCOUNTING_LINES_IND)) {
            return isDebitUtils.isDebitConsideringNothingPositiveOrNegative(this, postable);

        } else {
            return isDebitUtils.isDebitConsideringNothingPositiveOnly(this, postable);
        }
    }

    /**
     * Override to change the doc type based on payment method. This is needed to pick up different offset definitions.
     *
     * @param accountingLine accounting line in submitted accounting document
     * @param explicitEntry  explicit GLPE
     */
    @Override
    public void customizeExplicitGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySourceDetail accountingLine,
        GeneralLedgerPendingEntry explicitEntry) {

        /* change document type based on payment method to pick up different offsets */
        if (KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK.equals(getDisbVchrPaymentMethodCode())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("changing doc type on pending entry " + explicitEntry
                    .getTransactionLedgerEntrySequenceNumber() + " to " + DisbursementVoucherConstants.DOCUMENT_TYPE_CHECKACH);
            }
            explicitEntry.setFinancialDocumentTypeCode(DisbursementVoucherConstants.DOCUMENT_TYPE_CHECKACH);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("changing doc type on pending entry " + explicitEntry
                    .getTransactionLedgerEntrySequenceNumber() + " to " + DisbursementVoucherConstants.DOCUMENT_TYPE_CHECKACH);
            }
            explicitEntry.setFinancialDocumentTypeCode(DisbursementVoucherConstants.DOCUMENT_TYPE_WTFD);
        }
    }

    /**
     * Return true if GLPE's are generated successfully (i.e. there are either 0 GLPE's or 1 GLPE in disbursement
     * voucher document)
     *
     * @param sequenceHelper    helper class to keep track of GLPE sequence
     * @return true if GLPE's are generated successfully
     */
    @Override
    public boolean generateDocumentGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        if (getGeneralLedgerPendingEntries() == null || getGeneralLedgerPendingEntries().size() < 2) {
            LOG.warn("No gl entries for accounting lines.");
            return true;
        }

        // only generate additional charge entries for payment method wire charge, and if the fee has not been waived
        if (KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE
            .equals(getDisbVchrPaymentMethodCode()) && !getWireTransfer().isWireTransferFeeWaiverIndicator()) {
            LOG.debug("generating wire charge gl pending entries.");

            // retrieve wire charge
            WireCharge wireCharge = getPaymentSourceHelperService().retrieveCurrentYearWireCharge();

            // generate debits
            GeneralLedgerPendingEntry chargeEntry = getPaymentSourceHelperService()
                .processWireChargeDebitEntries(this, sequenceHelper, wireCharge);

            // generate credits
            getPaymentSourceHelperService()
                .processWireChargeCreditEntries(this, sequenceHelper, wireCharge, chargeEntry);
        }

        // for wire or drafts generate bank offset entry (if enabled), for ACH and checks offset will be generated by PDP
        if (KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE
            .equals(getDisbVchrPaymentMethodCode()) || KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_DRAFT
            .equals(getDisbVchrPaymentMethodCode())) {
            getPaymentSourceHelperService().generateDocumentBankOffsetEntries(this, sequenceHelper,
                DisbursementVoucherConstants.DOCUMENT_TYPE_WTFD);
        }

        return true;
    }

    /**
     * Gets the payeeAssigned attribute. This method returns a flag that is used to indicate if the payee type and value
     * has been set on the DV. This value is used to determine the correct page that should be loaded by the DV flow.
     *
     * @return Returns the payeeAssigned.
     */
    public boolean isPayeeAssigned() {
        // If value is false, check state of document. We should assume payee is assigned if document has been saved.
        // Otherwise, value will be set during creation process.
        if (!payeeAssigned) {
            payeeAssigned = !this.getDocumentHeader().getWorkflowDocument().isInitiated();
        }
        return payeeAssigned;
    }

    /**
     * @param payeeAssigned The payeeAssigned to set.
     */
    public void setPayeeAssigned(boolean payeeAssigned) {
        this.payeeAssigned = payeeAssigned;
    }

    /**
     * This method returns a flag that is used to indicate if the W9/W8BEN check box can be edited
     * by the initiator on the DV.
     *
     * @return Returns the editW9W8BENbox.
     */
    public boolean isEditW9W8BENbox() {
        String initiatorPrincipalID = this.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();
        if (GlobalVariables.getUserSession().getPrincipalId().equals(initiatorPrincipalID)) {
            editW9W8BENbox = true;
        }
        return editW9W8BENbox;
    }

    public void setEditW9W8BENbox(boolean editW9W8BENbox) {
        this.editW9W8BENbox = editW9W8BENbox;
    }

    public String getDisbVchrPdpBankCode() {
        return disbVchrPdpBankCode;
    }

    public void setDisbVchrPdpBankCode(String disbVchrPdpBankCode) {
        this.disbVchrPdpBankCode = disbVchrPdpBankCode;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    /**
     * @return whether this document should be paid out immediately from PDP
     */
    public boolean isImmediatePaymentIndicator() {
        return immediatePaymentIndicator;
    }

    /**
     * @param immediatePaymentIndicator {@code true} if this should be immediately disbursed; {@code false} otherwise
     */
    public void setImmediatePaymentIndicator(boolean immediatePaymentIndicator) {
        this.immediatePaymentIndicator = immediatePaymentIndicator;
    }

    public boolean isAchSignUpStatusFlag() {
        return achSignUpStatusFlag;
    }

    public void setAchSignUpStatusFlag(boolean achSignUpStatusFlag) {
        this.achSignUpStatusFlag = achSignUpStatusFlag;
    }

    @Override
    public String getDocumentTitle() {
        String documentTitle = super.getDocumentTitle();
        return this.buildDocumentTitle(documentTitle);
    }

    /**
     * build document title based on the properties of current document
     *
     * @param title the default document title
     * @return the combine information of the given title and additional payment indicators
     */
    protected String buildDocumentTitle(String title) {
        DisbursementVoucherPayeeDetail payee = getDvPayeeDetail();
        if (payee == null) {
            return title;
        }

        Boolean addPaymentReasonToTitle = getParameterService().getParameterValueAsBoolean(this.getClass(),
            FPParameterConstants.ADD_PAYMENT_REASON_TO_DV_TITLE, Boolean.TRUE);
        Boolean addPayeeTaxRevToTitle = getParameterService().getParameterValueAsBoolean(this.getClass(),
            FPParameterConstants.ADD_PAYEE_TAX_REV_TO_DV_TITLE, Boolean.TRUE);
        Boolean addPaymentReasonTaxRevToTitle = getParameterService().getParameterValueAsBoolean(this.getClass(),
            FPParameterConstants.ADD_PAYMENT_REASON_TAX_REV_TO_DV_TITLE, Boolean.TRUE);

        DisbursementVoucherPaymentReasonService paymentReasonService = SpringContext
            .getBean(DisbursementVoucherPaymentReasonService.class);
        if (title != null && title.contains(DisbursementVoucherConstants.DV_DOC_NAME) && addPaymentReasonToTitle) {
            String paymentCodeAndDescription = StringUtils.EMPTY;

            if (StringUtils.isNotBlank(payee.getDisbVchrPaymentReasonCode())) {
                PaymentReasonCode paymentReasonCode = paymentReasonService
                    .getPaymentReasonByPrimaryId(payee.getDisbVchrPaymentReasonCode());

                paymentCodeAndDescription = ObjectUtils.isNotNull(paymentReasonCode) ? paymentReasonCode
                    .getCodeAndDescription() : paymentCodeAndDescription;
            }

            String replaceTitle = DisbursementVoucherConstants.DV_DOC_NAME + " " + paymentCodeAndDescription;
            title = title.replace(DisbursementVoucherConstants.DV_DOC_NAME, replaceTitle);
        }

        List<String> indicatorsArr = new ArrayList<>();
        indicatorsArr.add(payee.isEmployee() ? AdHocPaymentIndicator.EMPLOYEE_PAYEE : AdHocPaymentIndicator.OTHER);
        indicatorsArr.add(payee.isDisbVchrNonresidentPaymentCode() ? AdHocPaymentIndicator.NONRESIDENT_PAYEE : AdHocPaymentIndicator.OTHER);

        if (addPayeeTaxRevToTitle) {
            String taxControlCode = this.getDisbVchrPayeeTaxControlCode();
            if (StringUtils
                .equals(taxControlCode, DisbursementVoucherDocument.TAX_CONTROL_BACKUP_HOLDING) || StringUtils
                .equals(taxControlCode, DisbursementVoucherDocument.TAX_CONTROL_HOLD_PAYMENTS)) {
                indicatorsArr.add(AdHocPaymentIndicator.TAX_CONTROL_REQUIRING_TAX_REVIEW);
            } else {
                indicatorsArr.add(AdHocPaymentIndicator.OTHER);
            }
        }

        if (addPaymentReasonTaxRevToTitle) {
            boolean isTaxReviewRequired = paymentReasonService.isTaxReviewRequired(payee.getDisbVchrPaymentReasonCode());
            indicatorsArr.add(
                isTaxReviewRequired ? AdHocPaymentIndicator.PAYMENT_REASON_REQUIRING_TAX_REVIEW : AdHocPaymentIndicator.OTHER);
        }
        boolean needIndicators = false;
        StringBuilder titleWithIndicators = new StringBuilder();
        titleWithIndicators.append(title);
        titleWithIndicators.append(" [");
        for (String indicator : indicatorsArr) {
            titleWithIndicators.append(String.format("%s:", indicator));
            if (!AdHocPaymentIndicator.OTHER.equals(indicator)) {
                needIndicators = true;
            }
        }
        if (needIndicators) {
            titleWithIndicators.replace(titleWithIndicators.length() - 1, titleWithIndicators.length(), "]");
            return titleWithIndicators.toString();
        }

        return title;
    }

    /**
     * Provides answers to the following splits: PayeeIsPurchaseOrderVendor RequiresTaxReview RequiresTravelReview
     */
    @Override
    public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
        if (nodeName.equals(DisbursementVoucherDocument.PAYEE_IS_PURCHASE_ORDER_VENDOR_SPLIT)) {
            return isPayeePurchaseOrderVendor();
        }
        if (nodeName.equals(DisbursementVoucherDocument.DOCUMENT_REQUIRES_TAX_REVIEW_SPLIT)) {
            return isTaxReviewRequired();
        }
        if (nodeName.equals(DisbursementVoucherDocument.DOCUMENT_REQUIRES_TRAVEL_REVIEW_SPLIT)) {
            return isTravelReviewRequired();
        }
        if (nodeName.equals(DOCUMENT_REQUIRES_SEPARATION_OF_DUTIES)) {
            return isSeparationOfDutiesReviewRequired();
        }
        throw new UnsupportedOperationException("Cannot answer split question for this node you call \"" + nodeName + "\"");
    }

    protected boolean isSeparationOfDutiesReviewRequired() {
        ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        boolean sepOfDutiesRequired = parameterService
            .getParameterValueAsBoolean(KFSConstants.CoreModuleNamespaces.FINANCIAL, DISBURSEMENT_VOUCHER_TYPE,
                FPParameterConstants.SEPARATION_OF_DUTIES);

        if (sepOfDutiesRequired) {
            try {
                List<Person> priorApprovers = new ArrayList<>(getAllPriorApprovers());
                // The payee cannot be the only approver
                String payeeEmployeeId = this.getDvPayeeDetail().getDisbVchrPayeeIdNumber();

                if (priorApprovers.size() == 1 &&
                    priorApprovers.get(0).getEmployeeId().equals(payeeEmployeeId)) {
                    return true;
                }

                // if there are more than 0 prior approvers which means there had been at least another approver than
                // the current approver then no need for separation of duties
                if (priorApprovers.size() > 0) {
                    return false;
                }
            } catch (WorkflowException we) {
                LOG.error("Exception while attempting to retrieve all prior approvers from workflow: " + we);
            }
        }
        return false;
    }

    public Set<Person> getAllPriorApprovers() throws WorkflowException {
        PersonService personService = KimApiServiceLocator.getPersonService();
        List<ActionTaken> actionsTaken = getDocumentHeader().getWorkflowDocument().getActionsTaken();
        Set<String> principalIds = new HashSet<>();
        Set<Person> persons = new HashSet<>();

        for (ActionTaken actionTaken : actionsTaken) {
            if (KewApiConstants.ACTION_TAKEN_APPROVED_CD.equals(actionTaken.getActionTaken())) {
                String principalId = actionTaken.getPrincipalId();
                if (!principalIds.contains(principalId)) {
                    principalIds.add(principalId);
                    persons.add(personService.getPerson(principalId));
                }
            }
        }
        return persons;
    }

    /**
     * @return true if the payee is a purchase order vendor and therefore should receive vendor review, false otherwise
     */
    protected boolean isPayeePurchaseOrderVendor() {
        if (!this.getDvPayeeDetail().getDisbursementVoucherPayeeTypeCode().equals(KFSConstants.PaymentPayeeTypes.VENDOR)) {
            return false;
        }

        VendorDetail vendor = getVendorService().getByVendorNumber(this.getDvPayeeDetail().getDisbVchrPayeeIdNumber());
        if (vendor == null) {
            return false;
        }

        vendor.refreshReferenceObject("vendorHeader");
        return vendor.getVendorHeader().getVendorTypeCode().equals(DisbursementVoucherDocument.PURCHASE_ORDER_VENDOR_TYPE);
    }

    /**
     * Tax review is required under the following circumstances: the payee was an employee the payee was a
     * nonresident vendor the tax control code = "B" or "H" the payment reason code was "D" the payment reason code
     * was "M" and the campus was listed in the CAMPUSES_TAXED_FOR_MOVING_REIMBURSEMENTS_PARAMETER_NAME parameter
     *
     * @return true if any of the above conditions exist and this document should receive tax review, false otherwise
     */
    protected boolean isTaxReviewRequired() {
        if (isPayeePurchaseOrderVendorHasWithholding()) {
            return true;
        }

        String payeeTypeCode = this.getDvPayeeDetail().getDisbursementVoucherPayeeTypeCode();
        if (payeeTypeCode.equals(KFSConstants.PaymentPayeeTypes.EMPLOYEE)) {
            return false;
        } else if (payeeTypeCode.equals(KFSConstants.PaymentPayeeTypes.VENDOR)) {
            if (vendorService.isVendorInstitutionEmployee(this.getDvPayeeDetail().getDisbVchrVendorHeaderIdNumberAsInteger())) {
                return true;
            }
        }

        if (payeeTypeCode.equals(KFSConstants.PaymentPayeeTypes.VENDOR) && this.getVendorService()
            .isVendorForeign(getDvPayeeDetail().getDisbVchrVendorHeaderIdNumberAsInteger())) {
            return true;
        }

        String taxControlCode = this.getDisbVchrPayeeTaxControlCode();
        if (StringUtils.equals(taxControlCode, DisbursementVoucherDocument.TAX_CONTROL_BACKUP_HOLDING) || StringUtils
            .equals(taxControlCode, DisbursementVoucherDocument.TAX_CONTROL_HOLD_PAYMENTS)) {
            return true;
        }

        String paymentReasonCode = this.getDvPayeeDetail().getDisbVchrPaymentReasonCode();
        if (this.getDisbursementVoucherPaymentReasonService().isDecedentCompensationPaymentReason(paymentReasonCode)) {
            return true;
        }

        if (this.getDisbursementVoucherPaymentReasonService()
            .isMovingPaymentReason(paymentReasonCode) && taxedCampusForMovingReimbursements()) {
            return true;
        }

        return SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(this.getClass(),
                DisbursementVoucherDocument.PAYMENT_REASONS_REQUIRING_TAX_REVIEW_PARAMETER_NAME, paymentReasonCode)
                .evaluationSucceeds();
    }

    /**
     * @return true if the payee is a vendor and has withholding dates therefore should receive tax review, false otherwise
     */
    protected boolean isPayeePurchaseOrderVendorHasWithholding() {
        if (!this.getDvPayeeDetail().getDisbursementVoucherPayeeTypeCode().equals(KFSConstants.PaymentPayeeTypes.VENDOR)) {
            return false;
        }

        VendorDetail vendor = getVendorService().getByVendorNumber(this.getDvPayeeDetail().getDisbVchrPayeeIdNumber());
        if (vendor == null) {
            return false;
        }

        vendor.refreshReferenceObject("vendorHeader");
        return vendor.getVendorHeader().getVendorFederalWithholdingTaxBeginningDate() != null
                || vendor.getVendorHeader().getVendorFederalWithholdingTaxEndDate() != null;
    }

    /**
     * Determines if the campus this DV is related to is taxed (and should get tax review routing) for moving reimbursements
     *
     * @return true if the campus is taxed for moving reimbursements, false otherwise
     */
    protected boolean taxedCampusForMovingReimbursements() {
        return /*REFACTORME*/SpringContext.getBean(ParameterEvaluatorService.class)
            .getParameterEvaluator(this.getClass(),
                FPParameterConstants.CAMPUSES_TAXED_FOR_MOVING_REIMBURSEMENTS, this.getCampusCode())
            .evaluationSucceeds();
    }

    /**
     * Travel review is required under the following circumstances: payment reason code is "P" or "N"
     *
     * @return
     */
    public boolean isTravelReviewRequired() {
        String paymentReasonCode = this.getDvPayeeDetail().getDisbVchrPaymentReasonCode();

        return this.getDisbursementVoucherPaymentReasonService().isPrepaidTravelPaymentReason(paymentReasonCode) || this
            .getDisbursementVoucherPaymentReasonService().isNonEmployeeTravelPaymentReason(paymentReasonCode);
    }

    /**
     * Overridden to immediately extract DV, if it has been marked for immediate extract
     */
    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);
        if (getDocumentHeader().getWorkflowDocument().isProcessed()) {
            if (isImmediatePaymentIndicator()) {
                getDisbursementVoucherExtractService().extractSingleImmediatePayment(this);
            }
        }
    }

    public boolean isDisbExcptAttachedIndicator() {
        return disbExcptAttachedIndicator;
    }

    public void setDisbExcptAttachedIndicator(boolean disbExcptAttachedIndicator) {
        this.disbExcptAttachedIndicator = disbExcptAttachedIndicator;
    }

    /**
     * RQ_AP_0760: Ability to view disbursement information on the Disbursement Voucher Document.
     * <p>
     * This method returns the document type of payment detail of the Disbursement Voucher Document. It is invoked when
     * the user clicks on the disbursement info button on the Pre-Disbursement Processor Status tab on Disbursement
     * Voucher Document.
     *
     * @return
     */
    public String getPaymentDetailDocumentType() {
        return DisbursementVoucherConstants.DOCUMENT_TYPE_CHECKACH;
    }

    protected VendorService getVendorService() {
        if (vendorService == null) {
            vendorService = SpringContext.getBean(VendorService.class);
        }
        return vendorService;
    }

    public static void setVendorService(VendorService vendorService) {
        DisbursementVoucherDocument.vendorService = vendorService;
    }

    protected DisbursementVoucherPayeeService getDisbursementVoucherPayeeService() {
        if (disbursementVoucherPayeeService == null) {
            disbursementVoucherPayeeService = SpringContext.getBean(DisbursementVoucherPayeeService.class);
        }
        return disbursementVoucherPayeeService;
    }

    public static void setDisbursementVoucherPayeeService(DisbursementVoucherPayeeService disbursementVoucherPayeeService) {
        DisbursementVoucherDocument.disbursementVoucherPayeeService = disbursementVoucherPayeeService;
    }

    public DisbursementVoucherPaymentReasonService getDisbursementVoucherPaymentReasonService() {
        if (disbursementVoucherPaymentReasonService == null) {
            disbursementVoucherPaymentReasonService = SpringContext.getBean(DisbursementVoucherPaymentReasonService.class);
        }
        return disbursementVoucherPaymentReasonService;
    }

    public static void setDisbursementVoucherPaymentReasonService(DisbursementVoucherPaymentReasonService disbursementVoucherPaymentReasonService) {
        DisbursementVoucherDocument.disbursementVoucherPaymentReasonService = disbursementVoucherPaymentReasonService;
    }

    public DisbursementVoucherTaxService getDisbursementVoucherTaxService() {
        if (disbursementVoucherTaxService == null) {
            disbursementVoucherTaxService = SpringContext.getBean(DisbursementVoucherTaxService.class);
        }
        return disbursementVoucherTaxService;
    }

    public static IdentityService getIdentityService() {
        if (identityService == null) {
            identityService = SpringContext.getBean(IdentityService.class);
        }
        return identityService;
    }

    public static void setIdentityService(IdentityService identityService) {
        DisbursementVoucherDocument.identityService = identityService;
    }

    public static PaymentSourceExtractionService getDisbursementVoucherExtractService() {
        if (paymentSourceExtractionService == null) {
            paymentSourceExtractionService = SpringContext.getBean(PaymentSourceExtractionService.class,
                DisbursementVoucherConstants.DISBURSEMENT_VOUCHER_PAYMENT_SOURCE_EXTRACTION_SERVICE);
        }
        return paymentSourceExtractionService;
    }

    public static PaymentSourceHelperService getPaymentSourceHelperService() {
        if (paymentSourceHelperService == null) {
            paymentSourceHelperService = SpringContext.getBean(PaymentSourceHelperService.class);
        }
        return paymentSourceHelperService;
    }
}
