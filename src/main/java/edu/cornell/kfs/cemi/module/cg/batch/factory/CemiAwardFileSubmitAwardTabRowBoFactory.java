package edu.cornell.kfs.cemi.module.cg.batch.factory;

import org.apache.commons.lang3.Validate;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardAllocationDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardBudgetDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardFileSubmitAwardTabRowBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardHeaderDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLineDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardSpecialConditionDataBo;

@SuppressWarnings("deprecation")
public class CemiAwardFileSubmitAwardTabRowBoFactory {
    
    private CemiAwardHeaderDataBo headerBo;
    private CemiAwardLineDataBo awardLineBo;
    private CemiAwardSpecialConditionDataBo specialConditionBo;
    private CemiAwardBudgetDataBo budgetBo;
    private CemiAwardAllocationDataBo allocationBo;
    
    public CemiAwardFileSubmitAwardTabRowBoFactory (
            final CemiAwardHeaderDataBo headerBo, final CemiAwardLineDataBo awardLineBo,
            final CemiAwardSpecialConditionDataBo specialConditionBo, final CemiAwardBudgetDataBo budgetBo,
            final CemiAwardAllocationDataBo allocationBo) {
        Validate.notNull(headerBo, "headerBo cannot be null");
        Validate.notNull(awardLineBo, "awardLineBo cannot be null");
        Validate.notNull(specialConditionBo, "specialConditionBo cannot be null");
        Validate.notNull(budgetBo, "budgetBo cannot be null");
        Validate.notNull(allocationBo, "allocationBo cannot be null");
        this.headerBo = headerBo;
        this.awardLineBo = awardLineBo;
        this.specialConditionBo = specialConditionBo;
        this.budgetBo = budgetBo;
        this.allocationBo = allocationBo;
    }
    
    public static CemiAwardFileSubmitAwardTabRowBo createTabRowBoFrom (
            final CemiAwardHeaderDataBo headerBo, final CemiAwardLineDataBo awardLineBo,
            final CemiAwardSpecialConditionDataBo specialConditionBo, final CemiAwardBudgetDataBo budgetBo,
            final CemiAwardAllocationDataBo allocationBo) {
        final CemiAwardFileSubmitAwardTabRowBoFactory factory = 
                new CemiAwardFileSubmitAwardTabRowBoFactory(headerBo, awardLineBo, specialConditionBo, 
                        budgetBo, allocationBo);
        return factory.createCemiAwardFileSubmitAwardTabRowBo();
    }
    
