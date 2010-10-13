package edu.cornell.kfs.sys.businessobject;

import static org.kuali.kfs.sys.KFSPropertyConstants.ACCOUNT_NUMBER;
import static org.kuali.kfs.sys.KFSPropertyConstants.BASE_BUDGET_ADJUSTMENT_AMOUNT;
import static org.kuali.kfs.sys.KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE;
import static org.kuali.kfs.sys.KFSPropertyConstants.CURRENT_BUDGET_ADJUSTMENT_AMOUNT;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_DOCUMENT_LINE_DESCRIPTION;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_DOCUMENT_MONTH_10_LINE_AMOUNT;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_DOCUMENT_MONTH_11_LINE_AMOUNT;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_DOCUMENT_MONTH_12_LINE_AMOUNT;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_DOCUMENT_MONTH_1_LINE_AMOUNT;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_DOCUMENT_MONTH_2_LINE_AMOUNT;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_DOCUMENT_MONTH_3_LINE_AMOUNT;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_DOCUMENT_MONTH_4_LINE_AMOUNT;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_DOCUMENT_MONTH_5_LINE_AMOUNT;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_DOCUMENT_MONTH_6_LINE_AMOUNT;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_DOCUMENT_MONTH_7_LINE_AMOUNT;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_DOCUMENT_MONTH_8_LINE_AMOUNT;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_DOCUMENT_MONTH_9_LINE_AMOUNT;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_OBJECT_CODE;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE;
import static org.kuali.kfs.sys.KFSPropertyConstants.ORGANIZATION_REFERENCE_ID;
import static org.kuali.kfs.sys.KFSPropertyConstants.PROJECT_CODE;
import static org.kuali.kfs.sys.KFSPropertyConstants.SUB_ACCOUNT_NUMBER;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_DOCUMENT_LINE_DESCRIPTION;

import org.kuali.kfs.sys.businessobject.AccountingLineParserBase;

public class CUBudgetAdjustmentAccountingLineParser extends AccountingLineParserBase {

    private static final String[] AD_FORMAT = { CHART_OF_ACCOUNTS_CODE, ACCOUNT_NUMBER, SUB_ACCOUNT_NUMBER, FINANCIAL_OBJECT_CODE, FINANCIAL_SUB_OBJECT_CODE, PROJECT_CODE, ORGANIZATION_REFERENCE_ID, CURRENT_BUDGET_ADJUSTMENT_AMOUNT, BASE_BUDGET_ADJUSTMENT_AMOUNT, FINANCIAL_DOCUMENT_MONTH_1_LINE_AMOUNT, FINANCIAL_DOCUMENT_MONTH_2_LINE_AMOUNT, FINANCIAL_DOCUMENT_MONTH_3_LINE_AMOUNT, FINANCIAL_DOCUMENT_MONTH_4_LINE_AMOUNT, FINANCIAL_DOCUMENT_MONTH_5_LINE_AMOUNT, FINANCIAL_DOCUMENT_MONTH_6_LINE_AMOUNT, FINANCIAL_DOCUMENT_MONTH_7_LINE_AMOUNT, FINANCIAL_DOCUMENT_MONTH_8_LINE_AMOUNT, FINANCIAL_DOCUMENT_MONTH_9_LINE_AMOUNT, FINANCIAL_DOCUMENT_MONTH_10_LINE_AMOUNT, FINANCIAL_DOCUMENT_MONTH_11_LINE_AMOUNT, FINANCIAL_DOCUMENT_MONTH_12_LINE_AMOUNT, FINANCIAL_DOCUMENT_LINE_DESCRIPTION };

    /**
     * Constructs a AdvanceDepositAccountingLineParser.java.
     */
    public CUBudgetAdjustmentAccountingLineParser() {
        super();
    }

    /**
     * @see org.kuali.rice.kns.bo.AccountingLineParserBase#getSourceAccountingLineFormat()
     */
    @Override
    public String[] getSourceAccountingLineFormat() {
        return AD_FORMAT;
    }

    /**
     * @see org.kuali.rice.kns.bo.AccountingLineParser#getTargetAccountingLineFormat()
     */
    @Override
    public String[] getTargetAccountingLineFormat() {
        return AD_FORMAT;
    }
}
