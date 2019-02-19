package edu.cornell.kfs.module.ar.document.validation.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.businessobject.InvoiceAccountDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.dataaccess.ContractsGrantsInvoiceDocumentDao;
import org.kuali.kfs.module.ar.document.validation.SuspensionCategoryBase;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentService;
import org.kuali.rice.core.api.search.SearchOperator;

import edu.cornell.kfs.module.ar.CuArPropertyConstants;

public class FirstInvoiceForAwardSuspensionCategory extends SuspensionCategoryBase {

    private FinancialSystemDocumentService financialSystemDocumentService;
    private ContractsGrantsInvoiceDocumentDao contractsGrantsInvoiceDocumentDao;

    @Override
    public boolean shouldSuspend(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        return priorSuccessfulInvoicesDoNotExist(contractsGrantsInvoiceDocument);
    }

    protected boolean priorSuccessfulInvoicesDoNotExist(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        Map<String, String> criteria = new HashMap<>();
        String proposalNumber = contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getProposalNumber();
        String documentNumber = contractsGrantsInvoiceDocument.getDocumentNumber();
        ContractsAndGrantsBillingAward award = contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getAward();
        
        criteria.put(ArPropertyConstants.ContractsGrantsInvoiceDocumentFields.PROPOSAL_NUMBER, proposalNumber);
        criteria.put(KFSPropertyConstants.DOCUMENT_HEADER + KFSConstants.DELIMITER + KFSPropertyConstants.WORKFLOW_DOCUMENT_STATUS_CODE,
                buildSuccessfulDocumentStatusesCriteria());
        criteria.put(KFSPropertyConstants.DOCUMENT_NUMBER, SearchOperator.NOT.op() + documentNumber);
        
        Map<String, String> extraCriteria = buildAdditionalCriteriaForInvoicingOption(
                contractsGrantsInvoiceDocument, award.getInvoicingOptionCode());
        criteria.putAll(extraCriteria);
        
        Collection<ContractsGrantsInvoiceDocument> priorInvoices = contractsGrantsInvoiceDocumentDao.getMatchingInvoicesByCollection(criteria);
        return CollectionUtils.isEmpty(priorInvoices);
    }

    protected String buildSuccessfulDocumentStatusesCriteria() {
        Set<String> successfulStatuses = financialSystemDocumentService.getSuccessfulDocumentStatuses();
        return StringUtils.join(successfulStatuses, SearchOperator.OR.op());
    }

    protected Map<String, String> buildAdditionalCriteriaForInvoicingOption(
            ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument, String invoicingOption) {
        if (CollectionUtils.isEmpty(contractsGrantsInvoiceDocument.getAccountDetails())) {
            throw new IllegalStateException("No account details were found for CINV document "
                    + contractsGrantsInvoiceDocument.getDocumentNumber());
        }
        Map<String, String> extraCriteria = new HashMap<>();
        InvoiceAccountDetail firstAccountDetail = contractsGrantsInvoiceDocument.getAccountDetails().get(0);
        Account account = getAccountForInvoiceDetail(firstAccountDetail);
        
        switch (invoicingOption) {
            case ArConstants.INV_ACCOUNT :
            case ArConstants.INV_SCHEDULE :
                extraCriteria.put(CuArPropertyConstants.ContractsAndGrantsBillingAwardFields.ACCOUNT_DETAILS_CHART_OF_ACCOUNTS_CODE,
                        firstAccountDetail.getChartOfAccountsCode());
                extraCriteria.put(ArPropertyConstants.ACCOUNT_DETAILS_ACCOUNT_NUMBER,
                        firstAccountDetail.getAccountNumber());
                break;
            case ArConstants.INV_CONTRACT_CONTROL_ACCOUNT :
                extraCriteria.put(CuArPropertyConstants.ContractsAndGrantsBillingAwardFields.ACCOUNT_DETAILS_CONTRACT_CONTROL_CHART_OF_ACCOUNTS_CODE,
                        account.getContractControlFinCoaCode());
                extraCriteria.put(CuArPropertyConstants.ContractsAndGrantsBillingAwardFields.ACCOUNT_DETAILS_CONTRACT_CONTROL_ACCOUNT_NUMBER,
                        account.getContractControlAccountNumber());
                break;
            case ArConstants.INV_AWARD :
            default :
                break;
        }
        
        return extraCriteria;
    }

    protected Account getAccountForInvoiceDetail(InvoiceAccountDetail accountDetail) {
        Account account = accountDetail.getAccount();
        if (ObjectUtils.isNull(account)) {
            accountDetail.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);
            account = accountDetail.getAccount();
        }
        if (ObjectUtils.isNull(account)) {
            throw new IllegalStateException("Could not find account " + accountDetail.getChartOfAccountsCode()
                    + KFSConstants.DASH + accountDetail.getAccountNumber());
        }
        return account;
    }

    public void setFinancialSystemDocumentService(FinancialSystemDocumentService financialSystemDocumentService) {
        this.financialSystemDocumentService = financialSystemDocumentService;
    }

    public void setContractsGrantsInvoiceDocumentDao(ContractsGrantsInvoiceDocumentDao contractsGrantsInvoiceDocumentDao) {
        this.contractsGrantsInvoiceDocumentDao = contractsGrantsInvoiceDocumentDao;
    }

}
