/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.module.ar.document.web.struts;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.businessobject.BalanceType;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ObjectType;
import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.group.Group;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.document.authorization.DocumentAuthorizer;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.kns.web.ui.ExtraButton;
import org.kuali.kfs.kns.web.ui.HeaderField;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.rest.responses.AccountingLineResponse;
import org.kuali.kfs.krad.rest.responses.AdHocRoutePersonResponse;
import org.kuali.kfs.krad.rest.responses.AdHocRouteWorkgroupResponse;
import org.kuali.kfs.krad.rest.responses.GlpeResponse;
import org.kuali.kfs.krad.rest.responses.NoteResponse;
import org.kuali.kfs.krad.rest.responses.PendingActionResponse;
import org.kuali.kfs.krad.rest.responses.RouteLogResponse;
import org.kuali.kfs.krad.rest.responses.TakenActionResponse;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.businessobject.Customer;
import org.kuali.kfs.module.ar.businessobject.InvoicePaidApplied;
import org.kuali.kfs.module.ar.businessobject.NonAppliedHolding;
import org.kuali.kfs.module.ar.document.CustomerInvoiceDocument;
import org.kuali.kfs.module.ar.document.PaymentApplicationAdjustmentDocument;
import org.kuali.kfs.module.ar.rest.resource.responses.PaymentApplicationAdjustmentInvoiceResponse;
import org.kuali.kfs.sys.FileUtil;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.OriginationCode;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.businessobject.service.DetailsUrlService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.web.struts.FinancialSystemTransactionalDocumentFormBase;
import org.kuali.kfs.sys.rest.presentation.Button;
import org.kuali.kfs.sys.rest.presentation.ButtonGroup;
import org.kuali.kfs.sys.rest.presentation.StandardButtonManager;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CU Customization:
 * File from KualiCo Patch release 2022-10-19 used for initial overlay to add 
 * backport of FINP-8894 from 2023-06-07 KualiCo patch release, KFSPTS-28661.
 * This file can be removed when we reach that financials release.
 */

/**
 * A Struts {@link org.apache.struts.action.ActionForm} instance associated with the "Payment Application
 * Adjustment" transactional document.
 */
public class PaymentApplicationAdjustmentForm extends FinancialSystemTransactionalDocumentFormBase {

    private static final Logger LOG = LogManager.getLogger();
    private static final String ADJUST_BUTTON_EXTRA_BUTTON_PROPERTY = "adjust";
    private static final String EMPTY_JSON = "{}";

    private BusinessObjectService businessObjectService;
    private PersonService personService;
    private String headerFieldsJson;
    private List<PaymentApplicationAdjustmentNonAppliedHolding> nonAppliedHoldings = new ArrayList<>();
    private List<CustomerInvoiceDocument> invoices = new ArrayList<>();
    private DateTimeService dateTimeService;

    @Override
    public void populate(final HttpServletRequest request) {
        // Register any editable properties. See KualiDocumentFormBase.java line 733
        registerEditableProperty("methodToCall");

        super.populate(request);
    }

    @Override
    public void reset(final ActionMapping mapping, final ServletRequest request) {
        super.reset(mapping, request);
    }

    @Override
    protected String getDefaultDocumentTypeName() {
        return ArConstants.ArDocumentTypeCodes.PAYMENT_APPLICATION_ADJUSTMENT_DOCUMENT_TYPE_CODE;
    }

    PaymentApplicationAdjustmentDocument getApplicationAdjustmentDocument() {
        return (PaymentApplicationAdjustmentDocument) getDocument();
    }

    @Override
    protected List<HeaderField> getStandardHeaderFields(final WorkflowDocument workflowDocument) {
        final List<HeaderField> standardHeaderFields = super.getStandardHeaderFields(workflowDocument);

        // The APPA doc does not use the old approach to displaying header fields. All we want is the url and not
        // the rest of the anchor information. So once the fields are generated, we find the initiator field and
        // replace its value with a URI
        final HeaderField initiatorInquiryHeaderField = standardHeaderFields.stream()
                .filter(field -> field.getId().equals(KRADConstants.DocumentFormHeaderFieldIds.DOCUMENT_INITIATOR))
                .findFirst()
                .orElse(null);
        if (initiatorInquiryHeaderField != null) {
            final String initiatorUrl = getPersonInquiryUrl(getInitiator());
            initiatorInquiryHeaderField.setNonLookupValue(initiatorUrl);
        }

        final HeaderField adjustee = getAdjusteeHeaderField();
        standardHeaderFields.add(adjustee);

        getAdjustmentHeaderField().ifPresent(standardHeaderFields::add);

        return standardHeaderFields;
    }

