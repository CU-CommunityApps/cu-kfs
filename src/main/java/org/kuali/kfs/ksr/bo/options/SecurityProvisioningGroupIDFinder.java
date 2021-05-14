package org.kuali.kfs.ksr.bo.options;

import org.kuali.kfs.ksr.KsrConstants;
import org.kuali.kfs.ksr.service.KSRServiceLocator;
import org.kuali.rice.krad.data.platform.MaxValueIncrementerFactory;
import org.kuali.rice.krad.valuefinder.ValueFinder;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;


/**
 * ====
 * CU Customization:
 * Added ID finder for SecurityProvisioningGroup IDs.
 * ====
 */
public class SecurityProvisioningGroupIDFinder implements ValueFinder {

    @Override
    public String getValue() {
        DataFieldMaxValueIncrementer incrementer = MaxValueIncrementerFactory.getIncrementer(
                KSRServiceLocator.getDataSource(), KsrConstants.SECURITY_PROVISIONING_GROUP_SEQ_NAME);
        return incrementer.nextStringValue();
    }

}
