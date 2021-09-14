/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package edu.cornell.kfs.ksr.web.struts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kim.api.KimConstants.AttributeConstants;
import org.kuali.kfs.kim.api.role.RoleMembership;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
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
    private static final String SECURITY_REQUEST_DISTRIBUTED_AUTHORIZER_ROLE_NAME = "Security Request Distributed Authorizer";
    private static final String SECURITY_REQUEST_ADDITIONAL_AUTHORIZER_ROLE_NAME = "Security Request Additional Authorizer";
    private static final String SECURITY_REQUEST_CENTRAL_AUTHORIZER_ROLE_NAME = "Security Request Central Authorizer";

    // ==== CU Customization (CYNERGY-2377): Added the Logger below, and tweaked existing logging lines to use this logger instead. ====
	private static final Logger LOG = LogManager.getLogger(SecurityRequestDocumentAction.class);
    
    /**
     * Verify security group id has been set for creating the security request document
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#createDocument(org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase)
     */
    @Override
    protected void createDocument(KualiDocumentFormBase kualiDocumentFormBase) throws WorkflowException {
        SecurityRequestDocumentForm documentForm = (SecurityRequestDocumentForm) kualiDocumentFormBase;

        if (documentForm.getSecurityGroupId() == null) {
            LOG.error("Security group id not given for new security request document request");
            throw new RuntimeException("Security group id not given for new security request document request");
        }

        // initiate new document instance
        super.createDocument(kualiDocumentFormBase);

        // set security group id on new document instance
        SecurityRequestDocument document = (SecurityRequestDocument) documentForm.getDocument();
        document.setSecurityGroupId(documentForm.getSecurityGroupId());
        document.refreshReferenceObject(KSRPropertyConstants.SECURITY_GROUP);
    }

    /**
     * Invokes <code>SecurityRequestDocumentService</code> to prepare document instance after loading from database
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#loadDocument(org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase)
     */
    @Override
    protected void loadDocument(KualiDocumentFormBase kualiDocumentFormBase) throws WorkflowException {
        super.loadDocument(kualiDocumentFormBase);

        SecurityRequestDocument document = (SecurityRequestDocument) kualiDocumentFormBase.getDocument();
        SpringContext.getBean(SecurityRequestDocumentService.class).prepareSecurityRequestDocument(document);
        ((SecurityRequestDocumentForm) kualiDocumentFormBase).setTabRoleIndexes(buildTabRoleIndexes(document));
    }

    /**
     * Processes the request to add a new role qualification line
     * 
     * <p>
     * From the methodToCall request parameter the requested role index for the new qualification line to add is parsed
     * out and the corresponding <code>SecurityRequestRole</code> instance is pulled for the document. Business rules
     * are then invoked on the qualification line and if successful, the line is added to the existing collection of
     * qualifications for the request role, and a new blank qualification line is constructed
     * </p>
     */
    public ActionForward addQualificationLine(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        SecurityRequestDocumentForm documentForm = (SecurityRequestDocumentForm) form;
        SecurityRequestDocument document = (SecurityRequestDocument) documentForm.getDocument();

        // get role request index from method to call parameter
        int roleRequestIndex = -1;

        String parameterName = (String) request.getAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
        if (StringUtils.isNotBlank(parameterName) && StringUtils.contains(parameterName, ".roleRequestIndex")) {
            String roleRequestIndexStr = StringUtils.substringBetween(parameterName, ".roleRequestIndex", ".");
            roleRequestIndex = Integer.parseInt(roleRequestIndexStr);
        }
        else {
            LOG.error("Unable to find role request index for new qualification line");
            throw new RuntimeException("Unable to find role request index for new qualification line");
        }

        // get request role instance for index
        SecurityRequestRole securityRequestRole = document.getSecurityRequestRoles().get(roleRequestIndex);
        if (securityRequestRole == null) {
            LOG.error("Security request role not found for index: " + roleRequestIndex);
            throw new RuntimeException("Security request role not found for index: " + roleRequestIndex);
        }

        // check any business rules
        String errorPath = "document.securityRequestRoles[" + roleRequestIndex + "].newRequestRoleQualification";
        boolean rulePassed = KRADServiceLocatorWeb.getKualiRuleService().applyRules(
                new AddQualificationLineEvent(errorPath, document, securityRequestRole.getNewRequestRoleQualification()));

        if (rulePassed) {
            securityRequestRole.getRequestRoleQualifications().add(securityRequestRole.getNewRequestRoleQualification());

            SecurityRequestRoleQualification newRequestRoleQualification = SpringContext.getBean(SecurityRequestDocumentService.class)
                    .buildRoleQualificationLine(securityRequestRole, null);
            securityRequestRole.setNewRequestRoleQualification(newRequestRoleQualification);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Processes the request to delete a qualification line
     * 
     * <p>
     * From the methodToCall request parameter the requested role index for the delete qualification line is parsed out
     * and the corresponding <code>SecurityRequestRole</code> instance is pulled for the document. Likewise the index
     * for the qualification line within the security request role is parsed from the methodToCall parameter. If both
     * are found, the corresponding qualification line is removed from the collection
     * </p>
     */
    public ActionForward deleteQualificationLine(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        SecurityRequestDocumentForm documentForm = (SecurityRequestDocumentForm) form;
        SecurityRequestDocument document = (SecurityRequestDocument) documentForm.getDocument();

        // get role request index and qualification index from method to call parameter
        int roleRequestIndex = -1;
        int qualificationIndex = -1;

        String parameterName = (String) request.getAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
        if (StringUtils.isNotBlank(parameterName) && StringUtils.contains(parameterName, ".roleRequestIndex")
                && StringUtils.contains(parameterName, ".qualificationIndex")) {
            String roleRequestIndexStr = StringUtils.substringBetween(parameterName, ".roleRequestIndex", ".");
            roleRequestIndex = Integer.parseInt(roleRequestIndexStr);

            String qualificationIndexStr = StringUtils.substringBetween(parameterName, ".qualificationIndex", ".");
            qualificationIndex = Integer.parseInt(qualificationIndexStr);
        }
        else {
            LOG.error("Unable to find qualification index for line to delete");
            throw new RuntimeException("Unable to find qualification index for line to delete");
        }

        // get request role instance for index
        SecurityRequestRole securityRequestRole = document.getSecurityRequestRoles().get(roleRequestIndex);
        if (securityRequestRole == null) {
            LOG.error("Security request role not found for index: " + roleRequestIndex);
            throw new RuntimeException("Security request role not found for index: " + roleRequestIndex);
        }

        // remove qualification line
        securityRequestRole.getRequestRoleQualifications().remove(qualificationIndex);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * On return from the person lookup, if principal id is selected then invokes the
     * <code>SecurityRequestDocumentService</code> to initiate the document instance
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#refresh(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward refresh(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        super.refresh(mapping, form, request, response);

        SecurityRequestDocumentForm documentForm = (SecurityRequestDocumentForm) form;
        SecurityRequestDocument document = (SecurityRequestDocument) documentForm.getDocument();
        
        if(StringUtils.isNotBlank(document.getPrincipalId()) && ObjectUtils.isNotNull(document.getRequestPerson())) {
                SpringContext.getBean(SecurityRequestDocumentService.class).initiateSecurityRequestDocument(document,
                        GlobalVariables.getUserSession().getPerson());
                documentForm.setTabRoleIndexes(buildTabRoleIndexes(document));
        }


        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * @see org.kuali.rice.kns.web.struts.action.KualiAction#getReturnLocation(javax.servlet.http.HttpServletRequest,
     *      org.apache.struts.action.ActionMapping)
     */
    protected String getReturnLocation(HttpServletRequest request, ActionMapping mapping) {
        String mappingPath = mapping.getPath();
        String basePath = getApplicationBaseUrl();

        return basePath + mappingPath + ".do";
    }

    /**
     * Builds a List of <code>TabRoleIndexes</code> that is used within the UI to determine how to render the tabs,
     * which security request roles go within each tab, and the order in which they should be rendered
     * 
     * @param document {@link SecurityRequestDocument} for which to build indexes from
     * @return List<SecurityRequestDocumentForm.TabRoleIndexes>
     */
    @SuppressWarnings({"rawtypes","unchecked"})
    protected List<SecurityRequestDocumentForm.TabRoleIndexes> buildTabRoleIndexes(final SecurityRequestDocument document) {
        final List<SecurityRequestDocumentForm.TabRoleIndexes> tabRoleIndexes = new ArrayList<SecurityRequestDocumentForm.TabRoleIndexes>();

        // sort security group tabs
        List sortPropertyNames = new ArrayList();
        sortPropertyNames.add(KSRPropertyConstants.SECURITY_REQUEST_DOCUMENT_TAB_ORDER);

        Collections.sort(document.getSecurityGroup().getSecurityGroupTabs(), new BeanPropertyComparator(sortPropertyNames));

        for (SecurityGroupTab groupTab : document.getSecurityGroup().getSecurityGroupTabs()) {
            SecurityRequestDocumentForm.TabRoleIndexes tabIndexes = new SecurityRequestDocumentForm.TabRoleIndexes();
            tabIndexes.setTabId(groupTab.getTabId());
            tabIndexes.setTabName(groupTab.getTabName());

            // sort tab roles
            sortPropertyNames = new ArrayList();
            sortPropertyNames.add(KSRPropertyConstants.SECURITY_REQUEST_DOCUMENT_ROLE_TAB_ORDER);

            Collections.sort(groupTab.getSecurityProvisioningGroups(), new BeanPropertyComparator(sortPropertyNames));

            // build list of index for request roles for tab, ordered by the tab
            // role order
            List<Integer> requestRoleIndexes = new ArrayList<Integer>();
            for (SecurityProvisioningGroup provisioningGroup : groupTab.getSecurityProvisioningGroups()) {
                if (provisioningGroup.isActive()) {
                    int roleIndex = findSecurityRequestRoleIndex(document, provisioningGroup.getRoleId());
                    if (roleIndex == -1) {
                        throw new RuntimeException("Unable to find security request role record for role id: " + provisioningGroup.getRoleId());
                    }

                    requestRoleIndexes.add(new Integer(roleIndex));
                }

            }

            if(requestRoleIndexes.size() > 0){
                tabIndexes.setRoleRequestIndexes(requestRoleIndexes);
                tabRoleIndexes.add(tabIndexes);
            }
            
        }

        return tabRoleIndexes;
    }

    /**
     * Searches the securityRequestRoles collection of the given security request document for the request role instance
     * that is associated with the given role id, only one such instance should exist
     * 
     * @param document
     *            - security request document instance with collection to search
     * @param roleId
     *            - id for the role to match
     * @return index of the security request role in the collection, or -1 if not found
     */
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

    /**
     * Calls the document service to approve the document
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward approve(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        final SecurityRequestDocumentForm documentForm = (SecurityRequestDocumentForm) form;
        
        final StringBuffer annotation = new StringBuffer();
        
        final SecurityRequestDocument document = (SecurityRequestDocument) documentForm.getDocument();
        
        if (documentForm.getMethodToCall().indexOf("approve") > -1) {
            final String principalId = GlobalVariables.getUserSession().getPerson().getPrincipalId();
            for (final SecurityRequestRole securityRequestRole : document.getSecurityRequestRoles()) {
                LOG.info("Checking if " + principalId + " has access");
                if (canApproveRequestForRole(securityRequestRole, principalId, document)) {
                    annotation.append(securityRequestRole.getRoleInfo().getNamespaceCode())
                        .append(" - ")
                        .append(securityRequestRole.getRoleInfo().getName())
                        .append("\n");
                }
            }
        }
        documentForm.setAnnotation(annotation.toString());
        

        return super.approve(mapping, form, request, response);
    }

    protected List<RoleMembership> getRoleMembers(final SecurityRequestRole role, 
                                                      final String principalId, 
                                                      final SecurityRequestDocument document) {

        final Map<String,String> attributes = new HashMap<String,String>();
        attributes.put(AttributeConstants.DOCUMENT_NUMBER, document.getDocumentNumber());
        attributes.put(AttributeConstants.DOCUMENT_TYPE_NAME, KSRConstants.SECURITY_REQUEST_DOC_TYPE_NAME);

        final List<RoleMembership> roleMembers = new ArrayList<RoleMembership>();

        attributes.put(AttributeConstants.ROUTE_NODE_NAME, "DistributedAuthorizer");  
        roleMembers.addAll(SpringContext.getBean(SecurityRequestDerivedRoleTypeServiceImpl.class)
            .getRoleMembersFromDerivedRole(SECURITY_REQUEST_DISTRIBUTED_AUTHORIZER_ROLE_NAME,
                                               document, role));

        attributes.put(AttributeConstants.ROUTE_NODE_NAME, "AdditionalAuthorizer");
        roleMembers.addAll(getSecurityRequestDerivedRoleTypeService()
            .getRoleMembersFromDerivedRole(SECURITY_REQUEST_ADDITIONAL_AUTHORIZER_ROLE_NAME,
                                               document, role));

        attributes.put(AttributeConstants.ROUTE_NODE_NAME, "CentralAuthorizer");
        roleMembers.addAll(getSecurityRequestDerivedRoleTypeService()
            .getRoleMembersFromDerivedRole(SECURITY_REQUEST_CENTRAL_AUTHORIZER_ROLE_NAME,
                                               document, role));
                                               
        
        LOG.info("Got role members " + roleMembers.size());
        return roleMembers;
    }

    private SecurityRequestDerivedRoleTypeServiceImpl getSecurityRequestDerivedRoleTypeService() {
        return SpringContext.getBean(SecurityRequestDerivedRoleTypeServiceImpl.class);
    }

    /**
     * Determine if the currently logged in user can approve the given {@link SecurityRequestRole} for this document. It does this by utilizing the
     * {@link SecurityRequestDerivedRoleTypeServiceImpl} and returning members of the SecurityRequest Derived Role. It then checks
     * to see if a {@link RoleMembershipInfo} <code>principalId</code> is present
     *
     * @param role the {@link SecurityRequestRole} to check for access on
     * @param principalId (Usually the current user's)
     * @param documentNumber used for qualifications.
     */
    protected boolean canApproveRequestForRole(final SecurityRequestRole role, final String principalId, final SecurityRequestDocument document) {
        for (final RoleMembership roleMember : getRoleMembers(role, principalId, document)) {
            LOG.info("Got role member " + roleMember.getMemberId());
            if (roleMember.getMemberId().equals(principalId)) {
                return true;
            }
        }
        return false;
    }
}
