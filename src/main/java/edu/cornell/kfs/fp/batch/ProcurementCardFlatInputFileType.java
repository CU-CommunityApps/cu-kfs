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
package edu.cornell.kfs.fp.batch;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransaction;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.batch.BatchInputFileTypeBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.CUKFSConstants;
import org.kuali.kfs.fp.FPKeyConstants;
import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.fp.batch.service.ProcurementCardErrorEmailService;
import edu.cornell.kfs.fp.batch.service.ProcurementCardSkippedTransactionEmailService;
import edu.cornell.kfs.fp.businessobject.ProcurementCardTransactionExtendedAttribute;
import edu.cornell.kfs.fp.businessobject.USBankRecordFieldUtils;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class ProcurementCardFlatInputFileType extends BatchInputFileTypeBase {
    
	private static final Logger LOG = LogManager.getLogger(ProcurementCardFlatInputFileType.class);
    
    private static final String FP_PRCRMNT_CARD_TRN_MT_SEQ = "FP_PRCRMNT_CARD_TRN_MT_SEQ";

    private ProcurementCardTransaction parent;
    private boolean duplicateTransactions = false;
    private int transactionCount = 0;
    private int headerTransactionCount = 0;
    private int footerTransactionCount = 0;
    private int lineCount = 1;
    private KualiDecimal accumulatedDebits = new KualiDecimal(0);
    private KualiDecimal accumulatedCredits = new KualiDecimal(0);
    private KualiDecimal totalDebits = new KualiDecimal(0);
    private KualiDecimal totalCredits = new KualiDecimal(0);
    private KualiDecimal fileFooterCredits = new KualiDecimal(0);
    private KualiDecimal fileFooterDebits = new KualiDecimal(0);        
    
    private String defaultChart;
    private ArrayList<String> errorMessages;
    
    private DateTimeService dateTimeService;
    private ParameterService parameterService;
    private ProcurementCardErrorEmailService procurementCardErrorEmailService;
    protected ProcurementCardSkippedTransactionEmailService procurementCardSkippedTransactionEmailService;
    
    // USBank record ID's
    // If/when we create dedicated classes for representing these records, 
    // these lines can move there as we've done with the addendum records.
    public static final String FILE_HEADER_RECORD_ID = "01";
    public static final String CARDHOLDER_DATA_HEADER_RECORD_ID = "02";
    public static final String TRANSACTION_INFORMATION_RECORD_ID = "05";
    public static final String CARDHOLDER_TRAILER_RECORD_ID = "95";
    public static final String FILE_TRAILER_RECORD_ID = "98";

    /**
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#getFileTypeIdentifier()
     */
    public String getFileTypeIdentifier() {
     	return "procurementCardFlatInputFileType";
//    	return KFSConstants.PCDO_FILE_TYPE_INDENTIFIER;
    }

    /**
     * No additional information is added to procurement card batch files.
     * 
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#getFileName(org.kuali.kfs.kim.bo.Person, java.lang.Object,
     *      java.lang.String)
     */
    public String getFileName(String principalName, Object parsedFileContents, String userIdentifier) {
        String fileName = "pcdo_" + principalName;
        if (StringUtils.isNotBlank(userIdentifier)) {
            fileName += "_" + userIdentifier;
        }
        fileName += "_" + dateTimeService.toDateTimeStringForFilename(dateTimeService.getCurrentDate());

        // remove spaces in filename
        fileName = StringUtils.remove(fileName, " ");

        return fileName;
    }
    
    public String getAuthorPrincipalName(File file) {
        String[] fileNameParts = StringUtils.split(file.getName(), "_");
        if (fileNameParts.length >= 2) {
            return fileNameParts[1];
        }
        return null;
    }

    /**
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#validate(java.lang.Object)
     */
    public boolean validate(Object parsedFileContents) {
        return true;
    }

    /**
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#getTitleKey()
     */
    public String getTitleKey() {
        return FPKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_PCDO;
    }

    
    
    public ParameterService getParameterService() {
		return parameterService;
	}

	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}

	public ProcurementCardErrorEmailService getProcurementCardErrorEmailService() {
		return procurementCardErrorEmailService;
	}

	public void setProcurementCardErrorEmailService(
			ProcurementCardErrorEmailService procurementCardErrorEmailService) {
		this.procurementCardErrorEmailService = procurementCardErrorEmailService;
	}

	/**
     * Gets the dateTimeService attribute.
     */
    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    /**
     * Sets the dateTimeService attribute value.
     */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setProcurementCardSkippedTransactionEmailService(
            ProcurementCardSkippedTransactionEmailService procurementCardSkippedTransactionEmailService) {
        this.procurementCardSkippedTransactionEmailService = procurementCardSkippedTransactionEmailService;
    }

    private void initialize() {
         parent = null;
         duplicateTransactions = false;
         transactionCount = 0;
         headerTransactionCount = 0;
         footerTransactionCount = 0;
         lineCount = 1;
         accumulatedDebits = new KualiDecimal(0);
         accumulatedCredits = new KualiDecimal(0);
         totalDebits = new KualiDecimal(0);
         totalCredits = new KualiDecimal(0);
         fileFooterCredits = new KualiDecimal(0);
         fileFooterDebits = new KualiDecimal(0);
    }
    
    public Object parse(byte[] fileByteContent) throws ParseException {
        BufferedReader bufferedFileReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileByteContent)));
        String fileLine;
        defaultChart = parameterService.getParameterValueAsString(KfsParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class, CUKFSParameterKeyConstants.DEFAULT_CHART_CODE);
        errorMessages = new ArrayList<String>();
        
        ArrayList<ProcurementCardTransaction> transactions = new ArrayList<ProcurementCardTransaction>();
        List<ProcurementCardSkippedTransaction> skippedTransactions = new ArrayList<>();
        LOG.info("Beginning parse of file");
        try {                       
            initialize();
        	
            while ((fileLine = bufferedFileReader.readLine()) != null) {
                ProcurementCardTransactionResult transactionResult = generateProcurementCardTransaction(fileLine);

                if (transactionResult.hasTransaction()) {
                    ProcurementCardTransaction theTransaction = transactionResult.getTransaction();
                    if (ObjectUtils.isNotNull(theTransaction.getExtension())) {
                        lineCount = ((ProcurementCardTransactionExtendedAttribute) theTransaction.getExtension())
                                .addAddendumLines(bufferedFileReader, lineCount);
                    }
                    transactions.add(theTransaction);
                } else if (transactionResult.hasSkippedTransaction()) {
                    skippedTransactions.add(transactionResult.getSkippedTransaction());
                }
                lineCount++;
            }
            
            if (CollectionUtils.isNotEmpty(skippedTransactions)) {
                LOG.error("fileByteContent, there were " + skippedTransactions.size() + " transactions skipped.");
                procurementCardSkippedTransactionEmailService.sendSkippedTransactionEmail(skippedTransactions);
            }
            
        	if (totalDebits.compareTo(fileFooterDebits.abs()) != 0) {
        		StringBuffer sb = new StringBuffer();
        		sb.append("The sum of all debits does not match the value given in the file footer.");
        		sb.append(" Sum of debits generated during ingestion of PCard file: ");
        		sb.append(totalDebits);
        		sb.append(" Total of debits given in file footer: ");
        		sb.append(fileFooterDebits);
        		throw new Exception(sb.toString());
        	}
        	if (totalCredits.compareTo(fileFooterCredits.abs()) != 0) {
        		StringBuffer sb = new StringBuffer();
        		sb.append("The sum of all credits does not match the value given in the file footer.");
        		sb.append(" Sum of credits generated during ingestion of PCard file: ");
        		sb.append(totalCredits);
        		sb.append(" Total of credits given in file footer: ");
        		sb.append(fileFooterCredits);
        		throw new Exception(sb.toString());
        	}
            if ((headerTransactionCount != footerTransactionCount)
                    || (headerTransactionCount != transactionCount + skippedTransactions.size())
                    || (footerTransactionCount != transactionCount + skippedTransactions.size())) {
                StringBuffer sb = new StringBuffer();
                sb.append("There is a discrepancy between the number of transactions counted during the ingestion process.");
                sb.append(" Transactions in header: ").append(headerTransactionCount);
                sb.append(" Transactions in footer: ").append(footerTransactionCount);
                sb.append(" Transactions counted while parsing file: ").append(transactionCount);
                sb.append(" Transactions skipped while parsing file: ").append(skippedTransactions.size());
                throw new Exception(sb.toString());
            }
        } catch (IOException e) {
            LOG.error("Error encountered reading from file content", e);
            throw new ParseException("Error encountered reading from file content", e);
        } catch (Exception e) {
        	e.printStackTrace();
        	errorMessages.add(e.getMessage());
        	errorMessages.add("\r\n");
        	errorMessages.add("Parsing of file stopped on line " + lineCount);
        	procurementCardErrorEmailService.sendErrorEmail(errorMessages);
        	throw new ParseException(e.getMessage());
        }
        
        return transactions;
    }

    public void process(String fileName, Object parsedFileContents) {
        
    }

    /**
     * Parses a line into a ProcurementCardTransaction.
     * 
     * The data in the ProcurementCardTransaction comes from USBank record types 02 and 05. 
     * Other USBank record types (01, 95, 98) are either skipped or used for validation.
     * 
     * @param line The current line
     * @return A result object with a completed transaction, or an empty result object if there are additional lines in the transaction
     * @throws Exception
     */
    private ProcurementCardTransactionResult generateProcurementCardTransaction(String line) throws Exception {
	        
        String recordId = USBankRecordFieldUtils.extractNormalizedString(line, 0, 2);
        if (recordId == null) {
        	throw new Exception("Unable to determine record Id necessary in order to parse line " + lineCount);
        }
        if (recordId.equals(FILE_HEADER_RECORD_ID)){ //file header record
        	headerTransactionCount = Integer.parseInt(USBankRecordFieldUtils.extractNormalizedString(line, 67, 75));
        }
        if (recordId.equals(FILE_TRAILER_RECORD_ID)) { //file footer record
        		        	
        	footerTransactionCount = Integer.parseInt(USBankRecordFieldUtils.extractNormalizedString(line, 21, 29));
        	fileFooterCredits = fileFooterCredits.add( new KualiDecimal(USBankRecordFieldUtils.extractNormalizedString(line,44,59)));
        	fileFooterDebits = fileFooterDebits.add( new KualiDecimal(USBankRecordFieldUtils.extractNormalizedString(line,29,44)));
        	if (totalDebits.compareTo(fileFooterDebits.abs()) != 0) {
        		StringBuffer sb = new StringBuffer();
        		sb.append("Debits in file footer do not match the sum of debits in transaction.");
        		sb.append(USBankRecordFieldUtils.lineCountMessage(lineCount));
        		sb.append(" Debit value given in file footer: ");
        		sb.append(fileFooterDebits.abs());
        		sb.append(" Debit value generated by summing transactions: ");
        		sb.append(totalDebits);
        		throw new Exception( sb.toString() );
        	}
        	if (totalCredits.compareTo(fileFooterCredits.abs()) != 0) {
        		StringBuffer sb = new StringBuffer();
        		sb.append("Credits in file footer do not match the sum of credits in transaction.");
        		sb.append(USBankRecordFieldUtils.lineCountMessage(lineCount));
        		sb.append(" Credit value given in file footer: ");
        		sb.append(fileFooterCredits.abs());
        		sb.append(" Credit value generated by summing transactions: ");
        		sb.append(totalCredits);        		        		
        		throw new Exception(sb.toString());

        	}
        }
        if (recordId.equals(CARDHOLDER_DATA_HEADER_RECORD_ID)) { //cardholder header

        	parent = buildProcurementCardTransactionObject();

            parent.setTransactionCreditCardNumber(USBankRecordFieldUtils.extractNormalizedString(line, 2, 18, true, lineCount)); //req
            parent.setChartOfAccountsCode(defaultChart); //req
            parent.setTransactionCycleEndDate(USBankRecordFieldUtils.extractCycleDate(line, 294, 296, lineCount));
//            parent.setTransactionCycleStartDate(transactionCycleStartDate); //may not be able to have
            parent.setCardHolderName(USBankRecordFieldUtils.extractNormalizedString(line, 43, 68));
            parent.setCardHolderLine1Address(USBankRecordFieldUtils.extractNormalizedString(line, 68, 104));
            parent.setCardHolderLine2Address(USBankRecordFieldUtils.extractNormalizedString(line, 104,140));
            parent.setCardHolderCityName(USBankRecordFieldUtils.extractNormalizedString(line, 140, 165));
            parent.setCardHolderStateCode(USBankRecordFieldUtils.extractNormalizedString(line, 165, 167));
            // KITI-2203 : Removing last four characters from zip+4 so validation performs properly.
            parent.setCardHolderZipCode(USBankRecordFieldUtils.extractNormalizedString(line, 167, 172));
//            parent.setCardHolderZipCode(extractNormalizedString(line, 167, 176)); 
            parent.setCardHolderAlternateName(USBankRecordFieldUtils.extractNormalizedString(line, 191, 206));
            parent.setCardHolderWorkPhoneNumber(USBankRecordFieldUtils.extractNormalizedString(line, 206, 216));
//            parent.setCardLimit(extractDecimal(line, 352, 363));
            parent.setCardStatusCode(USBankRecordFieldUtils.extractNormalizedString(line, 267, 268));
            
            String companyCode = USBankRecordFieldUtils.extractNormalizedString(line, 252, 257);
            
            if(companyCode.equals("99998")) {
            	duplicateTransactions = true;
            } else {
            	duplicateTransactions = false;
            }
        }
        if (recordId.equals(TRANSACTION_INFORMATION_RECORD_ID) && shouldTransactionLineBySkipped(line)) {
            KualiDecimal transactionAmount = USBankRecordFieldUtils.extractDecimal(line, 79, 91, lineCount);
            ProcurementCardSkippedTransaction skippedTransaction = new ProcurementCardSkippedTransaction();
            skippedTransaction.setFileLineNumber(lineCount);
            skippedTransaction.setCardHolderName(parent.getCardHolderName());
            skippedTransaction.setTransactionAmount(transactionAmount);
            return new ProcurementCardTransactionResult(skippedTransaction);
        }
        if (recordId.equals(TRANSACTION_INFORMATION_RECORD_ID) && !duplicateTransactions) {	        		        	
        	
        	ProcurementCardTransaction child = buildProcurementCardTransactionObject();
        	
        	//Pull everything in from the preceding '05' record
            child.setTransactionCreditCardNumber(parent.getTransactionCreditCardNumber());
            child.setTransactionCycleEndDate(parent.getTransactionCycleEndDate());
            child.setCardHolderName(parent.getCardHolderName());
            child.setCardHolderLine1Address(parent.getCardHolderLine1Address());
            child.setCardHolderLine2Address(parent.getCardHolderLine2Address());
            child.setCardHolderCityName(parent.getCardHolderCityName());
            child.setCardHolderStateCode(parent.getCardHolderStateCode());
            child.setCardHolderZipCode(parent.getCardHolderZipCode());
            child.setCardHolderWorkPhoneNumber(parent.getCardHolderWorkPhoneNumber());
            child.setCardHolderAlternateName(parent.getCardHolderAlternateName());
//            child.setCardLimit(parent.getCardLimit());
            child.setCardStatusCode(parent.getCardStatusCode());
            parseAccountingInformation(line, child);
            child.setProjectCode(USBankRecordFieldUtils.extractNormalizedString(line, 336, 346));	        	
            child.setFinancialDocumentTotalAmount(USBankRecordFieldUtils.extractDecimal(line, 79, 91, lineCount));  //req
            child.setTransactionDebitCreditCode(USBankRecordFieldUtils.convertDebitCreditCode(line.substring(64,65))); //req
            child.setTransactionDate(USBankRecordFieldUtils.extractDate(line, 45, 53, lineCount)); // req	
            child.setTransactionOriginalCurrencyCode(USBankRecordFieldUtils.extractNormalizedString(line, 61, 64));
            child.setTransactionBillingCurrencyCode(USBankRecordFieldUtils.extractNormalizedString(line, 76, 79));
            child.setVendorName(USBankRecordFieldUtils.extractNormalizedString(line, 95, 120));
            child.setTransactionReferenceNumber(USBankRecordFieldUtils.extractNormalizedString(line, 18, 41));
            child.setTransactionMerchantCategoryCode(USBankRecordFieldUtils.extractNormalizedString(line, 91, 95));
            child.setTransactionPostingDate(USBankRecordFieldUtils.extractDate(line, 53, 61, lineCount));
            child.setTransactionOriginalCurrencyAmount(USBankRecordFieldUtils.extractDecimalWithCents(line, 64, 76, lineCount));
            child.setTransactionCurrencyExchangeRate(USBankRecordFieldUtils.extractDecimal(line, 304, 317, lineCount).bigDecimalValue());
            child.setTransactionSettlementAmount(USBankRecordFieldUtils.extractDecimal(line, 79, 91, lineCount));

//            child.setTransactionTaxExemptIndicator(USBankAddendumRecordFieldUtils.extractNormalizedString(line, 234, 235));
            child.setTransactionPurchaseIdentifierIndicator(USBankRecordFieldUtils.extractNormalizedString(line, 272, 273));
            
            child.setTransactionPurchaseIdentifierDescription(USBankRecordFieldUtils.extractNormalizedString(line, 273, 298));
            child.setVendorCityName(USBankRecordFieldUtils.extractNormalizedString(line, 120, 146));
            child.setVendorStateCode(USBankRecordFieldUtils.extractNormalizedString(line, 146, 148));
//            child.setVendorZipCode(USBankAddendumRecordFieldUtils.extractNormalizedString(line, 152, 161));

            //
            //   Fix to handle zip codes provided by US Bank
            //
            String vendorZipCode = USBankRecordFieldUtils.extractNormalizedString(line,152,161);
            if (vendorZipCode.startsWith("0000")) {
            	vendorZipCode = USBankRecordFieldUtils.extractNormalizedString(line,156,161);
            }
            child.setVendorZipCode(vendorZipCode);

            child.setVisaVendorIdentifier(USBankRecordFieldUtils.extractNormalizedString(line, 176, 192));
            
            if (child.getTransactionDebitCreditCode().equals("D")) {
            	accumulatedDebits = accumulatedDebits.add(child.getTransactionSettlementAmount());
            } else {
            	accumulatedCredits = accumulatedCredits.add(child.getTransactionSettlementAmount());	            	
            }
            
            ProcurementCardTransactionExtendedAttribute extension = buildProcurementCardTransactionExtendedAttributeObject();
            extension.setTransactionType(USBankRecordFieldUtils.extractNormalizedString(line, 346, 348));
            if (child.getTransactionSequenceRowNumber() == null) {
                Integer generatedTransactionSequenceRowNumber = SpringContext.getBean(SequenceAccessorService.class).getNextAvailableSequenceNumber(FP_PRCRMNT_CARD_TRN_MT_SEQ).intValue();
                child.setTransactionSequenceRowNumber(generatedTransactionSequenceRowNumber);
                extension.setTransactionSequenceRowNumber(child.getTransactionSequenceRowNumber());
            }

            child.setExtension(extension);
            
            transactionCount++;
            return new ProcurementCardTransactionResult(child);

        } 
        // Still need to update totals and transaction count so validation works properly, but don't want transactions to be loaded
        if (recordId.equals(TRANSACTION_INFORMATION_RECORD_ID) && duplicateTransactions){	        		        	
            if (USBankRecordFieldUtils.convertDebitCreditCode(line.substring(64,65)).equals("D")) {
            	accumulatedDebits = accumulatedDebits.add(USBankRecordFieldUtils.extractDecimal(line, 79, 91, lineCount));
            } else {
            	accumulatedCredits = accumulatedCredits.add(USBankRecordFieldUtils.extractDecimal(line, 79, 91, lineCount));	            	
            }
            transactionCount++;
        }
        if (recordId.equals(CARDHOLDER_TRAILER_RECORD_ID)) { //cardholder footer
        	KualiDecimal recordDebits = new KualiDecimal(USBankRecordFieldUtils.extractNormalizedString(line, 24, 36));
        	KualiDecimal recordCredits = new KualiDecimal(USBankRecordFieldUtils.extractNormalizedString(line, 36, 48));
        	
        	if (accumulatedCredits.compareTo(recordCredits.abs()) !=0) {
        		StringBuffer sb = new StringBuffer();
        		sb.append("Total credits given in cardholder footer do not sum to same value as transactions.");
        		sb.append(USBankRecordFieldUtils.lineCountMessage(lineCount));
        		sb.append(" Credit value given in cardholder footer: ");
        		sb.append(recordCredits.abs());
        		sb.append(" Credit value generated by summing transactions: ");
        		sb.append(accumulatedCredits);
        		throw new Exception(sb.toString());
        	}
        	if (accumulatedDebits.compareTo(recordDebits.abs()) != 0) {
        		StringBuffer sb = new StringBuffer();
        		sb.append("Total debits given in cardholder footer do not sum to same value as transactions."); 
        		sb.append(USBankRecordFieldUtils.lineCountMessage(lineCount));
        		sb.append(" Debit value given in cardholder footer: "); 
        		sb.append(recordDebits.abs());
        		sb.append(" Debit value generated by summing transactions: ");
        		sb.append(accumulatedDebits);
        		throw new Exception(sb.toString());
        	}
        	
        	totalCredits = totalCredits.add(accumulatedCredits);
        	totalDebits = totalDebits.add(accumulatedDebits);
        	accumulatedCredits = new KualiDecimal(0);
        	accumulatedDebits = new KualiDecimal(0);
        	
        }

        return ProcurementCardTransactionResult.EMPTY;
    }

    protected ProcurementCardTransactionExtendedAttribute buildProcurementCardTransactionExtendedAttributeObject() {
        return new ProcurementCardTransactionExtendedAttribute();
    }

    protected ProcurementCardTransaction buildProcurementCardTransactionObject() {
        return new ProcurementCardTransaction();
    }

    protected boolean shouldTransactionLineBySkipped(String line) {
        String transactionCode = USBankRecordFieldUtils.extractNormalizedString(line, 41, 45);
        Collection<String> transactionTypesToSkip = parameterService.getParameterValuesAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, 
                CUKFSConstants.ProcurementCardParameters.PCARD_BATCH_LOAD_STEP, CuFPParameterConstants.ProcurementCardDocument.CARD_TRANSACTION_TYPES_TO_SKIP);
        boolean skipTransaction = false;
        
        if (CollectionUtils.isNotEmpty(transactionTypesToSkip)) {
            if (transactionTypesToSkip.contains(transactionCode)) {
                skipTransaction = true;
                LOG.info("shouldTransactionLineBySkipped, the transaction type " + transactionCode + " is set to be skipped");
            } else {
                LOG.debug("shouldTransactionLineBySkipped, the transaction type " + transactionCode + " is NOT set to be skipped");
            }
        }
        return skipTransaction;
    }

    protected void parseAccountingInformation(String line, ProcurementCardTransaction child)
            throws java.text.ParseException {
        child.setChartOfAccountsCode(defaultChart);
        child.setAccountNumber(USBankRecordFieldUtils.extractNormalizedString(line, 317, 324, false, lineCount));
        child.setSubAccountNumber(USBankRecordFieldUtils.extractNormalizedString(line, 324, 329));
        // KITI-2583 : Object code is not a required field and will be replaced by an error code if it is not present.
        child.setFinancialObjectCode(USBankRecordFieldUtils.extractNormalizedString(line, 329, 333, false, lineCount)); //req
        child.setFinancialSubObjectCode(USBankRecordFieldUtils.extractNormalizedString(line,333,336));
    }

}
