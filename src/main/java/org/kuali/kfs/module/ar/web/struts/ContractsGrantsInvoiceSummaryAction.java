//KualiCo Patch Release 2020-02-13
//FINP-4769 FK-117 changes applied
/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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
package org.kuali.kfs.module.ar.web.struts;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.krad.service.KualiModuleService;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceDocumentErrorLog;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceLookupResult;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceLookupResultAward;
import org.kuali.kfs.module.ar.report.service.ContractsGrantsInvoiceReportService;
import org.kuali.kfs.module.ar.service.ContractsGrantsInvoiceCreateDocumentService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.SegmentedLookupResultsService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Action class for Contracts & Grants Invoice Summary.
 */
public class ContractsGrantsInvoiceSummaryAction extends ContractsGrantsBillingSummaryActionBase {
    private static volatile ContractsGrantsInvoiceReportService contractsGrantsInvoiceReportService;
    private static volatile SegmentedLookupResultsService segmentedLookupResultsService;

    /**
     * This method passes the control from Contracts & Grants Invoice lookup to the Contracts & Grants Invoice
     * Summary page and retrieves the list of selected awards by agency for creating invoices.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward viewSummary(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ContractsGrantsInvoiceSummaryForm contractsGrantsInvoiceSummaryForm = (ContractsGrantsInvoiceSummaryForm) form;
        String lookupResultsSequenceNumber = contractsGrantsInvoiceSummaryForm.getLookupResultsSequenceNumber();
        if (StringUtils.isNotBlank(lookupResultsSequenceNumber)) {
            String personId = GlobalVariables.getUserSession().getPerson().getPrincipalId();
            Collection<ContractsGrantsInvoiceLookupResult> contractsGrantsInvoiceLookupResults =
                    getContractsGrantsInvoiceResultsFromLookupResultsSequenceNumber(lookupResultsSequenceNumber, personId);

            contractsGrantsInvoiceSummaryForm.setContractsGrantsInvoiceLookupResults(contractsGrantsInvoiceLookupResults);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * This method would create invoices for the list of awards. It calls the batch process to reuse the functionality
     * to create the invoices.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward createInvoices(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        ContractsGrantsInvoiceSummaryForm contractsGrantsInvoiceSummaryForm = (ContractsGrantsInvoiceSummaryForm) form;
        ContractsGrantsInvoiceCreateDocumentService cgInvoiceDocumentCreateService =
                SpringContext.getBean(ContractsGrantsInvoiceCreateDocumentService.class);
        String lookupResultsSequenceNumber = "";
        String parameterName = (String) request.getAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
        if (StringUtils.isNotBlank(parameterName)) {
            lookupResultsSequenceNumber = StringUtils.substringBetween(parameterName, ".number", ".");
        }

        Collection<ContractsGrantsInvoiceLookupResult> lookupResults =
                getContractsGrantsInvoiceResultsFromLookupResultsSequenceNumber(lookupResultsSequenceNumber,
                        GlobalVariables.getUserSession().getPerson().getPrincipalId());

        contractsGrantsInvoiceSummaryForm.setAwardInvoiced(true);
        int validationErrors = 0;
        int validAwards = 0;

        // Create Invoices from list of Awards.
        List<ErrorMessage> errorMessages = null;
        for (ContractsGrantsInvoiceLookupResult contractsGrantsInvoiceLookupResult : lookupResults) {
            Collection<ContractsAndGrantsBillingAward> awards = setupSelectedAccountsForValidationAndInvoiceCreation(
                    contractsGrantsInvoiceLookupResult.getAwards(), contractsGrantsInvoiceLookupResult);
            Collection<ContractsGrantsInvoiceDocumentErrorLog> contractsGrantsInvoiceDocumentErrorLogs =
                    new ArrayList<>();
            awards = cgInvoiceDocumentCreateService.validateAwards(awards, contractsGrantsInvoiceDocumentErrorLogs,
                    null, ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType.MANUAL);
            validationErrors += contractsGrantsInvoiceDocumentErrorLogs.size();
            validAwards += awards.size();
            if (awards.size() > 0) {
                errorMessages = cgInvoiceDocumentCreateService.createCGInvoiceDocumentsByAwards(awards,
                        ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType.MANUAL);
            }
        }

        if (validationErrors > 0) {
            KNSGlobalVariables.getMessageList().add(ArKeyConstants.ContractsGrantsInvoiceConstants.ERROR_AWARDS_INVALID);
        }

        if (validAwards > 0) {
            KNSGlobalVariables.getMessageList().add(ArKeyConstants.ContractsGrantsInvoiceConstants
                    .MESSAGE_CONTRACTS_GRANTS_INVOICE_BATCH_SENT);
        }

        if (ObjectUtils.isNotNull(errorMessages)) {
            KNSGlobalVariables.getMessageList().addAll(errorMessages);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * For Milestone and Predetermined Billing, we only want to perform validation on and create invoices for
     * the award accounts the user selected. This sets up a list of the selected accounts on the awards to facilitate
     * that downstream.
     *
     * @param awards awards to process
     * @param contractsGrantsInvoiceLookupResult lookup result used to get the users selected award accounts
     * @return awards with lists of selected award accounts
     */
    private Collection<ContractsAndGrantsBillingAward> setupSelectedAccountsForValidationAndInvoiceCreation(
            Collection<ContractsAndGrantsBillingAward> awards,
            ContractsGrantsInvoiceLookupResult contractsGrantsInvoiceLookupResult) {

        for (ContractsAndGrantsBillingAward award: awards) {
            if (ArConstants.BillingFrequencyValues.isMilestone(award)
                    || ArConstants.BillingFrequencyValues.isPredeterminedBilling(award)) {
                for (ContractsGrantsInvoiceLookupResultAward lookupResultAward: contractsGrantsInvoiceLookupResult.getLookupResultAwards()) {
                    if (StringUtils.equals(award.getProposalNumber(), lookupResultAward.getProposalNumber())) {
                        award.getSelectedAccounts().add(lookupResultAward.getChartOfAccountsCode() + lookupResultAward.getAccountNumber());
                    }
                }
            }
        }

        return awards;
    }

