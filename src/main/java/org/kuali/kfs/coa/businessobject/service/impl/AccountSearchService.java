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
package org.kuali.kfs.coa.businessobject.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.service.impl.DefaultSearchService;
import org.springframework.util.MultiValueMap;

import edu.cornell.kfs.kim.CuKimPropertyConstants;

import java.util.Collection;
import java.util.List;
import java.util.Map;

// CU Customization: A customization was added to handle name masking for Person objects. Name masking is a local Cornell customization. This overlay cannot be removed with the 05-01-2024 upgrade due to the customization
// needed to handle masked names for the Person object. 
public class AccountSearchService extends DefaultSearchService {

    /**
     * Overridden to change the sortField from name to lastName when sorting on Person names since name is synthetic
     * (concatenates last name, first name and middle name (if it exists)) and isn't in the database, but lastName is,
     * and sorting on lastName will work against the database, and is good enough.
     */
    @Override
    protected Pair<Collection<? extends BusinessObjectBase>, Integer> executeSearch(
            final Class<? extends BusinessObjectBase> businessObjectClass,
            final int skip,
            final int limit,
            final String sortField,
            final boolean sortAscending,
            final Map<String, String> searchProps
    ) {
        return super.executeSearch(
                businessObjectClass,
                skip,
                limit,
                transformSortFieldIfNecessary(sortField),
                sortAscending,
                searchProps
        );
    }

    // CU Customization to handle name masking for Person objects
    private static String transformSortFieldIfNecessary(final String sortField) {
        if (sortField == null) {
            return null;
        }

        String transformedSortField = sortField;
        if (StringUtils.endsWithIgnoreCase(transformedSortField, CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX)) {
            transformedSortField = transformedSortField.replace(CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX,
                    KFSConstants.EMPTY_STRING);
        }
        if (transformedSortField.endsWith("." + KFSPropertyConstants.NAME)) {
            transformedSortField = transformedSortField.replace(KFSPropertyConstants.NAME,
                    KIMPropertyConstants.Person.LAST_NAME);
        }
        return transformedSortField;
    }

    /**
     * Overridden to invert the closed search parameter (Y -> N, N -> Y) and change the key from "closed" to "active"
     * since closed is on the Account lookup search form, but active is what's in the db and closed on Account is just
     * the active value negated.
     */
    @Override
    protected MultiValueMap<String, String> transformSearchParams(
            final Class<? extends BusinessObjectBase> boClass,
            final MultiValueMap<String, String> searchParams
    ) {
        final MultiValueMap<String, String> transformedSearchParams =
                super.transformSearchParams(boClass, searchParams);

        if (transformedSearchParams.containsKey(KFSPropertyConstants.CLOSED)) {
            final String closed = transformedSearchParams.remove(KFSPropertyConstants.CLOSED).get(0);
            if (KFSConstants.ParameterValues.YES.equals(closed)) {
                transformedSearchParams.put(KFSPropertyConstants.ACTIVE, List.of(KFSConstants.ParameterValues.NO));
            } else {
                transformedSearchParams.put(KFSPropertyConstants.ACTIVE, List.of(KFSConstants.ParameterValues.YES));
            }
        }

        return transformedSearchParams;
    }
}
