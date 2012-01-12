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

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.document.validation.impl.AccountRule;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.service.BusinessObjectService;

import edu.cornell.kfs.coa.businessobject.AccountExtendedAttribute;
import edu.cornell.kfs.coa.businessobject.AppropriationAccount;
import edu.cornell.kfs.coa.businessobject.SubFundProgram;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import org.kuali.kfs.module.ld.businessobject.LaborBenefitRateCategory;

/**
 * This class...
 */
public class AccountExtensionRule extends AccountRule {

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        // TODO Auto-generated method stub
        boolean success = super.processCustomRouteDocumentBusinessRules(document);

        success &= checkSubFundProgram(document);
        success &= checkAppropriationAccount(document);
        success &= checkLaborBenefitCategoryCode(document);
        
        return success;
    }

    protected boolean checkLaborBenefitCategoryCode(MaintenanceDocument document) {
        boolean success = true;

        String laborBenefitCategoryCode  = ((AccountExtendedAttribute) newAccount.getExtension()).getLaborBenefitRateCategoryCode();
        BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);

        // Benefit Category Code is not a required field. if no value is entered 
        // no validation is performed.
        if (!StringUtils.isBlank(laborBenefitCategoryCode)) {
            Map fieldValues = new HashMap();
            fieldValues.put("laborBenefitRateCategoryCode", laborBenefitCategoryCode	);
            
            Collection<SubFundProgram> retVals = bos.findMatching(LaborBenefitRateCategory.class, fieldValues);
            
            if (retVals.isEmpty()) {
                success = false;
                putFieldError("extension.laborBenefitRateCategoryCode", KFSKeyConstants.ERROR_EXISTENCE, " Labor Benefit Rate Category Code "+laborBenefitCategoryCode);
            }
        	
        }
        return success;

        
        
    }
    
    
    protected boolean checkSubFundProgram(MaintenanceDocument document) {
        boolean success = true;

        String subFundGroupCode = newAccount.getSubFundGroupCode();
        String subFundProg = ((AccountExtendedAttribute) newAccount.getExtension()).getProgramCode();
//        String subFundProgramCode = ((AccountExtendedAttribute)newAccount.getExtension()).getSubFundProgram().getProgramCode();
        BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);

        if (!StringUtils.isBlank(subFundProg )) {
            Map fieldValues = new HashMap();
            fieldValues.put("subFundGroupCode", subFundGroupCode);
            fieldValues.put("programCode", subFundProg	);
            
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
                putFieldError("extension.appropriationAccountNumber", CUKFSKeyConstants.ERROR_DOCUMENT_ACCMAINT_APPROP_ACCT_NOT_GROUP_CODE, new String[] {appropriationAccountNumber, subFundGroupCode});
            } else {
            	for (AppropriationAccount sfp : retVals) {
            		if (!sfp.isActive()) {
                        putFieldError("extension.appropriationAccountNumber", KFSKeyConstants.ERROR_INACTIVE, getFieldLabel(Account.class, "extension.appropriationAccountNumber"));
                        success = false;
            		}
            	}
            }
        }
    	return success;
    }

}
