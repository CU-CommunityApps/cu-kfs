/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/* Cornell Customization: backport redis*/
public class HierarchicalData implements Serializable {

    private static final long serialVersionUID = -3151847669169779902L;
    
    private String label;
    private String value;

    private List<HierarchicalData> children = new ArrayList<>();

    public HierarchicalData(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<HierarchicalData> getChildren() {
        return children;
    }

    public void setChildren(List<HierarchicalData> children) {
        this.children = children;
    }

    public void addChild(HierarchicalData child) {
        this.children.add(child);
    }
}
