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

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.fp.document.PreEncumbranceDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.AccountingLineOverride;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.web.struts.KualiAccountingDocumentActionBase;
import org.kuali.kfs.sys.web.struts.KualiAccountingDocumentFormBase;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kns.bo.PersistableBusinessObject;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.KNSServiceLocator;
import org.kuali.rice.kns.service.PersistenceService;
import org.kuali.rice.kns.util.GlobalVariables;


/**
 * This class piggy backs on all of the functionality in the FinancialSystemTransactionalDocumentActionBase.
 */
public class PreEncumbranceAction extends KualiAccountingDocumentActionBase {

	 @Override
	    public ActionForward route(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
	        KualiAccountingDocumentFormBase tmpForm = (KualiAccountingDocumentFormBase) form;
	        
	       
	       
	        
	        String doc_num = tmpForm.getDocId();
	       
	        PreEncumbranceDocument PreEncDoc = SpringContext.getBean(BusinessObjectService.class).findBySinglePrimaryKey(PreEncumbranceDocument.class, doc_num);
    		if (PreEncDoc == null){
    			GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath( "." + "Save", KFSKeyConstants.ERROR_CUSTOM, "This Document needs to be saved before Submit");

    		}
    		
	       
	        
	        ActionForward forward = super.route(mapping, form, request, response);

	        checkSalesTaxRequiredAllLines(tmpForm, tmpForm.getFinancialDocument().getSourceAccountingLines());
	        checkSalesTaxRequiredAllLines(tmpForm, tmpForm.getFinancialDocument().getTargetAccountingLines());

	        return forward;
	    }

	 
	 protected void processAccountingLines(AccountingDocument transDoc, KualiAccountingDocumentFormBase transForm, String lineSet) {
	        // figure out which set of lines we're looking at
	        List formLines;
	        String pathPrefix;
	        boolean source;
	        if (lineSet.equals(KFSConstants.SOURCE)) {
	            formLines = transDoc.getSourceAccountingLines();
	            pathPrefix = KFSConstants.DOCUMENT_PROPERTY_NAME + "." + KFSConstants.EXISTING_SOURCE_ACCT_LINE_PROPERTY_NAME;
	            source = true;
	        }
	        else {
	            formLines = transDoc.getTargetAccountingLines();
	            pathPrefix = KFSConstants.DOCUMENT_PROPERTY_NAME + "." + KFSConstants.EXISTING_TARGET_ACCT_LINE_PROPERTY_NAME;
	            source = false;
	        }

	        // find and process corresponding form and baselines
	        int index = 0;
	        for (Iterator i = formLines.iterator(); i.hasNext(); index++) {
	            AccountingLine formLine = (AccountingLine) i.next();
	            if (formLine.isSourceAccountingLine()) {
	            	formLine.setReferenceNumber(transDoc.getDocumentNumber());
	            }
	            // update sales tax required attribute for view
	            // handleSalesTaxRequired(transDoc, formLine, source, false, index);
	            checkSalesTax(transDoc, formLine, source, false, index);
	        }
	    }	 
	 
	 	
}
