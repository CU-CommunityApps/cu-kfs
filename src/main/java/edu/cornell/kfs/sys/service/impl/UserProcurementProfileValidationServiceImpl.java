package edu.cornell.kfs.sys.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kns.service.DictionaryValidationService;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.businessobject.UserProcurementProfile;
import edu.cornell.kfs.sys.service.UserProcurementProfileValidationService;

public class UserProcurementProfileValidationServiceImpl implements UserProcurementProfileValidationService{
	private DictionaryValidationService dictionaryValidationService;
	private BusinessObjectService businessObjectService;

	/**
	 * validate the favorite accounts in maint doc
	 * @param favoriteAccounts
	 * @return
	 */
    public boolean validateAccounts(List<FavoriteAccount> favoriteAccounts) {
//		GlobalVariables.getMessageMap().clearErrorMessages();
		if (CollectionUtils.isNotEmpty(favoriteAccounts)) {
			String propertyPreFix = "document.newMaintainableObject.favoriteAccounts[";
			int i = 0;
			boolean hasPrimary = false;
			for (FavoriteAccount account : favoriteAccounts) {
				validateAccount(propertyPreFix + i + "]", account, hasPrimary);
				hasPrimary = hasPrimary || account.getPrimaryInd();
				GlobalVariables.getMessageMap().removeFromErrorPath(propertyPreFix + i + "]");
				i++;
			}
			validateDuplicateAccount(favoriteAccounts);
		}
		return GlobalVariables.getMessageMap().hasNoErrors();

	}

    /**
     * check if user has procurement profile set up
     */
	public boolean validateUserProfileExist(String principalId) {
    	Map<String, String> fieldValues = new HashMap<String, String>();
    	fieldValues.put(KFSPropertyConstants.PRINCIPAL_ID, principalId);
    	if (CollectionUtils.isNotEmpty(businessObjectService.findMatching(UserProcurementProfile.class, fieldValues))) {
		    GlobalVariables.getMessageMap().putError("document.newMaintainableObject.profileUser.principalName", CUKFSKeyConstants.ERROR_USER_PROFILE_EXIST);
		    return true;
    	}
    	return false;


	}

