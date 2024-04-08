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
package edu.cornell.kfs.module.purap.businessobject.options;

import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;

import java.util.List;

/**
 * This class returns list of boolean key value pairs.
 */
public class IndicatorMinusBothValuesFinder extends org.kuali.kfs.krad.keyvalues.KeyValuesBase {

    public static final IndicatorMinusBothValuesFinder INSTANCE = new IndicatorMinusBothValuesFinder();

    protected static final List<KeyValue> ACTIVE_LABELS = List.of(
            new ConcreteKeyValue(KRADConstants.YES_INDICATOR_VALUE, "Yes"),
            new ConcreteKeyValue(KRADConstants.NO_INDICATOR_VALUE, "No")
    );

    @Override
    public List<KeyValue> getKeyValues() {
        return ACTIVE_LABELS;
    }
}
