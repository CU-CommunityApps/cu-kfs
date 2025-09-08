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
package org.kuali.kfs.module.ld.businessobject.lookup;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.service.BalanceTypeService;
import org.kuali.kfs.gl.Constant;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.OJBUtility;
import org.kuali.kfs.gl.businessobject.lookup.BalanceLookupableHelperServiceImpl;
import org.kuali.kfs.module.ld.businessobject.inquiry.AbstractPositionDataDetailsInquirableImpl;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.kfs.kns.lookup.CollectionIncomplete;
import org.kuali.kfs.krad.util.BeanPropertyComparator;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.module.ld.LaborConstants;
import org.kuali.kfs.module.ld.businessobject.LedgerBalance;
import org.kuali.kfs.module.ld.businessobject.inquiry.LedgerBalanceInquirableImpl;
import org.kuali.kfs.module.ld.businessobject.inquiry.PositionDataDetailsInquirableImpl;
import org.kuali.kfs.module.ld.service.LaborInquiryOptionsService;
import org.kuali.kfs.module.ld.service.LaborLedgerBalanceService;
import org.kuali.kfs.module.ld.util.ConsolidationUtil;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.core.api.search.SearchOperator;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.BusinessObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * ====
 * CU Customization: Backported the FINP-6108 changes.
 *                   This file can be removed when we upgrade to the 2024-05-08 financials patch.
 * ====
 * 
 * Service implementation of LedgerBalanceLookupableHelperService. The class is the front-end for all Ledger balance
 * inquiry processing.
 */
public class LedgerBalanceLookupableHelperServiceImpl extends BalanceLookupableHelperServiceImpl {

    private static final Logger LOG = LogManager.getLogger();

    LaborLedgerBalanceService balanceService;
    private LaborInquiryOptionsService laborInquiryOptionsService;
    protected BalanceTypeService balanceTypService;

    @Override
    public HtmlData getInquiryUrl(final BusinessObject bo, final String propertyName) {
        if (KFSPropertyConstants.POSITION_NUMBER.equals(propertyName)) {
            final LedgerBalance balance = (LedgerBalance) bo;
            final AbstractPositionDataDetailsInquirableImpl positionDataDetailsInquirable =
                    new PositionDataDetailsInquirableImpl();

            final Map<String, String> fieldValues = new HashMap<>();
            fieldValues.put(propertyName, balance.getPositionNumber());

            final BusinessObject positionData = positionDataDetailsInquirable.getBusinessObject(fieldValues);

            return positionData == null ? new AnchorHtmlData(KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING) :
                    positionDataDetailsInquirable.getInquiryUrl(positionData, propertyName);
        }
        return new LedgerBalanceInquirableImpl().getInquiryUrl(bo, propertyName);
    }

    @Override
    public List<? extends BusinessObject> getSearchResults(final Map<String, String> fieldValues) {
        String wildCards = "";
        for (final SearchOperator op : SearchOperator.QUERY_CHARACTERS) {
            wildCards += op.op();
        }

        if (wildCards.contains(fieldValues.get(KFSPropertyConstants.EMPLID).trim())) {
            final List emptySearchResults = new ArrayList<>();
            final Long actualCountIfTruncated = 0L;
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.EMPLID,
                    KFSConstants.WILDCARD_NOT_ALLOWED_ON_FIELD, "Employee ID field ");
            return new CollectionIncomplete(emptySearchResults, actualCountIfTruncated);
        }

        setBackLocation(fieldValues.get(KFSConstants.BACK_LOCATION));
        setDocFormKey(fieldValues.get(KFSConstants.DOC_FORM_KEY));

        // get the pending entry option. This method must be prior to the get search results
        final String pendingEntryOption = laborInquiryOptionsService.getSelectedPendingEntryOption(fieldValues);

        // get the cgBeginningBalanceExcludeOption
        final boolean isCgBeginningBalanceExcluded = laborInquiryOptionsService.isCgBeginningBalanceOnlyExcluded(fieldValues);

        // test if the consolidation option is selected or not
        // ==== CU Customization: Backport FINP-6108 changes. ====
        final String consolidationOption = fieldValues.get(GeneralLedgerConstants.DummyBusinessObject.CONSOLIDATION_OPTION);
        boolean isConsolidated = laborInquiryOptionsService.isConsolidationSelected(fieldValues);

        if (consolidationOption.equals(Constant.EXCLUDE_SUBACCOUNTS)) {
            fieldValues.put(Constant.SUB_ACCOUNT_OPTION, KFSConstants.getDashSubAccountNumber());
            isConsolidated = false;
        }
        // === End CU Customization ====

