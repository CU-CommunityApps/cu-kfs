/**
 * 
 */
package edu.cornell.kfs.sys.service.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import edu.cornell.kfs.sys.dataaccess.DocumentRequeueFileBuilderDao;
import edu.cornell.kfs.sys.service.DocumentRequeueFileBuilderService;
import edu.cornell.kfs.sys.web.struts.DocumentRequeueFileBuilderAction;

/**
 * @author Admin-dwf5
 *
 */
public class DocumentRequeueFileBuilderServiceImpl implements DocumentRequeueFileBuilderService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DocumentRequeueFileBuilderServiceImpl.class);

	private DocumentRequeueFileBuilderDao documentRequeueFileBuilderDao;
	private String directoryPath;
	private String fileName;
	private String fileExtension;
	
	/**
	 * @return Return the record count for new document that will be created.
	 */
	public int generateRequeueFile() {

		// Retrieve all the document IDs for docs to be requeued.
		ArrayList<String> requeueFileValues = (ArrayList<String>) documentRequeueFileBuilderDao.getDocumentRequeueFileValues();
		
		// Build file out of the values returned from the DAO
		buildRequeueFile(requeueFileValues);
		
		return requeueFileValues.size();
	}

	private boolean buildRequeueFile(ArrayList<String> docIds) {
        LOG.info("Building document requeuer file from "+docIds.size()+" results. ");
        // construct the outgoing file name
        String filename = directoryPath + "/" + fileName + "." + fileExtension;

        try {
	        FileOutputStream out = new FileOutputStream(filename);
	        PrintStream p = new PrintStream(out);
	        
	        for(String id : docIds) {
	        	p.println(id);
	        }
	        
	        p.close();
	        out.close();
        } catch(FileNotFoundException fnf) {
	        LOG.info("Error while creating file: File Not Found ");
        	return false;
        } catch(IOException io) {
	        LOG.info("Error while creating file: Could not close stream connection ");
        	return false;
        }
		
		return true;
	}
	
	/**
	 * @return the documentRequeueFileBuilderDao
	 */
	public DocumentRequeueFileBuilderDao getDocumentRequeueFileBuilderDao() {
		return documentRequeueFileBuilderDao;
	}

	/**
	 * @param documentRequeueFileBuilderDao the documentRequeueFileBuilderDao to set
	 */
	public void setDocumentRequeueFileBuilderDao(DocumentRequeueFileBuilderDao documentRequeueFileBuilderDao) {
		this.documentRequeueFileBuilderDao = documentRequeueFileBuilderDao;
	}

	/**
	 * @return the directoryPath
	 */
	public String getDirectoryPath() {
		return directoryPath;
	}

	/**
	 * @param directoryPath the directoryPath to set
	 */
	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
	}

	/**
	 * @return the fileExtension
	 */
	public String getFileExtension() {
		return fileExtension;
	}

	/**
	 * @param fileExtension the fileExtension to set
	 */
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
}
