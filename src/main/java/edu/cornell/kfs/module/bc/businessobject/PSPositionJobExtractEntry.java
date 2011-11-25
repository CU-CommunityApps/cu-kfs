package edu.cornell.kfs.module.bc.businessobject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kns.bo.BusinessObjectBase;

import edu.cornell.kfs.module.bc.CUBCConstants;
import edu.cornell.kfs.module.bc.CUBCConstants.StatusFlag;

/**
 * A class that holds the fields from the PS position/job extraat file to be loaded in
 * KFS.
 */
public class PSPositionJobExtractEntry extends BusinessObjectBase {

    public CUBCConstants.PSEntryStatus deleteStatus;
    public StatusFlag changeStatus;

    private String positionNumber;
    private String emplid;
    private String name;
    private String employeeType;
    private String defaultObjectCode;
    private String positionUnionCode;
    private String workMonths;
    private String jobCode;
    protected String jobCodeDesc;
    protected String jobCodeDescShrt;
    protected String company;
    protected String fullPartTime;
    protected String classInd;
    protected String addsToActualFte;
    protected String cuStateCert;
    protected String employeeRecord;
    protected String employeeStatus;
    protected String jobStandardHours;
    protected String jobCodeStandardHours;
    protected String employeeClass;
    protected String earningDistributionType;
    protected String compRate;
    protected String annualBenefitBaseRate;
    protected String cuAbbrFlag;
    protected String annualRate;
    protected String jobFamily;
    protected String compFreq;
    protected String jobFunction;
    protected String jobFunctionDesc;
    protected String cuPlannedFTE;

    // POS accounting Strings
    private String posTimePercent1;
    private String posChartOfAccountsCode1;
    private String posAccountNumber1;
    private String posSubAccountNumber1;
    private String posFinancialObjectCode1;
    private String posFinancialSubObjectCode1;
    //not from the feed, used for flagging purposes
    private StatusFlag posStatus1;
    private String posTimePercent2;
    private String posChartOfAccountsCode2;
    private String posAccountNumber2;
    private String posSubAccountNumber2;
    private String posFinancialObjectCode2;
    private String posFinancialSubObjectCode2;
    //not from the feed, used for flagging purposes
    private StatusFlag posStatus2;
    private String posTimePercent3;
    private String posChartOfAccountsCode3;
    private String posAccountNumber3;
    private String posSubAccountNumber3;
    private String posFinancialObjectCode3;
    private String posFinancialSubObjectCode3;
    //not from the feed, used for flagging purposes
    private StatusFlag posStatus3;
    private String posTimePercent4;
    private String posChartOfAccountsCode4;
    private String posAccountNumber4;
    private String posSubAccountNumber4;
    private String posFinancialObjectCode4;
    private String posFinancialSubObjectCode4;
    //not from the feed, used for flagging purposes
    private StatusFlag posStatus4;
    private String posTimePercent5;
    private String posChartOfAccountsCode5;
    private String posAccountNumber5;
    private String posSubAccountNumber5;
    private String posFinancialObjectCode5;
    private String posFinancialSubObjectCode5;
    //not from the feed, used for flagging purposes
    private StatusFlag posStatus5;
    private String posTimePercent6;
    private String posChartOfAccountsCode6;
    private String posAccountNumber6;
    private String posSubAccountNumber6;
    private String posFinancialObjectCode6;
    private String posFinancialSubObjectCode6;
    //not from the feed, used for flagging purposes
    private StatusFlag posStatus6;
    private String posTimePercent7;
    private String posChartOfAccountsCode7;
    private String posAccountNumber7;
    private String posSubAccountNumber7;
    private String posFinancialObjectCode7;
    private String posFinancialSubObjectCode7;
    //not from the feed, used for flagging purposes
    private StatusFlag posStatus7;
    private String posTimePercent8;
    private String posChartOfAccountsCode8;
    private String posAccountNumber8;
    private String posSubAccountNumber8;
    private String posFinancialObjectCode8;
    private String posFinancialSubObjectCode8;
    //not from the feed, used for flagging purposes
    private StatusFlag posStatus8;
    private String posTimePercent9;
    private String posChartOfAccountsCode9;
    private String posAccountNumber9;
    private String posSubAccountNumber9;
    private String posFinancialObjectCode9;
    private String posFinancialSubObjectCode9;
    //not from the feed, used for flagging purposes
    private StatusFlag posStatus9;
    private String posTimePercent10;
    private String posChartOfAccountsCode10;
    private String posAccountNumber10;
    private String posSubAccountNumber10;
    private String posFinancialObjectCode10;
    private String posFinancialSubObjectCode10;
    //not from the feed, used for flagging purposes
    private StatusFlag posStatus10;

    // CSF accountung Strings
    private String csfTimePercent1;
    private String chartOfAccountsCode1;
    private String accountNumber1;
    private String subAccountNumber1;
    private String financialObjectCode1;
    private String financialSubObjectCode1;
    //not from the feed, used for flagging purposes
    private StatusFlag csfStatus1;
    private String csfTimePercent2;
    private String chartOfAccountsCode2;
    private String accountNumber2;
    private String subAccountNumber2;
    private String financialObjectCode2;
    private String financialSubObjectCode2;
    //not from the feed, used for flagging purposes
    private StatusFlag csfStatus2;
    private String csfTimePercent3;
    private String chartOfAccountsCode3;
    private String accountNumber3;
    private String subAccountNumber3;
    private String financialObjectCode3;
    private String financialSubObjectCode3;
    //not from the feed, used for flagging purposes
    private StatusFlag csfStatus3;
    private String csfTimePercent4;
    private String chartOfAccountsCode4;
    private String accountNumber4;
    private String subAccountNumber4;
    private String financialObjectCode4;
    private String financialSubObjectCode4;
    //not from the feed, used for flagging purposes
    private StatusFlag csfStatus4;
    private String csfTimePercent5;
    private String chartOfAccountsCode5;
    private String accountNumber5;
    private String subAccountNumber5;
    private String financialObjectCode5;
    private String financialSubObjectCode5;
    //not from the feed, used for flagging purposes
    private StatusFlag csfStatus5;
    private String csfTimePercent6;
    private String chartOfAccountsCode6;
    private String accountNumber6;
    private String subAccountNumber6;
    private String financialObjectCode6;
    private String financialSubObjectCode6;
    //not from the feed, used for flagging purposes
    private StatusFlag csfStatus6;
    private String csfTimePercent7;
    private String chartOfAccountsCode7;
    private String accountNumber7;
    private String subAccountNumber7;
    private String financialObjectCode7;
    private String financialSubObjectCode7;
    //not from the feed, used for flagging purposes
    private StatusFlag csfStatus7;
    private String csfTimePercent8;
    private String chartOfAccountsCode8;
    private String accountNumber8;
    private String subAccountNumber8;
    private String financialObjectCode8;
    private String financialSubObjectCode8;
    //not from the feed, used for flagging purposes
    private StatusFlag csfStatus8;
    private String csfTimePercent9;
    private String chartOfAccountsCode9;
    private String accountNumber9;
    private String subAccountNumber9;
    private String financialObjectCode9;
    private String financialSubObjectCode9;
    //not from the feed, used for flagging purposes
    private StatusFlag csfStatus9;
    private String csfTimePercent10;
    private String chartOfAccountsCode10;
    private String accountNumber10;
    private String subAccountNumber10;
    private String financialObjectCode10;
    private String financialSubObjectCode10;
    //not from the feed, used for flagging purposes
    private StatusFlag csfStatus10;

    private List<PSPositionJobExtractAccountingInfo> csfAccountingInfoList;
    private List<PSPositionJobExtractAccountingInfo> posAccountingInfoList;

    /**
     * Gets the positionNumber.
     * 
     * @return positionNumber
     */
    public String getPositionNumber() {
        return CUBCConstants.POSITION_NUMBER_PREFIX + positionNumber;
    }

    /**
     * Sets the positionNumber.
     * 
     * @param positionNumber
     */
    public void setPositionNumber(String positionNumber) {
        this.positionNumber = positionNumber;
    }

    /**
     * Gets the emplid.
     * 
     * @return emplid
     */
    public String getEmplid() {
        return emplid;
    }

    /**
     * Sets the emplid.
     * 
     * @param emplid
     */
    public void setEmplid(String emplid) {
        this.emplid = emplid;
    }

    /**
     * Gets the chartOfAccountsCode1.
     * 
     * @return chartOfAccountsCode1
     */
    public String getChartOfAccountsCode1() {
        return chartOfAccountsCode1;
    }

    /**
     * Sets the chartOfAccountsCode1.
     * 
     * @param chartOfAccountsCode1
     */
    public void setChartOfAccountsCode1(String chartOfAccountsCode1) {
        this.chartOfAccountsCode1 = chartOfAccountsCode1;
    }

    /**
     * Gets the accountNumber1.
     * 
     * @return accountNumber1
     */
    public String getAccountNumber1() {
        return accountNumber1;
    }

    /**
     * Sets the accountNumber1.
     * 
     * @param accountNumber1
     */
    public void setAccountNumber1(String accountNumber1) {
        this.accountNumber1 = accountNumber1;
    }

    /**
     * Gets the subAccountNumber1.
     * 
     * @return subAccountNumber1
     */
    public String getSubAccountNumber1() {
        return subAccountNumber1;
    }

    /**
     * Sets the subAccountNumber1.
     * 
     * @param subAccountNumber1
     */
    public void setSubAccountNumber1(String subAccountNumber1) {
        this.subAccountNumber1 = subAccountNumber1;
    }

    /**
     * Gets the financialObjectCode1.
     * 
     * @return financialObjectCode1
     */
    public String getFinancialObjectCode1() {
        return financialObjectCode1;
    }

    /**
     * Sets the financialObjectCode1.
     * 
     * @param financialObjectCode1
     */
    public void setFinancialObjectCode1(String financialObjectCode1) {
        this.financialObjectCode1 = financialObjectCode1;
    }

    /**
     * Gets the financialSubObjectCode1.
     * 
     * @return financialSubObjectCode1
     */
    public String getFinancialSubObjectCode1() {
        return financialSubObjectCode1;
    }

    /**
     * Sets the financialSubObjectCode1.
     * 
     * @param financialSubObjectCode1
     */
    public void setFinancialSubObjectCode1(String financialSubObjectCode1) {
        this.financialSubObjectCode1 = financialSubObjectCode1;
    }

    /**
     * Gets the chartOfAccountsCode2.
     * 
     * @return chartOfAccountsCode2
     */
    public String getChartOfAccountsCode2() {
        return chartOfAccountsCode2;
    }

    /**
     * Sets the chartOfAccountsCode2.
     * 
     * @param chartOfAccountsCode2
     */
    public void setChartOfAccountsCode2(String chartOfAccountsCode2) {
        this.chartOfAccountsCode2 = chartOfAccountsCode2;
    }

    /**
     * Gets the accountNumber2.
     * 
     * @return accountNumber2
     */
    public String getAccountNumber2() {
        return accountNumber2;
    }

    /**
     * Sets the accountNumber2.
     * 
     * @param accountNumber2
     */
    public void setAccountNumber2(String accountNumber2) {
        this.accountNumber2 = accountNumber2;
    }

    /**
     * Gets the subAccountNumber2.
     * 
     * @return subAccountNumber2
     */
    public String getSubAccountNumber2() {
        return subAccountNumber2;
    }

    /**
     * Sets the subAccountNumber2.
     * 
     * @param subAccountNumber2
     */
    public void setSubAccountNumber2(String subAccountNumber2) {
        this.subAccountNumber2 = subAccountNumber2;
    }

    /**
     * Gets the financialObjectCode2.
     * 
     * @return financialObjectCode2
     */
    public String getFinancialObjectCode2() {
        return financialObjectCode2;
    }

    /**
     * Sets the financialObjectCode2.
     * 
     * @param financialObjectCode2
     */
    public void setFinancialObjectCode2(String financialObjectCode2) {
        this.financialObjectCode2 = financialObjectCode2;
    }

    /**
     * Gets the financialSubObjectCode2.
     * 
     * @return financialSubObjectCode2
     */
    public String getFinancialSubObjectCode2() {
        return financialSubObjectCode2;
    }

    /**
     * Sets the financialSubObjectCode2
     * 
     * @param financialSubObjectCode2
     */
    public void setFinancialSubObjectCode2(String financialSubObjectCode2) {
        this.financialSubObjectCode2 = financialSubObjectCode2;
    }

    /**
     * Gets the chartOfAccountsCode3.
     * 
     * @return chartOfAccountsCode3
     */
    public String getChartOfAccountsCode3() {
        return chartOfAccountsCode3;
    }

    /**
     * Sets the chartOfAccountsCode3.
     * 
     * @param chartOfAccountsCode3
     */
    public void setChartOfAccountsCode3(String chartOfAccountsCode3) {
        this.chartOfAccountsCode3 = chartOfAccountsCode3;
    }

    /**
     * Gets the accountNumber3.
     * 
     * @return accountNumber3
     */
    public String getAccountNumber3() {
        return accountNumber3;
    }

    /**
     * Sets the accountNumber3.
     * 
     * @param accountNumber3
     */
    public void setAccountNumber3(String accountNumber3) {
        this.accountNumber3 = accountNumber3;
    }

    /**
     * Gets the subAccountNumber3.
     * 
     * @return subAccountNumber3
     */
    public String getSubAccountNumber3() {
        return subAccountNumber3;
    }

    /**
     * Sets the subAccountNumber3.
     * 
     * @param subAccountNumber3
     */
    public void setSubAccountNumber3(String subAccountNumber3) {
        this.subAccountNumber3 = subAccountNumber3;
    }

    /**
     * Gets the financialObjectCode3.
     * 
     * @return financialObjectCode3
     */
    public String getFinancialObjectCode3() {
        return financialObjectCode3;
    }

    /**
     * Sets the financialObjectCode3.
     * 
     * @param financialObjectCode3
     */
    public void setFinancialObjectCode3(String financialObjectCode3) {
        this.financialObjectCode3 = financialObjectCode3;
    }

    /**
     * Gets the financialSubObjectCode3.
     * 
     * @return financialSubObjectCode3
     */
    public String getFinancialSubObjectCode3() {
        return financialSubObjectCode3;
    }

    /**
     * Sets the financialSubObjectCode3.
     * 
     * @param financialSubObjectCode3
     */
    public void setFinancialSubObjectCode3(String financialSubObjectCode3) {
        this.financialSubObjectCode3 = financialSubObjectCode3;
    }

    /**
     * Gets the chartOfAccountsCode4.
     * 
     * @return chartOfAccountsCode4
     */
    public String getChartOfAccountsCode4() {
        return chartOfAccountsCode4;
    }

