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
package org.kuali.kfs.coa.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.ObjectType;
import org.kuali.kfs.coa.service.ObjectTypeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;
import java.util.List;

/* Cornell Customization: backport redis*/
public class ObjectTypeServiceImpl implements ObjectTypeService {
    private static final Logger LOG = LogManager.getLogger();

    protected UniversityDateService universityDateService;
    protected BusinessObjectService businessObjectService;

    @Override
    @Cacheable(cacheNames = ObjectType.CACHE_NAME, key = "'objectTypeCode='+#p0")
    public ObjectType getByPrimaryKey(String objectTypeCode) {
        return businessObjectService.findBySinglePrimaryKey(ObjectType.class, objectTypeCode);
    }

    @Override
    @Cacheable(cacheNames = SystemOptions.CACHE_NAME, key = "'{AssetObjectType}'+#p0")
    public String getAssetObjectType(Integer universityFiscalYear) {
        SystemOptions options = businessObjectService.findBySinglePrimaryKey(SystemOptions.class, universityFiscalYear);
        return options.getFinancialObjectTypeAssetsCd();
    }

    @Override
    @Cacheable(cacheNames = SystemOptions.CACHE_NAME, key = "'{BasicExpenseObjectTypes}'+#p0")
    public List<String> getBasicExpenseObjectTypes(Integer universityFiscalYear) {
        List<String> basicExpenseObjectTypes = new ArrayList<>();
        SystemOptions option = businessObjectService.findBySinglePrimaryKey(SystemOptions.class, universityFiscalYear);
        basicExpenseObjectTypes.add(option.getFinObjTypeExpenditureexpCd());
        basicExpenseObjectTypes.add(option.getFinObjTypeExpendNotExpCode());
        basicExpenseObjectTypes.add(option.getFinObjTypeExpNotExpendCode());
        return basicExpenseObjectTypes;
    }

    @Override
    @Cacheable(cacheNames = SystemOptions.CACHE_NAME, key = "'{ExpenseObjectTypes}'+#p0", unless = "#result.empty")
    public List<String> getExpenseObjectTypes(Integer universityFiscalYear) {
        List<String> expenseObjectTypes = new ArrayList<>();
        SystemOptions option = businessObjectService.findBySinglePrimaryKey(SystemOptions.class, universityFiscalYear);
        expenseObjectTypes.add(option.getFinObjTypeExpenditureexpCd());
        expenseObjectTypes.add(option.getFinObjTypeExpendNotExpCode());
        expenseObjectTypes.add(option.getFinObjTypeExpNotExpendCode());
        expenseObjectTypes.add(option.getFinancialObjectTypeTransferExpenseCd());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Expense object types are " + StringUtils.join(expenseObjectTypes, ", "));
        }

