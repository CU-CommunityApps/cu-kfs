/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.module.purap.document.web.struts;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kew.api.KewApiServiceLocator;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.api.document.DocumentRefreshQueue;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kns.datadictionary.DocumentEntry;
import org.kuali.kfs.kns.question.ConfirmationQuestion;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.KualiRuleService;
import org.kuali.kfs.krad.service.PersistenceService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.RequisitionStatuses;
import org.kuali.kfs.module.purap.businessobject.DefaultPrincipalAddress;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.RequisitionItem;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.document.PurchasingDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.service.RequisitionService;
import org.kuali.kfs.module.purap.document.validation.event.AttributedAddPurchasingAccountsPayableItemEvent;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.FinancialSystemUserService;
import org.kuali.kfs.sys.service.FinancialSystemWorkflowHelperService;
import org.kuali.kfs.vnd.businessobject.VendorCommodityCode;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * CU Customization: Backported the FINP-9050 changes into this file, adjusting for compatibility as needed.
 * This overlay can tentatively be removed when we upgrade to the 2023-02-08 financials patch.
 * 
 * NOTE: The new annotation message has a typo in base code at the time of this writing. We have fixed the typo
 * in this overlay for now, though we don't appear to be using the related Organization routing at the moment.
 * If we're still not using the REQS Organization routing when we upgrade to the 2023-02-08 patch or later,
 * then it should be safe to remove this overlay despite the typo.
 * ============
 * 
 * Struts Action for Requisition document.
 */
public class RequisitionAction extends PurchasingActionBase {

    private static final Logger LOG = LogManager.getLogger();
    private RequisitionService requisitionService;
    private FinancialSystemWorkflowHelperService financialSystemWorkflowHelperService;

    /**
     * save the document without any validations.....
     */
    @Override
    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        //call the super save to save the document without validations...
        super.save(mapping, form, request, response);

        // we need to make "calculated" to false so that the "below lines" can be edited until calculated button is
        // clicked.
        KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        PurchasingFormBase baseForm = (PurchasingFormBase) form;
        PurchasingAccountsPayableDocument purapDocument =
                (PurchasingAccountsPayableDocument) kualiDocumentFormBase.getDocument();

        baseForm.setCalculated(false);
        purapDocument.setCalculated(false);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Does initialization for a new requisition.
     */
    @Override
    protected void createDocument(KualiDocumentFormBase kualiDocumentFormBase) {
        super.createDocument(kualiDocumentFormBase);
        ((RequisitionDocument) kualiDocumentFormBase.getDocument()).initiateDocument();
    }

