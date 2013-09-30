package edu.cornell.kfs.fp.businessobject;

import static org.kuali.kfs.sys.KFSPropertyConstants.ACCOUNT_NUMBER;
import static org.kuali.kfs.sys.KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE;
import static org.kuali.kfs.sys.KFSPropertyConstants.CREDIT;
import static org.kuali.kfs.sys.KFSPropertyConstants.DEBIT;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_DOCUMENT_LINE_DESCRIPTION;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_OBJECT_CODE;
import static org.kuali.kfs.sys.KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE;
import static org.kuali.kfs.sys.KFSPropertyConstants.ORGANIZATION_REFERENCE_ID;
import static org.kuali.kfs.sys.KFSPropertyConstants.PROJECT_CODE;
import static org.kuali.kfs.sys.KFSPropertyConstants.SUB_ACCOUNT_NUMBER;

import org.kuali.kfs.fp.businessobject.AuxiliaryVoucherAccountingLineParser;

public class CuAuxiliaryVoucherAccountingLineParser extends AuxiliaryVoucherAccountingLineParser {
    protected static final String[] AV_FORMAT = { CHART_OF_ACCOUNTS_CODE, ACCOUNT_NUMBER, SUB_ACCOUNT_NUMBER, FINANCIAL_OBJECT_CODE, FINANCIAL_SUB_OBJECT_CODE, PROJECT_CODE, ORGANIZATION_REFERENCE_ID, DEBIT, CREDIT, FINANCIAL_DOCUMENT_LINE_DESCRIPTION };

    @Override
    public String[] getSourceAccountingLineFormat() {
        return removeChartFromFormatIfNeeded(AV_FORMAT);
    }

}
