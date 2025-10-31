package edu.cornell.kfs.fp.businessobject;

import java.sql.Date;
import java.util.LinkedHashMap;

import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class TravelMealCardVerificationData extends PersistableBusinessObjectBase {
    
    protected String cardHolderAccountNumber;
    protected String emplid;
    protected String cardHolderLine1Address;
    protected String cardHolderLine2Address;
    protected String cardHolderCityName;
    protected String cardHolderStateCode;
    protected String cardHolderPostalCode;
    protected String cardHolderWorkPhoneNumber;
    protected String cardHolderPersonalPhoneNumber;
    protected KualiDecimal creditLine;
    protected Date expireDate;
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
    
    public KualiDecimal getCreditLine() {
        return creditLine;
    }
    
    public void setCreditLine(KualiDecimal creditLine) {
        this.creditLine = creditLine;
    }
    
    public Date getExpireDate() {
        return expireDate;
    }
    
    public void setExpireDate(Date  expireDate) {
        this.expireDate = expireDate;
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
