package edu.cornell.kfs.fp.document.authorization;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.document.authorization.DisbursementVoucherDocumentPresentationController;
import org.kuali.kfs.krad.document.Document;

import edu.cornell.kfs.sys.CUKFSAuthorizationConstants;

public class CuDisbursementVoucherDocumentPresentationController extends DisbursementVoucherDocumentPresentationController{

    private static final long serialVersionUID = 1L;
	private static final Logger LOG = LogManager.getLogger(CuDisbursementVoucherDocumentPresentationController.class);
    
    @Override
    public Set<String> getEditModes(final Document document) {
        LOG.info("Checking presentation permissions for DV.");
        final Set<String> editModes = super.getEditModes(document);
        editModes.add(CUKFSAuthorizationConstants.DisbursementVoucherEditMode.DISPLAY_INVOICE_FIELDS); 

        return editModes;
    }

}
