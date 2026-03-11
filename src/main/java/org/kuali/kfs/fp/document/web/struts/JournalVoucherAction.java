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
package org.kuali.kfs.fp.document.web.struts;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.coa.businessobject.BalanceType;
import org.kuali.kfs.coa.service.BalanceTypeService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.web.format.CurrencyFormatter;
import org.kuali.kfs.fp.FPKeyConstants;
import org.kuali.kfs.fp.businessobject.VoucherAccountingLineHelper;
import org.kuali.kfs.fp.businessobject.VoucherAccountingLineHelperBase;
import org.kuali.kfs.fp.businessobject.VoucherSourceAccountingLine;
import org.kuali.kfs.fp.document.JournalVoucherDocument;
import org.kuali.kfs.fp.document.VoucherDocument;
import org.kuali.kfs.kns.question.ConfirmationQuestion;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ====
 * CU Customization: Reintroduce custom logic that forcibly executes the "changeBalanceType" methodToCall
 *                   when the form auto-submits in response to a balance type change.
 * ====
 * 
 * This class piggy backs on all of the functionality in the FinancialSystemTransactionalDocumentActionBase but is
 * necessary for this document type. The Journal Voucher is unique in that it defines several fields that aren't
 * typically used by the other financial transaction processing eDocs (i.e. external system fields, object type override,
 * credit and debit amounts).
 */
public class JournalVoucherAction extends VoucherAction {

    // used to determine which way the change balance type action is switching these are local constants only used
    // within this action class these should not be used outside of this class
    protected static final int CREDIT_DEBIT_TO_SINGLE_AMT_MODE = 0;
    protected static final int SINGLE_AMT_TO_CREDIT_DEBIT_MODE = 1;
    protected static final int EXT_ENCUMB_TO_NON_EXT_ENCUMB = 0;
    protected static final int NON_EXT_ENCUMB_TO_EXT_ENCUMB = 1;
    protected static final int NO_MODE_CHANGE = -1;

    /**
     * Overrides the parent and then calls the super method after building the array lists for valid accounting periods
     * and balance types.
     */
    @Override
    public ActionForward execute(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final JournalVoucherForm journalVoucherForm = (JournalVoucherForm) form;

        populateBalanceTypeOneDocument(journalVoucherForm);

        /*
         * CU Customization: Reintroduced custom handling, but adjusted it to instead override the methodToCall property
         *                   on the ActionForm so that super.execute() can still be invoked.
         */
        // now check to see if the balance type was changed and if so, we want to
        // set the method to call so that the appropriate action can be invoked
        // had to do it this way b/c the changing of the drop down causes the page to re-submit
        // and couldn't use a hidden field called "methodToCall" b/c it screwed everything up
        if (StringUtils.isNotBlank(journalVoucherForm.getOriginalBalanceType())
                && !journalVoucherForm.getSelectedBalanceType().getCode().equals(journalVoucherForm.getOriginalBalanceType())) {
            journalVoucherForm.setMethodToCall(KFSConstants.CHANGE_JOURNAL_VOUCHER_BALANCE_TYPE_METHOD);
        }
        /*
         * End CU Customization
         */

        return super.execute(mapping, journalVoucherForm, request, response);
    }

    /**
     * Overrides the parent to first prompt the user appropriately to make sure that they want to submit and out of balance
     * document, then calls super's route method.
     */
    @Override
    public ActionForward route(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        // process the question but we need to make sure there are lines and then check to see if it's not balanced
        final VoucherDocument vDoc = ((VoucherForm) form).getVoucherDocument();

        final KualiDecimal balance = vDoc.getCreditTotal().subtract(vDoc.getDebitTotal());
        if (vDoc.getSourceAccountingLines().size() > 0 && balance.compareTo(KualiDecimal.ZERO) != 0) {
            // it's not in "balance"
            final ActionForward returnForward = processRouteOutOfBalanceDocumentConfirmationQuestion(mapping, form, request, response);

            // if not null, then the question component either has control of the flow and needs to ask its questions
            // or the person chose the "cancel" or "no" button; otherwise we have control
            if (returnForward != null) {
                return returnForward;
            }
        }
        // now call the route method
        return super.route(mapping, form, request, response);
    }

