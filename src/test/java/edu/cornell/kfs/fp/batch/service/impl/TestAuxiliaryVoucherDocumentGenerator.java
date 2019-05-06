package edu.cornell.kfs.fp.batch.service.impl;

import java.util.function.Supplier;

import org.joda.time.DateTime;
import org.kuali.kfs.fp.document.AuxiliaryVoucherDocument;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;

import edu.cornell.kfs.fp.CuFPTestConstants;
import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

public class TestAuxiliaryVoucherDocumentGenerator extends AuxiliaryVoucherDocumentGenerator {

    public TestAuxiliaryVoucherDocumentGenerator() {
        super();
    }

    public TestAuxiliaryVoucherDocumentGenerator(
            Supplier<Note> emptyNoteGenerator, Supplier<AdHocRoutePerson> emptyAdHocRoutePersonGenerator) {
        super(emptyNoteGenerator, emptyAdHocRoutePersonGenerator);
    }

    @Override
    protected DateTime getDocumentCreateDate(AuxiliaryVoucherDocument document) {
        return StringToJavaDateAdapter.parseToDateTime(CuFPTestConstants.DATE_02_21_2019);
    }
}
