/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
package org.kuali.kfs.coa.businessobject;

import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.KualiCodeBase;

/**
 * This class is the business object for the Balance Type object.
 */
/* Cornell Customization: backport redis*/
public class BalanceType extends KualiCodeBase implements MutableInactivatable {
	
    public static final String CACHE_NAME = "BalanceType";

    protected String financialBalanceTypeShortNm;
    protected boolean financialOffsetGenerationIndicator;
    protected boolean finBalanceTypeEncumIndicator;

    public BalanceType() {
        // always active, plus no column in the table
        super.setActive(true);
    }

    public BalanceType(String typeCode) {
        this();
        setCode(typeCode);
    }

    public String getFinancialBalanceTypeName() {
        return this.getName();
    }

    public void setFinancialBalanceTypeName(String financialBalanceTypeName) {
        this.setName(financialBalanceTypeName);
    }

    public String getFinancialBalanceTypeCode() {
        return this.getCode();
    }

    public void setFinancialBalanceTypeCode(String financialBalanceTypeCode) {
        this.setCode(financialBalanceTypeCode);
    }

    public boolean isFinBalanceTypeEncumIndicator() {
        return finBalanceTypeEncumIndicator;
    }

    public void setFinBalanceTypeEncumIndicator(boolean finBalanceTypeEncumIndicator) {
        this.finBalanceTypeEncumIndicator = finBalanceTypeEncumIndicator;
    }

    public String getFinancialBalanceTypeShortNm() {
        return financialBalanceTypeShortNm;
    }

    public void setFinancialBalanceTypeShortNm(String financialBalanceTypeShortNm) {
        this.financialBalanceTypeShortNm = financialBalanceTypeShortNm;
    }

    public boolean isFinancialOffsetGenerationIndicator() {
        return financialOffsetGenerationIndicator;
    }

    public void setFinancialOffsetGenerationIndicator(boolean financialOffsetGenerationIndicator) {
        this.financialOffsetGenerationIndicator = financialOffsetGenerationIndicator;
    }

}
