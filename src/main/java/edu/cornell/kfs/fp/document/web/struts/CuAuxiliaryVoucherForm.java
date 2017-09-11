package edu.cornell.kfs.fp.document.web.struts;

import java.sql.Date;
import java.util.Calendar;

import org.kuali.kfs.fp.document.web.struts.AuxiliaryVoucherForm;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.krad.util.ObjectUtils;

public class CuAuxiliaryVoucherForm extends AuxiliaryVoucherForm {
	
	/**
	 * Overrides method to fix reversal date calculation when selected accounting period is in previous year
	 * 
	 * @see org.kuali.kfs.fp.document.web.struts.AuxiliaryVoucherForm#getAvReversalDate()
	 */
	//@Override
	protected Date getAvReversalDate() {
		Date documentReveralDate = getAuxiliaryVoucherDocument().getReversalDate();
		if (ObjectUtils.isNotNull(documentReveralDate)) {
			return documentReveralDate;
		}

		java.sql.Date avReversalDate = getAuxiliaryVoucherDocument().getAccountingPeriod().getUniversityFiscalPeriodEndDate();

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(avReversalDate.getTime());

		int thisMonth;

		if (getAuxiliaryVoucherDocument().getAccountingPeriod().getUniversityFiscalPeriodCode().equals(KFSConstants.MONTH13)) {
			thisMonth = cal.JULY;
		} else
			thisMonth = getAuxiliaryVoucherDocument().getAccountingPeriod().getMonth();

		// the Calendar month of January starts at 0 while the months from
		// AccountingPeriod start with January being 1; that is why we get
		// reversal date one month ahead except from period 13 which will get
		// reversal date July
		cal.set(Calendar.MONTH, (thisMonth));

		int reversalDateDefaultDayOfMonth = 0; //this.getReversalDateDefaultDayOfMonth();

		cal.set(Calendar.DAY_OF_MONTH, reversalDateDefaultDayOfMonth);

		long timeInMillis = cal.getTimeInMillis();
		avReversalDate.setTime(timeInMillis);

		return avReversalDate;
	}

}
