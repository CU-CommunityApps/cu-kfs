/**
 * 
 */
package edu.cornell.kfs.sys.service.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.actionrequest.service.DocumentRequeuerService;
import org.kuali.rice.kew.docsearch.service.SearchableAttributeProcessingService;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.sys.dataaccess.DocumentMaintenanceDao;
import edu.cornell.kfs.sys.service.DocumentMaintenanceService;

/**
 * @author Admin-dwf5
 *
 */
public class DocumentMaintenanceServiceImpl implements DocumentMaintenanceService {
		private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DocumentMaintenanceServiceImpl.class);

		private DocumentMaintenanceDao documentMaintenanceDao;

		/**
		 * 
		 */
		@Transactional
		public boolean requeueDocuments() {
		    boolean result = true;
			List<String> docIds = new ArrayList<String>();

		    docIds = documentMaintenanceDao.getDocumentRequeueValues();
		    LOG.info("Total number of documents flagged for requeuing: "+docIds.size());
		    
			for (Iterator<String> it = docIds.iterator(); it.hasNext(); ) {
				Long id = new Long(it.next());
				SpringContext.getBean(DocumentRequeuerService.class).requeueDocument(id);
			}
			
			return result;
		}

		/**
		 * 
		 */
		@Transactional
		public boolean reindexDocuments() {
			boolean result = true;
		    List<String> docIds = new ArrayList<String>();

		    docIds = documentMaintenanceDao.getDocumentReindexValues();
		    LOG.info("Total number of documents flagged for reindexing: "+docIds.size());
		    
			for (Iterator<String> it = docIds.iterator(); it.hasNext(); ) {
				Long id = new Long(it.next());
				SpringContext.getBean(SearchableAttributeProcessingService.class).indexDocument(id);
			}
			
			return result;
		}
		
		/**
		 * @return the documentMaintenanceDao
		 */
		public DocumentMaintenanceDao getDocumentMaintenanceDao() {
				return documentMaintenanceDao;
		}

		/**
		 * @param documentMaintenanceDao the documentMaintenanceDao to set
		 */
		public void setDocumentMaintenanceDao(DocumentMaintenanceDao documentMaintenanceDao) {
				this.documentMaintenanceDao = documentMaintenanceDao;
		}

}
	