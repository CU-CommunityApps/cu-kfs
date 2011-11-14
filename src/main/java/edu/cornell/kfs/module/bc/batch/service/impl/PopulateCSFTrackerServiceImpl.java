package edu.cornell.kfs.module.bc.batch.service.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.module.bc.BCConstants;
import org.kuali.kfs.module.bc.businessobject.CalculatedSalaryFoundationTracker;
import org.kuali.kfs.module.bc.util.BudgetParameterFinder;
import org.kuali.kfs.module.ld.LaborConstants;
import org.kuali.kfs.module.ld.businessobject.LaborObject;
import org.kuali.kfs.module.ld.businessobject.PositionData;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.document.validation.impl.AccountingDocumentRuleBaseConstants.ACCOUNT_NUMBER;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.DictionaryValidationService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.cornell.kfs.module.bc.CUBCConstants;
import edu.cornell.kfs.module.bc.CUBCParameterKeyConstants;
import edu.cornell.kfs.module.bc.batch.PopulateCSFTrackerStep;
import edu.cornell.kfs.module.bc.batch.service.PopulateCSFTrackerService;
import edu.cornell.kfs.module.bc.businessobject.PSJobCode;
import edu.cornell.kfs.module.bc.businessobject.PSJobData;
import edu.cornell.kfs.module.bc.businessobject.PSPositionInfo;
import edu.cornell.kfs.module.bc.businessobject.PSPositionJobExtractAccountingInfo;
import edu.cornell.kfs.module.bc.businessobject.PSPositionJobExtractEntry;

/**
 * An implementation of a service that contains methods to populate the CSF Tracker table
 * (LD_CSF_TRACKER_T).
 */