    private HeaderField getAdjusteeHeaderField() {
        final String ddAttributeEntryName =
                "DataDictionary.PaymentApplicationAdjustmentDocument.attributes.adjusteeDocumentNumber";
        final String displayValue = getPaymentApplicationAdjustmentDocument().getAdjusteeDocumentNumber();
        final String nonLookupValue = getDocumentHandlerUrl(displayValue);
        return new HeaderField(
                KRADConstants.DocumentFormHeaderFieldIds.DOCUMENT_ADJUSTEE,
                ddAttributeEntryName,
                displayValue,
                nonLookupValue
        );
    }

    private Optional<HeaderField> getAdjustmentHeaderField() {
        final String adjustmentDocumentNumber =
                getPaymentApplicationAdjustmentDocument().getAdjustmentDocumentNumber();

        return Optional.ofNullable(adjustmentDocumentNumber)
                .map(displayValue -> {
                    final String ddAttributeEntryName =
                            "DataDictionary.PaymentApplicationAdjustmentDocument.attributes.adjustmentDocumentNumber";
                    final String nonLookupValue = getDocumentHandlerUrl(displayValue);
                    return new HeaderField(
                            KRADConstants.DocumentFormHeaderFieldIds.DOCUMENT_ADJUSTER,
                            ddAttributeEntryName,
                            displayValue,
                            nonLookupValue
                    );
                });
    }

    PaymentApplicationAdjustmentDocument getPaymentApplicationAdjustmentDocument() {
        return (PaymentApplicationAdjustmentDocument) getDocument();
    }

    private String urlForCustomer(final Customer customer) {
        final DetailsUrlService detailsUrlService = SpringContext.getBean(DetailsUrlService.class);
        return detailsUrlService.getDetailsUrl(customer, "customerNumber");
    }

    private String urlForGroup(final Group group) {
        final DetailsUrlService detailsUrlService = SpringContext.getBean(DetailsUrlService.class);
        return detailsUrlService.getDetailsUrl(group, "name");
    }

    private String urlForPerson(final Person person) {
        return getPersonInquiryUrl(person);
    }

    private void setupNonAppliedHoldingsWrapper() {
        nonAppliedHoldings.clear();

        final var documentNonAppliedHoldings = getPaymentApplicationAdjustmentDocument().getNonAppliedHoldings();
        if (documentNonAppliedHoldings != null) {
            nonAppliedHoldings = documentNonAppliedHoldings.stream().map(holding ->
                    addNonAppliedHoldingWithCustomer(holding.getCustomer(), holding.getFinancialDocumentLineAmount())
            ).collect(Collectors.toList());
        }
    }

    public PaymentApplicationAdjustmentNonAppliedHolding addNonAppliedHoldingWithCustomer(
            final Customer customer,
            final KualiDecimal amount) {
        final var nonAppliedHolding = new PaymentApplicationAdjustmentNonAppliedHolding(customer.getCustomerName(),
                customer.getCustomerNumber(), urlForCustomer(customer), amount);
        nonAppliedHoldings.add(nonAppliedHolding);
        return nonAppliedHolding;
    }

    public AdHocRouteWorkgroupResponse createAdHocRouteWorkgroupResponse(final Group group, final String action) {
        return new AdHocRouteWorkgroupResponse(
                group.getId(),
                group.getName(),
                group.getNamespaceCode(),
                action,
                urlForGroup(group)
        );
    }

    public NoteResponse createNoteResponse(final Note note) {
        final Attachment attachment = note.getAttachment();
        return new NoteResponse(
                note.getObjectId(),
                note.getNoteText(),
                note.getAuthorUniversal().getName(),
                getPersonInquiryUrl(note.getAuthorUniversal()),
                attachment != null ? attachment.getAttachmentFileName() : null,
                attachment != null ? attachment.getAttachmentFileSizeWithUnits() : null,
                note.getAttachmentLink(),
                note.getNotePostedTimestamp()
        );
    }

