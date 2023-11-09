/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.gl.batch.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.A21IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryRate;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryRateDetail;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.OffsetDefinition;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.dataaccess.IndirectCostRecoveryRateDetailDao;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.OffsetDefinitionService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.BusinessObjectDictionaryService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.gl.GLParameterConstants;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.batch.PosterIcrGenerationStep;
import org.kuali.kfs.gl.batch.service.AccountingCycleCachingService;
import org.kuali.kfs.gl.batch.service.PostTransaction;
import org.kuali.kfs.gl.batch.service.PosterService;
import org.kuali.kfs.gl.batch.service.RunDateService;
import org.kuali.kfs.gl.batch.service.VerifyTransaction;
import org.kuali.kfs.gl.businessobject.ExpenditureTransaction;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.gl.businessobject.OriginEntryInformation;
import org.kuali.kfs.gl.businessobject.Reversal;
import org.kuali.kfs.gl.businessobject.Transaction;
import org.kuali.kfs.gl.dataaccess.ExpenditureTransactionDao;
import org.kuali.kfs.gl.dataaccess.ReversalDao;
import org.kuali.kfs.gl.report.LedgerSummaryReport;
import org.kuali.kfs.gl.report.TransactionListingReport;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.PersistenceService;
import org.kuali.kfs.krad.service.PersistenceStructureService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.Message;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.kuali.kfs.sys.exception.InvalidFlexibleOffsetException;
import org.kuali.kfs.sys.service.FlexibleOffsetAccountService;
import org.kuali.kfs.sys.service.ReportWriterService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Transactional
public class PosterServiceImpl implements PosterService {

    private static final Logger LOG = LogManager.getLogger();

    protected static final int CONTINUATION_ACCOUNT_DEPTH_LIMIT = 10;
    // known users: Cornell, UCI
    protected static final String DOCUMENT_TYPE = "DOCUMENT_TYPE";
    protected static final String DATE_FORMAT_STRING = "yyyyMMdd";
    protected static final KualiDecimal WARNING_MAX_DIFFERENCE = new KualiDecimal("0.03");

    public static final DecimalFormat DFPCT;
    public static final DecimalFormat DFAMT;
    static {
        DFPCT = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        DFPCT.applyPattern("#0.000");
        DFAMT = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        DFAMT.applyPattern("##########.00");
    }

    public static final BigDecimal BDONEHUNDRED = new BigDecimal("100");

    protected List transactionPosters;
    protected VerifyTransaction verifyTransaction;
    protected DateTimeService dateTimeService;
    protected ReversalDao reversalDao;
    protected AccountingPeriodService accountingPeriodService;
    protected ExpenditureTransactionDao expenditureTransactionDao;
    protected IndirectCostRecoveryRateDetailDao indirectCostRecoveryRateDetailDao;
    protected ObjectCodeService objectCodeService;
    protected ParameterService parameterService;
    protected ConfigurationService configurationService;
    protected FlexibleOffsetAccountService flexibleOffsetAccountService;
    protected RunDateService runDateService;
    protected OffsetDefinitionService offsetDefinitionService;
    protected BusinessObjectDictionaryService businessObjectDictionaryService;
    protected DataDictionaryService dataDictionaryService;
    protected BusinessObjectService businessObjectService;
    protected PersistenceStructureService persistenceStructureService;
    protected ReportWriterService reportWriterService;
    protected ReportWriterService errorListingReportWriterService;
    protected ReportWriterService reversalReportWriterService;
    protected ReportWriterService ledgerSummaryReportWriterService;

    private PersistenceService persistenceService;

    protected String batchFileDirectoryName;
    protected AccountingCycleCachingService accountingCycleCachingService;

