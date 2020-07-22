/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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
package org.kuali.kfs.coreservice.impl.parameter;

import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.coreservice.impl.component.DerivedComponentBo;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.sys.businessobject.lookup.NoDbSortLookupSearchService;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * CU Customization:
 * Added the 2020-04-09 financials version of this class, to backport the FINP-6619 fix.
 */
public class ParameterBoSearchService extends NoDbSortLookupSearchService {

    protected Pair<? extends Collection<? extends BusinessObjectBase>, Integer> getAllMatchingBos(
            Class<? extends BusinessObjectBase> businessObjectClass, boolean sortAscending,
            Map<String, String> searchProps) {
        if (businessObjectClass != ParameterBo.class) {
            throw new IllegalArgumentException("ParameterBoSearchService only supports searching for ParameterBos." +
                    "It does not support searching for instances of " + businessObjectClass.getName());
        }

        // we can't pass component.name in as searchProps b/c the components haven't been normalized yet; if we do,
        // it will remove basically all the results b/c those guys won't match
        String componentName = searchProps.remove("component.name");
        Pair<? extends Collection<? extends BusinessObjectBase>, Integer> results =
                super.getAllMatchingBos(businessObjectClass, sortAscending, searchProps);

        normalizeParameterComponents((Collection<ParameterBo>) results.getLeft());
        return manuallyFilterIfNecessary(componentName, results);
    }

    private Pair<? extends Collection<? extends BusinessObjectBase>, Integer> manuallyFilterIfNecessary(
            String componentName, Pair<? extends Collection<? extends BusinessObjectBase>, Integer> results) {
        // if componentName from component was in the search params, we need to apply it now that the component has
        // been normalized
        if (componentName != null) {
            Collection<ParameterBo> filteredBos = ((Collection<ParameterBo>) results.getLeft()).stream()
                    .filter(parameter -> parameter.getComponent() != null)
                    .filter(parameter -> fieldMatches(componentName, parameter.getComponent().getName()))
                    .collect(Collectors.toList());
            results = Pair.of(filteredBos, filteredBos.size());
        }
        return results;
    }

    private void normalizeParameterComponents(Collection<ParameterBo> parameters) {
        // attach the derived components where needed
        for (ParameterBo parameterBo : parameters) {
            if (parameterBo.getComponent() == null) {
                parameterBo.setComponent(DerivedComponentBo.toComponentBo(parameterBo.getDerivedComponent()));
            }
        }
    }

    // This checks if the search field value matches the value in the now-normalized component. The matching provided
    // by the prior OJB implementation supported trailing wildcard in addition to case insensitive match so this
    // method replicates that support.
    private boolean fieldMatches(String searchValue, String componentValue) {
        String loweredSearchValue = searchValue.trim().toLowerCase();
        String loweredComponentValue = componentValue.toLowerCase();
        if (loweredSearchValue.endsWith("*")) {
            String trimmedSearchValue = loweredSearchValue.substring(0, loweredSearchValue.length() - 1);
            return loweredComponentValue.startsWith(trimmedSearchValue);
        }
        return loweredSearchValue.equalsIgnoreCase(loweredComponentValue);
    }
}