    private AccountingLineResponse createAccountingLineResponse(
            final SourceAccountingLine nonArAccountingLine
    ) {
        final String chartCodeInquiryUrl =
                getInquiryUrl(
                        Chart.class,
                        KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE + "=" +
                                nonArAccountingLine.getChartOfAccountsCode()
                );

        final String accountNumberInquiryUrl =
                getInquiryUrl(
                        Account.class,
                        KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE + "=" +
                                nonArAccountingLine.getChartOfAccountsCode()
                                + "&" + KFSPropertyConstants.ACCOUNT_NUMBER + "=" +
                                nonArAccountingLine.getAccountNumber()
                );

        final String subAccountNumberInquiryUrl =
                getInquiryUrl(
                        SubAccount.class,
                        KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE + "=" +
                                nonArAccountingLine.getChartOfAccountsCode()
                                + "&" + KFSPropertyConstants.ACCOUNT_NUMBER + "=" +
                                nonArAccountingLine.getAccountNumber()
                                + "&" + KFSPropertyConstants.SUB_ACCOUNT_NUMBER + "=" +
                                nonArAccountingLine.getSubAccountNumber()
                );

        final String objectInquiryUrl =
                getInquiryUrl(
                        ObjectCode.class,
                        KFSPropertyConstants.FINANCIAL_OBJECT_CODE + "=" +
                                nonArAccountingLine.getFinancialObjectCode()
                                + "&" + KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE + "=" +
                                nonArAccountingLine.getChartOfAccountsCode()
                );

        final String subObjectInquiryUrl =
                getInquiryUrl(
                        SubObjectCode.class,
                        KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE + "=" +
                                nonArAccountingLine.getFinancialSubObjectCode()
                                + "&" + KFSPropertyConstants.FINANCIAL_OBJECT_CODE + "=" +
                                nonArAccountingLine.getFinancialObjectCode()
                                + "&" + KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE + "=" +
                                nonArAccountingLine.getChartOfAccountsCode()
                );

        final String projectCodeInquiryUrl =
                getInquiryUrl(
                        ProjectCode.class,
                        KFSPropertyConstants.CODE + "=" + nonArAccountingLine.getProjectCode()
                );

        return new AccountingLineResponse(
                nonArAccountingLine.getSequenceNumber(),
                nonArAccountingLine.getChartOfAccountsCode(),
                chartCodeInquiryUrl,
                nonArAccountingLine.getAccountNumber(),
                accountNumberInquiryUrl,
                nonArAccountingLine.getSubAccountNumber(),
                subAccountNumberInquiryUrl,
                nonArAccountingLine.getFinancialObjectCode(),
                objectInquiryUrl,
                nonArAccountingLine.getFinancialSubObjectCode(),
                subObjectInquiryUrl,
                nonArAccountingLine.getProjectCode(),
                projectCodeInquiryUrl,
                nonArAccountingLine.getOrganizationReferenceId(),
                nonArAccountingLine.getFinancialDocumentLineDescription(),
                nonArAccountingLine.getAmount()
        );
    }

