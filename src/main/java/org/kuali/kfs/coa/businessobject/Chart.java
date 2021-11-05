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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.service.ChartService;
import org.kuali.kfs.krad.bo.KualiCode;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.sys.businessobject.serialization.PersistableBusinessObjectSerializer;
import org.kuali.kfs.sys.context.SpringContext;

/* Cornell Customization: backport redis*/
public class Chart extends PersistableBusinessObjectBase implements KualiCode {

    private static final long serialVersionUID = 4129020803214027609L;
    
    public static final String CACHE_NAME = "Chart";

    protected String finChartOfAccountDescription;
    protected boolean active;
    protected String reportsToChartOfAccountsCode;
    protected String chartOfAccountsCode;
    protected String finAccountsPayableObjectCode;
    protected String finExternalEncumbranceObjCd;
    protected String finPreEncumbranceObjectCode;
    protected String financialCashObjectCode;
    protected String icrIncomeFinancialObjectCode;
    protected String finAccountsReceivableObjCode;
    protected String finInternalEncumbranceObjCd;
    protected String icrExpenseFinancialObjectCd;
    protected String incBdgtEliminationsFinObjCd;
    protected String expBdgtEliminationsFinObjCd;
    protected String fundBalanceObjectCode;

    protected ObjectCode incBdgtEliminationsFinObj;
    protected ObjectCode expBdgtEliminationsFinObj;
    protected ObjectCode finAccountsPayableObject;
    protected ObjectCode finExternalEncumbranceObj;
    protected ObjectCode finPreEncumbranceObject;
    protected ObjectCode financialCashObject;
    protected ObjectCode icrIncomeFinancialObject;
    protected ObjectCode finAccountsReceivableObj;
    protected ObjectCode finInternalEncumbranceObj;
    protected ObjectCode icrExpenseFinancialObject;
    protected ObjectCode fundBalanceObject;
    protected Chart reportsToChartOfAccounts;

    private static transient ChartService chartService;

    public String getFinChartOfAccountDescription() {
        return finChartOfAccountDescription;
    }

