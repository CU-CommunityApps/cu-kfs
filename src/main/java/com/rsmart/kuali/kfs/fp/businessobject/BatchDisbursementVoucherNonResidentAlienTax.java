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

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonresidentTax;
import org.kuali.rice.core.web.format.BooleanFormatter;


/**
 * Provides String setter methods for population from XML (batch)
 */
public class BatchDisbursementVoucherNonResidentAlienTax extends DisbursementVoucherNonresidentTax {

    /**
     * Takes a <code>String</code> and attempt to format as <code>KualiDecimal</code> for setting the federalIncomeTaxPercent field
     * 
     * @param federalIncomeTaxPercent as string
     */
    public void setFederalIncomeTaxPercent(String federalIncomeTaxPercent) {
        super.setFederalIncomeTaxPercent(new BigDecimal(federalIncomeTaxPercent));
    }

    /**
     * Takes a <code>String</code> and attempt to format as <code>Boolean</code> for setting the foreignSourceIncomeCode field
     * 
     * @param foreignSourceIncomeCode as string
     */
    public void setForeignSourceIncomeCode(String foreignSourceIncomeCode) {
        if (StringUtils.isNotBlank(foreignSourceIncomeCode)) {
            Boolean foreignSourceIncome = (Boolean) (new BooleanFormatter()).convertFromPresentationFormat(foreignSourceIncomeCode);
            super.setForeignSourceIncomeCode(foreignSourceIncome);
        }
    }

    /**
     * Takes a <code>String</code> and attempt to format as <code>Boolean</code> for setting the incomeTaxGrossUpCode field
     * 
     * @param incomeTaxGrossUpCode as string
     */
    public void setIncomeTaxGrossUpCode(String incomeTaxGrossUpCode) {
        if (StringUtils.isNotBlank(incomeTaxGrossUpCode)) {
            Boolean incomeTaxGrossUp = (Boolean) (new BooleanFormatter()).convertFromPresentationFormat(incomeTaxGrossUpCode);
            super.setIncomeTaxGrossUpCode(incomeTaxGrossUp);
        }
    }

    /**
     * Takes a <code>String</code> and attempt to format as <code>Boolean</code> for setting the incomeTaxTreatyExemptCode field
     * 
     * @param incomeTaxTreatyExemptCode as string
     */
    public void setIncomeTaxTreatyExemptCode(String incomeTaxTreatyExemptCode) {
        if (StringUtils.isNotBlank(incomeTaxTreatyExemptCode)) {
            Boolean incomeTaxTreatyExempt = (Boolean) (new BooleanFormatter()).convertFromPresentationFormat(incomeTaxTreatyExemptCode);
            super.setIncomeTaxTreatyExemptCode(incomeTaxTreatyExempt);
        }
    }

    /**
     * Takes a <code>String</code> and attempt to format as <code>KualiDecimal</code> for setting the stateIncomeTaxPercent field
     * 
     * @param stateIncomeTaxPercent as string
     */
    public void setStateIncomeTaxPercent(String stateIncomeTaxPercent) {
        super.setStateIncomeTaxPercent(new BigDecimal(stateIncomeTaxPercent));
    }

}
