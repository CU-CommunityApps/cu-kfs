package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import java.sql.Date;
import java.util.Calendar;

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
        String expected = "Doe, John, Q.";
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
        String results = concurStandardAccountExtractPdpEntryServiceImpl.buildPayeeName("zxcvbnmasdfghjklqwer", "qwertyuiopasdfgh", "l");
        String expected = "zxcvbnmasdfghjklqwer, qwertyuiopasdfgh";
        assertEquals("The names should be the same", expected, results);
    }
    
    @Test
    public void buildPayeeNameOverFortyCharsEndWithComma2() {
        String results = concurStandardAccountExtractPdpEntryServiceImpl.buildPayeeName("zxcvbnmasdfghjklqwer", "qwertyuiopasdfghi", "l");
        String expected = "zxcvbnmasdfghjklqwer, qwertyuiopasdfghi";
        assertEquals("The names should be the same", expected, results);
    }
    
    @Test
    public void formatDate() {
        Date testDate = new Date(2017-1900, Calendar.FEBRUARY, 7);
        String results = concurStandardAccountExtractPdpEntryServiceImpl.formatDate(testDate);
        assertEquals("The dates should format as expected", "02/07/2017", results);
    }

}
