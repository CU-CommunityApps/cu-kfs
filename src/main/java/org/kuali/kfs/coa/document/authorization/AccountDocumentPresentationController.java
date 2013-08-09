/*
 * Copyright 2009 The Kuali Foundation
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
package org.kuali.kfs.coa.document.authorization;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.document.validation.impl.AccountRule;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.businessobject.OrganizationOptions;
import org.kuali.kfs.module.ld.LaborKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.authorization.FinancialSystemMaintenanceDocumentPresentationControllerBase;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.actionitem.ActionItem;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.document.authorization.DocumentPresentationControllerBase;
import org.kuali.rice.kns.util.KNSConstants;



public class AccountDocumentPresentationController extends FinancialSystemMaintenanceDocumentPresentationControllerBase {

    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AccountDocumentPresentationController.class);

    @Override
    public Set<String> getConditionallyReadOnlyPropertyNames(MaintenanceDocument document) {
        Set<String> readOnlyPropertyNames = super.getConditionallyReadOnlyPropertyNames(document);
        setLaborBenefitRateCategoryCodeEditable(readOnlyPropertyNames);
        return readOnlyPropertyNames;
    }


// vvvvvvvvvv  REMOVE THE CODE BELOW BETWEEN THIS LINE AND THE NEXT WHEN UPGRADING TO KFS VERSION 4.0 OR HIGHER vvvvvvvvvv
	/* ************ NOTE **********  THIS MUST BE REMOVED WHEN UPGRADING KFS TO A VERSION HIGHER THAN 3.0.1 !!!!! *********
	 * This method overrides the canBlanketApprove in the base class.  It is performed here since the base class 
	 * (DocumentPresentationControllerBase) is not eDoc specific and changes there would affect all eDocs when we 
	 * only wish to affect the account maintenance eDoc.  In Rice 1.0.3.3 and above, the entire button rendering 
	 * logic has been refactored meaning that this code modification is no longer needed above that version of Rice.
	 * Therefore this method should be removed once KFS is upgraded to a version higher than 3.0.1 (see KFSPTS-800
	 * for details on the issue requiring this fix).
	 */
    //TODO UPGRADE-911
//	@Override
//    protected boolean canBlanketApprove(Document document) {
//	    {
//	    	try {
//		    	if ( getParameterService().getIndicatorParameter(KNSConstants.KNS_NAMESPACE, KNSConstants.DetailTypes.DOCUMENT_DETAIL_TYPE, KNSConstants.SystemGroupParameterNames.ALLOW_ENROUTE_BLANKET_APPROVE_WITHOUT_APPROVAL_REQUEST_IND) ) {
//		    		return canEdit(document);
//		    	}
//	    	} catch ( IllegalArgumentException ex ) {
//	    		// do nothing, the parameter does not exist and defaults to "N"
//	    	}
//	    	// otherwise, limit the display of the blanket approve button to only the initiator of the document (prior to routing)
//	    	KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
//	    	if ( canRoute(document) && StringUtils.equals( workflowDocument.getInitiatorPrincipalId(), GlobalVariables.getUserSession().getPrincipalId() ) ) {
//	    		return true;
//	    	}
//	    	// or to a user with an approval action request
//	    	if ( workflowDocument.isApprovalRequested() ) {
//	    		return true;
//	    	}
//	    	
//	    	return false;
//	    }
//	}
// ^^^^^^^^^^^^^^^^^^  REMOVE THE CODE ABOVE BETWEEN THIS LINE AND THE NEXT WHEN UPGRADING TO KFS VERSION 4.0 OR HIGHER ^^^^^^^^^

//    /**
//     * @see org.kuali.rice.kns.document.authorization.MaintenanceDocumentPresentationControllerBase#getConditionallyHiddenPropertyNames(org.kuali.rice.kns.bo.BusinessObject)
//     */
//    @Override
//    public Set<String> getConditionallyHiddenPropertyNames(BusinessObject businessObject) {
//        Set<String> hiddenPropertyNames = super.getConditionallyHiddenPropertyNames(businessObject);
//        setLaborBenefitRateCategoryCodeHidden(hiddenPropertyNames);
//        return hiddenPropertyNames;
//    }

	
    /**
     * 
     * Sets the Labor Benefit Rate Category Code, otherwise leave
     * it read/write.
     * 
     * @param readOnlyPropetyNames
     */
    protected void setLaborBenefitRateCategoryCodeEditable(Set<String> readOnlyPropertyNames){
        ParameterService service = SpringContext.getBean(ParameterService.class);
        
        //make sure the parameter exists
        if(service.parameterExists(KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class, "ENABLE_FRINGE_BENEFIT_CALC_BY_BENEFIT_RATE_CATEGORY")){
          //check the system param to see if the labor benefit rate category should be editable
            String sysParam = SpringContext.getBean(ParameterService.class).getParameterValueAsString(KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class, "ENABLE_FRINGE_BENEFIT_CALC_BY_BENEFIT_RATE_CATEGORY");
            LOG.debug("sysParam: " + sysParam);
            //if sysParam != Y then Labor Benefit Rate Category Code is not editable
            if (!sysParam.equalsIgnoreCase("Y")) {
                readOnlyPropertyNames.add("laborBenefitRateCategoryCode");
                
            }
        }else{
            LOG.debug("System paramter doesn't exist.  Making the Labor Benefit Rate Category Code not editable.");
            readOnlyPropertyNames.add("laborBenefitRateCategoryCode");
        }
    }
    
    /**
     * 
     * Hides the Labor Benefit Rate Category Code depending on the system parameter ENABLE_FRINGE_BENEFIT_CALC_BY_BENEFIT_RATE_CATEGORY
     * 
     * @param hiddenPropetyNames
     */
    protected void setLaborBenefitRateCategoryCodeHidden(Set<String> hiddenPropertyNames){
        ParameterService service = SpringContext.getBean(ParameterService.class);
        
        //make sure the parameter exists
        if(service.parameterExists(KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class, "ENABLE_FRINGE_BENEFIT_CALC_BY_BENEFIT_RATE_CATEGORY")){
          //check the system param to see if the labor benefit rate category should be hidden
            String sysParam = SpringContext.getBean(ParameterService.class).getParameterValueAsString(KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class, "ENABLE_FRINGE_BENEFIT_CALC_BY_BENEFIT_RATE_CATEGORY");
            LOG.debug("sysParam: " + sysParam);
            //if sysParam != Y then Labor Benefit Rate Category Code is hidden
            if (!sysParam.equalsIgnoreCase("Y")) {
                hiddenPropertyNames.add("laborBenefitRateCategoryCode");
                
            }
        }else{
            LOG.debug("System paramter doesn't exist.  Making the Labor Benefit Rate Category Code not editable.");
            hiddenPropertyNames.add("laborBenefitRateCategoryCode");
        }
    }
}
