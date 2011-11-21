package edu.cornell.kfs.module.bc.businessobject;

import edu.cornell.kfs.module.bc.CUBCConstants.StatusFlag;

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

    private StatusFlag statusFlag;

    /**
     * Constructs a PSPositionJobExtractAccountingInfo object.
     * 
     * @param csfTimePercent
     * @param chartOfAccountsCode
     * @param accountNumber
     * @param subAccountNumber
     * @param financialObjectCode
     * @param financialSubObjectCode
     * @param statusFlag
     */
    public PSPositionJobExtractAccountingInfo(String csfTimePercent, String chartOfAccountsCode, String accountNumber,
            String subAccountNumber, String financialObjectCode, String financialSubObjectCode, StatusFlag statusFlag) {
        this.csfTimePercent = csfTimePercent;
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.accountNumber = accountNumber;
        this.subAccountNumber = subAccountNumber;
        this.financialObjectCode = financialObjectCode;
        this.financialSubObjectCode = financialSubObjectCode;
        this.statusFlag = statusFlag;
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

    /**
     * This overridden method ...
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accountNumber == null) ? 0 : accountNumber.hashCode());
        result = prime * result + ((chartOfAccountsCode == null) ? 0 : chartOfAccountsCode.hashCode());
        result = prime * result + ((csfTimePercent == null) ? 0 : csfTimePercent.hashCode());
        result = prime * result + ((financialObjectCode == null) ? 0 : financialObjectCode.hashCode());
        result = prime * result + ((financialSubObjectCode == null) ? 0 : financialSubObjectCode.hashCode());
        result = prime * result + ((subAccountNumber == null) ? 0 : subAccountNumber.hashCode());
        return result;
    }

    /**
     * This overridden method ...
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
        PSPositionJobExtractAccountingInfo other = (PSPositionJobExtractAccountingInfo) obj;
        if (accountNumber == null) {
            if (other.accountNumber != null)
                return false;
        } else if (!accountNumber.equals(other.accountNumber))
            return false;
        if (chartOfAccountsCode == null) {
            if (other.chartOfAccountsCode != null)
                return false;
        } else if (!chartOfAccountsCode.equals(other.chartOfAccountsCode))
            return false;
        if (csfTimePercent == null) {
            if (other.csfTimePercent != null)
                return false;
        } else if (!csfTimePercent.equals(other.csfTimePercent))
            return false;
        if (financialObjectCode == null) {
            if (other.financialObjectCode != null)
                return false;
        } else if (!financialObjectCode.equals(other.financialObjectCode))
            return false;
        if (financialSubObjectCode == null) {
            if (other.financialSubObjectCode != null)
                return false;
        } else if (!financialSubObjectCode.equals(other.financialSubObjectCode))
            return false;
        if (subAccountNumber == null) {
            if (other.subAccountNumber != null)
                return false;
        } else if (!subAccountNumber.equals(other.subAccountNumber))
            return false;
        return true;
    }

    /**
     * @return the statusFlag
     */
    public StatusFlag getStatusFlag() {
        return statusFlag;
    }

    /**
     * @param statusFlag the statusFlag to set
     */
    public void setStatusFlag(StatusFlag statusFlag) {
        this.statusFlag = statusFlag;
    }

    /**
     * This overridden method ...
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PSPositionJobExtractAccountingInfo [");
        builder.append(csfTimePercent);
        builder.append(", ");
        builder.append(chartOfAccountsCode);
        builder.append(", ");
        builder.append(accountNumber);
        builder.append(", ");
        builder.append(subAccountNumber);
        builder.append(", ");
        builder.append(financialObjectCode);
        builder.append(", ");
        builder.append(financialSubObjectCode);
        builder.append("]");
        return builder.toString();
    }

}
