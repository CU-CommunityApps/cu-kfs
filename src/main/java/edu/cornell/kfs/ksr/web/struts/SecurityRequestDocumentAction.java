package edu.cornell.kfs.ksr.web.struts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kim.api.KimConstants.AttributeConstants;
import org.kuali.kfs.kim.api.role.RoleMembership;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.util.BeanPropertyComparator;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.web.struts.FinancialSystemTransactionalDocumentActionBase;

import edu.cornell.kfs.ksr.KSRConstants;
import edu.cornell.kfs.ksr.KSRPropertyConstants;
import edu.cornell.kfs.ksr.businessobject.SecurityGroupTab;
import edu.cornell.kfs.ksr.businessobject.SecurityProvisioningGroup;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRole;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRoleQualification;
import edu.cornell.kfs.ksr.document.SecurityRequestDocument;
import edu.cornell.kfs.ksr.document.validation.impl.AddQualificationLineEvent;
import edu.cornell.kfs.ksr.service.SecurityRequestDocumentService;
import edu.cornell.kfs.ksr.service.impl.SecurityRequestDerivedRoleTypeServiceImpl;

public class SecurityRequestDocumentAction extends FinancialSystemTransactionalDocumentActionBase {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    protected void createDocument(KualiDocumentFormBase kualiDocumentFormBase) {
        SecurityRequestDocumentForm documentForm = (SecurityRequestDocumentForm) kualiDocumentFormBase;

        if (documentForm.getSecurityGroupId() == null) {
            LOG.error("createDocument() Security group id not given for new security request document request");
            throw new RuntimeException("Security group id not given for new security request document request");
        }

        super.createDocument(kualiDocumentFormBase);

        SecurityRequestDocument document = (SecurityRequestDocument) documentForm.getDocument();
        document.setSecurityGroupId(documentForm.getSecurityGroupId());
        documentForm.setCurrentPrincipalId(document.getPrincipalId());
        document.refreshReferenceObject(KSRPropertyConstants.SECURITY_GROUP);
    }

    @Override
    protected void loadDocument(KualiDocumentFormBase kualiDocumentFormBase) {
        super.loadDocument(kualiDocumentFormBase);
        SecurityRequestDocumentForm documentForm = (SecurityRequestDocumentForm) kualiDocumentFormBase;
        SecurityRequestDocument document = (SecurityRequestDocument) kualiDocumentFormBase.getDocument();

        documentForm.setCurrentPrincipalId(document.getPrincipalId());
        SpringContext.getBean(SecurityRequestDocumentService.class).prepareSecurityRequestDocument(document);
        ((SecurityRequestDocumentForm) kualiDocumentFormBase).setTabRoleIndexes(buildTabRoleIndexes(document));
    }

