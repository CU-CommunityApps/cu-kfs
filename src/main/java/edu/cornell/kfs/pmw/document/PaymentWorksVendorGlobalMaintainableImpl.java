package edu.cornell.kfs.pmw.document;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.kns.web.ui.Section;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.document.FinancialSystemGlobalMaintainable;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.businessobject.PaymentWorksVendorGlobal;
import edu.cornell.kfs.pmw.businessobject.PaymentWorksVendorGlobalDetail;

@SuppressWarnings("deprecation")
public class PaymentWorksVendorGlobalMaintainableImpl extends FinancialSystemGlobalMaintainable {

    private static final long serialVersionUID = 1L;

    private static final String EDIT_PAYMENTWORKS_VENDOR_SECTION = "Edit PaymentWorks Vendors";
    private static final String PAYMENTWORKS_VENDOR_CONTAINER_ELEMENT_NAME = "PaymentWorks Vendor";

    @Override
    public List<MaintenanceLock> generateMaintenanceLocks() {
        final PaymentWorksVendorGlobal pmwVendorGlobal = (PaymentWorksVendorGlobal) getBusinessObject();
        final List<MaintenanceLock> maintenanceLocks = new ArrayList<>();
        for (final PaymentWorksVendorGlobalDetail vendorDetail : pmwVendorGlobal.getVendorDetails()) {
            final MaintenanceLock maintenanceLock = new MaintenanceLock();
            maintenanceLock.setDocumentNumber(pmwVendorGlobal.getDocumentNumber());
            maintenanceLock.setLockingRepresentation(buildPaymentWorksVendorLockingRepresentation(vendorDetail));
            maintenanceLocks.add(maintenanceLock);
        }
        return maintenanceLocks;
    }

    private String buildPaymentWorksVendorLockingRepresentation(final PaymentWorksVendorGlobalDetail vendorDetail) {
        return StringUtils.join(PaymentWorksVendor.class.getName(), KFSConstants.Maintenance.AFTER_CLASS_DELIM,
                KRADPropertyConstants.ID, KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM,
                String.valueOf(vendorDetail.getPmwVendorId()));
    }

    @Override
    public Class<? extends PersistableBusinessObject> getPrimaryEditedBusinessObjectClass() {
        return PaymentWorksVendor.class;
    }

    /**
     * Overridden to forcibly remove the add-line PaymentWorks Vendor fields, if present. Non-multi-value
     * newer-style lookups have technical issues with triggering on-change events or assisting with
     * auto-refreshing of read-only values upon return; however, the related multi-value lookup link
     * will be configured to open the older-style lookup if the single-row add-line is excluded.
     * To work around this issue, the single-row add-line fields will be forcibly removed after the correct
     * multi-value lookup link has been prepared.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public List getSections(final MaintenanceDocument document, final Maintainable oldMaintainable) {
        final List<?> sections = super.getSections(document, oldMaintainable);
        sections.stream()
                .map(Section.class::cast)
                .filter(section -> StringUtils.equals(section.getSectionId(), EDIT_PAYMENTWORKS_VENDOR_SECTION))
                .flatMap(section -> section.getRows().stream())
                .flatMap(row -> row.getFields().stream())
                .filter(this::fieldRepresentsContainerForPaymentWorksVendorAddLine)
                .findFirst()
                .ifPresent(containerField -> containerField.setContainerRows(new ArrayList<>()));
        return sections;
    }

    private boolean fieldRepresentsContainerForPaymentWorksVendorAddLine(final Field field) {
        return StringUtils.equals(field.getFieldType(), Field.CONTAINER)
                && StringUtils.equals(field.getContainerElementName(), PAYMENTWORKS_VENDOR_CONTAINER_ELEMENT_NAME)
                && StringUtils.isBlank(field.getContainerName())
                && StringUtils.equals(field.getMultipleValueLookupClassName(), PaymentWorksVendor.class.getName());
    }

}
