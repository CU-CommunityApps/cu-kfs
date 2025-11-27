package edu.cornell.kfs.module.ar.document.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.businessobject.InvoiceAccountDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.dataaccess.ContractsGrantsInvoiceDocumentDao;
import org.kuali.kfs.module.ar.document.service.SuspensionCategoryService;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentService;
import org.kuali.kfs.core.api.search.SearchOperator;

import edu.cornell.kfs.module.ar.CuArPropertyConstants;

public class FirstInvoiceForAwardSuspensionCategoryServiceImpl implements SuspensionCategoryService {

    private FinancialSystemDocumentService financialSystemDocumentService;
    private ContractsGrantsInvoiceDocumentDao contractsGrantsInvoiceDocumentDao;

    @Override
    public boolean shouldSuspend(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        return !priorSuccessfulInvoicesExist(contractsGrantsInvoiceDocument);
    }

    protected boolean priorSuccessfulInvoicesExist(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        Map<String, String> criteria = new HashMap<>();
        String proposalNumber = contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getProposalNumber();
        String documentNumber = contractsGrantsInvoiceDocument.getDocumentNumber();
        Award award = contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getAward();
        
        criteria.put(ArPropertyConstants.ContractsGrantsInvoiceDocumentFields.PROPOSAL_NUMBER, proposalNumber);
        criteria.put(KFSPropertyConstants.DOCUMENT_HEADER + KFSConstants.DELIMITER + KFSPropertyConstants.WORKFLOW_DOCUMENT_STATUS_CODE,
                buildSuccessfulDocumentStatusesCriteria());
        criteria.put(KFSPropertyConstants.DOCUMENT_NUMBER, SearchOperator.NOT.op() + documentNumber);
        
        Map<String, String> extraCriteria = buildAdditionalCriteriaForInvoicingOption(
                contractsGrantsInvoiceDocument, award.getInvoicingOptionCode());
        criteria.putAll(extraCriteria);
        
        Collection<ContractsGrantsInvoiceDocument> priorInvoices = contractsGrantsInvoiceDocumentDao.getMatchingInvoicesByCollection(criteria);
        return CollectionUtils.isNotEmpty(priorInvoices);
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
        InvoiceAccountDetail firstAccountDetail = contractsGrantsInvoiceDocument.getAccountDetails().get(0);
        
        switch (invoicingOption) {
            case ArConstants.INV_ACCOUNT :
            case ArConstants.INV_SCHEDULE :
                return buildCriteriaToMatchSoleAccountForInvoice(firstAccountDetail);
            case ArConstants.INV_CONTRACT_CONTROL_ACCOUNT :
                return buildCriteriaToMatchSoleContractControlAccountForInvoice(firstAccountDetail);
            case ArConstants.INV_AWARD :
            default :
                return Collections.emptyMap();
        }
    }

    protected Map<String, String> buildCriteriaToMatchSoleAccountForInvoice(InvoiceAccountDetail firstAccountDetailForInvoice) {
        Map<String, String> criteria = new HashMap<>();
        criteria.put(CuArPropertyConstants.ContractsGrantsInvoiceAccountDetailFields.CHART_OF_ACCOUNTS_CODE,
                firstAccountDetailForInvoice.getChartOfAccountsCode());
        criteria.put(CuArPropertyConstants.ContractsGrantsInvoiceAccountDetailFields.ACCOUNT_NUMBER,
                firstAccountDetailForInvoice.getAccountNumber());
        return criteria;
    }

    protected Map<String, String> buildCriteriaToMatchSoleContractControlAccountForInvoice(InvoiceAccountDetail firstAccountDetailForInvoice) {
        Account firstAccountForInvoice = getAccountForInvoiceDetail(firstAccountDetailForInvoice);
        Map<String, String> criteria = new HashMap<>();
        criteria.put(CuArPropertyConstants.ContractsGrantsInvoiceAccountDetailFields.CONTRACT_CONTROL_CHART_OF_ACCOUNTS_CODE,
                firstAccountForInvoice.getContractControlFinCoaCode());
        criteria.put(CuArPropertyConstants.ContractsGrantsInvoiceAccountDetailFields.CONTRACT_CONTROL_ACCOUNT_NUMBER,
                firstAccountForInvoice.getContractControlAccountNumber());
        return criteria;
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
