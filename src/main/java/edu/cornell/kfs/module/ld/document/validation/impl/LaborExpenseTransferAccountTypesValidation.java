package edu.cornell.kfs.module.ld.document.validation.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.kfs.module.ld.LaborConstants;
import org.kuali.kfs.module.ld.document.LaborExpenseTransferDocumentBase;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.coreservice.framework.CoreFrameworkServiceLocator;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

import edu.cornell.kfs.module.ld.CuLaborConstants;
import edu.cornell.kfs.module.ld.CuLaborKeyConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants.LdParameterConstants;

/**
 * 
 * This class is to validate if expense transfer is valid between account types.
 *
 */
public class LaborExpenseTransferAccountTypesValidation extends GenericValidation {
    private Document documentForValidation;
    private Set<String> invalidTransferTargetAccountTypesInParam;  
    private Map<String, Set<String>> invalidTransferAccountTypesMap;
    
    private ParameterService parameterService;
    /**
     * Validates before the document routes 
     * @see org.kuali.kfs.validation.Validation#validate(java.lang.Object[])
     */
	@SuppressWarnings("unchecked")
	public boolean validate(AttributedDocumentEvent event) {
        boolean isValid = true;
           
        if (getParameterService().getParameterValueAsBoolean(LaborConstants.LABOR_MODULE_CODE, KfsParameterConstants.DOCUMENT_COMPONENT, LdParameterConstants.VALIDATE_TRANSFER_ACCOUNT_TYPES_IND)
        		&& !hasExceptionPermission()) {
			Document documentForValidation = getDocumentForValidation();

			LaborExpenseTransferDocumentBase expenseTransferDocument = (LaborExpenseTransferDocumentBase) documentForValidation;

			List<AccountingLine> sourceLines = (List<AccountingLine>)expenseTransferDocument.getSourceAccountingLines();
			List<AccountingLine> targetLines = (List<AccountingLine>)expenseTransferDocument.getTargetAccountingLines();
			setInvalidTransferAccountTypesMap();

			if (CollectionUtils.isNotEmpty(sourceLines) && CollectionUtils.isNotEmpty(targetLines) && invalidTransferAccountTypesMap.size() > 0) {
		    	if (isInvalidTransferBetweenAccountTypes(sourceLines, targetLines)) {
				    GlobalVariables.getMessageMap().putError(KFSPropertyConstants.SOURCE_ACCOUNTING_LINES, CuLaborKeyConstants.INVALID_ACCOUNTTRANSFER_ERROR);
				    isValid = false;
			    }
			}
        }

        return isValid;       
    }

	/*
	 * get the invalid transfer target account type set for each source account invalid transfer account type
	 * also set the complete invalid transfer target account type set.
	 */
	private void setInvalidTransferAccountTypesMap() {
		invalidTransferAccountTypesMap = new HashMap<String, Set<String>>();
		invalidTransferTargetAccountTypesInParam = new HashSet<String>();
		
		for (String sourceTargetAccounttypes : getParameterService().getParameterValuesAsString(LaborConstants.LABOR_MODULE_CODE,
				KfsParameterConstants.DOCUMENT_COMPONENT,LdParameterConstants.INVALID_TO_ACCOUNT_BY_FROM_ACCOUNT)) {
			String[] acctTypePair = sourceTargetAccounttypes.split("=");
			if (!invalidTransferAccountTypesMap.containsKey(acctTypePair[0])) {
				invalidTransferAccountTypesMap.put(acctTypePair[0], new HashSet<String>());
			}
			invalidTransferAccountTypesMap.get(acctTypePair[0]).add(acctTypePair[1]);
			invalidTransferTargetAccountTypesInParam.add(acctTypePair[1]);
		}
	}
	
