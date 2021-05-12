package edu.cornell.kfs.sys.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kew.api.KewApiServiceLocator;
import org.kuali.kfs.kew.api.document.attribute.DocumentAttributeIndexingQueue;

import edu.cornell.kfs.sys.batch.CuAbstractStep;

public class DocumentReindexStep extends CuAbstractStep {
	private String stagingDirectory;
	private String fileName = "documentReindex.txt";
	private static final Logger LOG = LogManager.getLogger(DocumentReindexStep.class);
	
	public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        final DocumentAttributeIndexingQueue documentAttributeIndexingQueue = KewApiServiceLocator.getDocumentAttributeIndexingQueue();
        
		File f = new File(stagingDirectory+File.separator+fileName);
	    ArrayList<String> docIds = new ArrayList<String>();

	    try {
	    	BufferedReader reader = new BufferedReader(new FileReader(f));

	    	String line = null;
	    	while ((line=reader.readLine()) != null) {
	    		docIds.add(line);   	
	    	}
	    } catch (IOException ioe) {
	    	ioe.printStackTrace();
	    	return false;
	    }
		for (Iterator<String> it = docIds.iterator(); it.hasNext(); ) {
			String documentId = it.next();
			
			try {
			    LOG.info("execute, indexing document " + documentId);
			    documentAttributeIndexingQueue.indexDocument(documentId);
			} catch (Exception e) {
			    LOG.error("execute, had an error indexing docuemnt " + documentId, e);
			}
		}
		
		addTimeStampToFileName(f, fileName, stagingDirectory);
		return true;
	}

	public void setStagingDirectory(String stagingDirectory) {
		this.stagingDirectory = stagingDirectory;
	}

}
