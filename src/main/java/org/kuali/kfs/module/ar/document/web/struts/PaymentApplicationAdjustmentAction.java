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
package org.kuali.kfs.module.ar.document.web.struts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.coa.service.ChartService;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.SubAccountService;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.SubObjectCodeService;
import org.kuali.kfs.coa.service.ProjectCodeService;
import org.kuali.kfs.kim.impl.group.GroupBo;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.kns.web.ui.HeaderField;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.AdHocRouteWorkgroup;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.exception.DocumentAuthorizationException;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.rest.responses.AdHocRoutePersonResponse;
import org.kuali.kfs.krad.rest.responses.AdHocRouteWorkgroupResponse;
import org.kuali.kfs.krad.rest.responses.NoteResponse;
import org.kuali.kfs.krad.rules.rule.event.AddAdHocRoutePersonEvent;
import org.kuali.kfs.krad.rules.rule.event.RouteDocumentEvent;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.module.ar.ArKeyConstants.PaymentApplicationAdjustmentDocumentErrors;
import org.kuali.kfs.module.ar.businessobject.AccountsReceivableDocumentHeader;
import org.kuali.kfs.module.ar.businessobject.Customer;
import org.kuali.kfs.module.ar.businessobject.InvoicePaidApplied;
import org.kuali.kfs.module.ar.document.CustomerInvoiceDocument;
import org.kuali.kfs.module.ar.document.PaymentApplicationAdjustmentDocument;
import org.kuali.kfs.module.ar.document.PaymentApplicationDocument;
import org.kuali.kfs.module.ar.document.service.AccountsReceivableDocumentHeaderService;
import org.kuali.kfs.module.ar.document.service.impl.PaymentApplicationAdjustmentDocumentService;
import org.kuali.kfs.module.ar.rest.resource.requests.AdHocRoutingRequest;
import org.kuali.kfs.module.ar.rest.resource.requests.PaymentApplicationAdjustmentRequest;
import org.kuali.kfs.module.ar.rest.resource.requests.PaymentApplicationAdjustmentRequest.AccountingLine;
import org.kuali.kfs.module.ar.rest.resource.responses.PaymentApplicationAdjustmentInvoiceResponse;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.Message;
import org.kuali.kfs.sys.MessageBuilder;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.service.AccountingLineRuleHelperService;
import org.kuali.kfs.sys.document.service.impl.AccountingLineValidationError;
import org.kuali.kfs.sys.document.web.struts.FinancialSystemTransactionalDocumentActionBase;
import org.kuali.kfs.sys.rest.resource.responses.ErrorResponse;
import org.kuali.rice.core.api.exception.RiceIllegalArgumentException;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.action.ActionRequest;
import org.kuali.rice.kew.api.action.InvalidActionTakenException;
import org.kuali.rice.kew.api.document.InvalidDocumentContentException;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;
import org.springframework.http.HttpHeaders;
import org.springframework.util.AutoPopulatingList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.HashMap;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Map.entry;

/**
 * CU Customization KFSPTS-21426 - FINP 7302 - APPA GLPEs are not created correctly
 * 
 * A Struts {@link org.apache.struts.actions.DispatchAction} instance associated
 * with the "Payment Application Adjustment" transactional document.
 */
public class PaymentApplicationAdjustmentAction extends FinancialSystemTransactionalDocumentActionBase {
    private static final Logger LOG = LogManager.getLogger();

    private ChartService chartService;
    private AccountService accountService;
    private SubAccountService subAccountService;
    private ObjectCodeService objectCodeService;
    private SubObjectCodeService subObjectCodeService;
    private ProjectCodeService projectCodeService;
    private PaymentApplicationAdjustmentDocumentService paymentApplicationAdjustmentDocumentService;
    private AccountingLineRuleHelperService accountingLineRuleHelperService;

    @Override
    public ActionForward execute(
            final ActionMapping mapping,
            final ActionForm form,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        final ActionForward forward = super.execute(mapping, form, request, response);

        final PaymentApplicationAdjustmentForm appaForm = (PaymentApplicationAdjustmentForm) form;

        final List<Map> headerFields =
                appaForm.getDocInfo().stream().map(this::convertHeaderField).collect(Collectors.toList());
        serializeToJsonSafely(headerFields)
                .ifPresent(json -> appaForm.setHeaderFieldsJson(json));

        return forward;
    }

