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
import edu.cornell.kfs.module.bc.CUBCConstants.StatusFlag;
import edu.cornell.kfs.module.bc.CUBCPropertyConstants;
import edu.cornell.kfs.module.bc.batch.service.PSBudgetFeedService;
import edu.cornell.kfs.module.bc.businessobject.PSJobCode;
import edu.cornell.kfs.module.bc.businessobject.PSJobData;
import edu.cornell.kfs.module.bc.businessobject.PSPositionInfo;
import edu.cornell.kfs.module.bc.businessobject.PSPositionJobExtractAccountingInfo;
import edu.cornell.kfs.module.bc.businessobject.PSPositionJobExtractEntry;

/**
 * An implementation of a service that contains methods to populate the CSF Tracker table
 * (LD_CSF_TRACKER_T) and CU_PS_POSITION_EXTRA, CU_PS_JOB_DATA, CU_PS_JOB_CD tables with PS/HR
 * data.
 */
public class PSBudgetFeedServiceImpl implements PSBudgetFeedService {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PSBudgetFeedServiceImpl.class);

    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType psBudgetFeedFlatInputFileType;
    protected DateTimeService dateTimeService;
    protected BusinessObjectService businessObjectService;
    protected DictionaryValidationService dictionaryValidationService;
    protected ParameterService parameterService;

    /**
     * @see edu.cornell.kfs.module.bc.batch.service.PSBudgetFeedService#populateCSFTracker(java.lang.String)
     */
    public boolean loadBCDataFromPS(String fileName, String currentFileName, boolean startFresh) {

        FileInputStream fileContents = null;

        try {
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
                return true;
            } else {
                setAccountingInfoLists(psPositionJobExtractEntries);
            }

            Collection<PSPositionJobExtractEntry> filteredPsPositionJobExtractEntries = null;
            // are we starting fresh? then we delete all old records

            if (startFresh || currentFileName == null) {
                filteredPsPositionJobExtractEntries = psPositionJobExtractEntries;
            } else {
                // filter only updated entries from last run
                //for better performance look for the last successful file so that we can compare with that
                filteredPsPositionJobExtractEntries = filterEntriesToUpdate(
                        currentFileName, psPositionJobExtractEntries, startFresh);
            }

            // if no current file treat as if we are starting fresh
            if (currentFileName == null) {
                startFresh = true;
            }

            // validate entries to load
            Collection<PSPositionJobExtractEntry> validCsfTackerEntries = validateEntriesForCSFTracker(filteredPsPositionJobExtractEntries);

            List<CalculatedSalaryFoundationTracker> entriesToLoad = new ArrayList<CalculatedSalaryFoundationTracker>();
            Map<String, PSPositionInfo> positionInfoMap = new HashMap<String, PSPositionInfo>();
            Map<String, PSJobData> jobInfoMap = new HashMap<String, PSJobData>();
            Map<String, PSJobCode> jobCodeMap = new HashMap<String, PSJobCode>();

            //generate CalculatedSalaryFoundationTracker, PSPositionInfo, PSJobData and PSJobCode entries from the valid PSPositionJobExtractEntry list
            for (PSPositionJobExtractEntry psPositionJobExtractEntry : validCsfTackerEntries) {

                // entries for the CalculatedSalaryFoundationTracker
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
                    jobCodeMap
                            .put(psPositionJobExtractEntry.getJobCode(), generatePSJobCode(psPositionJobExtractEntry));
                }
            }

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

            return true;

        } catch (FileNotFoundException e) {
            LOG.error("file to parse not found " + fileName, e);
            throw new RuntimeException(
                    "Cannot find the file requested to be parsed " + fileName
                            + " " + e.getMessage(), e);
        } catch (IOException e) {
            LOG.error("error while getting file bytes:  " + e.getMessage(), e);
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

        if (!startFresh) {
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
                    } else {
                        //set the lists of accounting info
                        for (PSPositionJobExtractEntry entry : currentPSPositionJobExtractEntries) {
                            entry.setCsfAccountingInfoList();
                            entry.setPosAccountingInfoList();
                        }
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

                        // filter entries
                        for (String key : newEntriesMap.keySet()) {

                            PSPositionJobExtractEntry currentEntry = currentEntriesMap.get(key);
                            PSPositionJobExtractEntry newEntry = newEntriesMap.get(key);

                            // basic filter, if anything changed on the line we take the new entry
                            if (currentEntry != null) {
                                if (currentEntry.equals(newEntry)) {
                                    //do nothing; these entries will have a status flag code of active = "-"
                                    // when we are done with this current entry remove it from the map; whatever is left will need to be deleted
                                    currentEntriesMap.remove(key);
                                } else {

                                    newEntry.deleteStatus = CUBCConstants.PSEntryStatus.UPDATE;
                                    // set change status
                                    newEntry.changeStatus = StatusFlag.CHANGED;
                                    updateAccountingInfoStatusFlag(newEntry, currentEntry);

                                    //add to update list
                                    filteredEntries.add(newEntry);
                                }
                            } else {
                                //add to the toAdd list: this is a new entry that did not exist in the old file
                                newEntry.deleteStatus = CUBCConstants.PSEntryStatus.ADD;
                                //change status
                                newEntry.changeStatus = StatusFlag.NEW;

                                filteredEntries.add(newEntry);
                            }

                        }

                        // add whatever is left in the currententryMap to the toDelete list
                        for (PSPositionJobExtractEntry entry : currentEntriesMap.values()) {
                            entry.deleteStatus = CUBCConstants.PSEntryStatus.DELETE;
                        }
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
    private void updateAccountingInfoStatusFlag(PSPositionJobExtractEntry newEntry,
            PSPositionJobExtractEntry currentEntry) {

        Map<String, PSPositionJobExtractAccountingInfo> currentEntryCsfAccountingMap = new HashMap<String, PSPositionJobExtractAccountingInfo>();
        for (PSPositionJobExtractAccountingInfo accountingInfo : currentEntry.getCsfAccountingInfoList()) {
            currentEntryCsfAccountingMap.put(accountingInfo.getKey(), accountingInfo);
        }

        Map<String, PSPositionJobExtractAccountingInfo> newEntryCsfAccountingMap = new HashMap<String, PSPositionJobExtractAccountingInfo>();
        for (PSPositionJobExtractAccountingInfo accountingInfo : newEntry.getCsfAccountingInfoList()) {
            newEntryCsfAccountingMap.put(accountingInfo.getKey(), accountingInfo);
        }

        Map<String, PSPositionJobExtractAccountingInfo> currentEntryPosAccountingMap = new HashMap<String, PSPositionJobExtractAccountingInfo>();
        for (PSPositionJobExtractAccountingInfo accountingInfo : currentEntry.getPosAccountingInfoList()) {
            currentEntryPosAccountingMap.put(accountingInfo.getKey(), accountingInfo);
        }

        Map<String, PSPositionJobExtractAccountingInfo> newEntryPosAccountingMap = new HashMap<String, PSPositionJobExtractAccountingInfo>();
        for (PSPositionJobExtractAccountingInfo accountingInfo : newEntry.getPosAccountingInfoList()) {
            newEntryPosAccountingMap.put(accountingInfo.getKey(), accountingInfo);
        }

        if (newEntryCsfAccountingMap != null && newEntryCsfAccountingMap.size() > 0) {
            updateStatusFlag(currentEntryCsfAccountingMap, newEntryCsfAccountingMap);
        } else {
            if (newEntryPosAccountingMap != null && newEntryPosAccountingMap.size() > 0)
                updateStatusFlag(currentEntryPosAccountingMap, newEntryPosAccountingMap);
        }

    }

    /**
     * Sets the flags on the accounting strings for the new/changed accounting strings.
     * 
     * @param currentInfoMap
     * @param newInfoMap
     */
    private void updateStatusFlag(Map<String, PSPositionJobExtractAccountingInfo> currentInfoMap,
            Map<String, PSPositionJobExtractAccountingInfo> newInfoMap) {

        if (newInfoMap != null) {
            for (String key : newInfoMap.keySet()) {
                if (currentInfoMap != null) {
                    if (currentInfoMap.containsKey(key)) {
                        newInfoMap.get(key).setStatusFlag(StatusFlag.ACTIVE);
                    } else {
                        newInfoMap.get(key).setStatusFlag(StatusFlag.CHANGED);
                    }
                } else {
                    newInfoMap.get(key).setStatusFlag(StatusFlag.CHANGED);
                }
            }
        }

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
                warningMessage.append("Invalid position number: " + extractEntry.getPositionNumber() + "; ");
                continue;
            }
            valid &= validateCSFAmount(extractEntry.getAnnualRate());
            if (!valid) {
                warningMessage.append("Invalid csf Amount: " + extractEntry.getAnnualRate() + "; ");
                continue;
            }

            if (valid) {
                if (extractEntry.getCsfAccountingInfoList() != null
                        && extractEntry.getCsfAccountingInfoList().size() > 0) {
                    for (PSPositionJobExtractAccountingInfo accountingInfo : extractEntry
                            .getCsfAccountingInfoList()) {
                        valid &= validateAccountingInfo(universityFiscalYear, extractEntry, accountingInfo);
                    }
                } else {
                    for (PSPositionJobExtractAccountingInfo accountingInfo : extractEntry
                            .getPosAccountingInfoList()) {
                        valid &= validateAccountingInfo(universityFiscalYear, extractEntry, accountingInfo);
                    }
                }
            }

            if (valid) {
                validEntries.add(extractEntry);
            } else {
                LOG.warn(warningMessage.toString() + " for " + extractEntry.toString());
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
     * @return true if valid, false otherwise
     */
    protected boolean validateAccountingInfo(int universityFiscalYear, PSPositionJobExtractEntry extractEntry,
            PSPositionJobExtractAccountingInfo accountingInfo) {

        boolean valid = true;
        StringBuffer warningMessage = new StringBuffer();

        if (StringUtils.isNotBlank(accountingInfo.getCsfTimePercent())) {
            valid &= validateTimePercent(accountingInfo.getCsfTimePercent());
            if (!valid) {
                warningMessage.append("Invalid time percent " + accountingInfo.getCsfTimePercent() + "; ");
            }
            valid &= validateAccount(accountingInfo.getChartOfAccountsCode(),
                    accountingInfo.getAccountNumber());
            if (!valid) {
                warningMessage.append("Invalid Account: " + accountingInfo.getChartOfAccountsCode() + "; ");
            }
            if (StringUtils.isNotBlank(accountingInfo.getSubAccountNumber())) {
                valid &= validateSubAccount(accountingInfo.getChartOfAccountsCode(),
                        accountingInfo.getAccountNumber(),
                        accountingInfo.getSubAccountNumber());
                if (!valid) {
                    warningMessage.append("Invalid Sub Account: " + accountingInfo.getChartOfAccountsCode() + ","
                            + accountingInfo.getAccountNumber() + ","
                            + accountingInfo.getSubAccountNumber()
                            + "; ");
                }
            }

            valid &= validateLaborObject(universityFiscalYear, accountingInfo.getChartOfAccountsCode(),
                    accountingInfo.getFinancialObjectCode());
            if (!valid) {
                warningMessage.append("Invalid Labor Object: " + universityFiscalYear + ","
                        + accountingInfo.getChartOfAccountsCode() + ","
                        + accountingInfo.getFinancialObjectCode() + "; ");
            }
            valid &= validateObjectCode(universityFiscalYear, accountingInfo.getChartOfAccountsCode(),
                    accountingInfo.getFinancialObjectCode());
            if (!valid) {
                warningMessage.append("Invalid Object Code: " + universityFiscalYear + ","
                        + accountingInfo.getChartOfAccountsCode() + ","
                        + accountingInfo.getFinancialObjectCode() + "; ");
            }

            if (StringUtils.isNotBlank(accountingInfo.getFinancialSubObjectCode())) {
                valid &= validateSubObject(universityFiscalYear, accountingInfo.getChartOfAccountsCode(),
                        accountingInfo.getAccountNumber(), accountingInfo.getFinancialObjectCode(),
                        accountingInfo.getFinancialSubObjectCode());
                if (!valid) {
                    warningMessage.append("Invalid Sub Object Code: " + universityFiscalYear + ","
                            + accountingInfo.getChartOfAccountsCode() + ","
                            + accountingInfo.getFinancialObjectCode() + "; ");
                }
            }
        }

        if (warningMessage.length() > 0) {
            LOG.warn(warningMessage.toString() + " for entry " + extractEntry.getPositionNumber() + " "
                    + extractEntry.getEmplid() + " accounting string:" + accountingInfo.toString());
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

        // if in the map add percentages and add only one account

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
        psJobData.setStatus(psPositionJobExtractEntry.deleteStatus);

        return psJobData;
    }

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

            //load in the new entries
            for (CalculatedSalaryFoundationTracker entry : csfTrackerEntries) {
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

                businessObjectService
                            .deleteMatching(CalculatedSalaryFoundationTracker.class, keyFields);

                if (CUBCConstants.StatusFlag.ACTIVE.getFlagValue().equals(entry.getCsfDeleteCode())) {

                    businessObjectService.save(entry);

                }

            }
        }
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

            //load in the new entries
            for (PSPositionInfo entry : psPositionInfoEntries) {
                Map<String, Object> keyFields = new HashMap<String, Object>();
                keyFields.put(CUBCPropertyConstants.PSPositionInfoProperties.POSITION_NBR,
                              entry.getPositionNumber());

                businessObjectService
                              .deleteMatching(PSPositionInfo.class, keyFields);

                if (CUBCConstants.PSEntryStatus.UPDATE.equals(entry.getStatus())
                        || CUBCConstants.PSEntryStatus.ADD.equals(entry.getStatus())) {
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

            //load in the new entries
            for (PSJobData entry : jobDataEntries) {
                Map<String, Object> keyFields = new HashMap<String, Object>();
                keyFields.put(CUBCPropertyConstants.PSJobDataProperties.POSITION_NBR,
                             entry.getPositionNumber());
                keyFields.put(CUBCPropertyConstants.PSJobDataProperties.EMPLID, entry.getEmplid());

                businessObjectService
                             .deleteMatching(PSJobData.class, keyFields);

                if (CUBCConstants.PSEntryStatus.UPDATE.equals(entry.getStatus())
                        || CUBCConstants.PSEntryStatus.ADD.equals(entry.getStatus())) {

                    businessObjectService.save(entry);
                }
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

            //load in the new entries
            for (PSJobCode entry : psJobCodeEntries) {
                Map<String, Object> keyFields = new HashMap<String, Object>();
                keyFields.put(CUBCPropertyConstants.PSJobCodeProperties.JOB_CD,
                             entry.getJobCode());

                businessObjectService
                             .deleteMatching(PSJobCode.class, keyFields);

                if (CUBCConstants.PSEntryStatus.UPDATE.equals(entry.getStatus())
                        || CUBCConstants.PSEntryStatus.ADD.equals(entry.getStatus())) {
                    businessObjectService.save(entry);
                }

            }
        }
    }

    /**
     * Sets the accounting info lists in the PS entries. Each entry has 10 accounting
     * strings at position level and 10 at job level. This methods creates two lists on
     * each entries for the accounting Strings. This allows for easy manipulation of the
     * accounting strings during the process.
     * 
     * @param psPositionJobExtractEntries
     */
    protected void setAccountingInfoLists(List<PSPositionJobExtractEntry> psPositionJobExtractEntries) {
        //set the lists of accounting info
        for (PSPositionJobExtractEntry entry : psPositionJobExtractEntries) {
            entry.setCsfAccountingInfoList();
            entry.setPosAccountingInfoList();
        }
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

}
