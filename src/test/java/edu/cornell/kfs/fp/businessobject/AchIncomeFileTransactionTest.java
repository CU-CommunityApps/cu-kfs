package edu.cornell.kfs.fp.businessobject;

import edu.cornell.kfs.fp.CuFPConstants;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AchIncomeFileTransactionTest {

    private AchIncomeFileTransaction achIncomeFileTransaction;

    @Before
    public void setUp() {
        achIncomeFileTransaction = new AchIncomeFileTransaction();
    }

    @Test
    public void testGetPayerName() {
        List<AchIncomeFileTransactionPayerOrPayeeName> payerOrPayees = new ArrayList<>();
        AchIncomeFileTransactionPayerOrPayeeName payeeName = new AchIncomeFileTransactionPayerOrPayeeName();
        payeeName.setType("PE");
        payeeName.setName("CORNELL UNIVERSITY, INC");
        payeeName.setIdQualifier("93");
        payeeName.setIdCode("4B578");
        payerOrPayees.add(payeeName);

        AchIncomeFileTransactionPayerOrPayeeName payerName = new AchIncomeFileTransactionPayerOrPayeeName();
        payerName.setType("PR");
        payerName.setName("NATIONAL SCIENCE FOUNDATION");
        payerOrPayees.add(payerName);

        achIncomeFileTransaction.setPayerOrPayees(payerOrPayees);
        assertEquals("Payee Name didn't match what was expected.", "NATIONAL SCIENCE FOUNDATION", achIncomeFileTransaction.getPayerName());
    }

    @Test
    public void testGetPayerNameHasPremiumReceiverName() {
        List<AchIncomeFileTransactionPayerOrPayeeName> payerOrPayees = new ArrayList<>();
        AchIncomeFileTransactionPayerOrPayeeName payeeName = new AchIncomeFileTransactionPayerOrPayeeName();
        payeeName.setType("PE");
        payeeName.setName("CORNELL UNIVERSITY, INC");
        payeeName.setIdQualifier("93");
        payeeName.setIdCode("4B578");
        payerOrPayees.add(payeeName);

        AchIncomeFileTransactionPayerOrPayeeName payerName = new AchIncomeFileTransactionPayerOrPayeeName();
        payerName.setType("PR");
        payerName.setName("NATIONAL SCIENCE FOUNDATION");
        payerOrPayees.add(payerName);

        achIncomeFileTransaction.setPayerOrPayees(payerOrPayees);

        AchIncomeFileTransactionPremiumReceiverName achIncomeFileTransactionPremiumReceiverName = new AchIncomeFileTransactionPremiumReceiverName();
        achIncomeFileTransactionPremiumReceiverName.setName("PREMIUM RECIEVER NAME");
        achIncomeFileTransaction.setPremiumReceiverName(achIncomeFileTransactionPremiumReceiverName);

        assertEquals("Payee Name didn't match what was expected.", "NATIONAL SCIENCE FOUNDATION", achIncomeFileTransaction.getPayerName());
    }

    @Test
    public void testGetPayerNameMissingHasPremiumReceiverName() {
        List<AchIncomeFileTransactionPayerOrPayeeName> payerOrPayees = new ArrayList<>();
        AchIncomeFileTransactionPayerOrPayeeName payeeName = new AchIncomeFileTransactionPayerOrPayeeName();
        payeeName.setType("PE");
        payeeName.setName("CORNELL UNIVERSITY, INC");
        payeeName.setIdQualifier("93");
        payeeName.setIdCode("4B578");
        payerOrPayees.add(payeeName);

        achIncomeFileTransaction.setPayerOrPayees(payerOrPayees);

        AchIncomeFileTransactionPremiumReceiverName achIncomeFileTransactionPremiumReceiverName = new AchIncomeFileTransactionPremiumReceiverName();
        achIncomeFileTransactionPremiumReceiverName.setName("PREMIUM RECEIVER NAME");
        achIncomeFileTransaction.setPremiumReceiverName(achIncomeFileTransactionPremiumReceiverName);

        assertEquals("Payee Name didn't match what was expected.", "PREMIUM RECEIVER NAME", achIncomeFileTransaction.getPayerName());
    }

    @Test
    public void testGetPayerNameMissing() {
        assertEquals("Payee Name didn't match what was expected.", CuFPConstants.AchIncomeFileTransaction.PAYER_NOT_IDENTIFIED, achIncomeFileTransaction.getPayerName());
    }

}