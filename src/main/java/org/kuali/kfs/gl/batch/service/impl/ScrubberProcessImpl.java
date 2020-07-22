/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.BalanceType;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.OffsetDefinition;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.gl.GLParameterConstants;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.batch.BatchSortUtil;
import org.kuali.kfs.gl.batch.CollectorBatch;
import org.kuali.kfs.gl.batch.DemergerSortComparator;
import org.kuali.kfs.gl.batch.ScrubberSortComparator;
import org.kuali.kfs.gl.batch.ScrubberStep;
import org.kuali.kfs.gl.batch.service.AccountingCycleCachingService;
import org.kuali.kfs.gl.batch.service.RunDateService;
import org.kuali.kfs.gl.batch.service.ScrubberProcess;
import org.kuali.kfs.gl.batch.service.impl.FilteringOriginEntryFileIterator.OriginEntryFilter;
import org.kuali.kfs.gl.businessobject.DemergerReportData;
import org.kuali.kfs.gl.businessobject.OriginEntryFieldUtil;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.gl.businessobject.OriginEntryInformation;
import org.kuali.kfs.gl.businessobject.ScrubberProcessTransactionError;
import org.kuali.kfs.gl.businessobject.ScrubberProcessUnitOfWork;
import org.kuali.kfs.gl.businessobject.Transaction;
import org.kuali.kfs.gl.report.CollectorReportData;
import org.kuali.kfs.gl.report.LedgerSummaryReport;
import org.kuali.kfs.gl.report.PreScrubberReport;
import org.kuali.kfs.gl.report.PreScrubberReportData;
import org.kuali.kfs.gl.report.TransactionListingReport;
import org.kuali.kfs.gl.service.PreScrubberService;
import org.kuali.kfs.gl.service.ScrubberReportData;
import org.kuali.kfs.gl.service.ScrubberValidator;
import org.kuali.kfs.gl.service.impl.ScrubberStatus;
import org.kuali.kfs.gl.service.impl.StringHelper;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.PersistenceService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.Message;
import org.kuali.kfs.sys.batch.service.WrappingBatchService;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.kuali.kfs.sys.exception.InvalidFlexibleOffsetException;
import org.kuali.kfs.sys.service.DocumentNumberAwareReportWriterService;
import org.kuali.kfs.sys.service.FlexibleOffsetAccountService;
import org.kuali.kfs.sys.service.ReportWriterService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.parameter.ParameterEvaluator;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Date;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class has the logic for the scrubber. It is required because the scrubber process needs instance variables.
 * Instance variables in a spring service are shared between all code calling the service. This will make sure each
 * run of the scrubber has it's own instance variables instead of being shared.
 */
public class ScrubberProcessImpl implements ScrubberProcess {

    private static final Logger LOG = LogManager.getLogger();
    private static final String CAPITALIZATION_CHART_CODES = "CAPITALIZATION_CHARTS";
    private static final String CAPITALIZATION_DOC_TYPE_CODES = "CAPITALIZATION_DOCUMENT_TYPES";
    private static final String CAPITALIZATION_FISCAL_PERIOD_CODES = "CAPITALIZATION_FISCAL_PERIODS";
    private static final String CAPITALIZATION_IND = "CAPITALIZATION_IND";
    private static final String CAPITALIZATION_OBJ_SUB_TYPE_CODES = "CAPITALIZATION_OBJECT_SUB_TYPES";
    private static final String CAPITALIZATION_OFFSET_CODE = "CAPITALIZATION_OFFSET_CODE";
    private static final String CAPITALIZATION_SUBTYPE_OBJECT = "CAPITALIZATION_OBJECT_CODE_BY_OBJECT_SUB_TYPE";
    private static final String CAPITALIZATION_SUB_FUND_GROUP_CODES = "CAPITALIZATION_SUB_FUND_GROUPS";
    private static final String COST_SHARE_ENC_DOC_TYPE_CODES = "COST_SHARE_DOCUMENT_TYPES";
    private static final String COST_SHARE_ENC_BAL_TYP_CODES = "COST_SHARE_ENCUMBRANCE_BALANCE_TYPES";
    private static final String COST_SHARE_ENC_FISCAL_PERIOD_CODES = "COST_SHARE_ENCUMBRANCE_FISCAL_PERIODS";
    private static final String COST_SHARE_FISCAL_PERIOD_CODES = "COST_SHARE_FISCAL_PERIODS";
    private static final String COST_SHARE_OBJ_TYPE_CODES = "COST_SHARE_OBJECT_TYPES";
    private static final String LIABILITY_CHART_CODES = "LIABILITY_CHARTS";
    private static final String LIABILITY_DOC_TYPE_CODES = "LIABILITY_DOCUMENT_TYPES";
    private static final String LIABILITY_FISCAL_PERIOD_CODES = "LIABILITY_FISCAL_PERIODS";
    private static final String LIABILITY_IND = "LIABILITY_IND";
    private static final String LIABILITY_OBJ_SUB_TYPE_CODES = "LIABILITY_OBJECT_SUB_TYPES";
    private static final String LIABILITY_OBJECT_CODE = "LIABILITY_OBJECT_CODE";
    private static final String LIABILITY_OFFSET_CODE = "LIABILITY_OFFSET_CODE";
    private static final String LIABILITY_SUB_FUND_GROUP_CODES = "LIABILITY_SUB_FUND_GROUPS";
    private static final String PLANT_FUND_CAMPUS_OBJECT_SUB_TYPE_CODES = "PLANT_FUND_CAMPUS_OBJECT_SUB_TYPES";
    private static final String PLANT_FUND_ORG_OBJECT_SUB_TYPE_CODES = "PLANT_FUND_ORGANIZATION_OBJECT_SUB_TYPES";
    private static final String PLANT_INDEBTEDNESS_OBJ_SUB_TYPE_CODES = "PLANT_INDEBTEDNESS_OBJECT_SUB_TYPES";
    private static final String PLANT_INDEBTEDNESS_OFFSET_CODE = "PLANT_INDEBTEDNESS_OFFSET_CODE";
    private static final String PLANT_INDEBTEDNESS_SUB_FUND_GROUP_CODES = "PLANT_INDEBTEDNESS_SUB_FUND_GROUPS";
    private static final String TRANSACTION_DATE_BYPASS_ORIGINATIONS = "TRANSACTION_DATE_BYPASS_ORIGINATIONS";

    protected static final String COST_SHARE_CODE = "CSHR";
    protected static final String COST_SHARE_OBJECT_CODE = "COST_SHARE_OBJECT_CODE";
    protected static final String COST_SHARE_TRANSFER_ENTRY_IND = "***";
    protected static final String PLANT_INDEBTEDNESS_IND = "PLANT_INDEBTEDNESS_IND";
    protected static final String TRANSACTION_TYPE_COST_SHARE_ENCUMBRANCE = "CE";
    protected static final String TRANSACTION_TYPE_OFFSET = "O";
    protected static final String TRANSACTION_TYPE_CAPITALIZATION = "C";
    protected static final String TRANSACTION_TYPE_LIABILITY = "L";
    protected static final String TRANSACTION_TYPE_TRANSFER = "T";
    protected static final String TRANSACTION_TYPE_COST_SHARE = "CS";
    protected static final String TRANSACTION_TYPE_OTHER = "X";

    // These lengths are different then database field lengths, hence they are not from the DD
    protected static final int COST_SHARE_ENCUMBRANCE_ENTRY_MAXLENGTH = 28;
    protected static final int DEMERGER_TRANSACTION_LEDGET_ENTRY_DESCRIPTION = 33;
    protected static final int OFFSET_MESSAGE_MAXLENGTH = 33;

    /* Services required */
    protected FlexibleOffsetAccountService flexibleOffsetAccountService;
    protected DateTimeService dateTimeService;
    protected ConfigurationService configurationService;
    protected PersistenceService persistenceService;
    protected ScrubberValidator scrubberValidator;
    protected RunDateService runDateService;
    protected AccountingCycleCachingService accountingCycleCachingService;
    protected DocumentNumberAwareReportWriterService scrubberReportWriterService;
    protected DocumentNumberAwareReportWriterService scrubberLedgerReportWriterService;
    protected DocumentNumberAwareReportWriterService scrubberListingReportWriterService;
    protected DocumentNumberAwareReportWriterService preScrubberReportWriterService;
    protected ReportWriterService scrubberBadBalanceListingReportWriterService;
    protected ReportWriterService demergerRemovedTransactionsListingReportWriterService;
    protected ReportWriterService demergerReportWriterService;
    protected PreScrubberService preScrubberService;

    // these three members will only be populated when in collector mode, otherwise the memory requirements will be huge
    protected Map<OriginEntryInformation, OriginEntryInformation> unscrubbedToScrubbedEntries = new HashMap<>();
    protected Map<Transaction, List<Message>> scrubberReportErrors = new IdentityHashMap<>();
    protected LedgerSummaryReport ledgerSummaryReport = new LedgerSummaryReport();

    protected ScrubberReportData scrubberReport;
    protected DemergerReportData demergerReport;

    /* These are all different forms of the run date for this job */
    protected Date runDate;
    protected Calendar runCal;
    protected UniversityDate universityRunDate;
    protected String offsetString;

    /* Unit Of Work info */
    protected ScrubberProcessUnitOfWork scrubberProcessUnitOfWork;
    protected KualiDecimal scrubCostShareAmount;

    /* Statistics for the reports */
    protected List<Message> transactionErrors;

    /* Description names */
    protected String offsetDescription;
    protected String capitalizationDescription;
    protected String liabilityDescription;
    protected String transferDescription;
    protected String costShareDescription;

    protected ParameterService parameterService;
    protected BusinessObjectService businessObjectService;

    /**
     * Whether this instance is being used to support the scrubbing of a collector batch
     */
    protected boolean collectorMode;
    protected String batchFileDirectoryName;

    protected PrintStream OUTPUT_GLE_FILE_ps;
    protected PrintStream OUTPUT_ERR_FILE_ps;
    protected PrintStream OUTPUT_EXP_FILE_ps;

    protected String inputFile;
    protected String validFile;
    protected String errorFile;
    protected String expiredFile;
    //CU customization change access level from private to protected
    protected ParameterEvaluatorService parameterEvaluatorService;

    /**
     * Scrub this single group read only. This will only output the scrubber report. It won't output any other groups.
     *
     * @param fileName
     * @param documentNumber
     */
    @Override
    public void scrubGroupReportOnly(String fileName, String documentNumber) {
        LOG.debug("scrubGroupReportOnly() started");
        this.inputFile = fileName + ".sort";
        this.validFile = batchFileDirectoryName + File.separator +
                GeneralLedgerConstants.BatchFileSystem.SCRUBBER_VALID_OUTPUT_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION;
        this.errorFile = batchFileDirectoryName + File.separator +
                GeneralLedgerConstants.BatchFileSystem.SCRUBBER_ERROR_OUTPUT_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION;
        this.expiredFile = batchFileDirectoryName + File.separator +
                GeneralLedgerConstants.BatchFileSystem.SCRUBBER_EXPIRED_OUTPUT_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION;
        String prescrubOutput = batchFileDirectoryName + File.separator +
                GeneralLedgerConstants.BatchFileSystem.PRE_SCRUBBER_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION;
        this.ledgerSummaryReport = new LedgerSummaryReport();
        runDate = calculateRunDate(dateTimeService.getCurrentDate());

        PreScrubberReportData preScrubberReportData;

        // run pre-scrubber on the raw input into the sort process
        LineIterator inputEntries = null;
        try {
            inputEntries = FileUtils.lineIterator(new File(fileName));
            preScrubberReportData = preScrubberService.preprocessOriginEntries(inputEntries, prescrubOutput);
        } catch (IOException e1) {
            LOG.error("Error encountered trying to prescrub GLCP/LLCP document", e1);
            throw new RuntimeException("Error encountered trying to prescrub GLCP/LLCP document", e1);
        } finally {
            LineIterator.closeQuietly(inputEntries);
        }
        if (preScrubberReportData != null) {
            preScrubberReportWriterService.setDocumentNumber(documentNumber);
            ((WrappingBatchService) preScrubberReportWriterService).initialize();
            try {
                new PreScrubberReport().generateReport(preScrubberReportData, preScrubberReportWriterService);
            } finally {
                ((WrappingBatchService) preScrubberReportWriterService).destroy();
            }
        }
        BatchSortUtil.sortTextFileWithFields(prescrubOutput, inputFile, new ScrubberSortComparator());

        scrubEntries(true, documentNumber);

        // delete files
        File deleteSortFile = new File(inputFile);
        File deleteValidFile = new File(validFile);
        File deleteErrorFile = new File(errorFile);
        File deleteExpiredFile = new File(expiredFile);
        try {
            deleteSortFile.delete();
            deleteValidFile.delete();
            deleteErrorFile.delete();
            deleteExpiredFile.delete();
        } catch (Exception e) {
            LOG.error("scrubGroupReportOnly delete output files process Stopped: " + e.getMessage());
            throw new RuntimeException("scrubGroupReportOnly delete output files process Stopped: " + e.getMessage(),
                    e);
        }
    }

    /**
     * Scrubs all entries in all groups and documents.
     */
    @Override
    public void scrubEntries() {
        this.inputFile = batchFileDirectoryName + File.separator +
                GeneralLedgerConstants.BatchFileSystem.SCRUBBER_INPUT_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION;
        this.validFile = batchFileDirectoryName + File.separator +
                GeneralLedgerConstants.BatchFileSystem.SCRUBBER_VALID_OUTPUT_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION;
        this.errorFile = batchFileDirectoryName + File.separator +
                GeneralLedgerConstants.BatchFileSystem.SCRUBBER_ERROR_OUTPUT_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION;
        this.expiredFile = batchFileDirectoryName + File.separator +
                GeneralLedgerConstants.BatchFileSystem.SCRUBBER_EXPIRED_OUTPUT_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION;
        runDate = calculateRunDate(dateTimeService.getCurrentDate());

        scrubEntries(false, null);
    }

