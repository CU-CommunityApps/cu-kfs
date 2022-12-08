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
package org.kuali.kfs.gl.web.struts;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.ObjectHelper;
import org.kuali.kfs.gl.businessobject.AccountBalance;
import org.kuali.kfs.gl.businessobject.lookup.AccountBalanceByConsolidationLookupableHelperServiceImpl;
import org.kuali.kfs.integration.ld.SegmentedBusinessObject;
import org.kuali.kfs.kns.lookup.CollectionIncomplete;
import org.kuali.kfs.kns.lookup.LookupResultsService;
import org.kuali.kfs.kns.lookup.Lookupable;
import org.kuali.kfs.kns.web.struts.action.KualiMultipleValueLookupAction;
import org.kuali.kfs.kns.web.struts.form.MultipleValueLookupForm;
import org.kuali.kfs.kns.web.ui.Column;
import org.kuali.kfs.kns.web.ui.ResultRow;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADUtils;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Balance inquiries are pretty much just lookups already, but are not used in the traditional sense. In most cases,
 * balance inquiries only show the end-user data, and allow the end-user to drill-down into inquiries. A traditional
 * lookup allows the user to return data to a form. This class is for balance inquiries implemented in the sense of a
 * traditional lookup for forms that pull data out of inquiries.<br/> <br/>
 * One example of this is the {@code org.kuali.kfs.module.ld.document.SalaryExpenseTransferDocument} which creates
 * source lines from a labor ledger balance inquiry screen.<br/> <br/>
 * This is a {@link KualiMultipleValueLookupAction} which required some customization because requirements were not
 * possible with displaytag.
 *
 * See:
 * {@code org.kuali.kfs.module.ld.document.SalaryExpenseTransferDocument}
 * {@code org.kuali.kfs.module.ld.document.web.struts.SalaryExpenseTransferAction}
 * {@code org.kuali.kfs.module.ld.document.web.struts.SalaryExpenseTransferForm}
 */
public class BalanceInquiryLookupAction extends KualiMultipleValueLookupAction {
    private static final Logger LOG = LogManager.getLogger();

    private static final String TOTALS_TABLE_KEY = "totalsTable";

    private ConfigurationService kualiConfigurationService;
    private String[] totalTitles;

