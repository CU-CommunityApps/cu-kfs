/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.sys.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kns.datadictionary.validation.fieldlevel.ZipcodeValidationPattern;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.businessobject.State;
import org.kuali.kfs.sys.service.LocationService;
import org.kuali.kfs.sys.service.PostalCodeValidationService;

public class PostalCodeValidationServiceImpl implements PostalCodeValidationService {

    // locationService is protected for sake of known customization
    protected LocationService locationService;

    public boolean validateAddress(String postalCountryCode, String stateCode, String postalCode,
            String statePropertyConstant, String postalCodePropertyConstant) {
        boolean valid = true;

        if (StringUtils.equals(KFSConstants.COUNTRY_CODE_UNITED_STATES, postalCountryCode)) {
            if (StringUtils.isBlank(stateCode)) {
                valid = false;
                if (StringUtils.isNotBlank(statePropertyConstant)) {
                    GlobalVariables.getMessageMap().putError(statePropertyConstant,
                            KFSKeyConstants.ERROR_US_REQUIRES_STATE);
                }
            }

            if (StringUtils.isBlank(postalCode)) {
                valid = false;
                if (StringUtils.isNotBlank(postalCodePropertyConstant)) {
                    GlobalVariables.getMessageMap().putError(postalCodePropertyConstant,
                            KFSKeyConstants.ERROR_US_REQUIRES_ZIP);
                }
            } else {
                ZipcodeValidationPattern zipPattern = getZipcodeValidatePattern();
                if (!zipPattern.matches(StringUtils.defaultString(postalCode))) {
                    valid = false;
                    if (StringUtils.isNotBlank(postalCodePropertyConstant)) {
                        GlobalVariables.getMessageMap().putError(postalCodePropertyConstant,
                                KFSKeyConstants.ERROR_POSTAL_CODE_INVALID);
                    }
                }
            }
        }

        // verify state code exist
        if (StringUtils.isNotBlank(postalCountryCode) && StringUtils.isNotBlank(stateCode)) {
            State state = locationService.getState(postalCountryCode, stateCode);
            if (state == null) {
                valid = false;
                GlobalVariables.getMessageMap().putError(statePropertyConstant,
                        KFSKeyConstants.ERROR_STATE_CODE_INVALID, stateCode);
            }
        }

        return valid;
    }
	
	/**
	 * Pulling getZipcodeValidatePattern() out for testability.  Lower down, ZipcodeValidationPattern loads params from a config file.
	 * @return
	 */
	protected ZipcodeValidationPattern getZipcodeValidatePattern() {
		return new ZipcodeValidationPattern();
	}

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }
}
