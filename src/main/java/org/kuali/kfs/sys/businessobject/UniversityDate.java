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
package org.kuali.kfs.sys.businessobject;

import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

import java.sql.Date;

/**
 * Represents a specific university date
 */
/* Cornell Customization: backport redis*/
public class UniversityDate extends PersistableBusinessObjectBase implements FiscalYearBasedBusinessObject {

    static final long serialVersionUID = 2587833750168955556L;

    public static final String CACHE_NAME = "UniversityDate";

    private Date universityDate;
    private Integer universityFiscalYear;
    private String universityFiscalAccountingPeriod;

    private AccountingPeriod accountingPeriod;
    private SystemOptions options;

    public Date getUniversityDate() {
        return universityDate;
    }

    public void setUniversityDate(Date universityDate) {
        this.universityDate = universityDate;
    }

    public String getUniversityFiscalAccountingPeriod() {
        return universityFiscalAccountingPeriod;
    }

    public void setUniversityFiscalAccountingPeriod(String universityFiscalAccountingPeriod) {
        this.universityFiscalAccountingPeriod = universityFiscalAccountingPeriod;
    }

    public Integer getUniversityFiscalYear() {
        return universityFiscalYear;
    }

    public void setUniversityFiscalYear(Integer universityFiscalYear) {
        this.universityFiscalYear = universityFiscalYear;
    }

    public AccountingPeriod getAccountingPeriod() {
        return accountingPeriod;
    }

    /**
     * @deprecated
     */
    public void setAccountingPeriod(AccountingPeriod accountingPeriod) {
        this.accountingPeriod = accountingPeriod;
    }

    public SystemOptions getOptions() {
        return options;
    }

    /**
     * @deprecated
     */
    public void setOptions(SystemOptions options) {
        this.options = options;
    }

}
