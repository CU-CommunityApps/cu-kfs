package edu.cornell.kfs.vnd.document;

import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.document.authorization.FieldRestriction;
import org.kuali.kfs.kns.document.authorization.MaintenanceDocumentRestrictions;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.kns.web.ui.Row;
import org.kuali.kfs.kns.web.ui.Section;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectPropertyUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorAlias;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.businessobject.VendorSupplierDiversity;
import org.kuali.kfs.vnd.document.VendorMaintainableImpl;

import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.vnd.CUVendorKeyConstants;
import edu.cornell.kfs.vnd.CuVendorParameterConstants;
import edu.cornell.kfs.vnd.businessobject.CuVendorAddressExtension;
import edu.cornell.kfs.vnd.businessobject.CuVendorHeaderExtension;
import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupResult;
import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupRoot;
import edu.cornell.kfs.vnd.service.CuVendorWorkDayService;

public class CuVendorMaintainableImpl extends VendorMaintainableImpl {
    private static final long serialVersionUID = -181405787799707576L;
    private static final Logger LOG = LogManager.getLogger();
    private static final String HEADER_ID_SEQ = "VNDR_HDR_GNRTD_ID";
    private static final String ADDRESS_HEADER_ID_SEQ = "VNDR_ADDR_GNRTD_ID";
    private static final String VENDOR_SECTION_ID = "Vendor";
    private static final String PROC_METHODS_FIELD_NAME = "extension.procurementMethods";
    private static final String PROC_METHODS_MULTISELECT_FIELD_NAME = "extension.procurementMethodsArray";
    private static final String MULTISELECT_FIELD_PATH_PREFIX = "dataObject.";
    private static final String REQUIRES_VENDOR_TAX_ID_MANAGER = "RequiresVendorTaxIdManager";
    
    protected transient PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    protected transient ConfigurationService configurationService;
    protected transient ParameterService parameterService;
    protected transient DocumentService documentService;
    protected transient CuVendorWorkDayService cuVendorWorkDayService;
    
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(CUKFSConstants.DATE_FORMAT_yyyy_MM_dd, Locale.US);
    
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
        if (nodeName.equals(VENDOR_REQUIRES_APPROVAL_SPLIT_NODE)) {
            return true;
        }

        if (nodeName.equals(REQUIRES_VENDOR_TAX_ID_MANAGER)) {
            if (isTaxIdReviewerNodeEnabled()) {
                final VendorDetail vendorDetail = (VendorDetail) super.getBusinessObject();
                String vendorTaxNumber;
                if (vendorDetail.getVendorHeader().getVendorForeignIndicator()) {
                    vendorTaxNumber = vendorDetail.getVendorHeader().getVendorForeignTaxId();
                } else {
                    vendorTaxNumber = vendorDetail.getVendorHeader().getVendorTaxNumber();
                }
                return isVendorOwnershipCodeApplicableForTaxIdRoute(vendorDetail)
                        && isVendorTaxNumberInWorkday(vendorTaxNumber);
            } else {
                LOG.debug("answerSplitNodeQuestion, tax id manager route node has been disabled");
                return false;
            }
        }