    /**
     * Sets the chartOfAccountsCode4.
     * 
     * @param chartOfAccountsCode4
     */
    public void setChartOfAccountsCode4(String chartOfAccountsCode4) {
        this.chartOfAccountsCode4 = chartOfAccountsCode4;
    }

    /**
     * Gets the accountNumber4.
     * 
     * @return accountNumber4
     */
    public String getAccountNumber4() {
        return accountNumber4;
    }

    /**
     * Sets the accountNumber4.
     * 
     * @param accountNumber4
     */
    public void setAccountNumber4(String accountNumber4) {
        this.accountNumber4 = accountNumber4;
    }

    /**
     * Gets the subAccountNumber4.
     * 
     * @return subAccountNumber4
     */
    public String getSubAccountNumber4() {
        return subAccountNumber4;
    }

    /**
     * Sets the subAccountNumber4.
     * 
     * @param subAccountNumber4
     */
    public void setSubAccountNumber4(String subAccountNumber4) {
        this.subAccountNumber4 = subAccountNumber4;
    }

    /**
     * Gets the financialObjectCode4
     * 
     * @return financialObjectCode4
     */
    public String getFinancialObjectCode4() {
        return financialObjectCode4;
    }

    /**
     * Sets the financialObjectCode4.
     * 
     * @param financialObjectCode4
     */
    public void setFinancialObjectCode4(String financialObjectCode4) {
        this.financialObjectCode4 = financialObjectCode4;
    }

    /**
     * Gets the financialSubObjectCode4.
     * 
     * @return financialSubObjectCode4
     */
    public String getFinancialSubObjectCode4() {
        return financialSubObjectCode4;
    }

    /**
     * Sets the financialSubObjectCode4.
     * 
     * @param financialSubObjectCode4
     */
    public void setFinancialSubObjectCode4(String financialSubObjectCode4) {
        this.financialSubObjectCode4 = financialSubObjectCode4;
    }

    /**
     * Gets the chartOfAccountsCode5.
     * 
     * @return chartOfAccountsCode5
     */
    public String getChartOfAccountsCode5() {
        return chartOfAccountsCode5;
    }

    /**
     * Sets the chartOfAccountsCode5.
     * 
     * @param chartOfAccountsCode5
     */
    public void setChartOfAccountsCode5(String chartOfAccountsCode5) {
        this.chartOfAccountsCode5 = chartOfAccountsCode5;
    }

    /**
     * Gets the accountNumber5.
     * 
     * @return accountNumber5
     */
    public String getAccountNumber5() {
        return accountNumber5;
    }

    /**
     * Sets the accountNumber5.
     * 
     * @param accountNumber5
     */
    public void setAccountNumber5(String accountNumber5) {
        this.accountNumber5 = accountNumber5;
    }

    /**
     * Gets the subAccountNumber5.
     * 
     * @return subAccountNumber5
     */
    public String getSubAccountNumber5() {
        return subAccountNumber5;
    }

    /**
     * Sets the subAccountNumber5.
     * 
     * @param subAccountNumber5
     */
    public void setSubAccountNumber5(String subAccountNumber5) {
        this.subAccountNumber5 = subAccountNumber5;
    }

    /**
     * Gets the financialObjectCode5.
     * 
     * @return financialObjectCode5
     */
    public String getFinancialObjectCode5() {
        return financialObjectCode5;
    }

    /**
     * Sets the financialObjectCode5.
     * 
     * @param financialObjectCode5
     */
    public void setFinancialObjectCode5(String financialObjectCode5) {
        this.financialObjectCode5 = financialObjectCode5;
    }

    /**
     * Gets the financialSubObjectCode5.
     * 
     * @return financialSubObjectCode5
     */
    public String getFinancialSubObjectCode5() {
        return financialSubObjectCode5;
    }

    /**
     * Sets the financialSubObjectCode5.
     * 
     * @param financialSubObjectCode5
     */
    public void setFinancialSubObjectCode5(String financialSubObjectCode5) {
        this.financialSubObjectCode5 = financialSubObjectCode5;
    }

    /**
     * Gets the chartOfAccountsCode6.
     * 
     * @return chartOfAccountsCode6
     */
    public String getChartOfAccountsCode6() {
        return chartOfAccountsCode6;
    }

    /**
     * Sets the chartOfAccountsCode6.
     * 
     * @param chartOfAccountsCode6
     */
    public void setChartOfAccountsCode6(String chartOfAccountsCode6) {
        this.chartOfAccountsCode6 = chartOfAccountsCode6;
    }

    /**
     * Gets the accountNumber6.
     * 
     * @return accountNumber6
     */
    public String getAccountNumber6() {
        return accountNumber6;
    }

    /**
     * Sets the accountNumber6.
     * 
     * @param accountNumber6
     */
    public void setAccountNumber6(String accountNumber6) {
        this.accountNumber6 = accountNumber6;
    }

    /**
     * Gets the subAccountNumber6.
     * 
     * @return subAccountNumber6
     */
    public String getSubAccountNumber6() {
        return subAccountNumber6;
    }

    /**
     * Sets the subAccountNumber6.
     * 
     * @param subAccountNumber6
     */
    public void setSubAccountNumber6(String subAccountNumber6) {
        this.subAccountNumber6 = subAccountNumber6;
    }

    /**
     * Gets the financialObjectCode6.
     * 
     * @return financialObjectCode6
     */
    public String getFinancialObjectCode6() {
        return financialObjectCode6;
    }

    /**
     * Sets the financialObjectCode6.
     * 
     * @param financialObjectCode6
     */
    public void setFinancialObjectCode6(String financialObjectCode6) {
        this.financialObjectCode6 = financialObjectCode6;
    }

    /**
     * Gets the financialSubObjectCode6.
     * 
     * @return financialSubObjectCode6
     */
    public String getFinancialSubObjectCode6() {
        return financialSubObjectCode6;
    }

    /**
     * Sets the financialSubObjectCode6.
     * 
     * @param financialSubObjectCode6
     */
    public void setFinancialSubObjectCode6(String financialSubObjectCode6) {
        this.financialSubObjectCode6 = financialSubObjectCode6;
    }

    /**
     * Gets the chartOfAccountsCode7.
     * 
     * @return chartOfAccountsCode7
     */
    public String getChartOfAccountsCode7() {
        return chartOfAccountsCode7;
    }

    /**
     * Sets the chartOfAccountsCode7.
     * 
     * @param chartOfAccountsCode7
     */
    public void setChartOfAccountsCode7(String chartOfAccountsCode7) {
        this.chartOfAccountsCode7 = chartOfAccountsCode7;
    }

    /**
     * Gets the accountNumber7.
     * 
     * @return accountNumber7
     */
    public String getAccountNumber7() {
        return accountNumber7;
    }

    /**
     * Sets the accountNumber7.
     * 
     * @param accountNumber7
     */
    public void setAccountNumber7(String accountNumber7) {
        this.accountNumber7 = accountNumber7;
    }

    /**
     * Gets the subAccountNumber7.
     * 
     * @return subAccountNumber7
     */
    public String getSubAccountNumber7() {
        return subAccountNumber7;
    }

    /**
     * Sets the subAccountNumber7.
     * 
     * @param subAccountNumber7
     */
    public void setSubAccountNumber7(String subAccountNumber7) {
        this.subAccountNumber7 = subAccountNumber7;
    }

    /**
     * Gets the financialObjectCode7.
     * 
     * @return financialObjectCode7
     */
    public String getFinancialObjectCode7() {
        return financialObjectCode7;
    }

    /**
     * Sets the financialObjectCode7.
     * 
     * @param financialObjectCode7
     */
    public void setFinancialObjectCode7(String financialObjectCode7) {
        this.financialObjectCode7 = financialObjectCode7;
    }

    /**
     * Gets the financialSubObjectCode7.
     * 
     * @return financialSubObjectCode7
     */
    public String getFinancialSubObjectCode7() {
        return financialSubObjectCode7;
    }

    /**
     * Sets the financialSubObjectCode7.
     * 
     * @param financialSubObjectCode7
     */
    public void setFinancialSubObjectCode7(String financialSubObjectCode7) {
        this.financialSubObjectCode7 = financialSubObjectCode7;
    }

    /**
     * Gets the chartOfAccountsCode8.
     * 
     * @return chartOfAccountsCode8
     */
    public String getChartOfAccountsCode8() {
        return chartOfAccountsCode8;
    }

    /**
     * Sets the chartOfAccountsCode8.
     * 
     * @param chartOfAccountsCode8
     */
    public void setChartOfAccountsCode8(String chartOfAccountsCode8) {
        this.chartOfAccountsCode8 = chartOfAccountsCode8;
    }

    /**
     * Gets the accountNumber8.
     * 
     * @return accountNumber8
     */
    public String getAccountNumber8() {
        return accountNumber8;
    }

    /**
     * Sets the accountNumber8.
     * 
     * @param accountNumber8
     */
    public void setAccountNumber8(String accountNumber8) {
        this.accountNumber8 = accountNumber8;
    }

    /**
     * Gets the subAccountNumber8.
     * 
     * @return subAccountNumber8
     */
    public String getSubAccountNumber8() {
        return subAccountNumber8;
    }

    /**
     * Sets the subAccountNumber8.
     * 
     * @param subAccountNumber8
     */
    public void setSubAccountNumber8(String subAccountNumber8) {
        this.subAccountNumber8 = subAccountNumber8;
    }

    /**
     * Gets the financialObjectCode8.
     * 
     * @return financialObjectCode8
     */
    public String getFinancialObjectCode8() {
        return financialObjectCode8;
    }

    /**
     * Sets the financialObjectCode8.
     * 
     * @param financialObjectCode8
     */
    public void setFinancialObjectCode8(String financialObjectCode8) {
        this.financialObjectCode8 = financialObjectCode8;
    }

    /**
     * Gets the financialSubObjectCode8.
     * 
     * @return financialSubObjectCode8
     */
    public String getFinancialSubObjectCode8() {
        return financialSubObjectCode8;
    }

    /**
     * Sets the financialSubObjectCode8.
     * 
     * @param financialSubObjectCode8
     */
    public void setFinancialSubObjectCode8(String financialSubObjectCode8) {
        this.financialSubObjectCode8 = financialSubObjectCode8;
    }

    /**
     * Gets the chartOfAccountsCode9.
     * 
     * @return chartOfAccountsCode9
     */
    public String getChartOfAccountsCode9() {
        return chartOfAccountsCode9;
    }

    /**
     * Sets the chartOfAccountsCode9.
     * 
     * @param chartOfAccountsCode9
     */
    public void setChartOfAccountsCode9(String chartOfAccountsCode9) {
        this.chartOfAccountsCode9 = chartOfAccountsCode9;
    }

    /**
     * Gets the accountNumber9.
     * 
     * @return accountNumber9
     */
    public String getAccountNumber9() {
        return accountNumber9;
    }

    /**
     * Sets the accountNumber9.
     * 
     * @param accountNumber9
     */
    public void setAccountNumber9(String accountNumber9) {
        this.accountNumber9 = accountNumber9;
    }

    /**
     * Gets the subAccountNumber9.
     * 
     * @return subAccountNumber9
     */
    public String getSubAccountNumber9() {
        return subAccountNumber9;
    }

    /**
     * Sets the subAccountNumber9.
     * 
     * @param subAccountNumber9
     */
    public void setSubAccountNumber9(String subAccountNumber9) {
        this.subAccountNumber9 = subAccountNumber9;
    }

    /**
     * Gets the financialObjectCode9.
     * 
     * @return financialObjectCode9
     */
    public String getFinancialObjectCode9() {
        return financialObjectCode9;
    }

    /**
     * Sets the financialObjectCode9.
     * 
     * @param financialObjectCode9
     */
    public void setFinancialObjectCode9(String financialObjectCode9) {
        this.financialObjectCode9 = financialObjectCode9;
    }

    /**
     * Gets the financialSubObjectCode9.
     * 
     * @return financialSubObjectCode9
     */
    public String getFinancialSubObjectCode9() {
        return financialSubObjectCode9;
    }

    /**
     * Sets the financialSubObjectCode9.
     * 
     * @param financialSubObjectCode9
     */
    public void setFinancialSubObjectCode9(String financialSubObjectCode9) {
        this.financialSubObjectCode9 = financialSubObjectCode9;
    }

    /**
     * Gets the chartOfAccountsCode10.
     * 
     * @return chartOfAccountsCode10
     */
    public String getChartOfAccountsCode10() {
        return chartOfAccountsCode10;
    }

    /**
     * .Sets the chartOfAccountsCode10
     * 
     * @param chartOfAccountsCode10
     */
    public void setChartOfAccountsCode10(String chartOfAccountsCode10) {
        this.chartOfAccountsCode10 = chartOfAccountsCode10;
    }

    /**
     * Gets the accountNumber10.
     * 
     * @return accountNumber10
     */
    public String getAccountNumber10() {
        return accountNumber10;
    }

    /**
     * Sets the accountNumber10.
     * 
     * @param accountNumber10
     */
    public void setAccountNumber10(String accountNumber10) {
        this.accountNumber10 = accountNumber10;
    }

    /**
     * Gets the subAccountNumber10.
     * 
     * @return subAccountNumber10
     */
    public String getSubAccountNumber10() {
        return subAccountNumber10;
    }

    /**
     * Sets the subAccountNumber10.
     * 
     * @param subAccountNumber10
     */
    public void setSubAccountNumber10(String subAccountNumber10) {
        this.subAccountNumber10 = subAccountNumber10;
    }

    /**
     * Gets the financialObjectCode10.
     * 
     * @return financialObjectCode10
     */
    public String getFinancialObjectCode10() {
        return financialObjectCode10;
    }

    /**
     * Sets the financialObjectCode10.
     * 
     * @param financialObjectCode10
     */
    public void setFinancialObjectCode10(String financialObjectCode10) {
        this.financialObjectCode10 = financialObjectCode10;
    }

    /**
     * Gets the financialSubObjectCode10.
     * 
     * @return financialSubObjectCode10
     */
    public String getFinancialSubObjectCode10() {
        return financialSubObjectCode10;
    }

    /**
     * Sets the financialSubObjectCode10.
     * 
     * @param financialSubObjectCode10
     */
    public void setFinancialSubObjectCode10(String financialSubObjectCode10) {
        this.financialSubObjectCode10 = financialSubObjectCode10;
    }

    /**
     * Gets the csfTimePercent1.
     * 
     * @return csfTimePercent1
     */
    public String getCsfTimePercent1() {
        return csfTimePercent1;
    }

    /**
     * Sets the csfTimePercent1.
     * 
     * @param csfTimePercent1
     */
    public void setCsfTimePercent1(String csfTimePercent1) {
        this.csfTimePercent1 = csfTimePercent1;
    }

