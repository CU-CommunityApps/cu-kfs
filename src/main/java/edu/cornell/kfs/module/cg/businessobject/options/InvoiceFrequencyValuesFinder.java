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

import edu.cornell.kfs.module.cg.businessobject.InvoiceFrequency;

public class InvoiceFrequencyValuesFinder extends KeyValuesBase {
	private static final long serialVersionUID = 1L;
	protected KeyValuesService keyValuesService;

	public List<KeyValue> getKeyValues() {
	      Collection<InvoiceFrequency> invoiceFrequencyCodes = keyValuesService.findAll(InvoiceFrequency.class);
	      List<KeyValue> invoiceFrequencyKeyLabels = new ArrayList<KeyValue>();
	      for (Iterator<InvoiceFrequency> iter = invoiceFrequencyCodes.iterator(); iter.hasNext();) {
	          InvoiceFrequency element = (InvoiceFrequency) iter.next();
	          if (element.isActive()) {
	        	  invoiceFrequencyKeyLabels.add(new ConcreteKeyValue(element.getInvoiceFrequencyCode(), element.getInvoiceFrequencyCode()));
	          }
	      }

	      return invoiceFrequencyKeyLabels;
	}

    public void setKeyValuesService(KeyValuesService keyValuesService) {
        this.keyValuesService = keyValuesService;
    }

}
