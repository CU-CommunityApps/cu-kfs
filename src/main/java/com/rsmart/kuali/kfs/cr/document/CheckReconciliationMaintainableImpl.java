package com.rsmart.kuali.kfs.cr.document;

import java.sql.Date;

import org.apache.ojb.broker.cache.RuntimeCacheException;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kns.bo.DocumentHeader;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.DocumentService;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;

/**
 * This class overrides the functionality in the base FinancialSystemMaintainable to update the status change date when the
 * staus of a check is changed.
 */
public class CheckReconciliationMaintainableImpl extends FinancialSystemMaintainable {

    /**
     * @see org.kuali.rice.kns.maintenance.KualiMaintainableImpl#doRouteStatusChange(org.kuali.rice.kns.bo.DocumentHeader)
     */
    public void doRouteStatusChange(DocumentHeader documentHeader) {
        KualiWorkflowDocument workflowDocument = documentHeader.getWorkflowDocument();

        if (workflowDocument.stateIsProcessed() && !KFSConstants.MAINTENANCE_NEW_ACTION.equalsIgnoreCase(getMaintenanceAction())) {

            DocumentService documentService = SpringContext.getBean(DocumentService.class);
            MaintenanceDocument document;
            try {
                document = (MaintenanceDocument) documentService.getByDocumentHeaderId(documentHeader.getDocumentNumber());


                CheckReconciliation oldCr = (CheckReconciliation) document.getOldMaintainableObject().getBusinessObject();
                CheckReconciliation newCr = (CheckReconciliation) document.getNewMaintainableObject().getBusinessObject();

                if (ObjectUtils.isNotNull(oldCr) && !oldCr.getStatus().equalsIgnoreCase(newCr.getStatus())) {

                    Date currentDate = SpringContext.getBean(DateTimeService.class).getCurrentSqlDate();

                    newCr.setStatusChangeDate(currentDate);
                    
                    //KSPTS-2719
                    if(CRConstants.CANCELLED.equalsIgnoreCase(newCr.getStatus())){
                        newCr.setCancelDocHdrId(documentHeader.getDocumentNumber());
                    }
                }

            } catch (WorkflowException e) {
                throw new RuntimeCacheException(e);
            }

        }
    }

}