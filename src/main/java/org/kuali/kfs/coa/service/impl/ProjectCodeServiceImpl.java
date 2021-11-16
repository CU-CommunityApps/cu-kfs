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
package org.kuali.kfs.coa.service.impl;

import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.service.ProjectCodeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.springframework.cache.annotation.Cacheable;

/* Cornell Customization: backport redis*/
public class ProjectCodeServiceImpl implements ProjectCodeService {

    private BusinessObjectService businessObjectService;

    @Override
    @Cacheable(cacheNames = ProjectCode.CACHE_NAME, key = "'projectCode='+#p0")
    public ProjectCode getByPrimaryId(String projectCode) {
        return businessObjectService.findBySinglePrimaryKey(ProjectCode.class, projectCode);
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
}
