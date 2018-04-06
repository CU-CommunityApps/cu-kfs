package edu.cornell.kfs.pdp.service.impl;

import static org.kuali.kfs.module.purap.PurapConstants.HUNDRED;
import static org.kuali.kfs.module.purap.PurapConstants.PURAP_ORIGIN_CODE;
import static org.kuali.kfs.sys.KFSConstants.GL_CREDIT_CODE;
import static org.kuali.kfs.sys.KFSConstants.GL_DEBIT_CODE;
import static org.kuali.rice.core.api.util.type.KualiDecimal.ZERO;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.businessobject.OffsetDefinition;
import org.kuali.kfs.coa.service.OffsetDefinitionService;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.businessobject.CreditMemoItem;
import org.kuali.kfs.module.purap.businessobject.ItemType;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PurApItemUseTax;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderAccount;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.service.AccountsPayableDocumentSpecificService;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.module.purap.service.PurapAccountRevisionService;
import org.kuali.kfs.module.purap.service.PurapAccountingService;
import org.kuali.kfs.module.purap.util.SummaryAccount;
import org.kuali.kfs.module.purap.util.UseTaxContainer;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.GlPendingTransaction;
import org.kuali.kfs.pdp.businessobject.PaymentAccountDetail;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.service.impl.PendingTransactionServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentBase;
import org.kuali.kfs.sys.service.FlexibleOffsetAccountService;
import org.kuali.kfs.sys.service.GeneralLedgerPendingEntryService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.api.util.type.KualiInteger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.datadictionary.AttributeSecurity;
import org.kuali.kfs.krad.datadictionary.BusinessObjectEntry;
import org.kuali.kfs.krad.datadictionary.mask.MaskFormatterLiteral;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.batch.CheckReconciliationImportStep;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;

import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pdp.service.CuPendingTransactionService;


