package edu.cornell.kfs.ksr.businessobject;

import java.util.Collections;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.service.KualiModuleService;
import org.kuali.kfs.krad.service.ModuleService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.context.SpringContext;

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
    default void initializeRoleBoIfNecessary(String roleId, Role role, Consumer<Role> roleSetter) {
        if (StringUtils.isBlank(roleId)) {
            if (ObjectUtils.isNotNull(role)) {
                roleSetter.accept(null);
            }
        } else if (ObjectUtils.isNull(role) || !StringUtils.equals(roleId, role.getId())) {
            Role newRole = getBusinessObjectService().findBySinglePrimaryKey(Role.class, roleId);
            roleSetter.accept(newRole);
        }
    }

    default BusinessObjectService getBusinessObjectService() {
        return SpringContext.getBean(BusinessObjectService.class);
    }

    /**
     * If the given role object is non-null, then a String
     * in the format of "roleId : namespaceCode - name" will be returned.
     * Otherwise, an empty String will be returned instead.
     * 
     * @param role The role to be printed; may be null.
     * @return A formatted role name String as described above, or an empty String if the role is null.
     */
    default String getFormattedRoleName(Role role) {
        if (ObjectUtils.isNotNull(role)) {
            return String.format("%s : %s - %s", role.getId(), role.getNamespaceCode(), role.getName());
        } else {
            return StringUtils.EMPTY;
        }
    }


}