    @Override
    public ActionForward blanketApprove(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        loadDocument((KualiDocumentFormBase) form);
        try {
            super.blanketApprove(mapping, form, request, response);
        } catch (Exception e) {
            final var appaForm = (PaymentApplicationAdjustmentForm) form;
            final var errorMessages = getErrorMessages();
            String errorJson = e.getMessage();
            if (MapUtils.isNotEmpty(errorMessages)) {
                final String msg = String.format("Unable to blanket approve document: documentNumber=%s",
                        appaForm.getDocument().getDocumentNumber());
                LOG.error("blanketApprove(...) - Returning; {}", msg);
                errorJson = MAPPER.writeValueAsString(errorMessages);
            }
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, errorJson));
        }
        return null;
    }

    @Override
    public ActionForward fyi(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        loadDocument((KualiDocumentFormBase) form);
        try {
            super.fyi(mapping, form, request, response);
        } catch (Exception e) {
            final var appaForm = (PaymentApplicationAdjustmentForm) form;
            final var errorMessages = getErrorMessages();
            String errorJson = e.getMessage();
            if (MapUtils.isNotEmpty(errorMessages)) {
                final String msg = String.format("Unable to fyi document: documentNumber=%s",
                        appaForm.getDocument().getDocumentNumber());
                LOG.error("fyi(...) - Returning; {}", msg);
                errorJson = MAPPER.writeValueAsString(errorMessages);
            }
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, errorJson));
        }
        return null;
    }

    @Override
    public ActionForward acknowledge(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        loadDocument((KualiDocumentFormBase) form);
        try {
            super.acknowledge(mapping, form, request, response);
        } catch (Exception e) {
            final var appaForm = (PaymentApplicationAdjustmentForm) form;
            final var errorMessages = getErrorMessages();
            String errorJson = e.getMessage();
            if (MapUtils.isNotEmpty(errorMessages)) {
                final String msg = String.format("Unable to acknowledge document: documentNumber=%s",
                        appaForm.getDocument().getDocumentNumber());
                LOG.error("acknowledge(...) - Returning; {}", msg);
                errorJson = MAPPER.writeValueAsString(errorMessages);
            }
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, errorJson));
        }
        return null;
    }

    @Override
    public ActionForward approve(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        loadDocument((KualiDocumentFormBase) form);
        try {
            super.approve(mapping, form, request, response);
        } catch (Exception e) {
            final var appaForm = (PaymentApplicationAdjustmentForm) form;
            final var errorMessages = getErrorMessages();
            String errorJson = e.getMessage();
            if (MapUtils.isNotEmpty(errorMessages)) {
                final String msg = String.format("Unable to approve document: documentNumber=%s",
                        appaForm.getDocument().getDocumentNumber());
                LOG.error("approve(...) - Returning; {}", msg);
                errorJson = MAPPER.writeValueAsString(errorMessages);
            }
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, errorJson));
        }
        return null;
    }

    @Override
    public ActionForward disapprove(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        final KualiDocumentFormBase documentForm = (KualiDocumentFormBase) form;
        loadDocument(documentForm);

        try {
            final String reason = extractValueFromDataJson("/reason", request);
            if (StringUtils.isBlank(reason)) {
                final String msg = "Missing 'reason' request parameter";
                LOG.warn("disapprove(...) - Returning; {}", msg);
                writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, msg));
                return null;
            }
            doDisapprove(documentForm, request, reason);
        } catch (Exception e) {
            final var appaForm = (PaymentApplicationAdjustmentForm) form;
            final var errorMessages = getErrorMessages();
            String errorJson = e.getMessage();
            if (MapUtils.isNotEmpty(errorMessages)) {
                final String msg = String.format("Unable to disapprove document: documentNumber=%s",
                        appaForm.getDocument().getDocumentNumber());
                LOG.error("disapprove(...) - Returning; {}", msg);
                errorJson = MAPPER.writeValueAsString(errorMessages);
            }
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, errorJson));
        }
        return null;
    }

    /**
     *
     * @return {@code null} always, so Struts will not do a redirect.
     * @throws WorkflowException if the document cannot be loaded.
     */
    public ActionForward addCustomer(
            final ActionMapping mapping,
            final ActionForm form,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        final String customerNumber = extractValueFromDataJson("/customerNumber", request);
        if (StringUtils.isBlank(customerNumber)) {
            final String msg = "Missing 'customerNumber' request parameter";
            LOG.warn("addCustomer(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, msg));
            return null;
        }

        final var customer = getBusinessObjectService().findBySinglePrimaryKey(Customer.class, customerNumber);
        if (customer == null) {
            final String msg = String.format("Requested customer not found : customerNumber=%s", customerNumber);
            LOG.warn("addCustomer(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.NOT_FOUND, msg));
            return null;
        }

        final var appaForm = (PaymentApplicationAdjustmentForm) form;
        final var nonAppliedHolding = appaForm.addNonAppliedHoldingWithCustomer(customer, KualiDecimal.ZERO);

        serializeToJsonSafely(nonAppliedHolding)
                .ifPresentOrElse(
                    json -> writeJsonToResponse(response, Response.Status.OK, json),
                    () -> response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));

        return null;
    }

    public ActionForward addAdHocRoutePerson(
            final ActionMapping mapping,
            final ActionForm form,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws WorkflowException {
        final String principalName = extractValueFromDataJson("/person/principalName", request);
        final String action = extractValueFromDataJson("/person/action", request);

        if (StringUtils.isBlank(principalName)) {
            final String msg = "principalName is required";
            LOG.warn("adAdHocRoutePerson(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, msg));
            return null;
        }

        if (StringUtils.isBlank(action)) {
            final String msg = "action is required";
            LOG.warn("adAdHocRoutePerson(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, msg));
            return null;
        }

        final Person person = getPersonService().getPersonByPrincipalName(principalName);
        if (ObjectUtils.isNull(person)) {
            final String msg = String.format("No person found with principalName=%s", principalName);
            LOG.warn("adAdHocRoutePerson(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.NOT_FOUND, msg));
            return null;
        }

        final PaymentApplicationAdjustmentForm appaForm = (PaymentApplicationAdjustmentForm) form;
        loadDocument(appaForm);

        final Document document = appaForm.getDocument();
        final Person user = getUserFromRequest(request);
        final AdHocRoutePerson adHocRoutePerson = new AdHocRoutePerson();
        adHocRoutePerson.setId(person.getPrincipalName());
        adHocRoutePerson.setActionRequested(action);

        if (!getAdHocRoutingService().canSendAdHocRequest(document, adHocRoutePerson, user)) {
            final String msg = String.format("No permission to add principal with principalName=%s", principalName);
            LOG.warn("adAdHocRoutePerson(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.FORBIDDEN, msg));
            return null;
        }

        final AdHocRoutePersonResponse personResponse = appaForm.createAdHocRoutePersonResponse(person, action);
        serializeToJsonSafely(personResponse)
                .ifPresentOrElse(
                    json -> writeJsonToResponse(response, Response.Status.OK, json),
                    () -> response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
        return null;
    }

    public ActionForward addAdHocRouteWorkgroup(
            final ActionMapping mapping,
            final ActionForm form,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws WorkflowException {
        final String groupId = extractValueFromDataJson("/group/id", request);
        final String action = extractValueFromDataJson("/group/action", request);

        if (StringUtils.isBlank(groupId)) {
            final String msg = String.format("Group id is required");
            LOG.warn("addAdHocRouteWorkgroup(...) - Returning {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, msg));
            return null;
        }

        if (StringUtils.isBlank(action)) {
            final String msg = String.format("Action is required");
            LOG.warn("addAdHocRouteWorkgroup(...) - Returning {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, msg));
            return null;
        }

        final GroupBo group = getBusinessObjectService().findBySinglePrimaryKey(GroupBo.class, groupId);
        if (ObjectUtils.isNull(group)) {
            final String msg = String.format("No group found with id=%s", groupId);
            LOG.warn("addAdHocRouteWorkgroup(...) - Returning {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.NOT_FOUND, msg));
            return null;
        }

        final PaymentApplicationAdjustmentForm appaForm = (PaymentApplicationAdjustmentForm) form;
        loadDocument(appaForm);

        final Document document = appaForm.getDocument();
        final Person user = getUserFromRequest(request);
        final AdHocRouteWorkgroup workgroup = new AdHocRouteWorkgroup();
        workgroup.setRecipientName(group.getName());
        workgroup.setRecipientNamespaceCode(group.getNamespaceCode());

        if (!getAdHocRoutingService().canSendAdHocRequest(document, workgroup, user)) {
            final String msg = String.format("No permission to add workgroup with id=%s", groupId);
            LOG.warn("addAdHocRouteWorkgroup(...) - Returning {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.FORBIDDEN, msg));
            return null;
        }

        final AdHocRouteWorkgroupResponse workgroupResponse = appaForm.createAdHocRouteWorkgroupResponse(group, action);
        serializeToJsonSafely(workgroupResponse)
                .ifPresentOrElse(
                    json -> writeJsonToResponse(response, Response.Status.OK, json),
                    () -> response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
        return null;
    }
    
    /*
     * CU Customization KFSPTS-21426
     */
    @Override
    public ActionForward route(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        final var appaRequest = getDataFromRequest(request, response);
        final var appaForm = (PaymentApplicationAdjustmentForm) form;
        final var appaDoc = appaForm.getApplicationAdjustmentDocument();

        final List<AccountingLine> nonArAccountingLines = appaRequest.getNonArAccountingLines();
        final List<AccountingLineValidationError> validationErrors = getNonArAccountingLinesValidationErrors(nonArAccountingLines, appaDoc.getDocumentNumber());
        if (!validationErrors.isEmpty()) {
            final MessageMap messageMap = GlobalVariables.getMessageMap();

            validationErrors.forEach(error -> {
                messageMap.putError(error.getPropertyName(), error.getErrorKey());
            });

            messageMap.putError(
                    KFSConstants.MODAL_TITLE_KEY,
                    PaymentApplicationAdjustmentDocumentErrors.NON_AR_ACCOUNTING_LINE_VALIDATION_ERROR_TITLE
            );
            messageMap.putError(
                    KFSConstants.MODAL_MESSAGE_KEY,
                    PaymentApplicationAdjustmentDocumentErrors.NON_AR_ACCOUNTING_LINE_VALIDATION_ERROR_MESSAGE
            );
            putErrorMessagesInResponse(response, appaForm, "route");
            return null;
        }

        loadDocument(appaForm);

        updateAdHocRouting(request, appaForm, appaRequest.getAdHocRoutingRequest());
        updateDocumentWithRequestData(appaForm, appaRequest);
        //getPaymentApplicationAdjustmentDocumentService().removeZeroAmountInvoicePaidAppliedsFromDocument(appaForm.getPaymentApplicationAdjustmentDocument());
        //getPaymentApplicationAdjustmentDocumentService().removeZeroAmountNonAppliedHoldingsFromDocument(appaForm.getPaymentApplicationAdjustmentDocument());

        try {
            super.route(mapping, form, request, response);
        } catch (Exception e) {
            putErrorMessagesInResponse(response, appaForm, "route");
        }
        
        LOG.info("CU Customization KFSPTS-21426, remove zero amounts after super.route");
        
        getPaymentApplicationAdjustmentDocumentService().removeZeroAmountInvoicePaidAppliedsFromDocument(appaForm.getPaymentApplicationAdjustmentDocument());
        getPaymentApplicationAdjustmentDocumentService().removeZeroAmountNonAppliedHoldingsFromDocument(appaForm.getPaymentApplicationAdjustmentDocument());
        
        return null;
    }

    @Override
    public ActionForward save(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        final var appaRequest = getDataFromRequest(request, response);
        final var appaForm = (PaymentApplicationAdjustmentForm) form;
        final var appaDoc = appaForm.getApplicationAdjustmentDocument();

        final List<AccountingLine> nonArAccountingLines = appaRequest.getNonArAccountingLines();
        final List<AccountingLineValidationError> validationErrors = getNonArAccountingLinesValidationErrors(nonArAccountingLines, appaDoc.getDocumentNumber());
        if (!validationErrors.isEmpty()) {
            final MessageMap messageMap = GlobalVariables.getMessageMap();

            validationErrors.forEach(error -> {
                messageMap.putError(error.getPropertyName(), error.getErrorKey(), error.getMessageParameters());
            });
            putErrorMessagesInResponse(response, appaForm, "save");
            return null;
        }

        loadDocument(appaForm);

        updateAdHocRouting(request, appaForm, appaRequest.getAdHocRoutingRequest());
        updateDocumentWithRequestData(appaForm, appaRequest);

        try {
            super.save(mapping, form, request, response);
            final var warningMessages = getWarningMessages();
            if (!warningMessages.isEmpty()) {
                final String warningJson = MAPPER.writeValueAsString(warningMessages);
                writeJsonToResponse(response, Response.Status.OK, warningJson);
            }
        } catch (final Exception e) {
            putErrorMessagesInResponse(response, appaForm, "save");
        }

        return null;
    }

    private List<AccountingLineValidationError> getNonArAccountingLinesValidationErrors(final List<AccountingLine> nonArAccountingLines, String appaDocumentNumber) {
        AccountingLineRuleHelperService accountingLineRuleHelperService = getAccountingLineRuleHelperService();
        return IntStream.range(0, nonArAccountingLines.size())
                .mapToObj(i -> {
                    AccountingLine accountingLine = nonArAccountingLines.get(i);
                    SourceAccountingLine sourceAccountingLine = getPaymentApplicationAdjustmentDocumentService().createSourceAccountingLine(accountingLine, appaDocumentNumber, accountingLine.getSequenceNumber());
                    final String accountingLinePath = String.format("accountingLines.%d", i);
                    List<AccountingLineValidationError> errors = accountingLineRuleHelperService.getAccountingLineValidationErrors(sourceAccountingLine, true);
                    errors.forEach(error -> {
                        error.setPropertyName(accountingLinePath + "." + error.getPropertyName());
                    });
                    return errors;
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private void putErrorMessagesInResponse(
            final HttpServletResponse response,
            final PaymentApplicationAdjustmentForm appaForm,
            final String operationName
    ) throws JsonProcessingException {
        final var errorMessages = getErrorMessages();
        if (MapUtils.isEmpty(errorMessages)) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } else {
            final String msg = String.format(
                    "Unable to %s document: documentNumber=%s",
                    operationName,
                    appaForm.getDocument().getDocumentNumber()
            );
            LOG.error("putErrorMessagesInResponse(...) - Exit; {}", msg);
            final String errorJson = MAPPER.writeValueAsString(errorMessages);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, errorJson));
        }
    }

    @Override
    public ActionForward insertBONote(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        final var appaForm = (PaymentApplicationAdjustmentForm) form;
        loadDocument(appaForm);
        super.insertBONote(mapping, form, request, response);

        List<Note> notes = appaForm.getDocument().getNotes();
        Note newNote = notes.get(notes.size() - 1);
        NoteResponse noteResponse = appaForm.createNoteResponse(newNote);

        serializeToJsonSafely(noteResponse)
                .ifPresentOrElse(
                    json -> writeJsonToResponse(response, Response.Status.OK, json),
                    () -> response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));

        return null;
    }

    @Override
    public ActionForward deleteBONote(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        final var appaForm = (PaymentApplicationAdjustmentForm) form;
        loadDocument(appaForm);
        PaymentApplicationAdjustmentDocument document = appaForm.getPaymentApplicationAdjustmentDocument();
        final String noteId = extractValueFromDataJson("/noteId", request);

        if (StringUtils.isBlank(noteId)) {
            final String msg = "Missing 'noteId' request parameter";
            LOG.warn("deleteBONote(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, msg));
            return null;
        }

        Note note = document.getNoteById(noteId);

        try {
            deleteNoteFromDocument(document, note);
        } catch (IllegalArgumentException | DocumentAuthorizationException e) {
            final String msg = String.format("Unable to delete note: documentNumber=%s, noteId=%s",
                    appaForm.getDocument().getDocumentNumber(), noteId);
            LOG.error("deleteBONote(...) - Returning; {}", msg);
            final String errorJson = MAPPER.writeValueAsString(msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, errorJson));
            return null;
        }
        return null;
    }

    @Override
    public ActionForward sendNoteWorkflowNotification(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        final var appaForm = (PaymentApplicationAdjustmentForm) form;
        final String noteId = extractValueFromDataJson("/noteId", request);

        if (StringUtils.isBlank(noteId)) {
            final String msg = "Missing 'noteId' request parameter";
            LOG.warn("sendNoteWorkflowNotification(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, msg));
            return null;
        }

        final String principalName = extractValueFromDataJson("/principalName", request);

        if (StringUtils.isBlank(principalName)) {
            final String msg = "Missing 'principalName' request parameter";
            LOG.warn("sendNoteWorkflowNotification(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, msg));
            return null;
        }

        loadDocument(appaForm);
        PaymentApplicationAdjustmentDocument document = appaForm.getPaymentApplicationAdjustmentDocument();
        Note note = document.getNoteById(noteId);
        note.getAdHocRouteRecipient().setId(principalName);

        try {
            sendNoteNotification(note, document, request, appaForm);
        } catch (WorkflowException | ValidationException e) {
            final String msg = String.format("Unable to send note notification: documentNumber=%s, noteId=%s, principalName=%s, message=%s",
                    appaForm.getDocument().getDocumentNumber(), noteId, principalName, e.getMessage());
            LOG.error("sendNoteWorkflowNotification(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, msg));
            return null;
        }
        return null;
    }

    @Override
    public ActionForward sendAdHocRequests(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        final var appaForm = (PaymentApplicationAdjustmentForm) form;
        loadDocument(appaForm);

        try {
            final var appaRequest = getDataFromRequest(request, response);
            AdHocRoutingRequest adHocRoutingRequest = appaRequest.getAdHocRoutingRequest();
            if (adHocRoutingRequest.getPersons().isEmpty() && adHocRoutingRequest.getGroups().isEmpty()) {
                throw new ValidationException("No ad hoc recipients specified");
            }
            updateAdHocRouting(request, appaForm, appaRequest.getAdHocRoutingRequest());
            super.sendAdHocRequests(mapping, form, request, response);
        } catch (Exception e) {
            final var errorMessages = getErrorMessages();
            String errorJson = e.getMessage();
            if (MapUtils.isNotEmpty(errorMessages)) {
                final String msg = String.format("Unable to send ad hoc requests: documentNumber=%s",
                        appaForm.getDocument().getDocumentNumber());
                LOG.error("sendAdHocRequests(...) - Returning; {}", msg);
                errorJson = MAPPER.writeValueAsString(errorMessages);
            }
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, errorJson));
        }
        return null;
    }

    private void sendNoteNotification(
            Note note,
            Document document,
            HttpServletRequest request,
            KualiDocumentFormBase form
    ) throws WorkflowException, ValidationException {
        // verify recipient was specified
        if (StringUtils.isBlank(note.getAdHocRouteRecipient().getId())) {
            throw new ValidationException("No recipient specified");
        } else {
            // check recipient is valid
            note.getAdHocRouteRecipient().setActionRequested(determineNoteWorkflowNotificationAction(request, form, note));

            boolean rulePassed = getKualiRuleService().applyRules(new AddAdHocRoutePersonEvent(
                    KRADPropertyConstants.NEW_DOCUMENT_NOTE, document,
                    (AdHocRoutePerson) note.getAdHocRouteRecipient()));
            if (!rulePassed) {
                throw new ValidationException("Invalid recipient");
            }
        }

        // check if document is saved
        if (document.getDocumentHeader().getWorkflowDocument().isInitiated()) {
            throw new ValidationException("Document is not saved");
        }

        // do the send
        getDocumentService().sendNoteRouteNotification(document, note, getUserFromRequest(request));
    }

    /**
     *
     * @return {@code null} always, so Struts will not do a redirect.
     * @throws WorkflowException if the document cannot be loaded.
     */
    public ActionForward addInvoice(
            final ActionMapping mapping,
            final ActionForm form,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws WorkflowException {
        final String invoiceNumber = extractValueFromDataJson("/invoiceNumber", request);
        if (StringUtils.isBlank(invoiceNumber)) {
            final String msg = "Missing 'invoiceNumber' request parameter";
            LOG.warn("addInvoice(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, msg));
            return null;
        }

        final CustomerInvoiceDocument invoice =
                getBusinessObjectService().findBySinglePrimaryKey(CustomerInvoiceDocument.class, invoiceNumber);
        if (invoice == null) {
            final String msg = String.format("Requested invoice not found : invoiceNumber=%s", invoiceNumber);
            LOG.warn("addInvoice(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.NOT_FOUND, msg));
            return null;
        }

        final KualiDecimal openAmount = invoice.getOpenAmount();
        if (openAmount == null || !openAmount.isPositive()) {
            final String msg = String.format("Invoice does not have an open amount : invoiceNumber=%s", invoiceNumber);
            LOG.warn("addInvoice(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.PRECONDITION_FAILED, msg));
            return null;
        }

        final PaymentApplicationAdjustmentForm appaForm = (PaymentApplicationAdjustmentForm) form;
        loadDocument(appaForm);
        appaForm.getInvoices().add(invoice);

        final PaymentApplicationAdjustmentInvoiceResponse invoiceResponse = appaForm.createInvoiceResponse(invoice);
        serializeToJsonSafely(invoiceResponse)
                .ifPresentOrElse(
                    json -> writeJsonToResponse(response, Response.Status.OK, json),
                    () -> response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));

        return null;
    }

    @Override
    public ActionForward cancel(
            final ActionMapping mapping,
            final ActionForm form,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        final PaymentApplicationAdjustmentForm appaForm = (PaymentApplicationAdjustmentForm) form;
        try {
            loadDocument(appaForm);
            markAdjusteeAsNoLongerBeingAdjusted(appaForm);
            doProcessingAfterPost(appaForm, request);
            getDocumentService().cancelDocument(appaForm.getDocument(), appaForm.getAnnotation());
            return null;
        } catch (WorkflowException e) {
            final String msg = String.format("Failed to cancel document: %s", appaForm.getDocId());
            LOG.warn("cancel(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, msg));
            return null;
        }
    }

    public ActionForward processSuperUserActions(
        final ActionMapping mapping,
        final ActionForm form,
        final HttpServletRequest request,
        final HttpServletResponse response
    ) throws Exception {
        final PaymentApplicationAdjustmentForm appaForm = (PaymentApplicationAdjustmentForm) form;
        loadDocument(appaForm);

        if (!appaForm.isStateAllowsApproveSingleActionRequest()) {
            final String msg = "User is not permitted to take requested actions";
            LOG.warn("takeSuperUserActions(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.FORBIDDEN, msg));
            return null;
        }

        final String annotation = extractValueFromDataJson("/annotation", request);
        if (StringUtils.isBlank(annotation)) {
            final String msg = "Missing 'annotation' request parameter";
            LOG.warn("takeSuperUserActions(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, msg));
            return null;
        }

        final JsonNode rootNode = getRootNode(request.getParameter("data"));
        final JsonNode actionIds = rootNode.get("actionIds");
        if (actionIds == null || !actionIds.isArray()) {
            final String msg = "Invalid 'actionIds'. Expected list";
            LOG.warn("takeSuperUserActions(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, msg));
            return null;
        }

        List<ActionRequest> actionRequests = getWorkflowDocumentService().getPendingActionRequests(appaForm.getDocId());
        final Map<String, ActionRequest> mappedActionRequests = actionRequests.stream().collect(
                Collectors.toMap(ActionRequest::getId, Function.identity()));
        actionIds.forEach(actionIdJson -> {
            final String actionId = actionIdJson.asText();
            final ActionRequest actionRequest = mappedActionRequests.get(actionId);
            if (actionRequest == null) {
                return;
            }

            final Document document = appaForm.getDocument();
            if (actionRequest.isCompleteRequest() || actionRequest.isApprovalRequest()) {
                try {
                    getDocumentService().validateAndPersistDocument(document, new RouteDocumentEvent(document));
                } catch (ValidationException validationException) {
                    return;
                }
            }

            final Person user = getUserFromRequest(request);
            try {
                getSuperUserService().takeRequestedAction(
                        actionRequest,
                        appaForm.getWorkflowDocument().getDocumentTypeId(),
                        appaForm.getDocId(),
                        user,
                        annotation);
            } catch (RiceIllegalArgumentException | InvalidDocumentContentException | InvalidActionTakenException e) {
                final String msg = String.format("Unable to takeSuperUserActions: documentNumber=%s, principalName=%s, actions=%s, message=%s",
                        document.getDocumentNumber(), user.getPrincipalName(), actionIds.toString(), e.getMessage());
                LOG.error("takeSuperUserActions(...) - Returning; {}", msg);
                writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, msg));
            }
        });

        final var errorMessages = getErrorMessages();
        if (MapUtils.isNotEmpty(errorMessages)) {
            final String msg = String.format("Error when taking super user actions document: documentNumber=%s",
                    appaForm.getDocument().getDocumentNumber());
            LOG.error("takeSuperUserActions(...) - Returning; {}", msg);
            final String errorJson = MAPPER.writeValueAsString(errorMessages);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, errorJson));
        } else {
            writeJsonToResponse(response, Response.Status.OK, "");
        }

        return null;
    }

    public ActionForward superUserAction(
            final ActionMapping mapping,
        final ActionForm form,
        final HttpServletRequest request,
        final HttpServletResponse response
    ) {
        final String annotation = extractValueFromDataJson("/annotation", request);
        if (StringUtils.isBlank(annotation)) {
            final String msg = "Missing 'annotation' request parameter";
            LOG.warn("superUserApprove(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, msg));
            return null;
        }

        final String action = extractValueFromDataJson("/action", request);
        if (StringUtils.isBlank(action)) {
            final String msg = "Missing 'action' request parameter";
            LOG.warn("superUserAction(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, msg));
            return null;
        }

        final KualiDocumentFormBase documentForm = (KualiDocumentFormBase) form;
        try {
            loadDocument(documentForm);
        } catch (WorkflowException e) {
            final String msg = String.format("Unable to load document for super user action: documentNumber=%s, message=%s",
                    documentForm.getDocument().getDocumentNumber(), e.getMessage());
            LOG.error("superUserAction(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, msg));
            return null;
        }

        final Person user = getUserFromRequest(request);
        final String docTypeId = documentForm.getWorkflowDocument().getDocumentTypeId();
        final String docId = documentForm.getDocId();
        try {
            if (StringUtils.equals(action, KFSConstants.SuperUserActions.APPROVE)) {
                getSuperUserService().blanketApprove(docTypeId, docId, user, annotation);
            } else if (StringUtils.equals(action, KFSConstants.SuperUserActions.DISAPPROVE)) {
                markAdjusteeAsNoLongerBeingAdjusted(documentForm);
                getSuperUserService().disapprove(docTypeId, docId, user, annotation);
            }
            writeJsonToResponse(response, Response.Status.OK, "");
        } catch (InvalidActionTakenException | InvalidDocumentContentException | RiceIllegalArgumentException
                | WorkflowException e) {
            final String msg = String.format("Unable to super user action: documentNumber=%s, principalName=%s, action=%s, message=%s",
                    documentForm.getDocument().getDocumentNumber(), user.getPrincipalName(), action, e.getMessage());
            LOG.error("superUserAction(...) - Returning; {}", msg);
            writeErrorToResponse(response, new ErrorResponse(Response.Status.BAD_REQUEST, msg));
        }
        return null;
    }

    void markAdjusteeAsNoLongerBeingAdjusted(final KualiDocumentFormBase form) throws WorkflowException {
        final PaymentApplicationAdjustmentDocument adjustmentDocument =
                (PaymentApplicationAdjustmentDocument) form.getDocument();
        final String adjusteeDocumentNumber = adjustmentDocument.getAdjusteeDocumentNumber();
        final Document adjusteeDocument = getDocumentService().getByDocumentHeaderId(adjusteeDocumentNumber);
        if (adjusteeDocument instanceof PaymentApplicationDocument) {
            ((PaymentApplicationDocument) adjusteeDocument).clearAdjusterDocumentNumber();
        } else if (adjusteeDocument instanceof PaymentApplicationAdjustmentDocument) {
            ((PaymentApplicationAdjustmentDocument) adjusteeDocument).clearAdjustmentDocumentNumber();
        }
        getDocumentService().updateDocument(adjusteeDocument);
    }

    private Map<String, Object> convertHeaderField(final HeaderField headerField) {
        final Map<String, Object> field = new HashMap<>();
        field.put("value", headerField.getDisplayValue());

        final String[] ddNameParts = headerField.getDdAttributeEntryName().split("\\.");
        // We expect parts to be of the format
        // "DataDictionary.<entityName>.attributes.<attributeName>
        if (ddNameParts.length == 4) {
            final String entityName = ddNameParts[1];
            final String attributeName = ddNameParts[3];
            field.put("label", getDataDictionaryService().getAttributeShortLabel(entityName, attributeName));
        }

        if (StringUtils.isNotEmpty(headerField.getNonLookupValue()) && headerField.isLookupAware()) {
            field.put("url", headerField.getNonLookupValue());
        }

        return field;
    }

    private Map<String, List<String>> getErrorMessages() {
        return extractMessagesFromMap(GlobalVariables.getMessageMap().getErrorMessages());
    }

    private Map<String, List<String>> getWarningMessages() {
        return extractMessagesFromMap(GlobalVariables.getMessageMap().getWarningMessages());
    }

    private Map<String, List<String>> extractMessagesFromMap(Map<String, AutoPopulatingList<ErrorMessage>> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> convertErrorMessagesToString(entry.getValue())));
    }

    private List<String> convertErrorMessagesToString(List<ErrorMessage> errorMessages) {
        return errorMessages.stream()
                .map(ErrorMessage::getErrorKey)
                .map(errorKey -> MessageBuilder.buildMessage(errorKey, null, 0))
                .map(Message::getMessage)
                .collect(Collectors.toList());
    }

    private PaymentApplicationAdjustmentRequest getDataFromRequest(HttpServletRequest request,
                                                                   HttpServletResponse response) {
        String dataJson = request.getParameter("data");
        PaymentApplicationAdjustmentRequest appaRequest = null;
        try {
            appaRequest = MAPPER.readValue(dataJson, PaymentApplicationAdjustmentRequest.class);
        } catch (JsonProcessingException e) {
            LOG.error("Unable to deserialize APPA Request. Error: {}", e.getMessage());
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        return appaRequest;
    }

    private void updateDocumentWithRequestData(
            final PaymentApplicationAdjustmentForm form,
            final PaymentApplicationAdjustmentRequest request
    ) {
        final var appaDoc = form.getApplicationAdjustmentDocument();
        final var documentHeader = appaDoc.getDocumentHeader();
        documentHeader.setDocumentDescription(request.getDescription());
        documentHeader.setExplanation(request.getExplanation());
        documentHeader.setOrganizationDocumentNumber(request.getOrgDocNumber());

        getPaymentApplicationAdjustmentDocumentService()
                .updateInvoicePaidApplieds(appaDoc, request.getInvoiceApplications());
        getPaymentApplicationAdjustmentDocumentService()
                .updateNonAppliedHoldings(appaDoc, request.getNonAppliedHoldings());
        getPaymentApplicationAdjustmentDocumentService()
                .updateNonArAccountingLines(appaDoc, request.getNonArAccountingLines());
    }

    public void updateAdHocRouting(
            HttpServletRequest request,
            KualiDocumentFormBase form,
            AdHocRoutingRequest adHocRoutingRequest
    ) {
        final var doc = form.getDocument();
        List<AdHocRouteWorkgroup> workgroups = adHocRoutingRequest.getGroups().stream()
                .map(groupRequest -> {
                    final var workgroup = new AdHocRouteWorkgroup();
                    workgroup.setRecipientName(groupRequest.getName());
                    workgroup.setRecipientNamespaceCode(groupRequest.getNamespaceCode());
                    workgroup.setActionRequested(groupRequest.getAction());

                    final var group = getAdHocRoutingService()
                            .getGroup(workgroup.getRecipientName(), workgroup.getRecipientNamespaceCode());
                    if (group != null) {
                        workgroup.setId(group.getId());
                    }
                    return workgroup;
                })
                .collect(Collectors.toList());

        List<AdHocRoutePerson> persons = adHocRoutingRequest.getPersons().stream()
                .map(personRequest -> {
                    final var recipient = new AdHocRoutePerson();
                    recipient.setId(personRequest.getPrincipalName());
                    recipient.setActionRequested(personRequest.getAction());
                    return recipient;
                })
                .collect(Collectors.toList());

        final var user = getUserFromRequest(request);
        getAdHocRoutingService().updateAdHocWorkgroups(doc, workgroups, user);
        getAdHocRoutingService().updateAdHocPersons(doc, persons, user);
    }

    /*
     * Retrieves the 'data' parameter from the request, which will be a JSON String. Then, retrieves the value of the
     * requested 'jsonPath' from the JSON.
     */
    private static String extractValueFromDataJson(final String jsonPath, final HttpServletRequest request) {
        final var dataJson = request.getParameter("data");
        final JsonNode rootNode = getRootNode(dataJson);
        final JsonNode desiredNode = rootNode.at(jsonPath);
        final String value = Objects.requireNonNullElseGet(desiredNode, MissingNode::getInstance).asText();
        LOG.debug("extractValueFromDataJson(...) - Returning : value={}; jsonPath={}; dataJson={}",
                value,
                jsonPath,
                dataJson
        );
        return value;
    }

    private static JsonNode getRootNode(final String dataJson) {
        JsonNode rootNode = null;
        try {
            rootNode = MAPPER.readTree(dataJson);
        } catch (final JsonProcessingException e) {
            LOG.error("getRootNode(...) - Unable to create JsonNode : dataJson={}", dataJson, e);
        }
        return Objects.requireNonNullElseGet(rootNode, MissingNode::getInstance);
    }

    /*
     * Wrapping SpringContext.getBean(...) in a method so the test can use a spy to provide a mock version and not
     * actually use Spring. This way, static mocking is not necessary.
     */
    PaymentApplicationAdjustmentDocumentService getPaymentApplicationAdjustmentDocumentService() {
        if (paymentApplicationAdjustmentDocumentService == null) {
            paymentApplicationAdjustmentDocumentService =
                    SpringContext.getBean(PaymentApplicationAdjustmentDocumentService.class);
        }
        return paymentApplicationAdjustmentDocumentService;
    }

    private Person getUserFromRequest(HttpServletRequest request) {
        final UserSession userSession = (UserSession) request.getSession().getAttribute(KRADConstants.USER_SESSION_KEY);
        return userSession.getPerson();
    }

    public ActionForward adjust(
            final ActionMapping mapping,
            final ActionForm form,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws WorkflowException {
        final PaymentApplicationAdjustmentForm appaForm = (PaymentApplicationAdjustmentForm) form;

        loadDocument(appaForm);

        final PaymentApplicationAdjustmentDocument adjusteeDocument =
                (PaymentApplicationAdjustmentDocument) appaForm.getDocument();

        final PaymentApplicationAdjustmentDocument adjustmentDocument =
                getPaymentApplicationAdjustmentDocumentService().createPaymentApplicationAdjustment(adjusteeDocument);

        appaForm.setDocument(adjustmentDocument);

        final var locationHeader = createLocationHeader(adjustmentDocument.getDocumentNumber());
        response.setHeader(HttpHeaders.LOCATION, locationHeader);

        return null;
    }

    private String createLocationHeader(final String docNum) {
        final Map<String, String> parameters = Map.ofEntries(
                entry(KRADConstants.DISPATCH_REQUEST_PARAMETER, KFSConstants.DOC_HANDLER_METHOD),
                entry(KRADConstants.PARAMETER_COMMAND, KFSConstants.METHOD_DISPLAY_DOC_SEARCH_VIEW),
                entry(KRADConstants.PARAMETER_DOC_ID, docNum)
        );

        final String baseUrl = getApplicationBaseUrl() + "/arPaymentApplicationAdjustment.do";

        final String url = UrlFactory.parameterizeUrl(baseUrl, parameters);
        LOG.debug("createLocationHeader(...) - Exit : url={}", url);
        return url;
    }

    public ActionForward getButtonGroup(
            final ActionMapping mapping,
            final ActionForm form,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws WorkflowException, JsonProcessingException {
        final PaymentApplicationAdjustmentForm appaForm = (PaymentApplicationAdjustmentForm) form;
        loadDocument(appaForm);
        populateAuthorizationFields(appaForm);

        String buttons = appaForm.getButtonGroupJson();
        writeJsonToResponse(response, Response.Status.OK, buttons);
        return null;
    }

    public ActionForward getDocumentActions(
            final ActionMapping mapping,
            final ActionForm form,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws WorkflowException, JsonProcessingException {
        final PaymentApplicationAdjustmentForm appaForm = (PaymentApplicationAdjustmentForm) form;
        loadDocument(appaForm);
        populateAuthorizationFields(appaForm);

        String actions = appaForm.getDocumentActionsJson();
        writeJsonToResponse(response, Response.Status.OK, actions);
        return null;
    }

    /**
     * Retrieve the General Ledger Pending Entries data for the doc
     */
    public ActionForward getGeneralLedgerPendingEntries(
            final ActionMapping mapping,
            final ActionForm form,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws WorkflowException {
        final PaymentApplicationAdjustmentForm appaForm = (PaymentApplicationAdjustmentForm) form;
        loadDocument(appaForm);

        String glpes = appaForm.getGeneralLedgerPendingEntriesJson();
        writeJsonToResponse(response, Response.Status.OK, glpes);
        return null;
    }

    /**
     * Retrieve the Route Log data for the doc
     */
    public ActionForward getRouteLog(
            final ActionMapping mapping,
            final ActionForm form,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws WorkflowException {
        final PaymentApplicationAdjustmentForm appaForm = (PaymentApplicationAdjustmentForm) form;
        loadDocument(appaForm);

        String routeLog = appaForm.getRouteLogResponseJson();
        writeJsonToResponse(response, Response.Status.OK, routeLog);
        return null;
    }

    public ActionForward getInvoiceApplications(
            final ActionMapping mapping,
            final ActionForm form,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws WorkflowException {
        final PaymentApplicationAdjustmentForm appaForm = (PaymentApplicationAdjustmentForm) form;
        loadDocument(appaForm);

        String routeLog = appaForm.getInvoiceResponsesJson();
        writeJsonToResponse(response, Response.Status.OK, routeLog);
        return null;
    }

    @Override
    protected void createDocument(KualiDocumentFormBase kualiDocumentFormBase) throws WorkflowException {
        super.createDocument(kualiDocumentFormBase);
        PaymentApplicationAdjustmentForm form = (PaymentApplicationAdjustmentForm) kualiDocumentFormBase;
        PaymentApplicationAdjustmentDocument document = form.getPaymentApplicationAdjustmentDocument();

        // create new accounts receivable header and set it to the payment application document
        AccountsReceivableDocumentHeaderService accountsReceivableDocumentHeaderService =
                SpringContext.getBean(AccountsReceivableDocumentHeaderService.class);
        AccountsReceivableDocumentHeader accountsReceivableDocumentHeader = accountsReceivableDocumentHeaderService
                .getNewAccountsReceivableDocumentHeaderForCurrentUser();
        accountsReceivableDocumentHeader.setDocumentNumber(document.getDocumentNumber());
        document.setAccountsReceivableDocumentHeader(accountsReceivableDocumentHeader);
    }

    @Override
    protected void loadDocument(KualiDocumentFormBase kualiDocumentFormBase) throws WorkflowException {
        super.loadDocument(kualiDocumentFormBase);
        final PaymentApplicationAdjustmentForm appaForm = (PaymentApplicationAdjustmentForm) kualiDocumentFormBase;
        loadInvoices(appaForm);
    }

    private void loadInvoices(PaymentApplicationAdjustmentForm appaForm) {
        final var appaDoc = appaForm.getPaymentApplicationAdjustmentDocument();

        final Set<CustomerInvoiceDocument> invoices =
                appaDoc.getInvoicePaidApplieds().stream()
                        .map(InvoicePaidApplied::getCustomerInvoiceDocument)
                        .collect(Collectors.toSet());
        appaForm.setInvoices(new ArrayList<>(invoices));
    }

    // Non-private for testing purposes
    ChartService getChartService() {
        if (chartService == null) {
            chartService = SpringContext.getBean(ChartService.class);
        }
        return chartService;
    }

    AccountService getAccountService() {
        if (accountService == null) {
            accountService = SpringContext.getBean(AccountService.class);
        }
        return accountService;
    }

    SubAccountService getSubAccountService() {
        if (subAccountService == null) {
            subAccountService = SpringContext.getBean(SubAccountService.class);
        }
        return subAccountService;
    }

    ObjectCodeService getObjectCodeService() {
        if (objectCodeService == null) {
            objectCodeService = SpringContext.getBean(ObjectCodeService.class);
        }
        return objectCodeService;
    }

    SubObjectCodeService getSubObjectCodeService() {
        if (subObjectCodeService == null) {
            subObjectCodeService = SpringContext.getBean(SubObjectCodeService.class);
        }
        return subObjectCodeService;
    }

    ProjectCodeService getProjectCodeService() {
        if (projectCodeService == null) {
            projectCodeService = SpringContext.getBean(ProjectCodeService.class);
        }
        return projectCodeService;
    }

    AccountingLineRuleHelperService getAccountingLineRuleHelperService() {
        if (accountingLineRuleHelperService == null) {
            accountingLineRuleHelperService = SpringContext.getBean(AccountingLineRuleHelperService.class);
        }
        return accountingLineRuleHelperService;
    }

}
