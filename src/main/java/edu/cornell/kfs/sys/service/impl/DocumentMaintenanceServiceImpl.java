/**
 * 
 */
package edu.cornell.kfs.sys.service.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.CoreConfigHelper;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.DocumentRefreshQueue;
import org.kuali.rice.kew.api.document.attribute.DocumentAttributeIndexingQueue;
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
			Collection<String> docIds = documentMaintenanceDao.getDocumentRequeueValues();
		    LOG.info("Total number of documents flagged for requeuing: "+docIds.size());
		    
			for (Iterator<String> it = docIds.iterator(); it.hasNext(); ) {
				Long id = new Long(it.next());
                DocumentRefreshQueue documentRequeuer = KewApiServiceLocator.getDocumentRequeuerService(CoreConfigHelper.getApplicationId(), id.toString(), 0 /*no wait*/);
                documentRequeuer.refreshDocument(id.toString());
			}
			
			return result;
		}

		/**
		 * 
		 */
		@Transactional
		public boolean reindexDocuments() {
			boolean result = true;
			final DocumentAttributeIndexingQueue queue = KewApiServiceLocator.getDocumentAttributeIndexingQueue();

		    List<String> docIds = new ArrayList<String>();

		    docIds = documentMaintenanceDao.getDocumentReindexValues();
		    LOG.info("Total number of documents flagged for reindexing: "+docIds.size());
		    
			for (Iterator<String> it = docIds.iterator(); it.hasNext(); ) {
				Long id = new Long(it.next());
				queue.indexDocument(id.toString());
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
	