    /**
     * @param lookupResultsSequenceNumber
     * @param personId
     * @return
     * @throws Exception
     */
    protected Collection<ContractsGrantsInvoiceLookupResult> getContractsGrantsInvoiceResultsFromLookupResultsSequenceNumber(String lookupResultsSequenceNumber, String personId) throws Exception {
        return getContractsGrantsInvoiceReportService().getPopulatedContractsGrantsInvoiceLookupResultsFromAwardAccounts(
                getAwardAccountsFromLookupResultsSequenceNumber(lookupResultsSequenceNumber, personId));
    }

    /**
     * Get the Awards based on what the user selected in the lookup results.
     *
     * @param lookupResultsSequenceNumber sequence number used to retrieve the lookup results
     * @param personId                    person who performed the lookup
     * @return Collection of ContractsAndGrantsBillingAwards
     * @throws Exception if there was a problem getting the selected proposal numbers from the lookup results
     */
    protected Collection<ContractsAndGrantsBillingAwardAccount> getAwardAccountsFromLookupResultsSequenceNumber(String lookupResultsSequenceNumber, String personId) throws Exception {
        KualiModuleService kualiModuleService = SpringContext.getBean(KualiModuleService.class);
        Collection<ContractsAndGrantsBillingAwardAccount> awardAccounts = new ArrayList<>();

        Set<String> selectedObjectIds = getSegmentedLookupResultsService()
                .retrieveSetOfSelectedObjectIds(lookupResultsSequenceNumber,
                        GlobalVariables.getUserSession().getPerson().getPrincipalId());

        for (String selectedObjectId : selectedObjectIds) {
            String[] keys = selectedObjectId.split("-");
            Map<String, Object> map = new HashMap<>();
            map.put(KFSPropertyConstants.PROPOSAL_NUMBER, keys[0]);
            map.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, keys[1]);
            map.put(KFSPropertyConstants.ACCOUNT_NUMBER, keys[2]);
            map.put(KFSPropertyConstants.ACTIVE, true);
            List<ContractsAndGrantsBillingAwardAccount> awardAccount = kualiModuleService.getResponsibleModuleService(
                    ContractsAndGrantsBillingAwardAccount.class).getExternalizableBusinessObjectsList(
                    ContractsAndGrantsBillingAwardAccount.class, map);
            if (ObjectUtils.isNotNull(awardAccount)) {
                awardAccounts.addAll(awardAccount);
            }
        }

        return awardAccounts;
    }

    /**
     * To cancel the document, invoices are not created when the cancel method is called.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward cancel(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return mapping.findForward(KFSConstants.MAPPING_CANCEL);
    }

    public static ContractsGrantsInvoiceReportService getContractsGrantsInvoiceReportService() {
        if (contractsGrantsInvoiceReportService == null) {
            contractsGrantsInvoiceReportService = SpringContext.getBean(ContractsGrantsInvoiceReportService.class);
        }
        return contractsGrantsInvoiceReportService;
    }

    public static SegmentedLookupResultsService getSegmentedLookupResultsService() {
        if (segmentedLookupResultsService == null) {
            segmentedLookupResultsService = SpringContext.getBean(SegmentedLookupResultsService.class);
        }
        return segmentedLookupResultsService;
    }
}
