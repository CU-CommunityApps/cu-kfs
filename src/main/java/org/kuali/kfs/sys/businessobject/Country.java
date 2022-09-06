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
package org.kuali.kfs.sys.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;

public class Country extends PersistableBusinessObjectBase implements MutableInactivatable {

    private String name;
    private String code;
    private String alternateCode;
    private boolean active;

    public Country() {
        super();
    }
    
    public Country(String name, String code, String alternateCode, boolean active) {
        super();
        this.name = name;
        this.code = code;
        this.alternateCode = alternateCode;
        this.active = active;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAlternateCode() {
        return alternateCode;
    }

    public void setAlternateCode(String alternateCode) {
        this.alternateCode = alternateCode;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
