package edu.cornell.kfs.fp.businessobject;

import java.sql.Date;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class ProcurementCardSummaryEntry extends PersistableBusinessObjectBase {
    protected String cardHolderAccountNumber;
    protected String cardHolderName;
    protected String emplid;
    protected String netid;
    protected String accountStatus;
    protected Date cycleStartDate;
    protected KualiDecimal summaryAmount;
    protected Date loadDate;

	protected LinkedHashMap toStringMapper() {
		return null;
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

	public Date getCycleStartDate() {
		return cycleStartDate;
	}

	public void setCycleStartDate(Date cycleStartDate) {
		this.cycleStartDate = cycleStartDate;
	}

	public KualiDecimal getSummaryAmount() {
		return summaryAmount;
	}

	public void setSummaryAmount(KualiDecimal summaryAmount) {
		this.summaryAmount = summaryAmount;
	}

	public Date getLoadDate() {
		return loadDate;
	}

	public void setLoadDate(Date loadDate) {
		this.loadDate = loadDate;
	}

	@Override
	public String toString() {
	    ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
	    return builder.build();
	}
}
