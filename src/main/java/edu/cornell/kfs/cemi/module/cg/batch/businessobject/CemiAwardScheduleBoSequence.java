package edu.cornell.kfs.cemi.module.cg.batch.businessobject;

import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.valuefinder.DefaultValueFinder;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.cemi.module.cg.CemiAwardScheduleConstants;

public class CemiAwardScheduleBoSequence implements DefaultValueFinder {

    private static volatile SequenceAccessorService sequenceAccessorService;

    /**
     * Pulls the next value from the CU_CEMI_EXTR_AWD_SCHD_TAB_AWD_SCHD_SEQ sequence
     */
    @Override
    public String getDefaultValue() {
        return getLongValue().toString();
    }
    
    public Long getLongValue() {
        return getSequenceAccessorService()
                .getNextAvailableSequenceNumber(CemiAwardScheduleConstants.CU_CEMI_EXTR_AWD_SCHD_TAB_AWD_SCHD_SEQ);
    }

    public SequenceAccessorService getSequenceAccessorService() {
        if (sequenceAccessorService == null) {
            sequenceAccessorService = SpringContext.getBean(SequenceAccessorService.class);
        }
        return sequenceAccessorService;
    }

}
