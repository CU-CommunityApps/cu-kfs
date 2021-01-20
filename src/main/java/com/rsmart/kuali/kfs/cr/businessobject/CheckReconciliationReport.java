/*
 * Copyright 2008 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.cr.businessobject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.kuali.kfs.sys.KFSConstants;

import com.rsmart.kuali.kfs.cr.CRConstants;

/**
 * Check Reconciliation Report
 * 
 * @author Derek Helbert
 * @version $Revision$
 */
public class CheckReconciliationReport implements Comparable<CheckReconciliationReport> {

	private String payeeId;
    private String payeeName;
    private String payeeType;
    private String checkNumber;
    private String bankAccountNumber;
    private String checkDate;
    private String checkMonth;
    private String status;
    private String amount;
    private Double subTotal;
    public static SimpleDateFormat MONYYF = new SimpleDateFormat("yyyy/MM", Locale.US);
    public static SimpleDateFormat SDF = new SimpleDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT, Locale.US);
    public static DecimalFormat DF = new DecimalFormat("#0.00", new DecimalFormatSymbols(Locale.US));
    
    public CheckReconciliationReport() {
    }
    
    public CheckReconciliationReport(CheckReconciliation cr) {
    	this.setPayeeId(cr.getPayeeId());
    	this.setPayeeName(cr.getPayeeName());
    	this.setPayeeType(cr.getPayeeType());
        this.setSubTotal(cr.getAmount().doubleValue());
        this.setBankAccountNumber(cr.getBankAccountNumber());
        this.setCheckDate(SDF.format(cr.getCheckDate()));
        this.setCheckMonth(MONYYF.format(cr.getCheckDate()));
        this.setCheckNumber(cr.getCheckNumber().toString());
        this.setStatus(cr.getStatus());
        this.setSubTotal(cr.getAmount().doubleValue());
    }
    
    public String getPayeeId() {
		return payeeId;
	}

	public void setPayeeId(String payeeId) {
		this.payeeId = payeeId;
	}

	public String getPayeeName() {
		return payeeName;
	}

	public void setPayeeName(String payeeName) {
		this.payeeName = payeeName;
	}

	public String getPayeeType() {
		return payeeType;
	}

	public void setPayeeType(String payeeType) {
		this.payeeType = payeeType;
	}
    
    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(String checkDate) {
        this.checkDate = checkDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCheckMonth() {
        return checkMonth;
    }

    public void setCheckMonth(String checkMonth) {
        this.checkMonth = checkMonth;
    }

    public Double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(Double subTotal) {
        this.subTotal = subTotal;
        setAmount(DF.format(subTotal));
    }

    public String getStatusDesc() {
        return CRConstants.ISSUED.equals(status) ? "Issued" : "";
    }
    
    public Integer getCleared() {
        return CRConstants.CLEARED.equals(status) ? new Integer(1) : new Integer(0);
    }
    
    public Integer getIssued() {
        return CRConstants.ISSUED.equals(status) ? new Integer(1) : new Integer(0);
    }
    
    public Integer getCancelled() {
        return CRConstants.CANCELLED.equals(status) ? new Integer(1) : new Integer(0);
    }
    
    public Integer getVoided() {
        return CRConstants.VOIDED.equals(status) ? new Integer(1) : new Integer(0);
    }
    
    public Integer getStale() {
        return CRConstants.STALE.equals(status) ? new Integer(1) : new Integer(0);
    }
    
    public int compareTo(CheckReconciliationReport report) {
		return getCheckNumber().compareTo(report.getCheckNumber());
	}
}