    public CemiAwardFileSubmitAwardTabRowBo createCemiAwardFileSubmitAwardTabRowBo() {
        
        final CemiAwardFileSubmitAwardTabRowBo submitAwardRow = new CemiAwardFileSubmitAwardTabRowBo();
        
        //header 
        submitAwardRow.setSpreadsheetKey(headerBo.getSpreadsheetKey());
        submitAwardRow.setAddOnly(headerBo.getAddOnly());
        submitAwardRow.setAward(headerBo.getAward());
        submitAwardRow.setAutoComplete(headerBo.getAutoComplete());
        submitAwardRow.setAwardReferenceId(headerBo.getAwardReferenceId());
        submitAwardRow.setAwardNumber(headerBo.getAwardNumber());
        submitAwardRow.setCompany(headerBo.getCompany());
        submitAwardRow.setSponsorAwardReferenceNumber(headerBo.getSponsorAwardReferenceNumber());
        submitAwardRow.setAwardName(headerBo.getAwardName());
        submitAwardRow.setAwardDescription(headerBo.getAwardDescription());
        submitAwardRow.setAwardEffectiveDate(headerBo.getAwardEffectiveDate());
        submitAwardRow.setAwardSignedDate(headerBo.getAwardSignedDate());
        submitAwardRow.setAwardType(headerBo.getAwardType());
        submitAwardRow.setPurposeCode(headerBo.getPurposeCode());
        submitAwardRow.setPaymentTerms(headerBo.getPaymentTerms());
        submitAwardRow.setInstitutionalId(headerBo.getInstitutionalId());
        submitAwardRow.setAwardSalaryCapDefault(headerBo.getAwardSalaryCapDefault());
        submitAwardRow.setSpendRestrictionDefault(headerBo.getSpendRestrictionDefault());
        submitAwardRow.setRelatedAward(headerBo.getRelatedAward());
        submitAwardRow.setAwardGroup(headerBo.getAwardGroup());
        submitAwardRow.setAwardCostCenter(headerBo.getAwardCostCenter());
        submitAwardRow.setAwardFund(headerBo.getAwardFund());
        submitAwardRow .setAwardProgram(headerBo.getAwardProgram());
        submitAwardRow.setAwardContractOwner(headerBo.getAwardContractOwner());
        submitAwardRow.setAwardLifecycleStatus(headerBo.getAwardLifecycleStatus());
        submitAwardRow.setSubAward(headerBo.getSubAward());
        submitAwardRow.setPrimeSponsor(headerBo.getPrimeSponsor());
        submitAwardRow.setSponsor(headerBo.getSponsor());
        submitAwardRow.setBillToSponsor(headerBo.getBillToSponsor());
        submitAwardRow.setPaymentType(headerBo.getPaymentType());
        submitAwardRow.setLetterOfCredit(headerBo.getLetterOfCredit());
        submitAwardRow.setLetterOfCreditDocumentId(headerBo.getLetterOfCreditDocumentId());
        submitAwardRow.setAwardSequenceBillingActiveReference(headerBo.getAwardSequenceBillingActiveReference());
        submitAwardRow.setAwardBillingSequenceNumberFormatSyntaxReference(headerBo.getAwardBillingSequenceNumberFormatSyntaxReference());
        submitAwardRow.setCurrentAwardBillingSequenceNumberUsedReference(headerBo.getCurrentAwardBillingSequenceNumberUsedReference());
        submitAwardRow.setAwardBillingSequenceGeneratorRule(headerBo.getAwardBillingSequenceGeneratorRule());
        submitAwardRow.setCurrency(headerBo.getCurrency());
        submitAwardRow.setZeroAmountAward(headerBo.getZeroAmountAward());
        submitAwardRow.setSponsorDirectCostAmount(headerBo.getSponsorDirectCostAmount());
        submitAwardRow.setSponsorFacilitiesAndAdministrationAmount(headerBo.getSponsorFacilitiesAndAdministrationAmount());
        submitAwardRow.setCostShareTotalAmount(headerBo.getCostShareTotalAmount());
        submitAwardRow.setAuthorizedAmount(headerBo.getAuthorizedAmount());
        submitAwardRow.setBillingLimitOverride(headerBo.getBillingLimitOverride());
        submitAwardRow.setCostShareRequiredBySponsor(headerBo.getCostShareRequiredBySponsor());
        submitAwardRow.setAnticipatedSponsorDirectCostAmount(headerBo.getAnticipatedSponsorDirectCostAmount());
        submitAwardRow.setAnticipatedFacilitiesAndAdministrationAmount(headerBo.getAnticipatedFacilitiesAndAdministrationAmount());
        submitAwardRow.setAwardSchedule(headerBo.getAwardSchedule());
        submitAwardRow.setFederalAwardIdNumber(headerBo.getFederalAwardIdNumber());
        submitAwardRow.setCfdaNumber(headerBo.getCfdaNumber());
        submitAwardRow.setProposalId(headerBo.getProposalId());
        submitAwardRow.setProposalVersion(headerBo.getProposalVersion());
        submitAwardRow.setOriginalProposal(headerBo.getOriginalProposal());
        submitAwardRow.setAwardAmendmentReason(headerBo.getAwardAmendmentReason());
        submitAwardRow.setAwardAmendmentEffectiveDate(headerBo.getAwardAmendmentEffectiveDate());
        submitAwardRow.setAwardNotes(headerBo.getAwardNotes());
        submitAwardRow.setBillingNotes(headerBo.getBillingNotes());
        
        return submitAwardRow;
    }
}
