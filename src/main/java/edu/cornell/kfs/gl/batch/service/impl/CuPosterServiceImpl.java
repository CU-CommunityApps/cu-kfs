package edu.cornell.kfs.gl.batch.service.impl;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.batch.service.AccountingCycleCachingService;
import org.kuali.kfs.gl.batch.service.PostTransaction;
import org.kuali.kfs.gl.batch.service.PosterService;
import org.kuali.kfs.gl.batch.service.VerifyTransaction;
import org.kuali.kfs.gl.batch.service.impl.PosterServiceImpl;
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
import org.kuali.kfs.sys.service.ReportWriterService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.PersistenceService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuPosterServiceImpl extends PosterServiceImpl {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuPosterServiceImpl.class);

    private static final int CONTINUATION_ACCOUNT_DEPTH_LIMIT = 10;

    // Copied these fields from the superclass, since they're declared as private and have no getters but are needed for the overrides below.
    private AccountingCycleCachingService accountingCycleCachingService;
    private AccountingPeriodService accountingPeriodService;
    private ConfigurationService configurationService;
    private ReportWriterService reportWriterService;
    private ReversalDao reversalDao;
    private VerifyTransaction verifyTransaction;
    // This field doesn't use generics in the superclass; leaving it as-is.
    @SuppressWarnings("rawtypes")
    private List transactionPosters;

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
     * Overridden to check for closed or expired accounts on ICR transactions, and to update
     * such transactions to use continuation accounts if possible.
     * 
     * @see org.kuali.kfs.gl.batch.service.impl.PosterServiceImpl#postTransaction(
     * org.kuali.kfs.gl.businessobject.Transaction, int, java.util.Map, org.kuali.kfs.gl.report.LedgerSummaryReport, java.io.PrintStream,
     * org.kuali.kfs.sys.businessobject.UniversityDate, java.lang.String, java.io.PrintStream)
     */
    @Override
    protected boolean postTransaction(Transaction tran, int mode, Map<String,Integer> reportSummary, LedgerSummaryReport ledgerSummaryReport,
            PrintStream invalidGroup, UniversityDate runUniversityDate, String line, PrintStream OUTPUT_GLE_FILE_ps) {

        List<Message> errors = new ArrayList<Message>();
        Transaction originalTransaction = tran;

        try {
            final String GL_ORIGIN_ENTRY_T = getPersistenceStructureService().getTableName(OriginEntryFull.class);

            // Update select count in the report
            if ((mode == PosterService.MODE_ENTRIES) || (mode == PosterService.MODE_ICR) || (mode == PosterService.MODE_ICRENCMB)) {
                addReporting(reportSummary, GL_ORIGIN_ENTRY_T, GeneralLedgerConstants.SELECT_CODE);
            }
            // If these are reversal entries, we need to reverse the entry and
            // modify a few fields
            if (mode == PosterService.MODE_REVERSAL) {
                Reversal reversal = new Reversal(tran);
                // Reverse the debit/credit code
                if (KFSConstants.GL_DEBIT_CODE.equals(reversal.getTransactionDebitCreditCode())) {
                    reversal.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
                }
                else if (KFSConstants.GL_CREDIT_CODE.equals(reversal.getTransactionDebitCreditCode())) {
                    reversal.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
                }
                UniversityDate udate = SpringContext.getBean(BusinessObjectService.class).findBySinglePrimaryKey(
                        UniversityDate.class, reversal.getFinancialDocumentReversalDate());

                if (udate != null) {
                    reversal.setUniversityFiscalYear(udate.getUniversityFiscalYear());
                    reversal.setUniversityFiscalPeriodCode(udate.getUniversityFiscalAccountingPeriod());
                    AccountingPeriod ap = accountingPeriodService.getByPeriod(reversal.getUniversityFiscalPeriodCode(), reversal.getUniversityFiscalYear());
                    if (ap != null) {
                        if (!ap.isActive()) { // Make sure accounting period is closed
                            reversal.setUniversityFiscalYear(runUniversityDate.getUniversityFiscalYear());
                            reversal.setUniversityFiscalPeriodCode(runUniversityDate.getUniversityFiscalAccountingPeriod());
                        }
                        reversal.setFinancialDocumentReversalDate(null);
                        String newDescription = KFSConstants.GL_REVERSAL_DESCRIPTION_PREFIX + reversal.getTransactionLedgerEntryDescription();
                        if (newDescription.length() > 40) {
                            newDescription = newDescription.substring(0, 40);
                        }
                        reversal.setTransactionLedgerEntryDescription(newDescription);
                    }
                    else {
                        errors.add(new Message(configurationService.getPropertyValueAsString(
                                KFSKeyConstants.ERROR_UNIV_DATE_NOT_IN_ACCOUNTING_PERIOD_TABLE), Message.TYPE_WARNING));
                    }
                }
                else {
                    errors.add(new Message (configurationService.getPropertyValueAsString(
                            KFSKeyConstants.ERROR_REVERSAL_DATE_NOT_IN_UNIV_DATE_TABLE) , Message.TYPE_WARNING));
                }
                // Make sure the row will be unique when adding to the entries table by adjusting the transaction sequence id
                int maxSequenceId = accountingCycleCachingService.getMaxSequenceNumber(reversal);
                reversal.setTransactionLedgerEntrySequenceNumber(new Integer(maxSequenceId + 1));

                PersistenceService ps = SpringContext.getBean(PersistenceService.class);
                ps.retrieveNonKeyFields(reversal);
                tran = reversal;
            }
            else {
                // CU Customization: If an ICR transaction, switch to continuation account if ICR account is closed.
                if (mode == PosterService.MODE_ICR) {
                    Account account = getAccountWithPotentialContinuation(tran, errors);
                    tran.setChart(accountingCycleCachingService.getChart(tran.getChartOfAccountsCode()));
                    tran.setAccount(account);
                } else {
                    tran.setChart(accountingCycleCachingService.getChart(tran.getChartOfAccountsCode()));
                    tran.setAccount(accountingCycleCachingService.getAccount(tran.getChartOfAccountsCode(), tran.getAccountNumber()));
                }
                // End CU Customization.
                tran.setObjectType(accountingCycleCachingService.getObjectType(tran.getFinancialObjectTypeCode()));
                tran.setBalanceType(accountingCycleCachingService.getBalanceType(tran.getFinancialBalanceTypeCode()));
                tran.setOption(accountingCycleCachingService.getSystemOptions(tran.getUniversityFiscalYear()));

                ObjectCode objectCode = accountingCycleCachingService.getObjectCode(
                        tran.getUniversityFiscalYear(), tran.getChartOfAccountsCode(), tran.getFinancialObjectCode());
                if (ObjectUtils.isNull(objectCode)) {
                    LOG.warn(configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_OBJECT_CODE_NOT_FOUND_FOR)
                            + tran.getUniversityFiscalYear() + "," + tran.getChartOfAccountsCode() + "," + tran.getFinancialObjectCode());
                    errors.add(new Message(configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_OBJECT_CODE_NOT_FOUND_FOR)
                            + tran.getUniversityFiscalYear() + "," + tran.getChartOfAccountsCode() + "," + tran.getFinancialObjectCode(),
                            Message.TYPE_WARNING));
                }
                else {
                    tran.setFinancialObject(accountingCycleCachingService.getObjectCode(
                            tran.getUniversityFiscalYear(), tran.getChartOfAccountsCode(), tran.getFinancialObjectCode()));
                }

                // Make sure the row will be unique when adding to the entries table by adjusting the transaction sequence id
                int maxSequenceId = accountingCycleCachingService.getMaxSequenceNumber(tran);
                ((OriginEntryFull) tran).setTransactionLedgerEntrySequenceNumber(new Integer(maxSequenceId + 1));
            }

            // verify accounting period
            AccountingPeriod originEntryAccountingPeriod = accountingCycleCachingService.getAccountingPeriod(
                    tran.getUniversityFiscalYear(), tran.getUniversityFiscalPeriodCode());
            if (originEntryAccountingPeriod == null) {
                errors.add(new Message(configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_ACCOUNTING_PERIOD_NOT_FOUND)
                        + " for " + tran.getUniversityFiscalYear() + "/" + tran.getUniversityFiscalPeriodCode(),  Message.TYPE_FATAL));
            }

            if (errors.size() == 0) {
                try {
                    errors = verifyTransaction.verifyTransaction(tran);
                }
                catch (Exception e) {
                    errors.add(new Message(e.toString() + " occurred for this record.", Message.TYPE_FATAL));
                }
            }

            if (errors.size() > 0) {
                // Error on this transaction
                reportWriterService.writeError(tran, errors);
                addReporting(reportSummary, "WARNING", GeneralLedgerConstants.INSERT_CODE);
                try {
                    writeErrorEntry(line, invalidGroup);
                }
                catch (IOException ioe) {
                    LOG.error("PosterServiceImpl Stopped: " + ioe.getMessage(), ioe);
                    throw new RuntimeException("PosterServiceImpl Stopped: " + ioe.getMessage(), ioe);
                }
            }
            else {
                // No error so post it
                for (Iterator<?> posterIter = transactionPosters.iterator(); posterIter.hasNext();) {
                    PostTransaction poster = (PostTransaction) posterIter.next();
                    String actionCode = poster.post(tran, mode, runUniversityDate.getUniversityDate(), reportWriterService);

                    if (actionCode.startsWith(GeneralLedgerConstants.ERROR_CODE)) {
                        errors = new ArrayList<Message>();
                        errors.add(new Message(actionCode, Message.TYPE_WARNING));
                        reportWriterService.writeError(tran, errors);
                    }
                    else if (actionCode.indexOf(GeneralLedgerConstants.INSERT_CODE) >= 0) {
                        addReporting(reportSummary, poster.getDestinationName(), GeneralLedgerConstants.INSERT_CODE);
                    }
                    else if (actionCode.indexOf(GeneralLedgerConstants.UPDATE_CODE) >= 0) {
                        addReporting(reportSummary, poster.getDestinationName(), GeneralLedgerConstants.UPDATE_CODE);
                    }
                    else if (actionCode.indexOf(GeneralLedgerConstants.DELETE_CODE) >= 0) {
                        addReporting(reportSummary, poster.getDestinationName(), GeneralLedgerConstants.DELETE_CODE);
                    }
                    else if (actionCode.indexOf(GeneralLedgerConstants.SELECT_CODE) >= 0) {
                        addReporting(reportSummary, poster.getDestinationName(), GeneralLedgerConstants.SELECT_CODE);
                    }
                }
                if (errors.size() == 0) {
                    // Delete the reversal entry
                    if (mode == PosterService.MODE_REVERSAL) {
                        createOutputEntry(tran, OUTPUT_GLE_FILE_ps);
                        reversalDao.delete((Reversal) originalTransaction);
                        addReporting(reportSummary, getPersistenceStructureService().getTableName(Reversal.class), GeneralLedgerConstants.DELETE_CODE);
                    }

                    ledgerSummaryReport.summarizeEntry(new OriginEntryFull(tran));
                    return true;
                }
            }

            return false;
        }
        catch (IOException ioe) {
            LOG.error("PosterServiceImpl Stopped: " + ioe.getMessage(), ioe);
            throw new RuntimeException("PosterServiceImpl Stopped: " + ioe.getMessage(), ioe);

        }
        catch (RuntimeException re) {
            LOG.error("PosterServiceImpl Stopped: " + re.getMessage(), re);
            throw new RuntimeException("PosterServiceImpl Stopped: " + re.getMessage(), re);
        }
    }

    /**
     * Helper method for retrieving the account for an ICR transaction, or its continuation account
     * if the base account is closed. May update the transaction's chart code and account number
     * if continuation account usage is necessary. Will return just the regular account if a valid
     * continuation one could not be found, but will also update the errors list accordingly.
     * 
     * As with similar handling in the Scrubber job, closed continuation accounts will trigger
     * further traversal of the continuation account hierarchy, up to a depth of 10.
     * 
     * @param tran The transaction to process.
     * @param errors The list of errors for this transaction.
     * @return The transaction's account, or the descendant continuation account up to a depth to 10.
     */
    protected Account getAccountWithPotentialContinuation(Transaction tran, List<Message> errors) {
        Account account = accountingCycleCachingService.getAccount(tran.getChartOfAccountsCode(), tran.getAccountNumber());
        if (ObjectUtils.isNotNull(account) && account.isClosed()) {
            // Account is closed, search for continuation account to use instead.
            Account contAccount = account;
            for (int i = 0; i < CONTINUATION_ACCOUNT_DEPTH_LIMIT && ObjectUtils.isNotNull(contAccount) && contAccount.isClosed(); i++) {
                contAccount = accountingCycleCachingService.getAccount(
                        contAccount.getContinuationFinChrtOfAcctCd(), contAccount.getContinuationAccountNumber());
            }
            if (ObjectUtils.isNull(contAccount) || contAccount == account || contAccount.isClosed()) {
                // No valid continuation account found; do not post transaction.
                errors.add(new Message(MessageFormat.format(
                        configurationService.getPropertyValueAsString(CUKFSKeyConstants.ERROR_ICRACCOUNT_CONTINUATION_ACCOUNT_CLOSED),
                        tran.getChartOfAccountsCode(), tran.getAccountNumber()), Message.TYPE_WARNING));
            } else if (tran instanceof OriginEntryInformation) {
                // Found a valid continuation account for an origin-entry-related transaction; update accordingly for posting.
                LOG.warn(MessageFormat.format(
                        configurationService.getPropertyValueAsString(CUKFSKeyConstants.WARNING_ICRACCOUNT_CONTINUATION_ACCOUNT_USED),
                        tran.getChartOfAccountsCode(), tran.getAccountNumber(),
                        contAccount.getChartOfAccountsCode(), contAccount.getAccountNumber()));
                account = contAccount;
                ((OriginEntryInformation) tran).setChartOfAccountsCode(contAccount.getChartOfAccountsCode());
                ((OriginEntryInformation) tran).setAccountNumber(contAccount.getAccountNumber());
            } else {
                /*
                 * Found a valid continuation account, but for a transaction that may not necessarily
                 * allow for overwriting the chart code and account number; do not post.
                 * (NOTE: Theoretically, this case should never happen, due to how the superclass is set up.)
                 */
                errors.add(new Message(MessageFormat.format(
                        configurationService.getPropertyValueAsString(CUKFSKeyConstants.ERROR_ICRACCOUNT_CONTINUATION_ACCOUNT_INVALID_TRANSACTION),
                        tran.getChartOfAccountsCode(), tran.getAccountNumber(),
                        contAccount.getChartOfAccountsCode(), contAccount.getAccountNumber()), Message.TYPE_WARNING));
            }
        }
        
        return account;
    }



    @Override
    public void setAccountingCycleCachingService(AccountingCycleCachingService accountingCycleCachingService) {
        super.setAccountingCycleCachingService(accountingCycleCachingService);
        this.accountingCycleCachingService = accountingCycleCachingService;
    }

    @Override
    public void setAccountingPeriodService(AccountingPeriodService accountingPeriodService) {
        super.setAccountingPeriodService(accountingPeriodService);
        this.accountingPeriodService = accountingPeriodService;
    }

    @Override
    public void setConfigurationService(ConfigurationService configurationService) {
        super.setConfigurationService(configurationService);
        this.configurationService = configurationService;
    }

    @Override
    public void setReportWriterService(ReportWriterService reportWriterService) {
        super.setReportWriterService(reportWriterService);
        this.reportWriterService = reportWriterService;
    }

    @Override
    public void setReversalDao(ReversalDao reversalDao) {
        super.setReversalDao(reversalDao);
        this.reversalDao = reversalDao;
    }

    @Override
    public void setVerifyTransaction(VerifyTransaction verifyTransaction) {
        super.setVerifyTransaction(verifyTransaction);
        this.verifyTransaction = verifyTransaction;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setTransactionPosters(List transactionPosters) {
        super.setTransactionPosters(transactionPosters);
        this.transactionPosters = transactionPosters;
    }

}
