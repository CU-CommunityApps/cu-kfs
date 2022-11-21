package edu.cornell.kfs.coa.document;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.krad.maintenance.MaintenanceUtils;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;
import org.springframework.cache.Cache;

public class CuSubObjectCodeMaintainableImpl extends FinancialSystemMaintainable {
    
    private static final Logger LOG = LogManager.getLogger();
    
    @Override
    public List<MaintenanceLock> generateMaintenanceLocks() {
        List<MaintenanceLock> maintenanceLocks = super.generateMaintenanceLocks();
        
        Cache cache = MaintenanceUtils.getBlockingCache();
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateMaintenanceLocks, clear all blocking cache ");
        }
        cache.clear();
        
        return maintenanceLocks;
        
    }

}
