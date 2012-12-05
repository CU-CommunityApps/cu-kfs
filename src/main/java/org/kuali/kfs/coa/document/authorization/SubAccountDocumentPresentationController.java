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
import org.kuali.rice.kew.actionitem.ActionItem;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kim.bo.entity.KimPrincipal;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.kuali.rice.kns.bo.BusinessObject;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.document.authorization.DocumentPresentationControllerBase;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;


public class SubAccountDocumentPresentationController extends FinancialSystemMaintenanceDocumentPresentationControllerBase {

    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SubAccountDocumentPresentationController.class);


// vvvvvvvvvv  REMOVE THE CODE BELOW BETWEEN THIS LINE AND THE NEXT WHEN UPGRADING TO KFS VERSION 4.0 OR HIGHER vvvvvvvvvv
	/* ************ NOTE **********  THIS MUST BE REMOVED WHEN UPGRADING KFS TO A VERSION HIGHER THAN 3.0.1 !!!!! *********
	 * This method overrides the canBlanketApprove in the base class.  It is performed here since the base class 
	 * (DocumentPresentationControllerBase) is not eDoc specific and changes there would affect all eDocs when we 
	 * only wish to affect the account maintenance eDoc.  In Rice 1.0.3.3 and above, the entire button rendering 
	 * logic has been refactored meaning that this code modification is no longer needed above that version of Rice.
	 * Therefore this method should be removed once KFS is upgraded to a version higher than 3.0.1 (see KFSPTS-800, KFSPTS-1275
	 * for details on the issue requiring this fix).
	 */
	@Override
    protected boolean canBlanketApprove(Document document) {
	    {
	    	try {
		    	if ( getParameterService().getIndicatorParameter(KNSConstants.KNS_NAMESPACE, KNSConstants.DetailTypes.DOCUMENT_DETAIL_TYPE, KNSConstants.SystemGroupParameterNames.ALLOW_ENROUTE_BLANKET_APPROVE_WITHOUT_APPROVAL_REQUEST_IND) ) {
		    		return canEdit(document);
		    	}
	    	} catch ( IllegalArgumentException ex ) {
	    		// do nothing, the parameter does not exist and defaults to "N"
	    	}
	    	// otherwise, limit the display of the blanket approve button to only the initiator of the document (prior to routing)
	    	KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
	    	if ( canRoute(document) && StringUtils.equals( workflowDocument.getInitiatorPrincipalId(), GlobalVariables.getUserSession().getPrincipalId() ) ) {
	    		return true;
	    	}
	    	// or to a user with an approval action request
	    	if ( workflowDocument.isApprovalRequested() ) {
	    		return true;
	    	}
	    	
	    	return false;
	    }
	}
// ^^^^^^^^^^^^^^^^^^  REMOVE THE CODE ABOVE BETWEEN THIS LINE AND THE ONE ABOVE WHEN UPGRADING TO KFS VERSION 4.0 OR HIGHER ^^^^^^^^^

}