    /**
     * This method handles grabbing the values from the form and pushing them into the document appropriately.
     *
     * @param journalVoucherForm
     */
    protected void populateBalanceTypeOneDocument(final JournalVoucherForm journalVoucherForm) {
        final String selectedBalanceTypeCode = journalVoucherForm.getSelectedBalanceType().getCode();
        final BalanceType selectedBalanceType = getPopulatedBalanceTypeInstance(selectedBalanceTypeCode);
        journalVoucherForm.getJournalVoucherDocument().setBalanceTypeCode(selectedBalanceTypeCode);
        journalVoucherForm.getJournalVoucherDocument().setBalanceType(selectedBalanceType);
        // set the fully populated balance type object into the form's selected balance type
        journalVoucherForm.setSelectedBalanceType(selectedBalanceType);
    }


    /**
     * Overrides to call super, and then to repopulate the credit/debit amounts b/c the credit/debit code might change
     * during a JV error correction.
     */
    @Override
    public ActionForward correct(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final ActionForward actionForward = super.correct(mapping, form, request, response);

        final JournalVoucherDocument jvDoc = (JournalVoucherDocument) ((JournalVoucherForm) form).getDocument();

        jvDoc.refreshReferenceObject(KFSPropertyConstants.BALANCE_TYPE);
        // only repopulate if this is a JV that was entered in debit/credit mode
        if (jvDoc.getBalanceType().isFinancialOffsetGenerationIndicator()) {
            // now make sure to repopulate credit/debit amounts
            populateAllVoucherAccountingLineHelpers((JournalVoucherForm) form);
        }

        return actionForward;
    }

    /**
     * This method processes a change in the balance type for a Journal Voucher document - from either a offset
     * generation balance type to a non-offset generation balance type or visa-versa.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward changeBalanceType(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final JournalVoucherForm journalVoucherForm = (JournalVoucherForm) form;

        // figure out which way the balance type is changing
        final int balanceTypeAmountChangeMode = determineBalanceTypeAmountChangeMode(journalVoucherForm);
        final int balanceTypeExternalEncumbranceChangeMode = determineBalanceTypeEncumbranceChangeMode(journalVoucherForm);

        // process the question
        if (balanceTypeAmountChangeMode != NO_MODE_CHANGE || balanceTypeExternalEncumbranceChangeMode != NO_MODE_CHANGE) {

            // deal with balance type changes first amount change
            if (balanceTypeAmountChangeMode == CREDIT_DEBIT_TO_SINGLE_AMT_MODE) {
                switchFromCreditDebitModeToSingleAmountMode(journalVoucherForm);
            } else if (balanceTypeAmountChangeMode == SINGLE_AMT_TO_CREDIT_DEBIT_MODE) {
                switchFromSingleAmountModeToCreditDebitMode(journalVoucherForm);
            }

            // then look to see if the external encumbrance was involved
            if (balanceTypeExternalEncumbranceChangeMode == EXT_ENCUMB_TO_NON_EXT_ENCUMB) {
                switchFromExternalEncumbranceModeToNonExternalEncumbrance(journalVoucherForm);
            }
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * This method will determine which balance type amount mode to switch to. A change in the balance type selection
     * will eventually invoke this mechanism, which looks at the old balance type value, and the new balance type value
     * to determine what the next mode is.
     *
     * @param journalVoucherForm
     * @throws Exception
     */
    protected int determineBalanceTypeAmountChangeMode(final JournalVoucherForm journalVoucherForm) throws Exception {
        int balanceTypeAmountChangeMode = NO_MODE_CHANGE;

        // retrieve fully populated balance type instances
        final BalanceType origBalType = getPopulatedBalanceTypeInstance(journalVoucherForm.getOriginalBalanceType());
        final BalanceType newBalType = getPopulatedBalanceTypeInstance(journalVoucherForm.getSelectedBalanceType().getCode());

        // figure out which ways we are switching the modes first deal with amount changes
        if (origBalType.isFinancialOffsetGenerationIndicator() && !newBalType.isFinancialOffsetGenerationIndicator()) {
            // credit/debit
            balanceTypeAmountChangeMode = CREDIT_DEBIT_TO_SINGLE_AMT_MODE;
        } else if (!origBalType.isFinancialOffsetGenerationIndicator() && newBalType.isFinancialOffsetGenerationIndicator()) {
            // single
            balanceTypeAmountChangeMode = SINGLE_AMT_TO_CREDIT_DEBIT_MODE;
        }

        return balanceTypeAmountChangeMode;
    }

