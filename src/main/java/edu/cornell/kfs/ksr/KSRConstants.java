package edu.cornell.kfs.ksr;

public class KSRConstants {
	public static final String KSR_DOCUMENT_MAINTAINABLE = "document.newMaintainableObject";
	
	public static final String KSR_NAMESPACE = "KFS-SR";

	// Security Group sections
	public static final String SECTION_SECURITY_TABS = "SecurityGroupTabs";

    // Security Group parameters
    public static final String SECURITY_GROUP_NAME = "securityGroupName";
    public static final String SECURITY_GROUP_ID = "securityGroupId";
	
	// Security Group Tab properties
	public static final String SECURITY_GROUP_TAB_NAME = "tabName";
	public static final String SECURITY_GROUP_TAB_ID = "tabId";
	public static final String SECURITY_GROUP_TAB_ORDER = "tabOrder";
	
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

	// Sequence names
	public static final String SECURITY_GROUP_SEQ_NAME = "KRSR_SEC_GRP_ID_SEQ";
	public static final String SECURITY_GROUP_TAB_SEQ_NAME = "KRSR_SEC_GRP_TB_ID_SEQ";
	public static final String SECURITY_PROVISIONING_GROUP_SEQ_NAME = "KRSR_SEC_PRV_ID_SEQ";
    
    public static final String SECURITY_PROVISIONING_URL_NAME = "Edit Provisioning";
    
    // Security Provisioning Group labels
    public static final String SECURITY_PROVISIONING_GROUP_ADD_AUTH_LBL = "Additional Authorizer";
    public static final String SECURITY_PROVISIONING_GROUP_CENT_AUTH_LBL = "Central Authorizer";
    public static final String SECURITY_PROVISIONING_GROUP_DIST_AUTH_LBL = "Distributed Authorizer";
    public static final String SECURITY_PROVISIONING_GROUP_DEP_ROLE_LBL = "Dependent Role";
    public static final String SECURITY_PROVISIONING_GROUP_ROLE_LBL = "Role (to be granted)";
}
