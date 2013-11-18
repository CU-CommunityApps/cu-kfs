package edu.cornell.kfs.vnd.document;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.businessobject.VendorSupplierDiversity;
import org.kuali.kfs.vnd.document.VendorMaintainableImpl;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.service.SequenceAccessorService;
import org.kuali.rice.krad.util.KRADConstants;

import edu.cornell.kfs.vnd.businessobject.CuVendorAddressExtension;
import edu.cornell.kfs.vnd.businessobject.CuVendorHeaderExtension;
import edu.cornell.kfs.vnd.businessobject.CuVendorSupplierDiversityExtension;
import edu.cornell.kfs.vnd.document.service.CUVendorService;

public class CuVendorMaintainableImpl extends VendorMaintainableImpl {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuVendorMaintainableImpl.class);
    private static final String HEADER_ID_SEQ = "VNDR_HDR_GNRTD_ID";
    private static final String ADDRESS_HEADER_ID_SEQ = "VNDR_ADDR_GNRTD_ID";
    
    public void saveBusinessObject() {
        VendorDetail vendorDetail = (VendorDetail) super.getBusinessObject();

            // a  workaround for now.  headerextension's pk is not linked
        populateGeneratedHerderId(vendorDetail.getVendorHeader());
        populateGeneratedAddressId(vendorDetail);
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

    @Override
    protected boolean answerSplitNodeQuestion(String nodeName) {
        Document document = null;

        try {
            document = SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(getDocumentNumber());
        } catch (WorkflowException e) {
            LOG.debug("Vendor doc could not find doc to answerSplitNodeQuestion " + e.getMessage());
        }
 
        Person systemUser = getPersonService().getPersonByPrincipalName(KFSConstants.SYSTEM_USER);  
        
        if (nodeName.equals(VENDOR_REQUIRES_APPROVAL_SPLIT_NODE)) {
            return !StringUtils.equalsIgnoreCase(document.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId(), systemUser.getPrincipalId());
        }
        return super.answerSplitNodeQuestion(nodeName);
    }

    private void populateGeneratedHerderId(VendorHeader vendorHeader) {
        if (vendorHeader.getVendorHeaderGeneratedIdentifier() == null) {
            Integer generatedHeaderId = SpringContext.getBean(SequenceAccessorService.class).getNextAvailableSequenceNumber(HEADER_ID_SEQ).intValue();
            vendorHeader.setVendorHeaderGeneratedIdentifier(generatedHeaderId.intValue());
            ((CuVendorHeaderExtension)vendorHeader.getExtension()).setVendorHeaderGeneratedIdentifier(generatedHeaderId);
        }
        if (CollectionUtils.isNotEmpty(vendorHeader.getVendorSupplierDiversities())) {
            for (VendorSupplierDiversity supplierDiversity : vendorHeader.getVendorSupplierDiversities()) {
                supplierDiversity.setVendorHeaderGeneratedIdentifier(vendorHeader.getVendorHeaderGeneratedIdentifier());
                ((CuVendorSupplierDiversityExtension)supplierDiversity.getExtension()).setVendorHeaderGeneratedIdentifier(vendorHeader.getVendorHeaderGeneratedIdentifier());
                ((CuVendorSupplierDiversityExtension)supplierDiversity.getExtension()).setVendorSupplierDiversityCode(supplierDiversity.getVendorSupplierDiversityCode());
            }
        }
 
    }

    private void populateGeneratedAddressId(VendorDetail vendorDetail) {
        if (CollectionUtils.isNotEmpty(vendorDetail.getVendorAddresses())) {
            for (VendorAddress vendorAddress : vendorDetail.getVendorAddresses()) {
                if (vendorAddress.getVendorAddressGeneratedIdentifier() == null) {
                    Integer generatedHeaderId = SpringContext.getBean(SequenceAccessorService.class).getNextAvailableSequenceNumber(ADDRESS_HEADER_ID_SEQ).intValue();
                    vendorAddress.setVendorAddressGeneratedIdentifier(generatedHeaderId);
                    ((CuVendorAddressExtension)vendorAddress.getExtension()).setVendorAddressGeneratedIdentifier(generatedHeaderId);
                }

            }
        }
 
    }

}
