package edu.cornell.kfs.fp.document.authorization;

import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.authorization.DisbursementVoucherDocumentPresentationController;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KfsAuthorizationConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.kfs.krad.document.Document;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.service.CULegacyTravelService;
import edu.cornell.kfs.sys.CUKFSAuthorizationConstants;

public class CuDisbursementVoucherDocumentPresentationController extends DisbursementVoucherDocumentPresentationController{

    private static final long serialVersionUID = 1L;
	private static final Logger LOG = LogManager.getLogger(CuDisbursementVoucherDocumentPresentationController.class);
    
    @Override
    public Set<String> getEditModes(Document document) {
        LOG.info("Checking presentation permissions for DV.");
        Set<String> editModes = super.getEditModes(document);
        addTravelEntryMode(document, editModes);
        addTravelSystemGeneratedEntryMode(document, editModes);
        editModes.add(CUKFSAuthorizationConstants.DisbursementVoucherEditMode.DISPLAY_INVOICE_FIELDS); 

        return editModes;
    }
    
    @Override
    protected void addTravelEntryMode(Document document, Set<String> editModes) {
        final WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();

        final Set<String> currentRouteLevels = workflowDocument.getCurrentNodeNames();
        if (CollectionUtils.isNotEmpty(currentRouteLevels)) {
            if (currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.ACCOUNT)) {  
                editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.TRAVEL_ENTRY);
            }
        }
    }       
    
    /**
     * If the DV was generated from a connection with the Cornell Travel System, enforces special edit rules regarding information on the disbursement voucher.
     * 
     * Added condition that DV is not in the Payment Method Reviewers' queue, as the check amount needs to be editable for that circustance.
     * 
     * @param document the disbursement voucher document authorization is being sought on
     * @param editModes the edit modes so far, which can be added to
     */
    protected void addTravelSystemGeneratedEntryMode(Document document, Set<String> editModes) {
        final CuDisbursementVoucherDocument dvDocument = (CuDisbursementVoucherDocument)document;
        final WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        
        final Set<String> currentRouteLevels = workflowDocument.getCurrentNodeNames();
        boolean isAssociatedWithTrip = SpringContext.getBean(CULegacyTravelService.class).isCULegacyTravelIntegrationInterfaceAssociatedWithTrip(dvDocument);
        if(isAssociatedWithTrip && !currentRouteLevels.contains(KFSConstants.RouteLevelNames.PAYMENT_METHOD)) {
            LOG.info("Checking travel system generated entry permissions.");
            editModes.add(CUKFSAuthorizationConstants.DisbursementVoucherEditMode.TRAVEL_SYSTEM_GENERATED_ENTRY); 
        }
    }

}
