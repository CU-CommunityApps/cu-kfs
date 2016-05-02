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

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.rice.krad.service.KeyValuesService;

import edu.cornell.kfs.module.cg.businessobject.InvoiceFrequency;


/**
 * This class creates a new finder for our forms view (creates a drop-down of {@link InvoiceFrequency}s)
 */
public class InvoiceFrequencyValuesFinder extends KeyValuesBase {

	private static final long serialVersionUID = 1L;

	public List<KeyValue> getKeyValues() {
		  KeyValuesService boService = SpringContext.getBean(KeyValuesService.class);
	      Collection<InvoiceFrequency> invoiceFrequencyCodes = boService.findAll(InvoiceFrequency.class);
	      List<KeyValue> invoiceFrequencyKeyLabels = new ArrayList<KeyValue>();
	      for (Iterator<InvoiceFrequency> iter = invoiceFrequencyCodes.iterator(); iter.hasNext();) {
	          InvoiceFrequency element = (InvoiceFrequency) iter.next();
	          if (element.isActive()) { // only show active invoice types
	        	  invoiceFrequencyKeyLabels.add(new ConcreteKeyValue(element.getInvoiceFrequencyCode(), element.getInvoiceFrequencyCode()));
	          }
	      }

	      return invoiceFrequencyKeyLabels;
	}

}
