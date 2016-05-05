package edu.cornell.kfs.sys.service.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.PostalCodeValidationServiceImpl;
import org.kuali.rice.kns.datadictionary.validation.fieldlevel.ZipcodeValidationPattern;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.location.api.state.State;
import org.kuali.rice.location.api.state.StateService;

public class CuPostalCodeValidationServiceImpl extends PostalCodeValidationServiceImpl {
	
	private StateService stateService;
	
	@Override
	public boolean validateAddress(String postalCountryCode, String stateCode, String postalCode, String statePropertyConstant, String postalCodePropertyConstant) {
        boolean valid = true;

        if (StringUtils.equals(KFSConstants.COUNTRY_CODE_UNITED_STATES, postalCountryCode)) {

            if (StringUtils.isBlank(stateCode)) {
                valid &= false;
                if (StringUtils.isNotBlank(statePropertyConstant)) {
                    GlobalVariables.getMessageMap().putError(statePropertyConstant, KFSKeyConstants.ERROR_US_REQUIRES_STATE);
                }
            }

            if (StringUtils.isBlank(postalCode)) {
                valid &= false;
                if (StringUtils.isNotBlank(postalCodePropertyConstant)) {
                    GlobalVariables.getMessageMap().putError(postalCodePropertyConstant, KFSKeyConstants.ERROR_US_REQUIRES_ZIP);
                }
            }
            else {
                ZipcodeValidationPattern zipPattern = getZipcodeValidatePattern();
                if (!zipPattern.matches(StringUtils.defaultString(postalCode))) {
                    valid &= false;
                    if (StringUtils.isNotBlank(postalCodePropertyConstant)) {
                        GlobalVariables.getMessageMap().putError(postalCodePropertyConstant, KFSKeyConstants.ERROR_POSTAL_CODE_INVALID);
                    }
                }
            }

        }

        // verify state code exist
        if (StringUtils.isNotBlank(postalCountryCode) && StringUtils.isNotBlank(stateCode)) {
            State state = getStateService().getState(postalCountryCode, stateCode);
            if (state == null) {
                GlobalVariables.getMessageMap().putError(statePropertyConstant, KFSKeyConstants.ERROR_STATE_CODE_INVALID, stateCode);
                //KFSPTS-3490
                valid = false;
            }
        }
        
        return valid;
    }
	
	/**
	 * Pulling getZipcodeValidatePattern() out for testability.  Lower down, ZipcodeValidationPattern loads params from a config file.
	 * @return
	 */
	protected ZipcodeValidationPattern getZipcodeValidatePattern() {
		ZipcodeValidationPattern zipPattern = new ZipcodeValidationPattern();
		return zipPattern;
	}
	
	public StateService getStateService() {
		return stateService;
	}
	
	public void setStateService(StateService stateService) {
		this.stateService = stateService;
	}
}
