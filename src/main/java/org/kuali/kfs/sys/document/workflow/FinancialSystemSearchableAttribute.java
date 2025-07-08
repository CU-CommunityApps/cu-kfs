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
package org.kuali.kfs.sys.document.workflow;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountGlobal;
import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.coa.document.AccountGlobalMaintainableImpl;
import org.kuali.kfs.core.api.CoreConstants;
import org.kuali.kfs.core.api.uif.AttributeError;
import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.datadictionary.legacy.DocumentDictionaryService;
import org.kuali.kfs.kew.api.document.attribute.DocumentAttribute;
import org.kuali.kfs.kew.api.document.attribute.DocumentAttributeDecimal;
import org.kuali.kfs.kew.api.document.attribute.DocumentAttributeString;
import org.kuali.kfs.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.rule.bo.RuleAttribute;
import org.kuali.kfs.kns.datadictionary.DocumentEntry;
import org.kuali.kfs.kns.lookup.LookupUtils;
import org.kuali.kfs.kns.service.DictionaryValidationService;
import org.kuali.kfs.kns.util.FieldUtils;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.kns.web.ui.Row;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.workflow.attribute.DataDictionarySearchableAttribute;
import org.kuali.kfs.module.ld.businessobject.LaborLedgerPendingEntry;
import org.kuali.kfs.module.ld.document.LaborLedgerPostingDocument;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.AmountTotaling;
import org.kuali.kfs.sys.document.FinancialSystemMaintenanceDocument;
import org.kuali.kfs.sys.document.GeneralLedgerPostingDocument;
import org.kuali.kfs.sys.document.datadictionary.AccountingLineGroupDefinition;
import org.kuali.kfs.sys.document.datadictionary.FinancialSystemTransactionalDocumentEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.kfs.sys.CUKFSConstants;

//RICE20 This class needs to be fixed to support pre-rice2.0 features
public class FinancialSystemSearchableAttribute extends DataDictionarySearchableAttribute {

    private static final Logger LOG = LogManager.getLogger();

    protected static final String DISPLAY_TYPE_SEARCH_ATTRIBUTE_LABEL = "Search Result Type";
    protected static final String WORKFLOW_DISPLAY_TYPE_LABEL = "Workflow Data";
    protected static final String DOCUMENT_DISPLAY_TYPE_LABEL = "Document Specific Data";
    protected static final String WORKFLOW_DISPLAY_TYPE_VALUE = "workflow";
    protected static final String DOCUMENT_DISPLAY_TYPE_VALUE = "document";
    protected static final String DISPLAY_TYPE_SEARCH_ATTRIBUTE_NAME = "displayType";

    protected static final List<KeyValue> SEARCH_RESULT_TYPE_OPTION_LIST = new ArrayList<>(2);

    static {
        SEARCH_RESULT_TYPE_OPTION_LIST.add(new ConcreteKeyValue(DOCUMENT_DISPLAY_TYPE_VALUE, DOCUMENT_DISPLAY_TYPE_LABEL));
        SEARCH_RESULT_TYPE_OPTION_LIST.add(new ConcreteKeyValue(WORKFLOW_DISPLAY_TYPE_VALUE, WORKFLOW_DISPLAY_TYPE_LABEL));
    }

    // used to map the special fields to the DD Entry that validate it.
    private static final Map<String, String> magicFields = new HashMap<>();

