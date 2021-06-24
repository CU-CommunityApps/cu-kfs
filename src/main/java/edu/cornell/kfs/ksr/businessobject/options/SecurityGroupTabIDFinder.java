package edu.cornell.kfs.ksr.businessobject.options;

import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.valuefinder.DefaultValueFinder;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.ksr.KSRConstants;

public class SecurityGroupTabIDFinder implements DefaultValueFinder {
    
    public String getDefaultValue() {
        return getLongValue().toString();
    }

    /**
     * Get the next sequence number value as a Long.
     *
     * @return Long
     */
    public static Long getLongValue() {
        SequenceAccessorService sas = SpringContext.getBean(SequenceAccessorService.class);
        return sas.getNextAvailableSequenceNumber(KSRConstants.SECURITY_GROUP_TAB_SEQ_NAME);
    }

}
