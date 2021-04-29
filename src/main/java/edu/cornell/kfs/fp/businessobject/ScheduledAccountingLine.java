package edu.cornell.kfs.fp.businessobject;

import java.sql.Date;

import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

public interface ScheduledAccountingLine extends AccountingLine {
	public abstract String getPartialTransactionCount();
    
    void setPartialTransactionCount(String partialTransactionCount);
    
    KualiDecimal getPartialAmount();
    
    void setPartialAmount(KualiDecimal partialAmount);
    
    String getScheduleType();
    
    void setScheduleType(String scheduleType);
    
    Date getEndDate();
    
    void setEndDate(Date endDate);
    
    Date getStartDate();
    
    void setStartDate(Date startDate);

}
