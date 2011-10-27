package edu.cornell.kfs.module.bc.businessobject;

/**
 * This class represents the information in one of the 10 accounting strings in the
 * PSPositionJobExtract.
 */
public class PSPositionJobExtractAccountingInfo {
    private String csfTimePercent;
    private String chartOfAccountsCode;
    private String accountNumber;
    private String subAccountNumber;
    private String financialObjectCode;
    private String financialSubObjectCode;

    /**
     * Constructs a PSPositionJobExtractAccountingInfo object.
     * 
     * @param csfTimePercent
     * @param chartOfAccountsCode
     * @param accountNumber
     * @param subAccountNumber
     * @param financialObjectCode
     * @param financialSubObjectCode
     */
    public PSPositionJobExtractAccountingInfo(String csfTimePercent, String chartOfAccountsCode, String accountNumber,
            String subAccountNumber, String financialObjectCode, String financialSubObjectCode) {
        this.csfTimePercent = csfTimePercent;
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.accountNumber = accountNumber;
        this.subAccountNumber = subAccountNumber;
        this.financialObjectCode = financialObjectCode;
        this.financialSubObjectCode = financialSubObjectCode;
    }

    /**
     * Gets the csfTimePercent
     * 
     * @return csfTimePercent
     */
    public String getCsfTimePercent() {
        return csfTimePercent;
    }

    /**
     * Sets the csfTimePercent.
     * 
     * @param csfTimePercent
     */
    public void setCsfTimePercent(String csfTimePercent) {
        this.csfTimePercent = csfTimePercent;
    }

    /**
     * Gets the chartOfAccountsCode.
     * 
     * @return chartOfAccountsCode
     */
    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    /**
     * Sets the chartOfAccountsCode.
     * 
     * @param chartOfAccountsCode
     */
    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    /**
     * Gets the accountNumber.
     * 
     * @return accountNumber
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * Sets the accountNumber.
     * 
     * @param accountNumber
     */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    /**
     * Gets the subAccountNumber.
     * 
     * @return subAccountNumber
     */
    public String getSubAccountNumber() {
        return subAccountNumber;
    }

    /**
     * Sets the subAccountNumber.
     * 
     * @param subAccountNumber
     */
    public void setSubAccountNumber(String subAccountNumber) {
        this.subAccountNumber = subAccountNumber;
    }

    /**
     * Gets the financialObjectCode.
     * 
     * @return financialObjectCode
     */
    public String getFinancialObjectCode() {
        return financialObjectCode;
    }

    /**
     * Sets the financialObjectCode.
     * 
     * @param financialObjectCode
     */
    public void setFinancialObjectCode(String financialObjectCode) {
        this.financialObjectCode = financialObjectCode;
    }

    /**
     * Gets the financialSubObjectCode.
     * 
     * @return financialSubObjectCode
     */
    public String getFinancialSubObjectCode() {
        return financialSubObjectCode;
    }

    /**
     * Sets the financialSubObjectCode.
     * 
     * @param financialSubObjectCode
     */
    public void setFinancialSubObjectCode(String financialSubObjectCode) {
        this.financialSubObjectCode = financialSubObjectCode;
    }

    public String getKey() {
        return chartOfAccountsCode + accountNumber + subAccountNumber + financialObjectCode + financialSubObjectCode;
    }

}
