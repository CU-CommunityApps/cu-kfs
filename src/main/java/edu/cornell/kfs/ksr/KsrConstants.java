/*
 * Copyright 2007 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.ksr;



/**
 * ====
 * CU Customization: Added new constants as needed.
 * ====
 * 
 * Constants for the KSR module
 * 
 * @author rSmart Development Team
 */
public class KsrConstants {
	public static final String KSR_DOCUMENT_MAINTANABLE = "document.newMaintainableObject";
	
	public static final String KSR_NAMESPACE = "KR-SR";
	public static final String REQUIRED_QUALIFICATIONS_PARAMETER = "REQUIRED_QUALIFICATIONS";
	
	//Security Request Document
    public static final String ERROR_SECURITY_REQUEST_DOC_QUALIFIER_MISSING = "error.ksr.securityrequestdocument.qualifier.missing";
    public static final String ERROR_SECURITY_REQUEST_DOC_PRINCIPAL_ID_MISSING = "error.ksr.securityrequestdocument.principalId.missing";
    public static final String ERROR_SECURITY_REQUEST_DOC_DEPENDENT_ROLE_MISSING = "error.ksr.securityrequestdocument.dependentRole.missing";
    public static final String ERROR_SECURITY_REQUEST_DOC_CHANGE_MISSING = "error.ksr.securityrequestdocument.change.missing";
    public static final String ERROR_SECURITY_REQUEST_DOC_QUALIFIER_VALID = "error.ksr.securityrequestdocument.qualifier.valid";
    public static final String ERROR_SECURITY_REQUEST_DOC_SERVICE_EXCEPTION = "error.ksr.securityrequestdocument.service.exception";
    
	public static final String SECURITY_REQUEST_DOC_REQUEST_ROLE = "document.securityRequestRoles";
	public static final String SECURITY_REQUEST_DOC_PRINCIPAL_ID = "document.principalId";
	public static final String SECURITY_REQUEST_DOC_TYPE_NAME = "SecurityRequestDocument";
	public static final String SECURITY_REQUEST_DOC_URL = "securityRequestDocument.do";
	public static final String SECURITY_REQUEST_DOC_ROLE_QUAL = "requestRoleQualifications";
	public static final String SECURITY_REQUEST_DOC_ROLE_QUAL_DETAILS = "roleQualificationDetails";
	public static final String SECURITY_REQUEST_DOC_ROLE_ATTR_VALUE = "attributeValue";

	public static final String SECURITY_REQUEST_DOC_KRAD_URL = "/securityRequestDocument";
	public static final String SECURITY_REQUEST_WIZARD_KRAD_URL = "/securityRequestWizard";

	public static final String SECURITY_REQUEST_DISTRIBUTED_AUTHORIZER_ROLE_NAME = "Security Request Distributed Authorizer";
	public static final String SECURITY_REQUEST_ADDITIONAL_AUTHORIZER_ROLE_NAME = "Security Request Additional Authorizer";
	public static final String SECURITY_REQUEST_CENTRAL_AUTHORIZER_ROLE_NAME = "Security Request Central Authorizer";

	// Security Group sections
	public static final String SECTION_SECURITY_PROVISIONING_GROUP = "SecurityGroup";
	public static final String SECTION_SECURITY_TABS = "SecurityGroupTabs";

	// Security Group parameters
	public static final String SECURITY_GROUP_NAME = "securityGroupName";
	public static final String SECURITY_GROUP_ID = "securityGroupId";
	
	// Security Group Tab parameters
	public static final String SECURITY_GROUP_TAB_NAME = "tabName";
	public static final String SECURITY_GROUP_TAB_ID = "tabId";
	public static final String SECURITY_GROUP_TAB_ORDER = "tabOrder";
	public static final String SECURITY_GROUP = "securityGroup";

	// Security Group errors
	public static final String ERROR_SECURITY_GROUP_NAME_UNIQUE = "error.ksr.securitygroup.name.notunique";
	public static final String ERROR_SECURITY_GROUP_TAB_ORDER_UNIQUE = "error.ksr.securitytab.tab.order.notunique";
	public static final String ERROR_SECURITY_GROUP_TAB_NAME_UNIQUE = "error.ksr.securitytab.tab.name.notunique";
	public static final String ERROR_SECURITY_GROUP_TAB_MISSING = "error.ksr.security.tab.missing";
	
	// Security Group sections
	public static final String SECURITY_PROVISIONING_GROUP = "securityProvisioningGroup";
	public static final String SECURITY_PROVISIONING_GROUPS = "securityProvisioningGroups";
	public static final String DEPENDENT_ROLES = "dependentRoles";
	
