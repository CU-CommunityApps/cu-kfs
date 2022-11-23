package edu.cornell.kfs.pdp.businessobject;

import java.math.BigInteger;
import java.sql.Date;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.businessobject.PurgeableBusinessObjectInterface;

/**
 * Transient BO representing the a parsed line from a Payee ACH Account Extract .csv file.
 */
public class PayeeACHAccountExtractDetail extends PersistableBusinessObjectBase implements PurgeableBusinessObjectInterface {
    private static final long serialVersionUID = -2028073232803324343L;
    
    private KualiInteger id;
    private Date createDate;
    private String status;
    private Integer retryCount = 0;
    private String employeeID;
    private String netID;
    private String lastName;
    private String firstName;
    private String paymentType;
    private String balanceAccount;
    private String completedDate;
    private String bankName;
    private String bankRoutingNumber;
    private String bankAccountNumber;
    private String bankAccountType;

    public PayeeACHAccountExtractDetail() {

    }
    
    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public String getNetID() {
        return netID;
    }

    public void setNetID(String netID) {
        this.netID = netID;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getBalanceAccount() {
        return balanceAccount;
    }

    public void setBalanceAccount(String balanceAccount) {
        this.balanceAccount = balanceAccount;
    }

    public String getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(String completedDate) {
        this.completedDate = completedDate;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankRoutingNumber() {
        return bankRoutingNumber;
    }

    public void setBankRoutingNumber(String bankRoutingNumber) {
        this.bankRoutingNumber = bankRoutingNumber;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getBankAccountType() {
        return bankAccountType;
    }

    public void setBankAccountType(String bankAccountType) {
        this.bankAccountType = bankAccountType;
    }

    public String getLogData() {
        StringBuilder sb = new StringBuilder();
        sb.append(getNetID()).append(CUKFSConstants.SEMICOLON)
        .append(getFirstName()).append(CUKFSConstants.SEMICOLON)
        .append(getLastName());
        return sb.toString();
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public KualiInteger getId() {
        return id;
    }

    public void setId(KualiInteger id) {
        this.id = id;
    }
    
    public int getIdIntValue() {
        return id.intValue();
    }
    
    public BigInteger getIdBigIntValue() {
        return id.bigIntegerValue();
    }

    @Override
    public String toString() {
            ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
            
            builder.setExcludeFieldNames(KFSPropertyConstants.BANK_ACCOUNT_NUMBER);
            
            builder.append(KFSPropertyConstants.BANK_ACCOUNT_NUMBER, buildRestrictedFieldPrintableValue(bankAccountNumber));
            
            return builder.build();
    }
    
    private String buildRestrictedFieldPrintableValue(String fieldValue) {
        if (StringUtils.isNotEmpty(fieldValue)) {
            return CUKFSConstants.RESTRICTED_DATA_PLACEHOLDER;
        } else {
            return StringUtils.EMPTY;
        }
    }

    @Override
    public String buildObjectSpecificPurgeableRecordData() {
        return toString();
    }
    
}