        // get Amount View Option and determine if the results has to be accumulated
        final String amountViewOption = getSelectedAmountViewOption(fieldValues);
        final boolean isAccumulated = amountViewOption.equals(Constant.ACCUMULATE);

        // get the input balance type code
        final String balanceTypeCode = fieldValues.get(KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE);
        final boolean isA21Balance = StringUtils.isNotEmpty(balanceTypeCode)
                                     && LaborConstants.BalanceInquiries.BALANCE_TYPE_AC_AND_A21.equals(balanceTypeCode.trim());

        // get the ledger balances with actual balance type code
        if (isA21Balance) {
            fieldValues.put(KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE, KFSConstants.BALANCE_TYPE_ACTUAL);
        }
        final Integer recordCountForActualBalance = balanceService.getBalanceRecordCount(fieldValues, isConsolidated,
                getEncumbranceBalanceTypes(fieldValues), false);
        final Iterator actualBalanceIterator = balanceService.findBalance(fieldValues, isConsolidated,
                getEncumbranceBalanceTypes(fieldValues), false);
        Collection searchResultsCollection = buildBalanceCollection(actualBalanceIterator, isConsolidated,
                pendingEntryOption);
        laborInquiryOptionsService.updateLedgerBalanceByPendingLedgerEntry(searchResultsCollection, fieldValues,
                pendingEntryOption, isConsolidated);

        // get the search result collection
        Integer recordCountForEffortBalance = 0;
        if (isA21Balance) {
            fieldValues.put(KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE, KFSConstants.BALANCE_TYPE_A21);
            recordCountForEffortBalance = balanceService.getBalanceRecordCount(fieldValues, isConsolidated,
                    getEncumbranceBalanceTypes(fieldValues), false);

            final Iterator effortBalanceIterator = balanceService.findBalance(fieldValues, isConsolidated,
                    getEncumbranceBalanceTypes(fieldValues), false);
            final Collection effortBalances = buildBalanceCollection(effortBalanceIterator, isConsolidated,
                    pendingEntryOption);
            laborInquiryOptionsService.updateLedgerBalanceByPendingLedgerEntry(effortBalances, fieldValues,
                    pendingEntryOption, isConsolidated);

            final List<String> consolidationKeyList = LedgerBalance.getPrimaryKeyList();
            searchResultsCollection = ConsolidationUtil.consolidateA2Balances(searchResultsCollection, effortBalances,
                    LaborConstants.BalanceInquiries.BALANCE_TYPE_AC_AND_A21, consolidationKeyList);
        }

        // filter out rows with all amounts zero except CG beginning balance if cgBeginningBalanceExcludeOption is checked.
        // Note: this has to be done before accumulate, because accumulate adds up CG amount into monthly amounts.
        if (isCgBeginningBalanceExcluded) {
            searchResultsCollection = filterOutCGBeginningBalanceOnlyRows(searchResultsCollection);
        }

        // perform the accumulation of the amounts
        accumulate(searchResultsCollection, isAccumulated);

        // get the actual size of all qualified search results
        final Integer recordCount = recordCountForActualBalance + recordCountForEffortBalance;
        final Long actualSize = OJBUtility.getResultActualSize(searchResultsCollection, recordCount, fieldValues,
                new LedgerBalance());

