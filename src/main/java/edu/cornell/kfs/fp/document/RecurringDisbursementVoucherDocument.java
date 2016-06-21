package edu.cornell.kfs.fp.document;

import java.sql.Date;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.GeneralLedgerPendingEntryService;
import org.kuali.kfs.sys.service.HomeOriginationService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.COMPONENT;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.NAMESPACE;
import org.kuali.rice.krad.exception.ValidationException;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.businessobject.ScheduledAccountingLine;
import edu.cornell.kfs.fp.businessobject.ScheduledSourceAccountingLine;
import edu.cornell.kfs.fp.service.impl.ScheduledAccountingLineServiceImpl;
import edu.cornell.kfs.gl.service.ScheduledAccountingLineService;

@NAMESPACE(namespace = KFSConstants.CoreModuleNamespaces.FINANCIAL)
@COMPONENT(component = "RecurringDisbursementVoucher")
public class RecurringDisbursementVoucherDocument extends CuDisbursementVoucherDocument {

	private static final long serialVersionUID = 5578411133159987973L;
	protected static Log LOG = LogFactory.getLog(RecurringDisbursementVoucherDocument.class);
	private ScheduledAccountingLineService scheduledAccountingLineService;
	private HomeOriginationService homeOriginationService;
	private GeneralLedgerPendingEntryService generalLedgerPendingEntryService;
	
