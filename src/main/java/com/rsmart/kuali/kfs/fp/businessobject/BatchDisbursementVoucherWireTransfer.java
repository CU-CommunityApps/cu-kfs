/*
 * Copyright 2009 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.fp.businessobject;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.businessobject.PaymentSourceWireTransfer;
//import org.kuali.kfs.fp.businessobject.DisbursementVoucherWireTransfer;
import org.kuali.kfs.core.web.format.BooleanFormatter;

/**
 * Provides String setter methods for population from XML (batch)
 */
public class BatchDisbursementVoucherWireTransfer extends PaymentSourceWireTransfer {

    /**
     * Takes a <code>String</code> and attempt to format as <code>Boolean</code> for setting the
     * disbursementVoucherWireTransferFeeWaiverIndicator field
     * 
     * @param disbursementVoucherWireTransferFeeWaiverIndicator as string
     */
    public void setDisbursementVoucherWireTransferFeeWaiverIndicator(String disbursementVoucherWireTransferFeeWaiverIndicator) {
        if (StringUtils.isNotBlank(disbursementVoucherWireTransferFeeWaiverIndicator)) {
            Boolean disbursementVoucherWireTransferFeeWaiver = (Boolean) (new BooleanFormatter()).convertFromPresentationFormat(disbursementVoucherWireTransferFeeWaiverIndicator);
            super.setWireTransferFeeWaiverIndicator(disbursementVoucherWireTransferFeeWaiver);
        }
    }

}
