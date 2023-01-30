/*
 * Copyright 2010 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.coa.document.validation.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.COAKeyConstants;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryRateDetail;
import org.kuali.kfs.coa.document.validation.impl.AccountRule;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;

import edu.cornell.kfs.coa.businessobject.AccountExtendedAttribute;
import edu.cornell.kfs.coa.businessobject.AppropriationAccount;
import edu.cornell.kfs.coa.businessobject.MajorReportingCategory;
import edu.cornell.kfs.coa.businessobject.SubFundProgram;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

/**
 * This class...
 */
public class AccountExtensionRule extends AccountRule {

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        boolean success = super.processCustomRouteDocumentBusinessRules(document);

        success &= checkSubFundProgram(document);
        success &= checkAppropriationAccount(document);
        success &= checkMajorReportingCategoryCode(document);
        
        return success;
    }

    protected boolean checkSubFundProgram(MaintenanceDocument document) {
        boolean success = true;

        String subFundGroupCode = newAccount.getSubFundGroupCode();
        String subFundProg = ((AccountExtendedAttribute) newAccount.getExtension()).getProgramCode();
//        String subFundProgramCode = ((AccountExtendedAttribute)newAccount.getExtension()).getSubFundProgram().getProgramCode();
        BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);

        if (!StringUtils.isBlank(subFundProg)) {
            Map fieldValues = new HashMap();
            fieldValues.put("subFundGroupCode", subFundGroupCode);
            fieldValues.put("programCode", subFundProg);
            
            Collection<SubFundProgram> retVals = bos.findMatching(SubFundProgram.class, fieldValues);
            
            if (retVals.isEmpty()) {
                success = false;
                putFieldError("extension.programCode", CUKFSKeyConstants.ERROR_DOCUMENT_ACCMAINT_PROGRAM_CODE_NOT_GROUP_CODE, new String[] {subFundProg, subFundGroupCode});
            } else {
            	for (SubFundProgram sfp : retVals) {
            		if (!sfp.isActive()) {
                        putFieldError("extension.programCode", KFSKeyConstants.ERROR_INACTIVE, getFieldLabel(Account.class, "extension.programCode"));
                        success = false;
            		}
            	}
            }
            
        } else {
        	// BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
            Map fieldValues = new HashMap();
            fieldValues.put("subFundGroupCode", subFundGroupCode);
            Collection<SubFundProgram> retVals = bos.findMatching(SubFundProgram.class, fieldValues);
            if (!retVals.isEmpty()) {
                success = false;
                putFieldError("extension.programCode", CUKFSKeyConstants.ERROR_DOCUMENT_ACCMAINT_PROGRAM_CODE_CANNOT_BE_BLANK_FOR_GROUP_CODE, new String[] { subFundGroupCode});
            }
        }
        return success; 
    }
    
    protected boolean checkAppropriationAccount(MaintenanceDocument document) {
    	boolean success = true;

        String subFundGroupCode = newAccount.getSubFundGroupCode();
        String appropriationAccountNumber = ((AccountExtendedAttribute)newAccount.getExtension()).getAppropriationAccountNumber();
        BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);

        if (!StringUtils.isBlank(appropriationAccountNumber)) {
            Map fieldValues = new HashMap();
            fieldValues.put("subFundGroupCode", subFundGroupCode);
            fieldValues.put("appropriationAccountNumber", appropriationAccountNumber);
            
            Collection<AppropriationAccount> retVals = bos.findMatching(AppropriationAccount.class, fieldValues);
            
            if (retVals.isEmpty()) {
                success = false;
                putFieldError("extension.appropriationAccountNumber", 
                        CUKFSKeyConstants.ERROR_DOCUMENT_ACCMAINT_APPROP_ACCT_NOT_GROUP_CODE, 
                        new String[] {appropriationAccountNumber, subFundGroupCode});
            } else {
            	for (AppropriationAccount sfp : retVals) {
            		if (!sfp.isActive()) {
                        putFieldError("extension.appropriationAccountNumber", 
                                KFSKeyConstants.ERROR_INACTIVE, 
                                getFieldLabel(Account.class, "extension.appropriationAccountNumber"));
                        success = false;
            		}
            	}
            }
        }
    	return success;
    }
    
        
	protected boolean checkMajorReportingCategoryCode(MaintenanceDocument document) {
		boolean success = true;

		String majorReportingCategoryCode = ((AccountExtendedAttribute)newAccount.getExtension()).getMajorReportingCategoryCode();
		BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
		
		//MajorReportingCategory code is not a required field, if no value is entered no validation is performed
		if (!StringUtils.isBlank(majorReportingCategoryCode)) {
			Map fieldValues = new HashMap();
			fieldValues.put("majorReportingCategoryCode", majorReportingCategoryCode);
        
			Collection<MajorReportingCategory> retVals = bos.findMatching(MajorReportingCategory.class, fieldValues);
        
			if (retVals.isEmpty()) {			
				putFieldError("extension.majorReportingCategoryCode", CUKFSKeyConstants.ERROR_DOCUMENT_ACCMAINT_MJR_RPT_CAT_CODE_NOT_EXIST, new String[] {majorReportingCategoryCode});
				success = false;
			} else {
				for (MajorReportingCategory sfp : retVals) {
					if (!sfp.isActive()) {	
						putFieldError("extension.majorReportingCategoryCode", KFSKeyConstants.ERROR_INACTIVE, getFieldLabel(Account.class, "extension.majorReportingCategoryCode"));
                        success = false;
					}
				}
			}
		}
		return success;
	}

    /**
     * Overridden to reintroduce some of KualiCo's older ICR Detail validation
     * until KualiCo fixes the ICR validation bug in a new patch.
     * 
     * @see org.kuali.kfs.coa.document.validation.impl.AccountRule#checkCgRequiredFields(org.kuali.kfs.coa.businessobject.Account)
     */
    @Override
    protected boolean checkCgRequiredFields(Account newAccount) {
        boolean result = super.checkCgRequiredFields(newAccount);
        
        if (shouldCheckIndirectCostRecoveryRateDetails(newAccount)) {
            String fiscalYear = getUniversityDateService().getCurrentFiscalYear().toString();
            String icrSeriesId = newAccount.getFinancialIcrSeriesIdentifier();
            Collection<IndirectCostRecoveryRateDetail> icrRateDetails = findIndirectCostRecoveryRateDetails(fiscalYear, icrSeriesId);
            
            if (CollectionUtils.isEmpty(icrRateDetails)) {
                String label = getDataDictionaryService().getAttributeLabel(
                        Account.class, KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER);
                putFieldError(KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER, KFSKeyConstants.ERROR_EXISTENCE,
                        label + " (" + newAccount.getFinancialIcrSeriesIdentifier() + ")");
                result &= false;
            } else {
                for (IndirectCostRecoveryRateDetail icrRateDetail : icrRateDetails) {
                    if (ObjectUtils.isNull(icrRateDetail.getIndirectCostRecoveryRate())) {
                        putFieldError(KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER,
                                COAKeyConstants.ERROR_DOCUMENT_ICR_RATE_NOT_FOUND,
                                new String[] {fiscalYear, icrSeriesId});
                        result &= false;
                        break;
                    }
                }
            }
        }
        
        return result;
    }

    protected boolean shouldCheckIndirectCostRecoveryRateDetails(Account newAccount) {
        return ObjectUtils.isNotNull(newAccount.getSubFundGroup())
                && getSubFundGroupService().isForContractsAndGrants(newAccount.getSubFundGroup())
                && StringUtils.isNotBlank(newAccount.getFinancialIcrSeriesIdentifier());
    }

    protected Collection<IndirectCostRecoveryRateDetail> findIndirectCostRecoveryRateDetails(String fiscalYear, String icrSeriesId) {
        Map<String, String> criteria = new HashMap<>();
        criteria.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, fiscalYear.toString());
        criteria.put(KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER, icrSeriesId);
        return getBoService().findMatching(IndirectCostRecoveryRateDetail.class, criteria);
    }

    protected UniversityDateService getUniversityDateService() {
        return SpringContext.getBean(UniversityDateService.class);
    }

}
