package edu.cornell.kfs.concur.batch.fixture;

import java.sql.Date;
import java.util.Calendar;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;

public class ConcurStandardAccountingExtractFileFixture {
    private static final String EMPLOYEE_GROUP_ID = "CORNELL";
    private static final String EMPLOYEE_LAST_NAME = "LastName";
    private static final String EMPLOYEE_FIRST_NAME = "FirstName";
    private static final String EMPLOYEE_MIDDLE_INITIAL = "I";
    private static final String DEFAULT_POLICY = "DefaultPolicy";
    private static final String DEFAULT_EXPENSE_TYPE_NAME = "DefaultExpenseTypeName";

    public static ConcurStandardAccountingExtractFile buildConcurStandardAccountingExtractFile(KualiDecimal[] debits, KualiDecimal[] credits, String employeeGroupId) {
        ConcurStandardAccountingExtractFile file = new ConcurStandardAccountingExtractFile();
        Date today = new Date(Calendar.getInstance().getTimeInMillis());
        file.setBatchDate(today);
        file.setRecordCount(new Integer(debits.length + credits.length));
        
        KualiDecimal journalTotal = KualiDecimal.ZERO;
        for (KualiDecimal debitAmount : debits) {
            journalTotal = journalTotal.add(debitAmount);
            file.getConcurStandardAccountingExtractDetailLines().add(buildConcurStandardAccountingExtractDetailLine(
                    ConcurConstants.DEBIT, debitAmount, employeeGroupId));
        }
        for (KualiDecimal creditAmount : credits) {
            journalTotal = journalTotal.add(creditAmount);
            file.getConcurStandardAccountingExtractDetailLines().add(buildConcurStandardAccountingExtractDetailLine(
                    ConcurConstants.CREDIT, creditAmount, employeeGroupId));
        }
        file.setJournalAmountTotal(journalTotal);

        return file;
    }

    private static ConcurStandardAccountingExtractDetailLine buildConcurStandardAccountingExtractDetailLine(String debitCredit, KualiDecimal amount, String employeeGroupId) {
        ConcurStandardAccountingExtractDetailLine line = new ConcurStandardAccountingExtractDetailLine();
        line.setJournalDebitCredit(debitCredit);
        line.setJournalAmount(amount);
        line.setEmployeeGroupId(employeeGroupId);
        line.setEmployeeLastName(EMPLOYEE_LAST_NAME);
        line.setEmployeeFirstName(EMPLOYEE_FIRST_NAME);
        line.setEmployeeMiddleInitial(EMPLOYEE_MIDDLE_INITIAL);
        line.setPolicy(DEFAULT_POLICY);
        line.setExpenseType(DEFAULT_EXPENSE_TYPE_NAME);
        return line;
    }

}
