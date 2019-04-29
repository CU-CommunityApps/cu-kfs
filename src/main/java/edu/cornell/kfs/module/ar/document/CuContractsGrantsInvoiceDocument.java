package edu.cornell.kfs.module.ar.document;

import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.framework.postprocessor.DocumentRouteStatusChange;

import edu.cornell.kfs.module.ar.document.service.CuContractsGrantsInvoiceDocumentService;

public class CuContractsGrantsInvoiceDocument extends ContractsGrantsInvoiceDocument {

    private static final long serialVersionUID = 3934462638032882286L;

    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        if (getDocumentHeader().getWorkflowDocument().isProcessed()) {
            CuContractsGrantsInvoiceDocumentService cuContractsGrantsInvoiceDocumentService = SpringContext.getBean(
                    CuContractsGrantsInvoiceDocumentService.class);
            cuContractsGrantsInvoiceDocumentService.setInvoiceDueDateBasedOnNetTermsAndCurrentDate(this);
        }
        
        super.doRouteStatusChange(statusChangeEvent);
    }

}
