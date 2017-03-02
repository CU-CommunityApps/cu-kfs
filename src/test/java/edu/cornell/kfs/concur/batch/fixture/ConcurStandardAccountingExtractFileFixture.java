package edu.cornell.kfs.concur.batch.fixture;

import java.sql.Date;
import java.util.Calendar;

import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;

public class ConcurStandardAccountingExtractFileFixture {
    
    public static ConcurStandardAccountingExtractFile buildConcurStandardAccountingExtractFile(double[] debits, double[] credits) {
        ConcurStandardAccountingExtractFile file = new ConcurStandardAccountingExtractFile();
        Date today = new Date(Calendar.getInstance().getTimeInMillis());
        file.setBatchDate(today);
        file.setRecordCount(new Integer(debits.length + credits.length));
        
        double journalTotal = 0;
        for (double debitAmount : debits) {
            journalTotal += debitAmount;
            file.getConcurStandardAccountingExtractDetailLines().add(buildConcurStandardAccountingExtractDetailLine(
                    ConcurConstants.ConcurPdpConstants.DEBIT, new KualiDecimal(debitAmount)));
        }
        for (double creditAmount : credits) {
            journalTotal += creditAmount;
            file.getConcurStandardAccountingExtractDetailLines().add(buildConcurStandardAccountingExtractDetailLine(
                    ConcurConstants.ConcurPdpConstants.CREDIT, new KualiDecimal(creditAmount)));
        }
        file.setJournalAmountTotal(new KualiDecimal(journalTotal));

        return file;
    }
    
    private static ConcurStandardAccountingExtractDetailLine buildConcurStandardAccountingExtractDetailLine(String debitCredit, KualiDecimal amount) {
        ConcurStandardAccountingExtractDetailLine line = new ConcurStandardAccountingExtractDetailLine();
        line.setJounalDebitCredit(debitCredit);
        line.setJournalAmount(amount);
        return line;
    }

}
