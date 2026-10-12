package edu.cornell.kfs.cemi.module.cg.batch.businessobject;

import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class CemiAwardLegacyCostingSummariesForSingleProposalNumberBo extends PersistableBusinessObjectBase {
    
    private String proposalNumber;
    private KualiDecimal sponsorDirectCostAmount;
    private KualiDecimal sponsorIndirectCostAmount;
    private KualiDecimal authorizedAmount;
    
    public CemiAwardLegacyCostingSummariesForSingleProposalNumberBo() {
        
    }
    
    public CemiAwardLegacyCostingSummariesForSingleProposalNumberBo(String proposalNumber,
            KualiDecimal sponsorDirectCostAmount, KualiDecimal sponsorIndirectCostAmount, 
            KualiDecimal authorizedAmount) {
        this.proposalNumber = proposalNumber;
        this.sponsorDirectCostAmount = sponsorDirectCostAmount;
        this.sponsorIndirectCostAmount = sponsorIndirectCostAmount;
        this.authorizedAmount = authorizedAmount;
    }

    public String getProposalNumber() {
        return proposalNumber;
    }

    public void setProposalNumber(String proposalNumber) {
        this.proposalNumber = proposalNumber;
    }

    public KualiDecimal getSponsorDirectCostAmount() {
        return sponsorDirectCostAmount;
    }

    public void setSponsorDirectCostAmount(KualiDecimal sponsorDirectCostAmount) {
        this.sponsorDirectCostAmount = sponsorDirectCostAmount;
    }

    public KualiDecimal getSponsorIndirectCostAmount() {
        return sponsorIndirectCostAmount;
    }

    public void setSponsorIndirectCostAmount(KualiDecimal sponsorIndirectCostAmount) {
        this.sponsorIndirectCostAmount = sponsorIndirectCostAmount;
    }

    public KualiDecimal getAuthorizedAmount() {
        return authorizedAmount;
    }

    public void setAuthorizedAmount(KualiDecimal authorizedAmount) {
        this.authorizedAmount = authorizedAmount;
    }
    
}