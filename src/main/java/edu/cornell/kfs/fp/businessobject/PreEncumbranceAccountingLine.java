package edu.cornell.kfs.fp.businessobject;

import java.sql.Date;

import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.rice.kns.util.KualiDecimal;

public interface PreEncumbranceAccountingLine extends AccountingLine {

	public abstract String getPartialTransactionCount();
	
	public abstract void setPartialTransactionCount(String partialTransactionCount);
	
	public abstract KualiDecimal getPartialAmount();
	
	public abstract void setPartialAmount(KualiDecimal partialAmount);
	
	public abstract String getAutoDisEncumberType();
	
	public abstract void setAutoDisEncumberType(String autoDisEncumberType);
	
	public abstract Date getEndDate();
	
	public abstract void setEndDate(Date endDate);

	public abstract Date getStartDate();
	
	public abstract void setStartDate(Date startDate);
}
