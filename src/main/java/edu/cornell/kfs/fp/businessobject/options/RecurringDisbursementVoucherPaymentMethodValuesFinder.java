package edu.cornell.kfs.fp.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import org.kuali.kfs.sys.businessobject.options.PaymentMethodValuesFinderBase;

import edu.cornell.kfs.fp.CuFPConstants;

public class RecurringDisbursementVoucherPaymentMethodValuesFinder extends PaymentMethodValuesFinderBase {
    
    private static final String DISPLAY_ON_RECURRING_DV_DOCUMENT = "extension.displayOnRecurringDVDocument";

    public RecurringDisbursementVoucherPaymentMethodValuesFinder() {
        super(DISPLAY_ON_RECURRING_DV_DOCUMENT);
    }  

}