    public GlpeResponse createGlpeResponse(final GeneralLedgerPendingEntry glpe) {
        final String universityFiscalYearInquiryUrl = getInquiryUrl(SystemOptions.class, KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR + "=" + glpe.getUniversityFiscalYear().toString());
        final String chartCodeInquiryUrl = getInquiryUrl(Chart.class, KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE + "=" + glpe.getChartOfAccountsCode());
        final String accountNumberInquiryUrl = getInquiryUrl(Account.class,
                KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE + "=" + glpe.getChartOfAccountsCode()
                        + "&" + KFSPropertyConstants.ACCOUNT_NUMBER + "=" + glpe.getAccountNumber()
        );
        final String subAccountNumberInquiryUrl = getInquiryUrl(SubAccount.class,
                KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE + "=" + glpe.getChartOfAccountsCode()
                        + "&" + KFSPropertyConstants.ACCOUNT_NUMBER + "=" + glpe.getAccountNumber()
                        + "&" + KFSPropertyConstants.SUB_ACCOUNT_NUMBER + "=" + glpe.getSubAccountNumber()
        );
        final String objectInquiryUrl = getInquiryUrl(ObjectCode.class,
                KFSPropertyConstants.FINANCIAL_OBJECT_CODE + "=" + glpe.getFinancialObjectCode()
                        + "&" + KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR + "=" + glpe.getUniversityFiscalYear().toString()
                        + "&" + KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE + "=" + glpe.getChartOfAccountsCode()
        );
        final String subObjectInquiryUrl = getInquiryUrl(SubObjectCode.class,
                KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE + "=" + glpe.getFinancialSubObjectCode()
                        + "&" + KFSPropertyConstants.FINANCIAL_OBJECT_CODE + "=" + glpe.getFinancialObjectCode()
                        + "&" + KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR + "=" + glpe.getUniversityFiscalYear().toString()
                        + "&" + KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE + "=" + glpe.getChartOfAccountsCode()
        );
        final String projectCodeInquiryUrl = getInquiryUrl(ProjectCode.class, KFSPropertyConstants.CODE + "=" + glpe.getProjectCode());
        final String documentTypeCodeInquiryUrl = getInquiryUrl(DocumentType.class, KFSPropertyConstants.DOCUMENT_TYPE_ID + "=" + glpe.getFinancialSystemDocumentType().getDocumentTypeId());
        final String balanceTypeInquiryUrl = getInquiryUrl(BalanceType.class, KFSPropertyConstants.CODE + "=" + glpe.getFinancialBalanceTypeCode());
        final String objectTypeInquiryUrl = getInquiryUrl(ObjectType.class, KFSPropertyConstants.CODE + "=" + glpe.getFinancialObjectTypeCode());
        final String fiscalPeriodCodeInquiryUrl = getInquiryUrl(AccountingPeriod.class,
                KFSPropertyConstants.UNIVERSITY_FISCAL_PERIOD_CODE + "=" + glpe.getUniversityFiscalPeriodCode()
                + "&" + KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR + "=" + glpe.getUniversityFiscalYear().toString()
        );
        final String originCodeInquiryUrl = getInquiryUrl(OriginationCode.class, KFSPropertyConstants.FINANCIAL_SYSTEM_ORIGINATION_CODE + "=" + glpe.getFinancialSystemOriginationCode());
        final String documentNumberUrl = getDocumentUrl(glpe.getDocumentNumber());

        return new GlpeResponse(
                glpe.getUniversityFiscalYear().toString(),
                universityFiscalYearInquiryUrl,
                glpe.getChartOfAccountsCode(),
                chartCodeInquiryUrl,
                glpe.getAccountNumber(),
                accountNumberInquiryUrl,
                glpe.getSubAccountNumber(),
                subAccountNumberInquiryUrl,
                glpe.getFinancialObjectCode(),
                objectInquiryUrl,
                glpe.getFinancialSubObjectCode(),
                subObjectInquiryUrl,
                glpe.getProjectCode(),
                projectCodeInquiryUrl,
                glpe.getFinancialDocumentTypeCode(),
                documentTypeCodeInquiryUrl,
                glpe.getFinancialBalanceTypeCode(),
                balanceTypeInquiryUrl,
                glpe.getFinancialObjectTypeCode(),
                objectTypeInquiryUrl,
                glpe.getTransactionLedgerEntryAmount(),
                glpe.getTransactionDebitCreditCode(),

                glpe.getUniversityFiscalPeriodCode(),
                fiscalPeriodCodeInquiryUrl,
                glpe.getFinancialSystemOriginationCode(),
                originCodeInquiryUrl,
                glpe.getDocumentNumber(),
                documentNumberUrl,
                glpe.getTransactionLedgerEntryDescription(),
                glpe.getTransactionDate(),
                glpe.getOrganizationDocumentNumber(),
                glpe.getOrganizationReferenceId(),
                glpe.getReferenceFinancialDocumentTypeCode(),
                glpe.getReferenceFinancialSystemOriginationCode(),
                glpe.getReferenceFinancialDocumentNumber()
        );
    }

