package edu.cornell.kfs.fp.batch.service.impl;

import java.util.function.Supplier;

import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.mockito.Mockito;

public class TestableCuDisbursementVoucherDocumentGenerator extends CuDisbursementVoucherDocumentGenerator {
    
    public TestableCuDisbursementVoucherDocumentGenerator() {
        super();
    }
    
    public TestableCuDisbursementVoucherDocumentGenerator(Supplier<Note> emptyNoteGenerator, Supplier<AdHocRoutePerson> emptyAdHocRoutePersonGenerator) {
        super(emptyNoteGenerator, emptyAdHocRoutePersonGenerator);
    }
    
    @Override
    protected org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeExpense buildNonEmployeeExpense() {
        org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeExpense expense = Mockito.spy(new org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeExpense());
        Mockito.doNothing().when(expense).refreshNonUpdateableReferences();
        return expense;
    }
}
