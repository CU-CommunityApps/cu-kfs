/*
 * Copyright 2005 The Kuali Foundation
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
package org.kuali.kfs.fp.document.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.fp.document.DistributionOfIncomeAndExpenseDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.web.struts.KualiAccountingDocumentActionBase;
import org.kuali.rice.core.util.RiceConstants;
import org.kuali.rice.kns.question.ConfirmationQuestion;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase;

import edu.cornell.kfs.fp.document.service.CULegacyTravelService;


/**
 * This class handles Actions for the Distribution Income Expense objects.
 */
public class DistributionOfIncomeAndExpenseAction extends KualiAccountingDocumentActionBase {
	
    /**
     * Calls the document service to cancel the document
     *
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#cancel()
     */
    @Override
    public ActionForward cancel(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object question = request.getParameter(KNSConstants.QUESTION_INST_ATTRIBUTE_NAME);
        // this should probably be moved into a private instance variable
        // logic for cancel question
        if (question == null) {
            // ask question if not already asked
            return this.performQuestionWithoutInput(mapping, form, request, response, KNSConstants.DOCUMENT_CANCEL_QUESTION, getKualiConfigurationService().getPropertyString("document.question.cancel.text"), KNSConstants.CONFIRMATION_QUESTION, KNSConstants.MAPPING_CANCEL, "");
        }
        else {
            Object buttonClicked = request.getParameter(KNSConstants.QUESTION_CLICKED_BUTTON);
            if ((KNSConstants.DOCUMENT_CANCEL_QUESTION.equals(question)) && ConfirmationQuestion.NO.equals(buttonClicked)) {
                // if no button clicked just reload the doc
                return mapping.findForward(RiceConstants.MAPPING_BASIC);
            }
            // else go to cancel logic below
        }

        KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        
        // if travel DI, then reopen associated trip
        Boolean tripReOpened = true;
        boolean isTravelDI = false;
        try {
        	CULegacyTravelService cuLegacyTravelService = SpringContext.getBean(CULegacyTravelService.class);
			DistributionOfIncomeAndExpenseDocument diDoc = (DistributionOfIncomeAndExpenseDocument) kualiDocumentFormBase.getDocument();
			boolean isTravelDi = cuLegacyTravelService.isCULegacyTravelIntegrationInterfaceAssociatedWithTrip(diDoc);
			if (isTravelDi) {
				tripReOpened &= cuLegacyTravelService.reopenLegacyTrip(kualiDocumentFormBase.getDocId());
				LOG.info("Trip successfully reopened : "+tripReOpened);
			} else {
				LOG.info("DI is not a travel DI");
			}
        } catch (Exception ex) {
        	LOG.info("Exception occurred while trying to cancel a trip.");
      	  	ex.printStackTrace();
      	  	tripReOpened=false;
        }

        if(!isTravelDI || (isTravelDI && tripReOpened)) {
	        doProcessingAfterPost( kualiDocumentFormBase, request );
	        getDocumentService().cancelDocument(kualiDocumentFormBase.getDocument(), kualiDocumentFormBase.getAnnotation());
	
	        return returnToSender(request, mapping, kualiDocumentFormBase);
        } else {
        	// TODO add message to DI indicating why doc was not canceled.
            return mapping.findForward(RiceConstants.MAPPING_BASIC);
        }
    }

    /**
     * 
     */
    @Override
    public ActionForward disapprove(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object question = request.getParameter(KNSConstants.QUESTION_INST_ATTRIBUTE_NAME);
        String reason = request.getParameter(KNSConstants.QUESTION_REASON_ATTRIBUTE_NAME);

        if(ObjectUtils.isNotNull(question)) {
	    	KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
	        
	        // if travel DI, then reopen associated trip
	        Boolean tripReOpened = true;
	        boolean isTravelDI = false;
	        try {
	        	CULegacyTravelService cuLegacyTravelService = SpringContext.getBean(CULegacyTravelService.class);
	        	DistributionOfIncomeAndExpenseDocument diDoc = (DistributionOfIncomeAndExpenseDocument) kualiDocumentFormBase.getDocument();
				boolean isTravelDi = cuLegacyTravelService.isCULegacyTravelIntegrationInterfaceAssociatedWithTrip(diDoc);
				if (isTravelDi) {
					tripReOpened &= cuLegacyTravelService.reopenLegacyTrip(kualiDocumentFormBase.getDocId(), reason);
					LOG.info("Trip successfully reopened : "+tripReOpened);
				} else {
					LOG.info("DV is not a travel DV");
				}
	        } catch (Exception ex) {
	        	LOG.info("Exception occurred while trying to disapprove a disbursement voucher.");
	      	  	ex.printStackTrace();
	      	  	tripReOpened=false;
	        }
	
	        if(!isTravelDI || (isTravelDI && tripReOpened)) {
	        	return super.disapprove(mapping, form, request, response);
	        } else {
	        	// TODO add message to DI indicating why doc was not canceled.
	            return mapping.findForward(RiceConstants.MAPPING_BASIC);
	        }
        } else {
        	return super.disapprove(mapping, form, request, response);
        }
    }
    
	
}