    private String getInquiryUrl(final Class boClass, final String keyValues) {
        final String basePath = getConfigurationService().getPropertyValueAsString(
                KFSConstants.APPLICATION_URL_KEY);

        return basePath + "/inquiry.do?methodToCall=start&businessObjectClassName=" + boClass.getName() + "&" + keyValues;
    }

    private String getDocumentUrl(final String docNumber) {
        return getConfigurationService().getPropertyValueAsString(KFSConstants.APPLICATION_URL_KEY) +
                "/DocHandler.do?docId=" + docNumber + "&command=displayDocSearchView";
    }

    public AdHocRoutePersonResponse createAdHocRoutePersonResponse(final Person person, final String action) {
        return new AdHocRoutePersonResponse(
                person.getPrincipalName(),
                String.format("%s %s", person.getFirstName(), person.getLastName()),
                action,
                urlForPerson(person)
        );
    }

    public TakenActionResponse createTakenActionResponse(final ActionTaken actionTaken) {
        final Person target = getPersonService().getPerson(actionTaken.getPrincipalId());
        final Person delegator = getPersonService().getPerson(actionTaken.getDelegatorPrincipalId());
        return new TakenActionResponse(
                actionTaken.getActionTakenLabel(),
                target,
                getPersonInquiryUrl(target),
                delegator,
                getPersonInquiryUrl(delegator),
                actionTaken.getActionDate(),
                actionTaken.getAnnotation()
        );
    }

    public PendingActionResponse createPendingActionResponse(final ActionRequest actionRequest) {
        String targetName = StringUtils.EMPTY;
        String targetUrl = StringUtils.EMPTY;
        if (actionRequest.isGroupRequest()) {
            final Group targetGroup = getBusinessObjectService().findBySinglePrimaryKey(Group.class,
                    actionRequest.getGroupId());
            if (targetGroup != null) {
                targetName = targetGroup.getName();
                targetUrl = urlForGroup(targetGroup);
            }
        } else {
            final Person targetPerson = getPersonService().getPerson(actionRequest.getPrincipalId());
            targetName = String.format("%s, %s", targetPerson.getLastName(), targetPerson.getFirstName());
            targetUrl = getPersonInquiryUrl(targetPerson);
        }

        return new PendingActionResponse(
                actionRequest.getActionRequestId(),
                actionRequest.getActionRequestedLabel(),
                actionRequest.isActive(),
                targetName,
                targetUrl,
                actionRequest.getCreateDate(),
                actionRequest.getAnnotation()
        );
    }

    public RouteLogResponse createRouteLogResponse() {
        final String documentId = StringUtils.defaultIfBlank(getDocumentId(), getDocId());
        if (StringUtils.isBlank(documentId)) {
            LOG.warn("No document id to create route log response");
            return null;
        }

        final DocumentRouteHeaderValue routeHeaderValue =
                KEWServiceLocator.getRouteHeaderService().getRouteHeader(documentId);

        final List<TakenActionResponse> takenActionsResponses = routeHeaderValue.getActionsTaken()
                .stream()
                .map(this::createTakenActionResponse)
                .collect(Collectors.toList());

        final List<PendingActionResponse> pendingActionsResponses = KEWServiceLocator.getActionRequestService()
                .getRootRequests(routeHeaderValue.getActionRequests())
                .stream()
                .filter(ActionRequest::isPending)
                .map(this::createPendingActionResponse)
                .collect(Collectors.toList());

        final Person initiator = getPersonService().getPerson(routeHeaderValue.getInitiatorPrincipalId());
        final String applicationUrl = getConfigurationService()
                .getPropertyValueAsString(KFSConstants.APPLICATION_URL_KEY);
        final DocumentType documentType = routeHeaderValue.getDocumentType();
        final String documentTypeUrl = String.format("%s/DocumentConfigurationView.do?methodToCall=start&documentTypeName=%s",
                applicationUrl, documentType.getName());
        return new RouteLogResponse(
                documentId,
                documentType,
                documentTypeUrl,
                initiator,
                getPersonInquiryUrl(initiator),
                routeHeaderValue.getDocRouteStatusLabel(),
                routeHeaderValue.getCurrentNodeNames(),
                routeHeaderValue.getCreateDate(),
                getDateTimeService().getUtilDate(routeHeaderValue.getDateLastModified()),
                routeHeaderValue.getApprovedDate(),
                routeHeaderValue.getFinalizedDate(),
                takenActionsResponses,
                pendingActionsResponses
        );
    }

