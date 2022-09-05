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
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ObjectCodeCurrent;
import org.kuali.kfs.coa.dataaccess.ObjectCodeDao;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/* Cornell Customization: backport redis fix on FINP-8169*/
public class ObjectCodeServiceImpl implements ObjectCodeService {

    protected ObjectCodeDao objectCodeDao;
    protected UniversityDateService universityDateService;
    protected BusinessObjectService businessObjectService;

    @Override
    @Cacheable(cacheNames = ObjectCode.CACHE_NAME, key = "'{" + ObjectCode.CACHE_NAME + "}'+#p0+'-'+#p1+'-'+#p2")
    public ObjectCode getByPrimaryId(Integer universityFiscalYear, String chartOfAccountsCode,
            String financialObjectCode) {
        Map<String, Object> keys = new HashMap<>(3);
        keys.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, universityFiscalYear);
        keys.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        keys.put(KFSPropertyConstants.FINANCIAL_OBJECT_CODE, financialObjectCode);
        return businessObjectService.findByPrimaryKey(ObjectCode.class, keys);
    }

    @Override
    @Cacheable(cacheNames = ObjectCode.CACHE_NAME, key = "'{" + ObjectCode.CACHE_NAME + "}'+#p0+'-'+#p1+'-'+#p2")
    public ObjectCode getByPrimaryIdWithCaching(Integer universityFiscalYear, String chartOfAccountsCode,
            String financialObjectCode) {
        return getByPrimaryId(universityFiscalYear, chartOfAccountsCode, financialObjectCode);
    }

    public void setObjectCodeDao(ObjectCodeDao objectCodeDao) {
        this.objectCodeDao = objectCodeDao;
    }

    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    @Override
    public List getYearList(String chartOfAccountsCode, String financialObjectCode) {
        return objectCodeDao.getYearList(chartOfAccountsCode, financialObjectCode);
    }

    @Override
    public String getObjectCodeNamesByCharts(Integer universityFiscalYear, String[] chartOfAccountCodes,
            String financialObjectCode) {
        String onlyObjectCodeName = "";
        SortedSet<String> objectCodeNames = new TreeSet<>();
        for (String chartOfAccountsCode : chartOfAccountCodes) {
            ObjectCode objCode = this.getByPrimaryId(universityFiscalYear, chartOfAccountsCode, financialObjectCode);
            if (objCode != null) {
                onlyObjectCodeName = objCode.getFinancialObjectCodeName();
                objectCodeNames.add(objCode.getFinancialObjectCodeName());
            } else {
                onlyObjectCodeName = "Not Found";
            }
        }
        if (objectCodeNames.size() > 1) {
            return StringUtils.join(objectCodeNames.toArray(), ", ");
        } else {
            return onlyObjectCodeName;
        }
    }

    @Override
    @Cacheable(cacheNames = ObjectCode.CACHE_NAME, key = "'CurrentFY'+'-'+#p0+'-'+#p1")
    public ObjectCode getByPrimaryIdForCurrentYear(String chartOfAccountsCode, String financialObjectCode) {
        return getByPrimaryId(universityDateService.getCurrentFiscalYear(), chartOfAccountsCode, financialObjectCode);
    }

    @Override
    public List<ObjectCode> getObjectCodesByLevelIds(List<String> levelCodes) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(KFSPropertyConstants.FINANCIAL_OBJECT_LEVEL_CODE, levelCodes);
        return new ArrayList<>(businessObjectService.findMatching(ObjectCode.class, fieldValues));
    }

    @Override
    public boolean doesObjectConsolidationContainObjectCode(String chartOfAccountsCode, String consolidationCode,
            String objectChartOfAccountsCode, String objectCode) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, objectChartOfAccountsCode);
        fieldValues.put(KFSPropertyConstants.FINANCIAL_OBJECT_CODE, objectCode);
        fieldValues.put(KFSPropertyConstants.FINANCIAL_OBJECT_LEVEL + "." +
                KFSPropertyConstants.FINANCIAL_CONSOLIDATION_OBJECT_CODE, consolidationCode);
        fieldValues.put(KFSPropertyConstants.FINANCIAL_OBJECT_LEVEL + "." +
                KFSPropertyConstants.FINANCIAL_CONSOLIDATION_OBJECT + "." + KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE,
                chartOfAccountsCode);
        return getBusinessObjectService().countMatching(ObjectCodeCurrent.class, fieldValues) > 0;
    }

    @Override
    public boolean doesObjectLevelContainObjectCode(String chartOfAccountsCode, String levelCode,
            String objectChartOfAccountsCode, String objectCode) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, objectChartOfAccountsCode);
        fieldValues.put(KFSPropertyConstants.FINANCIAL_OBJECT_CODE, objectCode);
        fieldValues.put(KFSPropertyConstants.FINANCIAL_OBJECT_LEVEL_CODE, levelCode);
        fieldValues.put(KFSPropertyConstants.FINANCIAL_OBJECT_LEVEL + "." + KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE,
                chartOfAccountsCode);
        return getBusinessObjectService().countMatching(ObjectCodeCurrent.class, fieldValues) > 0;
    }

    @Override
    public ObjectCode getByPrimaryIdForLatestValidYear(String chartOfAccountsCode, String financialObjectCode) {
        ObjectCode objectCode = getByPrimaryIdForCurrentYear(chartOfAccountsCode, financialObjectCode);
        if (ObjectUtils.isNull(objectCode)) {
            List<Integer> years = getYearList(chartOfAccountsCode, financialObjectCode);
            // sort the years in descending order so we start with the most recent years
            Collections.sort(years, Collections.reverseOrder());
            objectCode = years.stream()
                    .map(year -> getByPrimaryId(year, chartOfAccountsCode, financialObjectCode))
                    .filter(ObjectUtils::isNotNull)
                    .findFirst()
                    .get();
        }
        return objectCode;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
}
