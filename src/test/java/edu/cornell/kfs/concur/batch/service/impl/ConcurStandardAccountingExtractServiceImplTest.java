package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import java.sql.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public class ConcurStandardAccountingExtractServiceImplTest {
    
    ConcurStandardAccountingExtractServiceImpl concurStandardAccountingExtractServiceImpl;

    @Before
    public void setUp() throws Exception {
        concurStandardAccountingExtractServiceImpl = new ConcurStandardAccountingExtractServiceImpl();
        concurStandardAccountingExtractServiceImpl.setPayeeNameFieldSize(new Integer(40));
    }

    @After
    public void tearDown() throws Exception {
        concurStandardAccountingExtractServiceImpl = null;
    }

    @Test
    public void buildPayeeName() {
        String results = concurStandardAccountingExtractServiceImpl.buildPayeeName("Doe", "John", "Q");
        String expected = "Doe, John, Q.";
        assertEquals("The names should be the same", expected, results);
    }
    
    @Test
    public void buildPayeeNameNoMiddleInitial() {
        String results = concurStandardAccountingExtractServiceImpl.buildPayeeName("Doe", "John", "   ");
        String expected = "Doe, John";
        assertEquals("The names should be the same", expected, results);
    }
    
    @Test
    public void buildPayeeNameOverFortyChars() {
        String results = concurStandardAccountingExtractServiceImpl.buildPayeeName("zxcvbnmasdfghjklqwer", "qwertyuiopasdfghjklz", "l");
        String expected = "zxcvbnmasdfghjklqwer, qwertyuiopasdfghjk";
        assertEquals("The names should be the same", expected, results);
    }
    
    @Test
    public void buildPayeeNameOverFortyCharsEndWithComma() {
        String results = concurStandardAccountingExtractServiceImpl.buildPayeeName("zxcvbnmasdfghjklqwer", "qwertyuiopasdfgh", "l");
        String expected = "zxcvbnmasdfghjklqwer, qwertyuiopasdfgh";
        assertEquals("The names should be the same", expected, results);
    }
    
    @Test
    public void buildPayeeNameOverFortyCharsEndWithComma2() {
        String results = concurStandardAccountingExtractServiceImpl.buildPayeeName("zxcvbnmasdfghjklqwer", "qwertyuiopasdfghi", "l");
        String expected = "zxcvbnmasdfghjklqwer, qwertyuiopasdfghi";
        assertEquals("The names should be the same", expected, results);
    }
    
    @Test
    public void addAmounts() {
        String starting = "100";
        KualiDecimal addAmount = new KualiDecimal(500.45); 
        String results = concurStandardAccountingExtractServiceImpl.addAmounts(starting, addAmount);
        String expected = "600.45";
        assertEquals("The amounts should be the same", expected, results);
    }
    
    @Test
    public void formatDate() {
        Date testDate = new Date(2017-1900, 1, 7);
        String results = concurStandardAccountingExtractServiceImpl.formatDate(testDate);
        assertEquals("The dates should format as expected", "02/07/2017", results);
    }

}