    public String getDocumentActionsJson() {
        return serializeToJsonSafely(documentActions).orElse(EMPTY_JSON);
    }

    /**
     * @return A String containing the JSON representation of the buttons to display.
     * @throws JsonProcessingException If there is a serialization issue.
     */
    public String getButtonGroupJson() throws JsonProcessingException {
        final List<Button> extraButtons = convertExtraButtons();

        final Map<String, String> documentActions = getDocumentActions();
        final List<Button> standardButtons = StandardButtonManager.getStandardButtons(documentActions);

        final List<Button> buttons = ListUtils.union(extraButtons, standardButtons);
        final ButtonGroup buttonGroup = new ButtonGroup(buttons);

        final String buttonGroupJson = getJsonMapperWithJavaTime().writeValueAsString(buttonGroup);
        LOG.debug("getButtonGroupJson() - Returning : buttonGroupJson={}", buttonGroupJson);
        return buttonGroupJson;
    }

    private List<Button> convertExtraButtons() {
        final List<Button> buttons =
                getExtraButtons()
                        .stream()
                        .map(Button::from)
                        .collect(Collectors.toList());
        LOG.debug("convertExtraButtons() - Returning : buttons={}", buttons);
        return buttons;
    }

    @Override
    public List<ExtraButton> getExtraButtons() {
        // We are depending on a side-affect here.
        super.getExtraButtons();

        if (canAdjust()) {
            final Map<String, ExtraButton> buttonsMap = createButtonsMap();
            extraButtons.add(buttonsMap.get(ADJUST_BUTTON_EXTRA_BUTTON_PROPERTY));
        }

        return extraButtons;
    }

    /**
     * Creates a Map of all the buttons to appear on the Payment Application Form.
     *
     * @return A {@code Map} whose keys are the extraButtonProperty and whose values are @{code ExtraButton}s.
     */
    private static Map<String, ExtraButton> createButtonsMap() {
        final ExtraButton adjustButton = new ExtraButton();
        adjustButton.setExtraButtonProperty(ADJUST_BUTTON_EXTRA_BUTTON_PROPERTY);
        // TODO: Where does .gif come from?
        final String extraButtonSource =
                "${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY + "}buttonsmall_adjust.gif";
        adjustButton.setExtraButtonSource(extraButtonSource);
        adjustButton.setExtraButtonAltText("Adjust");

