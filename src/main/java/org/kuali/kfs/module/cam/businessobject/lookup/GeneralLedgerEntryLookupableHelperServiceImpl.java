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
package org.kuali.kfs.module.cam.businessobject.lookup;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.search.SearchOperator;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.kns.lookup.CollectionIncomplete;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.kfs.kns.lookup.KualiLookupableHelperServiceImpl;
import org.kuali.kfs.kns.lookup.LookupUtils;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.businessobject.GeneralLedgerEntry;
import org.kuali.kfs.module.cam.businessobject.GeneralLedgerEntryAsset;
import org.kuali.kfs.module.cam.businessobject.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.sys.KFSConstants;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ====
 * CU Customization: Force the search results to include PREQ sub-type entries (such as PRNC entries)
 * that were processed prior to our upgrade to the 2024-07-31 version of financials.
 * ====
 * 
 * This class overrides the base getActionUrls method
 */
public class GeneralLedgerEntryLookupableHelperServiceImpl extends KualiLookupableHelperServiceImpl {

    private PermissionService permissionService;

    // CU Customization: Add new fields.
    private DateTimeService dateTimeService;
    private String preqCutoffDate;

    @Override
    public List<HtmlData> getCustomActionUrls(final BusinessObject bo, final List pkNames) {
        final Map<String, String> permissionDetails = new HashMap<>();
        permissionDetails.put(KimConstants.AttributeConstants.NAMESPACE_CODE, "KFS-CAM");
        permissionDetails.put(KimConstants.AttributeConstants.ACTION_CLASS,
                "org.kuali.kfs.module.cam.web.struts.CapitalAssetInformationAction");

        if (!permissionService.hasPermissionByTemplate(GlobalVariables.getUserSession().getPrincipalId(),
                KFSConstants.CoreModuleNamespaces.KFS, KimConstants.PermissionTemplateNames.USE_SCREEN, permissionDetails)) {
            return super.getEmptyActionUrls();
        }

        final GeneralLedgerEntry entry = (GeneralLedgerEntry) bo;
        final List<HtmlData> anchorHtmlDataList = new ArrayList<>();
        if (entry.isActive()) {
            final AnchorHtmlData processLink = new AnchorHtmlData("camsCapitalAssetInformation.do?methodToCall=process&" +
                                                                  CamsPropertyConstants.GeneralLedgerEntry.GENERAL_LEDGER_ACCOUNT_IDENTIFIER + "=" +
                                                                  entry.getGeneralLedgerAccountIdentifier(), "process", "process");
            processLink.setTarget(entry.getGeneralLedgerAccountIdentifier().toString());
            anchorHtmlDataList.add(processLink);
        } else {
            final List<GeneralLedgerEntryAsset> generalLedgerEntryAssets = entry.getGeneralLedgerEntryAssets();
            if (!generalLedgerEntryAssets.isEmpty()) {
                for (final GeneralLedgerEntryAsset generalLedgerEntryAsset : generalLedgerEntryAssets) {
                    final AnchorHtmlData viewDocLink = new AnchorHtmlData(
                            "camsCapitalAssetInformation.do?methodToCall=viewDoc&" + "documentNumber" + "=" +
                                    generalLedgerEntryAsset.getCapitalAssetManagementDocumentNumber(), "viewDoc",
                            generalLedgerEntryAsset.getCapitalAssetManagementDocumentNumber());
                    viewDocLink.setTarget(generalLedgerEntryAssets.get(0).getCapitalAssetManagementDocumentNumber());
                    anchorHtmlDataList.add(viewDocLink);
                }
            } else {
                anchorHtmlDataList.add(new AnchorHtmlData("", "n/a", "n/a"));
            }
        }
        return anchorHtmlDataList;
    }

