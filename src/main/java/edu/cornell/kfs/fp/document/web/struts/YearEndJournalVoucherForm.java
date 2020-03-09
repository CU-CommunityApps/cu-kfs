package edu.cornell.kfs.fp.document.web.struts;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.businessobject.BalanceType;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.coa.service.BalanceTypeService;
import org.kuali.kfs.fp.document.web.struts.JournalVoucherForm;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.GLParameterConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

public class YearEndJournalVoucherForm extends JournalVoucherForm {
	private static final Logger LOG = LogManager.getLogger(YearEndJournalVoucherForm.class);

	/**
	 * Constructs a YearEndJournalVoucherForm instance and sets up the
	 * appropriately casted document.
	 */
	public YearEndJournalVoucherForm() {
		super();
	}

	@Override
	protected String getDefaultDocumentTypeName() {
		return "YEJV";
	}
	
	@Override
	protected void populateBalanceTypeListForRendering() {
		balanceTypes = new ArrayList<BalanceType>();
        BalanceType balanceTypeAC = SpringContext.getBean(BalanceTypeService.class).getBalanceTypeByCode(KFSConstants.BALANCE_TYPE_ACTUAL);
        balanceTypes.add(balanceTypeAC);

        this.setBalanceTypes(balanceTypes);

        String selectedBalanceTypeCode = KFSConstants.BALANCE_TYPE_ACTUAL;

        setSelectedBalanceType(getPopulatedBalanceTypeInstance(selectedBalanceTypeCode));
        getJournalVoucherDocument().setBalanceTypeCode(selectedBalanceTypeCode);
	}
	
	@Override
	public void populateDefaultSelectedAccountingPeriod() {
		Integer fiscalYear = new Integer(SpringContext.getBean(ParameterService.class).getParameterValueAsString(KfsParameterConstants.GENERAL_LEDGER_BATCH.class, GLParameterConstants.ANNUAL_CLOSING_FISCAL_YEAR));
        AccountingPeriod accountingPeriod = SpringContext.getBean(AccountingPeriodService.class).getByPeriod("13", fiscalYear);

        StringBuffer sb = new StringBuffer();
        sb.append(accountingPeriod.getUniversityFiscalPeriodCode());
        sb.append(accountingPeriod.getUniversityFiscalYear());

        setSelectedAccountingPeriod(sb.toString());
	}

}
