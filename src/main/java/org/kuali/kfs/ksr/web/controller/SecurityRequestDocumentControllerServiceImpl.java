package org.kuali.kfs.ksr.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.ksr.KsrConstants;
import org.kuali.kfs.ksr.bo.SecurityGroup;
import org.kuali.kfs.ksr.bo.SecurityRequestRole;
import org.kuali.kfs.ksr.document.SecurityRequestDocument;
import org.kuali.kfs.ksr.identity.SecurityRequestDerivedRoleTypeServiceImpl;
import org.kuali.kfs.ksr.service.SecurityRequestDocumentService;
import org.kuali.kfs.ksr.web.form.SecurityRequestDocumentForm;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.role.Role;
import org.kuali.rice.kim.api.role.RoleMembership;
import org.kuali.rice.krad.data.DataObjectService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADUtils;
import org.kuali.rice.krad.web.form.DocumentFormBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.springframework.web.servlet.ModelAndView;

import edu.cornell.cynergy.krad.document.CynergyTransactionalDocumentControllerServiceImpl;

/**
 * Custom TransactionalDocumentControllerService implementation that performs
 * additional handling for SecurityRequestDocuments.
 * 
 * The operations here are based upon those from the archaic SecurityRequestDocumentAction KNS class.
 */
public class SecurityRequestDocumentControllerServiceImpl extends CynergyTransactionalDocumentControllerServiceImpl {
    private static final Logger LOG = LogManager.getLogger(SecurityRequestDocumentControllerServiceImpl.class);

    protected DataObjectService dataObjectService;
    protected SecurityRequestDocumentService securityRequestDocumentService;
    protected SecurityRequestDerivedRoleTypeServiceImpl securityRequestRoleTypeService;

    /**
     * Overridden to also set up the SecurityGroup on the new document,
     * whose ID should have been passed in from the "wizard" form.
     * 
     * @see org.kuali.rice.krad.document.DocumentControllerServiceImpl#createDocument(org.kuali.rice.krad.web.form.DocumentFormBase)
     */
    @Override
    protected void createDocument(DocumentFormBase form) throws WorkflowException {
        SecurityRequestDocumentForm documentForm = (SecurityRequestDocumentForm) form;
        Long securityGroupId = documentForm.getSecurityGroupId();
        SecurityGroup securityGroup;
        
        if (securityGroupId == null) {
            throw new WorkflowException("Security group id not given for new security request document request");
        }
        
        securityGroup = dataObjectService.find(SecurityGroup.class, securityGroupId);
        if (KRADUtils.isNull(securityGroup)) {
            throw new WorkflowException("Security group not found for new security request document request");
        }
        
        super.createDocument(form);
        
        SecurityRequestDocument document = (SecurityRequestDocument) documentForm.getDocument();
        document.setSecurityGroupId(securityGroupId);
        document.setSecurityGroup(securityGroup);
        
        documentForm.setCurrentPrincipalId(document.getPrincipalId());
    }

    /**
     * Overridden to also perform additional SecurityRequestDocument-specific loading operations.
     * 
     * @see org.kuali.rice.krad.document.DocumentControllerServiceImpl#loadDocument(org.kuali.rice.krad.web.form.DocumentFormBase)
     */
    @Override
    protected void loadDocument(DocumentFormBase form) throws WorkflowException {
        super.loadDocument(form);
        
        SecurityRequestDocumentForm documentForm = (SecurityRequestDocumentForm) form;
        SecurityRequestDocument document = (SecurityRequestDocument) form.getDocument();
        securityRequestDocumentService.prepareSecurityRequestDocument(document);
        documentForm.setCurrentPrincipalId(document.getPrincipalId());
    }

    /**
     * Overridden to also customize the approval action's annotation to indicate
     * which request roles the approver is currently approving updates for.
     * 
     * @see org.kuali.rice.krad.document.TransactionalDocumentControllerServiceImpl#approve(org.kuali.rice.krad.web.form.DocumentFormBase)
     */
    @Override
    public ModelAndView approve(DocumentFormBase form) {
        SecurityRequestDocumentForm documentForm = (SecurityRequestDocumentForm) form;
        SecurityRequestDocument document = (SecurityRequestDocument) documentForm.getDocument();
        StringBuilder annotation = new StringBuilder();
        String principalId = GlobalVariables.getUserSession().getPrincipalId();
        
        for (SecurityRequestRole securityRequestRole : document.getSecurityRequestRoles()) {
            LOG.info("Checking if " + principalId + " has access");
            
            if (canApproveRequestForRole(securityRequestRole, principalId, document)) {
                Role role = securityRequestRole.getRoleInfo();
                annotation.append(role.getNamespaceCode())
                        .append(" - ")
                        .append(role.getName())
                        .append("\n");
            }
        }
        
        documentForm.setAnnotation(annotation.toString());
        
        ModelAndView modelAndView =  super.approve(form);
        encodeAmpersandsInRedirectUrl(modelAndView);
        return modelAndView;
    }
    
