package edu.cornell.kfs.module.cg.businessobject.lookup;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CuAwardLookupableHelperServiceImplTest {
    
    private CuAwardLookupableHelperServiceImpl lookupHelper;

    @Before
    public void setUp() throws Exception {
        lookupHelper = new CuAwardLookupableHelperServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        lookupHelper = null;
    }

    @Test
    public void testParseHrefKFSExample() {
        String anchorTag = "<a title=\"View Invoices Award withProposal Number=77734 \" href=\"http://localhost:8080/kfs/kew/DocumentSearch.do?docFormKey=88888888&amp;returnLocation=http://localhost:8080/kfs/portal.do&amp;hideReturnLink=true&amp;documentAttribute.proposalNumber=77734&amp;documentTypeName=CINV&amp;methodToCall=search\" target=\"_blank\">View Invoices</a>";
        String expectedResults = "http://localhost:8080/kfs/kew/DocumentSearch.do?docFormKey=88888888&amp;returnLocation=http://localhost:8080/kfs/portal.do&amp;hideReturnLink=true&amp;documentAttribute.proposalNumber=77734&amp;documentTypeName=CINV&amp;methodToCall=search";
        String actual = lookupHelper.parseHrefLinkFromAnchorTag(anchorTag);
        assertEquals(expectedResults, actual);
    }
    
    public void testParseHrefGenericExample1() {
        String anchorTag = "<a href=\"https://www.google.com\">Google</a>";
        String expectedResults = "https://www.google.com";
        String actual = lookupHelper.parseHrefLinkFromAnchorTag(anchorTag);
        assertEquals(expectedResults, actual);
    }
    
    public void testParseHrefGenericExample2() {
        String anchorTag = "<a href=\"https://www.google.com\" title=\"Google Link\">Google</a>";
        String expectedResults = "https://www.google.com";
        String actual = lookupHelper.parseHrefLinkFromAnchorTag(anchorTag);
        assertEquals(expectedResults, actual);
    }

}
