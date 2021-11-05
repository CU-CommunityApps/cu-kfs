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

/* Cornell Customization: backport redis*/
public class HomeOrigination extends PersistableBusinessObjectBase {

	public static final String CACHE_NAME = "HomeOrigination";

    private String finSystemHomeOriginationCode;
    private OriginationCode originationCode;

    public HomeOrigination() {
        super();
    }

    public String getFinSystemHomeOriginationCode() {
        return finSystemHomeOriginationCode;
    }

    public void setFinSystemHomeOriginationCode(String finSystemHomeOriginationCode) {
        this.finSystemHomeOriginationCode = finSystemHomeOriginationCode;
    }

    public OriginationCode getOriginationCode() {
        return originationCode;
    }

    public void setOriginationCode(OriginationCode originationCode) {
        this.originationCode = originationCode;
    }
}
