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
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.krad.bo.KualiCode;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.pdp.PdpConstants.PaymentStatusCodes;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentStatus;
import org.kuali.kfs.pdp.service.PaymentDetailService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Bank;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconError;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;
import com.rsmart.kuali.kfs.cr.document.service.GlTransactionService;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.batch.CuAbstractStep;


/**
 * Check Reconciliation Import Step
 * 
 * @author Derek Helbert
 * @version $Revision$
 */
public class CheckReconciliationImportStep extends CuAbstractStep {

    private static final Logger LOG = LogManager.getLogger();

    private static final Pattern ALL_ZEROS_PATTERN = Pattern.compile("^0+$");
    private static final String DATE_000000 = "000000";
    private static final String DATE_991231 = "991231";
    private static final int YEAR_2099 = 2099;
    private static final int MONTH_12 = 12;
    private static final int DAY_31 = 31;

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
    
    private int issueDateCol;
    
    private int payeeIdCol;
    
    private int payeeNameCol;
    
    private boolean isAmountDecimalValue;
    
    private boolean isAccountNumHeaderValue;
    
    private Function<String, Date> dateParser;
    
    private BusinessObjectService businessObjectService;
    
    private PaymentDetailService paymentDetailService;

    private GlTransactionService glTransactionService;
    
    private List<Column> columns = new ArrayList<Column>();
    
    private List<Column> headerColumns = new ArrayList<Column>();
    
    private Map<Integer,String> headerMap;
    
    private Map<Integer,String> footerMap;
    
    private Map<String,String> statusMap = new HashMap<String,String>();;
    
    private List<Column> footerColumns = new ArrayList<Column>();
    
    private ConfigurationService  kualiConfigurationService;
    
    private static char QUOTE = '\"';

    private static char COMMA = ',';
    
    /**
     * Execute
     * 
     * @param jobName Job Name
     * @param jobRunDate Job Date
     * @see org.kuali.kfs.kns.bo.Step#execute(java.lang.String, java.util.Date)
     */
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        LOG.info("Started CheckReconciliationImportStep @ " + (new Date()).toString());
        LOG.info("execute, Using parameters with prefix: \"{}\"", getCrImportParameterPrefix());
        
        List<CheckReconError> records = new ArrayList<CheckReconError>();
        
        // Update canceled check from PDP
        updateCanceledChecks();
        
        // Import missing PDP payments
        importPdpPayments();
        
        // Get column numbers
        checkNumCol   = Integer.parseInt(getCrImportParameterValueAsString(CRConstants.CHECK_NUM_COL));
        checkDateCol  = Integer.parseInt(getCrImportParameterValueAsString(CRConstants.CHECK_DATE_COL));
        statusCol     = Integer.parseInt(getCrImportParameterValueAsString(CRConstants.STATUS_COL));
        amountCol     = Integer.parseInt(getCrImportParameterValueAsString(CRConstants.AMOUNT_COL));
        accountNumCol = Integer.parseInt(getCrImportParameterValueAsString(CRConstants.ACCOUNT_NUM_COL));
        issueDateCol  = Integer.parseInt(getCrImportParameterValueAsString(CRConstants.ISSUE_DATE_COL));
        payeeIdCol    = Integer.parseInt(getCrImportParameterValueAsString(CRConstants.PAYEE_ID_COL));
        payeeNameCol  = Integer.parseInt(getCrImportParameterValueAsString(CRConstants.PAYEE_NAME_COL));
        
        isAmountDecimalValue    = getCrImportParameterValueAsBoolean(CRConstants.AMOUNT_DECIMAL_IND);
        isAccountNumHeaderValue = getCrImportParameterValueAsBoolean(CRConstants.ACCOUNT_NUM_HEADER_IND);
        
        // Get file info
        fileType   = getCrImportParameterValueAsString(CRConstants.CHECK_FILE_TYPE);
        header     = getCrImportParameterValueAsBoolean(CRConstants.CHECK_FILE_HEADER);
        footer     = getCrImportParameterValueAsBoolean(CRConstants.CHECK_FILE_FOOTER);
        