    /**
     * This method checks if the account transfer is invalid between account types.
     * 
     * @param sourceLines
     * @param targetLines
     * @return
     */
	private boolean isInvalidTransferBetweenAccountTypes(List<AccountingLine> sourceLines, List<AccountingLine> targetLines) {

        Set<String> invalidTransferSourceAccountTypes = getInvalidTransferSourceAccountTypes(sourceLines);

        Set<String> invalidTransferTargetAccountTypes = new HashSet<String>();
        if (CollectionUtils.isNotEmpty(invalidTransferSourceAccountTypes)) {
        	invalidTransferTargetAccountTypes = getInvalidTransferTargetAccountTypes(targetLines);
        }

        return !sourceHasNoInvalidTransferAccountTypes(invalidTransferSourceAccountTypes) && !targetHasNoInvalidTransferAccountTypes(invalidTransferTargetAccountTypes)
        		&& isInvalidTransferTargetAccountTypeMatched(invalidTransferSourceAccountTypes, invalidTransferTargetAccountTypes);
    }
      
	/*
	 * get the invalid transfer account types that are in source account lines
	 */
	private Set<String> getInvalidTransferSourceAccountTypes(List<AccountingLine> accountLines) {
        Set <String> invalidTransferAccountTypes = new HashSet<String>();
       for (AccountingLine line : accountLines) {
            if (invalidTransferAccountTypesMap.containsKey(line.getAccount().getAccountTypeCode())) {
                invalidTransferAccountTypes.add(line.getAccount().getAccountTypeCode());
            }
        }
       return invalidTransferAccountTypes;
		
	}
	
	/*
	 * get the invalid transfer account types from the target account lines
	 */
	private Set<String> getInvalidTransferTargetAccountTypes(
			List<AccountingLine> accountLines) {
		Set<String> invalidTransferAccountTypes = new HashSet<String>();
		for (AccountingLine line : accountLines) {
			if (invalidTransferTargetAccountTypesInParam.contains(line.getAccount().getAccountTypeCode())) {
				invalidTransferAccountTypes.add(line.getAccount().getAccountTypeCode());
			}
		}
		return invalidTransferAccountTypes;

	}

	
	/*
	 * if source has no invalid transfer account types
	 */
	private boolean sourceHasNoInvalidTransferAccountTypes(Set<String> invalidTransferSourceAccountTypes) {
		return CollectionUtils.isEmpty(invalidTransferSourceAccountTypes);
	}
	
	/*
	 * if target has no invalid transfer account types
	 */
	private boolean targetHasNoInvalidTransferAccountTypes(Set<String> invalidTransferTargetAccountTypes) {
		return CollectionUtils.isEmpty(invalidTransferTargetAccountTypes);
	}
	
	/*
	 * check if target invalid transfer account type is included in the invalid source transfer account type mapped set
	 */
	private boolean isInvalidTransferTargetAccountTypeMatched(Set<String> invalidTransferSourceAccountTypes, Set<String> invalidTransferTargetAccountTypes) {

		boolean isInvalidTransferTargetAcctMatched = false;
		for (String souerceAccountType : invalidTransferSourceAccountTypes) {
			for (String taregetccountType : invalidTransferTargetAccountTypes) {
			    if (invalidTransferAccountTypesMap.get(souerceAccountType).contains(taregetccountType)) {
			    	isInvalidTransferTargetAcctMatched = true;
			    	break;
			    }
			}
		}
		return isInvalidTransferTargetAcctMatched;
	}
		
	/*
	 * Check if user has "LD account type transfer exception" permission
	 * if user has this permission, then user can bypass this rule validation.
	 * 
	 */
	private boolean hasExceptionPermission() {
        return KimApiServiceLocator.getPermissionService().hasPermission(
    			GlobalVariables.getUserSession().getPrincipalId(), LaborConstants.LABOR_MODULE_CODE, CuLaborConstants.LD_ACCOUNT_TYPE_TRANSFER_EXCEPTION) ;
    }

    /**
     * Gets the documentForValidation attribute. 
     * @return Returns the documentForValidation.
     */
    public Document getDocumentForValidation() {
        return documentForValidation;
    }

    /**
     * Sets the accountingDocumentForValidation attribute value.
     * @param documentForValidation The documentForValidation to set.
     */
    public void setDocumentForValidation(Document documentForValidation) {
        this.documentForValidation = documentForValidation;
    }

	public ParameterService getParameterService() {
		
		if (parameterService == null) {
			parameterService = CoreFrameworkServiceLocator.getParameterService();
		}
		
		return parameterService;
	}    

}
