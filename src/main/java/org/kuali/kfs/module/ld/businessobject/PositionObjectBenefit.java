/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
package org.kuali.kfs.module.ld.businessobject;

import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.sys.businessobject.FiscalYearBasedBusinessObject;
import org.kuali.kfs.sys.businessobject.SystemOptions;

/**
 * Labor business object for Modeling of Position Object Benefit
 */
// CU customization: backport FINP-12883
public class PositionObjectBenefit extends PersistableBusinessObjectBase implements MutableInactivatable,
        FiscalYearBasedBusinessObject {

    protected Integer universityFiscalYear;
    protected String chartOfAccountsCode;
    protected String financialObjectCode;
    protected String financialObjectBenefitsTypeCode;
    protected Chart chartOfAccounts;
    protected boolean active;

    protected ObjectCode financialObject;
    protected transient SystemOptions universityFiscal;
    protected BenefitsType financialObjectBenefitsType;
    protected LaborObject laborObject;
    private String laborBenefitRateCategoryCode;
    private LaborBenefitRateCategory laborBenefitRateCategory;

    public PositionObjectBenefit() {

    }

    @Override
    public Integer getUniversityFiscalYear() {
        return universityFiscalYear;
    }

    @Override
    public void setUniversityFiscalYear(final Integer universityFiscalYear) {
        this.universityFiscalYear = universityFiscalYear;
    }

    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(final String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public String getFinancialObjectCode() {
        return financialObjectCode;
    }

    public void setFinancialObjectCode(final String financialObjectCode) {
        this.financialObjectCode = financialObjectCode;
    }

    public String getFinancialObjectBenefitsTypeCode() {
        return financialObjectBenefitsTypeCode;
    }

    public void setFinancialObjectBenefitsTypeCode(final String financialObjectBenefitsTypeCode) {
        this.financialObjectBenefitsTypeCode = financialObjectBenefitsTypeCode;
    }

    public ObjectCode getFinancialObject() {
        return financialObject;
    }

    @Deprecated
    public void setFinancialObject(final ObjectCode financialObject) {
        this.financialObject = financialObject;
    }

    public Chart getChartOfAccounts() {
        return chartOfAccounts;
    }

    @Deprecated
    public void setChartOfAccounts(final Chart chartOfAccounts) {
        this.chartOfAccounts = chartOfAccounts;
    }

    public SystemOptions getUniversityFiscal() {
        return universityFiscal;
    }

    @Deprecated
    public void setUniversityFiscal(final SystemOptions universityFiscal) {
        this.universityFiscal = universityFiscal;
    }

    public BenefitsType getFinancialObjectBenefitsType() {
        return financialObjectBenefitsType;
    }

    @Deprecated
    public void setFinancialObjectBenefitsType(final BenefitsType financialObjectBenefitsType) {
        this.financialObjectBenefitsType = financialObjectBenefitsType;
    }

    public LaborObject getLaborObject() {
        return laborObject;
    }

    @Deprecated
    public void setLaborObject(final LaborObject laborObject) {
        this.laborObject = laborObject;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(final boolean active) {
        this.active = active;
    }

    /**
     * This method (a hack by any other name...) returns a string so that an Labor Object Code Benefits can have a link
     * to view its own inquiry page after a look up
     *
     * @return the String "View Labor Object Code Benefits"
     */
    public String getLaborObjectCodeBenefitsViewer() {
        return "View Labor Object Code Benefits";
    }

    public String getLaborBenefitRateCategoryCode() {
        return laborBenefitRateCategoryCode;
    }

    public void setLaborBenefitRateCategoryCode(final String laborBenefitRateCategoryCode) {
        this.laborBenefitRateCategoryCode = laborBenefitRateCategoryCode;
    }

    public LaborBenefitRateCategory getLaborBenefitRateCategory() {
        return laborBenefitRateCategory;
    }

    public void setLaborBenefitRateCategory(final LaborBenefitRateCategory laborBenefitRateCategory) {
        this.laborBenefitRateCategory = laborBenefitRateCategory;
    }
}
