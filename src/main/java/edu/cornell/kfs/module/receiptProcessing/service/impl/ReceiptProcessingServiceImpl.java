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
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.service.AttachmentService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.exception.ParseException;

import edu.cornell.kfs.fp.dataaccess.ProcurementCardDocumentDao;
import edu.cornell.kfs.module.receiptProcessing.businessobject.ReceiptProcessing;
import edu.cornell.kfs.module.receiptProcessing.service.ReceiptProcessingService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSConstants.FileExtensions;
import edu.cornell.kfs.sys.util.LoadFileUtils;

public class ReceiptProcessingServiceImpl implements ReceiptProcessingService {
	private static final Logger LOG = LogManager.getLogger(ReceiptProcessingServiceImpl.class);
    
    public static final String RESULT_FILE_HEADER_LINE_WITH_EXTRA_FIELDS = "\"cardHolder\",\"amount\",\"purchasedate\",\"filePath\",\"filename\",\"cardHolderNetID\",\"sourceUniqueID\",\"eDocNumber\",\"Success\"\n";  
    public static final String RESULT_FILE_HEADER_LINE = "\"cardHolder\",\"amount\",\"purchasedate\",\"filePath\",\"filename\",\"Success\"\n";  
    public static final String CUSTOMER_PDF_SUBFOLDER_SUFFIX = "-input-csv";
    public static final String CSV_OUTPUT_FILENAME_PREFIX = "CIT_OUT_";
    
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
   
