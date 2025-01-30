package edu.cornell.kfs.vnd.document;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.businessobject.VendorSupplierDiversity;
import org.kuali.kfs.vnd.document.VendorMaintainableImpl;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.document.authorization.FieldRestriction;
import org.kuali.kfs.kns.document.authorization.MaintenanceDocumentRestrictions;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.kns.web.ui.Row;
import org.kuali.kfs.kns.web.ui.Section;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.ObjectPropertyUtils;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.vnd.businessobject.CuVendorAddressExtension;
import edu.cornell.kfs.vnd.businessobject.CuVendorHeaderExtension;
import edu.cornell.kfs.vnd.businessobject.CuVendorSupplierDiversityExtension;
import edu.cornell.kfs.vnd.businessobject.WorkdayKfsVendorLooupResultsDTO;
import edu.cornell.kfs.vnd.service.CuVendorWorkDayService;

public class CuVendorMaintainableImpl extends VendorMaintainableImpl {
    private static final Logger LOG = LogManager.getLogger();
    private static final String HEADER_ID_SEQ = "VNDR_HDR_GNRTD_ID";
    private static final String ADDRESS_HEADER_ID_SEQ = "VNDR_ADDR_GNRTD_ID";
    private static final String VENDOR_SECTION_ID = "Vendor";
    private static final String PROC_METHODS_FIELD_NAME = "extension.procurementMethods";
    private static final String PROC_METHODS_MULTISELECT_FIELD_NAME = "extension.procurementMethodsArray";
    private static final String MULTISELECT_FIELD_PATH_PREFIX = "dataObject.";
    private static final String REQUIRES_VENDOR_TAX_ID_MANAGER = "RequiresVendorTaxIdManager";
    
    protected transient PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    
    @Override
    public void saveBusinessObject() {
        final VendorDetail vendorDetail = (VendorDetail) super.getBusinessObject();

            // a  workaround for now.  headerextension's pk is not linked
        populateGeneratedHerderId(vendorDetail.getVendorHeader());
        populateGeneratedAddressId(vendorDetail);
        vendorDetail.setVendorHeaderGeneratedIdentifier(vendorDetail.getVendorHeader().getVendorHeaderGeneratedIdentifier());
        super.saveBusinessObject();

    }

    @Override
    protected boolean answerSplitNodeQuestion(final String nodeName) {
        final Document document = SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(getDocumentNumber());
        
         
        if (nodeName.equals(VENDOR_REQUIRES_APPROVAL_SPLIT_NODE)) {
            return true;
        }
        
        if (nodeName.equals(REQUIRES_VENDOR_TAX_ID_MANAGER)) {
            if (enableTaxIdReviewerNode()) {
                final VendorDetail vendorDetail = (VendorDetail) super.getBusinessObject();
                String vendorTaxNumber;
                if (vendorDetail.getVendorHeader().getVendorForeignIndicator()) {
                    vendorTaxNumber = vendorDetail.getVendorHeader().getVendorForeignTaxId();
                } else {
                    vendorTaxNumber = vendorDetail.getVendorHeader().getVendorTaxNumber();
                }
                return isVendorTypeApplicableForTaxIdRoute(vendorDetail) && isVendorTaxNumberInWorkday(vendorTaxNumber);
            } else {
                LOG.debug("answerSplitNodeQuestion, tax id manager route node has been disabled");
                return false;
            }
        }
        
        return super.answerSplitNodeQuestion(nodeName);
    }
    
    private boolean enableTaxIdReviewerNode() {
        /*
         * @todo drive this off a parameter
         */
        return true;
    }
    
    private boolean isVendorTypeApplicableForTaxIdRoute(final VendorDetail vendorDetail) {
        boolean shouldRouteForTaxId = StringUtils.equalsIgnoreCase(vendorDetail.getVendorHeader().getVendorOwnershipCode(), 
                CUPurapConstants.JaggaerLegalStructure.INDIVIDUAL.kfsOwnerShipTypeCode);
        LOG.info("isVendorTypeApplicableForTaxIdRoute, returning {}", shouldRouteForTaxId);
        return shouldRouteForTaxId;
    }
    
