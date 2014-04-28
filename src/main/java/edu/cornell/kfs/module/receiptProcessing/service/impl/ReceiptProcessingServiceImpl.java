/**
 * @author cab379
 */
package edu.cornell.kfs.module.receiptProcessing.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.service.PersonService;
import org.kuali.rice.kns.bo.Attachment;
import org.kuali.rice.kns.bo.Note;
import org.kuali.rice.kns.service.AttachmentService;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.NoteService;
import org.kuali.rice.kns.util.KNSConstants;

import edu.cornell.kfs.fp.dataaccess.ProcurementCardDocumentDao;
import edu.cornell.kfs.module.receiptProcessing.businessobject.ReceiptProcessing;
import edu.cornell.kfs.module.receiptProcessing.service.ReceiptProcessingService;

public class ReceiptProcessingServiceImpl implements ReceiptProcessingService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ReceiptProcessingServiceImpl.class);
    
    private BatchInputFileService batchInputFileService;    
    private ProcurementCardDocumentDao procurementCardDocumentDao;
    private DateTimeService dateTimeService; 
    private List<BatchInputFileType> batchInputFileTypes;
    private String pdfDirectory;
    private AttachmentService attachmentService;
    private NoteService noteService;
    private PersonService personService;
    
    public ReceiptProcessingServiceImpl() {
    }
    
    /**
     * @see org.kuali.kfs.module.ar.batch.service.ReceiptLoadService#loadFiles()
     */
    public boolean loadFiles() {
        
        LOG.info("Beginning processing of all available files for Receipt Batch Upload.");
        boolean result = true;
                     
        //  create a list of the files to process
         Map<String, BatchInputFileType> fileNamesToLoad = getListOfFilesToProcess();
        LOG.info("Found " + fileNamesToLoad.size() + " file(s) to process.");
        
        //  process each file in turn
        List<String> processedFiles = new ArrayList<String>();
        for (String inputFileName : fileNamesToLoad.keySet()) {
            
            LOG.info("Beginning processing of filename: " + inputFileName + ".");
   
            if (attachFiles(inputFileName, fileNamesToLoad.get(inputFileName))) {
                result &= true;
                LOG.info("Successfully loaded csv file");
                processedFiles.add(inputFileName);
            }
            else {
                LOG.error("Failed to load file");
                result &= false;
            }
        }

        //  remove done files
        removeDoneFiles(processedFiles);
       
        return result;
    }
    
    /**
     * Create a collection of the files to process with the mapped value of the BatchInputFileType
     * 
     * @return
     */
    protected Map<String, BatchInputFileType> getListOfFilesToProcess() {

        Map<String, BatchInputFileType> inputFileTypeMap = new LinkedHashMap<String, BatchInputFileType>();

        for (BatchInputFileType batchInputFileType : batchInputFileTypes) {

            List<String> inputFileNames = batchInputFileService.listInputFileNamesWithDoneFile(batchInputFileType);
            if (inputFileNames == null) {
                criticalError("BatchInputFileService.listInputFileNamesWithDoneFile(" + batchInputFileType.getFileTypeIdentifer() + ") returned NULL which should never happen.");
            }
            else {
                // update the file name mapping
                for (String inputFileName : inputFileNames) {

                    // filenames returned should never be blank/empty/null
                    if (StringUtils.isBlank(inputFileName)) {
                        criticalError("One of the file names returned as ready to process [" + inputFileName + "] was blank.  This should not happen, so throwing an error to investigate.");
                    }

                    inputFileTypeMap.put(inputFileName, batchInputFileType);
                }
            }
        }

        return inputFileTypeMap;
    }
    
    /**
     * Clears out associated .done files for the processed data files.
     * 
     * @param dataFileNames
     */
    protected void removeDoneFiles(List<String> dataFileNames) {
        for (String dataFileName : dataFileNames) {
            File doneFile = new File(StringUtils.substringBeforeLast(dataFileName, ".") + ".done");
            if (doneFile.exists()) {
                doneFile.delete();
            }
        }
    }

    /**
    */
    public boolean attachFiles(String fileName, BatchInputFileType batchInputFileType) {
        
        boolean result = true;
        
        //  load up the file into a byte array 
        byte[] fileByteContent = safelyLoadFileBytes(fileName);
        
        LOG.info("Attempting to parse the file");
        Object parsedObject = null;
        try {
             parsedObject =  batchInputFileService.parse(batchInputFileType, fileByteContent);
        }
        catch (ParseException e) {
            String errorMessage ="Error parsing batch file: " + e.getMessage();
            LOG.error(errorMessage, e);
            throw new RuntimeException(errorMessage);
        }
        
        //  make sure we got the type we expected, then cast it
        if (!(parsedObject instanceof List)) {
            String errorMessage = "Parsed file was not of the expected type.  Expected [" + List.class + "] but got [" + parsedObject.getClass() + "].";
            criticalError(errorMessage);
        }
        
        
        
        StringBuilder processResults = new StringBuilder();
        processResults.append("\"cardHolder\",\"amount\",\"purchasedate\",\"SharePointPath\",\"filename\",\"Success\"\n");        
       
        List<ReceiptProcessing> receipts =  ((List<ReceiptProcessing>) parsedObject);
        final String attachmentsPath = pdfDirectory;
        String mimeTypeCode = "pdf";
        
        for (ReceiptProcessing receipt  : receipts) {                                   
            Note note = new Note();

            java.util.Date pdate = null;
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            try
            {
                pdate = (java.util.Date) df.parse(receipt.getPurchasedate());
            }
            catch(ParseException e)
            {
                processResults.append(receipt.badData()); 
                LOG.error("Bad date field on incoming csv");
                continue;
            } catch (java.text.ParseException e) {
                processResults.append(receipt.badData()); 
                LOG.error("Bad date field on incoming csv");
                continue;
            }
            Date pdateSQL = null;
            if (pdate != null) {             
                pdateSQL = new Date(pdate.getTime());
            }
            List<ProcurementCardDocument> pcdoList = procurementCardDocumentDao.getDocumentByCarhdHolderAmountDateVendor(receipt.getCardHolder(), receipt.getAmount(), pdateSQL);
            ProcurementCardDocument pcdo = null;
             
            if (pcdoList.isEmpty()) {
                processResults.append(receipt.noMatch());
                continue;
            }
            if (pcdoList.size() >1 ){
                processResults.append(receipt.multipleMatch());
                continue;
            }
            if (pcdoList.size() == 1){
                pcdo = pcdoList.get(0);
            }
            
            String pdfFileName = attachmentsPath + "/" + receipt.getFilename();
            
            File f = null;
            FileInputStream fileInputStream = null;
            try {
                f = new File(pdfFileName);
                fileInputStream = new FileInputStream(pdfFileName);                                
            }
            catch (FileNotFoundException e) {
                LOG.error("file not found for Document " + pcdo.getDocumentNumber());
                processResults.append(receipt.badData());;
                continue;
            }
            catch (IOException e) {
                LOG.error("generic Io exception for Document " + pcdo.getDocumentNumber());
                processResults.append(receipt.badData());
                continue;
            }
            
            long fileSizeLong = f.length();          
            Integer fileSize = Integer.parseInt(Long.toString(fileSizeLong));
    
            String attachType = "";
            Attachment noteAttachment = null;
            try {
                noteAttachment = attachmentService.createAttachment(pcdo.getDocumentHeader(), pdfFileName, mimeTypeCode , fileSize, fileInputStream, attachType);
            } catch (IOException e) {
                LOG.error("Failed to attache file for Document " + pcdo.getDocumentNumber());
                processResults.append(receipt.noMatch());               
                e.printStackTrace();
                continue;
            }
            
            if (noteAttachment != null) {
                note.setNoteText("Receipt Attached");
                note.addAttachment(noteAttachment);
                note.setRemoteObjectIdentifier(pcdo.getDocumentHeader().getObjectId());
                note.setAuthorUniversalIdentifier(getSystemUser().getPrincipalId());
                note.setNoteTypeCode(KNSConstants.NoteTypeEnum.BUSINESS_OBJECT_NOTE_TYPE.getCode());               
                                
                try {
                    noteService.save(note);
                } catch (Exception e) {
                    LOG.error("Failed to save note for Document " + pcdo.getDocumentNumber());
                    processResults.append(receipt.noMatch());               
                    e.printStackTrace();
                    continue;
                }
                
                LOG.info("Attached pdf for document " + pcdo.getDocumentNumber());
                processResults.append(receipt.match() + pcdo.getDocumentNumber() +"\n");                
            }
            
            
        }        
        String outputCsv = processResults.toString();
        getcsvWriter(outputCsv);       
                
        return result;
    }  
    
    
    protected void getcsvWriter(String csvDoc) {
        
        String reportDropFolder = pdfDirectory + "/CIT-csv-archive/";
        String fileName = "CIT_OUT_" +
            new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(dateTimeService.getCurrentDate()) + ".csv";
        
         //  setup the writer
         File reportFile = new File(reportDropFolder + fileName);
         BufferedWriter writer = null;
         try {
             writer = new BufferedWriter(new FileWriter(reportFile));
             writer.write(csvDoc);
             writer.close();
         } catch (IOException e1) {
             LOG.error("IOException when trying to write report file");
             e1.printStackTrace();
         }
         
                         
     }
    
 
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
     * LOG error and throw RunTimeException
     * 
     * @param errorMessage
     */
    private void criticalError(String errorMessage){
        LOG.error(errorMessage);
        throw new RuntimeException(errorMessage);
    }    
    
    public void setBatchInputFileTypes(List<BatchInputFileType> batchInputFileType) {
        this.batchInputFileTypes = batchInputFileType;
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }
    
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setprocurementCardDocumentDao(ProcurementCardDocumentDao procurementCardDocumentDao) {
        this.procurementCardDocumentDao = procurementCardDocumentDao;
    }
    
    public void setAttachmentService(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }
             
    public NoteService getNoteService() {
        return noteService;
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    public Person getSystemUser() {
        return personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
    }
    
    /**
     * @return Returns the personService.
     */
    protected PersonService getPersonService() {
        return personService;
    }

    /**
     * @param personService The personService to set.
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public String getPdfDirectory() {
        return pdfDirectory;
    }

    public void setPdfDirectory(String pdfDirectory) {
        this.pdfDirectory = pdfDirectory;
    }
              
    
}

