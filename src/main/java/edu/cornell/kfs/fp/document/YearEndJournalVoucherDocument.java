package edu.cornell.kfs.fp.document;

import java.util.Collection;

import org.kuali.kfs.fp.document.JournalVoucherDocument;
import org.kuali.kfs.fp.document.YearEndDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.fp.document.service.YearEndGeneralLedgerPendingEntriesService;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class YearEndJournalVoucherDocument extends JournalVoucherDocument implements YearEndDocument {

	public YearEndJournalVoucherDocument() {
		super();
	}

	public boolean generateDocumentGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
		
		Collection<String> yejvClosingCharts = SpringContext.getBean(ParameterService.class).getParameterValuesAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL,"YearEndJournalVoucherDocument", CUKFSParameterKeyConstants.FpParameterConstants.YEJV_CLOSING_CHARTS);
		return SpringContext.getBean(YearEndGeneralLedgerPendingEntriesService.class).generateYearEndGeneralLedgerPendingEntries(this, "YEJV", this.getSourceAccountingLines(), sequenceHelper, yejvClosingCharts);

	}
	
}
