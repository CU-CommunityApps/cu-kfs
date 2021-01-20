package edu.cornell.kfs.concur.businessobjects;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ValidationResultTest {
    
    private ValidationResult validationResult;
    
    private static final String TEST_STRING_ONE = "test string 1";
    private static final String TEST_STRING_TWO = "test string 2";
    private static final String TEST_STRING_THREE = "test string 3";
    private static final String TEST_STRING_THREE_UPPER_CASE = StringUtils.upperCase(TEST_STRING_THREE, Locale.US);

    @Before
    public void setUp() throws Exception {
        validationResult = new ValidationResult();
    }

    @After
    public void tearDown() throws Exception {
        validationResult = null;
    }

    @Test
    public void verifyAddMessage() {
        validationResult.addMessage(TEST_STRING_ONE);
        validationResult.addMessage(TEST_STRING_TWO);
        validationResult.addMessage(TEST_STRING_ONE);
        validationResult.addMessage(TEST_STRING_THREE);
        validationResult.addMessage(TEST_STRING_THREE_UPPER_CASE);
        assertEquals("There should only be three lines", 3, validationResult.getMessages().size());
    }
    
    @Test
    public void verifyAddMessages() {
        List<String> messagesToAdd = new ArrayList<String>();
        messagesToAdd.add(TEST_STRING_ONE);
        messagesToAdd.add(TEST_STRING_TWO);
        messagesToAdd.add(TEST_STRING_ONE);
        messagesToAdd.add(TEST_STRING_THREE);
        messagesToAdd.add(TEST_STRING_THREE_UPPER_CASE);
        validationResult.addMessages(messagesToAdd);
        
        assertEquals("There should only be three lines", 3, validationResult.getMessages().size());
        assertEquals("There should only be five lines", 5, messagesToAdd.size());
    }
    
    @Test
    public void verifyAddNullCollectionMessage() {
        validationResult.addMessage(TEST_STRING_ONE);
        validationResult.addMessages(null);
        assertEquals("There should only be one line", 1, validationResult.getMessages().size());
    }
    
    @Test
    public void verifyAddEmptyCollectionMessage() {
        validationResult.addMessage(TEST_STRING_ONE);
        validationResult.addMessages(new ArrayList<String>());
        assertEquals("There should only be one line", 1, validationResult.getMessages().size());
    }

}
