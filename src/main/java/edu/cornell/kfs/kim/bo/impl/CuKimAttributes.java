package edu.cornell.kfs.kim.bo.impl;

import org.kuali.kfs.coa.businessobject.FundGroup;
import org.kuali.kfs.kim.bo.impl.KimAttributes;

public class CuKimAttributes extends KimAttributes {
    
    public static final String FUND_GROUP_CODE = "fundGroupCode";
    
    protected String fundGroupCode;
    
    protected FundGroup fundGroup;
    
    public String getFundGroupCode() {
        return fundGroupCode;
    }

    public void setFundGroupCode(String fundGroupCode) {
        this.fundGroupCode = fundGroupCode;
    }

    public FundGroup getFundGroup() {
        return fundGroup;
    }

    public void setFundGroup(FundGroup fundGroup) {
        this.fundGroup = fundGroup;
    }

}