        return super.answerSplitNodeQuestion(nodeName);
    }
    
    private boolean isTaxIdReviewerNodeEnabled() {
        boolean enabled = getParameterService().getParameterValueAsBoolean(VendorDetail.class,
                CuVendorParameterConstants.VENDOR_TAX_ID_REVIEW_NODE_ENABLED);
        LOG.debug("isTaxIdReviewerNodeEnabled, returning {}", enabled);
        return enabled;
    }
    
    private boolean isVendorOwnershipCodeApplicableForTaxIdRoute(final VendorDetail vendorDetail) {
        Collection<String> ownerShipCodes = getParameterService().getParameterValuesAsString(VendorDetail.class,
                CuVendorParameterConstants.VENDOR_OWNERSHIP_CODES_FOR_TAX_ID_REVIEW);
        boolean shouldRouteForTaxId = ownerShipCodes.contains(vendorDetail.getVendorHeader().getVendorOwnershipCode());
        LOG.debug("isVendorOwnershipCodeApplicableForTaxIdRoute, returning {} for document {}", shouldRouteForTaxId, getDocumentNumber());
        return shouldRouteForTaxId;
    }
    
    private boolean isVendorTaxNumberInWorkday(final String vendorTaxNumber) {
        try {
            WorkdayKfsVendorLookupRoot result = getCuVendorWorkDayService().findEmployeeBySocialSecurityNumber(vendorTaxNumber, getDocumentNumber());
            final boolean isActiveOrTerminatedEmployee = isActiveOrTerminatedEmployeeWithinDateRange(result);
            if (isActiveOrTerminatedEmployee) {
                String message;
                if (isActiveEmployee(result)) {
                    message = getConfigurationService().getPropertyValueAsString(CUVendorKeyConstants.ACTIVE_EMPLOYEE_MESSAGE);
                } else {
                    message = MessageFormat.format(getConfigurationService().getPropertyValueAsString(CUVendorKeyConstants.TERMINATED_EMPLOYEE_MESSAGE),
                            getEmployeeTerminationNumberOfDays());
                }
                addSearchAliasAndSaveVendorDetailIfNeeded(result);
                annotateDocument(message);
            }
            LOG.debug("isVendorTaxNumberInWorkday, returning {} for document {}", isActiveOrTerminatedEmployee, getDocumentNumber());
            return isActiveOrTerminatedEmployee;
        } catch (RuntimeException | URISyntaxException e) {
            LOG.error("isVendorTaxNumberInWorkday, got an error calling workday for document " + getDocumentNumber(), e);
            String message = getConfigurationService().getPropertyValueAsString(CUVendorKeyConstants.VENDOR_UNABLE_TO_CALL_WORKDAY);
            annotateDocument(message);
            return true;
        }
    }
    
    public boolean isActiveEmployee(WorkdayKfsVendorLookupRoot root) {
        return root != null && root.getResults().stream().anyMatch(result -> result.isActive());
    }

    public boolean isActiveOrTerminatedEmployeeWithinDateRange(WorkdayKfsVendorLookupRoot root) {
        return root != null && 
                root.getResults().stream().anyMatch(result -> result.isActive() || isTerminatedWithinDateRange(result));
    }

    public boolean isTerminatedWithinDateRange(WorkdayKfsVendorLookupResult result) {
        if (result != null && !result.isActive()) {
            String employeeTerminationDateString = result.getTerminationDate();
            if (StringUtils.isNotBlank(employeeTerminationDateString)) {
                LocalDateTime employeeTerminationDate = LocalDate.parse(employeeTerminationDateString, DATE_FORMATTER).atStartOfDay();
                LocalDateTime minimumTerminationDate = LocalDate.now().minus(getEmployeeTerminationNumberOfDays(), ChronoUnit.DAYS).atStartOfDay();
                boolean isTerminatedInRange = employeeTerminationDate.compareTo(minimumTerminationDate) >= 0;
                
                LOG.debug("isTerminatedWithinDateRange, the employee termination date string is {}", employeeTerminationDateString);
                LOG.debug("isTerminatedWithinDateRange, the minimum termination date to be routed to the tax id review route node is {}", minimumTerminationDate);
                LOG.debug("isTerminatedWithinDateRange, returning {} for netid {}", isTerminatedInRange, result.getNetID());

                return isTerminatedInRange;
            }
            LOG.warn("isTerminatedWithinDateRange, found an empty termination date, returning true by default for netid {}", result.getNetID());
            return true;
        }
        return false;
    }
    
    private int getEmployeeTerminationNumberOfDays() {
        try {
            String numberOfDaysString = getParameterService().getParameterValueAsString(VendorDetail.class,
                    CuVendorParameterConstants.EMPLOYEE_TERMINATION_NUMBER_OF_DAYS_FOR_TAX_ID_REVIEW);
            return Integer.parseInt(numberOfDaysString);
        } catch (Exception e) {
            LOG.error("getEmployeeTerminationNumberOfDays, unable to get the number of days from the parameter EMPLOYEE_TERMINATION_NUMBER_OF_DAYS_FOR_TAX_ID_REVIEW, returning 365", e);
            return 365;
        }
    }
    
    private void addSearchAliasAndSaveVendorDetailIfNeeded(WorkdayKfsVendorLookupRoot root) {
        String employeeId = findEmployeeId(root);
        if (StringUtils.isNotBlank(employeeId)) {
            MaintenanceDocument document = (MaintenanceDocument) getDocumentService()
                    .getByDocumentHeaderId(getDocumentNumber());
            if (ObjectUtils.isNotNull(document)) {
                VendorDetail newVendorDetail = (VendorDetail) document.getNewMaintainableObject().getBusinessObject();
                VendorDetail oldVendorDetail = (VendorDetail) document.getOldMaintainableObject().getBusinessObject();
                VendorAlias alias = findEmployeeIdAlias(newVendorDetail, employeeId);

                if (alias == null) {
                    LOG.debug("addSearchAliasAndSaveVendorDetailIfNeeded, vendor {}, adding alias named {}",
                            newVendorDetail.getVendorNumber(), employeeId);
                    addEmployeeIdliasToNewVendorDetail(newVendorDetail, employeeId);
                    
                    /*
                     * We need to add a blank alias to the old vendor detail so the maintenance document can properly display all the changes
                     */
                    addBlankAliasToOldVendorDetail(oldVendorDetail);
                    
                    saveDocumentInNewGlobalVariables(document);
                    if (alias.isActive()) {
                        LOG.debug("addSearchAliasAndSaveVendorDetailIfNeeded, vendor {}, already has active alias named {}",
                                newVendorDetail.getVendorNumber(), employeeId);
                    } else {
                        LOG.debug("addSearchAliasAndSaveVendorDetailIfNeeded, vendor {}, updating alias named {} to active",
                                newVendorDetail.getVendorNumber(), employeeId);
                        alias.setActive(true);
                        saveDocumentInNewGlobalVariables(document);
                    }
                }
            }
        }
    }
    
    private String findEmployeeId(WorkdayKfsVendorLookupRoot root) {
        String employeeId = StringUtils.EMPTY;
        if (root == null || root.getResults().get(0) == null) {
            LOG.error("findEmployeeId, for document {}, WorkdayKfsVendorLookupRoot was null, or result list was empty, this should not happen",
                    getDocumentNumber());
        } else {
            WorkdayKfsVendorLookupResult result = root.getResults().get(0);
            employeeId = result.getEmployeeID();
            if (StringUtils.isBlank(employeeId)) {
                LOG.error("findEmployeeId, for document {}, there was a Workday response, but no employee id was provided, this should not happen",
                        getDocumentNumber());
            }
        }
        return employeeId;
    }
    
    private VendorAlias findEmployeeIdAlias(VendorDetail vendorDetail, String employeeId) {
        for (VendorAlias alias : vendorDetail.getVendorAliases()) {
            if (StringUtils.equalsAnyIgnoreCase(employeeId, alias.getVendorAliasName())) {
                return alias;
            }
        }
        return null;
    }
    
    private void addEmployeeIdliasToNewVendorDetail(VendorDetail newVendorDetail, String employeeId) {
        VendorAlias alias = new VendorAlias();
        alias.setActive(true);
        alias.setVendorAliasName(employeeId);
        alias.setNewCollectionRecord(true);
        newVendorDetail.getVendorAliases().add(alias);
    }

    private void addBlankAliasToOldVendorDetail(VendorDetail oldVendorDetail) {
        VendorAlias blankAlias = new VendorAlias();
        blankAlias.setActive(false);
        blankAlias.setNewCollectionRecord(true);
        oldVendorDetail.getVendorAliases().add(blankAlias);
    }

    private void saveDocumentInNewGlobalVariables(MaintenanceDocument document) {
        try {
            GlobalVariables.doInNewGlobalVariables(new UserSession(KFSConstants.SYSTEM_USER), new Callable<Object>() {
                @Override
                public Object call() throws WorkflowException {
                    getDocumentService().saveDocument(document);
                    return document;
                }
            });
        } catch (Exception e) {
            LOG.error("saveDocumentInNewGlobalVariables, unable to save document", e);
            throw new RuntimeException(e);
        }
    }
    
    @SuppressWarnings("deprecation")
    private void annotateDocument(final String message) {
        try {
            GlobalVariables.doInNewGlobalVariables(new UserSession(KFSConstants.SYSTEM_USER), new Callable<Object>() {
                @Override
                public Object call() throws WorkflowException {
                    final Document document = getDocumentService().getByDocumentHeaderId(getDocumentNumber());
                    if (ObjectUtils.isNotNull(document)) {
                        final WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
                        if (ObjectUtils.isNotNull(workflowDocument)) {
                            LOG.debug("annotateDocument, the annotation message: {}", message);
                            workflowDocument.logAnnotation(message);
                            return document;
                        } else {
                            throw new WorkflowException("Unable to get workflow document for document id " + getDocumentNumber());
                        }
                    } else {
                        throw new WorkflowException("Unable to get document for document id " + getDocumentNumber());
                    }
                }
            });
        } catch (Exception e) {
            LOG.error("annotateCouldNotCallWorkDay, unable to annotate in new user session that workday could not be called", e);
            throw new RuntimeException(e);
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
                supplierDiversity.setVendorHeaderGeneratedIdentifier(vendorHeader.getVendorHeaderGeneratedIdentifier());
                supplierDiversity.setVendorSupplierDiversityCode(supplierDiversity.getVendorSupplierDiversityCode());
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

    public ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = SpringContext.getBean(ConfigurationService.class);
        }
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public ParameterService getParameterService() {
        if (parameterService == null) {
            parameterService = SpringContext.getBean(ParameterService.class);
        }
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public DocumentService getDocumentService() {
        if (documentService == null) {
            documentService = SpringContext.getBean(DocumentService.class);
        }
        return documentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public CuVendorWorkDayService getCuVendorWorkDayService() {
        if (cuVendorWorkDayService == null) {
            cuVendorWorkDayService = SpringContext.getBean(CuVendorWorkDayService.class);
        }
        return cuVendorWorkDayService;
    }

    public void setCuVendorWorkDayService(CuVendorWorkDayService cuVendorWorkDayService) {
        this.cuVendorWorkDayService = cuVendorWorkDayService;
    }
}