    public void setFinChartOfAccountDescription(String finChartOfAccountDescription) {
        this.finChartOfAccountDescription = finChartOfAccountDescription;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ObjectCode getFinAccountsPayableObject() {
        return finAccountsPayableObject;
    }

    public void setFinAccountsPayableObject(ObjectCode finAccountsPayableObject) {
        this.finAccountsPayableObject = finAccountsPayableObject;
    }

    public ObjectCode getFinExternalEncumbranceObj() {
        return finExternalEncumbranceObj;
    }

    public void setFinExternalEncumbranceObj(ObjectCode finExternalEncumbranceObj) {
        this.finExternalEncumbranceObj = finExternalEncumbranceObj;
    }

    public ObjectCode getFinPreEncumbranceObject() {
        return finPreEncumbranceObject;
    }

    public void setFinPreEncumbranceObject(ObjectCode finPreEncumbranceObject) {
        this.finPreEncumbranceObject = finPreEncumbranceObject;
    }

    public ObjectCode getFinancialCashObject() {
        return financialCashObject;
    }

    public void setFinancialCashObject(ObjectCode financialCashObject) {
        this.financialCashObject = financialCashObject;
    }

    public ObjectCode getIcrIncomeFinancialObject() {
        return icrIncomeFinancialObject;
    }

    public void setIcrIncomeFinancialObject(ObjectCode icrIncomeFinancialObject) {
        this.icrIncomeFinancialObject = icrIncomeFinancialObject;
    }

    public ObjectCode getFinAccountsReceivableObj() {
        return finAccountsReceivableObj;
    }

    public void setFinAccountsReceivableObj(ObjectCode finAccountsReceivableObj) {
        this.finAccountsReceivableObj = finAccountsReceivableObj;
    }

    @JsonSerialize(using = PersistableBusinessObjectSerializer.class)
    public Chart getReportsToChartOfAccounts() {
        return reportsToChartOfAccounts;
    }

    public void setReportsToChartOfAccounts(Chart reportsToChartOfAccounts) {
        this.reportsToChartOfAccounts = reportsToChartOfAccounts;
    }

    public ObjectCode getFinInternalEncumbranceObj() {
        return finInternalEncumbranceObj;
    }

    public void setFinInternalEncumbranceObj(ObjectCode finInternalEncumbranceObj) {
        this.finInternalEncumbranceObj = finInternalEncumbranceObj;
    }

    public ObjectCode getIcrExpenseFinancialObject() {
        return icrExpenseFinancialObject;
    }

    public void setIcrExpenseFinancialObject(ObjectCode icrExpenseFinancialObject) {
        this.icrExpenseFinancialObject = icrExpenseFinancialObject;
    }

    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public ObjectCode getExpBdgtEliminationsFinObj() {
        return expBdgtEliminationsFinObj;
    }

    public void setExpBdgtEliminationsFinObj(ObjectCode expBdgtEliminationsFinObj) {
        this.expBdgtEliminationsFinObj = expBdgtEliminationsFinObj;
    }

    public ObjectCode getIncBdgtEliminationsFinObj() {
        return incBdgtEliminationsFinObj;
    }

    public void setIncBdgtEliminationsFinObj(ObjectCode incBdgtEliminationsFinObj) {
        this.incBdgtEliminationsFinObj = incBdgtEliminationsFinObj;
    }

    public String getFinAccountsPayableObjectCode() {
        return finAccountsPayableObjectCode;
    }

    public void setFinAccountsPayableObjectCode(String finAccountsPayableObjectCode) {
        this.finAccountsPayableObjectCode = finAccountsPayableObjectCode;
    }

    public String getFinAccountsReceivableObjCode() {
        return finAccountsReceivableObjCode;
    }

    public void setFinAccountsReceivableObjCode(String finAccountsReceivableObjCode) {
        this.finAccountsReceivableObjCode = finAccountsReceivableObjCode;
    }

    public String getFinancialCashObjectCode() {
        return financialCashObjectCode;
    }

    public void setFinancialCashObjectCode(String financialCashObjectCode) {
        this.financialCashObjectCode = financialCashObjectCode;
    }

    public String getFinExternalEncumbranceObjCd() {
        return finExternalEncumbranceObjCd;
    }

    public void setFinExternalEncumbranceObjCd(String finExternalEncumbranceObjCd) {
        this.finExternalEncumbranceObjCd = finExternalEncumbranceObjCd;
    }

    public String getFinInternalEncumbranceObjCd() {
        return finInternalEncumbranceObjCd;
    }

    public void setFinInternalEncumbranceObjCd(String finInternalEncumbranceObjCd) {
        this.finInternalEncumbranceObjCd = finInternalEncumbranceObjCd;
    }

    public String getFinPreEncumbranceObjectCode() {
        return finPreEncumbranceObjectCode;
    }

    public void setFinPreEncumbranceObjectCode(String finPreEncumbranceObjectCode) {
        this.finPreEncumbranceObjectCode = finPreEncumbranceObjectCode;
    }

    public String getIcrExpenseFinancialObjectCd() {
        return icrExpenseFinancialObjectCd;
    }

    public void setIcrExpenseFinancialObjectCd(String icrExpenseFinancialObjectCd) {
        this.icrExpenseFinancialObjectCd = icrExpenseFinancialObjectCd;
    }

    public String getIcrIncomeFinancialObjectCode() {
        return icrIncomeFinancialObjectCode;
    }

    public void setIcrIncomeFinancialObjectCode(String icrIncomeFinancialObjectCode) {
        this.icrIncomeFinancialObjectCode = icrIncomeFinancialObjectCode;
    }

    public String getExpBdgtEliminationsFinObjCd() {
        return expBdgtEliminationsFinObjCd;
    }

    public void setExpBdgtEliminationsFinObjCd(String expBdgtEliminationsFinObjCd) {
        this.expBdgtEliminationsFinObjCd = expBdgtEliminationsFinObjCd;
    }

    public String getIncBdgtEliminationsFinObjCd() {
        return incBdgtEliminationsFinObjCd;
    }

    public void setIncBdgtEliminationsFinObjCd(String incBdgtEliminationsFinObjCd) {
        this.incBdgtEliminationsFinObjCd = incBdgtEliminationsFinObjCd;
    }

    public String getReportsToChartOfAccountsCode() {
        return reportsToChartOfAccountsCode;
    }

    public void setReportsToChartOfAccountsCode(String reportsToChartOfAccountsCode) {
        this.reportsToChartOfAccountsCode = reportsToChartOfAccountsCode;
    }

    public ObjectCode getFundBalanceObject() {
        return fundBalanceObject;
    }

    public void setFundBalanceObject(ObjectCode fundBalanceObject) {
        this.fundBalanceObject = fundBalanceObject;
    }

    public String getFundBalanceObjectCode() {
        return fundBalanceObjectCode;
    }

    public void setFundBalanceObjectCode(String fundBalanceObjectCode) {
        this.fundBalanceObjectCode = fundBalanceObjectCode;
    }

    /**
     * @return Returns the code and description in format: xx - xxxxxxxxxxxxxxxx
     */
    public String getCodeAndDescription() {
        if (StringUtils.isNotBlank(getChartOfAccountsCode()) && StringUtils.isNotBlank(getFinChartOfAccountDescription())) {
            return getChartOfAccountsCode() + " - " + getFinChartOfAccountDescription();
        } else {
            return "";
        }
    }

    public String getCode() {
        return this.chartOfAccountsCode;
    }

    public String getName() {
        return this.finChartOfAccountDescription;
    }

    protected static ChartService getChartService() {
        if (chartService == null) {
            chartService = SpringContext.getBean(ChartService.class);
        }
        return chartService;
    }

    public void setCode(String chartOfAccountsCode) {
        setChartOfAccountsCode(chartOfAccountsCode);
    }

    public void setName(String finChartOfAccountDescription) {
        setFinChartOfAccountDescription(finChartOfAccountDescription);
    }

    public String getChartCodeForReport() {
        return this.chartOfAccountsCode;
    }
}

