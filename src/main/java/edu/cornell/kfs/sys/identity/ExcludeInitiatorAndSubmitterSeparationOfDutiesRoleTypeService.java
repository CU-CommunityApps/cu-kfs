package edu.cornell.kfs.sys.identity;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.identity.ExclusionRoleTypeServiceBase;
import org.kuali.rice.kew.api.action.ActionTaken;
import org.kuali.rice.kew.api.document.WorkflowDocumentService;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.role.RoleMembership;

public class ExcludeInitiatorAndSubmitterSeparationOfDutiesRoleTypeService extends ExclusionRoleTypeServiceBase {
    
    protected volatile WorkflowDocumentService workflowDocumentService;

    @Override
    public List<RoleMembership> getMatchingRoleMemberships(Map<String, String> qualification,
            List<RoleMembership> roleMemberList) {
        List<RoleMembership> membershipInfos = super.getMatchingRoleMemberships(qualification, roleMemberList);
        String documentId = qualification.get(KimConstants.AttributeConstants.DOCUMENT_NUMBER);
        
        String submitter = getSubmitter(documentId);
        if (ObjectUtils.isNotNull(submitter)) {
            membershipInfos = excludePrincipalAsNeeded(submitter, qualification, membershipInfos);
        }
        
        String approverOrInitiator = getApproverOrInitiator(documentId);
        if (ObjectUtils.isNotNull(approverOrInitiator)) {
            membershipInfos = excludePrincipalAsNeeded(approverOrInitiator, qualification, membershipInfos);
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
    
    private String getApproverOrInitiator(String documentId) {
        String approverOrInitiatorPrincipalId = null;

        String principalId = workflowDocumentService.getDocumentInitiatorPrincipalId(documentId);
        List<ActionTaken> actionTakenDTOs = workflowDocumentService.getActionsTaken(documentId);
        for (ActionTaken actionTaken : actionTakenDTOs) {
            if (principalId.equals(actionTaken.getPrincipalId())) {
                approverOrInitiatorPrincipalId = principalId;
            }
        }

        return approverOrInitiatorPrincipalId;
    }

    public void setWorkflowDocumentService(WorkflowDocumentService workflowDocumentService) {
        this.workflowDocumentService = workflowDocumentService;
    }

}
