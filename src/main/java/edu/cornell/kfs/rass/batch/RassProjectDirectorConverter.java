package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.role.RoleService;
import org.kuali.kfs.kim.impl.role.RoleLite;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.ObjectPropertyUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.businessobject.CGProjectDirector;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.rass.batch.xml.RassXMLAwardPiCoPiEntry;

public class RassProjectDirectorConverter extends RassValueConverterBase {
    private static final Logger LOG = LogManager.getLogger(RassProjectDirectorConverter.class);

    protected PersonService personService;
    protected RoleService roleService;
    protected Class<? extends CGProjectDirector> projectDirectorImplementationClass;
    protected String projectDirectorPrimaryIndicatorPropertyName;

    @SuppressWarnings("unchecked")
    @Override
    public Object convert(Class<? extends PersistableBusinessObject> businessObjectClass, RassPropertyDefinition propertyMapping, Object propertyValue) {
        List<RassXMLAwardPiCoPiEntry> rassAwardPiCoPiEntries = (List<RassXMLAwardPiCoPiEntry>) propertyValue;
        List<CGProjectDirector> projectDirectors = new ArrayList<>();
        if (rassAwardPiCoPiEntries != null && rassAwardPiCoPiEntries.size() > 0) {
            for (RassXMLAwardPiCoPiEntry rassAwardPiCoPi : rassAwardPiCoPiEntries) {
                CGProjectDirector projectDirector = buildProjectDirector(rassAwardPiCoPi);
                projectDirectors.add(projectDirector);
            }
        }

        return projectDirectors;
    }

    protected CGProjectDirector buildProjectDirector(RassXMLAwardPiCoPiEntry rassAwardPiCoPi) {
        CGProjectDirector projectDirector = createNewProjectDirectorInstance();
        Person projectDirectorPerson = personService.getPersonByPrincipalName(rassAwardPiCoPi.getProjectDirectorPrincipalName());
        if (ObjectUtils.isNull(projectDirectorPerson)) {
            throw new RuntimeException("Cannot find person with principal name \"" + rassAwardPiCoPi.getProjectDirectorPrincipalName() + "\"");
        }

        if (!doesPersonHaveProjectDirectorRole(projectDirectorPerson)) {
            LOG.info("buildProjectDirector, " + projectDirectorPerson.getPrincipalName() + " needs to be added to the project director role");
            roleService.assignPrincipalToRole(projectDirectorPerson.getPrincipalId(), KFSConstants.CoreModuleNamespaces.KFS,
                    KFSConstants.SysKimApiConstants.CONTRACTS_AND_GRANTS_PROJECT_DIRECTOR, new HashMap<String, String>());
        }

        projectDirector.setPrincipalId(projectDirectorPerson.getPrincipalId());
        ObjectPropertyUtils.setPropertyValue(projectDirector, projectDirectorPrimaryIndicatorPropertyName,
                getNullSafePrimaryDirectorFlag(rassAwardPiCoPi));
        return projectDirector;
    }

    protected CGProjectDirector createNewProjectDirectorInstance() {
        if (projectDirectorImplementationClass == null) {
            throw new IllegalStateException("Project director implementation class cannot be null");
        }

        try {
            return projectDirectorImplementationClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Unable to create project director of type " + projectDirectorImplementationClass.getName(), e);
        }
    }

    protected boolean doesPersonHaveProjectDirectorRole(Person person) {
        RoleLite orojectDirectRole = roleService.getRoleByNamespaceCodeAndName(KFSConstants.CoreModuleNamespaces.KFS,
                KFSConstants.SysKimApiConstants.CONTRACTS_AND_GRANTS_PROJECT_DIRECTOR);
        if (ObjectUtils.isNull(orojectDirectRole)) {
            throw new RuntimeException("Unable to find Contracts and Greants project director role");
        }
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(orojectDirectRole.getId());
        return roleService.principalHasRole(person.getPrincipalId(), roleIds, new HashMap<String, String>());
    }

    protected Boolean getNullSafePrimaryDirectorFlag(RassXMLAwardPiCoPiEntry rassAwardPiCoPi) {
        return rassAwardPiCoPi.getPrimary() != null ? rassAwardPiCoPi.getPrimary() : Boolean.FALSE;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setProjectDirectorImplementationClass(Class<? extends CGProjectDirector> projectDirectorImplementationClass) {
        this.projectDirectorImplementationClass = projectDirectorImplementationClass;
    }

    public void setProjectDirectorPrimaryIndicatorPropertyName(String projectDirectorPrimaryIndicatorPropertyName) {
        this.projectDirectorPrimaryIndicatorPropertyName = projectDirectorPrimaryIndicatorPropertyName;
    }

    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

}
