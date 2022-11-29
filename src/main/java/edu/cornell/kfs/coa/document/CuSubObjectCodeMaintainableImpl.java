package edu.cornell.kfs.coa.document;

import org.kuali.kfs.krad.maintenance.MaintenanceUtils;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;

public class CuSubObjectCodeMaintainableImpl extends FinancialSystemMaintainable {
    
    @Override
    public void doRouteStatusChange(DocumentHeader documentHeader) {
        super.doRouteStatusChange(documentHeader);
        if (MaintenanceUtils.shouldClearCacheOnStatusChange(documentHeader)) {
            MaintenanceUtils.clearAllBlockingCache();
        }
    }

}