    @Override
    public ModelAndView close(DocumentFormBase form) {
        ModelAndView modelAndView =  super.close(form);
        encodeAmpersandsInRedirectUrl(modelAndView);
        return modelAndView;
    }
    
    @Override
    public ModelAndView disapprove(DocumentFormBase form) {
        ModelAndView modelAndView =  super.disapprove(form);
        encodeAmpersandsInRedirectUrl(modelAndView);
        return modelAndView;
    }
    
    @Override
    public ModelAndView cancel(UifFormBase form) {
        ModelAndView modelAndView =  super.cancel(form);
        encodeAmpersandsInRedirectUrl(modelAndView);
        return modelAndView;
    }
    
    @Override
    public ModelAndView route(DocumentFormBase form) {
        ModelAndView modelAndView =  super.route(form);
        encodeAmpersandsInRedirectUrl(modelAndView);
        return modelAndView;
    }
    
    @Override
    public ModelAndView acknowledge(DocumentFormBase form) {
        ModelAndView modelAndView =  super.acknowledge(form);
        encodeAmpersandsInRedirectUrl(modelAndView);
        return modelAndView;
    }
    
    @Override
    public ModelAndView fyi(DocumentFormBase form) {
        ModelAndView modelAndView =  super.fyi(form);
        encodeAmpersandsInRedirectUrl(modelAndView);
        return modelAndView;
    }
    
    /*
     * Something is causing &current to be encoded to %C2%A4t .  This function encodes & to &amp; to avoid this bug.
     */
    private void encodeAmpersandsInRedirectUrl(ModelAndView modelAndView) {
        String redirectUrl = (String) modelAndView.getModel().get(KsrConstants.REDIRECT_URL_MODEL_KEY);
        LOG.debug("encodeAmpersandsInRedirectUrl, the redirect URL: " + redirectUrl);
        if (StringUtils.containsIgnoreCase(redirectUrl, KsrConstants.AMPERSAND)) {
            redirectUrl = StringUtils.replace(redirectUrl, KsrConstants.AMPERSAND, KsrConstants.HTML_ENCODED_AMPERSAND);
            LOG.debug("encodeAmpersandsInRedirectUrl, the new URL is " + redirectUrl);
            modelAndView.getModel().put(KsrConstants.REDIRECT_URL_MODEL_KEY, redirectUrl);
        }
    }

    /**
     * Determine if the currently logged in user can approve the given SecurityRequestRole for this document,
     * based upon the associated distributed/additional/central authorizer roles' derived memberships.
     *
     * @param requestRole The SecurityRequestRole to check for access on.
     * @param principalId The current user's principal ID.
     * @param document The document that the SecurityRequestRole belongs to.
     * @return true if the principal has a derived role membership associated with the given request role, or false otherwise.
     */
    protected boolean canApproveRequestForRole(SecurityRequestRole requestRole, String principalId, SecurityRequestDocument document) {
        for (RoleMembership roleMember : getAuthorizerRoleMembers(requestRole, document)) {
            if (roleMember.getMemberId().equals(principalId)) {
                return true;
            }
        }
        return false;
    }

    protected List<RoleMembership> getAuthorizerRoleMembers(SecurityRequestRole requestRole, SecurityRequestDocument document) {
        List<RoleMembership> roleMembers = new ArrayList<>();
        List<String> authorizerRoleNames = Arrays.asList(
                KsrConstants.SECURITY_REQUEST_DISTRIBUTED_AUTHORIZER_ROLE_NAME, KsrConstants.SECURITY_REQUEST_ADDITIONAL_AUTHORIZER_ROLE_NAME,
                        KsrConstants.SECURITY_REQUEST_CENTRAL_AUTHORIZER_ROLE_NAME);

        for (String authorizerRoleName : authorizerRoleNames) {
            List<RoleMembership> authorizerRoleMembers = securityRequestRoleTypeService.getRoleMembersFromDerivedRole(
                    authorizerRoleName, document, requestRole);
            roleMembers.addAll(authorizerRoleMembers);
        }

        return roleMembers;
    }

    public void setDataObjectService(DataObjectService dataObjectService) {
        this.dataObjectService = dataObjectService;
    }

    public void setSecurityRequestDocumentService(SecurityRequestDocumentService securityRequestDocumentService) {
        this.securityRequestDocumentService = securityRequestDocumentService;
    }

    public void setSecurityRequestRoleTypeService(SecurityRequestDerivedRoleTypeServiceImpl securityRequestRoleTypeService) {
        this.securityRequestRoleTypeService = securityRequestRoleTypeService;
    }

}
