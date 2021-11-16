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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.gl.businessobject.SufficientFundRebuild;
import org.kuali.kfs.krad.bo.KualiCode;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.impl.PersistenceStructureServiceImpl;
import org.kuali.kfs.sys.businessobject.FiscalYearBasedBusinessObject;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.businessobject.serialization.PersistableBusinessObjectSerializer;
import org.kuali.kfs.sys.context.SpringContext;

/* Cornell Customization: backport redis*/
public class ObjectCode extends PersistableBusinessObjectBase implements KualiCode, FiscalYearBasedBusinessObject {

    static {
        PersistenceStructureServiceImpl.referenceConversionMap.put(ObjectCode.class, ObjectCodeCurrent.class);
    }

    private static final Logger LOG = LogManager.getLogger();
    
    public static final String CACHE_NAME = "ObjectCode";

    protected static BusinessObjectService businessObjectService;

    private static final long serialVersionUID = -965833141452795485L;
    protected Integer universityFiscalYear;
    protected String chartOfAccountsCode;
    protected String financialObjectCode;
    protected String financialObjectCodeName;
    protected String financialObjectCodeShortName;
    protected String historicalFinancialObjectCode;
    protected boolean active;
    protected String financialObjectLevelCode;
    protected String reportsToChartOfAccountsCode;
    protected String reportsToFinancialObjectCode;
    protected String financialObjectTypeCode;
    protected String financialObjectSubTypeCode;
    protected String financialBudgetAggregationCd;
    protected String nextYearFinancialObjectCode;
    protected String finObjMandatoryTrnfrelimCd;
    protected String financialFederalFundedCode;

    protected transient BudgetAggregationCode financialBudgetAggregation;
    protected transient MandatoryTransferEliminationCode finObjMandatoryTrnfrelim;
    protected transient FederalFundedCode financialFederalFunded;
    protected transient SystemOptions universityFiscal;
    protected transient ObjectLevel financialObjectLevel;
    protected transient Chart chartOfAccounts;
    protected transient Chart reportsToChartOfAccounts;
    protected transient ObjectCode reportsToFinancialObject;
    protected transient ObjectType financialObjectType;
    protected transient ObjectSubType financialObjectSubType;

    public ObjectCode() {
        // construct the referenced objects for the calling of the referencing object
        this.financialObjectLevel = new ObjectLevel();
        this.financialObjectType = new ObjectType();
    }

    /**
     * Constructs a ObjectCode.java with the given defaults; this way, it is not necessary to use any deprecated setters.
     *
     * @param fiscalYear
     * @param chart
     * @param financialObjectCode - an active object code
     */
    public ObjectCode(Integer fiscalYear, String chart, String financialObjectCode) {
        this.universityFiscalYear = fiscalYear;
        this.chartOfAccountsCode = chart;
        this.financialObjectCode = financialObjectCode;
        this.active = true;
    }

    /**
     * This method is only for use by the framework
     */
    public void setUniversityFiscalYear(Integer i) {
        this.universityFiscalYear = i;
    }

    public FederalFundedCode getFinancialFederalFunded() {
        return financialFederalFunded;
    }

    public void setFinancialFederalFunded(FederalFundedCode financialFederalFunded) {
        this.financialFederalFunded = financialFederalFunded;
    }

    public MandatoryTransferEliminationCode getFinObjMandatoryTrnfrelim() {
        return finObjMandatoryTrnfrelim;
    }

    public void setFinObjMandatoryTrnfrelim(MandatoryTransferEliminationCode finObjMandatoryTrnfrelim) {
        this.finObjMandatoryTrnfrelim = finObjMandatoryTrnfrelim;
    }

    public String getFinancialObjectCode() {
        return financialObjectCode;
    }

    public void setFinancialObjectCode(String financialObjectCode) {
        this.financialObjectCode = financialObjectCode;
    }

    public String getFinancialObjectCodeName() {
        return financialObjectCodeName;
    }

