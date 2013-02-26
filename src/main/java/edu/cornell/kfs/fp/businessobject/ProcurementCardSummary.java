package edu.cornell.kfs.fp.businessobject;

import java.sql.Date;
import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;
import org.kuali.rice.kns.util.KualiDecimal;

import edu.cornell.kfs.module.bc.CUBCConstants;
import edu.cornell.kfs.module.bc.businessobject.PSPositionInfo;

public class ProcurementCardSummary extends PersistableBusinessObjectBase{
    protected String cardHolderAccountNumber;
    protected String cardHolderName;
    protected String emplid;
    protected String netid;
    protected String accountStatus;
    protected String cycleStartDate;
    protected String summaryAmount;
    protected String loadDate;

 
	@Override
	protected LinkedHashMap toStringMapper() {
		// TODO Auto-generated method stub
		return null;
	}

	   /**
     * Gets the key for the current entry which is cardHolderAccountNumber + emplid;
     * 
     * @return
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

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getEmplid() {
        return emplid;
    }

    public void setEmplid(String emplid) {
        this.emplid = emplid;
    }

    public String getNetid() {
        return netid;
    }

    public void setNetid(String netid) {
        this.netid = netid;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getCycleStartDate() {
        return cycleStartDate;
    }

    public void setCycleStartDate(String cycleStartDate) {
        this.cycleStartDate = cycleStartDate;
    }

    public String getSummaryAmount() {
        return summaryAmount;
    }

    public void setSummaryAmount(String summaryAmount) {
    	this.summaryAmount = summaryAmount;
    }
 
    public String getLoadDate() {
        return loadDate;
    }

    public void setLoadDate(String loadDate) {
        this.loadDate = loadDate;
    }
  
  
}
