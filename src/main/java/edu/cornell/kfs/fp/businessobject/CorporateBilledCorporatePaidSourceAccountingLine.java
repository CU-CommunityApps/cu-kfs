package edu.cornell.kfs.fp.businessobject;

import org.kuali.kfs.fp.businessobject.ProcurementCardSourceAccountingLine;

public class CorporateBilledCorporatePaidSourceAccountingLine extends ProcurementCardSourceAccountingLine {
    private static final long serialVersionUID = -1280185559910261976L;
    
    public CorporateBilledCorporatePaidSourceAccountingLine() {
        super();
    }
    
    public CorporateBilledCorporatePaidSourceAccountingLine(ProcurementCardSourceAccountingLine pCardLine, String documentNumber) {
        this();
        setAccountExpiredOverride(pCardLine.getAccountExpiredOverride());
        setAccountExpiredOverrideNeeded(pCardLine.getAccountExpiredOverrideNeeded());
        setAccountNumber(pCardLine.getAccountNumber());
        setAmount(pCardLine.getAmount());
        setBalanceTypeCode(pCardLine.getBalanceTypeCode());
        setChartOfAccountsCode(pCardLine.getChartOfAccountsCode());
        setDebitCreditCode(pCardLine.getDebitCreditCode());
        setDocumentNumber(documentNumber);
        setEncumbranceUpdateCode(pCardLine.getEncumbranceUpdateCode());
        setFinancialDocumentLineDescription(pCardLine.getFinancialDocumentLineDescription());
        setFinancialDocumentLineTypeCode(pCardLine.getFinancialDocumentLineTypeCode());
        setFinancialDocumentTransactionLineNumber(pCardLine.getFinancialDocumentTransactionLineNumber());
        setFinancialObjectCode(pCardLine.getFinancialObjectCode());
        setFinancialSubObjectCode(pCardLine.getFinancialSubObjectCode());
        setLastUpdatedTimestamp(pCardLine.getLastUpdatedTimestamp());
        setNewCollectionRecord(pCardLine.isNewCollectionRecord());
        setNonFringeAccountOverride(pCardLine.getNonFringeAccountOverride());
        setNonFringeAccountOverrideNeeded(pCardLine.getNonFringeAccountOverrideNeeded());
        setObjectBudgetOverride(pCardLine.isObjectBudgetOverride());
        setOrganizationReferenceId(pCardLine.getOrganizationReferenceId());
        setOverrideCode(pCardLine.getOverrideCode());
        setPostingYear(pCardLine.getPostingYear());
        setProjectCode(pCardLine.getProjectCode());
        setReferenceNumber(pCardLine.getReferenceNumber());
        setReferenceOriginCode(pCardLine.getReferenceOriginCode());
        setReferenceTypeCode(pCardLine.getReferenceTypeCode());
        setSalesTaxRequired(pCardLine.isSalesTaxRequired());
        setSequenceNumber(pCardLine.getSequenceNumber());
        setSubAccountNumber(pCardLine.getSubAccountNumber());
        setVersionNumber(pCardLine.getVersionNumber());
    }

}