    public void setFinancialObjectCodeName(String financialObjectCodeName) {
        this.financialObjectCodeName = financialObjectCodeName;
    }

    public String getFinancialObjectCodeShortName() {
        return financialObjectCodeShortName;
    }

    public void setFinancialObjectCodeShortName(String financialObjectCodeShortName) {
        this.financialObjectCodeShortName = financialObjectCodeShortName;
    }

    public String getHistoricalFinancialObjectCode() {
        return historicalFinancialObjectCode;
    }

    public void setHistoricalFinancialObjectCode(String historicalFinancialObjectCode) {
        this.historicalFinancialObjectCode = historicalFinancialObjectCode;
    }

    public boolean isFinancialObjectActiveCode() {
        return active;
    }

    public void setFinancialObjectActiveCode(boolean active) {
        this.active = active;
    }

    public SystemOptions getUniversityFiscal() {
        return universityFiscal;
    }

    public void setUniversityFiscal(SystemOptions universityFiscal) {
        this.universityFiscal = universityFiscal;
    }

    public ObjectLevel getFinancialObjectLevel() {
        return financialObjectLevel;
    }

    public void setFinancialObjectLevel(ObjectLevel financialObjectLevel) {
        this.financialObjectLevel = financialObjectLevel;
    }

    public Chart getChartOfAccounts() {
        return chartOfAccounts;
    }

    public void setChartOfAccounts(Chart chartOfAccounts) {
        this.chartOfAccounts = chartOfAccounts;
    }

    public Chart getReportsToChartOfAccounts() {
        return reportsToChartOfAccounts;
    }

    public void setReportsToChartOfAccounts(Chart reportsToChartOfAccounts) {
        this.reportsToChartOfAccounts = reportsToChartOfAccounts;
    }

    @JsonSerialize(using = PersistableBusinessObjectSerializer.class)
    public ObjectCode getReportsToFinancialObject() {
        return reportsToFinancialObject;
    }

    public void setReportsToFinancialObject(ObjectCode reportsToFinancialObject) {
        this.reportsToFinancialObject = reportsToFinancialObject;
    }

    public ObjectType getFinancialObjectType() {
        return financialObjectType;
    }

    public void setFinancialObjectType(ObjectType financialObjectType) {
        this.financialObjectType = financialObjectType;
    }

    public ObjectSubType getFinancialObjectSubType() {
        return financialObjectSubType;
    }

    public void setFinancialObjectSubType(ObjectSubType financialObjectSubType) {
        this.financialObjectSubType = financialObjectSubType;
    }

    public void setChartOfAccountsCode(String string) {
        this.chartOfAccountsCode = string;
    }

    public String getChartOfAccountsCode() {
        return this.chartOfAccountsCode;
    }

    public Integer getUniversityFiscalYear() {
        return this.universityFiscalYear;
    }

    public String getFinancialBudgetAggregationCd() {
        return financialBudgetAggregationCd;
    }

    public void setFinancialBudgetAggregationCd(String financialBudgetAggregationCd) {
        this.financialBudgetAggregationCd = financialBudgetAggregationCd;
    }

    public String getFinancialObjectLevelCode() {
        return financialObjectLevelCode;
    }

    public void setFinancialObjectLevelCode(String financialObjectLevelCode) {
        this.financialObjectLevelCode = financialObjectLevelCode;
    }

    public String getFinancialObjectSubTypeCode() {
        return financialObjectSubTypeCode;
    }

    public void setFinancialObjectSubTypeCode(String financialObjectSubTypeCode) {
        this.financialObjectSubTypeCode = financialObjectSubTypeCode;
    }

    public String getFinancialObjectTypeCode() {
        return financialObjectTypeCode;
    }

    public void setFinancialObjectTypeCode(String financialObjectTypeCode) {
        this.financialObjectTypeCode = financialObjectTypeCode;
    }