    static {
        magicFields.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, SourceAccountingLine.class.getSimpleName());
        magicFields.put(KFSPropertyConstants.ORGANIZATION_CODE, Organization.class.getSimpleName());
        magicFields.put(KFSPropertyConstants.ACCOUNT_NUMBER, SourceAccountingLine.class.getSimpleName());
        magicFields.put(KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE, GeneralLedgerPendingEntry.class.getSimpleName());
        magicFields.put(KFSPropertyConstants.FINANCIAL_DOCUMENT_TOTAL_AMOUNT, DocumentHeader.class.getSimpleName());
    }

    @Override
    protected List<Row> getSearchingRows(final String documentTypeName) {
        LOG.debug("getSearchingRows( {} )", documentTypeName);
        LOG.trace("Stack Trace at point of call", Throwable::new);

        final List<Row> docSearchRows = super.getSearchingRows(documentTypeName);

        // add account number search field when selected document type is COA
        if (StringUtils.isNotEmpty(documentTypeName)) {
            if(CUKFSConstants.COA_DOCUMENT_TYPE.equalsIgnoreCase( documentTypeName)){
                final Field accountField = FieldUtils.getPropertyField(Account.class, KFSPropertyConstants.ACCOUNT_NUMBER, true);
                accountField.setFieldDataType(CoreConstants.DATA_TYPE_STRING);
                accountField.setColumnVisible(true);
                docSearchRows.add(new Row(Collections.singletonList(accountField)));
            }
        }

        final DocumentEntry entry = SpringContext.getBean(DocumentDictionaryService.class).getDocumentEntry(documentTypeName);
        if (entry != null) {
            final Class<? extends Document> docClass = entry.getDocumentClass();

            if (AccountingDocument.class.isAssignableFrom(docClass)) {
                final Map<String, AccountingLineGroupDefinition> alGroups =
                        ((FinancialSystemTransactionalDocumentEntry) entry).getAccountingLineGroups();
                Class alClass = SourceAccountingLine.class;

                if (ObjectUtils.isNotNull(alGroups)) {
                    if (alGroups.containsKey("source")) {
                        alClass = alGroups.get("source").getAccountingLineClass();
                    }
                }

                final BusinessObject alBusinessObject;
                try {
                    alBusinessObject = (BusinessObject) alClass.newInstance();
                } catch (final Exception cnfe) {
                    throw new RuntimeException("Unable to instantiate accounting line class: " + alClass, cnfe);
                }

                final Field chartField = FieldUtils.getPropertyField(alClass, KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE,
                        true);
                chartField.setFieldDataType(CoreConstants.DATA_TYPE_STRING);
                chartField.setColumnVisible(true);
                LookupUtils.setFieldQuickfinder(alBusinessObject, KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE,
                        chartField, Collections.singletonList(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE));
                docSearchRows.add(new Row(Collections.singletonList(chartField)));

                final Field orgField = FieldUtils.getPropertyField(Organization.class, KFSPropertyConstants.ORGANIZATION_CODE,
                        true);
                orgField.setFieldDataType(CoreConstants.DATA_TYPE_STRING);
                orgField.setColumnVisible(true);
                LookupUtils.setFieldQuickfinder(new Account(), KFSPropertyConstants.ORGANIZATION_CODE, orgField,
                        Collections.singletonList(KFSPropertyConstants.ORGANIZATION_CODE));
                docSearchRows.add(new Row(Collections.singletonList(orgField)));

                final Field accountField = FieldUtils.getPropertyField(alClass, KFSPropertyConstants.ACCOUNT_NUMBER, true);
                accountField.setFieldDataType(CoreConstants.DATA_TYPE_STRING);
                accountField.setColumnVisible(true);
                LookupUtils.setFieldQuickfinder(alBusinessObject, KFSPropertyConstants.ACCOUNT_NUMBER, accountField,
                        Collections.singletonList(KFSPropertyConstants.ACCOUNT_NUMBER));
                docSearchRows.add(new Row(Collections.singletonList(accountField)));
            }

            boolean displayedLedgerPostingDoc = false;
            if (LaborLedgerPostingDocument.class.isAssignableFrom(docClass)) {
                final Field searchField = FieldUtils.getPropertyField(GeneralLedgerPendingEntry.class,
                        KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE, true);
                searchField.setFieldDataType(CoreConstants.DATA_TYPE_STRING);
                LookupUtils.setFieldQuickfinder(new GeneralLedgerPendingEntry(),
                        KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE, searchField,

                        Collections.singletonList(KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE));
                docSearchRows.add(new Row(Collections.singletonList(searchField)));
                displayedLedgerPostingDoc = true;
            }

            if (GeneralLedgerPostingDocument.class.isAssignableFrom(docClass) && !displayedLedgerPostingDoc) {
                final Field searchField = FieldUtils.getPropertyField(GeneralLedgerPendingEntry.class,
                        KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE, true);
                searchField.setFieldDataType(CoreConstants.DATA_TYPE_STRING);
                LookupUtils.setFieldQuickfinder(new GeneralLedgerPendingEntry(),
                        KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE, searchField,
                        Collections.singletonList(KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE));
                docSearchRows.add(new Row(Collections.singletonList(searchField)));
            }

            if (AmountTotaling.class.isAssignableFrom(docClass)) {
                final Field searchField = FieldUtils.getPropertyField(DocumentHeader.class,
                        KFSPropertyConstants.FINANCIAL_DOCUMENT_TOTAL_AMOUNT, true);
                searchField.setFieldDataType(CoreConstants.DATA_TYPE_FLOAT);
                docSearchRows.add(new Row(Collections.singletonList(searchField)));
            }
        }

        final Row resultType = createSearchResultDisplayTypeRow();
        docSearchRows.add(resultType);
        LOG.debug("Returning Rows: {}", docSearchRows);
        return docSearchRows;
    }

    @Override
    public List<DocumentAttribute> extractDocumentAttributes(
            final RuleAttribute ruleAttribute,
            final DocumentRouteHeaderValue document) {
        LOG.debug("ruleAttributes( {}, {} )", ruleAttribute, document);
        final List<DocumentAttribute> searchAttrValues = super.extractDocumentAttributes(ruleAttribute, document);

        final String docId = document.getDocumentId();
        final DocumentService docService = SpringContext.getBean(DocumentService.class);
        Document doc = null;
        doc = docService.getByDocumentHeaderId(docId);
        if (doc != null) {
            if (doc instanceof AmountTotaling && ((AmountTotaling) doc).getTotalDollarAmount() != null) {
                final DocumentAttributeDecimal searchableAttributeValue =
                        new DocumentAttributeDecimal(KFSPropertyConstants.FINANCIAL_DOCUMENT_TOTAL_AMOUNT,
                                ((AmountTotaling) doc).getTotalDollarAmount().bigDecimalValue());
                searchAttrValues.add(searchableAttributeValue);
            }

            if (doc instanceof AccountingDocument) {
                final AccountingDocument accountingDoc = (AccountingDocument) doc;
                searchAttrValues.addAll(harvestAccountingDocumentSearchableAttributes(accountingDoc));
            }

            boolean indexedLedgerDoc = false;
            if (doc instanceof LaborLedgerPostingDocument) {
                final LaborLedgerPostingDocument LLPostingDoc = (LaborLedgerPostingDocument) doc;
                searchAttrValues.addAll(harvestLLPDocumentSearchableAttributes(LLPostingDoc));
                indexedLedgerDoc = true;
            }

            if (doc instanceof GeneralLedgerPostingDocument && !indexedLedgerDoc) {
                final GeneralLedgerPostingDocument GLPostingDoc = (GeneralLedgerPostingDocument) doc;
                searchAttrValues.addAll(harvestGLPDocumentSearchableAttributes(GLPostingDoc));
            }

            DocumentHeader docHeader = doc.getDocumentHeader();

            if (ObjectUtils.isNotNull(docHeader) && ObjectUtils.isNotNull(docHeader.getWorkflowDocument())  && CUKFSConstants.GACC_DOCUMENT_TYPE.equalsIgnoreCase(docHeader.getWorkflowDocument().getDocumentTypeName())) {
                for ( AccountGlobalDetail detail : ((AccountGlobal)((AccountGlobalMaintainableImpl)((FinancialSystemMaintenanceDocument)doc).getNewMaintainableObject()).getBusinessObject()).getAccountGlobalDetails()){                   
                    if (!StringUtils.isBlank(detail.getAccountNumber())) {
                        DocumentAttributeString searchableAttributeValue = new DocumentAttributeString(
                                KFSPropertyConstants.ACCOUNT_NUMBER, detail.getAccountNumber());
                        searchAttrValues.add(searchableAttributeValue);
                    }
                }
            }

        }
        return searchAttrValues;
    }

    @Override
    public List<AttributeError> validateDocumentAttributeCriteria(
            final RuleAttribute ruleAttribute,
            final DocumentSearchCriteria documentSearchCriteria) {
        LOG.debug("validateDocumentAttributeCriteria( {}, {} )", ruleAttribute, documentSearchCriteria);
        // this list is irrelevant. the validation errors are put on the stack in the validationService.
        final List<AttributeError> errors = super.validateDocumentAttributeCriteria(ruleAttribute,
                documentSearchCriteria);

        final DictionaryValidationService validationService = SpringContext.getBean(DictionaryValidationService.class);
        final Map<String, List<String>> paramMap = documentSearchCriteria.getDocumentAttributeValues();
        for (final String key : paramMap.keySet()) {
            final List<String> values = paramMap.get(key);
            if (values != null && !values.isEmpty()) {
                for (final String value : values) {
                    if (StringUtils.isNotEmpty(value)) {
                        if (magicFields.containsKey(key)) {
                            validationService.validateAttributeFormat(magicFields.get(key), key, value, key);
                        }
                    }
                }
            }
        }

        retrieveValidationErrorsFromGlobalVariables(errors);

        return errors;
    }

    /**
     * Harvest chart of accounts code, account number, and organization code as searchable attributes from an
     * accounting document
     *
     * @param accountingDoc the accounting document to pull values from
     * @return a List of searchable values
     */
    protected List<DocumentAttribute> harvestAccountingDocumentSearchableAttributes(final AccountingDocument accountingDoc) {
        final List<DocumentAttribute> searchAttrValues = new ArrayList<>();

        for (final AccountingLine line : (List<AccountingLine>) accountingDoc.getSourceAccountingLines()) {
            addSearchableAttributesForAccountingLine(searchAttrValues, line);
        }
        for (final AccountingLine line : (List<AccountingLine>) accountingDoc.getTargetAccountingLines()) {
            addSearchableAttributesForAccountingLine(searchAttrValues, line);
        }

        return searchAttrValues;
    }

    /**
     * Harvest GLPE document type as searchable attributes from a GL posting document
     *
     * @param doc the GLP document to pull values from
     * @return a List of searchable values
     */
    protected List<DocumentAttribute> harvestGLPDocumentSearchableAttributes(final GeneralLedgerPostingDocument doc) {
        final List<DocumentAttribute> searchAttrValues = new ArrayList<>();

        for (final GeneralLedgerPendingEntry glpe : doc.getGeneralLedgerPendingEntries()) {
            addSearchableAttributesForGLPE(searchAttrValues, glpe);
        }
        return searchAttrValues;
    }

    /**
     * Harvest LLPE document type as searchable attributes from a LL posting document
     *
     * @param LLPDoc the LLP document to pull values from
     * @return a List of searchable values
     */
    protected List<DocumentAttribute> harvestLLPDocumentSearchableAttributes(final LaborLedgerPostingDocument LLPDoc) {
        final List<DocumentAttribute> searchAttrValues = new ArrayList<>();

        for (final Object llpeObj : LLPDoc.getLaborLedgerPendingEntriesForSearching()) {
            final LaborLedgerPendingEntry llpe = (LaborLedgerPendingEntry) llpeObj;
            addSearchableAttributesForLLPE(searchAttrValues, llpe);
        }
        return searchAttrValues;
    }

    /**
     * Pulls the default searchable attributes - chart code, account number, and account organization code - from a
     * given accounting line and populates the searchable attribute values in the given list
     *
     * @param searchAttrValues a List of SearchableAttributeValue objects to populate
     * @param accountingLine   an AccountingLine to get values from
     */
    protected void addSearchableAttributesForAccountingLine(
            final List<DocumentAttribute> searchAttrValues,
            final AccountingLine accountingLine) {
        DocumentAttributeString searchableAttributeValue;
        if (ObjectUtils.isNotNull(accountingLine)) {
            if (StringUtils.isNotBlank(accountingLine.getChartOfAccountsCode())) {
                searchableAttributeValue = new DocumentAttributeString(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE,
                        accountingLine.getChartOfAccountsCode());
                searchAttrValues.add(searchableAttributeValue);
            }

            if (StringUtils.isNotBlank(accountingLine.getAccountNumber())) {
                searchableAttributeValue = new DocumentAttributeString(KFSPropertyConstants.ACCOUNT_NUMBER,
                        accountingLine.getAccountNumber());
                searchAttrValues.add(searchableAttributeValue);
            }

            if (ObjectUtils.isNotNull(accountingLine.getAccount())
                    && StringUtils.isNotBlank(accountingLine.getAccount().getOrganizationCode())) {
                searchableAttributeValue = new DocumentAttributeString(KFSPropertyConstants.ORGANIZATION_CODE,
                        accountingLine.getAccount().getOrganizationCode());
                searchAttrValues.add(searchableAttributeValue);
            }
        }
    }

    /**
     * Pulls the default searchable attribute - financialSystemTypeCode - from a given accounting line and populates
     * the searchable attribute values in the given list
     *
     * @param searchAttrValues a List of SearchableAttributeValue objects to populate
     * @param glpe             a GeneralLedgerPendingEntry to get values from
     */
    protected void addSearchableAttributesForGLPE(
            final List<DocumentAttribute> searchAttrValues,
            final GeneralLedgerPendingEntry glpe) {
        if (glpe != null && StringUtils.isNotBlank(glpe.getFinancialDocumentTypeCode())) {
            final DocumentAttributeString searchableAttributeValue = new DocumentAttributeString(
                    KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE, glpe.getFinancialDocumentTypeCode());
            searchAttrValues.add(searchableAttributeValue);
        }
    }

    /**
     * Pulls the default searchable attribute - financialSystemTypeCode from a given accounting line and populates
     * the searchable attribute values in the given list
     *
     * @param searchAttrValues a List of SearchableAttributeValue objects to populate
     * @param llpe             a LaborLedgerPendingEntry to get values from
     */
    protected void addSearchableAttributesForLLPE(
            final List<DocumentAttribute> searchAttrValues,
            final LaborLedgerPendingEntry llpe) {
        if (llpe != null && StringUtils.isNotBlank(llpe.getFinancialDocumentTypeCode())) {
            final DocumentAttributeString searchableAttributeValue =
                    new DocumentAttributeString(KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE,
                            llpe.getFinancialDocumentTypeCode());
            searchAttrValues.add(searchableAttributeValue);
        }
    }

    protected Row createSearchResultDisplayTypeRow() {
        final Field searchField = new Field(DISPLAY_TYPE_SEARCH_ATTRIBUTE_NAME, DISPLAY_TYPE_SEARCH_ATTRIBUTE_LABEL);
        searchField.setFieldType(Field.RADIO);
        searchField.setIndexedForSearch(false);
        searchField.setBusinessObjectClassName("");
        searchField.setFieldHelpName("");
        searchField.setFieldHelpSummary("");
        searchField.setColumnVisible(false);
        searchField.setFieldValidValues(SEARCH_RESULT_TYPE_OPTION_LIST);
        searchField.setPropertyValue(DOCUMENT_DISPLAY_TYPE_VALUE);
        searchField.setDefaultValue(DOCUMENT_DISPLAY_TYPE_VALUE);
        return new Row(Collections.singletonList(searchField));
    }
}
