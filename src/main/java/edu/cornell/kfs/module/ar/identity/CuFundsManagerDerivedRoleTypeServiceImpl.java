package edu.cornell.kfs.module.ar.identity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.ar.identity.FundsManagerDerivedRoleTypeServiceImpl;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.role.RoleMembership;

public class CuFundsManagerDerivedRoleTypeServiceImpl extends FundsManagerDerivedRoleTypeServiceImpl {

    @Override
    public List<RoleMembership> getRoleMembersFromDerivedRole(
            String namespaceCode, String roleName, Map<String, String> qualification) {
        if (MapUtils.isNotEmpty(qualification)) {
            Map<String, Object> fundManagerCriteria = new HashMap<>();
            String principalId = qualification.get(KimConstants.AttributeConstants.PRINCIPAL_ID);
            String proposalNumber = qualification.get(KFSPropertyConstants.PROPOSAL_NUMBER);
            if (StringUtils.isNotBlank(principalId)) {
                fundManagerCriteria.put(KimConstants.AttributeConstants.PRINCIPAL_ID, principalId);
            }
            if (StringUtils.isNotBlank(proposalNumber)) {
                fundManagerCriteria.put(KFSPropertyConstants.PROPOSAL_NUMBER, proposalNumber);
            }
            if (MapUtils.isNotEmpty(fundManagerCriteria)) {
                return getRoleMembers(fundManagerCriteria);
            }
        }
        return Collections.emptyList();
    }

}
