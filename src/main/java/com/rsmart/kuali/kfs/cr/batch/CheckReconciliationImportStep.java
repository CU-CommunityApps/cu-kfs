/*
 * Copyright 2008 The Kuali Foundation.
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
package com.rsmart.kuali.kfs.cr.batch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.io.comparator.NameFileComparator;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentStatus;
import org.kuali.kfs.pdp.service.PaymentDetailService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.rice.kns.bo.KualiCode;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.KualiConfigurationService;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.KualiInteger;
import org.springframework.transaction.annotation.Transactional;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconError;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;
import com.rsmart.kuali.kfs.cr.document.service.GlTransactionService;


/**
 * Check Reconciliation Import Step
 * 
 * @author Derek Helbert
 * @version $Revision$
 */
public class CheckReconciliationImportStep extends AbstractStep {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CheckReconciliationImportStep.class);

    private String delimeter = null;

    private String fileType = null;
    
    private boolean header;
    
    private boolean footer;
    
    private String headerLine;
    
    private String footerLine;
    
    private int checkNumCol;
    
    private int checkDateCol;
    
    private int statusCol;
    
    private int amountCol;
    
    private int accountNumCol;
    
    private boolean isAmountDecimalValue;
    
    private boolean isAccountNumHeaderValue;
    
    private static SimpleDateFormat dateformat = null;

    private static SimpleDateFormat ARCHIVE_DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
    
    private static DecimalFormat DECFORMAT = new DecimalFormat("#.00");
    
    private BusinessObjectService businessObjectService;
    
    private PaymentDetailService paymentDetailService;

    private GlTransactionService glTransactionService;
    
    private List<Column> columns = new ArrayList<Column>();
    
    private List<Column> headerColumns = new ArrayList<Column>();
    
    private Map<Integer,String> headerMap;
    
    private Map<Integer,String> footerMap;
    
    private Map<String,String> statusMap = new HashMap<String,String>();;
    
    private List<Column> footerColumns = new ArrayList<Column>();
    
    private KualiConfigurationService  kualiConfigurationService;
    
    private static char QUOTE = '\"';

    private static char COMMA = ',';
    
    /**
     * Execute
     * 
     * @param jobName Job Name
     * @param jobRunDate Job Date
     * @see org.kuali.kfs.sys.batch.Step#execute(java.lang.String, java.util.Date)
     */
    @Transactional
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        LOG.info("Started CheckReconciliationImportStep @ " + (new Date()).toString());
        
        List<CheckReconError> records = new ArrayList<CheckReconError>();
        
        // Update canceled check from PDP
        updateCanceledChecks();
        
        // Import missing PDP payments
        importPdpPayments();
        
        // Get column numbers
        checkNumCol   = Integer.parseInt(getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.CHECK_NUM_COL));
        checkDateCol  = Integer.parseInt(getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.CHECK_DATE_COL));
        statusCol     = Integer.parseInt(getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.STATUS_COL));
        amountCol     = Integer.parseInt(getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.AMOUNT_COL));
        accountNumCol = Integer.parseInt(getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.ACCOUNT_NUM_COL));
        
        isAmountDecimalValue    = getParameterService().getIndicatorParameter(CheckReconciliationImportStep.class,CRConstants.AMOUNT_DECIMAL_IND);
        isAccountNumHeaderValue = getParameterService().getIndicatorParameter(CheckReconciliationImportStep.class,CRConstants.ACCOUNT_NUM_HEADER_IND);
        
        // Get file info
        fileType   = getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.CHECK_FILE_TYPE);
        header     = getParameterService().getIndicatorParameter(CheckReconciliationImportStep.class,CRConstants.CHECK_FILE_HEADER);
        footer     = getParameterService().getIndicatorParameter(CheckReconciliationImportStep.class,CRConstants.CHECK_FILE_FOOTER);
        dateformat = new SimpleDateFormat(getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.CHECK_DATE_FORMAT));
        
        try {
            if( CRConstants.DELIMITED.equals(fileType) ) {
                delimeter = getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.CHECK_FILE_DELIMETER);
                setStatusMap();
            }
            else if( CRConstants.FIXED.equals(fileType) ) {
                String checkFileCols = getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.CHECK_FILE_COLUMNS);
            
                StringTokenizer st = new StringTokenizer(checkFileCols, ";", false);
                int min = 0;
                int max = 0;
            
                while( st.hasMoreTokens() ) {
                    max = max + Integer.parseInt(st.nextToken());
                    columns.add( new Column(min,max) );
                    min = max;
                }
            
                if(header) {
                    checkFileCols = getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.CHECK_FILE_HEADER_COLUMNS);
                
                    st = new StringTokenizer(checkFileCols, ";", false);
                    min = 0;
                    max = 0;
                
                    while( st.hasMoreTokens() ) {
                        max = max + Integer.parseInt(st.nextToken());
                        headerColumns.add( new Column(min,max) );
                        min = max;
                    }
                }
            
                if(footer) {
                    checkFileCols = getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.CHECK_FILE_FOOTER_COLUMNS);
                
                    st = new StringTokenizer(checkFileCols, ";", false);
                    min = 0;
                    max = 0;
                
                    while( st.hasMoreTokens() ) {
                        max = max + Integer.parseInt(st.nextToken());
                        footerColumns.add( new Column(min,max) );
                        min = max;
                    }
                }
            
                setStatusMap();
            }
            else {
                LOG.warn("File Type Has Not Been Set");
            }
        
            List<String> list = getFileList();

            for (int i = 0; i < list.size(); i++) {
                parseTextFile(list.get(i), records);
                archiveFile(list.get(i));
            }
            
            writeLog(records);
        }
        catch (Exception err) {
            LOG.error("CheckReconciliationImportStep ERROR", err);
            return false;
        }

        LOG.info("Completed CheckReconciliationImportStep @ " + (new Date()).toString());

        return true;
    }

    /**
     * Write Log
     * 
     * @param records
     */
    private void writeLog(List<CheckReconError> records) {
        String prop = kualiConfigurationService.getPropertyString(KFSConstants.REPORTS_DIRECTORY_KEY) + "/cr/";

        File folder = new File(prop);

        if (!folder.exists()) {
            boolean created = folder.mkdir();

            if (created) {
                LOG.info("Created new CR log folder : " + folder.getAbsolutePath());
            }
            else {
                LOG.warn("Unable to create log folder : " + folder.getAbsolutePath());
                return;
            }
        }
        else if (!folder.isDirectory()) {
            LOG.warn("'" + prop + "' is not a folder.");
            return;
        }

        if (folder.exists()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

            String sepa = "/";
            String logFile = folder.getAbsolutePath() + sepa + "cr_" + sdf.format(new java.util.Date()) + ".txt";

            Writer output = null;
                
            try {   
                output = new BufferedWriter(new FileWriter(logFile));
                output.write((new java.util.Date().toString()) + "\n");
                output.write("Check Reconciliation Error Report\n\n");

                output.write("Account Number Check Number ");
                output.write(padString("Date", 30));
                output.write("Comment\n");


                for (CheckReconError temp : records) {
                    output.write(padString(temp.getBankAcctNum(), 15));
                    output.write(padString(temp.getCheckNum(), 13));
                    output.write(padString(temp.getCheckDate(), 30));
                    output.write(temp.getMessage());
                    output.write('\n');
                }
            }
            catch(Exception err) {
                LOG.error("writeLog",err);
            }
            finally {
                try {
                    output.close();
                } catch (Exception e) {
                }
            }

        }
    }
    
    /**
     * Get Check Recon Error
     * 
     * @param cr
     * @param message
     * @return CheckReconError
     */
    private CheckReconError getCheckReconError(CheckReconciliation cr, String message) {
        CheckReconError amr = new CheckReconError();
        amr.setBankAcctNum(cr.getBankAccountNumber());
        amr.setCheckNum(cr.getCheckNumber().toString());
        amr.setCheckDate(cr.getCheckDate().toString());
        amr.setMessage(message);

        return amr;
    }
    
    /**
     * Pad String
     * 
     * @param val
     * @param size
     * @return String
     */
    private String padString(String val, int size) {
        String temp = val;
        int length = temp.length();

        if (length < size) {
            for (int i = length; i < size; i++) {
                temp += ' ';
            }
        }

        return temp;
    }
    
    /**
     * Import PDP Payments
     * 
     */
    private void importPdpPayments() {
        Collection<Bank> banks = businessObjectService.findAll(Bank.class);
        Collection<CheckReconciliation> payments = glTransactionService.getNewCheckReconciliations(banks);
        
        for(CheckReconciliation temp : payments ) {
            businessObjectService.save(temp);
        }
        
        LOG.info("Found (" + payments.size() + ") New PDP payments.");
    }
    
    /**
     * Method to update canceled check records.
     * 
     */
    private void updateCanceledChecks() {
        Collection<Integer> col = glTransactionService.getCanceledChecks();
        
        for(Integer i : col) {
            CheckReconciliation cr = businessObjectService.findBySinglePrimaryKey(CheckReconciliation.class, i);
            
            cr.setLastUpdate(new Timestamp(new java.util.Date().getTime()));
            cr.setStatus(CRConstants.CANCELLED);
            cr.setGlTransIndicator(Boolean.TRUE);
            
            businessObjectService.save(cr);
        }
        
        LOG.info("Found (" + col.size() + ") canceled PDP payments for update.");
    }
    
    /**
     * Set Status Map
     */
    private void setStatusMap() throws Exception {
        StringTokenizer st = null;
        String statusProps = null;
        
        // Setup status map
        statusProps = getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.CLRD_STATUS);
        
        if( statusProps == null ) {
            throw new Exception( CRConstants.CLRD_STATUS + " system parameter is null." );
        }
        else {
            st = new StringTokenizer(statusProps, ";", false);
        
            while( st.hasMoreTokens() ) {
                statusMap.put(st.nextToken(),CRConstants.CLEARED);
            }
        }
        
        statusProps = getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.ISSD_STATUS);
        
        if( statusProps == null ) {
            LOG.warn( CRConstants.ISSD_STATUS + " system parameter is null." );
        }
        else {
            st = new StringTokenizer(statusProps, ";", false);
        
            while( st.hasMoreTokens() ) {
                statusMap.put(st.nextToken(),CRConstants.ISSUED);
            }
        }
        
        statusProps = getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.VOID_STATUS);
        
        if( statusProps == null ) {
            LOG.warn( CRConstants.VOID_STATUS + " system parameter is null." );
        }
        else {
            st = new StringTokenizer(statusProps, ";", false);
        
            while( st.hasMoreTokens() ) {
                statusMap.put(st.nextToken(),CRConstants.VOIDED);
            }
        }
        
        statusProps = getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.CNCL_STATUS);
        
        if( statusProps == null ) {
            LOG.warn( CRConstants.CNCL_STATUS + " system parameter is null." );
        }
        else {
            st = new StringTokenizer(statusProps, ";", false);
        
            while( st.hasMoreTokens() ) {
                statusMap.put(st.nextToken(),CRConstants.CANCELLED);
            }
        }
        
        statusProps = getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.STAL_STATUS);
        
        if( statusProps == null ) {
            LOG.warn( CRConstants.STAL_STATUS + " system parameter is null." );
        }
        else {
            st = new StringTokenizer(statusProps, ";", false);
        
            while( st.hasMoreTokens() ) {
                statusMap.put(st.nextToken(),CRConstants.STALE);
            }
        }
        
        statusProps = getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.STOP_STATUS);
        
        if( statusProps == null ) {
            LOG.warn( CRConstants.STOP_STATUS + " system parameter is null." );
        }
        else {
            st = new StringTokenizer(statusProps, ";", false);
        
            while( st.hasMoreTokens() ) {
                statusMap.put(st.nextToken(),CRConstants.STOP);
            }
        }
    }

    /**
     * Get File List
     * 
     * @return List
     * @throws Exception
     */
    private List<String> getFileList() throws Exception {
        List<String> fileList = new ArrayList<String>();

        String prop = kualiConfigurationService.getPropertyString(KFSConstants.STAGING_DIRECTORY_KEY) + "/cr/upload";
          
        File folder = new File(prop);

        if( !folder.exists() ) {
            boolean created = folder.mkdir();
            
            if( created ) {
                LOG.info("Created new CR upload folder : " + folder.getAbsolutePath() );
            }
            else {
                throw new Exception("Unable to create folder : " + folder.getAbsolutePath());
            }
        }
        else if (!folder.isDirectory()) {
            throw new Exception("'" + prop + "' is not a folder.");
        }
        
        else {
            File[] files = folder.listFiles();
            
    		Arrays.sort(files, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);

            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    fileList.add(files[i].getAbsolutePath());
                }
            }
        }

        return fileList;
    }

    /**
     * Archive Check File
     * 
     * @param checkFile Path to File
     * @throws Exception
     */
    private void archiveFile(String checkFile) throws Exception {
        LOG.info("Archiving File : " + checkFile);

        String prop = kualiConfigurationService.getPropertyString(KFSConstants.STAGING_DIRECTORY_KEY) + "/cr/archive";
        
        File file    = new File(checkFile); // Check File
        File archive = new File(prop);      // Archive Folder

        if (!archive.exists()) {
            boolean created = archive.mkdir();
            
            if( created ) {
                LOG.info("Created new CR archive folder : " + archive.getAbsolutePath() );
            }
            else {
                throw new Exception("Unable to create folder : " + archive.getAbsolutePath());
            }
        }
        else if (!archive.isDirectory()) {
            throw new Exception("'" + prop + "' is not a folder.");
        }

        // Create Folder Today
        File datedFolder = new File(prop + System.getProperty("file.separator") + ARCHIVE_DATEFORMAT.format(new Date()));

        if (!datedFolder.exists()) {
            LOG.info("Creating archive folder : " + datedFolder.getAbsoluteFile());
            datedFolder.mkdir();
        }

        // Move file to dated folder
        boolean success = file.renameTo(new File(datedFolder, file.getName()));

        if (!success) {
            throw new Exception("Unable to archive check file.");
        }
    }

    /**
     * Get Header Value
     * 
     * @param column
     * 
     * @return String
     */
    private String getHeaderValue(Integer column) {
        return headerMap.get(column);
    }
    
    /**
     * Get Property
     * 
     * @param key
     * 
     * @return String
     */
    private String getProperty(String key) {
        return kualiConfigurationService.getPropertyString(key);
    }
    
    /**
     * Get Property
     * 
     * @param key
     * 
     * @return String
     */
    private Integer getIntProperty(String key) {
        return new Integer(kualiConfigurationService.getPropertyString(key));
    }
    
    /**
     * Parse Footer Line
     * 
     * @return Map
     * @throws Exception
     */
    private Map<Integer,String> parseFooterLine() throws Exception {
        Map<Integer,String> hash = new HashMap<Integer,String>();
        
        if( CRConstants.FIXED.equals(fileType) ) {
            for( int i=0; i<footerColumns.size(); i++) {
                hash.put(new Integer(i+1), footerLine.substring(footerColumns.get(i).getStart(), footerColumns.get(i).getEnd()).trim());
            }
        }
        else if( CRConstants.DELIMITED.equals(fileType) ) {
            int cnt = 1;
            
            List<String> list = processDelimitedLine(footerLine);
            
            for( String value : list ) {
                hash.put(new Integer(cnt++), value);
            }
        }
        
        return hash;
    }
    
    /**
     * Parse Header Line
     * 
     * @return Map
     * @throws Exception
     */
    private Map<Integer,String> parseHeaderLine() throws Exception {
        Map<Integer,String> hash = new HashMap<Integer,String>();
        
        if( CRConstants.FIXED.equals(fileType) ) {
            for( int i=0; i<headerColumns.size(); i++) {
                hash.put(new Integer(i+1), headerLine.substring(headerColumns.get(i).getStart(), headerColumns.get(i).getEnd()).trim());
            }
        }
        else if( CRConstants.DELIMITED.equals(fileType) ) {
            int cnt = 1;
            
            List<String> list = processDelimitedLine(headerLine);
            
            for( String value : list ) {
                hash.put(new Integer(cnt++), value);
            }
        }
        
        return hash;
    }
    
    /**
     * Add Decimal Points
     * 
     * @param value
     * @return String
     */
    private String addDecimalPoint(String value) {
        String temp   = null;
        int    length = value.length();
        
        if( length > 2 ) {
            temp = value.substring(0,length-2) + "." + value.substring(length-2);
        }
        else {
            temp = value;
        }
        
        return temp;
    }
    
    /**
     * Parse Fixed Width Line
     * 
     * @param line
     * @return CheckReconciliation
     * @throws Exception
     */
    private CheckReconciliation parseFixedLine(String line) throws Exception {
        Map<Integer,String> hash = new HashMap<Integer,String>();
        int noOfColumns = columns.size();
        int sizeOfLine 	= line.length();
        for( int i=0; i< noOfColumns; i++){
        	int startPosition 	= columns.get(i).getStart();
        	int endPosition 	= columns.get(i).getEnd();
        	if(endPosition > sizeOfLine) endPosition = sizeOfLine;
        	String field = line.substring(startPosition,endPosition);
        	hash.put(new Integer(i+1),field);
        	if(endPosition == sizeOfLine) break;
        }
        /*
        for( int i=0; i<columns.size(); i++) {
            hash.put(new Integer(i+1), line.substring(columns.get(i).getStart(), columns.get(i).getEnd()).trim());
        }
        */
        
        return setCheckReconciliationAttributes(hash);
    }

    /**
     * Parse Delimited Line
     * 
     * @param line
     * @return CheckReconciliation
     * @throws Exception
     */
    private CheckReconciliation parseDelimitedLine(String line) throws Exception {
        List<String> list = processDelimitedLine(line);
        
        Map<Integer,String> hash = new HashMap<Integer,String>();
        int cnt = 1;

        for( String temp : list ) {
            hash.put(new Integer(cnt++), temp.trim());
        }
        
        return setCheckReconciliationAttributes(hash);
    }
    
    /**
     * Process Line
     * 
     * @param line
     */
    public List<String> processDelimitedLine(String line) {
        List<String> fields = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        
        if (line == null) {
            return fields;
        }
        else if (line.length() == 0) {
            fields.add(line);
        }

        int index = 0;
        
        do {
            sb.setLength(0);
            if (index < line.length() && line.charAt(index) == QUOTE) {
                index = handleQuotedField(line, sb, ++index);
            } 
            else {
                index = handlePlainField(line, sb, index);
            }
            
            fields.add(sb.toString());
            index++;
        } 
        while (index < line.length());

        return fields;
    }

    /**
     * Handles a quoted field.
     * 
     * @return int
     */
    protected int handleQuotedField(String s, StringBuffer sb, int i) {
        int j;
        int len = s.length();

        for (j = i; j < len; j++) {
            if ((s.charAt(j) == QUOTE) && (j + 1 < len)) {
                if (s.charAt(j + 1) == QUOTE) {
                    j++;
                } else if (s.charAt(j + 1) == COMMA) {
                    j++;
                    break;
                }
            } else if ((s.charAt(j) == QUOTE) && (j + 1 == len)) {
                break;
            }

            sb.append(s.charAt(j)); // regular character
        }
        return j;
    }

    /**
     * Handles an unquoted field.
     * 
     * @return int
     */
    protected int handlePlainField(String s, StringBuffer sb, int i) {
        int j = s.indexOf(COMMA, i);

        if (j == -1) {
            sb.append(s.substring(i));
            return s.length();
        } else {
            sb.append(s.substring(i, j));
            return j;
        }
    }
    
    /**
     * Set CheckReconciliation Attributes
     * 
     * @param hash
     * @return
     * @throws ParseException
     */
    private CheckReconciliation setCheckReconciliationAttributes(Map<Integer, String> hash) throws ParseException {
        String checkNumber = null;
        Date checkDate = null;
        KualiDecimal amount = null;
        String accountNumber = null;
        String status = null;
        Date issueDate = null;
        String payeeName = "";
        String payeeID	= "";
        //Cornell Columns
        Integer payeeNameCol = Integer.parseInt("12");
        Integer issueDateCol = Integer.parseInt("7");
        Integer payeeIDCol	 = Integer.parseInt("6");
        
        checkNumber   = hash.get(checkNumCol);
        String rawCheckDate = hash.get(checkDateCol);
        if(rawCheckDate==null||rawCheckDate.equals("") ||rawCheckDate.equals("000000"))
        	rawCheckDate = "991231";
        
        checkDate = getGregorianCalendar(rawCheckDate).getTime();
       // checkDate     = dateformat.parse(rawCheckDate);  //Date Paid
        amount        = isAmountDecimalValue    ? new KualiDecimal(addDecimalPoint(hash.get(amountCol))) : new KualiDecimal(hash.get(amountCol));
        if(accountNumCol>0)
        	accountNumber = isAccountNumHeaderValue ? getHeaderValue(accountNumCol) : hash.get(accountNumCol);
        else
        	accountNumber =   getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.ACCOUNT_NUM);
        
        status        = hash.get(statusCol);
        
        String issueDateRawValue 	= hash.get(issueDateCol);
        payeeName 					= hash.get(payeeNameCol);
        payeeID	  					= hash.get(payeeIDCol);
        
        if(issueDateRawValue==null||issueDateRawValue.equals("")||issueDateRawValue.equals("000000"))
        	issueDateRawValue = "991231";
        
        //issueDate = dateformat.parse(issueDateRawValue);
        issueDate = getGregorianCalendar(issueDateRawValue).getTime();
        
        CheckReconciliation cr = new CheckReconciliation();
        cr.setAmount(amount);
        cr.setCheckDate(new java.sql.Date(issueDate.getTime()));
        cr.setCheckNumber(new KualiInteger(checkNumber));
        cr.setBankAccountNumber(accountNumber);
        cr.setStatus(status);
        cr.setStatusChangeDate(new java.sql.Date(checkDate.getTime()));
        cr.setGlTransIndicator(false);
        cr.setPayeeName(payeeName);
        cr.setPayeeId(payeeID);
        
        return cr;
    }
    
    
   private GregorianCalendar getGregorianCalendar(String yyMMDD){
	   String year  = "20"+yyMMDD.substring(0,2);
	   String month = yyMMDD.substring(2,4);
	   String day = yyMMDD.substring(4);
	   
	   month = ""+(Integer.parseInt(month) -1);
	   
	   GregorianCalendar gc = new GregorianCalendar(Integer.parseInt(year),Integer.parseInt(month),Integer.parseInt(day));
	 
	   return gc;
	   
   }

    /**
     * Parse File
     * 
     * @param checkFile Check File Path
     * @throws Exception
     */
    private void parseTextFile(String checkFile, List<CheckReconError> records) throws Exception {
        LOG.info("Parsing File : " + checkFile);

        File file = new File(checkFile);
        BufferedReader br = new BufferedReader(new FileReader(file));

        String line = null;
        int    totalLinesProcessed = 0;

        Timestamp ts = new Timestamp( new java.util.Date().getTime() );
        
        Collection<Bank> banks = businessObjectService.findAll(Bank.class);
        
        while ((line = br.readLine()) != null) {
            if( header && totalLinesProcessed == 0 ) {
                headerLine = line;
                headerMap  = parseHeaderLine();
                totalLinesProcessed++;
            }
            else if( footer && !br.ready() ) {
                footerLine = line;
                footerMap  = parseFooterLine();
                totalLinesProcessed++;
            }
            else {
                CheckReconciliation cr = null;
            
                if( CRConstants.DELIMITED.equals(fileType) ) {
                    cr = parseDelimitedLine(line);
                }
                else if( CRConstants.FIXED.equals(fileType) ) {
                    cr = parseFixedLine(line);
                }
                
                // Find existing check record
                CheckReconciliation existingRecord = getCheckReconciliation(cr.getCheckNumber(),cr.getBankAccountNumber());
                
                if( existingRecord == null ) {                    
                    // Update update ts
                    cr.setLastUpdate(ts);
                    // Set Status to Exception
                    cr.setStatus(CRConstants.EXCP);
                    // Set GL Trans False
                    cr.setGlTransIndicator(Boolean.FALSE);
                    String notFoundSrc   	= getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.SRC_NOT_FOUND);
                    String notFoundBankCd   = getParameterService().getParameterValue(CheckReconciliationImportStep.class,CRConstants.BNK_CD_NOT_FOUND);
                    
                    cr.setSourceCode(notFoundSrc);  
                    cr.setBankCode(notFoundBankCd);
                    businessObjectService.save(cr);
                    
                    records.add(getCheckReconError(cr, "The bank record does not exist in reconciliation table. " + cr.getCheckNumber()));
                    LOG.error("Check Record Not Found");
                }
                else {
                    if( CRConstants.PDP_SRC.equals(existingRecord.getSourceCode()) ) {
                    	//update pdp records if checkstatus id cleared
                    	String checkStatus = updateCheckStatus(cr,banks,records);
                    	//update CheckRecon record if checkstatus is cleared	
                        if(checkStatus.equals(CRConstants.CLEARED)){
                            cr.setStatus(checkStatus);
                            existingRecord.setStatus(checkStatus);
                            existingRecord.setStatusChangeDate(cr.getStatusChangeDate());
                            existingRecord.setLastUpdate(ts);
                            businessObjectService.save(existingRecord);
                            LOG.info("Updated Check Recon Record : " + existingRecord.getId());
                        }
                        else if(checkStatus.equals(CRConstants.STOP)){
                        	if(!existingRecord.getStatus().equalsIgnoreCase(CRConstants.STOP)){
	                            records.add(getCheckReconError(cr, "Bank file status shows STOP and Check Recon table status is not STOP"));
	                            LOG.error("Bank file status is STOP and Check Recon table status is not STOP for check " + cr.getCheckNumber());
                        	}
                        }
                        else if(checkStatus.equals(CRConstants.VOIDED)){
                        	if(!existingRecord.getStatus().equalsIgnoreCase(CRConstants.VOIDED)){
	                            records.add(getCheckReconError(cr, "Bank file status is VOIDED and Check Recon table status is not VOIDED"));
	                            LOG.error("Bank file status is VOIDED and Check Recon table status is not VOID for check "+ cr.getCheckNumber());
                        	}
                        }

                    }
                    else {
                        String checkStatus = getCheckStatus(cr);
                        if(checkStatus.equals(CRConstants.CLEARED)){
                            cr.setStatus(checkStatus);
                            existingRecord.setStatus(checkStatus);
                            existingRecord.setStatusChangeDate(cr.getStatusChangeDate());
                            existingRecord.setLastUpdate(ts);
                            businessObjectService.save(existingRecord);
                            LOG.info("Updated Check Recon Record : " + existingRecord.getId());

                        }
                        else if(checkStatus.equals(CRConstants.STOP)){
                        	if(!existingRecord.getStatus().equalsIgnoreCase(CRConstants.STOP)){
	                            records.add(getCheckReconError(cr, "Bank file status is STOP and Check Recon table status is not STOP"));
	                            LOG.error("Bank file status is STOP and Check Recon table status is not STOP for check " + cr.getCheckNumber());
                        	}
                        }
                        else if(checkStatus.equals(CRConstants.VOIDED)){
                        	if(!existingRecord.getStatus().equalsIgnoreCase(CRConstants.VOIDED)){
	                            records.add(getCheckReconError(cr, "Bank file status is VOID and Check Recon table status is not VOID"));
	                            LOG.error("Bank file status is VOIDED and Check Recon table status is not VOID for check " + cr.getCheckNumber());
                        	}
                        }


                    }
                    
                }
                
                totalLinesProcessed++;
            }
        }

        LOG.info("Processed Records : " + totalLinesProcessed);

        br.close();
    }

    /**
     * Get CheckReconciliation
     * 
     * @param checkNumber
     * @param bankAccountNumber
     * 
     * @return CheckReconciliation
     */
    private CheckReconciliation getCheckReconciliation(KualiInteger checkNumber, String bankAccountNumber) {
        Map<Object,Object> fieldValues = new HashMap<Object,Object>();
        fieldValues.put("checkNumber", checkNumber);
        fieldValues.put("bankAccountNumber", bankAccountNumber);  
        
        Collection<CheckReconciliation> checks = businessObjectService.findMatching(CheckReconciliation.class, fieldValues);
        
        if(checks.size() > 0) {
            return checks.iterator().next();
        }
        else {
            return null;
        }
    }
    
    /**
     * Get Check Status - No PDP Payment Update
     * 
     * @param cr Check Reconciliation Object
     * @return String
     */
    private String getCheckStatus(CheckReconciliation cr ) {
        String defaultStatus = CRConstants.EXCP;

        if( statusMap.get(cr.getStatus()) != null ) {
            defaultStatus = statusMap.get(cr.getStatus());
        }
        else {
            LOG.warn("Update Record Status Failed ( " + cr.getStatus() + ") ID : " + cr.getId());
        }
        
        return defaultStatus;
    }
   
    
    private KualiDecimal getTotalNetAmount(Collection<PaymentGroup> paymentGroups ){
       KualiDecimal total = new KualiDecimal(0.0);
        
    	for (PaymentGroup paymentGroup : paymentGroups) {
    		KualiDecimal kd = paymentGroup.getNetPaymentAmount();
    		total = total.add(kd);
        }
    	
    	return total;
    	
    }
    
    /**
     * Get Check Status  and update pdp
     * 
     * @param cr Check Reconciliation Object
     * @return String
     */
    private String updateCheckStatus(CheckReconciliation cr, Collection<Bank> banks, List<CheckReconError> records) throws Exception {
        String defaultStatus = CRConstants.EXCP;

        List<String> bankCodes = new ArrayList<String>();
        
        // Generate list of valid bank codes
        for( Bank bank : banks ) {
            if( bank.getBankAccountNumber().equals(cr.getBankAccountNumber()) ) {
                bankCodes.add(bank.getBankCode());
            }
        }
        
        if( bankCodes.size() == 0 ) {
            throw new Exception("Invalid Bank Account Number : " + cr.getBankAccountNumber() );
        }
        
        Collection<PaymentGroup> paymentGroups = glTransactionService.getAllPaymentGroupForSearchCriteria(cr.getCheckNumber(), bankCodes);
        KualiDecimal totalNetAmount =getTotalNetAmount(paymentGroups); 
        
        for (PaymentGroup paymentGroup : paymentGroups) {
        	/*
        	 * At Cornell Check amount may consist of one or more payment group amounts.  
        	 * 
            if( !cr.getAmount().equals(paymentGroup.getNetPaymentAmount()) ) {
                records.add(getCheckReconError(cr, "The check amount does not match payment net amount."));
            }
            */
        	if(!(totalNetAmount.doubleValue() ==  cr.getAmount().doubleValue())){
                records.add(getCheckReconError(cr, "The check amount does not match payment net amount from the payment groups."));
        	}
        	
        	
            if( statusMap.get(cr.getStatus()) != null ) {
                defaultStatus = statusMap.get(cr.getStatus());
                // Update PDP status and save
                KualiCode code = businessObjectService.findBySinglePrimaryKey(PaymentStatus.class, defaultStatus);
                if (paymentGroup.getPaymentStatus() != ((PaymentStatus) code)) {
                    paymentGroup.setPaymentStatus((PaymentStatus) code);
                    paymentGroup.setLastUpdate(new Timestamp(cr.getStatusChangeDate().getTime()));
                }
                
                // Update PDP if the check status is cleared from the bank file
                
                if(defaultStatus.equals(CRConstants.CLEARED)){
                	businessObjectService.save(paymentGroup);
                	LOG.info("Check Status in the bank file is cleared. Updated Payment Group : " + paymentGroup.getId() + " Disbursement  " + paymentGroup.getDisbursementNbr());
                }
                
                	
            }
            else {
                LOG.warn("Update Payment Group Failed ( " + cr.getStatus() + ") ID : " + paymentGroup.getId());
            }
        }
        
        if( paymentGroups == null ) {
            LOG.info("No Payments Found : " + cr.getBankAccountNumber() + "-" + cr.getCheckNumber() );
        }
        else if( paymentGroups.size() == 0 ) {
            LOG.info("No Payments Found : " + cr.getBankAccountNumber() + "-" + cr.getCheckNumber() );
        }
        
        return defaultStatus;
    }

    /**
     * Get Business Object Service
     * 
     * @return BusinessObjectService
     */
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    /**
     * Set Business Object Service
     * 
     * @param businessObjectService Business Object Service
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * Column
     * 
     */
    private class Column {
        private int start;
        
        private int end;

        public Column(int start, int end) {
            this.start = start;
            this.end   = end;
        }
        
        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }
        
    }
    
    /**
     * Get KualiConfigurationService
     * 
     * @return KualiConfigurationService
     */
    public KualiConfigurationService getKualiConfigurationService() {
        return kualiConfigurationService;
    }

    /**
     * Set KualiConfigurationService
     * 
     * @param kualiConfigurationService
     */
    public void setKualiConfigurationService(KualiConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    /**
     * Get Payment Detail Service
     * 
     * @return
     */
    public PaymentDetailService getPaymentDetailService() {
        return paymentDetailService;
    }

    /**
     * Set PaymentDetailService
     * 
     * @param paymentDetailService
     */
    public void setPaymentDetailService(PaymentDetailService paymentDetailService) {
        this.paymentDetailService = paymentDetailService;
    }

    /**
     * Get GlTransactionService
     * 
     * @return GlTransactionService
     */
    public GlTransactionService getGlTransactionService() {
        return glTransactionService;
    }

    /**
     * Set GlTransactionService
     * 
     * @param glTransactionService
     */
    public void setGlTransactionService(GlTransactionService glTransactionService) {
        this.glTransactionService = glTransactionService;
    }
    
}
