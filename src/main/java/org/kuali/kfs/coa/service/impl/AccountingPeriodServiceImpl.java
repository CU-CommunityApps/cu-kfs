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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.sql.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This service implementation is the default implementation of the AccountingPeriod service that is delivered with Kuali.
 */
/* Cornell Customization: backport redis*/
public class AccountingPeriodServiceImpl implements AccountingPeriodService {
    // member data
    private static final Logger LOG = LogManager.getLogger();
    protected BusinessObjectService businessObjectService;
    protected DateTimeService dateTimeService;

    protected static final Set<String> _invalidPeriodCodes = new TreeSet<>();

    static {
        _invalidPeriodCodes.add("13");
        _invalidPeriodCodes.add("AB");
        _invalidPeriodCodes.add("BB");
        _invalidPeriodCodes.add("CB");
    }

    /**
     * The default implementation.
     */
    @Override
    @Cacheable(cacheNames = AccountingPeriod.CACHE_NAME, key = "'{getAllAccountingPeriods}'")
    public Collection<AccountingPeriod> getAllAccountingPeriods() {
        return businessObjectService.findAll(AccountingPeriod.class);
    }

    /**
     * Implements by choosing only accounting periods that are active.
     */
    @Override
    @Cacheable(cacheNames = AccountingPeriod.CACHE_NAME, key = "'{getOpenAccountingPeriods}'")
    public Collection<AccountingPeriod> getOpenAccountingPeriods() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(KFSConstants.ACCOUNTING_PERIOD_ACTIVE_INDICATOR_FIELD, Boolean.TRUE);

        return businessObjectService.findMatchingOrderBy(AccountingPeriod.class, map,
                KFSPropertyConstants.ACCTING_PERIOD_UNIV_FISCAL_PERIOD_END_DATE, true);
    }

    /**
     * This method is a helper method to easily grab an accounting period by looking up it's period and fiscal year
     *
     * @param periodCode
     * @param fiscalYear
     * @return an accounting period
     */
    @Override
    @Cacheable(cacheNames = AccountingPeriod.CACHE_NAME, key = "'{getByPeriod}'+#p0+'-'+#p1")
    public AccountingPeriod getByPeriod(String periodCode, Integer fiscalYear) {
        // build up the hashmap to find the accounting period
        HashMap<String, Object> keys = new HashMap<>();
        keys.put(KFSPropertyConstants.UNIVERSITY_FISCAL_PERIOD_CODE, periodCode);
        keys.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, fiscalYear);
        return businessObjectService.findByPrimaryKey(AccountingPeriod.class, keys);
    }

    /**
     * This method allows for AccountingPeriod retrieval via String date.
     *
     * @param dateString
     */
    @Override
    public AccountingPeriod getByStringDate(String dateString) {
        AccountingPeriod acctPeriod;
        try {
            acctPeriod = getByDate(dateTimeService.convertToSqlDate(dateString));
        } catch (Exception pe) {
            LOG.error("AccountingPeriod getByStringDate unable to convert string " + dateString + " into date.", pe);
            throw new RuntimeException("AccountingPeriod getByStringDate unable to convert string " + dateString + " into date.", pe);
        }
        return acctPeriod;
    }

    /**
     * This method is a helper method to get the current period.
     */
    @Override
    @Cacheable(cacheNames = AccountingPeriod.CACHE_NAME, key = "'{getByDate}-date='+#p0")
    public AccountingPeriod getByDate(Date date) {
        Map<String, Object> primaryKeys = new HashMap<>();
        primaryKeys.put(KFSPropertyConstants.UNIVERSITY_DATE, date);
        UniversityDate universityDate = businessObjectService.findByPrimaryKey(UniversityDate.class, primaryKeys);
        primaryKeys.clear();
        primaryKeys.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, universityDate.getUniversityFiscalYear());
        primaryKeys.put(KFSPropertyConstants.UNIVERSITY_FISCAL_PERIOD_CODE, universityDate.getUniversityFiscalAccountingPeriod());
        return businessObjectService.findByPrimaryKey(AccountingPeriod.class, primaryKeys);
    }

    /**
     * Helper method to get the previous period given an accounting period.
     */
    @Override
    public AccountingPeriod getPreviousAccountingPeriod(AccountingPeriod currentAccountingPeriod) {
        int currentAccountingPeriodCode = Integer.parseInt(currentAccountingPeriod.getUniversityFiscalPeriodCode());
        int currentFiscalYear = currentAccountingPeriod.getUniversityFiscalYear();

        int previousAccountingPeriodCode = currentAccountingPeriodCode - 1;

        if (previousAccountingPeriodCode == 0) {
            previousAccountingPeriodCode = 12;
            currentFiscalYear -= 1;
        }
        String periodCode;
        if (previousAccountingPeriodCode < 10) {
            periodCode = "0" + previousAccountingPeriodCode;
        } else {
            periodCode = "" + previousAccountingPeriodCode;
        }
        return getByPeriod(periodCode, currentFiscalYear);
    }

    /**
     * This checks to see if the period code is empty or invalid ("13", "AB", "BB", "CB")
     *
     * @param period
     * @return
     */
    protected boolean isInvalidPeriodCode(AccountingPeriod period) {
        String periodCode = period.getUniversityFiscalPeriodCode();
        if (periodCode == null) {
            throw new IllegalArgumentException("invalid (null) universityFiscalPeriodCode for" + period);
        }
        return _invalidPeriodCodes.contains(periodCode);
    }

    @Override
    public int compareAccountingPeriodsByDate(AccountingPeriod tweedleDee, AccountingPeriod tweedleDum) {
        // note the lack of defensive programming here. If you send a null accounting
        // period...then chances are, you deserve the NPE that you receive
        Date tweedleDeeClose = tweedleDee.getUniversityFiscalPeriodEndDate();
        Date tweedleDumClose = tweedleDum.getUniversityFiscalPeriodEndDate();
        return tweedleDeeClose.compareTo(tweedleDumClose);
    }

    @Override
    @CacheEvict(value = AccountingPeriod.CACHE_NAME, allEntries = true)
    public void clearCache() {
        // nothing to do - annotation does it all
    }

    public Date getAccountingPeriodReversalDateByType(String avTypeCode, String selectedPostingPeriodCode,
            Integer selectedPostingYear, Date documentCreateDate) {
        switch (avTypeCode) {
            case KFSConstants.AuxiliaryVoucher.ACCRUAL_DOC_TYPE:
                AccountingPeriod accountingPeriod = this.getByPeriod(selectedPostingPeriodCode, selectedPostingYear);
                return accountingPeriod.getAuxiliaryVoucherDefaultReversalDate();
            case KFSConstants.AuxiliaryVoucher.RECODE_DOC_TYPE:
                return documentCreateDate;
            default:
                return null;
        }
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }
}
