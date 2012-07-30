/*
 * Copyright 2011 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.gl.batch.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.gl.service.ScrubberReportData;
import org.kuali.kfs.module.ld.businessobject.BenefitsCalculation;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.Message;
import org.kuali.kfs.sys.batch.service.WrappingBatchService;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.kuali.kfs.sys.service.DocumentNumberAwareReportWriterService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.kns.util.KualiDecimal;

import com.rsmart.kuali.kfs.gl.Constant;

public class ScrubberProcessImpl extends org.kuali.kfs.gl.batch.service.impl.ScrubberProcessImpl {
    
    private List<Message> transactionErrors;
    private String thisBatchFileDirectoryName;
    private UniversityDate universityRunDate;
    private Date runDate;
    private ScrubberReportData scrubberReport;
    private KualiDecimal scrubCostShareAmount;
    
    private String inputOutputFile;
    PrintStream OUTPUT_GLE_FILE_ps;
    
    private boolean createOffsetEntries = false;
    
    private DocumentNumberAwareReportWriterService ldOffsetReportWriterService;

    private static final String TRANSACTION_TYPE_OFFSET = "O";
    
    /**
     * Scrubs all entries in all groups and documents.
     */
    public void scrubEntries() {
        super.scrubEntries();
        
        createLdOffsetEntriesEntries();
        
    }
    
    /**
     * Processes all entries in all groups and documents to create the LD offset entries.
     */
    protected void createLdOffsetEntriesEntries() {
        //Only calculate the GL LD offset entry if it is enabled
        if(getParameterService().parameterExists(KfsParameterConstants.GENERAL_LEDGER_ALL.class, Constant.LD_GL_OFFSET_ENABLED)) {
            String ldGlOffsetIndicator = getParameterService().getParameterValue(KfsParameterConstants.GENERAL_LEDGER_ALL.class, Constant.LD_GL_OFFSET_ENABLED);
            
            if (StringUtils.isNotEmpty(ldGlOffsetIndicator)  && ldGlOffsetIndicator.trim().equalsIgnoreCase("y")) {
                ((WrappingBatchService) ldOffsetReportWriterService).initialize();
                
                createOffsetEntries = true;
                
                this.inputOutputFile = thisBatchFileDirectoryName + File.separator + GeneralLedgerConstants.BatchFileSystem.SCRUBBER_VALID_OUTPUT_FILE + GeneralLedgerConstants.BatchFileSystem.EXTENSION;
                
                // setup an object to hold the "default" date information
                runDate = calculateRunDate(getDateTimeService().getCurrentDate());
        
                universityRunDate = getAccountingCycleCachingService().getUniversityDate(runDate);
                if (universityRunDate == null) {
                    throw new IllegalStateException(getConfigurationService().getPropertyString(KFSKeyConstants.ERROR_UNIV_DATE_NOT_FOUND));
                }
        
                scrubberReport = new ScrubberReportData();
                
                processGroup(scrubberReport);

                ((WrappingBatchService) ldOffsetReportWriterService).destroy();
            }
        }
    }
    
    /**
     * This will process a group of origin entries. To create the LD Offset Entries.
     * 
     * @param ScrubberReportData Data for process
     */
    protected void processGroup(ScrubberReportData scrubberReport) {
        if(createOffsetEntries) {
            scrubCostShareAmount = KualiDecimal.ZERO;
            FileReader INPUT_VALID_GLE_FILE = null;
            String GLEN_RECORD;
            BufferedReader INPUT_VALID_GLE_FILE_br;
            List<String> outputValues = new ArrayList<String>();
            Map<String, String> laborBenefitObjectCodes = new HashMap<String, String>(); 
            try {
                INPUT_VALID_GLE_FILE = new FileReader(inputOutputFile);
            }
            catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            
            Map<String, List<OriginEntryFull>> offsets = new HashMap<String, List<OriginEntryFull>>();
    
            INPUT_VALID_GLE_FILE_br = new BufferedReader(INPUT_VALID_GLE_FILE);
            
            String objectCodes = null, key = null;
            int line = 0;
            try {
                Map<String, Object> fieldValues = new HashMap<String, Object>();
                
                //Loop through all the scrubbed entries to calculate the GL LD offset entries
                while ((GLEN_RECORD = INPUT_VALID_GLE_FILE_br.readLine()) != null) {
                    if (!org.apache.commons.lang.StringUtils.isEmpty(GLEN_RECORD) && !org.apache.commons.lang.StringUtils.isBlank(GLEN_RECORD.trim())) {
                        //Add all scrubbed entries to the file
                        outputValues.add(GLEN_RECORD);
                        line++;
                        OriginEntryFull scrubbedEntry = new OriginEntryFull();
                        scrubbedEntry.setFromTextFileForBatch(GLEN_RECORD, line);
                        
                        //Get a list of all the Labor Benefit Calculation Object codes based on year and chart of accounts code
                        objectCodes = null;
                        key = scrubbedEntry.getUniversityFiscalYear() + "-" + scrubbedEntry.getChartOfAccountsCode();
                        if (!laborBenefitObjectCodes.containsKey(key)) {
                            fieldValues = new HashMap<String, Object>();
                            fieldValues.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, scrubbedEntry.getUniversityFiscalYear());
                            fieldValues.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, scrubbedEntry.getChartOfAccountsCode());
                            
                            List<BenefitsCalculation> calculations = (List<BenefitsCalculation>) this.getBusinessObjectService().findMatching(BenefitsCalculation.class, fieldValues);
                            objectCodes = ";";
                            for(BenefitsCalculation calculation : calculations) {
                                if(!objectCodes.contains(";" + calculation.getPositionFringeBenefitObjectCode() + ";")) {
                                    objectCodes = objectCodes + calculation.getPositionFringeBenefitObjectCode() + ";";
                                }
                            }
                            laborBenefitObjectCodes.put(key, objectCodes);
                        }
                        objectCodes = laborBenefitObjectCodes.get(key);
                        
                        //Only add the scrubbed entry to the list for the GL LD offset if it is not an offset entry or a benefits calculation entry
                        if(scrubbedEntry.getFinancialDocumentTypeCode().trim().equalsIgnoreCase("PAYR") && StringUtils.isNotEmpty(scrubbedEntry.getTransactionLedgerEntryDescription()) && 
                                !getTransactionType(scrubbedEntry).equalsIgnoreCase(TRANSACTION_TYPE_OFFSET) && !scrubbedEntry.getTransactionLedgerEntryDescription().contains("GENERATED BENEFIT OFFSET") &&
                                !objectCodes.contains(";" + scrubbedEntry.getFinancialObjectCode() + ";")) {
                            List<OriginEntryFull> entries = new ArrayList<OriginEntryFull>();
                            key = scrubbedEntry.getChartOfAccountsCode() + "-" + scrubbedEntry.getUniversityFiscalYear() + "-" + scrubbedEntry.getUniversityFiscalPeriodCode();
                            
                            //Add it to a list of entries to be processed for the GL LD offset entry
                            if(!offsets.containsKey(key)) {
                                entries = new ArrayList<OriginEntryFull>();
                                offsets.put(key, entries);
                            } else {
                                entries = offsets.get(key);
                            }
                            entries.add(scrubbedEntry);
                        }
                    }
                }
                
                String errorMsg = null;
                Map<String, String> possibleChartOfAccounts = new HashMap<String, String>();
                Map<String, String> possibleChartOfAccountsCOC = new HashMap<String, String>();
                
                //Get a list of the chart of accounts, account numbers, and object codes for the GL LD offset entry from the LD_GL_OFFSET_CLEARING_ACCOUNT_OBJECT_CODE system parameter
                if (getParameterService().parameterExists(KfsParameterConstants.GENERAL_LEDGER_ALL.class, Constant.LD_GL_OFFSET_CLEARING_ACCOUNT_OBJECT_CODE)) {
                    List<String> parmValues = getParameterService().getParameterValues(KfsParameterConstants.GENERAL_LEDGER_ALL.class, Constant.LD_GL_OFFSET_CLEARING_ACCOUNT_OBJECT_CODE);
                    for(String parmValue : parmValues) {
                        //Split the values to get the three pieces separated.
                        String [] values = parmValue.split(",");
                        if(values.length == 3) {
                            String chartOfAccounts = values[0], accountNumber = values[1], objectCode = values[2];
                            errorMsg = null;
                            
                            //Check if the chart of accounts code is defined twice in the system parameter
                            if(!possibleChartOfAccounts.containsKey(chartOfAccounts)) {
                                //Verify the chart of accounts is a valid chart of accounts code in KFS
                                Chart chart = getAccountingCycleCachingService().getChart(chartOfAccounts);
                                if (chart == null) {
                                    if(errorMsg == null) {
                                        errorMsg = "";
                                    }
                                    errorMsg += "The chart of accounts code: " + chartOfAccounts + " does not exist.  ";
                                }
                                
                                //Verify the account number is a valid account number in KFS
                                Account account = getAccountingCycleCachingService().getAccount(chartOfAccounts, accountNumber);
                                if (account == null) {
                                    if(errorMsg == null) {
                                        errorMsg = "";
                                    }
                                    errorMsg += "The account: " + accountNumber + " does not exist for chart of accounts code: " + chartOfAccounts + ".  ";
                                }
                                
                                //Verify the object code is a valid object code in KFS
                                for (String loopKey : offsets.keySet()) {
                                    if (loopKey.contains(chartOfAccounts)) {
                                        List<OriginEntryFull> entryList = offsets.get(chartOfAccounts);
                                        if (entryList != null && entryList.size() > 0) {
                                            OriginEntryFull offsetEntry = OriginEntryFull.copyFromOriginEntryable(entryList.get(0));

                                            ObjectCode object = getAccountingCycleCachingService().getObjectCode(offsetEntry.getUniversityFiscalYear(), chartOfAccounts, objectCode);
                                            if (object == null) {
                                                if (errorMsg == null) {
                                                    errorMsg = "";
                                                }
                                                errorMsg += "The financial object code: " + objectCode + " does not exist for chart of accounts code: " + chartOfAccounts + " in university fiscal year: " + offsetEntry.getUniversityFiscalYear() + ".  ";
                                            }
                                        }
                                    }
                                }
                                
                                //Write error message if error found otherwise add it to the list of possibilities
                                if (errorMsg != null) {
                                    ldOffsetReportWriterService.writeFormattedMessageLine(errorMsg);
                                } else {
                                    possibleChartOfAccounts.put(chartOfAccounts, accountNumber + "," + objectCode);
                                }
                            } else {
                                ldOffsetReportWriterService.writeFormattedMessageLine("WARNING: The chart of accounts code: " + chartOfAccounts + " exists in the LD_GL_OFFSET_CLEARING_ACCOUNT_OBJECT_CODE system parameter multiple times.\n    The system will use the first instance of the " + chartOfAccounts + " chart of accounts code.");
                            }
                        }
                    }
                }
                
                //Get a list of the chart of accounts and object codes for the GL LD offset claim on cash entry from the LD_GL_OFFSET_CLAIM_ON_CASH_ACCOUNT_OBJECT_CODE system parameter
                if (getParameterService().parameterExists(KfsParameterConstants.GENERAL_LEDGER_ALL.class, Constant.LD_GL_OFFSET_CLAIM_ON_CASH_ACCOUNT_OBJECT_CODE)) {
                    List<String> parmValues = getParameterService().getParameterValues(KfsParameterConstants.GENERAL_LEDGER_ALL.class, Constant.LD_GL_OFFSET_CLAIM_ON_CASH_ACCOUNT_OBJECT_CODE);
                    for(String parmValue : parmValues) {
                        //Split the values to get the two pieces separated.
                        String [] values = parmValue.split(",");
                        if(values.length == 2) {
                            String chartOfAccounts = values[0], objectCode = values[1];
                            errorMsg = null;
                            
                            //Check if the chart of accounts code is defined twice in the system parameter
                            if(!possibleChartOfAccountsCOC.containsKey(chartOfAccounts)) {
                                //Verify the chart of accounts is a valid chart of accounts code in KFS
                                Chart chart = getAccountingCycleCachingService().getChart(chartOfAccounts);
                                if (chart == null) {
                                    if(errorMsg == null) {
                                        errorMsg = "";
                                    }
                                    errorMsg += "The chart of accounts code: " + chartOfAccounts + " does not exist.  ";
                                }
                                
                                //Verify the object code is a valid object code in KFS
                                for (String loopKey : offsets.keySet()) {
                                    if (loopKey.contains(chartOfAccounts)) {
                                        List<OriginEntryFull> entryList = offsets.get(chartOfAccounts);
                                        if (entryList != null && entryList.size() > 0) {
                                            OriginEntryFull offsetEntry = OriginEntryFull.copyFromOriginEntryable(entryList.get(0));

                                            ObjectCode object = getAccountingCycleCachingService().getObjectCode(offsetEntry.getUniversityFiscalYear(), chartOfAccounts, objectCode);
                                            if (object == null) {
                                                if (errorMsg == null) {
                                                    errorMsg = "";
                                                }
                                                errorMsg += "The financial object code: " + objectCode + " does not exist for chart of accounts code: " + chartOfAccounts + " in university fiscal year: " + offsetEntry.getUniversityFiscalYear() + ".  ";
                                            }
                                        }
                                    }
                                }
                                
                                //Write error message if error found otherwise add it to the list of possibilities
                                if (errorMsg != null) {
                                    ldOffsetReportWriterService.writeFormattedMessageLine(errorMsg);
                                } else {
                                    possibleChartOfAccountsCOC.put(chartOfAccounts, objectCode);
                                }
                            } else {
                                ldOffsetReportWriterService.writeFormattedMessageLine("WARNING: The chart of accounts code: " + chartOfAccounts + " exists in the LD_GL_OFFSET_CLAIM_ON_CASH_ACCOUNT_OBJECT_CODE system parameter multiple times.\n    The system will use the first instance of the " + chartOfAccounts + " chart of accounts code.");
                            }
                        }
                    }
                }
                
                //Loop through all the entries to calculate the GL LD offset entries
                for(Entry<String, List<OriginEntryFull>> entrySet : offsets.entrySet()) {
                    List<OriginEntryFull> entryList = entrySet.getValue();
                    if(entryList != null && entryList.size() > 0) {
                        OriginEntryFull offsetEntry = new OriginEntryFull();
                        KualiDecimal total = new KualiDecimal(0);
                        errorMsg = null;
                        
                        String keyChartOfAccounts = entrySet.getKey().split("-")[0];

                        //Check the values from the LD_GL_OFFSET_CLEARING_ACCOUNT_OBJECT_CODE system parameter to see if this chart of accounts code is defined in it
                        if(possibleChartOfAccounts.containsKey(keyChartOfAccounts)) {
                            String [] values = possibleChartOfAccounts.get(keyChartOfAccounts).split(",");
                            String chartOfAccounts = keyChartOfAccounts, accountNumber = values[0], objectCode = values[1];
                            offsetEntry = OriginEntryFull.copyFromOriginEntryable(entryList.get(0));
                            
                            //Loop through all the entries to calculate the total for the GL LD offset entry
                            for(OriginEntryFull entry : entryList) {
                                if(entry.getTransactionDebitCreditCode().equalsIgnoreCase("D")) {
                                    total = total.add(entry.getTransactionLedgerEntryAmount());
                                } else {
                                    total = total.subtract(entry.getTransactionLedgerEntryAmount());
                                }
                            }
                            
                            //Set the total and the debit/credit code
                            offsetEntry.setTransactionLedgerEntryAmount(total.abs());
                            offsetEntry.setTransactionDebitCreditCode(total.isLessThan(KualiDecimal.ZERO) ? KFSConstants.GL_DEBIT_CODE : KFSConstants.GL_CREDIT_CODE);
                            
                            //Set the values from the system parameter for this chart of accounts
                            offsetEntry.setChartOfAccountsCode(chartOfAccounts);
                            offsetEntry.setAccountNumber(accountNumber);
                            offsetEntry.setFinancialObjectCode(objectCode);
                            offsetEntry.setTransactionLedgerEntryDescription("GENERATED LD OFFSET");
                            
                            //KUALISUPPORT-72 Setting the object type code for the offset entry to the object type code from the object code object
                            ObjectCode object = getAccountingCycleCachingService().getObjectCode(offsetEntry.getUniversityFiscalYear(), chartOfAccounts, objectCode);
                            offsetEntry.setFinancialObjectTypeCode(object.getFinancialObjectTypeCode());
                            
                            //Add entry to the file
                            outputValues.add(offsetEntry.getLine());
                            scrubberReport.incrementOffsetEntryGenerated();
                            
                            //Find the chart of accounts code for the claim on cash entry from the LD_GL_OFFSET_CLAIM_ON_CASH_ACCOUNT_OBJECT_CODE system parameter
                            if(possibleChartOfAccountsCOC.containsKey(keyChartOfAccounts)) {
                                //Copy the GL LD offset entry since only a few fields are different for the claim on cash entry
                                OriginEntryFull cocEntry = offsetEntry;
                                
                                values = possibleChartOfAccountsCOC.get(keyChartOfAccounts).split(",");
                                chartOfAccounts = keyChartOfAccounts;
                                objectCode = values[0];
                                
                                //Change the credit/debit code to the opposite of the GL LD offset entry
                                cocEntry.setTransactionDebitCreditCode(offsetEntry.getTransactionDebitCreditCode().equalsIgnoreCase(KFSConstants.GL_CREDIT_CODE) ? KFSConstants.GL_DEBIT_CODE : KFSConstants.GL_CREDIT_CODE);
                                
                                //Set the values from the system parameter based on the chart of accounts code.
                                cocEntry.setChartOfAccountsCode(chartOfAccounts);
                                cocEntry.setFinancialObjectCode(objectCode);
                                cocEntry.setTransactionLedgerEntryDescription("GENERATED CLAIM ON CASH");
                                
                                //KUALISUPPORT-72 Setting the object type code for the offset entry to the object type code from the object code object
                                object = getAccountingCycleCachingService().getObjectCode(offsetEntry.getUniversityFiscalYear(), chartOfAccounts, objectCode);
                                offsetEntry.setFinancialObjectTypeCode(object.getFinancialObjectTypeCode());
                                
                                //Add entry to the file
                                outputValues.add(cocEntry.getLine());
                            } else {
                                //This chart of accounts code was not defined in the system parameter record the error
                                ldOffsetReportWriterService.writeFormattedMessageLine("Unable to generate the claim on cash entry, the chart of accounts code: " + keyChartOfAccounts + " does not exist in the LD_GL_OFFSET_CLAIM_ON_CASH_ACCOUNT_OBJECT_CODE system parameter.");
                            }
                        } else {
                            //This chart of accounts code was not defined in the system parameter record error
                            errorMsg = "The chart of accounts code: " + keyChartOfAccounts + " was not found in the LD_GL_OFFSET_CLEARING_ACCOUNT_OBJECT_CODE system parameter or it contained an invalid object code.  ";
                        }
                        
                        //If an error occurred write it to the error log
                        if (errorMsg != null) {
                            errorMsg += "Unable to generate Labor Distribution GL Offset Entry for the following entries:";

                            ldOffsetReportWriterService.writeFormattedMessageLine(errorMsg);
                            //Write all the entries that were affected by this error to the log so the GL LD offset entry can be calculated by hand
                            for(OriginEntryFull entry : entryList) {
                                ldOffsetReportWriterService.writeFormattedMessageLine(entry.getLine());
                            }
                        }
                    }
                }

                INPUT_VALID_GLE_FILE_br.close();
                INPUT_VALID_GLE_FILE.close();
                
                try {
                    OUTPUT_GLE_FILE_ps = new PrintStream(inputOutputFile);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
                
                for(String output : outputValues) {
                    createOutputEntry(output, OUTPUT_GLE_FILE_ps);
                }
                
                OUTPUT_GLE_FILE_ps.close();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Gets the thisBatchFileDirectoryName attribute. 
     * @return Returns the thisBatchFileDirectoryName.
     */
    public String getThisBatchFileDirectoryName() {
        return thisBatchFileDirectoryName;
    }

    /**
     * Sets the thisBatchFileDirectoryName attribute value.
     * @param thisBatchFileDirectoryName The thisBatchFileDirectoryName to set.
     */
    public void setThisBatchFileDirectoryName(String thisBatchFileDirectoryName) {
        this.thisBatchFileDirectoryName = thisBatchFileDirectoryName;
    }

    /**
     * Gets the ldOffsetReportWriterService attribute. 
     * @return Returns the ldOffsetReportWriterService.
     */
    public DocumentNumberAwareReportWriterService getLdOffsetReportWriterService() {
        return ldOffsetReportWriterService;
    }

    /**
     * Sets the ldOffsetReportWriterService attribute value.
     * @param ldOffsetReportWriterService The ldOffsetReportWriterService to set.
     */
    public void setLdOffsetReportWriterService(DocumentNumberAwareReportWriterService ldOffsetReportWriterService) {
        this.ldOffsetReportWriterService = ldOffsetReportWriterService;
    }
    
}