public class PopulateCSFTrackerServiceImpl implements PopulateCSFTrackerService {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PopulateCSFTrackerServiceImpl.class);

    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType csfTrackerFlatInputFileType;
    protected DateTimeService dateTimeService;
    protected BusinessObjectService businessObjectService;
    protected DictionaryValidationService dictionaryValidationService;
    protected ParameterService parameterService;

    /**
     * @see edu.cornell.kfs.module.bc.batch.service.PopulateCSFTrackerService#populateCSFTracker(java.lang.String)
     */
    public boolean populateCSFTracker(String fileName, String currentFileName) {

        FileInputStream fileContents = null;

        //read file contents
        try {
            fileContents = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            LOG.error("file to parse not found " + fileName, e);
            throw new RuntimeException(
                    "Cannot find the file requested to be parsed " + fileName
                            + " " + e.getMessage(), e);
        }

        Collection<PSPositionJobExtractEntry> psPositionJobExtractEntries = null;
        // read csf tracker entries
        try {
            byte[] fileByteContent = IOUtils.toByteArray(fileContents);
            psPositionJobExtractEntries = (Collection<PSPositionJobExtractEntry>) batchInputFileService.parse(
                    csfTrackerFlatInputFileType, fileByteContent);
        } catch (IOException e) {
            LOG.error("error while getting file bytes:  " + e.getMessage(), e);
            throw new RuntimeException(
                    "Error encountered while attempting to get file bytes: "
                            + e.getMessage(), e);
        }

        // if no entries read log
        if (psPositionJobExtractEntries == null || psPositionJobExtractEntries.isEmpty()) {
            LOG.warn("No entries in the PS Job extract input file " + fileName);
        }

        // filter only updated entries from last run
        //for better performance look for the last successful file so that we can compare with that
        Collection<PSPositionJobExtractEntry> filteredPsPositionJobExtractEntries = filterEntriesToUpdate(
                currentFileName, psPositionJobExtractEntries);

        // validate entries to load
        Collection<PSPositionJobExtractEntry> validCsfTackerEntries = validateEntriesForCSFTracker(filteredPsPositionJobExtractEntries);

        List<CalculatedSalaryFoundationTracker> entriesToLoad = new ArrayList<CalculatedSalaryFoundationTracker>();
        Map<String, PSPositionInfo> positionInfoMap = new HashMap<String, PSPositionInfo>();
        Map<String, PSJobData> jobInfoMap = new HashMap<String, PSJobData>();
        Map<String, PSJobCode> jobCodeMap = new HashMap<String, PSJobCode>();

        //generate CalculatedSalaryFoundationTracker entries from the valid PSPositionJobExtractEntry list
        for (PSPositionJobExtractEntry psPositionJobExtractEntry : validCsfTackerEntries) {
            entriesToLoad.addAll(generateCalculatedSalaryFoundationTrackerCollection(psPositionJobExtractEntry));

            // build map for position Info

            if (positionInfoMap.get(psPositionJobExtractEntry.getPositionNumber()) == null) {
                positionInfoMap
                        .put(psPositionJobExtractEntry.getPositionNumber(),
                                generatePSPositionInfo(psPositionJobExtractEntry));
            }

            // build map for job info
            if (StringUtils.isNotBlank(psPositionJobExtractEntry.getEmplid())
                    && (jobInfoMap.get(psPositionJobExtractEntry.getKey()) == null)) {
                jobInfoMap.put(psPositionJobExtractEntry.getKey(), generatePSJobData(psPositionJobExtractEntry));
            }

            if (StringUtils.isNotBlank(psPositionJobExtractEntry.getJobCode())
                    && (jobCodeMap.get(psPositionJobExtractEntry.getJobCode()) == null)) {
                // build map for job code
                jobCodeMap.put(psPositionJobExtractEntry.getJobCode(), generatePSJobCode(psPositionJobExtractEntry));
            }
        }

        // load entries in the CSF tracker table
        loadEntriesInCSFTrackerTable(entriesToLoad);

        // load entries in PS_POSITION_EXTRA table
        loadEntriesInPSPositionInfoTable(positionInfoMap.values());

        // load entries in PS_JOB_CODE
        loadEntriesInPSJobCodeTable(jobCodeMap.values());

        // load entries in PS_JOB_DATA
        loadEntriesInPSJobDataTable(jobInfoMap.values());

        // log the number of entries loaded
        LOG.info("Total entries loaded: " + Integer.toString(entriesToLoad.size()));
        return true;

    }

    /**
     * Returns only the entries that were updated in the new PS extract.
     * 
     * @param csfTackerEntries
     * @return only entries that need to be updated
     */
    Collection<PSPositionJobExtractEntry> filterEntriesToUpdate(String currentFileName,
            Collection<PSPositionJobExtractEntry> psPositionJobExtractEntries) {

        Collection<PSPositionJobExtractEntry> filteredCsfTackerEntries = psPositionJobExtractEntries;
        //        if (!getRunPopulateCSFTRackerForNewYear()) {
        //            Collection<PSPositionJobExtractEntry> currentPSPositionJobExtractEntries = null;
        //
        //            FileInputStream fileContents = null;
        //
        //            //read file contents
        //            if (StringUtils.isNotBlank(currentFileName)) {
        //                try {
        //                    fileContents = new FileInputStream(currentFileName);
        //                    byte[] fileByteContent = IOUtils.toByteArray(fileContents);
        //                    currentPSPositionJobExtractEntries = (Collection<PSPositionJobExtractEntry>) batchInputFileService
        //                                    .parse(csfTrackerFlatInputFileType, fileByteContent);
        //
        //                    if (currentPSPositionJobExtractEntries != null) {
        //                        //build hash maps for current entries
        //                        Map<String, PSPositionJobExtractEntry> currentEntriesMap = new HashMap<String, PSPositionJobExtractEntry>();
        //                        for (PSPositionJobExtractEntry extractEntry : currentPSPositionJobExtractEntries) {
        //                            currentEntriesMap.put(extractEntry.getKey(), extractEntry);
        //                        }
        //
        //                        //build hash map for new entries
        //                        Map<String, PSPositionJobExtractEntry> newEntriesMap = new HashMap<String, PSPositionJobExtractEntry>();
        //                        for (PSPositionJobExtractEntry extractEntry : psPositionJobExtractEntries) {
        //                            newEntriesMap.put(extractEntry.getKey(), extractEntry);
        //                        }
        //                        // filter entries
        //                        for (String key : newEntriesMap.keySet()) {
        //                            PSPositionJobExtractEntry currentEntry = currentEntriesMap.get(key);
        //                            PSPositionJobExtractEntry newEntry = newEntriesMap.get(key);
        //
        //                            // basic filter, if anything changed on the line we take the new entry
        //                            if (currentEntry != null && currentEntry.equals(newEntry)) {
        //                                filteredCsfTackerEntries.remove(newEntry);
        //                            }
        //                        }
        //                    }
        //
        //                } catch (FileNotFoundException e) {
        //                    LOG.error("Current file to parse not found " + currentFileName, e);
        //                } catch (IOException e) {
        //                    LOG.error("error while getting current file bytes:  " + e.getMessage(), e);
        //                }
        //            }
        //        }

        return filteredCsfTackerEntries;

    }

    /**
     * Validates the entries to be loaded in the CSF Tracker table.
     * 
     * @param csfTackerEntries the entries to be loaded into the CSF Tracker table.
     */
    protected Collection<PSPositionJobExtractEntry> validateEntriesForCSFTracker(
            Collection<PSPositionJobExtractEntry> csfTackerEntries) {
        Collection<PSPositionJobExtractEntry> validEntries = new ArrayList<PSPositionJobExtractEntry>();

        Integer universityFiscalYear = BudgetParameterFinder.getBaseFiscalYear();
        // validate entries

        for (PSPositionJobExtractEntry extractEntry : csfTackerEntries) {
            boolean valid = true;
            valid &= validatePosition(extractEntry.getPositionNumber());

            if (!valid) {
                LOG.warn("Invalid position number for " + extractEntry.toString());
                continue;
            }
            valid &= validateCSFAmount(extractEntry.getAnnualRate());
            if (!valid) {
                LOG.warn("Invalid csf Amount for " + extractEntry.toString());
                continue;
            }

            if (valid) {
                if (extractEntry.getCSFAccountingInfoCollection() != null
                        && extractEntry.getCSFAccountingInfoCollection().size() > 0) {
                    for (PSPositionJobExtractAccountingInfo accountingInfo : extractEntry
                            .getCSFAccountingInfoCollection()) {
                        valid &= validateAccountingInfo(universityFiscalYear, extractEntry, accountingInfo);
                    }
                } else {
                    for (PSPositionJobExtractAccountingInfo accountingInfo : extractEntry
                            .getPOSAccountingInfoCollection()) {
                        valid &= validateAccountingInfo(universityFiscalYear, extractEntry, accountingInfo);
                    }
                }
            }

            if (valid) {
                validEntries.add(extractEntry);
            }

        }

        return validEntries;

    }

    protected boolean validateAccountingInfo(int universityFiscalYear, PSPositionJobExtractEntry extractEntry,
            PSPositionJobExtractAccountingInfo accountingInfo) {

        boolean valid = true;
        if (StringUtils.isNotBlank(accountingInfo.getCsfTimePercent())) {
            valid &= validateTimePercent(accountingInfo.getCsfTimePercent());
            if (!valid) {
                LOG.warn("Invalid time percent " + accountingInfo.getCsfTimePercent() + " for extract "
                        + extractEntry.toString());
            }
            valid &= validateAccount(accountingInfo.getChartOfAccountsCode(),
                    accountingInfo.getAccountNumber());
            if (!valid) {
                LOG.warn("Invalid Account: " + accountingInfo.getChartOfAccountsCode() + ","
                        + accountingInfo.getAccountNumber() + " for extract " + extractEntry.toString());
            }
            if (StringUtils.isNotBlank(accountingInfo.getSubAccountNumber())) {
                valid &= validateSubAccount(accountingInfo.getChartOfAccountsCode(),
                        accountingInfo.getAccountNumber(),
                        accountingInfo.getSubAccountNumber());
                if (!valid) {
                    LOG.warn("Invalid Sub Account: " + accountingInfo.getChartOfAccountsCode() + ","
                            + accountingInfo.getAccountNumber() + ","
                            + accountingInfo.getSubAccountNumber()
                            + " for extract " + extractEntry.toString());
                }
            }

            valid &= validateLaborObject(universityFiscalYear, accountingInfo.getChartOfAccountsCode(),
                    accountingInfo.getFinancialObjectCode());
            if (!valid) {
                LOG.warn("Invalid Labor Object: " + universityFiscalYear + ","
                        + accountingInfo.getChartOfAccountsCode() + ","
                        + accountingInfo.getFinancialObjectCode() + " for extract "
                        + extractEntry.toString());
            }
            valid &= validateObjectCode(universityFiscalYear, accountingInfo.getChartOfAccountsCode(),
                    accountingInfo.getFinancialObjectCode());
            if (!valid) {
                LOG.warn("Invalid Object Code: " + universityFiscalYear + ","
                        + accountingInfo.getChartOfAccountsCode() + ","
                        + accountingInfo.getFinancialObjectCode() + " for extract "
                        + extractEntry.toString());
            }

            if (StringUtils.isNotBlank(accountingInfo.getFinancialSubObjectCode())) {
                valid &= validateSubObject(universityFiscalYear, accountingInfo.getChartOfAccountsCode(),
                        accountingInfo.getAccountNumber(), accountingInfo.getFinancialObjectCode(),
                        accountingInfo.getFinancialSubObjectCode());
                if (!valid) {
                    LOG.warn("Invalid Sub Object Code: " + universityFiscalYear + ","
                            + accountingInfo.getChartOfAccountsCode() + ","
                            + accountingInfo.getFinancialObjectCode() + " for extract "
                            + extractEntry.toString());
                }
            }
        }

        return valid;
    }

    /**
     * Validates that Position is in HR / P has a "Budgeted Position" = "Y"
     * 
     * @param positionNumber
     * @return
     */
    protected boolean validatePosition(String positionNumber) {
        //validate position
        Map<String, Object> positionCriteria = new HashMap<String, Object>();
        positionCriteria.put(KFSPropertyConstants.POSITION_NUMBER, positionNumber);
        positionCriteria.put("budgetedPosition", "Y");
        int count = businessObjectService.countMatching(PositionData.class, positionCriteria);

        if (count > 0) {
            return true;
        } else
            return false;
    }

    /**
     * Validates that Account is in KFS and has a "Budget Record Level" not equal to "N"
     * 
     * @param chart
     * @param accountNumber
     * @return true if valid, false otherwise
     */
    protected boolean validateAccount(String chart, String accountNumber) {
        //validate account
        Map<String, Object> accountCriteria = new HashMap<String, Object>();
        accountCriteria.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber);
        accountCriteria.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chart);
        Account account = (Account) businessObjectService.findByPrimaryKey(Account.class, accountCriteria);

        if (ObjectUtils.isNotNull(account)) {
            if (ACCOUNT_NUMBER.BUDGET_LEVEL_NO_BUDGET.equalsIgnoreCase(account.getBudgetRecordingLevelCode())) {
                return false;
            } else
                return true;
        } else
            return false;
    }

    /**
     * Validates that sub-account exists in KFS
     * 
     * @param chart
     * @param accountNumber
     * @param subAccountNumber
     * @return true if valid, false otherwise
     */
    protected boolean validateSubAccount(String chart, String accountNumber, String subAccountNumber) {
        Map<String, Object> subAccountCriteria = new HashMap<String, Object>();

        subAccountCriteria.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chart);
        subAccountCriteria.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber);
        subAccountCriteria.put(KFSPropertyConstants.SUB_ACCOUNT_NUMBER, subAccountNumber);
        SubAccount subAccount = (SubAccount) businessObjectService.findByPrimaryKey(SubAccount.class,
                subAccountCriteria);

        if (ObjectUtils.isNotNull(subAccount)) {
            return true;
        } else
            return false;
    }

    /**
     * Validates that Object: is in "Labor Object Code" table with
     * "Financial Object Fringe Or Salary Code" = "S"
     * 
     * @param universityFiscalYear
     * @param chart
     * @param objectCode
     * @return true if valid, false otherwise
     */
    protected boolean validateLaborObject(Integer universityFiscalYear, String chart, String objectCode) {

        Map<String, Object> objectCodeCriteria = new HashMap<String, Object>();
        objectCodeCriteria.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, universityFiscalYear);
        objectCodeCriteria.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chart);
        objectCodeCriteria.put(KFSPropertyConstants.FINANCIAL_OBJECT_CODE, objectCode);

        LaborObject object = (LaborObject) businessObjectService.findByPrimaryKey(LaborObject.class,
                objectCodeCriteria);

        if (ObjectUtils.isNotNull(object)) {
            if (!LaborConstants.LABOR_OBJECT_SALARY_CODE
                    .equalsIgnoreCase(object.getFinancialObjectFringeOrSalaryCode())) {
                return false;
            }
            return true;
        } else
            return false;
    }

    /**
     * Validates sub-object in KFS.
     * 
     * @param universityFiscalYear
     * @param chart
     * @param accountNumber
     * @param objectCode
     * @param subObjectCode
     * @return true if valid, false otherwise
     */
    protected boolean validateSubObject(Integer universityFiscalYear, String chart, String accountNumber,
            String objectCode, String subObjectCode) {
        Map<String, Object> subObjectCodeCriteria = new HashMap<String, Object>();
        subObjectCodeCriteria.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, universityFiscalYear);
        subObjectCodeCriteria.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chart);
        subObjectCodeCriteria.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber);
        subObjectCodeCriteria.put(KFSPropertyConstants.FINANCIAL_OBJECT_CODE, objectCode);
        subObjectCodeCriteria.put(KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE, subObjectCode);

        SubObjectCode subObject = (SubObjectCode) businessObjectService.findByPrimaryKey(SubObjectCode.class,
                subObjectCodeCriteria);

        if (ObjectUtils.isNotNull(subObject)) {
            return true;
        } else
            return false;
    }

    /**
     * Validates that the Object exists in KFS
     * 
     * @param universityFiscalYear
     * @param chart
     * @param objectCode
     * @return true if valid, false otherwise
     */
    protected boolean validateObjectCode(Integer universityFiscalYear, String chart,
            String objectCode) {
        Map<String, Object> objectCodeCriteria = new HashMap<String, Object>();
        objectCodeCriteria.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, universityFiscalYear);
        objectCodeCriteria.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chart);
        objectCodeCriteria.put(KFSPropertyConstants.FINANCIAL_OBJECT_CODE, objectCode);

        ObjectCode object = (ObjectCode) businessObjectService.findByPrimaryKey(ObjectCode.class,
                objectCodeCriteria);

        if (ObjectUtils.isNotNull(object)) {

            return true;
        } else
            return false;
    }

    /**
     * Validates the amount is a valid number.
     * 
     * @param csfAmount
     * @return true if valid, false otherwise
     */
    protected boolean validateCSFAmount(String csfAmount) {
        boolean valid = true;
        try {
            generateKualiDecimal(csfAmount);
        } catch (NumberFormatException exception) {
            valid = false;
        }
        return valid;
    }

    /**
     * Validates the time percent is a valid number.
     * 
     * @param timePercent
     * @return true if valid, false otherwise
     */
    protected boolean validateTimePercent(String timePercent) {
        boolean valid = true;
        try {
            generateKualiDecimal(timePercent);
        } catch (NumberFormatException exception) {
            valid = false;
        }
        return valid;
    }

    /**
     * Generate a collection of CalculatedSalaryFoundationTracker from the given
     * PSPositionJobExtractEntry entry.
     * 
     * @param PSPositionJobExtractEntry
     * @return a collection of CalculatedSalaryFoundationTracker
     */
    protected Collection<CalculatedSalaryFoundationTracker> generateCalculatedSalaryFoundationTrackerCollection(
            PSPositionJobExtractEntry psPositionJobExtractEntry) {
        // common values

        String positionNumber = psPositionJobExtractEntry.getPositionNumber();
        Integer universityFiscalYear = BudgetParameterFinder.getBaseFiscalYear();

        String emplid = psPositionJobExtractEntry.getEmplid();
        if (StringUtils.isBlank(emplid)) {
            emplid = BCConstants.VACANT_EMPLID;
        }

        String name = psPositionJobExtractEntry.getName();
        Timestamp csfCreateTimestamp = new Timestamp(0);

        KualiDecimal csfAmount = generateKualiDecimal(psPositionJobExtractEntry.getAnnualRate());

        Map<String, CalculatedSalaryFoundationTracker> mapOfEntries = new HashMap<String, CalculatedSalaryFoundationTracker>();

        // accounting data
        if (psPositionJobExtractEntry
                .getCSFAccountingInfoCollection() != null && psPositionJobExtractEntry
                .getCSFAccountingInfoCollection().size() > 0) {
            for (PSPositionJobExtractAccountingInfo accountingInfo : psPositionJobExtractEntry
                    .getCSFAccountingInfoCollection()) {

                BigDecimal csfTimePercent = generateKualiDecimal(accountingInfo.getCsfTimePercent()).bigDecimalValue();
                BigDecimal csfFullTimeEmploymentQuantity = generateKualiDecimal(accountingInfo.getCsfTimePercent())
                        .bigDecimalValue()
                        .divide(new BigDecimal(100));
                String chartOfAccountsCode = accountingInfo.getChartOfAccountsCode();
                String accountNumber = accountingInfo.getAccountNumber();

                String subAccountNumber = accountingInfo.getSubAccountNumber();
                if (StringUtils.isBlank(subAccountNumber)) {
                    subAccountNumber = CUBCConstants.DEFAULT_SUB_ACCOUNT_NUMBER;
                }

                String financialObjectCode = accountingInfo.getFinancialObjectCode();

                String financialSubObjectCode = accountingInfo.getFinancialSubObjectCode();
                if (StringUtils.isBlank(financialSubObjectCode)) {
                    financialSubObjectCode = CUBCConstants.DEFAULT_FINANCIAL_SUB_OBJECT_CODE;
                }

                String csfDeleteCode = generateDeleteCode(psPositionJobExtractEntry);
                String csfFundingStatusCode = generateFundingStatusCode(psPositionJobExtractEntry);

                KualiDecimal percentOfCSFAmount = new KualiDecimal(csfAmount.bigDecimalValue().multiply(
                        csfFullTimeEmploymentQuantity));

                CalculatedSalaryFoundationTracker entry = generateCalculatedSalaryFoundationTracker(positionNumber,
                        universityFiscalYear, emplid, name, csfCreateTimestamp, csfFullTimeEmploymentQuantity,
                        percentOfCSFAmount,
                        csfTimePercent, chartOfAccountsCode, accountNumber, subAccountNumber, financialObjectCode,
                        financialSubObjectCode, csfDeleteCode, csfFundingStatusCode);

                // if in the map add percentages and add only one account
                CalculatedSalaryFoundationTracker csfEntryFromMap = mapOfEntries.get(accountingInfo.getKey());
                if (csfEntryFromMap != null) {
                    BigDecimal tmpTimePercent = csfEntryFromMap.getCsfTimePercent().add(csfTimePercent);
                    entry.setCsfTimePercent(tmpTimePercent);

                    BigDecimal csfFTE = csfEntryFromMap.getCsfFullTimeEmploymentQuantity().add(
                            csfFullTimeEmploymentQuantity);
                    entry.setCsfFullTimeEmploymentQuantity(csfFTE);

                    KualiDecimal amount = csfEntryFromMap.getCsfAmount().add(percentOfCSFAmount);
                    entry.setCsfAmount(amount);
                }

                mapOfEntries.put(accountingInfo.getKey(), entry);

            }
        }
        // if there are no accounting strings at the job level get the info from the position level
        else {
            if (psPositionJobExtractEntry
                    .getPOSAccountingInfoCollection() != null && psPositionJobExtractEntry
                    .getPOSAccountingInfoCollection().size() > 0) {
                for (PSPositionJobExtractAccountingInfo accountingInfo : psPositionJobExtractEntry
                        .getPOSAccountingInfoCollection()) {
                    BigDecimal csfTimePercent = generateKualiDecimal(accountingInfo.getCsfTimePercent())
                            .bigDecimalValue();
                    BigDecimal csfFullTimeEmploymentQuantity = generateKualiDecimal(accountingInfo.getCsfTimePercent())
                            .bigDecimalValue()
                            .divide(new BigDecimal(100));
                    String chartOfAccountsCode = accountingInfo.getChartOfAccountsCode();
                    String accountNumber = accountingInfo.getAccountNumber();

                    String subAccountNumber = accountingInfo.getSubAccountNumber();
                    if (StringUtils.isBlank(subAccountNumber)) {
                        subAccountNumber = CUBCConstants.DEFAULT_SUB_ACCOUNT_NUMBER;
                    }

                    String financialObjectCode = accountingInfo.getFinancialObjectCode();

                    String financialSubObjectCode = accountingInfo.getFinancialSubObjectCode();
                    if (StringUtils.isBlank(financialSubObjectCode)) {
                        financialSubObjectCode = CUBCConstants.DEFAULT_FINANCIAL_SUB_OBJECT_CODE;
                    }

                    String csfDeleteCode = generateDeleteCode(psPositionJobExtractEntry);
                    String csfFundingStatusCode = generateFundingStatusCode(psPositionJobExtractEntry);

                    KualiDecimal percentOfCSFAmount = new KualiDecimal(csfAmount.bigDecimalValue().multiply(
                            csfFullTimeEmploymentQuantity));

                    CalculatedSalaryFoundationTracker entry = generateCalculatedSalaryFoundationTracker(positionNumber,
                            universityFiscalYear, emplid, name, csfCreateTimestamp, csfFullTimeEmploymentQuantity,
                            percentOfCSFAmount,
                            csfTimePercent, chartOfAccountsCode, accountNumber, subAccountNumber, financialObjectCode,
                            financialSubObjectCode, csfDeleteCode, csfFundingStatusCode);

                    // if in the map add percentages and add only one account
                    CalculatedSalaryFoundationTracker csfEntryFromMap = mapOfEntries.get(accountingInfo.getKey());
                    if (csfEntryFromMap != null) {
                        BigDecimal tmpTimePercent = csfEntryFromMap.getCsfTimePercent().add(csfTimePercent);
                        entry.setCsfTimePercent(tmpTimePercent);

                        BigDecimal csfFTE = csfEntryFromMap.getCsfFullTimeEmploymentQuantity().add(
                                csfFullTimeEmploymentQuantity);
                        entry.setCsfFullTimeEmploymentQuantity(csfFTE);

                        KualiDecimal amount = csfEntryFromMap.getCsfAmount().add(percentOfCSFAmount);
                        entry.setCsfAmount(amount);
                    }
                    
                    mapOfEntries.put(accountingInfo.getKey(), entry);

                }
            }
        }

        return mapOfEntries.values();

    }

    /**
     * Builds a CalculatedSalaryFoundationTracker from the input values.
     * 
     * @param positionNumber
     * @param universityFiscalYear
     * @param emplid
     * @param name
     * @param csfCreateTimestamp
     * @param csfFullTimeEmploymentQuantity
     * @param csfAmount
     * @param csfTimePercent
     * @param chartOfAccountsCode
     * @param accountNumber
     * @param subAccountNumber
     * @param financialObjectCode
     * @param financialSubObjectCode
     * @param csfDeleteCode
     * @param csfFundingStatusCode
     * @return a CalculatedSalaryFoundationTracker entry
     */
    protected CalculatedSalaryFoundationTracker generateCalculatedSalaryFoundationTracker(
            String positionNumber,
            Integer universityFiscalYear,
            String emplid,
            String name,
            Timestamp csfCreateTimestamp,
            BigDecimal csfFullTimeEmploymentQuantity,
            KualiDecimal csfAmount,
            BigDecimal csfTimePercent,
            String chartOfAccountsCode,
            String accountNumber,
            String subAccountNumber,
            String financialObjectCode,
            String financialSubObjectCode,
            String csfDeleteCode,
            String csfFundingStatusCode
            ) {

        CalculatedSalaryFoundationTracker entry = new CalculatedSalaryFoundationTracker();

        entry.setPositionNumber(positionNumber);
        entry.setEmplid(emplid);
        entry.setCsfCreateTimestamp(csfCreateTimestamp);
        entry.setUniversityFiscalYear(universityFiscalYear);
        entry.setChartOfAccountsCode(chartOfAccountsCode);
        entry.setAccountNumber(accountNumber);
        entry.setSubAccountNumber(subAccountNumber);
        entry.setFinancialObjectCode(financialObjectCode);
        entry.setFinancialSubObjectCode(financialSubObjectCode);

        //        CalculatedSalaryFoundationTracker retrievedCSFEntry = (CalculatedSalaryFoundationTracker) businessObjectService
        //                .retrieve(entry);
        //        if (ObjectUtils.isNotNull(retrievedCSFEntry)) {
        //            entry = retrievedCSFEntry;
        //        }

        entry.setName(name);
        entry.setCsfAmount(csfAmount);
        entry.setCsfDeleteCode(csfDeleteCode);
        entry.setCsfTimePercent(csfTimePercent);
        entry.setCsfFullTimeEmploymentQuantity(csfFullTimeEmploymentQuantity);
        entry.setCsfFundingStatusCode(csfFundingStatusCode);

        return entry;

    }

    /**
     * Creates a new PSPositionInfo object
     * 
     * @param positionNumber
     * @param positionType
     * @param defaultObjectCode
     * @param positionUnionCode
     * @param workMonths
     * @return a new PSPositionInfo object
     */
    protected PSPositionInfo generatePSPositionInfo(PSPositionJobExtractEntry psPositionJobExtractEntry) {

        PSPositionInfo psPositionInfo = new PSPositionInfo();

        psPositionInfo.setPositionNumber(psPositionJobExtractEntry.getPositionNumber());

        //        PSPositionInfo retrievedPSPositionInfo = (PSPositionInfo) businessObjectService
        //                .retrieve(psPositionInfo);
        //        if (ObjectUtils.isNotNull(retrievedPSPositionInfo)) {
        //            psPositionInfo = retrievedPSPositionInfo;
        //        }

        psPositionInfo.setPositionType(psPositionJobExtractEntry.getEmployeeType());
        psPositionInfo.setPositionUnionCode(psPositionJobExtractEntry.getPositionUnionCode());
        psPositionInfo.setWorkMonths(generateWorkMonths(psPositionJobExtractEntry.getWorkMonths()));
        psPositionInfo.setJobCode(psPositionJobExtractEntry.getJobCode());
        psPositionInfo.setClassInd(psPositionJobExtractEntry.getClassInd());
        psPositionInfo.setCuStateCert(psPositionJobExtractEntry.getCuStateCert());
        psPositionInfo.setFullPartTime(psPositionJobExtractEntry.getFullPartTime());
        psPositionInfo.setAddsToActualFte(psPositionJobExtractEntry.getAddsToActualFte());

        return psPositionInfo;

    }

    protected PSJobData generatePSJobData(PSPositionJobExtractEntry psPositionJobExtractEntry) {

        PSJobData psJobData = new PSJobData();

        psJobData.setPositionNumber(psPositionJobExtractEntry.getPositionNumber());
        psJobData.setEmplid(psPositionJobExtractEntry.getEmplid());

        //        PSJobData retrievedPSJobData = (PSJobData) businessObjectService
        //                .retrieve(psJobData);
        //        
        //        if (ObjectUtils.isNotNull(retrievedPSJobData)) {
        //            psJobData = retrievedPSJobData;
        //        }

        psJobData.setEmployeeRecord(psPositionJobExtractEntry.getEmployeeRecord());
        psJobData.setEmployeeStatus(psPositionJobExtractEntry.getEmployeeStatus());
        psJobData.setJobStandardHours(generateKualiDecimal(psPositionJobExtractEntry.getJobStandardHours()));
        psJobData.setEmployeeClass(psPositionJobExtractEntry.getEmployeeClass());
        psJobData.setEarningDistributionType(psPositionJobExtractEntry.getEarningDistributionType());
        psJobData.setCompRate(generateKualiDecimal(psPositionJobExtractEntry.getCompRate()));
        psJobData.setAnnualBenefitBaseRate(generateKualiDecimal(psPositionJobExtractEntry.getAnnualBenefitBaseRate()));
        psJobData.setCuAbbrFlag(psPositionJobExtractEntry.getCuAbbrFlag());
        psJobData.setAnnualRate(generateKualiDecimal(psPositionJobExtractEntry.getAnnualRate()));
        psJobData.setEmployeeName(psPositionJobExtractEntry.getName());

        return psJobData;
    }

    protected PSJobCode generatePSJobCode(PSPositionJobExtractEntry psPositionJobExtractEntry) {

        PSJobCode psJobCode = new PSJobCode();

        psJobCode.setJobCode(psPositionJobExtractEntry.getJobCode());

        //        PSJobCode retrievedPSJobCode = (PSJobCode) businessObjectService
        //                .retrieve(psJobCode);
        //        if (ObjectUtils.isNotNull(retrievedPSJobCode)) {
        //            psJobCode = retrievedPSJobCode;
        //        }

        psJobCode.setCuObjectCode(psPositionJobExtractEntry.getDefaultObjectCode());
        psJobCode.setJobCodeDesc(psPositionJobExtractEntry.getJobCodeDesc());
        psJobCode.setJobCodeDescShort(psPositionJobExtractEntry.getJobCodeDescShrt());
        psJobCode.setJobFamily(psPositionJobExtractEntry.getJobFamily());
        psJobCode.setJobStandardHours(generateKualiDecimal(psPositionJobExtractEntry.getJobCodeStandardHours()));

        return psJobCode;
    }

    /**
     * Generates the csf time percent from the value in the input file. The value comes
     * like this: Acct Distribution % (the first 5 digits in all 20 “account” fields)
     * 00000 and we will add the decimal point and create a KualiDecimal value 000.00.
     * Generate the CSFAmount from the value in the input file. The comes in like this
     * Annual Rt (pos 664) 000000000000 and we will add the decimal point 0000000000.00
     * and create a KualiDecimal value.
     * 
     * @param csfTimePrecent
     * @return a KualiDecimal value for the input csfTimePrecent
     */
    protected KualiDecimal generateKualiDecimal(String input) {
        //prepare time percent
        String result = input;
        result = result.substring(0, result.length() - 2) + "."
                + result.substring(result.length() - 2, result.length());

        return new KualiDecimal(result);
    }

    /**
     * Generates a delete code for the input PSPositionJobExtractEntry.
     * 
     * @param psPositionJobExtractEntry
     * @return the generated delete code
     */
    protected String generateDeleteCode(PSPositionJobExtractEntry psPositionJobExtractEntry) {

        //so far we have determined that the only value used in the system is "-". Cornell does not plan to keep a history of the changes in the csf tracker table so the other values are not needed.
        String deleteCode = BCConstants.ACTIVE_CSF_DELETE_CODE;
        return deleteCode;

    }

    /**
     * Generates the funding status code for the input PSPositionJobExtractEntry.
     * 
     * @param psPositionJobExtractEntry
     * @return
     */
    protected String generateFundingStatusCode(PSPositionJobExtractEntry psPositionJobExtractEntry) {
        String fundingStatusCode = BCConstants.csfFundingStatusFlag.ACTIVE.getFlagValue();

        //determine the right value for the funding status code
        return fundingStatusCode;

    }

    /**
     * Generate the workMonths from the value in the input file. The workMonths comes in
     * like this Work Months (pos 1174) 000 and we will will only use 00 and create an
     * Integer value.
     * 
     * @param workMonths
     * @return a Integer value for the input workMonths
     */
    protected Integer generateWorkMonths(String workMonths) {
        //prepare workMonths to create a KualiDecimal
        if (StringUtils.isNotBlank(workMonths)) {
            workMonths = workMonths.substring(0, workMonths.length() - 1);
            return Integer.parseInt(workMonths);
        } else
            return null;
    }

    /**
     * Loads the entries in the LS_CSF_TRACKER_T table.
     * 
     * @param csfTackerEntries
     */
    protected void loadEntriesInCSFTrackerTable(List<CalculatedSalaryFoundationTracker> csfTrackerEntries) {

        //if (getRunPopulateCSFTRackerForNewYear())
        {
            // wipe out everything first
            businessObjectService
                    .deleteMatching(CalculatedSalaryFoundationTracker.class, new HashMap<String, String>());
        }

        //load in the new entries
        for (CalculatedSalaryFoundationTracker entry : csfTrackerEntries) {

            businessObjectService.save(entry);
        }
    }

    /**
     * Load entries in the PS_POSITION_EXTRA table.
     * 
     * @param psPositionInfoEntries
     */
    protected void loadEntriesInPSPositionInfoTable(Collection<PSPositionInfo> psPositionInfoEntries) {

        businessObjectService
                    .deleteMatching(PSPositionInfo.class, new HashMap<String, String>());

        //load in the new entries
        for (PSPositionInfo entry : psPositionInfoEntries) {
            businessObjectService.save(entry);
        }
    }

    /**
     * Load entries in the PS_JOB_DATA table.
     * 
     * @param jobDataEntries
     */
    protected void loadEntriesInPSJobDataTable(Collection<PSJobData> jobDataEntries) {

        businessObjectService
                    .deleteMatching(PSJobData.class, new HashMap<String, String>());

        //load in the new entries
        for (PSJobData entry : jobDataEntries) {
            businessObjectService.save(entry);
        }
    }

    /**
     * Load entries in the PS_JOB_CODE table.
     * 
     * @param psPositionInfoEntries
     */
    protected void loadEntriesInPSJobCodeTable(Collection<PSJobCode> psJobCodeEntries) {

        businessObjectService
                    .deleteMatching(PSJobCode.class, new HashMap<String, String>());

        //load in the new entries
        for (PSJobCode entry : psJobCodeEntries) {
            businessObjectService.save(entry);
        }
    }

    /**
     * Gets the value of the RUN_POPULATE_CSF_TRACKER_FOR_NEW_YEAR parameter.
     * 
     * @return
     */
    private boolean getRunPopulateCSFTRackerForNewYear() {
        boolean runPopulateCSFTRackerForNewYear = parameterService.getIndicatorParameter(PopulateCSFTrackerStep.class,
                CUBCParameterKeyConstants.RUN_POPULATE_CSF_TRACKER_FOR_NEW_YEAR);
        return runPopulateCSFTRackerForNewYear;
    }

    /**
     * Gets the batchInputFileService.
     * 
     * @return batchInputFileService
     */
    public BatchInputFileService getBatchInputFileService() {
        return batchInputFileService;
    }

    /**
     * Sets the batchInputFileService.
     * 
     * @param batchInputFileService
     */
    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    /**
     * Gets the csfTrackerFlatInputFileType.
     * 
     * @return csfTrackerFlatInputFileType
     */
    public BatchInputFileType getCsfTrackerFlatInputFileType() {
        return csfTrackerFlatInputFileType;
    }

    /**
     * Sets the csfTrackerFlatInputFileType.
     * 
     * @param csfTrackerFlatInputFileType
     */
    public void setCsfTrackerFlatInputFileType(BatchInputFileType csfTrackerFlatInputFileType) {
        this.csfTrackerFlatInputFileType = csfTrackerFlatInputFileType;
    }

    /**
     * Sets the dateTimeService.
     * 
     * @param dateTimeService
     */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    /**
     * Gets the businessObjectService.
     * 
     * @return businessObjectService
     */
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    /**
     * Sets the businessObjectService.
     * 
     * @param businessObjectService
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * Sets the parameterService.
     * 
     * @param parameterService
     */
    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
