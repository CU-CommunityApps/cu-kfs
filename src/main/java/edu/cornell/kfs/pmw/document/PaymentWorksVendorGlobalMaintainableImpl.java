package edu.cornell.kfs.pmw.document;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.document.FinancialSystemGlobalMaintainable;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.businessobject.PaymentWorksVendorGlobal;
import edu.cornell.kfs.pmw.businessobject.PaymentWorksVendorGlobalDetail;

public class PaymentWorksVendorGlobalMaintainableImpl extends FinancialSystemGlobalMaintainable {

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
                String.valueOf(vendorDetail.getId()));
    }

    @Override
    public Class<? extends PersistableBusinessObject> getPrimaryEditedBusinessObjectClass() {
        return PaymentWorksVendor.class;
    }

}