    public BalanceInquiryLookupAction() {
        super();
        kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);
    }

    private void setTotalTitles() {
        totalTitles = new String[7];
        totalTitles[0] = kualiConfigurationService.getPropertyValueAsString(
                KFSKeyConstants.AccountBalanceService.INCOME);
        totalTitles[1] = kualiConfigurationService.getPropertyValueAsString(
                KFSKeyConstants.AccountBalanceService.INCOME_FROM_TRANSFERS);
        totalTitles[2] = kualiConfigurationService.getPropertyValueAsString(
                KFSKeyConstants.AccountBalanceService.INCOME_TOTAL);
        totalTitles[3] = kualiConfigurationService.getPropertyValueAsString(
                KFSKeyConstants.AccountBalanceService.EXPENSE);
        totalTitles[4] = kualiConfigurationService.getPropertyValueAsString(
                KFSKeyConstants.AccountBalanceService.EXPENSE_FROM_TRANSFERS);
        totalTitles[5] = kualiConfigurationService.getPropertyValueAsString(
                KFSKeyConstants.AccountBalanceService.EXPENSE_TOTAL);
        totalTitles[6] = kualiConfigurationService.getPropertyValueAsString(
                KFSKeyConstants.AccountBalanceService.TOTAL);
    }

    private String[] getTotalTitles() {
        if (null == totalTitles) {
            setTotalTitles();
        }

        return totalTitles;
    }

    /**
     * search - sets the values of the data entered on the form on the jsp into a map and then searches for the results.
     */
    @Override
    public ActionForward search(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        BalanceInquiryLookupForm lookupForm = (BalanceInquiryLookupForm) form;
        Lookupable lookupable = lookupForm.getLookupable();

        if (lookupable == null) {
            LOG.error("Lookupable is null.");
            throw new RuntimeException("Lookupable is null.");
        }

        lookupable.validateSearchParameters(lookupForm.getFields());
        if (GlobalVariables.getMessageMap().getErrorCount() > 0) {
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        List<ResultRow> resultTable = new ArrayList<>();
        Collection displayList = performMultipleValueLookup(lookupForm, resultTable, getMaxRowsPerPage(lookupForm),
                true);
        CollectionIncomplete incompleteDisplayList = (CollectionIncomplete) displayList;
        Long totalSize = incompleteDisplayList.getActualSizeIfTruncated();

        if (lookupable.isSearchUsingOnlyPrimaryKeyValues()) {
            lookupForm.setSearchUsingOnlyPrimaryKeyValues(true);
            lookupForm.setPrimaryKeyFieldLabels(lookupable.getPrimaryKeyFieldLabels());
        } else {
            lookupForm.setSearchUsingOnlyPrimaryKeyValues(false);
            lookupForm.setPrimaryKeyFieldLabels(KFSConstants.EMPTY_STRING);
        }

        // TODO: use inheritance instead of this if statement
        if (lookupable.getLookupableHelperService() instanceof
                AccountBalanceByConsolidationLookupableHelperServiceImpl) {
            Object[] resultTableAsArray = resultTable.toArray();
            Collection totalsTable = new ArrayList();

            int arrayIndex = 0;

            try {
                for (int listIndex = 0; listIndex < incompleteDisplayList.size(); listIndex++) {
                    AccountBalance balance = (AccountBalance) incompleteDisplayList.get(listIndex);
                    boolean ok = ObjectHelper.isOneOf(balance.getTitle(), getTotalTitles());
                    if (ok) {
                        if (totalSize > 7) {
                            totalsTable.add(resultTableAsArray[arrayIndex]);
                        }
                        resultTable.remove(resultTableAsArray[arrayIndex]);
                        incompleteDisplayList.remove(balance);
                    }
                    arrayIndex++;
                }

                request.setAttribute(TOTALS_TABLE_KEY, totalsTable);
                GlobalVariables.getUserSession().addObject(TOTALS_TABLE_KEY, totalsTable);
            } catch (NumberFormatException e) {
                GlobalVariables.getMessageMap().putError(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR,
                        KFSKeyConstants.ERROR_CUSTOM, "Fiscal Year must be a four-digit number");
            } catch (Exception e) {
                GlobalVariables.getMessageMap().putError(KFSConstants.DOCUMENT_ERRORS, KFSKeyConstants.ERROR_CUSTOM,
                        "Please report the server error.≥");
                LOG.error("Application Errors", e);
            }
        }

        request.setAttribute(KFSConstants.REQUEST_SEARCH_RESULTS_SIZE, totalSize);
        request.setAttribute(KFSConstants.REQUEST_SEARCH_RESULTS, resultTable);
        lookupForm.setResultsActualSize((int) totalSize.longValue());
        lookupForm.setResultsLimitedSize(resultTable.size());

        if (lookupForm.isSegmented()) {
            LOG.debug("I'm segmented");
            request.setAttribute(GeneralLedgerConstants.LookupableBeanKeys.SEGMENTED_LOOKUP_FLAG_NAME, Boolean.TRUE);
        }

        if (request.getParameter(KFSConstants.SEARCH_LIST_REQUEST_KEY) != null) {
            GlobalVariables.getUserSession().removeObject(request.getParameter(KFSConstants.SEARCH_LIST_REQUEST_KEY));
            request.setAttribute(KFSConstants.SEARCH_LIST_REQUEST_KEY,
                    GlobalVariables.getUserSession().addObjectWithGeneratedKey(resultTable));
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * @param mapping
     * @param form     must be an instance of MultipleValueLookupForm
     * @param request
     * @param response
     * @return none of the selected results and redirects back to the lookup caller.
     */
    @Override
    public ActionForward prepareToReturnNone(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) {
        MultipleValueLookupForm multipleValueLookupForm = (MultipleValueLookupForm) form;
        prepareToReturnNone(multipleValueLookupForm);

        // build the parameters for the refresh url
        Map<String, String> parameters = new HashMap<>();
        parameters.put(KFSConstants.DOC_FORM_KEY, multipleValueLookupForm.getFormKey());
        parameters.put(KFSConstants.DISPATCH_REQUEST_PARAMETER, KFSConstants.RETURN_METHOD_TO_CALL);
        parameters.put(KFSConstants.REFRESH_CALLER, KFSConstants.MULTIPLE_VALUE);
        if (StringUtils.isNotBlank(multipleValueLookupForm.getLookupAnchor())) {
            parameters.put(KFSConstants.ANCHOR, multipleValueLookupForm.getLookupAnchor());
        }

        String backUrl = UrlFactory.parameterizeUrl(multipleValueLookupForm.getBackLocation(), parameters);
        return new ActionForward(backUrl, true);
    }

    /**
     * This method does the processing necessary to return selected results and sends a redirect back to the lookup
     * caller.
     *
     * @param mapping
     * @param form     must be an instance of MultipleValueLookupForm
     * @param request
     * @param response
     * @return
     */
    @Override
    public ActionForward prepareToReturnSelectedResults(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) {
        MultipleValueLookupForm multipleValueLookupForm = (MultipleValueLookupForm) form;
        if (StringUtils.isBlank(multipleValueLookupForm.getLookupResultsSequenceNumber())) {
            // no search was executed
            return prepareToReturnNone(mapping, form, request, response);
        }

        prepareToReturnSelectedResultBOs(multipleValueLookupForm);

        // build the parameters for the refresh url
        Map<String, String> parameters = new HashMap<>();
        parameters.put(KFSConstants.LOOKUP_RESULTS_BO_CLASS_NAME, multipleValueLookupForm.getBusinessObjectClassName());
        parameters.put(KFSConstants.LOOKUP_RESULTS_SEQUENCE_NUMBER,
                multipleValueLookupForm.getLookupResultsSequenceNumber());
        parameters.put(KFSConstants.DOC_FORM_KEY, multipleValueLookupForm.getFormKey());
        parameters.put(KFSConstants.DISPATCH_REQUEST_PARAMETER, KFSConstants.RETURN_METHOD_TO_CALL);
        parameters.put(KFSConstants.REFRESH_CALLER, KFSConstants.MULTIPLE_VALUE);
        if (StringUtils.isNotBlank(multipleValueLookupForm.getLookupAnchor())) {
            parameters.put(KFSConstants.ANCHOR, multipleValueLookupForm.getLookupAnchor());
        }
        String backUrl = UrlFactory.parameterizeUrl(multipleValueLookupForm.getBackLocation(), parameters);
        return new ActionForward(backUrl, true);
    }

    @Override
    public ActionForward sort(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        request.setAttribute(GeneralLedgerConstants.LookupableBeanKeys.SEGMENTED_LOOKUP_FLAG_NAME, Boolean.TRUE);
        return super.sort(mapping, form, request, response);
    }

    @Override
    public ActionForward selectAll(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        request.setAttribute(GeneralLedgerConstants.LookupableBeanKeys.SEGMENTED_LOOKUP_FLAG_NAME, Boolean.TRUE);
        return super.selectAll(mapping, form, request, response);
    }

    @Override
    protected List<ResultRow> selectAll(MultipleValueLookupForm multipleValueLookupForm, int maxRowsPerPage) {
        List<ResultRow> resultTable;
        try {
            LookupResultsService lookupResultsService = SpringContext.getBean(LookupResultsService.class);
            String lookupResultsSequenceNumber = multipleValueLookupForm.getLookupResultsSequenceNumber();

            resultTable = lookupResultsService.retrieveResultsTable(lookupResultsSequenceNumber,
                    GlobalVariables.getUserSession().getPerson().getPrincipalId());
        } catch (Exception e) {
            LOG.error("error occurred trying to export multiple lookup results", e);
            throw new RuntimeException("error occurred trying to export multiple lookup results");
        }

        Map<String, String> selectedObjectIds = this.getSelectedObjectIds(multipleValueLookupForm, resultTable);

        multipleValueLookupForm.jumpToPage(multipleValueLookupForm.getViewedPageNumber(), resultTable.size(),
                maxRowsPerPage);
        multipleValueLookupForm.setColumnToSortIndex(
                Integer.parseInt(multipleValueLookupForm.getPreviouslySortedColumnIndex()));
        multipleValueLookupForm.setCompositeObjectIdMap(selectedObjectIds);

        return resultTable;
    }

    @Override
    public ActionForward unselectAll(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        request.setAttribute(GeneralLedgerConstants.LookupableBeanKeys.SEGMENTED_LOOKUP_FLAG_NAME, Boolean.TRUE);
        return super.unselectAll(mapping, form, request, response);
    }

    @Override
    public ActionForward switchToPage(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        request.setAttribute(GeneralLedgerConstants.LookupableBeanKeys.SEGMENTED_LOOKUP_FLAG_NAME, Boolean.TRUE);
        return super.switchToPage(mapping, form, request, response);
    }

    /**
     * This method performs the lookup and returns a collection of lookup items. Also initializes values in the form
     * that will allow the multiple value lookup page to render.
     *
     * @param multipleValueLookupForm
     * @param resultTable            a list of result rows (used to generate what's shown in the UI). This list will be
     *                               modified by this method
     * @param maxRowsPerPage
     * @param bounded                whether the results will be bounded
     * @return the list of result BOs, possibly bounded by size
     */
    @Override
    protected Collection performMultipleValueLookup(MultipleValueLookupForm multipleValueLookupForm,
            List<ResultRow> resultTable, int maxRowsPerPage, boolean bounded) {
        Lookupable lookupable = multipleValueLookupForm.getLookupable();
        Collection displayList = lookupable.performLookup(multipleValueLookupForm, resultTable, bounded);

        List defaultSortColumns = lookupable.getDefaultSortColumns();
        if (defaultSortColumns != null && !defaultSortColumns.isEmpty() && resultTable != null && !resultTable
                .isEmpty()) {
            // there's a default sort order, just find the first sort column, and we can't go wrong
            String firstSortColumn = (String) defaultSortColumns.get(0);

            // go thru the first result row to find the index of the column (more efficient than calling
            // lookupable.getColumns since we don't have to recreate column list)
            int firstSortColumnIdx = -1;
            List<Column> columnsForFirstResultRow = resultTable.get(0).getColumns();
            for (int i = 0; i < columnsForFirstResultRow.size(); i++) {
                if (StringUtils.equals(firstSortColumn, columnsForFirstResultRow.get(i).getPropertyName())) {
                    firstSortColumnIdx = i;
                    break;
                }
            }
            multipleValueLookupForm.setColumnToSortIndex(firstSortColumnIdx);
        } else {
            // don't know how results were sorted, so we just say -1
            multipleValueLookupForm.setColumnToSortIndex(-1);
        }

        // we just performed the lookup, so we're on the first page (indexed from 0)
        multipleValueLookupForm.jumpToFirstPage(resultTable.size(), maxRowsPerPage);

        SequenceAccessorService sequenceAccessorService = SpringContext.getBean(SequenceAccessorService.class);
        String lookupResultsSequenceNumber = String.valueOf(
                sequenceAccessorService.getNextAvailableSequenceNumber(KRADConstants.LOOKUP_RESULTS_SEQUENCE));
        multipleValueLookupForm.setLookupResultsSequenceNumber(lookupResultsSequenceNumber);
        try {
            LookupResultsService lookupResultsService = SpringContext.getBean(LookupResultsService.class);
            lookupResultsService.persistResultsTable(lookupResultsSequenceNumber, resultTable,
                    GlobalVariables.getUserSession().getPerson().getPrincipalId());
        } catch (Exception e) {
            LOG.error("error occurred trying to persist multiple lookup results", e);
            throw new RuntimeException("error occurred trying to persist multiple lookup results");
        }

        // since new search, nothing's checked
        multipleValueLookupForm.setCompositeObjectIdMap(new HashMap<>());

        return displayList;
    }

    /**
     * put all entities into select object map. This implementation only deals with the money amount objects.
     *
     * @param multipleValueLookupForm the given struts form
     * @param resultTable             the given result table that holds all data being presented
     * @return the map containing all entries available for selection
     */
    private Map<String, String> getSelectedObjectIds(MultipleValueLookupForm multipleValueLookupForm,
            List<ResultRow> resultTable) {
        String businessObjectClassName = multipleValueLookupForm.getBusinessObjectClassName();
        SegmentedBusinessObject segmentedBusinessObject;
        try {
            segmentedBusinessObject = (SegmentedBusinessObject) Class.forName(
                    multipleValueLookupForm.getBusinessObjectClassName()).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Fail to create an object of " + businessObjectClassName + e);
        }

        Map<String, String> selectedObjectIds = new HashMap<>();
        Collection<String> segmentedPropertyNames = segmentedBusinessObject.getSegmentedPropertyNames();
        for (ResultRow row : resultTable) {
            for (Column column : row.getColumns()) {
                String propertyName = column.getPropertyName();
                if (segmentedPropertyNames.contains(propertyName)) {
                    String propertyValue = StringUtils.replace(column.getPropertyValue(), ",", "");
                    
                    //Cornell Customization If there is a negative value, we need to convert it from (##.##) to -##.##
                    if (StringUtils.contains(propertyValue, "(")) {
                        propertyValue = StringUtils.replace(propertyValue, "(", "-");
                        propertyValue = StringUtils.replace(propertyValue, ")", "");
                    }
                    
                    KualiDecimal amount = new KualiDecimal(propertyValue);

                    if (amount.isNonZero()) {
                        String objectId = row.getObjectId() + "." + propertyName + "." + KRADUtils
                                .convertDecimalIntoInteger(amount);
                        selectedObjectIds.put(objectId, objectId);
                    }
                }
            }
        }

        return selectedObjectIds;
    }
}

