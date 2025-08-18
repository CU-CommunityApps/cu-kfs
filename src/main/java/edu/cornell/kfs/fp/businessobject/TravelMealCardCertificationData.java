package edu.cornell.kfs.fp.businessobject;

import java.sql.Date;
import java.util.LinkedHashMap;

import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class TravelMealCardCertificationData extends PersistableBusinessObjectBase {
    
    protected String cardHolderAccountNumber;
    protected String emplid;
    protected String cardHolderName;
    protected String cardStatus;
    protected String cardType;
    protected String netid;
    protected Date lastTransactionDate;
    protected String defaultAccountNumber;
    protected KualiDecimal creditLine;
    protected Date openDate;
    protected Date cycleStartDate;
    protected KualiDecimal cycleSpendToDate;
    protected String activationStatus;
    protected Date fileCreateDate;
    protected Date loadDate;
    
    protected LinkedHashMap toStringMapper() {
        return null;
    }
    
    /**
     * Gets the key for the current entry which is cardHolderAccountNumber + emplid;
     */
    public String getKey() {
        return this.getCardHolderAccountNumber() + this.getEmplid();
    }
    
    public String getCardHolderAccountNumber() {
        return cardHolderAccountNumber;
    }
    
    public void setCardHolderAccountNumber(String cardHolderAccountNumber) {
        this.cardHolderAccountNumber = cardHolderAccountNumber;
    }
    
    public String getEmplid() {
        return emplid;
    }
    
    public void setEmplid(String emplid) {
        this.emplid = emplid;
    }
    
    public String getCardHolderName() {
        return cardHolderName;
    }
    
    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }
    
    public String getCardStatus() {
        return cardStatus;
    }
    
    public void setCardStatus(String cardStatus) {
        this.cardStatus = cardStatus;
    }
    
    public String getCardType() {
        return cardType;
    }
    
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
    
    public String getNetid() {
        return netid;
    }
    
    public void setNetid(String netid) {
        this.netid = netid;
    }
    
    public Date getLastTransactionDate() {
        return lastTransactionDate;
    }
    
    public void setLastTransactionDate(Date lastTransactionDate) {
        this.lastTransactionDate = lastTransactionDate;
    }
    
    public String getDefaultAccountNumber() {
        return defaultAccountNumber;
    }
    
    public void setDefaultAccountNumber(String defaultAccountNumber) {
        this.defaultAccountNumber = defaultAccountNumber;
    }
    
    public KualiDecimal getCreditLine() {
        return creditLine;
    }
    
    public void setCreditLine(KualiDecimal creditLine) {
        this.creditLine = creditLine;
    }
    
    public Date getOpenDate() {
        return openDate;
    }
    
    public void setOpenDate(Date openDate) {
        this.openDate = openDate;
    }
    
    public Date getCycleStartDate() {
        return cycleStartDate;
    }
    
    public void setCycleStartDate(Date cycleStartDate) {
        this.cycleStartDate = cycleStartDate;
    }
    
    public KualiDecimal getCycleSpendToDate() {
        return cycleSpendToDate;
    }
    
    public void setCycleSpendToDate(KualiDecimal cycleSpendToDate) {
        this.cycleSpendToDate = cycleSpendToDate;
    }
    
    public String getActivationStatus() {
        return activationStatus;
    }
    
    public void setActivationStatus(String activationStatus) {
        this.activationStatus = activationStatus;
    }
    
    public Date getFileCreateDate() {
        return fileCreateDate;
    }
    
    public void setFileCreateDate(Date fileCreateDate) {
        this.fileCreateDate = fileCreateDate;
    }
    
    public Date getLoadDate() {
        return loadDate;
    }
    
    public void setLoadDate(Date loadDate) {
        this.loadDate = loadDate;
    }

}