            if (attachFiles(inputFileName, fileNamesToLoad.get(inputFileName), null)) {
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
                criticalError("BatchInputFileService.listInputFileNamesWithDoneFile(" + batchInputFileType.getFileTypeIdentifier() + ") returned NULL which should never happen.");
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
    public boolean attachFiles(String fileName, BatchInputFileType batchInputFileType, String customerName) {
        
        boolean result = true;
        
        //  load up the file into a byte array 
        byte[] fileByteContent = LoadFileUtils.safelyLoadFileBytes(fileName);
        
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

        List<ReceiptProcessing> receipts =  ((List<ReceiptProcessing>) parsedObject);
        final String attachmentsPath = pdfDirectory;

        String mimeTypeCode = "pdf";
        
        // determine in which mode we are: match& attach, match, attach
        // if first 5 fields ate not blank then it is a match and attach
        boolean matchAndAttach = false;

        // if any receipt get the first one
        if(ObjectUtils.isNotNull(receipts) && receipts.size() > 0){
        	ReceiptProcessing receipt = receipts.get(0);
        	// match and attach only occurs for CALS; for CALS files the source unique ID is blank
        	matchAndAttach = StringUtils.isBlank(receipt.getSourceUniqueID()) ;
        }
        
        if(matchAndAttach){
        	matchAndAttach(receipts, attachmentsPath, mimeTypeCode);
        }
        else{
        	matchOrAttachOnly(fileName, batchInputFileType, receipts, attachmentsPath, mimeTypeCode);
		}  
        
        return result;
        
    }  
    
    /**
     * Performs match and attach. It will search for a match PCDO document and attempt to attach the receipt.
     * 
     * @param receipts
     * @param attachmentsPath
     * @param mimeTypeCode
     */
    protected void matchAndAttach(List<ReceiptProcessing> receipts, String attachmentsPath, String mimeTypeCode){
		StringBuilder processResults = new StringBuilder();
		processResults.append(RESULT_FILE_HEADER_LINE);

		for (ReceiptProcessing receipt : receipts) {
			Note note = new Note();

			java.util.Date pdate = null;
			DateFormat df = new SimpleDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT, Locale.US);
			try {
				pdate = (java.util.Date) df.parse(receipt.getPurchasedate());
			} catch (ParseException e) {
				processResults.append(receipt.badData(false));
				LOG.error("Bad date field on incoming csv");
				continue;
			} catch (java.text.ParseException e) {
				processResults.append(receipt.badData(false));
				LOG.error("Bad date field on incoming csv");
				continue;
			}
			Date pdateSQL = null;
			if (pdate != null) {
				pdateSQL = new Date(pdate.getTime());
			}

			List<ProcurementCardDocument> pcdoList = procurementCardDocumentDao.getDocumentByCarhdHolderAmountDateVendor(receipt.getCardHolder(), receipt.getAmount(), pdateSQL);
			ProcurementCardDocument pcdo = null;

			if (ObjectUtils.isNull(pcdoList) || pcdoList.isEmpty()) {
				processResults.append(receipt.noMatch(false));
				continue;
			}
			if (pcdoList.size() > 1) {
				processResults.append(receipt.multipleMatch(false));
				continue;
			}
			if (pcdoList.size() == 1) {
				pcdo = pcdoList.get(0);
			}

			String pdfFileName = attachmentsPath + "/" + receipt.getFilename();
			LOG.info("Start creating note and attaching pdf file " + pdfFileName + " to PCDO document #" + pcdo.getDocumentNumber());

			File f = null;
			FileInputStream fileInputStream = null;
			try {
				f = new File(pdfFileName);
				fileInputStream = new FileInputStream(pdfFileName);
			} catch (FileNotFoundException e) {
				LOG.error("File " + pdfFileName + " not found for Document " + pcdo.getDocumentNumber());
				processResults.append(receipt.badData(false));
				continue;
			} catch (IOException e) {
				LOG.error("generic Io exception for Document "
						+ pcdo.getDocumentNumber());
				processResults.append(receipt.badData(false));
				continue;
			}

			long fileSizeLong = f.length();
			Integer fileSize = Integer.parseInt(Long.toString(fileSizeLong));

			String attachType = "";
			Attachment noteAttachment = null;
			try {
				noteAttachment = attachmentService.createAttachment(pcdo.getDocumentHeader(), pdfFileName, mimeTypeCode, fileSize, fileInputStream, attachType);
			} catch (IOException e) {
				LOG.error("Failed to attach file for Document " + pcdo.getDocumentNumber());
				processResults.append(receipt.noMatch(false));
				e.printStackTrace();
				continue;
			} catch (IllegalArgumentException e) {
			    /*
			     * Our custom attachment service will throw an IllegalArgumentException if the virus scan fails.
			     * The virus scan could also end up failing if the file size is too large. In such cases,
			     * return an error code indicating such a problem (or a problem with invalid parameters).
			     */
			    LOG.error("Failed to create attachment for Document " + pcdo.getDocumentNumber(), e);
			    processResults.append(receipt.attachmentCreationError(false));
			    continue;
			}

			if (noteAttachment != null) {
				note.setNoteText("Receipt Attached");
				note.addAttachment(noteAttachment);
				note.setRemoteObjectIdentifier(pcdo.getDocumentHeader().getObjectId());
				note.setAuthorUniversalIdentifier(getSystemUser().getPrincipalId());
				note.setNoteTypeCode(KFSConstants.NoteTypeEnum.BUSINESS_OBJECT_NOTE_TYPE.getCode());
				note.setNotePostedTimestampToCurrent();

				try {
					noteService.save(note);
				} catch (Exception e) {
					LOG.error("Failed to save note for Document " + pcdo.getDocumentNumber());
					processResults.append(receipt.noMatch(false));
					e.printStackTrace();
					continue;
				}

				LOG.info("Attached pdf " + pdfFileName + " for document " + pcdo.getDocumentNumber());
				processResults.append(receipt.match(pcdo.getDocumentNumber(), false));
			}
		}

		String outputCsv = processResults.toString();
		// this is CALS output folder and it has to stay unchanged
		String reportDropFolder = pdfDirectory + "/CIT-csv-archive/";
		writeToCsvFile(outputCsv, reportDropFolder);
	}
    
