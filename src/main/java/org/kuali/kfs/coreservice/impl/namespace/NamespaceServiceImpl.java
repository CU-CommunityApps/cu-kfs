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
package org.kuali.kfs.coreservice.impl.namespace;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coreservice.api.namespace.NamespaceService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.springframework.cache.annotation.Cacheable;

import java.util.Collections;
import java.util.List;

/* Cornell Customization: backport redis*/
public class NamespaceServiceImpl implements NamespaceService {

    private BusinessObjectService boService;

    @Cacheable(cacheNames = Namespace.CACHE_NAME, key = "'{getNamespace}-key=' + #p0")
    @Override
    public Namespace getNamespace(String code) {
        if (StringUtils.isBlank(code)) {
            throw new IllegalArgumentException("the code is blank");
        }

        return boService.findByPrimaryKey(Namespace.class, Collections.singletonMap("code", code));
    }

    @Cacheable(cacheNames = Namespace.CACHE_NAME, key = "'{findAllNamespaces}-all'")
    @Override
    public List<Namespace> findAllNamespaces() {
        List<Namespace> namespaces = (List<Namespace>) boService.findAll(Namespace.class);
        return Collections.unmodifiableList(namespaces);
    }

    public void setBusinessObjectService(BusinessObjectService boService) {
        this.boService = boService;
    }
}
