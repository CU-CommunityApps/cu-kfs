/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
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
package org.kuali.kfs.sys.rest.application;

import edu.cornell.kfs.module.purap.rest.resource.EinvoiceApiResource;
import org.kuali.kfs.sys.rest.filter.AcceptHeaderContainerRequestFilter;
import org.kuali.kfs.sys.rest.resource.AuthenticationResource;
import org.kuali.kfs.sys.rest.resource.AuthorizationResource;
import org.kuali.kfs.sys.rest.resource.BackdoorResource;
import org.kuali.kfs.sys.rest.resource.businessobject.BusinessObjectResource;
import org.kuali.kfs.sys.rest.resource.OJBConfigurationResource;
import org.kuali.kfs.sys.rest.resource.PreferencesResource;
import org.kuali.kfs.sys.rest.resource.SystemResource;
import org.kuali.kfs.sys.rest.resource.UserFavoritesResource;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("sys/api/v1")
public class SysApiApplication extends BaseApiApplication {

    public static final String SYS_ROOT = "sys/api/v1";
    public static final String BUSINESS_OBJECT_RESOURCE = "business-objects";

    public SysApiApplication() {
        addSingleton(new PreferencesResource());
        addSingleton(new BackdoorResource());
        addSingleton(new AuthenticationResource());
        addSingleton(new SystemResource());
        addSingleton(new AuthorizationResource());
        addSingleton(new OJBConfigurationResource());
        addSingleton(new UserFavoritesResource());
        addSingleton(new BusinessObjectResource());
        addSingleton(new EinvoiceApiResource());
        addClass(AcceptHeaderContainerRequestFilter.class);
    }
}
