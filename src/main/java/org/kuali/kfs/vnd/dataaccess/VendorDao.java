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
package org.kuali.kfs.vnd.dataaccess;

import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.vnd.businessobject.VendorContract;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import java.sql.Date;
import java.util.Collection;
import java.util.Map;

//CU customization: partial backport of FINP-10792. This can be removed with the 03/20/2024 upgrade.
public interface VendorDao {

    VendorContract getVendorB2BContract(VendorDetail vendorDetail, String campus, Date currentSqlDate);

    Pair<Collection<? extends BusinessObjectBase>, Integer> getVendorDetails(
            Map<String, String> searchProps,
            int skip,
            int limit,
            String sortField,
            boolean sortAscending
    );
}