@Transactional
public class CuPendingTransactionServiceImpl extends PendingTransactionServiceImpl implements CuPendingTransactionService{
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuPendingTransactionServiceImpl.class);
	
    protected static String FDOC_TYP_CD_STOP_CHECK = "CHKS";
    protected static String FDOC_TYP_CD_STALE_CHECK = "CHKL";
    
    private DocumentService documentService;
    private NoteService noteService;
    private ParameterService parameterService;
    
    /**
     * @see org.kuali.kfs.pdp.service.PendingTransactionService#generateCRCancellationGeneralLedgerPendingEntry(org.kuali.kfs.pdp.businessobject.PaymentGroup)
     */
    public void generateCRCancellationGeneralLedgerPendingEntry(PaymentGroup paymentGroup) {
        GeneralLedgerPendingEntrySequenceHelper sequenceHelper = new GeneralLedgerPendingEntrySequenceHelper();
        for (PaymentDetail paymentDetail : paymentGroup.getPaymentDetails()) {

            // Need to reverse the payment document's GL entries if the check is stopped or cancelled
            reverseSourceDocumentsEntries(paymentDetail, sequenceHelper);
        }
        this.populatePaymentGeneralLedgerPendingEntry(paymentGroup, FDOC_TYP_CD_CANCEL_ACH, FDOC_TYP_CD_CANCEL_CHECK, true);
    }

    /**
     * @see org.kuali.kfs.pdp.service.PendingTransactionService#generateCancellationGeneralLedgerPendingEntry(org.kuali.kfs.pdp.businessobject.PaymentGroup)
     */
    public void generateStopGeneralLedgerPendingEntry(PaymentGroup paymentGroup) {
        GeneralLedgerPendingEntrySequenceHelper sequenceHelper = new GeneralLedgerPendingEntrySequenceHelper();
        for (PaymentDetail paymentDetail : paymentGroup.getPaymentDetails()) {

            // Need to reverse the payment document's GL entries if the check is stopped or cancelled
            reverseSourceDocumentsEntries(paymentDetail, sequenceHelper);
        }
        this.populatePaymentGeneralLedgerPendingEntry(paymentGroup, FDOC_TYP_CD_CANCEL_ACH, FDOC_TYP_CD_STOP_CHECK, true);
    }
    
    
    
    /**
     * @see org.kuali.kfs.pdp.service.PendingTransactionService#generateCancellationGeneralLedgerPendingEntry(org.kuali.kfs.pdp.businessobject.PaymentGroup)
     */
    public void generateStaleGeneralLedgerPendingEntry(PaymentGroup paymentGroup) {
        this.populatePaymentGeneralLedgerPendingEntry(paymentGroup, FDOC_TYP_CD_STALE_CHECK, FDOC_TYP_CD_STALE_CHECK, true);
    }
    
    /**
     * Populates and stores a new GLPE for each account detail in the payment group.
     * 
     * @param paymentGroup payment group to generate entries for
     * @param achFdocTypeCode doc type for ach disbursements
     * @param checkFdocTypeCod doc type for check disbursements
     * @param reversal boolean indicating if this is a reversal
     */
    protected void populatePaymentGeneralLedgerPendingEntry(PaymentGroup paymentGroup, String achFdocTypeCode, String checkFdocTypeCod, boolean reversal) {
        List<PaymentAccountDetail> accountListings = new ArrayList<PaymentAccountDetail>();
        GeneralLedgerPendingEntrySequenceHelper sequenceHelper = new GeneralLedgerPendingEntrySequenceHelper();
        
        for (PaymentDetail paymentDetail : paymentGroup.getPaymentDetails()) {
            accountListings.addAll(paymentDetail.getAccountDetail());
        }

        //GeneralLedgerPendingEntrySequenceHelper sequenceHelper = new GeneralLedgerPendingEntrySequenceHelper();
        for (PaymentAccountDetail paymentAccountDetail : accountListings) {
            GlPendingTransaction glPendingTransaction = new GlPendingTransaction();
            glPendingTransaction.setSequenceNbr(new KualiInteger(sequenceHelper.getSequenceCounter()));

            if (StringUtils.isNotBlank(paymentAccountDetail.getPaymentDetail().getFinancialSystemOriginCode()) && StringUtils.isNotBlank(paymentAccountDetail.getPaymentDetail().getFinancialDocumentTypeCode())) {
                glPendingTransaction.setFdocRefTypCd(paymentAccountDetail.getPaymentDetail().getFinancialDocumentTypeCode());
                glPendingTransaction.setFsRefOriginCd(paymentAccountDetail.getPaymentDetail().getFinancialSystemOriginCode());
            }
            else {
                glPendingTransaction.setFdocRefTypCd(PdpConstants.PDP_FDOC_TYPE_CODE);
                glPendingTransaction.setFsRefOriginCd(PdpConstants.PDP_FDOC_ORIGIN_CODE);
            }

            glPendingTransaction.setFinancialBalanceTypeCode(org.kuali.kfs.sys.KFSConstants.BALANCE_TYPE_ACTUAL);

            Date transactionTimestamp = new Date(dateTimeService.getCurrentDate().getTime());
            glPendingTransaction.setTransactionDt(transactionTimestamp);
            AccountingPeriod fiscalPeriod = accountingPeriodService.getByDate(new java.sql.Date(transactionTimestamp.getTime()));
            glPendingTransaction.setUniversityFiscalYear(fiscalPeriod.getUniversityFiscalYear());
            glPendingTransaction.setUnivFiscalPrdCd(fiscalPeriod.getUniversityFiscalPeriodCode());

            glPendingTransaction.setAccountNumber(paymentAccountDetail.getAccountNbr());
            glPendingTransaction.setSubAccountNumber(paymentAccountDetail.getSubAccountNbr());
            glPendingTransaction.setChartOfAccountsCode(paymentAccountDetail.getFinChartCode());

            if (paymentGroup.getDisbursementType().getCode().equals(PdpConstants.DisbursementTypeCodes.ACH)) {
                glPendingTransaction.setFinancialDocumentTypeCode(achFdocTypeCode);
            }
            else if (paymentGroup.getDisbursementType().getCode().equals(PdpConstants.DisbursementTypeCodes.CHECK)) {
                glPendingTransaction.setFinancialDocumentTypeCode(checkFdocTypeCod);
            }

            glPendingTransaction.setFsOriginCd(PdpConstants.PDP_FDOC_ORIGIN_CODE);
            glPendingTransaction.setFdocNbr(paymentGroup.getDisbursementNbr().toString());
            
            // if stale
            if (StringUtils.equals(FDOC_TYP_CD_STALE_CHECK, checkFdocTypeCod)) {
                ParameterService parameterService = SpringContext.getBean(ParameterService.class);

                String clAcct = parameterService.getParameterValueAsString(CheckReconciliationImportStep.class, CRConstants.CLEARING_ACCOUNT);
                String obCode = parameterService.getParameterValueAsString(CheckReconciliationImportStep.class, CRConstants.CLEARING_OBJECT_CODE);
                String coaCode = parameterService.getParameterValueAsString(CheckReconciliationImportStep.class, CRConstants.CLEARING_COA);

                // Use clearing parameters if stale
                glPendingTransaction.setAccountNumber(clAcct);
                glPendingTransaction.setFinancialObjectCode(obCode);
                glPendingTransaction.setChartOfAccountsCode(coaCode);
                glPendingTransaction.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
                
                // KFSUPGRADE-943
                glPendingTransaction.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());              
            } else {

                Boolean relieveLiabilities = paymentGroup.getBatch().getCustomerProfile().getRelieveLiabilities();
                if ((relieveLiabilities != null) && (relieveLiabilities.booleanValue()) && paymentAccountDetail.getPaymentDetail().getFinancialDocumentTypeCode() != null) {
                    OffsetDefinition offsetDefinition = SpringContext.getBean(OffsetDefinitionService.class).getByPrimaryId(glPendingTransaction.getUniversityFiscalYear(), glPendingTransaction.getChartOfAccountsCode(), paymentAccountDetail.getPaymentDetail().getFinancialDocumentTypeCode(), glPendingTransaction.getFinancialBalanceTypeCode());
                    glPendingTransaction.setFinancialObjectCode(offsetDefinition != null ? offsetDefinition.getFinancialObjectCode() : paymentAccountDetail.getFinObjectCode());
                    glPendingTransaction.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
                } else {
                    glPendingTransaction.setFinancialObjectCode(paymentAccountDetail.getFinObjectCode());
                    glPendingTransaction.setFinancialSubObjectCode(paymentAccountDetail.getFinSubObjectCode());
                }
            }

            glPendingTransaction.setProjectCd(paymentAccountDetail.getProjectCode());

            glPendingTransaction.setDebitCrdtCd(pdpUtilService.isDebit(paymentAccountDetail, reversal) ? KFSConstants.GL_DEBIT_CODE : KFSConstants.GL_CREDIT_CODE);
            
            glPendingTransaction.setAmount(paymentAccountDetail.getAccountNetAmount().abs());

            //Changes for Research Participant Upload
            String trnDesc = StringUtils.EMPTY;
            CustomerProfile customerProfile = paymentGroup.getBatch().getCustomerProfile();

            // KFSUPGRADE-973 
            if (researchParticipantPaymentValidationService.isResearchParticipantPayment(customerProfile)) {
                BusinessObjectEntry businessObjectEntry = dataDictionaryService.getDataDictionary().getBusinessObjectEntry(PaymentDetail.class.getName());
                AttributeDefinition attributeDefinition = businessObjectEntry.getAttributeDefinition("paymentGroup.payeeName");
                AttributeSecurity originalPayeeNameAttributeSecurity = attributeDefinition.getAttributeSecurity();
                //This is a temporary work around for an issue introduced with KFSCNTRB-705.
                if (ObjectUtils.isNotNull(originalPayeeNameAttributeSecurity)) {
                    String maskLiteral = ((MaskFormatterLiteral) originalPayeeNameAttributeSecurity.getMaskFormatter()).getLiteral();
                    trnDesc = maskLiteral;
                }
            }
            else {
                String payeeName = paymentGroup.getPayeeName();
                if (StringUtils.isNotBlank(payeeName)) {
                    trnDesc = payeeName.length() > 40 ? payeeName.substring(0, 40) : StringUtils.rightPad(payeeName, 40);
                }

                if (reversal) {
                    String poNbr = paymentAccountDetail.getPaymentDetail().getPurchaseOrderNbr();
                    if (StringUtils.isNotBlank(poNbr)) {
                        trnDesc += " " + (poNbr.length() > 9 ? poNbr.substring(0, 9) : StringUtils.rightPad(poNbr, 9));
                    }

                    String invoiceNbr = paymentAccountDetail.getPaymentDetail().getInvoiceNbr();
                    if (StringUtils.isNotBlank(invoiceNbr)) {
                        trnDesc += " " + (invoiceNbr.length() > 14 ? invoiceNbr.substring(0, 14) : StringUtils.rightPad(invoiceNbr, 14));
                    }

                    if (trnDesc.length() > 40) {
                        trnDesc = trnDesc.substring(0, 40);
                    }
                }
            }
            glPendingTransaction.setDescription(trnDesc);

            glPendingTransaction.setOrgDocNbr(paymentAccountDetail.getPaymentDetail().getOrganizationDocNbr());
            glPendingTransaction.setOrgReferenceId(paymentAccountDetail.getOrgReferenceId());
            glPendingTransaction.setFdocRefNbr(paymentAccountDetail.getPaymentDetail().getCustPaymentDocNbr());

            // update the offset account if necessary
            SpringContext.getBean(FlexibleOffsetAccountService.class).updateOffset(glPendingTransaction);

            this.businessObjectService.save(glPendingTransaction);

            sequenceHelper.increment();

            if (bankService.isBankSpecificationEnabled()) {
                this.populateBankOffsetEntry(paymentGroup, glPendingTransaction, sequenceHelper);
            }
        }
    }
    
    /**
     * Reverses the entries of the source documents
     * 
     * @param paymentDetail
     * @param sequenceHelper
     */
    protected void reverseSourceDocumentsEntries(PaymentDetail paymentDetail, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {

        // Need to reverse the payment document's GL entries if the check is stopped or cancelled
        if (PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT.equalsIgnoreCase(paymentDetail.getFinancialDocumentTypeCode()) || CUPdpConstants.PdpDocumentTypes.DISBURSEMENT_VOUCHER.equalsIgnoreCase(paymentDetail.getFinancialDocumentTypeCode()) || CUPdpConstants.PdpDocumentTypes.CREDIT_MEMO.equalsIgnoreCase(paymentDetail.getFinancialDocumentTypeCode())) {
            try {
                String sourceDocumentNumber = paymentDetail.getCustPaymentDocNbr();
                try {
                    Long.valueOf(sourceDocumentNumber);
                } catch (NumberFormatException nfe) {
                    sourceDocumentNumber = null;
                }

                if (sourceDocumentNumber != null && StringUtils.isNotBlank(sourceDocumentNumber)) {

                    Document doc = (AccountingDocumentBase) SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(paymentDetail.getCustPaymentDocNbr());

                    if (ObjectUtils.isNotNull(doc)) {
                        if (doc instanceof DisbursementVoucherDocument) {
                        	//KFSUPGRADE-775
                            DisbursementVoucherDocument dv = (DisbursementVoucherDocument) doc;
                            generateDisbursementVoucherReversalEntries(dv, sequenceHelper);
                            //end KFSUPGRADE-775

                        } else if (doc instanceof VendorCreditMemoDocument) {
                            // KFSPTS-2719
                            String crCmCancelNote = parameterService.getParameterValueAsString(VendorCreditMemoDocument.class, CUPurapParameterConstants.PURAP_CR_CM_CANCEL_NOTE);
                            VendorCreditMemoDocument cmDocument = (VendorCreditMemoDocument) doc;
                            String crCancelMaintDocNbr = getCrCancelMaintenancedocumentNumber(paymentDetail);
                            crCmCancelNote = crCmCancelNote + crCancelMaintDocNbr;

                            try {
                                Note noteObj = documentService.createNoteFromDocument(cmDocument, crCmCancelNote);
                                cmDocument.addNote(noteObj);
                                noteService.save(noteObj);
                            } catch (Exception e) {
                                throw new RuntimeException(e.getMessage());
                            }
                            
                            //KFSUPGRADE-775
                            VendorCreditMemoDocument cm = (VendorCreditMemoDocument) doc;
                            AccountsPayableDocumentSpecificService accountsPayableDocumentSpecificService = cm.getDocumentSpecificService();
                            accountsPayableDocumentSpecificService.updateStatusByNode("", cm);
                            //end KFSUPGRADE-775

                            generateCreditMemoReversalEntries((VendorCreditMemoDocument) doc);

                        } else if (doc instanceof PaymentRequestDocument) {
                            // KFSPTS-2719
                            String crPreqCancelNote = parameterService.getParameterValueAsString(PaymentRequestDocument.class, CUPurapParameterConstants.PURAP_CR_PREQ_CANCEL_NOTE);
                            PaymentRequestDocument paymentRequest = (PaymentRequestDocument) doc;
                            String crCancelMaintDocNbr = getCrCancelMaintenancedocumentNumber(paymentDetail);

                            crPreqCancelNote = crPreqCancelNote + crCancelMaintDocNbr;

                            try {

                                Note cancelNote = documentService.createNoteFromDocument(paymentRequest, crPreqCancelNote);
                                paymentRequest.addNote(cancelNote);
                                noteService.save(cancelNote);
                            } catch (Exception e) {
                                throw new RuntimeException(PurapConstants.REQ_UNABLE_TO_CREATE_NOTE + " " + e);
                            }

                            // cancel extracted should not reopen PO
                            paymentRequest.setReopenPurchaseOrderIndicator(false);
                            
                            //KFSUPGRADE-775
                            AccountsPayableDocumentSpecificService accountsPayableDocumentSpecificService = paymentRequest.getDocumentSpecificService();
                            accountsPayableDocumentSpecificService.updateStatusByNode("", paymentRequest);

                            //end KFSUPGRADE-775

                            generatePaymentRequestReversalEntries(paymentRequest);

                        }
                    }
                }

            } catch (WorkflowException we) {
                System.out.println("Exception retrieving document " + paymentDetail.getCustPaymentDocNbr());
            }
        }

    }
    
    
    private String getCrCancelMaintenancedocumentNumber(PaymentDetail paymentDetail){
        String crCancelMaintDocNbr = KFSConstants.EMPTY_STRING;

        KualiInteger crCheckNbr = paymentDetail.getPaymentGroup().getDisbursementNbr();
        Map<String, KualiInteger> fieldValues = new HashMap<String, KualiInteger>();
        fieldValues.put("checkNumber", crCheckNbr);

        Collection<CheckReconciliation> crEntries = businessObjectService.findMatching(CheckReconciliation.class, fieldValues);
        if (crEntries != null && crEntries.size() > 0) {
            CheckReconciliation crEntry = crEntries.iterator().next();
            crCancelMaintDocNbr = crEntry.getCancelDocHdrId();
        }
        
        return crCancelMaintDocNbr;
    }
    
    
    /**
     * Generates the reversal entries for the given input DisbursementVoucherDocument.
     * 
     * @param doc
     *            the DisbursementVoucherDocument for which we generate the reversal entries
     * @param sequenceHelper
     */
    protected void generateDisbursementVoucherReversalEntries(DisbursementVoucherDocument doc, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        // generate all the pending entries for the document
        SpringContext.getBean(GeneralLedgerPendingEntryService.class).generateGeneralLedgerPendingEntries(doc);
        // for each pending entry, opposite-ify it and reattach it to the document

        List<GeneralLedgerPendingEntry> glpes = doc.getGeneralLedgerPendingEntries();

        if (glpes != null && glpes.size() > 0) {
            for (GeneralLedgerPendingEntry glpe : glpes) {

                if (KFSConstants.GL_CREDIT_CODE.equalsIgnoreCase(glpe.getTransactionDebitCreditCode())) {
                    glpe.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
                } else if (KFSConstants.GL_DEBIT_CODE.equalsIgnoreCase(glpe.getTransactionDebitCreditCode())) {
                    glpe.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
                }
                glpe.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
                sequenceHelper.increment();
                Date transactionTimestamp = new Date(dateTimeService.getCurrentDate().getTime());

                AccountingPeriod fiscalPeriod = accountingPeriodService.getByDate(new java.sql.Date(transactionTimestamp.getTime()));
                glpe.setFinancialDocumentApprovedCode(KFSConstants.PENDING_ENTRY_APPROVED_STATUS_CODE.APPROVED);
                glpe.setUniversityFiscalYear(fiscalPeriod.getUniversityFiscalYear());
                glpe.setUniversityFiscalPeriodCode(fiscalPeriod.getUniversityFiscalPeriodCode());
                businessObjectService.save(glpe);

            }
        }
    }
    
    
    /**
     * Generates the reversal entries for the given input VendorCreditMemoDocument.
     * 
     * @param doc
     *            the VendorCreditMemoDocument for which we generate the reversal entries
     * @param sequenceHelper
     */
    protected void generateCreditMemoReversalEntries(VendorCreditMemoDocument cm) {

        cm.setGeneralLedgerPendingEntries(new ArrayList());

        boolean success = true;
        GeneralLedgerPendingEntrySequenceHelper sequenceHelper = new GeneralLedgerPendingEntrySequenceHelper(getNextAvailableSequence(cm.getDocumentNumber()));

        if (!cm.isSourceVendor()) {
            LOG.debug("generateEntriesCreditMemo() create encumbrance entries for CM against a PO or PREQ (not vendor)");
            PurchaseOrderDocument po = null;
            if (cm.isSourceDocumentPurchaseOrder()) {
                LOG.debug("generateEntriesCreditMemo() PO type");
                po = SpringContext.getBean(PurchaseOrderService.class).getCurrentPurchaseOrder(cm.getPurchaseOrderIdentifier());
            }
            else if (cm.isSourceDocumentPaymentRequest()) {
                LOG.debug("generateEntriesCreditMemo() PREQ type");
                po = SpringContext.getBean(PurchaseOrderService.class).getCurrentPurchaseOrder(cm.getPaymentRequestDocument().getPurchaseOrderIdentifier());
            }

            // for CM cancel or create, do not book encumbrances if PO is CLOSED, but do update the amounts on the PO
            List encumbrances = getCreditMemoEncumbrance(cm, po);
            if (!(PurapConstants.PurchaseOrderStatuses.APPDOC_CLOSED.equals(po.getApplicationDocumentStatus()))) {
                if (encumbrances != null) {
                    cm.setGenerateEncumbranceEntries(true);

                    // even if generating encumbrance entries on cancel, call is the same because the method gets negative amounts
                    // from
                    // the map so Debits on negatives = a credit
                    cm.setDebitCreditCodeForGLEntries(GL_DEBIT_CODE);

                    for (Iterator iter = encumbrances.iterator(); iter.hasNext();) {
                        AccountingLine accountingLine = (AccountingLine) iter.next();
                        if (accountingLine.getAmount().compareTo(ZERO) != 0) {
                            cm.generateGeneralLedgerPendingEntries(accountingLine, sequenceHelper);
                            sequenceHelper.increment(); // increment for the next line
                        }
                    }
                }
            }
        }

        List<SummaryAccount> summaryAccounts = SpringContext.getBean(PurapAccountingService.class).generateSummaryAccountsWithNoZeroTotalsNoUseTax(cm);
        if (summaryAccounts != null) {
            LOG.debug("generateEntriesCreditMemo() now book the actuals");
            cm.setGenerateEncumbranceEntries(false);

                // on cancel, use DEBIT code


            for (Iterator iter = summaryAccounts.iterator(); iter.hasNext();) {
                SummaryAccount summaryAccount = (SummaryAccount) iter.next();
                cm.generateGeneralLedgerPendingEntries(summaryAccount.getAccount(), sequenceHelper);
                sequenceHelper.increment(); // increment for the next line
            }
            // generate offset accounts for use tax if it exists (useTaxContainers will be empty if not a use tax document)
            List<UseTaxContainer> useTaxContainers = SpringContext.getBean(PurapAccountingService.class).generateUseTaxAccount(cm);
            for (UseTaxContainer useTaxContainer : useTaxContainers) {
                PurApItemUseTax offset = useTaxContainer.getUseTax();
                List<SourceAccountingLine> accounts = useTaxContainer.getAccounts();
                for (SourceAccountingLine sourceAccountingLine : accounts) {
                    cm.generateGeneralLedgerPendingEntries(sourceAccountingLine, sequenceHelper, useTaxContainer.getUseTax());
                    sequenceHelper.increment(); // increment for the next line
                }

            }

            // manually save cm account change tables (CAMS needs this)

                SpringContext.getBean(PurapAccountRevisionService.class).cancelCreditMemoAccountRevisions(cm.getItems(), cm.getPostingYearFromPendingGLEntries(), cm.getPostingPeriodCodeFromPendingGLEntries());
      
        }

        businessObjectService.save(cm.getGeneralLedgerPendingEntries());

    }
    
    /**
     * Re-encumber the Encumbrance on a PO based on values in a PREQ. This is used when a PREQ is cancelled. Note: This modifies the
     * encumbrance values on the PO and saves the PO
     * 
     * @param cm Credit Memo document
     * @param po Purchase Order document modify encumbrances
     * @return List of accounting lines to use to create the pending general ledger entries
     */
    protected List<SourceAccountingLine> getCreditMemoEncumbrance(VendorCreditMemoDocument cm, PurchaseOrderDocument po) {
        LOG.debug("getCreditMemoEncumbrance() started");

        if (ObjectUtils.isNull(po)) {
            return null;
        }

            LOG.debug("getCreditMemoEncumbrance() Receiving items back from vendor (cancelled CM)");
       

        Map encumbranceAccountMap = new HashMap();

        // Get each item one by one
        for (Iterator items = cm.getItems().iterator(); items.hasNext();) {
            CreditMemoItem cmItem = (CreditMemoItem) items.next();
            PurchaseOrderItem poItem = getPoItem(po, cmItem.getItemLineNumber(), cmItem.getItemType());

            KualiDecimal itemDisEncumber = null; // Amount to disencumber for this item
            KualiDecimal itemAlterInvoiceAmt = null; // Amount to alter the invoicedAmt on the PO item

            String logItmNbr = "Item # " + cmItem.getItemLineNumber();
            LOG.debug("getCreditMemoEncumbrance() " + logItmNbr);

            final KualiDecimal cmItemTotalAmount = (cmItem.getTotalAmount() == null) ? KualiDecimal.ZERO : cmItem.getTotalAmount();
            ;
            // If there isn't a PO item or the total amount is 0, we don't need encumbrances
            if ((poItem == null) || (cmItemTotalAmount == null) || (cmItemTotalAmount.doubleValue() == 0)) {
                LOG.debug("getCreditMemoEncumbrance() " + logItmNbr + " No encumbrances required");
            }
            else {
                LOG.debug("getCreditMemoEncumbrance() " + logItmNbr + " Calculate encumbrance GL entries");

                // Do we calculate the encumbrance amount based on quantity or amount?
                if (poItem.getItemType().isQuantityBasedGeneralLedgerIndicator()) {
                    LOG.debug("getCreditMemoEncumbrance() " + logItmNbr + " Calculate encumbrance based on quantity");

                    // Do encumbrance calculations based on quantity                    
                    KualiDecimal cmQuantity = cmItem.getItemQuantity() == null ? ZERO : cmItem.getItemQuantity();                    
                    
                    KualiDecimal encumbranceQuantityChange = calculateQuantityChange(poItem, cmQuantity);

                    LOG.debug("getCreditMemoEncumbrance() " + logItmNbr + " encumbranceQtyChange " + encumbranceQuantityChange + " outstandingEncumberedQty " + poItem.getItemOutstandingEncumberedQuantity() + " invoicedTotalQuantity " + poItem.getItemInvoicedTotalQuantity());
                    
                    itemDisEncumber = encumbranceQuantityChange.multiply(new KualiDecimal(poItem.getItemUnitPrice()));
                    
                    //add tax for encumbrance
                    KualiDecimal itemTaxAmount = poItem.getItemTaxAmount() == null ? ZERO : poItem.getItemTaxAmount();
                    KualiDecimal encumbranceTaxAmount = encumbranceQuantityChange.divide(poItem.getItemQuantity()).multiply(itemTaxAmount);
                    itemDisEncumber = itemDisEncumber.add(encumbranceTaxAmount);
                    
                    itemAlterInvoiceAmt = cmItemTotalAmount;
                  
                        itemAlterInvoiceAmt = itemAlterInvoiceAmt.multiply(new KualiDecimal("-1"));
                  
                }
                else {
                    LOG.debug("getCreditMemoEncumbrance() " + logItmNbr + " Calculate encumbrance based on amount");

                    // Do encumbrance calculations based on amount only

                        // Decrease encumbrance
                        itemDisEncumber = cmItemTotalAmount.multiply(new KualiDecimal("-1"));

                        if (poItem.getItemOutstandingEncumberedAmount().add(itemDisEncumber).doubleValue() < 0) {
                            LOG.debug("getCreditMemoEncumbrance() Cancel overflow");

                            itemDisEncumber = poItem.getItemOutstandingEncumberedAmount();
                        }
       
                    itemAlterInvoiceAmt = itemDisEncumber;
                }

                // alter the encumbrance based on what was originally encumbered
                poItem.setItemOutstandingEncumberedAmount(poItem.getItemOutstandingEncumberedAmount().add(itemDisEncumber));

                // alter the invoiced amt based on what was actually credited on the credit memo
                poItem.setItemInvoicedTotalAmount(poItem.getItemInvoicedTotalAmount().subtract(itemAlterInvoiceAmt));
                if (poItem.getItemInvoicedTotalAmount().compareTo(ZERO) < 0) {
                    poItem.setItemInvoicedTotalAmount(ZERO);
                }


                LOG.debug("getCreditMemoEncumbrance() " + logItmNbr + " Amount to disencumber: " + itemDisEncumber);

                // Sort accounts
                Collections.sort((List) poItem.getSourceAccountingLines());

                // make the list of accounts for the disencumbrance entry
                PurchaseOrderAccount lastAccount = null;
                KualiDecimal accountTotal = ZERO;
                // Collections.sort((List)poItem.getSourceAccountingLines());
                for (Iterator accountIter = poItem.getSourceAccountingLines().iterator(); accountIter.hasNext();) {
                    PurchaseOrderAccount account = (PurchaseOrderAccount) accountIter.next();
                    if (!account.isEmpty()) {
                        KualiDecimal encumbranceAmount = null;

                        SourceAccountingLine acctString = account.generateSourceAccountingLine();
                        // amount = item disencumber * account percent / 100
                        encumbranceAmount = itemDisEncumber.multiply(new KualiDecimal(account.getAccountLinePercent().toString())).divide(new KualiDecimal(100));

                        account.setItemAccountOutstandingEncumbranceAmount(account.getItemAccountOutstandingEncumbranceAmount().add(encumbranceAmount));

                        // For rounding check at the end
                        accountTotal = accountTotal.add(encumbranceAmount);

                        lastAccount = account;

                        LOG.debug("getCreditMemoEncumbrance() " + logItmNbr + " " + acctString + " = " + encumbranceAmount);

                        if (encumbranceAccountMap.get(acctString) == null) {
                            encumbranceAccountMap.put(acctString, encumbranceAmount);
                        }
                        else {
                            KualiDecimal amt = (KualiDecimal) encumbranceAccountMap.get(acctString);
                            encumbranceAccountMap.put(acctString, amt.add(encumbranceAmount));
                        }
                    }
                }

                // account for rounding by adjusting last account as needed
                if (lastAccount != null) {
                    KualiDecimal difference = itemDisEncumber.subtract(accountTotal);
                    LOG.debug("getCreditMemoEncumbrance() difference: " + logItmNbr + " " + difference);

                    SourceAccountingLine acctString = lastAccount.generateSourceAccountingLine();
                    KualiDecimal amount = (KualiDecimal) encumbranceAccountMap.get(acctString);
                    if (amount == null) {
                        encumbranceAccountMap.put(acctString, difference);
                    }
                    else {
                        encumbranceAccountMap.put(acctString, amount.add(difference));
                    }
                    lastAccount.setItemAccountOutstandingEncumbranceAmount(lastAccount.getItemAccountOutstandingEncumbranceAmount().add(difference));
                }
            }
        }

        List<SourceAccountingLine> encumbranceAccounts = new ArrayList();
        for (Iterator iter = encumbranceAccountMap.keySet().iterator(); iter.hasNext();) {
            SourceAccountingLine acctString = (SourceAccountingLine) iter.next();
            KualiDecimal amount = (KualiDecimal) encumbranceAccountMap.get(acctString);
            if (amount.doubleValue() != 0) {
                acctString.setAmount(amount);
                encumbranceAccounts.add(acctString);
            }
        }

        return encumbranceAccounts;
    }
    
    /**
     * Calculate quantity change for creating Credit Memo entries
     * 
     * @param cancel Boolean indicating whether entries are for creation or cancellation of credit memo
     * @param poItem Purchase Order Item
     * @param cmQuantity Quantity on credit memo item
     * @return Calculated change
     */
    protected KualiDecimal calculateQuantityChange( PurchaseOrderItem poItem, KualiDecimal cmQuantity) {
        LOG.debug("calculateQuantityChange() started");

        // Calculate quantity change & adjust invoiced quantity & outstanding encumbered quantity
        KualiDecimal encumbranceQuantityChange = null;
    
            encumbranceQuantityChange = cmQuantity.multiply(new KualiDecimal("-1"));
        
        poItem.setItemInvoicedTotalQuantity(poItem.getItemInvoicedTotalQuantity().subtract(encumbranceQuantityChange));
        poItem.setItemOutstandingEncumberedQuantity(poItem.getItemOutstandingEncumberedQuantity().add(encumbranceQuantityChange));

        // Check for overflows
            if (poItem.getItemOutstandingEncumberedQuantity().doubleValue() < 0) {
                LOG.debug("calculateQuantityChange() Cancel overflow");
                KualiDecimal difference = poItem.getItemOutstandingEncumberedQuantity().abs();
                poItem.setItemOutstandingEncumberedQuantity(ZERO);
                poItem.setItemInvoicedTotalQuantity(poItem.getItemQuantity());
                encumbranceQuantityChange = encumbranceQuantityChange.add(difference);
            }
        
        return encumbranceQuantityChange;
    }
    
    
    /**
     * Generates the reversal entries for the input PaymentRequestDocument.
     * 
     * @param preq
     */
    protected void generatePaymentRequestReversalEntries(PaymentRequestDocument preq) {

        List<SourceAccountingLine> encumbrances = reencumberEncumbrance(preq);
        List<SummaryAccount> summaryAccounts = SpringContext.getBean(PurapAccountingService.class).generateSummaryAccountsWithNoZeroTotalsNoUseTax( preq);
        generateEntriesPaymentRequest(preq, encumbrances, summaryAccounts);

    }
    

    /**
     * Re-encumber the Encumbrance on a PO based on values in a PREQ. This is used when a PREQ is cancelled. Note: This modifies the
     * encumbrance values on the PO and saves the PO
     * 
     * @param preq PREQ for invoice
     * @return List of accounting lines to use to create the pending general ledger entries
     */
    protected List<SourceAccountingLine> reencumberEncumbrance(PaymentRequestDocument preq) {
        LOG.debug("reencumberEncumbrance() started");

        PurchaseOrderDocument po = SpringContext.getBean(PurchaseOrderService.class).getCurrentPurchaseOrder(preq.getPurchaseOrderIdentifier());
        Map encumbranceAccountMap = new HashMap();

        // Get each item one by one
        for (Iterator items = preq.getItems().iterator(); items.hasNext();) {
            PaymentRequestItem payRequestItem = (PaymentRequestItem) items.next();
            PurchaseOrderItem poItem = getPoItem(po, payRequestItem.getItemLineNumber(), payRequestItem.getItemType());

            KualiDecimal itemReEncumber = null; // Amount to reencumber for this item

            String logItmNbr = "Item # " + payRequestItem.getItemLineNumber();
            LOG.debug("reencumberEncumbrance() " + logItmNbr);

            // If there isn't a PO item or the total amount is 0, we don't need encumbrances
            final KualiDecimal preqItemTotalAmount = (payRequestItem.getTotalAmount() == null) ? KualiDecimal.ZERO : payRequestItem.getTotalAmount();
            if ((poItem == null) || (preqItemTotalAmount.doubleValue() == 0)) {
                LOG.debug("reencumberEncumbrance() " + logItmNbr + " No encumbrances required");
            }
            else {
                LOG.debug("reencumberEncumbrance() " + logItmNbr + " Calculate encumbrance GL entries");

                // Do we calculate the encumbrance amount based on quantity or amount?
                if (poItem.getItemType().isQuantityBasedGeneralLedgerIndicator()) {
                    LOG.debug("reencumberEncumbrance() " + logItmNbr + " Calculate encumbrance based on quantity");

                    // Do disencumbrance calculations based on quantity
                    KualiDecimal preqQuantity = payRequestItem.getItemQuantity() == null ? ZERO : payRequestItem.getItemQuantity();
                    KualiDecimal outstandingEncumberedQuantity = poItem.getItemOutstandingEncumberedQuantity() == null ? ZERO : poItem.getItemOutstandingEncumberedQuantity();
                    KualiDecimal invoicedTotal = poItem.getItemInvoicedTotalQuantity() == null ? ZERO : poItem.getItemInvoicedTotalQuantity();

                    poItem.setItemInvoicedTotalQuantity(invoicedTotal.subtract(preqQuantity));
                    poItem.setItemOutstandingEncumberedQuantity(outstandingEncumberedQuantity.add(preqQuantity));

                    itemReEncumber = preqQuantity.multiply(new KualiDecimal(poItem.getItemUnitPrice()));

                    //add tax for encumbrance
                    KualiDecimal itemTaxAmount = poItem.getItemTaxAmount() == null ? ZERO : poItem.getItemTaxAmount();
                    KualiDecimal encumbranceTaxAmount = preqQuantity.divide(poItem.getItemQuantity()).multiply(itemTaxAmount);
                    itemReEncumber = itemReEncumber.add(encumbranceTaxAmount);

                }
                else {
                    LOG.debug("reencumberEncumbrance() " + logItmNbr + " Calculate encumbrance based on amount");

                    itemReEncumber = preqItemTotalAmount;
                    // if re-encumber amount is more than original PO ordered amount... do not exceed ordered amount
                    // this prevents negative encumbrance
                    if ((poItem.getTotalAmount() != null) && (poItem.getTotalAmount().bigDecimalValue().signum() < 0)) {
                        // po item extended cost is negative
                        if ((poItem.getTotalAmount().compareTo(itemReEncumber)) > 0) {
                            itemReEncumber = poItem.getTotalAmount();
                        }
                    }
                    else if ((poItem.getTotalAmount() != null) && (poItem.getTotalAmount().bigDecimalValue().signum() >= 0)) {
                        // po item extended cost is positive
                        if ((poItem.getTotalAmount().compareTo(itemReEncumber)) < 0) {
                            itemReEncumber = poItem.getTotalAmount();
                        }
                    }
                }

                LOG.debug("reencumberEncumbrance() " + logItmNbr + " Amount to reencumber: " + itemReEncumber);

                KualiDecimal outstandingEncumberedAmount = poItem.getItemOutstandingEncumberedAmount() == null ? ZERO : poItem.getItemOutstandingEncumberedAmount();
                LOG.debug("reencumberEncumbrance() " + logItmNbr + " PO Item Outstanding Encumbrance Amount set to: " + outstandingEncumberedAmount);
                KualiDecimal newOutstandingEncumberedAmount = outstandingEncumberedAmount.add(itemReEncumber);
                LOG.debug("reencumberEncumbrance() " + logItmNbr + " New PO Item Outstanding Encumbrance Amount to set: " + newOutstandingEncumberedAmount);
                poItem.setItemOutstandingEncumberedAmount(newOutstandingEncumberedAmount);

                KualiDecimal invoicedTotalAmount = poItem.getItemInvoicedTotalAmount() == null ? ZERO : poItem.getItemInvoicedTotalAmount();
                LOG.debug("reencumberEncumbrance() " + logItmNbr + " PO Item Invoiced Total Amount set to: " + invoicedTotalAmount);
                KualiDecimal newInvoicedTotalAmount = invoicedTotalAmount.subtract(preqItemTotalAmount);
                LOG.debug("reencumberEncumbrance() " + logItmNbr + " New PO Item Invoiced Total Amount to set: " + newInvoicedTotalAmount);
                poItem.setItemInvoicedTotalAmount(newInvoicedTotalAmount);

                // make the list of accounts for the reencumbrance entry
                PurchaseOrderAccount lastAccount = null;
                KualiDecimal accountTotal = ZERO;

                // Sort accounts
                Collections.sort((List) poItem.getSourceAccountingLines());

                for (Iterator accountIter = poItem.getSourceAccountingLines().iterator(); accountIter.hasNext();) {
                    PurchaseOrderAccount account = (PurchaseOrderAccount) accountIter.next();
                    if (!account.isEmpty()) {
                        SourceAccountingLine acctString = account.generateSourceAccountingLine();

                        // amount = item reencumber * account percent / 100
                        KualiDecimal reencumbranceAmount = itemReEncumber.multiply(new KualiDecimal(account.getAccountLinePercent().toString())).divide(HUNDRED);

                        account.setItemAccountOutstandingEncumbranceAmount(account.getItemAccountOutstandingEncumbranceAmount().add(reencumbranceAmount));

                        // For rounding check at the end
                        accountTotal = accountTotal.add(reencumbranceAmount);

                        lastAccount = account;

                        LOG.debug("reencumberEncumbrance() " + logItmNbr + " " + acctString + " = " + reencumbranceAmount);
                        if (encumbranceAccountMap.containsKey(acctString)) {
                            KualiDecimal currentAmount = (KualiDecimal) encumbranceAccountMap.get(acctString);
                            encumbranceAccountMap.put(acctString, reencumbranceAmount.add(currentAmount));
                        }
                        else {
                            encumbranceAccountMap.put(acctString, reencumbranceAmount);
                        }
                    }
                }

                // account for rounding by adjusting last account as needed
                if (lastAccount != null) {
                    KualiDecimal difference = itemReEncumber.subtract(accountTotal);
                    LOG.debug("reencumberEncumbrance() difference: " + logItmNbr + " " + difference);

                    SourceAccountingLine acctString = lastAccount.generateSourceAccountingLine();
                    KualiDecimal amount = (KualiDecimal) encumbranceAccountMap.get(acctString);
                    if (amount == null) {
                        encumbranceAccountMap.put(acctString, difference);
                    }
                    else {
                        encumbranceAccountMap.put(acctString, amount.add(difference));
                    }
                    lastAccount.setItemAccountOutstandingEncumbranceAmount(lastAccount.getItemAccountOutstandingEncumbranceAmount().add(difference));
                }
            }
        }

        List<SourceAccountingLine> encumbranceAccounts = new ArrayList<SourceAccountingLine>();
        for (Iterator<SourceAccountingLine> iter = encumbranceAccountMap.keySet().iterator(); iter.hasNext();) {
            SourceAccountingLine acctString = (SourceAccountingLine) iter.next();
            KualiDecimal amount = (KualiDecimal) encumbranceAccountMap.get(acctString);
            if (amount.doubleValue() != 0) {
                acctString.setAmount(amount);
                encumbranceAccounts.add(acctString);
            }
        }

        return encumbranceAccounts;
    }
    
    /**
     * Find item in PO based on given parameters. Must send either the line # or item type.
     * 
     * @param po Purchase Order containing list of items
     * @param nbr Line # of desired item (could be null)
     * @param itemType Item type of desired item
     * @return PurcahseOrderItem found matching given criteria
     */
    protected PurchaseOrderItem getPoItem(PurchaseOrderDocument po, Integer nbr, ItemType itemType) {
        LOG.debug("getPoItem() started");
        for (Iterator iter = po.getItems().iterator(); iter.hasNext();) {
            PurchaseOrderItem element = (PurchaseOrderItem) iter.next();
            if (itemType.isLineItemIndicator()) {
                if (ObjectUtils.isNotNull(nbr) && ObjectUtils.isNotNull(element.getItemLineNumber()) && (nbr.compareTo(element.getItemLineNumber()) == 0)) {
                    return element;
                }
            }
            else {
                if (element.getItemTypeCode().equals(itemType.getItemTypeCode())) {
                    return element;
                }
            }
        }
        return null;
    }
    
    /**
     * Creates the general ledger entries for Payment Request actions.
     * 
     * @param preq Payment Request document to create entries
     * @param encumbrances List of encumbrance accounts if applies
     * @param accountingLines List of preq accounts to create entries
     * @param processType Type of process (create, modify, cancel)
     * @return Boolean returned indicating whether entry creation succeeded
     */
    protected boolean generateEntriesPaymentRequest(PaymentRequestDocument preq, List encumbrances, List summaryAccounts) {
        LOG.debug("generateEntriesPaymentRequest() started");
        boolean success = true;
        preq.setGeneralLedgerPendingEntries(new ArrayList());

        /*
         * Can't let generalLedgerPendingEntryService just create all the entries because we need the sequenceHelper to carry over
         * from the encumbrances to the actuals and also because we need to tell the PaymentRequestDocumentRule customize entry
         * method how to customize differently based on if creating an encumbrance or actual.
         */
        GeneralLedgerPendingEntrySequenceHelper sequenceHelper = new GeneralLedgerPendingEntrySequenceHelper(getNextAvailableSequence(preq.getDocumentNumber()));

        // when cancelling a PREQ, do not book encumbrances if PO is CLOSED
        if (encumbrances != null) {

                // on cancel, use DEBIT code
                preq.setDebitCreditCodeForGLEntries(GL_DEBIT_CODE);
            

            preq.setGenerateEncumbranceEntries(true);
            for (Iterator iter = encumbrances.iterator(); iter.hasNext();) {
                AccountingLine accountingLine = (AccountingLine) iter.next();
                preq.generateGeneralLedgerPendingEntries(accountingLine, sequenceHelper);
                sequenceHelper.increment(); // increment for the next line
            }
        }

        if (ObjectUtils.isNotNull(summaryAccounts) && !summaryAccounts.isEmpty()) {
            LOG.debug("generateEntriesPaymentRequest() now book the actuals");
            preq.setGenerateEncumbranceEntries(false);


                // on cancel, use CREDIT code
                preq.setDebitCreditCodeForGLEntries(GL_CREDIT_CODE);


            for (Iterator iter = summaryAccounts.iterator(); iter.hasNext();) {
                SummaryAccount summaryAccount = (SummaryAccount) iter.next();
                preq.generateGeneralLedgerPendingEntries(summaryAccount.getAccount(), sequenceHelper);
                sequenceHelper.increment(); // increment for the next line
            }

            // generate offset accounts for use tax if it exists (useTaxContainers will be empty if not a use tax document)
            List<UseTaxContainer> useTaxContainers = SpringContext.getBean(PurapAccountingService.class).generateUseTaxAccount(preq);
            for (UseTaxContainer useTaxContainer : useTaxContainers) {
                PurApItemUseTax offset = useTaxContainer.getUseTax();
                List<SourceAccountingLine> accounts = useTaxContainer.getAccounts();
                for (SourceAccountingLine sourceAccountingLine : accounts) {
                    preq.generateGeneralLedgerPendingEntries(sourceAccountingLine, sequenceHelper, useTaxContainer.getUseTax());
                    sequenceHelper.increment(); // increment for the next line
                }

            }

          
        }


        // Manually save GL entries for Payment Request and encumbrances
        LOG.debug("saveGLEntries() started");
        businessObjectService.save(preq.getGeneralLedgerPendingEntries());

        return success;
    }
    
    /**
     * Retrieves the next available sequence number from the general ledger pending entry table for this document
     * 
     * @param documentNumber Document number to find next sequence number
     * @return Next available sequence number
     */
    protected int getNextAvailableSequence(String documentNumber) {
        LOG.debug("getNextAvailableSequence() started");
        Map fieldValues = new HashMap();
        fieldValues.put("financialSystemOriginationCode", PURAP_ORIGIN_CODE);
        fieldValues.put("documentNumber", documentNumber);
        int count = businessObjectService.countMatching(GeneralLedgerPendingEntry.class, fieldValues);
        return count + 1;
    }

	public DocumentService getDocumentService() {
		return documentService;
	}

	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}

	public NoteService getNoteService() {
		return noteService;
	}

	public void setNoteService(NoteService noteService) {
		this.noteService = noteService;
	}

	public ParameterService getParameterService() {
		return parameterService;
	}

	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}



}
