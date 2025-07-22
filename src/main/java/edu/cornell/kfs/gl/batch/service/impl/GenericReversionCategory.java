/*
 * Copyright 2006 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.gl.batch.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.COAConstants;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.core.api.parameter.ParameterEvaluatorService;

import edu.cornell.kfs.coa.businessobject.ReversionCategory;
import edu.cornell.kfs.gl.batch.AccountReversionCurrentYearAccountStep;
import edu.cornell.kfs.gl.batch.service.ReversionCategoryLogic;
import edu.cornell.kfs.sys.CUKFSConstants;

/**
 * A generic implementation of ReversionCategoryLogic; it is completely based off of parameters
 * @see org.kuali.kfs.gl.batch.service.ReversionCategoryLogic
 */
public class GenericReversionCategory implements ReversionCategoryLogic {
	private static final Logger LOG = LogManager.getLogger(GenericReversionCategory.class);

    private String categoryCode;
    private String categoryName;
    private boolean isExpense;

    private ParameterEvaluatorService parameterEvaluatorService;

    /**
     * Constructs a GenericOrganizationReversionCategory
     */
    public GenericReversionCategory() {
    }

    /**
     * Sets the category code for this logic, so that the parameters for this category can be looked
     * up in the database 
     *
     * @param code the code for this logic
     */
    public void setCategoryCode(String code) {
        categoryCode = code;
        isExpense = parameterEvaluatorService.getParameterEvaluator(AccountReversionCurrentYearAccountStep.class, CUKFSConstants.Reversion.IS_EXPENSE_PARAM, categoryCode).evaluationSucceeds();
    }

    /**
     * Sets the name of this category
     * 
     * @param name the name to set
     */
    public void setCategoryName(String name) {
        categoryName = name;
    }

    /**
     * Determines if balances with a given object code should be processed by this logic or not
     * 
     * @param oc the object code to qualify
     * @return true if balances with the given object code should be processed by this logic, false if otherwise
     * @see org.kuali.kfs.gl.batch.service.OrganizationReversionCategoryLogic#containsObjectCode(org.kuali.kfs.coa.businessobject.ObjectCode)
     */
    public boolean containsObjectCode(ObjectCode oc) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("containsObjectCode() started");
        }

        String cons = oc.getFinancialObjectLevel().getFinancialConsolidationObjectCode();
        String level = oc.getFinancialObjectLevelCode();
        String objType = oc.getFinancialObjectTypeCode();
        String objSubType = oc.getFinancialObjectSubType().getCode();

        boolean consolidationRulesPassed = parameterEvaluatorService.getParameterEvaluator(ReversionCategory.class, CUKFSConstants.Reversion.VALID_PREFIX + CUKFSConstants.Reversion.OBJECT_CONSOL_PARAM_SUFFIX, CUKFSConstants.Reversion.INVALID_PREFIX + CUKFSConstants.Reversion.OBJECT_CONSOL_PARAM_SUFFIX, categoryCode, cons).evaluationSucceeds();
        boolean levelRulesPassed = parameterEvaluatorService.getParameterEvaluator(ReversionCategory.class, CUKFSConstants.Reversion.VALID_PREFIX + CUKFSConstants.Reversion.OBJECT_LEVEL_PARAM_SUFFIX, CUKFSConstants.Reversion.INVALID_PREFIX + CUKFSConstants.Reversion.OBJECT_LEVEL_PARAM_SUFFIX, categoryCode, level).evaluationSucceeds();
        boolean objectTypeRulesPassed = parameterEvaluatorService.getParameterEvaluator(ReversionCategory.class, CUKFSConstants.Reversion.VALID_PREFIX + CUKFSConstants.Reversion.OBJECT_TYPE_PARAM_SUFFIX, CUKFSConstants.Reversion.INVALID_PREFIX + CUKFSConstants.Reversion.OBJECT_TYPE_PARAM_SUFFIX, categoryCode, objType).evaluationSucceeds();
        boolean objectSubTypeRulesPassed = parameterEvaluatorService.getParameterEvaluator(ReversionCategory.class, CUKFSConstants.Reversion.VALID_PREFIX + CUKFSConstants.Reversion.OBJECT_SUB_TYPE_PARAM_SUFFIX, CUKFSConstants.Reversion.INVALID_PREFIX + CUKFSConstants.Reversion.OBJECT_SUB_TYPE_PARAM_SUFFIX, categoryCode, objSubType).evaluationSucceeds();

        return consolidationRulesPassed && levelRulesPassed && objectTypeRulesPassed && objectSubTypeRulesPassed;
    }

    /**
     * Returns the name of the category
     * 
     * @return the name of the category
     * @see org.kuali.kfs.gl.batch.service.OrganizationReversionCategoryLogic#getName()
     */
    public String getName() {
        return categoryName;
    }

    /**
     * Returns the code of this category
     * 
     * @return the code of this category
     * @see org.kuali.kfs.gl.batch.service.OrganizationReversionCategoryLogic#getCode()
     */
    public String getCode() {
        return categoryCode;
    }

    /**
     * Returns whether this category represents an expense or not
     * 
     * @return true if this category represents expenses, false if otherwise
     * @see org.kuali.kfs.gl.batch.service.OrganizationReversionCategoryLogic#isExpense()
     */
    public boolean isExpense() {
        return isExpense;
    }

	/**
	 * Gets the parameterEvaluatorService.
	 * 
	 * @return parameterEvaluatorService
	 */
	public ParameterEvaluatorService getParameterEvaluatorService() {
		return parameterEvaluatorService;
	}

	/**
	 * Sets the parameterEvaluatorService.
	 * 
	 * @param parameterEvaluatorService
	 */
	public void setParameterEvaluatorService(
			ParameterEvaluatorService parameterEvaluatorService) {
		this.parameterEvaluatorService = parameterEvaluatorService;
	}
}
