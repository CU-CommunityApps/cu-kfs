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

import org.kuali.kfs.coa.businessobject.BalanceType;
import org.kuali.kfs.coa.dataaccess.BalanceTypeDao;
import org.kuali.kfs.coa.service.BalanceTypeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This service implementation is the default implementation of the BalanceTyp service that is delivered with Kuali.
 * It uses the balance types that are defined in the Kuali database.
 */
/* Cornell Customization: backport redis*/
public class BalanceTypeServiceImpl implements BalanceTypeService {
    protected BusinessObjectService businessObjectService;
    protected BalanceTypeDao balanceTypeDao;
    protected UniversityDateService universityDateService;

    /**
     * This method retrieves a BalanceTyp instance from the Kuali database by its primary key - the balance type's code.
     *
     * @param code The primary key in the database for this data type.
     * @return A fully populated object instance.
     */
    @Override
    @Cacheable(cacheNames = BalanceType.CACHE_NAME, key = "'code='+#p0")
    public BalanceType getBalanceTypeByCode(String code) {
        return businessObjectService.findBySinglePrimaryKey(BalanceType.class, code);
    }

    @Override
    @Cacheable(cacheNames = BalanceType.CACHE_NAME, key = "'{getAllBalanceTypes}'")
    public Collection<BalanceType> getAllBalanceTypes() {
        return businessObjectService.findAll(BalanceType.class);
    }

    @Cacheable(cacheNames = BalanceType.CACHE_NAME, key = "'{getAllEncumbranceBalanceTypes}'")
    @Override
    public Collection<BalanceType> getAllEncumbranceBalanceTypes() {
        return balanceTypeDao.getEncumbranceBalanceTypes();
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setBalanceTypeDao(BalanceTypeDao balanceTypeDao) {
        this.balanceTypeDao = balanceTypeDao;
    }

    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    @Override
    @Cacheable(cacheNames = SystemOptions.CACHE_NAME, key = "'{getCostShareEncumbranceBalanceType}'+#p0")
    public String getCostShareEncumbranceBalanceType(Integer universityFiscalYear) {
        SystemOptions option = businessObjectService.findBySinglePrimaryKey(SystemOptions.class, universityFiscalYear);
        return option.getCostShareEncumbranceBalanceTypeCd();
    }

    @Override
    @Cacheable(cacheNames = SystemOptions.CACHE_NAME, key = "'{getEncumbranceBalanceTypes}'+#p0")
    public List<String> getEncumbranceBalanceTypes(Integer universityFiscalYear) {
        SystemOptions option = businessObjectService.findBySinglePrimaryKey(SystemOptions.class, universityFiscalYear);
        List<String> encumbranceBalanceTypes = new ArrayList<>();
        encumbranceBalanceTypes.add(option.getExtrnlEncumFinBalanceTypCd());
        encumbranceBalanceTypes.add(option.getIntrnlEncumFinBalanceTypCd());
        encumbranceBalanceTypes.add(option.getPreencumbranceFinBalTypeCd());
        encumbranceBalanceTypes.add(option.getCostShareEncumbranceBalanceTypeCd());
        return encumbranceBalanceTypes;
    }

    @Override
    @Cacheable(cacheNames = SystemOptions.CACHE_NAME, key = "'{getCostShareEncumbranceBalanceType}CurrentFY'")
    public String getCurrentYearCostShareEncumbranceBalanceType() {
        return getCostShareEncumbranceBalanceType(universityDateService.getCurrentFiscalYear());
    }

    @Override
    @Cacheable(cacheNames = SystemOptions.CACHE_NAME, key = "'{getEncumbranceBalanceTypes}CurrentFY'")
    public List<String> getCurrentYearEncumbranceBalanceTypes() {
        return getEncumbranceBalanceTypes(universityDateService.getCurrentFiscalYear());
    }

    @Override
    @Cacheable(cacheNames = SystemOptions.CACHE_NAME, key = "'{getContinuationAccountBypassBalanceTypeCodes}'+#p0")
    public List<String> getContinuationAccountBypassBalanceTypeCodes(Integer universityFiscalYear) {
        SystemOptions option = businessObjectService.findBySinglePrimaryKey(SystemOptions.class, universityFiscalYear);
        List<String> continuationAccountBypassBalanceTypes = new ArrayList<>(3);
        continuationAccountBypassBalanceTypes.add(option.getExtrnlEncumFinBalanceTypCd());
        continuationAccountBypassBalanceTypes.add(option.getIntrnlEncumFinBalanceTypCd());
        continuationAccountBypassBalanceTypes.add(option.getPreencumbranceFinBalTypeCd());
        return continuationAccountBypassBalanceTypes;
    }
}
