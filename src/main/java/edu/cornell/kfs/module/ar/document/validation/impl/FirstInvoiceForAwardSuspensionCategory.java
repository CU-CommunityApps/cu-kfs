package edu.cornell.kfs.module.ar.document.validation.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.dataaccess.ContractsGrantsInvoiceDocumentDao;
import org.kuali.kfs.module.ar.document.validation.SuspensionCategoryBase;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentService;
import org.kuali.rice.core.api.search.SearchOperator;

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
        
        criteria.put(ArPropertyConstants.ContractsGrantsInvoiceDocumentFields.PROPOSAL_NUMBER, proposalNumber);
        criteria.put(KFSPropertyConstants.DOCUMENT_HEADER + KFSConstants.DELIMITER + KFSPropertyConstants.WORKFLOW_DOCUMENT_STATUS_CODE,
                buildSuccessfulDocumentStatusesCriteria());
        criteria.put(KFSPropertyConstants.DOCUMENT_NUMBER, SearchOperator.NOT.op() + documentNumber);
        
        Collection<ContractsGrantsInvoiceDocument> priorInvoices = contractsGrantsInvoiceDocumentDao.getMatchingInvoicesByCollection(criteria);
        return CollectionUtils.isEmpty(priorInvoices);
    }

    protected String buildSuccessfulDocumentStatusesCriteria() {
        Set<String> successfulStatuses = financialSystemDocumentService.getSuccessfulDocumentStatuses();
        return StringUtils.join(successfulStatuses, SearchOperator.OR.op());
    }

    public void setFinancialSystemDocumentService(FinancialSystemDocumentService financialSystemDocumentService) {
        this.financialSystemDocumentService = financialSystemDocumentService;
    }

    public void setContractsGrantsInvoiceDocumentDao(ContractsGrantsInvoiceDocumentDao contractsGrantsInvoiceDocumentDao) {
        this.contractsGrantsInvoiceDocumentDao = contractsGrantsInvoiceDocumentDao;
    }

}
