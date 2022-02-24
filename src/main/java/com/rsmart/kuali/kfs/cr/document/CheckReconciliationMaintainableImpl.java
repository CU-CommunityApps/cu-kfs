package com.rsmart.kuali.kfs.cr.document;

import java.sql.Date;

import org.apache.ojb.broker.cache.RuntimeCacheException;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ObjectUtils;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;

/**
 * This class overrides the functionality in the base FinancialSystemMaintainable to update the status change date when the
 * staus of a check is changed.
 */
public class CheckReconciliationMaintainableImpl extends FinancialSystemMaintainable {

    /**
     * @see org.kuali.kfs.kns.maintenance.MaintainableImpl#doRouteStatusChange(org.kuali.kfs.kns.bo.DocumentHeader)
     */
    public void doRouteStatusChange(DocumentHeader documentHeader) {
        WorkflowDocument workflowDocument = documentHeader.getWorkflowDocument();

        if (workflowDocument.isProcessed() && !KFSConstants.MAINTENANCE_NEW_ACTION.equalsIgnoreCase(getMaintenanceAction())) {

            DocumentService documentService = SpringContext.getBean(DocumentService.class);
            MaintenanceDocument document;
            document = (MaintenanceDocument) documentService.getByDocumentHeaderId(documentHeader.getDocumentNumber());


            CheckReconciliation oldCr = (CheckReconciliation) document.getOldMaintainableObject().getBusinessObject();
            CheckReconciliation newCr = (CheckReconciliation) document.getNewMaintainableObject().getBusinessObject();

            if (ObjectUtils.isNotNull(oldCr) && !oldCr.getStatus().equalsIgnoreCase(newCr.getStatus())) {

                Date currentDate = SpringContext.getBean(DateTimeService.class).getCurrentSqlDate();

                newCr.setStatusChangeDate(currentDate);
                
                //KFSUPGRADE-377
                if(CRConstants.CANCELLED.equalsIgnoreCase(newCr.getStatus())){
                    newCr.setCancelDocHdrId(documentHeader.getDocumentNumber());
                }
            }

        }
    }

}