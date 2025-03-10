package edu.cornell.kfs.module.cg.document.validation.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.cg.CGKeyConstants;
import org.kuali.kfs.module.cg.CGPropertyConstants;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.AwardAccount;
import org.kuali.kfs.module.cg.businessobject.AwardOrganization;
import org.kuali.kfs.module.cg.businessobject.AwardProjectDirector;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.module.cg.document.validation.impl.AwardRule;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;
import edu.cornell.kfs.module.cg.document.service.CuCGMaintenanceDocumentService;
import edu.cornell.kfs.module.cg.service.CuAwardAccountService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;

@SuppressWarnings("deprecation")
public class AwardExtensionRule extends AwardRule {
    private static final Logger LOG = LogManager.getLogger(AwardExtensionRule.class);
    
    protected ParameterService parameterService;
    protected CuCGMaintenanceDocumentService cuCGMaintenanceDocumentService;

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
    	boolean success = true;
    	success &= super.processCustomRouteDocumentBusinessRules(document);
    	success &= checkFinalFinancialReportRequired();
        success &= checkForDuplicateAccoutnts();
        success &= checkAccountsNotUsedOnOtherAwards();
        success &= checkForDuplicateAwardProjectDirector();
        success &= checkForDuplicateAwardOrganization();
        success &= checkEndAfterBegin(((AwardExtendedAttribute) newAwardCopy.getExtension()).getBudgetBeginningDate(),
                ((AwardExtendedAttribute) newAwardCopy.getExtension()).getBudgetEndingDate(), CUKFSPropertyConstants.AWARD_EXTENSION_BUDGET_ENDING_DATE);
        success &= checkAutoApproveReason(document);
    	
