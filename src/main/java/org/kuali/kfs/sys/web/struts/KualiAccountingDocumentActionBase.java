/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
package org.kuali.kfs.sys.web.struts;

import edu.cornell.kfs.sys.document.service.CUFinancialSystemDocumentService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.parameter.ParameterEvaluator;
import org.kuali.kfs.core.api.parameter.ParameterEvaluatorService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.fp.FPParameterConstants;
import org.kuali.kfs.fp.businessobject.SalesTax;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.krad.rules.rule.event.SaveDocumentEvent;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.KualiRuleService;
import org.kuali.kfs.krad.service.PersistenceService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.AccountingLineOverride;
import org.kuali.kfs.sys.businessobject.AccountingLineParser;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.AmountTotaling;
import org.kuali.kfs.sys.document.validation.event.AddAccountingLineEvent;
import org.kuali.kfs.sys.document.validation.event.DeleteAccountingLineEvent;
import org.kuali.kfs.sys.document.web.struts.FinancialSystemTransactionalDocumentActionBase;
import org.kuali.kfs.sys.exception.AccountingLineParserException;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class handles UI actions for all shared methods of financial documents.
 */
public class KualiAccountingDocumentActionBase extends FinancialSystemTransactionalDocumentActionBase {

    private static final Logger LOG = LogManager.getLogger();

    // Set of actions for which updateEvents should be generated
    protected static final Set UPDATE_EVENT_ACTIONS;

    /**
     * Adds check for accountingLine updates, generates and dispatches any events caused by such updates
     */
    @Override
    public ActionForward execute(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiAccountingDocumentFormBase transForm = (KualiAccountingDocumentFormBase) form;

        // handle changes to accountingLines
        if (transForm.hasDocumentId()) {
            final AccountingDocument financialDocument = (AccountingDocument) transForm.getDocument();

            processAccountingLines(financialDocument, transForm, KFSConstants.SOURCE);
            processAccountingLines(financialDocument, transForm, KFSConstants.TARGET);
        }

        // This is after a potential handleUpdate(), to display automatically cleared overrides following a route or save.
        processAccountingLineOverrides(transForm);

        // proceed as usual
        return super.execute(mapping, form, request, response);
    }

    /**
     * All document-load operations get routed through here
     */
    @Override
    protected void loadDocument(final KualiDocumentFormBase kualiDocumentFormBase) {
        super.loadDocument(kualiDocumentFormBase);
        final KualiAccountingDocumentFormBase tform = (KualiAccountingDocumentFormBase) kualiDocumentFormBase;

        // clear out the new accounting line holders
        tform.setNewSourceLine(null);
        tform.setNewTargetLine(null);

        processAccountingLineOverrides(tform);
    }

