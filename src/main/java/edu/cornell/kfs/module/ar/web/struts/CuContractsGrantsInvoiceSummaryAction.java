package edu.cornell.kfs.module.ar.web.struts;

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
import org.kuali.kfs.module.ar.web.struts.ContractsGrantsInvoiceSummaryAction;
import org.kuali.kfs.module.ar.web.struts.ContractsGrantsInvoiceSummaryForm;
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

public class CuContractsGrantsInvoiceSummaryAction extends ContractsGrantsInvoiceSummaryAction {
    
    //CUMod: Base code method copied locally so that private method could be modified.
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
     * CU Customization 
     * 
     * Obtain the award accouts the user selected to perform validation on and create invoices for.
     * This sets up a list of the selected accounts on the awards to facilitate that downstream.
     * CU functional users desire that 
     *
     * @param awards awards to process
     * @param contractsGrantsInvoiceLookupResult lookup result used to get the users selected award accounts
     * @return awards with lists of selected award accounts
     */
    private Collection<ContractsAndGrantsBillingAward> setupSelectedAccountsForValidationAndInvoiceCreation(
            Collection<ContractsAndGrantsBillingAward> awards,
            ContractsGrantsInvoiceLookupResult contractsGrantsInvoiceLookupResult) {

        for (ContractsAndGrantsBillingAward award: awards) {
            for (ContractsGrantsInvoiceLookupResultAward lookupResultAward: contractsGrantsInvoiceLookupResult.getLookupResultAwards()) {
                    if (StringUtils.equals(award.getProposalNumber(), lookupResultAward.getProposalNumber())) {
                        award.getSelectedAccounts().add(lookupResultAward.getChartOfAccountsCode() + lookupResultAward.getAccountNumber());
                    }
            }
        }
        return awards;
    }
    
}
