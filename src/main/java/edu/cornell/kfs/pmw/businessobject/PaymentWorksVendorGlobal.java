package edu.cornell.kfs.pmw.businessobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.bo.GlobalBusinessObject;
import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetail;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.PaymentWorksVendorGlobalAction;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.SupplierUploadStatus;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;

public class PaymentWorksVendorGlobal extends PersistableBusinessObjectBase implements GlobalBusinessObject {

    private static final long serialVersionUID = 1L;

    private String documentNumber;
    private String actionTypeCode;
    private List<PaymentWorksVendorGlobalDetail> vendorDetails;

    private transient BusinessObjectService businessObjectService;

    public PaymentWorksVendorGlobal() {
        vendorDetails = new ArrayList<>();
    }

    @Override
    public List<PersistableBusinessObject> generateDeactivationsToPersist() {
        return new ArrayList<>();
    }

    @Override
    public List<PersistableBusinessObject> generateGlobalChangesToPersist() {
        final PaymentWorksVendorGlobalAction actionType = getActionType();
        if (actionType == null) {
            throw new IllegalStateException("No global action was specified");
        }
        switch (actionType) {
            case RESTAGE_FOR_UPLOAD :
                return generateChangesToRestagePaymentWorksVendorsForUpload();

            default :
                throw new IllegalStateException("Invalid global action: " + actionType);
        }
    }

    private List<PersistableBusinessObject> generateChangesToRestagePaymentWorksVendorsForUpload() {
        final Map<Integer, PaymentWorksVendorGlobalDetail> detailMappings = vendorDetails.stream()
                .collect(Collectors.toUnmodifiableMap(
                        PaymentWorksVendorGlobalDetail::getPmwVendorId, Function.identity()));

        final Map<String, ?> fieldValues = Map.ofEntries(
                Map.entry(KRADPropertyConstants.ID, List.copyOf(detailMappings.keySet()))
        );
        final Collection<PaymentWorksVendor> existingPmwVendors = getBusinessObjectService().findMatching(
                PaymentWorksVendor.class, fieldValues);
        final Set<Integer> encounteredPmwVendors = new HashSet<>();
        final List<PersistableBusinessObject> pmwVendorsToUpdate = new ArrayList<>(existingPmwVendors.size());

        if (vendorDetails.size() != existingPmwVendors.size()) {
            throw new IllegalStateException("Document is updating " + vendorDetails.size()
                    + " PaymentWorks Vendors but only " + existingPmwVendors.size()
                    + " matching records were found in the system. The missing records may have been deleted/purged.");
        }

        for (final PaymentWorksVendor existingPmwVendor : existingPmwVendors) {
            final PaymentWorksVendorGlobalDetail globalDetail = detailMappings.get(existingPmwVendor.getId());
            if (ObjectUtils.isNull(globalDetail)) {
                throw new IllegalStateException("Document does not contain the specified PaymentWorks Vendor: "
                        + existingPmwVendor.getId());
            } else if (!encounteredPmwVendors.add(existingPmwVendor.getId())) {
                throw new IllegalStateException("Duplicate PaymentWorks Vendor encountered during processing: "
                        + existingPmwVendor.getId());
            }

            existingPmwVendor.setSupplierUploadStatus(SupplierUploadStatus.READY_FOR_UPLOAD);
            pmwVendorsToUpdate.add(existingPmwVendor);
        }

        return pmwVendorsToUpdate;
    }

    @Override
    public List<? extends GlobalBusinessObjectDetail> getAllDetailObjects() {
        return new ArrayList<>(vendorDetails);
    }

    @Override
    public List<Collection<PersistableBusinessObject>> buildListOfDeletionAwareLists() {
        final List<Collection<PersistableBusinessObject>> managedLists = super.buildListOfDeletionAwareLists();
        managedLists.add(new ArrayList<>(vendorDetails));
        return managedLists;
    }

    @Override
    public boolean isPersistable() {
        return true;
    }

    @Override
    public String getDocumentNumber() {
        return documentNumber;
    }

    @Override
    public void setDocumentNumber(final String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getActionTypeCode() {
        return actionTypeCode;
    }

    public void setActionTypeCode(final String actionTypeCode) {
        this.actionTypeCode = actionTypeCode;
    }

    public PaymentWorksVendorGlobalAction getActionType() {
        return StringUtils.isNotBlank(actionTypeCode) ? PaymentWorksVendorGlobalAction.valueOf(actionTypeCode) : null;
    }

    public List<PaymentWorksVendorGlobalDetail> getVendorDetails() {
        return vendorDetails;
    }

    public void setVendorDetails(final List<PaymentWorksVendorGlobalDetail> vendorDetails) {
        this.vendorDetails = vendorDetails;
    }

    public BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        }
        return businessObjectService;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
