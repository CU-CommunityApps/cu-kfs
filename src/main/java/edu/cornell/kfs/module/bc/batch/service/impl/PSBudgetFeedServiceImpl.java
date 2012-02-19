package edu.cornell.kfs.module.bc.batch.service.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.module.bc.CUBCConstants;
import edu.cornell.kfs.module.bc.CUBCConstants.StatusFlag;
import edu.cornell.kfs.module.bc.CUBCPropertyConstants;
import edu.cornell.kfs.module.bc.batch.dataaccess.PSJobDataDao;
import edu.cornell.kfs.module.bc.batch.dataaccess.PSPositionDataDao;
import edu.cornell.kfs.module.bc.batch.service.PSBudgetFeedService;
import edu.cornell.kfs.module.bc.businessobject.PSJobCode;
import edu.cornell.kfs.module.bc.businessobject.PSJobData;
import edu.cornell.kfs.module.bc.businessobject.PSPositionInfo;
import edu.cornell.kfs.module.bc.businessobject.PSPositionJobExtractAccountingInfo;
import edu.cornell.kfs.module.bc.businessobject.PSPositionJobExtractEntry;
import edu.cornell.kfs.module.bc.util.CUBudgetParameterFinder;

/**
 * An implementation of a service that contains methods to populate the CSF Tracker table
 * (LD_CSF_TRACKER_T) and CU_PS_POSITION_EXTRA, CU_PS_JOB_DATA, CU_PS_JOB_CD tables with
 * PS/HR data.
 */
