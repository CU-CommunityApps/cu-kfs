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
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.cornell.kfs.module.bc.CUBCConstants;
import edu.cornell.kfs.module.bc.batch.service.PopulateCSFTrackerService;
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

    /**
     * @see edu.cornell.kfs.module.bc.batch.service.PopulateCSFTrackerService#populateCSFTracker(java.lang.String)
     */
    public boolean populateCSFTracker(String fileName) {

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

        Collection csfTackerEntries = null;
        // read csf tracker entries
        try {
            byte[] fileByteContent = IOUtils.toByteArray(fileContents);
            csfTackerEntries = (Collection) batchInputFileService.parse(
                    csfTrackerFlatInputFileType, fileByteContent);
        } catch (IOException e) {
            LOG.error("error while getting file bytes:  " + e.getMessage(), e);
            throw new RuntimeException(
                    "Error encountered while attempting to get file bytes: "
                            + e.getMessage(), e);
        }

        // if no entries read log
        if (csfTackerEntries == null || csfTackerEntries.isEmpty()) {
            LOG.warn("No entries in the PS Job extract input file " + fileName);
        }

        // filter only updated entries from last run
        //for better performance look for the last successful file so that we can compare with that
        Collection<PSPositionJobExtractEntry> filteredCsfTackerEntries = filterEntriesToUpdate(fileName,
                csfTackerEntries);

        // load entries in CSF tracker
        Collection<PSPositionJobExtractEntry> validCsfTackerEntries = validateEntriesForCSFTracker(csfTackerEntries);

        List<CalculatedSalaryFoundationTracker> entriesToLoad = new ArrayList<CalculatedSalaryFoundationTracker>();

        //generate CalculatedSalaryFoundationTracker entries
        for (PSPositionJobExtractEntry psPositionJobExtractEntry : validCsfTackerEntries) {
            entriesToLoad.addAll(generateCalculatedSalaryFoundationTrackerCollection(psPositionJobExtractEntry));
        }

        loadEntriesInCSFTrackerTable(entriesToLoad);

        // log the number of entries loaded
        LOG.info("Total entries loaded: " + Integer.toString(csfTackerEntries.size()));
        return true;

    }

    /**
     * Returns only the entries that were updated in the new PS extract.
     * 
     * @param fileName
     * @param csfTackerEntries
     * @return
     */
    Collection<PSPositionJobExtractEntry> filterEntriesToUpdate(String fileName,
            Collection<PSPositionJobExtractEntry> csfTackerEntries) {
        Collection<PSPositionJobExtractEntry> filteredCsfTackerEntries = csfTackerEntries;
        //        Collection<PSPositionJobExtractEntry> currentCsfTackerEntries = null;
        //        
        //        FileInputStream fileContents = null;
        //        
        //        // get the current file name
        //        String currentFileName = fileName.substring(0,fileName.lastIndexOf(csfTrackerFlatInputFileType.getFileExtension())) + ".current";
        //
        //        //read file contents
        //        try {
        //            fileContents = new FileInputStream(currentFileName);
        //            byte[] fileByteContent = IOUtils.toByteArray(fileContents);
        //            currentCsfTackerEntries = (Collection) batchInputFileService.parse(
        //                    csfTrackerFlatInputFileType, fileByteContent);
        //            
        //            if(currentCsfTackerEntries!=null){
        //                //build hash maps
        //                for(int i=0; i< currentCsfTackerEntries.size(); i++){
        //                    
        //                }
        //            }
        //        } catch (FileNotFoundException e) {
        //            LOG.error("Current file to parse not found " + fileName, e);       
        //        }
        //        catch (IOException e) {
        //            LOG.error("error while getting current file bytes:  " + e.getMessage(), e);
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
            valid &= validateCSFAmount(extractEntry.getCsfAmount());
            if (!valid) {
                LOG.warn("Invalid csf Amount for " + extractEntry.toString());
                continue;
            }

            if (valid) {
                for (PSPositionJobExtractAccountingInfo accountingInfo : extractEntry.getAccountingInfoCollection()) {

                    if (StringUtils.isNotBlank(accountingInfo.getCsfTimePercent())) {
                        valid &= validateTimePercent(accountingInfo.getCsfTimePercent());
                        if (!valid) {
                            LOG.warn("Invalid time percent " + accountingInfo.getCsfTimePercent() + " for extract "
                                    + extractEntry.toString());
                            break;
                        }
                        valid &= validateAccount(accountingInfo.getChartOfAccountsCode(),
                                accountingInfo.getAccountNumber());
                        if (!valid) {
                            LOG.warn("Invalid Account: " + accountingInfo.getChartOfAccountsCode() + ","
                                    + accountingInfo.getAccountNumber() + " for extract " + extractEntry.toString());
                            break;
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
                                break;
                            }
                        }

                        valid &= validateLaborObject(universityFiscalYear, accountingInfo.getChartOfAccountsCode(),
                                accountingInfo.getFinancialObjectCode());
                        if (!valid) {
                            LOG.warn("Invalid Labor Object: " + universityFiscalYear + ","
                                    + accountingInfo.getChartOfAccountsCode() + ","
                                    + accountingInfo.getFinancialObjectCode() + " for extract "
                                    + extractEntry.toString());
                            break;
                        }
                        valid &= validateObjectCode(universityFiscalYear, accountingInfo.getChartOfAccountsCode(),
                                accountingInfo.getFinancialObjectCode());
                        if (!valid) {
                            LOG.warn("Invalid Object Code: " + universityFiscalYear + ","
                                    + accountingInfo.getChartOfAccountsCode() + ","
                                    + accountingInfo.getFinancialObjectCode() + " for extract "
                                    + extractEntry.toString());
                            break;
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
                                break;
                            }
                        }
                    }

                }
            }

            if (valid) {
                validEntries.add(extractEntry);
            }

        }

        return validEntries;

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
            generateCSFAmount(csfAmount);
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
            generateCsfTimePercent(timePercent);
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
        Timestamp csfCreateTimestamp = dateTimeService.getCurrentTimestamp();

        BigDecimal csfFullTimeEmploymentQuantity = psPositionJobExtractEntry.getCsfFullTimeEmploymentQuantity()
                .bigDecimalValue();
        KualiDecimal csfAmount = generateCSFAmount(psPositionJobExtractEntry.getCsfAmount());

        Map<String, CalculatedSalaryFoundationTracker> mapOfEntries = new HashMap<String, CalculatedSalaryFoundationTracker>();

        // accounting data
        for (PSPositionJobExtractAccountingInfo accountingInfo : psPositionJobExtractEntry
                .getAccountingInfoCollection()) {
            BigDecimal csfTimePercent = generateCsfTimePercent(accountingInfo.getCsfTimePercent()).bigDecimalValue();
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

            CalculatedSalaryFoundationTracker entry = generateCalculatedSalaryFoundationTracker(positionNumber,
                    universityFiscalYear, emplid, name, csfCreateTimestamp, csfFullTimeEmploymentQuantity, csfAmount,
                    csfTimePercent, chartOfAccountsCode, accountNumber, subAccountNumber, financialObjectCode,
                    financialSubObjectCode, csfDeleteCode, csfFundingStatusCode);

            // if in the map add percentages and add only one account
            CalculatedSalaryFoundationTracker csfEntryFromMap = mapOfEntries.get(accountingInfo.getKey());
            if (csfEntryFromMap != null) {
                entry.setCsfTimePercent(csfTimePercent.add(csfEntryFromMap.getCsfTimePercent()));
            }
            mapOfEntries.put(accountingInfo.getKey(), entry);

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
        entry.setName(name);

        entry.setCsfAmount(csfAmount);
        entry.setCsfCreateTimestamp(csfCreateTimestamp);
        entry.setCsfDeleteCode(csfDeleteCode);
        entry.setCsfTimePercent(csfTimePercent);
        entry.setUniversityFiscalYear(universityFiscalYear);
        entry.setChartOfAccountsCode(chartOfAccountsCode);
        entry.setAccountNumber(accountNumber);
        entry.setSubAccountNumber(subAccountNumber);
        entry.setFinancialObjectCode(financialObjectCode);
        entry.setFinancialSubObjectCode(financialSubObjectCode);
        entry.setCsfFullTimeEmploymentQuantity(csfFullTimeEmploymentQuantity);
        entry.setCsfFundingStatusCode(csfFundingStatusCode);

        return entry;

    }

    /**
     * Generates the csf time percent from the value in the input file. The value comes
     * like this: Acct Distribution % (the first 5 digits in all 20 “account” fields)
     * 00000 and we will add the decimal point and create a KualiDecimal value 000.00.
     * 
     * @param csfTimePrecent
     * @return a KualiDecimal value for the input csfTimePrecent
     */
    protected KualiDecimal generateCsfTimePercent(String csfTimePrecent) {
        //prepare time percent
        String timePercent = csfTimePrecent;
        timePercent = timePercent.substring(0, timePercent.length() - 2) + "."
                + timePercent.substring(timePercent.length() - 2, timePercent.length());

        return new KualiDecimal(timePercent);
    }

    /**
     * Generate the CSFAmount from the value in the input file. The comes in like this
     * Annual Rt (pos 664) 000000000000 and we will add the decimal point 0000000000.00
     * and create a KualiDecimal value.
     * 
     * @param csfAmount
     * @return a KualiDecimal value for the input csfAmount
     */
    protected KualiDecimal generateCSFAmount(String csfAmount) {
        //prepare annual rate to create a KualiDecimal
        csfAmount = csfAmount.substring(0, csfAmount.length() - 2) + "."
                + csfAmount.substring(csfAmount.length() - 2, csfAmount.length());
        return new KualiDecimal(csfAmount);
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
     * Loads the entries in the LS_CSF_TRACKER_T table.
     * 
     * @param csfTackerEntries
     */
    protected void loadEntriesInCSFTrackerTable(List<CalculatedSalaryFoundationTracker> csfTackerEntries) {
        LOG.info("Start timestamp:" + System.currentTimeMillis());
        // wipe out everything first
        businessObjectService.deleteMatching(CalculatedSalaryFoundationTracker.class, new HashMap<String, String>());

        //load in the new entries
        businessObjectService.save(csfTackerEntries);
        LOG.info("End timestamp:" + System.currentTimeMillis());
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

}
