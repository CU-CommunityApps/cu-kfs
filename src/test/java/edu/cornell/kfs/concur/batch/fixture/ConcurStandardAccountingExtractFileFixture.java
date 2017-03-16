package edu.cornell.kfs.concur.batch.fixture;

import java.sql.Date;
import java.util.Calendar;

import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;

public class ConcurStandardAccountingExtractFileFixture {
    
    public static ConcurStandardAccountingExtractFile buildConcurStandardAccountingExtractFile(KualiDecimal[] debits, KualiDecimal[] credits) {
        ConcurStandardAccountingExtractFile file = new ConcurStandardAccountingExtractFile();
        Date today = new Date(Calendar.getInstance().getTimeInMillis());
        file.setBatchDate(today);
        file.setRecordCount(new Integer(debits.length + credits.length));
        
        KualiDecimal journalTotal = KualiDecimal.ZERO;
        for (KualiDecimal debitAmount : debits) {
            journalTotal = journalTotal.add(debitAmount);
            file.getConcurStandardAccountingExtractDetailLines().add(buildConcurStandardAccountingExtractDetailLine(
                    ConcurConstants.DEBIT, debitAmount));
        }
        for (KualiDecimal creditAmount : credits) {
            journalTotal = journalTotal.add(creditAmount);
            file.getConcurStandardAccountingExtractDetailLines().add(buildConcurStandardAccountingExtractDetailLine(
                    ConcurConstants.CREDIT, creditAmount));
        }
        file.setJournalAmountTotal(journalTotal);

        return file;
    }
    
    private static ConcurStandardAccountingExtractDetailLine buildConcurStandardAccountingExtractDetailLine(String debitCredit, KualiDecimal amount) {
        ConcurStandardAccountingExtractDetailLine line = new ConcurStandardAccountingExtractDetailLine();
        line.setJounalDebitCredit(debitCredit);
        line.setJournalAmount(amount);
        line.setEmployeeGroupId("CORNELL");
        return line;
    }

}
