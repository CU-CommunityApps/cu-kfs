/*
 * Copyright 2008 The Kuali Foundation
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
package org.kuali.kfs.fp.document.authorization;

import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.rice.kew.api.WorkflowDocument;


public class PreEncumbranceAccountingLineAuthorizer extends FinancialProcessingAccountingLineAuthorizer {
    
  @Override
    public boolean determineEditPermissionOnField(AccountingDocument accountingDocument, AccountingLine accountingLine, String accountingLineCollectionProperty, String fieldName, boolean editablePage) {
    	final FinancialSystemDocumentHeader documentHeader = (FinancialSystemDocumentHeader) accountingDocument.getDocumentHeader();
        final WorkflowDocument workflowDocument = documentHeader.getWorkflowDocument();
        if (workflowDocument.isInitiated() ) {
        	return true;
        }
        else {
    	
        	if (!editablePage) return false; // no edits by default on non editable pages

        	// if a document is cancelled or in error, all of its fields cannot be editable
        	if (workflowDocument.isCanceled() ) {
        		return false;
        	}
        }

        return true;
    	      
    }
}
