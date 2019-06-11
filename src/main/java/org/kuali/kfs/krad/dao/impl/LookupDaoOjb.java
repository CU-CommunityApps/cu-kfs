/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
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
package org.kuali.kfs.krad.dao.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.Query;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.krad.bo.InactivatableFromTo;
import org.kuali.kfs.krad.criteria.OjbUtility;
import org.kuali.kfs.krad.dao.LookupDao;
import org.kuali.kfs.krad.lookup.CollectionIncomplete;
import org.kuali.kfs.krad.lookup.LookupUtils;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.service.PersistenceStructureService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.search.SearchOperator;
import org.kuali.rice.core.api.util.RiceKeyConstants;
import org.kuali.rice.core.api.util.type.TypeUtils;
import org.kuali.rice.core.framework.persistence.ojb.conversion.OjbCharBooleanConversion;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.kuali.rice.core.framework.persistence.platform.DatabasePlatform;
import org.kuali.rice.krad.bo.BusinessObject;
import org.springframework.dao.DataIntegrityViolationException;
import org.springmodules.orm.ojb.OjbOperationException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LookupDaoOjb extends PlatformAwareDaoBaseOjb implements LookupDao, OjbUtility {
    private static final Logger LOG = LogManager.getLogger(LookupDaoOjb.class);

    private DateTimeService dateTimeService;
    private PersistenceStructureService persistenceStructureService;
    private DataDictionaryService dataDictionaryService;

    @Override
    public Pair<Collection, Integer> findObjects(Class example, Map formProps, int skip, int limit, String sortField,
                                                 boolean sortAscending) {
        BusinessObject businessObject = checkBusinessObjectClass(example);
        Criteria criteria = getCollectionCriteriaFromMap(businessObject, formProps);
        QueryByCriteria queryByCriteria = new QueryByCriteria(example, criteria);

        if (StringUtils.isNotBlank(sortField)) {
            if (sortAscending) {
                queryByCriteria.addOrderByAscending(sortField);
            } else {
                queryByCriteria.addOrderByDescending(sortField);
            }
        }

        skip = Math.max(skip, 0);
        queryByCriteria.setStartAtIndex(skip + 1);
        if (limit != -1) {
            queryByCriteria.setEndAtIndex(skip + limit);
        }

        Collection collection = getPersistenceBrokerTemplate().getCollectionByQuery(queryByCriteria);
        Integer count = getPersistenceBrokerTemplate().getCount(queryByCriteria);
        return Pair.of(collection, count);
    }

    @Override
    public Collection findCollectionBySearchHelper(Class businessObjectClass, Map formProps, boolean unbounded,
                                                   boolean usePrimaryKeyValuesOnly) {
        LOG.debug("findCollectionBySearchHelper() started");

        BusinessObject businessObject = checkBusinessObjectClass(businessObjectClass);
        if (usePrimaryKeyValuesOnly) {
            return executeSearch(businessObjectClass,
                    getCollectionCriteriaFromMapUsingPrimaryKeysOnly(businessObjectClass, formProps), unbounded);
        }

        Criteria crit = getCollectionCriteriaFromMap(businessObject, formProps);
        return executeSearch(businessObjectClass, crit, unbounded);
    }

    @Override
    public <T extends Object> T findObjectByMap(T example, Map<String, String> formProps) {
        LOG.debug("findObjectByMap() started");

        if (persistenceStructureService.isPersistable(example.getClass())) {
            Criteria criteria = new Criteria();

            // iterate through the parameter map for search criteria
            for (Map.Entry<String, String> formProp : formProps.entrySet()) {

                String propertyName = formProp.getKey();
                String searchValue = "";
                if (formProp.getValue() != null) {
                    searchValue = formProp.getValue();
                }

                if (StringUtils.isNotBlank(searchValue) & PropertyUtils.isWriteable(example, propertyName)) {
                    Class propertyType = ObjectUtils.getPropertyType(example, propertyName, persistenceStructureService);
                    if (TypeUtils.isIntegralClass(propertyType) || TypeUtils.isDecimalClass(propertyType)) {
                        criteria.addEqualTo(propertyName, cleanNumeric(searchValue));
                    } else if (TypeUtils.isTemporalClass(propertyType)) {
                        criteria.addEqualTo(propertyName, parseDate(ObjectUtils.clean(searchValue)));
                    } else {
                        criteria.addEqualTo(propertyName, searchValue);
                    }
                }
            }

            // execute query and return result list
            Query query = QueryFactory.newQuery(example.getClass(), criteria);
            return (T) getPersistenceBrokerTemplate().getObjectByQuery(query);
        }
        return null;
    }

    /**
     * Find count of records meeting criteria based on the object and map.
     */
    @Override
    public Long findCountByMap(Object example, Map formProps) {
        LOG.debug("findCountByMap() started");

        Criteria criteria = new Criteria();
        // iterate through the parameter map for key values search criteria
        Iterator propsIter = formProps.keySet().iterator();
        while (propsIter.hasNext()) {
            String propertyName = (String) propsIter.next();
            String searchValue = (String) formProps.get(propertyName);

            // if searchValue is empty and the key is not a valid property ignore
            if (StringUtils.isBlank(searchValue) || !(PropertyUtils.isWriteable(example, propertyName))) {
                continue;
            }

            // get property type which is used to determine type of criteria
            Class propertyType = ObjectUtils.getPropertyType(example, propertyName, persistenceStructureService);
            if (propertyType == null) {
                continue;
            }
            Boolean caseInsensitive = Boolean.TRUE;
            if (KRADServiceLocatorWeb.getDataDictionaryService().isAttributeDefined(example.getClass(), propertyName)) {
                caseInsensitive = !KRADServiceLocatorWeb.getDataDictionaryService().getAttributeForceUppercase(example.getClass(), propertyName);
            }
            if (caseInsensitive == null) {
                caseInsensitive = Boolean.TRUE;
            }

            boolean treatWildcardsAndOperatorsAsLiteral = KNSServiceLocator
                    .getBusinessObjectDictionaryService().isLookupFieldTreatWildcardsAndOperatorsAsLiteral(example.getClass(), propertyName);

            if (!caseInsensitive) {
                // Verify that the searchValue is uppercased if caseInsensitive is false
                searchValue = searchValue.toUpperCase();
            }

            // build criteria
            addCriteria(propertyName, searchValue, propertyType, caseInsensitive, treatWildcardsAndOperatorsAsLiteral, criteria);
        }

        // execute query and return result list
        Query query = QueryFactory.newQuery(example.getClass(), criteria);

        return new Long(getPersistenceBrokerTemplate().getCount(query));
    }

    @Override
    public boolean createCriteria(Object example, String searchValue, String propertyName, Object criteria) {
        LOG.debug("createCriteria() started");

        return createCriteria(example, searchValue, propertyName, false, false, criteria);
    }

    @Override
    public boolean createCriteria(Object example, String searchValue, String propertyName, boolean caseInsensitive, boolean treatWildcardsAndOperatorsAsLiteral, Object criteria) {
        LOG.debug("createCriteria() started");

        return createCriteria(example, searchValue, propertyName, caseInsensitive, treatWildcardsAndOperatorsAsLiteral, criteria, null);
    }

    /**
     * Builds up criteria object based on the object and map.
     */
    @Override
    public Criteria getCollectionCriteriaFromMap(BusinessObject example, Map formProps) {
        LOG.debug("getCollectionCriteriaFromMap() started");

        Criteria criteria = new Criteria();
        Iterator propsIter = formProps.keySet().iterator();
        while (propsIter.hasNext()) {
            String propertyName = (String) propsIter.next();
            Boolean caseInsensitive = Boolean.TRUE;
            if (KRADServiceLocatorWeb.getDataDictionaryService().isAttributeDefined(example.getClass(), propertyName)) {
                // If forceUppercase is true, both the database value and the user entry should be converted to Uppercase -- so change the caseInsensitive to false since we don't need to
                // worry about the values not matching.  However, if forceUppercase is false, make sure to do a caseInsensitive search because the database value and user entry
                // could be mixed case.  Thus, caseInsensitive will be the opposite of forceUppercase.
                caseInsensitive = !KRADServiceLocatorWeb.getDataDictionaryService().getAttributeForceUppercase(example.getClass(), propertyName);
            }
            if (caseInsensitive == null) {
                caseInsensitive = Boolean.TRUE;
            }
            boolean treatWildcardsAndOperatorsAsLiteral = KNSServiceLocator
                    .getBusinessObjectDictionaryService().isLookupFieldTreatWildcardsAndOperatorsAsLiteral(example.getClass(), propertyName);

            if (formProps.get(propertyName) instanceof Collection) {
                Iterator iter = ((Collection) formProps.get(propertyName)).iterator();
                while (iter.hasNext()) {
                    String searchValue = (String) iter.next();
                    if (!caseInsensitive) {
                        // Verify that the searchValue is uppercased if caseInsensitive is false
                        searchValue = searchValue.toUpperCase();
                    }
                    if (!createCriteria(example, searchValue, propertyName, caseInsensitive, treatWildcardsAndOperatorsAsLiteral, criteria, formProps)) {
                        throw new RuntimeException("Invalid value in Collection");
                    }
                }
            } else {
                String searchValue = (String) formProps.get(propertyName);
                if (!caseInsensitive) {
                    // Verify that the searchValue is uppercased if caseInsensitive is false
                    searchValue = searchValue.toUpperCase();
                }
                if (!createCriteria(example, searchValue, propertyName, caseInsensitive, treatWildcardsAndOperatorsAsLiteral, criteria, formProps)) {
                    continue;
                }
            }
        }
        return criteria;
    }

    protected Criteria getCollectionCriteriaFromMapUsingPrimaryKeysOnly(Class businessObjectClass, Map formProps) {
        BusinessObject businessObject = checkBusinessObjectClass(businessObjectClass);
        Criteria criteria = new Criteria();
        List pkFields = KRADServiceLocatorWeb.getDataObjectMetaDataService().listPrimaryKeyFieldNames(businessObjectClass);
        Iterator pkIter = pkFields.iterator();
        while (pkIter.hasNext()) {
            String pkFieldName = (String) pkIter.next();
            String pkValue = (String) formProps.get(pkFieldName);

            if (StringUtils.isBlank(pkValue)) {
                throw new RuntimeException("Missing pk value for field " + pkFieldName + " when a search based on PK values only is performed.");
            } else {
                for (SearchOperator op : SearchOperator.QUERY_CHARACTERS) {
                    if (pkValue.contains(op.op())) {
                        throw new RuntimeException("Value \"" + pkValue + "\" for PK field " + pkFieldName + " contains wildcard/operator characters.");
                    }
                }
            }
            boolean treatWildcardsAndOperatorsAsLiteral = KNSServiceLocator.
                    getBusinessObjectDictionaryService().isLookupFieldTreatWildcardsAndOperatorsAsLiteral(businessObjectClass, pkFieldName);
            createCriteria(businessObject, pkValue, pkFieldName, false, treatWildcardsAndOperatorsAsLiteral, criteria);
        }
        return criteria;
    }

    protected BusinessObject checkBusinessObjectClass(Class businessObjectClass) {
        if (businessObjectClass == null) {
            throw new IllegalArgumentException("BusinessObject class passed to LookupDaoOjb findCollectionBySearchHelper... method was null");
        }
        BusinessObject businessObject = null;
        try {
            businessObject = (BusinessObject) businessObjectClass.newInstance();
        } catch (IllegalAccessException e) {
            throw new RuntimeException("LookupDaoOjb could not get instance of " + businessObjectClass.getName(), e);
        } catch (InstantiationException e) {
            throw new RuntimeException("LookupDaoOjb could not get instance of " + businessObjectClass.getName(), e);
        }
        return businessObject;
    }

    protected Integer getSearchResultsLimit(Class businessObjectClass) {
        return org.kuali.kfs.kns.lookup.LookupUtils.getSearchResultsLimit(businessObjectClass);
    }

    protected int getCount(Class businessObjectClass, Criteria criteria) {
        return getPersistenceBrokerTemplate().getCount(QueryFactory.newQuery(businessObjectClass, criteria));
    }

    protected void applySearchResultsLimit(Class businessObjectClass, Criteria criteria, DatabasePlatform databasePlatform) {
        org.kuali.kfs.kns.lookup.LookupUtils.applySearchResultsLimit(businessObjectClass, criteria, databasePlatform);
    }

    protected Collection getCollectionByQuery(Class businessObjectClass, Criteria criteria) {
        return getPersistenceBrokerTemplate().getCollectionByQuery(QueryFactory.newQuery(businessObjectClass, criteria));
    }

    protected Collection executeSearch(Class businessObjectClass, Criteria criteria, boolean unbounded) {
        try {
            Long matchingResultsCount = null;
            Integer searchResultsLimit = getSearchResultsLimit(businessObjectClass);

            // A negative number in searchResultsLimit means the search results should be unlimited.
            if (!unbounded && (searchResultsLimit != null) && searchResultsLimit >= 0) {
                matchingResultsCount = new Long(getCount(businessObjectClass, criteria));
                applySearchResultsLimit(businessObjectClass, criteria, getDbPlatform());
            }
            if ((matchingResultsCount == null) || (matchingResultsCount.intValue() <= searchResultsLimit.intValue())) {
                matchingResultsCount = 0L;
            }

            Collection searchResults = getCollectionByQuery(businessObjectClass, criteria);

            List bos = new ArrayList();
            bos.addAll(searchResults);

            return new CollectionIncomplete(bos, matchingResultsCount);
        } catch (OjbOperationException | DataIntegrityViolationException e) {
            LOG.error("executeSearch() Error:", e);
            throw new RuntimeException("LookupDaoOjb encountered exception during executeSearch", e);
        }
    }

    protected boolean createCriteria(Object example, String searchValue, String propertyName, boolean caseInsensitive, boolean treatWildcardsAndOperatorsAsLiteral, Object criteria, Map searchValues) {
        // if searchValue is empty and the key is not a valid property ignore
        if (!(criteria instanceof Criteria) || StringUtils.isBlank(searchValue) || !ObjectUtils.isWriteable(example, propertyName, persistenceStructureService)) {
            return false;
        }

        // get property type which is used to determine type of criteria
        Class propertyType = ObjectUtils.getPropertyType(example, propertyName, persistenceStructureService);
        if (propertyType == null) {
            return false;
        }

        // build criteria
        if (example instanceof InactivatableFromTo) {
            if (KRADPropertyConstants.ACTIVE.equals(propertyName)) {
                addInactivateableFromToActiveCriteria(example, searchValue, (Criteria) criteria, searchValues);
            } else if (KRADPropertyConstants.CURRENT.equals(propertyName)) {
                addInactivateableFromToCurrentCriteria(example, searchValue, (Criteria) criteria, searchValues);
            } else if (!KRADPropertyConstants.ACTIVE_AS_OF_DATE.equals(propertyName)) {
                addCriteria(propertyName, searchValue, propertyType, caseInsensitive,
                        treatWildcardsAndOperatorsAsLiteral, (Criteria) criteria);
            }
        } else {
            addCriteria(propertyName, searchValue, propertyType, caseInsensitive, treatWildcardsAndOperatorsAsLiteral,
                    (Criteria) criteria);
        }

        return true;
    }

    /**
     * Adds to the criteria object based on the property type and any query characters given.
     */
    protected void addCriteria(String propertyName, String propertyValue, Class propertyType, boolean caseInsensitive, boolean treatWildcardsAndOperatorsAsLiteral, Criteria criteria) {
        if (!treatWildcardsAndOperatorsAsLiteral && StringUtils.contains(propertyValue, SearchOperator.OR.op())) {
            addOrCriteria(propertyName, propertyValue, propertyType, caseInsensitive, criteria);
            return;
        }

        if (!treatWildcardsAndOperatorsAsLiteral && StringUtils.contains(propertyValue, SearchOperator.AND.op())) {
            addAndCriteria(propertyName, propertyValue, propertyType, caseInsensitive, criteria);
            return;
        }

        if (StringUtils.equalsIgnoreCase(propertyValue, SearchOperator.NULL.op()) || StringUtils.equalsIgnoreCase(propertyValue, SearchOperator.NOT_NULL.op())) {
            // KULRICE-6846 null Lookup criteria causes sql exception
            if (StringUtils.contains(propertyValue, SearchOperator.NOT.op())) {
                criteria.addNotNull(propertyName);
            } else {
                criteria.addIsNull(propertyName);
            }
        } else if (TypeUtils.isStringClass(propertyType)) {
            // KULRICE-85 : made string searches case insensitive - used new DBPlatform function to force strings to upper case
            if (caseInsensitive) {
                propertyName = getDbPlatform().getUpperCaseFunction() + "(" + propertyName + ")";
                propertyValue = propertyValue.toUpperCase();
            }
            if (!treatWildcardsAndOperatorsAsLiteral && StringUtils.contains(propertyValue, SearchOperator.NOT.op())) {
                addNotCriteria(propertyName, propertyValue, propertyType, caseInsensitive, criteria);
            } else if (
                    !treatWildcardsAndOperatorsAsLiteral && propertyValue != null && (
                            StringUtils.contains(propertyValue, SearchOperator.BETWEEN.op())
                                    || propertyValue.startsWith(">")
                                    || propertyValue.startsWith("<"))) {
                addStringRangeCriteria(propertyName, propertyValue, criteria);
            } else {
                if (treatWildcardsAndOperatorsAsLiteral) {
                    propertyValue = StringUtils.replace(propertyValue, "*", "\\*");
                }
                criteria.addLike(propertyName, propertyValue);
            }
        } else if (TypeUtils.isIntegralClass(propertyType) || TypeUtils.isDecimalClass(propertyType)) {
            addNumericRangeCriteria(propertyName, propertyValue, treatWildcardsAndOperatorsAsLiteral, criteria);
        } else if (TypeUtils.isTemporalClass(propertyType)) {
            addDateRangeCriteria(propertyName, propertyValue, treatWildcardsAndOperatorsAsLiteral, criteria);
        } else if (TypeUtils.isBooleanClass(propertyType)) {
            criteria.addEqualTo(propertyName, ObjectUtils.clean(propertyValue));
        } else {
            LOG.error("not adding criterion for: " + propertyName + "," + propertyType + "," + propertyValue);
        }
    }

    /**
     * Translates criteria for active status to criteria on the active from and to fields
     *
     * @param example           - business object being queried on
     * @param activeSearchValue - value for the active search field, should convert to boolean
     * @param criteria          - Criteria object being built
     * @param searchValues      - Map containing all search keys and values
     */
    protected void addInactivateableFromToActiveCriteria(Object example, String activeSearchValue, Criteria criteria, Map searchValues) {
        Timestamp activeTimestamp = LookupUtils.getActiveDateTimestampForCriteria(searchValues);

        String activeBooleanStr = (String) (new OjbCharBooleanConversion()).javaToSql(activeSearchValue);
        if (OjbCharBooleanConversion.DATABASE_BOOLEAN_TRUE_STRING_REPRESENTATION.equals(activeBooleanStr)) {
            // (active from date <= date or active from date is null) and (date < active to date or active to date is null)
            Criteria criteriaBeginDate = new Criteria();
            criteriaBeginDate.addLessOrEqualThan(KRADPropertyConstants.ACTIVE_FROM_DATE, activeTimestamp);

            Criteria criteriaBeginDateNull = new Criteria();
            criteriaBeginDateNull.addIsNull(KRADPropertyConstants.ACTIVE_FROM_DATE);
            criteriaBeginDate.addOrCriteria(criteriaBeginDateNull);

            criteria.addAndCriteria(criteriaBeginDate);

            Criteria criteriaEndDate = new Criteria();
            criteriaEndDate.addGreaterThan(KRADPropertyConstants.ACTIVE_TO_DATE, activeTimestamp);

            Criteria criteriaEndDateNull = new Criteria();
            criteriaEndDateNull.addIsNull(KRADPropertyConstants.ACTIVE_TO_DATE);
            criteriaEndDate.addOrCriteria(criteriaEndDateNull);

            criteria.addAndCriteria(criteriaEndDate);
        } else if (OjbCharBooleanConversion.DATABASE_BOOLEAN_FALSE_STRING_REPRESENTATION.equals(activeBooleanStr)) {
            // (date < active from date) or (active from date is null) or (date >= active to date)
            Criteria criteriaNonActive = new Criteria();
            criteriaNonActive.addGreaterThan(KRADPropertyConstants.ACTIVE_FROM_DATE, activeTimestamp);

            Criteria criteriaEndDate = new Criteria();
            criteriaEndDate.addLessOrEqualThan(KRADPropertyConstants.ACTIVE_TO_DATE, activeTimestamp);
            criteriaNonActive.addOrCriteria(criteriaEndDate);

            criteria.addAndCriteria(criteriaNonActive);
        }
    }

    /**
     * Translates criteria for current status to criteria on the active from field
     *
     * @param example            - business object being queried on
     * @param currentSearchValue - value for the current search field, should convert to boolean
     * @param criteria           - Criteria object being built
     */
    protected void addInactivateableFromToCurrentCriteria(Object example, String currentSearchValue, Criteria criteria, Map searchValues) {
        Criteria maxBeginDateCriteria = new Criteria();

        Timestamp activeTimestamp = LookupUtils.getActiveDateTimestampForCriteria(searchValues);

        maxBeginDateCriteria.addLessOrEqualThan(KRADPropertyConstants.ACTIVE_FROM_DATE, activeTimestamp);

        List<String> groupByFieldList = dataDictionaryService.getGroupByAttributesForEffectiveDating(example
                .getClass());
        if (groupByFieldList == null) {
            return;
        }

        // join back to main query with the group by fields
        String[] groupBy = new String[groupByFieldList.size()];
        for (int i = 0; i < groupByFieldList.size(); i++) {
            String groupByField = groupByFieldList.get(i);
            groupBy[i] = groupByField;

            maxBeginDateCriteria.addEqualToField(groupByField, Criteria.PARENT_QUERY_PREFIX + groupByField);
        }

        String[] columns = new String[1];
        columns[0] = "max(" + KRADPropertyConstants.ACTIVE_FROM_DATE + ")";

        QueryByCriteria query = QueryFactory.newReportQuery(example.getClass(), columns, maxBeginDateCriteria, true);
        query.addGroupBy(groupBy);

        String currentBooleanStr = (String) (new OjbCharBooleanConversion()).javaToSql(currentSearchValue);
        if (OjbCharBooleanConversion.DATABASE_BOOLEAN_TRUE_STRING_REPRESENTATION.equals(currentBooleanStr)) {
            criteria.addIn(KRADPropertyConstants.ACTIVE_FROM_DATE, query);
        } else if (OjbCharBooleanConversion.DATABASE_BOOLEAN_FALSE_STRING_REPRESENTATION.equals(currentBooleanStr)) {
            criteria.addNotIn(KRADPropertyConstants.ACTIVE_FROM_DATE, query);
        }
    }

    /**
     * @param propertyName
     * @param propertyValue
     * @param propertyType
     * @param criteria
     */
    protected void addOrCriteria(String propertyName, String propertyValue, Class propertyType, boolean caseInsensitive, Criteria criteria) {
        addLogicalOperatorCriteria(propertyName, propertyValue, propertyType, caseInsensitive, criteria, SearchOperator.OR.op());
    }

    /**
     * @param propertyName
     * @param propertyValue
     * @param propertyType
     * @param criteria
     */
    protected void addAndCriteria(String propertyName, String propertyValue, Class propertyType, boolean caseInsensitive, Criteria criteria) {
        addLogicalOperatorCriteria(propertyName, propertyValue, propertyType, caseInsensitive, criteria, SearchOperator.AND.op());
    }

    protected void addNotCriteria(String propertyName, String propertyValue, Class propertyType, boolean caseInsensitive, Criteria criteria) {

        String[] splitPropVal = StringUtils.split(propertyValue, SearchOperator.NOT.op());

        int strLength = splitPropVal.length;
        // if more than one NOT operator assume an implicit and (i.e. !a!b = !a&!b)
        if (strLength > 1) {
            String expandedNot = SearchOperator.NOT + StringUtils.join(splitPropVal, SearchOperator.AND.op() + SearchOperator.NOT.op());
            // we know that since this method was called, treatWildcardsAndOperatorsAsLiteral must be false
            addCriteria(propertyName, expandedNot, propertyType, caseInsensitive, false, criteria);
        } else {
            // only one so add a not like
            criteria.addNotLike(propertyName, splitPropVal[0]);
        }
    }

    /**
     * Builds a sub criteria object joined with an 'AND' or 'OR' (depending on splitValue) using the split values of propertyValue. Then joins back the
     * sub criteria to the main criteria using an 'AND'.
     */
    protected void addLogicalOperatorCriteria(String propertyName, String propertyValue, Class propertyType, boolean caseInsensitive, Criteria criteria, String splitValue) {
        String[] splitPropVal = StringUtils.split(propertyValue, splitValue);

        Criteria subCriteria = new Criteria();
        for (int i = 0; i < splitPropVal.length; i++) {
            Criteria predicate = new Criteria();

            addCriteria(propertyName, splitPropVal[i], propertyType, caseInsensitive, false, predicate);
            if (splitValue.equals(SearchOperator.OR.op())) {
                subCriteria.addOrCriteria(predicate);
            }
            if (splitValue.equals(SearchOperator.AND.op())) {
                subCriteria.addAndCriteria(predicate);
            }
        }

        criteria.addAndCriteria(subCriteria);
    }

    protected java.sql.Date parseDate(String dateString) {
        dateString = dateString.trim();
        try {
            return dateTimeService.convertToSqlDate(dateString);
        } catch (ParseException ex) {
            return null;
        }
    }

    /**
     * Adds to the criteria object based on query characters given
     */
    protected void addDateRangeCriteria(String propertyName, String propertyValue, boolean treatWildcardsAndOperatorsAsLiteral, Criteria criteria) {
        if (StringUtils.contains(propertyValue, SearchOperator.BETWEEN.op())) {
            if (treatWildcardsAndOperatorsAsLiteral) {
                throw new RuntimeException("Wildcards and operators are not allowed on this date field: " + propertyName);
            }
            String[] rangeValues = StringUtils.split(propertyValue, SearchOperator.BETWEEN.op());
            criteria.addBetween(propertyName, parseDate(ObjectUtils.clean(rangeValues[0])), parseDate(ObjectUtils.clean(rangeValues[1])));
        } else if (propertyValue.startsWith(SearchOperator.GREATER_THAN_EQUAL.op())) {
            if (treatWildcardsAndOperatorsAsLiteral) {
                throw new RuntimeException("Wildcards and operators are not allowed on this date field: " + propertyName);
            }
            criteria.addGreaterOrEqualThan(propertyName, parseDate(ObjectUtils.clean(propertyValue)));
        } else if (propertyValue.startsWith(SearchOperator.LESS_THAN_EQUAL.op())) {
            if (treatWildcardsAndOperatorsAsLiteral) {
                throw new RuntimeException("Wildcards and operators are not allowed on this date field: " + propertyName);
            }
            criteria.addLessOrEqualThan(propertyName, parseDate(ObjectUtils.clean(propertyValue)));
        } else if (propertyValue.startsWith(SearchOperator.GREATER_THAN.op())) {
            if (treatWildcardsAndOperatorsAsLiteral) {
                throw new RuntimeException("Wildcards and operators are not allowed on this date field: " + propertyName);
            }
            criteria.addGreaterThan(propertyName, parseDate(ObjectUtils.clean(propertyValue)));
        } else if (propertyValue.startsWith(SearchOperator.LESS_THAN.op())) {
            if (treatWildcardsAndOperatorsAsLiteral) {
                throw new RuntimeException("Wildcards and operators are not allowed on this date field: " + propertyName);
            }
            criteria.addLessThan(propertyName, parseDate(ObjectUtils.clean(propertyValue)));
        } else {
            criteria.addEqualTo(propertyName, parseDate(ObjectUtils.clean(propertyValue)));
        }
    }

    protected BigDecimal cleanNumeric(String value) {
        String cleanedValue = value;
        // ensure only one "minus" at the beginning, if any
        if (cleanedValue.lastIndexOf('-') > 0) {
            if (cleanedValue.charAt(0) == '-') {
                cleanedValue = "-" + cleanedValue.replaceAll("-", "");
            } else {
                cleanedValue = cleanedValue.replaceAll("-", "");
            }
        }
        // ensure only one decimal in the string
        int decimalLoc = cleanedValue.lastIndexOf('.');
        if (cleanedValue.indexOf('.') != decimalLoc) {
            cleanedValue = cleanedValue.substring(0, decimalLoc).replaceAll("\\.", "") + cleanedValue.substring(
                    decimalLoc);
        }
        try {
            return new BigDecimal(cleanedValue);
        } catch (NumberFormatException ex) {
            GlobalVariables.getMessageMap().putError(KRADConstants.DOCUMENT_ERRORS, RiceKeyConstants.ERROR_CUSTOM,
                    new String[]{"Invalid Numeric Input: " + value});
            return null;
        }
    }

    /**
     * Adds to the criteria object based on query characters given
     */
    protected void addNumericRangeCriteria(String propertyName, String propertyValue, boolean treatWildcardsAndOperatorsAsLiteral, Criteria criteria) {
        if (StringUtils.contains(propertyValue, SearchOperator.BETWEEN.op())) {
            if (treatWildcardsAndOperatorsAsLiteral) {
                throw new RuntimeException("Cannot use wildcards and operators on numeric field " + propertyName);
            }
            String[] rangeValues = StringUtils.split(propertyValue, SearchOperator.BETWEEN.op());
            criteria.addBetween(propertyName, cleanNumeric(rangeValues[0]), cleanNumeric(rangeValues[1]));
        } else if (propertyValue.startsWith(SearchOperator.GREATER_THAN_EQUAL.op())) {
            if (treatWildcardsAndOperatorsAsLiteral) {
                throw new RuntimeException("Cannot use wildcards and operators on numeric field " + propertyName);
            }
            criteria.addGreaterOrEqualThan(propertyName, cleanNumeric(stripNumericRangeOperator(propertyValue, SearchOperator.GREATER_THAN_EQUAL)));
        } else if (propertyValue.startsWith(SearchOperator.LESS_THAN_EQUAL.op())) {
            if (treatWildcardsAndOperatorsAsLiteral) {
                throw new RuntimeException("Cannot use wildcards and operators on numeric field " + propertyName);
            }
            criteria.addLessOrEqualThan(propertyName, cleanNumeric(stripNumericRangeOperator(propertyValue, SearchOperator.LESS_THAN_EQUAL)));
        } else if (propertyValue.startsWith(SearchOperator.GREATER_THAN.op())) {
            if (treatWildcardsAndOperatorsAsLiteral) {
                throw new RuntimeException("Cannot use wildcards and operators on numeric field " + propertyName);
            }
            criteria.addGreaterThan(propertyName, cleanNumeric(stripNumericRangeOperator(propertyValue, SearchOperator.GREATER_THAN)));
        } else if (propertyValue.startsWith(SearchOperator.LESS_THAN.op())) {
            if (treatWildcardsAndOperatorsAsLiteral) {
                throw new RuntimeException("Cannot use wildcards and operators on numeric field " + propertyName);
            }
            criteria.addLessThan(propertyName, cleanNumeric(stripNumericRangeOperator(propertyValue, SearchOperator.LESS_THAN)));
        } else {
            criteria.addEqualTo(propertyName, cleanNumeric(propertyValue));
        }
    }

    /**
     * Strip the operator from the value
     *
     * @param propertyValue
     * @param operator
     * @return
     */
    protected String stripNumericRangeOperator(String propertyValue, SearchOperator operator) {
        String propertyValueWithoutOperator = StringUtils.replace(propertyValue, operator.op(), org.kuali.kfs.krad.util.KRADConstants.EMPTY_STRING);
        return propertyValueWithoutOperator;
    }

    /**
     * Adds to the criteria object based on query characters given
     */
    protected void addStringRangeCriteria(String propertyName, String propertyValue, Criteria criteria) {

        if (StringUtils.contains(propertyValue, SearchOperator.BETWEEN.op())) {
            String[] rangeValues = StringUtils.split(propertyValue, SearchOperator.BETWEEN.op());
            criteria.addBetween(propertyName, rangeValues[0], rangeValues[1]);
        } else if (propertyValue.startsWith(SearchOperator.GREATER_THAN_EQUAL.op())) {
            criteria.addGreaterOrEqualThan(propertyName, ObjectUtils.clean(propertyValue));
        } else if (propertyValue.startsWith(SearchOperator.LESS_THAN_EQUAL.op())) {
            criteria.addLessOrEqualThan(propertyName, ObjectUtils.clean(propertyValue));
        } else if (propertyValue.startsWith(SearchOperator.GREATER_THAN.op())) {
            criteria.addGreaterThan(propertyName, ObjectUtils.clean(propertyValue));
        } else if (propertyValue.startsWith(SearchOperator.LESS_THAN.op())) {
            criteria.addLessThan(propertyName, ObjectUtils.clean(propertyValue));
        } else {
            criteria.addEqualTo(propertyName, ObjectUtils.clean(propertyValue));
        }
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setPersistenceStructureService(PersistenceStructureService persistenceStructureService) {
        this.persistenceStructureService = persistenceStructureService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }
}
