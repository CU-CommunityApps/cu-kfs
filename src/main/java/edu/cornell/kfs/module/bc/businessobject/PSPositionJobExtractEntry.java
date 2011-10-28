package edu.cornell.kfs.module.bc.businessobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kns.bo.BusinessObjectBase;
import org.kuali.rice.kns.util.KualiDecimal;

import edu.cornell.kfs.module.bc.CUBCConstants;

/**
 * A class that holds the fields from the PS position/job extraat file to be loaded in
 * KFS.
 */
public class PSPositionJobExtractEntry extends BusinessObjectBase {

    private String positionNumber;
    private String emplid;
    private String csfAmount;
    private KualiDecimal csfFullTimeEmploymentQuantity;
    private String name;

    private String csfTimePercent1;
    private String chartOfAccountsCode1;
    private String accountNumber1;
    private String subAccountNumber1;
    private String financialObjectCode1;
    private String financialSubObjectCode1;
    private String csfTimePercent2;
    private String chartOfAccountsCode2;
    private String accountNumber2;
    private String subAccountNumber2;
    private String financialObjectCode2;
    private String financialSubObjectCode2;
    private String csfTimePercent3;
    private String chartOfAccountsCode3;
    private String accountNumber3;
    private String subAccountNumber3;
    private String financialObjectCode3;
    private String financialSubObjectCode3;
    private String csfTimePercent4;
    private String chartOfAccountsCode4;
    private String accountNumber4;
    private String subAccountNumber4;
    private String financialObjectCode4;
    private String financialSubObjectCode4;
    private String csfTimePercent5;
    private String chartOfAccountsCode5;
    private String accountNumber5;
    private String subAccountNumber5;
    private String financialObjectCode5;
    private String financialSubObjectCode5;
    private String csfTimePercent6;
    private String chartOfAccountsCode6;
    private String accountNumber6;
    private String subAccountNumber6;
    private String financialObjectCode6;
    private String financialSubObjectCode6;
    private String csfTimePercent7;
    private String chartOfAccountsCode7;
    private String accountNumber7;
    private String subAccountNumber7;
    private String financialObjectCode7;
    private String financialSubObjectCode7;
    private String csfTimePercent8;
    private String chartOfAccountsCode8;
    private String accountNumber8;
    private String subAccountNumber8;
    private String financialObjectCode8;
    private String financialSubObjectCode8;
    private String csfTimePercent9;
    private String chartOfAccountsCode9;
    private String accountNumber9;
    private String subAccountNumber9;
    private String financialObjectCode9;
    private String financialSubObjectCode9;
    private String csfTimePercent10;
    private String chartOfAccountsCode10;
    private String accountNumber10;
    private String subAccountNumber10;
    private String financialObjectCode10;
    private String financialSubObjectCode10;

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
     * Gets the csfAmount.
     * 
     * @return csfAmount
     */
    public String getCsfAmount() {
        return csfAmount;
    }

    /**
     * Sets the csfAmount.
     * 
     * @param csfAmount
     */
    public void setCsfAmount(String csfAmount) {
        this.csfAmount = csfAmount;
    }

