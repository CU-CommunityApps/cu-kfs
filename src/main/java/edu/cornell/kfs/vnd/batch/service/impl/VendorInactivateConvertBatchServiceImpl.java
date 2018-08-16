/**
 * @author cab379
 */

package edu.cornell.kfs.vnd.batch.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.businessobject.VendorInactiveReason;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.vnd.batch.service.VendorInactivateConvertBatchService;
import edu.cornell.kfs.vnd.businessobject.VendorInactivateConvertBatch;
import edu.cornell.kfs.vnd.document.service.CUVendorService;

public class VendorInactivateConvertBatchServiceImpl implements VendorInactivateConvertBatchService {
	private static final Logger LOG = LogManager.getLogger(VendorInactivateConvertBatchServiceImpl.class);
    
    private BatchInputFileService batchInputFileService;    
    private List<BatchInputFileType> batchInputFileTypes;
    private NoteService noteService;
    private CUVendorService cuVendorService;
	private String reportsDirectoryPath;
	private BusinessObjectService businessObjectService;

    
    public VendorInactivateConvertBatchServiceImpl() {
    }
    
    /**
     *
     */
    public boolean processVendorUpdates() {
        
        LOG.info("Beginning processing of all available files for InactivateConvert Batch Upload.");
        boolean result = true;
                     
        //  create a list of the files to process
         Map<String, BatchInputFileType> fileNamesToLoad = getListOfFilesToProcess();
        LOG.info("Found " + fileNamesToLoad.size() + " file(s) to process.");
        
        //  process each file in turn
        List<String> processedFiles = new ArrayList<String>();
        for (String inputFileName : fileNamesToLoad.keySet()) {
            
            LOG.info("Beginning processing of filename: " + inputFileName + ".");
   
            if (inactivateConvert(inputFileName, fileNamesToLoad.get(inputFileName))) {
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
    public boolean inactivateConvert(String fileName, BatchInputFileType batchInputFileType) {
        
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
                              
       
        List<VendorInactivateConvertBatch> vendors =  ((List<VendorInactivateConvertBatch>) parsedObject);

        for (VendorInactivateConvertBatch vendor : vendors) {
            String[] vendorId = vendor.getVendorId().split("-");
            
            
            Collection<VendorDetail> vendorDets = businessObjectService.findMatching(VendorDetail.class,
                    Collections.singletonMap("vendorHeaderGeneratedIdentifier", vendorId[0]));
            
            
            
            
            GlobalVariables.setUserSession(new UserSession("kfs"));
            
            VendorDetail vnd = cuVendorService.getByVendorNumber(vendor.getVendorId());
            
            if(ObjectUtils.isNull(vnd)){
            	LOG.info("Vendor with id: " + vendor.getVendorId() + " does not exist in the database.");
            }
           
            if ((ObjectUtils.isNotNull(vnd))) {
            	VendorHeader vHead = businessObjectService.findBySinglePrimaryKey(VendorHeader.class, vnd.getVendorHeaderGeneratedIdentifier());
                if (vendor.getAction().equalsIgnoreCase("inactivate") && ((vendorDets.size() == 1) || !(vendorId[1].equalsIgnoreCase("0")))) {
                      inactivateVendor(vnd, vendor.getNote(), vendor.getReason());
                }
                else if (vendor.getAction().equalsIgnoreCase("activate") && ((vendorDets.size() == 1) || !(vendorId[1].equalsIgnoreCase("0")))) {
                    activateVendor(vnd, vendor.getNote(), vendor.getReason());
                }
                else if (vendor.getAction().equalsIgnoreCase("convert") && ((vendorDets.size() == 1) || !(vendorId[1].equalsIgnoreCase("0")))) {
                     convertVendor(vHead, vnd, vendor.getNote(), vendor.getConvertType());
                }
                else if (vendorDets.size() > 1) {
                	LOG.info("failed to process for "+vnd.getVendorNumber()+", This vendor has child records. These must be processed through the application");
                }                           
                else {
                    String errorMessage = "Failed to parse vendor action expected inactivate or convert but recevied " + vendor.getAction();
                    criticalError(errorMessage);
                }
            }
               
        }
        
              
       
                
        return result;
    }  
    
    private boolean checkReasonCd (String reasonCd) {
    	
    	Collection<VendorInactiveReason> reasonCodes = businessObjectService.findAll(VendorInactiveReason.class);
		
    	for ( VendorInactiveReason reasonCode : reasonCodes) {
    		if (reasonCode.getVendorInactiveReasonCode().equalsIgnoreCase(reasonCd)) {
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    private void activateVendor(VendorDetail vnd, String note, String reasonCd) {
    	
    	vnd.setActiveIndicator(true);
        vnd.setVendorInactiveReasonCode(null);
        vnd.setVendorInactiveReason(null);
         
        Note newNote = new Note();
        newNote.setNoteText("Vendor has been activated via the activate batch job for the following reason: "+note);
        newNote.setNotePostedTimestampToCurrent();
        LOG.info("activating "+vnd.getVendorNumber());

        Note tmpNote = noteService.createNote(newNote, vnd, GlobalVariables.getUserSession().getPrincipalId());
        LOG.info("save note");

        SpringContext.getBean(NoteService.class).save(tmpNote);
         
        businessObjectService.save(vnd);
    	    	
		
	}

	private void inactivateVendor (VendorDetail vnd, String note, String reasonCd) {
		if (checkReasonCd(reasonCd)) { 
	        vnd.setActiveIndicator(false);
	        vnd.setVendorInactiveReasonCode(reasonCd);
	        
	        Note newNote = new Note();
	        newNote.setNoteText("Vendor has been inactivated via inactivate vendor batch job for reason: "+note);
	        newNote.setNotePostedTimestampToCurrent();
	        LOG.info("Inactivating "+vnd.getVendorNumber());
	
	        Note tmpNote = noteService.createNote(newNote, vnd, GlobalVariables.getUserSession().getPrincipalId());
	        LOG.info("save note");
	
	        SpringContext.getBean(NoteService.class).save(tmpNote);
	        
	        businessObjectService.save(vnd);
		}
		else {
			LOG.info("Invalid reason code for vendor: "+vnd.getVendorName()+". This vendor was not deactivated");
					
		}
    }
    
private void convertVendor (VendorHeader vHead, VendorDetail vnd, String note, String vndTypeCd) {
    vHead.setVendorTypeCode(vndTypeCd);
    
    Note newNote = new Note();
    newNote.setNoteText("Vendor Type has been converted to "+vndTypeCd+" via the convert vendor batch job for the following reason: "+note);
    newNote.setNotePostedTimestampToCurrent();
    LOG.info("Converting "+vnd.getVendorNumber());

    Note tmpNote = noteService.createNote(newNote, vnd, GlobalVariables.getUserSession().getPrincipalId());
    LOG.info("save note");

    SpringContext.getBean(NoteService.class).save(tmpNote);
    
    businessObjectService.save(vHead);
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
    
             
    public NoteService getNoteService() {
        return noteService;
    }
    
    public void setReportsDirectoryPath(String reportsDirectoryPath) {
		this.reportsDirectoryPath = reportsDirectoryPath;
	}

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    public CUVendorService getCuVendorService() {
        return cuVendorService;
    }

    public void setCuVendorService(CUVendorService cuVendorService) {
        this.cuVendorService = cuVendorService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
           
              
    
}

