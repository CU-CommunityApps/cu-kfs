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
package org.kuali.kfs.vnd.businessobject.lookup;

import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.sys.businessobject.service.impl.DefaultSearchService;
import org.kuali.kfs.vnd.dataaccess.VendorDao;

import java.util.Collection;
import java.util.Map;

/**
 * This is a custom search service for VendorDetail. The original Lookupable Helper did a lot of stuff in memory, and
 * the new lookup framework doesn't really support that. So instead, this is using custom sql in a prepared statement to
 * do the processing at the database level directly. This allows sorting/filtering to be done on the full set of data vs
 * just what is returned from a partial result set, which is how the current lookupable works.
 */
// CU customization: partial backport of FINP-10792. This can be removed with the 03/20/2024 upgrade.
public class VendorDetailSearchService extends DefaultSearchService {
    private VendorDao vendorDao;

    @Override
    protected Pair<Collection<? extends BusinessObjectBase>, Integer> executeSearch(
            final Class<? extends BusinessObjectBase> businessObjectClass,
            final int skip,
            final int limit,
            final String sortField,
            final boolean sortAscending,
            final Map<String, String> searchProps
    ) {
        return vendorDao.getVendorDetails(searchProps, skip, limit, sortField, sortAscending);
    }

    public void setVendorDao(final VendorDao vendorDao) {
        this.vendorDao = vendorDao;
    }
}
