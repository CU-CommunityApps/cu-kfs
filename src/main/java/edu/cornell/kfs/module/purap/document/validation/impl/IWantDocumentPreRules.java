package edu.cornell.kfs.module.purap.document.validation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.role.RoleLite;
import org.kuali.kfs.kns.rules.PromptBeforeValidationBase;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.document.IWantDocument;

@SuppressWarnings("deprecation")
public class IWantDocumentPreRules extends PromptBeforeValidationBase {
    private static final Logger LOG = LogManager.getLogger();
    
    private static final String EDIT_IWNT_CONTRACT_INDICATOR_PERMISSION = "Edit Contract Indicator field on IWNT documents";
    private static final String REVIEW_IWNT_CONTRACT_RESPONSIBILITY= "Review IWNT Contracts";
    private static final String SHARED_SERVICE_CENTER_ROLE = "FTC/BSC members(cu)";
    private static final String IWNT_NODE_ORGANIZATION_HIERARCHY = "OrganizationHierarchy";
    private static final String IWNT_ROUTE_NODE_PURCHASING_CONTRACT_ASSISTANT = "PurchasingContractAssistant";
    private static final String IWNT_CREATE_REQ_QUESTION_ID = "confirmIWantCreateReqWithContractIndicated";

    @Override
    public boolean doPrompts(Document document) {
LOG.info("doPrompts  ===>>> ENTERED");
        boolean proceedWithRouting = true;

        IWantDocument iWantDoc = (IWantDocument) document;
        String currentUserPrincipalId = GlobalVariables.getUserSession().getPrincipalId();
        
        proceedWithRouting &= checkContractIndicatorWhenRoutedToOrganizationHierarchyUser(iWantDoc, currentUserPrincipalId);

LOG.info("doPrompts  ===>>> right before leaving: Returning {} <======= to proceed with routing user answered Yes, I want to Proceed with Creating the Req implies true ; No implies go backto IWNT should be FALSE", proceedWithRouting);
        if (!proceedWithRouting) {
            abortRulesCheck();
        }
        return proceedWithRouting;
    }

    protected boolean checkContractIndicatorWhenRoutedToOrganizationHierarchyUser(IWantDocument iWantDoc, String currentUserPrincipalId) {
        boolean userWantsReqCreatedWithContractChecked = true;
LOG.info("checkContractIndicatorWhenRoutedToOrganizationHierarchyUser  ===>>> ENTERED");
        if (contractIndicatorIsChecked(iWantDoc) 
                && isSharedServiceCenterUser(iWantDoc, currentUserPrincipalId)
                && isDocumentStoppedInRouteNode(iWantDoc, IWNT_NODE_ORGANIZATION_HIERARCHY)) {
            String questionText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(
                    CUPurapKeyConstants.IWNT_CONFIRM_REQ_CREATE_WITH_CONTRACT_INDICATOR);
            
            //Note: User's response is cached by method askOrAnalyzeYesNoQuestion
LOG.info("checkContractIndicatorWhenRoutedToOrganizationHierarchyUser  ===>>> idForUserResponse={}=", IWNT_CREATE_REQ_QUESTION_ID);

            userWantsReqCreatedWithContractChecked = super.askOrAnalyzeYesNoQuestion(IWNT_CREATE_REQ_QUESTION_ID, questionText);
        }
LOG.info("checkContractIndicatorWhenRoutedToOrganizationHierarchyUser===>>> userWantsReqCreatedWithContractChecked={}=", userWantsReqCreatedWithContractChecked);
        return userWantsReqCreatedWithContractChecked;
    }

    private boolean contractIndicatorIsChecked(IWantDocument iWantDoc) {
LOG.info("contractIndicatorIsChecked  ===>>> iWantDoc.isContractIndicator()={}=", iWantDoc.isContractIndicator());
LOG.info("contractIndicatorIsChecked  ===>>> iWantDoc.getRoutingContractIndicator()={}=", iWantDoc.getRoutingContractIndicator());
        return iWantDoc.isContractIndicator();
    }

    private boolean isSharedServiceCenterUser(IWantDocument iWantDoc, String currentUserPrincipalId) {
        RoleLite sscRole = KimApiServiceLocator.getRoleService().getRoleByNamespaceCodeAndName(KFSConstants.CoreModuleNamespaces.KFS, SHARED_SERVICE_CENTER_ROLE);
        if (ObjectUtils.isNull(sscRole)) {
LOG.info("isSharedServiceCenterUser  ===>>> sccRole detected as null");
            throw new RuntimeException("Unable to find role: " + SHARED_SERVICE_CENTER_ROLE);
        }
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(sscRole.getId());
LOG.info("isSharedServiceCenterUser  ===>>> sscRole.getId()={}=", sscRole.getId());
        return  KimApiServiceLocator.getRoleService().principalHasRole(currentUserPrincipalId, roleIds, new HashMap<String, String>());
    }
    
    private boolean isDocumentStoppedInRouteNode(IWantDocument iWantDoc, String nodeName) {
LOG.info("isDocumentStoppedInRouteNode  ===>>> ENTERED");
        final WorkflowDocument workflowDocument = iWantDoc.getDocumentHeader().getWorkflowDocument();

        final Set<String> names = workflowDocument.getCurrentNodeNames();
        if (CollectionUtils.isNotEmpty(names)) {
LOG.info("isDocumentStoppedInRouteNode  ===>>> route nodes exist");
            final List<String> currentRouteLevels = new ArrayList<String>(names);
            for (final String routeLevel  : currentRouteLevels) {
                if (routeLevel.contains(nodeName) && workflowDocument.isApprovalRequested()) {
LOG.info("isDocumentStoppedInRouteNode  ===>>> returning TRUE");
                    return true;
                }
            }
        }
LOG.info("isDocumentStoppedInRouteNode  ===>>> returning FALSE");
        return false;
    }
}