@Transactional
public class PSBudgetFeedServiceImpl implements PSBudgetFeedService {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PSBudgetFeedServiceImpl.class);

    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType psBudgetFeedFlatInputFileType;
    protected DateTimeService dateTimeService;
    protected BusinessObjectService businessObjectService;
    protected DictionaryValidationService dictionaryValidationService;
    protected ParameterService parameterService;
    protected PSPositionDataDao positionDataDao;
    protected PSJobDataDao psJobDataDao;

    /**
     * @see edu.cornell.kfs.module.bc.batch.service.PSBudgetFeedService#populateCSFTracker(java.lang.String)
     */
    public boolean loadBCDataFromPS(String fileName, String currentFileName, boolean startFresh) {

        LOG.info("\n Processing .done file: " + fileName + " and .current file: " + currentFileName + "\n");

        // if no current file treat as if we are starting fresh
        if (currentFileName == null) {
            startFresh = true;
        }

        LOG.info("\n Reading PS extract entries. \n");
        // read the data from the PS extract
        List<PSPositionJobExtractEntry> psPositionJobExtractEntries = readPSExtractFileContents(fileName);

        if (psPositionJobExtractEntries == null) {
            LOG.info("No entries in the input file \n");
            return true;
        }

        LOG.info("\n Filtering only entries that have changed since last run. \n");
        //for better performance look for the last successful file so that we can compare with that
        Collection<PSPositionJobExtractEntry> filteredPsPositionJobExtractEntries = filterEntriesToUpdate(
                currentFileName, psPositionJobExtractEntries, startFresh);

        LOG.info("\n Validating entries. \n");
        // validate entries to load
        Collection<PSPositionJobExtractEntry> validCsfTackerEntries = validateEntriesForCSFTracker(filteredPsPositionJobExtractEntries);

        //generate CalculatedSalaryFoundationTracker, PSPositionInfo, PSJobData and PSJobCode entries from the valid PSPositionJobExtractEntry list
        List<CalculatedSalaryFoundationTracker> entriesToLoad = new ArrayList<CalculatedSalaryFoundationTracker>();
        Map<String, PSPositionInfo> positionInfoMap = new HashMap<String, PSPositionInfo>();
        Map<String, PSJobData> jobInfoMap = new HashMap<String, PSJobData>();
        Map<String, PSJobCode> jobCodeMap = new HashMap<String, PSJobCode>();

        LOG.info("\n Generating CalculatedSalaryFoundationTracker, PSPositionInfo, PSJobData and PSJobCode entries from the valid PSPositionJobExtractEntry list. \n");
        generateNewEntriesToLoadInTheDB(validCsfTackerEntries, entriesToLoad, positionInfoMap, jobInfoMap, jobCodeMap);

        LOG.info("\n Loading data in the database. \n");
        // load data in the DB
        loadDataInDB(startFresh, entriesToLoad, positionInfoMap, jobInfoMap, jobCodeMap);

        LOG.info("\n Updating the executives based on the SIP_EXECUTIVES param. \n");
        //update the executives based on the SIP_EXECUTIVES param
        updateTheSipEligibility(psPositionJobExtractEntries);

        return true;

    }

    /**
     * Reads the new file from PS and builds a list of PSPositionJobExtractEntry objects.
     * 
     * @param fileName the name of the last PS extract
     * @return a list of PSPositionJobExtractEntry objects
     */
    protected List<PSPositionJobExtractEntry> readPSExtractFileContents(String fileName) {

        try {

            FileInputStream fileContents = null;
            //read file contents
            fileContents = new FileInputStream(fileName);
            // read csf tracker entries
            List<PSPositionJobExtractEntry> psPositionJobExtractEntries = null;
            byte[] fileByteContent = IOUtils.toByteArray(fileContents);
            psPositionJobExtractEntries = (List<PSPositionJobExtractEntry>) batchInputFileService.parse(
                    psBudgetFeedFlatInputFileType, fileByteContent);
            // if no entries, log
            if (psPositionJobExtractEntries == null || psPositionJobExtractEntries.isEmpty()) {
                LOG.warn("No entries in the PS Job extract input file " + fileName);
                return null;
            }

            return psPositionJobExtractEntries;

        } catch (FileNotFoundException e) {
            LOG.error("File to parse not found " + fileName, e);
            throw new RuntimeException(
                    "Cannot find the file requested to be parsed " + fileName
                            + " " + e.getMessage(), e);
        } catch (IOException e) {
            LOG.error("Error while getting file bytes:  " + e.getMessage(), e);
            throw new RuntimeException(
                    "Error encountered while attempting to get file bytes: "
                            + e.getMessage(), e);
        }
    }

    /**
     * Returns only the entries that were updated in the new PS extract.
     * 
     * @param currentFileName
     * @param inputPSPositionJobExtractEntries
     * @param startFresh
     * 
     * @return only entries that need to be updated
     */
    protected Collection<PSPositionJobExtractEntry> filterEntriesToUpdate(String currentFileName,
            Collection<PSPositionJobExtractEntry> inputPSPositionJobExtractEntries, boolean startFresh) {

        Collection<PSPositionJobExtractEntry> filteredEntries = new ArrayList<PSPositionJobExtractEntry>();
        // are we starting fresh? then we delete all old records

        if (startFresh) {
            filteredEntries = inputPSPositionJobExtractEntries;
        } else {

            Collection<PSPositionJobExtractEntry> currentPSPositionJobExtractEntries = null;

            FileInputStream fileContents = null;

            //read file contents
            if (StringUtils.isNotBlank(currentFileName)) {
                try {
                    fileContents = new FileInputStream(currentFileName);
                    byte[] fileByteContent = IOUtils.toByteArray(fileContents);
                    currentPSPositionJobExtractEntries = (Collection<PSPositionJobExtractEntry>) batchInputFileService
                                            .parse(psBudgetFeedFlatInputFileType, fileByteContent);
                    // if no entries read log
                    if (currentPSPositionJobExtractEntries == null || currentPSPositionJobExtractEntries.isEmpty()) {
                        LOG.warn("No entries in the current file " + currentFileName);
                    }

                    if (currentPSPositionJobExtractEntries != null) {
                        //build hash maps for current entries
                        Map<String, PSPositionJobExtractEntry> currentEntriesMap = new HashMap<String, PSPositionJobExtractEntry>();
                        for (PSPositionJobExtractEntry extractEntry : currentPSPositionJobExtractEntries) {
                            currentEntriesMap.put(extractEntry.getKey(), extractEntry);
                        }

                        //build hash map for new entries
                        Map<String, PSPositionJobExtractEntry> newEntriesMap = new HashMap<String, PSPositionJobExtractEntry>();
                        for (PSPositionJobExtractEntry extractEntry : inputPSPositionJobExtractEntries) {
                            newEntriesMap.put(extractEntry.getKey(), extractEntry);
                        }

                        // log entries that changed 
                        StringBuffer newChangedlogInfo = new StringBuffer();
                        newChangedlogInfo.append("\n Changed/New entries in the new file:\n");

                        // filter entries
                        for (String key : newEntriesMap.keySet()) {

                            PSPositionJobExtractEntry currentEntry = currentEntriesMap.get(key);
                            PSPositionJobExtractEntry newEntry = newEntriesMap.get(key);

                            // basic filter, if anything changed on the line we take the new entry
                            if (currentEntry != null) {
                                if (currentEntry.equals(newEntry)) {
                                    //do nothing; these entries will have a status flag code of active = "-"
                                    // when we are done with this current entry remove it from the map; whatever is left will need to be deleted

                                } else {

                                    newEntry.deleteStatus = CUBCConstants.PSEntryStatus.UPDATE;
                                    // set change status
                                    newEntry.changeStatus = StatusFlag.CHANGED;

                                    if (newEntry.getAnnualRate() != null
                                            && !newEntry.getAnnualRate().equalsIgnoreCase(currentEntry.getAnnualRate())) {
                                        // set all changed
                                        if (newEntry.getCsfAccountingInfoList() != null
                                                && newEntry.getCsfAccountingInfoList().size() > 0) {
                                            for (PSPositionJobExtractAccountingInfo accInfo : newEntry
                                                    .getCsfAccountingInfoList()) {
                                                accInfo.setStatusFlag(StatusFlag.CHANGED);
                                            }
                                        } else {
                                            if (newEntry.getPosAccountingInfoList() != null
                                                    && newEntry.getPosAccountingInfoList().size() > 0) {
                                                for (PSPositionJobExtractAccountingInfo accInfo : newEntry
                                                        .getPosAccountingInfoList()) {
                                                    accInfo.setStatusFlag(StatusFlag.CHANGED);
                                                }
                                            }
                                        }
                                    } //else {

                                    boolean result = updateAccountingInfoStatusFlag(newEntry, currentEntry);

                                    if (result) {
                                        // delete old entry
                                        currentEntry.deleteStatus = CUBCConstants.PSEntryStatus.DELETE;
                                        filteredEntries.add(currentEntry);
                                    }
                                    // }

                                    //add to update list
                                    filteredEntries.add(newEntry);

                                    newChangedlogInfo.append(newEntry.toString() + "\n");

                                }
                                currentEntriesMap.remove(key);
                            } else {
                                //add to the toAdd list: this is a new entry that did not exist in the old file
                                newEntry.deleteStatus = CUBCConstants.PSEntryStatus.ADD;
                                //change status
                                newEntry.changeStatus = StatusFlag.NEW;

                                filteredEntries.add(newEntry);

                                newChangedlogInfo.append(newEntry.toString() + "\n");
                            }

                        }

                        LOG.info(newChangedlogInfo.toString());

                        StringBuffer deletedLogInfo = new StringBuffer();

                        deletedLogInfo
                                .append("\n Entries that existed in the old file but don't exist in the new file:\n");

                        // add whatever is left in the currententryMap to the toDelete list
                        for (PSPositionJobExtractEntry entry : currentEntriesMap.values()) {
                            entry.deleteStatus = CUBCConstants.PSEntryStatus.DELETE;
                            deletedLogInfo.append(entry.toString() + "\n");
                        }
                        LOG.info(deletedLogInfo.toString());

                        filteredEntries.addAll(currentEntriesMap.values());
                    }

                } catch (FileNotFoundException e) {
                    LOG.error("Current file to parse not found " + currentFileName, e);
                    filteredEntries = inputPSPositionJobExtractEntries;
                } catch (IOException e) {
                    LOG.error("error while getting current file bytes:  " + e.getMessage(), e);
                    filteredEntries = inputPSPositionJobExtractEntries;
                }
            }
        }

        return filteredEntries;

    }

    /**
     * Sets the flags on the accounting strings for the new/changed accounting strings.
     * 
     * @param newEntry
     * @param currentEntry
     */
    private boolean updateAccountingInfoStatusFlag(PSPositionJobExtractEntry newEntry,
            PSPositionJobExtractEntry currentEntry) {

        Map<String, PSPositionJobExtractAccountingInfo> newEntryCsfAccountingMap = null;
        Map<String, PSPositionJobExtractAccountingInfo> newEntryPosAccountingMap = null;
        Map<String, PSPositionJobExtractAccountingInfo> currentEntryCsfAccountingMap = null;
        Map<String, PSPositionJobExtractAccountingInfo> currentEntryPosAccountingMap = null;

        if (newEntry != null) {
            newEntryCsfAccountingMap = new HashMap<String, PSPositionJobExtractAccountingInfo>();
            for (PSPositionJobExtractAccountingInfo accountingInfo : newEntry.getCsfAccountingInfoList()) {
                newEntryCsfAccountingMap.put(accountingInfo.getKey(), accountingInfo);
            }

            newEntryPosAccountingMap = new HashMap<String, PSPositionJobExtractAccountingInfo>();
            for (PSPositionJobExtractAccountingInfo accountingInfo : newEntry.getPosAccountingInfoList()) {
                newEntryPosAccountingMap.put(accountingInfo.getKey(), accountingInfo);
            }
        }

        if (currentEntry != null) {
            currentEntryCsfAccountingMap = new HashMap<String, PSPositionJobExtractAccountingInfo>();
            for (PSPositionJobExtractAccountingInfo accountingInfo : currentEntry.getCsfAccountingInfoList()) {
                currentEntryCsfAccountingMap.put(accountingInfo.getKey(), accountingInfo);
            }

            currentEntryPosAccountingMap = new HashMap<String, PSPositionJobExtractAccountingInfo>();
            for (PSPositionJobExtractAccountingInfo accountingInfo : currentEntry.getPosAccountingInfoList()) {
                currentEntryPosAccountingMap.put(accountingInfo.getKey(), accountingInfo);
            }
        }

        boolean result = true;
        if (newEntryCsfAccountingMap != null && newEntryCsfAccountingMap.size() > 0) {
            result = updateStatusFlag(currentEntryCsfAccountingMap, newEntryCsfAccountingMap);
        } else {
            if (newEntryPosAccountingMap != null && newEntryPosAccountingMap.size() > 0)
                result = updateStatusFlag(currentEntryPosAccountingMap, newEntryPosAccountingMap);
        }
        return result;

    }

    /**
     * Sets the flags on the accounting strings for the new/changed accounting strings.
     * 
     * @param currentInfoMap
     * @param newInfoMap
     */
    private boolean updateStatusFlag(Map<String, PSPositionJobExtractAccountingInfo> currentInfoMap,
            Map<String, PSPositionJobExtractAccountingInfo> newInfoMap) {

        boolean result = true;
        if (newInfoMap != null) {
            for (String key : newInfoMap.keySet()) {
                if (currentInfoMap != null) {
                    if (currentInfoMap.containsKey(key)) {
                        PSPositionJobExtractAccountingInfo newInfo = newInfoMap.get(key);
                        PSPositionJobExtractAccountingInfo currentInfo = currentInfoMap.get(key);
                        if (newInfo.equals(currentInfo)) {
                            newInfoMap.get(key).setStatusFlag(StatusFlag.ACTIVE);
                        } else {
                            result = false;
                            newInfoMap.get(key).setStatusFlag(StatusFlag.CHANGED);
                        }
                    } else {
                        newInfoMap.get(key).setStatusFlag(StatusFlag.CHANGED);
                    }
                } else {
                    newInfoMap.get(key).setStatusFlag(StatusFlag.CHANGED);
                }
            }
        }

        return result;

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
            StringBuffer warningMessage = new StringBuffer();
            valid &= validatePosition(extractEntry.getPositionNumber());

            if (!valid) {
                warningMessage.append("Invalid position number: " + extractEntry.getPositionNumber() + "\n");
                continue;
            }
            valid &= validateCSFAmount(extractEntry.getAnnualRate());
            if (!valid) {
                warningMessage.append("Invalid csf Amount: " + extractEntry.getAnnualRate() + "\n");
                continue;
            }

            if (valid) {
                if (extractEntry.getCsfAccountingInfoList() != null
                        && extractEntry.getCsfAccountingInfoList().size() > 0) {
                    for (PSPositionJobExtractAccountingInfo accountingInfo : extractEntry
                            .getCsfAccountingInfoList()) {
                        StringBuffer accInfoWarningMessage = validateAccountingInfo(universityFiscalYear, extractEntry,
                                accountingInfo);
                        if (accInfoWarningMessage != null && accInfoWarningMessage.length() > 0) {
                            valid = false;
                            warningMessage.append(accInfoWarningMessage);
                        }
                    }
                } else {
                    for (PSPositionJobExtractAccountingInfo accountingInfo : extractEntry
                            .getPosAccountingInfoList()) {
                        StringBuffer accInfoWarningMessage = validateAccountingInfo(universityFiscalYear, extractEntry,
                                accountingInfo);
                        if (accInfoWarningMessage != null && accInfoWarningMessage.length() > 0) {
                            valid = false;
                            warningMessage.append(accInfoWarningMessage);
                        }
                    }
                }
            }

            if (valid) {
                validEntries.add(extractEntry);
            } else {
                LOG.warn("\n Invalid entry for position number: " + extractEntry.getPositionNumber()
                        + " and employee ID: " + extractEntry.getEmplid() + "\n" +
                        "Errors found: " + warningMessage.toString() + "\n");

            }

        }

        return validEntries;

    }

    /**
     * Validates the accounting string.
     * 
     * @param universityFiscalYear
     * @param extractEntry
     * @param accountingInfo
     * @return a warning message if invalid, an empty warning message if valid
     */
    protected StringBuffer validateAccountingInfo(int universityFiscalYear, PSPositionJobExtractEntry extractEntry,
            PSPositionJobExtractAccountingInfo accountingInfo) {

        boolean valid = true;
        StringBuffer warningMessage = new StringBuffer();

        if (StringUtils.isNotBlank(accountingInfo.getCsfTimePercent())) {
            valid = validateTimePercent(accountingInfo.getCsfTimePercent());
            if (!valid) {
                warningMessage.append("Invalid time percent " + accountingInfo.getCsfTimePercent() + "\n");
            }
            valid = validateAccount(accountingInfo.getChartOfAccountsCode(),
                    accountingInfo.getAccountNumber());
            if (!valid) {
                warningMessage.append("Invalid Account: " + accountingInfo.getChartOfAccountsCode() + ","
                        + accountingInfo.getAccountNumber() + "\n");
            }
            if (StringUtils.isNotBlank(accountingInfo.getSubAccountNumber())) {
                valid = validateSubAccount(accountingInfo.getChartOfAccountsCode(),
                        accountingInfo.getAccountNumber(),
                        accountingInfo.getSubAccountNumber());
                if (!valid) {
                    warningMessage.append("Invalid Sub Account: " + accountingInfo.getChartOfAccountsCode() + ","
                            + accountingInfo.getAccountNumber() + ","
                            + accountingInfo.getSubAccountNumber()
                            + "\n");
                }
            }

            valid = validateLaborObject(universityFiscalYear, accountingInfo.getChartOfAccountsCode(),
                    accountingInfo.getFinancialObjectCode());
            if (!valid) {
                warningMessage.append("Invalid Labor Object: " + universityFiscalYear + ","
                        + accountingInfo.getChartOfAccountsCode() + ","
                        + accountingInfo.getFinancialObjectCode() + "\n");
            }
            valid = validateObjectCode(universityFiscalYear, accountingInfo.getChartOfAccountsCode(),
                    accountingInfo.getFinancialObjectCode());
            if (!valid) {
                warningMessage.append("Invalid Object Code: " + universityFiscalYear + ","
                        + accountingInfo.getChartOfAccountsCode() + ","
                        + accountingInfo.getFinancialObjectCode() + "\n");
            }

            if (StringUtils.isNotBlank(accountingInfo.getFinancialSubObjectCode())) {
                valid = validateSubObject(universityFiscalYear, accountingInfo.getChartOfAccountsCode(),
                        accountingInfo.getAccountNumber(), accountingInfo.getFinancialObjectCode(),
                        accountingInfo.getFinancialSubObjectCode());
                if (!valid) {
                    warningMessage.append("Invalid Sub Object Code: " + universityFiscalYear + ","
                            + accountingInfo.getChartOfAccountsCode() + ","
                            + accountingInfo.getFinancialObjectCode() + "\n");
                }
            }
        }

        return warningMessage;
    }

    /**
     * Validates that Position is in HR / P has a "Budgeted Position" = "Y".
     * 
     * @param positionNumber
     * 
     * @return true if valid, false otherwise
     */
    protected boolean validatePosition(String positionNumber) {
        return positionDataDao.isPositionBudgeted(positionNumber);
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
            generateThreeDecimalsBigDecimal(timePercent);
        } catch (NumberFormatException exception) {
            valid = false;
        }
        return valid;
    }

    /**
     * Builds maps with objects to be loaded in the database
     * 
     * @param validCsfTackerEntries
     * @param entriesToLoad
     * @param positionInfoMap
     * @param jobInfoMap
     * @param jobCodeMap
     */
    private void generateNewEntriesToLoadInTheDB(Collection<PSPositionJobExtractEntry> validCsfTackerEntries,
            List<CalculatedSalaryFoundationTracker> entriesToLoad, Map<String, PSPositionInfo> positionInfoMap,
            Map<String, PSJobData> jobInfoMap, Map<String, PSJobCode> jobCodeMap) {

        for (PSPositionJobExtractEntry psPositionJobExtractEntry : validCsfTackerEntries) {

            // entries for the CalculatedSalaryFoundationTracker
            entriesToLoad.addAll(generateCalculatedSalaryFoundationTrackerCollection(psPositionJobExtractEntry));

            // build map for position Info
            if (positionInfoMap.get(psPositionJobExtractEntry.getPositionNumber()) == null) {
                positionInfoMap
                            .put(psPositionJobExtractEntry.getPositionNumber()
                                    + psPositionJobExtractEntry.getDeleteStatus(),
                                    generatePSPositionInfo(psPositionJobExtractEntry));
            }

            // build map for job info
            if (StringUtils.isNotBlank(psPositionJobExtractEntry.getEmplid())
                        && (jobInfoMap.get(psPositionJobExtractEntry.getKey()) == null)) {
                jobInfoMap.put(psPositionJobExtractEntry.getKey() + psPositionJobExtractEntry.getDeleteStatus(),
                        generatePSJobData(psPositionJobExtractEntry));
            }

            if (StringUtils.isNotBlank(psPositionJobExtractEntry.getJobCode())
                        && (jobCodeMap.get(psPositionJobExtractEntry.getJobCode()) == null)) {
                // build map for job code
                jobCodeMap
                            .put(psPositionJobExtractEntry.getJobCode() + psPositionJobExtractEntry.getDeleteStatus(),
                                    generatePSJobCode(psPositionJobExtractEntry));
            }
        }
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
                .getCsfAccountingInfoList() != null && psPositionJobExtractEntry
                .getCsfAccountingInfoList().size() > 0) {
            for (PSPositionJobExtractAccountingInfo accountingInfo : psPositionJobExtractEntry
                    .getCsfAccountingInfoList()) {
                CalculatedSalaryFoundationTracker csfEntryFromMap = mapOfEntries.get(accountingInfo.getKey());
                CalculatedSalaryFoundationTracker entry = generateCSFEntryWithCummulation(csfEntryFromMap,
                        psPositionJobExtractEntry,
                        accountingInfo, csfAmount, positionNumber, universityFiscalYear, emplid, name,
                        csfCreateTimestamp);
                mapOfEntries.put(accountingInfo.getKey(), entry);

            }
        }

        // if there are no accounting strings at the job level get the info from the position level
        else {
            if (psPositionJobExtractEntry
                    .getPosAccountingInfoList() != null && psPositionJobExtractEntry
                    .getPosAccountingInfoList().size() > 0) {
                for (PSPositionJobExtractAccountingInfo accountingInfo : psPositionJobExtractEntry
                        .getPosAccountingInfoList()) {
                    CalculatedSalaryFoundationTracker csfEntryFromMap = mapOfEntries.get(accountingInfo.getKey());
                    CalculatedSalaryFoundationTracker entry = generateCSFEntryWithCummulation(csfEntryFromMap,
                            psPositionJobExtractEntry, accountingInfo, csfAmount, positionNumber, universityFiscalYear,
                            emplid, name, csfCreateTimestamp);
                    mapOfEntries.put(accountingInfo.getKey(), entry);

                }
            }
        }

        return mapOfEntries.values();

    }

    /**
     * Generates a CalculatedSalaryFoundationTracker entry if one does not already exist
     * for account, sub account, object, sub object key. If one already exists just add
     * the amount to the existing one. Also checks if one entry of the other has a
     * changed/ new status flag and keeps the one that is different from active.
     * 
     * TODO: NEW should take precedence to change so if we have a new and a changed with
     * the same key, keep the new flag
     * 
     * @param csfEntryFromMap
     * @param psPositionJobExtractEntry
     * @param accountingInfo
     * @param csfAmount
     * @param positionNumber
     * @param universityFiscalYear
     * @param emplid
     * @param name
     * @param csfCreateTimestamp
     * @return
     */
    protected CalculatedSalaryFoundationTracker generateCSFEntryWithCummulation(
            CalculatedSalaryFoundationTracker csfEntryFromMap,
            PSPositionJobExtractEntry psPositionJobExtractEntry, PSPositionJobExtractAccountingInfo accountingInfo,
            KualiDecimal csfAmount, String positionNumber, Integer universityFiscalYear, String emplid, String name,
            Timestamp csfCreateTimestamp) {

        BigDecimal csfTimePercent = generateThreeDecimalsBigDecimal(accountingInfo.getCsfTimePercent());
        BigDecimal csfFullTimeEmploymentQuantity = BigDecimal.ZERO;
        if (csfTimePercent.compareTo(BigDecimal.ZERO) != 0) {
            csfFullTimeEmploymentQuantity = csfTimePercent.divide(new BigDecimal(100));
        }

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
        String csfFundingStatusCode = StatusFlag.ACTIVE.getFlagValue();

        if (StatusFlag.NEW.equals(psPositionJobExtractEntry.getChangeStatus())) {
            csfFundingStatusCode = StatusFlag.NEW.getFlagValue();
        } else {
            csfFundingStatusCode = accountingInfo.getStatusFlag() != null ? accountingInfo.getStatusFlag()
                    .getFlagValue() : StatusFlag.ACTIVE.getFlagValue();
        }
        KualiDecimal percentOfCSFAmount = new KualiDecimal(csfAmount.bigDecimalValue().multiply(
                csfFullTimeEmploymentQuantity));

        CalculatedSalaryFoundationTracker entry = generateCalculatedSalaryFoundationTracker(positionNumber,
                universityFiscalYear, emplid, name, csfCreateTimestamp, csfFullTimeEmploymentQuantity,
                percentOfCSFAmount,
                csfTimePercent, chartOfAccountsCode, accountNumber, subAccountNumber, financialObjectCode,
                financialSubObjectCode, csfDeleteCode, csfFundingStatusCode);

        // if the PS entry has several accounting Strings with the same account, sub account, object, sub object then we add only one entry in the CSF tracker and we add up the percentages and the amounts

        if (csfEntryFromMap != null) {
            BigDecimal tmpTimePercent = csfEntryFromMap.getCsfTimePercent().add(csfTimePercent);
            entry.setCsfTimePercent(tmpTimePercent);

            BigDecimal csfFTE = csfEntryFromMap.getCsfFullTimeEmploymentQuantity().add(
                    csfFullTimeEmploymentQuantity);
            entry.setCsfFullTimeEmploymentQuantity(csfFTE);

            KualiDecimal amount = csfEntryFromMap.getCsfAmount().add(percentOfCSFAmount);
            entry.setCsfAmount(amount);

            if (!CUBCConstants.StatusFlag.ACTIVE.getFlagValue().equalsIgnoreCase(
                    csfEntryFromMap.getCsfFundingStatusCode())) {
                entry.setCsfFundingStatusCode(csfEntryFromMap.getCsfFundingStatusCode());
            }
        }

        return entry;
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
        psPositionInfo.setPositionType(psPositionJobExtractEntry.getEmployeeType());
        psPositionInfo.setPositionUnionCode(psPositionJobExtractEntry.getPositionUnionCode());
        psPositionInfo.setWorkMonths(generateWorkMonths(psPositionJobExtractEntry.getWorkMonths()));
        psPositionInfo.setJobCode(psPositionJobExtractEntry.getJobCode());
        psPositionInfo.setClassInd(psPositionJobExtractEntry.getClassInd());
        psPositionInfo.setCuStateCert(psPositionJobExtractEntry.getCuStateCert());
        psPositionInfo.setFullPartTime(psPositionJobExtractEntry.getFullPartTime());
        psPositionInfo.setAddsToActualFte(psPositionJobExtractEntry.getAddsToActualFte());
        psPositionInfo.setStatus(psPositionJobExtractEntry.deleteStatus);

        return psPositionInfo;

    }

    protected PSJobData generatePSJobData(PSPositionJobExtractEntry psPositionJobExtractEntry) {

        PSJobData psJobData = new PSJobData();

        psJobData.setPositionNumber(psPositionJobExtractEntry.getPositionNumber());
        psJobData.setEmplid(psPositionJobExtractEntry.getEmplid());
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
        psJobData.setCuPlannedFTE(generateKualiDecimal(psPositionJobExtractEntry.getCuPlannedFTE()));
        String positionRehireDate = psPositionJobExtractEntry.getReHireDate();
        Date reHireDate = null;

        try {
            if (positionRehireDate != null && StringUtils.isNotBlank(positionRehireDate)) {
                // the date comes in YYYY-MM-DD format so we change it to MM/DD/YYYY
                String[] dateParts = positionRehireDate.split("-");
                if (dateParts.length == 3) {
                    positionRehireDate = dateParts[1] + "/" + dateParts[2] + "/" + dateParts[0];

                    reHireDate = dateTimeService.convertToSqlDate(positionRehireDate);
                } else {
                    LOG.error("Invalid re-hire date for position:" + psPositionJobExtractEntry.getPositionNumber()
                            + " and emplid:" + psPositionJobExtractEntry.getEmplid() + "\n");
                }
            }
        } catch (ParseException e) {
            throw new RuntimeException("Invalid re-hire date");
        }

        psJobData.setReHireDate(reHireDate);
        psJobData.setSipEligibility(generateSipEligilibility(reHireDate));
        psJobData.setEmployeeType(generateEmployeeType(psPositionJobExtractEntry));
        psJobData.setStatus(psPositionJobExtractEntry.deleteStatus);

        return psJobData;
    }

    /**
     * Generates the SIP Eligibility Flag. If the re-hire date is before March 1st of the
     * source year as in the BC SOURCE parameter then the SIP Eligibility flag is Y,
     * otherwise is N.
     * 
     * @param reHireDate
     * @return Y if the re-hire date is before March 1st of the source year as in the BC
     * SOURCE parameter, N otherwise.
     */
    protected String generateSipEligilibility(Date reHireDate) {
        String sipEligibility = CUBCConstants.SIPEligibility.NO;
        int sourceYear = BudgetParameterFinder.getBaseFiscalYear();

        try {
            Date marchFirst = dateTimeService.convertToSqlDate("03/01/" + sourceYear);
            if (reHireDate.before(marchFirst)) {
                sipEligibility = CUBCConstants.SIPEligibility.YES;
            }
        } catch (ParseException e) {
            LOG.warn("Unable to create March 1st date!\n");
        }

        return sipEligibility;

    }

    /**
     * Generates the employee type.
     * 
     * @param psPositionJobExtractEntry
     * @return the employee type
     */
    protected String generateEmployeeType(PSPositionJobExtractEntry psPositionJobExtractEntry) {
        String employeeType = CUBCConstants.EmployeeType.DEFAULT;

        String jobFunction = psPositionJobExtractEntry.getJobFunction();
        String positionType = psPositionJobExtractEntry.getEmployeeType();
        String jobCode = psPositionJobExtractEntry.getJobCode();
        String positionNumber = psPositionJobExtractEntry.getPositionNumber();
        String unionCode = psPositionJobExtractEntry.getPositionUnionCode();

        // Faculty F if job function is ACF
        if (CUBCConstants.JobFunction.ACF.equalsIgnoreCase(psPositionJobExtractEntry.getJobFunction())) {
            employeeType = CUBCConstants.EmployeeType.FACULTY;
        }

        // Academics A if job function is ACO, ACL, ACR or ACS or job code is 10841 or 10835
        boolean isNonFacultyAcademic = CUBCConstants.JobFunction.ACO.equalsIgnoreCase(jobFunction)
                || CUBCConstants.JobFunction.ACL.equalsIgnoreCase(jobFunction)
                || CUBCConstants.JobFunction.ACR.equalsIgnoreCase(jobFunction)
                || CUBCConstants.JobFunction.ACS.equalsIgnoreCase(jobFunction) ||
                CUBCConstants.JobCode._10841.equalsIgnoreCase(jobCode)
                || CUBCConstants.JobCode._10835.equalsIgnoreCase(jobCode);

        if (isNonFacultyAcademic) {
            employeeType = CUBCConstants.EmployeeType.NON_FACULTY_ACADEMICS;
        }

        // Exempt staff E if job function is (ACF, ACO, ACL, ACR or ACS) and position type S
        boolean isExemptStaff = !(CUBCConstants.JobFunction.ACF.equalsIgnoreCase(jobFunction)
                || CUBCConstants.JobFunction.ACO.equalsIgnoreCase(jobFunction)
                || CUBCConstants.JobFunction.ACL.equalsIgnoreCase(jobFunction)
                || CUBCConstants.JobFunction.ACR.equalsIgnoreCase(jobFunction) || CUBCConstants.JobFunction.ACS
                .equalsIgnoreCase(jobFunction)) && CUBCConstants.PositionType.EXEMPT.equalsIgnoreCase(positionType);

        if (isExemptStaff) {
            employeeType = CUBCConstants.EmployeeType.EXEMPT_STAFF;
        }

        //Non-exempt N if job function is ACF, ACO, ACL, ACR, ACS and position type E or H
        boolean isNonExemptStaff = !(CUBCConstants.JobFunction.ACF.equalsIgnoreCase(jobFunction)
                || CUBCConstants.JobFunction.ACO.equalsIgnoreCase(jobFunction)
                || CUBCConstants.JobFunction.ACL.equalsIgnoreCase(jobFunction)
                || CUBCConstants.JobFunction.ACR.equalsIgnoreCase(jobFunction) || CUBCConstants.JobFunction.ACS
                .equalsIgnoreCase(jobFunction))
                && (CUBCConstants.PositionType.NON_EXEMPT_E.equalsIgnoreCase(positionType) || CUBCConstants.PositionType.NON_EXEMPT_H
                        .equalsIgnoreCase(positionType));

        if (isNonExemptStaff) {
            employeeType = CUBCConstants.EmployeeType.NON_EXEMPT_STAFF;
        }

        // if position number is SIP_EXECUTIVES param values then empl type is Z = Executives
        List<String> sipExecutives = CUBudgetParameterFinder.getSIPExecutives();

        if (sipExecutives != null && sipExecutives.contains(positionNumber)) {
            employeeType = CUBCConstants.EmployeeType.EXECUTIVES;
        }

        // union 
        boolean isUnion = false;

        if (StringUtils.isNotBlank(unionCode)) {
            for (String union : CUBCConstants.UNION_CODES_SIP) {
                if (union.equalsIgnoreCase(unionCode)) {
                    isUnion = true;
                    break;
                }
            }
        }

        if (isUnion) {
            employeeType = CUBCConstants.EmployeeType.UNION;
        }
        return employeeType;
    }

    /**
     * Generates a PSJobCode entry from the PS export entry.
     * 
     * @param psPositionJobExtractEntry
     * @return PSJobCode entry
     */
    protected PSJobCode generatePSJobCode(PSPositionJobExtractEntry psPositionJobExtractEntry) {

        PSJobCode psJobCode = new PSJobCode();

        psJobCode.setJobCode(psPositionJobExtractEntry.getJobCode());
        psJobCode.setCuObjectCode(psPositionJobExtractEntry.getDefaultObjectCode());
        psJobCode.setJobCodeDesc(psPositionJobExtractEntry.getJobCodeDesc());
        psJobCode.setJobCodeDescShort(psPositionJobExtractEntry.getJobCodeDescShrt());
        psJobCode.setJobFamily(psPositionJobExtractEntry.getJobFamily());
        psJobCode.setJobStandardHours(generateKualiDecimal(psPositionJobExtractEntry.getJobCodeStandardHours()));
        psJobCode.setCompFreq(psPositionJobExtractEntry.getCompFreq());
        psJobCode.setJobFunction(psPositionJobExtractEntry.getJobFunction());
        psJobCode.setJobFunctionDesc(psPositionJobExtractEntry.getJobFunctionDesc());
        psJobCode.setStatus(psPositionJobExtractEntry.deleteStatus);

        return psJobCode;
    }

    /**
     * Generates a KualiDecimal from an input String by inserting first the decimal point
     * before the last two digits.
     * 
     * @param input
     * @return a KualiDecimal value for the input
     */
    protected KualiDecimal generateKualiDecimal(String input) {
        String result = input;
        result = result.substring(0, result.length() - 2) + "."
                + result.substring(result.length() - 2, result.length());

        return new KualiDecimal(result);
    }

    /**
     * Generates a BigDecimal from a String by first inserting the decimal point before
     * the last 3 digits.
     * 
     * @param input
     * @return
     */
    protected BigDecimal generateThreeDecimalsBigDecimal(String input) {

        String result = input;
        result = result.substring(0, result.length() - 3) + "."
                + result.substring(result.length() - 3, result.length());

        return new BigDecimal(result);
    }

    /**
     * Generates a delete code for the input PSPositionJobExtractEntry.
     * 
     * @param psPositionJobExtractEntry
     * @return the generated delete code
     */
    protected String generateDeleteCode(PSPositionJobExtractEntry psPositionJobExtractEntry) {

        //so far we have determined that the only value used in the system is "-". Cornell does not plan to keep a history of the changes in the csf tracker table so the other values are not needed.
        // String deleteCode = BCConstants.ACTIVE_CSF_DELETE_CODE;
        String deleteCode = BCConstants.csfFundingStatusFlag.ACTIVE.getFlagValue();

        if (CUBCConstants.PSEntryStatus.ADD.equals(psPositionJobExtractEntry.deleteStatus)
                || CUBCConstants.PSEntryStatus.UPDATE.equals(psPositionJobExtractEntry.deleteStatus)) {
            deleteCode = BCConstants.csfFundingStatusFlag.ACTIVE.getFlagValue();
        }

        if (CUBCConstants.PSEntryStatus.DELETE.equals(psPositionJobExtractEntry.deleteStatus)) {
            deleteCode = CUBCConstants.StatusFlag.DELETED.getFlagValue();
        }

        return deleteCode;

    }

    /**
     * Generate the workMonths from the value in the input file. The workMonths comes in
     * like this Work Months (pos 1174) 000 and we will will only use 00 and create an
     * Integer value.
     * 
     * @param workMonths
     * @return a KualiDecimal value for the input workMonths
     */
    protected KualiDecimal generateWorkMonths(String workMonths) {
        //prepare workMonths to create a KualiDecimal
        if (StringUtils.isNotBlank(workMonths)) {

            //prepare time percent
            String result = workMonths;
            result = result.substring(0, result.length() - 1) + "."
                    + result.substring(result.length() - 1, result.length());

            return new KualiDecimal(result);
        } else
            return null;
    }

    /**
     * Loads the new data in the database.
     * 
     * @param startFresh
     * @param entriesToLoad
     * @param positionInfoMap
     * @param jobInfoMap
     * @param jobCodeMap
     */
    private void loadDataInDB(boolean startFresh, List<CalculatedSalaryFoundationTracker> entriesToLoad,
            Map<String, PSPositionInfo> positionInfoMap,
            Map<String, PSJobData> jobInfoMap, Map<String, PSJobCode> jobCodeMap) {
        // load entries in the CSF tracker table
        loadEntriesInCSFTrackerTable(entriesToLoad, startFresh);

        // load entries in CU_PS_POSITION_EXTRA table
        loadEntriesInPSPositionInfoTable(new ArrayList<PSPositionInfo>(positionInfoMap.values()), startFresh);

        // load entries in CU_PS_JOB_CODE
        loadEntriesInPSJobCodeTable(new ArrayList<PSJobCode>(jobCodeMap.values()), startFresh);

        // load entries in CU_PS_JOB_DATA
        loadEntriesInPSJobDataTable(new ArrayList<PSJobData>(jobInfoMap.values()), startFresh);

        // log the number of entries loaded
        LOG.info("Total number of entries loaded/updated in the CSF Tracker table: "
                + Integer.toString(entriesToLoad.size()));
        LOG.info("Total number of entries loaded/updated in the  CU_PS_POSITION_EXTRA table: "
                + Integer.toString(positionInfoMap.values().size()));
        LOG.info("Total number of entries loaded/updated in the  CU_PS_JOB_DATA table: "
                + Integer.toString(jobInfoMap.values().size()));
        LOG.info("Total number of entries loaded/updated in the  CU_PS_JOB_CD table: "
                + Integer.toString(jobCodeMap.values().size()));
    }

    /**
     * Loads the entries in the LS_CSF_TRACKER_T table.
     * 
     * @param csfTackerEntries
     */
    protected void loadEntriesInCSFTrackerTable(List<CalculatedSalaryFoundationTracker> csfTrackerEntries,
            boolean startFresh) {

        if (startFresh) {
            // wipe out everything first
            businessObjectService
                    .deleteMatching(CalculatedSalaryFoundationTracker.class, new HashMap<String, String>());
            businessObjectService.save(csfTrackerEntries);
        } else {
            List<CalculatedSalaryFoundationTracker> csfTrackerEntriesForAddOrUpdate = new ArrayList<CalculatedSalaryFoundationTracker>();

            //load in the new entries
            for (CalculatedSalaryFoundationTracker entry : csfTrackerEntries) {

                // do deletes first and save the rest for add/update

                if (CUBCConstants.StatusFlag.DELETED.getFlagValue().equals(entry.getCsfDeleteCode())) {

                    Map<String, Object> keyFields = new HashMap<String, Object>();
                    keyFields.put(
                            CUBCPropertyConstants.CalculateSalaryFoundationTrackerProperties.UNIVERSITY_FISCAL_YEAR,
                            entry.getUniversityFiscalYear());
                    keyFields.put(CUBCPropertyConstants.CalculateSalaryFoundationTrackerProperties.CHART_OF_ACCOUNTS,
                            entry.getChartOfAccountsCode());
                    keyFields.put(CUBCPropertyConstants.CalculateSalaryFoundationTrackerProperties.ACCOUNT_NBR,
                            entry.getAccountNumber());
                    keyFields.put(CUBCPropertyConstants.CalculateSalaryFoundationTrackerProperties.SUB_ACCOUNT_NBR,
                            entry.getSubAccountNumber());
                    keyFields.put(CUBCPropertyConstants.CalculateSalaryFoundationTrackerProperties.FIN_OBJ_CD,
                            entry.getFinancialObjectCode());
                    keyFields.put(CUBCPropertyConstants.CalculateSalaryFoundationTrackerProperties.FIN_SUB_OBJ_CD,
                            entry.getFinancialSubObjectCode());
                    keyFields.put(CUBCPropertyConstants.CalculateSalaryFoundationTrackerProperties.CREATE_TIMESTAMP,
                            entry.getCsfCreateTimestamp());
                    keyFields.put(CUBCPropertyConstants.CalculateSalaryFoundationTrackerProperties.EMPLID,
                            entry.getEmplid());
                    keyFields.put(CUBCPropertyConstants.CalculateSalaryFoundationTrackerProperties.POSITION_NBR,
                            entry.getPositionNumber());

                    LOG.info("\n Deleting entry: " + toStringCSFTracker(entry));

                    businessObjectService.deleteMatching(CalculatedSalaryFoundationTracker.class, keyFields);

                } else {
                    // save for later
                    csfTrackerEntriesForAddOrUpdate.add(entry);
                }

            }

            // do the add/update now we don't delete entries to mimic the bcUpdate behavior with positions that is never delete positions for request year
            for (CalculatedSalaryFoundationTracker entry : csfTrackerEntriesForAddOrUpdate) {
                CalculatedSalaryFoundationTracker retrievedEntry = (CalculatedSalaryFoundationTracker) businessObjectService
                        .retrieve(entry);//(CalculatedSalaryFoundationTracker.class, keyFields);
                if (ObjectUtils.isNotNull(retrievedEntry)) {
                    entry.setVersionNumber(retrievedEntry.getVersionNumber());
                }
                LOG.info("\n Adding entry: " + toStringCSFTracker(entry));
                businessObjectService.save(entry);
            }
        }
    }

    private String toStringCSFTracker(CalculatedSalaryFoundationTracker entry) {
        StringBuffer stringRepresentation = new StringBuffer();
        stringRepresentation.append("\n CSF Tracker entry:");
        stringRepresentation.append("\n Position number: " + entry.getPositionNumber());
        stringRepresentation.append("\n Emplid: " + entry.getEmplid());
        stringRepresentation.append("\n University fiscal year: " + entry.getUniversityFiscalYear());
        stringRepresentation.append("\n Chart: " + entry.getChartOfAccountsCode());
        stringRepresentation.append("\n Acoount Number: " + entry.getAccountNumber());
        stringRepresentation.append("\n Sub Account Number: " + entry.getSubAccountNumber());
        stringRepresentation.append("\n Object Code: " + entry.getFinancialObjectCode());
        stringRepresentation.append("\n Financial Sub Object Code: " + entry.getFinancialSubObjectCode());
        return stringRepresentation.toString();
    }

    /**
     * Load entries in the CU_PS_POSITION_EXTRA table.
     * 
     * @param psPositionInfoEntries
     */
    protected void loadEntriesInPSPositionInfoTable(List<PSPositionInfo> psPositionInfoEntries, boolean startFresh) {
        if (startFresh) {
            businessObjectService
                    .deleteMatching(PSPositionInfo.class, new HashMap<String, String>());
            businessObjectService.save(psPositionInfoEntries);
        } else {

            // do the add/update now; we don't delete entries to mimic the bcUpdate behavior with positions that is never delete positions for request year
            for (PSPositionInfo entry : psPositionInfoEntries) {
                if (!CUBCConstants.PSEntryStatus.DELETE.equals(entry.getStatus())) {
                    PSPositionInfo retrievedEntry = (PSPositionInfo) businessObjectService.retrieve(entry);

                    if (ObjectUtils.isNotNull(retrievedEntry)) {
                        entry.setVersionNumber(retrievedEntry.getVersionNumber());
                    }
                    businessObjectService.save(entry);
                }
            }
        }
    }

    /**
     * Load entries in the CU_PS_JOB_DATA table.
     * 
     * @param jobDataEntries
     */
    protected void loadEntriesInPSJobDataTable(List<PSJobData> jobDataEntries, boolean startFresh) {

        if (startFresh) {
            businessObjectService
                    .deleteMatching(PSJobData.class, new HashMap<String, String>());
            businessObjectService.save(jobDataEntries);
        } else {

            List<PSJobData> jobDataEntriesForAddOrUpdate = new ArrayList<PSJobData>();
            //load in the new entries

            // do deletes first and save the rest for later add/update
            for (PSJobData entry : jobDataEntries) {

                if (CUBCConstants.PSEntryStatus.DELETE.equals(entry.getStatus())) {
                    Map<String, Object> keyFields = new HashMap<String, Object>();
                    keyFields.put(CUBCPropertyConstants.PSJobDataProperties.POSITION_NBR,
                                 entry.getPositionNumber());
                    keyFields.put(CUBCPropertyConstants.PSJobDataProperties.EMPLID, entry.getEmplid());
                    businessObjectService
                              .deleteMatching(PSJobData.class, keyFields);
                } else {
                    jobDataEntriesForAddOrUpdate.add(entry);

                }
            }

            // do the add/update now
            for (PSJobData entry : jobDataEntriesForAddOrUpdate) {
                PSJobData retrievedEntry = (PSJobData) businessObjectService.retrieve(entry);

                if (ObjectUtils.isNotNull(retrievedEntry)) {
                    entry.setVersionNumber(retrievedEntry.getVersionNumber());
                }
                businessObjectService.save(entry);
            }
        }
    }

    /**
     * Load entries in the CU_PS_JOB_CODE table.
     * 
     * @param psPositionInfoEntries
     */
    protected void loadEntriesInPSJobCodeTable(List<PSJobCode> psJobCodeEntries, boolean startFresh) {

        if (startFresh) {
            businessObjectService
                    .deleteMatching(PSJobCode.class, new HashMap<String, String>());
            businessObjectService.save(psJobCodeEntries);
        } else {

            // do the add/update now
            for (PSJobCode entry : psJobCodeEntries) {
                if (!CUBCConstants.PSEntryStatus.DELETE.equals(entry.getStatus())) {
                    PSJobCode retrievedEntry = (PSJobCode) businessObjectService.retrieve(entry);

                    if (ObjectUtils.isNotNull(retrievedEntry)) {
                        entry.setVersionNumber(retrievedEntry.getVersionNumber());
                    }
                    businessObjectService.save(entry);
                }
            }
        }
    }

    /**
     * Set the executives based on the SIP_EXECUTIVES parameter
     * 
     */
    protected void updateTheSipEligibility(List<PSPositionJobExtractEntry> psPositionJobExtractEntries) {

        // if position number is SIP_EXECUTIVES param values then empl type is Z = Executives
        List<String> sipExecutives = CUBudgetParameterFinder.getSIPExecutives();
        List<PSJobData> dataToUpdate = new ArrayList<PSJobData>();

        //build hash map for new entries
        Map<String, PSPositionJobExtractEntry> newEntriesMap = new HashMap<String, PSPositionJobExtractEntry>();
        for (PSPositionJobExtractEntry extractEntry : psPositionJobExtractEntries) {
            newEntriesMap.put(extractEntry.getKey(), extractEntry);
        }

        // get all jobs that are set as executives and generate employee type; in case someone was an executive and is not anymore it should set it the correct empl type

        List<PSJobData> existingExecutives = (List<PSJobData>) psJobDataDao.getExistingExecutives();
        List<String> existingExecutivesPositionNbrs = new ArrayList<String>();

        for (PSJobData jobData : existingExecutives) {
            existingExecutivesPositionNbrs.add(jobData.getPositionNumber());
        }

        for (PSJobData executive : existingExecutives) {
            //if not an executive anymore generate the empl type
            if (!sipExecutives.contains(executive.getPositionNumber())) {
                PSPositionJobExtractEntry psPositionJobExtractEntry = newEntriesMap.get(executive.getPositionNumber()
                        + executive.getEmplid());
                String employeeType = generateEmployeeType(psPositionJobExtractEntry);
                executive.setEmployeeType(employeeType);
                dataToUpdate.add(executive);
            }
        }

        if (dataToUpdate != null && dataToUpdate.size() > 0) {
            businessObjectService.save(dataToUpdate);

            dataToUpdate.clear();
        }

        // set as executives only those in the SIP_EXECUTIVES param

        List<PSJobData> psJobDataEntries = (List<PSJobData>) psJobDataDao
                .getPSJobDataEntriesByPositionNumbers(sipExecutives);

        for (PSJobData jobDataEntry : psJobDataEntries) {
            if (Collections.binarySearch(existingExecutivesPositionNbrs, jobDataEntry.getPositionNumber()) < 0) {
                jobDataEntry.setEmployeeType(CUBCConstants.EmployeeType.EXECUTIVES);
                dataToUpdate.add(jobDataEntry);
            }
        }

        businessObjectService.save(dataToUpdate);

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

    /**
     * Gets the psBudgetFeedFlatInputFileType.
     * 
     * @return psBudgetFeedFlatInputFileType
     */
    public BatchInputFileType getPsBudgetFeedFlatInputFileType() {
        return psBudgetFeedFlatInputFileType;
    }

    /**
     * Sets the psBudgetFeedFlatInputFileType.
     * 
     * @param psBudgetFeedFlatInputFileType
     */
    public void setPsBudgetFeedFlatInputFileType(BatchInputFileType psBudgetFeedFlatInputFileType) {
        this.psBudgetFeedFlatInputFileType = psBudgetFeedFlatInputFileType;
    }

    /**
     * Gets the positionDataDao.
     * 
     * @return positionDataDao
     */
    public PSPositionDataDao getPositionDataDao() {
        return positionDataDao;
    }

    /**
     * Sets the positionDataDao.
     * 
     * @param positionDataDao
     */
    public void setPositionDataDao(PSPositionDataDao positionDataDao) {
        this.positionDataDao = positionDataDao;
    }

    /**
     * Gets the psJobDataDao.
     * 
     * @return psJobDataDao
     */
    public PSJobDataDao getPsJobDataDao() {
        return psJobDataDao;
    }

    /**
     * Sets the psJobDataDao.
     * 
     * @param psJobDataDao
     */
    public void setPsJobDataDao(PSJobDataDao psJobDataDao) {
        this.psJobDataDao = psJobDataDao;
    }

}