	// Security Provisioning Group parameters
	public static final String SECURITY_PROVISIONING_GROUP_ADD_AUTH_ROLE_ID = "additionalAuthorizerRoleId";
	public static final String SECURITY_PROVISIONING_GROUP_CENT_AUTH_ROLE_ID = "centralAuthorizerRoleId";
	public static final String SECURITY_PROVISIONING_GROUP_DIST_AUTH_ROLE_ID = "distributedAuthorizerRoleId";
	public static final String SECURITY_PROVISIONING_GROUP_ADD_AUTH_ROLE_NAME = "additionalAuthorizerRoleName";
	public static final String SECURITY_PROVISIONING_GROUP_CENT_AUTH_ROLE_NAME = "centralAuthorizerRoleName";
	public static final String SECURITY_PROVISIONING_GROUP_DIST_AUTH_ROLE_NAME = "distributedAuthorizerRoleName";
	public static final String SECURITY_PROVISIONING_GROUP_ROLE_NAME = "roleName";
	public static final String SECURITY_PROVISIONING_GROUP_SECURITY_PROVISIONING_GROUP_NAME = "securityGroup.securityGroupName";
	public static final String PROVISIONING_ROLE_ID = "roleId";
	public static final String PROVISIONING_ROLE_TAB_ORDER = "roleTabOrder";
	
	// Security Provisioning Group Dependent Roles parameters
	
	// Security Provisioning Group labels
	public static final String SECURITY_PROVISIONING_GROUP_ADD_AUTH_LBL = "Additional Authorizer";
	public static final String SECURITY_PROVISIONING_GROUP_CENT_AUTH_LBL = "Central Authorizer";
	public static final String SECURITY_PROVISIONING_GROUP_DIST_AUTH_LBL = "Distributed Authorizer";
	public static final String SECURITY_PROVISIONING_GROUP_DEP_ROLE_LBL = "Dependent Role";
	public static final String SECURITY_PROVISIONING_GROUP_ROLE_LBL = "Role (to be granted)";
	
	// Security Provisioning Group errors
	public static final String ERROR_SECURITY_PROVISIONING_GROUP_AUTH_UNIQUE = "error.ksr.securityprovisioninggroup.authorizer.notunique";
	public static final String ERROR_SECURITY_PROVISIONING_GROUP_ROLE_INVALID = "error.ksr.securityprovisioninggroup.role.invalid";
	public static final String ERROR_SECURITY_PROVISIONING_GROUP_ROLE_UNIQUE = "error.ksr.securityprovisioninggroup.role.notunique";
	public static final String ERROR_SECURITY_PROVISIONING_GROUP_ROLE_DERIVED = "error.ksr.securityprovisioninggroup.role.derived";
	public static final String ERROR_SECURITY_PROVISIONING_GROUP_TAB_ORDER_UNIQUE = "error.ksr.securityprovisioninggroup.tabOrder.notunique";
	public static final String ERROR_SECURITY_PROVISIONING_GROUP_DEPENDENT_ROLE_UNIQUE = "error.ksr.securityprovisioninggroup.dependentRole.notunique";
	public static final String ERROR_SECURITY_PROVISIONING_GROUP_DEPENDENT_ROLE_BLANK = "error.ksr.securityprovisioninggroup.dependentRole.blank";
	public static final String ERROR_SECURITY_PROVISIONING_GROUP_CIRCULAR_REFERENCE = "error.ksr.securityprovisioninggroup.role.circular.reference";
	public static final String ERROR_SECURITY_PROVISIONING_GROUP_DEPENDENT_ROLE_MATCH = "error.ksr.securityprovisioninggroup.dependentRole.match";
	
	// Sequence names
	public static final String SECURITY_GROUP_SEQ_NAME = "KRSR_SEC_GRP_ID_SEQ";
	public static final String SECURITY_GROUP_TAB_SEQ_NAME = "KRSR_SEC_GRP_TB_ID_SEQ";
	public static final String SECURITY_PROVISIONING_GROUP_SEQ_NAME = "KRSR_SEC_PRV_ID_SEQ";
	
    public static final String SECURITY_REQUEST_WIZARD = "wizard";
    
    public static final String SECURITY_PROVISIONING_URL_NAME = "Edit Provisioning";
    
    
    
    public static final class PropertyConstants {
        public static final String TAB_ID = "tabId";
        public static final String TAB_ORDER = "tabOrder";
        public static final String ROLE_TAB_ORDER = "roleTabOrder";
        public static final String SECURITY_GROUP = "securityGroup";
    }
    
    public static final String AMPERSAND = "&";
    public static final String HTML_ENCODED_AMPERSAND = "&amp;";
    public static final String REDIRECT_URL_MODEL_KEY = "redirectUrl";
}
