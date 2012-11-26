/*
 * Copyright 2005-2009 The Kuali Foundation
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
/*
 * Created on Mar 9, 2005
 *
 */
package org.kuali.kfs.module.purap.businessobject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.module.purap.document.ElectronicInvoiceRejectDocument;

public class ElectronicInvoiceLoad {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ElectronicInvoiceLoad.class);

    private Map<String, ElectronicInvoiceLoadSummary> invoiceLoadSummaries;
    private List<ElectronicInvoiceRejectDocument> rejectDocumentList;

    public ElectronicInvoiceLoad() {
        invoiceLoadSummaries = new HashMap<String, ElectronicInvoiceLoadSummary>();
        rejectDocumentList = new ArrayList<ElectronicInvoiceRejectDocument>();
    }

    public void insertInvoiceLoadSummary(ElectronicInvoiceLoadSummary eils) {
        invoiceLoadSummaries.put(eils.getVendorDunsNumber(), eils);
    }

    public void addInvoiceReject(ElectronicInvoiceRejectDocument eir) {
        rejectDocumentList.add(eir);
    }

    public List<ElectronicInvoiceRejectDocument> getRejectDocuments() {
        return rejectDocumentList;
    }

    public Map<String, ElectronicInvoiceLoadSummary> getInvoiceLoadSummaries() {
        return invoiceLoadSummaries;
    }

    public void setInvoiceLoadSummaries(Map<String, ElectronicInvoiceLoadSummary> invoiceLoadSummaries) {
        this.invoiceLoadSummaries = invoiceLoadSummaries;
    }

    public boolean containsRejects(){
        return !rejectDocumentList.isEmpty();
    }

}
