package com.rsmart.kuali.kfs.cr.document;

import java.sql.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;

/**
 * This class overrides the functionality in the base FinancialSystemMaintainable to update the status change date when the
 * staus of a check is changed.
 */
public class CheckReconciliationMaintainableImpl extends FinancialSystemMaintainable {
    private static final Logger LOG = LogManager.getLogger();

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
    
    @Override
    public void processAfterEdit(final MaintenanceDocument document, final Map<String, String[]> requestParameters) {
        LOG.debug("processAfterEdit, entering");
        super.processAfterEdit(document, requestParameters);
        if (document != null && document.getDocumentHeader() != null) {
            if (StringUtils.isBlank(document.getDocumentHeader().getOrganizationDocumentNumber())) {
                CheckReconciliation newCr = (CheckReconciliation) document.getNewMaintainableObject().getBusinessObject();
                String checkNumber = newCr.getCheckNumber().toString();
                document.getDocumentHeader().setOrganizationDocumentNumber(checkNumber);
                LOG.debug("processAfterEdit, setting org ref id to '{}'", checkNumber);
            } else {
                LOG.debug("processAfterEdit, org ref id already set to {}", document.getDocumentHeader().getOrganizationDocumentNumber());
            }
        }
    }

}