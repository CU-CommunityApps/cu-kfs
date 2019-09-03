package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.uif.util.ObjectPropertyUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.businessobject.CGProjectDirector;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;

import edu.cornell.kfs.rass.batch.xml.RassXMLAwardPiCoPiEntry;

public class RassProjectDirectorConverter extends RassValueConverterBase {

	protected PersonService personService;
	protected Class<? extends CGProjectDirector> projectDirectorImplementationClass;
	protected String projectDirectorPrimaryIndicatorPropertyName;

	@SuppressWarnings("unchecked")
	@Override
	public Object convert(Class<? extends PersistableBusinessObject> businessObjectClass, RassPropertyDefinition propertyMapping, Object propertyValue) {
		List<RassXMLAwardPiCoPiEntry> rassAwardPiCoPiEntries = (List<RassXMLAwardPiCoPiEntry>) propertyValue;
		List<CGProjectDirector> projectDirectors = new ArrayList<>();
		if (rassAwardPiCoPiEntries != null && rassAwardPiCoPiEntries.size() > 0) {
			for (RassXMLAwardPiCoPiEntry rassAwardPiCoPi : rassAwardPiCoPiEntries) {
				CGProjectDirector projectDirector = createNewProjectDirectorInstance();
				Person projectDirectorPerson = personService
						.getPersonByPrincipalName(rassAwardPiCoPi.getProjectDirectorPrincipalName());
				if (ObjectUtils.isNull(projectDirectorPerson)) {
				    throw new RuntimeException("Cannot find person with principal name \""
				            + rassAwardPiCoPi.getProjectDirectorPrincipalName() + "\"");
				}
				projectDirector.setPrincipalId(projectDirectorPerson.getPrincipalId());
				ObjectPropertyUtils.setPropertyValue(
				        projectDirector, projectDirectorPrimaryIndicatorPropertyName, getNullSafePrimaryDirectorFlag(rassAwardPiCoPi));
				projectDirectors.add(projectDirector);
			}
		}

		return projectDirectors;
	}

    protected CGProjectDirector createNewProjectDirectorInstance() {
        if (projectDirectorImplementationClass == null) {
            throw new IllegalStateException("Project director implementation class cannot be null");
        }
        
        try {
            return projectDirectorImplementationClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Unable to create project director of type "
                    + projectDirectorImplementationClass.getName(), e);
        }
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

}
