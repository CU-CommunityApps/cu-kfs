/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.fp.businessobject.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.businessobject.PaymentMethod;

/**
 * This class returns list of payment method key value pairs.
 * 
 * Customization for UA: addition of "A" type for credit card payments.
 * 
 * @author jonathan
 * @see org.kuali.kfs.fp.businessobject.options.PaymentMethodValuesFinder
 */
public class PaymentMethodValuesFinder extends KeyValuesBase {
    private static BusinessObjectService businessObjectService;
    static protected Map<String,String> filterCriteria = new HashMap<String, String>();
    static {
        filterCriteria.put(CuFPConstants.ACTIVE, CuFPConstants.YES);
    }
    
    /*
     * @see org.kuali.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List<KeyValue> getKeyValues() {
        Collection<PaymentMethod> paymentMethods = getBusinessObjectService().findMatchingOrderBy(PaymentMethod.class, getFilterCriteria(),  "paymentMethodName",true);
        List<KeyValue> labels = new ArrayList<KeyValue>( paymentMethods.size() );       
        for ( PaymentMethod pm : paymentMethods ) {
            labels.add(new ConcreteKeyValue(pm.getPaymentMethodCode(), pm.getPaymentMethodCode() + " - " + pm.getPaymentMethodName()));
        }
        return labels;
    }

    protected BusinessObjectService getBusinessObjectService() {
        if ( businessObjectService == null ) {
            businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        }
        return businessObjectService;
    }
    
    protected Map<String,String> getFilterCriteria() {
        return filterCriteria;
    }
    
}