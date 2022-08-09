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
package org.kuali.kfs.kim.impl.responsibility;

import org.kuali.kfs.kew.api.responsibility.ResponsibilityChangeQueue;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.responsibility.ResponsibilityService;
import org.kuali.kfs.kim.impl.common.delegate.DelegateMember;
import org.kuali.kfs.kim.impl.role.RoleMember;
import org.kuali.kfs.kim.impl.role.RoleResponsibility;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADPropertyConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResponsibilityInternalServiceImpl implements ResponsibilityInternalService {

    // CU Customization: Increase businessObjectService visibility to protected.
    protected BusinessObjectService businessObjectService;
    private ResponsibilityChangeQueue responsibilityChangeQueue;
    private ResponsibilityService responsibilityService;

    @Override
    public RoleMember saveRoleMember(RoleMember roleMember) {

        //need to find what responsibilities changed so we can notify interested clients.  Like workflow.
        List<RoleResponsibility> oldRoleResp = getRoleResponsibilities(roleMember.getRoleId());

        // add row to member table
        RoleMember member = businessObjectService.save(roleMember);

        //need to find what responsibilities changed so we can notify interested clients.  Like workflow.
        // the new member has been added
        List<RoleResponsibility> newRoleResp = getRoleResponsibilities(roleMember.getRoleId());

        updateActionRequestsForResponsibilityChange(getChangedRoleResponsibilityIds(oldRoleResp, newRoleResp));

        return member;
    }

    @Override
    public DelegateMember saveDelegateMember(DelegateMember delegateMember) {
        // add row to member table
        return businessObjectService.save(delegateMember);
    }

    @Override
    public void removeRoleMember(RoleMember roleMember) {
        //need to find what responsibilities changed so we can notify interested clients.  Like workflow.
        List<RoleResponsibility> oldRoleResp = getRoleResponsibilities(roleMember.getRoleId());

        // need to set end date to inactivate, not delete
        roleMember.setActiveToDateValue(new java.sql.Timestamp(System.currentTimeMillis()));
        businessObjectService.save(roleMember);

        //need to find what responsibilities changed so we can notify interested clients.  Like workflow.
        // the new member has been added
        List<RoleResponsibility> newRoleResp = getRoleResponsibilities(roleMember.getRoleId());

        updateActionRequestsForResponsibilityChange(getChangedRoleResponsibilityIds(oldRoleResp, newRoleResp));
    }

    @Override
    public void updateActionRequestsForResponsibilityChange(Set<String> responsibilityIds) {
        responsibilityChangeQueue.responsibilitiesChanged(responsibilityIds);
    }

    public List<RoleResponsibility> getRoleResponsibilities(String roleId) {
        Map<String, String> criteria = new HashMap<>();
        criteria.put(KimConstants.PrimaryKeyConstants.SUB_ROLE_ID, roleId);
        criteria.put(KRADPropertyConstants.ACTIVE, "Y");
        return (List<RoleResponsibility>) businessObjectService.findMatching(RoleResponsibility.class, criteria);
    }

    /**
     * This method compares the two lists of responsibilitiy IDs and does a union.
     *
     * @param oldRespList
     * @param newRespList
     * @return a unique list of responsibility ids.
     */
    protected Set<String> getChangedRoleResponsibilityIds(List<RoleResponsibility> oldRespList,
            List<RoleResponsibility> newRespList) {
        Set<String> lRet = new HashSet<>();

        for (RoleResponsibility resp : oldRespList) {
            lRet.add(resp.getResponsibilityId());
        }
        for (RoleResponsibility resp : newRespList) {
            lRet.add(resp.getResponsibilityId());
        }

        return lRet;
    }

    private boolean areActionsAtAssignmentLevel(Responsibility responsibility) {
        Map<String, String> details = responsibility.getAttributes();
        if (details == null) {
            return false;
        }
        String actionDetailsAtRoleMemberLevel = details.get(
                KimConstants.AttributeConstants.ACTION_DETAILS_AT_ROLE_MEMBER_LEVEL);
        return Boolean.parseBoolean(actionDetailsAtRoleMemberLevel);
    }

    @Override
    public boolean areActionsAtAssignmentLevelById(String responsibilityId) {
        Responsibility responsibility = responsibilityService.getResponsibility(responsibilityId);
        if (responsibility == null) {
            return false;
        }
        return areActionsAtAssignmentLevel(responsibility);
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setResponsibilityService(ResponsibilityService responsibilityService) {
        this.responsibilityService = responsibilityService;
    }

    public void setResponsibilityChangeQueue(ResponsibilityChangeQueue responsibilityChangeQueue) {
        this.responsibilityChangeQueue = responsibilityChangeQueue;
    }
}
