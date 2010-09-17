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

import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;
import org.kuali.rice.kns.service.KeyValuesService;

import edu.cornell.kfs.module.cg.businessobject.InvoiceType;


/**
 * This class creates a new finder for our forms view (creates a drop-down of {@link InvoiceType}s)
 */
public class InvoiceTypeValuesFinder extends KeyValuesBase {

  public List getKeyValues() {
	  KeyValuesService boService = SpringContext.getBean(KeyValuesService.class);
      Collection invoiceTypeCodes = boService.findAll(InvoiceType.class);
      List invoiceTypeKeyLabels = new ArrayList();
      for (Iterator iter = invoiceTypeCodes.iterator(); iter.hasNext();) {
          InvoiceType element = (InvoiceType) iter.next();
          if (element.isActive()) { // only show active invoice types
        	  invoiceTypeKeyLabels.add(new KeyLabelPair(element.getInvoiceTypeCode(), element.getInvoiceTypeCode()));
          }
      }

      return invoiceTypeKeyLabels;
}

}