    	return success;
    }
    
    /**
     * @see org.kuali.kfs.module.cg.document.validation.impl.AwardRule#processCustomAddCollectionLineBusinessRules(org.kuali.kfs.kns.document.MaintenanceDocument, java.lang.String, org.kuali.kfs.krad.bo.PersistableBusinessObject)
     */
    @Override
    public boolean processCustomAddCollectionLineBusinessRules(MaintenanceDocument document, String collectionName, PersistableBusinessObject bo) {
    	boolean success = super.processCustomAddCollectionLineBusinessRules(document, collectionName, bo);
    	
        if (bo instanceof AwardAccount) {
        	AwardAccount awardAccount = (AwardAccount) bo;
            success &= checkAccountNotUsedOnOtherAwards(awardAccount);
        }
    	return success;
    }
    
    
	protected boolean checkFinalFinancialReportRequired() {
    	boolean success = true;
    	

    	AwardExtendedAttribute awardExtendedAttributeNew = (AwardExtendedAttribute) ( (Award) super.getNewBo()).getExtension();
    	AwardExtendedAttribute awardExtendedAttributeOld = (AwardExtendedAttribute) ( (Award) super.getOldBo()).getExtension();

    	if (awardExtendedAttributeNew.isFinalFinancialReportRequired() && null==awardExtendedAttributeNew.getFinalFiscalReportDate()) {
    		success = false;
    		putFieldError("extension.finalFiscalReportDate", CUKFSKeyConstants.ERROR_FINAL_FINANCIAL_REPORT_DATE_REQUIRED);
    	}

    	if (awardExtendedAttributeOld.isFinalFinancialReportRequired() && null==awardExtendedAttributeOld.getFinalFiscalReportDate()) {
    		success = false;
    		putFieldError("extension.finalFiscalReportDate", CUKFSKeyConstants.ERROR_FINAL_FINANCIAL_REPORT_DATE_REQUIRED);
    	}

    	
    	return success;
    }
	
	/**
     * @see org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase#setupBaseConvenienceObjects(org.kuali.kfs.kns.document.MaintenanceDocument)
     */
	@Override
    public void setupBaseConvenienceObjects(MaintenanceDocument document) {
        newAwardCopy = (Award) document.getNewMaintainableObject().getBusinessObject();
        Proposal tempProposal = newAwardCopy.getProposal();
        super.setupBaseConvenienceObjects(document);
        newAwardCopy = (Award) document.getNewMaintainableObject().getBusinessObject();
        newAwardCopy.setProposal(tempProposal);
        super.setNewBo(newAwardCopy);
        oldAwardCopy = (Award) super.getOldBo();
    }

    /**
     * Verifies the required federal pass through agency number is filled in when the
     * federal pass through indicator is set. Uses a service method to perform this check
     * so the same logic is invoked for both Awards and Proposals.
     */
    @Override
    protected boolean checkFederalPassThrough() {
        boolean success = getCuCGMaintenanceDocumentService().validFederalPassThroughData(
                newAwardCopy.getFederalPassThroughIndicator(), 
                newAwardCopy.getFederalPassThroughAgencyNumber());
               
        if (!success) {
            putFieldError(KFSPropertyConstants.FEDERAL_PASS_THROUGH_AGENCY_NUMBER, KFSKeyConstants.ERROR_FPT_AGENCY_NUMBER_REQUIRED);
        }
        
        return success;
    }

	protected boolean checkForDuplicateAccoutnts() {
        boolean success = true;
        String accountNumber;
        String accountChart;
        
        Collection<AwardAccount> awardAccounts = newAwardCopy.getAwardAccounts();
        HashSet<String> accountHash = new HashSet<String>();

        //validate if the newly entered award account is already on that award
        for(AwardAccount account: awardAccounts){
            if(account!=null && StringUtils.isNotEmpty(account.getAccountNumber())){
                accountNumber = account.getAccountNumber();
                accountChart  = account.getChartOfAccountsCode();
                if (!accountHash.add(accountChart+accountNumber)){
                   putFieldError(KFSPropertyConstants.AWARD_ACCOUNTS, CUKFSKeyConstants.ERROR_DUPLICATE_AWARD_ACCOUNT, accountChart + "-" + accountNumber);
                   return false;
                }
            }    
         }        
         return success;     
    }
   
    protected boolean checkForDuplicateAwardProjectDirector() {
        boolean success = true;
        String principalId;
        Collection<AwardProjectDirector> awardProjectDirectors = newAwardCopy.getAwardProjectDirectors();
        HashSet<String> principalIdHash = new HashSet<String>();

        //validate if the newly entered AwardProjectDirector is already on that award
        for(AwardProjectDirector projectDirector: awardProjectDirectors){
             if(projectDirector!=null && StringUtils.isNotEmpty(projectDirector.getPrincipalId())){
                principalId = projectDirector.getPrincipalId();
                if (!principalIdHash.add(principalId)){
                   putFieldError(KFSPropertyConstants.AWARD_PROJECT_DIRECTORS, CUKFSKeyConstants.ERROR_DUPLICATE_AWARD_PROJECT_DIRECTOR, principalId);
                   return false;
                }
            }   
         }        
         return success;
    }     
  
    protected boolean checkForDuplicateAwardOrganization() {
        boolean success = true;
        String organizationCode;
        String organizationChart;
        Collection<AwardOrganization> awardOrganizations = newAwardCopy.getAwardOrganizations();
        HashSet<String> orgaizationHash = new HashSet<String>();
        
        //validate if the newly entered awardOrganization is already on that award
        for(AwardOrganization awardOrganization: awardOrganizations){
            if(awardOrganization!=null && StringUtils.isNotEmpty(awardOrganization.getOrganizationCode())){
                organizationCode = awardOrganization.getOrganizationCode();
                organizationChart  = awardOrganization.getChartOfAccountsCode();
                if (!orgaizationHash.add(organizationChart+organizationCode)){
                   putFieldError(KFSPropertyConstants.AWARD_ORGRANIZATIONS, CUKFSKeyConstants.ERROR_DUPLICATE_AWARD_ORGANIZATION, organizationChart + "-" + organizationCode);
                   return false;
                }
            }
        }
        return success;
    }
    
    
	/**
	 * Checks that none of the accounts on the award are being used on an award.
	 * 
	 * @return true if not used, false otherwise
	 */
	protected boolean checkAccountsNotUsedOnOtherAwards() {
        boolean success = true;
        
        Collection<AwardAccount> awardAccounts = newAwardCopy.getAwardAccounts();

        //validate if the accounts on the award are not already used on another award
        for(AwardAccount account: awardAccounts){
        	success &= checkAccountNotUsedOnOtherAwards(account);  
         }        
         return success;     
    }
	
	/**
	 * Checks that the current account is not already used on another award.
	 * 
	 * @param awardAccount
	 * @return true if not used, false otherwise
	 */
	protected boolean checkAccountNotUsedOnOtherAwards(AwardAccount awardAccount) {
		boolean success = true;

		if (ObjectUtils.isNotNull(awardAccount) && StringUtils.isNotBlank(awardAccount.getChartOfAccountsCode()) && StringUtils.isNotBlank(awardAccount.getAccountNumber()) && awardAccount.isActive()) {
			String accountNumber = awardAccount.getAccountNumber();
			String accountChart = awardAccount.getChartOfAccountsCode();
			String proposalNumber = newAwardCopy.getProposalNumber();
			
			Collection<String> exemptAccounts = getParameterService().getParameterValuesAsString(AwardAccount.class, CUKFSConstants.CGParms.ACCOUNTS_EXEMPT_FROM_MULTIPLE_AWARDS_VALIDATION);
			
			if(exemptAccounts.contains(StringUtils.upperCase(accountChart, Locale.US) + ":" + StringUtils.upperCase(accountNumber, Locale.US))){
				// validation not needed
				return true;
			}

			boolean alreadyUsed = SpringContext.getBean(CuAwardAccountService.class).isAccountUsedOnAnotherAward(accountChart, accountNumber, proposalNumber);
			
			if (alreadyUsed) {
				putFieldError(KFSPropertyConstants.AWARD_ACCOUNTS, CUKFSKeyConstants.ERROR_AWARD_ACCOUNT_ALREADY_IN_USE, accountChart + "-" + accountNumber);
				success = false;
			}
		}

		return success;
	}
	
    protected boolean checkAutoApproveReason(MaintenanceDocument document) {
        boolean success = true;
        Award award = (Award) super.getNewBo();

        if (shouldEnforceAutoApproveReasonRule(document) && !award.getAutoApproveIndicator()) {
            AwardExtendedAttribute awardExtendedAttribute = (AwardExtendedAttribute) award.getExtension();
            if (StringUtils.isBlank(awardExtendedAttribute.getAutoApproveReason())) {
                putFieldError("extension.autoApproveReason", CUKFSKeyConstants.ERROR_AUTO_APPROVE_REASON_REQUIRED);
                success = false;
            }
        }

        return success;
    }

    private boolean shouldEnforceAutoApproveReasonRule(MaintenanceDocument document) {
        String documentInitiator = document.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();
        Collection<String> byPassInitiators = getParameterService().getParameterValuesAsString(
                CUKFSConstants.OptionalModuleNamespaces.CONTRACTS_AND_GRANTS,
                CUKFSConstants.CGParms.AWARD_RULE_COMPNONENT,
                CUKFSConstants.CGParms.BYPASS_AWARD_EXTENSION_AUTO_APPROVE_REASON_RULE_INITIATORS);
        if (LOG.isDebugEnabled()) {
            LOG.debug("shouldEnforceAutoApproveReasonRule, the document intiator: " + documentInitiator);
            LOG.debug("shouldEnforceAutoApproveReasonRule, the bypass intiators: " + byPassInitiators);
        }

        if (CollectionUtils.isNotEmpty(byPassInitiators) && byPassInitiators.contains(documentInitiator)) {
            LOG.debug("shouldEnforceAutoApproveReasonRule, the document initiator is in the list of bypass initiators, so do not enforce for the auto approve reason rule.");
            return false;
        }

        return true;
    }

    /**
     * Checks for null on the nullable Billing Frequency Code field. Fixes NullPointerException.
     */
    @Override
    protected boolean checkBillingFrequency() {
        boolean success = true;

        String newBillingFrequencyCode = newAwardCopy.getBillingFrequencyCode();
        String oldBillingFrequencyCode = oldAwardCopy.getBillingFrequencyCode();

        if (!StringUtils.equals(newBillingFrequencyCode, oldBillingFrequencyCode)) {
            final String proposalNumber = newAwardCopy.getProposalNumber();
            for(ContractsAndGrantsBillingAwardAccount awardAccount: newAwardCopy.getActiveAwardAccounts()) {
                if (StringUtils.equals(oldBillingFrequencyCode, ArConstants.BillingFrequencyValues.MILESTONE.getCode()) &&
                    hasActiveUnbilledMilestones(proposalNumber, awardAccount.getChartOfAccountsCode(),
                            awardAccount.getAccountNumber())) {
                    success = false;
                    putFieldError(CGPropertyConstants.AwardFields.BILLING_FREQUENCY_CODE,
                        CGKeyConstants.AwardConstants.ERROR_CG_ACTIVE_MILESTONES_EXIST,
                        getBillingFrequencyDescription(newAwardCopy));
                    break;
                } else if (StringUtils.equals(oldBillingFrequencyCode, ArConstants.BillingFrequencyValues.PREDETERMINED_BILLING.getCode()) &&
                    hasActiveUnbilledBills(proposalNumber, awardAccount.getChartOfAccountsCode(),
                            awardAccount.getAccountNumber())) {
                    success = false;
                    putFieldError(CGPropertyConstants.AwardFields.BILLING_FREQUENCY_CODE,
                        CGKeyConstants.AwardConstants.ERROR_CG_ACTIVE_BILLS_EXIST,
                        getBillingFrequencyDescription(newAwardCopy));
                    break;
                }
            }
        }

        return success;
    }

    private String getBillingFrequencyDescription(Award award) {
        if (ObjectUtils.isNull(award) || ObjectUtils.isNull(award.getBillingFrequency())) {
            return StringUtils.EMPTY;
        }
        return award.getBillingFrequency().getFrequencyDescription();
    }
    
    /**
     * Gets the parameterService
     * 
     * @return parameterService
     */
    public ParameterService getParameterService() {
        if ( parameterService == null ) {
            parameterService = SpringContext.getBean(ParameterService.class);
        }
        return parameterService;
    }
    
    public CuCGMaintenanceDocumentService getCuCGMaintenanceDocumentService() {
        if ( cuCGMaintenanceDocumentService == null ) {
            cuCGMaintenanceDocumentService = SpringContext.getBean(CuCGMaintenanceDocumentService.class);
        }
        return cuCGMaintenanceDocumentService;
    }
}
