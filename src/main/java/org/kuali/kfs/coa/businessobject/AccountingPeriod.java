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

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.sys.businessobject.FiscalYearBasedBusinessObject;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;

import java.sql.Date;
import java.util.Calendar;

/* Cornell Customization: backport redis*/
public class AccountingPeriod extends PersistableBusinessObjectBase implements MutableInactivatable, FiscalYearBasedBusinessObject {
	
    public static final String CACHE_NAME = "AccountingPeriod";
	
    private Integer universityFiscalYear;
    private String universityFiscalPeriodCode;
    private String universityFiscalPeriodName;
    private boolean active;
    private boolean budgetRolloverIndicator;

    private Date universityFiscalPeriodEndDate;
    private Date auxiliaryVoucherDefaultReversalDate;
    private Date openDate;
    private Date closeDate;
    private SystemOptions options;

    public AccountingPeriod() {

    }

    public Integer getUniversityFiscalYear() {
        return universityFiscalYear;
    }

    public void setUniversityFiscalYear(Integer universityFiscalYear) {
        this.universityFiscalYear = universityFiscalYear;
    }

    public String getUniversityFiscalPeriodCode() {
        return universityFiscalPeriodCode;
    }

    public void setUniversityFiscalPeriodCode(String universityFiscalPeriodCode) {
        this.universityFiscalPeriodCode = universityFiscalPeriodCode;
    }

    public String getUniversityFiscalPeriodName() {
        return universityFiscalPeriodName;
    }

    public void setUniversityFiscalPeriodName(String universityFiscalPeriodName) {
        this.universityFiscalPeriodName = universityFiscalPeriodName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isBudgetRolloverIndicator() {
        return budgetRolloverIndicator;
    }

    public void setBudgetRolloverIndicator(boolean budgetRolloverIndicator) {
        this.budgetRolloverIndicator = budgetRolloverIndicator;
    }

    public Date getUniversityFiscalPeriodEndDate() {
        return universityFiscalPeriodEndDate;
    }

    public void setUniversityFiscalPeriodEndDate(Date universityFiscalPeriodEndDate) {
        this.universityFiscalPeriodEndDate = universityFiscalPeriodEndDate;
    }

    public Date getAuxiliaryVoucherDefaultReversalDate() {
        return auxiliaryVoucherDefaultReversalDate;
    }

    public void setAuxiliaryVoucherDefaultReversalDate(Date auxiliaryVoucherDefaultReversalDate) {
        this.auxiliaryVoucherDefaultReversalDate = auxiliaryVoucherDefaultReversalDate;
    }

    public Date getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Date openDate) {
        this.openDate = openDate;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    /**
     * Determine if the current account period is open
     *
     * @return true if the accounting period is open; otherwise, false
     */
    public boolean isOpen() {
        return this.isActive();
    }

    public SystemOptions getOptions() {
        return options;
    }

    public void setOptions(SystemOptions options) {
        this.options = options;
    }

    /**
     * This method returns the month that this period represents
     *
     * @return the actual month (1 - 12) that this period represents
     */
    public int getMonth() {
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(universityFiscalPeriodEndDate);
        return currentCalendar.get(Calendar.MONTH) + 1;
    }

    /**
     * generates a hash code for this accounting period, based on the primary keys of the AccountingPeriod
     * BusinessObject: university fiscal year and university fiscal period code
     */
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (universityFiscalPeriodCode == null ? 0 : universityFiscalPeriodCode.hashCode());
        result = PRIME * result + (universityFiscalYear == null ? 0 : universityFiscalYear.hashCode());
        return result;
    }

    /**
     * determines if two accounting periods are equal, based on the primary keys of the AccountingPeriod
     * BusinessObject: university fiscal year and university fiscal period code
     */
    @Override
    public boolean equals(Object obj) {
        // this method was added so that
        // org.kuali.kfs.fp.document.web.struts.AuxiliaryVoucherForm.populateAccountingPeriodListForRendering works properly
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AccountingPeriod other = (AccountingPeriod) obj;
        if (universityFiscalPeriodCode == null) {
            if (other.universityFiscalPeriodCode != null) {
                return false;
            }
        } else if (!universityFiscalPeriodCode.equals(other.universityFiscalPeriodCode)) {
            return false;
        }
        if (universityFiscalYear == null) {
            return other.universityFiscalYear == null;
        }
        return universityFiscalYear.equals(other.universityFiscalYear);
    }
}