    /**
     * Scrub all entries that need it in origin entry. Put valid scrubbed entries in a scrubber valid group, put
     * errors in a scrubber error group, and transactions with an expired account in the scrubber expired account
     * group.
     *
     * @param reportOnlyMode
     * @param documentNumber the number of the document with entries to scrub
     */
    @Override
    public void scrubEntries(boolean reportOnlyMode, String documentNumber) {
        LOG.debug("scrubEntries() started");

        if (reportOnlyMode) {
            scrubberReportWriterService.setDocumentNumber(documentNumber);
            scrubberLedgerReportWriterService.setDocumentNumber(documentNumber);
        }

        // setup an object to hold the "default" date information
        runDate = calculateRunDate(dateTimeService.getCurrentDate());
        runCal = Calendar.getInstance();
        runCal.setTime(runDate);

        universityRunDate = accountingCycleCachingService.getUniversityDate(runDate);
        if (universityRunDate == null) {
            throw new IllegalStateException(configurationService.getPropertyValueAsString(
                    KFSKeyConstants.ERROR_UNIV_DATE_NOT_FOUND));
        }

        setOffsetString();
        setDescriptions();
        scrubberReport = new ScrubberReportData();

        try {
            if (!collectorMode) {
                ((WrappingBatchService) scrubberReportWriterService).initialize();
                ((WrappingBatchService) scrubberLedgerReportWriterService).initialize();
            }

            processGroup(reportOnlyMode, scrubberReport);

            if (reportOnlyMode) {
                generateScrubberTransactionListingReport(documentNumber, inputFile);
            } else if (!collectorMode) {
                generateScrubberBlankBalanceTypeCodeReport(inputFile);
            }
        } finally {
            if (!collectorMode) {
                ((WrappingBatchService) scrubberReportWriterService).destroy();
                ((WrappingBatchService) scrubberLedgerReportWriterService).destroy();
            }
        }
    }

    /**
     * Scrubs the origin entry and ID billing details if the given batch. Store all scrubber output into the
     * collectorReportData parameter. NOTE: DO NOT CALL ANY OF THE scrub* METHODS OF THIS CLASS AFTER CALLING THIS
     * METHOD FOR EVERY UNIQUE INSTANCE OF THIS CLASS, OR THE COLLECTOR REPORTS MAY BE CORRUPTED
     *
     * @param batch               the data gathered from a Collector file
     * @param collectorReportData the statistics generated by running the Collector
     */
    @Override
    public void scrubCollectorBatch(ScrubberStatus scrubberStatus, CollectorBatch batch,
            CollectorReportData collectorReportData) {
        collectorMode = true;

        this.inputFile = batchFileDirectoryName + File.separator +
                GeneralLedgerConstants.BatchFileSystem.COLLECTOR_SCRUBBER_INPUT_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION;
        this.validFile = batchFileDirectoryName + File.separator +
                GeneralLedgerConstants.BatchFileSystem.COLLECTOR_SCRUBBER_VALID_OUTPUT_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION;
        this.errorFile = batchFileDirectoryName + File.separator +
                GeneralLedgerConstants.BatchFileSystem.COLLECTOR_SCRUBBER_ERROR_OUTPUT_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION;
        this.expiredFile = batchFileDirectoryName + File.separator +
                GeneralLedgerConstants.BatchFileSystem.COLLECTOR_SCRUBBER_EXPIRED_OUTPUT_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION;
        runDate = calculateRunDate(dateTimeService.getCurrentDate());

        this.ledgerSummaryReport = collectorReportData.getLedgerSummaryReport();

        // sort input file
        String scrubberSortInputFile = batchFileDirectoryName + File.separator +
                GeneralLedgerConstants.BatchFileSystem.COLLECTOR_BACKUP_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION;
        String scrubberSortOutputFile = batchFileDirectoryName + File.separator +
                GeneralLedgerConstants.BatchFileSystem.COLLECTOR_SCRUBBER_INPUT_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION;
        BatchSortUtil.sortTextFileWithFields(scrubberSortInputFile, scrubberSortOutputFile,
                new ScrubberSortComparator());

        scrubEntries(false, null);

        //sort scrubber error file for demerger
        String demergerSortInputFile = batchFileDirectoryName + File.separator +
                GeneralLedgerConstants.BatchFileSystem.COLLECTOR_SCRUBBER_ERROR_OUTPUT_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION;
        String demergerSortOutputFile = batchFileDirectoryName + File.separator +
                GeneralLedgerConstants.BatchFileSystem.COLLECTOR_SCRUBBER_ERROR_SORTED_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION;
        BatchSortUtil.sortTextFileWithFields(demergerSortInputFile, demergerSortOutputFile,
                new DemergerSortComparator());

        performDemerger();

        // the scrubber process has just updated several member variables of this class. Store these values for the
        // collector report
        collectorReportData.setBatchOriginEntryScrubberErrors(batch, scrubberReportErrors);
        collectorReportData.setScrubberReportData(batch, scrubberReport);
        collectorReportData.setDemergerReportData(batch, demergerReport);

        // report purpose - commented out.  If we need, the put string values for fileNames.
        scrubberStatus.setInputFileName(GeneralLedgerConstants.BatchFileSystem.COLLECTOR_SCRUBBER_INPUT_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION);
        scrubberStatus.setValidFileName(GeneralLedgerConstants.BatchFileSystem.COLLECTOR_DEMERGER_VALID_OUTPUT_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION);
        scrubberStatus.setErrorFileName(GeneralLedgerConstants.BatchFileSystem.COLLECTOR_DEMERGER_ERROR_OUTPUT_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION);
        scrubberStatus.setExpiredFileName(GeneralLedgerConstants.BatchFileSystem.COLLECTOR_SCRUBBER_EXPIRED_OUTPUT_FILE +
                GeneralLedgerConstants.BatchFileSystem.EXTENSION);
        scrubberStatus.setUnscrubbedToScrubbedEntries(unscrubbedToScrubbedEntries);
    }

