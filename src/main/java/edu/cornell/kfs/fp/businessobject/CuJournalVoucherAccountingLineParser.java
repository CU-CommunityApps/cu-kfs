package edu.cornell.kfs.fp.businessobject;

import static org.kuali.kfs.sys.KFSPropertyConstants.ACCOUNT_NUMBER;
import static org.kuali.kfs.sys.KFSPropertyConstants.AMOUNT;
import static org.kuali.kfs.sys.KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE;
import static org.kuali.kfs.sys.KFSPropertyConstants.CREDIT;
import static org.kuali.kfs.sys.KFSPropertyConstants.DEBIT;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_DOCUMENT_LINE_DESCRIPTION;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_OBJECT_CODE;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE;
import static org.kuali.kfs.sys.KFSPropertyConstants.OBJECT_TYPE_CODE;
import static org.kuali.kfs.sys.KFSPropertyConstants.ORGANIZATION_REFERENCE_ID;
import static org.kuali.kfs.sys.KFSPropertyConstants.PROJECT_CODE;
import static org.kuali.kfs.sys.KFSPropertyConstants.REFERENCE_NUMBER;
import static org.kuali.kfs.sys.KFSPropertyConstants.REFERENCE_ORIGIN_CODE;
import static org.kuali.kfs.sys.KFSPropertyConstants.REFERENCE_TYPE_CODE;
import static org.kuali.kfs.sys.KFSPropertyConstants.SUB_ACCOUNT_NUMBER;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.service.BalanceTypeService;
import org.kuali.kfs.fp.businessobject.JournalVoucherAccountingLineParser;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

public class CuJournalVoucherAccountingLineParser extends JournalVoucherAccountingLineParser {
    private String balanceTypeCode;
    protected static final String[] NON_OFFSET_FORMAT = { CHART_OF_ACCOUNTS_CODE, ACCOUNT_NUMBER, SUB_ACCOUNT_NUMBER, FINANCIAL_OBJECT_CODE, OBJECT_TYPE_CODE, FINANCIAL_SUB_OBJECT_CODE, PROJECT_CODE, ORGANIZATION_REFERENCE_ID, AMOUNT, FINANCIAL_DOCUMENT_LINE_DESCRIPTION };
    protected static final String[] OFFSET_FORMAT = { CHART_OF_ACCOUNTS_CODE, ACCOUNT_NUMBER, SUB_ACCOUNT_NUMBER, FINANCIAL_OBJECT_CODE, OBJECT_TYPE_CODE, FINANCIAL_SUB_OBJECT_CODE, PROJECT_CODE, ORGANIZATION_REFERENCE_ID, DEBIT, CREDIT, FINANCIAL_DOCUMENT_LINE_DESCRIPTION };
    protected static final String[] ENCUMBRANCE_FORMAT = { CHART_OF_ACCOUNTS_CODE, ACCOUNT_NUMBER, SUB_ACCOUNT_NUMBER, FINANCIAL_OBJECT_CODE, OBJECT_TYPE_CODE, FINANCIAL_SUB_OBJECT_CODE, PROJECT_CODE, ORGANIZATION_REFERENCE_ID, REFERENCE_ORIGIN_CODE, REFERENCE_TYPE_CODE, REFERENCE_NUMBER, DEBIT, CREDIT, FINANCIAL_DOCUMENT_LINE_DESCRIPTION };

    public CuJournalVoucherAccountingLineParser(String balanceTypeCode) {
        super(balanceTypeCode);
        this.balanceTypeCode = balanceTypeCode;
    }
    
    public String[] getSourceAccountingLineFormat() {
        return removeChartFromFormatIfNeeded(selectFormat());
    }

    private String[] selectFormat() {
        if (StringUtils.equals(balanceTypeCode, KFSConstants.BALANCE_TYPE_EXTERNAL_ENCUMBRANCE)) {
            return ENCUMBRANCE_FORMAT;
        }
        else if (SpringContext.getBean(BalanceTypeService.class).getBalanceTypeByCode(balanceTypeCode).isFinancialOffsetGenerationIndicator()) {
            return OFFSET_FORMAT;
        }
        else {
            return NON_OFFSET_FORMAT;
        }
    }

}
