package edu.cornell.kfs.sys.fixture;

import static org.kuali.kfs.sys.KFSConstants.GL_CREDIT_CODE;
import static org.kuali.kfs.sys.KFSConstants.GL_DEBIT_CODE;

import org.kuali.kfs.fp.businessobject.VoucherSourceAccountingLine;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.context.IntegTestUtils;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

public enum CuAccountingLineFixture {
    ST_LINE_1(1, "IT", "R704750", "AC", "", "5200", "", "", "", "", "", "", "", GL_DEBIT_CODE, "6916.68"),
    ST_LINE_2(1, "IT", "L683714", "AC", "", "5200", "", "", "", "", "", "", "", GL_CREDIT_CODE, "6916.68"),
    ICA_LINE_1(1, "IT", "G264750", "", "", "9070", "", "", "", "", "", "", "", GL_DEBIT_CODE, "6916.68"),
    ICA_LINE_2(1, "IT", "G254700", "", "", "4290", "", "", "", "", "", "", "", GL_CREDIT_CODE, "6916.68");

    public final String accountNumber;
    public final String balanceTypeCode;
    public final String chartOfAccountsCode;
    public final String debitCreditCode;
    public final String encumbranceUpdateCode;
    public final String financialObjectCode;
    public final String financialSubObjectCode;
    public final String organizationReferenceId;
    public final String projectCode;
    public final String referenceOriginCode;
    public final String referenceNumber;
    public final String referenceTypeCode;
    public final String subAccountNumber;
    public final KualiDecimal amount;
    public final Integer postingYear;
    public final Integer sequenceNumber;


    CuAccountingLineFixture(Integer sequenceNumber, String chartOfAccountsCode, String accountNumber, String balanceTypeCode, String subAccountNumber, String financialObjectCode, String financialSubObjectCode, String projectCode, String encumbranceUpdateCode, String organizationReferenceId, String referenceOriginCode, String referenceNumber, String referenceTypeCode, String debitCreditCode, String amount) {

        postingYear = IntegTestUtils.getFiscalYearForTesting();
        this.sequenceNumber = sequenceNumber;
        this.accountNumber = accountNumber;
        this.balanceTypeCode = balanceTypeCode;
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.debitCreditCode = debitCreditCode;
        this.encumbranceUpdateCode = encumbranceUpdateCode;
        this.financialObjectCode = financialObjectCode;
        this.financialSubObjectCode = financialSubObjectCode;
        this.organizationReferenceId = organizationReferenceId;
        this.projectCode = projectCode;
        this.referenceOriginCode = referenceOriginCode;
        this.referenceNumber = referenceNumber;
        this.referenceTypeCode = referenceTypeCode;
        this.subAccountNumber = subAccountNumber;
        this.amount = new KualiDecimal(amount);
    }

    private <T extends AccountingLine> T createAccountingLine(final Class<T> lineClass) throws InstantiationException, IllegalAccessException {
        return createAccountingLine(lineClass, "", this.postingYear, this.sequenceNumber);
    }

    public <T extends AccountingLine> T createAccountingLine(final Class<T> lineClass, final String debitCreditCode) throws InstantiationException, IllegalAccessException {
        T line = createAccountingLine(lineClass, "", this.postingYear, this.sequenceNumber);
        line.setDebitCreditCode(debitCreditCode);
        return line;
    }

    public <T extends AccountingLine> T createAccountingLine(final Class<T> lineClass, final String documentNumber, final Integer postingYear, final Integer sequenceNumber) throws InstantiationException, IllegalAccessException {
        final T line = createLine(lineClass);

        line.setDocumentNumber(documentNumber);
        line.setPostingYear(postingYear);
        line.setSequenceNumber(sequenceNumber);

        line.refresh();
        return line;
    }

    private <T extends AccountingLine> T createLine(final Class<T> lineClass) throws InstantiationException, IllegalAccessException {
        final T line = (T) lineClass.newInstance();
        line.setAccountNumber(this.accountNumber);
        line.setAmount(this.amount);
        line.setBalanceTypeCode(this.balanceTypeCode);
        line.setChartOfAccountsCode(this.chartOfAccountsCode);
        line.setDebitCreditCode(this.debitCreditCode);
        line.setEncumbranceUpdateCode(this.encumbranceUpdateCode);
        line.setFinancialObjectCode(this.financialObjectCode);
        line.setFinancialSubObjectCode(this.financialSubObjectCode);
        line.setOrganizationReferenceId(this.organizationReferenceId);
        line.setProjectCode(this.projectCode);
        line.setReferenceOriginCode(this.referenceOriginCode);
        line.setReferenceNumber(this.referenceNumber);
        line.setReferenceTypeCode(this.referenceTypeCode);
        line.setSubAccountNumber(this.subAccountNumber);

        return line;
    }

    public SourceAccountingLine createSourceAccountingLine() throws InstantiationException, IllegalAccessException {
        return createAccountingLine(SourceAccountingLine.class);
    }
    
    public VoucherSourceAccountingLine createVoucherSourceAccountingLine() throws InstantiationException, IllegalAccessException {
        final VoucherSourceAccountingLine line = createAccountingLine(VoucherSourceAccountingLine.class);
        line.refreshReferenceObject("objectCode");
        line.setObjectTypeCode(line.getObjectCode().getFinancialObjectTypeCode());
        return line;
    }

    public TargetAccountingLine createTargetAccountingLine() throws InstantiationException, IllegalAccessException {
        return createAccountingLine(TargetAccountingLine.class);
    }

    public void addAsSourceTo(final AccountingDocument document) throws IllegalAccessException, InstantiationException {
        document.addSourceAccountingLine(createAccountingLine(SourceAccountingLine.class, document.getDocumentNumber(), document.getPostingYear(), document.getNextSourceLineNumber()));
    }
    
    public void addAsVoucherSourceTo(final AccountingDocument document) throws IllegalAccessException, InstantiationException {
        final VoucherSourceAccountingLine line = createAccountingLine(VoucherSourceAccountingLine.class, document.getDocumentNumber(), document.getPostingYear(), document.getNextSourceLineNumber());
        line.refreshReferenceObject("objectCode");
        line.setObjectTypeCode(line.getObjectCode().getFinancialObjectTypeCode());
        document.addSourceAccountingLine(line);
    }

    public void addAsTargetTo(final AccountingDocument document) throws IllegalAccessException, InstantiationException {
        document.addTargetAccountingLine(createAccountingLine(TargetAccountingLine.class, document.getDocumentNumber(), document.getPostingYear(), document.getNextTargetLineNumber()));
    }
}