    /**
     * Post scrubbed GL entries to GL tables.
     */
    @Override
    public void postMainEntries() {
        LOG.debug("postMainEntries() started");

        try {
            final FileReader INPUT_GLE_FILE = new FileReader(batchFileDirectoryName + File.separator +
                                                             GeneralLedgerConstants.BatchFileSystem.POSTER_INPUT_FILE +
                                                             GeneralLedgerConstants.BatchFileSystem.EXTENSION, StandardCharsets.UTF_8);
            final File OUTPUT_ERR_FILE = new File(batchFileDirectoryName + File.separator +
                                                  GeneralLedgerConstants.BatchFileSystem.POSTER_ERROR_OUTPUT_FILE +
                                                  GeneralLedgerConstants.BatchFileSystem.EXTENSION);

            postEntries(PosterService.MODE_ENTRIES, INPUT_GLE_FILE, null, OUTPUT_ERR_FILE);

            INPUT_GLE_FILE.close();
        } catch (final FileNotFoundException e1) {
            e1.printStackTrace();
            throw new RuntimeException("PosterMainEntries Stopped: " + e1.getMessage(), e1);
        } catch (final IOException ioe) {
            LOG.error("postMainEntries stopped due to: {}", ioe::getMessage, () -> ioe);
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Post reversal GL entries to GL tables.
     */
    @Override
    public void postReversalEntries() {
        LOG.debug("postReversalEntries() started");

        try {
            final PrintStream OUTPUT_GLE_FILE_ps = new PrintStream(batchFileDirectoryName + File.separator +
                                                                   GeneralLedgerConstants.BatchFileSystem.REVERSAL_POSTER_VALID_OUTPUT_FILE +
                                                                   GeneralLedgerConstants.BatchFileSystem.EXTENSION);
            final File OUTPUT_ERR_FILE = new File(batchFileDirectoryName + File.separator +
                                                  GeneralLedgerConstants.BatchFileSystem.REVERSAL_POSTER_ERROR_OUTPUT_FILE +
                                                  GeneralLedgerConstants.BatchFileSystem.EXTENSION);

            postEntries(PosterService.MODE_REVERSAL, null, OUTPUT_GLE_FILE_ps, OUTPUT_ERR_FILE);

            OUTPUT_GLE_FILE_ps.close();
        } catch (final IOException e1) {
            e1.printStackTrace();
            throw new RuntimeException("PosterReversalEntries Stopped: " + e1.getMessage(), e1);
        }
    }

    /**
     * Post ICR GL entries to GL tables.
     */
    @Override
    public void postIcrEntries() {
        LOG.debug("postIcrEntries() started");

        try {
            final FileReader INPUT_GLE_FILE = new FileReader(batchFileDirectoryName + File.separator +
                                                             GeneralLedgerConstants.BatchFileSystem.ICR_POSTER_INPUT_FILE +
                                                             GeneralLedgerConstants.BatchFileSystem.EXTENSION, StandardCharsets.UTF_8);
            final File OUTPUT_ERR_FILE = new File(batchFileDirectoryName + File.separator +
                                                  GeneralLedgerConstants.BatchFileSystem.ICR_POSTER_ERROR_OUTPUT_FILE +
                                                  GeneralLedgerConstants.BatchFileSystem.EXTENSION);

            postEntries(PosterService.MODE_ICR, INPUT_GLE_FILE, null, OUTPUT_ERR_FILE);

            INPUT_GLE_FILE.close();
        } catch (final FileNotFoundException e1) {
            e1.printStackTrace();
            throw new RuntimeException("PosterIcrEntries Stopped: " + e1.getMessage(), e1);
        } catch (final IOException ioe) {
            LOG.error("postIcrEntries stopped due to: {}", ioe::getMessage, () -> ioe);
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Post ICR Encumbrance GL entries to GL tables.
     */
    @Override
    public void postIcrEncumbranceEntries() {
        LOG.debug("postIcrEncumbranceEntries() started");

        try {
            final FileReader inputFile = new FileReader(batchFileDirectoryName + File.separator +
                                                        GeneralLedgerConstants.BatchFileSystem.ICR_ENCUMBRANCE_POSTER_INPUT_FILE +
                                                        GeneralLedgerConstants.BatchFileSystem.EXTENSION, StandardCharsets.UTF_8);
            final File outputErrorFile = new File(batchFileDirectoryName + File.separator +
                                                  GeneralLedgerConstants.BatchFileSystem.ICR_ENCUMBRANCE_POSTER_ERROR_OUTPUT_FILE +
                                                  GeneralLedgerConstants.BatchFileSystem.EXTENSION);
            final PrintStream outputFile = new PrintStream(batchFileDirectoryName + File.separator +
                                                           GeneralLedgerConstants.BatchFileSystem.ICR_ENCUMBRANCE_POSTER_OUTPUT_FILE +
                                                           GeneralLedgerConstants.BatchFileSystem.EXTENSION);

            postEntries(PosterService.MODE_ICRENCMB, inputFile, outputFile, outputErrorFile);

            outputFile.close();
            inputFile.close();
        } catch (final FileNotFoundException e1) {
            e1.printStackTrace();
            throw new RuntimeException("postIcrEncumbranceEntries Stopped: " + e1.getMessage(), e1);
        } catch (final IOException ioe) {
            LOG.error("postIcrEncumbranceEntries stopped due to: {}", ioe::getMessage, () -> ioe);
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Actually post the entries. The mode variable decides which entries to post.
     *
     * @param mode the poster's current run mode
     */
    protected void postEntries(
            final int mode, final FileReader INPUT_GLE_FILE, final PrintStream OUTPUT_GLE_FILE_ps,
            final File OUTPUT_ERR_FILE) throws IOException {
        LOG.debug("postEntries() started");

        final PrintStream OUTPUT_ERR_FILE_ps = new PrintStream(OUTPUT_ERR_FILE, StandardCharsets.UTF_8);
        BufferedReader INPUT_GLE_FILE_br = null;
        if (INPUT_GLE_FILE != null) {
            INPUT_GLE_FILE_br = new BufferedReader(INPUT_GLE_FILE);
        }

        String GLEN_RECORD;
        final Date executionDate = new Date(dateTimeService.getCurrentDate().getTime());
        final Date runDate = new Date(runDateService.calculateRunDate(executionDate).getTime());
        final UniversityDate runUniversityDate = businessObjectService.findBySinglePrimaryKey(UniversityDate.class, runDate);
        final LedgerSummaryReport ledgerSummaryReport = new LedgerSummaryReport();

        // Build the summary map so all the possible combinations of destination & operation
        // are included in the summary part of the report.
        final Map<String, Integer> reportSummary = new HashMap<>();
        for (final Object transactionPoster : transactionPosters) {
            final PostTransaction poster = (PostTransaction) transactionPoster;
            reportSummary.put(poster.getDestinationName() + "," + GeneralLedgerConstants.DELETE_CODE, 0);
            reportSummary.put(poster.getDestinationName() + "," + GeneralLedgerConstants.INSERT_CODE, 0);
            reportSummary.put(poster.getDestinationName() + "," + GeneralLedgerConstants.UPDATE_CODE, 0);
        }
        int ecount = 0;

        OriginEntryFull tran = null;
        Transaction reversalTransaction = null;
        try {
            if (mode == PosterService.MODE_ENTRIES || mode == PosterService.MODE_ICR
                    || mode == PosterService.MODE_ICRENCMB) {
                LOG.debug("postEntries() Processing groups");
                while ((GLEN_RECORD = INPUT_GLE_FILE_br.readLine()) != null) {
                    if (StringUtils.isNotEmpty(GLEN_RECORD) && StringUtils.isNotBlank(GLEN_RECORD.trim())) {
                        ecount++;

                        GLEN_RECORD = StringUtils.rightPad(GLEN_RECORD, 183, ' ');
                        tran = new OriginEntryFull();

                        // checking parsing process and stop poster when it has errors.
                        final List<Message> parsingError = tran.setFromTextFileForBatch(GLEN_RECORD, ecount);
                        if (parsingError.size() > 0) {
                            String messages = "";
                            for (final Message msg : parsingError) {
                                messages += msg + " ";
                            }
                            throw new RuntimeException("Exception happened from parsing process: " + messages);
                        }
                        // need to pass ecount for building better message
                        addReporting(reportSummary, "SEQUENTIAL", GeneralLedgerConstants.SELECT_CODE);
                        postTransaction(tran, mode, reportSummary, ledgerSummaryReport, OUTPUT_ERR_FILE_ps,
                                runUniversityDate, GLEN_RECORD, OUTPUT_GLE_FILE_ps);

                        if (ecount % 1000 == 0) {
                            LOG.info("postEntries() Posted Entry {}", ecount);
                        }

                        // need to create offset as this was not done in the ICR Encumbrance Feed Step
                        if (mode == PosterService.MODE_ICRENCMB) {
                            ecount++;
                            generateOffset(tran, mode, reportSummary, ledgerSummaryReport, OUTPUT_ERR_FILE_ps,
                                    runUniversityDate, GLEN_RECORD, OUTPUT_GLE_FILE_ps);

                            if (ecount % 1000 == 0) {
                                LOG.info("postEntries() Posted Entry {}", ecount);
                            }
                        }
                    }
                }
                if (INPUT_GLE_FILE_br != null) {
                    INPUT_GLE_FILE_br.close();
                }
                OUTPUT_ERR_FILE_ps.close();
                reportWriterService.writeStatisticLine("SEQUENTIAL RECORDS READ                    %,9d",
                        reportSummary.get("SEQUENTIAL,S"));
            } else {
                LOG.debug("postEntries() Processing reversal transactions");

                final String GL_REVERSAL_T = persistenceStructureService.getTableName(Reversal.class);
                final Iterator reversalTransactions = reversalDao.getByDate(runDate);
                final TransactionListingReport reversalListingReport = new TransactionListingReport();
                while (reversalTransactions.hasNext()) {
                    ecount++;
                    reversalTransaction = (Transaction) reversalTransactions.next();
                    addReporting(reportSummary, GL_REVERSAL_T, GeneralLedgerConstants.SELECT_CODE);

                    final boolean posted = postTransaction(reversalTransaction, mode, reportSummary, ledgerSummaryReport,
                            OUTPUT_ERR_FILE_ps, runUniversityDate, GL_REVERSAL_T, OUTPUT_GLE_FILE_ps);

                    if (posted) {
                        reversalListingReport.generateReport(reversalReportWriterService, reversalTransaction);
                    }

                    if (ecount % 1000 == 0) {
                        LOG.info("postEntries() Posted Entry {}", ecount);
                    }
                }

                OUTPUT_ERR_FILE_ps.close();

                reportWriterService.writeStatisticLine("GLRV RECORDS READ (GL_REVERSAL_T)          %,9d",
                        reportSummary.get("GL_REVERSAL_T,S"));
                reversalListingReport.generateStatistics(reversalReportWriterService);
            }

            //PDF version had this abstracted to print I/U/D for each table in 7 posters, but some statistics are
            // meaningless (i.e. GLEN is never updated), so un-abstracted here
            reportWriterService.writeStatisticLine("GLEN RECORDS INSERTED (GL_ENTRY_T)         %,9d",
                    reportSummary.get("GL_ENTRY_T,I"));
            reportWriterService.writeStatisticLine("GLBL RECORDS INSERTED (GL_BALANCE_T)       %,9d",
                    reportSummary.get("GL_BALANCE_T,I"));
            reportWriterService.writeStatisticLine("GLBL RECORDS UPDATED  (GL_BALANCE_T)       %,9d",
                    reportSummary.get("GL_BALANCE_T,U"));
            reportWriterService.writeStatisticLine("GLEX RECORDS INSERTED (GL_EXPEND_TRN_MT)    %,9d",
                    reportSummary.get("GL_EXPEND_TRN_MT,I"));
            reportWriterService.writeStatisticLine("GLEX RECORDS UPDATED  (GL_EXPEND_TRN_MT)    %,9d",
                    reportSummary.get("GL_EXPEND_TRN_MT,U"));
            reportWriterService.writeStatisticLine("GLEC RECORDS INSERTED (GL_ENCUMBRANCE_T)   %,9d",
                    reportSummary.get("GL_ENCUMBRANCE_T,I"));
            reportWriterService.writeStatisticLine("GLEC RECORDS UPDATED  (GL_ENCUMBRANCE_T)   %,9d",
                    reportSummary.get("GL_ENCUMBRANCE_T,U"));
            reportWriterService.writeStatisticLine("GLRV RECORDS INSERTED (GL_REVERSAL_T)      %,9d",
                    reportSummary.get("GL_REVERSAL_T,I"));
            reportWriterService.writeStatisticLine("GLRV RECORDS DELETED  (GL_REVERSAL_T)      %,9d",
                    reportSummary.get("GL_REVERSAL_T,D"));
            reportWriterService.writeStatisticLine("SFBL RECORDS INSERTED (GL_SF_BALANCES_T)   %,9d",
                    reportSummary.get("GL_SF_BALANCES_T,I"));
            reportWriterService.writeStatisticLine("SFBL RECORDS UPDATED  (GL_SF_BALANCES_T)   %,9d",
                    reportSummary.get("GL_SF_BALANCES_T,U"));
            reportWriterService.writeStatisticLine("ACBL RECORDS INSERTED (GL_ACCT_BALANCES_T) %,9d",
                    reportSummary.get("GL_ACCT_BALANCES_T,I"));
            reportWriterService.writeStatisticLine("ACBL RECORDS UPDATED  (GL_ACCT_BALANCES_T) %,9d",
                    reportSummary.get("GL_ACCT_BALANCES_T,U"));
            reportWriterService.writeStatisticLine("ERROR RECORDS WRITTEN                      %,9d",
                    reportSummary.get("WARNING,I"));
        } catch (final RuntimeException re) {
            final int loggableEcount = ecount;
            LOG.error(
                    "postEntries stopped due to: {}, on line number : {}",
                    re::getMessage,
                    () -> loggableEcount,
                    () -> re
            );
            // the null checking in the following code doesn't work as intended, since Java evaluates "+" before "==";
            // as a result, if reversalTransaction is indeed null, it will cause NPE; and that happened.
            // fixing it by adding () around the ? expression
            final OriginEntryFull loggableTran = tran;
            LOG.error(
                    "transaction failure occurred  on: {}",
                    () -> ObjectUtils.isNull(loggableTran) ? null : loggableTran.toString()
            );
            final Transaction loggableReversalTransaction = reversalTransaction;
            LOG.error(
                    "reversalTransaction failure occurred  on: {}",
                    () -> ObjectUtils.isNull(loggableReversalTransaction) ? null : loggableReversalTransaction.toString()
            );
            throw new RuntimeException("PosterService Stopped: " + re.getMessage(), re);
        } catch (final IOException e) {
            LOG.error("postEntries stopped due to: {}", e::getMessage, () -> e);
            throw new RuntimeException(e);
        } catch (final Exception e) {
            // do nothing - handled in postTransaction method.
        }

        LOG.info("postEntries() done, total count = {}", ecount);
        // Generate the reports
        ledgerSummaryReport.writeReport(ledgerSummaryReportWriterService);
        new TransactionListingReport().generateReport(errorListingReportWriterService,
                new OriginEntryFileIterator(OUTPUT_ERR_FILE));
    }

    /**
     * Runs the given transaction through each transaction posting algorithms associated with this instance
     *
     * @param tran                a transaction to post
     * @param mode                the mode the poster is running in
     * @param reportSummary       a Map of summary counts generated by the posting process
     * @param ledgerSummaryReport for summary reporting
     * @param invalidGroup        the group to save invalid entries to
     * @param runUniversityDate   the university date of this poster run
     * @param line
     * @return whether the transaction was posted or not. Useful if calling class attempts to report on the transaction
     */
    protected boolean postTransaction(Transaction tran, final int mode, final Map<String, Integer> reportSummary,
            final LedgerSummaryReport ledgerSummaryReport, final PrintStream invalidGroup, final UniversityDate runUniversityDate,
            final String line, final PrintStream OUTPUT_GLE_FILE_ps) {
        List<Message> errors = new ArrayList<>();
        final Transaction originalTransaction = tran;

        try {
            final String GL_ORIGIN_ENTRY_T = persistenceStructureService.getTableName(OriginEntryFull.class);

            // Update select count in the report
            if (mode == PosterService.MODE_ENTRIES || mode == PosterService.MODE_ICR
                || mode == PosterService.MODE_ICRENCMB) {
                addReporting(reportSummary, GL_ORIGIN_ENTRY_T, GeneralLedgerConstants.SELECT_CODE);
            }
            // If these are reversal entries, we need to reverse the entry and modify a few fields
            if (mode == PosterService.MODE_REVERSAL) {
                final Reversal reversal = new Reversal(tran);
                reverseDebitCreditCodeOrAmount(reversal);
                final UniversityDate udate = businessObjectService.findBySinglePrimaryKey(UniversityDate.class,
                        reversal.getFinancialDocumentReversalDate());

                if (udate != null) {
                    reversal.setUniversityFiscalYear(udate.getUniversityFiscalYear());
                    reversal.setUniversityFiscalPeriodCode(udate.getUniversityFiscalAccountingPeriod());
                    final AccountingPeriod ap = accountingPeriodService.getByPeriod(reversal.getUniversityFiscalPeriodCode(),
                            reversal.getUniversityFiscalYear());
                    if (ap != null) {
                        // Make sure accounting period is closed
                        if (!ap.isActive()) {
                            reversal.setUniversityFiscalYear(runUniversityDate.getUniversityFiscalYear());
                            reversal.setUniversityFiscalPeriodCode(
                                    runUniversityDate.getUniversityFiscalAccountingPeriod());
                        }
                        reversal.setFinancialDocumentReversalDate(null);
                        String newDescription = KFSConstants.GL_REVERSAL_DESCRIPTION_PREFIX +
                                reversal.getTransactionLedgerEntryDescription();
                        if (newDescription.length() > 40) {
                            newDescription = newDescription.substring(0, 40);
                        }
                        reversal.setTransactionLedgerEntryDescription(newDescription);
                    } else {
                        errors.add(new Message(configurationService.getPropertyValueAsString(
                                KFSKeyConstants.ERROR_UNIV_DATE_NOT_IN_ACCOUNTING_PERIOD_TABLE),
                                Message.TYPE_WARNING));
                    }
                } else {
                    errors.add(new Message(configurationService.getPropertyValueAsString(
                            KFSKeyConstants.ERROR_REVERSAL_DATE_NOT_IN_UNIV_DATE_TABLE), Message.TYPE_WARNING));
                }
                // Make sure the row will be unique when adding to the entries table by adjusting the transaction
                // sequence id
                final int maxSequenceId = accountingCycleCachingService.getMaxSequenceNumber(reversal);
                reversal.setTransactionLedgerEntrySequenceNumber(maxSequenceId + 1);

                persistenceService.retrieveNonKeyFields(reversal);
                tran = reversal;
            } else {
                if (mode == PosterService.MODE_ICR) {
                    Account account = getAccountWithPotentialContinuation(tran, errors);
                    LOG.info("set account to potential continuation: " + account);
                    tran.setAccount(getAccountWithPotentialContinuation(tran, errors));
                } else {
                    tran.setAccount(accountingCycleCachingService.getAccount(tran.getChartOfAccountsCode(),
                            tran.getAccountNumber()));
                }
                tran.setChart(accountingCycleCachingService.getChart(tran.getChartOfAccountsCode()));
                tran.setObjectType(accountingCycleCachingService.getObjectType(tran.getFinancialObjectTypeCode()));
                tran.setBalanceType(accountingCycleCachingService.getBalanceType(tran.getFinancialBalanceTypeCode()));
                tran.setOption(accountingCycleCachingService.getSystemOptions(tran.getUniversityFiscalYear()));

                final ObjectCode objectCode = accountingCycleCachingService.getObjectCode(tran.getUniversityFiscalYear(),
                        tran.getChartOfAccountsCode(), tran.getFinancialObjectCode());
                if (ObjectUtils.isNull(objectCode)) {
                    LOG.warn(
                            "{}{},{},{}",
                            () -> configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_OBJECT_CODE_NOT_FOUND_FOR),
                            tran::getUniversityFiscalYear,
                            tran::getChartOfAccountsCode,
                            tran::getFinancialObjectCode
                    );
                    errors.add(new Message(configurationService.getPropertyValueAsString(
                            KFSKeyConstants.ERROR_OBJECT_CODE_NOT_FOUND_FOR) + tran.getUniversityFiscalYear() + "," +
                            tran.getChartOfAccountsCode() + "," + tran.getFinancialObjectCode(), Message.TYPE_WARNING));
                } else {
                    tran.setFinancialObject(accountingCycleCachingService.getObjectCode(tran.getUniversityFiscalYear(),
                            tran.getChartOfAccountsCode(), tran.getFinancialObjectCode()));
                }

                // Make sure the row will be unique when adding to the entries table by adjusting the transaction
                // sequence id
                final int maxSequenceId = accountingCycleCachingService.getMaxSequenceNumber(tran);
                ((OriginEntryFull) tran).setTransactionLedgerEntrySequenceNumber(maxSequenceId + 1);
            }

            // verify accounting period
            final AccountingPeriod originEntryAccountingPeriod = accountingCycleCachingService.getAccountingPeriod(
                    tran.getUniversityFiscalYear(), tran.getUniversityFiscalPeriodCode());
            if (originEntryAccountingPeriod == null) {
                errors.add(new Message(configurationService.getPropertyValueAsString(
                        KFSKeyConstants.ERROR_ACCOUNTING_PERIOD_NOT_FOUND) + " for " +
                        tran.getUniversityFiscalYear() + "/" + tran.getUniversityFiscalPeriodCode(),
                        Message.TYPE_FATAL));
            }

            if (errors.size() == 0) {
                try {
                    errors = verifyTransaction.verifyTransaction(tran);
                } catch (final Exception e) {
                    errors.add(new Message(e.toString() + " occurred for this record.", Message.TYPE_FATAL));
                }
            }

            if (errors.size() > 0) {
                // Error on this transaction
                reportWriterService.writeError(tran, errors);
                addReporting(reportSummary, "WARNING", GeneralLedgerConstants.INSERT_CODE);
                try {
                    writeErrorEntry(line, invalidGroup);
                } catch (final IOException ioe) {
                    LOG.error("PosterServiceImpl Stopped: {}", ioe::getMessage, () -> ioe);
                    throw new RuntimeException("PosterServiceImpl Stopped: " + ioe.getMessage(), ioe);
                }
            } else {
                // No error so post it
                for (final Object transactionPoster : transactionPosters) {
                    final PostTransaction poster = (PostTransaction) transactionPoster;
                    final String actionCode = poster.post(tran, mode, runUniversityDate.getUniversityDate(),
                            reportWriterService);

                    if (actionCode.startsWith(GeneralLedgerConstants.ERROR_CODE)) {
                        errors = new ArrayList<>();
                        errors.add(new Message(actionCode, Message.TYPE_WARNING));
                        reportWriterService.writeError(tran, errors);
                    } else if (actionCode.contains(GeneralLedgerConstants.INSERT_CODE)) {
                        addReporting(reportSummary, poster.getDestinationName(), GeneralLedgerConstants.INSERT_CODE);
                    } else if (actionCode.contains(GeneralLedgerConstants.UPDATE_CODE)) {
                        addReporting(reportSummary, poster.getDestinationName(), GeneralLedgerConstants.UPDATE_CODE);
                    } else if (actionCode.contains(GeneralLedgerConstants.DELETE_CODE)) {
                        addReporting(reportSummary, poster.getDestinationName(), GeneralLedgerConstants.DELETE_CODE);
                    } else if (actionCode.contains(GeneralLedgerConstants.SELECT_CODE)) {
                        addReporting(reportSummary, poster.getDestinationName(), GeneralLedgerConstants.SELECT_CODE);
                    }
                }
                if (errors.size() == 0) {
                    // Delete the reversal entry
                    if (mode == PosterService.MODE_REVERSAL) {
                        createOutputEntry(tran, OUTPUT_GLE_FILE_ps);
                        reversalDao.delete((Reversal) originalTransaction);
                        addReporting(reportSummary, persistenceStructureService.getTableName(Reversal.class),
                                GeneralLedgerConstants.DELETE_CODE);
                    } else if (mode == PosterService.MODE_ICRENCMB) {
                        createOutputEntry(tran, OUTPUT_GLE_FILE_ps);
                    }

                    ledgerSummaryReport.summarizeEntry(new OriginEntryFull(tran));
                    return true;
                }
            }

            return false;
        } catch (IOException | RuntimeException e) {
            LOG.error("PosterServiceImpl Stopped: {}", e::getMessage, () -> e);
            throw new RuntimeException("PosterServiceImpl Stopped: " + e.getMessage(), e);
        }
    }

    /**
     * If this is a budget entry (i.e. doesn't have a debit/credit code), reverse (i.e. negate) the amount, otherwise
     * reverse the debit / credit code.
     *
     * @param reversal the reversal of the transaction to change
     */
    protected void reverseDebitCreditCodeOrAmount(final Reversal reversal) {
        final String transactionDebitCreditCode = reversal.getTransactionDebitCreditCode();

        if (StringUtils.isBlank(transactionDebitCreditCode)) {
            reversal.setTransactionLedgerEntryAmount(reversal.getTransactionLedgerEntryAmount().negated());
        } else {
            if (KFSConstants.GL_DEBIT_CODE.equals(transactionDebitCreditCode)) {
                reversal.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            } else if (KFSConstants.GL_CREDIT_CODE.equals(transactionDebitCreditCode)) {
                reversal.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            }
        }
    }

    /**
     * Helper method for retrieving the account for an ICR transaction, or its continuation account if the base
     * account is closed. May update the transaction's chart code and account number if continuation account usage is
     * necessary. Will return just the regular account if a valid continuation one could not be found, but will also
     * update the errors list accordingly.
     *
     * As with similar handling in the Scrubber job, closed continuation accounts will trigger further traversal of
     * the continuation account hierarchy, up to a depth defined by the CONTINUATION_ACCOUNT_DEPTH_LIMIT constant.
     *
     * @param tran The transaction to process.
     * @param errors The list of errors for this transaction.
     * @return The transaction's account, or the descendant continuation account up to a depth to 10.
     */
    protected Account getAccountWithPotentialContinuation(final Transaction tran, final List<Message> errors) {
        Account account = accountingCycleCachingService.getAccount(tran.getChartOfAccountsCode(),
                tran.getAccountNumber());

        if (ObjectUtils.isNotNull(account) && account.isClosed()) {
            Account contAccount = account;
            for (int i = 0; i < CONTINUATION_ACCOUNT_DEPTH_LIMIT && ObjectUtils.isNotNull(contAccount)
                    && contAccount.isClosed(); i++) {
                contAccount = accountingCycleCachingService.getAccount(
                    contAccount.getContinuationFinChrtOfAcctCd(), contAccount.getContinuationAccountNumber());
            }
            if (ObjectUtils.isNull(contAccount) || contAccount == account || contAccount.isClosed()) {
                errors.add(new Message(MessageFormat.format(
                    configurationService.getPropertyValueAsString(
                            KFSKeyConstants.ERROR_ICRACCOUNT_CONTINUATION_ACCOUNT_CLOSED),
                    tran.getChartOfAccountsCode(), tran.getAccountNumber(), CONTINUATION_ACCOUNT_DEPTH_LIMIT),
                        Message.TYPE_WARNING));
            } else {
                final String formattedErrorMessage = MessageFormat.format(
                    configurationService.getPropertyValueAsString(
                            KFSKeyConstants.WARNING_ICRACCOUNT_CONTINUATION_ACCOUNT_USED),
                    tran.getChartOfAccountsCode(), tran.getAccountNumber(),
                    contAccount.getChartOfAccountsCode(), contAccount.getAccountNumber());
                errors.add(new Message(formattedErrorMessage, Message.TYPE_WARNING));
                LOG.warn(formattedErrorMessage);
                account = contAccount;
                ((OriginEntryInformation) tran).setChartOfAccountsCode(contAccount.getChartOfAccountsCode());
                ((OriginEntryInformation) tran).setAccountNumber(contAccount.getAccountNumber());
            }
        }

        return account;
    }

    /**
     * This step reads the expenditure table and uses the data to generate Indirect Cost Recovery transactions.
     */
    @Override
    public void generateIcrTransactions() {
        LOG.debug("generateIcrTransactions() started");

        final Date executionDate = dateTimeService.getCurrentSqlDate();
        final Date runDate = new Date(runDateService.calculateRunDate(executionDate).getTime());

        try {
            final PrintStream OUTPUT_GLE_FILE_ps = new PrintStream(batchFileDirectoryName + File.separator +
                                                                   GeneralLedgerConstants.BatchFileSystem.ICR_TRANSACTIONS_OUTPUT_FILE +
                                                                   GeneralLedgerConstants.BatchFileSystem.EXTENSION);

            int reportExpendTranRetrieved = 0;
            int reportExpendTranDeleted = 0;
            final int reportExpendTranKept = 0;
            int reportOriginEntryGenerated = 0;
            final Iterator expenditureTransactions;

            try {
                expenditureTransactions = expenditureTransactionDao.getAllExpenditureTransactions();
            } catch (final RuntimeException re) {
                LOG.error("generateIcrTransactions Stopped: {}", re::getMessage);
                throw new RuntimeException("generateIcrTransactions Stopped: " + re.getMessage(), re);
            }

            while (expenditureTransactions.hasNext()) {
                ExpenditureTransaction et = new ExpenditureTransaction();
                try {
                    et = (ExpenditureTransaction) expenditureTransactions.next();
                    reportExpendTranRetrieved++;

                    final KualiDecimal transactionAmount = et.getAccountObjectDirectCostAmount();
                    KualiDecimal distributionAmount = KualiDecimal.ZERO;

                    if (shouldIgnoreExpenditureTransaction(et)) {
                        // Delete expenditure record
                        expenditureTransactionDao.delete(et);
                        reportExpendTranDeleted++;
                        continue;
                    }

                    IndirectCostRecoveryGenerationMetadata icrGenerationMetadata =
                            retrieveSubAccountIndirectCostRecoveryMetadata(et);
                    if (icrGenerationMetadata == null) {
                        // ICR information was not set up properly for sub-account, default to using ICR information
                        // from the account
                        icrGenerationMetadata = retrieveAccountIndirectCostRecoveryMetadata(et);
                    }

                    // Note: This returns debits first, then credits since the logic below only works (assigns debits
                    // and credits correctly and handles remainders) when debits are processed first, followed by
                    // credits.
                    final Collection<IndirectCostRecoveryRateDetail> automatedEntries = indirectCostRecoveryRateDetailDao
                            .getActiveRateDetailsByRate(et.getUniversityFiscalYear(),
                                    icrGenerationMetadata.getFinancialIcrSeriesIdentifier());
                    final int automatedEntriesCount = automatedEntries.size();

                    if (automatedEntriesCount > 0) {
                        for (final Iterator icrIter = automatedEntries.iterator(); icrIter.hasNext(); ) {
                            final IndirectCostRecoveryRateDetail icrEntry = (IndirectCostRecoveryRateDetail) icrIter.next();
                            KualiDecimal generatedTransactionAmount = null;

                            if (!icrIter.hasNext()) {
                                generatedTransactionAmount = distributionAmount;

                                // Log differences that are over WARNING_MAX_DIFFERENCE
                                if (getPercentage(transactionAmount, icrEntry.getAwardIndrCostRcvyRatePct()).subtract(
                                        distributionAmount).abs().isGreaterThan(WARNING_MAX_DIFFERENCE)) {
                                    final List<Message> warnings = new ArrayList<>();
                                    warnings.add(new Message("ADJUSTMENT GREATER THAN " + WARNING_MAX_DIFFERENCE,
                                            Message.TYPE_WARNING));
                                    reportWriterService.writeError(et, warnings);
                                }
                            } else if (icrEntry.getTransactionDebitIndicator().equals(KFSConstants.GL_DEBIT_CODE)) {
                                generatedTransactionAmount = getPercentage(transactionAmount,
                                        icrEntry.getAwardIndrCostRcvyRatePct());
                                distributionAmount = distributionAmount.add(generatedTransactionAmount);
                            } else if (icrEntry.getTransactionDebitIndicator().equals(KFSConstants.GL_CREDIT_CODE)) {
                                generatedTransactionAmount = getPercentage(transactionAmount,
                                        icrEntry.getAwardIndrCostRcvyRatePct());
                                distributionAmount = distributionAmount.subtract(generatedTransactionAmount);
                            } else {
                                // Log if D / C code not found
                                final List<Message> warnings = new ArrayList<>();
                                warnings.add(new Message("DEBIT OR CREDIT CODE NOT FOUND", Message.TYPE_FATAL));
                                reportWriterService.writeError(et, warnings);
                            }

                            generateTransactionsBySymbol(et, icrEntry, generatedTransactionAmount, runDate,
                                    OUTPUT_GLE_FILE_ps, icrGenerationMetadata);

                            reportOriginEntryGenerated = reportOriginEntryGenerated + 2;
                        }
                    }
                    // Delete expenditure record
                    expenditureTransactionDao.delete(et);
                    reportExpendTranDeleted++;

                } catch (final RuntimeException re) {
                    LOG.error("generateIcrTransactions Stopped: {}", re::getMessage);
                    throw new RuntimeException("generateIcrTransactions Stopped: " + re.getMessage(), re);
                } catch (final Exception e) {
                    final List<Message> errorList = new ArrayList<>();
                    errorList.add(new Message(e.toString() + " occurred for this record.", Message.TYPE_FATAL));
                    reportWriterService.writeError(et, errorList);
                }
            }
            OUTPUT_GLE_FILE_ps.close();
            reportWriterService.writeStatisticLine("GLEX RECORDS READ               (GL_EXPEND_TRN_MT) %,9d",
                    reportExpendTranRetrieved);
            reportWriterService.writeStatisticLine("GLEX RECORDS DELETED            (GL_EXPEND_TRN_MT) %,9d",
                    reportExpendTranDeleted);
            reportWriterService.writeStatisticLine("GLEX RECORDS KEPT DUE TO ERRORS (GL_EXPEND_TRN_MT) %,9d",
                    reportExpendTranKept);
            reportWriterService.writeStatisticLine("TRANSACTIONS GENERATED                            %,9d",
                    reportOriginEntryGenerated);
        } catch (final FileNotFoundException e) {
            throw new RuntimeException("generateIcrTransactions Stopped: " + e.getMessage(), e);
        }
    }

    /**
     * Wrapper function to allow for internal iterations on ICR account distribution collection if determined to use
     * ICR from account
     *
     * @param et
     * @param icrRateDetail
     * @param generatedTransactionAmount
     * @param runDate
     * @param group
     * @param icrGenerationMetadata
     */
    private void generateTransactionsBySymbol(
            final ExpenditureTransaction et, final IndirectCostRecoveryRateDetail icrRateDetail,
            final KualiDecimal generatedTransactionAmount, final Date runDate, final PrintStream group,
            final IndirectCostRecoveryGenerationMetadata icrGenerationMetadata) {
        KualiDecimal icrTransactionAmount;
        KualiDecimal unappliedTransactionAmount = new KualiDecimal(generatedTransactionAmount.bigDecimalValue());

        //if symbol is denoted to use ICR from account
        if (GeneralLedgerConstants.PosterService.SYMBOL_USE_ICR_FROM_ACCOUNT.equals(icrRateDetail.getAccountNumber())) {
            int icrCount = icrGenerationMetadata.getAccountLists().size();

            for (final IndirectCostRecoveryAccountDistributionMetadata meta : icrGenerationMetadata.getAccountLists()) {
                //set a new icr meta data for transaction processing
                final IndirectCostRecoveryGenerationMetadata icrMeta = new IndirectCostRecoveryGenerationMetadata(
                        icrGenerationMetadata.getIndirectCostRecoveryTypeCode(),
                        icrGenerationMetadata.getFinancialIcrSeriesIdentifier());
                icrMeta.setIndirectCostRcvyFinCoaCode(meta.getIndirectCostRecoveryFinCoaCode());
                icrMeta.setIndirectCostRecoveryAcctNbr(meta.getIndirectCostRecoveryAccountNumber());

                //change the transaction amount base on ICR percentage
                if (icrCount-- == 1) {
                    // Deplete the rest of un-applied transaction amount
                    icrTransactionAmount = unappliedTransactionAmount;
                } else {
                    // Normal transaction amount is calculated by icr account line percentage
                    icrTransactionAmount = getPercentage(generatedTransactionAmount, meta.getAccountLinePercent());
                    unappliedTransactionAmount = unappliedTransactionAmount.subtract(icrTransactionAmount);
                }

                //perform the actual transaction generation
                generateTransactions(et, icrRateDetail, icrTransactionAmount, runDate, group, icrMeta);
            }
        } else {
            //non-ICR; process as usual
            generateTransactions(et, icrRateDetail, generatedTransactionAmount, runDate, group, icrGenerationMetadata);
        }
    }

    /**
     * Generate a transfer transaction and an offset transaction
     *
     * @param et                         an expenditure transaction
     * @param generatedTransactionAmount the amount of the transaction
     * @param runDate                    the transaction date for the newly created origin entry
     * @param group                      the group to save the origin entry to
     */
    protected void generateTransactions(
            final ExpenditureTransaction et, final IndirectCostRecoveryRateDetail icrRateDetail,
            final KualiDecimal generatedTransactionAmount, final Date runDate, final PrintStream group,
            final IndirectCostRecoveryGenerationMetadata icrGenerationMetadata) {
        BigDecimal pct = new BigDecimal(icrRateDetail.getAwardIndrCostRcvyRatePct().toString());
        pct = pct.divide(BDONEHUNDRED);

        OriginEntryFull e = new OriginEntryFull();
        e.setTransactionLedgerEntrySequenceNumber(0);

        // SYMBOL_USE_EXPENDITURE_ENTRY means we use the field from the expenditure entry, SYMBOL_USE_IRC_FROM_ACCOUNT
        // means we use the ICR field from the account record, otherwise, use the field in the icrRateDetail
        if (GeneralLedgerConstants.PosterService.SYMBOL_USE_EXPENDITURE_ENTRY.equals(
                    icrRateDetail.getFinancialObjectCode())
                || GeneralLedgerConstants.PosterService.SYMBOL_USE_ICR_FROM_ACCOUNT.equals(
                        icrRateDetail.getFinancialObjectCode())) {
            e.setFinancialObjectCode(et.getObjectCode());
            e.setFinancialSubObjectCode(et.getSubObjectCode());
        } else {
            e.setFinancialObjectCode(icrRateDetail.getFinancialObjectCode());
            e.setFinancialSubObjectCode(icrRateDetail.getFinancialSubObjectCode());
        }

        if (GeneralLedgerConstants.PosterService.SYMBOL_USE_EXPENDITURE_ENTRY.equals(
                icrRateDetail.getAccountNumber())) {
            e.setAccountNumber(et.getAccountNumber());
            e.setChartOfAccountsCode(et.getChartOfAccountsCode());
            e.setSubAccountNumber(et.getSubAccountNumber());
        } else if (GeneralLedgerConstants.PosterService.SYMBOL_USE_ICR_FROM_ACCOUNT.equals(
                icrRateDetail.getAccountNumber())) {
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
        if (et.getAccountNumber().equals(e.getAccountNumber())
                && et.getChartOfAccountsCode().equals(e.getChartOfAccountsCode())
                && et.getSubAccountNumber().equals(e.getSubAccountNumber())
                && et.getObjectCode().equals(e.getFinancialObjectCode())
                && et.getSubObjectCode().equals(e.getFinancialSubObjectCode())) {
            final List<Message> warnings = new ArrayList<>();
            warnings.add(new Message("Infinite recursive encumbrance error " + et.getChartOfAccountsCode() + " " +
                    et.getAccountNumber() + " " + et.getSubAccountNumber() + " " + et.getObjectCode() + " " +
                    et.getSubObjectCode(), Message.TYPE_WARNING));
            reportWriterService.writeError(et, warnings);
            return;
        }

        e.setFinancialDocumentTypeCode(parameterService.getParameterValueAsString(
                PosterIcrGenerationStep.class, DOCUMENT_TYPE));
        e.setFinancialSystemOriginationCode(parameterService.getParameterValueAsString(
                KfsParameterConstants.GENERAL_LEDGER_BATCH.class, GLParameterConstants.GL_ORIGINATION_CODE));
        final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_STRING, Locale.US);
        e.setDocumentNumber(sdf.format(runDate));
        if (KFSConstants.GL_DEBIT_CODE.equals(icrRateDetail.getTransactionDebitIndicator())) {
            e.setTransactionLedgerEntryDescription(getChargeDescription(pct, et.getObjectCode(),
                    icrGenerationMetadata.getIndirectCostRecoveryTypeCode(),
                    et.getAccountObjectDirectCostAmount().abs()));
        } else {
            e.setTransactionLedgerEntryDescription(getOffsetDescription(pct,
                    et.getAccountObjectDirectCostAmount().abs(), et.getChartOfAccountsCode(), et.getAccountNumber()));
        }
        e.setTransactionDate(new java.sql.Date(runDate.getTime()));
        e.setTransactionDebitCreditCode(icrRateDetail.getTransactionDebitIndicator());
        e.setFinancialBalanceTypeCode(et.getBalanceTypeCode());
        e.setUniversityFiscalYear(et.getUniversityFiscalYear());
        e.setUniversityFiscalPeriodCode(et.getUniversityFiscalAccountingPeriod());

        final ObjectCode oc = objectCodeService.getByPrimaryId(e.getUniversityFiscalYear(), e.getChartOfAccountsCode(),
                e.getFinancialObjectCode());
        if (oc == null) {
            LOG.warn(
                    "{}{},{},{}",
                    () -> configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_OBJECT_CODE_NOT_FOUND_FOR),
                    e::getUniversityFiscalYear,
                    e::getChartOfAccountsCode,
                    e::getFinancialObjectCode
            );
            // this will be written out the ICR file. Then, when that file attempts to post, the transaction won't
            // validate and will end up in the icr error file
            e.setFinancialObjectCode(icrRateDetail.getFinancialObjectCode());
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

        if (et.getBalanceTypeCode().equals(et.getOption().getExtrnlEncumFinBalanceTypCd())
                || et.getBalanceTypeCode().equals(et.getOption().getIntrnlEncumFinBalanceTypCd())
                || et.getBalanceTypeCode().equals(et.getOption().getPreencumbranceFinBalTypeCd())
                || et.getBalanceTypeCode().equals(et.getOption().getCostShareEncumbranceBalanceTypeCd())) {
            e.setDocumentNumber(parameterService.getParameterValueAsString(PosterIcrGenerationStep.class,
                    DOCUMENT_TYPE
            ));
        }
        e.setProjectCode(et.getProjectCode());
        if (GeneralLedgerConstants.getDashOrganizationReferenceId().equals(et.getOrganizationReferenceId())) {
            e.setOrganizationReferenceId(null);
        } else {
            e.setOrganizationReferenceId(et.getOrganizationReferenceId());
        }

        try {
            createOutputEntry(e, group);
        } catch (final IOException ioe) {
            LOG.error("generateTransactions Stopped: {}", ioe::getMessage);
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

        final String offsetBalanceSheetObjectCodeNumber = determineIcrOffsetBalanceSheetObjectCodeNumber(e, et,
                icrRateDetail);
        e.setFinancialObjectCode(offsetBalanceSheetObjectCodeNumber);
        final ObjectCode balSheetObjectCode = objectCodeService.getByPrimaryId(icrRateDetail.getUniversityFiscalYear(),
                e.getChartOfAccountsCode(), offsetBalanceSheetObjectCodeNumber);
        if (balSheetObjectCode == null) {
            final List<Message> warnings = new ArrayList<>();
            warnings.add(new Message(configurationService
                    .getPropertyValueAsString(KFSKeyConstants.ERROR_INVALID_OFFSET_OBJECT_CODE) + icrRateDetail
                    .getUniversityFiscalYear() + "-" + e
                    .getChartOfAccountsCode() + "-" + offsetBalanceSheetObjectCodeNumber, Message.TYPE_WARNING));
            reportWriterService.writeError(et, warnings);

        } else {
            e.setFinancialObjectTypeCode(balSheetObjectCode.getFinancialObjectTypeCode());
        }

        if (KFSConstants.GL_DEBIT_CODE.equals(icrRateDetail.getTransactionDebitIndicator())) {
            e.setTransactionLedgerEntryDescription(getChargeDescription(pct, et.getObjectCode(),
                    icrGenerationMetadata.getIndirectCostRecoveryTypeCode(),
                    et.getAccountObjectDirectCostAmount().abs()));
        } else {
            e.setTransactionLedgerEntryDescription(getOffsetDescription(pct,
                    et.getAccountObjectDirectCostAmount().abs(),
                    et.getChartOfAccountsCode(), et.getAccountNumber()));
        }

        try {
            flexibleOffsetAccountService.updateOffset(e);
        } catch (final InvalidFlexibleOffsetException ex) {
            final List<Message> warnings = new ArrayList<>();
            warnings.add(new Message("FAILED TO GENERATE FLEXIBLE OFFSETS " + ex.getMessage(),
                    Message.TYPE_WARNING));
            reportWriterService.writeError(et, warnings);
            LOG.warn("FAILED TO GENERATE FLEXIBLE OFFSETS FOR EXPENDITURE TRANSACTION {}", et, ex);
        }

        try {
            createOutputEntry(e, group);
        } catch (final IOException ioe) {
            LOG.error("generateTransactions Stopped: {}", ioe::getMessage);
            throw new RuntimeException("generateTransactions Stopped: " + ioe.getMessage(), ioe);
        }
    }

    /**
     * Returns ICR Generation Metadata based on SubAccount information if the SubAccount on the expenditure
     * transaction is properly set up for ICR
     *
     * @param et
     * @return null if the ET does not have a SubAccount properly set up for ICR
     */
    protected IndirectCostRecoveryGenerationMetadata retrieveSubAccountIndirectCostRecoveryMetadata(
            final ExpenditureTransaction et) {
        final SubAccount subAccount = accountingCycleCachingService.getSubAccount(et.getChartOfAccountsCode(),
                et.getAccountNumber(), et.getSubAccountNumber());
        if (ObjectUtils.isNotNull(subAccount)) {
            subAccount.setA21SubAccount(accountingCycleCachingService.getA21SubAccount(et.getChartOfAccountsCode(),
                    et.getAccountNumber(), et.getSubAccountNumber()));
        }

        if (ObjectUtils.isNotNull(subAccount) && ObjectUtils.isNotNull(subAccount.getA21SubAccount())) {
            final A21SubAccount a21SubAccount = subAccount.getA21SubAccount();
            final List<A21IndirectCostRecoveryAccount> activeICRAccounts =
                    a21SubAccount.getA21ActiveIndirectCostRecoveryAccounts();

            if (StringUtils.isBlank(a21SubAccount.getIndirectCostRecoveryTypeCode())
                    && StringUtils.isBlank(a21SubAccount.getFinancialIcrSeriesIdentifier())
                    && activeICRAccounts.isEmpty()) {
                // all ICR fields were blank, therefore, this sub account was not set up for ICR
                return null;
            }
            // refresh the indirect cost recovery account, accounting cycle style!
            Account refreshSubAccount = null;
            if (StringUtils.isNotBlank(a21SubAccount.getChartOfAccountsCode())
                    && StringUtils.isNotBlank(a21SubAccount.getAccountNumber())) {
                refreshSubAccount = accountingCycleCachingService.getAccount(a21SubAccount.getChartOfAccountsCode(),
                        a21SubAccount.getAccountNumber());
            }

            // these fields will be used to construct warning messages
            final String warningMessagePattern = configurationService.getPropertyValueAsString(
                    KFSKeyConstants.WARNING_ICR_GENERATION_PROBLEM_WITH_A21SUBACCOUNT_FIELD_BLANK_INVALID);
            final String subAccountBOLabel = businessObjectDictionaryService.getBusinessObjectEntry(
                    SubAccount.class.getName()).getObjectLabel();
            final String subAccountValue = subAccount.getChartOfAccountsCode() + "-" + subAccount.getAccountNumber() + "-" +
                                           subAccount.getSubAccountNumber();
            final String accountBOLabel = businessObjectDictionaryService.getBusinessObjectEntry(Account.class.getName())
                    .getObjectLabel();
            final String accountValue = et.getChartOfAccountsCode() + "-" + et.getAccountNumber();

            boolean subAccountOK = true;

            // there were some ICR fields that were filled in, make sure they're all filled in and are valid values
            a21SubAccount.setIndirectCostRecoveryType(accountingCycleCachingService
                    .getIndirectCostRecoveryType(a21SubAccount.getIndirectCostRecoveryTypeCode()));
            if (StringUtils.isBlank(a21SubAccount.getIndirectCostRecoveryTypeCode())
                    || ObjectUtils.isNull(a21SubAccount.getIndirectCostRecoveryType())) {
                final String errorFieldName = dataDictionaryService.getAttributeShortLabel(A21SubAccount.class,
                        KFSPropertyConstants.INDIRECT_COST_RECOVERY_TYPE_CODE);
                final String warningMessage = MessageFormat.format(warningMessagePattern, errorFieldName, subAccountBOLabel,
                        subAccountValue, accountBOLabel, accountValue);
                reportWriterService.writeError(et, new Message(warningMessage, Message.TYPE_WARNING));
                subAccountOK = false;
            }

            if (StringUtils.isBlank(a21SubAccount.getFinancialIcrSeriesIdentifier())) {
                final Map<String, Object> icrRatePkMap = new HashMap<>();
                icrRatePkMap.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, et.getUniversityFiscalYear());
                icrRatePkMap.put(KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER,
                        a21SubAccount.getFinancialIcrSeriesIdentifier());
                final IndirectCostRecoveryRate indirectCostRecoveryRate = businessObjectService.findByPrimaryKey(
                        IndirectCostRecoveryRate.class, icrRatePkMap);
                if (indirectCostRecoveryRate == null) {
                    final String errorFieldName = dataDictionaryService.getAttributeShortLabel(A21SubAccount.class,
                            KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER);
                    final String warningMessage = MessageFormat.format(warningMessagePattern, errorFieldName,
                            subAccountBOLabel, subAccountValue, accountBOLabel, accountValue);
                    reportWriterService.writeError(et, new Message(warningMessage, Message.TYPE_WARNING));
                    subAccountOK = false;
                }
            }

            if (activeICRAccounts.isEmpty() || ObjectUtils.isNull(refreshSubAccount)) {
                final String errorFieldName = dataDictionaryService.getAttributeShortLabel(
                        A21IndirectCostRecoveryAccount.class,
                        KFSPropertyConstants.ICR_CHART_OF_ACCOUNTS_CODE) + "/" + dataDictionaryService
                        .getAttributeShortLabel(A21IndirectCostRecoveryAccount.class,
                                KFSPropertyConstants.ICR_ACCOUNT_NUMBER);
                final String warningMessage = MessageFormat.format(warningMessagePattern, errorFieldName, subAccountBOLabel,
                        subAccountValue, accountBOLabel, accountValue);
                reportWriterService.writeError(et, new Message(warningMessage, Message.TYPE_WARNING));
                subAccountOK = false;
            }

            if (subAccountOK) {
                final IndirectCostRecoveryGenerationMetadata metadata = new IndirectCostRecoveryGenerationMetadata(
                        a21SubAccount.getIndirectCostRecoveryTypeCode(),
                    a21SubAccount.getFinancialIcrSeriesIdentifier());

                final List<IndirectCostRecoveryAccountDistributionMetadata> icrAccountList = metadata.getAccountLists();
                for (final A21IndirectCostRecoveryAccount a21 : activeICRAccounts) {
                    icrAccountList.add(new IndirectCostRecoveryAccountDistributionMetadata(a21));
                }
                return metadata;
            }
        }
        return null;
    }

    protected IndirectCostRecoveryGenerationMetadata retrieveAccountIndirectCostRecoveryMetadata(
            final ExpenditureTransaction et) {
        final Account account = et.getAccount();

        final IndirectCostRecoveryGenerationMetadata metadata = new IndirectCostRecoveryGenerationMetadata(
                account.getAcctIndirectCostRcvyTypeCd(),
            account.getFinancialIcrSeriesIdentifier());

        final List<IndirectCostRecoveryAccountDistributionMetadata> icrAccountList = metadata.getAccountLists();
        for (final IndirectCostRecoveryAccount icr : account.getActiveIndirectCostRecoveryAccounts()) {
            icrAccountList.add(new IndirectCostRecoveryAccountDistributionMetadata(icr));
        }

        return metadata;
    }

    /**
     * Generates a percent of a KualiDecimal amount (great for finding out how much of an origin entry should be
     * recouped by indirect cost recovery)
     *
     * @param amount  the original amount
     * @param percent the percentage of that amount to calculate
     * @return the percent of the amount
     */
    protected KualiDecimal getPercentage(final KualiDecimal amount, final BigDecimal percent) {
        final BigDecimal result = amount.bigDecimalValue().multiply(percent).divide(BDONEHUNDRED, 2,
                RoundingMode.HALF_UP);
        return new KualiDecimal(result);
    }

    /**
     * Generates the description of a charge
     *
     * @param rate       the ICR rate for this entry
     * @param objectCode the object code of this entry
     * @param type       the ICR type code of this entry's account
     * @param amount     the amount of this entry
     * @return a description for the charge entry
     */
    protected String getChargeDescription(final BigDecimal rate, final String objectCode, final String type, final KualiDecimal amount) {
        final BigDecimal newRate = rate.multiply(PosterServiceImpl.BDONEHUNDRED);

        final StringBuffer desc = new StringBuffer("CHG ");
        if (newRate.doubleValue() < 10) {
            desc.append(" ");
        }
        desc.append(DFPCT.format(newRate));
        desc.append("% ON ");
        desc.append(objectCode);
        desc.append(" (");
        desc.append(type);
        desc.append(")  ");
        String amt = DFAMT.format(amount);
        while (amt.length() < 13) {
            amt = " " + amt;
        }
        desc.append(amt);
        return desc.toString();
    }

    /**
     * Returns the description of a debit origin entry created by generateTransactions
     *
     * @param rate                the ICR rate that relates to this entry
     * @param amount              the amount of this entry
     * @param chartOfAccountsCode the chart codce of the debit entry
     * @param accountNumber       the account number of the debit entry
     * @return a description for the debit entry
     */
    protected String getOffsetDescription(
            final BigDecimal rate, final KualiDecimal amount, final String chartOfAccountsCode,
            final String accountNumber) {
        final BigDecimal newRate = rate.multiply(PosterServiceImpl.BDONEHUNDRED);

        final StringBuffer desc = new StringBuffer("RCV ");
        if (newRate.doubleValue() < 10) {
            desc.append(" ");
        }
        desc.append(DFPCT.format(newRate));
        desc.append("% ON ");
        String amt = DFAMT.format(amount);
        while (amt.length() < 13) {
            amt = " " + amt;
        }
        desc.append(amt);
        desc.append(" FRM ");
        desc.append(accountNumber);
        return desc.toString();
    }

    /**
     * Increments a named count holding statistics about posted transactions
     *
     * @param reporting   a Map of counts generated by this process
     * @param destination the destination of a given transaction
     * @param operation   the operation being performed on the transaction
     */
    protected void addReporting(final Map<String, Integer> reporting, final String destination, final String operation) {
        final String key = destination + "," + operation;
        //TODO: remove this if block. Added to troubleshoot FSKD-194.
        if ("GL_EXPEND_TRN_MT".equals(destination)) {
            LOG.info("Counting GLEX operation: {}", operation);
        }
        if (reporting.containsKey(key)) {
            final Integer c = reporting.get(key);
            reporting.put(key, c + 1);
        } else {
            reporting.put(key, 1);
        }
    }

    protected String determineIcrOffsetBalanceSheetObjectCodeNumber(
            final OriginEntryInformation offsetEntry,
            final ExpenditureTransaction et, final IndirectCostRecoveryRateDetail icrRateDetail) {
        final String icrEntryDocumentType = parameterService.getParameterValueAsString(
                PosterIcrGenerationStep.class, DOCUMENT_TYPE);
        final OffsetDefinition offsetDefinition = offsetDefinitionService.getByPrimaryId(
                offsetEntry.getUniversityFiscalYear(), offsetEntry.getChartOfAccountsCode(), icrEntryDocumentType,
                et.getBalanceTypeCode());
        if (ObjectUtils.isNotNull(offsetDefinition)) {
            return offsetDefinition.getFinancialObjectCode();
        } else {
            return null;
        }
    }

    /**
     * The purpose of this method is to build and post the offset transaction. It does this by performing the
     * following steps:
     * 1. Getting the offset object code from the GL Offset Definition Table.
     * 2. For the offset object code it needs to get the associated object type.
     * 3. Flip the transaction debit credit code
     * 4. Post the offset transaction.
     *
     * @param tran                a transaction to post
     * @param mode                the mode the poster is running in
     * @param reportSummary       a Map of summary counts generated by the posting process
     * @param ledgerSummaryReport for summary reporting
     * @param invalidGroup        the group to save invalid entries to
     * @param runUniversityDate   the university date of this poster run
     * @param line                the input line used for printing
     * @return true if an offset would be needed for this entry, false otherwise
     */
    protected boolean generateOffset(
            final OriginEntryFull tran, final int mode, final Map<String, Integer> reportSummary,
            final LedgerSummaryReport ledgerSummaryReport, final PrintStream invalidGroup, final UniversityDate runUniversityDate,
            String line, final PrintStream OUTPUT_GLE_FILE_ps) {
        LOG.debug("generateOffset() started");

        final List<Message> errors = new ArrayList<>();
        final OriginEntryFull offsetEntry = new OriginEntryFull(tran);

        final String offsetDescription = configurationService.getPropertyValueAsString(KFSKeyConstants.MSG_GENERATED_OFFSET);
        offsetEntry.setTransactionLedgerEntryDescription(offsetDescription);

        final OffsetDefinition offsetDefinition = offsetDefinitionService.getByPrimaryId(offsetEntry.getUniversityFiscalYear(),
                offsetEntry.getChartOfAccountsCode(), offsetEntry.getFinancialDocumentTypeCode(),
                offsetEntry.getFinancialBalanceTypeCode());
        if (ObjectUtils.isNotNull(offsetDefinition)) {
            if (offsetDefinition.getFinancialObject() == null) {
                errors.add(new Message(configurationService.getPropertyValueAsString(
                        KFSKeyConstants.ERROR_OFFSET_DEFINITION_OBJECT_CODE_NOT_FOUND), Message.TYPE_WARNING));
            }
            offsetEntry.setFinancialObject(offsetDefinition.getFinancialObject());
            offsetEntry.setFinancialObjectCode(offsetDefinition.getFinancialObjectCode());
            offsetEntry.setFinancialSubObject(null);
            offsetEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            offsetEntry.setFinancialObjectTypeCode(offsetEntry.getFinancialObject().getFinancialObjectTypeCode());
        } else {
            errors.add(new Message(configurationService.getPropertyValueAsString(
                    KFSKeyConstants.ERROR_OFFSET_DEFINITION_NOT_FOUND), Message.TYPE_WARNING));
        }

        if (KFSConstants.GL_DEBIT_CODE.equals(offsetEntry.getTransactionDebitCreditCode())) {
            offsetEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
        } else {
            offsetEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
        }

        try {
            flexibleOffsetAccountService.updateOffset(offsetEntry);
        } catch (final InvalidFlexibleOffsetException e) {
            LOG.debug("generateOffset() Offset Flexible Offset Error: {}", e::getMessage);
            errors.add(new Message("FAILED TO GENERATE FLEXIBLE OFFSETS " + e.getMessage(), Message.TYPE_WARNING));
        }

        if (errors.size() > 0) {
            reportWriterService.writeError(offsetEntry, errors);
            addReporting(reportSummary, "WARNING", GeneralLedgerConstants.INSERT_CODE);
        }

        //update input record from offset entry for error reporting
        line = offsetEntry.getLine();
        postTransaction(offsetEntry, mode, reportSummary, ledgerSummaryReport, invalidGroup, runUniversityDate, line,
                OUTPUT_GLE_FILE_ps);

        return true;
    }

    protected void createOutputEntry(final Transaction entry, final PrintStream group) throws IOException {
        final OriginEntryFull oef = new OriginEntryFull();
        oef.copyFieldsFromTransaction(entry);
        try {
            group.printf("%s\n", oef.getLine());
        } catch (final Exception e) {
            throw new IOException(e.toString());
        }
    }

    protected void writeErrorEntry(final String line, final PrintStream invalidGroup) throws IOException {
        try {
            invalidGroup.printf("%s\n", line);
        } catch (final Exception e) {
            throw new IOException(e.toString());
        }
    }

    protected boolean shouldIgnoreExpenditureTransaction(final ExpenditureTransaction et) {
        if (ObjectUtils.isNotNull(et.getOption())) {
            final SystemOptions options = et.getOption();
            return StringUtils.isNotBlank(options.getActualFinancialBalanceTypeCd())
                    && !options.getActualFinancialBalanceTypeCd().equals(et.getBalanceTypeCode());
        }
        return true;
    }

    public void setVerifyTransaction(final VerifyTransaction vt) {
        verifyTransaction = vt;
    }

    public void setTransactionPosters(final List p) {
        transactionPosters = p;
    }

    @Override
    public void setDateTimeService(final DateTimeService dts) {
        dateTimeService = dts;
    }

    public void setReversalDao(final ReversalDao red) {
        reversalDao = red;
    }

    public void setAccountingPeriodService(final AccountingPeriodService aps) {
        accountingPeriodService = aps;
    }

    public void setExpenditureTransactionDao(final ExpenditureTransactionDao etd) {
        expenditureTransactionDao = etd;
    }

    public void setIndirectCostRecoveryRateDetailDao(final IndirectCostRecoveryRateDetailDao iaed) {
        indirectCostRecoveryRateDetailDao = iaed;
    }

    public void setObjectCodeService(final ObjectCodeService ocs) {
        objectCodeService = ocs;
    }

    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setFlexibleOffsetAccountService(final FlexibleOffsetAccountService flexibleOffsetAccountService) {
        this.flexibleOffsetAccountService = flexibleOffsetAccountService;
    }

    public void setRunDateService(final RunDateService runDateService) {
        this.runDateService = runDateService;
    }

    public void setAccountingCycleCachingService(final AccountingCycleCachingService accountingCycleCachingService) {
        this.accountingCycleCachingService = accountingCycleCachingService;
    }

    public void setOffsetDefinitionService(final OffsetDefinitionService offsetDefinitionService) {
        this.offsetDefinitionService = offsetDefinitionService;
    }

    public void setBusinessObjectDictionaryService(
            final BusinessObjectDictionaryService businessObjectDictionaryService) {
        this.businessObjectDictionaryService = businessObjectDictionaryService;
    }

    public void setDataDictionaryService(final DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setBatchFileDirectoryName(final String batchFileDirectoryName) {
        this.batchFileDirectoryName = batchFileDirectoryName;
    }

    public void setPersistenceStructureService(final PersistenceStructureService persistenceStructureService) {
        this.persistenceStructureService = persistenceStructureService;
    }

    public void setReportWriterService(final ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }

    public void setErrorListingReportWriterService(final ReportWriterService errorListingReportWriterService) {
        this.errorListingReportWriterService = errorListingReportWriterService;
    }

    public void setReversalReportWriterService(final ReportWriterService reversalReportWriterService) {
        this.reversalReportWriterService = reversalReportWriterService;
    }

    public void setLedgerSummaryReportWriterService(final ReportWriterService ledgerSummaryReportWriterService) {
        this.ledgerSummaryReportWriterService = ledgerSummaryReportWriterService;
    }

    public void setPersistenceService(final PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }
}