        return buildSearchResultList(searchResultsCollection, actualSize);
    }

    /**
     * Filter out rows with all amounts zero except CG beginning balance from the given searchResultsCollection.
     */
    protected Collection<LedgerBalance> filterOutCGBeginningBalanceOnlyRows(
            final Collection<LedgerBalance> searchResultsCollection) {
        final Collection<LedgerBalance> filteredSearchResults = new ArrayList<>();
        for (final LedgerBalance balance : searchResultsCollection) {
            if (!balance.isCGBeginningBalanceOnly()) {
                filteredSearchResults.add(balance);
            }
        }
        return filteredSearchResults;
    }

    /**
     * This method builds the balance collection based on the input iterator
     *
     * @param iterator           the iterator of search results of balance
     * @param isConsolidated     determine if the consolidated result is desired
     * @param pendingEntryOption the given pending entry option that can be no, approved or all
     * @return the balance collection
     */
    protected Collection buildBalanceCollection(final Iterator iterator, final boolean isConsolidated, final String pendingEntryOption) {
        final Collection balanceCollection;

        if (isConsolidated) {
            balanceCollection = buildConsolidatedBalanceCollection(iterator, pendingEntryOption);
        } else {
            balanceCollection = buildDetailedBalanceCollection(iterator, pendingEntryOption);
        }
        return balanceCollection;
    }

    /**
     * This method builds the balance collection with consolidation option from an iterator
     *
     * @param iterator
     * @param pendingEntryOption the selected pending entry option
     * @return the consolidated balance collection
     */
    protected Collection buildConsolidatedBalanceCollection(final Iterator iterator, final String pendingEntryOption) {
        final Collection<LedgerBalance> balanceCollection = new ArrayList<>();

        while (iterator.hasNext()) {
            final Object collectionEntry = iterator.next();

            if (collectionEntry.getClass().isArray()) {
                int i = 0;
                final Object[] array = (Object[]) collectionEntry;
                LedgerBalance balance = new LedgerBalance();

                if (LedgerBalance.class.isAssignableFrom(getBusinessObjectClass())) {
                    try {
                        balance = (LedgerBalance) getBusinessObjectClass().newInstance();
                    } catch (final Exception e) {
                        LOG.warn(
                                "Using {} for results because I couldn't instantiate the {}",
                                () -> LedgerBalance.class,
                                this::getBusinessObjectClass
                        );
                    }
                } else {
                    LOG.warn(
                            "Using {} for results because I couldn't instantiate the {}",
                            () -> LedgerBalance.class,
                            this::getBusinessObjectClass
                    );
                }

                balance.setUniversityFiscalYear(Integer.valueOf(array[i++].toString()));
                balance.setChartOfAccountsCode(array[i++].toString());
                balance.setAccountNumber(array[i++].toString());

                final String subAccountNumber = Constant.CONSOLIDATED_SUB_ACCOUNT_NUMBER;
                balance.setSubAccountNumber(subAccountNumber);

                balance.setBalanceTypeCode(array[i++].toString());
                balance.setFinancialObjectCode(array[i++].toString());

                balance.setEmplid(array[i++].toString());
                balance.setPositionNumber(array[i++].toString());

                balance.setFinancialSubObjectCode(Constant.CONSOLIDATED_SUB_OBJECT_CODE);
                balance.setFinancialObjectTypeCode(Constant.CONSOLIDATED_OBJECT_TYPE_CODE);

                balance.setAccountLineAnnualBalanceAmount(new KualiDecimal(array[i++].toString()));
                balance.setBeginningBalanceLineAmount(new KualiDecimal(array[i++].toString()));
                balance.setContractsGrantsBeginningBalanceAmount(new KualiDecimal(array[i++].toString()));

                balance.setMonth1Amount(new KualiDecimal(array[i++].toString()));
                balance.setMonth2Amount(new KualiDecimal(array[i++].toString()));
                balance.setMonth3Amount(new KualiDecimal(array[i++].toString()));
                balance.setMonth4Amount(new KualiDecimal(array[i++].toString()));
                balance.setMonth5Amount(new KualiDecimal(array[i++].toString()));
                balance.setMonth6Amount(new KualiDecimal(array[i++].toString()));
                balance.setMonth7Amount(new KualiDecimal(array[i++].toString()));
                balance.setMonth8Amount(new KualiDecimal(array[i++].toString()));
                balance.setMonth9Amount(new KualiDecimal(array[i++].toString()));

                balance.setMonth10Amount(new KualiDecimal(array[i++].toString()));
                balance.setMonth11Amount(new KualiDecimal(array[i++].toString()));
                balance.setMonth12Amount(new KualiDecimal(array[i++].toString()));
                balance.setMonth13Amount(new KualiDecimal(array[i].toString()));

                balance.getDummyBusinessObject().setPendingEntryOption(pendingEntryOption);
                balance.getDummyBusinessObject().setConsolidationOption(Constant.CONSOLIDATION);

                balanceCollection.add(balance);
            }
        }
        return balanceCollection;
    }

    /**
     * This method builds the balance collection with detail option from an iterator
     *
     * @param iterator           the balance iterator
     * @param pendingEntryOption the selected pending entry option
     * @return the detailed balance collection
     */
    protected Collection buildDetailedBalanceCollection(final Iterator iterator, final String pendingEntryOption) {
        final Collection<LedgerBalance> balanceCollection = new ArrayList<>();

        while (iterator.hasNext()) {
            final LedgerBalance copyBalance = (LedgerBalance) iterator.next();

            LedgerBalance balance = new LedgerBalance();
            if (LedgerBalance.class.isAssignableFrom(getBusinessObjectClass())) {
                try {
                    balance = (LedgerBalance) getBusinessObjectClass().newInstance();
                } catch (final Exception e) {
                    LOG.warn(
                            "Using {} for results because I couldn't instantiate the {}",
                            () -> LedgerBalance.class,
                            this::getBusinessObjectClass
                    );
                }
            } else {
                LOG.warn(
                        "Using {} for results because I couldn't instantiate the {}",
                        () -> LedgerBalance.class,
                        this::getBusinessObjectClass
                );
            }

            balance.setUniversityFiscalYear(copyBalance.getUniversityFiscalYear());
            balance.setChartOfAccountsCode(copyBalance.getChartOfAccountsCode());
            balance.setAccountNumber(copyBalance.getAccountNumber());
            balance.setSubAccountNumber(copyBalance.getSubAccountNumber());
            balance.setBalanceTypeCode(copyBalance.getBalanceTypeCode());
            balance.setFinancialObjectCode(copyBalance.getFinancialObjectCode());
            balance.setEmplid(copyBalance.getEmplid());
            balance.setObjectId(copyBalance.getObjectId());
            balance.setPositionNumber(copyBalance.getPositionNumber());
            balance.setFinancialSubObjectCode(copyBalance.getFinancialSubObjectCode());
            balance.setFinancialObjectTypeCode(copyBalance.getFinancialObjectTypeCode());
            balance.setAccountLineAnnualBalanceAmount(copyBalance.getAccountLineAnnualBalanceAmount());
            balance.setBeginningBalanceLineAmount(copyBalance.getBeginningBalanceLineAmount());
            balance.setContractsGrantsBeginningBalanceAmount(copyBalance.getContractsGrantsBeginningBalanceAmount());
            balance.setMonth1Amount(copyBalance.getMonth1Amount());
            balance.setMonth2Amount(copyBalance.getMonth2Amount());
            balance.setMonth3Amount(copyBalance.getMonth3Amount());
            balance.setMonth4Amount(copyBalance.getMonth4Amount());
            balance.setMonth5Amount(copyBalance.getMonth5Amount());
            balance.setMonth6Amount(copyBalance.getMonth6Amount());
            balance.setMonth7Amount(copyBalance.getMonth7Amount());
            balance.setMonth8Amount(copyBalance.getMonth8Amount());
            balance.setMonth9Amount(copyBalance.getMonth9Amount());
            balance.setMonth10Amount(copyBalance.getMonth10Amount());
            balance.setMonth11Amount(copyBalance.getMonth11Amount());
            balance.setMonth12Amount(copyBalance.getMonth12Amount());
            balance.setMonth13Amount(copyBalance.getMonth13Amount());

            balance.getDummyBusinessObject().setPendingEntryOption(pendingEntryOption);
            balance.getDummyBusinessObject().setConsolidationOption(Constant.DETAIL);

            balanceCollection.add(balance);
        }
        return balanceCollection;
    }

    /**
     * build the search result list from the given collection and the number of all qualified search results
     *
     * @param searchResultsCollection the given search results, which may be a subset of the qualified search results
     * @param actualSize              the number of all qualified search results
     * @return the search result list with the given results and actual size
     */
    @Override
    protected List buildSearchResultList(final Collection searchResultsCollection, final Long actualSize) {
        final CollectionIncomplete results = new CollectionIncomplete(searchResultsCollection, actualSize);

        // sort list if default sort column given
        final List searchResults = results;
        final List<String> defaultSortColumns = getDefaultSortColumns();
        if (defaultSortColumns.size() > 0) {
            results.sort(new BeanPropertyComparator(defaultSortColumns, true));
        }
        return searchResults;
    }

    protected List<String> getEncumbranceBalanceTypes(final Map<String, String> fieldValues) {
        List<String> encumbranceBalanceTypes = new ArrayList<>();

        if (fieldValues.containsKey(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR)) {
            // parse the university fiscal year since it's a required field from the lookups
            final String universityFiscalYearStr = fieldValues.get(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR);
            final Integer universityFiscalYear = Integer.valueOf(universityFiscalYearStr);
            encumbranceBalanceTypes = balanceTypService.getEncumbranceBalanceTypes(universityFiscalYear);
        }

        return encumbranceBalanceTypes;
    }

    public void setLaborInquiryOptionsService(final LaborInquiryOptionsService laborInquiryOptionsService) {
        this.laborInquiryOptionsService = laborInquiryOptionsService;
    }

    public void setBalanceService(final LaborLedgerBalanceService balanceService) {
        this.balanceService = balanceService;
    }

    public void setBalanceTypService(final BalanceTypeService balanceTypService) {
        this.balanceTypService = balanceTypService;
    }
}
