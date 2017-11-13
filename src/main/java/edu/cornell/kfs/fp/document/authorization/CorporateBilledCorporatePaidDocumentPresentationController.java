package edu.cornell.kfs.fp.document.authorization;

import org.kuali.kfs.krad.document.Document;

public class CorporateBilledCorporatePaidDocumentPresentationController extends CuProcurementCardDocumentPresentationController {
    private static final long serialVersionUID = 5146419858161957920L;
    
    
    @Override
    public boolean canCancel(Document document) {
        return canRoute(document);
    }

}
