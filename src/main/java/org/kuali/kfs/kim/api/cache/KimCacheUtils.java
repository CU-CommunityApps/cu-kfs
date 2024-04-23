/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.kim.api.cache;

import org.apache.commons.collections4.CollectionUtils;
import org.kuali.kfs.kim.api.role.RoleService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;

import java.util.Collections;
import java.util.List;

/*
 * CU Customization: Backported the FINP-9235 changes.
 * This overlay can be removed when we upgrade to the 2023-03-08 financials patch.
 */
public final class KimCacheUtils {

    private KimCacheUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Used for a caching condition to determine if a role passed to a method is derived or not.
     *
     * @param roleIds list of role id values
     * @return true if list contains a derived role.
     */
    public static boolean isDynamicRoleMembership(final List<String> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return false;
        }
        final RoleService roleService = KimApiServiceLocator.getRoleService();
        for (final String roleId : roleIds) {
            if (roleService.isDynamicRoleMembership(roleId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Used for a caching condition to determine if a role passed to a method is derived or not.
     *
     * @param namespaceCode namespaceCode of role
     * @param roleName      name of role
     * @return true if list contains role.
     */
    public static boolean isDynamicMembshipRoleByNamespaceAndName(final String namespaceCode, final String roleName) {
        final List<String> roleIds = Collections.singletonList(
                KimApiServiceLocator.getRoleService().getRoleIdByNamespaceCodeAndName(namespaceCode, roleName));
        return isDynamicRoleMembership(roleIds);
    }

    /**
     * Used for a caching condition to determine if a role passed to a method is derived or not.
     *
     * @param namespaceCode namespaceCode of role
     * @param roleName      name of role
     * @return true if list contains role.
     */
    public static boolean isDynamicMembshipAllRolesByNamespaceAndName(final String namespaceCode,
            final String roleName) {
        final List<String> roleIds = KimApiServiceLocator.getRoleService().getAllRoleIdsByNamespaceCodeAndName(namespaceCode, roleName);
        return isDynamicRoleMembership(roleIds);
    }
}
