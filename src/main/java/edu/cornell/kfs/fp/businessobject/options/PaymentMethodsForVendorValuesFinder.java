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
package edu.cornell.kfs.fp.businessobject.options;

import java.util.HashMap;
import java.util.Map;

import org.kuali.kfs.sys.businessobject.options.PaymentMethodValuesFinderBase;

import edu.cornell.kfs.fp.CuFPConstants;

/**
 * This class returns list of payment method key value pairs.
 * 
 * Customization for UA: addition of "A" type for credit card payments.
 * 
 * @author jonathan
 * @see org.kuali.kfs.fp.businessobject.options.PaymentMethodValuesFinder
 */
public class PaymentMethodsForVendorValuesFinder extends PaymentMethodValuesFinderBase {
    private static final String DISPLAY_ON_VENDOR_DOCUMENT = "extension.displayOnVendorDocument";

    public PaymentMethodsForVendorValuesFinder() {
        super(DISPLAY_ON_VENDOR_DOCUMENT);
    }  
}
