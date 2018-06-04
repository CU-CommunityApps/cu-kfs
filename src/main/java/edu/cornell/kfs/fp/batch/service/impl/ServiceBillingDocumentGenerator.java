package edu.cornell.kfs.fp.batch.service.impl;

import java.util.function.Supplier;

import org.kuali.kfs.fp.document.ServiceBillingDocument;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;

public class ServiceBillingDocumentGenerator extends InternalBillingDocumentGenerator {

    public ServiceBillingDocumentGenerator() {
        super();
    }

    public ServiceBillingDocumentGenerator(
            Supplier<Note> emptyNoteGenerator, Supplier<AdHocRoutePerson> emptyAdHocRoutePersonGenerator) {
        super(emptyNoteGenerator, emptyAdHocRoutePersonGenerator);
    }

    @Override
    public Class<? extends ServiceBillingDocument> getDocumentClass() {
        return ServiceBillingDocument.class;
    }

}
