package edu.cornell.kfs.fp.businessobject;

import org.kuali.kfs.fp.businessobject.ProcurementCardSourceAccountingLine;

public class CorporateBilledCorporatePaidSourceAccountingLine extends ProcurementCardSourceAccountingLine {
    private static final long serialVersionUID = -1280185559910261976L;
    
    public CorporateBilledCorporatePaidSourceAccountingLine() {
        super();
    }
    
    public CorporateBilledCorporatePaidSourceAccountingLine(ProcurementCardSourceAccountingLine pCardSourceLine, String documentNumber) {
        this();
        setDocumentNumber(documentNumber);
        setAccountNumber(pCardSourceLine.getAccountNumber());
        setChartOfAccountsCode(pCardSourceLine.getChartOfAccountsCode());
        setSubAccountNumber(pCardSourceLine.getSubAccountNumber());
        setFinancialObjectCode(pCardSourceLine.getFinancialObjectCode());
        setFinancialSubObjectCode(pCardSourceLine.getFinancialSubObjectCode());
        setOrganizationReferenceId(organizationReferenceId);
        setProjectCode(pCardSourceLine.getProjectCode());
        setAmount(pCardSourceLine.getAmount());
        setFinancialDocumentLineDescription(pCardSourceLine.getFinancialDocumentLineDescription());
        setFinancialDocumentTransactionLineNumber(pCardSourceLine.getFinancialDocumentTransactionLineNumber());
        setSequenceNumber(pCardSourceLine.getSequenceNumber());
    }

}
