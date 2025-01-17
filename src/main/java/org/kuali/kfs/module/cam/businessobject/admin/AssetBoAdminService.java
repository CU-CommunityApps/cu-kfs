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

//**************************************************************************************
// Cornell Backport of FINP-9514 from KualiCo Patch Release 11/01/2023.
// 
// This file SHOULD BE REMOVED when Cornell reaches that KualiCo patch release version.
//**************************************************************************************

package org.kuali.kfs.module.cam.businessobject.admin;

import org.kuali.kfs.kim.bo.impl.KimAttributes;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.sys.businessobject.admin.DefaultBoAdminService;

import java.util.Map;

public class AssetBoAdminService extends DefaultBoAdminService {

    /**
     * Overridden to add Chart and Org to role qualifications so users can edit the Assets they have permissions for.
     */
    @Override
    protected Map<String, String> buildRoleQualifications(final Object businessObject, final String principalId) {
        final Map<String, String> roleQualifications = super.buildRoleQualifications(businessObject, principalId);

        final Asset asset = (Asset) businessObject;

        final String chart = asset.getOrganizationOwnerChartOfAccountsCode();
        roleQualifications.put(KimAttributes.CHART_OF_ACCOUNTS_CODE, chart);

        if (ObjectUtils.isNotNull(asset.getOrganizationOwnerAccount())) {
            // should only be null if AssetService.isAssetFabrication=true
            final String org = asset.getOrganizationOwnerAccount().getOrganizationCode();

            roleQualifications.put(KimAttributes.ORGANIZATION_CODE, org);
        }

        return roleQualifications;
    }
}
