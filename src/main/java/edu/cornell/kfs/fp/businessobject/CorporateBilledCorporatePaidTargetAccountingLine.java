package edu.cornell.kfs.fp.businessobject;

import org.kuali.kfs.fp.businessobject.ProcurementCardTargetAccountingLine;

public class CorporateBilledCorporatePaidTargetAccountingLine extends ProcurementCardTargetAccountingLine {
    private static final long serialVersionUID = -3090715460211979834L;

    public CorporateBilledCorporatePaidTargetAccountingLine() {
        super();
    }

    public CorporateBilledCorporatePaidTargetAccountingLine(ProcurementCardTargetAccountingLine pCardTargetLine, String documentNumber) {
        this();
        setDocumentNumber(documentNumber);
        setAccountNumber(pCardTargetLine.getAccountNumber());
        setChartOfAccountsCode(pCardTargetLine.getChartOfAccountsCode());
        setSubAccountNumber(pCardTargetLine.getSubAccountNumber());
        setFinancialObjectCode(pCardTargetLine.getFinancialObjectCode());
        setFinancialSubObjectCode(pCardTargetLine.getFinancialSubObjectCode());
        setOrganizationReferenceId(organizationReferenceId);
        setProjectCode(pCardTargetLine.getProjectCode());
        setAmount(pCardTargetLine.getAmount());
        setFinancialDocumentLineDescription(pCardTargetLine.getFinancialDocumentLineDescription());
        setFinancialDocumentTransactionLineNumber(pCardTargetLine.getFinancialDocumentTransactionLineNumber());
        setSequenceNumber(pCardTargetLine.getSequenceNumber());
    }

}
