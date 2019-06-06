/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
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
package org.kuali.kfs.kns.document.authorization;

import org.kuali.kfs.krad.datadictionary.mask.MaskFormatter;

import java.io.Serializable;
import java.util.Set;

public interface BusinessObjectRestrictions extends Serializable {

    boolean hasAnyFieldRestrictions();

    boolean hasRestriction(String fieldName);

    void addFullyMaskedField(String fieldName, MaskFormatter maskFormatter);

    void addPartiallyMaskedField(String fieldName, MaskFormatter maskFormatter);

    /**
     * This method returns the authorization setting for the given field name.
     * If the field name is not restricted in any way, a default full-editable value is returned.
     *
     * @param fieldName name of field to get authorization restrictions for.
     * @return a populated FieldAuthorization class for this field
     */
    FieldRestriction getFieldRestriction(String fieldName);

    /**
     * Returns a set of field names that currently have FieldRestrictions associated with this object
     * @return a set string representing the names of fields that have restrictions
     */
    Set<String> getAllFieldRestrictionNames();
}
