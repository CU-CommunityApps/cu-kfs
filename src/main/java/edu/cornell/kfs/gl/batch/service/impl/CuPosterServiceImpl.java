package edu.cornell.kfs.gl.batch.service.impl;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryRateDetail;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.batch.PosterIndirectCostRecoveryEntriesStep;
import org.kuali.kfs.gl.batch.service.AccountingCycleCachingService;
import org.kuali.kfs.gl.batch.service.PostTransaction;
import org.kuali.kfs.gl.batch.service.PosterService;
import org.kuali.kfs.gl.batch.service.VerifyTransaction;
import org.kuali.kfs.gl.batch.service.impl.IndirectCostRecoveryGenerationMetadata;
import org.kuali.kfs.gl.batch.service.impl.PosterServiceImpl;
import org.kuali.kfs.gl.businessobject.ExpenditureTransaction;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.gl.businessobject.OriginEntryInformation;
import org.kuali.kfs.gl.businessobject.Reversal;
import org.kuali.kfs.gl.businessobject.Transaction;
import org.kuali.kfs.gl.dataaccess.ReversalDao;
import org.kuali.kfs.gl.report.LedgerSummaryReport;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.Message;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.exception.InvalidFlexibleOffsetException;
import org.kuali.kfs.sys.service.ReportWriterService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.PersistenceService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuPosterServiceImpl extends PosterServiceImpl implements PosterService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuPosterServiceImpl.class);
    /**
     * Calculates the percentage and rounds HALF_UP
     * 
     * @see org.kuali.kfs.gl.batch.service.impl.PosterServiceImpl#getPercentage(org.kuali.rice.core.api.util.type.KualiDecimal, java.math.BigDecimal)
     */
    @Override
    protected KualiDecimal getPercentage(KualiDecimal amount, BigDecimal percent) {
        BigDecimal result = amount.bigDecimalValue().multiply(percent).divide(BDONEHUNDRED, 2, BigDecimal.ROUND_HALF_UP);
        return new KualiDecimal(result);
    }

    /**
     * CUMod to prefix the "Indirect Cost Recovery System Generated" edoc number with "ICR"
     * 
     * Generate a transfer transaction and an offset transaction
     *
     * @param et                         an expenditure transaction
     * @param generatedTransactionAmount the amount of the transaction
     * @param runDate                    the transaction date for the newly created origin entry
     * @param group                      the group to save the origin entry to
     */
    protected final static String ICR_EDOC_PREFIX = "ICR";
    @Override
    protected void generateTransactions(ExpenditureTransaction et, IndirectCostRecoveryRateDetail icrRateDetail, KualiDecimal generatedTransactionAmount, Date runDate, PrintStream group, IndirectCostRecoveryGenerationMetadata icrGenerationMetadata) {

        BigDecimal pct = new BigDecimal(icrRateDetail.getAwardIndrCostRcvyRatePct().toString());
        pct = pct.divide(BDONEHUNDRED);

        OriginEntryFull e = new OriginEntryFull();
        e.setTransactionLedgerEntrySequenceNumber(0);

        // SYMBOL_USE_EXPENDITURE_ENTRY means we use the field from the expenditure entry, SYMBOL_USE_IRC_FROM_ACCOUNT
        // means we use the ICR field from the account record, otherwise, use the field in the icrRateDetail
        if (GeneralLedgerConstants.PosterService.SYMBOL_USE_EXPENDITURE_ENTRY.equals(icrRateDetail.getFinancialObjectCode()) || GeneralLedgerConstants.PosterService.SYMBOL_USE_ICR_FROM_ACCOUNT.equals(icrRateDetail.getFinancialObjectCode())) {
            e.setFinancialObjectCode(et.getObjectCode());
            e.setFinancialSubObjectCode(et.getSubObjectCode());
        } else {
            e.setFinancialObjectCode(icrRateDetail.getFinancialObjectCode());
            e.setFinancialSubObjectCode(icrRateDetail.getFinancialSubObjectCode());
        }

        if (GeneralLedgerConstants.PosterService.SYMBOL_USE_EXPENDITURE_ENTRY.equals(icrRateDetail.getAccountNumber())) {
            e.setAccountNumber(et.getAccountNumber());
            e.setChartOfAccountsCode(et.getChartOfAccountsCode());
            e.setSubAccountNumber(et.getSubAccountNumber());
        } else if (GeneralLedgerConstants.PosterService.SYMBOL_USE_ICR_FROM_ACCOUNT.equals(icrRateDetail.getAccountNumber())) {
            e.setAccountNumber(icrGenerationMetadata.getIndirectCostRecoveryAcctNbr());
            e.setChartOfAccountsCode(icrGenerationMetadata.getIndirectCostRcvyFinCoaCode());
            e.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
        } else {
            e.setAccountNumber(icrRateDetail.getAccountNumber());
            e.setSubAccountNumber(icrRateDetail.getSubAccountNumber());
            e.setChartOfAccountsCode(icrRateDetail.getChartOfAccountsCode());
            // TODO Reporting thing line 1946
        }
        // take care of infinite recursive error case - do not generate entries
        if ((et.getAccountNumber().equals(e.getAccountNumber())) &&
            (et.getChartOfAccountsCode().equals(e.getChartOfAccountsCode())) &&
            (et.getSubAccountNumber().equals(e.getSubAccountNumber())) &&
            (et.getObjectCode().equals(e.getFinancialObjectCode())) &&
            (et.getSubObjectCode().equals(e.getFinancialSubObjectCode()))) {
            List<Message> warnings = new ArrayList<Message>();
            warnings.add(new Message("Infinite recursive encumbrance error " + et.getChartOfAccountsCode() + " " + et.getAccountNumber() + " " + et.getSubAccountNumber() + " " + et.getObjectCode() + " " + et.getSubObjectCode(), Message.TYPE_WARNING));
            reportWriterService.writeError(et, warnings);
            return;
        }

        e.setFinancialDocumentTypeCode(parameterService.getParameterValueAsString(PosterIndirectCostRecoveryEntriesStep.class, KFSConstants.SystemGroupParameterNames.GL_INDIRECT_COST_RECOVERY));
        e.setFinancialSystemOriginationCode(parameterService.getParameterValueAsString(KfsParameterConstants.GENERAL_LEDGER_BATCH.class, KFSConstants.SystemGroupParameterNames.GL_ORIGINATION_CODE));
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_STRING);
        /*CUMod - start*/
        StringBuffer docNbr = new StringBuffer(ICR_EDOC_PREFIX);
        docNbr.append(sdf.format(runDate));
        e.setDocumentNumber(docNbr.toString());
        LOG.debug("CuPosterServiceImpl.generateTransactions: setDocumentNumber=" + docNbr.toString() + "=");
        //e.setDocumentNumber(sdf.format(runDate));
        /*CUMod - stop*/
        if (KFSConstants.GL_DEBIT_CODE.equals(icrRateDetail.getTransactionDebitIndicator())) {
            e.setTransactionLedgerEntryDescription(getChargeDescription(pct, et.getObjectCode(), icrGenerationMetadata.getIndirectCostRecoveryTypeCode(), et.getAccountObjectDirectCostAmount().abs()));
        } else {
            e.setTransactionLedgerEntryDescription(getOffsetDescription(pct, et.getAccountObjectDirectCostAmount().abs(), et.getChartOfAccountsCode(), et.getAccountNumber()));
        }
        e.setTransactionDate(new java.sql.Date(runDate.getTime()));
        e.setTransactionDebitCreditCode(icrRateDetail.getTransactionDebitIndicator());
        e.setFinancialBalanceTypeCode(et.getBalanceTypeCode());
        e.setUniversityFiscalYear(et.getUniversityFiscalYear());
        e.setUniversityFiscalPeriodCode(et.getUniversityFiscalAccountingPeriod());

        ObjectCode oc = objectCodeService.getByPrimaryId(e.getUniversityFiscalYear(), e.getChartOfAccountsCode(), e.getFinancialObjectCode());
        if (oc == null) {
            LOG.warn(configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_OBJECT_CODE_NOT_FOUND_FOR) + e.getUniversityFiscalYear() + "," + e.getChartOfAccountsCode() + "," + e.getFinancialObjectCode());
            e.setFinancialObjectCode(icrRateDetail.getFinancialObjectCode()); // this will be written out the ICR file. Then, when that file attempts to post, the transaction won't validate and will end up in the icr error file
        } else {
            e.setFinancialObjectTypeCode(oc.getFinancialObjectTypeCode());
        }

        if (generatedTransactionAmount.isNegative()) {
            if (KFSConstants.GL_DEBIT_CODE.equals(icrRateDetail.getTransactionDebitIndicator())) {
                e.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            } else {
                e.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            }
            e.setTransactionLedgerEntryAmount(generatedTransactionAmount.negated());
        } else {
            e.setTransactionLedgerEntryAmount(generatedTransactionAmount);
        }

        if (et.getBalanceTypeCode().equals(et.getOption().getExtrnlEncumFinBalanceTypCd()) || et.getBalanceTypeCode().equals(et.getOption().getIntrnlEncumFinBalanceTypCd()) || et.getBalanceTypeCode().equals(et.getOption().getPreencumbranceFinBalTypeCd()) || et.getBalanceTypeCode().equals(et.getOption().getCostShareEncumbranceBalanceTypeCd())) {
            e.setDocumentNumber(parameterService.getParameterValueAsString(PosterIndirectCostRecoveryEntriesStep.class, KFSConstants.SystemGroupParameterNames.GL_INDIRECT_COST_RECOVERY));
        }
        e.setProjectCode(et.getProjectCode());
        if (GeneralLedgerConstants.getDashOrganizationReferenceId().equals(et.getOrganizationReferenceId())) {
            e.setOrganizationReferenceId(null);
        } else {
            e.setOrganizationReferenceId(et.getOrganizationReferenceId());
        }
        // TODO 2031-2039
        try {
            createOutputEntry(e, group);
        } catch (IOException ioe) {
            LOG.error("generateTransactions Stopped: " + ioe.getMessage());
            throw new RuntimeException("generateTransactions Stopped: " + ioe.getMessage(), ioe);
        }

        // Now generate Offset
        e = new OriginEntryFull(e);
        if (KFSConstants.GL_DEBIT_CODE.equals(e.getTransactionDebitCreditCode())) {
            e.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
        } else {
            e.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
        }
        e.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());

        String offsetBalanceSheetObjectCodeNumber = determineIcrOffsetBalanceSheetObjectCodeNumber(e, et, icrRateDetail);
        e.setFinancialObjectCode(offsetBalanceSheetObjectCodeNumber);
        ObjectCode balSheetObjectCode = objectCodeService.getByPrimaryId(icrRateDetail.getUniversityFiscalYear(), e.getChartOfAccountsCode(), offsetBalanceSheetObjectCodeNumber);
        if (balSheetObjectCode == null) {
            List<Message> warnings = new ArrayList<Message>();
            warnings.add(new Message(configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_INVALID_OFFSET_OBJECT_CODE) + icrRateDetail.getUniversityFiscalYear() + "-" + e.getChartOfAccountsCode() + "-" + offsetBalanceSheetObjectCodeNumber, Message.TYPE_WARNING));
            reportWriterService.writeError(et, warnings);

        } else {
            e.setFinancialObjectTypeCode(balSheetObjectCode.getFinancialObjectTypeCode());
        }

        if (KFSConstants.GL_DEBIT_CODE.equals(icrRateDetail.getTransactionDebitIndicator())) {
            e.setTransactionLedgerEntryDescription(getChargeDescription(pct, et.getObjectCode(), icrGenerationMetadata.getIndirectCostRecoveryTypeCode(), et.getAccountObjectDirectCostAmount().abs()));
        } else {
            e.setTransactionLedgerEntryDescription(getOffsetDescription(pct, et.getAccountObjectDirectCostAmount().abs(), et.getChartOfAccountsCode(), et.getAccountNumber()));
        }

        try {
            flexibleOffsetAccountService.updateOffset(e);
        } catch (InvalidFlexibleOffsetException ex) {
            List<Message> warnings = new ArrayList<Message>();
            warnings.add(new Message("FAILED TO GENERATE FLEXIBLE OFFSETS " + ex.getMessage(), Message.TYPE_WARNING));
            reportWriterService.writeError(et, warnings);
            LOG.warn("FAILED TO GENERATE FLEXIBLE OFFSETS FOR EXPENDITURE TRANSACTION " + et.toString(), ex);
        }

        try {
            createOutputEntry(e, group);
        } catch (IOException ioe) {
            LOG.error("generateTransactions Stopped: " + ioe.getMessage());
            throw new RuntimeException("generateTransactions Stopped: " + ioe.getMessage(), ioe);
        }
    }
    
}