        return expenseObjectTypes;
    }

    @Override
    @Cacheable(cacheNames = SystemOptions.CACHE_NAME, key = "'{BasicIncomeObjectTypes}'+#p0")
    public List<String> getBasicIncomeObjectTypes(Integer universityFiscalYear) {
        List<String> basicIncomeObjectTypes = new ArrayList<>();
        SystemOptions option = businessObjectService.findBySinglePrimaryKey(SystemOptions.class, universityFiscalYear);
        basicIncomeObjectTypes.add(option.getFinObjectTypeIncomecashCode());
        basicIncomeObjectTypes.add(option.getFinObjTypeIncomeNotCashCd());
        basicIncomeObjectTypes.add(option.getFinObjTypeCshNotIncomeCd());
        return basicIncomeObjectTypes;
    }

    @Override
    @Cacheable(cacheNames = SystemOptions.CACHE_NAME, key = "'{ExpenseTransferObjectType}'+#p0")
    public String getExpenseTransferObjectType(Integer universityFiscalYear) {
        SystemOptions option = businessObjectService.findBySinglePrimaryKey(SystemOptions.class, universityFiscalYear);
        return option.getFinancialObjectTypeTransferExpenseCd();
    }

    @Override
    @Cacheable(value = SystemOptions.CACHE_NAME, key = "'{IncomeTransferObjectType}'+#p0")
    public String getIncomeTransferObjectType(Integer universityFiscalYear) {
        SystemOptions option = businessObjectService.findBySinglePrimaryKey(SystemOptions.class, universityFiscalYear);
        return option.getFinancialObjectTypeTransferIncomeCd();
    }

    @Override
    @Cacheable(value = SystemOptions.CACHE_NAME, key = "'{AssetObjectType}CurrentFY'")
    public String getCurrentYearAssetObjectType() {
        return getAssetObjectType(universityDateService.getCurrentFiscalYear());
    }

    @Override
    @Cacheable(value = SystemOptions.CACHE_NAME, key = "'{BasicExpenseObjectTypes}CurrentFY'")
    public List<String> getCurrentYearBasicExpenseObjectTypes() {
        return getBasicExpenseObjectTypes(universityDateService.getCurrentFiscalYear());
    }

    @Override
    @Cacheable(value = SystemOptions.CACHE_NAME, key = "'{ExpenseObjectTypes}CurrentFY'", unless = "#result.empty")
    public List<String> getCurrentYearExpenseObjectTypes() {
        final List<String> expenseObjectTypes = getExpenseObjectTypes(universityDateService.getCurrentFiscalYear());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Expense object types are " + StringUtils.join(expenseObjectTypes, ", "));
        }

        return expenseObjectTypes;
    }

    @Override
    @Cacheable(value = SystemOptions.CACHE_NAME, key = "'{BasicIncomeObjectTypes}CurrentFY'")
    public List<String> getCurrentYearBasicIncomeObjectTypes() {
        return getBasicIncomeObjectTypes(universityDateService.getCurrentFiscalYear());
    }

    @Override
    @Cacheable(value = SystemOptions.CACHE_NAME, key = "'{ExpenseTransferObjectType}CurrentFY'")
    public String getCurrentYearExpenseTransferObjectType() {
        return getExpenseTransferObjectType(universityDateService.getCurrentFiscalYear());
    }

    @Override
    @Cacheable(value = SystemOptions.CACHE_NAME, key = "'{IncomeTransferObjectType}CurrentFY'")
    public String getCurrentYearIncomeTransferObjectType() {
        return getIncomeTransferObjectType(universityDateService.getCurrentFiscalYear());
    }

    @Override
    @Cacheable(value = SystemOptions.CACHE_NAME, key = "'{NominalActivityClosingAllowedObjectTypes}'+#p0")
    public List<String> getNominalActivityClosingAllowedObjectTypes(Integer universityFiscalYear) {
        List<String> nominalClosingObjectTypes = new ArrayList<>();
        SystemOptions option = businessObjectService.findBySinglePrimaryKey(SystemOptions.class, universityFiscalYear);
        nominalClosingObjectTypes.add(option.getFinObjTypeExpNotExpendCode());
        nominalClosingObjectTypes.add(option.getFinObjTypeExpenditureexpCd());
        nominalClosingObjectTypes.add(option.getFinancialObjectTypeTransferExpenseCd());
        nominalClosingObjectTypes.add(option.getFinancialObjectTypeTransferIncomeCd());
        nominalClosingObjectTypes.add(option.getFinObjTypeExpendNotExpCode());
        nominalClosingObjectTypes.add(option.getFinObjTypeCshNotIncomeCd());
        nominalClosingObjectTypes.add(option.getFinObjTypeIncomeNotCashCd());
        nominalClosingObjectTypes.add(option.getFinObjectTypeIncomecashCode());
        return nominalClosingObjectTypes;
    }

    @Override
    @Cacheable(value = SystemOptions.CACHE_NAME, key = "'{GeneralForwardBalanceObjectTypes}'+#p0")
    public List<String> getGeneralForwardBalanceObjectTypes(Integer universityFiscalYear) {
        SystemOptions option = businessObjectService.findBySinglePrimaryKey(SystemOptions.class, universityFiscalYear);
        List<String> generalBalanceForwardObjectTypes = new ArrayList<>();
        generalBalanceForwardObjectTypes.add(option.getFinancialObjectTypeAssetsCd());
        generalBalanceForwardObjectTypes.add(option.getFinObjectTypeLiabilitiesCode());
        generalBalanceForwardObjectTypes.add(option.getFinObjectTypeFundBalanceCd());
        return generalBalanceForwardObjectTypes;
    }

    @Override
    @Cacheable(value = SystemOptions.CACHE_NAME, key = "'{CumulativeForwardBalanceObjectTypes}'+#p0")
    public List<String> getCumulativeForwardBalanceObjectTypes(Integer universityFiscalYear) {
        SystemOptions option = businessObjectService.findBySinglePrimaryKey(SystemOptions.class, universityFiscalYear);
        List<String> cumulativeBalanceForwardsObjectTypes = new ArrayList<>();
        cumulativeBalanceForwardsObjectTypes.add(option.getFinObjTypeExpendNotExpCode());
        cumulativeBalanceForwardsObjectTypes.add(option.getFinObjTypeExpNotExpendCode());
        cumulativeBalanceForwardsObjectTypes.add(option.getFinObjTypeExpenditureexpCd());
        cumulativeBalanceForwardsObjectTypes.add(option.getFinObjTypeIncomeNotCashCd());
        cumulativeBalanceForwardsObjectTypes.add(option.getFinancialObjectTypeTransferExpenseCd());
        cumulativeBalanceForwardsObjectTypes.add(option.getFinancialObjectTypeTransferIncomeCd());
        cumulativeBalanceForwardsObjectTypes.add(option.getFinObjectTypeIncomecashCode());
        cumulativeBalanceForwardsObjectTypes.add(option.getFinObjTypeCshNotIncomeCd());
        return cumulativeBalanceForwardsObjectTypes;
    }

    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    @Override
    @Cacheable(value = SystemOptions.CACHE_NAME, key = "'{ExpenseAndTransferObjectTypesForPayments}CurrentFY'")
    public List<String> getExpenseAndTransferObjectTypesForPayments() {
        List<String> expenseAndTransferObjectTypes = new ArrayList<>();

        SystemOptions option = businessObjectService.findBySinglePrimaryKey(SystemOptions.class,
                universityDateService.getCurrentFiscalYear());

        expenseAndTransferObjectTypes.add(option.getFinancialObjectTypeAssetsCd());
        expenseAndTransferObjectTypes.add(option.getFinObjTypeExpenditureexpCd());
        expenseAndTransferObjectTypes.add(option.getFinObjTypeExpNotExpendCode());
        expenseAndTransferObjectTypes.add(option.getFinObjTypeExpendNotExpCode());
        expenseAndTransferObjectTypes.add(option.getFinancialObjectTypeTransferExpenseCd());

        return expenseAndTransferObjectTypes;
    }

    @Override
    @Cacheable(value = SystemOptions.CACHE_NAME, key = "'{IncomeAndTransferObjectTypesForPayments}CurrentFY'")
    public List<String> getIncomeAndTransferObjectTypesForPayments() {
        List<String> incomeAndTransferObjectTypes = new ArrayList<>();

        SystemOptions option = businessObjectService.findBySinglePrimaryKey(SystemOptions.class,
                universityDateService.getCurrentFiscalYear());

        incomeAndTransferObjectTypes.add(option.getFinObjectTypeLiabilitiesCode());
        incomeAndTransferObjectTypes.add(option.getFinObjectTypeFundBalanceCd());
        incomeAndTransferObjectTypes.add(option.getFinObjectTypeIncomecashCode());
        incomeAndTransferObjectTypes.add(option.getFinObjTypeIncomeNotCashCd());
        incomeAndTransferObjectTypes.add(option.getFinObjTypeCshNotIncomeCd());
        incomeAndTransferObjectTypes.add(option.getFinancialObjectTypeTransferIncomeCd());

        return incomeAndTransferObjectTypes;
    }
}