    /**
     * This method will determine which balance type encumbrance mode to switch to. A change in the balance type
     * selection will eventually invoke this mechanism, which looks at the old balance type value, and the new balance
     * type value to determine what the next mode is.
     *
     * @param journalVoucherForm
     * @throws Exception
     */
    protected int determineBalanceTypeEncumbranceChangeMode(final JournalVoucherForm journalVoucherForm) throws Exception {
        int balanceTypeExternalEncumbranceChangeMode = NO_MODE_CHANGE;

        // retrieve fully populated balance type instances
        final BalanceType origBalType = getPopulatedBalanceTypeInstance(journalVoucherForm.getOriginalBalanceType());
        final BalanceType newBalType = getPopulatedBalanceTypeInstance(journalVoucherForm.getSelectedBalanceType().getCode());

        // then deal with external encumbrance changes
        if (origBalType.getCode().equals(KFSConstants.BALANCE_TYPE_EXTERNAL_ENCUMBRANCE)
                && !newBalType.getCode().equals(KFSConstants.BALANCE_TYPE_EXTERNAL_ENCUMBRANCE)) {
            balanceTypeExternalEncumbranceChangeMode = EXT_ENCUMB_TO_NON_EXT_ENCUMB;
        } else if (!origBalType.getCode().equals(KFSConstants.BALANCE_TYPE_EXTERNAL_ENCUMBRANCE)
                && newBalType.getCode().equals(KFSConstants.BALANCE_TYPE_EXTERNAL_ENCUMBRANCE)) {
            balanceTypeExternalEncumbranceChangeMode = NON_EXT_ENCUMB_TO_EXT_ENCUMB;
        }

        return balanceTypeExternalEncumbranceChangeMode;
    }

    /**
     * This method will fully populate a balance type given the passed in code, by calling the business object service
     * that retrieves the rest of the instances' information.
     *
     * @param balanceTypeCode
     * @return BalanceTyp
     */
    protected BalanceType getPopulatedBalanceTypeInstance(final String balanceTypeCode) {
        // now we have to get the code and the name of the original and new balance types
        return SpringContext.getBean(BalanceTypeService.class).getBalanceTypeByCode(balanceTypeCode);
    }

    /**
     * This method will clear out the source line values that aren't needed for the "Single Amount" mode.
     */
    protected void switchFromSingleAmountModeToCreditDebitMode(final JournalVoucherForm journalVoucherForm) {
        // going from single amount to credit/debit view so we want to blank out the amount and the extra "reference"
        // fields that the single amount view uses
        final JournalVoucherDocument jvDoc = (JournalVoucherDocument) journalVoucherForm.getTransactionalDocument();
        final List sourceLines = jvDoc.getSourceAccountingLines();
        final List helperLines = journalVoucherForm.getVoucherLineHelpers();
        // reset so we can add in fresh empty ones
        helperLines.clear();

        for (final Object sourceLine1 : sourceLines) {
            final VoucherSourceAccountingLine sourceLine = (VoucherSourceAccountingLine) sourceLine1;
            sourceLine.setAmount(KualiDecimal.ZERO);
            // default to debit
            sourceLine.setDebitCreditCode(KFSConstants.GL_DEBIT_CODE);

            // populate with a fresh new empty object
            helperLines.add(new VoucherAccountingLineHelperBase());
        }
    }

