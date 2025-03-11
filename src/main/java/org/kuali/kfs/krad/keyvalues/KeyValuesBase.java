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
package org.kuali.kfs.krad.keyvalues;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * CU Customization:
 * Updated the code that retrieves the key-value pairs from the values finder,
 * so that it will add an empty key-value pair if one is not present.
 */
/**
 * Abstract base implementation of {@link KeyValuesFinder}.
 */
public abstract class KeyValuesBase implements KeyValuesFinder, Serializable {

    @Override
    public Map<String, String> getKeyLabelMap() {
        final Map<String, String> keyLabelMap = new HashMap<>();

        // CU customization to add a blank key-value entry if needed
        List<KeyValue> keyLabels = getKeyValues();
        addBlankKeyValueEntry(keyLabels);
        for (final KeyValue keyLabel : keyLabels) {
            keyLabelMap.put(keyLabel.getKey(), keyLabel.getValue());
        }

        return keyLabelMap;
    }
    
    
    /*
     * CU Customization: Added this method and the one right below it,
     * to forcibly add a blank key-value entry if the values finder does not return one.
     */
    private List<KeyValue> addBlankKeyValueEntry(List<KeyValue> keyLabels) {
        if (hasEntryForBlankKey(keyLabels)) {
            return keyLabels;
        } else {
            final KeyValue blankKeyValue = new ConcreteKeyValue(StringUtils.EMPTY, StringUtils.EMPTY);
            return Stream.concat(Stream.of(blankKeyValue), keyLabels.stream())
                    .collect(Collectors.toList());
        }
    }

    private boolean hasEntryForBlankKey(final List<KeyValue> keyValues) {
        return keyValues.stream()
                .anyMatch(keyValue -> StringUtils.isBlank(keyValue.getKey()));
    }
    @Override
    public String getKeyLabel(final String key) {
        final Map<String, String> keyLabelMap = getKeyLabelMap();

        if (keyLabelMap.containsKey(key)) {
            return keyLabelMap.get(key);
        }
        return null;
    }

    @Override
    public List<KeyValue> getKeyValues(final boolean includeActiveOnly) {
        return Collections.emptyList();
    }

}
