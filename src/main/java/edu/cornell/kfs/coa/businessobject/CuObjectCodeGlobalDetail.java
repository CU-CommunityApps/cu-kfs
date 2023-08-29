package edu.cornell.kfs.coa.businessobject;

import org.kuali.kfs.coa.businessobject.ObjectCodeGlobalDetail;

public class CuObjectCodeGlobalDetail extends ObjectCodeGlobalDetail {
    private static final long serialVersionUID = 7971543974891105660L;
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CuObjectCodeGlobalDetail: ");
        sb.append("chart of accounts :").append(getChartOfAccountsCode());
        sb.append(" fiscal year: ").append(getUniversityFiscalYear());
        sb.append(" object code: ").append(getFinancialObjectCode());
        return sb.toString();
    }

}