	/*
	 * check if the favorite accounts has any duplicate
	 */
	private boolean validateDuplicateAccount(List<FavoriteAccount> favoriteAccounts) {
			String propertyPreFix = "document.newMaintainableObject.favoriteAccounts[";
			for (FavoriteAccount account : favoriteAccounts) {
				int i = 0;
				for (FavoriteAccount account1 : favoriteAccounts) {
					GlobalVariables.getMessageMap().addToErrorPath(propertyPreFix + i + "]");
					if (account != account1) {
						if (StringUtils.isNotBlank(account.getDescription()) && StringUtils.isNotBlank(account1.getDescription()) 
								&& StringUtils.equals(account.getDescription(), account1.getDescription())) {
							GlobalVariables.getMessageMap().putError(CUKFSPropertyConstants.DESCRIPTION, CUKFSKeyConstants.ERROR_DUPLICATE_FAVORITE_ACCOUNT_DESCRIPTION);
							
						}
						if (account.equals(account1)) {
							GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ACCOUNT_NUMBER, CUKFSKeyConstants.ERROR_DUPLICATE_ACCOUNTINGLINE);
							
						}
							
					}
					GlobalVariables.getMessageMap().removeFromErrorPath(propertyPreFix + i + "]");
					i++;
				}
			}
		return GlobalVariables.getMessageMap().hasNoErrors();

	}

	/**
	 * validate the favorite account 
	 */
	public boolean validateAccount(String propertyPreFix, FavoriteAccount account, boolean hasPrimary) {
		boolean valid = true;
		valid &= validateWithDictionary(propertyPreFix, account);
		if (hasPrimary && account.getPrimaryInd()) {
			GlobalVariables.getMessageMap().putError("primaryInd", CUKFSKeyConstants.ERROR_MULTIPLE_FAVORITE_ACCOUNTS);
			valid = false;
		}
		valid &= isValidCode(account);
		return valid;
	}
	

	/**
	 * validate bo by calling dictionaryValidationService
	 */
    public boolean validateBo(String propertyPreFix, PersistableBusinessObjectBase pbo) {
    	GlobalVariables.getMessageMap().clearErrorMessages();
        return validateWithDictionary(propertyPreFix, pbo);

    }
    
    private boolean validateWithDictionary(String propertyPreFix, PersistableBusinessObjectBase pbo) {
    	GlobalVariables.getMessageMap().addToErrorPath(propertyPreFix);
    	dictionaryValidationService.validate(pbo);
        return GlobalVariables.getMessageMap().hasNoErrors();

    }

    /**
     * check if user has permission to maintain other user's procurement profile
     */
    public boolean canMaintainUserProcurementProfile() {
    	return KimApiServiceLocator.getPermissionService().hasPermission(
    			GlobalVariables.getUserSession().getPrincipalId(), KFSConstants.CoreModuleNamespaces.KFS, CUKFSConstants.MAINTAIN_FAVORITE_ACCOUNT);
	
    }
    
	public void setDictionaryValidationService(DictionaryValidationService dictionaryValidationService) {
		this.dictionaryValidationService = dictionaryValidationService;
	}

	private boolean isValidCode(FavoriteAccount account) {
	    boolean valid = true;
	    if (StringUtils.isNotBlank(account.getChartOfAccountsCode())) {
	    	valid &= isValidChart(account.getChart(), "Chart", KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE);
	    }
	    if (StringUtils.isNotBlank(account.getAccountNumber())) {
	    	valid &= isValidAccount(account.getAccount(), "Account Number", KFSPropertyConstants.ACCOUNT_NUMBER);
	    }
	    if (StringUtils.isNotBlank(account.getSubAccountNumber())) {
	    	valid &= isValidSubAccount(account.getSubAccount(), "Sub Account Number", KFSPropertyConstants.SUB_ACCOUNT_NUMBER);
	    }
	    if (StringUtils.isNotBlank(account.getFinancialObjectCode())) {
	    	valid &= isValidObjectCode(account.getObjectCode(), "Object Code", KFSPropertyConstants.FINANCIAL_OBJECT_CODE);
	    }
	    if (StringUtils.isNotBlank(account.getFinancialSubObjectCode())) {
	    	valid &= isValidSubObjectCode(account.getSubObjectCode(), "Sub Object Code", KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE);
	    }
	    if (StringUtils.isNotBlank(account.getProjectCode())) {
	    	valid &= isValidProjectCode(account.getProject(), "Project Code", KFSPropertyConstants.PROJECT_CODE);
	    }
	    
	    
	    return valid;
	}
	
    private boolean isValidAccount(Account account, String label, String errorPropertyName) {
        // make sure it exists
        if (ObjectUtils.isNull(account) || StringUtils.isBlank(account.getAccountNumber())) {
            GlobalVariables.getMessageMap().putError(errorPropertyName, KFSKeyConstants.ERROR_EXISTENCE, label);
            return false;
        }

        // make sure it's active for usage
        if (account.isExpired()) {
            GlobalVariables.getMessageMap().putError(errorPropertyName, CUKFSKeyConstants.ERROR_ACCOUNT_EXPIRED, label);
            return false;
        }
        // make sure it's active for usage
        if (!account.isActive()) {
            GlobalVariables.getMessageMap().putError(errorPropertyName, KFSKeyConstants.ERROR_DOCUMENT_ACCOUNT_CLOSED, label);
            return false;
        }

        return true;
    }

    private boolean isValidObjectCode(ObjectCode objectCode, String label, String errorPropertyName) {

    	if (ObjectUtils.isNull(objectCode) || StringUtils.isBlank(objectCode.getFinancialObjectCode())) {
            GlobalVariables.getMessageMap().putError(errorPropertyName, KFSKeyConstants.ERROR_EXISTENCE, label);
            return false;
        }

        // check active status
        if (!objectCode.isFinancialObjectActiveCode()) {
            GlobalVariables.getMessageMap().putError(errorPropertyName, KFSKeyConstants.ERROR_INACTIVE, label);
            return false;
        }

        return true;
    }

    private boolean isValidChart(Chart chart, String label, String errorPropertyName) {

        // make sure it exists
        if (ObjectUtils.isNull(chart)) {
            GlobalVariables.getMessageMap().putError(errorPropertyName, KFSKeyConstants.ERROR_EXISTENCE, label);
            return false;
        }

        // make sure it's active for usage
        if (!chart.isActive()) {
            GlobalVariables.getMessageMap().putError(errorPropertyName, KFSKeyConstants.ERROR_INACTIVE, label);
            return false;
        }

        return true;
    }

    private boolean isValidProjectCode(ProjectCode project, String label, String errorPropertyName) {
        if (ObjectUtils.isNull(project)) {
            GlobalVariables.getMessageMap().putError(errorPropertyName, KFSKeyConstants.ERROR_EXISTENCE, label);
            return false;
        }

        // check activity
        if (!project.isActive()) {
            GlobalVariables.getMessageMap().putError(errorPropertyName, KFSKeyConstants.ERROR_INACTIVE, label);
            return false;
        }

        return true;
    }


    private boolean isValidSubAccount(SubAccount subAccount,  String label, String errorPropertyName) {
        // make sure it exists
        if (ObjectUtils.isNull(subAccount)) {
            GlobalVariables.getMessageMap().putError(errorPropertyName, KFSKeyConstants.ERROR_EXISTENCE, label);
            return false;
        }

        // check to make sure it is active
        if (!subAccount.isActive()) {
            GlobalVariables.getMessageMap().putError(errorPropertyName, KFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_INACTIVE, label);
            return false;
        }

        return true;
    }


    private boolean isValidSubObjectCode(SubObjectCode subObjectCode,  String label, String errorPropertyName) {
        if (ObjectUtils.isNull(subObjectCode)) {
            GlobalVariables.getMessageMap().putError(errorPropertyName, KFSKeyConstants.ERROR_EXISTENCE, label);
            return false;
        }

        // check active flag
        if (!subObjectCode.isActive()) {
            GlobalVariables.getMessageMap().putError(errorPropertyName, KFSKeyConstants.ERROR_INACTIVE, label);
            return false;
        }
        return true;
    }

    /**
     * check if the favorite account is already added to accounting line
     */
    public boolean isAccountExist(FavoriteAccount accountingLine, List<? extends GeneralLedgerPendingEntrySourceDetail> acctLines) {
    	for (GeneralLedgerPendingEntrySourceDetail acctline : acctLines) {
    		if (isEqualAcct(accountingLine, acctline)) {
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    private boolean isEqualAcct(FavoriteAccount accountingLine, GeneralLedgerPendingEntrySourceDetail acctLine) {
        return new EqualsBuilder().append(acctLine.getChartOfAccountsCode(), accountingLine.getChartOfAccountsCode())
                .append(acctLine.getAccountNumber(), accountingLine.getAccountNumber())
                .append(acctLine.getSubAccountNumber(), accountingLine.getSubAccountNumber())
                .append(acctLine.getFinancialObjectCode(), accountingLine.getFinancialObjectCode())
                .append(acctLine.getFinancialSubObjectCode(), accountingLine.getFinancialSubObjectCode())
                .append(acctLine.getProjectCode(), accountingLine.getProjectCode())
                .append(acctLine.getOrganizationReferenceId(), accountingLine.getOrganizationReferenceId())
                .isEquals();
    }

	public void setBusinessObjectService(BusinessObjectService businessObjectService) {
		this.businessObjectService = businessObjectService;
	}

}
