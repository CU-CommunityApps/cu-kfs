/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.datadictionary.legacy.model;

import org.kuali.kfs.krad.datadictionary.AttributeDefinition;

/**
 * ====
 * CU Customization: Added a no-op "personAttributeName" setter so that overriding an instance
 * of MaskedPersonAttributeDefinition won't cause the Data Dictionary to crash on startup.
 * This overlay should be removed when KualiCo removes this class from base code.
 * ====
 */
public class DisplayAttributeDefinition extends AttributeDefinition {

    public void setPersonAttributeName(String personAttributeName) {
        // Do nothing.
    }

    // now that all props have been moved up, this guy only exists until we get all the DD entries updated
}
