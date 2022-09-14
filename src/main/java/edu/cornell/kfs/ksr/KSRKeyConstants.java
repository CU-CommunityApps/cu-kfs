package edu.cornell.kfs.ksr;

public class KSRKeyConstants {
    
    // Security Group errors
    public static final String ERROR_SECURITY_GROUP_NAME_UNIQUE = "error.ksr.securitygroup.name.notunique";
    public static final String ERROR_SECURITY_GROUP_TAB_ORDER_UNIQUE = "error.ksr.securitytab.tab.order.notunique";
    public static final String ERROR_SECURITY_GROUP_TAB_NAME_UNIQUE = "error.ksr.securitytab.tab.name.notunique";
    public static final String ERROR_SECURITY_GROUP_TAB_MISSING = "error.ksr.security.tab.missing";

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
    
    public static final String ERROR_SECURITY_REQUEST_DOC_PRINCIPAL_ID_MISSING = "error.ksr.securityrequestdocument.principalId.missing";
    public static final String ERROR_SECURITY_REQUEST_DOC_DEPENDENT_ROLE_MISSING = "error.ksr.securityrequestdocument.dependentRole.missing";
    public static final String ERROR_SECURITY_REQUEST_DOC_CHANGE_MISSING = "error.ksr.securityrequestdocument.change.missing";
    public static final String ERROR_SECURITY_REQUEST_DOC_SERVICE_EXCEPTION = "error.ksr.securityrequestdocument.service.exception";
    public static final String ERROR_SECURITY_REQUEST_DOC_PRIMARY_DEPARTMENT = "error.ksr.securityrequestdocument.service.primary.department";
    public static final String ERROR_SECURITY_REQUEST_DOC_QUALIFIER_MULTI_MISSING = "error.ksr.securityrequestdocument.qualifier.multi.missing";
    public static final String ERROR_SECURITY_REQUEST_DOC_QUALIFIER_EXCEPTION = "error.ksr.securityrequestdocument.qualifier.exception";
}