    /**
     * Gets the csfTimePercent2.
     * 
     * @return csfTimePercent2
     */
    public String getCsfTimePercent2() {
        return csfTimePercent2;
    }

    /**
     * Sets the csfTimePercent2.
     * 
     * @param csfTimePercent2
     */
    public void setCsfTimePercent2(String csfTimePercent2) {
        this.csfTimePercent2 = csfTimePercent2;
    }

    /**
     * Gets the csfTimePercent3.
     * 
     * @return csfTimePercent3
     */
    public String getCsfTimePercent3() {
        return csfTimePercent3;
    }

    /**
     * Sets the csfTimePercent3.
     * 
     * @param csfTimePercent3
     */
    public void setCsfTimePercent3(String csfTimePercent3) {
        this.csfTimePercent3 = csfTimePercent3;
    }

    /**
     * Gets the csfTimePercent4.
     * 
     * @return csfTimePercent4
     */
    public String getCsfTimePercent4() {
        return csfTimePercent4;
    }

    /**
     * Sets the csfTimePercent4.
     * 
     * @param csfTimePercent4
     */
    public void setCsfTimePercent4(String csfTimePercent4) {
        this.csfTimePercent4 = csfTimePercent4;
    }

    /**
     * Gets the csfTimePercent5.
     * 
     * @return csfTimePercent5
     */
    public String getCsfTimePercent5() {
        return csfTimePercent5;
    }

    /**
     * Sets the csfTimePercent5.
     * 
     * @param csfTimePercent5
     */
    public void setCsfTimePercent5(String csfTimePercent5) {
        this.csfTimePercent5 = csfTimePercent5;
    }

    /**
     * Gets the csfTimePercent6
     * 
     * @return csfTimePercent6
     */
    public String getCsfTimePercent6() {
        return csfTimePercent6;
    }

    /**
     * Sets the csfTimePercent6.
     * 
     * @param csfTimePercent6
     */
    public void setCsfTimePercent6(String csfTimePercent6) {
        this.csfTimePercent6 = csfTimePercent6;
    }

    /**
     * Gets the csfTimePercent7.
     * 
     * @return csfTimePercent7
     */
    public String getCsfTimePercent7() {
        return csfTimePercent7;
    }

    /**
     * Sets the csfTimePercent7.
     * 
     * @param csfTimePercent7
     */
    public void setCsfTimePercent7(String csfTimePercent7) {
        this.csfTimePercent7 = csfTimePercent7;
    }

    /**
     * Gets the csfTimePercent8.
     * 
     * @return csfTimePercent8
     */
    public String getCsfTimePercent8() {
        return csfTimePercent8;
    }

    /**
     * Sets the csfTimePercent8.
     * 
     * @param csfTimePercent8
     */
    public void setCsfTimePercent8(String csfTimePercent8) {
        this.csfTimePercent8 = csfTimePercent8;
    }

    /**
     * Gets the csfTimePercent9.
     * 
     * @return csfTimePercent9
     */
    public String getCsfTimePercent9() {
        return csfTimePercent9;
    }

    /**
     * Sets the csfTimePercent9.
     * 
     * @param csfTimePercent9
     */
    public void setCsfTimePercent9(String csfTimePercent9) {
        this.csfTimePercent9 = csfTimePercent9;
    }

    /**
     * Gets the csfTimePercent10.
     * 
     * @return csfTimePercent10
     */
    public String getCsfTimePercent10() {
        return csfTimePercent10;
    }

    /**
     * Sets the csfTimePercent10.
     * 
     * @param csfTimePercent10
     */
    public void setCsfTimePercent10(String csfTimePercent10) {
        this.csfTimePercent10 = csfTimePercent10;
    }

    /**
     * Gets the name.
     * 
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Build a list with the non-blank accounts.
     * 
     * @return a list of accounting information
     */
    private List<PSPositionJobExtractAccountingInfo> getPOSAccountingInfoCollection() {
        List<PSPositionJobExtractAccountingInfo> accountingInfoCollection = new ArrayList<PSPositionJobExtractAccountingInfo>();

        if (StringUtils.isNotBlank(posTimePercent1)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(posTimePercent1,
                    posChartOfAccountsCode1, posAccountNumber1, posSubAccountNumber1, posFinancialObjectCode1,
                    posFinancialSubObjectCode1, posStatus1);

            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(posTimePercent2)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(posTimePercent2,
                    posChartOfAccountsCode2, posAccountNumber2, posSubAccountNumber2, posFinancialObjectCode2,
                    posFinancialSubObjectCode2, posStatus2);

            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(posTimePercent3)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(posTimePercent3,
                    posChartOfAccountsCode3, posAccountNumber3, posSubAccountNumber3, posFinancialObjectCode3,
                    posFinancialSubObjectCode3, posStatus3);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(posTimePercent4)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(posTimePercent4,
                    posChartOfAccountsCode4, posAccountNumber4, posSubAccountNumber4, posFinancialObjectCode4,
                    posFinancialSubObjectCode4, posStatus4);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(posTimePercent5)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(posTimePercent5,
                    posChartOfAccountsCode5, posAccountNumber5, posSubAccountNumber5, posFinancialObjectCode5,
                    posFinancialSubObjectCode5, posStatus5);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(posTimePercent6)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(posTimePercent6,
                    posChartOfAccountsCode6, posAccountNumber6, posSubAccountNumber6, posFinancialObjectCode6,
                    posFinancialSubObjectCode6, posStatus6);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(posTimePercent7)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(posTimePercent7,
                    posChartOfAccountsCode7, posAccountNumber7, posSubAccountNumber7, posFinancialObjectCode7,
                    posFinancialSubObjectCode7, posStatus7);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(posTimePercent8)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(posTimePercent8,
                    posChartOfAccountsCode8, posAccountNumber8, posSubAccountNumber8, posFinancialObjectCode8,
                    posFinancialSubObjectCode8, posStatus8);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(posTimePercent9)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(posTimePercent9,
                    posChartOfAccountsCode9, posAccountNumber9, posSubAccountNumber9, posFinancialObjectCode9,
                    posFinancialSubObjectCode9, posStatus9);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(posTimePercent10)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(
                    posTimePercent10, posChartOfAccountsCode10, posAccountNumber10, posSubAccountNumber10,
                    posFinancialObjectCode10, posFinancialSubObjectCode10, posStatus10);
            accountingInfoCollection.add(accountingInfo);
        }

        return accountingInfoCollection;
    }

    /**
     * Build a list with the non-blank accounts.
     * 
     * @return a list of accounting information
     */
    private List<PSPositionJobExtractAccountingInfo> getCSFAccountingInfoCollection() {
        List<PSPositionJobExtractAccountingInfo> accountingInfoCollection = new ArrayList<PSPositionJobExtractAccountingInfo>();

        if (StringUtils.isNotBlank(csfTimePercent1)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(csfTimePercent1,
                    chartOfAccountsCode1, accountNumber1, subAccountNumber1, financialObjectCode1,
                    financialSubObjectCode1, csfStatus1);

            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(csfTimePercent2)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(csfTimePercent2,
                    chartOfAccountsCode2, accountNumber2, subAccountNumber2, financialObjectCode2,
                    financialSubObjectCode2, csfStatus2);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(csfTimePercent3)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(csfTimePercent3,
                    chartOfAccountsCode3, accountNumber3, subAccountNumber3, financialObjectCode3,
                    financialSubObjectCode3, csfStatus3);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(csfTimePercent4)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(csfTimePercent4,
                    chartOfAccountsCode4, accountNumber4, subAccountNumber4, financialObjectCode4,
                    financialSubObjectCode4, csfStatus4);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(csfTimePercent5)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(csfTimePercent5,
                    chartOfAccountsCode5, accountNumber5, subAccountNumber5, financialObjectCode5,
                    financialSubObjectCode5, csfStatus5);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(csfTimePercent6)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(csfTimePercent6,
                    chartOfAccountsCode6, accountNumber6, subAccountNumber6, financialObjectCode6,
                    financialSubObjectCode6, csfStatus6);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(csfTimePercent7)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(csfTimePercent7,
                    chartOfAccountsCode7, accountNumber7, subAccountNumber7, financialObjectCode7,
                    financialSubObjectCode7, csfStatus7);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(csfTimePercent8)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(csfTimePercent8,
                    chartOfAccountsCode8, accountNumber8, subAccountNumber8, financialObjectCode8,
                    financialSubObjectCode8, csfStatus8);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(csfTimePercent9)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(csfTimePercent9,
                    chartOfAccountsCode9, accountNumber9, subAccountNumber9, financialObjectCode9,
                    financialSubObjectCode9, csfStatus9);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(csfTimePercent10)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(
                    csfTimePercent10, chartOfAccountsCode10, accountNumber10, subAccountNumber10,
                    financialObjectCode10, financialSubObjectCode10, csfStatus10);
            accountingInfoCollection.add(accountingInfo);
        }

        return accountingInfoCollection;
    }

    @Override
    protected LinkedHashMap toStringMapper() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Gets the key for the current entry which is positionNumber + emplid;
     * 
     * @return
     */
    public String getKey() {
        return this.getPositionNumber() + this.getEmplid();
    }

    /**
     * Gets the employeeType.
     * 
     * @return employeeType
     */
    public String getEmployeeType() {
        return employeeType;
    }

