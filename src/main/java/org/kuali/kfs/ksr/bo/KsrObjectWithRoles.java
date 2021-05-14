package org.kuali.kfs.ksr.bo;

import java.util.Collections;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kim.impl.KIMPropertyConstants;
import org.kuali.rice.kim.impl.role.RoleBo;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;
import org.kuali.rice.krad.service.KualiModuleService;
import org.kuali.rice.krad.service.ModuleService;
import org.kuali.rice.krad.util.KRADUtils;

/**
 * ====
 * CU Customization:
 * Added a helper interface containing default method implementations
 * for retrieving roles and printing role names. This allows for
 * reducing the amount of duplicated code among the KSR data objects.
 * ====
 */
public interface KsrObjectWithRoles {

    /**
     * Updates the reference to a role BO if the ID field has changed.
     * If the ID is blank but the role BO is not, then a null role
     * will be passed to the given Consumer.
     * If the ID is non-blank and the role BO doesn't match or is null,
     * then the role will be reloaded and then passed to the given Consumer.
     * 
     * @param roleId The role ID to check against; may be blank.
     * @param role The role to be checked; may be null.
     * @param roleSetter The Consumer for updating the role object reference.
     */
    default void initializeRoleBoIfNecessary(String roleId, RoleBo role, Consumer<RoleBo> roleSetter) {
        if (StringUtils.isBlank(roleId)) {
            if (KRADUtils.isNotNull(role)) {
                roleSetter.accept(null);
            }
        } else if (KRADUtils.isNull(role) || !StringUtils.equals(roleId, role.getId())) {
            RoleBo newRole = getModuleServiceForRoleBo().getExternalizableBusinessObject(
                    RoleBo.class, Collections.singletonMap(KIMPropertyConstants.Role.ROLE_ID, roleId));
            roleSetter.accept(newRole);
        }
    }

    /**
     * If the given role object is non-null, then a String
     * in the format of "roleId : namespaceCode - name" will be returned.
     * Otherwise, an empty String will be returned instead.
     * 
     * @param role The role to be printed; may be null.
     * @return A formatted role name String as described above, or an empty String if the role is null.
     */
    default String getFormattedRoleName(RoleBo role) {
        if (KRADUtils.isNotNull(role)) {
            return String.format("%s : %s - %s", role.getId(), role.getNamespaceCode(), role.getName());
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Retrieves the ModuleService responsible for RoleBo objects.
     */
    default ModuleService getModuleServiceForRoleBo() {
        ModuleService moduleService = getKualiModuleService().getResponsibleModuleService(RoleBo.class);
        if (moduleService == null) {
            throw new IllegalStateException("Could not find module service that handles RoleBo objects");
        }
        return moduleService;
    }

    /**
     * Retrieves Rice's KualiModuleService. The default implementation
     * just returns the service from the KRAD service locator.
     */
    default KualiModuleService getKualiModuleService() {
        return KRADServiceLocatorWeb.getKualiModuleService();
    }

}