    /**
     * Gets the csfFullTimeEmploymentQuantity.
     * 
     * @return csfFullTimeEmploymentQuantity
     */
    public KualiDecimal getCsfFullTimeEmploymentQuantity() {
        return csfFullTimeEmploymentQuantity;
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
     * Sets the csfFullTimeEmploymentQuantity.
     * 
     * @param csfFullTimeEmploymentQuantity
     */
    public void setCsfFullTimeEmploymentQuantity(
            KualiDecimal csfFullTimeEmploymentQuantity) {
        this.csfFullTimeEmploymentQuantity = csfFullTimeEmploymentQuantity;
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
     * Build a collection with the non-blank accounts.
     * 
     * @return a collection of accounting information
     */
    public Collection<PSPositionJobExtractAccountingInfo> getAccountingInfoCollection() {
        List<PSPositionJobExtractAccountingInfo> accountingInfoCollection = new ArrayList<PSPositionJobExtractAccountingInfo>();

        if (StringUtils.isNotBlank(csfTimePercent1)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(csfTimePercent1,
                    chartOfAccountsCode1, accountNumber1, subAccountNumber1, financialObjectCode1,
                    financialSubObjectCode1);

            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(csfTimePercent2)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(csfTimePercent2,
                    chartOfAccountsCode2, accountNumber2, subAccountNumber2, financialObjectCode2,
                    financialSubObjectCode2);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(csfTimePercent3)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(csfTimePercent3,
                    chartOfAccountsCode3, accountNumber3, subAccountNumber3, financialObjectCode3,
                    financialSubObjectCode3);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(csfTimePercent4)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(csfTimePercent4,
                    chartOfAccountsCode4, accountNumber4, subAccountNumber4, financialObjectCode4,
                    financialSubObjectCode4);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(csfTimePercent5)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(csfTimePercent5,
                    chartOfAccountsCode5, accountNumber5, subAccountNumber5, financialObjectCode5,
                    financialSubObjectCode5);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(csfTimePercent6)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(csfTimePercent6,
                    chartOfAccountsCode6, accountNumber6, subAccountNumber6, financialObjectCode6,
                    financialSubObjectCode6);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(csfTimePercent7)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(csfTimePercent7,
                    chartOfAccountsCode7, accountNumber7, subAccountNumber7, financialObjectCode7,
                    financialSubObjectCode7);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(csfTimePercent8)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(csfTimePercent8,
                    chartOfAccountsCode8, accountNumber8, subAccountNumber8, financialObjectCode8,
                    financialSubObjectCode8);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(csfTimePercent9)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(csfTimePercent9,
                    chartOfAccountsCode9, accountNumber9, subAccountNumber9, financialObjectCode9,
                    financialSubObjectCode9);
            accountingInfoCollection.add(accountingInfo);
        }
        if (StringUtils.isNotBlank(csfTimePercent10)) {
            PSPositionJobExtractAccountingInfo accountingInfo = new PSPositionJobExtractAccountingInfo(
                    csfTimePercent10, chartOfAccountsCode10, accountNumber10, subAccountNumber10,
                    financialObjectCode10, financialSubObjectCode10);
            accountingInfoCollection.add(accountingInfo);
        }

        return accountingInfoCollection;
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
        result = prime * result + ((csfAmount == null) ? 0 : csfAmount.hashCode());
        result = prime * result
                + ((csfFullTimeEmploymentQuantity == null) ? 0 : csfFullTimeEmploymentQuantity.hashCode());
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
        result = prime * result + ((emplid == null) ? 0 : emplid.hashCode());
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
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((positionNumber == null) ? 0 : positionNumber.hashCode());
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
        return result;
    }

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
        if (csfAmount == null) {
            if (other.csfAmount != null)
                return false;
        } else if (!csfAmount.equals(other.csfAmount))
            return false;
        if (csfFullTimeEmploymentQuantity == null) {
            if (other.csfFullTimeEmploymentQuantity != null)
                return false;
        } else if (!csfFullTimeEmploymentQuantity.equals(other.csfFullTimeEmploymentQuantity))
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
        if (emplid == null) {
            if (other.emplid != null)
                return false;
        } else if (!emplid.equals(other.emplid))
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
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (positionNumber == null) {
            if (other.positionNumber != null)
                return false;
        } else if (!positionNumber.equals(other.positionNumber))
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
        return true;
    }

    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    protected LinkedHashMap toStringMapper() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PSPositionJobExtractEntry [positionNumber=");
        builder.append(positionNumber);
        builder.append(", emplid=");
        builder.append(emplid);
        builder.append(", csfAmount=");
        builder.append(csfAmount);
        builder.append(", csfFullTimeEmploymentQuantity=");
        builder.append(csfFullTimeEmploymentQuantity);
        builder.append(", name=");
        builder.append(name);
        builder.append(", csfTimePercent1=");
        builder.append(csfTimePercent1);
        builder.append(", chartOfAccountsCode1=");
        builder.append(chartOfAccountsCode1);
        builder.append(", accountNumber1=");
        builder.append(accountNumber1);
        builder.append(", subAccountNumber1=");
        builder.append(subAccountNumber1);
        builder.append(", financialObjectCode1=");
        builder.append(financialObjectCode1);
        builder.append(", financialSubObjectCode1=");
        builder.append(financialSubObjectCode1);
        builder.append(", csfTimePercent2=");
        builder.append(csfTimePercent2);
        builder.append(", chartOfAccountsCode2=");
        builder.append(chartOfAccountsCode2);
        builder.append(", accountNumber2=");
        builder.append(accountNumber2);
        builder.append(", subAccountNumber2=");
        builder.append(subAccountNumber2);
        builder.append(", financialObjectCode2=");
        builder.append(financialObjectCode2);
        builder.append(", financialSubObjectCode2=");
        builder.append(financialSubObjectCode2);
        builder.append(", csfTimePercent3=");
        builder.append(csfTimePercent3);
        builder.append(", chartOfAccountsCode3=");
        builder.append(chartOfAccountsCode3);
        builder.append(", accountNumber3=");
        builder.append(accountNumber3);
        builder.append(", subAccountNumber3=");
        builder.append(subAccountNumber3);
        builder.append(", financialObjectCode3=");
        builder.append(financialObjectCode3);
        builder.append(", financialSubObjectCode3=");
        builder.append(financialSubObjectCode3);
        builder.append(", csfTimePercent4=");
        builder.append(csfTimePercent4);
        builder.append(", chartOfAccountsCode4=");
        builder.append(chartOfAccountsCode4);
        builder.append(", accountNumber4=");
        builder.append(accountNumber4);
        builder.append(", subAccountNumber4=");
        builder.append(subAccountNumber4);
        builder.append(", financialObjectCode4=");
        builder.append(financialObjectCode4);
        builder.append(", financialSubObjectCode4=");
        builder.append(financialSubObjectCode4);
        builder.append(", csfTimePercent5=");
        builder.append(csfTimePercent5);
        builder.append(", chartOfAccountsCode5=");
        builder.append(chartOfAccountsCode5);
        builder.append(", accountNumber5=");
        builder.append(accountNumber5);
        builder.append(", subAccountNumber5=");
        builder.append(subAccountNumber5);
        builder.append(", financialObjectCode5=");
        builder.append(financialObjectCode5);
        builder.append(", financialSubObjectCode5=");
        builder.append(financialSubObjectCode5);
        builder.append(", csfTimePercent6=");
        builder.append(csfTimePercent6);
        builder.append(", chartOfAccountsCode6=");
        builder.append(chartOfAccountsCode6);
        builder.append(", accountNumber6=");
        builder.append(accountNumber6);
        builder.append(", subAccountNumber6=");
        builder.append(subAccountNumber6);
        builder.append(", financialObjectCode6=");
        builder.append(financialObjectCode6);
        builder.append(", financialSubObjectCode6=");
        builder.append(financialSubObjectCode6);
        builder.append(", csfTimePercent7=");
        builder.append(csfTimePercent7);
        builder.append(", chartOfAccountsCode7=");
        builder.append(chartOfAccountsCode7);
        builder.append(", accountNumber7=");
        builder.append(accountNumber7);
        builder.append(", subAccountNumber7=");
        builder.append(subAccountNumber7);
        builder.append(", financialObjectCode7=");
        builder.append(financialObjectCode7);
        builder.append(", financialSubObjectCode7=");
        builder.append(financialSubObjectCode7);
        builder.append(", csfTimePercent8=");
        builder.append(csfTimePercent8);
        builder.append(", chartOfAccountsCode8=");
        builder.append(chartOfAccountsCode8);
        builder.append(", accountNumber8=");
        builder.append(accountNumber8);
        builder.append(", subAccountNumber8=");
        builder.append(subAccountNumber8);
        builder.append(", financialObjectCode8=");
        builder.append(financialObjectCode8);
        builder.append(", financialSubObjectCode8=");
        builder.append(financialSubObjectCode8);
        builder.append(", csfTimePercent9=");
        builder.append(csfTimePercent9);
        builder.append(", chartOfAccountsCode9=");
        builder.append(chartOfAccountsCode9);
        builder.append(", accountNumber9=");
        builder.append(accountNumber9);
        builder.append(", subAccountNumber9=");
        builder.append(subAccountNumber9);
        builder.append(", financialObjectCode9=");
        builder.append(financialObjectCode9);
        builder.append(", financialSubObjectCode9=");
        builder.append(financialSubObjectCode9);
        builder.append(", csfTimePercent10=");
        builder.append(csfTimePercent10);
        builder.append(", chartOfAccountsCode10=");
        builder.append(chartOfAccountsCode10);
        builder.append(", accountNumber10=");
        builder.append(accountNumber10);
        builder.append(", subAccountNumber10=");
        builder.append(subAccountNumber10);
        builder.append(", financialObjectCode10=");
        builder.append(financialObjectCode10);
        builder.append(", financialSubObjectCode10=");
        builder.append(financialSubObjectCode10);
        builder.append("]");
        return builder.toString();
    }

}
