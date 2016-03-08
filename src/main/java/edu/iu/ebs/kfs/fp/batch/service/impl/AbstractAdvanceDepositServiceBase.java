package edu.iu.ebs.kfs.fp.batch.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.gl.service.impl.StringHelper;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.FlatFileInformation;
import org.kuali.kfs.sys.batch.FlatFileTransactionInformation;
import org.kuali.kfs.sys.batch.PhysicalFlatFileInformation;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.mail.MailMessage;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.web.format.FormatException;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.exception.InvalidAddressException;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.MailService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.iu.ebs.kfs.fp.FinancialProcessingConstants;
import edu.iu.ebs.kfs.fp.FinancialProcessingParameterConstants;
import edu.iu.ebs.kfs.fp.batch.LoadAchIncomeFileStep;
import edu.iu.ebs.kfs.fp.batch.service.AdvanceDepositService;
import edu.iu.ebs.kfs.fp.businessobject.AchIncomeFile;
import edu.iu.ebs.kfs.fp.businessobject.AchIncomeFileGroup;
import edu.iu.ebs.kfs.fp.businessobject.AchIncomeFileTransaction;
import edu.iu.ebs.kfs.fp.businessobject.AchIncomeFileTransactionDateTime;
import edu.iu.ebs.kfs.fp.businessobject.AchIncomeFileTransactionNote;
import edu.iu.ebs.kfs.fp.businessobject.AchIncomeFileTransactionOpenItemReference;
import edu.iu.ebs.kfs.fp.businessobject.AchIncomeFileTransactionPayerOrPayeeName;
import edu.iu.ebs.kfs.fp.businessobject.AchIncomeFileTransactionReference;
import edu.iu.ebs.kfs.fp.businessobject.AchIncomeFileTransactionSet;
import edu.iu.ebs.kfs.fp.businessobject.AchIncomeNote;
import edu.iu.ebs.kfs.fp.businessobject.AchIncomeTransaction;
/**
Copyright Indiana University
   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as
   published by the Free Software Foundation, either version 3 of the
   License, or (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.
   
   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
public abstract class AbstractAdvanceDepositServiceBase implements AdvanceDepositService {
    private static final Logger LOG = Logger.getLogger(AbstractAdvanceDepositServiceBase.class);

    private BatchInputFileService batchInputFileService; 
    private BatchInputFileType batchInputFileType;
    protected DateTimeService dateTimeService;
    protected BusinessObjectService businessObjectService;
    private MailService mailService;
    protected ParameterService parameterService;
    private  ConfigurationService configurationService;

    private static final Pattern REFERENCE_NUMBER_REGEX = Pattern.compile(".*01.?([A-Z]{2}[0-9]{7}).*");


    private int  achTotalPostedTransactions ;
    private int  achTotalSkippedTransactions ;
    private int  wiredTotalPostedTransactions ;
    private int  wiredTotalSkippedTransactions ;
    private KualiDecimal achTotalPostedTransactionAmount ;
    private KualiDecimal achTotalSkippedTransactionAmount ;
    private KualiDecimal wiredTotalPostedTransactionAmount ;
    private KualiDecimal wiredTotalSkippedTransactionAmount ;

    public boolean loadFile() {
        // TODO implement this method - see LockboxLoadServiceImpl.loadFile
        boolean result = true;
        List<PhysicalFlatFileInformation> flatFileInformationList = new ArrayList<PhysicalFlatFileInformation>();
        PhysicalFlatFileInformation physicalflatFileInformation = null;
        List<String> fileNamesToLoad = getListOfFilesToProcess();
        LOG.info("Found " + fileNamesToLoad.size() + " file(s) to process.");

        List<String> processedFiles = new ArrayList<String>();

        for (String inputFileName : fileNamesToLoad) {
            LOG.info("Beginning processing of filename: " + inputFileName + ".");
            physicalflatFileInformation = new PhysicalFlatFileInformation(inputFileName);
            flatFileInformationList.add(physicalflatFileInformation);

            if (loadFile(inputFileName, physicalflatFileInformation)) {
                processedFiles.add(inputFileName);
                physicalflatFileInformation.addFileInfoMessage("File successfully completed processing.");
            }
            else {
                physicalflatFileInformation.addFileErrorMessage("Unable to process file.");

            }

        }

        //  remove done files
        removeDoneFiles(processedFiles);
        sendEmailSummary(flatFileInformationList);

        return result ;
        // get the files from the staging directory, read each file, load data into temp tables, generate messages, send emails, delete physical files
    }

    public boolean loadFile(String fileName,PhysicalFlatFileInformation physicalFlatFileInformation) {
        boolean valid = true;
        //  load up the file into a byte array 
        byte[] fileByteContent = safelyLoadFileBytes(fileName);

        //  parse the file against the configuration define in the spring file and load it into an object
        LOG.info("Attempting to parse the file ");
        Object parsedObject = null;

        try {
            parsedObject = batchInputFileService.parse(batchInputFileType, fileByteContent);
        }
        catch (ParseException e) {
            LOG.error("Error parsing batch file: " + e.getMessage());
            FlatFileInformation fileInformation = new FlatFileInformation();
            fileInformation.addFileInfoMessage("Unable to process file" + StringUtils.substringAfterLast(fileName, "\\") + "." + e.getMessage());
            physicalFlatFileInformation.getFlatFileInfomationList().add(fileInformation);
            return false;
        }

        // validate the parsed data 
        if (parsedObject != null ) {
            valid = validate(parsedObject);
            copyAllMessage(parsedObject,physicalFlatFileInformation);
            if (valid) {
                loadAchIncomeTransactions(parsedObject);
            }            
        }
        
        return valid ;
    }


    // TODO this is where we'll put messages in he GlobalMessages object so they'll be displayed on the screen
    public boolean validate(Object parsedFileContents) {
        boolean valid = true;
        List<AchIncomeFile> achIncomeFiles = (ArrayList<AchIncomeFile>) parsedFileContents;
        for (AchIncomeFile achIncomeFile : achIncomeFiles) {
         
            achTotalPostedTransactions = 0;
            achTotalSkippedTransactions = 0;
            wiredTotalPostedTransactions = 0;
            wiredTotalSkippedTransactions = 0;
            achTotalPostedTransactionAmount = new KualiDecimal(0.00);
            achTotalSkippedTransactionAmount = new KualiDecimal(0.00);
            wiredTotalPostedTransactionAmount = new KualiDecimal(0.00);
            wiredTotalSkippedTransactionAmount = new KualiDecimal(0.00);
            String payerMessage = "";
            
            //verify that the we are processing correct file in correct environment.
            String productionEnvironmentCode = configurationService.getPropertyValueAsString(KFSConstants.PROD_ENVIRONMENT_CODE_KEY);
            String environmentCode = configurationService.getPropertyValueAsString(KFSConstants.ENVIRONMENT_KEY);
            if (StringUtils.equals(productionEnvironmentCode, environmentCode)
                    &&!StringUtils.equals(productionEnvironmentCode, achIncomeFile.getProductionOrTestIndicator()) ) {
                String message = "Invalid file: Unable to process test file in production" ;
                LOG.error("Error while validating the Ach Income file" + message);
                GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,KFSKeyConstants.ERROR_CUSTOM, message);
                valid = false;
            }

            // verify there is an ISA and IEA record for each logical file
            if(achIncomeFile.getTrailer()== null) {
                String message = "No logical file trailer found for file header :" + achIncomeFile.getInterchangeControlNumber() ;
                LOG.error("Error while validating the Ach Income file" + message);
                GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,KFSKeyConstants.ERROR_CUSTOM, message);
                valid = false;
            }
            else {
                int totalGroups = achIncomeFile.getTrailer().getTotalGroups();
                if (totalGroups != achIncomeFile.getGroups().size()) {
                    String message = "The group count on the file trailer," + totalGroups + "," 
                    + "does not match the number of groups," + achIncomeFile.getGroups().size()
                    + ",in the file:" + achIncomeFile.getInterchangeControlNumber();  
                    LOG.error("Error while validating the Ach Income file" + message);
                    GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,KFSKeyConstants.ERROR_CUSTOM, message);
                    valid = false;
                }

                // verify that logical file control numbers on the ISA and IEA match
                if (!achIncomeFile.getInterchangeControlNumber().equals(achIncomeFile.getTrailer().getInterchangeControlNumber())) {
                    String message = "Cannot match logical file header to file trailer for file: ISA Control Number [ " + achIncomeFile.getInterchangeControlNumber() + "] " 
                    + "IEA Control Number [ " + achIncomeFile.getTrailer().getInterchangeControlNumber() + "]"; 
                    LOG.error("Error while validating the Ach Income file" + message);
                    GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,KFSKeyConstants.ERROR_CUSTOM, message);
                    valid = false;
                }
            }

            List<AchIncomeFileGroup> groups  = achIncomeFile.getGroups();
            for (AchIncomeFileGroup  achIncomeFileGroup :  achIncomeFile.getGroups()) {
                // verify there is an GS and GE record for each logical file

                if(achIncomeFileGroup.getGroupTrailer() == null ) {
                    String message = "No group trailer found for group : " +  achIncomeFileGroup.getGroupControlNumber();
                    
                    LOG.error("Error while validating the Ach Income file" + message);
                    GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,KFSKeyConstants.ERROR_CUSTOM, message);
                    valid = false;
                }
                else {
                    int totalTransactionSets = achIncomeFileGroup.getGroupTrailer().getTotalTransactionSets();
                    // verfiy  the group trailer transactionSets count matches the numbers of transactions
                    if (totalTransactionSets != achIncomeFileGroup.getTransactionSet().size()) {
                        String message = "The transaction count on the group trailer," + totalTransactionSets + "," 
                        + "does not match the number of transactions," + achIncomeFileGroup.getGroupTrailer().getTotalTransactionSets()
                        + ",in the group:" + achIncomeFileGroup.getGroupControlNumber();  
                        
                        LOG.error("Error while validating the Ach Income file" + message);
                        GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,KFSKeyConstants.ERROR_CUSTOM, message);
                        valid = false;
                    }
                    // verify that group control number on GS and GE match 
                    if (!achIncomeFileGroup.getGroupControlNumber().equals(achIncomeFileGroup.getGroupTrailer().getGroupControlNumber())) {
                        String message = "Cannot match group header to group trailer for group: GS Control Number [ " + achIncomeFileGroup.getGroupControlNumber() + "] " 
                        + "IEA Control Number [ " + achIncomeFileGroup.getGroupTrailer().getGroupControlNumber() + "]";
                        
                        LOG.error("Error while validating the Ach Income file" + message);
                        GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,KFSKeyConstants.ERROR_CUSTOM, message);
                        valid = false;
                    }
                }

                //verify RA is on the functional identifier code on the GS record
                if (!FinancialProcessingConstants.AchIncomeFileGroup.GROUP_FUNCTIONAL_IDENTIFER_CD_RA.equals(achIncomeFileGroup.getGroupFunctionIdentifierCode())) {
                    String message = "The Functional Identifier Code is not " + FinancialProcessingConstants.AchIncomeFileGroup.GROUP_FUNCTIONAL_IDENTIFER_CD_RA 
                    + " for group: " + achIncomeFileGroup.getGroupControlNumber() + "-" + achIncomeFileGroup.getGroupFunctionIdentifierCode();
                    
                    achIncomeFile.getFlatFileTransactionInformation().addWarnMessage(message);
                    GlobalVariables.getMessageMap().putWarning(KFSConstants.GLOBAL_ERRORS,KFSKeyConstants.ERROR_CUSTOM, message);
                }

                List<AchIncomeFileTransactionSet> achIncomeFileTransactionSets = achIncomeFileGroup.getTransactionSet();
                for (AchIncomeFileTransactionSet achIncomeFileTransactionSet : achIncomeFileTransactionSets) {
                    // verify there is an ST and SE record for each logical file
                    if (achIncomeFileTransactionSet.getTransactionSetTrailer() == null ) {
                        String message = "No transaction trailer found for transaction : " + achIncomeFileTransactionSet.getTransactionSetControlNumber();
                        
                        LOG.error("Error while validating the Ach Income file" + message);
                        GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,KFSKeyConstants.ERROR_CUSTOM, message);
                        valid = false;
                    }
                    else {
                        // verify that the transaction set control number on SE and ST match
                        if (!achIncomeFileTransactionSet.getTransactionSetControlNumber().
                                equals(achIncomeFileTransactionSet.getTransactionSetTrailer().getTransactionSetControlNumber())) {
                            String message = "Cannot match transaction header to transaction trailer for transaction set: ST Control Number [ " + achIncomeFileTransactionSet.getTransactionSetControlNumber() + "] " 
                            + "SE Control Number [ " + achIncomeFileTransactionSet.getTransactionSetTrailer().getTransactionSetControlNumber()+ "]"; 

                            LOG.error("Error while validating the Ach Income file" + message);
                            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,KFSKeyConstants.ERROR_CUSTOM, message);
                            valid = false;
                        }
                    } 
                    
                    // loop through the AchTransaction to get the total transaction count and amount 
                    // for ACH and wired posted and skipped transactions
                    setAchIncomeTransactionCountsAndAmounts(achIncomeFileTransactionSet.getTransactionGuts());     
                    
                    // validatePayerName 
                    payerMessage +=  validatePayerName(achIncomeFileTransactionSet.getTransactionGuts());                    
                }
            }            

            // FSKD-5472 add extra validation on achIncomeFile date/time, in case parse/format exception might happen later on               
            try {
                getFormattedTimestamp(achIncomeFile, "fileDate/Time");
            }
            catch (FormatException e) {                
                LOG.error("Error while validating the ACH Income file: " + e.getMessage());
                GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_CUSTOM, e.getMessage());
                valid = false;
            }

            String messageText = getEmailMessageText(achIncomeFile, payerMessage );
            achIncomeFile.setEmailMessageText(messageText) ;  
        }

        LOG.info("validate method executing");
        return valid;
    }


    public void sendEmailSummary(List<PhysicalFlatFileInformation> flatFileInformationList) {

        for (PhysicalFlatFileInformation physicalFlatFileInformation : flatFileInformationList) {
            List<FlatFileInformation> fileInformations = physicalFlatFileInformation.getFlatFileInfomationList();
            for(FlatFileInformation fileInformation : fileInformations) {
                sendEmail(fileInformation);
            }

        }
    }
    
    public void sendFailureEmail(PhysicalFlatFileInformation physicalFlatFileInformation) {
            List<FlatFileInformation> fileInformations = physicalFlatFileInformation.getFlatFileInfomationList();
            for(FlatFileInformation fileInformation : fileInformations) {
                sendEmail(fileInformation);
            }
    }

    private void sendEmail(FlatFileInformation fileInformation) {
        LOG.debug("sendEmail() starting");
        MailMessage message = new MailMessage();

        String returnAddress = parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL, FinancialProcessingConstants.BATCH_DETAIL_TYPE, FinancialProcessingConstants.IU_FROM_EMAIL_ADDRESS_PARM_NM);
        if(StringUtils.isEmpty(returnAddress)) {
            returnAddress = mailService.getBatchMailingList();
        }
        message.setFromAddress(returnAddress);
        String subject = parameterService.getParameterValueAsString(LoadAchIncomeFileStep.class, FinancialProcessingParameterConstants.AchIncome.ACH_INCOME_SUMMARY_EMAIL_SUBJECT);

        message.setSubject(subject);
        List<String> toAddressList = new ArrayList<String>( parameterService.getParameterValuesAsString(LoadAchIncomeFileStep.class, FinancialProcessingParameterConstants.AchIncome.ACH_INCOME_SUMMARY_TO_EMAIL_ADDRESSES) );
        message.getToAddresses().addAll(toAddressList);
        String body = composeAchIncomeSummaryEmailBody(fileInformation);
        message.setMessage(body);

        try {
            mailService.sendMessage(message);
        }
        catch (InvalidAddressException e) {
            LOG.error("sendErrorEmail() Invalid email address. Message not sent", e);
        }
        catch (MessagingException me) {
            throw new RuntimeException("Could not send mail", me);
        }

    }

    private String composeAchIncomeSummaryEmailBody( FlatFileInformation flatFileInformation) {
        StringBuffer body = new StringBuffer();

        for(String[] resultMessage : flatFileInformation.getMessages()) {
            body.append(resultMessage[1]);
            body.append("\n");
        }
        for(Object object  : flatFileInformation.getFlatFileIdentifierToTransactionInfomationMap().values()) {
            for (String[] message : ((FlatFileTransactionInformation)object).getMessages()) {
                body.append(message[1]);
                body.append("\n");
            }
        }

        return body.toString();
    }

    protected List<String> getListOfFilesToProcess() {

        //  create a list of the files to process
        List<String> fileNamesToLoad = batchInputFileService.listInputFileNamesWithDoneFile(batchInputFileType);

        if (fileNamesToLoad == null) {
            LOG.error("BatchInputFileService.listInputFileNamesWithDoneFile(" + 
                    batchInputFileType.getFileTypeIdentifer() + ") returned NULL which should never happen.");
            throw new RuntimeException("BatchInputFileService.listInputFileNamesWithDoneFile(" + 
                    batchInputFileType.getFileTypeIdentifer() + ") returned NULL which should never happen.");
        }

        //  filenames returned should never be blank/empty/null
        for (String inputFileName : fileNamesToLoad) {
            if (StringUtils.isBlank(inputFileName)) {
                LOG.error("One of the file names returned as ready to process [" + inputFileName + 
                "] was blank.  This should not happen, so throwing an error to investigate.");
                throw new RuntimeException("One of the file names returned as ready to process [" + inputFileName + 
                "] was blank.  This should not happen, so throwing an error to investigate.");
            }
        }

        return fileNamesToLoad;
    }


    /**
     * 
     * Accepts a file name and returns a byte-array of the file name contents, if possible.
     * 
     * Throws RuntimeExceptions if FileNotFound or IOExceptions occur.
     * 
     * @param fileName String containing valid path & filename (relative or absolute) of file to load.
     * @return A Byte Array of the contents of the file.
     */
    protected byte[] safelyLoadFileBytes(String fileName) {

        InputStream fileContents;
        byte[] fileByteContent;
        try {
            fileContents = new FileInputStream(fileName);
        }
        catch (FileNotFoundException e1) {
            LOG.error("Batch file not found [" + fileName + "]. " + e1.getMessage());
            throw new RuntimeException("Batch File not found [" + fileName + "]. " + e1.getMessage());
        }
        try {
            fileByteContent = IOUtils.toByteArray(fileContents);
        }
        catch (IOException e1) {
            LOG.error("IO Exception loading: [" + fileName + "]. " + e1.getMessage());
            throw new RuntimeException("IO Exception loading: [" + fileName + "]. " + e1.getMessage());
        }
        return fileByteContent;
    }

    /**
     * Clears out associated .done files for the processed data files.
     */
    protected void removeDoneFiles(List<String> dataFileNames) {
        for (String dataFileName : dataFileNames) {
            File doneFile = new File(StringUtils.substringBeforeLast(dataFileName, ".") + ".done");
            if (doneFile.exists()) {
                doneFile.delete();
            }
        }
    }

    private void loadAchIncomeTransactions(Object parsedObject) {
        List<AchIncomeTransaction> achIncomeTransactions = new ArrayList();
        List<AchIncomeFile> achIncomeFiles = (List<AchIncomeFile>)parsedObject;
        for (AchIncomeFile achIncomeFile : achIncomeFiles) {
            setAchTransactions(achIncomeFile,achIncomeTransactions);
        }

        saveTransaction(achIncomeTransactions);
    }
    
    private void saveTransaction(List achIncomeFileTransactions) {
        businessObjectService.save(achIncomeFileTransactions);

    }
    
    /**
     * Calls businessObjectService to remove all the ach Income transaction rows from the transaction load table.
     */
    public void cleanTransactionsTable() {
        businessObjectService.deleteMatching(AchIncomeTransaction.class, new HashMap());
    }


    private void setAchTransactions(AchIncomeFile achIncomeFile, List achIncomeTransactions) {
        // FSKD-5472 take formatting of bankTimestamp out of the loop       
        Timestamp bankTimestamp = getFormattedTimestamp(achIncomeFile, "fileDate/time" );
        List<AchIncomeFileGroup> groups = achIncomeFile.getGroups();
        List<String> payerNames = new ArrayList<String>( parameterService.getParameterValuesAsString(LoadAchIncomeFileStep.class, FinancialProcessingParameterConstants.AchIncome.ACH_INCOME_PAYER_NAMES) );

        for (AchIncomeFileGroup  achIncomeFileGroup :  achIncomeFile.getGroups()) {
            List<AchIncomeFileTransactionSet> achIncomeFileTransactionSets = achIncomeFileGroup.getTransactionSet();
            for (AchIncomeFileTransactionSet achIncomeFileTransactionSet : achIncomeFileTransactionSets) {
                List<AchIncomeFileTransaction> achIncomeFileTransactions = achIncomeFileTransactionSet.getTransactionGuts();
                for (AchIncomeFileTransaction achIncomeFileTransaction : achIncomeFileTransactions) {
                    boolean isSkippedTransaction = false;
                    String payerName  = "";
                    
                    payerName = isPayerIndianaUniversity(achIncomeFileTransaction);
                    if(FinancialProcessingConstants.AchIncomeFileTransaction.DEBIT_TRANS_IND.equals(achIncomeFileTransaction.getCreditDebitIndicator())
                            ||ObjectUtils.isNull(payerName) ) {
                        isSkippedTransaction = true;
                    }
                    
                    if (!isSkippedTransaction) {

                        AchIncomeTransaction achIncomeTransaction = new AchIncomeTransaction();
                        // payment method code
                        achIncomeTransaction.setPaymentMethodCode(achIncomeFileTransaction.getPaymentMethodCode());
                        // effectiveDate 
                        achIncomeTransaction.setEffectiveDate(achIncomeFileTransaction.getEffectiveDate());
                        // loadTimestamp
                        achIncomeTransaction.setLoadTimestamp(dateTimeService.getCurrentTimestamp());
                        //bankTimestamp
                        achIncomeTransaction.setBankTimestamp(bankTimestamp);

                        //transaction amount
                        achIncomeTransaction.setTransactionAmount(achIncomeFileTransaction.getTransactionAmount());

                        // payer Name
                        if(payerName.length() > FinancialProcessingConstants.AchIncomeFileTransactionPayerOrPayeeName.ACH_TRN_PAYR_NM_DB_SIZE) {
                            payerName = payerName.substring(0,40);
                        }
                        achIncomeTransaction.setPayerName(payerName);

                        // reference Number type RMR = IV
                        List<AchIncomeFileTransactionOpenItemReference> openItemReferences = achIncomeFileTransaction.getOpenItemReferences();
                        for(AchIncomeFileTransactionOpenItemReference openItemReference : openItemReferences) {
                            if(FinancialProcessingConstants.AchIncomeFileTransactionOpenItemReference.RMR_REFERENCE_TYPE_IV.equals(openItemReference.getType())) {
                                String referenceNumber  = openItemReference.getValue();
                                Matcher matcher = REFERENCE_NUMBER_REGEX.matcher(referenceNumber);
                                if (matcher.find()) {
                                    achIncomeTransaction.setReferenceNumber(matcher.group(matcher.groupCount()));
                                }
                                
                            }
                        }

                        // trace number from TRN or REF line
                        if (achIncomeFileTransaction.getTrace()!= null) {
                            achIncomeTransaction.setTraceNumber(achIncomeFileTransaction.getTrace().getTraceNumber());

                        }
                        else {
                            List<AchIncomeFileTransactionReference> achIncomeFileTransactionReferences = achIncomeFileTransaction.getReferences();
                            for (AchIncomeFileTransactionReference achIncomeFileTransactionReference : achIncomeFileTransactionReferences) {
                                if(FinancialProcessingConstants.AchIncomeFileTransactionReference.REF_REFERENCE_TYPE_TN.equals(achIncomeFileTransactionReference.getType())) {
                                    achIncomeTransaction.setTraceNumber(achIncomeFileTransactionReference.getValue());

                                }
                            }

                        }

                        // create note lines
                        List<String> notes = createNotes(achIncomeFileTransaction);

                        List<AchIncomeNote> achIncomeNotes = new ArrayList<AchIncomeNote>();
                        int noteLineNumber = 1;
                        for (String noteText : notes) {
                            AchIncomeNote   achIncomeNote = new AchIncomeNote();
                            achIncomeNote.setNoteLineNumber(noteLineNumber);
                            achIncomeNote.setNoteText(noteText);
                            achIncomeNotes.add(achIncomeNote);
                            noteLineNumber++;
                        }

                        achIncomeTransaction.setNotes(achIncomeNotes);
                        achIncomeTransactions.add(achIncomeTransaction);
                    }
                }
            }
        }

    }
    


    private List<String> createNotes(AchIncomeFileTransaction achIncomeFileTransaction) {

        StringBuffer notes = addPaymentInformationToNotes(achIncomeFileTransaction);
        List<String> achNotes = addRMRAndNTELinesToNotes(achIncomeFileTransaction, notes);

        return achNotes;
    }

    private StringBuffer addPaymentInformationToNotes(AchIncomeFileTransaction achIncomeFileTransaction ) {

        StringBuffer notes = new StringBuffer();

        if(FinancialProcessingConstants.AchIncomeFileTransaction.TRANS_PAYMENT_METHOD_ACH.equals(achIncomeFileTransaction.getPaymentMethodCode())) {
            notes.append("ACH PAYMENT INFORMATION: ");
        }

        if(FinancialProcessingConstants.AchIncomeFileTransaction.TRANS_PAYMENT_METHOD_FWT.equals(achIncomeFileTransaction.getPaymentMethodCode())) {
            notes.append("WIRE PAYMENT INFORMATION: ");
        }
        notes.append("\n");

        //Credit amount    

        notes.append("CREDIT: ");
        notes.append("$" + getFormattedAmount("##,##,###.00", achIncomeFileTransaction.getTransactionAmount()) +  " " + achIncomeFileTransaction.getCreditDebitIndicator()  + "\n");

        //Effective Date  
        notes.append("EFFECTIVE DATE: ");
        notes.append(achIncomeFileTransaction.getEffectiveDate()+ "\n");

        //Company Id 
        notes.append("COMPANY ID: ");
        notes.append(achIncomeFileTransaction.getCompanyId()+ "\n");

        //Trace Number 
        if (achIncomeFileTransaction.getTrace()!= null) {
            notes.append("TRACE NUMBER: ");
            notes.append(achIncomeFileTransaction.getTrace().getTraceNumber() + "\n");
        }

        // Reference number from REF record [ TYPE:TN,CT,VV]
        List<AchIncomeFileTransactionReference> fileTransactionReferences = achIncomeFileTransaction.getReferences();
        for(AchIncomeFileTransactionReference fileTransactionReference : fileTransactionReferences) {
            if (FinancialProcessingConstants.AchIncomeFileTransactionReference.REF_REFERENCE_TYPE_TN.equals(fileTransactionReference.getType())) {
                notes.append("TRANSACTION NUMBER: ");
                notes.append(fileTransactionReference.getValue() + "\n");
            }

            if (FinancialProcessingConstants.AchIncomeFileTransactionReference.REF_REFERENCE_TYPE_CT.equals(fileTransactionReference.getType())) {
                notes.append("CONTRACT NUMBER: ");
                notes.append(fileTransactionReference.getValue() + "\n");
            }

            if (FinancialProcessingConstants.AchIncomeFileTransactionReference.REF_REFERENCE_TYPE_VV.equals(fileTransactionReference.getType())) {
                notes.append("VOUCHER NUMBER: ");
                notes.append(fileTransactionReference.getValue() + "\n");
            }
        }

        // Transaction creation
        List<AchIncomeFileTransactionDateTime> transactionDateTimes =  achIncomeFileTransaction.getDateTimes();
        for (AchIncomeFileTransactionDateTime transactionDateTime : transactionDateTimes ) {
            if (FinancialProcessingConstants.AchIncomeFileTransactionDateTime.DTM_DATE_TYPE_097.equals(transactionDateTime.getType())) {
                notes.append("TRANSACTION CREATION: ");
                notes.append(transactionDateTime.getValue() + "\n");
            }
        }

        String payerNoteText = "";
        String receiverNoteText = "";

        // Receiver and Originator
        List<AchIncomeFileTransactionPayerOrPayeeName> payerOrPayeeNames = achIncomeFileTransaction.getPayerOrPayees();
        for (AchIncomeFileTransactionPayerOrPayeeName payerOrPayeeName : payerOrPayeeNames) {
            if(FinancialProcessingConstants.AchIncomeFileTransactionPayerOrPayeeName.PAYER_TYPE_PE.equals(payerOrPayeeName.getType())) {
                payerNoteText =payerOrPayeeName.getValue(); 
                if(payerOrPayeeName.getIdCode() != null && 
                        payerOrPayeeName.getIdQualifier() != null ) {
                    payerNoteText = payerNoteText + " " + payerOrPayeeName.getIdCode() + ":" + payerOrPayeeName.getIdQualifier();
                }
            }

            if(FinancialProcessingConstants.AchIncomeFileTransactionPayerOrPayeeName.PAYER_TYPE_PR.equals(payerOrPayeeName.getType())) {
                receiverNoteText = payerOrPayeeName.getValue() ;
            }
        }
        if (StringHelper.isEmpty(payerNoteText) &&
                achIncomeFileTransaction.getPremiumAdminsContact()!= null ){
            payerNoteText = achIncomeFileTransaction.getPremiumAdminsContact().getValue();
        }

        if (StringHelper.isEmpty(receiverNoteText) && 
                achIncomeFileTransaction.getPremiumReceiverName() != null) {
            receiverNoteText = achIncomeFileTransaction.getPremiumReceiverName().getValue();
        }

        //receiver
        if (!StringHelper.isEmpty(payerNoteText)) {
            notes.append("RECEIVER: ");
            notes.append(payerNoteText);
            notes.append("\n");
        }

        // Originator
        if (!StringHelper.isEmpty(receiverNoteText)) {
            notes.append("ORIGINATOR: ");
            notes.append(receiverNoteText);
            notes.append("\n");
        }

        return notes;

    }

    private List<String> addRMRAndNTELinesToNotes(AchIncomeFileTransaction achIncomeFileTransaction , StringBuffer notes ) {
        List<String> achNotes = new ArrayList();

        int maxNoteSize = 800;
        boolean header = true;
        Integer lineCount = 1;
        boolean foundRMRLine = false;
        boolean foundNTELine = false;
        //adding RMR lines to notes

        List<AchIncomeFileTransactionOpenItemReference> openItemReferences = achIncomeFileTransaction.getOpenItemReferences();
        for(AchIncomeFileTransactionOpenItemReference openItemReference : openItemReferences ) {
            int leftNoteSize = maxNoteSize - notes.length();
            if((notes.length() >= maxNoteSize)|| (leftNoteSize <= 80 ) ) {
                // went over the note quota ,add notes to the list and  creating new note string 
                achNotes.add(notes.toString());
                notes = new StringBuffer();
                header = true;
            }
                String type = openItemReference.getType();
                if(FinancialProcessingConstants.AchIncomeFileTransactionOpenItemReference.RMR_REFERENCE_TYPE_CR.equals(type) ||
                		FinancialProcessingConstants.AchIncomeFileTransactionOpenItemReference.RMR_REFERENCE_TYPE_IV.equals(type) ||
                		FinancialProcessingConstants.AchIncomeFileTransactionOpenItemReference.RMR_REFERENCE_TYPE_OI.equals(type) ) {
                    foundRMRLine = true;
                    if(header) {
                        notes.append(StringUtils.rightPad("LINE",FinancialProcessingConstants.AchIncomeFileTransactionOpenItemReference.RMR_NOTE_LINE_COLUMN_WIDTH));
                        notes.append(StringUtils.rightPad("INVOICE NUMBER",FinancialProcessingConstants.AchIncomeFileTransactionOpenItemReference.RMR_NOTE_INVOICE_NUMBER_COLUMN_WIDTH ));
                        notes.append(StringUtils.rightPad("NETAMOUNT PAID",FinancialProcessingConstants.AchIncomeFileTransactionOpenItemReference.RMR_NOTE_NET_AMOUNT_COLUMN_WIDTH ));
                        notes.append(StringUtils.rightPad("INVOICE AMOUNT",FinancialProcessingConstants.AchIncomeFileTransactionOpenItemReference.RMR_NOTE_INVOICE_AMOUNT_COLUMN_WIDTH ));
                        notes.append("\n");
                        header = false;
                    }

                    notes.append(StringUtils.rightPad(getFormattedAmount("00000",lineCount) ,FinancialProcessingConstants.AchIncomeFileTransactionOpenItemReference.RMR_NOTE_LINE_COLUMN_WIDTH));
                    notes.append(StringUtils.rightPad(openItemReference.getValue() ,FinancialProcessingConstants.AchIncomeFileTransactionOpenItemReference.RMR_NOTE_INVOICE_NUMBER_COLUMN_WIDTH));
                    if(openItemReference.getNetAmount() != null ) {
                        notes.append(StringUtils.leftPad(getFormattedAmount("##,##,###.00",openItemReference.getNetAmount()),FinancialProcessingConstants.AchIncomeFileTransactionOpenItemReference.RMR_NOTE_NET_AMOUNT_COLUMN_WIDTH));                        
                        
                    }
                    if(openItemReference.getInvoiceAmount()!= null ) {
                        notes.append(StringUtils.leftPad(getFormattedAmount("##,##,###.00",openItemReference.getInvoiceAmount()) ,FinancialProcessingConstants.AchIncomeFileTransactionOpenItemReference.RMR_NOTE_INVOICE_AMOUNT_COLUMN_WIDTH));
                    }
                    notes.append("\n");
                    lineCount++;
                }
        }

        List<AchIncomeFileTransactionNote> fileTransactionNotes = achIncomeFileTransaction.getNotes();
        for(AchIncomeFileTransactionNote fileTransactionNote : fileTransactionNotes) {
            foundNTELine = true;
         
            if (fileTransactionNote.getType() != null) {
                String nteTxt = fileTransactionNote.getType().toUpperCase()+ ": " + fileTransactionNote.getValue();
                //check notes quota
                
                int notesPlusNoteTxtLength = nteTxt.length() + notes.length();
                if(notesPlusNoteTxtLength >= maxNoteSize ) {
                    // went over the note quota , add to list and create new notes
                    achNotes.add(notes.toString());
                    notes = new StringBuffer();
                }
                notes.append(nteTxt.trim());
                notes.append("\n");
            }

        }
        achNotes.add(notes.toString());


        return achNotes;
    }

    private void  setAchIncomeTransactionCountsAndAmounts(List<AchIncomeFileTransaction> achIncomeFileTransaction) {
      
      for (AchIncomeFileTransaction transaction : achIncomeFileTransaction) {
            boolean isSkippedTransaction = false;
            
            if(FinancialProcessingConstants.AchIncomeFileTransaction.DEBIT_TRANS_IND.equals(transaction.getCreditDebitIndicator())
                    || StringHelper.isEmpty(isPayerIndianaUniversity(transaction))) {
                isSkippedTransaction = true;
            }

            if(FinancialProcessingConstants.AchIncomeFileTransaction.TRANS_PAYMENT_METHOD_ACH.equals(transaction.getPaymentMethodCode())){
                if(isSkippedTransaction) {
                    achTotalSkippedTransactions ++;
                    achTotalSkippedTransactionAmount = achTotalSkippedTransactionAmount.add(transaction.getTransactionAmount());
                }
                else {
                    achTotalPostedTransactions ++;
                    achTotalPostedTransactionAmount = achTotalPostedTransactionAmount.add(transaction.getTransactionAmount());
                }
            }
            else if(FinancialProcessingConstants.AchIncomeFileTransaction.TRANS_PAYMENT_METHOD_FWT.equals(transaction.getPaymentMethodCode())){
                if(isSkippedTransaction) {
                    wiredTotalSkippedTransactions ++;
                    wiredTotalSkippedTransactionAmount = wiredTotalSkippedTransactionAmount.add(transaction.getTransactionAmount());
                }
                else {
                    wiredTotalPostedTransactions ++;
                    wiredTotalPostedTransactionAmount = wiredTotalPostedTransactionAmount.add(transaction.getTransactionAmount());
                }
            }
        }
    }

    private String validatePayerName(List<AchIncomeFileTransaction> achIncomeFileTransaction) {
    	StringBuffer payerMessage = new StringBuffer();
    	
    	for (AchIncomeFileTransaction transaction : achIncomeFileTransaction) {
    		String payerName = isPayerIndianaUniversity(transaction);
    		if(StringHelper.isEmpty(payerName)) {
    			payerMessage.append("Payer Name is not found for transaction amount $" + getFormattedAmount("##,##,##0.00", transaction.getTransactionAmount()) +  " [ Date: " + transaction.getEffectiveDate() + " ] ");
    			payerMessage.append("\n");
    		}
    	}

    	return payerMessage.toString();
    }
    
    
    private String getEmailMessageText(AchIncomeFile achIncomeFile, String payerMessage ) {
        StringBuffer message = new StringBuffer();

        int totalPostedTransation = achTotalPostedTransactions + wiredTotalPostedTransactions;
        KualiDecimal totalPostedTransactionAmount = achTotalPostedTransactionAmount.add(wiredTotalPostedTransactionAmount);
        // FSKD-5472 
        String fileDateTime = "";   
        try {
            fileDateTime = getFormattedTimestamp(achIncomeFile, "fileDate/Time").toString();
        }
        catch (FormatException e) {          
            // use the original file Date/Time string if encountered invalid format
            fileDateTime = achIncomeFile.getFileDate() + " " + achIncomeFile.getFileTime();   
        }
        message.append("File Date: " + fileDateTime);
        message.append("\n");
        message.append("                    ");
        message.append("COUNT               ");
        message.append("        AMOUNT     ");
        message.append("\n");
        message.append(StringUtils.rightPad("ACH Posted",20));
        message.append(StringUtils.rightPad(achTotalPostedTransactions+ "",20)); 
        message.append(StringUtils.leftPad(getFormattedAmount("##,##,##0.00", achTotalPostedTransactionAmount),20));		
        message.append("\n");
        message.append(StringUtils.rightPad("Wire Posted",20));
        message.append(StringUtils.rightPad(wiredTotalPostedTransactions+ "",20)); 
        message.append(StringUtils.leftPad(getFormattedAmount("##,##,##0.00", wiredTotalPostedTransactionAmount),20));        
        message.append("\n");
        message.append(StringUtils.rightPad("Total Posted",20));
        message.append(StringUtils.rightPad(totalPostedTransation+ "",20)); 
        message.append(StringUtils.leftPad(getFormattedAmount("##,##,##0.00", totalPostedTransactionAmount),20));
        message.append("\n");
        message.append("\n");
        message.append(StringUtils.rightPad("ACH Skipped",20));
        message.append(StringUtils.rightPad(achTotalSkippedTransactions+ "",20)); 
        message.append(StringUtils.leftPad(getFormattedAmount("##,##,##0.00", achTotalSkippedTransactionAmount),20));        
        message.append("\n");
        message.append(StringUtils.rightPad("Wire Skipped",20));
        message.append(StringUtils.rightPad(wiredTotalSkippedTransactions+ "",20)); 
        message.append(StringUtils.leftPad(getFormattedAmount("##,##,##0.00", wiredTotalSkippedTransactionAmount),20));        
        message.append("\n");
        if(!StringHelper.isEmpty(payerMessage)){
        	message.append("\n");
        	message.append("Transactions Missing Payer Name: ");
        	message.append("\n");
        	message.append(payerMessage);
        }
        return message.toString();
    }


    private Timestamp getFormattedTimestamp(AchIncomeFile achIncomeFile, String fieldName) {
        // FSKD-5472 
        
        // to simplify, use the original file date/time to format        
        String fileDateTime = achIncomeFile.getFileDate() + achIncomeFile.getFileTime();

        // need to use 24 hour format, since otherwise exception will be thrown if the time falls in PM range.
        SimpleDateFormat dateFormat = new SimpleDateFormat(FinancialProcessingConstants.ACH_INCOME_FILE_DATE_FORMAT);
        dateFormat.setLenient(false);
        
        try {
            java.util.Date parsedDate = dateFormat.parse(fileDateTime);
            return new Timestamp(parsedDate.getTime());
        }
        catch (java.text.ParseException e) {
            throw new FormatException( fieldName + " must be of the format " + FinancialProcessingConstants.ACH_INCOME_FILE_DATE_FORMAT + "\n" + e);
        }
    }

    private void copyAllMessage(Object parsedObject, PhysicalFlatFileInformation physicalFlatFileInformation) {
        List<AchIncomeFile> achIncomeFiles = (List<AchIncomeFile>)parsedObject;
        for (AchIncomeFile achIncomeFile : achIncomeFiles) {
            FlatFileInformation fileInformation = new FlatFileInformation();
            FlatFileTransactionInformation information = achIncomeFile.getFlatFileTransactionInformation();
            fileInformation.getOrAddFlatFileData(achIncomeFile.getInterchangeControlNumber(), information);
            fileInformation.addFileInfoMessage(achIncomeFile.getEmailMessageText());
            physicalFlatFileInformation.getFlatFileInfomationList().add(fileInformation);
        }
    }
    
    private String isPayerIndianaUniversity(AchIncomeFileTransaction achIncomeFileTransaction) {
        boolean isPayerIndianaUniversity = false;
        String payerName = null ;
        List<String> payerNames = new ArrayList<String>( parameterService.getParameterValuesAsString(LoadAchIncomeFileStep.class, FinancialProcessingParameterConstants.AchIncome.ACH_INCOME_PAYER_NAMES) );
        List<AchIncomeFileTransactionPayerOrPayeeName>  payerOrPayeeNames = achIncomeFileTransaction.getPayerOrPayees();
        
        for (AchIncomeFileTransactionPayerOrPayeeName payerOrPayeeName : payerOrPayeeNames) {
            if(FinancialProcessingConstants.AchIncomeFileTransactionPayerOrPayeeName.PAYER_TYPE_PR.equals(payerOrPayeeName.getType())) {
            	if(payerOrPayeeName.getValue() != null ) {
                payerName = payerOrPayeeName.getValue().toUpperCase();
            }
            	else {
            		payerName = "";
        }
            }
        }
        
        if(StringHelper.isNullOrEmpty(payerName)
                && achIncomeFileTransaction.getPremiumReceiverName() != null ){
            payerName = achIncomeFileTransaction.getPremiumReceiverName().getValue();
        }
        
        for(String string : payerNames) {
            if(StringUtils.containsIgnoreCase(payerName, string)){
                isPayerIndianaUniversity = true;
                break;
            }
        }
        
        if(!isPayerIndianaUniversity) {
            return payerName;
        }
        
         return null;
    }

    private String getFormattedAmount(String pattern, KualiDecimal amount) {
        DecimalFormat formatter =  new DecimalFormat(pattern);
        return formatter.format(amount);
    }

    private String getFormattedAmount(String pattern, Integer value) {
        DecimalFormat formatter =  new DecimalFormat(pattern);
        return formatter.format(value);
    }

    
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void process(String fileName, Object parsedFileContents) {
        // nothing to do
    }
    
    
    public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifier) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public void setBatchInputFileType(BatchInputFileType batchInputFileType) {
        this.batchInputFileType = batchInputFileType;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }



}
