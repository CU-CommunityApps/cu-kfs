package edu.cornell.kfs.sys.businessobject;

import static org.kuali.kfs.sys.KFSPropertyConstants.ACCOUNT_NUMBER;
import static org.kuali.kfs.sys.KFSPropertyConstants.AMOUNT;
import static org.kuali.kfs.sys.KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_OBJECT_CODE;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE;
import static org.kuali.kfs.sys.KFSPropertyConstants.ORGANIZATION_REFERENCE_ID;
import static org.kuali.kfs.sys.KFSPropertyConstants.PROJECT_CODE;
import static org.kuali.kfs.sys.KFSPropertyConstants.SUB_ACCOUNT_NUMBER;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_DOCUMENT_LINE_DESCRIPTION;


import org.kuali.kfs.sys.businessobject.AccountingLineParserBase;

public class CUAccountingLineParserBase extends AccountingLineParserBase {
    protected static final String[] DEFAULT_FORMAT = { CHART_OF_ACCOUNTS_CODE, ACCOUNT_NUMBER, SUB_ACCOUNT_NUMBER, FINANCIAL_OBJECT_CODE, FINANCIAL_SUB_OBJECT_CODE, PROJECT_CODE, ORGANIZATION_REFERENCE_ID, AMOUNT,FINANCIAL_DOCUMENT_LINE_DESCRIPTION };
    
    public String[] getSourceAccountingLineFormat() {
        return DEFAULT_FORMAT;
    }

    /**
     * @see org.kuali.rice.kns.bo.AccountingLineParser#getTargetAccountingLineFormat()
     */
    public String[] getTargetAccountingLineFormat() {
        return DEFAULT_FORMAT;
    }
}
