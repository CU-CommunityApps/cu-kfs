package edu.cornell.kfs.fp.document.validation.impl;

import org.kuali.kfs.fp.document.AdjustmentAccrualVoucherDocument;
import org.kuali.kfs.fp.document.validation.impl.AdjustmentAccrualVoucherSingleSubFundValidation;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class CuAdjustmentAccrualVoucherSingleSubFundValidation extends AdjustmentAccrualVoucherSingleSubFundValidation {
    
    @Override
    public boolean validate(final AttributedDocumentEvent event) {
        final boolean allowMultiple = SpringContext.getBean(ParameterService.class)
                .getParameterValueAsBoolean(AdjustmentAccrualVoucherDocument.class, 
                        CUKFSParameterKeyConstants.FpParameterConstants.FP_ALLOW_MULTIPLE_SUBFUNDS);
        
        if (!allowMultiple) {
            return super.validate(event);
        }
        
        return true;
    }

}
