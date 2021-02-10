package edu.cornell.kfs.pdp.businessobject;

import java.math.BigInteger;
import java.sql.Date;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.core.api.util.type.KualiInteger;

/**
 * Transient BO representing the a parsed line from a Payee ACH Account Extract .csv file.
 */
public class PayeeACHAccountExtractDetail extends PersistableBusinessObjectBase {
    private static final long serialVersionUID = -2028073232803324343L;
    private static final String DATA_DELIMITER = ";";

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
        sb.append(getNetID()).append(DATA_DELIMITER)
        .append(getFirstName()).append(DATA_DELIMITER)
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

}
