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

import org.kuali.kfs.sys.document.authorization.FinancialSystemMaintenanceDocumentPresentationControllerBase;


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
// ^^^^^^^^^^^^^^^^^^  REMOVE THE CODE ABOVE BETWEEN THIS LINE AND THE ONE ABOVE WHEN UPGRADING TO KFS VERSION 4.0 OR HIGHER ^^^^^^^^^

}
