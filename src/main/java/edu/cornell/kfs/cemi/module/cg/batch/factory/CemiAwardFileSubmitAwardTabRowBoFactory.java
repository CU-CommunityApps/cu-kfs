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
        Validate.notNull(headerBo, "headerBo cannot be null for CemiAwardFileSubmitAwardTabRowBoFactory");
        Validate.notNull(awardLineBo, "awardLineBo cannot be null for CemiAwardFileSubmitAwardTabRowBoFactory");
        Validate.notNull(specialConditionBo, "specialConditionBo cannot be null for CemiAwardFileSubmitAwardTabRowBoFactory");
        Validate.notNull(budgetBo, "budgetBo cannot be null for CemiAwardFileSubmitAwardTabRowBoFactory");
        Validate.notNull(allocationBo, "allocationBo cannot be null for CemiAwardFileSubmitAwardTabRowBoFactory");
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
        //keys for row
        //submitAwardRow.setJobRunRowIndex(null);
        //submitAwardRow.setJobRunDateString(null);
        submitAwardRow.setProposalNumberUsedForDataRow(headerBo.getProposalNumberUsedForDataRow());
        
        //header 
        submitAwardRow.setSpreadsheetKey(headerBo.getSpreadsheetKey());
        submitAwardRow.setAddOnly(headerBo.getAddOnly());
        submitAwardRow.setAward(headerBo.getAward());
        submitAwardRow.setAutoComplete(headerBo.getAutoComplete());
        submitAwardRow.setAwardReferenceId(headerBo.getAwardReferenceId());
        submitAwardRow.setAwardNumber(headerBo.getAwardNumber());
        submitAwardRow.setAwardHeaderVersion(headerBo.getAwardHeaderVersion());
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
        submitAwardRow.setAwardFunction(headerBo.getAwardFunction());
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
        
        //award line
        submitAwardRow.setAwardLineDataRowId(awardLineBo.getAwardLineDataRowId());
        submitAwardRow.setReceivableContractLine(awardLineBo.getReceivableContractLine());
        submitAwardRow.setReceivableContractLineReferenceId(awardLineBo.getReceivableContractLineReferenceId());
        submitAwardRow.setLineNumber(awardLineBo.getLineNumber());
        submitAwardRow.setIntercompanyAffiliate(awardLineBo.getIntercompanyAffiliate());
        submitAwardRow.setRevenueCategory(awardLineBo.getRevenueCategory());
        submitAwardRow.setAwardLineDataAwardLifecycleStatus(awardLineBo.getAwardLineDataAwardLifecycleStatus());
        submitAwardRow.setLineType(awardLineBo.getLineType());
        submitAwardRow.setSpendRestriction(awardLineBo.getSpendRestriction());
        submitAwardRow.setLineItemDescriptionOverride(awardLineBo.getLineItemDescriptionOverride());
        submitAwardRow.setDeferredRevenue(awardLineBo.getDeferredRevenue());
        submitAwardRow.setLineStatus(awardLineBo.getLineStatus());
        submitAwardRow.setAwardLineDocumentStatus(awardLineBo.getAwardLineDocumentStatus());
        submitAwardRow.setPrimaryGrant(awardLineBo.getPrimaryGrant());
        submitAwardRow.setLineCfdaNumber(awardLineBo.getLineCfdaNumber());
        submitAwardRow.setGrantId(awardLineBo.getGrantId());
        submitAwardRow.setLineAmount(awardLineBo.getLineAmount());
        submitAwardRow.setRateAgreement(awardLineBo.getRateAgreement());
        submitAwardRow.setCostRateType(awardLineBo.getCostRateType());
        submitAwardRow.setException(awardLineBo.getException());
        submitAwardRow.setRevenueAllocationProfile(awardLineBo.getRevenueAllocationProfile());
        submitAwardRow.setDelete(awardLineBo.getDelete());
        submitAwardRow.setBasisLimit(awardLineBo.getBasisLimit());
        submitAwardRow.setBasisLimitId(awardLineBo.getBasisLimitId());
        submitAwardRow.setBasisLimitName(awardLineBo.getBasisLimitName());
        submitAwardRow.setBasisLimitAmount(awardLineBo.getBasisLimitAmount());
        submitAwardRow.setAwardLineStartDate(awardLineBo.getAwardLineStartDate());
        submitAwardRow.setAwardLineEndDate(awardLineBo.getAwardLineEndDate());
        submitAwardRow.setAwardLineDescription(awardLineBo.getAwardLineDescription());
        submitAwardRow.setLineInvoiceMemoOverride(awardLineBo.getLineInvoiceMemoOverride());
        submitAwardRow.setAwardLineDataCostCenter(awardLineBo.getAwardLineDataCostCenter());
        submitAwardRow.setAwardLineDataFund(awardLineBo.getAwardLineDataFund());
        submitAwardRow.setAwardLineDataFunction(awardLineBo.getAwardLineDataFunction());
        submitAwardRow.setAwardLineSalaryCap(awardLineBo.getAwardLineSalaryCap());
        submitAwardRow.setAwardLineSalaryCapOverride(awardLineBo.getAwardLineSalaryCapOverride());
        submitAwardRow.setSubrecipient(awardLineBo.getSubrecipient());
        submitAwardRow.setLineFederalAwardIdNumber(awardLineBo.getLineFederalAwardIdNumber());
        submitAwardRow.setLineBillingNotes(awardLineBo.getLineBillingNotes());
        submitAwardRow.setRevenueRecognitionLineNotes(awardLineBo.getRevenueRecognitionLineNotes());
        
        //special conditions
        submitAwardRow.setSpecialConditionDataRowId(specialConditionBo.getSpecialConditionDataRowId());
        submitAwardRow.setSpecialConditionDataDelete(specialConditionBo.getSpecialConditionDataDelete());
        submitAwardRow.setSpecialCondition(specialConditionBo.getSpecialCondition());
        submitAwardRow.setSpecialConditionReferenceId(specialConditionBo.getSpecialConditionReferenceId());
        submitAwardRow.setSpecialConditionType(specialConditionBo.getSpecialConditionType());
        submitAwardRow.setSpecialConditionComment(specialConditionBo.getSpecialConditionComment());
        
        //budget
        submitAwardRow.setAwardBudgetDataRowId(budgetBo.getAwardBudgetDataRowId());
        submitAwardRow.setDefaultBudgetStructure(budgetBo.getDefaultBudgetStructure());
        submitAwardRow.setDefaultBudgetType(budgetBo.getDefaultBudgetType());
        submitAwardRow.setDefaultBalancedAmendment(budgetBo.getDefaultBalancedAmendment());
        
        //allocation
        submitAwardRow.setNsfCodeAllocationDataRowId(allocationBo.getNsfCodeAllocationDataRowId());
        submitAwardRow.setNsfCodeAllocationDataDelete(allocationBo.getNsfCodeAllocationDataDelete());
        submitAwardRow.setNsfCodeAllocation(allocationBo.getNsfCodeAllocation());
        submitAwardRow.setNsfCodeAllocationId(allocationBo.getNsfCodeAllocationId());
        submitAwardRow.setNsfCodeAllocationPercentage(allocationBo.getNsfCodeAllocationPercentage());
        submitAwardRow.setNsfCode(allocationBo.getNsfCode());
        
        return submitAwardRow;
    }
}
