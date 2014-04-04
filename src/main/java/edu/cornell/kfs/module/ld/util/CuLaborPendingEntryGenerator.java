package edu.cornell.kfs.module.ld.util;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.module.ld.businessobject.LaborLedgerPendingEntry;
import org.kuali.kfs.module.ld.document.service.LaborPendingEntryConverterService;
import org.kuali.kfs.module.ld.util.LaborPendingEntryGenerator;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.context.SpringContext;
import edu.cornell.kfs.module.ld.document.service.impl.CuLaborPendingEntryConverterServiceImpl;

public class CuLaborPendingEntryGenerator extends LaborPendingEntryGenerator {
    public static List<LaborLedgerPendingEntry> generateOffsetPendingEntries(List<LaborLedgerPendingEntry> expenseEntries, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        List<LaborLedgerPendingEntry> offsetPendingEntries = new ArrayList<LaborLedgerPendingEntry>();
        for (LaborLedgerPendingEntry expenseEntry : expenseEntries) {
            offsetPendingEntries.addAll(((CuLaborPendingEntryConverterServiceImpl) SpringContext.getBean(LaborPendingEntryConverterService.class))
                    .getOffsetPendingEntries(expenseEntry, sequenceHelper));
        }
          
        return offsetPendingEntries;
    }
}
