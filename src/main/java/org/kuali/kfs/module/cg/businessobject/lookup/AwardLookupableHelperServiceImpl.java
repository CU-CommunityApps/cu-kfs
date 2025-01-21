/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.module.cg.businessobject.lookup;

import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.kfs.kns.lookup.KualiLookupableHelperServiceImpl;
import org.kuali.kfs.kns.util.FieldUtils;
import org.kuali.kfs.kns.web.ui.Column;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.service.ContractsGrantsBillingUtilityService;
import org.kuali.kfs.module.cg.CGPropertyConstants;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.service.ContractsAndGrantsLookupService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allows custom handling of Awards within the lookup framework.
 */
public class AwardLookupableHelperServiceImpl extends KualiLookupableHelperServiceImpl {

    protected ContractsAndGrantsLookupService contractsAndGrantsLookupService;
    //CU customization to change member access from private to protected
    protected ContractsGrantsBillingUtilityService contractsGrantsBillingUtilityService;

    @Override
    public List<Column> getColumns() {
        final List<Column> columns = super.getColumns();

        if (!contractsGrantsBillingUtilityService.isContractsGrantsBillingEnhancementActive()) {
            columns.removeIf(column -> getFieldsToIgnore().contains(column.getPropertyName()));
        }

        return columns;
    }

    /**
     * Ignore fields that are specific to the Contracts & Grants Billing (CGB) enhancement if CGB is disabled.
     */
    @Override
    protected void setRows() {
        List<String> lookupFieldNames = null;
        if (getBusinessObjectMetaDataService().isLookupable(getBusinessObjectClass())) {
            lookupFieldNames = getBusinessObjectMetaDataService().getLookupableFieldNames(
                getBusinessObjectClass());
        }
        if (lookupFieldNames == null) {
            throw new RuntimeException("Lookup not defined for business object " + getBusinessObjectClass());
        }

        final List<String> lookupFieldAttributeList = new ArrayList<>();
        for (final String lookupFieldName : lookupFieldNames) {
            if (!getFieldsToIgnore().contains(lookupFieldName)) {
                lookupFieldAttributeList.add(lookupFieldName);
            }
        }

        // construct field object for each search attribute
        final List fields;
        try {
            fields = FieldUtils.createAndPopulateFieldsForLookup(lookupFieldAttributeList, getReadOnlyFieldsList(),
                getBusinessObjectClass());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to create instance of business object class" + e.getMessage());
        }

        final int numCols = getBusinessObjectDictionaryService().getLookupNumberOfColumns(getBusinessObjectClass());

        rows = FieldUtils.wrapFields(fields, numCols);
    }

    /**
     * If the Contracts & Grants Billing (CGB) enhancement is disabled, we don't want to process sections only
     * related to CGB.
     *
     * @return list of fields to ignore
     */
    protected List<String> getFieldsToIgnore() {
        final List<String> fieldsToIgnore = new ArrayList<>();

        if (!contractsGrantsBillingUtilityService.isContractsGrantsBillingEnhancementActive()) {
            fieldsToIgnore.add(CGPropertyConstants.LOOKUP_FUND_MGR_USER_ID_FIELD);
            fieldsToIgnore.add(CGPropertyConstants.AWARD_LOOKUP_PRIMARY_FUND_MGR_FUND_MGR_NAME);
            fieldsToIgnore.add(CGPropertyConstants.AwardFields.LAST_BILLED_DATE);
            fieldsToIgnore.add(CGPropertyConstants.AwardFields.BILLING_FREQUENCY_CODE);
            fieldsToIgnore.add(CGPropertyConstants.AwardFields.EXCLUDED_FROM_INVOICING);
            fieldsToIgnore.add(CGPropertyConstants.AwardFields.ADDITIONAL_FORMS_DESCRIPTION);
            fieldsToIgnore.add(CGPropertyConstants.AwardFields.ADDITIONAL_FORMS_REQUIRED_INDICATOR);
            fieldsToIgnore.add(CGPropertyConstants.AwardFields.MIN_INVOICE_AMOUNT);
            fieldsToIgnore.add(CGPropertyConstants.AwardFields.FUNDING_EXPIRATION_DATE);
        }

        return fieldsToIgnore;
    }