    /**
     * This method will remove all PO related transactions from display on GL results
     */
    @Override
    public List<? extends BusinessObject> getSearchResults(final Map<String, String> fieldValues) {
        // update status code from user input value to DB value.
        updateStatusCodeCriteria(fieldValues);

        final List<? extends BusinessObject> searchResults = super.getSearchResults(fieldValues);
        if (searchResults == null || searchResults.isEmpty()) {
            return searchResults;
        }
        final Integer searchResultsLimit = LookupUtils.getSearchResultsLimit(GeneralLedgerEntry.class);
        long matchingResultsCount;
        // CU Customization: Add variable for holding the PREQ sub-type cutoff date.
        final LocalDate cutoffDate = parsePreqCutoffDate();
        final List<GeneralLedgerEntry> newList = new ArrayList<>();
        for (final BusinessObject businessObject : searchResults) {
            final GeneralLedgerEntry entry = (GeneralLedgerEntry) businessObject;
            // CU Customization: Use a CU-specific helper method to determine whether to include the entry.
            if (isTentativelyEligibleToIncludeInSearchResults(entry, cutoffDate)) {
                if (!PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT
                        .equals(entry.getFinancialDocumentTypeCode())) {
                    newList.add(entry);
                } else if (PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT
                        .equals(entry.getFinancialDocumentTypeCode())) {
                    final Map<String, String> cmKeys = new HashMap<>();
                    cmKeys.put(CamsPropertyConstants.PurchasingAccountsPayableDocument.DOCUMENT_NUMBER,
                            entry.getDocumentNumber());
                    // check if CAB PO document exists, if not included
                    final Collection<PurchasingAccountsPayableDocument> matchingCreditMemos =
                            businessObjectService.findMatching(PurchasingAccountsPayableDocument.class, cmKeys);
                    if (matchingCreditMemos == null || matchingCreditMemos.isEmpty()) {
                        newList.add(entry);
                    }
                }
            }
        }
        matchingResultsCount = newList.size();
        if (matchingResultsCount <= searchResultsLimit) {
            matchingResultsCount = 0L;
        }
        return new CollectionIncomplete(newList, matchingResultsCount);
    }

    /**
     * Update activity status code to the value used in DB. The reason is the value from user input will be 'Y' or
     * 'N'. However, these two status code are now replaced by 'N','E' and 'P'.
     *
     * @param fieldValues
     */
    protected void updateStatusCodeCriteria(final Map<String, String> fieldValues) {
        String activityStatusCode = null;
        if (fieldValues.containsKey(CamsPropertyConstants.GeneralLedgerEntry.ACTIVITY_STATUS_CODE)) {
            activityStatusCode = fieldValues.get(CamsPropertyConstants.GeneralLedgerEntry.ACTIVITY_STATUS_CODE);
        }

        if (KFSConstants.NON_ACTIVE_INDICATOR.equalsIgnoreCase(activityStatusCode)) {
            // not processed in CAMs: 'N'
            fieldValues.put(CamsPropertyConstants.GeneralLedgerEntry.ACTIVITY_STATUS_CODE,
                    CamsConstants.ActivityStatusCode.NEW);
        } else if (KFSConstants.ACTIVE_INDICATOR.equalsIgnoreCase(activityStatusCode)) {
            // processed in CAMs: 'E' or 'P'
            fieldValues.put(CamsPropertyConstants.GeneralLedgerEntry.ACTIVITY_STATUS_CODE,
                    CamsConstants.ActivityStatusCode.PROCESSED_IN_CAMS + SearchOperator.OR.op() +
                            CamsConstants.ActivityStatusCode.ENROUTE);
        }
    }

    public void setPermissionService(final PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /*
     * CU Customization: Add Cornell-specific helper methods.
     */

    private LocalDate parsePreqCutoffDate() {
        try {
            return dateTimeService.convertToLocalDate(preqCutoffDate);
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isTentativelyEligibleToIncludeInSearchResults(
            final GeneralLedgerEntry entry, final LocalDate cutoffDate) {
        if (!PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT_TYPES.contains(
                    entry.getFinancialDocumentTypeCode())) {
            return true;
        } else if (StringUtils.equals(entry.getFinancialDocumentTypeCode(),
                            PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT)) {
            return false;
        } else {
            return entryWasPostedBeforePreqSubTypesMovedToAccountsPayableLookup(entry, cutoffDate);
        }
    }

    private boolean entryWasPostedBeforePreqSubTypesMovedToAccountsPayableLookup(
            final GeneralLedgerEntry entry, final LocalDate cutoffDate) {
        if (entry.getTransactionPostingDate() == null) {
            return false;
        }
        final LocalDate postingDate = entry.getTransactionPostingDate().toLocalDate();
        return postingDate.compareTo(cutoffDate) < 0;
    }

    public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setPreqCutoffDate(final String preqCutoffDate) {
        this.preqCutoffDate = preqCutoffDate;
    }

    /*
     * End CU Customization
     */

}
