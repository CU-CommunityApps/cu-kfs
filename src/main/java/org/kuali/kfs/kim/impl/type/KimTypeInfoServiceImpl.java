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
package org.kuali.kfs.kim.impl.type;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.api.type.KimTypeInfoService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.springframework.cache.annotation.Cacheable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/* Cornell Customization: backport redis*/
public class KimTypeInfoServiceImpl implements KimTypeInfoService {

    private BusinessObjectService businessObjectService;

    @Cacheable(cacheNames = KimType.CACHE_NAME, key = "'{getKimType}-id=' + #p0")
    @Override
    public KimType getKimType(final String id) throws IllegalArgumentException {
        incomingParamCheck(id, "id");

        return businessObjectService.findBySinglePrimaryKey(KimType.class, id);
    }

    @Cacheable(cacheNames = KimType.CACHE_NAME, key = "'{findKimTypeByNameAndNamespace}-namespaceCode=' + #p0 + '|' + 'name=' + #p1")
    @Override
    public KimType findKimTypeByNameAndNamespace(final String namespaceCode, final String name) throws
            IllegalArgumentException {
        incomingParamCheck(namespaceCode, "namespaceCode");
        incomingParamCheck(name, "name");

        final Map<String, Object> crit = new HashMap<>();
        crit.put("namespaceCode", namespaceCode);
        crit.put("name", name);
        crit.put("active", "true");

        final Collection<KimType> bos = businessObjectService.findMatching(KimType.class, crit);

        if (bos != null && bos.size() > 1) {
            throw new IllegalStateException("multiple active results were found for the namespace code: " +
                    namespaceCode + " and name: " + name);
        }

        return bos != null && bos.iterator().hasNext() ? bos.iterator().next() : null;
    }

    @Cacheable(cacheNames = KimType.CACHE_NAME, key = "'{findAllKimTypes}-all'")
    @Override
    public Collection<KimType> findAllKimTypes() {
        return Collections.unmodifiableCollection(
                businessObjectService.findMatching(KimType.class, Collections.singletonMap("active", "true")));
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    private void incomingParamCheck(Object object, String name) {
        if (object == null) {
            throw new IllegalArgumentException(name + " was null");
        } else if (object instanceof String && StringUtils.isBlank((String) object)) {
            throw new IllegalArgumentException(name + " was blank");
        }
    }
}
