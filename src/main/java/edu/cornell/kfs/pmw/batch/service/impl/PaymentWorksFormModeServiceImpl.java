package edu.cornell.kfs.pmw.batch.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksParameterConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksPropertiesConstants;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksFormModeService;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class PaymentWorksFormModeServiceImpl implements PaymentWorksFormModeService {
    protected ParameterService parameterService;

    @Override
    public boolean shouldUseForeignFormProcessingMode() {
        return FormMode.findFormMode(getFormModePerameterValue()).useForeignVendorForm;
    }

    @Override
    public boolean shouldUseLegacyFormProcessingMode() {
        return FormMode.findFormMode(getFormModePerameterValue()).useLegacyVendorForm;
    }
    
    protected String getFormModePerameterValue() {
        return parameterService.getParameterValueAsString(PaymentWorksConstants.PAYMENTWORKS_NAMESPACE_CODE, 
                CUKFSParameterKeyConstants.ALL_COMPONENTS, PaymentWorksParameterConstants.PAYMENTWORKS_FORM_PROCESSING_MODE);
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
    
    private enum FormMode {
        FOREIGN(true, false),
        LEGACY(false, true);
        
        public final boolean useForeignVendorForm;
        public final boolean useLegacyVendorForm;
        
        private FormMode(boolean useForeignVendorForm, boolean useLegacyVendorForm) {
            this.useForeignVendorForm = useForeignVendorForm;
            this.useLegacyVendorForm = useLegacyVendorForm;
        }
        
        public static FormMode findFormMode(String formModeString) {
            if (StringUtils.equalsAnyIgnoreCase(formModeString, PaymentWorksPropertiesConstants.PaymentWorksFromModes.FOREIGN_FORM_MODE)) {
                return FOREIGN;
            } else if (StringUtils.equalsAnyIgnoreCase(formModeString, PaymentWorksPropertiesConstants.PaymentWorksFromModes.LEGACY_FORM_MODE)) {
                return LEGACY;
            } else {
                throw new IllegalArgumentException("The form mode '" + formModeString + "' is invalid");
            }
        }
        
    }

}
