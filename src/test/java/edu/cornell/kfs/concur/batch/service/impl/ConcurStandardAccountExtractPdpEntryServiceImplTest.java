package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import java.sql.Date;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.rice.core.impl.datetime.DateTimeServiceImpl;

public class ConcurStandardAccountExtractPdpEntryServiceImplTest {
    
    private ConcurStandardAccountExtractPdpEntryServiceImpl concurStandardAccountExtractPdpEntryServiceImpl;

    @Before
    public void setUp() throws Exception {
        concurStandardAccountExtractPdpEntryServiceImpl = new ConcurStandardAccountExtractPdpEntryServiceImpl();
        concurStandardAccountExtractPdpEntryServiceImpl.setPayeeNameFieldSize(new Integer(40));
        concurStandardAccountExtractPdpEntryServiceImpl.setDateTimeService(new DateTimeServiceImpl());
    }

    @After
    public void tearDown() throws Exception {
        concurStandardAccountExtractPdpEntryServiceImpl = null;
    }

    @Test
    public void buildPayeeName() {
        String results = concurStandardAccountExtractPdpEntryServiceImpl.buildPayeeName("Doe", "John", "Q");
        String expected = "Doe, John Q.";
        assertEquals("The names should be the same", expected, results);
    }
    
    @Test
    public void buildPayeeNameNoMiddleInitial() {
        String results = concurStandardAccountExtractPdpEntryServiceImpl.buildPayeeName("Doe", "John", "   ");
        String expected = "Doe, John";
        assertEquals("The names should be the same", expected, results);
    }
    
    @Test
    public void buildPayeeNameOverFortyChars() {
        String results = concurStandardAccountExtractPdpEntryServiceImpl.buildPayeeName("zxcvbnmasdfghjklqwer", "qwertyuiopasdfghjklz", "l");
        String expected = "zxcvbnmasdfghjklqwer, qwertyuiopasdfghjk";
        assertEquals("The names should be the same", expected, results);
    }
    
    @Test
    public void buildPayeeNameOverFortyCharsEndWithComma() {
        String results = concurStandardAccountExtractPdpEntryServiceImpl.buildPayeeName("zxcvbnmasdfghjklqwer", "qwertyuiopasdfghij", "l");
        String expected = "zxcvbnmasdfghjklqwer, qwertyuiopasdfghij";
        assertEquals("The names should be the same", expected, results);
    }
    
    @Test
    public void buildPayeeNameOverFortyCharsEndWithComma2() {
        String results = concurStandardAccountExtractPdpEntryServiceImpl.buildPayeeName("zxcvbnmasdfghjklqwer", "qwertyuiopasdfghijk", "l");
        String expected = "zxcvbnmasdfghjklqwer, qwertyuiopasdfghij";
        assertEquals("The names should be the same", expected, results);
    }
    
    @Test
    public void formatDate1() {
        DateTime jodaDateTime = new DateTime(2017, 2, 7, 0, 0);
        Date testDate = new Date(jodaDateTime.getMillis());
        String results = concurStandardAccountExtractPdpEntryServiceImpl.formatDate(testDate);
        assertEquals("The dates should format as expected", "02/07/2017", results);
    }
    
    @Test
    public void formatDate2() {
        DateTime jodaDateTime = new DateTime(1977, 12, 31, 3, 46);
        Date testDate = new Date(jodaDateTime.getMillis());
        String results = concurStandardAccountExtractPdpEntryServiceImpl.formatDate(testDate);
        assertEquals("The dates should format as expected", "12/31/1977", results);
    }

}
