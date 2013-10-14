package edu.cornell.kfs.vnd.document;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.VendorMaintainableImpl;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.util.KRADConstants;

import edu.cornell.kfs.vnd.businessobject.CuVendorDetail;
import edu.cornell.kfs.vnd.businessobject.CuVendorHeader;

public class CuVendorMaintainableImpl extends VendorMaintainableImpl {
    private static final long serialVersionUID = 1L;
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuVendorMaintainableImpl.class);

    @Override
    public void saveBusinessObject() {
        CuVendorDetail vendorDetail = (CuVendorDetail) super.getBusinessObject();
        CuVendorHeader vendorHeader = vendorDetail.getVendorHeader();

        super.saveBusinessObject();
        try {
            Document document = SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(getDocumentNumber());
            VendorDetail vndDetail = (VendorDetail) ((MaintenanceDocument) document).getNewMaintainableObject().getBusinessObject();
            if (vndDetail.getVendorHeaderGeneratedIdentifier() == null
                    || KRADConstants.MAINTENANCE_NEWWITHEXISTING_ACTION.equals(getMaintenanceAction())) {
                ((MaintenanceDocument) document).getNewMaintainableObject().setBusinessObject(vendorDetail);
                SpringContext.getBean(DocumentService.class).saveDocument(document);
            }
        } catch (WorkflowException e) {
            LOG.debug("Vendor doc not saved successfully " + e.getMessage());
        }

    }

}
