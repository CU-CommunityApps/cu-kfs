package edu.cornell.kfs.module.ezra.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class Compliance extends PersistableBusinessObjectBase {

    private static final long serialVersionUID = 4199190611451784075L;

    private Long complianceId;
    private String projectId;
    private String awardProposalId;
    private Boolean everify;

    public Long getComplianceId() {
        return complianceId;
    }

    public void setComplianceId(Long complianceId) {
        this.complianceId = complianceId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getAwardProposalId() {
        return awardProposalId;
    }

    public void setAwardProposalId(String awardProposalId) {
        this.awardProposalId = awardProposalId;
    }

    public Boolean getEverify() {
        return everify;
    }

    public void setEverify(Boolean everify) {
        this.everify = everify;
    }

}
