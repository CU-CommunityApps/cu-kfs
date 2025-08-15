package edu.cornell.kfs.fp.businessobject;

import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class TravelMealCardFileLineEntry extends PersistableBusinessObjectBase {

    protected String cardHolderName;
    protected String cardStatus;
    protected String cardType;
    protected String netid;
    protected String cardHolderLine1Address;
    protected String cardHolderLine2Address;
    protected String cardHolderCityName;
    protected String cardHolderStateCode;
    protected String cardHolderPostalCode;
    protected String cardHolderWorkPhoneNumber;
    protected String cardHolderPersonalPhoneNumber; 
    protected String lastTransactionDate;         //Date
    protected String defaultAccountNumber;
    protected String creditLine;                  //KualiDecimal
    protected String openDate;                    //Date
    protected String cardHolderAccountNumber;
    protected String emplid;
    protected String cycleStartDate;              //Date
    protected String cycleSpendToDate;            //KualiDecimal
    protected String expireDate;                  //String, yyyymm, there is no day in this value
    protected String activationStatus;
    protected String fileCreateDate;              //Date
    protected String loadDate;                    //Date, not in file, actual date batch job attempted to load the file

    protected LinkedHashMap toStringMapper() {
        return null;
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
    
    public String getCardHolderLine1Address() {
        return cardHolderLine1Address;
    }
    
    public void setCardHolderLine1Address(String cardHolderLine1Address) {
        this.cardHolderLine1Address = cardHolderLine1Address;
    }
    
    public String getCardHolderLine2Address() {
        return cardHolderLine2Address;
    }
    
    public void setCardHolderLine2Address(String cardHolderLine2Address) {
        this.cardHolderLine2Address = cardHolderLine2Address;
    }
    
    public String getCardHolderCityName() {
        return cardHolderCityName;
    }
    
    public void setCardHolderCityName(String cardHolderCityName) {
        this.cardHolderCityName = cardHolderCityName;
    }
    
    public String getCardHolderStateCode() {
        return cardHolderStateCode;
    }
    
    public void setCardHolderStateCode(String cardHolderStateCode) {
        this.cardHolderStateCode = cardHolderStateCode;
    }
    
    public String getCardHolderPostalCode() {
        return cardHolderPostalCode;
    }
    
    public void setCardHolderPostalCode(String cardHolderPostalCode) {
        this.cardHolderPostalCode = cardHolderPostalCode;
    }
    
    public String getCardHolderWorkPhoneNumber() {
        return cardHolderWorkPhoneNumber;
    }
    
    public void setCardHolderWorkPhoneNumber(String cardHolderWorkPhoneNumber) {
        this.cardHolderWorkPhoneNumber = cardHolderWorkPhoneNumber;
    }
    
    public String getCardHolderPersonalPhoneNumber() {
        return cardHolderPersonalPhoneNumber;
    }
    
    public void setCardHolderPersonalPhoneNumber(String cardHolderPersonalPhoneNumber) {
        this.cardHolderPersonalPhoneNumber = cardHolderPersonalPhoneNumber;
    }
    
    public String getLastTransactionDate() {
        return lastTransactionDate;
    }
    
    public void setLastTransactionDate(String lastTransactionDate) {
        this.lastTransactionDate = lastTransactionDate;
    }
    
    public String getDefaultAccountNumber() {
        return defaultAccountNumber;
    }
    
    public void setDefaultAccountNumber(String defaultAccountNumber) {
        this.defaultAccountNumber = defaultAccountNumber;
    }
    
    public String getCreditLine() {
        return creditLine;
    }
    
    public void setCreditLine(String creditLine) {
        this.creditLine = creditLine;
    }
    
    public String getOpenDate() {
        return openDate;
    }
    
    public void setOpenDate(String openDate) {
        this.openDate = openDate;
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
    
    public String getCycleStartDate() {
        return cycleStartDate;
    }
    
    public void setCycleStartDate(String cycleStartDate) {
        this.cycleStartDate = cycleStartDate;
    }
    
    public String getCycleSpendToDate() {
        return cycleSpendToDate;
    }
    
    public void setCycleSpendToDate(String cycleSpendToDate) {
        this.cycleSpendToDate = cycleSpendToDate;
    }
    
    public String getExpireDate() {
        return expireDate;
    }
    
    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }
    
    public String getActivationStatus() {
        return activationStatus;
    }
    
    public void setActivationStatus(String activationStatus) {
        this.activationStatus = activationStatus;
    }
    
    public String getFileCreateDate() {
        return fileCreateDate;
    }
    
    public void setFileCreateDate(String fileCreateDate) {
        this.fileCreateDate = fileCreateDate;
    }
    
    public String getLoadDate() {
        return loadDate;
    }
    
    public void setLoadDate(String loadDate) {
        this.loadDate = loadDate;
    }
    
    @Override
    public String toString() {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        return builder.build();
    }
    
}