    /**
     * Sets the employeeType.
     * 
     * @param employeeType
     */
    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }

    /**
     * Gets the defaultObjectCode.
     * 
     * @return defaultObjectCode
     */
    public String getDefaultObjectCode() {
        return defaultObjectCode;
    }

    /**
     * Sets the defaultObjectCode
     * 
     * @param defaultObjectCode
     */
    public void setDefaultObjectCode(String defaultObjectCode) {
        this.defaultObjectCode = defaultObjectCode;
    }

    /**
     * Gets the workMonths.
     * 
     * @return workMonths
     */
    public String getWorkMonths() {
        return workMonths;
    }

    /**
     * Sets the workMonths.
     * 
     * @param workMonths
     */
    public void setWorkMonths(String workMonths) {
        this.workMonths = workMonths;
    }

    /**
     * Gets the positionUnionCode.
     * 
     * @return positionUnionCode
     */
    public String getPositionUnionCode() {
        return positionUnionCode;
    }

    /**
     * Sets the positionUnionCode.
     * 
     * @param positionUnionCode
     */
    public void setPositionUnionCode(String positionUnionCode) {
        this.positionUnionCode = positionUnionCode;
    }

    public void refresh() {
        // TODO Auto-generated method stub

    }

    public String getJobCodeDesc() {
        return jobCodeDesc;
    }

    public void setJobCodeDesc(String jobCodeDesc) {
        this.jobCodeDesc = jobCodeDesc;
    }

    public String getJobCodeDescShrt() {
        return jobCodeDescShrt;
    }

    public void setJobCodeDescShrt(String jobCodeDescShrt) {
        this.jobCodeDescShrt = jobCodeDescShrt;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getFullPartTime() {
        return fullPartTime;
    }

    public void setFullPartTime(String fullPartTime) {
        this.fullPartTime = fullPartTime;
    }

    public String getClassInd() {
        return classInd;
    }

    public void setClassInd(String classInd) {
        this.classInd = classInd;
    }

    public String getAddsToActualFte() {
        return addsToActualFte;
    }

    public void setAddsToActualFte(String addsToActualFte) {
        this.addsToActualFte = addsToActualFte;
    }

    public String getCuStateCert() {
        return cuStateCert;
    }

    public void setCuStateCert(String cuStateCert) {
        this.cuStateCert = cuStateCert;
    }

    public String getEmployeeRecord() {
        return employeeRecord;
    }

    public void setEmployeeRecord(String employeeRecord) {
        this.employeeRecord = employeeRecord;
    }

    public String getEmployeeStatus() {
        return employeeStatus;
    }

    public void setEmployeeStatus(String employeeStatus) {
        this.employeeStatus = employeeStatus;
    }

    public String getJobStandardHours() {
        return jobStandardHours;
    }

    public void setJobStandardHours(String jobStandardHours) {
        this.jobStandardHours = jobStandardHours;
    }

    public String getJobCodeStandardHours() {
        return jobCodeStandardHours;
    }

    public void setJobCodeStandardHours(String jobCodeStandardHours) {
        this.jobCodeStandardHours = jobCodeStandardHours;
    }

    public String getEmployeeClass() {
        return employeeClass;
    }

    public void setEmployeeClass(String employeeClass) {
        this.employeeClass = employeeClass;
    }

    public String getEarningDistributionType() {
        return earningDistributionType;
    }

    public void setEarningDistributionType(String earningDistributionType) {
        this.earningDistributionType = earningDistributionType;
    }

    public String getCompRate() {
        return compRate;
    }

    public void setCompRate(String compRate) {
        this.compRate = compRate;
    }

    public String getAnnualBenefitBaseRate() {
        return annualBenefitBaseRate;
    }

    public void setAnnualBenefitBaseRate(String annualBenefitBaseRate) {
        this.annualBenefitBaseRate = annualBenefitBaseRate;
    }

    public String getJobFamily() {
        return jobFamily;
    }

    public void setJobFamily(String jobFamily) {
        this.jobFamily = jobFamily;
    }

    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public String getCuAbbrFlag() {
        return cuAbbrFlag;
    }

    public void setCuAbbrFlag(String cuAbbrFlag) {
        this.cuAbbrFlag = cuAbbrFlag;
    }

    public String getAnnualRate() {
        return annualRate;
    }

    public void setAnnualRate(String annualRate) {
        this.annualRate = annualRate;
    }

    public String getPosTimePercent1() {
        return posTimePercent1;
    }

    public void setPosTimePercent1(String posTimePercent1) {
        this.posTimePercent1 = posTimePercent1;
    }

    public String getPosChartOfAccountsCode1() {
        return posChartOfAccountsCode1;
    }

    public void setPosChartOfAccountsCode1(String posChartOfAccountsCode1) {
        this.posChartOfAccountsCode1 = posChartOfAccountsCode1;
    }

    public String getPosAccountNumber1() {
        return posAccountNumber1;
    }

    public void setPosAccountNumber1(String posAccountNumber1) {
        this.posAccountNumber1 = posAccountNumber1;
    }

    public String getPosSubAccountNumber1() {
        return posSubAccountNumber1;
    }

    public void setPosSubAccountNumber1(String posSubAccountNumber1) {
        this.posSubAccountNumber1 = posSubAccountNumber1;
    }

    public String getPosFinancialObjectCode1() {
        return posFinancialObjectCode1;
    }

    public void setPosFinancialObjectCode1(String posFinancialObjectCode1) {
        this.posFinancialObjectCode1 = posFinancialObjectCode1;
    }

    public String getPosFinancialSubObjectCode1() {
        return posFinancialSubObjectCode1;
    }

    public void setPosFinancialSubObjectCode1(String posFinancialSubObjectCode1) {
        this.posFinancialSubObjectCode1 = posFinancialSubObjectCode1;
    }

    public String getPosChartOfAccountsCode2() {
        return posChartOfAccountsCode2;
    }

    public void setPosChartOfAccountsCode2(String posChartOfAccountsCode2) {
        this.posChartOfAccountsCode2 = posChartOfAccountsCode2;
    }

    public String getPosAccountNumber2() {
        return posAccountNumber2;
    }

    public void setPosAccountNumber2(String posAccountNumber2) {
        this.posAccountNumber2 = posAccountNumber2;
    }

    public String getPosSubAccountNumber2() {
        return posSubAccountNumber2;
    }

    public void setPosSubAccountNumber2(String posSubAccountNumber2) {
        this.posSubAccountNumber2 = posSubAccountNumber2;
    }

    public String getPosFinancialObjectCode2() {
        return posFinancialObjectCode2;
    }

    public void setPosFinancialObjectCode2(String posFinancialObjectCode2) {
        this.posFinancialObjectCode2 = posFinancialObjectCode2;
    }

    public String getPosFinancialSubObjectCode2() {
        return posFinancialSubObjectCode2;
    }

    public void setPosFinancialSubObjectCode2(String posFinancialSubObjectCode2) {
        this.posFinancialSubObjectCode2 = posFinancialSubObjectCode2;
    }

    public String getPosChartOfAccountsCode3() {
        return posChartOfAccountsCode3;
    }

    public void setPosChartOfAccountsCode3(String posChartOfAccountsCode3) {
        this.posChartOfAccountsCode3 = posChartOfAccountsCode3;
    }

    public String getPosAccountNumber3() {
        return posAccountNumber3;
    }

    public void setPosAccountNumber3(String posAccountNumber3) {
        this.posAccountNumber3 = posAccountNumber3;
    }

    public String getPosSubAccountNumber3() {
        return posSubAccountNumber3;
    }

    public void setPosSubAccountNumber3(String posSubAccountNumber3) {
        this.posSubAccountNumber3 = posSubAccountNumber3;
    }

    public String getPosFinancialObjectCode3() {
        return posFinancialObjectCode3;
    }

    public void setPosFinancialObjectCode3(String posFinancialObjectCode3) {
        this.posFinancialObjectCode3 = posFinancialObjectCode3;
    }

    public String getPosFinancialSubObjectCode3() {
        return posFinancialSubObjectCode3;
    }

    public void setPosFinancialSubObjectCode3(String posFinancialSubObjectCode3) {
        this.posFinancialSubObjectCode3 = posFinancialSubObjectCode3;
    }

    public String getPosChartOfAccountsCode4() {
        return posChartOfAccountsCode4;
    }

    public void setPosChartOfAccountsCode4(String posChartOfAccountsCode4) {
        this.posChartOfAccountsCode4 = posChartOfAccountsCode4;
    }

    public String getPosAccountNumber4() {
        return posAccountNumber4;
    }

    public void setPosAccountNumber4(String posAccountNumber4) {
        this.posAccountNumber4 = posAccountNumber4;
    }

    public String getPosSubAccountNumber4() {
        return posSubAccountNumber4;
    }

    public void setPosSubAccountNumber4(String posSubAccountNumber4) {
        this.posSubAccountNumber4 = posSubAccountNumber4;
    }

    public String getPosFinancialObjectCode4() {
        return posFinancialObjectCode4;
    }

    public void setPosFinancialObjectCode4(String posFinancialObjectCode4) {
        this.posFinancialObjectCode4 = posFinancialObjectCode4;
    }

    public String getPosFinancialSubObjectCode4() {
        return posFinancialSubObjectCode4;
    }

    public void setPosFinancialSubObjectCode4(String posFinancialSubObjectCode4) {
        this.posFinancialSubObjectCode4 = posFinancialSubObjectCode4;
    }

    public String getPosChartOfAccountsCode5() {
        return posChartOfAccountsCode5;
    }

    public void setPosChartOfAccountsCode5(String posChartOfAccountsCode5) {
        this.posChartOfAccountsCode5 = posChartOfAccountsCode5;
    }

    public String getPosAccountNumber5() {
        return posAccountNumber5;
    }

    public void setPosAccountNumber5(String posAccountNumber5) {
        this.posAccountNumber5 = posAccountNumber5;
    }

    public String getPosSubAccountNumber5() {
        return posSubAccountNumber5;
    }

    public void setPosSubAccountNumber5(String posSubAccountNumber5) {
        this.posSubAccountNumber5 = posSubAccountNumber5;
    }

    public String getPosFinancialObjectCode5() {
        return posFinancialObjectCode5;
    }

    public void setPosFinancialObjectCode5(String posFinancialObjectCode5) {
        this.posFinancialObjectCode5 = posFinancialObjectCode5;
    }

    public String getPosFinancialSubObjectCode5() {
        return posFinancialSubObjectCode5;
    }

    public void setPosFinancialSubObjectCode5(String posFinancialSubObjectCode5) {
        this.posFinancialSubObjectCode5 = posFinancialSubObjectCode5;
    }

    public String getPosChartOfAccountsCode6() {
        return posChartOfAccountsCode6;
    }

    public void setPosChartOfAccountsCode6(String posChartOfAccountsCode6) {
        this.posChartOfAccountsCode6 = posChartOfAccountsCode6;
    }

    public String getPosAccountNumber6() {
        return posAccountNumber6;
    }

    public void setPosAccountNumber6(String posAccountNumber6) {
        this.posAccountNumber6 = posAccountNumber6;
    }

    public String getPosSubAccountNumber6() {
        return posSubAccountNumber6;
    }

    public void setPosSubAccountNumber6(String posSubAccountNumber6) {
        this.posSubAccountNumber6 = posSubAccountNumber6;
    }

    public String getPosFinancialObjectCode6() {
        return posFinancialObjectCode6;
    }

    public void setPosFinancialObjectCode6(String posFinancialObjectCode6) {
        this.posFinancialObjectCode6 = posFinancialObjectCode6;
    }

    public String getPosFinancialSubObjectCode6() {
        return posFinancialSubObjectCode6;
    }

    public void setPosFinancialSubObjectCode6(String posFinancialSubObjectCode6) {
        this.posFinancialSubObjectCode6 = posFinancialSubObjectCode6;
    }

    public String getPosChartOfAccountsCode7() {
        return posChartOfAccountsCode7;
    }

    public void setPosChartOfAccountsCode7(String posChartOfAccountsCode7) {
        this.posChartOfAccountsCode7 = posChartOfAccountsCode7;
    }

    public String getPosAccountNumber7() {
        return posAccountNumber7;
    }

    public void setPosAccountNumber7(String posAccountNumber7) {
        this.posAccountNumber7 = posAccountNumber7;
    }

    public String getPosSubAccountNumber7() {
        return posSubAccountNumber7;
    }

    public void setPosSubAccountNumber7(String posSubAccountNumber7) {
        this.posSubAccountNumber7 = posSubAccountNumber7;
    }

    public String getPosFinancialObjectCode7() {
        return posFinancialObjectCode7;
    }

    public void setPosFinancialObjectCode7(String posFinancialObjectCode7) {
        this.posFinancialObjectCode7 = posFinancialObjectCode7;
    }

    public String getPosFinancialSubObjectCode7() {
        return posFinancialSubObjectCode7;
    }

    public void setPosFinancialSubObjectCode7(String posFinancialSubObjectCode7) {
        this.posFinancialSubObjectCode7 = posFinancialSubObjectCode7;
    }

    public String getPosChartOfAccountsCode8() {
        return posChartOfAccountsCode8;
    }

    public void setPosChartOfAccountsCode8(String posChartOfAccountsCode8) {
        this.posChartOfAccountsCode8 = posChartOfAccountsCode8;
    }

    public String getPosAccountNumber8() {
        return posAccountNumber8;
    }

    public void setPosAccountNumber8(String posAccountNumber8) {
        this.posAccountNumber8 = posAccountNumber8;
    }

    public String getPosSubAccountNumber8() {
        return posSubAccountNumber8;
    }

    public void setPosSubAccountNumber8(String posSubAccountNumber8) {
        this.posSubAccountNumber8 = posSubAccountNumber8;
    }

    public String getPosFinancialObjectCode8() {
        return posFinancialObjectCode8;
    }

    public void setPosFinancialObjectCode8(String posFinancialObjectCode8) {
        this.posFinancialObjectCode8 = posFinancialObjectCode8;
    }

    public String getPosFinancialSubObjectCode8() {
        return posFinancialSubObjectCode8;
    }

    public void setPosFinancialSubObjectCode8(String posFinancialSubObjectCode8) {
        this.posFinancialSubObjectCode8 = posFinancialSubObjectCode8;
    }

    public String getPosChartOfAccountsCode9() {
        return posChartOfAccountsCode9;
    }

    public void setPosChartOfAccountsCode9(String posChartOfAccountsCode9) {
        this.posChartOfAccountsCode9 = posChartOfAccountsCode9;
    }

    public String getPosAccountNumber9() {
        return posAccountNumber9;
    }

    public void setPosAccountNumber9(String posAccountNumber9) {
        this.posAccountNumber9 = posAccountNumber9;
    }

    public String getPosSubAccountNumber9() {
        return posSubAccountNumber9;
    }

    public void setPosSubAccountNumber9(String posSubAccountNumber9) {
        this.posSubAccountNumber9 = posSubAccountNumber9;
    }

    public String getPosFinancialObjectCode9() {
        return posFinancialObjectCode9;
    }

    public void setPosFinancialObjectCode9(String posFinancialObjectCode9) {
        this.posFinancialObjectCode9 = posFinancialObjectCode9;
    }

    public String getPosFinancialSubObjectCode9() {
        return posFinancialSubObjectCode9;
    }

    public void setPosFinancialSubObjectCode9(String posFinancialSubObjectCode9) {
        this.posFinancialSubObjectCode9 = posFinancialSubObjectCode9;
    }

    public String getPosChartOfAccountsCode10() {
        return posChartOfAccountsCode10;
    }

    public void setPosChartOfAccountsCode10(String posChartOfAccountsCode10) {
        this.posChartOfAccountsCode10 = posChartOfAccountsCode10;
    }

    public String getPosAccountNumber10() {
        return posAccountNumber10;
    }

    public void setPosAccountNumber10(String posAccountNumber10) {
        this.posAccountNumber10 = posAccountNumber10;
    }

    public String getPosSubAccountNumber10() {
        return posSubAccountNumber10;
    }

    public void setPosSubAccountNumber10(String posSubAccountNumber10) {
        this.posSubAccountNumber10 = posSubAccountNumber10;
    }

    public String getPosFinancialObjectCode10() {
        return posFinancialObjectCode10;
    }

    public void setPosFinancialObjectCode10(String posFinancialObjectCode10) {
        this.posFinancialObjectCode10 = posFinancialObjectCode10;
    }

    public String getPosFinancialSubObjectCode10() {
        return posFinancialSubObjectCode10;
    }

    public void setPosFinancialSubObjectCode10(String posFinancialSubObjectCode10) {
        this.posFinancialSubObjectCode10 = posFinancialSubObjectCode10;
    }

    public String getPosTimePercent2() {
        return posTimePercent2;
    }

    public void setPosTimePercent2(String posTimePercent2) {
        this.posTimePercent2 = posTimePercent2;
    }

    public String getPosTimePercent3() {
        return posTimePercent3;
    }

    public void setPosTimePercent3(String posTimePercent3) {
        this.posTimePercent3 = posTimePercent3;
    }

    public String getPosTimePercent4() {
        return posTimePercent4;
    }

    public void setPosTimePercent4(String posTimePercent4) {
        this.posTimePercent4 = posTimePercent4;
    }

    public String getPosTimePercent5() {
        return posTimePercent5;
    }

    public void setPosTimePercent5(String posTimePercent5) {
        this.posTimePercent5 = posTimePercent5;
    }

    public String getPosTimePercent6() {
        return posTimePercent6;
    }

    public void setPosTimePercent6(String posTimePercent6) {
        this.posTimePercent6 = posTimePercent6;
    }

    public String getPosTimePercent7() {
        return posTimePercent7;
    }

    public void setPosTimePercent7(String posTimePercent7) {
        this.posTimePercent7 = posTimePercent7;
    }

    public String getPosTimePercent8() {
        return posTimePercent8;
    }

    public void setPosTimePercent8(String posTimePercent8) {
        this.posTimePercent8 = posTimePercent8;
    }

    public String getPosTimePercent9() {
        return posTimePercent9;
    }

    public void setPosTimePercent9(String posTimePercent9) {
        this.posTimePercent9 = posTimePercent9;
    }

    public String getPosTimePercent10() {
        return posTimePercent10;
    }

    public void setPosTimePercent10(String posTimePercent10) {
        this.posTimePercent10 = posTimePercent10;
    }

    public CUBCConstants.PSEntryStatus getStatus() {
        return deleteStatus;
    }

    public void setStatus(CUBCConstants.PSEntryStatus status) {
        this.deleteStatus = status;
    }

    public String getJobFunction() {
        return jobFunction;
    }

    public void setJobFunction(String jobFunction) {
        this.jobFunction = jobFunction;
    }

    public String getJobFunctionDesc() {
        return jobFunctionDesc;
    }

    public void setJobFunctionDesc(String jobFunctionDesc) {
        this.jobFunctionDesc = jobFunctionDesc;
    }

    public String getCompFreq() {
        return compFreq;
    }

    public void setCompFreq(String compFreq) {
        this.compFreq = compFreq;
    }

    public String getCuPlannedFTE() {
        return cuPlannedFTE;
    }

    public void setCuPlannedFTE(String cuPlannedFTE) {
        this.cuPlannedFTE = cuPlannedFTE;
    }

    public CUBCConstants.PSEntryStatus getDeleteStatus() {
        return deleteStatus;
    }

    public void setDeleteStatus(CUBCConstants.PSEntryStatus deleteStatus) {
        this.deleteStatus = deleteStatus;
    }

    public StatusFlag getChangeStatus() {
        return changeStatus;
    }

    public void setChangeStatus(StatusFlag changeStatus) {
        this.changeStatus = changeStatus;
    }

    public StatusFlag getPosStatus1() {
        return posStatus1;
    }

    public void setPosStatus1(StatusFlag posStatus1) {
        this.posStatus1 = posStatus1;
    }

    public StatusFlag getPosStatus2() {
        return posStatus2;
    }

    public void setPosStatus2(StatusFlag posStatus2) {
        this.posStatus2 = posStatus2;
    }

    public StatusFlag getPosStatus3() {
        return posStatus3;
    }

    public void setPosStatus3(StatusFlag posStatus3) {
        this.posStatus3 = posStatus3;
    }

    public StatusFlag getPosStatus4() {
        return posStatus4;
    }

    public void setPosStatus4(StatusFlag posStatus4) {
        this.posStatus4 = posStatus4;
    }

    public StatusFlag getPosStatus5() {
        return posStatus5;
    }

    public void setPosStatus5(StatusFlag posStatus5) {
        this.posStatus5 = posStatus5;
    }

    public StatusFlag getPosStatus6() {
        return posStatus6;
    }

    public void setPosStatus6(StatusFlag posStatus6) {
        this.posStatus6 = posStatus6;
    }

    public StatusFlag getPosStatus7() {
        return posStatus7;
    }

    public void setPosStatus7(StatusFlag posStatus7) {
        this.posStatus7 = posStatus7;
    }

    public StatusFlag getPosStatus8() {
        return posStatus8;
    }

    public void setPosStatus8(StatusFlag posStatus8) {
        this.posStatus8 = posStatus8;
    }

    public StatusFlag getPosStatus9() {
        return posStatus9;
    }

    public void setPosStatus9(StatusFlag posStatus9) {
        this.posStatus9 = posStatus9;
    }

    public StatusFlag getPosStatus10() {
        return posStatus10;
    }

    public void setPosStatus10(StatusFlag posStatus10) {
        this.posStatus10 = posStatus10;
    }

    public StatusFlag getCsfStatus1() {
        return csfStatus1;
    }

    public void setCsfStatus1(StatusFlag csfStatus1) {
        this.csfStatus1 = csfStatus1;
    }

    public StatusFlag getCsfStatus2() {
        return csfStatus2;
    }

    public void setCsfStatus2(StatusFlag csfStatus2) {
        this.csfStatus2 = csfStatus2;
    }

    public StatusFlag getCsfStatus3() {
        return csfStatus3;
    }

    public void setCsfStatus3(StatusFlag csfStatus3) {
        this.csfStatus3 = csfStatus3;
    }

    public StatusFlag getCsfStatus4() {
        return csfStatus4;
    }

    public void setCsfStatus4(StatusFlag csfStatus4) {
        this.csfStatus4 = csfStatus4;
    }

    public StatusFlag getCsfStatus5() {
        return csfStatus5;
    }

    public void setCsfStatus5(StatusFlag csfStatus5) {
        this.csfStatus5 = csfStatus5;
    }

    public StatusFlag getCsfStatus6() {
        return csfStatus6;
    }

    public void setCsfStatus6(StatusFlag csfStatus6) {
        this.csfStatus6 = csfStatus6;
    }

    public StatusFlag getCsfStatus7() {
        return csfStatus7;
    }

    public void setCsfStatus7(StatusFlag csfStatus7) {
        this.csfStatus7 = csfStatus7;
    }

    public StatusFlag getCsfStatus8() {
        return csfStatus8;
    }

    public void setCsfStatus8(StatusFlag csfStatus8) {
        this.csfStatus8 = csfStatus8;
    }

    /**
     * Gets the csfStatus9.
     * 
     * @return csfStatus9
     */
    public StatusFlag getCsfStatus9() {
        return csfStatus9;
    }

    /**
     * Sets the csfStatus9.
     * 
     * @param csfStatus9
     */
    public void setCsfStatus9(StatusFlag csfStatus9) {
        this.csfStatus9 = csfStatus9;
    }

    /**
     * Gets the csfStatus10.
     * 
     * @return csfStatus10
     */
    public StatusFlag getCsfStatus10() {
        return csfStatus10;
    }

    /**
     * Set the csfStatus10.
     * 
     * @param csfStatus10
     */
    public void setCsfStatus10(StatusFlag csfStatus10) {
        this.csfStatus10 = csfStatus10;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accountNumber1 == null) ? 0 : accountNumber1.hashCode());
        result = prime * result + ((accountNumber10 == null) ? 0 : accountNumber10.hashCode());
        result = prime * result + ((accountNumber2 == null) ? 0 : accountNumber2.hashCode());
        result = prime * result + ((accountNumber3 == null) ? 0 : accountNumber3.hashCode());
        result = prime * result + ((accountNumber4 == null) ? 0 : accountNumber4.hashCode());
        result = prime * result + ((accountNumber5 == null) ? 0 : accountNumber5.hashCode());
        result = prime * result + ((accountNumber6 == null) ? 0 : accountNumber6.hashCode());
        result = prime * result + ((accountNumber7 == null) ? 0 : accountNumber7.hashCode());
        result = prime * result + ((accountNumber8 == null) ? 0 : accountNumber8.hashCode());
        result = prime * result + ((accountNumber9 == null) ? 0 : accountNumber9.hashCode());
        result = prime * result + ((addsToActualFte == null) ? 0 : addsToActualFte.hashCode());
        result = prime * result + ((annualBenefitBaseRate == null) ? 0 : annualBenefitBaseRate.hashCode());
        result = prime * result + ((annualRate == null) ? 0 : annualRate.hashCode());
        result = prime * result + ((chartOfAccountsCode1 == null) ? 0 : chartOfAccountsCode1.hashCode());
        result = prime * result + ((chartOfAccountsCode10 == null) ? 0 : chartOfAccountsCode10.hashCode());
        result = prime * result + ((chartOfAccountsCode2 == null) ? 0 : chartOfAccountsCode2.hashCode());
        result = prime * result + ((chartOfAccountsCode3 == null) ? 0 : chartOfAccountsCode3.hashCode());
        result = prime * result + ((chartOfAccountsCode4 == null) ? 0 : chartOfAccountsCode4.hashCode());
        result = prime * result + ((chartOfAccountsCode5 == null) ? 0 : chartOfAccountsCode5.hashCode());
        result = prime * result + ((chartOfAccountsCode6 == null) ? 0 : chartOfAccountsCode6.hashCode());
        result = prime * result + ((chartOfAccountsCode7 == null) ? 0 : chartOfAccountsCode7.hashCode());
        result = prime * result + ((chartOfAccountsCode8 == null) ? 0 : chartOfAccountsCode8.hashCode());
        result = prime * result + ((chartOfAccountsCode9 == null) ? 0 : chartOfAccountsCode9.hashCode());
        result = prime * result + ((classInd == null) ? 0 : classInd.hashCode());
        result = prime * result + ((compFreq == null) ? 0 : compFreq.hashCode());
        result = prime * result + ((compRate == null) ? 0 : compRate.hashCode());
        result = prime * result + ((company == null) ? 0 : company.hashCode());
        result = prime * result + ((csfTimePercent1 == null) ? 0 : csfTimePercent1.hashCode());
        result = prime * result + ((csfTimePercent10 == null) ? 0 : csfTimePercent10.hashCode());
        result = prime * result + ((csfTimePercent2 == null) ? 0 : csfTimePercent2.hashCode());
        result = prime * result + ((csfTimePercent3 == null) ? 0 : csfTimePercent3.hashCode());
        result = prime * result + ((csfTimePercent4 == null) ? 0 : csfTimePercent4.hashCode());
        result = prime * result + ((csfTimePercent5 == null) ? 0 : csfTimePercent5.hashCode());
        result = prime * result + ((csfTimePercent6 == null) ? 0 : csfTimePercent6.hashCode());
        result = prime * result + ((csfTimePercent7 == null) ? 0 : csfTimePercent7.hashCode());
        result = prime * result + ((csfTimePercent8 == null) ? 0 : csfTimePercent8.hashCode());
        result = prime * result + ((csfTimePercent9 == null) ? 0 : csfTimePercent9.hashCode());
        result = prime * result + ((cuAbbrFlag == null) ? 0 : cuAbbrFlag.hashCode());
        result = prime * result + ((cuPlannedFTE == null) ? 0 : cuPlannedFTE.hashCode());
        result = prime * result + ((cuStateCert == null) ? 0 : cuStateCert.hashCode());
        result = prime * result + ((defaultObjectCode == null) ? 0 : defaultObjectCode.hashCode());
        result = prime * result + ((earningDistributionType == null) ? 0 : earningDistributionType.hashCode());
        result = prime * result + ((emplid == null) ? 0 : emplid.hashCode());
        result = prime * result + ((employeeClass == null) ? 0 : employeeClass.hashCode());
        result = prime * result + ((employeeRecord == null) ? 0 : employeeRecord.hashCode());
        result = prime * result + ((employeeStatus == null) ? 0 : employeeStatus.hashCode());
        result = prime * result + ((employeeType == null) ? 0 : employeeType.hashCode());
        result = prime * result + ((financialObjectCode1 == null) ? 0 : financialObjectCode1.hashCode());
        result = prime * result + ((financialObjectCode10 == null) ? 0 : financialObjectCode10.hashCode());
        result = prime * result + ((financialObjectCode2 == null) ? 0 : financialObjectCode2.hashCode());
        result = prime * result + ((financialObjectCode3 == null) ? 0 : financialObjectCode3.hashCode());
        result = prime * result + ((financialObjectCode4 == null) ? 0 : financialObjectCode4.hashCode());
        result = prime * result + ((financialObjectCode5 == null) ? 0 : financialObjectCode5.hashCode());
        result = prime * result + ((financialObjectCode6 == null) ? 0 : financialObjectCode6.hashCode());
        result = prime * result + ((financialObjectCode7 == null) ? 0 : financialObjectCode7.hashCode());
        result = prime * result + ((financialObjectCode8 == null) ? 0 : financialObjectCode8.hashCode());
        result = prime * result + ((financialObjectCode9 == null) ? 0 : financialObjectCode9.hashCode());
        result = prime * result + ((financialSubObjectCode1 == null) ? 0 : financialSubObjectCode1.hashCode());
        result = prime * result + ((financialSubObjectCode10 == null) ? 0 : financialSubObjectCode10.hashCode());
        result = prime * result + ((financialSubObjectCode2 == null) ? 0 : financialSubObjectCode2.hashCode());
        result = prime * result + ((financialSubObjectCode3 == null) ? 0 : financialSubObjectCode3.hashCode());
        result = prime * result + ((financialSubObjectCode4 == null) ? 0 : financialSubObjectCode4.hashCode());
        result = prime * result + ((financialSubObjectCode5 == null) ? 0 : financialSubObjectCode5.hashCode());
        result = prime * result + ((financialSubObjectCode6 == null) ? 0 : financialSubObjectCode6.hashCode());
        result = prime * result + ((financialSubObjectCode7 == null) ? 0 : financialSubObjectCode7.hashCode());
        result = prime * result + ((financialSubObjectCode8 == null) ? 0 : financialSubObjectCode8.hashCode());
        result = prime * result + ((financialSubObjectCode9 == null) ? 0 : financialSubObjectCode9.hashCode());
        result = prime * result + ((fullPartTime == null) ? 0 : fullPartTime.hashCode());
        result = prime * result + ((jobCode == null) ? 0 : jobCode.hashCode());
        result = prime * result + ((jobCodeDesc == null) ? 0 : jobCodeDesc.hashCode());
        result = prime * result + ((jobCodeDescShrt == null) ? 0 : jobCodeDescShrt.hashCode());
        result = prime * result + ((jobCodeStandardHours == null) ? 0 : jobCodeStandardHours.hashCode());
        result = prime * result + ((jobFamily == null) ? 0 : jobFamily.hashCode());
        result = prime * result + ((jobFunction == null) ? 0 : jobFunction.hashCode());
        result = prime * result + ((jobFunctionDesc == null) ? 0 : jobFunctionDesc.hashCode());
        result = prime * result + ((jobStandardHours == null) ? 0 : jobStandardHours.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((posAccountNumber1 == null) ? 0 : posAccountNumber1.hashCode());
        result = prime * result + ((posAccountNumber10 == null) ? 0 : posAccountNumber10.hashCode());
        result = prime * result + ((posAccountNumber2 == null) ? 0 : posAccountNumber2.hashCode());
        result = prime * result + ((posAccountNumber3 == null) ? 0 : posAccountNumber3.hashCode());
        result = prime * result + ((posAccountNumber4 == null) ? 0 : posAccountNumber4.hashCode());
        result = prime * result + ((posAccountNumber5 == null) ? 0 : posAccountNumber5.hashCode());
        result = prime * result + ((posAccountNumber6 == null) ? 0 : posAccountNumber6.hashCode());
        result = prime * result + ((posAccountNumber7 == null) ? 0 : posAccountNumber7.hashCode());
        result = prime * result + ((posAccountNumber8 == null) ? 0 : posAccountNumber8.hashCode());
        result = prime * result + ((posAccountNumber9 == null) ? 0 : posAccountNumber9.hashCode());
        result = prime * result + ((posChartOfAccountsCode1 == null) ? 0 : posChartOfAccountsCode1.hashCode());
        result = prime * result + ((posChartOfAccountsCode10 == null) ? 0 : posChartOfAccountsCode10.hashCode());
        result = prime * result + ((posChartOfAccountsCode2 == null) ? 0 : posChartOfAccountsCode2.hashCode());
        result = prime * result + ((posChartOfAccountsCode3 == null) ? 0 : posChartOfAccountsCode3.hashCode());
        result = prime * result + ((posChartOfAccountsCode4 == null) ? 0 : posChartOfAccountsCode4.hashCode());
        result = prime * result + ((posChartOfAccountsCode5 == null) ? 0 : posChartOfAccountsCode5.hashCode());
        result = prime * result + ((posChartOfAccountsCode6 == null) ? 0 : posChartOfAccountsCode6.hashCode());
        result = prime * result + ((posChartOfAccountsCode7 == null) ? 0 : posChartOfAccountsCode7.hashCode());
        result = prime * result + ((posChartOfAccountsCode8 == null) ? 0 : posChartOfAccountsCode8.hashCode());
        result = prime * result + ((posChartOfAccountsCode9 == null) ? 0 : posChartOfAccountsCode9.hashCode());
        result = prime * result + ((posFinancialObjectCode1 == null) ? 0 : posFinancialObjectCode1.hashCode());
        result = prime * result + ((posFinancialObjectCode10 == null) ? 0 : posFinancialObjectCode10.hashCode());
        result = prime * result + ((posFinancialObjectCode2 == null) ? 0 : posFinancialObjectCode2.hashCode());
        result = prime * result + ((posFinancialObjectCode3 == null) ? 0 : posFinancialObjectCode3.hashCode());
        result = prime * result + ((posFinancialObjectCode4 == null) ? 0 : posFinancialObjectCode4.hashCode());
        result = prime * result + ((posFinancialObjectCode5 == null) ? 0 : posFinancialObjectCode5.hashCode());
        result = prime * result + ((posFinancialObjectCode6 == null) ? 0 : posFinancialObjectCode6.hashCode());
        result = prime * result + ((posFinancialObjectCode7 == null) ? 0 : posFinancialObjectCode7.hashCode());
        result = prime * result + ((posFinancialObjectCode8 == null) ? 0 : posFinancialObjectCode8.hashCode());
        result = prime * result + ((posFinancialObjectCode9 == null) ? 0 : posFinancialObjectCode9.hashCode());
        result = prime * result + ((posFinancialSubObjectCode1 == null) ? 0 : posFinancialSubObjectCode1.hashCode());
        result = prime * result + ((posFinancialSubObjectCode10 == null) ? 0 : posFinancialSubObjectCode10.hashCode());
        result = prime * result + ((posFinancialSubObjectCode2 == null) ? 0 : posFinancialSubObjectCode2.hashCode());
        result = prime * result + ((posFinancialSubObjectCode3 == null) ? 0 : posFinancialSubObjectCode3.hashCode());
        result = prime * result + ((posFinancialSubObjectCode4 == null) ? 0 : posFinancialSubObjectCode4.hashCode());
        result = prime * result + ((posFinancialSubObjectCode5 == null) ? 0 : posFinancialSubObjectCode5.hashCode());
        result = prime * result + ((posFinancialSubObjectCode6 == null) ? 0 : posFinancialSubObjectCode6.hashCode());
        result = prime * result + ((posFinancialSubObjectCode7 == null) ? 0 : posFinancialSubObjectCode7.hashCode());
        result = prime * result + ((posFinancialSubObjectCode8 == null) ? 0 : posFinancialSubObjectCode8.hashCode());
        result = prime * result + ((posFinancialSubObjectCode9 == null) ? 0 : posFinancialSubObjectCode9.hashCode());
        result = prime * result + ((posSubAccountNumber1 == null) ? 0 : posSubAccountNumber1.hashCode());
        result = prime * result + ((posSubAccountNumber10 == null) ? 0 : posSubAccountNumber10.hashCode());
        result = prime * result + ((posSubAccountNumber2 == null) ? 0 : posSubAccountNumber2.hashCode());
        result = prime * result + ((posSubAccountNumber3 == null) ? 0 : posSubAccountNumber3.hashCode());
        result = prime * result + ((posSubAccountNumber4 == null) ? 0 : posSubAccountNumber4.hashCode());
        result = prime * result + ((posSubAccountNumber5 == null) ? 0 : posSubAccountNumber5.hashCode());
        result = prime * result + ((posSubAccountNumber6 == null) ? 0 : posSubAccountNumber6.hashCode());
        result = prime * result + ((posSubAccountNumber7 == null) ? 0 : posSubAccountNumber7.hashCode());
        result = prime * result + ((posSubAccountNumber8 == null) ? 0 : posSubAccountNumber8.hashCode());
        result = prime * result + ((posSubAccountNumber9 == null) ? 0 : posSubAccountNumber9.hashCode());
        result = prime * result + ((posTimePercent1 == null) ? 0 : posTimePercent1.hashCode());
        result = prime * result + ((posTimePercent10 == null) ? 0 : posTimePercent10.hashCode());
        result = prime * result + ((posTimePercent2 == null) ? 0 : posTimePercent2.hashCode());
        result = prime * result + ((posTimePercent3 == null) ? 0 : posTimePercent3.hashCode());
        result = prime * result + ((posTimePercent4 == null) ? 0 : posTimePercent4.hashCode());
        result = prime * result + ((posTimePercent5 == null) ? 0 : posTimePercent5.hashCode());
        result = prime * result + ((posTimePercent6 == null) ? 0 : posTimePercent6.hashCode());
        result = prime * result + ((posTimePercent7 == null) ? 0 : posTimePercent7.hashCode());
        result = prime * result + ((posTimePercent8 == null) ? 0 : posTimePercent8.hashCode());
        result = prime * result + ((posTimePercent9 == null) ? 0 : posTimePercent9.hashCode());
        result = prime * result + ((positionNumber == null) ? 0 : positionNumber.hashCode());
        result = prime * result + ((positionUnionCode == null) ? 0 : positionUnionCode.hashCode());
        result = prime * result + ((subAccountNumber1 == null) ? 0 : subAccountNumber1.hashCode());
        result = prime * result + ((subAccountNumber10 == null) ? 0 : subAccountNumber10.hashCode());
        result = prime * result + ((subAccountNumber2 == null) ? 0 : subAccountNumber2.hashCode());
        result = prime * result + ((subAccountNumber3 == null) ? 0 : subAccountNumber3.hashCode());
        result = prime * result + ((subAccountNumber4 == null) ? 0 : subAccountNumber4.hashCode());
        result = prime * result + ((subAccountNumber5 == null) ? 0 : subAccountNumber5.hashCode());
        result = prime * result + ((subAccountNumber6 == null) ? 0 : subAccountNumber6.hashCode());
        result = prime * result + ((subAccountNumber7 == null) ? 0 : subAccountNumber7.hashCode());
        result = prime * result + ((subAccountNumber8 == null) ? 0 : subAccountNumber8.hashCode());
        result = prime * result + ((subAccountNumber9 == null) ? 0 : subAccountNumber9.hashCode());
        result = prime * result + ((workMonths == null) ? 0 : workMonths.hashCode());
        return result;
    }

    /**
     * Checks all fields except the flags.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PSPositionJobExtractEntry other = (PSPositionJobExtractEntry) obj;
        if (accountNumber1 == null) {
            if (other.accountNumber1 != null)
                return false;
        } else if (!accountNumber1.equals(other.accountNumber1))
            return false;
        if (accountNumber10 == null) {
            if (other.accountNumber10 != null)
                return false;
        } else if (!accountNumber10.equals(other.accountNumber10))
            return false;
        if (accountNumber2 == null) {
            if (other.accountNumber2 != null)
                return false;
        } else if (!accountNumber2.equals(other.accountNumber2))
            return false;
        if (accountNumber3 == null) {
            if (other.accountNumber3 != null)
                return false;
        } else if (!accountNumber3.equals(other.accountNumber3))
            return false;
        if (accountNumber4 == null) {
            if (other.accountNumber4 != null)
                return false;
        } else if (!accountNumber4.equals(other.accountNumber4))
            return false;
        if (accountNumber5 == null) {
            if (other.accountNumber5 != null)
                return false;
        } else if (!accountNumber5.equals(other.accountNumber5))
            return false;
        if (accountNumber6 == null) {
            if (other.accountNumber6 != null)
                return false;
        } else if (!accountNumber6.equals(other.accountNumber6))
            return false;
        if (accountNumber7 == null) {
            if (other.accountNumber7 != null)
                return false;
        } else if (!accountNumber7.equals(other.accountNumber7))
            return false;
        if (accountNumber8 == null) {
            if (other.accountNumber8 != null)
                return false;
        } else if (!accountNumber8.equals(other.accountNumber8))
            return false;
        if (accountNumber9 == null) {
            if (other.accountNumber9 != null)
                return false;
        } else if (!accountNumber9.equals(other.accountNumber9))
            return false;
        if (addsToActualFte == null) {
            if (other.addsToActualFte != null)
                return false;
        } else if (!addsToActualFte.equals(other.addsToActualFte))
            return false;
        if (annualBenefitBaseRate == null) {
            if (other.annualBenefitBaseRate != null)
                return false;
        } else if (!annualBenefitBaseRate.equals(other.annualBenefitBaseRate))
            return false;
        if (annualRate == null) {
            if (other.annualRate != null)
                return false;
        } else if (!annualRate.equals(other.annualRate))
            return false;
        if (chartOfAccountsCode1 == null) {
            if (other.chartOfAccountsCode1 != null)
                return false;
        } else if (!chartOfAccountsCode1.equals(other.chartOfAccountsCode1))
            return false;
        if (chartOfAccountsCode10 == null) {
            if (other.chartOfAccountsCode10 != null)
                return false;
        } else if (!chartOfAccountsCode10.equals(other.chartOfAccountsCode10))
            return false;
        if (chartOfAccountsCode2 == null) {
            if (other.chartOfAccountsCode2 != null)
                return false;
        } else if (!chartOfAccountsCode2.equals(other.chartOfAccountsCode2))
            return false;
        if (chartOfAccountsCode3 == null) {
            if (other.chartOfAccountsCode3 != null)
                return false;
        } else if (!chartOfAccountsCode3.equals(other.chartOfAccountsCode3))
            return false;
        if (chartOfAccountsCode4 == null) {
            if (other.chartOfAccountsCode4 != null)
                return false;
        } else if (!chartOfAccountsCode4.equals(other.chartOfAccountsCode4))
            return false;
        if (chartOfAccountsCode5 == null) {
            if (other.chartOfAccountsCode5 != null)
                return false;
        } else if (!chartOfAccountsCode5.equals(other.chartOfAccountsCode5))
            return false;
        if (chartOfAccountsCode6 == null) {
            if (other.chartOfAccountsCode6 != null)
                return false;
        } else if (!chartOfAccountsCode6.equals(other.chartOfAccountsCode6))
            return false;
        if (chartOfAccountsCode7 == null) {
            if (other.chartOfAccountsCode7 != null)
                return false;
        } else if (!chartOfAccountsCode7.equals(other.chartOfAccountsCode7))
            return false;
        if (chartOfAccountsCode8 == null) {
            if (other.chartOfAccountsCode8 != null)
                return false;
        } else if (!chartOfAccountsCode8.equals(other.chartOfAccountsCode8))
            return false;
        if (chartOfAccountsCode9 == null) {
            if (other.chartOfAccountsCode9 != null)
                return false;
        } else if (!chartOfAccountsCode9.equals(other.chartOfAccountsCode9))
            return false;
        if (classInd == null) {
            if (other.classInd != null)
                return false;
        } else if (!classInd.equals(other.classInd))
            return false;
        if (compFreq == null) {
            if (other.compFreq != null)
                return false;
        } else if (!compFreq.equals(other.compFreq))
            return false;
        if (compRate == null) {
            if (other.compRate != null)
                return false;
        } else if (!compRate.equals(other.compRate))
            return false;
        if (company == null) {
            if (other.company != null)
                return false;
        } else if (!company.equals(other.company))
            return false;
        if (csfTimePercent1 == null) {
            if (other.csfTimePercent1 != null)
                return false;
        } else if (!csfTimePercent1.equals(other.csfTimePercent1))
            return false;
        if (csfTimePercent10 == null) {
            if (other.csfTimePercent10 != null)
                return false;
        } else if (!csfTimePercent10.equals(other.csfTimePercent10))
            return false;
        if (csfTimePercent2 == null) {
            if (other.csfTimePercent2 != null)
                return false;
        } else if (!csfTimePercent2.equals(other.csfTimePercent2))
            return false;
        if (csfTimePercent3 == null) {
            if (other.csfTimePercent3 != null)
                return false;
        } else if (!csfTimePercent3.equals(other.csfTimePercent3))
            return false;
        if (csfTimePercent4 == null) {
            if (other.csfTimePercent4 != null)
                return false;
        } else if (!csfTimePercent4.equals(other.csfTimePercent4))
            return false;
        if (csfTimePercent5 == null) {
            if (other.csfTimePercent5 != null)
                return false;
        } else if (!csfTimePercent5.equals(other.csfTimePercent5))
            return false;
        if (csfTimePercent6 == null) {
            if (other.csfTimePercent6 != null)
                return false;
        } else if (!csfTimePercent6.equals(other.csfTimePercent6))
            return false;
        if (csfTimePercent7 == null) {
            if (other.csfTimePercent7 != null)
                return false;
        } else if (!csfTimePercent7.equals(other.csfTimePercent7))
            return false;
        if (csfTimePercent8 == null) {
            if (other.csfTimePercent8 != null)
                return false;
        } else if (!csfTimePercent8.equals(other.csfTimePercent8))
            return false;
        if (csfTimePercent9 == null) {
            if (other.csfTimePercent9 != null)
                return false;
        } else if (!csfTimePercent9.equals(other.csfTimePercent9))
            return false;
        if (cuAbbrFlag == null) {
            if (other.cuAbbrFlag != null)
                return false;
        } else if (!cuAbbrFlag.equals(other.cuAbbrFlag))
            return false;
        if (cuPlannedFTE == null) {
            if (other.cuPlannedFTE != null)
                return false;
        } else if (!cuPlannedFTE.equals(other.cuPlannedFTE))
            return false;
        if (cuStateCert == null) {
            if (other.cuStateCert != null)
                return false;
        } else if (!cuStateCert.equals(other.cuStateCert))
            return false;
        if (defaultObjectCode == null) {
            if (other.defaultObjectCode != null)
                return false;
        } else if (!defaultObjectCode.equals(other.defaultObjectCode))
            return false;
        if (earningDistributionType == null) {
            if (other.earningDistributionType != null)
                return false;
        } else if (!earningDistributionType.equals(other.earningDistributionType))
            return false;
        if (emplid == null) {
            if (other.emplid != null)
                return false;
        } else if (!emplid.equals(other.emplid))
            return false;
        if (employeeClass == null) {
            if (other.employeeClass != null)
                return false;
        } else if (!employeeClass.equals(other.employeeClass))
            return false;
        if (employeeRecord == null) {
            if (other.employeeRecord != null)
                return false;
        } else if (!employeeRecord.equals(other.employeeRecord))
            return false;
        if (employeeStatus == null) {
            if (other.employeeStatus != null)
                return false;
        } else if (!employeeStatus.equals(other.employeeStatus))
            return false;
        if (employeeType == null) {
            if (other.employeeType != null)
                return false;
        } else if (!employeeType.equals(other.employeeType))
            return false;
        if (financialObjectCode1 == null) {
            if (other.financialObjectCode1 != null)
                return false;
        } else if (!financialObjectCode1.equals(other.financialObjectCode1))
            return false;
        if (financialObjectCode10 == null) {
            if (other.financialObjectCode10 != null)
                return false;
        } else if (!financialObjectCode10.equals(other.financialObjectCode10))
            return false;
        if (financialObjectCode2 == null) {
            if (other.financialObjectCode2 != null)
                return false;
        } else if (!financialObjectCode2.equals(other.financialObjectCode2))
            return false;
        if (financialObjectCode3 == null) {
            if (other.financialObjectCode3 != null)
                return false;
        } else if (!financialObjectCode3.equals(other.financialObjectCode3))
            return false;
        if (financialObjectCode4 == null) {
            if (other.financialObjectCode4 != null)
                return false;
        } else if (!financialObjectCode4.equals(other.financialObjectCode4))
            return false;
        if (financialObjectCode5 == null) {
            if (other.financialObjectCode5 != null)
                return false;
        } else if (!financialObjectCode5.equals(other.financialObjectCode5))
            return false;
        if (financialObjectCode6 == null) {
            if (other.financialObjectCode6 != null)
                return false;
        } else if (!financialObjectCode6.equals(other.financialObjectCode6))
            return false;
        if (financialObjectCode7 == null) {
            if (other.financialObjectCode7 != null)
                return false;
        } else if (!financialObjectCode7.equals(other.financialObjectCode7))
            return false;
        if (financialObjectCode8 == null) {
            if (other.financialObjectCode8 != null)
                return false;
        } else if (!financialObjectCode8.equals(other.financialObjectCode8))
            return false;
        if (financialObjectCode9 == null) {
            if (other.financialObjectCode9 != null)
                return false;
        } else if (!financialObjectCode9.equals(other.financialObjectCode9))
            return false;
        if (financialSubObjectCode1 == null) {
            if (other.financialSubObjectCode1 != null)
                return false;
        } else if (!financialSubObjectCode1.equals(other.financialSubObjectCode1))
            return false;
        if (financialSubObjectCode10 == null) {
            if (other.financialSubObjectCode10 != null)
                return false;
        } else if (!financialSubObjectCode10.equals(other.financialSubObjectCode10))
            return false;
        if (financialSubObjectCode2 == null) {
            if (other.financialSubObjectCode2 != null)
                return false;
        } else if (!financialSubObjectCode2.equals(other.financialSubObjectCode2))
            return false;
        if (financialSubObjectCode3 == null) {
            if (other.financialSubObjectCode3 != null)
                return false;
        } else if (!financialSubObjectCode3.equals(other.financialSubObjectCode3))
            return false;
        if (financialSubObjectCode4 == null) {
            if (other.financialSubObjectCode4 != null)
                return false;
        } else if (!financialSubObjectCode4.equals(other.financialSubObjectCode4))
            return false;
        if (financialSubObjectCode5 == null) {
            if (other.financialSubObjectCode5 != null)
                return false;
        } else if (!financialSubObjectCode5.equals(other.financialSubObjectCode5))
            return false;
        if (financialSubObjectCode6 == null) {
            if (other.financialSubObjectCode6 != null)
                return false;
        } else if (!financialSubObjectCode6.equals(other.financialSubObjectCode6))
            return false;
        if (financialSubObjectCode7 == null) {
            if (other.financialSubObjectCode7 != null)
                return false;
        } else if (!financialSubObjectCode7.equals(other.financialSubObjectCode7))
            return false;
        if (financialSubObjectCode8 == null) {
            if (other.financialSubObjectCode8 != null)
                return false;
        } else if (!financialSubObjectCode8.equals(other.financialSubObjectCode8))
            return false;
        if (financialSubObjectCode9 == null) {
            if (other.financialSubObjectCode9 != null)
                return false;
        } else if (!financialSubObjectCode9.equals(other.financialSubObjectCode9))
            return false;
        if (fullPartTime == null) {
            if (other.fullPartTime != null)
                return false;
        } else if (!fullPartTime.equals(other.fullPartTime))
            return false;
        if (jobCode == null) {
            if (other.jobCode != null)
                return false;
        } else if (!jobCode.equals(other.jobCode))
            return false;
        if (jobCodeDesc == null) {
            if (other.jobCodeDesc != null)
                return false;
        } else if (!jobCodeDesc.equals(other.jobCodeDesc))
            return false;
        if (jobCodeDescShrt == null) {
            if (other.jobCodeDescShrt != null)
                return false;
        } else if (!jobCodeDescShrt.equals(other.jobCodeDescShrt))
            return false;
        if (jobCodeStandardHours == null) {
            if (other.jobCodeStandardHours != null)
                return false;
        } else if (!jobCodeStandardHours.equals(other.jobCodeStandardHours))
            return false;
        if (jobFamily == null) {
            if (other.jobFamily != null)
                return false;
        } else if (!jobFamily.equals(other.jobFamily))
            return false;
        if (jobFunction == null) {
            if (other.jobFunction != null)
                return false;
        } else if (!jobFunction.equals(other.jobFunction))
            return false;
        if (jobFunctionDesc == null) {
            if (other.jobFunctionDesc != null)
                return false;
        } else if (!jobFunctionDesc.equals(other.jobFunctionDesc))
            return false;
        if (jobStandardHours == null) {
            if (other.jobStandardHours != null)
                return false;
        } else if (!jobStandardHours.equals(other.jobStandardHours))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (posAccountNumber1 == null) {
            if (other.posAccountNumber1 != null)
                return false;
        } else if (!posAccountNumber1.equals(other.posAccountNumber1))
            return false;
        if (posAccountNumber10 == null) {
            if (other.posAccountNumber10 != null)
                return false;
        } else if (!posAccountNumber10.equals(other.posAccountNumber10))
            return false;
        if (posAccountNumber2 == null) {
            if (other.posAccountNumber2 != null)
                return false;
        } else if (!posAccountNumber2.equals(other.posAccountNumber2))
            return false;
        if (posAccountNumber3 == null) {
            if (other.posAccountNumber3 != null)
                return false;
        } else if (!posAccountNumber3.equals(other.posAccountNumber3))
            return false;
        if (posAccountNumber4 == null) {
            if (other.posAccountNumber4 != null)
                return false;
        } else if (!posAccountNumber4.equals(other.posAccountNumber4))
            return false;
        if (posAccountNumber5 == null) {
            if (other.posAccountNumber5 != null)
                return false;
        } else if (!posAccountNumber5.equals(other.posAccountNumber5))
            return false;
        if (posAccountNumber6 == null) {
            if (other.posAccountNumber6 != null)
                return false;
        } else if (!posAccountNumber6.equals(other.posAccountNumber6))
            return false;
        if (posAccountNumber7 == null) {
            if (other.posAccountNumber7 != null)
                return false;
        } else if (!posAccountNumber7.equals(other.posAccountNumber7))
            return false;
        if (posAccountNumber8 == null) {
            if (other.posAccountNumber8 != null)
                return false;
        } else if (!posAccountNumber8.equals(other.posAccountNumber8))
            return false;
        if (posAccountNumber9 == null) {
            if (other.posAccountNumber9 != null)
                return false;
        } else if (!posAccountNumber9.equals(other.posAccountNumber9))
            return false;
        if (posChartOfAccountsCode1 == null) {
            if (other.posChartOfAccountsCode1 != null)
                return false;
        } else if (!posChartOfAccountsCode1.equals(other.posChartOfAccountsCode1))
            return false;
        if (posChartOfAccountsCode10 == null) {
            if (other.posChartOfAccountsCode10 != null)
                return false;
        } else if (!posChartOfAccountsCode10.equals(other.posChartOfAccountsCode10))
            return false;
        if (posChartOfAccountsCode2 == null) {
            if (other.posChartOfAccountsCode2 != null)
                return false;
        } else if (!posChartOfAccountsCode2.equals(other.posChartOfAccountsCode2))
            return false;
        if (posChartOfAccountsCode3 == null) {
            if (other.posChartOfAccountsCode3 != null)
                return false;
        } else if (!posChartOfAccountsCode3.equals(other.posChartOfAccountsCode3))
            return false;
        if (posChartOfAccountsCode4 == null) {
            if (other.posChartOfAccountsCode4 != null)
                return false;
        } else if (!posChartOfAccountsCode4.equals(other.posChartOfAccountsCode4))
            return false;
        if (posChartOfAccountsCode5 == null) {
            if (other.posChartOfAccountsCode5 != null)
                return false;
        } else if (!posChartOfAccountsCode5.equals(other.posChartOfAccountsCode5))
            return false;
        if (posChartOfAccountsCode6 == null) {
            if (other.posChartOfAccountsCode6 != null)
                return false;
        } else if (!posChartOfAccountsCode6.equals(other.posChartOfAccountsCode6))
            return false;
        if (posChartOfAccountsCode7 == null) {
            if (other.posChartOfAccountsCode7 != null)
                return false;
        } else if (!posChartOfAccountsCode7.equals(other.posChartOfAccountsCode7))
            return false;
        if (posChartOfAccountsCode8 == null) {
            if (other.posChartOfAccountsCode8 != null)
                return false;
        } else if (!posChartOfAccountsCode8.equals(other.posChartOfAccountsCode8))
            return false;
        if (posChartOfAccountsCode9 == null) {
            if (other.posChartOfAccountsCode9 != null)
                return false;
        } else if (!posChartOfAccountsCode9.equals(other.posChartOfAccountsCode9))
            return false;
        if (posFinancialObjectCode1 == null) {
            if (other.posFinancialObjectCode1 != null)
                return false;
        } else if (!posFinancialObjectCode1.equals(other.posFinancialObjectCode1))
            return false;
        if (posFinancialObjectCode10 == null) {
            if (other.posFinancialObjectCode10 != null)
                return false;
        } else if (!posFinancialObjectCode10.equals(other.posFinancialObjectCode10))
            return false;
        if (posFinancialObjectCode2 == null) {
            if (other.posFinancialObjectCode2 != null)
                return false;
        } else if (!posFinancialObjectCode2.equals(other.posFinancialObjectCode2))
            return false;
        if (posFinancialObjectCode3 == null) {
            if (other.posFinancialObjectCode3 != null)
                return false;
        } else if (!posFinancialObjectCode3.equals(other.posFinancialObjectCode3))
            return false;
        if (posFinancialObjectCode4 == null) {
            if (other.posFinancialObjectCode4 != null)
                return false;
        } else if (!posFinancialObjectCode4.equals(other.posFinancialObjectCode4))
            return false;
        if (posFinancialObjectCode5 == null) {
            if (other.posFinancialObjectCode5 != null)
                return false;
        } else if (!posFinancialObjectCode5.equals(other.posFinancialObjectCode5))
            return false;
        if (posFinancialObjectCode6 == null) {
            if (other.posFinancialObjectCode6 != null)
                return false;
        } else if (!posFinancialObjectCode6.equals(other.posFinancialObjectCode6))
            return false;
        if (posFinancialObjectCode7 == null) {
            if (other.posFinancialObjectCode7 != null)
                return false;
        } else if (!posFinancialObjectCode7.equals(other.posFinancialObjectCode7))
            return false;
        if (posFinancialObjectCode8 == null) {
            if (other.posFinancialObjectCode8 != null)
                return false;
        } else if (!posFinancialObjectCode8.equals(other.posFinancialObjectCode8))
            return false;
        if (posFinancialObjectCode9 == null) {
            if (other.posFinancialObjectCode9 != null)
                return false;
        } else if (!posFinancialObjectCode9.equals(other.posFinancialObjectCode9))
            return false;
        if (posFinancialSubObjectCode1 == null) {
            if (other.posFinancialSubObjectCode1 != null)
                return false;
        } else if (!posFinancialSubObjectCode1.equals(other.posFinancialSubObjectCode1))
            return false;
        if (posFinancialSubObjectCode10 == null) {
            if (other.posFinancialSubObjectCode10 != null)
                return false;
        } else if (!posFinancialSubObjectCode10.equals(other.posFinancialSubObjectCode10))
            return false;
        if (posFinancialSubObjectCode2 == null) {
            if (other.posFinancialSubObjectCode2 != null)
                return false;
        } else if (!posFinancialSubObjectCode2.equals(other.posFinancialSubObjectCode2))
            return false;
        if (posFinancialSubObjectCode3 == null) {
            if (other.posFinancialSubObjectCode3 != null)
                return false;
        } else if (!posFinancialSubObjectCode3.equals(other.posFinancialSubObjectCode3))
            return false;
        if (posFinancialSubObjectCode4 == null) {
            if (other.posFinancialSubObjectCode4 != null)
                return false;
        } else if (!posFinancialSubObjectCode4.equals(other.posFinancialSubObjectCode4))
            return false;
        if (posFinancialSubObjectCode5 == null) {
            if (other.posFinancialSubObjectCode5 != null)
                return false;
        } else if (!posFinancialSubObjectCode5.equals(other.posFinancialSubObjectCode5))
            return false;
        if (posFinancialSubObjectCode6 == null) {
            if (other.posFinancialSubObjectCode6 != null)
                return false;
        } else if (!posFinancialSubObjectCode6.equals(other.posFinancialSubObjectCode6))
            return false;
        if (posFinancialSubObjectCode7 == null) {
            if (other.posFinancialSubObjectCode7 != null)
                return false;
        } else if (!posFinancialSubObjectCode7.equals(other.posFinancialSubObjectCode7))
            return false;
        if (posFinancialSubObjectCode8 == null) {
            if (other.posFinancialSubObjectCode8 != null)
                return false;
        } else if (!posFinancialSubObjectCode8.equals(other.posFinancialSubObjectCode8))
            return false;
        if (posFinancialSubObjectCode9 == null) {
            if (other.posFinancialSubObjectCode9 != null)
                return false;
        } else if (!posFinancialSubObjectCode9.equals(other.posFinancialSubObjectCode9))
            return false;
        if (posSubAccountNumber1 == null) {
            if (other.posSubAccountNumber1 != null)
                return false;
        } else if (!posSubAccountNumber1.equals(other.posSubAccountNumber1))
            return false;
        if (posSubAccountNumber10 == null) {
            if (other.posSubAccountNumber10 != null)
                return false;
        } else if (!posSubAccountNumber10.equals(other.posSubAccountNumber10))
            return false;
        if (posSubAccountNumber2 == null) {
            if (other.posSubAccountNumber2 != null)
                return false;
        } else if (!posSubAccountNumber2.equals(other.posSubAccountNumber2))
            return false;
        if (posSubAccountNumber3 == null) {
            if (other.posSubAccountNumber3 != null)
                return false;
        } else if (!posSubAccountNumber3.equals(other.posSubAccountNumber3))
            return false;
        if (posSubAccountNumber4 == null) {
            if (other.posSubAccountNumber4 != null)
                return false;
        } else if (!posSubAccountNumber4.equals(other.posSubAccountNumber4))
            return false;
        if (posSubAccountNumber5 == null) {
            if (other.posSubAccountNumber5 != null)
                return false;
        } else if (!posSubAccountNumber5.equals(other.posSubAccountNumber5))
            return false;
        if (posSubAccountNumber6 == null) {
            if (other.posSubAccountNumber6 != null)
                return false;
        } else if (!posSubAccountNumber6.equals(other.posSubAccountNumber6))
            return false;
        if (posSubAccountNumber7 == null) {
            if (other.posSubAccountNumber7 != null)
                return false;
        } else if (!posSubAccountNumber7.equals(other.posSubAccountNumber7))
            return false;
        if (posSubAccountNumber8 == null) {
            if (other.posSubAccountNumber8 != null)
                return false;
        } else if (!posSubAccountNumber8.equals(other.posSubAccountNumber8))
            return false;
        if (posSubAccountNumber9 == null) {
            if (other.posSubAccountNumber9 != null)
                return false;
        } else if (!posSubAccountNumber9.equals(other.posSubAccountNumber9))
            return false;
        if (posTimePercent1 == null) {
            if (other.posTimePercent1 != null)
                return false;
        } else if (!posTimePercent1.equals(other.posTimePercent1))
            return false;
        if (posTimePercent10 == null) {
            if (other.posTimePercent10 != null)
                return false;
        } else if (!posTimePercent10.equals(other.posTimePercent10))
            return false;
        if (posTimePercent2 == null) {
            if (other.posTimePercent2 != null)
                return false;
        } else if (!posTimePercent2.equals(other.posTimePercent2))
            return false;
        if (posTimePercent3 == null) {
            if (other.posTimePercent3 != null)
                return false;
        } else if (!posTimePercent3.equals(other.posTimePercent3))
            return false;
        if (posTimePercent4 == null) {
            if (other.posTimePercent4 != null)
                return false;
        } else if (!posTimePercent4.equals(other.posTimePercent4))
            return false;
        if (posTimePercent5 == null) {
            if (other.posTimePercent5 != null)
                return false;
        } else if (!posTimePercent5.equals(other.posTimePercent5))
            return false;
        if (posTimePercent6 == null) {
            if (other.posTimePercent6 != null)
                return false;
        } else if (!posTimePercent6.equals(other.posTimePercent6))
            return false;
        if (posTimePercent7 == null) {
            if (other.posTimePercent7 != null)
                return false;
        } else if (!posTimePercent7.equals(other.posTimePercent7))
            return false;
        if (posTimePercent8 == null) {
            if (other.posTimePercent8 != null)
                return false;
        } else if (!posTimePercent8.equals(other.posTimePercent8))
            return false;
        if (posTimePercent9 == null) {
            if (other.posTimePercent9 != null)
                return false;
        } else if (!posTimePercent9.equals(other.posTimePercent9))
            return false;
        if (positionNumber == null) {
            if (other.positionNumber != null)
                return false;
        } else if (!positionNumber.equals(other.positionNumber))
            return false;
        if (positionUnionCode == null) {
            if (other.positionUnionCode != null)
                return false;
        } else if (!positionUnionCode.equals(other.positionUnionCode))
            return false;
        if (subAccountNumber1 == null) {
            if (other.subAccountNumber1 != null)
                return false;
        } else if (!subAccountNumber1.equals(other.subAccountNumber1))
            return false;
        if (subAccountNumber10 == null) {
            if (other.subAccountNumber10 != null)
                return false;
        } else if (!subAccountNumber10.equals(other.subAccountNumber10))
            return false;
        if (subAccountNumber2 == null) {
            if (other.subAccountNumber2 != null)
                return false;
        } else if (!subAccountNumber2.equals(other.subAccountNumber2))
            return false;
        if (subAccountNumber3 == null) {
            if (other.subAccountNumber3 != null)
                return false;
        } else if (!subAccountNumber3.equals(other.subAccountNumber3))
            return false;
        if (subAccountNumber4 == null) {
            if (other.subAccountNumber4 != null)
                return false;
        } else if (!subAccountNumber4.equals(other.subAccountNumber4))
            return false;
        if (subAccountNumber5 == null) {
            if (other.subAccountNumber5 != null)
                return false;
        } else if (!subAccountNumber5.equals(other.subAccountNumber5))
            return false;
        if (subAccountNumber6 == null) {
            if (other.subAccountNumber6 != null)
                return false;
        } else if (!subAccountNumber6.equals(other.subAccountNumber6))
            return false;
        if (subAccountNumber7 == null) {
            if (other.subAccountNumber7 != null)
                return false;
        } else if (!subAccountNumber7.equals(other.subAccountNumber7))
            return false;
        if (subAccountNumber8 == null) {
            if (other.subAccountNumber8 != null)
                return false;
        } else if (!subAccountNumber8.equals(other.subAccountNumber8))
            return false;
        if (subAccountNumber9 == null) {
            if (other.subAccountNumber9 != null)
                return false;
        } else if (!subAccountNumber9.equals(other.subAccountNumber9))
            return false;
        if (workMonths == null) {
            if (other.workMonths != null)
                return false;
        } else if (!workMonths.equals(other.workMonths))
            return false;
        return true;
    }

    /**
     * @return the csfAccountingInfoList
     */
    public List<PSPositionJobExtractAccountingInfo> getCsfAccountingInfoList() {
        return csfAccountingInfoList;
    }

    /**
     * @param csfAccountingInfoList the csfAccountingInfoList to set
     */
    public void setCsfAccountingInfoList() {
        this.csfAccountingInfoList = getCSFAccountingInfoCollection();
    }

    /**
     * @return the posAccountingInfoList
     */
    public List<PSPositionJobExtractAccountingInfo> getPosAccountingInfoList() {
        return posAccountingInfoList;
    }

    /**
     * @param posAccountingInfoList the posAccountingInfoList to set
     */
    public void setPosAccountingInfoList() {
        this.posAccountingInfoList = getPOSAccountingInfoCollection();
    }

    /**
     * Generates a Sting representation of the PS entry.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PSPositionJobExtractEntry [");
        builder.append(positionNumber);
        builder.append(", ");
        builder.append(emplid);
        builder.append(", ");
        builder.append(name);
        builder.append(", ");
        builder.append(employeeType);
        builder.append(", ");
        builder.append(defaultObjectCode);
        builder.append(", ");
        builder.append(positionUnionCode);
        builder.append(", ");
        builder.append(workMonths);
        builder.append(", ");
        builder.append(jobCode);
        builder.append(", ");
        builder.append(jobCodeDesc);
        builder.append(", ");
        builder.append(jobCodeDescShrt);
        builder.append(", ");
        builder.append(company);
        builder.append(", ");
        builder.append(fullPartTime);
        builder.append(", ");
        builder.append(classInd);
        builder.append(", ");
        builder.append(addsToActualFte);
        builder.append(", ");
        builder.append(cuStateCert);
        builder.append(", ");
        builder.append(employeeRecord);
        builder.append(", ");
        builder.append(employeeStatus);
        builder.append(", ");
        builder.append(jobStandardHours);
        builder.append(", ");
        builder.append(jobCodeStandardHours);
        builder.append(", ");
        builder.append(employeeClass);
        builder.append(", ");
        builder.append(earningDistributionType);
        builder.append(", ");
        builder.append(compRate);
        builder.append(", ");
        builder.append(annualBenefitBaseRate);
        builder.append(", ");
        builder.append(cuAbbrFlag);
        builder.append(", ");
        builder.append(annualRate);
        builder.append(", ");
        builder.append(jobFamily);
        builder.append(", ");
        builder.append(compFreq);
        builder.append(", ");
        builder.append(jobFunction);
        builder.append(", ");
        builder.append(jobFunctionDesc);
        builder.append(", ");
        builder.append(cuPlannedFTE);
        builder.append(", ");
        builder.append(posTimePercent1);
        builder.append(", ");
        builder.append(posChartOfAccountsCode1);
        builder.append(", ");
        builder.append(posAccountNumber1);
        builder.append(", ");
        builder.append(posSubAccountNumber1);
        builder.append(", ");
        builder.append(posFinancialObjectCode1);
        builder.append(", ");
        builder.append(posFinancialSubObjectCode1);
        builder.append(", ");
        builder.append(posTimePercent2);
        builder.append(", ");
        builder.append(posChartOfAccountsCode2);
        builder.append(", ");
        builder.append(posAccountNumber2);
        builder.append(", ");
        builder.append(posSubAccountNumber2);
        builder.append(", ");
        builder.append(posFinancialObjectCode2);
        builder.append(", ");
        builder.append(posFinancialSubObjectCode2);
        builder.append(", ");
        builder.append(posTimePercent3);
        builder.append(", ");
        builder.append(posChartOfAccountsCode3);
        builder.append(", ");
        builder.append(posAccountNumber3);
        builder.append(", ");
        builder.append(posSubAccountNumber3);
        builder.append(", ");
        builder.append(posFinancialObjectCode3);
        builder.append(", ");
        builder.append(posFinancialSubObjectCode3);
        builder.append(", ");
        builder.append(posTimePercent4);
        builder.append(", ");
        builder.append(posChartOfAccountsCode4);
        builder.append(", ");
        builder.append(posAccountNumber4);
        builder.append(", ");
        builder.append(posSubAccountNumber4);
        builder.append(", ");
        builder.append(posFinancialObjectCode4);
        builder.append(", ");
        builder.append(posFinancialSubObjectCode4);
        builder.append(", ");
        builder.append(posTimePercent5);
        builder.append(", ");
        builder.append(posChartOfAccountsCode5);
        builder.append(", ");
        builder.append(posAccountNumber5);
        builder.append(", ");
        builder.append(posSubAccountNumber5);
        builder.append(", ");
        builder.append(posFinancialObjectCode5);
        builder.append(", ");
        builder.append(posFinancialSubObjectCode5);
        builder.append(", ");
        builder.append(posTimePercent6);
        builder.append(", ");
        builder.append(posChartOfAccountsCode6);
        builder.append(", ");
        builder.append(posAccountNumber6);
        builder.append(", ");
        builder.append(posSubAccountNumber6);
        builder.append(", ");
        builder.append(posFinancialObjectCode6);
        builder.append(", ");
        builder.append(posFinancialSubObjectCode6);
        builder.append(", ");
        builder.append(posTimePercent7);
        builder.append(", ");
        builder.append(posChartOfAccountsCode7);
        builder.append(", ");
        builder.append(posAccountNumber7);
        builder.append(", ");
        builder.append(posSubAccountNumber7);
        builder.append(", ");
        builder.append(posFinancialObjectCode7);
        builder.append(", ");
        builder.append(posFinancialSubObjectCode7);
        builder.append(", ");
        builder.append(posTimePercent8);
        builder.append(", ");
        builder.append(posChartOfAccountsCode8);
        builder.append(", ");
        builder.append(posAccountNumber8);
        builder.append(", ");
        builder.append(posSubAccountNumber8);
        builder.append(", ");
        builder.append(posFinancialObjectCode8);
        builder.append(", ");
        builder.append(posFinancialSubObjectCode8);
        builder.append(", ");
        builder.append(posTimePercent9);
        builder.append(", ");
        builder.append(posChartOfAccountsCode9);
        builder.append(", ");
        builder.append(posAccountNumber9);
        builder.append(", ");
        builder.append(posSubAccountNumber9);
        builder.append(", ");
        builder.append(posFinancialObjectCode9);
        builder.append(", ");
        builder.append(posFinancialSubObjectCode9);
        builder.append(", ");
        builder.append(posTimePercent10);
        builder.append(", ");
        builder.append(posChartOfAccountsCode10);
        builder.append(", ");
        builder.append(posAccountNumber10);
        builder.append(", ");
        builder.append(posSubAccountNumber10);
        builder.append(", ");
        builder.append(posFinancialObjectCode10);
        builder.append(", ");
        builder.append(posFinancialSubObjectCode10);
        builder.append(", ");
        builder.append(csfTimePercent1);
        builder.append(", ");
        builder.append(chartOfAccountsCode1);
        builder.append(", ");
        builder.append(accountNumber1);
        builder.append(", ");
        builder.append(subAccountNumber1);
        builder.append(", ");
        builder.append(financialObjectCode1);
        builder.append(", ");
        builder.append(financialSubObjectCode1);
        builder.append(", ");
        builder.append(csfTimePercent2);
        builder.append(", ");
        builder.append(chartOfAccountsCode2);
        builder.append(", ");
        builder.append(accountNumber2);
        builder.append(", ");
        builder.append(subAccountNumber2);
        builder.append(", ");
        builder.append(financialObjectCode2);
        builder.append(", ");
        builder.append(financialSubObjectCode2);
        builder.append(", ");
        builder.append(csfTimePercent3);
        builder.append(", ");
        builder.append(chartOfAccountsCode3);
        builder.append(", ");
        builder.append(accountNumber3);
        builder.append(", ");
        builder.append(subAccountNumber3);
        builder.append(", ");
        builder.append(financialObjectCode3);
        builder.append(", ");
        builder.append(financialSubObjectCode3);
        builder.append(", ");
        builder.append(csfTimePercent4);
        builder.append(", ");
        builder.append(chartOfAccountsCode4);
        builder.append(", ");
        builder.append(accountNumber4);
        builder.append(", ");
        builder.append(subAccountNumber4);
        builder.append(", ");
        builder.append(financialObjectCode4);
        builder.append(", ");
        builder.append(financialSubObjectCode4);
        builder.append(", ");
        builder.append(csfTimePercent5);
        builder.append(", ");
        builder.append(chartOfAccountsCode5);
        builder.append(", ");
        builder.append(accountNumber5);
        builder.append(", ");
        builder.append(subAccountNumber5);
        builder.append(", ");
        builder.append(financialObjectCode5);
        builder.append(", ");
        builder.append(financialSubObjectCode5);
        builder.append(", ");
        builder.append(csfTimePercent6);
        builder.append(", ");
        builder.append(chartOfAccountsCode6);
        builder.append(", ");
        builder.append(accountNumber6);
        builder.append(", ");
        builder.append(subAccountNumber6);
        builder.append(", ");
        builder.append(financialObjectCode6);
        builder.append(", ");
        builder.append(financialSubObjectCode6);
        builder.append(", ");
        builder.append(csfTimePercent7);
        builder.append(", ");
        builder.append(chartOfAccountsCode7);
        builder.append(", ");
        builder.append(accountNumber7);
        builder.append(", ");
        builder.append(subAccountNumber7);
        builder.append(", ");
        builder.append(financialObjectCode7);
        builder.append(", ");
        builder.append(financialSubObjectCode7);
        builder.append(", ");
        builder.append(csfTimePercent8);
        builder.append(", ");
        builder.append(chartOfAccountsCode8);
        builder.append(", ");
        builder.append(accountNumber8);
        builder.append(", ");
        builder.append(subAccountNumber8);
        builder.append(", ");
        builder.append(financialObjectCode8);
        builder.append(", ");
        builder.append(financialSubObjectCode8);
        builder.append(", ");
        builder.append(csfTimePercent9);
        builder.append(", ");
        builder.append(chartOfAccountsCode9);
        builder.append(", ");
        builder.append(accountNumber9);
        builder.append(", ");
        builder.append(subAccountNumber9);
        builder.append(", ");
        builder.append(financialObjectCode9);
        builder.append(", ");
        builder.append(financialSubObjectCode9);
        builder.append(", ");
        builder.append(csfTimePercent10);
        builder.append(", ");
        builder.append(chartOfAccountsCode10);
        builder.append(", ");
        builder.append(accountNumber10);
        builder.append(", ");
        builder.append(subAccountNumber10);
        builder.append(", ");
        builder.append(financialObjectCode10);
        builder.append(", ");
        builder.append(financialSubObjectCode10);
        builder.append("]");
        return builder.toString();
    }

}
