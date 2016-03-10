package edu.iu.ebs.kfs.fp.businessobject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.kuali.rice.krad.bo.TransientBusinessObjectBase;
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
public class AchIncomeFileTransaction extends TransientBusinessObjectBase {
    
	private KualiDecimal transactionAmount;
	private String creditDebitIndicator;
	private Date effectiveDate;
	private String companyId;
	private String paymentMethodCode;
	private AchIncomeFileTransactionTrace trace;
	private List<AchIncomeFileTransactionReference> references;
	private List<AchIncomeFileTransactionDateTime> dateTimes;
	private List<AchIncomeFileTransactionPayerOrPayeeName> payerOrPayees;
	private List<AchIncomeFileTransactionNote> notes;
	private List<AchIncomeFileTransactionOpenItemReference> openItemReferences;
	private AchIncomeFileTransactionPremiumPayersAdminsContact premiumAdminsContact;
	private AchIncomeFileTransactionPremiumReceiverName premiumReceiverName;

    public AchIncomeFileTransaction() {
        this.references = new ArrayList<AchIncomeFileTransactionReference>();
        this.dateTimes = new ArrayList<AchIncomeFileTransactionDateTime>();
        this.payerOrPayees = new ArrayList<AchIncomeFileTransactionPayerOrPayeeName>();
        this.notes = new ArrayList<AchIncomeFileTransactionNote>();
        this.openItemReferences = new ArrayList<AchIncomeFileTransactionOpenItemReference>();
    }
	
	public KualiDecimal getTransactionAmount() {
		return transactionAmount;
	}

	public void setTransactionAmount(KualiDecimal transactionAmount) {
		this.transactionAmount = transactionAmount;
	}

	public String getCreditDebitIndicator() {
		return creditDebitIndicator;
	}

	public void setCreditDebitIndicator(String creditDebitIndicator) {
		this.creditDebitIndicator = creditDebitIndicator;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public AchIncomeFileTransactionTrace getTrace() {
		return trace;
	}

	public void setTrace(AchIncomeFileTransactionTrace trace) {
		this.trace = trace;
	}

	public List<AchIncomeFileTransactionReference> getReferences() {
		return references;
	}

	public void setReferences(List<AchIncomeFileTransactionReference> references) {
		this.references = references;
	}

	public List<AchIncomeFileTransactionDateTime> getDateTimes() {
		return dateTimes;
	}

	public void setDateTimes(List<AchIncomeFileTransactionDateTime> dateTimes) {
		this.dateTimes = dateTimes;
	}

	public List<AchIncomeFileTransactionPayerOrPayeeName> getPayerOrPayees() {
		return payerOrPayees;
	}

	public void setPayerOrPayees(
			List<AchIncomeFileTransactionPayerOrPayeeName> payerOrPayees) {
		this.payerOrPayees = payerOrPayees;
	}

	public List<AchIncomeFileTransactionNote> getNotes() {
		return notes;
	}

	public void setNotes(List<AchIncomeFileTransactionNote> notes) {
		this.notes = notes;
	}

	public List<AchIncomeFileTransactionOpenItemReference> getOpenItemReferences() {
		return openItemReferences;
	}

	public void setOpenItemReferences(
			List<AchIncomeFileTransactionOpenItemReference> openItemReferences) {
		this.openItemReferences = openItemReferences;
	}
	
	
	
    public AchIncomeFileTransactionPremiumPayersAdminsContact getPremiumAdminsContact() {
        return premiumAdminsContact;
    }

    public void setPremiumAdminsContact(AchIncomeFileTransactionPremiumPayersAdminsContact premiumAdminsContact) {
        this.premiumAdminsContact = premiumAdminsContact;
    }

    public AchIncomeFileTransactionPremiumReceiverName getPremiumReceiverName() {
        return premiumReceiverName;
    }

    public void setPremiumReceiverName(AchIncomeFileTransactionPremiumReceiverName premiumReceiverName) {
        this.premiumReceiverName = premiumReceiverName;
    }

    /**
     * 
     * @return payment method code ACH/FWT 
     * 
     * 
     */
	public String getPaymentMethodCode() {
        return paymentMethodCode;
    }

    public void setPaymentMethodCode(String paymentMethodCode) {
        this.paymentMethodCode = paymentMethodCode;
    }

    
	protected LinkedHashMap toStringMapper_RICE20_REFACTORME() {
		// TODO Auto-generated method stub
		return null;
	}
}