    @Override
    protected List<? extends BusinessObject> getSearchResultsHelper(final Map<String, String> fieldValues, final boolean unbounded) {
        // perform the lookup on the project director and fund manager objects first
        if (contractsAndGrantsLookupService.setupSearchFields(fieldValues,
                    CGPropertyConstants.LOOKUP_PROJECT_DIRECTOR_USER_ID_FIELD,
                CGPropertyConstants.AWARD_LOOKUP_UNIVERSAL_USER_ID_FIELD)
                && contractsAndGrantsLookupService.setupSearchFields(fieldValues,
                    CGPropertyConstants.LOOKUP_FUND_MGR_USER_ID_FIELD,
                    CGPropertyConstants.AWARD_LOOKUP_FUND_MGR_UNIVERSAL_USER_ID_FIELD)) {
            return super.getSearchResultsHelper(fieldValues, unbounded);
        }

        return Collections.EMPTY_LIST;
    }

    @Override
    public List<HtmlData> getCustomActionUrls(final BusinessObject businessObject, final List pkNames) {
        final List<HtmlData> anchorHtmlDataList = new ArrayList<>();
        anchorHtmlDataList.add(getUrlData(businessObject, KRADConstants.MAINTENANCE_EDIT_METHOD_TO_CALL, pkNames));
        if (allowsMaintenanceNewOrCopyAction()) {
            anchorHtmlDataList.add(getUrlData(businessObject, KRADConstants.MAINTENANCE_COPY_METHOD_TO_CALL, pkNames));
        }

        // only display invoice lookup URL if CGB is enabled
        if (contractsGrantsBillingUtilityService.isContractsGrantsBillingEnhancementActive()) {
            final AnchorHtmlData invoiceUrl = getInvoicesLookupUrl(businessObject);
            anchorHtmlDataList.add(invoiceUrl);
        }

        return anchorHtmlDataList;
    }

    /**
     * This method adds a link to the look up FOR the invoices associated with a given Award.
     *
     * @param bo
     * @return
     */
    protected AnchorHtmlData getInvoicesLookupUrl(final BusinessObject bo) {
        final Award award = (Award) bo;
        final Map<String, String> params = new HashMap<>();
        params.put(KFSPropertyConstants.DOCUMENT_TYPE_NAME, ArConstants.ArDocumentTypeCodes.CONTRACTS_GRANTS_INVOICE);
        params.put(KewApiConstants.DOCUMENT_ATTRIBUTE_FIELD_PREFIX + KFSPropertyConstants.PROPOSAL_NUMBER,
                award.getProposalNumber());
        final String url = UrlFactory.parameterizeUrl(configurationService
                .getPropertyValueAsString(KRADConstants.WORKFLOW_DOCUMENTSEARCH_URL_KEY), params);
        final AnchorHtmlData anchorHtmlData = new AnchorHtmlData(url, KFSConstants.SEARCH_METHOD, "View Invoices");
        anchorHtmlData.setTarget(KFSConstants.NEW_WINDOW_URL_TARGET);
        return anchorHtmlData;
    }

    public ContractsAndGrantsLookupService getContractsAndGrantsLookupService() {
        return contractsAndGrantsLookupService;
    }

    public void setContractsAndGrantsLookupService(final ContractsAndGrantsLookupService contractsAndGrantsLookupService) {
        this.contractsAndGrantsLookupService = contractsAndGrantsLookupService;
    }

    public void setContractsGrantsBillingUtilityService(
            final ContractsGrantsBillingUtilityService contractsGrantsBillingUtilityService
    ) {
        this.contractsGrantsBillingUtilityService = contractsGrantsBillingUtilityService;
    }
}