    /**
     * This method will clear out the extra "reference" fields that the external encumbrance balance type uses, but will
     * leave the amounts since we aren't changing the offset generation code stuff.
     *
     * @param journalVoucherForm
     */
    protected void switchFromExternalEncumbranceModeToNonExternalEncumbrance(final JournalVoucherForm journalVoucherForm) {
        // going from external encumbrance view to non external encumbrance view, so we want to blank out the extra
        // "reference" fields
        final JournalVoucherDocument jvDoc = (JournalVoucherDocument) journalVoucherForm.getTransactionalDocument();
        final List sourceLines = jvDoc.getSourceAccountingLines();

        for (final Object sourceLine1 : sourceLines) {
            final VoucherSourceAccountingLine sourceLine = (VoucherSourceAccountingLine) sourceLine1;
            // these three won't be needed in this mode
            sourceLine.setReferenceOriginCode(null);
            sourceLine.setReferenceNumber(null);
            sourceLine.setReferenceTypeCode(null);
        }
    }

    /**
     * This method will clear out the source line values that aren't needed for the "Credit/Debit" mode.
     *
     * @param journalVoucherForm
     */
    protected void switchFromCreditDebitModeToSingleAmountMode(final JournalVoucherForm journalVoucherForm) {
        // going from credit/debit view to single amount view so we don't need the debit and credit
        // indicator set any more and we need to blank out the amount values to zero
        final JournalVoucherDocument jvDoc = journalVoucherForm.getJournalVoucherDocument();
        final ArrayList sourceLines = (ArrayList) jvDoc.getSourceAccountingLines();
        final ArrayList helperLines = (ArrayList) journalVoucherForm.getVoucherLineHelpers();

        final KualiDecimal ZERO = new KualiDecimal("0.00");
        for (int i = 0; i < sourceLines.size(); i++) {
            final VoucherAccountingLineHelper helperLine = (VoucherAccountingLineHelper) helperLines.get(i);
            final SourceAccountingLine sourceLine = (SourceAccountingLine) sourceLines.get(i);
            sourceLine.setAmount(ZERO);
            // single sided is always debit
            sourceLine.setDebitCreditCode(KFSConstants.GL_DEBIT_CODE);

            // these two won't be needed in this mode
            helperLine.setCredit(null);
            helperLine.setDebit(null);
        }
    }

    /**
     * Overrides the parent to make sure that the JV specific accounting line helper forms are properly populated when
     * the document is first loaded. This first calls super, then populates the helper objects.
     */
    @Override
    protected void loadDocument(final KualiDocumentFormBase kualiDocumentFormBase) {
        super.loadDocument(kualiDocumentFormBase);
        final JournalVoucherForm journalVoucherForm = (JournalVoucherForm) kualiDocumentFormBase;

        // if the balance type is an offset generation balance type, then the user is able to enter the amount
        // as either a debit or a credit, otherwise, they only need to deal with the amount field
        final JournalVoucherDocument journalVoucherDocument = (JournalVoucherDocument) journalVoucherForm.getTransactionalDocument();
        if (journalVoucherDocument.getBalanceType().isFinancialOffsetGenerationIndicator()) {
            populateAllVoucherAccountingLineHelpers(journalVoucherForm);
            final KualiDecimal ZERO = new KualiDecimal("0.00");
            journalVoucherForm.setNewSourceLineCredit(ZERO);
            journalVoucherForm.setNewSourceLineDebit(ZERO);
        }

        // always wipe out the new source line
        journalVoucherForm.setNewSourceLine(null);

        // reload the balance type and accounting period selections since now we have data in the document bo
        populateSelectedJournalBalanceType(journalVoucherDocument, journalVoucherForm);
        populateSelectedAccountingPeriod(journalVoucherDocument, journalVoucherForm);
    }

    /**
     * This method grabs the value from the document bo and sets the selected balance type appropriately.
     *
     * @param journalVoucherDocument
     * @param journalVoucherForm
     */
    protected void populateSelectedJournalBalanceType(
            final JournalVoucherDocument journalVoucherDocument,
            final JournalVoucherForm journalVoucherForm) {
        journalVoucherForm.setSelectedBalanceType(journalVoucherDocument.getBalanceType());
        if (StringUtils.isNotBlank(journalVoucherDocument.getBalanceTypeCode())) {
            journalVoucherForm.setOriginalBalanceType(journalVoucherDocument.getBalanceTypeCode());
        }
    }