	@Override
    public void customizeExplicitGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySourceDetail postable, GeneralLedgerPendingEntry explicitEntry) {
		super.customizeExplicitGeneralLedgerPendingEntry(postable, explicitEntry);
		ScheduledAccountingLine accountingLine = (ScheduledAccountingLine) postable;
        explicitEntry.setTransactionEntryProcessedTs(null);
        if (accountingLine.isSourceAccountingLine()) {
            explicitEntry.setReferenceFinancialSystemOriginationCode(getHomeOriginationService().getHomeOrigination().getFinSystemHomeOriginationCode());
            explicitEntry.setReferenceFinancialDocumentNumber(accountingLine.getReferenceNumber());
            explicitEntry.setReferenceFinancialDocumentTypeCode(explicitEntry.getFinancialDocumentTypeCode());
        } else if (accountingLine.isTargetAccountingLine()) {
        	throw new IllegalArgumentException("we shouldn't have target lines on DVs");
        }
    }
	
	@Override
    protected void processExplicitGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySequenceHelper sequenceHelper,
        GeneralLedgerPendingEntrySourceDetail generalLedgerPendingEntryDetail, GeneralLedgerPendingEntry explicitEntry) {
		ScheduledSourceAccountingLine scheduledAccountingLine = (ScheduledSourceAccountingLine) generalLedgerPendingEntryDetail;
        if (isRecurringAccountingLine(scheduledAccountingLine)) {
            checkSufficientDetailForGLPEGeneration(scheduledAccountingLine);
            scheduledAccountingLine.setEndDate(getScheduledAccountingLineService().generateEndDate(scheduledAccountingLine));
            int rowId = ((AccountingLine) scheduledAccountingLine).getSequenceNumber() - 1;
            proccessRecurringTransactionLine(sequenceHelper, scheduledAccountingLine, rowId);
            return;
        } else {
        	processNonRecurringTransactionLine(sequenceHelper, scheduledAccountingLine, explicitEntry);
        }
    }
	
	private boolean isRecurringAccountingLine(ScheduledSourceAccountingLine scheduledAccountingLine) {
		return ObjectUtils.isNotNull(scheduledAccountingLine.getScheduleType());
	}
	
	private void checkSufficientDetailForGLPEGeneration(ScheduledSourceAccountingLine scheduledAccountingLine) {
		if (ObjectUtils.isNull(scheduledAccountingLine.getStartDate()) || ObjectUtils.isNull(scheduledAccountingLine.getPartialTransactionCount()) 
		    || ObjectUtils.isNull(scheduledAccountingLine.getPartialAmount())) {
		    throw new ValidationException("Insufficient information for GLPE generation");
		}
	}

	private void processNonRecurringTransactionLine(GeneralLedgerPendingEntrySequenceHelper sequenceHelper,
			ScheduledSourceAccountingLine scheduledAccountingLine, GeneralLedgerPendingEntry explicitEntry) {
		getGeneralLedgerPendingEntryService().populateExplicitGeneralLedgerPendingEntry(this, scheduledAccountingLine, sequenceHelper, explicitEntry);
        customizeExplicitGeneralLedgerPendingEntry(scheduledAccountingLine, explicitEntry);
        addPendingEntry(explicitEntry);
        sequenceHelper.increment();
        GeneralLedgerPendingEntry offsetEntry = new GeneralLedgerPendingEntry(explicitEntry);
        processOffsetGeneralLedgerPendingEntry(sequenceHelper, scheduledAccountingLine, explicitEntry, offsetEntry);
	}

	private void proccessRecurringTransactionLine(GeneralLedgerPendingEntrySequenceHelper sequenceHelper,
			ScheduledSourceAccountingLine scheduledAccountingLine, int rowId) {
		TreeMap<Date, KualiDecimal> datesAndAmounts = getScheduledAccountingLineService().generateDatesAndAmounts(scheduledAccountingLine, rowId);
		boolean isErrorCorrection = scheduledAccountingLine.getAmount().isNegative();
		Date today = new Date(Calendar.getInstance().getTimeInMillis());
		for (Date reveralDate : datesAndAmounts.keySet()) {
			if (!(isErrorCorrection && reveralDate.before(today))) {
				LOG.debug("Proccesing source line for document number " + 
					scheduledAccountingLine.getDocumentNumber() + " and line number " + scheduledAccountingLine.getSequenceNumber());
				KualiDecimal partialAmount = datesAndAmounts.get(reveralDate);
				KualiDecimal transactionAmount = isErrorCorrection ? partialAmount.negated() : partialAmount;
			    createGeneralLedgerPendingEntry(sequenceHelper, scheduledAccountingLine, reveralDate, transactionAmount);
			} else {
				LOG.debug("Found an error correction, not proccesing source line for document number " + 
						scheduledAccountingLine.getDocumentNumber() + " and line number " + scheduledAccountingLine.getSequenceNumber());
			}
		}
	}

	private void createGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySequenceHelper sequenceHelper,
			ScheduledSourceAccountingLine scheduledAccountingLine, Date reveralDate, KualiDecimal transactionAmount) {
		GeneralLedgerPendingEntry explicitPartialEntry = new GeneralLedgerPendingEntry();
		getGeneralLedgerPendingEntryService().populateExplicitGeneralLedgerPendingEntry(this, scheduledAccountingLine, sequenceHelper, explicitPartialEntry);
		explicitPartialEntry.setFinancialDocumentReversalDate(reveralDate);
		explicitPartialEntry.setTransactionLedgerEntryAmount(transactionAmount);
		customizeExplicitGeneralLedgerPendingEntry(scheduledAccountingLine, explicitPartialEntry);
		addPendingEntry(explicitPartialEntry);
		sequenceHelper.increment();
		GeneralLedgerPendingEntry offsetEntry = new GeneralLedgerPendingEntry(explicitPartialEntry);
		processOffsetGeneralLedgerPendingEntry(sequenceHelper, scheduledAccountingLine, explicitPartialEntry, offsetEntry);
		/**
		 * @Todo I think there needs to be some logic around this increment statement, we only want to do it if there are more recurring line items coming, 
		 * otherwise we have an extra number in the sequnece between the current set and the next set
		 */
		sequenceHelper.increment();
	}
	
	@Override
    public boolean generateGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySourceDetail glpeSourceDetail,
        GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        GeneralLedgerPendingEntry explicitEntry = new GeneralLedgerPendingEntry();
        processExplicitGeneralLedgerPendingEntry(sequenceHelper, glpeSourceDetail, explicitEntry);
        return true;
    }
	
	@Override
    protected boolean processOffsetGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySequenceHelper sequenceHelper,
        GeneralLedgerPendingEntrySourceDetail postable, GeneralLedgerPendingEntry explicitEntry, GeneralLedgerPendingEntry offsetEntry) {
        boolean success = getGeneralLedgerPendingEntryService().populateOffsetGeneralLedgerPendingEntry(getPostingYear(), explicitEntry, sequenceHelper, offsetEntry);
        success &= customizeOffsetGeneralLedgerPendingEntry(postable, explicitEntry, offsetEntry);
        addPendingEntry(offsetEntry);
        return success;
    }

	public ScheduledAccountingLineService getScheduledAccountingLineService() {
		if (scheduledAccountingLineService == null) {
			scheduledAccountingLineService = SpringContext.getBean(ScheduledAccountingLineService.class);
		}
		return scheduledAccountingLineService;
	}

	public void setScheduledAccountingLineService(ScheduledAccountingLineService scheduledAccountingLineService) {
		this.scheduledAccountingLineService = scheduledAccountingLineService;
	}

	public HomeOriginationService getHomeOriginationService() {
		if (homeOriginationService == null) {
			homeOriginationService = SpringContext.getBean(HomeOriginationService.class);
		}
		return homeOriginationService;
	}

	public void setHomeOriginationService(HomeOriginationService homeOriginationService) {
		this.homeOriginationService = homeOriginationService;
	}

	public GeneralLedgerPendingEntryService getGeneralLedgerPendingEntryService() {
		if (generalLedgerPendingEntryService == null) {
			generalLedgerPendingEntryService = SpringContext.getBean(GeneralLedgerPendingEntryService.class);
		}
		return generalLedgerPendingEntryService;
	}

	public void setGeneralLedgerPendingEntryService(GeneralLedgerPendingEntryService generalLedgerPendingEntryService) {
		this.generalLedgerPendingEntryService = generalLedgerPendingEntryService;
	}
	
}