    private boolean isVendorTaxNumberInWorkday(final String vendorTaxNumber) {
        try {
            CuVendorWorkDayService workDayService = SpringContext.getBean(CuVendorWorkDayService.class);
            WorkdayKfsVendorLooupResultsDTO result = workDayService.findEmployeeBySocialSecurityNumber(vendorTaxNumber);
            boolean isInWorkDay = result.isActive();
            LOG.info("isVendorTaxNumberInWorkday, returning {}", isInWorkDay);
            return isInWorkDay;
        } catch (RuntimeException e) {
            LOG.error("isVendorTaxNumberInWorkday, got an error calling workday.", e);
            
            return true;
        }
        
    }

    private void populateGeneratedHerderId(final VendorHeader vendorHeader) {
        if (vendorHeader.getVendorHeaderGeneratedIdentifier() == null) {
            final Integer generatedHeaderId = SpringContext.getBean(SequenceAccessorService.class).getNextAvailableSequenceNumber(HEADER_ID_SEQ).intValue();
            vendorHeader.setVendorHeaderGeneratedIdentifier(generatedHeaderId.intValue());
            ((CuVendorHeaderExtension) vendorHeader.getExtension()).setVendorHeaderGeneratedIdentifier(generatedHeaderId);
        }
        if (CollectionUtils.isNotEmpty(vendorHeader.getVendorSupplierDiversities())) {
            for (final VendorSupplierDiversity supplierDiversity : vendorHeader.getVendorSupplierDiversities()) {
                supplierDiversity.setVendorHeaderGeneratedIdentifier(vendorHeader.getVendorHeaderGeneratedIdentifier());
                ((CuVendorSupplierDiversityExtension)supplierDiversity.getExtension()).setVendorHeaderGeneratedIdentifier(vendorHeader.getVendorHeaderGeneratedIdentifier());
                ((CuVendorSupplierDiversityExtension)supplierDiversity.getExtension()).setVendorSupplierDiversityCode(supplierDiversity.getVendorSupplierDiversityCode());
            }
        }
    }

    private void populateGeneratedAddressId(final VendorDetail vendorDetail) {
        if (CollectionUtils.isNotEmpty(vendorDetail.getVendorAddresses())) {
            for (final VendorAddress vendorAddress : vendorDetail.getVendorAddresses()) {
                if (vendorAddress.getVendorAddressGeneratedIdentifier() == null) {
                    final Integer generatedHeaderId = SpringContext.getBean(SequenceAccessorService.class).getNextAvailableSequenceNumber(ADDRESS_HEADER_ID_SEQ).intValue();
                    vendorAddress.setVendorAddressGeneratedIdentifier(generatedHeaderId);
                    ((CuVendorAddressExtension)vendorAddress.getExtension()).setVendorAddressGeneratedIdentifier(generatedHeaderId);
                }

            }
        }
 
    }