    public String getNextYearFinancialObjectCode() {
        return nextYearFinancialObjectCode;
    }

    public void setNextYearFinancialObjectCode(String nextYearFinancialObjectCode) {
        this.nextYearFinancialObjectCode = nextYearFinancialObjectCode;
    }

    public String getReportsToChartOfAccountsCode() {
        return reportsToChartOfAccountsCode;
    }

    public void setReportsToChartOfAccountsCode(String reportsToChartOfAccountsCode) {
        this.reportsToChartOfAccountsCode = reportsToChartOfAccountsCode;
    }

    public String getReportsToFinancialObjectCode() {
        return reportsToFinancialObjectCode;
    }

    public void setReportsToFinancialObjectCode(String reportsToFinancialObjectCode) {
        this.reportsToFinancialObjectCode = reportsToFinancialObjectCode;
    }

    public String getFinancialFederalFundedCode() {
        return financialFederalFundedCode;
    }

    public void setFinancialFederalFundedCode(String financialFederalFundedCode) {
        this.financialFederalFundedCode = financialFederalFundedCode;
    }

    public String getFinObjMandatoryTrnfrelimCd() {
        return finObjMandatoryTrnfrelimCd;
    }

    public void setFinObjMandatoryTrnfrelimCd(String finObjMandatoryTrnfrelimCd) {
        this.finObjMandatoryTrnfrelimCd = finObjMandatoryTrnfrelimCd;
    }

    public BudgetAggregationCode getFinancialBudgetAggregation() {
        return financialBudgetAggregation;
    }

    public void setFinancialBudgetAggregation(BudgetAggregationCode financialBudgetAggregation) {
        this.financialBudgetAggregation = financialBudgetAggregation;
    }

    protected BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        }
        return businessObjectService;
    }

    @Override
    protected void beforeUpdate() {
        super.beforeUpdate();
        try {
            ObjectCode originalObjectCode = (ObjectCode) getBusinessObjectService().retrieve(this);

            if (originalObjectCode != null) {
                if (!originalObjectCode.getFinancialObjectLevelCode().equals(getFinancialObjectLevelCode())) {
                    SufficientFundRebuild sfr = new SufficientFundRebuild();
                    sfr.setAccountFinancialObjectTypeCode(SufficientFundRebuild.REBUILD_OBJECT);
                    sfr.setChartOfAccountsCode(originalObjectCode.getChartOfAccountsCode());
                    sfr.setAccountNumberFinancialObjectCode(originalObjectCode.getFinancialObjectLevelCode());
                    if (getBusinessObjectService().retrieve(sfr) == null) {
                        getBusinessObjectService().save(sfr);
                    }
                    sfr = new SufficientFundRebuild();
                    sfr.setAccountFinancialObjectTypeCode(SufficientFundRebuild.REBUILD_OBJECT);
                    sfr.setChartOfAccountsCode(getChartOfAccountsCode());
                    sfr.setAccountNumberFinancialObjectCode(getFinancialObjectLevelCode());
                    if (getBusinessObjectService().retrieve(sfr) == null) {
                        getBusinessObjectService().save(sfr);
                    }
                }
            }
        } catch (Exception ex) {
            LOG.error("Problem updating sufficient funds rebuild table: ", ex);
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean a) {
        this.active = a;
    }

    public void setCode(String code) {
        this.chartOfAccountsCode = code;
    }

    public void setName(String name) {
        this.financialObjectCodeName = name;
    }

    public String getCode() {
        return this.financialObjectCode;
    }

    public String getName() {
        return this.financialObjectCodeName;
    }

    /**
     * Determines if this object code reports to itself
     *
     * @return true if the object code reports to itself, false otherwise
     */
    public boolean isReportingToSelf() {
        return StringUtils.equals(this.getChartOfAccountsCode(), this.getReportsToChartOfAccountsCode()) && StringUtils.equals(this.getFinancialObjectCode(), this.getReportsToFinancialObjectCode());
    }
}
