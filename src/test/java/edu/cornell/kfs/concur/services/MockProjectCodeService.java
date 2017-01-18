package edu.cornell.kfs.concur.services;

import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.service.ProjectCodeService;

public class MockProjectCodeService implements ProjectCodeService {

    @Override
    public ProjectCode getByPrimaryId(String projectCode) {
        ProjectCode project = null;
        if (ConcurAccountValidationTestConstants.VALID_PROJECT_CODE.equalsIgnoreCase(projectCode)) {
            project = createProjectCode(projectCode);
            project.setActive(true);
        }
        if (ConcurAccountValidationTestConstants.INACTIVE_PROJECT_CODE.equalsIgnoreCase(projectCode)) {
            project = createProjectCode(projectCode);
            project.setActive(false);
        }
        return project;
    }
    
    private ProjectCode createProjectCode(String projectCode) {
        ProjectCode project = new ProjectCode();
        project.setCode(projectCode);
        return project;
    }

}