    @Override 
    public List<MaintenanceLock> generateMaintenanceLocks() {
        final VendorDetail vendor = (VendorDetail) getBusinessObject();
        if (ObjectUtils.isNull(vendor.getVendorDetailAssignedIdentifier())) {
            return new ArrayList<>();
        }

        final List<MaintenanceLock> maintenanceLocks = new ArrayList<MaintenanceLock>();
        
        final Class dataObjectClass = this.getDataObjectClass();
        final StringBuffer lockRepresentation = new StringBuffer(dataObjectClass.getName());
        lockRepresentation.append(KRADConstants.Maintenance.LOCK_AFTER_CLASS_DELIM);

        final Object bo = getDataObject();
        final List keyFieldNames = getDocumentDictionaryService().getLockingKeys(getDocumentTypeName());

        final StringBuffer old = new StringBuffer();
        old.append(lockRepresentation);
        for (Iterator i = keyFieldNames.iterator(); i.hasNext(); ) {
            final String fieldName = (String) i.next();
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
        
        final MaintenanceLock maintenanceLock = new MaintenanceLock();
        maintenanceLock.setDocumentNumber(this.getDocumentNumber());
        maintenanceLock.setLockingRepresentation(lockRepresentation.toString());
        maintenanceLocks.add(maintenanceLock);
        
        final VendorDetail parent = vendor.getVendorParent();
        if (ObjectUtils.isNotNull(parent)) {
            final MaintenanceLock parentLock = getMaintenanceLockService().createMaintenanceLock(
                    getDocumentNumber(),
                    parent
            );
            maintenanceLocks.add(parentLock);
        }

        return maintenanceLocks;
    
    }

    /**
     * Overridden to forcibly populate the multi-select procurementMethodsArray KNS field values,
     * and to forcibly hide the procurementMethods field's row.
     * 
     * @see org.kuali.kfs.kns.maintenance.MaintainableImpl#getSections(org.kuali.kfs.kns.document.MaintenanceDocument,
     *      org.kuali.kfs.kns.maintenance.Maintainable)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public List getSections(final MaintenanceDocument document, final Maintainable oldMaintainable) {
        @SuppressWarnings("unchecked")
        final List<Section> sections = super.getSections(document, oldMaintainable);
        final MaintenanceDocumentRestrictions restrictions = KNSServiceLocator.getBusinessObjectAuthorizationService().getMaintenanceDocumentRestrictions(
                document, GlobalVariables.getUserSession().getPerson());
        
        // Perform the forcible updates on the generated sections.
        boolean doneWithSections = false;
        for (int i = 0; !doneWithSections && i < sections.size(); i++) {
            final Section section = sections.get(i);
            if (VENDOR_SECTION_ID.equals(section.getSectionId())) {
                // Find and update the appropriate fields/rows.
                final List<Row> rows = section.getRows();
                int fieldsDone = 0;
                for (int j = 0; fieldsDone < 2 && j < rows.size(); j++) {
                    final List<Field> fields = rows.get(j).getFields();
                    for (int k = 0; fieldsDone < 2 && k < fields.size(); k++) {
                        final String fieldName = fields.get(k).getPropertyName();
                        if (PROC_METHODS_MULTISELECT_FIELD_NAME.equals(fieldName)) {
                            // Update the property values and security on the multiselect field.
                            setupMultiselectField(document, restrictions, fields.get(k), MULTISELECT_FIELD_PATH_PREFIX + fieldName);
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

    /*
     * Convenience method for performing final custom setup of multiselect fields.
     * KNS maintenance documents do not populate multiselect property values on such fields,
     * and a FieldUtils limitation will make it skip multiselect read-only/hidden restriction setup.
     * Thus, this method will forcibly update the multiselect's property values from the BO,
     * as well as configure the field's read-only/hidden restrictions.
     */
    private void setupMultiselectField(final MaintenanceDocument document, final MaintenanceDocumentRestrictions restrictions, final Field field, final String propertyPath) {
        // Manually update the property values.
        final Object val = ObjectPropertyUtils.getPropertyValue(this, propertyPath);
        field.setPropertyValues((String[]) val);
        
        // Update the read-only and hidden restrictions accordingly, similar to the setup in the FieldUtils.applyAuthorization() method.
        if (document.getNewMaintainableObject() == this && restrictions.hasRestriction(field.getPropertyName())) {
            final FieldRestriction restriction = restrictions.getFieldRestriction(field.getPropertyName());
            // Copied and tweaked the code below from the FieldUtils.applyAuthorization() method.
            if (restriction.isReadOnly()) {
                if (!field.isReadOnly() && !restriction.isMasked() && !restriction.isPartiallyMasked()) {
                    field.setReadOnly(true);
                }
            } else if (restriction.isHidden()) {
                if (field.getFieldType() != Field.HIDDEN) {
                    field.setFieldType(Field.HIDDEN);
                }
            }
            
            if (field.isReadOnly() && restriction.isHidden()) {
                field.setFieldType(Field.HIDDEN);
            }
        }
        
    }
    
    @Override
    public void doRouteStatusChange(final DocumentHeader header) {
        LOG.debug("doRouteStatusChange: entering");
        super.doRouteStatusChange(header);
        final VendorDetail vendorDetail = (VendorDetail) getBusinessObject();
        final WorkflowDocument workflowDoc = header.getWorkflowDocument();
        
        if (workflowDoc.isProcessed()) {
            LOG.debug("doRouteStatusChange: workflow is processed");
            if (recordPvenRouteStatusChangeForPaymentWorksIsEnabled()) {
                performPaymentWorksApprovalProcessingForVendor(vendorDetail, this.getDocumentNumber(), this.getMaintenanceAction());
            }
        }
        else if (workflowDoc.isDisapproved() || workflowDoc.isCanceled()) {
            LOG.debug("doRouteStatusChange: disapproved or canceled");
            if (recordPvenRouteStatusChangeForPaymentWorksIsEnabled()) {
                performPaymentWorksDisapprovalCancelProcessingForVendor(vendorDetail, this.getDocumentNumber(), this.getMaintenanceAction());
            }
        }
    }
    
    private void performPaymentWorksApprovalProcessingForVendor(final VendorDetail vendorDetail, final String kfsDocumentNumber, final String maintenanceAction) {
        if (isExistingPaymentWorksVendor(kfsDocumentNumber) && isMaintenanceActionNewOrNewWithExisting(maintenanceAction)) {
            LOG.debug("performPaymentWorksApprovalProcessingForVendor: isExistingPaymentWorksVendor");
            this.saveBusinessObject();
            processExistingPaymentWorksVendorApproval(kfsDocumentNumber, vendorDetail);
        }
        else if (isMaintenanceActionNewOrNewWithExisting(maintenanceAction)) {
            LOG.debug("performPaymentWorksApprovalProcessingForVendor: new action resulting from AVf or hand keying");
            this.saveBusinessObject();
            processKfsVendorNewActionApproval(kfsDocumentNumber, vendorDetail);
        }
        else if (StringUtils.equals(KRADConstants.MAINTENANCE_EDIT_ACTION, maintenanceAction)) {
            LOG.debug("performPaymentWorksApprovalProcessingForVendor: edit action");
            processKfsVendorEditActionApproval(kfsDocumentNumber, vendorDetail);
        }
        else {
            LOG.info("performPaymentWorksApprovalProcessingForVendor: No PVEN Approval processing performed for PMW because not existing PMW vendor -OR- KFS PVEN maintenance action not currently being tracked for PMW.");
        }
    }
    
    private boolean isMaintenanceActionNewOrNewWithExisting(final String maintenanceAction) {
        return (StringUtils.equals(KRADConstants.MAINTENANCE_NEW_ACTION, maintenanceAction)
                || StringUtils.equals(KRADConstants.MAINTENANCE_NEWWITHEXISTING_ACTION, maintenanceAction));
    }
    
    private boolean isExistingPaymentWorksVendor(final String kfsDocumentNumber) {
        return getPaymentWorksBatchUtilityService().foundExistingPaymentWorksVendorByKfsDocumentNumber(kfsDocumentNumber);
    }
    
    private boolean recordPvenRouteStatusChangeForPaymentWorksIsEnabled() {
        return getPaymentWorksBatchUtilityService().isPaymentWorksIntegrationProcessingEnabled();
    }
    
    private void processExistingPaymentWorksVendorApproval(final String kfsDocumentNumber, final VendorDetail vendorDetail) {
        getPaymentWorksBatchUtilityService().registerKfsPvenApprovalForExistingPaymentWorksVendor(kfsDocumentNumber, vendorDetail);
    }
    
    private void processKfsVendorNewActionApproval(final String kfsDocumentNumber, final VendorDetail vendorDetail) {
        getPaymentWorksBatchUtilityService().registerKfsPvenApprovalForKfsEnteredVendor(kfsDocumentNumber, vendorDetail);
    }
    
    private void processKfsVendorEditActionApproval(final String kfsDocumentNumber, final VendorDetail vendorDetail) {
        getPaymentWorksBatchUtilityService().registerKfsPvenApprovalForKfsEditedVendor(kfsDocumentNumber, vendorDetail);
    }
    
    private void performPaymentWorksDisapprovalCancelProcessingForVendor(final VendorDetail vendorDetail, final String kfsDocumentNumber, final String maintenanceAction) {
        if (isExistingPaymentWorksVendor(kfsDocumentNumber) && (isMaintenanceActionNewOrNewWithExisting(maintenanceAction))) {
            getPaymentWorksBatchUtilityService().registerKfsPvenDisapprovalForExistingPaymentWorksVendor(kfsDocumentNumber, vendorDetail);
        }
        else {
            LOG.info("performPaymentWorksDisapprovalCancelProcessingForVendor: No PVEN Disapproval processing performed for PMW because not existing PMW vendor with maintenance action currently being tracked for PMW.");
        }
    }
    
    public PaymentWorksBatchUtilityService getPaymentWorksBatchUtilityService() {
        if (paymentWorksBatchUtilityService == null) {
            paymentWorksBatchUtilityService = SpringContext.getBean(PaymentWorksBatchUtilityService.class);
        }
        return paymentWorksBatchUtilityService;
    }

    public void setPaymentWorksBatchUtilityService(final PaymentWorksBatchUtilityService paymentWorksBatchUtilityService) {
        this.paymentWorksBatchUtilityService = paymentWorksBatchUtilityService;
    }
}