    public ActionForward addQualificationLine(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        SecurityRequestDocumentForm documentForm = (SecurityRequestDocumentForm) form;
        SecurityRequestDocument document = (SecurityRequestDocument) documentForm.getDocument();

        int roleRequestIndex = -1;

        String parameterName = (String) request.getAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
        if (StringUtils.isNotBlank(parameterName) && StringUtils.contains(parameterName, ".roleRequestIndex")) {
            String roleRequestIndexStr = StringUtils.substringBetween(parameterName, ".roleRequestIndex", ".");
            roleRequestIndex = Integer.parseInt(roleRequestIndexStr);
        } else {
            LOG.error("addQualificationLine() Unable to find role request index for new qualification line");
            throw new RuntimeException("Unable to find role request index for new qualification line");
        }

        SecurityRequestRole securityRequestRole = document.getSecurityRequestRoles().get(roleRequestIndex);
        if (securityRequestRole == null) {
            LOG.error("addQualificationLine() Security request role not found for index: " + roleRequestIndex);
            throw new RuntimeException("Security request role not found for index: " + roleRequestIndex);
        }

        String errorPath = "document.securityRequestRoles[" + roleRequestIndex + "].newRequestRoleQualification";
        boolean rulePassed = KRADServiceLocatorWeb.getKualiRuleService()
                .applyRules(new AddQualificationLineEvent(errorPath, document, securityRequestRole.getNewRequestRoleQualification()));

        if (rulePassed) {
            securityRequestRole.getRequestRoleQualifications().add(securityRequestRole.getNewRequestRoleQualification());

            SecurityRequestRoleQualification newRequestRoleQualification = SpringContext.getBean(SecurityRequestDocumentService.class)
                    .buildRoleQualificationLine(securityRequestRole, null);
            securityRequestRole.setNewRequestRoleQualification(newRequestRoleQualification);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward deleteQualificationLine(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        SecurityRequestDocumentForm documentForm = (SecurityRequestDocumentForm) form;
        SecurityRequestDocument document = (SecurityRequestDocument) documentForm.getDocument();

        int roleRequestIndex = -1;
        int qualificationIndex = -1;

        String parameterName = (String) request.getAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
        if (StringUtils.isNotBlank(parameterName) && StringUtils.contains(parameterName, ".roleRequestIndex")
                && StringUtils.contains(parameterName, ".qualificationIndex")) {
            String roleRequestIndexStr = StringUtils.substringBetween(parameterName, ".roleRequestIndex", ".");
            roleRequestIndex = Integer.parseInt(roleRequestIndexStr);

            String qualificationIndexStr = StringUtils.substringBetween(parameterName, ".qualificationIndex", ".");
            qualificationIndex = Integer.parseInt(qualificationIndexStr);
        } else {
            LOG.error("deleteQualificationLine() Unable to find qualification index for line to delete");
            throw new RuntimeException("Unable to find qualification index for line to delete");
        }

        SecurityRequestRole securityRequestRole = document.getSecurityRequestRoles().get(roleRequestIndex);
        if (securityRequestRole == null) {
            LOG.error("deleteQualificationLine() Security request role not found for index: " + roleRequestIndex);
            throw new RuntimeException("Security request role not found for index: " + roleRequestIndex);
        }

        securityRequestRole.getRequestRoleQualifications().remove(qualificationIndex);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    @Override
    public ActionForward refresh(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        SecurityRequestDocumentForm documentForm = (SecurityRequestDocumentForm) form;
        SecurityRequestDocument document = (SecurityRequestDocument) documentForm.getDocument();

        super.refresh(mapping, form, request, response);

        if (StringUtils.isNotBlank(request.getParameter("document.principalId"))) {

            if (StringUtils.isNotBlank(document.getPrincipalId()) && ObjectUtils.isNotNull(document.getRequestPerson())) {
                if (!StringUtils.equals(document.getPrincipalId(), documentForm.getCurrentPrincipalId())) {
                    SpringContext.getBean(SecurityRequestDocumentService.class).initiateSecurityRequestDocument(document,
                            GlobalVariables.getUserSession().getPerson());
                    documentForm.setTabRoleIndexes(buildTabRoleIndexes(document));
                    documentForm.setCurrentPrincipalId(document.getPrincipalId());
                }
            }
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    protected String getReturnLocation(HttpServletRequest request, ActionMapping mapping) {
        return getApplicationBaseUrl() + mapping.getPath() + ".do";
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected List<SecurityRequestDocumentForm.TabRoleIndexes> buildTabRoleIndexes(final SecurityRequestDocument document) {
        final List<SecurityRequestDocumentForm.TabRoleIndexes> tabRoleIndexes = new ArrayList<SecurityRequestDocumentForm.TabRoleIndexes>();

        List sortPropertyNames = new ArrayList();
        sortPropertyNames.add(KSRPropertyConstants.SECURITY_REQUEST_DOCUMENT_TAB_ORDER);
        
        List<SecurityGroupTab> securityGroupTabs = document.getSecurityGroup().getSecurityGroupTabs().stream().collect(Collectors.toCollection(ArrayList::new));
        Collections.sort(securityGroupTabs, new BeanPropertyComparator(sortPropertyNames));

        for (SecurityGroupTab groupTab : securityGroupTabs) {
            SecurityRequestDocumentForm.TabRoleIndexes tabIndexes = new SecurityRequestDocumentForm.TabRoleIndexes();
            tabIndexes.setTabId(groupTab.getTabId());
            tabIndexes.setTabName(groupTab.getTabName());

            sortPropertyNames = new ArrayList();
            sortPropertyNames.add(KSRPropertyConstants.SECURITY_REQUEST_DOCUMENT_ROLE_TAB_ORDER);

            List<SecurityProvisioningGroup> provisioningGroups = groupTab.getSecurityProvisioningGroups().stream().collect(Collectors.toCollection(ArrayList::new));
            Collections.sort(provisioningGroups, new BeanPropertyComparator(sortPropertyNames));

            List<Integer> requestRoleIndexes = new ArrayList<Integer>();
            for (SecurityProvisioningGroup provisioningGroup : provisioningGroups) {
                if (provisioningGroup.isActive()) {
                    int roleIndex = findSecurityRequestRoleIndex(document, provisioningGroup.getRoleId());
                    if (roleIndex == -1) {
                        LOG.warn("buildTabRoleIndexes, Unable to find security request role record for role id '"
                                + provisioningGroup.getRoleId() + "' on document '" + document.getDocumentNumber()
                                + "'; this role will be omitted from the document");
                    } else {
                        requestRoleIndexes.add(new Integer(roleIndex));
                    }
                }

            }

            if (requestRoleIndexes.size() > 0) {
                tabIndexes.setRoleRequestIndexes(requestRoleIndexes);
                tabRoleIndexes.add(tabIndexes);
            }

        }

        return tabRoleIndexes;
    }

    protected int findSecurityRequestRoleIndex(final SecurityRequestDocument document, final String roleId) {
        int roleIndex = -1;

        for (int i = 0; i < document.getSecurityRequestRoles().size(); i++) {
            final SecurityRequestRole requestRole = document.getSecurityRequestRoles().get(i);

            if (StringUtils.equals(roleId, requestRole.getRoleId())) {
                roleIndex = i;
                break;
            }
        }

        return roleIndex;
    }

    public ActionForward approve(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        final SecurityRequestDocumentForm documentForm = (SecurityRequestDocumentForm) form;

        StringBuilder annotation = new StringBuilder();

        final SecurityRequestDocument document = (SecurityRequestDocument) documentForm.getDocument();

        if (documentForm.getMethodToCall().indexOf("approve") > -1) {
            final String principalId = GlobalVariables.getUserSession().getPerson().getPrincipalId();
            for (final SecurityRequestRole securityRequestRole : document.getSecurityRequestRoles()) {
                LOG.info("approve() Checking if " + principalId + " has access");
                if (canApproveRequestForRole(securityRequestRole, principalId, document)) {
                    annotation.append(securityRequestRole.getRoleInfo().getNamespaceCode()).append(" - ").append(securityRequestRole.getRoleInfo().getName())
                            .append("\n");
                }
            }
        }
        documentForm.setAnnotation(annotation.toString());

        return super.approve(mapping, form, request, response);
    }

    protected boolean canApproveRequestForRole(final SecurityRequestRole requestRole, final String principalId, final SecurityRequestDocument document) {
        for (final RoleMembership roleMember : getAuthorizerRoleMembers(requestRole, document)) {
            LOG.info("canApproveRequestForRole() Got role member " + roleMember.getMemberId());
            if (roleMember.getMemberId().equals(principalId)) {
                return true;
            }
        }
        return false;
    }
    
    protected List<RoleMembership> getAuthorizerRoleMembers(SecurityRequestRole requestRole, SecurityRequestDocument document) {
        List<RoleMembership> roleMembers = new ArrayList<>();
        List<String> authorizerRoleNames = Arrays.asList(
                KSRConstants.SECURITY_REQUEST_DISTRIBUTED_AUTHORIZER_ROLE_NAME, KSRConstants.SECURITY_REQUEST_ADDITIONAL_AUTHORIZER_ROLE_NAME,
                        KSRConstants.SECURITY_REQUEST_CENTRAL_AUTHORIZER_ROLE_NAME);

        for (String authorizerRoleName : authorizerRoleNames) {
            List<RoleMembership> authorizerRoleMembers = getSecurityRequestDerivedRoleTypeService().getRoleMembersFromDerivedRole(
                    authorizerRoleName, document, requestRole);
            roleMembers.addAll(authorizerRoleMembers);
        }

        return roleMembers;
    }
    
    private SecurityRequestDerivedRoleTypeServiceImpl getSecurityRequestDerivedRoleTypeService() {
        return SpringContext.getBean(SecurityRequestDerivedRoleTypeServiceImpl.class);
    }
}