    /**
     * Needed to override this to keep from losing Sales Tax information
     */
    @Override
    public ActionForward refresh(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        super.refresh(mapping, form, request, response);
        refreshSalesTaxInfo(form);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Needed to override this to keep from losing Sales Tax information
     */
    @Override
    public ActionForward toggleTab(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        super.toggleTab(mapping, form, request, response);
        refreshSalesTaxInfo(form);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    static {
        final String[] updateEventActions = {
            KFSConstants.SAVE_METHOD, KFSConstants.ROUTE_METHOD,
            KFSConstants.APPROVE_METHOD, KFSConstants.BLANKET_APPROVE_METHOD
        };
        UPDATE_EVENT_ACTIONS = new HashSet();
        Collections.addAll(UPDATE_EVENT_ACTIONS, updateEventActions);
    }

    /**
     * @param transForm
     */
    protected void processAccountingLineOverrides(final KualiAccountingDocumentFormBase transForm) {
        processAccountingLineOverrides(transForm.getNewSourceLine());
        processAccountingLineOverrides(transForm.getNewTargetLine());
        if (transForm.hasDocumentId()) {
            final AccountingDocument financialDocument = (AccountingDocument) transForm.getDocument();

            processAccountingLineOverrides(financialDocument, financialDocument.getSourceAccountingLines());
            processAccountingLineOverrides(financialDocument, financialDocument.getTargetAccountingLines());
        }
    }

    /**
     * @param line
     */
    protected void processAccountingLineOverrides(final AccountingLine line) {
        processAccountingLineOverrides(Arrays.asList(line));
    }

    protected void processAccountingLineOverrides(final List accountingLines) {
        processAccountingLineOverrides(null, accountingLines);
    }

    /**
     * @param accountingLines
     */
    protected void processAccountingLineOverrides(final AccountingDocument financialDocument, final List accountingLines) {
        if (!accountingLines.isEmpty()) {
            final PersistenceService persistenceService = SpringContext.getBean(PersistenceService.class);
            for (final Object accountingLine : accountingLines) {
                final AccountingLine line = (AccountingLine) accountingLine;
                persistenceService.retrieveReferenceObjects(line, AccountingLineOverride.REFRESH_FIELDS);
                AccountingLineOverride.processForOutput(financialDocument, line);
            }
        }
    }

    /**
     * @param transDoc
     * @param transForm
     * @param lineSet
     */
    protected void processAccountingLines(
            final AccountingDocument transDoc, final KualiAccountingDocumentFormBase transForm,
            final String lineSet) {
        // figure out which set of lines we're looking at
        final List formLines;
        final boolean source;
        if (lineSet.equals(KFSConstants.SOURCE)) {
            formLines = transDoc.getSourceAccountingLines();
            source = true;
        } else {
            formLines = transDoc.getTargetAccountingLines();
            source = false;
        }

        // find and process corresponding form and baselines
        int index = 0;
        for (final Iterator i = formLines.iterator(); i.hasNext(); index++) {
            final AccountingLine formLine = (AccountingLine) i.next();

            // update sales tax required attribute for view
            checkSalesTax(transDoc, formLine, source, false, index);
        }
    }

    /**
     * This method will remove a TargetAccountingLine from a FinancialDocument. This assumes that the user presses
     * the delete button for a specific accounting line on the document and that the document is represented by a
     * FinancialDocumentFormBase.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward deleteTargetLine(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiAccountingDocumentFormBase financialDocumentForm = (KualiAccountingDocumentFormBase) form;

        final int deleteIndex = getLineToDelete(request);
        final String errorPath = KFSConstants.DOCUMENT_PROPERTY_NAME + "." +
                                 KFSConstants.EXISTING_TARGET_ACCT_LINE_PROPERTY_NAME + "[" + deleteIndex + "]";
        final boolean rulePassed = SpringContext.getBean(KualiRuleService.class).applyRules(
                new DeleteAccountingLineEvent(errorPath, financialDocumentForm.getDocument(),
                        ((AccountingDocument) financialDocumentForm.getDocument()).getTargetAccountingLine(deleteIndex),
                        false));

        // if the rule evaluation passed, let's delete it
        if (rulePassed) {
            deleteAccountingLine(false, financialDocumentForm, deleteIndex);
        } else {
            final String[] errorParams = new String[]{"target", Integer.toString(deleteIndex + 1)};
            GlobalVariables.getMessageMap().putError(errorPath,
                    KFSKeyConstants.ERROR_ACCOUNTINGLINE_DELETERULE_INVALIDACCOUNT, errorParams);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * This method will remove a SourceAccountingLine from a FinancialDocument. This assumes that the user presses the
     * delete button for a specific accounting line on the document and that the document is represented by a
     * FinancialDocumentFormBase.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward deleteSourceLine(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiAccountingDocumentFormBase financialDocumentForm = (KualiAccountingDocumentFormBase) form;

        final int deleteIndex = getLineToDelete(request);
        final String errorPath = KFSConstants.DOCUMENT_PROPERTY_NAME + "." +
                                 KFSConstants.EXISTING_SOURCE_ACCT_LINE_PROPERTY_NAME + "[" + deleteIndex + "]";
        final boolean rulePassed = SpringContext.getBean(KualiRuleService.class).applyRules(
                new DeleteAccountingLineEvent(errorPath, financialDocumentForm.getDocument(),
                        ((AccountingDocument) financialDocumentForm.getDocument()).getSourceAccountingLine(deleteIndex),
                        false));

        // if the rule evaluation passed, let's delete it
        if (rulePassed) {
            deleteAccountingLine(true, financialDocumentForm, deleteIndex);
        } else {
            final String[] errorParams = new String[]{"source", Integer.toString(deleteIndex + 1)};
            GlobalVariables.getMessageMap().putError(errorPath,
                    KFSKeyConstants.ERROR_ACCOUNTINGLINE_DELETERULE_INVALIDACCOUNT, errorParams);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Deletes the source or target accountingLine with the given index from the given form. Assumes that the rule-
     * and form-validation have already occurred.
     *
     * @param isSource
     * @param financialDocumentForm
     * @param deleteIndex
     */
    protected void deleteAccountingLine(
            final boolean isSource, final KualiAccountingDocumentFormBase financialDocumentForm,
            final int deleteIndex) {
        if (isSource) {
            financialDocumentForm.getFinancialDocument().getSourceAccountingLines().remove(deleteIndex);

        } else {
            financialDocumentForm.getFinancialDocument().getTargetAccountingLines().remove(deleteIndex);
        }
        // update the doc total
        final AccountingDocument tdoc = (AccountingDocument) financialDocumentForm.getDocument();
        if (tdoc instanceof AmountTotaling) {
            financialDocumentForm.getDocument().getDocumentHeader()
                    .setFinancialDocumentTotalAmount(((AmountTotaling) tdoc).getTotalDollarAmount());
        }
    }

    /**
     * This action executes a call to upload CSV accounting line values as TargetAccountingLines for a given
     * transactional document. The "uploadAccountingLines()" method handles the multi-part request.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward uploadTargetLines(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        // call method that sourceform and destination list
        uploadAccountingLines(false, form);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * This action executes a call to upload CSV accounting line values as SourceAccountingLines for a given
     * transactional document. The "uploadAccountingLines()" method handles the multi-part request.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws FileNotFoundException
     * @throws IOException
     */
    public ActionForward uploadSourceLines(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws FileNotFoundException, IOException {
        LOG.info("Uploading source accounting lines");
        // call method that sourceform and destination list
        uploadAccountingLines(true, form);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * This method determines whether we are uploading source or target lines, and then calls uploadAccountingLines
     * directly on the document object. This method handles retrieving the actual upload file as an input stream into
     * the document.
     *
     * @param isSource
     * @param form
     * @throws FileNotFoundException
     * @throws IOException
     */
    protected void uploadAccountingLines(final boolean isSource, final ActionForm form) throws FileNotFoundException, IOException {
        final KualiAccountingDocumentFormBase tmpForm = (KualiAccountingDocumentFormBase) form;

        List importedLines = null;

        final AccountingDocument financialDocument = tmpForm.getFinancialDocument();
        final AccountingLineParser accountingLineParser = financialDocument.getAccountingLineParser();

        // import the lines
        String errorPathPrefix = null;
        try {
            if (isSource) {
                errorPathPrefix = KFSConstants.DOCUMENT_PROPERTY_NAME + "." + KFSConstants.SOURCE_ACCOUNTING_LINE_ERRORS;
                final FormFile sourceFile = tmpForm.getSourceFile();
                checkUploadFile(sourceFile);
                importedLines = accountingLineParser.importSourceAccountingLines(sourceFile.getFileName(),
                        sourceFile.getInputStream(), financialDocument);
            } else {
                errorPathPrefix = KFSConstants.DOCUMENT_PROPERTY_NAME + "." + KFSConstants.TARGET_ACCOUNTING_LINE_ERRORS;
                final FormFile targetFile = tmpForm.getTargetFile();
                checkUploadFile(targetFile);
                importedLines = accountingLineParser.importTargetAccountingLines(targetFile.getFileName(),
                        targetFile.getInputStream(), financialDocument);
            }
        } catch (final AccountingLineParserException e) {
            GlobalVariables.getMessageMap().putError(errorPathPrefix, e.getErrorKey(), e.getErrorParameters());
        }

        // add line to list for those lines which were successfully imported
        if (importedLines != null) {
            for (final Object importedLineObject : importedLines) {
                final AccountingLine importedLine = (AccountingLine) importedLineObject;
                insertAccountingLine(isSource, tmpForm, importedLine);
            }
        }
    }

    protected void checkUploadFile(final FormFile file) {
        if (file == null) {
            throw new AccountingLineParserException("invalid (null) upload file",
                    KFSKeyConstants.ERROR_UPLOADFILE_NULL);
        }
    }

    /**
     * This method will add a TargetAccountingLine to a FinancialDocument. This assumes that the user presses the add
     * button for a specific accounting line on the document and that the document is represented by a
     * FinancialDocumentFormBase. It first validates the line for data integrity and then checks appropriate business
     * rules.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward insertTargetLine(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiAccountingDocumentFormBase financialDocumentForm = (KualiAccountingDocumentFormBase) form;
        final TargetAccountingLine line = financialDocumentForm.getNewTargetLine();

        // before we check the regular rules we need to check the sales tax rules
        // TODO: Refactor rules so we no longer have to call this before a copy of the accountingLine
        boolean rulePassed = checkSalesTax((AccountingDocument) financialDocumentForm.getDocument(), line, false,
                true, 0);

        // check any business rules
        rulePassed &= SpringContext.getBean(KualiRuleService.class).applyRules(new AddAccountingLineEvent(
                KFSConstants.NEW_TARGET_ACCT_LINE_PROPERTY_NAME, financialDocumentForm.getDocument(), line));

        // if the rule evaluation passed, let's add it
        if (rulePassed) {
            // add accountingLine
            SpringContext.getBean(PersistenceService.class).refreshAllNonUpdatingReferences(line);
            insertAccountingLine(false, financialDocumentForm, line);

            // clear the used newTargetLine
            financialDocumentForm.setNewTargetLine(null);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * This action executes an insert of a SourceAccountingLine into a document only after validating the accounting
     * line and checking any appropriate business rules.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward insertSourceLine(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiAccountingDocumentFormBase financialDocumentForm = (KualiAccountingDocumentFormBase) form;
        final SourceAccountingLine line = financialDocumentForm.getNewSourceLine();

        boolean rulePassed = true;
        // DV acct line amount got error during form populate; should not insert this line.  KFSUPGRADE-847
        MessageMap msgMap = GlobalVariables.getMessageMap();
        if (msgMap.hasErrors() && msgMap.getErrorMessages().keySet().contains("newSourceLine.amount") && financialDocumentForm.getDocument() instanceof DisbursementVoucherDocument) {
            rulePassed = false;
        }
        // before we check the regular rules we need to check the sales tax rules
        // TODO: Refactor rules so we no longer have to call this before a copy of the accountingLine
        rulePassed &= checkSalesTax((AccountingDocument) financialDocumentForm.getDocument(), line, true,
                true, 0);
        // check any business rules
        rulePassed &= SpringContext.getBean(KualiRuleService.class).applyRules(new AddAccountingLineEvent(
                KFSConstants.NEW_SOURCE_ACCT_LINE_PROPERTY_NAME, financialDocumentForm.getDocument(), line));

        if (rulePassed) {
            // add accountingLine
            SpringContext.getBean(PersistenceService.class).refreshAllNonUpdatingReferences(line);
            insertAccountingLine(true, financialDocumentForm, line);

            // clear the used newTargetLine
            financialDocumentForm.setNewSourceLine(null);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Adds the given accountingLine to the appropriate form-related data structures.
     *
     * @param isSource
     * @param financialDocumentForm
     * @param line
     */
    protected void insertAccountingLine(
            final boolean isSource, final KualiAccountingDocumentFormBase financialDocumentForm,
            final AccountingLine line) {
        final AccountingDocument tdoc = financialDocumentForm.getFinancialDocument();
        if (isSource) {
            tdoc.addSourceAccountingLine((SourceAccountingLine) line);

            // add PK fields to sales tax if needed
            if (line.isSalesTaxRequired()) {
                populateSalesTax(line);
            }

            // Update the doc total
            if (tdoc instanceof AmountTotaling) {
                financialDocumentForm.getDocument().getDocumentHeader()
                        .setFinancialDocumentTotalAmount(((AmountTotaling) tdoc).getTotalDollarAmount());
            }
        } else {
            tdoc.addTargetAccountingLine((TargetAccountingLine) line);

            // add PK fields to sales tax if needed
            if (line.isSalesTaxRequired()) {
                populateSalesTax(line);
            }
        }
    }

    /**
     * TODO: remove this method once baseline accounting lines has been removed
     */
    protected List deepCopyAccountingLinesList(final List originals) {
        if (originals == null) {
            return null;
        }
        final List copiedLines = new ArrayList();
        for (final Object original : originals) {
            copiedLines.add(ObjectUtils.deepCopy((AccountingLine) original));
        }
        return copiedLines;
    }

    /**
     * This action changes the value of the hide field in the user interface so that when the page is rendered, the UI
     * knows to show all of the labels for each of the accounting line values.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward showDetails(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiAccountingDocumentFormBase tmpForm = (KualiAccountingDocumentFormBase) form;
        tmpForm.setHideDetails(false);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * This method is triggered when the user toggles the show/hide button to "hide" thus making the UI render without
     * any of the accounting line labels/descriptions showing up underneath the values in the UI.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward hideDetails(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiAccountingDocumentFormBase tmpForm = (KualiAccountingDocumentFormBase) form;
        tmpForm.setHideDetails(true);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Takes care of storing the action form in the User session and forwarding to the balance inquiry report menu
     * action for a source accounting line.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward performBalanceInquiryForSourceLine(
            final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final SourceAccountingLine line = getSourceAccountingLine(form, request);
        return performBalanceInquiryForAccountingLine(mapping, form, request, line);
    }

    /**
     * Takes care of storing the action form in the User session and forwarding to the balance inquiry report menu
     * action for a target accounting line.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward performBalanceInquiryForTargetLine(
            final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final TargetAccountingLine line = getTargetAccountingLine(form, request);
        return performBalanceInquiryForAccountingLine(mapping, form, request, line);
    }

    /**
     * This method is a helper method that will return a source accounting line. The reason we're making it protected
     * in here is so that we can override this method in some of the modules. PurchasingActionBase is one of the
     * subclasses that will be overriding this, because in PurchasingActionBase, we'll need to get the source
     * accounting line using both an item index and an account index.
     *
     * @param form
     * @param request
     * @return
     */
    protected SourceAccountingLine getSourceAccountingLine(final ActionForm form, final HttpServletRequest request) {
        final int lineIndex = getSelectedLine(request);
        return (SourceAccountingLine) ObjectUtils.deepCopy(
                ((KualiAccountingDocumentFormBase) form).getFinancialDocument().getSourceAccountingLine(lineIndex));
    }

    protected TargetAccountingLine getTargetAccountingLine(final ActionForm form, final HttpServletRequest request) {
        final int lineIndex = getSelectedLine(request);
        return ((KualiAccountingDocumentFormBase) form).getFinancialDocument().getTargetAccountingLine(lineIndex);
    }

    /**
     * This method handles preparing all of the accounting line data so that it can be pushed up to the balance
     * inquiries for populating the search criteria of each.
     *
     * @param mapping
     * @param form
     * @param request
     * @param line
     * @return ActionForward
     */
    protected ActionForward performBalanceInquiryForAccountingLine(
            final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final AccountingLine line) {
        // build out base path for return location
        final String basePath = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(
                KFSConstants.APPLICATION_URL_KEY);

        // build out the actual form key that will be used to retrieve the form on refresh
        final String callerDocFormKey = GlobalVariables.getUserSession().addObjectWithGeneratedKey(form);

        // now add required parameters
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(KFSConstants.DISPATCH_REQUEST_PARAMETER, KFSConstants.START_METHOD);
        // need this next param b/c the lookup's return back will overwrite the original doc form key
        parameters.put(KFSConstants.BALANCE_INQUIRY_REPORT_MENU_CALLER_DOC_FORM_KEY, callerDocFormKey);
        parameters.put(KFSConstants.DOC_FORM_KEY, callerDocFormKey);
        parameters.put(KFSConstants.BACK_LOCATION, basePath + mapping.getPath() + ".do");

        if (line.getPostingYear() != null) {
            parameters.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, line.getPostingYear().toString());
        }
        if (StringUtils.isNotBlank(line.getReferenceOriginCode())) {
            parameters.put("referenceOriginCode", line.getReferenceOriginCode());
        }
        if (StringUtils.isNotBlank(line.getReferenceNumber())) {
            parameters.put("referenceNumber", line.getReferenceNumber());
        }
        if (StringUtils.isNotBlank(line.getReferenceTypeCode())) {
            parameters.put("referenceTypeCode", line.getReferenceTypeCode());
        }
        if (StringUtils.isNotBlank(line.getDebitCreditCode())) {
            parameters.put("debitCreditCode", line.getDebitCreditCode());
        }
        if (StringUtils.isNotBlank(line.getChartOfAccountsCode())) {
            parameters.put("chartOfAccountsCode", line.getChartOfAccountsCode());
        }
        if (StringUtils.isNotBlank(line.getAccountNumber())) {
            parameters.put("accountNumber", line.getAccountNumber());
        }
        if (StringUtils.isNotBlank(line.getFinancialObjectCode())) {
            parameters.put("financialObjectCode", line.getFinancialObjectCode());
        }
        if (StringUtils.isNotBlank(line.getSubAccountNumber())) {
            parameters.put("subAccountNumber", line.getSubAccountNumber());
        }
        if (StringUtils.isNotBlank(line.getFinancialSubObjectCode())) {
            parameters.put("financialSubObjectCode", line.getFinancialSubObjectCode());
        }
        if (StringUtils.isNotBlank(line.getProjectCode())) {
            parameters.put("projectCode", line.getProjectCode());
        }
        if (StringUtils.isNotBlank(getObjectTypeCodeFromLine(line))) {
            if (StringUtils.isNotBlank(line.getObjectTypeCode())) {
                parameters.put("objectTypeCode", line.getObjectTypeCode());
            } else {
                line.refreshReferenceObject("objectCode");
                parameters.put("objectTypeCode", line.getObjectCode().getFinancialObjectTypeCode());
            }
        }

        final String lookupUrl = UrlFactory.parameterizeUrl(basePath + "/" +
                                                            KFSConstants.BALANCE_INQUIRY_REPORT_MENU_ACTION, parameters);

        // register that we're going to come back w/ to this form w/ a refresh methodToCall
        ((KualiAccountingDocumentFormBase) form).registerEditableProperty(KRADConstants.DISPATCH_REQUEST_PARAMETER);

        return new ActionForward(lookupUrl, true);
    }

    /**
     * A hook so that most accounting lines - which don't have object types - can have their object type codes used in
     * balance inquiries
     *
     * @param line the line to get the object type code from
     * @return the object type code the line would use
     */
    protected String getObjectTypeCodeFromLine(final AccountingLine line) {
        line.refreshReferenceObject("objectCode");
        return line.getObjectCode().getFinancialObjectTypeCode();
    }

    @Override
    public ActionForward save(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiAccountingDocumentFormBase tmpForm = (KualiAccountingDocumentFormBase) form;

        //KFSPTS-1735
        boolean passed = SpringContext.getBean(KualiRuleService.class).applyRules(new SaveDocumentEvent(tmpForm.getFinancialDocument()));
        if (tmpForm.getFinancialDocument().getDocumentHeader().getWorkflowDocument().isEnroute() && passed) {
            SpringContext.getBean(CUFinancialSystemDocumentService.class).checkAccountingLinesForChanges((AccountingDocument) tmpForm.getFinancialDocument());
        }
        //KFSPTS-1735
        
        final ActionForward forward = super.save(mapping, form, request, response);

        // need to check on sales tax for all the accounting lines
        checkSalesTaxRequiredAllLines(tmpForm, tmpForm.getFinancialDocument().getSourceAccountingLines());
        checkSalesTaxRequiredAllLines(tmpForm, tmpForm.getFinancialDocument().getTargetAccountingLines());
        return forward;
    }

    @Override
    public ActionForward approve(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiAccountingDocumentFormBase tmpForm = (KualiAccountingDocumentFormBase) form;

        final ActionForward forward = super.approve(mapping, form, request, response);

        if (GlobalVariables.getMessageMap().hasNoErrors()) {
	        // KFSPTS-1735
	 	     SpringContext.getBean(CUFinancialSystemDocumentService.class).checkAccountingLinesForChanges((AccountingDocument) tmpForm.getFinancialDocument());
	 	     // KFSPTS-1735
        }
        
        // need to check on sales tax for all the accounting lines
        checkSalesTaxRequiredAllLines(tmpForm, tmpForm.getFinancialDocument().getSourceAccountingLines());
        checkSalesTaxRequiredAllLines(tmpForm, tmpForm.getFinancialDocument().getTargetAccountingLines());

        return forward;
    }

    @Override
    public ActionForward route(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiAccountingDocumentFormBase tmpForm = (KualiAccountingDocumentFormBase) form;
        //   this.applyCapitalAssetInformation(tmpForm);

        final ActionForward forward = super.route(mapping, form, request, response);

        checkSalesTaxRequiredAllLines(tmpForm, tmpForm.getFinancialDocument().getSourceAccountingLines());
        checkSalesTaxRequiredAllLines(tmpForm, tmpForm.getFinancialDocument().getTargetAccountingLines());

        return forward;
    }

    @Override
    public ActionForward blanketApprove(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiAccountingDocumentFormBase tmpForm = (KualiAccountingDocumentFormBase) form;

        checkSalesTaxRequiredAllLines(tmpForm, tmpForm.getFinancialDocument().getSourceAccountingLines());
        checkSalesTaxRequiredAllLines(tmpForm, tmpForm.getFinancialDocument().getTargetAccountingLines());

        return super.blanketApprove(mapping, form, request, response);
    }

    /**
     * Encapsulate the rule check so we can call it from multiple places
     *
     * @param document
     * @param line
     * @return true if sales is either not required or it contains sales tax
     */
    protected boolean checkSalesTax(
            final AccountingDocument document, final AccountingLine line, final boolean source, final boolean newLine,
            final int index) {
        boolean passed = true;
        if (isSalesTaxRequired(document, line)) {
            // then set the salesTaxRequired on the accountingLine
            line.setSalesTaxRequired(true);
            populateSalesTax(line);
            // check to see if the sales tax info has been put in
            passed = isValidSalesTaxEntered(line, source, newLine, index);
        } else {
            //we do not need the saleTax bo for the line otherwise validations will fail.
            line.setSalesTax(null);
        }
        return passed;
    }

    /**
     * This method checks to see if this doctype needs sales tax If it does then it checks to see if the account and
     * object code require sales tax If it does then it returns true. Note - this is hackish as we shouldn't have to
     * call rules directly from the action class But we need to in this instance because we are copying the lines
     * before calling rules and need a way to modify them before they go on
     *
     * @param accountingLine
     * @return true if sales tax check is needed, false otherwise
     */
    protected boolean isSalesTaxRequired(final AccountingDocument financialDocument, final AccountingLine accountingLine) {
        boolean required = false;
        final String docType = SpringContext.getBean(DataDictionaryService.class)
                .getDocumentTypeNameByClass(financialDocument.getClass());
        // first we need to check just the doctype to see if it needs the sales tax check
        // apply the rule, see if it fails
        final ParameterEvaluatorService parameterEvaluatorService = SpringContext.getBean(ParameterEvaluatorService.class);
        final ParameterEvaluator docTypeSalesTaxCheckEvaluator =
                parameterEvaluatorService.getParameterEvaluator(KfsParameterConstants.FINANCIAL_PROCESSING_DOCUMENT.class,
                        FPParameterConstants.SALES_TAX_DOCUMENT_TYPES, docType);
        if (docTypeSalesTaxCheckEvaluator.evaluationSucceeds()) {
            required = true;
        }

        // second we need to check the account and object code combination to see if it needs sales tax
        if (required) {
            // get the object code and account
            final String objCd = accountingLine.getFinancialObjectCode();
            final String account = accountingLine.getAccountNumber();
            if (StringUtils.isNotEmpty(objCd) && StringUtils.isNotEmpty(account)) {
                final String compare = account + ":" + objCd;
                final ParameterEvaluator salesTaxApplicableAcctAndObjectEvaluator =
                        parameterEvaluatorService.getParameterEvaluator(KfsParameterConstants.FINANCIAL_PROCESSING_DOCUMENT.class,
                                FPParameterConstants.SALES_TAX_ACCOUNTS, compare);
                if (!salesTaxApplicableAcctAndObjectEvaluator.evaluationSucceeds()) {
                    required = false;
                }
            } else {
                // the two fields are currently empty and we don't need to check yet
                required = false;
            }
        }
        return required;
    }

    /**
     * This method checks to see if the sales tax information was put into the accounting line
     *
     * @param accountingLine
     * @return true if entered correctly, false otherwise
     */
    protected boolean isValidSalesTaxEntered(final AccountingLine accountingLine, final boolean source, final boolean newLine, final int index) {
        boolean valid = true;
        final BusinessObjectService boService = SpringContext.getBean(BusinessObjectService.class);
        final String objCd = accountingLine.getFinancialObjectCode();
        final String account = accountingLine.getAccountNumber();
        final SalesTax salesTax = accountingLine.getSalesTax();
        String pathPrefix = "";
        if (source && !newLine) {
            pathPrefix = "document." + KFSConstants.EXISTING_SOURCE_ACCT_LINE_PROPERTY_NAME + "[" + index + "]";
        } else if (!source && !newLine) {
            pathPrefix = "document." + KFSConstants.EXISTING_TARGET_ACCT_LINE_PROPERTY_NAME + "[" + index + "]";
        } else if (source && newLine) {
            pathPrefix = KFSConstants.NEW_SOURCE_ACCT_LINE_PROPERTY_NAME;
        } else if (!source && newLine) {
            pathPrefix = KFSConstants.NEW_TARGET_ACCT_LINE_PROPERTY_NAME;
        }
        GlobalVariables.getMessageMap().addToErrorPath(pathPrefix);
        if (ObjectUtils.isNull(salesTax)) {
            valid = false;
            GlobalVariables.getMessageMap().putError("salesTax.chartOfAccountsCode",
                    KFSKeyConstants.ERROR_DOCUMENT_ACCOUNTING_LINE_SALES_TAX_REQUIRED, account, objCd);
        } else {

            if (StringUtils.isBlank(salesTax.getChartOfAccountsCode())) {
                valid = false;
                GlobalVariables.getMessageMap().putError("salesTax.chartOfAccountsCode",
                        KFSKeyConstants.ERROR_REQUIRED, "Chart of Accounts");
            }
            if (StringUtils.isBlank(salesTax.getAccountNumber())) {
                valid = false;
                GlobalVariables.getMessageMap().putError("salesTax.accountNumber",
                        KFSKeyConstants.ERROR_REQUIRED, "Account Number");
            }
            if (salesTax.getFinancialDocumentGrossSalesAmount() == null) {
                valid = false;
                GlobalVariables.getMessageMap().putError("salesTax.financialDocumentGrossSalesAmount",
                        KFSKeyConstants.ERROR_REQUIRED, "Gross Sales Amount");
            }
            if (salesTax.getFinancialDocumentTaxableSalesAmount() == null) {
                valid = false;
                GlobalVariables.getMessageMap().putError("salesTax.financialDocumentTaxableSalesAmount",
                        KFSKeyConstants.ERROR_REQUIRED, "Taxable Sales Amount");
            }
            if (salesTax.getFinancialDocumentSaleDate() == null) {
                valid = false;
                GlobalVariables.getMessageMap().putError("salesTax.financialDocumentSaleDate",
                        KFSKeyConstants.ERROR_REQUIRED, "Sale Date");
            }
            if (StringUtils.isNotBlank(salesTax.getChartOfAccountsCode())
                    && StringUtils.isNotBlank(salesTax.getAccountNumber())) {
                if (boService.getReferenceIfExists(salesTax, "account") == null) {
                    valid = false;
                    GlobalVariables.getMessageMap().putError("salesTax.accountNumber",
                            KFSKeyConstants.ERROR_DOCUMENT_ACCOUNTING_LINE_SALES_TAX_INVALID_ACCOUNT,
                            salesTax.getChartOfAccountsCode(), salesTax.getAccountNumber());

                }
            }
            if (!valid) {
                GlobalVariables.getMessageMap().putError("salesTax.chartOfAccountsCode",
                        KFSKeyConstants.ERROR_DOCUMENT_ACCOUNTING_LINE_SALES_TAX_REQUIRED, account, objCd);
            }
        }
        GlobalVariables.getMessageMap().removeFromErrorPath(pathPrefix);
        return valid;
    }

    /**
     * This method removes the sales tax information from a line that no longer requires it
     *
     * @param accountingLine
     */
    protected void removeSalesTax(final AccountingLine accountingLine) {
        final SalesTax salesTax = accountingLine.getSalesTax();
        if (ObjectUtils.isNotNull(salesTax)) {
            accountingLine.setSalesTax(null);
        }
    }

    /**
     * This method checks to see if the given accounting needs sales tax and if it does it sets the salesTaxRequired
     * variable on the line If it doesn't and it has it then it removes the sales tax information from the line This
     * method is called from the execute() on all accounting lines that have been edited or lines that have already
     * been added to the document, not on new lines
     *
     * @param transDoc
     * @param formLine
     * @param source
     * @param newLine
     * @param index
     */
    protected void handleSalesTaxRequired(
            final AccountingDocument transDoc, final AccountingLine formLine, final boolean source,
            final boolean newLine, final int index) {
        final boolean salesTaxRequired = isSalesTaxRequired(transDoc, formLine);
        if (salesTaxRequired) {
            formLine.setSalesTaxRequired(true);
            populateSalesTax(formLine);
        } else if (hasSalesTaxBeenEntered(formLine, source, newLine, index)) {
            // remove it if it has been added but is no longer required
            removeSalesTax(formLine);
            formLine.setSalesTax(null);
        }

        if (!salesTaxRequired) {
            formLine.setSalesTax(null);
        }
    }

    protected boolean hasSalesTaxBeenEntered(
            final AccountingLine accountingLine, final boolean source, final boolean newLine,
            final int index) {
        boolean entered = true;
        final SalesTax salesTax = accountingLine.getSalesTax();
        if (ObjectUtils.isNull(salesTax)) {
            return false;
        }
        if (StringUtils.isBlank(salesTax.getChartOfAccountsCode())) {
            entered = false;
        }
        if (StringUtils.isBlank(salesTax.getAccountNumber())) {
            entered = false;
        }
        if (salesTax.getFinancialDocumentGrossSalesAmount() == null) {
            entered = false;
        }
        if (salesTax.getFinancialDocumentTaxableSalesAmount() == null) {
            entered = false;
        }
        if (salesTax.getFinancialDocumentSaleDate() == null) {
            entered = false;
        }
        return entered;
    }

    /**
     * This method is called from the createDocument and processes through all the accouting lines and checks to see
     * if they need sales tax fields
     *
     * @param kualiDocumentFormBase
     * @param baselineAcctingLines
     */
    protected void handleSalesTaxRequiredAllLines(
            final KualiDocumentFormBase kualiDocumentFormBase,
            final List<AccountingLine> baselineAcctingLines) {
        final AccountingDocument accoutingDocument = (AccountingDocument) kualiDocumentFormBase.getDocument();
        int index = 0;
        for (final AccountingLine accountingLine : baselineAcctingLines) {
            boolean source = false;
            if (accountingLine.isSourceAccountingLine()) {
                source = true;
            }
            handleSalesTaxRequired(accoutingDocument, accountingLine, source, false, index);
            index++;
        }

    }

    protected boolean checkSalesTaxRequiredAllLines(
            final KualiDocumentFormBase kualiDocumentFormBase,
            final List<AccountingLine> baselineAcctingLines) {
        final AccountingDocument accoutingDocument = (AccountingDocument) kualiDocumentFormBase.getDocument();
        boolean passed = true;
        int index = 0;
        for (final AccountingLine accountingLine : baselineAcctingLines) {
            boolean source = false;
            if (accountingLine.isSourceAccountingLine()) {
                source = true;
            }
            passed &= checkSalesTax(accoutingDocument, accountingLine, source, false, index);
            index++;
        }
        return passed;
    }

    /**
     * This method refreshes the sales tax fields on a refresh or tab toggle so that all the information that was
     * there before is still there after a state change
     *
     * @param form
     */
    protected void refreshSalesTaxInfo(final ActionForm form) {
        final KualiAccountingDocumentFormBase accountingForm = (KualiAccountingDocumentFormBase) form;
        final AccountingDocument document = (AccountingDocument) accountingForm.getDocument();
        final List sourceLines = document.getSourceAccountingLines();
        final List targetLines = document.getTargetAccountingLines();
        handleSalesTaxRequiredAllLines(accountingForm, sourceLines);
        handleSalesTaxRequiredAllLines(accountingForm, targetLines);

        final AccountingLine newTargetLine = accountingForm.getNewTargetLine();
        final AccountingLine newSourceLine = accountingForm.getNewSourceLine();
        if (newTargetLine != null) {
            handleSalesTaxRequired(document, newTargetLine, false, true, 0);
        }
        if (newSourceLine != null) {
            handleSalesTaxRequired(document, newSourceLine, true, true, 0);
        }
    }

    /**
     * This method populates the sales tax for a given accounting line with the appropriate primary key fields from
     * the accounting line since OJB won't do it automatically for us
     *
     * @param line
     */
    protected void populateSalesTax(final AccountingLine line) {
        final SalesTax salesTax = line.getSalesTax();

        if (ObjectUtils.isNotNull(salesTax)) {
            salesTax.setDocumentNumber(line.getDocumentNumber());
            salesTax.setFinancialDocumentLineTypeCode(line.getFinancialDocumentLineTypeCode());
            salesTax.setFinancialDocumentLineNumber(line.getSequenceNumber());
        }
    }

    @Override
    public ActionForward performLookup(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        // parse out the business object name from our methodToCall parameter
        final String boClassName = extractLookupBusinessObjectClassName(request);

        if (!StringUtils.equals(boClassName, GeneralLedgerPendingEntry.class.getName())) {
            return super.performLookup(mapping, form, request, response);
        }

        String path = super.performLookup(mapping, form, request, response).getPath();
        path = path.replaceFirst(KFSConstants.LOOKUP_ACTION, KFSConstants.GL_MODIFIED_INQUIRY_ACTION);

        return new ActionForward(path, true);
    }

    protected static String extractLookupBusinessObjectClassName(final HttpServletRequest request) {
        final String fullParameter = (String) request.getAttribute(KFSConstants.METHOD_TO_CALL_ATTRIBUTE);
        return StringUtils.substringBetween(
                fullParameter,
                KFSConstants.METHOD_TO_CALL_BOPARM_LEFT_DEL,
                KFSConstants.METHOD_TO_CALL_BOPARM_RIGHT_DEL);
    }

}
