package edu.cornell.kfs.fp.batch.service.impl;

import java.sql.Timestamp;
import java.util.function.Supplier;

import org.kuali.kfs.fp.businessobject.InternalBillingItem;
import org.kuali.kfs.fp.document.InternalBillingDocument;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentItem;

public class InternalBillingDocumentGenerator
        extends AccountingDocumentGeneratorBase<InternalBillingDocument> {

    public InternalBillingDocumentGenerator() {
        super();
    }

    public InternalBillingDocumentGenerator(
            Supplier<Note> emptyNoteGenerator, Supplier<AdHocRoutePerson> emptyAdHocRoutePersonGenerator) {
        super(emptyNoteGenerator, emptyAdHocRoutePersonGenerator);
    }

    @Override
    public Class<? extends InternalBillingDocument> getDocumentClass() {
        return InternalBillingDocument.class;
    }

    @Override
    protected void populateCustomAccountingDocumentData(
            InternalBillingDocument document, AccountingXmlDocumentEntry documentEntry) {
        super.populateCustomAccountingDocumentData(document, documentEntry);
        populateItems(document, documentEntry);
    }

    protected void populateItems(InternalBillingDocument document, AccountingXmlDocumentEntry documentEntry) {
        String documentNumber = document.getDocumentNumber();
        documentEntry.getItems().stream()
                .map((xmlItem) -> buildInternalBillingItem(xmlItem, documentNumber))
                .forEach(document::addItem);
    }

    protected InternalBillingItem buildInternalBillingItem(AccountingXmlDocumentItem xmlItem, String documentNumber) {
        InternalBillingItem item = new InternalBillingItem();
        
        item.setDocumentNumber(documentNumber);
        item.setItemStockNumber(xmlItem.getStockNumber());
        item.setItemStockDescription(xmlItem.getDescription());
        item.setItemQuantity(xmlItem.getQuantity());
        item.setUnitOfMeasureCode(xmlItem.getUnitOfMeasureCode());
        item.setItemUnitAmount(xmlItem.getItemCost());
        
        if (xmlItem.getServiceDate() != null) {
            Timestamp serviceDate = new Timestamp(xmlItem.getServiceDate().getTime());
            item.setItemServiceDate(serviceDate);
        }
        
        return item;
    }

}