        try {
            dateParser = getDateParser();
            if( CRConstants.DELIMITED.equals(fileType) ) {
                delimeter = getCrImportParameterValueAsString(CRConstants.CHECK_FILE_DELIMETER);
                setStatusMap();
            }
            else if( CRConstants.FIXED.equals(fileType) ) {
                String checkFileCols = getCrImportParameterValueAsString(CRConstants.CHECK_FILE_COLUMNS);
            
                StringTokenizer st = new StringTokenizer(checkFileCols, ";", false);
                int min = 0;
                int max = 0;
            
                while( st.hasMoreTokens() ) {
                    max = max + Integer.parseInt(st.nextToken());
                    columns.add( new Column(min,max) );
                    min = max;
                }
            
                if(header) {
                    checkFileCols = getCrImportParameterValueAsString(CRConstants.CHECK_FILE_HEADER_COLUMNS);
                
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
                    checkFileCols = getCrImportParameterValueAsString(CRConstants.CHECK_FILE_FOOTER_COLUMNS);
                
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
        } finally {
            dateParser = null;
            columns.clear();
            headerColumns.clear();
            footerColumns.clear();
            statusMap.clear();
            headerMap = null;
            footerMap = null;
            headerLine = null;
            footerLine = null;
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
        String prop = kualiConfigurationService.getPropertyValueAsString(KFSConstants.REPORTS_DIRECTORY_KEY) + "/cr/";

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
            SimpleDateFormat sdf = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_yyyyMMddHHmmss, Locale.US);

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
        statusProps = getCrImportParameterValueAsString(CRConstants.CLRD_STATUS);
        
        if( statusProps == null ) {
            throw new Exception( CRConstants.CLRD_STATUS + " system parameter is null." );
        }
        else {
            st = new StringTokenizer(statusProps, ";", false);
        
            while( st.hasMoreTokens() ) {
                statusMap.put(st.nextToken(),CRConstants.CLEARED);
            }
        }
        
        statusProps = getCrImportParameterValueAsString(CRConstants.ISSD_STATUS);
        
        if( statusProps == null ) {
            LOG.warn( CRConstants.ISSD_STATUS + " system parameter is null." );
        }
        else {
            st = new StringTokenizer(statusProps, ";", false);
        
            while( st.hasMoreTokens() ) {
                statusMap.put(st.nextToken(),CRConstants.ISSUED);
            }
        }
        
        statusProps = getCrImportParameterValueAsString(CRConstants.VOID_STATUS);
        
        if( statusProps == null ) {
            LOG.warn( CRConstants.VOID_STATUS + " system parameter is null." );
        }
        else {
            st = new StringTokenizer(statusProps, ";", false);
        
            while( st.hasMoreTokens() ) {
                statusMap.put(st.nextToken(),CRConstants.VOIDED);
            }
        }
        
        statusProps = getCrImportParameterValueAsString(CRConstants.CNCL_STATUS);
        
        if( statusProps == null ) {
            LOG.warn( CRConstants.CNCL_STATUS + " system parameter is null." );
        }
        else {
            st = new StringTokenizer(statusProps, ";", false);
        
            while( st.hasMoreTokens() ) {
                statusMap.put(st.nextToken(),CRConstants.CANCELLED);
            }
        }
        
        statusProps = getCrImportParameterValueAsString(CRConstants.STAL_STATUS);
        
        if( statusProps == null ) {
            LOG.warn( CRConstants.STAL_STATUS + " system parameter is null." );
        }
        else {
            st = new StringTokenizer(statusProps, ";", false);
        
            while( st.hasMoreTokens() ) {
                statusMap.put(st.nextToken(),CRConstants.STALE);
            }
        }
        
        statusProps = getCrImportParameterValueAsString(CRConstants.STOP_STATUS);
        
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

        String prop = kualiConfigurationService.getPropertyValueAsString(com.rsmart.kuali.kfs.sys.KFSConstants.STAGING_DIRECTORY_KEY) + "/cr/upload";
          
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

        String prop = kualiConfigurationService.getPropertyValueAsString(com.rsmart.kuali.kfs.sys.KFSConstants.STAGING_DIRECTORY_KEY) + "/cr/archive";
        
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

        // Add timestamp to the file name and save it in the archive folder
        addTimeStampToFileName(file, file.getName(), prop);
        
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
        return kualiConfigurationService.getPropertyValueAsString(key);
    }
    
    /**
     * Get Property
     * 
     * @param key
     * 
     * @return String
     */
    private Integer getIntProperty(String key) {
        return new Integer(kualiConfigurationService.getPropertyValueAsString(key));
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
        
        checkNumber   = hash.get(checkNumCol);
        String rawCheckDate = hash.get(checkDateCol);
        
        checkDate = dateParser.apply(rawCheckDate);

        amount        = isAmountDecimalValue    ? new KualiDecimal(addDecimalPoint(hash.get(amountCol))) : new KualiDecimal(hash.get(amountCol));
        if(accountNumCol>0)
        	accountNumber = isAccountNumHeaderValue ? getHeaderValue(accountNumCol) : hash.get(accountNumCol);
        else
        	accountNumber =   getCrImportParameterValueAsString(CRConstants.ACCOUNT_NUM);
        
        status        = hash.get(statusCol);
        
        String issueDateRawValue 	= hash.get(issueDateCol);
        payeeName 					= hash.get(payeeNameCol);
        payeeID	  					= hash.get(payeeIdCol);
        
        issueDate = dateParser.apply(issueDateRawValue);
        
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
    
    private Function<String, Date> getDateParser() {
        String dateFormatString = getCrImportParameterValueAsString(CRConstants.CHECK_DATE_FORMAT);
        if (StringUtils.equals(dateFormatString, CRConstants.LEGACY_DATE_FORMAT_yyMMdd)) {
            return this::getDateFromStringWithTwoDigitYear;
        } else {
            final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateFormatString, Locale.US);
            return dateString -> getDateFromString(dateString, dateFormatter);
        }
    }

    private Date getDateFromString(String dateString, DateTimeFormatter dateFormatter) {
        LocalDate localDate;
        if (StringUtils.isBlank(dateString) || ALL_ZEROS_PATTERN.matcher(dateString).matches()) {
            LOG.warn("getDateFromString, Row has a blank or all-zero date; defaulting to 12/31/2099");
            localDate = LocalDate.of(YEAR_2099, MONTH_12, DAY_31);
        } else {
            localDate = LocalDate.parse(dateString, dateFormatter);
        }
        Instant dateAsInstant = localDate.atStartOfDay(ZoneId.systemDefault())
                .toInstant();
        return Date.from(dateAsInstant);
    }

    private Date getDateFromStringWithTwoDigitYear(String dateString) {
        String revisedDateString = dateString;
        if (StringUtils.isEmpty(dateString) || StringUtils.equals(dateString, DATE_000000)) {
            LOG.warn("getDateFromStringWithTwoDigitYear, Row has an empty or all-zero date; defaulting to 12/31/2099");
            revisedDateString = DATE_991231;
        }
        return getGregorianCalendar(revisedDateString).getTime();
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
        try (
            FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(fileReader);
        ) {
            parseTextFile(br, records);
        }
    }

    private void parseTextFile(BufferedReader br, List<CheckReconError> records) throws Exception {
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
                    // Set Status to Exception
                    cr.setStatus(CRConstants.EXCP);
                    // Set GL Trans False
                    cr.setGlTransIndicator(Boolean.FALSE);
                    String notFoundSrc   	= getCrImportParameterValueAsString(CRConstants.SRC_NOT_FOUND);
                    String notFoundBankCd   = getCrImportParameterValueAsString(CRConstants.BNK_CD_NOT_FOUND);
                    
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
                            // ==== CU Customization: Only update when previous check status is Issued. ====
                            if (CRConstants.ISSUED.equals(existingRecord.getStatus())) {
                                cr.setStatus(checkStatus);
                                existingRecord.setStatus(checkStatus);
                                existingRecord.setStatusChangeDate(cr.getStatusChangeDate());
                                businessObjectService.save(existingRecord);
                                LOG.info("Updated Check Recon Record : " + existingRecord.getId());
                            } else {
                                LOG.warn("Could not update PDP-sourced Check Recon Record status to " + CRConstants.CLEARED
                                        + " because the existing status is " + existingRecord.getStatus()
                                        + " for this PDP-sourced record : " + existingRecord.getId());
                            }
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
                            // ==== CU Customization: Only update when previous check status is Issued. ====
                            if (CRConstants.ISSUED.equals(existingRecord.getStatus())) {
                                cr.setStatus(checkStatus);
                                existingRecord.setStatus(checkStatus);
                                existingRecord.setStatusChangeDate(cr.getStatusChangeDate());
                                businessObjectService.save(existingRecord);
                                LOG.info("Updated Check Recon Record : " + existingRecord.getId());
                            } else {
                                LOG.warn("Could not update Check Recon Record status to " + CRConstants.CLEARED
                                        + " because the current status is " + existingRecord.getStatus()
                                        + " for this record : " + existingRecord.getId());
                            }

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
        Map<String,Object> fieldValues = new HashMap<String,Object>();
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
        String oldStatus = CRConstants.EXCP;

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
                oldStatus = paymentGroup.getPaymentStatusCode();
                // Update PDP status and save
                KualiCode code = businessObjectService.findBySinglePrimaryKey(PaymentStatus.class, defaultStatus);
                if (paymentGroup.getPaymentStatus() != ((PaymentStatus) code)) {
                    paymentGroup.setPaymentStatus((PaymentStatus) code);
                    paymentGroup.setLastUpdatedTimestamp(new Timestamp(cr.getStatusChangeDate().getTime()));
                }
                
                // Update PDP if the check status is cleared from the bank file
                // ==== CU Customization: If the status is being updated to Cleared, only allow it when the current status is Extracted. ====
                
                if (defaultStatus.equals(CRConstants.CLEARED)) {
                    if (PaymentStatusCodes.EXTRACTED.equals(oldStatus)) {
                	    businessObjectService.save(paymentGroup);
                	    LOG.info("Check Status in the bank file is cleared. Updated Payment Group : " + paymentGroup.getId() + " Disbursement  " + paymentGroup.getDisbursementNbr());
                    } else {
                        LOG.warn("Check Status in the bank file is cleared, but Payment Group " + paymentGroup.getId() + " for Disbursement "
                                + paymentGroup.getDisbursementNbr() + " has a current status of " + oldStatus + " and cannot be cleared.");
                    }
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

    private String getCrImportParameterValueAsString(String parameterName) {
        String fullParameterName = getCrImportParameterPrefix() + parameterName;
        return getParameterService().getParameterValueAsString(
                CheckReconciliationImportStep.class, fullParameterName);
    }

    private boolean getCrImportParameterValueAsBoolean(String parameterName) {
        String fullParameterName = getCrImportParameterPrefix() + parameterName;
        return getParameterService().getParameterValueAsBoolean(
                CheckReconciliationImportStep.class, fullParameterName);
    }

    private String getCrImportParameterPrefix() {
        return StringUtils.trimToEmpty(getParameterService().getParameterValueAsString(
                CheckReconciliationImportStep.class, CRConstants.PARAMETER_PREFIX));
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
    public ConfigurationService getKualiConfigurationService() {
        return kualiConfigurationService;
    }

    /**
     * Set KualiConfigurationService
     * 
     * @param kualiConfigurationService
     */
    public void setKualiConfigurationService(ConfigurationService kualiConfigurationService) {
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
