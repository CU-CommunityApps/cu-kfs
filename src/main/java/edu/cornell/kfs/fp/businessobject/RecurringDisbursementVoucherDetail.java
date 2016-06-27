package edu.cornell.kfs.fp.businessobject;

import java.sql.Date;

import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

public class RecurringDisbursementVoucherDetail extends PersistableBusinessObjectBase {
	
	private String recurringDVDocumentNumber;
	private String dvDocumentNumber;
	private Date dvCheckDate;
	private KualiDecimal dvCheckAmount;
	private String dvCheckStub;
	
	public RecurringDisbursementVoucherDetail() {
		super();
	}
	
	public RecurringDisbursementVoucherDetail(Date dvCheckDate, KualiDecimal dvCheckAmount, String dvCheckStub) {
		this();
		this.dvCheckDate = dvCheckDate;
		this.dvCheckAmount = dvCheckAmount;
		this.dvCheckStub = dvCheckStub;
	}

	public String getRecurringDVDocumentNumber() {
		return recurringDVDocumentNumber;
	}

	public void setRecurringDVDocumentNumber(String recurringDVDocumentNumber) {
		this.recurringDVDocumentNumber = recurringDVDocumentNumber;
	}

	public String getDvDocumentNumber() {
		return dvDocumentNumber;
	}

	public void setDvDocumentNumber(String dvDocumentNumber) {
		this.dvDocumentNumber = dvDocumentNumber;
	}

	public Date getDvCheckDate() {
		return dvCheckDate;
	}

	public void setDvCheckDate(Date dvCheckDate) {
		this.dvCheckDate = dvCheckDate;
	}

	public KualiDecimal getDvCheckAmount() {
		return dvCheckAmount;
	}

	public void setDvCheckAmount(KualiDecimal dvCheckAmount) {
		this.dvCheckAmount = dvCheckAmount;
	}

	public String getDvCheckStub() {
		return dvCheckStub;
	}

	public void setDvCheckStub(String dvCheckStub) {
		this.dvCheckStub = dvCheckStub;
	}

}
