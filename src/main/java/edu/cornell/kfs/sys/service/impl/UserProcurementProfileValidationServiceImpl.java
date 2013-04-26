package edu.cornell.kfs.sys.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kim.service.RoleManagementService;
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DictionaryValidationService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.businessobject.UserProcurementProfile;
import edu.cornell.kfs.sys.service.UserProcurementProfileValidationService;

public class UserProcurementProfileValidationServiceImpl implements UserProcurementProfileValidationService{
	private DictionaryValidationService dictionaryValidationService;
	private BusinessObjectService businessObjectService;
	private RoleManagementService roleManagementService;

	/**
	 * validate the favorite accounts in maint doc
	 * @param favoriteAccounts
	 * @return
	 */
    public boolean validateAccounts(List<FavoriteAccount> favoriteAccounts) {
		GlobalVariables.getMessageMap().clearErrorMessages();
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
    	fieldValues.put("principalId", principalId);
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
							GlobalVariables.getMessageMap().putError("description", CUKFSKeyConstants.ERROR_DUPLICATE_FAVORITE_ACCOUNT_DESCRIPTION);
							
						}
						if (account.equals(account1)) {
							GlobalVariables.getMessageMap().putError("accountNumber", CUKFSKeyConstants.ERROR_DUPLICATE_ACCOUNTINGLINE);
							
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
    	dictionaryValidationService.validateBusinessObject(pbo);
        return GlobalVariables.getMessageMap().hasNoErrors();

    }

    /**
     * check if user has permission to maintain other user's procurement profile
     */
    public boolean canMaintainUserProcurementProfile() {
		List<String> roleIds = new ArrayList<String>();
		roleIds.add(roleManagementService.getRoleIdByName(KFSConstants.ParameterNamespaces.KFS,"Manager"));
		return roleManagementService.principalHasRole(GlobalVariables.getUserSession().getPrincipalId(),roleIds, null);
	
    }
    
	public void setDictionaryValidationService(DictionaryValidationService dictionaryValidationService) {
		this.dictionaryValidationService = dictionaryValidationService;
	}

	private boolean isValidCode(FavoriteAccount account) {
	    boolean valid = true;
	    if (StringUtils.isNotBlank(account.getChartOfAccountsCode())) {
	    	valid &= isValidChart(account.getChart(), "Chart", "chartOfAccountsCode");
	    }
	    if (StringUtils.isNotBlank(account.getAccountNumber())) {
	    	valid &= isValidAccount(account.getAccount(), "Account Number", "accountNumber");
	    }
	    if (StringUtils.isNotBlank(account.getSubAccountNumber())) {
	    	valid &= isValidSubAccount(account.getSubAccount(), "Sub Account Number", "subAccountNumber");
	    }
	    if (StringUtils.isNotBlank(account.getFinancialObjectCode())) {
	    	valid &= isValidObjectCode(account.getObjectCode(), "Object Code", "financialObjectCode");
	    }
	    if (StringUtils.isNotBlank(account.getFinancialSubObjectCode())) {
	    	valid &= isValidSubObjectCode(account.getSubObjectCode(), "Sub Object Code", "financialSubObjectCode");
	    }
	    if (StringUtils.isNotBlank(account.getProjectCode())) {
	    	valid &= isValidProjectCode(account.getProject(), "Project Code", "projectCode");
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
     * check if the favorite account is alreadded to accounting line
     */
    public boolean isAccountExist(FavoriteAccount accountingLine, List<PurApAccountingLine> acctLines, int itemIdx) {
    	// itemIdx = -2 is for setdistribution
    	boolean isExist = false;
    	for (PurApAccountingLine acctline : acctLines) {
    		if (isEqualAcct(accountingLine, acctline)) {
    			String propertyName = "document.favoriteAccountLineIdentifier";
    			if (itemIdx >= 0) {
    				propertyName = "document.item["+itemIdx+"].favoriteAccountLineIdentifier" ;
    			}
				GlobalVariables.getMessageMap().putError(propertyName, CUKFSKeyConstants.ERROR_FAVORITE_ACCOUNT_EXIST);
    			isExist = true;
    		}
    	}
    	
    	return isExist;
    }
    
    private boolean isEqualAcct(FavoriteAccount accountingLine, PurApAccountingLine acctLine) {
        return new EqualsBuilder().append(acctLine.getChartOfAccountsCode(), accountingLine.getChartOfAccountsCode()).append(acctLine.getAccountNumber(), accountingLine.getAccountNumber()).append(acctLine.getSubAccountNumber(), accountingLine.getSubAccountNumber()).append(acctLine.getFinancialObjectCode(), accountingLine.getFinancialObjectCode()).append(acctLine.getFinancialSubObjectCode(), accountingLine.getFinancialSubObjectCode()).append(acctLine.getProjectCode(), accountingLine.getProjectCode()).append(acctLine.getOrganizationReferenceId(), accountingLine.getOrganizationReferenceId()).isEquals();
    }

	public void setBusinessObjectService(BusinessObjectService businessObjectService) {
		this.businessObjectService = businessObjectService;
	}

	public void setRoleManagementService(RoleManagementService roleManagementService) {
		this.roleManagementService = roleManagementService;
	}

}
