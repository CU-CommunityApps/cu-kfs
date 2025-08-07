/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
package org.kuali.kfs.kew.routeheader;

import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.preferences.Preferences;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.sys.context.SpringContext;

/**
 * ====
 * CU Customization: Modified Person name references to use the potentially masked equivalents instead.
 * ====
 * 
 * An extension of {@link DocumentRouteHeaderValue} which is mapped to OJB to help
 * with optimization of the loading of a user's Action List.
 */
public class DocumentRouteHeaderValueActionListExtension extends DocumentRouteHeaderValue {

    private static final long serialVersionUID = 8458532812557846684L;

    private String initiatorName = "";
    private boolean isInitiatorNameInitialized;

    public void initialize(final Preferences preferences) {
        if (KewApiConstants.PREFERENCES_YES_VAL.equals(preferences.getShowInitiator())) {
            initializeInitiatorName();
        }
    }

    public String getInitiatorName() {
        initializeInitiatorName();
        return initiatorName;
    }

    /**
     * Fetches the initiator name, masked appropriately if restricted.
     */
    private void initializeInitiatorName() {
        if (!isInitiatorNameInitialized) {
            final Person person = SpringContext.getBean(PersonService.class).getPerson(getInitiatorPrincipalId());
            if (person != null) {
                // ==== CU Customization: Return potentially masked Person name instead. ====
                initiatorName = person.getNameMaskedIfNecessary();
            }
            isInitiatorNameInitialized = true;
        }
    }

}
