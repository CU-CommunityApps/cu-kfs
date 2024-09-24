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
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.document.FinancialSystemGlobalMaintainable;

import edu.cornell.kfs.pmw.batch.PaymentWorksPropertiesConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.businessobject.PaymentWorksVendorGlobal;
import edu.cornell.kfs.pmw.businessobject.PaymentWorksVendorGlobalDetail;

@SuppressWarnings("deprecation")
public class PaymentWorksVendorGlobalMaintainableImpl extends FinancialSystemGlobalMaintainable {

    private static final long serialVersionUID = 1L;

    private static final String EDIT_GLOBAL_PAYMENTWORKS_VENDOR_SECTION = "Edit Global PaymentWorks Vendor";
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
                PaymentWorksPropertiesConstants.PaymentWorksVendor.ID, KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM,
                String.valueOf(vendorDetail.getPmwVendorId()));
    }

    @Override
    public Class<? extends PersistableBusinessObject> getPrimaryEditedBusinessObjectClass() {
        return PaymentWorksVendor.class;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List getSections(final MaintenanceDocument document, final Maintainable oldMaintainable) {
        final List<?> sections = super.getSections(document, oldMaintainable);
        removeBlankOptionFromActionTypeDropDownIfPresent(sections);
        removePaymentWorksVendorAddLineFieldsIfPresent(sections);
        return sections;
    }

    private void removeBlankOptionFromActionTypeDropDownIfPresent(final List<?> maintenanceDocumentSections) {
        maintenanceDocumentSections.stream()
                .map(Section.class::cast)
                .filter(section -> StringUtils.equals(section.getSectionId(), EDIT_GLOBAL_PAYMENTWORKS_VENDOR_SECTION))
                .flatMap(section -> section.getRows().stream())
                .flatMap(row -> row.getFields().stream())
                .filter(field -> StringUtils.equals(field.getPropertyName(),
                        PaymentWorksPropertiesConstants.ACTION_TYPE_CODE))
                .findFirst()
                .ifPresent(field -> field.setSkipBlankValidValue(true));
    }

    /**
     * Added this to forcibly remove the add-line PaymentWorks Vendor fields, if present. To avoid document-updating
     * issues when returning from converted lookups in this financials release, we only want to use the multi-value
     * variant of the PaymentWorks Vendor lookup. However, there appears to be a bug where if the Data Dictionary
     * has been configured to hide the single-value lookup variant, the multi-value variant will always use the
     * legacy lookup. To work around the issue, we're keeping both the single-value and multi-value variants enabled
     * in the Data Dictionary, and then this method will forcibly remove the relevant add-line fields afterwards.
     * 
     * We could investigate removing this workaround after upgrading to the 2023-11-01 version of financials.
     */
    private void removePaymentWorksVendorAddLineFieldsIfPresent(final List<?> maintenanceDocumentSections) {
        maintenanceDocumentSections.stream()
                .map(Section.class::cast)
                .filter(section -> StringUtils.equals(section.getSectionId(), EDIT_PAYMENTWORKS_VENDOR_SECTION))
                .flatMap(section -> section.getRows().stream())
                .flatMap(row -> row.getFields().stream())
                .filter(this::fieldRepresentsContainerForPaymentWorksVendorAddLine)
                .findFirst()
                .ifPresent(containerField -> containerField.setContainerRows(new ArrayList<>()));
    }

    private boolean fieldRepresentsContainerForPaymentWorksVendorAddLine(final Field field) {
        return StringUtils.equals(field.getFieldType(), Field.CONTAINER)
                && StringUtils.equals(field.getContainerElementName(), PAYMENTWORKS_VENDOR_CONTAINER_ELEMENT_NAME)
                && StringUtils.isBlank(field.getContainerName())
                && StringUtils.equals(field.getMultipleValueLookupClassName(), PaymentWorksVendor.class.getName());
    }

}
