package edu.cornell.kfs.vnd.document;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.businessobject.VendorSupplierDiversity;
import org.kuali.kfs.vnd.document.VendorMaintainableImpl;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.maintenance.Maintainable;
import org.kuali.rice.kns.web.ui.Field;
import org.kuali.rice.kns.web.ui.Row;
import org.kuali.rice.kns.web.ui.Section;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.maintenance.MaintenanceLock;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.service.SequenceAccessorService;
import org.kuali.rice.krad.uif.util.ObjectPropertyUtils;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.vnd.businessobject.CuVendorAddressExtension;
import edu.cornell.kfs.vnd.businessobject.CuVendorHeaderExtension;
import edu.cornell.kfs.vnd.businessobject.CuVendorSupplierDiversityExtension;

public class CuVendorMaintainableImpl extends VendorMaintainableImpl {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuVendorMaintainableImpl.class);
    private static final String HEADER_ID_SEQ = "VNDR_HDR_GNRTD_ID";
    private static final String ADDRESS_HEADER_ID_SEQ = "VNDR_ADDR_GNRTD_ID";
    private static final String VENDOR_SECTION_ID = "Vendor";
    private static final String PROC_METHODS_FIELD_NAME = "extension.procurementMethods";
    private static final String PROC_METHODS_MULTISELECT_FIELD_NAME = "extension.procurementMethodsArray";
    private static final String PROC_METHODS_MULTISELECT_FIELD_PATH = "dataObject.extension.procurementMethodsArray";
    
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
         
        if (nodeName.equals(VENDOR_REQUIRES_APPROVAL_SPLIT_NODE)) {
            return true;
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

    @Override 
    public List<MaintenanceLock> generateMaintenanceLocks() {
        if (ObjectUtils.isNull(((VendorDetail) getBusinessObject()).getVendorDetailAssignedIdentifier())) {
            return new ArrayList();
        }

        List<MaintenanceLock> maintenanceLocks = new ArrayList<MaintenanceLock>();
        
        Class dataObjectClass = this.getDataObjectClass();
        StringBuffer lockRepresentation = new StringBuffer(dataObjectClass.getName());
        lockRepresentation.append(KRADConstants.Maintenance.LOCK_AFTER_CLASS_DELIM);

        Object bo = getDataObject();
        List keyFieldNames = getDocumentDictionaryService().getLockingKeys(getDocumentTypeName());

        StringBuffer old = new StringBuffer();
        old.append(lockRepresentation);
        for (Iterator i = keyFieldNames.iterator(); i.hasNext(); ) {
            String fieldName = (String) i.next();
            Object fieldValue = ObjectUtils.getPropertyValue(bo, fieldName);
            if (fieldValue == null) {
                fieldValue = "";
            }
            if (!i.hasNext()) {
            	//prevents the Vendor Detail info from being appended to the lock
            	//lock will be on the Vendor Header
            	break;
            }
            lockRepresentation.append(fieldName);
            lockRepresentation.append(KRADConstants.Maintenance.LOCK_AFTER_FIELDNAME_DELIM);
            lockRepresentation.append(String.valueOf(fieldValue));
            if (i.hasNext()) {
                lockRepresentation.append(KRADConstants.Maintenance.LOCK_AFTER_VALUE_DELIM);
            }
        }
        
        MaintenanceLock maintenanceLock = new MaintenanceLock();
        maintenanceLock.setDocumentNumber(this.getDocumentNumber());
        maintenanceLock.setLockingRepresentation(lockRepresentation.toString());
        maintenanceLocks.add(maintenanceLock);

        return maintenanceLocks;
	
    }

    /**
     * Overridden to forcibly populate the multi-select procurementMethodsArray KNS field values,
     * and to forcibly hide the procurementMethods field's row.
     * 
     * @see org.kuali.rice.kns.maintenance.KualiMaintainableImpl#getSections(org.kuali.rice.kns.document.MaintenanceDocument,
     *      org.kuali.rice.kns.maintenance.Maintainable)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public List getSections(MaintenanceDocument document, Maintainable oldMaintainable) {
        @SuppressWarnings("unchecked")
        List<Section> sections = super.getSections(document, oldMaintainable);
        
        // Perform the forcible updates on the generated sections.
        boolean doneWithSections = false;
        for (int i = 0; !doneWithSections && i < sections.size(); i++) {
            Section section = sections.get(i);
            if (VENDOR_SECTION_ID.equals(section.getSectionId())) {
                // Find and update the appropriate fields/rows.
                List<Row> rows = section.getRows();
                int fieldsDone = 0;
                for (int j = 0; fieldsDone < 2 && j < rows.size(); j++) {
                    List<Field> fields = rows.get(j).getFields();
                    for (int k = 0; fieldsDone < 2 && k < fields.size(); k++) {
                        String fieldName = fields.get(k).getPropertyName();
                        if (PROC_METHODS_MULTISELECT_FIELD_NAME.equals(fieldName)) {
                            // Update the property values on the multiselect field.
                            Object val = ObjectPropertyUtils.getPropertyValue(this, PROC_METHODS_MULTISELECT_FIELD_PATH);
                            fields.get(k).setPropertyValues((String[]) val);
                            fieldsDone++;
                        } else if (PROC_METHODS_FIELD_NAME.equals(fieldName)) {
                            // Hide the row containing the flattened version of the multiselect field.
                            rows.get(j).setHidden(true);
                            fieldsDone++;
                        }
                    }
                }
                doneWithSections = true;
            }
        }
        
        return sections;
    }

}