	/**
	 * Performs match or attach on each incoming record. The method determines
	 * for each incoming record if a match only or attach only should be
	 * performed. If first card holder, amount and purchase date are not blank
	 * then a match only is performed. If the source unique id and edoc number
	 * are not blank then an attach only is performed.
	 * 
	 * @param fileName
	 * @param receipts
	 * @param attachmentsPath
	 * @param mimeTypeCode
	 */
    protected void matchOrAttachOnly(String fileName, BatchInputFileType batchInputFileType, List<ReceiptProcessing> receipts, String attachmentsPath, String mimeTypeCode){
        StringBuilder processResults = new StringBuilder();
        processResults.append(RESULT_FILE_HEADER_LINE_WITH_EXTRA_FIELDS);  
        String customerName = getCustomerNameFromFileName(fileName, batchInputFileType);
        
    	for (ReceiptProcessing receipt  : receipts) {   
			boolean matchOnly = StringUtils.isNotBlank(receipt.getCardHolder()) && StringUtils.isNotBlank(receipt.getAmount()) && StringUtils.isNotBlank(receipt.getPurchasedate()) && StringUtils.isBlank(receipt.getFilePath()) && StringUtils.isBlank(receipt.getFilename());
			boolean attachOnly = StringUtils.isNotBlank(receipt.getSourceUniqueID()) && StringUtils.isNotBlank(receipt.getFilePath()) && StringUtils.isNotBlank(receipt.getFilename());

			if (matchOnly) {
				java.util.Date pdate = null;
				DateFormat df = new SimpleDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT, Locale.US);
				try {
					pdate = (java.util.Date) df.parse(receipt.getPurchasedate());
				} catch (ParseException e) {
					processResults.append(receipt.badData(true));
					LOG.error("Bad date field on incoming csv");
					continue;
				} catch (java.text.ParseException e) {
					processResults.append(receipt.badData(true));
					LOG.error("Bad date field on incoming csv");
					continue;
				}
				Date pdateSQL = null;
				if (pdate != null) {
					pdateSQL = new Date(pdate.getTime());
				}

				List<ProcurementCardDocument> pcdoList = procurementCardDocumentDao.getDocumentByCarhdHolderNameAmountDateCardHolderNetID(receipt.getAmount(), pdateSQL, receipt.getCardHolderNetID());
				ProcurementCardDocument pcdo = null;

				if (ObjectUtils.isNull(pcdoList) || pcdoList.isEmpty()) {
					processResults.append(receipt.noMatch(true));
					continue;
				}
				if (pcdoList.size() > 1) {
					processResults.append(receipt.multipleMatch(true));
					continue;
				}
				if (pcdoList.size() == 1) {
					pcdo = pcdoList.get(0);
					String eDocNumber = pcdo.getDocumentNumber();
					receipt.seteDocNumber(eDocNumber);
					processResults.append(receipt.match(eDocNumber, true));
				}

			}

			if (attachOnly) {
				Note note = new Note();

				List<ProcurementCardDocument> pcdoList = procurementCardDocumentDao.getDocumentByEdocNumber(receipt.geteDocNumber());
				ProcurementCardDocument pcdo = null;

				if (ObjectUtils.isNull(pcdoList) || pcdoList.isEmpty()) {
					processResults.append(receipt.attachOnlyError());
					continue;
				}
				if (pcdoList.size() > 1) {
					processResults.append(receipt.attachOnlyError());
					continue;
				}
				if (pcdoList.size() == 1) {
					pcdo = pcdoList.get(0);
				}
		        if(StringUtils.isNotBlank(customerName)){
		        	attachmentsPath = pdfDirectory + "/" + StringUtils.upperCase(customerName, Locale.US) + CUSTOMER_PDF_SUBFOLDER_SUFFIX ;
		        }
				String pdfFileName = attachmentsPath + "/" + receipt.getFilename();
				LOG.info("Start creating note and attaching pdf file " + pdfFileName + " to PCDO document #" + pcdo.getDocumentNumber());

				File f = null;
				FileInputStream fileInputStream = null;
				try {
					f = new File(pdfFileName);
					fileInputStream = new FileInputStream(pdfFileName);
				} catch (FileNotFoundException e) {
					LOG.error("File " + pdfFileName + " not found for Document " + pcdo.getDocumentNumber());
					processResults.append(receipt.attachOnlyError());
					continue;
				} catch (IOException e) {
					LOG.error("generic Io exception for Document " + pcdo.getDocumentNumber());
					processResults.append(receipt.attachOnlyError());
					continue;
				}

				long fileSizeLong = f.length();
				Integer fileSize = Integer.parseInt(Long.toString(fileSizeLong));

				String attachType = "";
				Attachment noteAttachment = null;
				try {
					noteAttachment = attachmentService.createAttachment(pcdo.getDocumentHeader(), pdfFileName, mimeTypeCode, fileSize, fileInputStream, attachType);
				} catch (IOException e) {
					LOG.error("Failed to attach file for Document " + pcdo.getDocumentNumber());
					processResults.append(receipt.attachOnlyError());
					e.printStackTrace();
					continue;
				} catch (IllegalArgumentException e) {
				    /*
				     * Our custom attachment service will throw an IllegalArgumentException if the virus scan fails.
				     * The virus scan could also end up failing if the file size is too large. In such cases,
				     * return an error code indicating such a problem (or a problem with invalid parameters).
				     */
				    LOG.error("Failed to create attachment for Document " + pcdo.getDocumentNumber(), e);
				    processResults.append(receipt.attachmentCreationError(true));
				    continue;
				}

				if (noteAttachment != null) {
					note.setNoteText("Receipt Attached");
					note.addAttachment(noteAttachment);
					note.setRemoteObjectIdentifier(pcdo.getDocumentHeader().getObjectId());
					note.setAuthorUniversalIdentifier(getSystemUser().getPrincipalId());
					note.setNoteTypeCode(KFSConstants.NoteTypeEnum.BUSINESS_OBJECT_NOTE_TYPE.getCode());
					note.setNotePostedTimestampToCurrent();

					try {
						noteService.save(note);
					} catch (Exception e) {
						LOG.error("Failed to save note for Document " + pcdo.getDocumentNumber());
						processResults.append(receipt.attachOnlyError());
						e.printStackTrace();
						continue;
					}

					LOG.info("Attached pdf " + pdfFileName + " for document " + pcdo.getDocumentNumber());
					processResults.append(receipt.match( "8", true));
				}

			}
			
			if(!matchOnly && !attachOnly){
				LOG.info("Invalid input data does not meet either match only nor attach only conditions: " + receipt.returnBoLine(true));
			}
		}
    	
