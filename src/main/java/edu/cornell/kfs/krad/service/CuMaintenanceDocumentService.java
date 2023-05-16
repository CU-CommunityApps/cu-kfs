package edu.cornell.kfs.krad.service;

import org.kuali.kfs.krad.service.MaintenanceDocumentService;

import org.kuali.kfs.kns.document.MaintenanceDocument;

public interface CuMaintenanceDocumentService extends MaintenanceDocumentService {
    
    /**
    * Prepares the <code>MaintenanceDocument</code> on initial request
    * <p>
    * <p>
    * This includes retrieving the data object for edit or copy, clearing fields
    *
    * @param objectClassName   class name for the object being maintained
    * @param docTypeName       workflow doc type for the maintenance document requested
    * @param maintenanceAction indicates whether this is a new, copy, or edit maintenance action
    * @return MaintenanceDocument prepared document instance
    */
   MaintenanceDocument setupNewMaintenanceDocument(String objectClassName, String docTypeName,
           String maintenanceAction);

}
