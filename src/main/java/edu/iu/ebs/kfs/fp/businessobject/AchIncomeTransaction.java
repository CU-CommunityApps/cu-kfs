package edu.iu.ebs.kfs.fp.businessobject;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.core.api.util.type.KualiDecimal;
/**
Copyright Indiana University
   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as
   published by the Free Software Foundation, either version 3 of the
   License, or (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.
   
   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
 
public class AchIncomeTransaction extends PersistableBusinessObjectBase {
	private Integer sequenceNumber;
    private Date effectiveDate;
	private Timestamp loadTimestamp;
	private Timestamp bankTimestamp;
	private String payerName;
	private KualiDecimal transactionAmount;
	private String referenceNumber;
	private String traceNumber;
	private String paymentMethodCode;
	private List<AchIncomeNote> notes;

	
	protected LinkedHashMap toStringMapper_RICE20_REFACTORME() {
		LinkedHashMap toString = new LinkedHashMap();
		toString.put("sequenceNumber", sequenceNumber);
		return toString;
	}

	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Timestamp getLoadTimestamp() {
		return loadTimestamp;
	}

	public void setLoadTimestamp(Timestamp loadTimestamp) {
		this.loadTimestamp = loadTimestamp;
	}

	public Timestamp getBankTimestamp() {
		return bankTimestamp;
	}

	public void setBankTimestamp(Timestamp bankTimestamp) {
		this.bankTimestamp = bankTimestamp;
	}

	public String getPayerName() {
		return payerName;
	}

	public void setPayerName(String payerName) {
		this.payerName = payerName;
	}

	public KualiDecimal getTransactionAmount() {
		return transactionAmount;
	}

	public void setTransactionAmount(KualiDecimal transactionAmount) {
		this.transactionAmount = transactionAmount;
	}

	public String getReferenceNumber() {
		return referenceNumber;
	}

	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}

	public String getTraceNumber() {
		return traceNumber;
	}

	public void setTraceNumber(String traceNumber) {
		this.traceNumber = traceNumber;
	}

	public List<AchIncomeNote> getNotes() {
		return notes;
	}
	
	
	public String getPaymentMethodCode() {
        return paymentMethodCode;
    }

    public void setPaymentMethodCode(String paymentMethodCode) {
        this.paymentMethodCode = paymentMethodCode;
    }

    public void setNotes(List<AchIncomeNote> notes) {
		this.notes = notes;
	}
    
}
