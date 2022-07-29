package edu.cornell.kfs.module.purap.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerRoleSet;
import edu.cornell.kfs.module.purap.JaggaerConstants;
import edu.cornell.kfs.module.purap.businessobject.JaggaerRoleLinkMapping;
import edu.cornell.kfs.module.purap.service.JaggaerRoleLinkMappingService;
import edu.cornell.kfs.module.purap.service.JaggaerRoleService;

public class JaggaerRoleServiceImpl implements JaggaerRoleService {

    private static final Logger LOG = LogManager.getLogger();

    protected JaggaerRoleLinkMappingService jaggaerRoleLinkMappingService;
    protected PermissionService permissionService;

    @Override
    public List<String> getJaggaerRoles(Person user, JaggaerRoleSet roleSet) {
        List<String> roles = new ArrayList<>();
        for (JaggaerRoleLinkMapping link : jaggaerRoleLinkMappingService.getJaggaerLinkRoles(roleSet)) {
            if (isEshopPreAuthRole(link.getJaggaerRoleName())) {
                if (StringUtils.equalsIgnoreCase(link.getJaggaerRoleName(),
                        getEshopPreAuthValue(user.getPrincipalId()))) {
                    roles.add(link.getJaggaerRoleName());
                }
            } else if (isEshopViewRole(link.getJaggaerRoleName())) {
                if (StringUtils.equalsIgnoreCase(link.getJaggaerRoleName(), getEshopViewValue(user.getPrincipalId()))) {
                    roles.add(link.getJaggaerRoleName());
                }
            } else if (isDefaultJaggaerRole(link.getJaggaerRoleName())) {
                roles.add(link.getJaggaerRoleName());
            } else if (checkJaggaerRolePermission(link.getJaggaerRoleName(), user.getPrincipalId())) {
                roles.add(link.getJaggaerRoleName());
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getJaggaerRoles, not including role " + link.getJaggaerRoleName());
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("getJaggaerRoles, roleSet: " + roleSet + " returning roles: " + roles);
        }
        return roles;
    }

    protected boolean isEshopPreAuthRole(String role) {
        return StringUtils.equalsIgnoreCase(role, CUPurapConstants.SCIQUEST_ROLE_BUYER)
                || StringUtils.equalsIgnoreCase(role, CUPurapConstants.SCIQUEST_ROLE_SHOPPER);
    }

    protected String getEshopPreAuthValue(String principalId) {
        try {
            if (permissionService.hasPermission(principalId,
                    KFSConstants.OptionalModuleNamespaces.PURCHASING_ACCOUNTS_PAYABLE,
                    CUPurapConstants.B2B_SUBMIT_ESHOP_CART_PERMISSION)) {
                return CUPurapConstants.SCIQUEST_ROLE_BUYER;
            } else {
                return CUPurapConstants.SCIQUEST_ROLE_SHOPPER;
            }

        } catch (Exception e) {
            LOG.info("getEshopPreAuthValue, unable to check permssions for " + principalId, e);
            return CUPurapConstants.SCIQUEST_ROLE_SHOPPER;
        }
    }

    protected boolean isEshopViewRole(String role) {
        return StringUtils.equalsIgnoreCase(role, CUPurapConstants.SCIQUEST_ROLE_OFFICE)
                || StringUtils.equalsIgnoreCase(role, CUPurapConstants.SCIQUEST_ROLE_LAB)
                || StringUtils.equalsIgnoreCase(role, CUPurapConstants.SCIQUEST_ROLE_FACILITIES)
                || StringUtils.equalsIgnoreCase(role, CUPurapConstants.SCIQUEST_ROLE_UNRESTRICTED);
    }

    protected String getEshopViewValue(String principalId) {
        try {
            if (permissionService.hasPermission(principalId,
                    KFSConstants.OptionalModuleNamespaces.PURCHASING_ACCOUNTS_PAYABLE,
                    CUPurapConstants.B2B_SHOPPER_OFFICE_PERMISSION)) {
                return CUPurapConstants.SCIQUEST_ROLE_OFFICE;
            } else if (permissionService.hasPermission(principalId,
                    KFSConstants.OptionalModuleNamespaces.PURCHASING_ACCOUNTS_PAYABLE,
                    CUPurapConstants.B2B_SHOPPER_LAB_PERMISSION)) {
                return CUPurapConstants.SCIQUEST_ROLE_LAB;
            } else if (permissionService.hasPermission(principalId,
                    KFSConstants.OptionalModuleNamespaces.PURCHASING_ACCOUNTS_PAYABLE,
                    CUPurapConstants.B2B_SHOPPER_FACILITIES_PERMISSION)) {
                return CUPurapConstants.SCIQUEST_ROLE_FACILITIES;
            } else {
                return CUPurapConstants.SCIQUEST_ROLE_UNRESTRICTED;
            }

        } catch (Exception e) {
            LOG.info("getEshopViewValue, unable to check permssions for " + principalId, e);
            return CUPurapConstants.SCIQUEST_ROLE_UNRESTRICTED;
        }
    }

    protected boolean isDefaultJaggaerRole(String role) {
        return StringUtils.equalsIgnoreCase(role, JaggaerConstants.JAGGAER_ROLE_NAME_VIEW_ONLY);
    }

    protected boolean checkJaggaerRolePermission(String role, String principalId) {
        Map<String, String> permissionDetails = new HashMap<>();
        permissionDetails.put(JaggaerConstants.JAGGAER_ATTRIBUTE_VALUE_KEY, role);
        boolean hasPerm = permissionService.hasPermissionByTemplate(principalId, JaggaerConstants.JAGGAER_NAMESPACE,
                JaggaerConstants.JAGGAER_PERMISSION_TEMPLATE_NAME, permissionDetails);
        return hasPerm;
    }

    public void setJaggaerRoleLinkMappingService(JaggaerRoleLinkMappingService jaggaerRoleLinkMappingService) {
        this.jaggaerRoleLinkMappingService = jaggaerRoleLinkMappingService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }
}
