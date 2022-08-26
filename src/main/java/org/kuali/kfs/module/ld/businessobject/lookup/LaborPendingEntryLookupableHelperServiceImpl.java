/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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

import org.kuali.kfs.gl.Constant;
import org.kuali.kfs.gl.businessobject.inquiry.EntryInquirableImpl;
import org.kuali.kfs.gl.businessobject.inquiry.InquirableFinancialDocument;
import org.kuali.kfs.integration.ld.businessobject.inquiry.AbstractPositionDataDetailsInquirableImpl;
import org.kuali.kfs.kns.lookup.AbstractLookupableHelperServiceImpl;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.kfs.kns.lookup.CollectionIncomplete;
import org.kuali.kfs.krad.util.BeanPropertyComparator;
import org.kuali.kfs.module.ld.businessobject.LaborLedgerPendingEntry;
import org.kuali.kfs.module.ld.businessobject.inquiry.LedgerPendingEntryInquirableImpl;
import org.kuali.kfs.module.ld.businessobject.inquiry.PositionDataDetailsInquirableImpl;
import org.kuali.kfs.module.ld.service.LaborInquiryOptionsService;
import org.kuali.kfs.module.ld.service.LaborLedgerPendingEntryService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.krad.bo.BusinessObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper Service for looking up instances of {@link LaborLedgerPendingEntry}
 */
/*
 * Cornell customization - backport FINP-8068. This can be removed when we upgrade to the 2/2/2022 version of financials.
 */
public class LaborPendingEntryLookupableHelperServiceImpl extends AbstractLookupableHelperServiceImpl {

    private LaborLedgerPendingEntryService laborLedgerPendingEntryService;
    private LaborInquiryOptionsService laborInquiryOptionsService;

    @Override
    public HtmlData getInquiryUrl(BusinessObject businessObject, String propertyName) {
        if (KFSPropertyConstants.DOCUMENT_NUMBER.equals(propertyName)
                && businessObject instanceof LaborLedgerPendingEntry) {
            LaborLedgerPendingEntry pendingEntry = (LaborLedgerPendingEntry) businessObject;
            return new AnchorHtmlData(new InquirableFinancialDocument().getInquirableDocumentUrl(pendingEntry),
                    KFSConstants.EMPTY_STRING);
        } else if (KFSPropertyConstants.POSITION_NUMBER.equals(propertyName)) {
            LaborLedgerPendingEntry pendingEntry = (LaborLedgerPendingEntry) businessObject;
            AbstractPositionDataDetailsInquirableImpl positionDataDetailsInquirable =
                    new PositionDataDetailsInquirableImpl();

            Map<String, String> fieldValues = new HashMap<>();
            fieldValues.put(propertyName, pendingEntry.getPositionNumber());

            BusinessObject positionData = positionDataDetailsInquirable.getBusinessObject(fieldValues);

            return positionData == null ? new AnchorHtmlData(KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING) :
                    positionDataDetailsInquirable.getInquiryUrl(positionData, propertyName);
        } 
        // Cornell customization - backport FINP-8068. 
        else if (KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE.equals(propertyName)) {
            // The LedgerPendingEntryInquirableImpl isn't capable of generating an inquiry url for a doc type, it
            // throws an exception instead, so we'll delegate to EntryInquirableImpl instead since it can generate
            // an inquiry url for a doc type just fine.
            return new EntryInquirableImpl().getInquiryUrl(businessObject, propertyName);
        }
        return new LedgerPendingEntryInquirableImpl().getInquiryUrl(businessObject, propertyName);
    }

    @Override
    public List<? extends BusinessObject> getSearchResults(Map<String, String> fieldValues) {
        setBackLocation(fieldValues.get(KFSConstants.BACK_LOCATION));
        setDocFormKey(fieldValues.get(KFSConstants.DOC_FORM_KEY));

        // determine if only approved pending entries need to be returned
        String pendingEntryOption = laborInquiryOptionsService.getSelectedPendingEntryOption(fieldValues);
        boolean isApprovedPendingSelected = Constant.APPROVED_PENDING_ENTRY.equals(pendingEntryOption);

        Collection<LaborLedgerPendingEntry> searchResults = laborLedgerPendingEntryService.findPendingEntries(
                fieldValues, isApprovedPendingSelected);
        Long resultSize = searchResults == null ? 0 : (long) searchResults.size();

        return this.buildSearchResultList(searchResults, resultSize);
    }

    /**
     * build the search result list from the given collection and the number of all qualified search results
     *
     * @param searchResultsCollection the given search results, which may be a subset of the qualified search results
     * @param actualSize              the number of all qualified search results
     * @return the search result list with the given results and actual size
     */
    protected List buildSearchResultList(Collection searchResultsCollection, Long actualSize) {
        CollectionIncomplete results = new CollectionIncomplete(searchResultsCollection, actualSize);

        // sort list if default sort column given
        List searchResults = results;
        List<String> defaultSortColumns = getDefaultSortColumns();
        if (defaultSortColumns.size() > 0) {
            results.sort(new BeanPropertyComparator(defaultSortColumns, true));
        }
        return searchResults;
    }

    public void setLaborLedgerPendingEntryService(LaborLedgerPendingEntryService laborLedgerPendingEntryService) {
        this.laborLedgerPendingEntryService = laborLedgerPendingEntryService;
    }

    public void setLaborInquiryOptionsService(LaborInquiryOptionsService laborInquiryOptionsService) {
        this.laborInquiryOptionsService = laborInquiryOptionsService;
    }
}
