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
package org.kuali.kfs.sys.dashboardnav.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.kuali.kfs.datadictionary.legacy.BusinessObjectDictionaryService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.datadictionary.legacy.DocumentDictionaryService;
import org.kuali.kfs.sys.dashboardnav.DashboardUtils;

import java.io.Serializable;
import java.util.List;

/* Cornell Customization: backport redis*/
public abstract class NavigationObject implements Serializable {

    private static final long serialVersionUID = -5211476800431154639L;

    private String id;
    protected transient BusinessObjectDictionaryService businessObjectDictionaryService;
    protected transient DataDictionaryService dataDictionaryService;
    protected transient DocumentDictionaryService documentDictionaryService;
    protected transient DashboardUtils dashboardUtils;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract void copyPrimitivesFrom(NavigationObject navObject);

    @JsonIgnore
    public void setBusinessObjectDictionaryService(
            BusinessObjectDictionaryService businessObjectDictionaryService) {
        this.businessObjectDictionaryService = businessObjectDictionaryService;
    }

    @JsonIgnore
    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    @JsonIgnore
    public void setDocumentDictionaryService(DocumentDictionaryService documentDictionaryService) {
        this.documentDictionaryService = documentDictionaryService;
    }

    @JsonIgnore
    public void setDashboardUtils(DashboardUtils dashboardUtils) {
        this.dashboardUtils = dashboardUtils;
    }

    protected void propagateServices(List<? extends NavigationObject> navObjects) {
        if (navObjects != null) {
            navObjects.parallelStream().forEach(navObject -> {
                navObject.setBusinessObjectDictionaryService(businessObjectDictionaryService);
                navObject.setDashboardUtils(dashboardUtils);
                navObject.setDocumentDictionaryService(documentDictionaryService);
                navObject.setDataDictionaryService(dataDictionaryService);
            });
        }
    }
}
