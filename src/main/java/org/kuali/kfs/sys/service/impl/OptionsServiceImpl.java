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
package org.kuali.kfs.sys.service.impl;

import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.service.OptionsService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.springframework.cache.annotation.Cacheable;

public class OptionsServiceImpl implements OptionsService {

    private BusinessObjectService businessObjectService;
    protected UniversityDateService universityDateService;

    @Override
    @Cacheable(cacheNames = SystemOptions.CACHE_NAME, key = "'CurrentFY'")
    public SystemOptions getCurrentYearOptions() {
        Integer fy = universityDateService.getCurrentFiscalYear();
        return businessObjectService.findBySinglePrimaryKey(SystemOptions.class, fy);
    }

    @Override
    @Cacheable(cacheNames = SystemOptions.CACHE_NAME, key = "'universityFiscalYear='+#p0")
    public SystemOptions getOptions(Integer universityFiscalYear) {
        return businessObjectService.findBySinglePrimaryKey(SystemOptions.class, universityFiscalYear);
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }
}