		String outputCsv = processResults.toString();
		// each customer will have a separate output folder for easier processing of the results files		
	    String reportDropFolder = pdfDirectory + "/CIT-" + customerName  +"-csv-archive/";
	    
        try {
            /**
             * Create, if not there
             */
            FileUtils.forceMkdir(new File(reportDropFolder));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writeToCsvFile(outputCsv, reportDropFolder);

    }
    
    
    /**
     * Writes the processing results in the output folder.
     * 
     * @param csvDoc
     * @param reportDropFolder
     */
    protected void writeToCsvFile(String csvDoc, String reportDropFolder) {
        String csvFileNameDateChunk = formatCurrentDateForInclusionInCsvFileName();
        String fileName = StringUtils.join(
                CSV_OUTPUT_FILENAME_PREFIX, csvFileNameDateChunk, FileExtensions.CSV);
        File reportFile = new File(reportDropFolder + fileName);
        
        try (
            FileWriter fileWriter = new FileWriter(reportFile, StandardCharsets.UTF_8);
            BufferedWriter writer = new BufferedWriter(fileWriter);
        ) {
            writer.write(csvDoc);
            writer.flush();
        } catch (IOException e) {
            LOG.error("writeToCsvFile, IOException when trying to write report file", e);
            throw new UncheckedIOException(e);
        }
        
        createDoneFileForNewCsvFile(fileName, reportDropFolder);
    }
    
    private String formatCurrentDateForInclusionInCsvFileName() {
        SimpleDateFormat csvFileNameDateFormat = new SimpleDateFormat(
                CUKFSConstants.DATE_FORMAT_yyyyMMdd_HHmmssSSS, Locale.US);
        java.util.Date currentDate = dateTimeService.getCurrentDate();
        return csvFileNameDateFormat.format(currentDate);
    }
    
    private void createDoneFileForNewCsvFile(String csvFileName, String reportDropFolder) {
        String fileNameWithoutExtension = StringUtils.substringBeforeLast(csvFileName, KFSConstants.DELIMITER);
        String doneFileName = fileNameWithoutExtension + FileExtensions.DONE;
        File doneFile = new File(reportDropFolder + doneFileName);
        try {
            FileUtils.touch(doneFile);
        } catch (IOException e) {
            LOG.error("createDoneFileForNewCsvFile, IOException when trying to create .done file", e);
            throw new UncheckedIOException(e);
        }
    }
    
    /**
     * Gets the customer name from the input file name. Files other than cals files will have the file name in this format: receiptProcessing_kfs_${customerName}_${date}.csv
     * 
     * @param fileName
     * @param batchInputFileType
     * @return the customer name
     */
    protected String getCustomerNameFromFileName(String fileName, BatchInputFileType batchInputFileType){
    	String customerName = fileName.substring(fileName.lastIndexOf(batchInputFileType.getFileTypeIdentifier() + "_") + batchInputFileType.getFileTypeIdentifier().length() + 1, fileName.lastIndexOf("_"));
    	return customerName;
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

