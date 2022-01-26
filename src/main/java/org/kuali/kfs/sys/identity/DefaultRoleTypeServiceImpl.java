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
package org.kuali.kfs.sys.identity;

import org.kuali.kfs.kim.framework.group.GroupTypeService;
import org.kuali.kfs.kns.kim.role.RoleTypeServiceBase;

/*
 * CU Customization:
 * Backported the FINP-7913 fix. This overlay can be removed when we upgrade to the 2021-10-14 patch or later.
 */
/**
 * The "default" type service (org.kuali.kfs.kns.kim.type.DataDictionaryTypeServiceBase) we were using for
 * FO delegation is no longer a DelegationTypeService, so this is an alternative we can use to avoid
 * having error messages in the log.
 *
 * Yes, implementing GroupTypeService here looks a bit odd, however, that is so there is a "Default" KIM Type
 * available for Groups. Once upon a time, there was a separate "Default" KIM Type in the Rice database with the
 * KUALI namespace, but no KIM Type Service associated with it. That was merged with this during the KEW migration
 * work, but that resulted in no "Default" KIM Type showing up for Groups. This addresses the issue, abeit in a
 * somewhat hacky way. Maybe we can revisit these things en masse in the future.
 */
public class DefaultRoleTypeServiceImpl extends RoleTypeServiceBase implements GroupTypeService {

}
