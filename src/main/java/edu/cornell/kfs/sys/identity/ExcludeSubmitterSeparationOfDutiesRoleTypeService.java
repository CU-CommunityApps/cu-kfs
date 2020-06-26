package edu.cornell.kfs.sys.identity;

import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.StringUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.identity.ExclusionRoleTypeServiceBase;
import org.kuali.rice.kew.api.action.ActionTaken;
import org.kuali.rice.kew.api.document.WorkflowDocumentService;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.role.RoleMembership;

public class ExcludeSubmitterSeparationOfDutiesRoleTypeService extends ExclusionRoleTypeServiceBase {
    
    protected volatile WorkflowDocumentService workflowDocumentService;

    @Override
    public List<RoleMembership> getMatchingRoleMemberships(Map<String, String> qualification,
            List<RoleMembership> roleMemberList) {
        List<RoleMembership> membershipInfos = super.getMatchingRoleMemberships(qualification, roleMemberList);
        String documentId = qualification.get(KimConstants.AttributeConstants.DOCUMENT_NUMBER);
        String submitter = getSubmitter(documentId);
        if (ObjectUtils.isNotNull(submitter)) {
            return excludePrincipalAsNeeded(submitter, qualification, membershipInfos);
        }

        return membershipInfos;
    }

    private String getSubmitter(String documentId) {
        String submitterPrincipalId = null;

        String principalId = workflowDocumentService.getRoutedByPrincipalIdByDocumentId(documentId);
        List<ActionTaken> actionTakenDTOs = workflowDocumentService.getActionsTaken(documentId);
        for (ActionTaken actionTaken : actionTakenDTOs) {
            if (StringUtils.equals(principalId, actionTaken.getPrincipalId())) {
                submitterPrincipalId = principalId;
            }
        }

        return submitterPrincipalId;
    }

    public void setWorkflowDocumentService(WorkflowDocumentService workflowDocumentService) {
        this.workflowDocumentService = workflowDocumentService;
    }

}