        return Map.of(ADJUST_BUTTON_EXTRA_BUTTON_PROPERTY, adjustButton);
    }

    /**
     * The Payment Application Adjustment can be adjusted if:
     * - The user has permission to initiate an APPA document.
     * - AND the PaymentApplicationAdjustment document does not have any previous OR pending adjustments
     * - AND the PaymentApplicationAdjustment document is Processed OR Final
     * - AND the PaymentApplicationAdjustment document has a non-zero amount applied to invoices and/or unapplied
     *
     * @return {@code true} if the document can be adjusted; otherwise, {@code false}.
     */
    private boolean canAdjust() {
        if (userCannotInitiateAnAdjustment()) {
            LOG.debug("canAdjust() - Exit; User does not have permission");
            return false;
        }
        final boolean canAdjust = noPreviousOrPendingAdjustments() && isFinalOrProcessed() && hasInvoiceAppliedsOrNonApplieds();
        LOG.debug("canAdjust() - Exit : canAdjust={}", canAdjust);
        return canAdjust;
    }

    /**
     * CU Customization: Backport FINP-8894 from 2023-06-07 KualiCo patch release, KFSPTS-28661
     */
    private boolean noPreviousOrPendingAdjustments() {
        final String adjustmentDocumentNumber = getPaymentApplicationAdjustmentDocument().getAdjustmentDocumentNumber();
        if (adjustmentDocumentNumber == null) {
            return true;
        }
        final DocumentHeader documentHeader =
                getPaymentApplicationAdjustmentDocument().getDocumentHeaderService().getDocumentHeaderById(adjustmentDocumentNumber);
        final WorkflowDocument workflowDocument = documentHeader.getWorkflowDocument();
        return workflowDocument.isDisapproved();
    }

    private boolean isFinalOrProcessed() {
        final PaymentApplicationAdjustmentDocument adjustmentDocument = getPaymentApplicationAdjustmentDocument();
        final WorkflowDocument workflowDocument = extractWorkFlowDocument(adjustmentDocument);
        return workflowDocument.isFinal() || workflowDocument.isProcessed();
    }

    private boolean hasInvoiceAppliedsOrNonApplieds() {
        final List<InvoicePaidApplied> invoicePaidApplieds = getPaymentApplicationAdjustmentDocument().getInvoicePaidApplieds();
        final List<NonAppliedHolding> nonAppliedHoldings = getPaymentApplicationAdjustmentDocument().getNonAppliedHoldings();
        return !(invoicePaidApplieds.isEmpty() && nonAppliedHoldings.isEmpty());
    }

    private static WorkflowDocument extractWorkFlowDocument(final Document document) {
        final var documentHeader = document.getDocumentHeader();
        return documentHeader.getWorkflowDocument();
    }

    private boolean userCannotInitiateAnAdjustment() {
        final Document document = getDocument();
        final DocumentAuthorizer documentAuthorizer =
                SpringContext.getBean(DocumentHelperService.class).getDocumentAuthorizer(document);
        final Person person = GlobalVariables.getUserSession().getPerson();
        return !documentAuthorizer.canInitiate(ArConstants.ArDocumentTypeCodes.PAYMENT_APPLICATION_ADJUSTMENT_DOCUMENT_TYPE_CODE, person);
    }

    public String getDocumentDescription() {
        return getPaymentApplicationAdjustmentDocument().getDocumentHeader().getDocumentDescription();
    }

    public String getDocumentExplanation() {
        return getPaymentApplicationAdjustmentDocument().getDocumentHeader().getExplanation();
    }

    public String getDocumentExplanationEscaped() {
        final String explanation = getPaymentApplicationAdjustmentDocument().getDocumentHeader().getExplanation();
        return StringUtils.isNotEmpty(explanation) ? explanation.replace("\n", "\\n") : explanation;
    }

    public String getOrganizationDocumentNumber() {
        return getPaymentApplicationAdjustmentDocument().getDocumentHeader().getOrganizationDocumentNumber();
    }

    public String getRouteLogResponseJson() {
        return serializeToJsonSafely(createRouteLogResponse()).orElse(EMPTY_JSON);
    }

    public long getAttachmentMaxFileSize() {
        final String attachmentSize = getParameterService().getParameterValueAsString(
                KFSConstants.CoreModuleNamespaces.KFS, KRADConstants.DetailTypes.DOCUMENT_DETAIL_TYPE,
                KRADConstants.ATTACHMENT_FILE_SIZE
        );

        return FileUtil.getBytes(attachmentSize);
    }

    /**
     * @return Non-applied holdings represented as a JSON String or {@code null}.
     */
    public String getNonAppliedHoldingsJson() {
        setupNonAppliedHoldingsWrapper();
        // Blindly using Optional.get() is generally discouraged. However, in this case, combined with
        // serializeToJsonSafely(...), it provides the currently desired behavior, with less code.
        return serializeToJsonSafely(nonAppliedHoldings).get();
    }

    public String getAdHocRouteWorkgroupJson() {
        try {
            final var workgroupResponses = new ArrayList<>();
            getAdHocRouteWorkgroups().forEach(workgroup -> {
                final var group = getBusinessObjectService().findBySinglePrimaryKey(Group.class, workgroup.getId());
                if (group != null) {
                    workgroupResponses.add(createAdHocRouteWorkgroupResponse(group, workgroup.getActionRequested()));
                }
            });
            return getJsonMapperWithJavaTime().writeValueAsString(workgroupResponses);
        } catch (final JsonProcessingException jpe) {
            LOG.error("Unable to serialize ad hoc route workgroups. Error {}", jpe::getMessage);
        }
        return null;
    }

    public String getAdHocRoutePersonJson() {
        try {
            final var personResponses = getAdHocRoutePersons().stream().map(recipient -> {
                final var person = getPersonService().getPersonByPrincipalName(recipient.getId());
                return createAdHocRoutePersonResponse(person, recipient.getActionRequested());
            }).collect(Collectors.toList());
            return getJsonMapperWithJavaTime().writeValueAsString(personResponses);
        } catch (final JsonProcessingException jpe) {
            LOG.error("Unable to serialize ad hoc route persons. Error {}", jpe::getMessage);
        }
        return null;
    }

    public String getAdHocActionRequestCodesJson() {
        try {
            return getJsonMapperWithJavaTime().writeValueAsString(getAdHocActionRequestCodes());
        } catch (final JsonProcessingException jpe) {
            LOG.error("Unable to serialize ad hoc request codes. Error {}", jpe::getMessage);
        }
        return null;
    }

    public String getNotesAndAttachmentsJson() {
        try {
            final List<Note> notes = getDocument().getNotes();
            final List<NoteResponse> noteResponses = notes.stream().map(this::createNoteResponse).collect(Collectors.toList());
            return getJsonMapperWithJavaTime().writeValueAsString(noteResponses);
        } catch (final JsonProcessingException jpe) {
            LOG.error("Unable to serialize notes. Error {}", jpe::getMessage);
        }
        return null;
    }

    public String getGeneralLedgerPendingEntriesJson() {
        final List<GeneralLedgerPendingEntry> glpes =
                getPaymentApplicationAdjustmentDocument().getGeneralLedgerPendingEntries();

        getPaymentApplicationAdjustmentDocument().fillInFiscalPeriodYear(glpes);

        final List<GlpeResponse> glpeResponses =
                glpes.stream()
                        .map(this::createGlpeResponse)
                        .collect(Collectors.toList());

        return serializeToJsonSafely(glpeResponses).orElse("{}");
    }

    public PaymentApplicationAdjustmentInvoiceResponse createInvoiceResponse(final CustomerInvoiceDocument invoice) {
        final List<InvoicePaidApplied> paidAppliedsForInvoice = getApplicationAdjustmentDocument().getInvoicePaidApplieds()
                .stream()
                .filter(invoicePaidApplied -> invoicePaidApplied.getFinancialDocumentReferenceInvoiceNumber().equals(invoice.getDocumentNumber()))
                .collect(Collectors.toList());
        return new PaymentApplicationAdjustmentInvoiceResponse(
                getDocId(),
                invoice,
                paidAppliedsForInvoice,
                getDocumentHandlerUrl(invoice.getDocumentNumber()),
                urlForCustomer(invoice.getCustomer())
        );
    }

    public String getAccountingLinesJson() {
        final List<AccountingLineResponse> accountingLineResponses =
                getPaymentApplicationAdjustmentDocument().getNonArAccountingLines()
                        .stream()
                        .map(this::createAccountingLineResponse)
                        .collect(Collectors.toList());

        return serializeToJsonSafely(accountingLineResponses).orElse("{}");
    }

    public List<PaymentApplicationAdjustmentInvoiceResponse> getInvoiceResponses() {
        return invoices.stream().map(this::createInvoiceResponse).collect(Collectors.toList());
    }

    public String getInvoiceResponsesJson() {
        // Blindly using Optional.get() is generally discouraged. However, in this case, combined with
        // serializeToJsonSafely(...), it provides the currently desired behavior, with less code.
        return serializeToJsonSafely(getInvoiceResponses()).get();
    }

    List<CustomerInvoiceDocument> getInvoices() {
        return invoices;
    }

    void setInvoices(final List<CustomerInvoiceDocument> invoices) {
        this.invoices = invoices;
    }

    public String getHeaderFieldsJson() {
        return headerFieldsJson;
    }

    void setHeaderFieldsJson(final String headerFieldsJson) {
        this.headerFieldsJson = headerFieldsJson;
    }

    private BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        }
        return businessObjectService;
    }

    private PersonService getPersonService() {
        if (personService == null) {
            personService = SpringContext.getBean(PersonService.class);
        }
        return personService;
    }

    private DateTimeService getDateTimeService() {
        if (dateTimeService == null) {
            dateTimeService = SpringContext.getBean(DateTimeService.class);
        }
        return dateTimeService;
    }

}
