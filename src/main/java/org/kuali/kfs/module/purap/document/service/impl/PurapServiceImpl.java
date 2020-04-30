/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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
package org.kuali.kfs.module.purap.document.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kns.service.DataDictionaryService;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.exception.InfrastructureException;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.service.PersistenceService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants.TaxParameters;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.PurapRuleConstants;
import org.kuali.kfs.module.purap.businessobject.AccountsPayableItem;
import org.kuali.kfs.module.purap.businessobject.BulkReceivingView;
import org.kuali.kfs.module.purap.businessobject.CorrectionReceivingView;
import org.kuali.kfs.module.purap.businessobject.CreditMemoView;
import org.kuali.kfs.module.purap.businessobject.ItemType;
import org.kuali.kfs.module.purap.businessobject.LineItemReceivingView;
import org.kuali.kfs.module.purap.businessobject.OrganizationParameter;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestView;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurApItemUseTax;
import org.kuali.kfs.module.purap.businessobject.PurapEnterableItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderView;
import org.kuali.kfs.module.purap.businessobject.PurchasingItem;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.module.purap.businessobject.RequisitionView;
import org.kuali.kfs.module.purap.document.AccountsPayableDocument;
import org.kuali.kfs.module.purap.document.AccountsPayableDocumentBase;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurapItemOperations;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.document.PurchasingDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.service.LogicContainer;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.module.purap.service.PurapAccountingService;
import org.kuali.kfs.module.purap.util.PurApItemUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TaxDetail;
import org.kuali.kfs.sys.document.validation.event.DocumentSystemSaveEvent;
import org.kuali.kfs.sys.service.TaxService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.vnd.businessobject.CommodityCode;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.parameter.ParameterEvaluator;
import org.kuali.rice.core.api.parameter.ParameterEvaluatorService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kew.api.document.attribute.DocumentAttributeIndexingQueue;
import org.kuali.rice.kew.api.exception.WorkflowException;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PurapServiceImpl implements PurapService {

    private static final Logger LOG = LogManager.getLogger();

    protected BusinessObjectService businessObjectService;
    protected DataDictionaryService dataDictionaryService;
    protected DateTimeService dateTimeService;
    protected DocumentService documentService;
    protected NoteService noteService;
    protected ParameterService parameterService;
    protected PersistenceService persistenceService;
    protected PurchaseOrderService purchaseOrderService;
    protected UniversityDateService universityDateService;
    protected VendorService vendorService;
    protected TaxService taxService;
    protected PurapAccountingService purapAccountingService;
    //CU customization change from private to protected
    protected ParameterEvaluatorService parameterEvaluatorService;

    public void setBusinessObjectService(BusinessObjectService boService) {
        this.businessObjectService = boService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setVendorService(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    public void setPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    public void setPurchaseOrderService(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    public void setTaxService(TaxService taxService) {
        this.taxService = taxService;
    }

    @Override
    public void saveRoutingDataForRelatedDocuments(Integer accountsPayablePurchasingDocumentLinkIdentifier) {
        try {
            //save requisition routing data
            List<RequisitionView> reqViews = getRelatedViews(RequisitionView.class,
                    accountsPayablePurchasingDocumentLinkIdentifier);
            for (RequisitionView view : reqViews) {
                Document doc = documentService.getByDocumentHeaderId(view.getDocumentNumber());
                doc.getDocumentHeader().getWorkflowDocument().saveDocumentData();
            }

            //save purchase order routing data
            List<PurchaseOrderView> poViews = getRelatedViews(PurchaseOrderView.class,
                    accountsPayablePurchasingDocumentLinkIdentifier);
            for (PurchaseOrderView view : poViews) {
                Document doc = documentService.getByDocumentHeaderId(view.getDocumentNumber());
                doc.getDocumentHeader().getWorkflowDocument().saveDocumentData();
            }

            //save payment request routing data
            List<PaymentRequestView> preqViews = getRelatedViews(PaymentRequestView.class,
                    accountsPayablePurchasingDocumentLinkIdentifier);
            for (PaymentRequestView view : preqViews) {
                Document doc = documentService.getByDocumentHeaderId(view.getDocumentNumber());
                doc.getDocumentHeader().getWorkflowDocument().saveDocumentData();
            }

            //save credit memo routing data
            List<CreditMemoView> cmViews = getRelatedViews(CreditMemoView.class,
                    accountsPayablePurchasingDocumentLinkIdentifier);
            for (CreditMemoView view : cmViews) {
                Document doc = documentService.getByDocumentHeaderId(view.getDocumentNumber());
                doc.getDocumentHeader().getWorkflowDocument().saveDocumentData();
            }

            //save line item receiving routing data
            List<LineItemReceivingView> lineViews = getRelatedViews(LineItemReceivingView.class,
                    accountsPayablePurchasingDocumentLinkIdentifier);
            for (LineItemReceivingView view : lineViews) {
                Document doc = documentService.getByDocumentHeaderId(view.getDocumentNumber());
                doc.getDocumentHeader().getWorkflowDocument().saveDocumentData();
            }

            //save correction receiving routing data
            List<CorrectionReceivingView> corrViews = getRelatedViews(CorrectionReceivingView.class,
                    accountsPayablePurchasingDocumentLinkIdentifier);
            for (CorrectionReceivingView view : corrViews) {
                Document doc = documentService.getByDocumentHeaderId(view.getDocumentNumber());
                doc.getDocumentHeader().getWorkflowDocument().saveDocumentData();
            }

            //save bulk receiving routing data
            List<BulkReceivingView> bulkViews = getRelatedViews(BulkReceivingView.class,
                    accountsPayablePurchasingDocumentLinkIdentifier);
            for (BulkReceivingView view : bulkViews) {
                Document doc = documentService.getByDocumentHeaderId(view.getDocumentNumber());
                doc.getDocumentHeader().getWorkflowDocument().saveDocumentData();
            }
        } catch (WorkflowException e) {
            throw new InfrastructureException("unable to save routing data for related docs", e);
        }

    }

    @Override
    public List<String> getRelatedDocumentIds(Integer accountsPayablePurchasingDocumentLinkIdentifier) {
        LOG.debug("getRelatedDocumentIds() started");
        List<String> documentIdList = new ArrayList<>();

        //get requisition views
        List<RequisitionView> reqViews = getRelatedViews(RequisitionView.class,
                accountsPayablePurchasingDocumentLinkIdentifier);
        for (RequisitionView view : reqViews) {
            documentIdList.add(view.getDocumentNumber());
        }

        //get purchase order views
        List<PurchaseOrderView> poViews = getRelatedViews(PurchaseOrderView.class,
                accountsPayablePurchasingDocumentLinkIdentifier);
        for (PurchaseOrderView view : poViews) {
            documentIdList.add(view.getDocumentNumber());
        }

        //get payment request views
        List<PaymentRequestView> preqViews = getRelatedViews(PaymentRequestView.class,
                accountsPayablePurchasingDocumentLinkIdentifier);
        for (PaymentRequestView view : preqViews) {
            documentIdList.add(view.getDocumentNumber());
        }

        //get credit memo views
        List<CreditMemoView> cmViews = getRelatedViews(CreditMemoView.class,
                accountsPayablePurchasingDocumentLinkIdentifier);
        for (CreditMemoView view : cmViews) {
            documentIdList.add(view.getDocumentNumber());
        }

        //get line item receiving views
        List<LineItemReceivingView> lineViews = getRelatedViews(LineItemReceivingView.class,
                accountsPayablePurchasingDocumentLinkIdentifier);
        for (LineItemReceivingView view : lineViews) {
            documentIdList.add(view.getDocumentNumber());
        }

        //get correction receiving views
        List<CorrectionReceivingView> corrViews = getRelatedViews(CorrectionReceivingView.class,
                accountsPayablePurchasingDocumentLinkIdentifier);
        for (CorrectionReceivingView view : corrViews) {
            documentIdList.add(view.getDocumentNumber());
        }

        //get bulk receiving views
        List<BulkReceivingView> bulkViews = getRelatedViews(BulkReceivingView.class,
                accountsPayablePurchasingDocumentLinkIdentifier);
        for (BulkReceivingView view : bulkViews) {
            documentIdList.add(view.getDocumentNumber());
        }

        //TODO (hjs)get electronic invoice reject views???

        return documentIdList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List getRelatedViews(Class clazz, Integer accountsPayablePurchasingDocumentLinkIdentifier) {
        LOG.debug("getRelatedViews() started");

        Map criteria = new HashMap();
        criteria.put("accountsPayablePurchasingDocumentLinkIdentifier",
                accountsPayablePurchasingDocumentLinkIdentifier);

        // retrieve in descending order of document number so that newer documents are in the front
        return (List) businessObjectService.findMatchingOrderBy(clazz, criteria, KFSPropertyConstants.DOCUMENT_NUMBER,
                false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addBelowLineItems(PurchasingAccountsPayableDocument document) {
        LOG.debug("addBelowLineItems() started");

        String[] itemTypes = getBelowTheLineForDocument(document);

        List<PurApItem> existingItems = document.getItems();

        List<PurApItem> belowTheLine = new ArrayList<>();
        // needed in case they get out of sync below won't work
        sortBelowTheLine(itemTypes, existingItems, belowTheLine);

        List<String> existingItemTypes = new ArrayList<>();
        for (PurApItem existingItem : existingItems) {
            existingItemTypes.add(existingItem.getItemTypeCode());
        }

        Class itemClass = document.getItemClass();

        for (int i = 0; i < itemTypes.length; i++) {
            int lastFound;
            if (!existingItemTypes.contains(itemTypes[i])) {
                try {
                    if (i > 0) {
                        lastFound = existingItemTypes.lastIndexOf(itemTypes[i - 1]) + 1;
                    } else {
                        lastFound = existingItemTypes.size();
                    }
                    PurApItem newItem = (PurApItem) itemClass.newInstance();
                    newItem.setItemTypeCode(itemTypes[i]);
                    newItem.setPurapDocument(document);
                    existingItems.add(lastFound, newItem);
                    existingItemTypes.add(itemTypes[i]);
                } catch (Exception e) {
                    // do something
                }
            }
        }

        document.fixItemReferences();
    }

    /**
     * Sorts the below the line elements
     *
     * @param itemTypes
     * @param existingItems
     * @param belowTheLine
     */
    protected void sortBelowTheLine(String[] itemTypes, List<PurApItem> existingItems, List<PurApItem> belowTheLine) {
        LOG.debug("sortBelowTheLine() started");

        // sort existing below the line if any
        for (PurApItem purApItem : existingItems) {
            if (purApItem.getItemType().isAdditionalChargeIndicator()) {
                belowTheLine.add(purApItem);
            }
        }
        existingItems.removeAll(belowTheLine);
        for (String itemType : itemTypes) {
            for (PurApItem purApItem : belowTheLine) {
                if (StringUtils.equalsIgnoreCase(purApItem.getItemTypeCode(), itemType)) {
                    existingItems.add(purApItem);
                    break;
                }
            }
        }
        belowTheLine.removeAll(existingItems);
        if (belowTheLine.size() != 0) {
            throw new RuntimeException("below the line item sort didn't work: trying to remove an item without " +
                    "adding it back");
        }
    }

    @Override
    public void sortBelowTheLine(PurchasingAccountsPayableDocument document) {
        LOG.debug("sortBelowTheLine() started");

        String[] itemTypes = getBelowTheLineForDocument(document);

        List<PurApItem> existingItems = document.getItems();

        List<PurApItem> belowTheLine = new ArrayList<>();
        // needed in case they get out of sync below won't work
        sortBelowTheLine(itemTypes, existingItems, belowTheLine);
    }

    @Override
    public String[] getBelowTheLineForDocument(PurchasingAccountsPayableDocument document) {
        LOG.debug("getBelowTheLineForDocument() started");

        String documentType = dataDictionaryService.getDocumentTypeNameByClass(document.getClass());

        try {
            return parameterService.getParameterValuesAsString(Class.forName(
                    PurapConstants.PURAP_DETAIL_TYPE_CODE_MAP.get(documentType)),
                    PurapConstants.BELOW_THE_LINES_PARAMETER).toArray(new String[]{});
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("The getBelowTheLineForDocument method of PurapServiceImpl was unable to " +
                    "resolve the document class for type: " +
                    PurapConstants.PURAP_DETAIL_TYPE_CODE_MAP.get(documentType), e);
        }
    }

    @Override
    public PurApItem getBelowTheLineByType(PurchasingAccountsPayableDocument document, ItemType iT) {
        LOG.debug("getBelowTheLineByType() started");

        String[] itemTypes = getBelowTheLineForDocument(document);
        boolean foundItemType = false;
        for (String itemType : itemTypes) {
            if (StringUtils.equals(iT.getItemTypeCode(), itemType)) {
                foundItemType = true;
                break;
            }
        }
        if (!foundItemType) {
            return null;
        }

        PurApItem belowTheLineItem = null;
        for (PurApItem item : document.getItems()) {
            if (item.getItemType().isAdditionalChargeIndicator()) {
                if (StringUtils.equals(iT.getItemTypeCode(), item.getItemType().getItemTypeCode())) {
                    belowTheLineItem = item;
                    break;
                }
            }
        }
        return belowTheLineItem;
    }

    @Override
    public java.sql.Date getDateFromOffsetFromToday(int offsetDays) {
        Calendar calendar = dateTimeService.getCurrentCalendar();
        calendar.add(Calendar.DATE, offsetDays);
        return new java.sql.Date(calendar.getTimeInMillis());
    }

    @Override
    public boolean isDateInPast(Date compareDate) {
        LOG.debug("isDateInPast() started");

        Date today = dateTimeService.getCurrentSqlDate();
        int diffFromToday = dateTimeService.dateDiff(today, compareDate, false);
        return diffFromToday < 0;
    }

    @Override
    public boolean isDateMoreThanANumberOfDaysAway(Date compareDate, int daysAway) {
        LOG.debug("isDateMoreThanANumberOfDaysAway() started");

        Date todayAtMidnight = dateTimeService.getCurrentSqlDateMidnight();
        Calendar daysAwayCalendar = dateTimeService.getCalendar(todayAtMidnight);
        daysAwayCalendar.add(Calendar.DATE, daysAway);
        Timestamp daysAwayTime = new Timestamp(daysAwayCalendar.getTime().getTime());
        Calendar compareCalendar = dateTimeService.getCalendar(compareDate);
        compareCalendar.set(Calendar.HOUR, 0);
        compareCalendar.set(Calendar.MINUTE, 0);
        compareCalendar.set(Calendar.SECOND, 0);
        compareCalendar.set(Calendar.MILLISECOND, 0);
        compareCalendar.set(Calendar.AM_PM, Calendar.AM);
        Timestamp compareTime = new Timestamp(compareCalendar.getTime().getTime());
        return compareTime.compareTo(daysAwayTime) > 0;
    }

    @Override
    public boolean isDateAYearBeforeToday(Date compareDate) {
        LOG.debug("isDateAYearBeforeToday() started");

        Calendar calendar = dateTimeService.getCurrentCalendar();
        calendar.add(Calendar.YEAR, -1);
        java.sql.Date yearAgo = new java.sql.Date(calendar.getTimeInMillis());
        int diffFromYearAgo = dateTimeService.dateDiff(compareDate, yearAgo, false);
        return diffFromYearAgo > 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public KualiDecimal getApoLimit(Integer vendorContractGeneratedIdentifier, String chart, String org) {
        LOG.debug("getApoLimit() started");

        KualiDecimal purchaseOrderTotalLimit = vendorService.getApoLimitFromContract(
                vendorContractGeneratedIdentifier, chart, org);

        // We didn't find the limit on the vendor contract, get it from the org parameter table.
        if (ObjectUtils.isNull(purchaseOrderTotalLimit) && !ObjectUtils.isNull(chart) && !ObjectUtils.isNull(org)) {
            OrganizationParameter organizationParameter = new OrganizationParameter();
            organizationParameter.setChartOfAccountsCode(chart);
            organizationParameter.setOrganizationCode(org);
            Map orgParamKeys = persistenceService.getPrimaryKeyFieldValues(organizationParameter);
            orgParamKeys.put(KRADPropertyConstants.ACTIVE_INDICATOR, true);
            organizationParameter = businessObjectService.findByPrimaryKey(OrganizationParameter.class, orgParamKeys);
            purchaseOrderTotalLimit = (organizationParameter == null) ? null :
                    organizationParameter.getOrganizationAutomaticPurchaseOrderLimit();
        }

        if (ObjectUtils.isNull(purchaseOrderTotalLimit)) {
            String defaultLimit = parameterService.getParameterValueAsString(RequisitionDocument.class,
                    PurapParameterConstants.AUTOMATIC_PURCHASE_ORDER_DEFAULT_LIMIT_AMOUNT);
            purchaseOrderTotalLimit = new KualiDecimal(defaultLimit);
        }

        return purchaseOrderTotalLimit;
    }

    @Override
    public boolean isFullDocumentEntryCompleted(PurchasingAccountsPayableDocument purapDocument) {
        LOG.debug("isFullDocumentEntryCompleted() started");

        // for now just return true if not in one of the first few states
        boolean value = false;
        if (purapDocument instanceof PaymentRequestDocument) {
            value = PurapConstants.PaymentRequestStatuses.STATUS_ORDER.isFullDocumentEntryCompleted(
                    purapDocument.getApplicationDocumentStatus());
        } else if (purapDocument instanceof VendorCreditMemoDocument) {
            value = PurapConstants.CreditMemoStatuses.STATUS_ORDER.isFullDocumentEntryCompleted(
                    purapDocument.getApplicationDocumentStatus());
        }
        return value;
    }

    @Override
    public boolean isPaymentRequestFullDocumentEntryCompleted(String purapDocumentStatus) {
        LOG.debug("isPaymentRequestFullDocumentEntryCompleted() started");
        return PurapConstants.PaymentRequestStatuses.STATUS_ORDER.isFullDocumentEntryCompleted(purapDocumentStatus);
    }

    @Override
    public boolean isVendorCreditMemoFullDocumentEntryCompleted(String purapDocumentStatus) {
        LOG.debug("isVendorCreditMemoFullDocumentEntryCompleted() started");
        return PurapConstants.CreditMemoStatuses.STATUS_ORDER.isFullDocumentEntryCompleted(purapDocumentStatus);
    }

    /**
     * Main hook point for close/Reopen PO.
     */
    @Override
    public void performLogicForCloseReopenPO(PurchasingAccountsPayableDocument purapDocument) {
        LOG.debug("performLogicForCloseReopenPO() started");

        if (purapDocument instanceof PaymentRequestDocument) {
            PaymentRequestDocument paymentRequest = (PaymentRequestDocument) purapDocument;

            if (paymentRequest.isClosePurchaseOrderIndicator()
                    && PurapConstants.PurchaseOrderStatuses.APPDOC_OPEN.equals(
                            paymentRequest.getPurchaseOrderDocument().getApplicationDocumentStatus())) {
                // get the po id and get the current po
                // check the current po: if status is not closed and there is no pending action... route close po as
                // system user
                processCloseReopenPo((AccountsPayableDocumentBase) purapDocument,
                        PurapConstants.PurchaseOrderDocTypes.PURCHASE_ORDER_CLOSE_DOCUMENT);
            }

        } else if (purapDocument instanceof VendorCreditMemoDocument) {
            VendorCreditMemoDocument creditMemo = (VendorCreditMemoDocument) purapDocument;

            if (creditMemo.isReopenPurchaseOrderIndicator()
                    && PurapConstants.PurchaseOrderStatuses.APPDOC_CLOSED.equals(
                            creditMemo.getPurchaseOrderDocument().getApplicationDocumentStatus())) {
                // get the po id and get the current PO
                // route 'Re-Open PO Document' if PO criteria meets requirements from business rules
                processCloseReopenPo((AccountsPayableDocumentBase) purapDocument,
                        PurapConstants.PurchaseOrderDocTypes.PURCHASE_ORDER_REOPEN_DOCUMENT);
            }

        } else {
            throw new RuntimeException("Attempted to perform full entry logic for unhandled document type '" +
                    purapDocument.getClass().getName() + "'");
        }
    }

    /**
     * Remove items that have not been "entered" which means no data has been added to them so no more processing
     * needs to continue on these items.
     *
     * @param document AccountsPayableDocument which contains list of items to be reviewed
     */
    @Override
    public void deleteUnenteredItems(PurapItemOperations document) {
        LOG.debug("deleteUnenteredItems() started");

        List<PurapEnterableItem> deletionList = new ArrayList<>();
        for (PurapEnterableItem item : (List<PurapEnterableItem>) document.getItems()) {
            if (!item.isConsideredEntered()) {
                deletionList.add(item);
            }
        }
        document.getItems().removeAll(deletionList);
    }

    /**
     * Actual method that will close or reopen a po.
     *
     * @param apDocument AccountsPayableDocument
     * @param docType
     */
    @SuppressWarnings("unchecked")
    public void processCloseReopenPo(AccountsPayableDocumentBase apDocument, String docType) {
        LOG.debug("processCloseReopenPo() started");

        String action;
        String newStatus;
        // setup text for note that will be created, will either be closed or reopened
        if (PurapConstants.PurchaseOrderDocTypes.PURCHASE_ORDER_CLOSE_DOCUMENT.equals(docType)) {
            action = "closed";
            newStatus = PurchaseOrderStatuses.APPDOC_PENDING_CLOSE;
        } else if (PurapConstants.PurchaseOrderDocTypes.PURCHASE_ORDER_REOPEN_DOCUMENT.equals(docType)) {
            action = "reopened";
            newStatus = PurchaseOrderStatuses.APPDOC_PENDING_REOPEN;
        } else {
            String errorMessage = "Method processCloseReopenPo called using ID + '" +
                    apDocument.getPurapDocumentIdentifier() + "' and invalid doc type '" + docType + "'";
            LOG.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }

        Integer poId = apDocument.getPurchaseOrderIdentifier();
        PurchaseOrderDocument purchaseOrderDocument = purchaseOrderService.getCurrentPurchaseOrder(poId);
        if (!StringUtils.equalsIgnoreCase(purchaseOrderDocument.getDocumentHeader().getWorkflowDocument()
                .getDocumentTypeName(), docType)) {
            // we are skipping the validation above because it would be too late to correct any errors (i.e. because
            // in post-processing)
            purchaseOrderService.createAndRoutePotentialChangeDocument(purchaseOrderDocument.getDocumentNumber(),
                    docType, assemblePurchaseOrderNote(apDocument, docType, action), new ArrayList(), newStatus);
        }

        // if we made it here, route document has not errored out, so set appropriate indicator depending on what is
        // being requested.
        if (PurapConstants.PurchaseOrderDocTypes.PURCHASE_ORDER_CLOSE_DOCUMENT.equals(docType)) {
            apDocument.setClosePurchaseOrderIndicator(false);

            //add a note to the purchase order indicating it has been closed by a payment request document
            String userName = apDocument.getLastActionPerformedByPersonName();
            StringBuffer poNote = new StringBuffer();
            poNote.append("PO was closed manually by ");
            poNote.append(userName);
            poNote.append(" in approving PREQ with ID ");
            poNote.append(apDocument.getDocumentNumber());

            //save the note to the purchase order
            try {
                Note noteObj = documentService.createNoteFromDocument(apDocument.getPurchaseOrderDocument(),
                        poNote.toString());
                noteObj.setNoteTypeCode(apDocument.getPurchaseOrderDocument().getNoteType().getCode());
                apDocument.getPurchaseOrderDocument().addNote(noteObj);
                noteService.save(noteObj);
            } catch (Exception e) {
                String errorMessage = "Error creating and saving close note for purchase order with document service";
                LOG.error("processCloseReopenPo() " + errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        } else if (PurapConstants.PurchaseOrderDocTypes.PURCHASE_ORDER_REOPEN_DOCUMENT.equals(docType)) {
            apDocument.setReopenPurchaseOrderIndicator(false);
        }
    }

    /**
     * Generate a note for the close/reopen po method.
     *
     * @param docType
     * @param apDocument
     * @return Note to be saved
     */
    protected String assemblePurchaseOrderNote(AccountsPayableDocumentBase apDocument, String docType, String action) {
        LOG.debug("assemblePurchaseOrderNote() started");

        String documentLabel = dataDictionaryService.getDocumentLabelByClass(apDocument.getClass());
        StringBuffer closeReopenNote = new StringBuffer();
        String userName = GlobalVariables.getUserSession().getPerson().getName();
        closeReopenNote.append(dataDictionaryService.getDocumentLabelByTypeName(
                KFSConstants.FinancialDocumentTypeCodes.PURCHASE_ORDER));
        closeReopenNote.append(" will be manually ");
        closeReopenNote.append(action);
        closeReopenNote.append(" by ");
        closeReopenNote.append(userName);
        closeReopenNote.append(" when approving ");
        closeReopenNote.append(documentLabel);
        closeReopenNote.append(" with ");
        closeReopenNote.append(dataDictionaryService.getAttributeLabel(apDocument.getClass(),
                PurapPropertyConstants.PURAP_DOC_ID));
        closeReopenNote.append(" ");
        closeReopenNote.append(apDocument.getPurapDocumentIdentifier());

        return closeReopenNote.toString();
    }

    @Override
    public Object performLogicWithFakedUserSession(String requiredPersonPersonUserId, LogicContainer logicToRun,
            Object... objects) throws WorkflowException, Exception {
        LOG.debug("performLogicWithFakedUserSession() started");

        if (StringUtils.isBlank(requiredPersonPersonUserId)) {
            throw new RuntimeException("Attempted to perform logic with a fake user session with a blank user " +
                    "person id: '" + requiredPersonPersonUserId + "'");
        }
        if (ObjectUtils.isNull(logicToRun)) {
            throw new RuntimeException("Attempted to perform logic with a fake user session with no logic to run");
        }
        UserSession actualUserSession = GlobalVariables.getUserSession();
        try {
            GlobalVariables.setUserSession(new UserSession(requiredPersonPersonUserId));
            return logicToRun.runLogic(objects);
        } finally {
            GlobalVariables.setUserSession(actualUserSession);
        }
    }

    @Override
    public void saveDocumentNoValidation(Document document) {
        try {
            // FIXME The following code of refreshing document header is a temporary fix for the issue that in some
            // cases (seem random) the doc header fields are null; and if doc header is refreshed, the workflow doc
            // becomes null. The root cause of this is that when some docs are retrieved manually using OJB criteria,
            // ref objs such as doc header or workflow doc aren't retrieved; the solution would be to add these
            // refreshing when documents are retrieved in those OJB methods.
            if (document.getDocumentHeader() != null && document.getDocumentHeader().getDocumentNumber() == null) {
                WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
                document.refreshReferenceObject("documentHeader");
                document.getDocumentHeader().setWorkflowDocument(workflowDocument);
            }
            documentService.saveDocument(document, DocumentSystemSaveEvent.class);

            // At this point, the work-flow status will not change for the current document, but the document status
            // will. This causes the search indices for the document to become out of synch, and will show a different
            // status type in the RICE lookup results screen.
            final DocumentAttributeIndexingQueue documentAttributeIndexingQueue =
                    KewApiServiceLocator.getDocumentAttributeIndexingQueue();

            documentAttributeIndexingQueue.indexDocument(document.getDocumentNumber());
        } catch (WorkflowException we) {
            String errorMsg = "Workflow error saving document # " + document.getDocumentHeader().getDocumentNumber() +
                    " " + we.getMessage();
            LOG.error(errorMsg, we);
            throw new RuntimeException(errorMsg, we);
        } catch (NumberFormatException ne) {
            String errorMsg = "Invalid document number format for document # " +
                    document.getDocumentHeader().getDocumentNumber() + " " + ne.getMessage();
            LOG.error(errorMsg, ne);
            throw new RuntimeException(errorMsg, ne);
        }
    }

    @Override
    public boolean isDocumentStoppedInRouteNode(PurchasingAccountsPayableDocument document, String nodeName) {
        WorkflowDocument workflowDoc = document.getDocumentHeader().getWorkflowDocument();
        Set<String> currentRouteLevels = workflowDoc.getCurrentNodeNames();
        return CollectionUtils.isNotEmpty(currentRouteLevels) && currentRouteLevels.contains(nodeName)
                && workflowDoc.isApprovalRequested();
    }

    @Override
    public boolean allowEncumberNextFiscalYear() {
        LOG.debug("allowEncumberNextFiscalYear() started");

        java.util.Date today = dateTimeService.getCurrentDate();
        java.util.Date closingDate = universityDateService.getLastDateOfFiscalYear(
                universityDateService.getCurrentFiscalYear());
        int allowEncumberNext = Integer.parseInt(parameterService.getParameterValueAsString(RequisitionDocument.class,
                PurapRuleConstants.ALLOW_ENCUMBER_NEXT_YEAR_DAYS));
        int diffTodayClosing = dateTimeService.dateDiff(today, closingDate, false);

        if (ObjectUtils.isNotNull(closingDate) && ObjectUtils.isNotNull(today)
                && ObjectUtils.isNotNull(allowEncumberNext)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("allowEncumberNextFiscalYear() today = " + dateTimeService.toDateString(today) +
                        "; encumber next FY range = " + allowEncumberNext + " - " +
                        dateTimeService.toDateTimeString(today));
            }

            if (allowEncumberNext >= diffTodayClosing && diffTodayClosing >= KualiDecimal.ZERO.intValue()) {
                LOG.debug("allowEncumberNextFiscalYear() encumber next FY allowed; return true.");
                return true;
            }
        }
        LOG.debug("allowEncumberNextFiscalYear() encumber next FY not allowed; return false.");
        return false;
    }

    @Override
    public List<Integer> getAllowedFiscalYears() {
        List<Integer> allowedYears = new ArrayList<>();
        Integer currentFY = universityDateService.getCurrentFiscalYear();
        allowedYears.add(currentFY);
        if (allowEncumberNextFiscalYear()) {
            allowedYears.add(currentFY + 1);
        }
        return allowedYears;
    }

    @Override
    public boolean isTodayWithinApoAllowedRange() {
        java.util.Date today = dateTimeService.getCurrentDate();
        Integer currentFY = universityDateService.getCurrentFiscalYear();
        java.util.Date closingDate = universityDateService.getLastDateOfFiscalYear(currentFY);
        int allowApoDate = Integer.parseInt(parameterService.getParameterValueAsString(RequisitionDocument.class,
                PurapRuleConstants.ALLOW_APO_NEXT_FY_DAYS));
        int diffTodayClosing = dateTimeService.dateDiff(today, closingDate, true);

        return diffTodayClosing <= allowApoDate;
    }

    @Override
    public void clearTax(PurchasingAccountsPayableDocument purapDocument, boolean useTax) {
        for (PurApItem item : purapDocument.getItems()) {
            if (useTax) {
                item.getUseTaxItems().clear();
            } else {
                item.setItemTaxAmount(null);
            }
        }
    }

    @Override
    public void updateUseTaxIndicator(PurchasingAccountsPayableDocument purapDocument,
            boolean newUseTaxIndicatorValue) {
        boolean currentUseTaxIndicator = purapDocument.isUseTaxIndicator();
        if (currentUseTaxIndicator != newUseTaxIndicatorValue) {
            //i.e. if the indicator changed clear out the tax
            clearTax(purapDocument, currentUseTaxIndicator);
        }
        purapDocument.setUseTaxIndicator(newUseTaxIndicatorValue);
    }

    @Override
    public void calculateTax(PurchasingAccountsPayableDocument purapDocument) {
        boolean salesTaxInd = parameterService.getParameterValueAsBoolean(
                KfsParameterConstants.PURCHASING_DOCUMENT.class, PurapParameterConstants.ENABLE_SALES_TAX_IND);
        boolean useTaxIndicator = purapDocument.isUseTaxIndicator();
        String deliveryState = getDeliveryState(purapDocument);
        String deliveryPostalCode = getDeliveryPostalCode(purapDocument);
        Date transactionTaxDate = purapDocument.getTransactionTaxDate();

        //calculate if sales tax enabled for purap
        if (salesTaxInd || useTaxIndicator) {
            //iterate over items and calculate tax if taxable
            for (PurApItem item : purapDocument.getItems()) {
                if (isTaxable(useTaxIndicator, deliveryState, item)) {
                    calculateItemTax(useTaxIndicator, deliveryPostalCode, transactionTaxDate, item,
                            item.getUseTaxClass(), purapDocument);
                }
            }
        }
    }

    @Override
    public String getDeliveryState(PurchasingAccountsPayableDocument purapDocument) {
        if (purapDocument instanceof PurchasingDocument) {
            PurchasingDocument document = (PurchasingDocument) purapDocument;
            return document.getDeliveryStateCode();
        } else if (purapDocument instanceof AccountsPayableDocument) {
            AccountsPayableDocument document = (AccountsPayableDocument) purapDocument;
            if (document.getPurchaseOrderDocument() == null) {
                throw new RuntimeException("PurchaseOrder document does not exists");
            }
            return document.getPurchaseOrderDocument().getDeliveryStateCode();
        }
        return null;
    }

    protected String getDeliveryPostalCode(PurchasingAccountsPayableDocument purapDocument) {
        if (purapDocument instanceof PurchasingDocument) {
            PurchasingDocument document = (PurchasingDocument) purapDocument;
            return document.getDeliveryPostalCode();
        } else if (purapDocument instanceof AccountsPayableDocument) {
            AccountsPayableDocument docBase = (AccountsPayableDocument) purapDocument;
            if (docBase.getPurchaseOrderDocument() == null) {
                throw new RuntimeException("PurchaseOrder document does not exists");
            }
            return docBase.getPurchaseOrderDocument().getDeliveryPostalCode();
        }
        return null;
    }

    /**
     * Determines if the item is taxable based on a decision tree.
     *
     * @param useTaxIndicator
     * @param deliveryState
     * @param item
     * @return
     */
    @Override
    public boolean isTaxable(boolean useTaxIndicator, String deliveryState, PurApItem item) {
        boolean taxable = false;

        if (item.getItemType().isTaxableIndicator()
                && (ObjectUtils.isNull(item.getItemTaxAmount()) || useTaxIndicator)
                && doesCommodityAllowCallToTaxService(item)
                && doesAccountAllowCallToTaxService(deliveryState, item)) {
            taxable = true;
        }

        return taxable;
    }

    @Override
    public boolean isTaxableForSummary(boolean useTaxIndicator, String deliveryState, PurApItem item) {
        return item.getItemType().isTaxableIndicator() && doesCommodityAllowCallToTaxService(item)
                && doesAccountAllowCallToTaxService(deliveryState, item);
    }

    /**
     * @param item
     * @return true if the the tax service should be called due to the commodity code.
     */
    protected boolean doesCommodityAllowCallToTaxService(PurApItem item) {
        boolean callService = true;

        // only check for commodity code on above the line times (additional charges don't allow commodity code)
        if (item.getItemType().isLineItemIndicator()) {
            if (item instanceof PurchasingItem) {
                PurchasingItemBase purItem = (PurchasingItemBase) item;
                if (purItem.getPurchasingCommodityCode() == null) {
                    return true;
                }
                callService = isCommodityCodeTaxable(purItem.getCommodityCode());
            } else if (item instanceof AccountsPayableItem) {
                // if not a purchasing item, then pull item from PO
                AccountsPayableItem apItem = (AccountsPayableItem) item;
                PurchaseOrderItem poItem = apItem.getPurchaseOrderItem();
                if (ObjectUtils.isNotNull(poItem)) {
                    if (poItem.getPurchasingCommodityCode() == null) {
                        return true;
                    }
                    callService = isCommodityCodeTaxable(poItem.getCommodityCode());
                }
            }
        }

        return callService;
    }

    protected boolean isCommodityCodeTaxable(CommodityCode commodityCode) {
        if (ObjectUtils.isNotNull(commodityCode)) {
            return commodityCode.isSalesTaxIndicator();
        }

        return true;
    }

    @Override
    public boolean isDeliveryStateTaxable(String deliveryState) {
        ParameterEvaluator parmEval = parameterEvaluatorService
                .getParameterEvaluator(KfsParameterConstants.PURCHASING_DOCUMENT.class,
                        TaxParameters.TAXABLE_DELIVERY_STATES, deliveryState);
        return parmEval.evaluationSucceeds();
    }

    /**
     * Checks if the account is taxable, based on the delivery state, fund/subfund groups, and object code
     * level/consolidations.
     *
     * @param deliveryState
     * @param item
     * @return
     */
    protected boolean doesAccountAllowCallToTaxService(String deliveryState, PurApItem item) {
        boolean callService = false;
        boolean deliveryStateTaxable = isDeliveryStateTaxable(deliveryState);

        for (PurApAccountingLine acctLine : item.getSourceAccountingLines()) {
            if (isAccountingLineTaxable(acctLine, deliveryStateTaxable)) {
                callService = true;
                break;
            }
        }

        return callService;
    }

    @Override
    public boolean isAccountingLineTaxable(PurApAccountingLine acctLine, boolean deliveryStateTaxable) {
        boolean isTaxable = false;
        String parameterSuffix;

        if (deliveryStateTaxable) {
            parameterSuffix = TaxParameters.FOR_TAXABLE_STATES_SUFFIX;
        } else {
            parameterSuffix = TaxParameters.FOR_NON_TAXABLE_STATES_SUFFIX;
        }

        // is account (fund/subfund) and object code (level/consolidation) taxable?
        if (isAccountTaxable(parameterSuffix, acctLine) && isObjectCodeTaxable(parameterSuffix, acctLine)) {
            isTaxable = true;
        }

        return isTaxable;
    }

    /**
     * Checks if the account fund/subfund groups are in a set of parameters taking into account allowed/denied
     * constraints and ultimately determines if taxable.
     *
     * @param parameterSuffix
     * @param acctLine
     * @return
     */
    protected boolean isAccountTaxable(String parameterSuffix, PurApAccountingLine acctLine) {
        boolean isAccountTaxable = false;
        String fundParam = TaxParameters.TAXABLE_FUND_GROUPS_PREFIX + parameterSuffix;
        String subFundParam = TaxParameters.TAXABLE_SUB_FUND_GROUPS_PREFIX + parameterSuffix;
        ParameterEvaluator fundParamEval;
        ParameterEvaluator subFundParamEval;

        if (ObjectUtils.isNull(acctLine.getAccount().getSubFundGroup())) {
            acctLine.refreshNonUpdateableReferences();
        }

        fundParamEval = parameterEvaluatorService.getParameterEvaluator(KfsParameterConstants.PURCHASING_DOCUMENT.class,
                fundParam, acctLine.getAccount().getSubFundGroup().getFundGroupCode());
        subFundParamEval = parameterEvaluatorService
                .getParameterEvaluator(KfsParameterConstants.PURCHASING_DOCUMENT.class, subFundParam,
                acctLine.getAccount().getSubFundGroupCode());

        if ((isAllowedFound(fundParamEval) && (isAllowedFound(subFundParamEval)
                || isAllowedNotFound(subFundParamEval) || isDeniedNotFound(subFundParamEval)))
                || (isAllowedNotFound(fundParamEval) && isAllowedFound(subFundParamEval))
                || (isDeniedFound(fundParamEval) && isAllowedFound(subFundParamEval))
                || (isDeniedNotFound(fundParamEval) && (isAllowedFound(subFundParamEval)
                || isAllowedNotFound(subFundParamEval) || isDeniedNotFound(subFundParamEval)))) {
            isAccountTaxable = true;
        }

        return isAccountTaxable;
    }

    /**
     * Checks if the object code level/consolidation groups are in a set of parameters taking into account
     * allowed/denied constraints and ultimately determines if taxable.
     *
     * @param parameterSuffix
     * @param acctLine
     * @return
     */
    protected boolean isObjectCodeTaxable(String parameterSuffix, PurApAccountingLine acctLine) {
        boolean isObjectCodeTaxable = false;
        String levelParam = TaxParameters.TAXABLE_OBJECT_LEVELS_PREFIX + parameterSuffix;
        String consolidationParam = TaxParameters.TAXABLE_OBJECT_CONSOLIDATIONS_PREFIX + parameterSuffix;
        ParameterEvaluator levelParamEval;
        ParameterEvaluator consolidationParamEval;

        //refresh financial object level
        acctLine.getObjectCode().refreshReferenceObject("financialObjectLevel");

        levelParamEval = parameterEvaluatorService.getParameterEvaluator(KfsParameterConstants.PURCHASING_DOCUMENT.class,
                levelParam, acctLine.getObjectCode().getFinancialObjectLevelCode());
        consolidationParamEval = parameterEvaluatorService
                .getParameterEvaluator(KfsParameterConstants.PURCHASING_DOCUMENT.class, consolidationParam,
                        acctLine.getObjectCode().getFinancialObjectLevel().getFinancialConsolidationObjectCode());

        if ((isAllowedFound(levelParamEval) && (isAllowedFound(consolidationParamEval)
                || isAllowedNotFound(consolidationParamEval) || isDeniedNotFound(consolidationParamEval)))
                || (isAllowedNotFound(levelParamEval) && isAllowedFound(consolidationParamEval))
                || (isDeniedFound(levelParamEval) && isAllowedFound(consolidationParamEval))
                || (isDeniedNotFound(levelParamEval) && (isAllowedFound(consolidationParamEval)
                || isAllowedNotFound(consolidationParamEval) || isDeniedNotFound(consolidationParamEval)))) {
            isObjectCodeTaxable = true;
        }

        return isObjectCodeTaxable;
    }

    /**
     * Helper method to work with parameter evaluator to find, allowed and found in parameter value.
     *
     * @param eval
     * @return
     */
    protected boolean isAllowedFound(ParameterEvaluator eval) {
        boolean exists = false;

        if (eval.evaluationSucceeds() && eval.constraintIsAllow()) {
            exists = true;
        }

        return exists;
    }

    /**
     * Helper method to work with parameter evaluator to find, allowed and not found in parameter value.
     *
     * @param eval
     * @return
     */
    protected boolean isAllowedNotFound(ParameterEvaluator eval) {
        boolean exists = false;

        if (!eval.evaluationSucceeds() && eval.constraintIsAllow()) {
            exists = true;
        }

        return exists;
    }

    /**
     * Helper method to work with parameter evaluator to find, denied and found in parameter value.
     *
     * @param eval
     * @return
     */
    protected boolean isDeniedFound(ParameterEvaluator eval) {
        boolean exists = false;

        if (!eval.evaluationSucceeds() && !eval.constraintIsAllow()) {
            exists = true;
        }

        return exists;
    }

    /**
     * Helper method to work with parameter evaluator to find, denied and not found in parameter value.
     *
     * @param eval
     * @return
     */
    protected boolean isDeniedNotFound(ParameterEvaluator eval) {
        boolean exists = false;

        if (eval.evaluationSucceeds() && !eval.constraintIsAllow()) {
            exists = true;
        }

        return exists;
    }

    /**
     * @param useTaxIndicator
     * @param deliveryPostalCode
     * @param transactionTaxDate
     * @param item
     * @param itemUseTaxClass
     */
    @SuppressWarnings("unchecked")
    protected void calculateItemTax(boolean useTaxIndicator, String deliveryPostalCode, Date transactionTaxDate,
            PurApItem item, Class itemUseTaxClass, PurchasingAccountsPayableDocument purapDocument) {
        if (!useTaxIndicator) {
            if (!StringUtils.equals(item.getItemTypeCode(),
                        PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE)
                    && !StringUtils.equals(item.getItemTypeCode(),
                        PurapConstants.ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE)) {
                KualiDecimal taxAmount = taxService.getTotalSalesTaxAmount(transactionTaxDate, deliveryPostalCode,
                        item.getExtendedPrice());
                item.setItemTaxAmount(taxAmount);
            }
        } else {
            KualiDecimal extendedPrice = item.getExtendedPrice();

            if (StringUtils.equals(item.getItemTypeCode(), PurapConstants.ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE)) {
                extendedPrice = getFullDiscountTaxablePrice(extendedPrice, purapDocument);
            }
            List<TaxDetail> taxDetails = taxService.getUseTaxDetails(transactionTaxDate, deliveryPostalCode,
                    extendedPrice);
            List<PurApItemUseTax> newUseTaxItems = new ArrayList<>();
            if (taxDetails != null) {
                for (TaxDetail taxDetail : taxDetails) {
                    try {
                        PurApItemUseTax useTaxItem = (PurApItemUseTax) itemUseTaxClass.newInstance();
                        useTaxItem.setChartOfAccountsCode(taxDetail.getChartOfAccountsCode());
                        useTaxItem.setFinancialObjectCode(taxDetail.getFinancialObjectCode());
                        useTaxItem.setAccountNumber(taxDetail.getAccountNumber());
                        useTaxItem.setItemIdentifier(item.getItemIdentifier());
                        useTaxItem.setRateCode(taxDetail.getRateCode());
                        useTaxItem.setTaxAmount(taxDetail.getTaxAmount());
                        newUseTaxItems.add(useTaxItem);
                    } catch (Exception e) {
                        /*
                         * Shallow.. This never happen - InstantiationException/IllegalAccessException
                         * To be safe, throw a runtime exception
                         */
                        throw new RuntimeException(e);
                    }
                }
            }
            item.setUseTaxItems(newUseTaxItems);
        }
    }

    public KualiDecimal getFullDiscountTaxablePrice(KualiDecimal extendedPrice,
            PurchasingAccountsPayableDocument purapDocument) {
        KualiDecimal taxablePrice = KualiDecimal.ZERO;
        KualiDecimal taxableLineItemPrice = KualiDecimal.ZERO;
        KualiDecimal totalLineItemPrice = KualiDecimal.ZERO;
        boolean useTaxIndicator = purapDocument.isUseTaxIndicator();
        String deliveryState = getDeliveryState(purapDocument);

        // iterate over items and calculate tax if taxable
        for (PurApItem item : purapDocument.getItems()) {
            if (item.getItemType().isLineItemIndicator()) {
                //only when extended price exists
                if (ObjectUtils.isNotNull(item.getExtendedPrice())) {
                    if (isTaxable(useTaxIndicator, deliveryState, item)) {
                        taxableLineItemPrice = taxableLineItemPrice.add(item.getExtendedPrice());
                        totalLineItemPrice = totalLineItemPrice.add(item.getExtendedPrice());
                    } else {
                        totalLineItemPrice = totalLineItemPrice.add(item.getExtendedPrice());
                    }
                }
            }
        }

        //check nonzero so no divide by zero errors, and make sure extended price is not null
        if (totalLineItemPrice.isNonZero() && ObjectUtils.isNotNull(extendedPrice)) {
            taxablePrice = taxableLineItemPrice.divide(totalLineItemPrice).multiply(extendedPrice);
        }

        return taxablePrice;
    }

    @Override
    public void prorateForTradeInAndFullOrderDiscount(PurchasingAccountsPayableDocument purDoc) {
        if (purDoc instanceof VendorCreditMemoDocument) {
            throw new RuntimeException("This method not applicable for VCM documents");
        }

        //TODO: are we throwing sufficient errors in this method?
        PurApItem fullOrderDiscount = null;
        PurApItem tradeIn = null;
        KualiDecimal totalAmount;
        KualiDecimal totalTaxAmount;

        List<PurApAccountingLine> distributedAccounts;
        List<SourceAccountingLine> summaryAccounts;

        // iterate through below the line and grab FoD and TrdIn.
        for (PurApItem item : purDoc.getItems()) {
            if (item.getItemTypeCode().equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE)) {
                fullOrderDiscount = item;
            } else if (item.getItemTypeCode().equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE)) {
                tradeIn = item;
            }
        }
        // If Discount is not null or zero get proration list for all non misc items and set (if not empty?)
        if (fullOrderDiscount != null && fullOrderDiscount.getExtendedPrice() != null
                && fullOrderDiscount.getExtendedPrice().isNonZero()) {
            // empty
            KNSGlobalVariables.getMessageList().add("Full order discount accounts cleared and regenerated");
            fullOrderDiscount.getSourceAccountingLines().clear();
            //total amount is pretax dollars
            totalAmount = purDoc.getTotalDollarAmountAboveLineItems().subtract(
                    purDoc.getTotalTaxAmountAboveLineItems());
            totalTaxAmount = purDoc.getTotalTaxAmountAboveLineItems();

            //Before we generate account summary, we should update the account amounts first.
            purapAccountingService.updateAccountAmounts(purDoc);

            //calculate tax
            boolean salesTaxInd = parameterService.getParameterValueAsBoolean(
                    KfsParameterConstants.PURCHASING_DOCUMENT.class, PurapParameterConstants.ENABLE_SALES_TAX_IND);
            boolean useTaxIndicator = purDoc.isUseTaxIndicator();

            if (salesTaxInd && (ObjectUtils.isNull(fullOrderDiscount.getItemTaxAmount()) && !useTaxIndicator)) {
                KualiDecimal discountAmount = fullOrderDiscount.getExtendedPrice();
                KualiDecimal discountTaxAmount = discountAmount.divide(totalAmount).multiply(totalTaxAmount);

                fullOrderDiscount.setItemTaxAmount(discountTaxAmount);
            }

            summaryAccounts = purapAccountingService.generateSummary(PurApItemUtils.getAboveTheLineOnly(
                    purDoc.getItems()));

            if (summaryAccounts.size() == 0) {
                if (purDoc.shouldGiveErrorForEmptyAccountsProration()) {
                    GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY,
                            PurapKeyConstants.ERROR_SUMMARY_ACCOUNTS_LIST_EMPTY, "full order discount");
                }
            } else {
                //prorate accounts
                distributedAccounts = purapAccountingService.generateAccountDistributionForProration(summaryAccounts,
                        totalAmount.add(totalTaxAmount), 2, fullOrderDiscount.getAccountingLineClass());

                for (PurApAccountingLine distributedAccount : distributedAccounts) {
                    BigDecimal percent = distributedAccount.getAccountLinePercent();
                    BigDecimal roundedPercent = new BigDecimal(Math.round(percent.doubleValue()));
                    distributedAccount.setAccountLinePercent(roundedPercent);
                }

                //update amounts on distributed accounts
                purapAccountingService.updateAccountAmountsWithTotal(distributedAccounts, totalAmount,
                        fullOrderDiscount.getTotalAmount());

                fullOrderDiscount.setSourceAccountingLines(distributedAccounts);
            }
        } else if (fullOrderDiscount != null && (fullOrderDiscount.getExtendedPrice() == null
                || fullOrderDiscount.getExtendedPrice().isZero())) {
            fullOrderDiscount.getSourceAccountingLines().clear();
        }

        // If tradeIn is not null or zero get proration list for all non misc items and set (if not empty?)
        if (tradeIn != null && tradeIn.getExtendedPrice() != null && tradeIn.getExtendedPrice().isNonZero()) {
            tradeIn.getSourceAccountingLines().clear();

            totalAmount = purDoc.getTotalDollarAmountForTradeIn();
            KualiDecimal tradeInTotalAmount = tradeIn.getTotalAmount();
            //Before we generate account summary, we should update the account amounts first.
            purapAccountingService.updateAccountAmounts(purDoc);

            // Before generating the summary, lets replace the object code in a cloned accounts collection so that we
            // can consolidate all the modified object codes during summary generation.
            List<PurApItem> clonedTradeInItems = new ArrayList<>();
            Collection<String> objectSubTypesRequiringQty = new ArrayList<>(parameterService
                    .getParameterValuesAsString(KfsParameterConstants.PURCHASING_DOCUMENT.class,
                            PurapParameterConstants.OBJECT_SUB_TYPES_REQUIRING_QUANTITY));
            Collection<String> purchasingObjectSubTypes = new ArrayList<>(parameterService
                    .getParameterValuesAsString(KfsParameterConstants.CAPITAL_ASSETS_DOCUMENT.class,
                            PurapParameterConstants.PURCHASING_OBJECT_SUB_TYPES));

            String tradeInCapitalObjectCode = parameterService.getParameterValueAsString(PurapConstants.PURAP_NAMESPACE,
                    "Document", "TRADE_IN_OBJECT_CODE_FOR_CAPITAL_ASSET");
            String tradeInCapitalLeaseObjCd = parameterService.getParameterValueAsString(PurapConstants.PURAP_NAMESPACE,
                    "Document", "TRADE_IN_OBJECT_CODE_FOR_CAPITAL_LEASE");

            for (PurApItem item : purDoc.getTradeInItems()) {
                PurApItem cloneItem = (PurApItem) ObjectUtils.deepCopy(item);
                List<PurApAccountingLine> sourceAccountingLines = cloneItem.getSourceAccountingLines();
                for (PurApAccountingLine accountingLine : sourceAccountingLines) {
                    if (objectSubTypesRequiringQty.contains(
                            accountingLine.getObjectCode().getFinancialObjectSubTypeCode())) {
                        accountingLine.setFinancialObjectCode(tradeInCapitalObjectCode);
                    } else if (purchasingObjectSubTypes.contains(
                            accountingLine.getObjectCode().getFinancialObjectSubTypeCode())) {
                        accountingLine.setFinancialObjectCode(tradeInCapitalLeaseObjCd);
                    }
                }
                clonedTradeInItems.add(cloneItem);
            }

            summaryAccounts = purapAccountingService.generateSummary(clonedTradeInItems);
            if (summaryAccounts.size() == 0) {
                if (purDoc.shouldGiveErrorForEmptyAccountsProration()) {
                    GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY,
                            PurapKeyConstants.ERROR_SUMMARY_ACCOUNTS_LIST_EMPTY, "trade in");
                }
            } else {
                distributedAccounts = purapAccountingService.generateAccountDistributionForProration(summaryAccounts,
                        totalAmount, 2, tradeIn.getAccountingLineClass());
                for (PurApAccountingLine distributedAccount : distributedAccounts) {
                    BigDecimal percent = distributedAccount.getAccountLinePercent();
                    BigDecimal roundedPercent = new BigDecimal(Math.round(percent.doubleValue()));
                    distributedAccount.setAccountLinePercent(roundedPercent);
                    // set the accountAmount same as tradeIn amount not line item's amount
                    resetAccountAmount(distributedAccount, tradeInTotalAmount);
                }
                tradeIn.setSourceAccountingLines(distributedAccounts);
            }
        }
    }

    private void resetAccountAmount(PurApAccountingLine distributedAccount, KualiDecimal tradeInTotalAmount) {
        BigDecimal pct = distributedAccount.getAccountLinePercent();
        BigDecimal amount = tradeInTotalAmount.bigDecimalValue().multiply(pct).divide(new BigDecimal(100));
        distributedAccount.setAmount(new KualiDecimal(amount));
    }

    @Override
    public void clearAllTaxes(PurchasingAccountsPayableDocument purapDoc) {
        if (purapDoc.getItems() != null) {
            for (int i = 0; i < purapDoc.getItems().size(); i++) {
                PurApItem item = purapDoc.getItems().get(i);
                if (purapDoc.isUseTaxIndicator()) {
                    item.setUseTaxItems(new ArrayList<>());
                } else {
                    item.setItemTaxAmount(null);
                }
            }
        }
    }

    /**
     * Determines if the item type specified conflict with the Account tax policy.
     *
     * @param purchasingDocument purchasing document to check
     * @param item               item to check if in conflict with tax policy
     * @return true if item is in conflict, false otherwise
     */
    @Override
    public boolean isItemTypeConflictWithTaxPolicy(PurchasingDocument purchasingDocument, PurApItem item) {
        boolean conflict = false;

        String deliveryState = getDeliveryState(purchasingDocument);
        if (item.getItemType().isLineItemIndicator()) {
            if (item.getItemType().isTaxableIndicator()) {
                if (isTaxDisabledForVendor(purchasingDocument)) {
                    conflict = true;
                }
            }
            // only check account tax policy if accounting line exists
            if (!item.getSourceAccountingLines().isEmpty()) {
                if (!doesAccountAllowCallToTaxService(deliveryState, item)) {
                    conflict = true;
                }
            }
        }
        return conflict;
    }

    /**
     * Determines if tax is disabled for vendor, in default always returns false
     *
     * @param purapDocument the PurchasingDocument with a vendor to check
     * @return true if tax is disabled, false if it is not - in foundation KFS, tax is never disabled
     */
    protected boolean isTaxDisabledForVendor(PurchasingDocument purapDocument) {
        return false;
    }

    public PurapAccountingService getPurapAccountingService() {
        return purapAccountingService;
    }

    public void setPurapAccountingService(PurapAccountingService purapAccountingService) {
        this.purapAccountingService = purapAccountingService;
    }

    public void setParameterEvaluatorService(ParameterEvaluatorService parameterEvaluatorService) {
        this.parameterEvaluatorService = parameterEvaluatorService;
    }
}