    public ActionForward setAsDefaultBuilding(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                              HttpServletResponse response) throws Exception {
        RequisitionDocument req = (RequisitionDocument) ((RequisitionForm) form).getDocument();

        if (ObjectUtils.isNotNull(req.getDeliveryCampusCode())
                && ObjectUtils.isNotNull(req.getDeliveryBuildingCode())) {
            DefaultPrincipalAddress defaultPrincipalAddress = new DefaultPrincipalAddress(
                    GlobalVariables.getUserSession().getPerson().getPrincipalId());
            Map addressKeys = SpringContext.getBean(PersistenceService.class)
                    .getPrimaryKeyFieldValues(defaultPrincipalAddress);
            defaultPrincipalAddress = SpringContext.getBean(BusinessObjectService.class)
                    .findByPrimaryKey(DefaultPrincipalAddress.class, addressKeys);

            if (ObjectUtils.isNull(defaultPrincipalAddress)) {
                defaultPrincipalAddress = new DefaultPrincipalAddress(
                        GlobalVariables.getUserSession().getPerson().getPrincipalId());
            }

            defaultPrincipalAddress.setDefaultBuilding(req.getDeliveryCampusCode(), req.getDeliveryBuildingCode(),
                    req.getDeliveryBuildingRoomNumber());
            SpringContext.getBean(BusinessObjectService.class).save(defaultPrincipalAddress);
            KNSGlobalVariables.getMessageList().add(PurapKeyConstants.DEFAULT_BUILDING_SAVED);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    @Override
    public ActionForward refresh(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        ActionForward forward = super.refresh(mapping, form, request, response);
        RequisitionForm rqForm = (RequisitionForm) form;
        RequisitionDocument document = (RequisitionDocument) rqForm.getDocument();

        // super.refresh() must occur before this line to get the correct APO limit
        document.setOrganizationAutomaticPurchaseOrderLimit(SpringContext.getBean(PurapService.class)
                .getApoLimit(document.getVendorContractGeneratedIdentifier(), document.getChartOfAccountsCode(),
                        document.getOrganizationCode()));
        return forward;
    }

    @Override
    public ActionForward approve(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        RequisitionForm rqForm = (RequisitionForm) form;
        RequisitionDocument document = (RequisitionDocument) rqForm.getDocument();
        if (document.getDocumentHeader().getWorkflowDocument().getCurrentNodeNames()
                .contains(RequisitionStatuses.NODE_CONTENT_REVIEW)) {

            boolean approver = false;

            final String principalId = GlobalVariables.getUserSession().getPrincipalId();
            final WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();

            final ChartOrgHolder initiatorPrimaryOrg = SpringContext.getBean(FinancialSystemUserService.class)
                    .getPrimaryOrganization(workflowDocument.getInitiatorPrincipalId(), PurapConstants.PURAP_NAMESPACE);

            //Yes, these 3 if's could be combined to remove the approver flag, but this seems to be more readable.
            if (document.getChartOfAccountsCode().equals(initiatorPrimaryOrg.getChartOfAccountsCode()) ||
                        document.getOrganizationCode().equals(initiatorPrimaryOrg.getOrganizationCode())) {
                approver = true;
            }

            if (getRequisitionService().getContentReviewers(
                    document.getOrganizationCode(), document.getChartOfAccountsCode())
                    .stream()
                    .anyMatch(roleMembership -> roleMembership.getMemberId().equals(principalId))) {
                approver = true;
            }

            if (getFinancialSystemWorkflowHelperService().isAdhocApprovalRequestedForPrincipal(
                    workflowDocument, principalId)) {
                approver = true;
            }

            if (!approver) {
                //Org was updated at the content review node, so it needs to reroute to the new approver.
                //we will attempt to swallow that action here and just reload the document at the end.
                getDocumentService().saveDocument(document);
                DocumentRouteHeaderValue routeHeader = KEWServiceLocator.getRouteHeaderService()
                        .getRouteHeader(document.getDocumentNumber());
                DocumentRefreshQueue docRequeue = KewApiServiceLocator
                        .getDocumentRequeuerService(routeHeader.getDocumentId(), 0L);
                docRequeue.refreshDocument(
                        routeHeader.getDocumentId(),
                        "Document was requeued in RequisitionAction because Org was updated at the content review node."
                );

                sendAdHocRequests(mapping, form, request, response);

                return returnToSender(request, mapping, rqForm);
            }
        }
        return super.approve(mapping, form, request, response);
    }

    public String getRoleName() {
        return "Content Reviewer";
    }

    public String getRoleNamespace() {
        return PurapConstants.PURAP_NAMESPACE;
    }

    /**
     * Adds a PurchasingItemCapitalAsset (a container for the Capital Asset Number) to the selected item's list.
     *
     * @param mapping  An ActionMapping
     * @param form     The Form
     * @param request  An HttpServletRequest
     * @param response The HttpServletResponse
     * @return An ActionForward
     * @throws Exception
     */
    public ActionForward addAsset(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                  HttpServletResponse response) throws Exception {
        RequisitionForm rqForm = (RequisitionForm) form;
        RequisitionDocument document = (RequisitionDocument) rqForm.getDocument();
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward displayB2BRequisition(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                               HttpServletResponse response) throws Exception {
        RequisitionForm reqForm = (RequisitionForm) form;
        reqForm.setDocId((String) request.getSession().getAttribute("docId"));
        loadDocument(reqForm);
        String multipleB2BReqs = (String) request.getSession().getAttribute("multipleB2BRequisitions");
        if (StringUtils.isNotEmpty(multipleB2BReqs)) {
            KNSGlobalVariables.getMessageList().add(PurapKeyConstants.B2B_MULTIPLE_REQUISITIONS);
        }
        request.getSession().removeAttribute("docId");
        request.getSession().removeAttribute("multipleB2BRequisitions");

        // attach any extra JS from the data dictionary
        if (reqForm.getAdditionalScriptFiles().isEmpty()) {
            DocumentEntry docEntry = getDocumentDictionaryService().getDocumentEntry(
                    reqForm.getDocument().getDocumentHeader().getWorkflowDocument().getDocumentTypeName());
            reqForm.getAdditionalScriptFiles().addAll(docEntry.getWebScriptFiles());
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Clears the vendor selection from the Requisition.  NOTE, this functionality is only available on Requisition
     * and not PO.
     *
     * @param mapping  An ActionMapping
     * @param form     An ActionForm
     * @param request  A HttpServletRequest
     * @param response A HttpServletResponse
     * @return An ActionForward
     * @throws Exception
     */
    public ActionForward clearVendor(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                     HttpServletResponse response) throws Exception {
        PurchasingFormBase baseForm = (PurchasingFormBase) form;
        RequisitionDocument document = (RequisitionDocument) baseForm.getDocument();

        document.setVendorHeaderGeneratedIdentifier(null);
        document.setVendorDetailAssignedIdentifier(null);
        document.setVendorDetail(null);
        document.setVendorName("");
        document.setVendorLine1Address("");
        document.setVendorLine2Address("");
        document.setVendorAddressInternationalProvinceName("");
        document.setVendorCityName("");
        document.setVendorStateCode("");
        document.setVendorPostalCode("");
        document.setVendorCountryCode("");
        document.setVendorContractGeneratedIdentifier(null);
        document.setVendorContract(null);
        document.setVendorFaxNumber("");
        document.setVendorCustomerNumber("");
        document.setVendorAttentionName("");
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Set up blanket approve indicator which will be used to decide if need to run accounting line validation at the
     * time of blanket approve.
     */
    @Override
    public ActionForward blanketApprove(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                        HttpServletResponse response) throws Exception {
        RequisitionDocument document = (RequisitionDocument) ((PurchasingFormBase) form).getDocument();
        document.setBlanketApproveRequest(true);
        return super.blanketApprove(mapping, form, request, response);
    }

    /**
     * Add a new item to the document.
     *
     * @param mapping  An ActionMapping
     * @param form     An ActionForm
     * @param request  The HttpServletRequest
     * @param response The HttpServletResponse
     * @return An ActionForward
     * @throws Exception
     */
    @Override
    public ActionForward addItem(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        PurApItem item = purchasingForm.getNewPurchasingItemLine();
        RequisitionItem requisitionItem = (RequisitionItem) item;
        PurchasingDocument purDocument = (PurchasingDocument) purchasingForm.getDocument();

        if (StringUtils.isBlank(requisitionItem.getPurchasingCommodityCode())) {
            boolean commCodeParam = SpringContext.getBean(ParameterService.class).getParameterValueAsBoolean(
                    RequisitionDocument.class, PurapParameterConstants.ENABLE_DEFAULT_VENDOR_COMMODITY_CODE_IND);

            if (commCodeParam && purchasingForm instanceof RequisitionForm) {
                RequisitionDocument reqs = (RequisitionDocument) purchasingForm.getDocument();
                VendorDetail dtl = reqs.getVendorDetail();
                if (ObjectUtils.isNotNull(dtl)) {
                    List<VendorCommodityCode> vcc = dtl.getVendorCommodities();
                    for (VendorCommodityCode commodity : vcc) {
                        if (commodity.isCommodityDefaultIndicator()) {
                            requisitionItem.setPurchasingCommodityCode(commodity.getPurchasingCommodityCode());
                        }
                    }
                }
            }
        }

        boolean rulePassed = SpringContext.getBean(KualiRuleService.class)
                .applyRules(new AttributedAddPurchasingAccountsPayableItemEvent("", purDocument, item));

        if (rulePassed) {
            item = purchasingForm.getAndResetNewPurchasingItemLine();
            purDocument.addItem(item);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    @Override
    public ActionForward route(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                               HttpServletResponse response) throws Exception {
        if (shouldWarnIfNoAccountingLines(form)) {
            String question = request.getParameter(KRADConstants.QUESTION_INST_ATTRIBUTE_NAME);
            if (StringUtils.equals(question, PurapConstants.REQUISITION_ACCOUNTING_LINES_QUESTION)) {
                // We're getting an answer from our question
                String answer = request.getParameter(KRADConstants.QUESTION_CLICKED_BUTTON);
                // if the answer is "yes"- continue routing, but if it isn't...
                if (!StringUtils.equals(answer, ConfirmationQuestion.YES)) {
                    // answer is "no, don't continue." so we'll just add a warning and refresh the page
                    LOG.info("add a warning and refresh the page ");
                    GlobalVariables.getMessageMap().putWarning(PurapConstants.ITEM_TAB_ERROR_PROPERTY,
                            PurapConstants.REQ_NO_ACCOUNTING_LINES);
                    return refresh(mapping, form, request, response);
                }
            } else {
                // We have an empty item and we have a content reviewer. We will now ask the user if he wants to
                // ignore the empty item (and let the content reviewer take care of it later).
                return this.performQuestionWithoutInput(mapping, form, request, response,
                        PurapConstants.REQUISITION_ACCOUNTING_LINES_QUESTION,
                        PurapConstants.QUESTION_REQUISITON_ROUTE_WITHOUT_ACCOUNTING_LINES,
                        KRADConstants.CONFIRMATION_QUESTION, KFSConstants.ROUTE_METHOD, "1");
            }
        }
        return super.route(mapping, form, request, response);
    }

    protected boolean shouldWarnIfNoAccountingLines(ActionForm form) {
        RequisitionDocument doc = (RequisitionDocument) ((PurchasingFormBase) form).getDocument();
        RequisitionService reqs = getRequisitionService();
        return doc.isMissingAccountingLines() && !reqs.getContentReviewers(doc.getOrganizationCode(),
                doc.getChartOfAccountsCode()).isEmpty();
    }

    protected synchronized RequisitionService getRequisitionService() {
        if (this.requisitionService == null) {
            this.requisitionService = SpringContext.getBean(RequisitionService.class);
        }
        return this.requisitionService;
    }

    protected synchronized FinancialSystemWorkflowHelperService getFinancialSystemWorkflowHelperService() {
        if (financialSystemWorkflowHelperService == null) {
            financialSystemWorkflowHelperService = SpringContext.getBean(FinancialSystemWorkflowHelperService.class);
        }
        return financialSystemWorkflowHelperService;
    }

}
