/*
 * Copyright 2007 The Kuali Foundation
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
package edu.cornell.kfs.module.cg.businessobject.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.krad.service.KeyValuesService;

import edu.cornell.kfs.module.cg.businessobject.InvoiceType;

/**
 * This class creates a new finder for our forms view (creates a drop-down of {@link InvoiceType}s)
 */
public class InvoiceTypeValuesFinder extends KeyValuesBase {
    private static final long serialVersionUID = 1L;
    protected KeyValuesService keyValuesService;

    public List<KeyValue> getKeyValues() {
        Collection<InvoiceType> invoiceTypeCodes = keyValuesService.findAll(InvoiceType.class);
        List<KeyValue> invoiceTypeKeyLabels = new ArrayList<KeyValue>();
        for (Iterator<InvoiceType> iter = invoiceTypeCodes.iterator(); iter.hasNext();) {
            InvoiceType element = (InvoiceType) iter.next();
            if (element.isActive()) {
                invoiceTypeKeyLabels.add(new ConcreteKeyValue(element.getInvoiceTypeCode(), element.getInvoiceTypeCode()));
            }
        }

        return invoiceTypeKeyLabels;
    }

    public void setKeyValuesService(KeyValuesService keyValuesService) {
        this.keyValuesService = keyValuesService;
    }

}
