/**
 * 
 */
package edu.cornell.kfs.sys.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.attribute.DocumentAttributeIndexingQueue;

import edu.cornell.kfs.sys.service.DocumentMaintenanceService;

/**
 * @author kwk43
 *
 */
public class DocumentReindexStep extends AbstractStep {

	private String stagingDirectory;
	private String fileName = "documentReindex.txt";
	
	
	
	/* (non-Javadoc)
	 * @see org.kuali.kfs.sys.batch.Step#execute(java.lang.String, java.util.Date)
	 */
	public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        
        final DocumentAttributeIndexingQueue queue = KewApiServiceLocator.getDocumentAttributeIndexingQueue();
        
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
			Long id = new Long(it.next());
			queue.indexDocument(id.toString());
		}
		
		//TODO UPGRADE-911
		//addTimeStampToFileName(f, fileName, stagingDirectory);
		
		return true;
//		return SpringContext.getBean(DocumentMaintenanceService.class).requeueDocuments();
	}


	public void setStagingDirectory(String stagingDirectory) {
		this.stagingDirectory = stagingDirectory;
	}
	
	

}
