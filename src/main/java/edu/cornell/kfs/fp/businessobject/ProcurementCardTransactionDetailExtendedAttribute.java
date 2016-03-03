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

package edu.cornell.kfs.fp.businessobject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.krad.bo.PersistableBusinessObjectExtensionBase;

/**
 * This class is used to represent a procurement card transaction detail business object.
 */
public class ProcurementCardTransactionDetailExtendedAttribute extends PersistableBusinessObjectExtensionBase {

    private static final long serialVersionUID = 1L;
    private String documentNumber;
    private Integer financialDocumentTransactionLineNumber;
    
    // list of pcard data
    private List<PurchasingDataDetail> purchasingDataDetails;
    
    public ProcurementCardTransactionDetailExtendedAttribute() {
        super();
        purchasingDataDetails = new ArrayList<PurchasingDataDetail>();
    }
    
    public String getDocumentNumber() {
        return documentNumber;
    }
    
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }
    
    public Integer getFinancialDocumentTransactionLineNumber() {
        return financialDocumentTransactionLineNumber;
    }
    
    public void setFinancialDocumentTransactionLineNumber(Integer financialDocumentTransactionLineNumber) {
        this.financialDocumentTransactionLineNumber = financialDocumentTransactionLineNumber;
    }

    public void setFinancialDocumentTransactionLineNumber(String financialDocumentTransactionLineNumber) {
        this.financialDocumentTransactionLineNumber = Integer.parseInt(financialDocumentTransactionLineNumber);
    }
    
    public List<PurchasingDataDetail> getPurchasingDataDetails() {
        return purchasingDataDetails;
    }
    
    public void setPurchasingDataDetails(List<PurchasingDataDetail> purchasingDataDetails) {
        this.purchasingDataDetails = purchasingDataDetails;
    }

    /**
     * @see org.kuali.rice.krad.bo.BusinessObjectBase#toStringMapper()
     */
    @SuppressWarnings("rawtypes")
    protected LinkedHashMap toStringMapper_RICE20_REFACTORME() {
        LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();
        m.put(KFSPropertyConstants.DOCUMENT_NUMBER, this.documentNumber);
        if (this.financialDocumentTransactionLineNumber != null) {
            m.put("financialDocumentTransactionLineNumber", this.financialDocumentTransactionLineNumber.toString());
        }
        return m;
    }
    
}
