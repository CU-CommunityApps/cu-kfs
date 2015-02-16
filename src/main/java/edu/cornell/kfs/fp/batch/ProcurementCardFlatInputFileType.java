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
import java.sql.Date;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransaction;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.batch.BatchInputFileTypeBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.service.SequenceAccessorService;

import edu.cornell.kfs.fp.batch.service.ProcurementCardErrorEmailService;
import edu.cornell.kfs.fp.businessobject.ProcurementCardTransactionExtendedAttribute;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class ProcurementCardFlatInputFileType extends BatchInputFileTypeBase {
    
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ProcurementCardFlatInputFileType.class);
    
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

    /**
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#getFileTypeIdentifer()
     */
    public String getFileTypeIdentifer() {
     	return "procurementCardFlatInputFileType";
//    	return KFSConstants.PCDO_FILE_TYPE_INDENTIFIER;
    }

    /**
     * No additional information is added to procurement card batch files.
     * 
     * @see org.kuali.kfs.sys.batch.BatchInputFileType#getFileName(org.kuali.rice.kim.bo.Person, java.lang.Object,
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
        return KFSKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_PCDO;
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
        LOG.info("Beginning parse of file");
        try {                       
            initialize();
        	
            while ((fileLine=bufferedFileReader.readLine()) != null) {
            	ProcurementCardTransaction theTransaction = generateProcurementCardTransaction(fileLine);
            	if (theTransaction!=null){
            		transactions.add(theTransaction);
            	}
            	lineCount++;
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
        	if ( (headerTransactionCount!=footerTransactionCount) || 
        		 (headerTransactionCount!=transactionCount) ||
        		 (footerTransactionCount!=transactionCount) ){
        		StringBuffer sb = new StringBuffer();
        		sb.append("There is a discrepancy between the number of transactions counted during the ingestion process.");
        		sb.append(" Transactions in header: ");
        		sb.append(headerTransactionCount);
        		sb.append(" Transactions in footer: ");
        		sb.append(footerTransactionCount);
        		sb.append(" Transactions counted while parsing file: ");
        		sb.append(transactionCount);
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

    private ProcurementCardTransaction generateProcurementCardTransaction(String line) throws Exception {
	        
        String recordId = extractNormalizedString(line, 0, 2);
        if (recordId == null) {
        	throw new Exception("Unable to determine record Id necessary in order to parse line " + lineCount);
        }
        if (recordId.equals("01")){ //file header record
        	headerTransactionCount = Integer.parseInt(extractNormalizedString(line, 67, 75));
        }
        if (recordId.equals("98")) { //file footer record
        		        	
        	footerTransactionCount = Integer.parseInt(extractNormalizedString(line, 21, 29));
        	fileFooterCredits = fileFooterCredits.add( new KualiDecimal(extractNormalizedString(line,44,59)));
        	fileFooterDebits = fileFooterDebits.add( new KualiDecimal(extractNormalizedString(line,29,44)));
        	if (totalDebits.compareTo(fileFooterDebits.abs()) != 0) {
        		StringBuffer sb = new StringBuffer();
        		sb.append("Debits in file footer do not match the sum of debits in transaction.");
        		sb.append(lineCountMessage());
        		sb.append(" Debit value given in file footer: ");
        		sb.append(fileFooterDebits.abs());
        		sb.append(" Debit value generated by summing transactions: ");
        		sb.append(totalDebits);
        		throw new Exception( sb.toString() );
        	}
        	if (totalCredits.compareTo(fileFooterCredits.abs()) != 0) {
        		StringBuffer sb = new StringBuffer();
        		sb.append("Credits in file footer do not match the sum of credits in transaction.");
        		sb.append(lineCountMessage());
        		sb.append(" Credit value given in file footer: ");
        		sb.append(fileFooterCredits.abs());
        		sb.append(" Credit value generated by summing transactions: ");
        		sb.append(totalCredits);        		        		
        		throw new Exception(sb.toString());

        	}
        }
        if (recordId.equals("02")) { //cardholder header

        	parent = new ProcurementCardTransaction();

            parent.setTransactionCreditCardNumber(extractNormalizedString(line, 2, 18, true)); //req
            parent.setChartOfAccountsCode(defaultChart); //req
            parent.setTransactionCycleEndDate(extractCycleDate(line, 294, 296));
//            parent.setTransactionCycleStartDate(transactionCycleStartDate); //may not be able to have
            parent.setCardHolderName(extractNormalizedString(line, 43, 68));
            parent.setCardHolderLine1Address(extractNormalizedString(line, 68, 104));
            parent.setCardHolderLine2Address(extractNormalizedString(line, 104,140));
            parent.setCardHolderCityName(extractNormalizedString(line, 140, 165));
            parent.setCardHolderStateCode(extractNormalizedString(line, 165, 167));
            // KITI-2203 : Removing last four characters from zip+4 so validation performs properly.
            parent.setCardHolderZipCode(extractNormalizedString(line, 167, 172));
//            parent.setCardHolderZipCode(extractNormalizedString(line, 167, 176)); 
            parent.setCardHolderAlternateName(extractNormalizedString(line, 191, 206));
            parent.setCardHolderWorkPhoneNumber(extractNormalizedString(line, 206, 216));
//            parent.setCardLimit(extractDecimal(line, 352, 363));
            parent.setCardStatusCode(extractNormalizedString(line, 267, 268));
            
            String companyCode = extractNormalizedString(line, 252, 257);
            
            if(companyCode.equals("99998")) {
            	duplicateTransactions = true;
            } else {
            	duplicateTransactions = false;
            }
        }
        if (recordId.equals("05") && !duplicateTransactions){	        		        	
        	
        	ProcurementCardTransaction child = new ProcurementCardTransaction();
        	
        	//Pull everything in from the preceding '05' record
            child.setTransactionCreditCardNumber(parent.getTransactionCreditCardNumber());
            child.setChartOfAccountsCode(defaultChart);
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
            
            child.setAccountNumber(extractNormalizedString(line, 317, 324, true)); //req
            child.setSubAccountNumber(extractNormalizedString(line, 324, 329));
            // KITI-2583 : Object code is not a required field and will be replaced by an error code if it is not present.
            child.setFinancialObjectCode(extractNormalizedString(line, 329, 333, false)); //req
            child.setFinancialSubObjectCode(extractNormalizedString(line,333,336));
            child.setProjectCode(extractNormalizedString(line, 336, 346));	        	
            child.setFinancialDocumentTotalAmount(extractDecimal(line, 79, 91));  //req
            child.setTransactionDebitCreditCode(convertDebitCreditCode(line.substring(64,65))); //req
            child.setTransactionDate(extractDate(line, 45, 53)); // req	
            child.setTransactionOriginalCurrencyCode(extractNormalizedString(line, 61, 64));
            child.setTransactionBillingCurrencyCode(extractNormalizedString(line, 76, 79));
            child.setVendorName(extractNormalizedString(line, 95, 120));
            child.setTransactionReferenceNumber(extractNormalizedString(line, 18, 41));
            child.setTransactionMerchantCategoryCode(extractNormalizedString(line, 91, 95));
            child.setTransactionPostingDate(extractDate(line, 53, 61));
            child.setTransactionOriginalCurrencyAmount(extractDecimalWithCents(line, 64, 76));
            child.setTransactionCurrencyExchangeRate(extractDecimal(line, 304,317).bigDecimalValue());
            child.setTransactionSettlementAmount(extractDecimal(line, 79, 91));

//            child.setTransactionTaxExemptIndicator(extractNormalizedString(line, 234, 235));
            child.setTransactionPurchaseIdentifierIndicator(extractNormalizedString(line, 272, 273));
            
            child.setTransactionPurchaseIdentifierDescription(extractNormalizedString(line, 273, 298));
            child.setVendorCityName(extractNormalizedString(line, 120, 146));
            child.setVendorStateCode(extractNormalizedString(line, 146, 148));
//            child.setVendorZipCode(extractNormalizedString(line, 152, 161));

            //
            //   Fix to handle zip codes provided by US Bank
            //
            String vendorZipCode = extractNormalizedString(line,152,161);
            if (vendorZipCode.startsWith("0000")) {
            	vendorZipCode = extractNormalizedString(line,156,161);
            }
            child.setVendorZipCode(vendorZipCode);

            child.setVisaVendorIdentifier(extractNormalizedString(line, 176, 192));
            
            if (child.getTransactionDebitCreditCode().equals("D")) {
            	accumulatedDebits = accumulatedDebits.add(child.getTransactionSettlementAmount());
            } else {
            	accumulatedCredits = accumulatedCredits.add(child.getTransactionSettlementAmount());	            	
            }
            
            ProcurementCardTransactionExtendedAttribute extension = new ProcurementCardTransactionExtendedAttribute();
            extension.setTransactionType(extractNormalizedString(line, 333, 335));
            if (child.getTransactionSequenceRowNumber() == null) {
                Integer generatedTransactionSequenceRowNumber = SpringContext.getBean(SequenceAccessorService.class).getNextAvailableSequenceNumber(FP_PRCRMNT_CARD_TRN_MT_SEQ).intValue();
                child.setTransactionSequenceRowNumber(generatedTransactionSequenceRowNumber);
                extension.setTransactionSequenceRowNumber(child.getTransactionSequenceRowNumber());
            }            
            
            child.setExtension(extension);
            
            transactionCount++;
            return child;

        } 
        // Still need to update totals and transaction count so validation works properly, but don't want transactions to be loaded
        if (recordId.equals("05") && duplicateTransactions){	        		        	
            if (convertDebitCreditCode(line.substring(64,65)).equals("D")) {
            	accumulatedDebits = accumulatedDebits.add(extractDecimal(line, 79, 91));
            } else {
            	accumulatedCredits = accumulatedCredits.add(extractDecimal(line, 79, 91));	            	
            }
            transactionCount++;
        }
        if (recordId.equals("95")) { //cardholder footer
        	KualiDecimal recordDebits = new KualiDecimal(extractNormalizedString(line, 24, 36));
        	KualiDecimal recordCredits = new KualiDecimal(extractNormalizedString(line, 36, 48));
        	
        	if (accumulatedCredits.compareTo(recordCredits.abs()) !=0) {
        		StringBuffer sb = new StringBuffer();
        		sb.append("Total credits given in cardholder footer do not sum to same value as transactions.");
        		sb.append(lineCountMessage());
        		sb.append(" Credit value given in cardholder footer: ");
        		sb.append(recordCredits.abs());
        		sb.append(" Credit value generated by summing transactions: ");
        		sb.append(accumulatedCredits);
        		throw new Exception(sb.toString());
        	}
        	if (accumulatedDebits.compareTo(recordDebits.abs()) != 0) {
        		StringBuffer sb = new StringBuffer();
        		sb.append("Total debits given in cardholder footer do not sum to same value as transactions."); 
        		sb.append(lineCountMessage());
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

        return null;
    }
        
    private Date extractDate(String line, int begin, int end) throws Exception {
    	Date theDate;
    	try {
	    	String sub = line.substring(begin, end);
	    	String year = sub.substring(0,4);
	    	String month = sub.substring(4,6);
	    	String day = sub.substring(6,8);
	    	theDate = Date.valueOf(year+"-"+month+"-"+day);
    	}
    	catch (Exception e) {
    		throw new Exception("Unable to parse date from the value " + line.substring(begin,end) + " on line " + lineCount);
    	}
    	return theDate;
    }
    
    private String extractNormalizedString(String line, int begin, int end) throws Exception {
    	String theString = line.substring(begin, end);
    	if(theString.trim().length()==0) {
    		return null;
    	}
    	return theString;
    }

    private KualiDecimal extractDecimal(String line, int begin, int end) throws Exception {
    	KualiDecimal theDecimal;
    	try {
	    	String sanitized = line.substring(begin, end);
	    	sanitized = StringUtils.remove(sanitized, '-');
	    	sanitized = StringUtils.remove(sanitized, '+');
	    	theDecimal = new KualiDecimal(sanitized);
    	}
    	catch (Exception e) {
    		throw new Exception("Unable to parse " +  line.substring(begin, end) + " into a decimal value on line " + lineCount);
    	}
    	return theDecimal;
    }
    
    private KualiDecimal extractDecimalWithCents(String line, int begin, int end) throws Exception {
    	KualiDecimal theDecimal;
    	KualiDecimal theCents;
    	try {
	    	String sanitized = line.substring(begin, end-2);
	    	sanitized = StringUtils.remove(sanitized, '-');
	    	sanitized = StringUtils.remove(sanitized, '+');
	    	theDecimal = new KualiDecimal(sanitized);
	    	theCents = new KualiDecimal(line.substring(end-2, end));
	    	theCents = theCents.multiply(new KualiDecimal("0.01"));
	    	theDecimal = theDecimal.add(theCents);
    	}
    	catch (Exception e) {
    		throw new Exception("Unable to parse " +  line.substring(begin, end) + " into a decimal value on line " + lineCount);
    	}
    	return theDecimal;
    }

    
    @SuppressWarnings("deprecation")
	private Date extractCycleDate(String line, int begin, int end) throws Exception {
    	String day = line.substring(begin, end);
    	Date theDate = new Date(System.currentTimeMillis());
    	theDate.setDate(Integer.parseInt(day));
    	return theDate;
    }
    
    private String extractNormalizedString(String line, int begin, int end, boolean required) throws Exception {
    	String theValue = extractNormalizedString(line, begin, end);
    	if (required) {
    		if (theValue==null) {
    			throw new Exception("A required value was missing at " + begin + " " + end + " on line " + lineCount);
    		}
    	}
    	return theValue;
    }
           
    private String convertDebitCreditCode(String val) throws Exception {
    	if (val.equals("+")) {
    		return "D";
    	}
    	else if (val.equals("-")) {
    		return "C";
    	}
    	else {
    		throw new Exception("Unable to determine whether transaction line is a debit or a credit");
    	}
    }
    
    private String lineCountMessage() {
    	return " Error occurred on line # " + lineCount;
    }
}