    /**
     * The demerger process reads all of the documents in the error group, then moves all of the original entries for
     * that document from the valid group to the error group. It does not move generated entries to the error group.
     * Those are deleted. It also modifies the doc number and origin code of cost share transfers.
     */
    @Override
    public void performDemerger() {
        LOG.debug("performDemerger() started");

        OriginEntryFieldUtil oefu = new OriginEntryFieldUtil();
        Map<String, Integer> pMap = oefu.getFieldBeginningPositionMap();

        // Without this step, the job fails with Optimistic Lock Exceptions
        // persistenceService.clearCache();

        demergerReport = new DemergerReportData();

        // set runDate here again, because demerger is calling outside from scrubber
        runDate = calculateRunDate(dateTimeService.getCurrentDate());
        runCal = Calendar.getInstance();
        runCal.setTime(runDate);

        // demerger called by outside from scrubber, so reset those values
        setOffsetString();
        setDescriptions();

        // new demerger starts

        String validOutputFilename;
        String errorOutputFilename;

        String demergerValidOutputFilename;
        String demergerErrorOutputFilename;

        if (!collectorMode) {
            validOutputFilename = batchFileDirectoryName + File.separator +
                    GeneralLedgerConstants.BatchFileSystem.SCRUBBER_VALID_OUTPUT_FILE +
                    GeneralLedgerConstants.BatchFileSystem.EXTENSION;
            errorOutputFilename = batchFileDirectoryName + File.separator +
                    GeneralLedgerConstants.BatchFileSystem.SCRUBBER_ERROR_SORTED_FILE +
                    GeneralLedgerConstants.BatchFileSystem.EXTENSION;

            demergerValidOutputFilename = batchFileDirectoryName + File.separator +
                    GeneralLedgerConstants.BatchFileSystem.DEMERGER_VAILD_OUTPUT_FILE +
                    GeneralLedgerConstants.BatchFileSystem.EXTENSION;
            demergerErrorOutputFilename = batchFileDirectoryName + File.separator +
                    GeneralLedgerConstants.BatchFileSystem.DEMERGER_ERROR_OUTPUT_FILE +
                    GeneralLedgerConstants.BatchFileSystem.EXTENSION;

        } else {

            validOutputFilename = batchFileDirectoryName + File.separator +
                    GeneralLedgerConstants.BatchFileSystem.COLLECTOR_SCRUBBER_VALID_OUTPUT_FILE +
                    GeneralLedgerConstants.BatchFileSystem.EXTENSION;
            errorOutputFilename = batchFileDirectoryName + File.separator +
                    GeneralLedgerConstants.BatchFileSystem.COLLECTOR_SCRUBBER_ERROR_SORTED_FILE +
                    GeneralLedgerConstants.BatchFileSystem.EXTENSION;

            demergerValidOutputFilename = batchFileDirectoryName + File.separator +
                    GeneralLedgerConstants.BatchFileSystem.COLLECTOR_DEMERGER_VALID_OUTPUT_FILE +
                    GeneralLedgerConstants.BatchFileSystem.EXTENSION;
            demergerErrorOutputFilename = batchFileDirectoryName + File.separator +
                    GeneralLedgerConstants.BatchFileSystem.COLLECTOR_DEMERGER_ERROR_OUTPUT_FILE +
                    GeneralLedgerConstants.BatchFileSystem.EXTENSION;
        }

        FileReader INPUT_GLE_FILE;
        FileReader INPUT_ERR_FILE;
        BufferedReader INPUT_GLE_FILE_br;
        BufferedReader INPUT_ERR_FILE_br;
        PrintStream OUTPUT_DEMERGER_GLE_FILE_ps;
        PrintStream OUTPUT_DEMERGER_ERR_FILE_ps;

        try {
            INPUT_GLE_FILE = new FileReader(validOutputFilename);
            INPUT_ERR_FILE = new FileReader(errorOutputFilename);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            OUTPUT_DEMERGER_GLE_FILE_ps = new PrintStream(demergerValidOutputFilename);
            OUTPUT_DEMERGER_ERR_FILE_ps = new PrintStream(demergerErrorOutputFilename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int validSaved = 0;
        int errorSaved = 0;

        int validReadLine = 0;
        int errorReadLine = 0;

        INPUT_GLE_FILE_br = new BufferedReader(INPUT_GLE_FILE);
        INPUT_ERR_FILE_br = new BufferedReader(INPUT_ERR_FILE);

        try {
            String currentValidLine = INPUT_GLE_FILE_br.readLine();
            String currentErrorLine = INPUT_ERR_FILE_br.readLine();

            while (currentValidLine != null || currentErrorLine != null) {

                // Demerger only catch IOexception since demerger report doesn't display detail error message.
                try {
                    //validLine is null means that errorLine is not null
                    if (org.apache.commons.lang3.StringUtils.isEmpty(currentValidLine)) {
                        String errorDesc = currentErrorLine.substring(
                                pMap.get(KFSPropertyConstants.TRANSACTION_LEDGER_ENTRY_DESC),
                                pMap.get(KFSPropertyConstants.TRANSACTION_LEDGER_ENTRY_AMOUNT));
                        String errorFinancialBalanceTypeCode = currentErrorLine.substring(
                                pMap.get(KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE),
                                pMap.get(KFSPropertyConstants.FINANCIAL_OBJECT_TYPE_CODE));

                        if (!checkingBypassEntry(errorFinancialBalanceTypeCode, errorDesc, demergerReport)) {
                            createOutputEntry(currentErrorLine, OUTPUT_DEMERGER_ERR_FILE_ps);
                            errorSaved++;
                        }
                        currentErrorLine = INPUT_ERR_FILE_br.readLine();
                        errorReadLine++;
                        continue;
                    }

                    String financialBalanceTypeCode = currentValidLine.substring(
                            pMap.get(KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE),
                            pMap.get(KFSPropertyConstants.FINANCIAL_OBJECT_TYPE_CODE));
                    String desc = currentValidLine.substring(
                            pMap.get(KFSPropertyConstants.TRANSACTION_LEDGER_ENTRY_DESC),
                            pMap.get(KFSPropertyConstants.TRANSACTION_LEDGER_ENTRY_AMOUNT));

                    //errorLine is null means that validLine is not null
                    if (org.apache.commons.lang3.StringUtils.isEmpty(currentErrorLine)) {
                        // Read all the transactions in the valid group and update the cost share transactions
                        String updatedValidLine = checkAndSetTransactionTypeCostShare(financialBalanceTypeCode, desc,
                                currentValidLine);
                        createOutputEntry(updatedValidLine, OUTPUT_DEMERGER_GLE_FILE_ps);
                        handleDemergerSaveValidEntry(updatedValidLine);
                        validSaved++;
                        currentValidLine = INPUT_GLE_FILE_br.readLine();
                        validReadLine++;
                        continue;
                    }

                    String compareStringFromValidEntry = currentValidLine.substring(
                            pMap.get(KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE),
                            pMap.get(KFSPropertyConstants.TRANSACTION_ENTRY_SEQUENCE_NUMBER));
                    String compareStringFromErrorEntry = currentErrorLine.substring(
                            pMap.get(KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE),
                            pMap.get(KFSPropertyConstants.TRANSACTION_ENTRY_SEQUENCE_NUMBER));

                    String errorDesc = currentErrorLine.substring(
                            pMap.get(KFSPropertyConstants.TRANSACTION_LEDGER_ENTRY_DESC),
                            pMap.get(KFSPropertyConstants.TRANSACTION_LEDGER_ENTRY_AMOUNT));
                    String errorFinancialBalanceTypeCode = currentErrorLine.substring(
                            pMap.get(KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE),
                            pMap.get(KFSPropertyConstants.FINANCIAL_OBJECT_TYPE_CODE));

                    if (compareStringFromValidEntry.compareTo(compareStringFromErrorEntry) < 0) {
                        // Read all the transactions in the valid group and update the cost share transactions
                        String updatedValidLine = checkAndSetTransactionTypeCostShare(financialBalanceTypeCode, desc,
                                currentValidLine);
                        createOutputEntry(updatedValidLine, OUTPUT_DEMERGER_GLE_FILE_ps);
                        handleDemergerSaveValidEntry(updatedValidLine);
                        validSaved++;
                        currentValidLine = INPUT_GLE_FILE_br.readLine();
                        validReadLine++;

                    } else if (compareStringFromValidEntry.compareTo(compareStringFromErrorEntry) > 0) {
                        if (!checkingBypassEntry(errorFinancialBalanceTypeCode, errorDesc, demergerReport)) {
                            createOutputEntry(currentErrorLine, OUTPUT_DEMERGER_ERR_FILE_ps);
                            errorSaved++;
                        }
                        currentErrorLine = INPUT_ERR_FILE_br.readLine();
                        errorReadLine++;

                    } else {
                        if (!checkingBypassEntry(financialBalanceTypeCode, desc, demergerReport)) {
                            createOutputEntry(currentValidLine, OUTPUT_DEMERGER_ERR_FILE_ps);
                            errorSaved++;
                        }
                        currentValidLine = INPUT_GLE_FILE_br.readLine();
                        validReadLine++;
                    }
                } catch (RuntimeException re) {
                    LOG.error("performDemerger Stopped: " + re.getMessage());
                    throw new RuntimeException("performDemerger Stopped: " + re.getMessage(), re);
                }
            }
            INPUT_GLE_FILE_br.close();
            INPUT_ERR_FILE_br.close();
            OUTPUT_DEMERGER_GLE_FILE_ps.close();
            OUTPUT_DEMERGER_ERR_FILE_ps.close();

        } catch (IOException e) {
            LOG.error("performDemerger Stopped: " + e.getMessage());
            throw new RuntimeException("performDemerger Stopped: " + e.getMessage(), e);
        }
        demergerReport.setErrorTransactionWritten(errorSaved);
        demergerReport.setErrorTransactionsRead(errorReadLine);
        demergerReport.setValidTransactionsRead(validReadLine);
        demergerReport.setValidTransactionsSaved(validSaved);

        if (!collectorMode) {
            demergerReportWriterService.writeStatisticLine("SCRUBBER ERROR TRANSACTIONS READ       %,9d",
                    demergerReport.getErrorTransactionsRead());
            demergerReportWriterService.writeStatisticLine("SCRUBBER VALID TRANSACTIONS READ       %,9d",
                    demergerReport.getValidTransactionsRead());
            demergerReportWriterService.writeNewLines(1);
            demergerReportWriterService.writeStatisticLine("DEMERGER ERRORS SAVED                  %,9d",
                    demergerReport.getErrorTransactionsSaved());
            demergerReportWriterService.writeStatisticLine("DEMERGER VALID TRANSACTIONS SAVED      %,9d",
                    demergerReport.getValidTransactionsSaved());
            demergerReportWriterService.writeStatisticLine("OFFSET TRANSACTIONS BYPASSED           %,9d",
                    demergerReport.getOffsetTransactionsBypassed());
            demergerReportWriterService.writeStatisticLine("CAPITALIZATION TRANSACTIONS BYPASSED   %,9d",
                    demergerReport.getCapitalizationTransactionsBypassed());
            demergerReportWriterService.writeStatisticLine("LIABILITY TRANSACTIONS BYPASSED        %,9d",
                    demergerReport.getLiabilityTransactionsBypassed());
            demergerReportWriterService.writeStatisticLine("TRANSFER TRANSACTIONS BYPASSED         %,9d",
                    demergerReport.getTransferTransactionsBypassed());
            demergerReportWriterService.writeStatisticLine("COST SHARE TRANSACTIONS BYPASSED       %,9d",
                    demergerReport.getCostShareTransactionsBypassed());
            demergerReportWriterService.writeStatisticLine("COST SHARE ENC TRANSACTIONS BYPASSED   %,9d",
                    demergerReport.getCostShareEncumbranceTransactionsBypassed());

            generateDemergerRemovedTransactionsReport(demergerErrorOutputFilename);
        }
    }

    /**
     * Determine the type of the transaction by looking at attributes
     *
     * @param transaction Transaction to identify
     * @return CE (Cost share encumbrance, O (Offset), C (apitalization), L (Liability), T (Transfer),
     *         CS (Cost Share), X (Other)
     */
    protected String getTransactionType(OriginEntryInformation transaction) {
        if (TRANSACTION_TYPE_COST_SHARE_ENCUMBRANCE.equals(transaction.getFinancialBalanceTypeCode())) {
            return TRANSACTION_TYPE_COST_SHARE_ENCUMBRANCE;
        }
        String desc = transaction.getTransactionLedgerEntryDescription();

        if (desc == null) {
            return TRANSACTION_TYPE_OTHER;
        }
        if (desc.startsWith(offsetDescription) && desc.contains(COST_SHARE_TRANSFER_ENTRY_IND)) {
            return TRANSACTION_TYPE_COST_SHARE;
        }
        if (desc.startsWith(costShareDescription) && desc.contains(COST_SHARE_TRANSFER_ENTRY_IND)) {
            return TRANSACTION_TYPE_COST_SHARE;
        }
        if (desc.startsWith(offsetDescription)) {
            return TRANSACTION_TYPE_OFFSET;
        }
        if (desc.startsWith(capitalizationDescription)) {
            return TRANSACTION_TYPE_CAPITALIZATION;
        }
        if (desc.startsWith(liabilityDescription)) {
            return TRANSACTION_TYPE_LIABILITY;
        }
        if (desc.startsWith(transferDescription)) {
            return TRANSACTION_TYPE_TRANSFER;
        }
        return TRANSACTION_TYPE_OTHER;
    }

    protected String getTransactionType(String financialBalanceTypeCode, String desc) {
        if (TRANSACTION_TYPE_COST_SHARE_ENCUMBRANCE.equals(financialBalanceTypeCode)) {
            return TRANSACTION_TYPE_COST_SHARE_ENCUMBRANCE;
        }
        if (desc == null) {
            return TRANSACTION_TYPE_OTHER;
        }

        if (desc.startsWith(offsetDescription) && desc.contains(COST_SHARE_TRANSFER_ENTRY_IND)) {
            return TRANSACTION_TYPE_COST_SHARE;
        }
        if (desc.startsWith(costShareDescription) && desc.contains(COST_SHARE_TRANSFER_ENTRY_IND)) {
            return TRANSACTION_TYPE_COST_SHARE;
        }
        if (desc.startsWith(offsetDescription)) {
            return TRANSACTION_TYPE_OFFSET;
        }
        if (desc.startsWith(capitalizationDescription)) {
            return TRANSACTION_TYPE_CAPITALIZATION;
        }
        if (desc.startsWith(liabilityDescription)) {
            return TRANSACTION_TYPE_LIABILITY;
        }
        if (desc.startsWith(transferDescription)) {
            return TRANSACTION_TYPE_TRANSFER;
        }
        return TRANSACTION_TYPE_OTHER;
    }

    /**
     * This will process a group of origin entries. The COBOL code was refactored a lot to get this so there isn't a
     * 1 to 1 section of Cobol relating to this.
     *
     * @param reportOnlyMode
     * @param scrubberReport
     */
    protected void processGroup(boolean reportOnlyMode, ScrubberReportData scrubberReport) {
        OriginEntryFull lastEntry = null;
        scrubCostShareAmount = KualiDecimal.ZERO;
        scrubberProcessUnitOfWork = new ScrubberProcessUnitOfWork();

        FileReader INPUT_GLE_FILE;
        String GLEN_RECORD;
        BufferedReader INPUT_GLE_FILE_br;
        try {
            INPUT_GLE_FILE = new FileReader(inputFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            OUTPUT_GLE_FILE_ps = new PrintStream(validFile);
            OUTPUT_ERR_FILE_ps = new PrintStream(errorFile);
            OUTPUT_EXP_FILE_ps = new PrintStream(expiredFile);
            LOG.info("Successfully opened " + validFile + ", " + errorFile + ", " + expiredFile + " for writing.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        INPUT_GLE_FILE_br = new BufferedReader(INPUT_GLE_FILE);
        int line = 0;
        LOG.debug("Starting Scrubber Process process group...");
        try {
            while ((GLEN_RECORD = INPUT_GLE_FILE_br.readLine()) != null) {
                if (!org.apache.commons.lang3.StringUtils.isEmpty(GLEN_RECORD)
                        && !org.apache.commons.lang3.StringUtils.isBlank(GLEN_RECORD.trim())) {
                    line++;
                    OriginEntryFull unscrubbedEntry = new OriginEntryFull();
                    List<Message> tmperrors = unscrubbedEntry.setFromTextFileForBatch(GLEN_RECORD, line);
                    scrubberReport.incrementUnscrubbedRecordsRead();
                    transactionErrors = new ArrayList<>();

                    // This is done so if the code modifies this row, then saves it, it will be an insert,
                    // and it won't touch the original. The Scrubber never modifies input rows/groups.
                    // not relevant for file version

                    boolean saveErrorTransaction = false;
                    boolean saveValidTransaction = false;
                    boolean fatalErrorOccurred = false;

                    // Build a scrubbed entry
                    OriginEntryFull scrubbedEntry = new OriginEntryFull();
                    scrubbedEntry.setDocumentNumber(unscrubbedEntry.getDocumentNumber());
                    scrubbedEntry.setOrganizationDocumentNumber(unscrubbedEntry.getOrganizationDocumentNumber());
                    scrubbedEntry.setOrganizationReferenceId(unscrubbedEntry.getOrganizationReferenceId());
                    scrubbedEntry.setReferenceFinancialDocumentNumber(
                            unscrubbedEntry.getReferenceFinancialDocumentNumber());

                    Integer transactionNumber = unscrubbedEntry.getTransactionLedgerEntrySequenceNumber();
                    scrubbedEntry.setTransactionLedgerEntrySequenceNumber(null == transactionNumber ?
                            new Integer(0) : transactionNumber);
                    scrubbedEntry.setTransactionLedgerEntryDescription(
                            unscrubbedEntry.getTransactionLedgerEntryDescription());
                    scrubbedEntry.setTransactionLedgerEntryAmount(unscrubbedEntry.getTransactionLedgerEntryAmount());
                    scrubbedEntry.setTransactionDebitCreditCode(unscrubbedEntry.getTransactionDebitCreditCode());

                    if (!collectorMode) {
                        ledgerSummaryReport.summarizeEntry(unscrubbedEntry);
                    }

                    // For Labor Scrubber
                    boolean laborIndicator = false;
                    tmperrors.addAll(scrubberValidator.validateTransaction(unscrubbedEntry, scrubbedEntry,
                            universityRunDate, laborIndicator, accountingCycleCachingService));
                    transactionErrors.addAll(tmperrors);

                    Account unscrubbedEntryAccount = accountingCycleCachingService.getAccount(
                            unscrubbedEntry.getChartOfAccountsCode(), unscrubbedEntry.getAccountNumber());
                    // KFSMI-173: both the expired and closed accounts rows are put in the expired account
                    if ((unscrubbedEntryAccount != null)
                            && (scrubberValidator.isAccountExpired(unscrubbedEntryAccount, universityRunDate)
                            || unscrubbedEntryAccount.isClosed())) {
                        // Make a copy of it so OJB doesn't just update the row in the original
                        // group. It needs to make a new one in the expired group
                        OriginEntryFull expiredEntry = OriginEntryFull.copyFromOriginEntryable(scrubbedEntry);
                        createOutputEntry(expiredEntry, OUTPUT_EXP_FILE_ps);
                        scrubberReport.incrementExpiredAccountFound();
                    }

                    // the collector scrubber uses this map to apply the same changes made on an origin entry during
                    // scrubbing to the collector detail record
                    if (collectorMode) {
                        unscrubbedToScrubbedEntries.put(unscrubbedEntry, scrubbedEntry);
                    }

                    if (!isFatal(transactionErrors)) {
                        saveValidTransaction = true;

                        if (!collectorMode) {
                            // See if unit of work has changed
                            if (!scrubberProcessUnitOfWork.isSameUnitOfWork(scrubbedEntry)) {
                                // Generate offset for last unit of work pass the String line for generating error
                                // files
                                generateOffset(lastEntry, scrubberReport);
                                scrubberProcessUnitOfWork = new ScrubberProcessUnitOfWork(scrubbedEntry);
                            }

                            KualiDecimal transactionAmount = scrubbedEntry.getTransactionLedgerEntryAmount();

                            ParameterEvaluator offsetFiscalPeriods = parameterEvaluatorService
                                    .getParameterEvaluator(ScrubberStep.class,
                                            GLParameterConstants.OFFSET_FISCAL_PERIOD_CODES,
                                            scrubbedEntry.getUniversityFiscalPeriodCode());

                            BalanceType scrubbedEntryBalanceType = accountingCycleCachingService
                                    .getBalanceType(scrubbedEntry.getFinancialBalanceTypeCode());
                            if (scrubbedEntryBalanceType.isFinancialOffsetGenerationIndicator()
                                    && offsetFiscalPeriods.evaluationSucceeds()) {
                                if (scrubbedEntry.isDebit()) {
                                    scrubberProcessUnitOfWork.setOffsetAmount(
                                            scrubberProcessUnitOfWork.getOffsetAmount().add(transactionAmount));
                                } else {
                                    scrubberProcessUnitOfWork.setOffsetAmount(
                                            scrubberProcessUnitOfWork.getOffsetAmount().subtract(transactionAmount));
                                }
                            }

                            // The sub account type code will only exist if there is a valid sub account
                            String subAccountTypeCode = GeneralLedgerConstants.getSpaceSubAccountTypeCode();
                            // major assumption: the a21 subaccount is proxied, so we don't want to query the database
                            // if the subacct number is dashes
                            if (!KFSConstants.getDashSubAccountNumber().equals(scrubbedEntry.getSubAccountNumber())) {
                                A21SubAccount scrubbedEntryA21SubAccount = accountingCycleCachingService
                                        .getA21SubAccount(scrubbedEntry.getChartOfAccountsCode(),
                                                scrubbedEntry.getAccountNumber(), scrubbedEntry.getSubAccountNumber());
                                if (ObjectUtils.isNotNull(scrubbedEntryA21SubAccount)) {
                                    subAccountTypeCode = scrubbedEntryA21SubAccount.getSubAccountTypeCode();
                                }
                            }

                            ParameterEvaluator costShareObjectTypeCodes = parameterEvaluatorService
                                    .getParameterEvaluator(ScrubberStep.class, COST_SHARE_OBJ_TYPE_CODES,
                                            scrubbedEntry.getFinancialObjectTypeCode());
                            ParameterEvaluator costShareEncBalanceTypeCodes = parameterEvaluatorService
                                    .getParameterEvaluator(ScrubberStep.class, COST_SHARE_ENC_BAL_TYP_CODES,
                                            scrubbedEntry.getFinancialBalanceTypeCode());
                            ParameterEvaluator costShareEncFiscalPeriodCodes = parameterEvaluatorService
                                    .getParameterEvaluator(ScrubberStep.class, COST_SHARE_ENC_FISCAL_PERIOD_CODES,
                                            scrubbedEntry.getUniversityFiscalPeriodCode());
                            ParameterEvaluator costShareEncDocTypeCodes = parameterEvaluatorService
                                    .getParameterEvaluator(ScrubberStep.class, COST_SHARE_ENC_DOC_TYPE_CODES,
                                            scrubbedEntry.getFinancialDocumentTypeCode().trim());
                            ParameterEvaluator costShareFiscalPeriodCodes = parameterEvaluatorService
                                    .getParameterEvaluator(ScrubberStep.class, COST_SHARE_FISCAL_PERIOD_CODES,
                                            scrubbedEntry.getUniversityFiscalPeriodCode());
                            Account scrubbedEntryAccount = accountingCycleCachingService.getAccount(
                                    scrubbedEntry.getChartOfAccountsCode(), scrubbedEntry.getAccountNumber());

                            if (costShareObjectTypeCodes.evaluationSucceeds()
                                    && costShareEncBalanceTypeCodes.evaluationSucceeds()
                                    && scrubbedEntryAccount.isForContractsAndGrants()
                                    && KFSConstants.SubAccountType.COST_SHARE.equals(subAccountTypeCode)
                                    && costShareEncFiscalPeriodCodes.evaluationSucceeds()
                                    && costShareEncDocTypeCodes.evaluationSucceeds()) {
                                ScrubberProcessTransactionError te1 = generateCostShareEncumbranceEntries(scrubbedEntry,
                                        scrubberReport);
                                if (te1 != null) {
                                    List<Message> errors = new ArrayList<>();
                                    errors.add(te1.getMessage());
                                    handleTransactionErrors(te1.getTransaction(), errors);
                                    saveValidTransaction = false;
                                    saveErrorTransaction = true;
                                }
                            }

                            SystemOptions scrubbedEntryOption = accountingCycleCachingService.getSystemOptions(
                                    scrubbedEntry.getUniversityFiscalYear());
                            if (costShareObjectTypeCodes.evaluationSucceeds()
                                    && scrubbedEntryOption.getActualFinancialBalanceTypeCd()
                                        .equals(scrubbedEntry.getFinancialBalanceTypeCode())
                                    && scrubbedEntryAccount.isForContractsAndGrants()
                                    && KFSConstants.SubAccountType.COST_SHARE.equals(subAccountTypeCode)
                                    && costShareFiscalPeriodCodes.evaluationSucceeds()
                                    && costShareEncDocTypeCodes.evaluationSucceeds()) {
                                if (scrubbedEntry.isDebit()) {
                                    scrubCostShareAmount = scrubCostShareAmount.subtract(transactionAmount);
                                } else {
                                    scrubCostShareAmount = scrubCostShareAmount.add(transactionAmount);
                                }
                            }

                            ParameterEvaluator otherDocTypeCodes = parameterEvaluatorService
                                    .getParameterEvaluator(ScrubberStep.class,
                                            GLParameterConstants.OFFSET_DOC_TYPE_CODES,
                                            scrubbedEntry.getFinancialDocumentTypeCode());

                            if (otherDocTypeCodes.evaluationSucceeds()) {
                                String m = processCapitalization(scrubbedEntry, scrubberReport);
                                if (m != null) {
                                    saveValidTransaction = false;
                                    saveErrorTransaction = false;
                                    addTransactionError(m, "", Message.TYPE_FATAL);
                                }

                                m = processLiabilities(scrubbedEntry, scrubberReport);
                                if (m != null) {
                                    saveValidTransaction = false;
                                    saveErrorTransaction = false;
                                    addTransactionError(m, "", Message.TYPE_FATAL);
                                }

                                m = processPlantIndebtedness(scrubbedEntry, scrubberReport);
                                if (m != null) {
                                    saveValidTransaction = false;
                                    saveErrorTransaction = false;
                                    addTransactionError(m, "", Message.TYPE_FATAL);
                                }
                            }

                            if (!scrubCostShareAmount.isZero()) {
                                ScrubberProcessTransactionError te = generateCostShareEntries(scrubbedEntry,
                                        scrubberReport);

                                if (te != null) {
                                    saveValidTransaction = false;
                                    saveErrorTransaction = false;

                                    // Make a copy of it so OJB doesn't just update the row in the original
                                    // group. It needs to make a new one in the error group
                                    OriginEntryFull errorEntry = new OriginEntryFull(te.getTransaction());
                                    errorEntry.setTransactionScrubberOffsetGenerationIndicator(false);
                                    createOutputEntry(GLEN_RECORD, OUTPUT_ERR_FILE_ps);
                                    scrubberReport.incrementErrorRecordWritten();
                                    scrubberProcessUnitOfWork.setErrorsFound(true);

                                    handleTransactionError(te.getTransaction(), te.getMessage());
                                }
                                scrubCostShareAmount = KualiDecimal.ZERO;
                            }

                            lastEntry = scrubbedEntry;
                        }
                    } else {
                        // Error transaction
                        saveErrorTransaction = true;
                        fatalErrorOccurred = true;
                    }
                    handleTransactionErrors(OriginEntryFull.copyFromOriginEntryable(unscrubbedEntry),
                            transactionErrors);

                    if (saveValidTransaction) {
                        scrubbedEntry.setTransactionScrubberOffsetGenerationIndicator(false);
                        createOutputEntry(scrubbedEntry, OUTPUT_GLE_FILE_ps);
                        scrubberReport.incrementScrubbedRecordWritten();
                    }

                    if (saveErrorTransaction) {
                        // Make a copy of it so OJB doesn't just update the row in the original
                        // group. It needs to make a new one in the error group
                        OriginEntryFull errorEntry = OriginEntryFull.copyFromOriginEntryable(unscrubbedEntry);
                        errorEntry.setTransactionScrubberOffsetGenerationIndicator(false);
                        createOutputEntry(GLEN_RECORD, OUTPUT_ERR_FILE_ps);
                        scrubberReport.incrementErrorRecordWritten();
                        if (!fatalErrorOccurred) {
                            // if a fatal error occurred, the creation of a new unit of work was by-passed;
                            // therefore, it shouldn't ruin the previous unit of work
                            scrubberProcessUnitOfWork.setErrorsFound(true);
                        }
                    }
                }
            }

            if (!collectorMode) {
                // Generate last offset (if necessary)
                generateOffset(lastEntry, scrubberReport);
            }

            INPUT_GLE_FILE_br.close();
            INPUT_GLE_FILE.close();
            OUTPUT_GLE_FILE_ps.close();
            OUTPUT_ERR_FILE_ps.close();
            OUTPUT_EXP_FILE_ps.close();
            LOG.info("Successfully written and closed " + validFile + ", " + errorFile + ", " + expiredFile + ".");

            handleEndOfScrubberReport(scrubberReport);

            if (!collectorMode) {
                ledgerSummaryReport.writeReport(this.scrubberLedgerReportWriterService);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Determines if a given error is fatal and should stop this scrubber run
     *
     * @param errors errors from a scrubber run
     * @return true if the run should be abended, false otherwise
     */
    protected boolean isFatal(List<Message> errors) {
        for (Message error : errors) {
            if (error.getType() == Message.TYPE_FATAL) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generates a cost share entry and offset for the given entry and saves both to the valid group
     *
     * @param scrubbedEntry the originEntry that was scrubbed
     * @return a TransactionError initialized with any error encountered during entry generation, or (hopefully) null
     */
    protected ScrubberProcessTransactionError generateCostShareEntries(OriginEntryInformation scrubbedEntry,
            ScrubberReportData scrubberReport) {
        // 3000-COST-SHARE to 3100-READ-OFSD in the cobol Generate Cost Share Entries
        LOG.debug("generateCostShareEntries() started");
        try {
            OriginEntryFull costShareEntry = OriginEntryFull.copyFromOriginEntryable(scrubbedEntry);

            SystemOptions scrubbedEntryOption = accountingCycleCachingService.getSystemOptions(
                    scrubbedEntry.getUniversityFiscalYear());
            A21SubAccount scrubbedEntryA21SubAccount = accountingCycleCachingService.getA21SubAccount(
                    scrubbedEntry.getChartOfAccountsCode(), scrubbedEntry.getAccountNumber(),
                    scrubbedEntry.getSubAccountNumber());

            costShareEntry.setFinancialObjectCode(parameterService.getParameterValueAsString(ScrubberStep.class,
                    COST_SHARE_OBJECT_CODE));
            costShareEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            costShareEntry.setFinancialObjectTypeCode(scrubbedEntryOption.getFinancialObjectTypeTransferExpenseCd());
            costShareEntry.setTransactionLedgerEntrySequenceNumber(0);

            StringBuffer description = new StringBuffer();
            description.append(costShareDescription);
            description.append(" ").append(scrubbedEntry.getAccountNumber());
            description.append(offsetString);
            costShareEntry.setTransactionLedgerEntryDescription(description.toString());

            costShareEntry.setTransactionLedgerEntryAmount(scrubCostShareAmount);
            if (scrubCostShareAmount.isPositive()) {
                costShareEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            } else {
                costShareEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
                costShareEntry.setTransactionLedgerEntryAmount(scrubCostShareAmount.negated());
            }

            costShareEntry.setTransactionDate(runDate);
            costShareEntry.setOrganizationDocumentNumber(null);
            costShareEntry.setProjectCode(KFSConstants.getDashProjectCode());
            costShareEntry.setOrganizationReferenceId(null);
            costShareEntry.setReferenceFinancialDocumentTypeCode(null);
            costShareEntry.setReferenceFinancialSystemOriginationCode(null);
            costShareEntry.setReferenceFinancialDocumentNumber(null);
            costShareEntry.setFinancialDocumentReversalDate(null);
            costShareEntry.setTransactionEncumbranceUpdateCode(null);

            createOutputEntry(costShareEntry, OUTPUT_GLE_FILE_ps);
            scrubberReport.incrementCostShareEntryGenerated();

            OriginEntryFull costShareOffsetEntry = new OriginEntryFull(costShareEntry);
            costShareOffsetEntry.setTransactionLedgerEntryDescription(getOffsetMessage());
            OffsetDefinition offsetDefinition = accountingCycleCachingService.getOffsetDefinition(
                    scrubbedEntry.getUniversityFiscalYear(), scrubbedEntry.getChartOfAccountsCode(),
                    KFSConstants.TRANSFER_FUNDS, scrubbedEntry.getFinancialBalanceTypeCode());
            if (offsetDefinition != null) {
                if (offsetDefinition.getFinancialObject() == null) {
                    String objectCodeKey = offsetDefinition.getUniversityFiscalYear() + "-" +
                            offsetDefinition.getChartOfAccountsCode() + "-" +
                            offsetDefinition.getFinancialObjectCode();
                    Message m = new Message(configurationService.getPropertyValueAsString(
                            KFSKeyConstants.ERROR_OFFSET_DEFINITION_OBJECT_CODE_NOT_FOUND) + " (" + objectCodeKey +
                            ")", Message.TYPE_FATAL);
                    LOG.debug("generateCostShareEntries() Error 1 object not found");
                    return new ScrubberProcessTransactionError(costShareEntry, m);
                }

                costShareOffsetEntry.setFinancialObjectCode(offsetDefinition.getFinancialObjectCode());
                costShareOffsetEntry.setFinancialObject(offsetDefinition.getFinancialObject());
                costShareOffsetEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            } else {
                String offsetKey = "cost share transfer " + scrubbedEntry.getUniversityFiscalYear() + "-" +
                        scrubbedEntry.getChartOfAccountsCode() + "-TF-" +
                        scrubbedEntry.getFinancialBalanceTypeCode();
                Message m = new Message(configurationService.getPropertyValueAsString(
                        KFSKeyConstants.ERROR_OFFSET_DEFINITION_NOT_FOUND) + " (" + offsetKey + ")",
                        Message.TYPE_FATAL);

                LOG.debug("generateCostShareEntries() Error 2 offset not found");
                return new ScrubberProcessTransactionError(costShareEntry, m);
            }

            costShareOffsetEntry.setFinancialObjectTypeCode(offsetDefinition.getFinancialObject()
                    .getFinancialObjectTypeCode());

            if (costShareEntry.isCredit()) {
                costShareOffsetEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            } else {
                costShareOffsetEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            }

            try {
                flexibleOffsetAccountService.updateOffset(costShareOffsetEntry);
            } catch (InvalidFlexibleOffsetException e) {
                Message m = new Message(e.getMessage(), Message.TYPE_FATAL);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("generateCostShareEntries() Cost Share Transfer Flexible Offset Error: " +
                            e.getMessage());
                }
                return new ScrubberProcessTransactionError(costShareEntry, m);
            }

            createOutputEntry(costShareOffsetEntry, OUTPUT_GLE_FILE_ps);
            scrubberReport.incrementCostShareEntryGenerated();

            OriginEntryFull costShareSourceAccountEntry = new OriginEntryFull(costShareEntry);

            description = new StringBuffer();
            description.append(costShareDescription);
            description.append(" ").append(scrubbedEntry.getAccountNumber());
            description.append(offsetString);
            costShareSourceAccountEntry.setTransactionLedgerEntryDescription(description.toString());

            costShareSourceAccountEntry.setChartOfAccountsCode(
                    scrubbedEntryA21SubAccount.getCostShareChartOfAccountCode());
            costShareSourceAccountEntry.setAccountNumber(scrubbedEntryA21SubAccount.getCostShareSourceAccountNumber());

            setCostShareObjectCode(costShareSourceAccountEntry, scrubbedEntry);
            costShareSourceAccountEntry.setSubAccountNumber(
                    scrubbedEntryA21SubAccount.getCostShareSourceSubAccountNumber());

            if (StringHelper.isNullOrEmpty(costShareSourceAccountEntry.getSubAccountNumber())) {
                costShareSourceAccountEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
            }

            costShareSourceAccountEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            costShareSourceAccountEntry.setFinancialObjectTypeCode(
                    scrubbedEntryOption.getFinancialObjectTypeTransferExpenseCd());
            costShareSourceAccountEntry.setTransactionLedgerEntrySequenceNumber(0);

            costShareSourceAccountEntry.setTransactionLedgerEntryAmount(scrubCostShareAmount);
            if (scrubCostShareAmount.isPositive()) {
                costShareSourceAccountEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            } else {
                costShareSourceAccountEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
                costShareSourceAccountEntry.setTransactionLedgerEntryAmount(scrubCostShareAmount.negated());
            }

            costShareSourceAccountEntry.setTransactionDate(runDate);
            costShareSourceAccountEntry.setOrganizationDocumentNumber(null);
            costShareSourceAccountEntry.setProjectCode(KFSConstants.getDashProjectCode());
            costShareSourceAccountEntry.setOrganizationReferenceId(null);
            costShareSourceAccountEntry.setReferenceFinancialDocumentTypeCode(null);
            costShareSourceAccountEntry.setReferenceFinancialSystemOriginationCode(null);
            costShareSourceAccountEntry.setReferenceFinancialDocumentNumber(null);
            costShareSourceAccountEntry.setFinancialDocumentReversalDate(null);
            costShareSourceAccountEntry.setTransactionEncumbranceUpdateCode(null);

            createOutputEntry(costShareSourceAccountEntry, OUTPUT_GLE_FILE_ps);
            scrubberReport.incrementCostShareEntryGenerated();

            OriginEntryFull costShareSourceAccountOffsetEntry = new OriginEntryFull(costShareSourceAccountEntry);
            costShareSourceAccountOffsetEntry.setTransactionLedgerEntryDescription(getOffsetMessage());

            // Lookup the new offset definition.
            offsetDefinition = accountingCycleCachingService.getOffsetDefinition(
                    scrubbedEntry.getUniversityFiscalYear(), scrubbedEntry.getChartOfAccountsCode(),
                    KFSConstants.TRANSFER_FUNDS, scrubbedEntry.getFinancialBalanceTypeCode());
            if (offsetDefinition != null) {
                if (offsetDefinition.getFinancialObject() == null) {
                    String objectCodeKey = costShareEntry.getUniversityFiscalYear() +
                            "-" + scrubbedEntry.getChartOfAccountsCode() +
                            "-" + scrubbedEntry.getFinancialObjectCode();
                    Message m = new Message(configurationService.getPropertyValueAsString(
                            KFSKeyConstants.ERROR_OFFSET_DEFINITION_OBJECT_CODE_NOT_FOUND) + " (" + objectCodeKey +
                            ")", Message.TYPE_FATAL);

                    LOG.debug("generateCostShareEntries() Error 3 object not found");
                    return new ScrubberProcessTransactionError(costShareSourceAccountEntry, m);
                }

                costShareSourceAccountOffsetEntry.setFinancialObjectCode(offsetDefinition.getFinancialObjectCode());
                costShareSourceAccountOffsetEntry.setFinancialObject(offsetDefinition.getFinancialObject());
                costShareSourceAccountOffsetEntry.setFinancialSubObjectCode(
                        KFSConstants.getDashFinancialSubObjectCode());
            } else {
                String offsetKey = "cost share transfer source " + scrubbedEntry.getUniversityFiscalYear() + "-" +
                        scrubbedEntry.getChartOfAccountsCode() + "-TF-" + scrubbedEntry.getFinancialBalanceTypeCode();
                Message m = new Message(configurationService.getPropertyValueAsString(
                        KFSKeyConstants.ERROR_OFFSET_DEFINITION_NOT_FOUND) + " (" + offsetKey + ")",
                        Message.TYPE_FATAL);

                LOG.debug("generateCostShareEntries() Error 4 offset not found");
                return new ScrubberProcessTransactionError(costShareSourceAccountEntry, m);
            }

            costShareSourceAccountOffsetEntry.setFinancialObjectTypeCode(offsetDefinition.getFinancialObject()
                    .getFinancialObjectTypeCode());

            if (scrubbedEntry.isCredit()) {
                costShareSourceAccountOffsetEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            } else {
                costShareSourceAccountOffsetEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            }

            try {
                flexibleOffsetAccountService.updateOffset(costShareSourceAccountOffsetEntry);
            } catch (InvalidFlexibleOffsetException e) {
                Message m = new Message(e.getMessage(), Message.TYPE_FATAL);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("generateCostShareEntries() Cost Share Transfer Account Flexible Offset Error: " +
                            e.getMessage());
                }
                return new ScrubberProcessTransactionError(costShareEntry, m);
            }

            createOutputEntry(costShareSourceAccountOffsetEntry, OUTPUT_GLE_FILE_ps);
            scrubberReport.incrementCostShareEntryGenerated();

            scrubCostShareAmount = KualiDecimal.ZERO;
        } catch (IOException ioe) {
            LOG.error("generateCostShareEntries() Stopped: " + ioe.getMessage());
            throw new RuntimeException("generateCostShareEntries() Stopped: " + ioe.getMessage(), ioe);
        }
        LOG.debug("generateCostShareEntries() successful");
        return null;
    }

    /**
     * Get all the transaction descriptions from the param table
     */
    protected void setDescriptions() {
        //TODO: move to constants class?
        offsetDescription = "GENERATED OFFSET";
        capitalizationDescription = "GENERATED CAPITALIZATION";
        liabilityDescription = "GENERATED LIABILITY";
        costShareDescription = "GENERATED COST SHARE FROM";
        transferDescription = "GENERATED TRANSFER FROM";
    }

    /**
     * Generate the flag for the end of specific descriptions. This will be used in the demerger step
     */
    protected void setOffsetString() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        nf.setMaximumIntegerDigits(2);
        nf.setMinimumFractionDigits(0);
        nf.setMinimumIntegerDigits(2);

        offsetString = COST_SHARE_TRANSFER_ENTRY_IND + nf.format(runCal.get(Calendar.MONTH) + 1) +
                nf.format(runCal.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Generate the offset message with the flag at the end
     *
     * @return a generated offset message
     */
    protected String getOffsetMessage() {
        String msg = offsetDescription + GeneralLedgerConstants.getSpaceTransactionLedgetEntryDescription();
        return msg.substring(0, OFFSET_MESSAGE_MAXLENGTH) + offsetString;
    }

    /**
     * Generates capitalization entries if necessary
     *
     * @param scrubbedEntry the entry to generate capitalization entries (possibly) for
     * @return null if no error, message if error
     */
    protected String processCapitalization(OriginEntryInformation scrubbedEntry, ScrubberReportData scrubberReport) {
        try {
            if (!parameterService.getParameterValueAsBoolean(ScrubberStep.class, CAPITALIZATION_IND)) {
                return null;
            }

            OriginEntryFull capitalizationEntry = OriginEntryFull.copyFromOriginEntryable(scrubbedEntry);
            SystemOptions scrubbedEntryOption = accountingCycleCachingService.getSystemOptions(
                    scrubbedEntry.getUniversityFiscalYear());
            ObjectCode scrubbedEntryObjectCode = accountingCycleCachingService.getObjectCode(
                    scrubbedEntry.getUniversityFiscalYear(),
                    scrubbedEntry.getChartOfAccountsCode(), scrubbedEntry.getFinancialObjectCode());
            Chart scrubbedEntryChart = accountingCycleCachingService.getChart(scrubbedEntry.getChartOfAccountsCode());
            Account scrubbedEntryAccount = accountingCycleCachingService.getAccount(
                    scrubbedEntry.getChartOfAccountsCode(),
                    scrubbedEntry.getAccountNumber());

            ParameterEvaluator documentTypeCodes = !ObjectUtils.isNull(scrubbedEntry) ? parameterEvaluatorService
                    .getParameterEvaluator(ScrubberStep.class, CAPITALIZATION_DOC_TYPE_CODES,
                            scrubbedEntry.getFinancialDocumentTypeCode()) : null;
            ParameterEvaluator fiscalPeriodCodes = !ObjectUtils.isNull(scrubbedEntry) ? parameterEvaluatorService
                    .getParameterEvaluator(ScrubberStep.class, CAPITALIZATION_FISCAL_PERIOD_CODES,
                            scrubbedEntry.getUniversityFiscalPeriodCode()) : null;
            ParameterEvaluator objectSubTypeCodes = !ObjectUtils.isNull(
                    scrubbedEntryObjectCode) ? parameterEvaluatorService.getParameterEvaluator(ScrubberStep.class,
                    CAPITALIZATION_OBJ_SUB_TYPE_CODES, scrubbedEntryObjectCode.getFinancialObjectSubTypeCode()) : null;
            ParameterEvaluator subFundGroupCodes = !ObjectUtils.isNull(
                    scrubbedEntryAccount) ? parameterEvaluatorService.getParameterEvaluator(ScrubberStep.class,
                    CAPITALIZATION_SUB_FUND_GROUP_CODES, scrubbedEntryAccount.getSubFundGroupCode()) : null;
            ParameterEvaluator chartCodes = !ObjectUtils.isNull(scrubbedEntry) ? parameterEvaluatorService
                    .getParameterEvaluator(ScrubberStep.class, CAPITALIZATION_CHART_CODES,
                            scrubbedEntry.getChartOfAccountsCode()) : null;

            if (scrubbedEntry.getFinancialBalanceTypeCode().equals(
                        scrubbedEntryOption.getActualFinancialBalanceTypeCd())
                    && scrubbedEntry.getUniversityFiscalYear() > 1995
                    && (documentTypeCodes != null && documentTypeCodes.evaluationSucceeds())
                    && (fiscalPeriodCodes != null && fiscalPeriodCodes.evaluationSucceeds())
                    && (objectSubTypeCodes != null && objectSubTypeCodes.evaluationSucceeds())
                    && (subFundGroupCodes != null && subFundGroupCodes.evaluationSucceeds())
                    && (chartCodes != null && chartCodes.evaluationSucceeds())) {

                String objectSubTypeCode = scrubbedEntryObjectCode.getFinancialObjectSubTypeCode();

                String capitalizationObjectCode = parameterService.getSubParameterValueAsString(ScrubberStep.class,
                        CAPITALIZATION_SUBTYPE_OBJECT, objectSubTypeCode);
                if (org.apache.commons.lang3.StringUtils.isNotBlank(capitalizationObjectCode)) {
                    capitalizationEntry.setFinancialObjectCode(capitalizationObjectCode);
                    capitalizationEntry.setFinancialObject(accountingCycleCachingService
                            .getObjectCode(capitalizationEntry.getUniversityFiscalYear(),
                                    capitalizationEntry.getChartOfAccountsCode(),
                                    capitalizationEntry.getFinancialObjectCode()));
                }

                // financialSubObjectCode should always be changed to dashes for capitalization entries
                capitalizationEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());

                capitalizationEntry.setFinancialObjectTypeCode(scrubbedEntryOption.getFinancialObjectTypeAssetsCd());
                capitalizationEntry.setTransactionLedgerEntryDescription(capitalizationDescription);

                plantFundAccountLookup(scrubbedEntry, capitalizationEntry);

                capitalizationEntry.setUniversityFiscalPeriodCode(scrubbedEntry.getUniversityFiscalPeriodCode());

                createOutputEntry(capitalizationEntry, OUTPUT_GLE_FILE_ps);
                scrubberReport.incrementCapitalizationEntryGenerated();

                // Clear out the id & the ojb version number to make sure we do an insert on the next one
                capitalizationEntry.setVersionNumber(null);
                capitalizationEntry.setEntryId(null);

                // Check system parameters for overriding fund balance object code; otherwise, use the chart fund
                // balance object code.
                String fundBalanceCode = parameterService.getParameterValueAsString(
                    ScrubberStep.class,
                    CAPITALIZATION_OFFSET_CODE);

                ObjectCode fundObjectCode = getFundBalanceObjectCode(fundBalanceCode, capitalizationEntry);

                if (fundObjectCode != null) {
                    capitalizationEntry.setFinancialObjectTypeCode(fundObjectCode.getFinancialObjectTypeCode());
                    capitalizationEntry.setFinancialObjectCode(fundBalanceCode);
                } else {
                    capitalizationEntry.setFinancialObjectCode(scrubbedEntryChart.getFundBalanceObjectCode());
                    if (ObjectUtils.isNotNull(scrubbedEntryChart.getFundBalanceObject())) {
                        capitalizationEntry.setFinancialObjectTypeCode(scrubbedEntryChart.getFundBalanceObject()
                                .getFinancialObjectTypeCode());
                    } else {
                        capitalizationEntry.setFinancialObjectTypeCode(
                                scrubbedEntryOption.getFinObjectTypeFundBalanceCd());
                    }
                }

                populateTransactionDebtCreditCode(scrubbedEntry, capitalizationEntry);

                try {
                    flexibleOffsetAccountService.updateOffset(capitalizationEntry);
                } catch (InvalidFlexibleOffsetException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("processCapitalization() Capitalization Flexible Offset Error: " + e.getMessage());
                    }
                    return e.getMessage();
                }

                createOutputEntry(capitalizationEntry, OUTPUT_GLE_FILE_ps);
                scrubberReport.incrementCapitalizationEntryGenerated();
            }
        } catch (IOException ioe) {
            LOG.error("processCapitalization() Stopped: " + ioe.getMessage());
            throw new RuntimeException("processCapitalization() Stopped: " + ioe.getMessage(), ioe);
        }
        return null;
    }

    /**
     * Generates the plant indebtedness entries
     *
     * @param scrubbedEntry the entry to generated plant indebtedness entries for if necessary
     * @return null if no error, message if error
     */
    protected String processPlantIndebtedness(OriginEntryInformation scrubbedEntry,
            ScrubberReportData scrubberReport) {
        try {
            if (!parameterService.getParameterValueAsBoolean(ScrubberStep.class, PLANT_INDEBTEDNESS_IND)) {
                return null;
            }

            OriginEntryFull plantIndebtednessEntry = OriginEntryFull.copyFromOriginEntryable(scrubbedEntry);

            SystemOptions scrubbedEntryOption = accountingCycleCachingService.getSystemOptions(
                    scrubbedEntry.getUniversityFiscalYear());
            ObjectCode scrubbedEntryObjectCode = accountingCycleCachingService.getObjectCode(
                    scrubbedEntry.getUniversityFiscalYear(), scrubbedEntry.getChartOfAccountsCode(),
                    scrubbedEntry.getFinancialObjectCode());
            Account scrubbedEntryAccount = accountingCycleCachingService.getAccount(
                    scrubbedEntry.getChartOfAccountsCode(), scrubbedEntry.getAccountNumber());
            Chart scrubbedEntryChart = accountingCycleCachingService.getChart(scrubbedEntry.getChartOfAccountsCode());
            if (!ObjectUtils.isNull(scrubbedEntryAccount)) {
                scrubbedEntryAccount.setOrganization(accountingCycleCachingService.getOrganization(
                        scrubbedEntryAccount.getChartOfAccountsCode(), scrubbedEntryAccount.getOrganizationCode()));
            }

            ParameterEvaluator objectSubTypeCodes = (!ObjectUtils.isNull(scrubbedEntryObjectCode)) ?
                    parameterEvaluatorService.getParameterEvaluator(ScrubberStep.class,
                            PLANT_INDEBTEDNESS_OBJ_SUB_TYPE_CODES,
                            scrubbedEntryObjectCode.getFinancialObjectSubTypeCode()) : null;
            ParameterEvaluator subFundGroupCodes = (!ObjectUtils.isNull(scrubbedEntryAccount)) ?
                    parameterEvaluatorService.getParameterEvaluator(ScrubberStep.class,
                            PLANT_INDEBTEDNESS_SUB_FUND_GROUP_CODES, scrubbedEntryAccount.getSubFundGroupCode()) : null;

            if (scrubbedEntry.getFinancialBalanceTypeCode()
                        .equals(scrubbedEntryOption.getActualFinancialBalanceTypeCd())
                    && (subFundGroupCodes != null && subFundGroupCodes.evaluationSucceeds())
                    && (objectSubTypeCodes != null && objectSubTypeCodes.evaluationSucceeds())) {
                plantIndebtednessEntry.setTransactionLedgerEntryDescription(
                        KFSConstants.PLANT_INDEBTEDNESS_ENTRY_DESCRIPTION);
                populateTransactionDebtCreditCode(scrubbedEntry, plantIndebtednessEntry);

                plantIndebtednessEntry.setTransactionScrubberOffsetGenerationIndicator(true);
                createOutputEntry(plantIndebtednessEntry, OUTPUT_GLE_FILE_ps);
                scrubberReport.incrementPlantIndebtednessEntryGenerated();

                // Clear out the id & the ojb version number to make sure we do an insert on the next one
                plantIndebtednessEntry.setVersionNumber(null);
                plantIndebtednessEntry.setEntryId(null);

                // Check system parameters for overriding fund balance object code; otherwise, use
                // the chart fund balance object code.
                String fundBalanceCode = parameterService.getParameterValueAsString(ScrubberStep.class,
                    PLANT_INDEBTEDNESS_OFFSET_CODE);

                ObjectCode fundObjectCode = getFundBalanceObjectCode(fundBalanceCode, plantIndebtednessEntry);
                if (fundObjectCode != null) {
                    plantIndebtednessEntry.setFinancialObjectTypeCode(fundObjectCode.getFinancialObjectTypeCode());
                    plantIndebtednessEntry.setFinancialObjectCode(fundBalanceCode);
                } else {
                    plantIndebtednessEntry.setFinancialObjectTypeCode(scrubbedEntryOption.getFinObjectTypeFundBalanceCd());
                    plantIndebtednessEntry.setFinancialObjectCode(scrubbedEntryChart.getFundBalanceObjectCode());
                }

                plantIndebtednessEntry.setTransactionDebitCreditCode(scrubbedEntry.getTransactionDebitCreditCode());

                plantIndebtednessEntry.setTransactionScrubberOffsetGenerationIndicator(true);
                plantIndebtednessEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());

                try {
                    flexibleOffsetAccountService.updateOffset(plantIndebtednessEntry);
                } catch (InvalidFlexibleOffsetException e) {
                    LOG.error("processPlantIndebtedness() Flexible Offset Exception (1)", e);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("processPlantIndebtedness() Plant Indebtedness Flexible Offset Error: " +
                                e.getMessage());
                    }
                    return e.getMessage();
                }

                createOutputEntry(plantIndebtednessEntry, OUTPUT_GLE_FILE_ps);
                scrubberReport.incrementPlantIndebtednessEntryGenerated();

                // Clear out the id & the ojb version number to make sure we do an insert on the next one
                plantIndebtednessEntry.setVersionNumber(null);
                plantIndebtednessEntry.setEntryId(null);

                plantIndebtednessEntry.setFinancialObjectCode(scrubbedEntry.getFinancialObjectCode());
                plantIndebtednessEntry.setFinancialObjectTypeCode(scrubbedEntry.getFinancialObjectTypeCode());
                plantIndebtednessEntry.setTransactionDebitCreditCode(scrubbedEntry.getTransactionDebitCreditCode());

                plantIndebtednessEntry.setTransactionLedgerEntryDescription(
                        scrubbedEntry.getTransactionLedgerEntryDescription());

                plantIndebtednessEntry.setAccountNumber(scrubbedEntry.getAccountNumber());
                plantIndebtednessEntry.setSubAccountNumber(scrubbedEntry.getSubAccountNumber());

                plantIndebtednessEntry.setAccountNumber(scrubbedEntryAccount.getOrganization()
                        .getCampusPlantAccountNumber());
                plantIndebtednessEntry.setChartOfAccountsCode(scrubbedEntryAccount.getOrganization()
                        .getCampusPlantChartCode());

                plantIndebtednessEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
                plantIndebtednessEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());

                String litGenPlantXferFrom = transferDescription + " " + scrubbedEntry.getChartOfAccountsCode() +
                        " " + scrubbedEntry.getAccountNumber();
                plantIndebtednessEntry.setTransactionLedgerEntryDescription(litGenPlantXferFrom);

                createOutputEntry(plantIndebtednessEntry, OUTPUT_GLE_FILE_ps);
                scrubberReport.incrementPlantIndebtednessEntryGenerated();

                // Clear out the id & the ojb version number to make sure we do an insert on the next one
                plantIndebtednessEntry.setVersionNumber(null);
                plantIndebtednessEntry.setEntryId(null);

                plantIndebtednessEntry.setFinancialObjectCode(scrubbedEntryChart.getFundBalanceObjectCode());
                plantIndebtednessEntry.setFinancialObjectTypeCode(scrubbedEntryOption.getFinObjectTypeFundBalanceCd());
                plantIndebtednessEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());

                populateTransactionDebtCreditCode(scrubbedEntry, plantIndebtednessEntry);

                try {
                    flexibleOffsetAccountService.updateOffset(plantIndebtednessEntry);
                } catch (InvalidFlexibleOffsetException e) {
                    LOG.error("processPlantIndebtedness() Flexible Offset Exception (2)", e);
                    LOG.debug("processPlantIndebtedness() Plant Indebtedness Flexible Offset Error: " +
                            e.getMessage());
                    return e.getMessage();
                }

                createOutputEntry(plantIndebtednessEntry, OUTPUT_GLE_FILE_ps);
                scrubberReport.incrementPlantIndebtednessEntryGenerated();
            }
        } catch (IOException ioe) {
            LOG.error("processPlantIndebtedness() Stopped: " + ioe.getMessage());
            throw new RuntimeException("processPlantIndebtedness() Stopped: " + ioe.getMessage(), ioe);
        }
        return null;
    }

    /**
     * Generate the liability entries for the entry if necessary
     *
     * @param scrubbedEntry the entry to generate liability entries for if necessary
     * @return null if no error, message if error
     */
    protected String processLiabilities(OriginEntryInformation scrubbedEntry, ScrubberReportData scrubberReport) {
        try {
            if (!parameterService.getParameterValueAsBoolean(ScrubberStep.class, LIABILITY_IND)) {
                return null;
            }

            Chart scrubbedEntryChart = accountingCycleCachingService.getChart(scrubbedEntry.getChartOfAccountsCode());
            SystemOptions scrubbedEntryOption = accountingCycleCachingService.getSystemOptions(
                    scrubbedEntry.getUniversityFiscalYear());
            ObjectCode scrubbedEntryFinancialObject = accountingCycleCachingService.getObjectCode(
                    scrubbedEntry.getUniversityFiscalYear(), scrubbedEntry.getChartOfAccountsCode(),
                    scrubbedEntry.getFinancialObjectCode());
            Account scrubbedEntryAccount = accountingCycleCachingService.getAccount(
                    scrubbedEntry.getChartOfAccountsCode(), scrubbedEntry.getAccountNumber());

            ParameterEvaluator chartCodes = (!ObjectUtils.isNull(scrubbedEntry)) ? parameterEvaluatorService
                    .getParameterEvaluator(ScrubberStep.class, LIABILITY_CHART_CODES,
                            scrubbedEntry.getChartOfAccountsCode()) : null;
            ParameterEvaluator docTypeCodes = (!ObjectUtils.isNull(scrubbedEntry)) ? parameterEvaluatorService
                    .getParameterEvaluator(ScrubberStep.class, LIABILITY_DOC_TYPE_CODES,
                            scrubbedEntry.getFinancialDocumentTypeCode()) : null;
            ParameterEvaluator fiscalPeriods = (!ObjectUtils.isNull(scrubbedEntry)) ? parameterEvaluatorService
                    .getParameterEvaluator(ScrubberStep.class, LIABILITY_FISCAL_PERIOD_CODES,
                            scrubbedEntry.getUniversityFiscalPeriodCode()) : null;
            ParameterEvaluator objSubTypeCodes = (!ObjectUtils.isNull(scrubbedEntryFinancialObject)) ?
                    parameterEvaluatorService.getParameterEvaluator(ScrubberStep.class,
                            LIABILITY_OBJ_SUB_TYPE_CODES,
                            scrubbedEntryFinancialObject.getFinancialObjectSubTypeCode()) : null;
            ParameterEvaluator subFundGroupCodes = (!ObjectUtils.isNull(scrubbedEntryAccount)) ?
                    parameterEvaluatorService.getParameterEvaluator(ScrubberStep.class, LIABILITY_SUB_FUND_GROUP_CODES,
                            scrubbedEntryAccount.getSubFundGroupCode()) : null;

            if (scrubbedEntry.getFinancialBalanceTypeCode().equals(
                        scrubbedEntryOption.getActualFinancialBalanceTypeCd())
                    && scrubbedEntry.getUniversityFiscalYear() > 1995
                    && (docTypeCodes != null && docTypeCodes.evaluationSucceeds())
                    && (fiscalPeriods != null && fiscalPeriods.evaluationSucceeds())
                    && (objSubTypeCodes != null && objSubTypeCodes.evaluationSucceeds())
                    && (subFundGroupCodes != null && subFundGroupCodes.evaluationSucceeds())
                    && (chartCodes != null && chartCodes.evaluationSucceeds())) {
                OriginEntryFull liabilityEntry = OriginEntryFull.copyFromOriginEntryable(scrubbedEntry);

                liabilityEntry.setFinancialObjectCode(parameterService.getParameterValueAsString(ScrubberStep.class,
                        LIABILITY_OBJECT_CODE));
                liabilityEntry.setFinancialObjectTypeCode(scrubbedEntryOption.getFinObjectTypeLiabilitiesCode());

                liabilityEntry.setTransactionDebitCreditCode(scrubbedEntry.getTransactionDebitCreditCode());
                liabilityEntry.setTransactionLedgerEntryDescription(liabilityDescription);
                plantFundAccountLookup(scrubbedEntry, liabilityEntry);

                createOutputEntry(liabilityEntry, OUTPUT_GLE_FILE_ps);
                scrubberReport.incrementLiabilityEntryGenerated();

                // Clear out the id & the ojb version number to make sure we do an insert on the next one
                liabilityEntry.setVersionNumber(null);
                liabilityEntry.setEntryId(null);

                // Check system parameters for overriding fund balance object code; otherwise, use
                // the chart fund balance object code.
                String fundBalanceCode = parameterService.getParameterValueAsString(ScrubberStep.class,
                    LIABILITY_OFFSET_CODE);

                ObjectCode fundObjectCode = getFundBalanceObjectCode(fundBalanceCode, liabilityEntry);
                if (fundObjectCode != null) {
                    liabilityEntry.setFinancialObjectTypeCode(fundObjectCode.getFinancialObjectTypeCode());
                    liabilityEntry.setFinancialObjectCode(fundBalanceCode);
                } else {
                    // ... and now generate the offset half of the liability entry
                    liabilityEntry.setFinancialObjectCode(scrubbedEntryChart.getFundBalanceObjectCode());
                    if (ObjectUtils.isNotNull(scrubbedEntryChart.getFundBalanceObject())) {
                        liabilityEntry.setFinancialObjectTypeCode(scrubbedEntryChart.getFundBalanceObject()
                                .getFinancialObjectTypeCode());
                    } else {
                        liabilityEntry.setFinancialObjectTypeCode(scrubbedEntryOption.getFinObjectTypeFundBalanceCd());
                    }
                }

                if (liabilityEntry.isDebit()) {
                    liabilityEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
                } else {
                    liabilityEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
                }

                try {
                    flexibleOffsetAccountService.updateOffset(liabilityEntry);
                } catch (InvalidFlexibleOffsetException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("processLiabilities() Liability Flexible Offset Error: " + e.getMessage());
                    }
                    return e.getMessage();
                }

                createOutputEntry(liabilityEntry, OUTPUT_GLE_FILE_ps);
                scrubberReport.incrementLiabilityEntryGenerated();
            }
        } catch (IOException ioe) {
            LOG.error("processLiabilities() Stopped: " + ioe.getMessage());
            throw new RuntimeException("processLiabilities() Stopped: " + ioe.getMessage(), ioe);
        }
        return null;
    }

    /**
     * @param fundBalanceCode
     * @param originEntryFull
     * @return
     */
    protected ObjectCode getFundBalanceObjectCode(String fundBalanceCode, OriginEntryFull originEntryFull) {
        ObjectCode fundBalanceObjectCode = null;
        if (fundBalanceCode != null) {
            Map<String, Object> criteriaMap = new HashMap<>();
            criteriaMap.put("universityFiscalYear", originEntryFull.getUniversityFiscalYear());
            criteriaMap.put("chartOfAccountsCode", originEntryFull.getChartOfAccountsCode());
            criteriaMap.put("financialObjectCode", fundBalanceCode);

            fundBalanceObjectCode = businessObjectService.findByPrimaryKey(ObjectCode.class, criteriaMap);
        }

        return fundBalanceObjectCode;
    }

    /**
     * @param scrubbedEntry
     * @param fullEntry
     */
    protected void populateTransactionDebtCreditCode(OriginEntryInformation scrubbedEntry, OriginEntryFull fullEntry) {
        if (scrubbedEntry.isDebit()) {
            fullEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
        } else {
            fullEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
        }
    }

    /**
     * Updates the entries with the proper chart and account for the plant fund
     *
     * @param scrubbedEntry  basis for plant fund entry
     * @param liabilityEntry liability entry
     */
    protected void plantFundAccountLookup(OriginEntryInformation scrubbedEntry, OriginEntryFull liabilityEntry) {
        liabilityEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
        ObjectCode scrubbedEntryObjectCode = accountingCycleCachingService.getObjectCode(
                scrubbedEntry.getUniversityFiscalYear(), scrubbedEntry.getChartOfAccountsCode(),
                scrubbedEntry.getFinancialObjectCode());
        Account scrubbedEntryAccount = accountingCycleCachingService.getAccount(
                scrubbedEntry.getChartOfAccountsCode(), scrubbedEntry.getAccountNumber());
        scrubbedEntryAccount.setOrganization(accountingCycleCachingService
                .getOrganization(scrubbedEntryAccount.getChartOfAccountsCode(),
                        scrubbedEntryAccount.getOrganizationCode()));

        if (!ObjectUtils.isNull(scrubbedEntryAccount) && !ObjectUtils.isNull(scrubbedEntryObjectCode)) {
            String objectSubTypeCode = scrubbedEntryObjectCode.getFinancialObjectSubTypeCode();
            ParameterEvaluator campusObjSubTypeCodes = parameterEvaluatorService
                    .getParameterEvaluator(ScrubberStep.class, PLANT_FUND_CAMPUS_OBJECT_SUB_TYPE_CODES,
                            objectSubTypeCode);
            ParameterEvaluator orgObjSubTypeCodes = parameterEvaluatorService.getParameterEvaluator(ScrubberStep.class,
                    PLANT_FUND_ORG_OBJECT_SUB_TYPE_CODES, objectSubTypeCode);

            if (campusObjSubTypeCodes.evaluationSucceeds()) {
                liabilityEntry.setAccountNumber(scrubbedEntryAccount.getOrganization().getCampusPlantAccountNumber());
                liabilityEntry.setChartOfAccountsCode(scrubbedEntryAccount.getOrganization().getCampusPlantChartCode());
            } else if (orgObjSubTypeCodes.evaluationSucceeds()) {
                liabilityEntry.setAccountNumber(scrubbedEntryAccount.getOrganization()
                        .getOrganizationPlantAccountNumber());
                liabilityEntry.setChartOfAccountsCode(scrubbedEntryAccount.getOrganization()
                        .getOrganizationPlantChartCode());
            }
        }
    }

    /**
     * The purpose of this method is to generate a "Cost Share Encumbrance" transaction for the current transaction
     * and its offset.
     *
     * @param scrubbedEntry the entry to perhaps create a cost share encumbrance for
     * @return a message if there was an error encountered generating the entries, or (hopefully) null if no errors
     *         were encountered
     */
    protected ScrubberProcessTransactionError generateCostShareEncumbranceEntries(OriginEntryInformation scrubbedEntry,
            ScrubberReportData scrubberReport) {
        try {
            LOG.debug("generateCostShareEncumbranceEntries() started");

            OriginEntryFull costShareEncumbranceEntry = OriginEntryFull.copyFromOriginEntryable(scrubbedEntry);

            // First 28 characters of the description, padding to 28 if shorter)
            String description = (scrubbedEntry.getTransactionLedgerEntryDescription() +
                    GeneralLedgerConstants.getSpaceTransactionLedgetEntryDescription()).substring(0,
                    COST_SHARE_ENCUMBRANCE_ENTRY_MAXLENGTH) + "FR-" +
                    costShareEncumbranceEntry.getChartOfAccountsCode() +
                    costShareEncumbranceEntry.getAccountNumber();
            costShareEncumbranceEntry.setTransactionLedgerEntryDescription(description);

            A21SubAccount scrubbedEntryA21SubAccount = accountingCycleCachingService.getA21SubAccount(
                    scrubbedEntry.getChartOfAccountsCode(), scrubbedEntry.getAccountNumber(),
                    scrubbedEntry.getSubAccountNumber());
            SystemOptions scrubbedEntryOption = accountingCycleCachingService.getSystemOptions(
                    scrubbedEntry.getUniversityFiscalYear());

            costShareEncumbranceEntry.setChartOfAccountsCode(
                    scrubbedEntryA21SubAccount.getCostShareChartOfAccountCode());
            costShareEncumbranceEntry.setAccountNumber(scrubbedEntryA21SubAccount.getCostShareSourceAccountNumber());
            costShareEncumbranceEntry.setSubAccountNumber(
                    scrubbedEntryA21SubAccount.getCostShareSourceSubAccountNumber());

            if (!StringUtils.hasText(costShareEncumbranceEntry.getSubAccountNumber())) {
                costShareEncumbranceEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
            }

            costShareEncumbranceEntry.setFinancialBalanceTypeCode(
                    scrubbedEntryOption.getCostShareEncumbranceBalanceTypeCd());
            setCostShareObjectCode(costShareEncumbranceEntry, scrubbedEntry);
            costShareEncumbranceEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            costShareEncumbranceEntry.setTransactionLedgerEntrySequenceNumber(0);

            if (!StringUtils.hasText(scrubbedEntry.getTransactionDebitCreditCode())) {
                if (scrubbedEntry.getTransactionLedgerEntryAmount().isPositive()) {
                    costShareEncumbranceEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
                } else {
                    costShareEncumbranceEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
                    costShareEncumbranceEntry.setTransactionLedgerEntryAmount(
                            scrubbedEntry.getTransactionLedgerEntryAmount().negated());
                }
            }

            costShareEncumbranceEntry.setTransactionDate(runDate);

            costShareEncumbranceEntry.setTransactionScrubberOffsetGenerationIndicator(true);
            createOutputEntry(costShareEncumbranceEntry, OUTPUT_GLE_FILE_ps);
            scrubberReport.incrementCostShareEncumbranceGenerated();

            OriginEntryFull costShareEncumbranceOffsetEntry = new OriginEntryFull(costShareEncumbranceEntry);
            costShareEncumbranceOffsetEntry.setTransactionLedgerEntryDescription(offsetDescription);
            OffsetDefinition offset = accountingCycleCachingService.getOffsetDefinition(
                    costShareEncumbranceEntry.getUniversityFiscalYear(),
                    costShareEncumbranceEntry.getChartOfAccountsCode(),
                    costShareEncumbranceEntry.getFinancialDocumentTypeCode(),
                    costShareEncumbranceEntry.getFinancialBalanceTypeCode());

            if (offset != null) {
                if (offset.getFinancialObject() == null) {
                    LOG.debug("generateCostShareEncumbranceEntries() object code not found");
                    String offsetKey = offset.getUniversityFiscalYear() + "-" + offset.getChartOfAccountsCode() +
                            "-" + offset.getFinancialObjectCode();
                    return new ScrubberProcessTransactionError(costShareEncumbranceEntry, new Message(
                            configurationService.getPropertyValueAsString(
                                    KFSKeyConstants.ERROR_NO_OBJECT_FOR_OBJECT_ON_OFSD) + "(" + offsetKey + ")",
                            Message.TYPE_FATAL));
                }
                costShareEncumbranceOffsetEntry.setFinancialObjectCode(offset.getFinancialObjectCode());
                costShareEncumbranceOffsetEntry.setFinancialObject(offset.getFinancialObject());
                costShareEncumbranceOffsetEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            } else {
                LOG.debug("generateCostShareEncumbranceEntries() offset not found");
                String offsetKey = "Cost share encumbrance " + costShareEncumbranceEntry.getUniversityFiscalYear() +
                        "-" + costShareEncumbranceEntry.getChartOfAccountsCode() + "-" +
                        costShareEncumbranceEntry.getFinancialDocumentTypeCode() + "-" +
                        costShareEncumbranceEntry.getFinancialBalanceTypeCode();
                return new ScrubberProcessTransactionError(costShareEncumbranceEntry, new Message(configurationService
                        .getPropertyValueAsString(KFSKeyConstants.ERROR_OFFSET_DEFINITION_NOT_FOUND) + "(" +
                        offsetKey + ")", Message.TYPE_FATAL));
            }

            costShareEncumbranceOffsetEntry.setFinancialObjectTypeCode(
                    offset.getFinancialObject().getFinancialObjectTypeCode());

            if (costShareEncumbranceEntry.isCredit()) {
                costShareEncumbranceOffsetEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            } else {
                costShareEncumbranceOffsetEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            }

            costShareEncumbranceOffsetEntry.setTransactionDate(runDate);
            costShareEncumbranceOffsetEntry.setOrganizationDocumentNumber(null);
            costShareEncumbranceOffsetEntry.setProjectCode(KFSConstants.getDashProjectCode());
            costShareEncumbranceOffsetEntry.setOrganizationReferenceId(null);
            costShareEncumbranceOffsetEntry.setReferenceFinancialDocumentTypeCode(null);
            costShareEncumbranceOffsetEntry.setReferenceFinancialSystemOriginationCode(null);
            costShareEncumbranceOffsetEntry.setReferenceFinancialDocumentNumber(null);
            costShareEncumbranceOffsetEntry.setReversalDate(null);
            costShareEncumbranceOffsetEntry.setTransactionEncumbranceUpdateCode(null);

            costShareEncumbranceOffsetEntry.setTransactionScrubberOffsetGenerationIndicator(true);

            try {
                flexibleOffsetAccountService.updateOffset(costShareEncumbranceOffsetEntry);
            } catch (InvalidFlexibleOffsetException e) {
                Message m = new Message(e.getMessage(), Message.TYPE_FATAL);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("generateCostShareEncumbranceEntries() Cost Share Encumbrance Flexible Offset Error: " +
                            e.getMessage());
                }
                return new ScrubberProcessTransactionError(costShareEncumbranceOffsetEntry, m);
            }

            createOutputEntry(costShareEncumbranceOffsetEntry, OUTPUT_GLE_FILE_ps);
            scrubberReport.incrementCostShareEncumbranceGenerated();
        } catch (IOException ioe) {
            LOG.error("generateCostShareEncumbranceEntries() Stopped: " + ioe.getMessage());
            throw new RuntimeException("generateCostShareEncumbranceEntries() Stopped: " + ioe.getMessage(), ioe);
        }
        LOG.debug("generateCostShareEncumbranceEntries() returned successfully");
        return null;
    }

    /**
     * Sets the proper cost share object code in an entry and its offset
     *
     * @param costShareEntry GL Entry for cost share
     * @param originEntry    Scrubbed GL Entry that this is based on
     */
    @Override
    public void setCostShareObjectCode(OriginEntryFull costShareEntry, OriginEntryInformation originEntry) {
        ObjectCode originEntryFinancialObject = accountingCycleCachingService.getObjectCode(
                originEntry.getUniversityFiscalYear(), originEntry.getChartOfAccountsCode(),
                originEntry.getFinancialObjectCode());

        if (originEntryFinancialObject == null) {
            addTransactionError(
                    configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_OBJECT_CODE_NOT_FOUND),
                    originEntry.getFinancialObjectCode(), Message.TYPE_FATAL);
        }

        String originEntryObjectLevelCode = (originEntryFinancialObject == null) ? "" : originEntryFinancialObject
                .getFinancialObjectLevelCode();

        // General rules
        String param = parameterService.getSubParameterValueAsString(ScrubberStep.class,
                GLParameterConstants.COST_SHARE_OBJECT_CODE_BY_LEVEL,
                originEntryObjectLevelCode);
        if (param == null) {
            param = parameterService.getSubParameterValueAsString(ScrubberStep.class,
                    GLParameterConstants.COST_SHARE_OBJECT_CODE_BY_LEVEL, "DEFAULT");
            if (param == null) {
                throw new RuntimeException("Unable to determine cost sharing object code from object level.  " +
                        "Default entry missing.");
            }
        }
        String financialOriginEntryObjectCode = param;

        // Lookup the new object code
        ObjectCode objectCode = accountingCycleCachingService.getObjectCode(costShareEntry.getUniversityFiscalYear(),
                costShareEntry.getChartOfAccountsCode(), financialOriginEntryObjectCode);
        if (objectCode != null) {
            costShareEntry.setFinancialObjectTypeCode(objectCode.getFinancialObjectTypeCode());
            costShareEntry.setFinancialObjectCode(financialOriginEntryObjectCode);
        } else {
            addTransactionError(
                    configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_COST_SHARE_OBJECT_NOT_FOUND),
                    costShareEntry.getFinancialObjectCode(), Message.TYPE_FATAL);
        }
    }

    /**
     * The purpose of this method is to build the actual offset transaction. It does this by performing the following
     * steps:
     * 1. Getting the offset object code and offset subobject code from the GL Offset Definition Table.
     * 2. For the offset object code it needs to get the associated object type, object subtype, and object active
     * code.
     *
     * @param scrubbedEntry entry to determine if an offset is needed for
     * @return true if an offset would be needed for this entry, false otherwise
     */
    protected boolean generateOffset(OriginEntryInformation scrubbedEntry, ScrubberReportData scrubberReport) {
        try {
            LOG.debug("generateOffset() started");

            // There was no previous unit of work so we need no offset
            if (scrubbedEntry == null) {
                return true;
            }

            // If there was an error, don't generate an offset since the record was pulled
            // and the rest of the document's records will be demerged
            if (scrubberProcessUnitOfWork.isErrorsFound()) {
                return true;
            }

            // If the offset amount is zero, don't bother to lookup the offset definition ...
            if (scrubberProcessUnitOfWork.getOffsetAmount().isZero()) {
                return true;
            }

            ParameterEvaluator docTypeRule = parameterEvaluatorService.getParameterEvaluator(ScrubberStep.class,
                    GLParameterConstants.OFFSET_DOC_TYPE_CODES, scrubbedEntry.getFinancialDocumentTypeCode());
            if (!docTypeRule.evaluationSucceeds()) {
                return true;
            }

            // do nothing if flexible offset is enabled and scrubber offset indicator of the document
            // type code is turned off in the document type table
            if (flexibleOffsetAccountService.getEnabled()
                    && !shouldScrubberGenerateOffsetsForDocType(scrubbedEntry.getFinancialDocumentTypeCode())) {
                return true;
            }

            // Create an offset
            OriginEntryFull offsetEntry = OriginEntryFull.copyFromOriginEntryable(scrubbedEntry);
            offsetEntry.setTransactionLedgerEntryDescription(offsetDescription);

            //of course this method should go elsewhere, not in ScrubberValidator!
            OffsetDefinition offsetDefinition = accountingCycleCachingService.getOffsetDefinition(
                    scrubbedEntry.getUniversityFiscalYear(), scrubbedEntry.getChartOfAccountsCode(),
                    scrubbedEntry.getFinancialDocumentTypeCode(), scrubbedEntry.getFinancialBalanceTypeCode());
            if (offsetDefinition != null) {
                if (offsetDefinition.getFinancialObject() == null) {
                    String offsetKey = offsetDefinition.getUniversityFiscalYear() + "-" +
                            offsetDefinition.getChartOfAccountsCode() + "-" +
                            offsetDefinition.getFinancialObjectCode();
                    putTransactionError(offsetEntry, configurationService.getPropertyValueAsString(
                            KFSKeyConstants.ERROR_OFFSET_DEFINITION_OBJECT_CODE_NOT_FOUND), offsetKey,
                            Message.TYPE_FATAL);

                    createOutputEntry(offsetEntry, OUTPUT_ERR_FILE_ps);
                    scrubberReport.incrementErrorRecordWritten();
                    return false;
                }

                offsetEntry.setFinancialObject(offsetDefinition.getFinancialObject());
                offsetEntry.setFinancialObjectCode(offsetDefinition.getFinancialObjectCode());

                offsetEntry.setFinancialSubObject(null);
                offsetEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            } else {
                String message = "Unit of work offset " + scrubbedEntry.getUniversityFiscalYear() + "-" +
                        scrubbedEntry.getChartOfAccountsCode() + "-" +
                        scrubbedEntry.getFinancialDocumentTypeCode() + "-" +
                        scrubbedEntry.getFinancialBalanceTypeCode();
                putTransactionError(offsetEntry, configurationService.getPropertyValueAsString(
                        KFSKeyConstants.ERROR_OFFSET_DEFINITION_NOT_FOUND), message, Message.TYPE_FATAL);

                createOutputEntry(offsetEntry, OUTPUT_ERR_FILE_ps);
                scrubberReport.incrementErrorRecordWritten();
                return false;
            }

            offsetEntry.setFinancialObjectTypeCode(offsetEntry.getFinancialObject().getFinancialObjectTypeCode());
            offsetEntry.setTransactionLedgerEntryAmount(scrubberProcessUnitOfWork.getOffsetAmount());

            if (scrubberProcessUnitOfWork.getOffsetAmount().isPositive()) {
                offsetEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            } else {
                offsetEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
                offsetEntry.setTransactionLedgerEntryAmount(scrubberProcessUnitOfWork.getOffsetAmount().negated());
            }

            offsetEntry.setOrganizationDocumentNumber(null);
            offsetEntry.setOrganizationReferenceId(null);
            offsetEntry.setReferenceFinancialDocumentTypeCode(null);
            offsetEntry.setReferenceFinancialSystemOriginationCode(null);
            offsetEntry.setReferenceFinancialDocumentNumber(null);
            offsetEntry.setTransactionEncumbranceUpdateCode(null);
            offsetEntry.setProjectCode(KFSConstants.getDashProjectCode());
            offsetEntry.setTransactionDate(getTransactionDateForOffsetEntry(scrubbedEntry));

            try {
                flexibleOffsetAccountService.updateOffset(offsetEntry);
            } catch (InvalidFlexibleOffsetException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("generateOffset() Offset Flexible Offset Error: " + e.getMessage());
                }
                putTransactionError(offsetEntry, e.getMessage(), "", Message.TYPE_FATAL);
                return true;
            }

            createOutputEntry(offsetEntry, OUTPUT_GLE_FILE_ps);
            scrubberReport.incrementOffsetEntryGenerated();

        } catch (IOException ioe) {
            LOG.error("generateOffset() Stopped: " + ioe.getMessage());
            throw new RuntimeException("generateOffset() Stopped: " + ioe.getMessage(), ioe);
        }

        return true;
    }

    protected void createOutputEntry(OriginEntryInformation entry, PrintStream ps) throws IOException {
        try {
            ps.printf("%s\n", entry.getLine());
        } catch (Exception e) {
            throw new IOException(e.toString());
        }
    }

    protected void createOutputEntry(String line, PrintStream ps) throws IOException {
        try {
            ps.printf("%s\n", line);
        } catch (Exception e) {
            throw new IOException(e.toString());
        }
    }

    /**
     * Add an error message to the list of messages for this transaction
     *
     * @param errorMessage Error message
     * @param errorValue   Value that is in error
     * @param type         Type of error (Fatal or Warning)
     */
    protected void addTransactionError(String errorMessage, String errorValue, int type) {
        transactionErrors.add(new Message(errorMessage + " (" + errorValue + ")", type));
    }

    /**
     * Puts a transaction error into this instance's collection of errors
     *
     * @param s            a transaction that caused a scrubber error
     * @param errorMessage the message of what caused the error
     * @param errorValue   the value in error
     * @param type         the type of error
     */
    protected void putTransactionError(Transaction s, String errorMessage, String errorValue, int type) {
        Message m = new Message(errorMessage + "(" + errorValue + ")", type);
        scrubberReportWriterService.writeError(s, m);
    }

    /**
     * Determines if the scrubber should generate offsets for the given document type
     *
     * @param docTypeCode the document type code to check if it generates scrubber offsets
     * @return true if the scrubber should generate offsets for this doc type, false otherwise
     */
    protected boolean shouldScrubberGenerateOffsetsForDocType(String docTypeCode) {
        return parameterEvaluatorService.getParameterEvaluator(ScrubberStep.class,
                GLParameterConstants.DOCUMENT_TYPES_REQUIRING_FLEXIBLE_OFFSET_BALANCING_ENTRIES, docTypeCode)
                .evaluationSucceeds();
    }

    /**
     * This method modifies the run date if it is before the cutoff time specified by the RunTimeService See
     * KULRNE-70 This method is public to facilitate unit testing
     *
     * @param currentDate the date the scrubber should report as having run on
     * @return the run date
     */
    @Override
    public Date calculateRunDate(java.util.Date currentDate) {
        return new Date(runDateService.calculateRunDate(currentDate).getTime());
    }

    protected boolean checkingBypassEntry(String financialBalanceTypeCode, String desc,
            DemergerReportData demergerReport) {
        String transactionType = getTransactionType(financialBalanceTypeCode, desc);

        if (TRANSACTION_TYPE_COST_SHARE_ENCUMBRANCE.equals(transactionType)) {
            demergerReport.incrementCostShareEncumbranceTransactionsBypassed();
            return true;
        } else if (TRANSACTION_TYPE_OFFSET.equals(transactionType)) {
            demergerReport.incrementOffsetTransactionsBypassed();
            return true;
        } else if (TRANSACTION_TYPE_CAPITALIZATION.equals(transactionType)) {
            demergerReport.incrementCapitalizationTransactionsBypassed();
            return true;
        } else if (TRANSACTION_TYPE_LIABILITY.equals(transactionType)) {
            demergerReport.incrementLiabilityTransactionsBypassed();
            return true;
        } else if (TRANSACTION_TYPE_TRANSFER.equals(transactionType)) {
            demergerReport.incrementTransferTransactionsBypassed();
            return true;
        } else if (TRANSACTION_TYPE_COST_SHARE.equals(transactionType)) {
            demergerReport.incrementCostShareTransactionsBypassed();
            return true;
        }

        return false;
    }

    protected String checkAndSetTransactionTypeCostShare(String financialBalanceTypeCode, String desc,
            String currentValidLine) {
        // Read all the transactions in the valid group and update the cost share transactions
        String transactionType = getTransactionType(financialBalanceTypeCode, desc);
        if (TRANSACTION_TYPE_COST_SHARE.equals(transactionType)) {
            OriginEntryFull transaction = new OriginEntryFull();
            transaction.setFromTextFileForBatch(currentValidLine, 0);

            transaction.setFinancialDocumentTypeCode(KFSConstants.TRANSFER_FUNDS);
            transaction.setFinancialSystemOriginationCode(KFSConstants.SubAccountType.COST_SHARE);

            String docNbr = COST_SHARE_CODE + desc.substring(36, 38) + "/" + desc.substring(38, 40);
            transaction.setDocumentNumber(docNbr);
            transaction.setTransactionLedgerEntryDescription(desc.substring(0,
                    DEMERGER_TRANSACTION_LEDGET_ENTRY_DESCRIPTION));

            currentValidLine = transaction.getLine();
        }

        return currentValidLine;
    }

    /**
     * Generates the scrubber listing report for the GLCP document
     *
     * @param documentNumber the document number of the GLCP document
     */
    protected void generateScrubberTransactionListingReport(String documentNumber, String inputFileName) {
        try {
            scrubberListingReportWriterService.setDocumentNumber(documentNumber);
            ((WrappingBatchService) scrubberListingReportWriterService).initialize();
            new TransactionListingReport().generateReport(scrubberListingReportWriterService,
                    new OriginEntryFileIterator(new File(inputFileName)));
        } finally {
            ((WrappingBatchService) scrubberListingReportWriterService).destroy();
        }
    }

    /**
     * Generates the scrubber report that lists out the input origin entries with blank balance type codes.
     */
    protected void generateScrubberBlankBalanceTypeCodeReport(String inputFileName) {
        OriginEntryFilter blankBalanceTypeFilter = originEntry -> {
            boolean acceptFlag = false;
            String financialBalancetype = originEntry.getFinancialBalanceTypeCode();
            BalanceType originEntryBalanceType = accountingCycleCachingService.getBalanceType(financialBalancetype);
            if (ObjectUtils.isNull(originEntryBalanceType)) {
                acceptFlag = true;
                for (int i = 0; i < financialBalancetype.length(); i++) {
                    if (financialBalancetype.charAt(i) != ' ') {
                        acceptFlag = false;
                        break;
                    }
                }
            }
            return acceptFlag;
        };
        Iterator<OriginEntryFull> blankBalanceOriginEntries = new FilteringOriginEntryFileIterator(
                new File(inputFileName), blankBalanceTypeFilter);
        new TransactionListingReport().generateReport(scrubberBadBalanceListingReportWriterService,
                blankBalanceOriginEntries);
    }

    protected void generateDemergerRemovedTransactionsReport(String errorFileName) {
        OriginEntryFileIterator removedTransactions = new OriginEntryFileIterator(new File(errorFileName));
        new TransactionListingReport().generateReport(demergerRemovedTransactionsListingReportWriterService,
                removedTransactions);
    }

    protected void handleTransactionError(Transaction errorTransaction, Message message) {
        if (collectorMode) {
            List<Message> messages = scrubberReportErrors.computeIfAbsent(errorTransaction, k -> new ArrayList<>());
            messages.add(message);
        } else {
            scrubberReportWriterService.writeError(errorTransaction, message);
        }
    }

    protected void handleTransactionErrors(Transaction errorTransaction, List<Message> messages) {
        if (collectorMode) {
            for (Message message : messages) {
                handleTransactionError(errorTransaction, message);
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Errors on transaction: " + errorTransaction);
                for (Message message : messages) {
                    LOG.debug(message);
                }
            }
            scrubberReportWriterService.writeError(errorTransaction, messages);
        }
    }

    protected void handleEndOfScrubberReport(ScrubberReportData scrubberReport) {
        if (!collectorMode) {
            scrubberReportWriterService.writeStatisticLine("UNSCRUBBED RECORDS READ              %,9d",
                    scrubberReport.getNumberOfUnscrubbedRecordsRead());
            scrubberReportWriterService.writeStatisticLine("SCRUBBED RECORDS WRITTEN             %,9d",
                    scrubberReport.getNumberOfScrubbedRecordsWritten());
            scrubberReportWriterService.writeStatisticLine("ERROR RECORDS WRITTEN                %,9d",
                    scrubberReport.getNumberOfErrorRecordsWritten());
            scrubberReportWriterService.writeStatisticLine("OFFSET ENTRIES GENERATED             %,9d",
                    scrubberReport.getNumberOfOffsetEntriesGenerated());
            scrubberReportWriterService.writeStatisticLine("CAPITALIZATION ENTRIES GENERATED     %,9d",
                    scrubberReport.getNumberOfCapitalizationEntriesGenerated());
            scrubberReportWriterService.writeStatisticLine("LIABILITY ENTRIES GENERATED          %,9d",
                    scrubberReport.getNumberOfLiabilityEntriesGenerated());
            scrubberReportWriterService.writeStatisticLine("PLANT INDEBTEDNESS ENTRIES GENERATED %,9d",
                    scrubberReport.getNumberOfPlantIndebtednessEntriesGenerated());
            scrubberReportWriterService.writeStatisticLine("COST SHARE ENTRIES GENERATED         %,9d",
                    scrubberReport.getNumberOfCostShareEntriesGenerated());
            scrubberReportWriterService.writeStatisticLine("COST SHARE ENC ENTRIES GENERATED     %,9d",
                    scrubberReport.getNumberOfCostShareEncumbrancesGenerated());
            scrubberReportWriterService.writeStatisticLine("TOTAL OUTPUT RECORDS WRITTEN         %,9d",
                    scrubberReport.getTotalNumberOfRecordsWritten());
            scrubberReportWriterService.writeStatisticLine("EXPIRED ACCOUNTS FOUND               %,9d",
                    scrubberReport.getNumberOfExpiredAccountsFound());
        }
    }

    protected void handleDemergerSaveValidEntry(String entryString) {
        if (collectorMode) {
            OriginEntryInformation tempEntry = new OriginEntryFull(entryString);
            ledgerSummaryReport.summarizeEntry(tempEntry);
        }
    }

    public void setBatchFileDirectoryName(String batchFileDirectoryName) {
        this.batchFileDirectoryName = batchFileDirectoryName;
    }

    public String getTransferDescription() {
        return transferDescription;
    }

    public void setTransferDescription(String transferDescription) {
        this.transferDescription = transferDescription;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setFlexibleOffsetAccountService(FlexibleOffsetAccountService flexibleOffsetAccountService) {
        this.flexibleOffsetAccountService = flexibleOffsetAccountService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    public void setScrubberValidator(ScrubberValidator scrubberValidator) {
        this.scrubberValidator = scrubberValidator;
    }

    public void setAccountingCycleCachingService(AccountingCycleCachingService accountingCycleCachingService) {
        this.accountingCycleCachingService = accountingCycleCachingService;
    }

    public void setScrubberReportWriterService(DocumentNumberAwareReportWriterService scrubberReportWriterService) {
        this.scrubberReportWriterService = scrubberReportWriterService;
    }

    public void setScrubberLedgerReportWriterService(
            DocumentNumberAwareReportWriterService scrubberLedgerReportWriterService) {
        this.scrubberLedgerReportWriterService = scrubberLedgerReportWriterService;
    }

    public void setScrubberListingReportWriterService(
            DocumentNumberAwareReportWriterService scrubberListingReportWriterService) {
        this.scrubberListingReportWriterService = scrubberListingReportWriterService;
    }

    public void setScrubberBadBalanceListingReportWriterService(
            ReportWriterService scrubberBadBalanceListingReportWriterService) {
        this.scrubberBadBalanceListingReportWriterService = scrubberBadBalanceListingReportWriterService;
    }

    public void setDemergerRemovedTransactionsListingReportWriterService(
            ReportWriterService demergerRemovedTransactionsListingReportWriterService) {
        this.demergerRemovedTransactionsListingReportWriterService =
                demergerRemovedTransactionsListingReportWriterService;
    }

    public void setDemergerReportWriterService(ReportWriterService demergerReportWriterService) {
        this.demergerReportWriterService = demergerReportWriterService;
    }

    public void setPreScrubberService(PreScrubberService preScrubberService) {
        this.preScrubberService = preScrubberService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setRunDateService(RunDateService runDateService) {
        this.runDateService = runDateService;
    }

    public FlexibleOffsetAccountService getFlexibleOffsetAccountService() {
        return flexibleOffsetAccountService;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public PersistenceService getPersistenceService() {
        return persistenceService;
    }

    public ScrubberValidator getScrubberValidator() {
        return scrubberValidator;
    }

    public RunDateService getRunDateService() {
        return runDateService;
    }

    public AccountingCycleCachingService getAccountingCycleCachingService() {
        return accountingCycleCachingService;
    }

    public DocumentNumberAwareReportWriterService getScrubberReportWriterService() {
        return scrubberReportWriterService;
    }

    public DocumentNumberAwareReportWriterService getScrubberLedgerReportWriterService() {
        return scrubberLedgerReportWriterService;
    }

    public DocumentNumberAwareReportWriterService getScrubberListingReportWriterService() {
        return scrubberListingReportWriterService;
    }

    public ReportWriterService getScrubberBadBalanceListingReportWriterService() {
        return scrubberBadBalanceListingReportWriterService;
    }

    public ReportWriterService getDemergerRemovedTransactionsListingReportWriterService() {
        return demergerRemovedTransactionsListingReportWriterService;
    }

    public ReportWriterService getDemergerReportWriterService() {
        return demergerReportWriterService;
    }

    public PreScrubberService getPreScrubberService() {
        return preScrubberService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setPreScrubberReportWriterService(
            DocumentNumberAwareReportWriterService preScrubberReportWriterService) {
        this.preScrubberReportWriterService = preScrubberReportWriterService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    // Offset entry to have the same transaction date as the original transaction for Payroll Posting
    protected Date getTransactionDateForOffsetEntry(OriginEntryInformation scrubbedEntry) {
        if (getParameterService().parameterExists(ScrubberStep.class, TRANSACTION_DATE_BYPASS_ORIGINATIONS)) {
            Collection<String> transactionDateAutoAssignmentBypassOriginCodes = getParameterService()
                    .getParameterValuesAsString(ScrubberStep.class, TRANSACTION_DATE_BYPASS_ORIGINATIONS);
            String originationCode = scrubbedEntry.getFinancialSystemOriginationCode();
            if (transactionDateAutoAssignmentBypassOriginCodes.contains(originationCode)) {
                return scrubbedEntry.getTransactionDate();
            }
        }
        return this.runDate;
    }

    public void setParameterEvaluatorService(ParameterEvaluatorService parameterEvaluatorService) {
        this.parameterEvaluatorService = parameterEvaluatorService;
    }
}
