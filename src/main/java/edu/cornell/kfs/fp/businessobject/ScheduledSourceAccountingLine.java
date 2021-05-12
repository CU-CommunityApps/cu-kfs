package edu.cornell.kfs.fp.businessobject;

import java.sql.Date;
import java.util.Map;

import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes;
import edu.cornell.kfs.sys.CUKFSConstants;

public class ScheduledSourceAccountingLine extends SourceAccountingLine implements ScheduledAccountingLine {

	private static final long serialVersionUID = 2423537350374532488L;
	private String partialTransactionCount;
	private KualiDecimal partialAmount;
	private Date endDate;
	private Date startDate;
	private String scheduleType;

	public String getPartialTransactionCount() {
		return partialTransactionCount;
	}

	public void setPartialTransactionCount(String partialTransactionCount) {
		this.partialTransactionCount = partialTransactionCount;
	}

	public KualiDecimal getPartialAmount() {
		return partialAmount;
	}

	public void setPartialAmount(KualiDecimal partialAmount) {
		this.partialAmount = partialAmount;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(String scheduleType) {
		this.scheduleType = scheduleType;
	}

	@Override
	public Map getValuesMap() {
		Map simpleValues = super.getValuesMap();
		simpleValues.put(CuFPConstants.ScheduledSourceAccountingLineConstants.PARTIAL_TRANSACTION_COUNT, partialTransactionCount);
		simpleValues.put(CuFPConstants.ScheduledSourceAccountingLineConstants.PARTIAL_AMOUNT, partialAmount);
		simpleValues.put(CuFPConstants.ScheduledSourceAccountingLineConstants.START_DATE, startDate);
		simpleValues.put(CuFPConstants.ScheduledSourceAccountingLineConstants.END_DATE, endDate);
		simpleValues.put(CuFPConstants.ScheduledSourceAccountingLineConstants.SCHEDULE_TYPE, scheduleType);
		return simpleValues;
	}

	@Override
	public boolean isSourceAccountingLine() {
		return true;
	}

	@Override
	public boolean isTargetAccountingLine() {
		return false;
	}
	
	@Override
	public void copyFrom(AccountingLine other) {
		super.copyFrom(other);
		if (ScheduledSourceAccountingLine.class.isAssignableFrom(other.getClass())) {
			ScheduledSourceAccountingLine fromLine = (ScheduledSourceAccountingLine) other;
			if (this != fromLine) {
				setPartialAmount(fromLine.getPartialAmount());
				setPartialTransactionCount(fromLine.getPartialTransactionCount());
				setStartDate(fromLine.getStartDate());
				setEndDate(fromLine.getEndDate());
				setScheduleType(fromLine.getScheduleType());
				setAmount(fromLine.getAmount());
			}
		}
	}
	
	public ScheduleTypes getScheduleTypeEnum() {
		return ScheduleTypes.fromName(this.getScheduleType());
	}
}