    /**
     * This helper method determines from the request object instance whether or not the user has been prompted about
     * the journal being out of balance. If they haven't, then the method will build the appropriate message given the
     * state of the document and return control to the question component so that the user receives the "yes"/"no" prompt.
     * If the question has been asked, the we evaluate the user's answer and direct the flow appropriately. If they
     * answer with a "No", then we build out a message stating that they chose that value and return an ActionForward of
     * a MAPPING_BASIC which keeps them at the same page that they were on. If they choose "Yes", then we return a null
     * ActionForward, which the calling action method recognizes as a "Yes" and continues on processing the "Route."
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    @Override
    protected ActionForward processRouteOutOfBalanceDocumentConfirmationQuestion(
            final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final JournalVoucherForm jvForm = (JournalVoucherForm) form;
        final JournalVoucherDocument jvDoc = jvForm.getJournalVoucherDocument();

        final String question = request.getParameter(KFSConstants.QUESTION_INST_ATTRIBUTE_NAME);
        final ConfigurationService kualiConfiguration = SpringContext.getBean(ConfigurationService.class);

        if (question == null) {
            // question hasn't been asked
            final String currencyFormattedDebitTotal = (String) new CurrencyFormatter().format(jvDoc.getDebitTotal());
            final String currencyFormattedCreditTotal = (String) new CurrencyFormatter().format(jvDoc.getCreditTotal());
            final String currencyFormattedTotal = (String) new CurrencyFormatter().format(jvDoc.getTotalDollarAmount());
            String message;
            jvDoc.refreshReferenceObject(KFSPropertyConstants.BALANCE_TYPE);
            if (jvDoc.getBalanceType().isFinancialOffsetGenerationIndicator()) {
                message = StringUtils.replace(kualiConfiguration
                                .getPropertyValueAsString(KFSKeyConstants.QUESTION_ROUTE_OUT_OF_BALANCE_JV_DOC), "{0}",
                        currencyFormattedDebitTotal);
                message = StringUtils.replace(message, "{1}", currencyFormattedCreditTotal);
            } else {
                message = StringUtils.replace(kualiConfiguration
                                .getPropertyValueAsString(KFSKeyConstants.QUESTION_ROUTE_OUT_OF_BALANCE_JV_DOC_SINGLE_AMT_MODE),
                        "{0}", currencyFormattedTotal);
            }

            // now transfer control over to the question component
            return performQuestionWithoutInput(mapping, form, request, response,
                    KFSConstants.JOURNAL_VOUCHER_ROUTE_OUT_OF_BALANCE_DOCUMENT_QUESTION, message,
                    KFSConstants.CONFIRMATION_QUESTION, KFSConstants.ROUTE_METHOD, "");
        } else {
            final String buttonClicked = request.getParameter(KFSConstants.QUESTION_CLICKED_BUTTON);
            if (KFSConstants.JOURNAL_VOUCHER_ROUTE_OUT_OF_BALANCE_DOCUMENT_QUESTION.equals(question)
                && ConfirmationQuestion.NO.equals(buttonClicked)) {
                KNSGlobalVariables.getMessageList().add(FPKeyConstants.MESSAGE_JV_CANCELLED_ROUTE);
                return mapping.findForward(KFSConstants.MAPPING_BASIC);
            }
        }
        return null;
    }

    /**
     * This action executes a call to upload CSV accounting line values as SourceAccountingLines for a given transactional
     * document. The "uploadAccountingLines()" method handles the multi-part request.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Override
    public ActionForward uploadSourceLines(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws FileNotFoundException, IOException {
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
    @Override
    protected void uploadAccountingLines(final boolean isSource, final ActionForm form) throws FileNotFoundException, IOException {
        final JournalVoucherForm jvForm = (JournalVoucherForm) form;
        // JournalVoucherAccountingLineParser needs a fresh BalanceType BO in the JournalVoucherDocument.
        jvForm.getJournalVoucherDocument().refreshReferenceObject(KFSPropertyConstants.BALANCE_TYPE);
        super.uploadAccountingLines(isSource, jvForm);
    }

}
