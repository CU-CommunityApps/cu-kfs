package edu.cornell.kfs.concur.businessobjects;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ValidationResultTest {
    
    private ValidationResult validationResult;
    
    private static final String TEST_STRING_ONE = "test string 1";
    private static final String TEST_STRING_TWO = "test string 2";
    private static final String TEST_STRING_THREE = "test string 3";
    private static final String TEST_STRING_FOO = "FOO";
    private static final String TEST_STRING_THREE_UPPER_CASE = StringUtils.upperCase(TEST_STRING_THREE, Locale.US);

    @BeforeEach
    public void setUp() throws Exception {
        validationResult = new ValidationResult();
    }

    @AfterEach
    public void tearDown() throws Exception {
        validationResult = null;
    }
    
    @ParameterizedTest
    @MethodSource("addMessageParams")
    public void testAddErrorMessage(List<String> messagesToAdd, int numberOfMessagesExpected, int baseListSize) {
        for (String message : messagesToAdd) {
            validationResult.addErrorMessage(message);
        }
        assertEquals(numberOfMessagesExpected, validationResult.getErrorMessages().size());
        assertEquals(baseListSize, messagesToAdd.size());
    }
    
    @ParameterizedTest
    @MethodSource("addMessageParams")
    public void testAddDetailMessage(List<String> messagesToAdd, int numberOfMessagesExpected, int baseListSize) {
        for (String message : messagesToAdd) {
            validationResult.addDetailMessage(message);
        }
        assertEquals(numberOfMessagesExpected, validationResult.getDetailMessages().size());
        assertEquals(baseListSize, messagesToAdd.size());
    }
    
    @ParameterizedTest
    @MethodSource("addMessageParams")
    public void testAddErrorMessages(List<String> messagesToAdd, int numberOfMessagesExpected, int baseListSize) {
        validationResult.addErrorMessage(TEST_STRING_FOO);
        validationResult.addErrorMessages(messagesToAdd);
        assertEquals(numberOfMessagesExpected + 1, validationResult.getErrorMessages().size());
        assertEquals(baseListSize, messagesToAdd.size());
    }
    
    @ParameterizedTest
    @MethodSource("addMessageParams")
    public void testAddDetailMessages(List<String> messagesToAdd, int numberOfMessagesExpected, int baseListSize) {
        validationResult.addDetailMessage(TEST_STRING_FOO);
        validationResult.addDetailMessages(messagesToAdd);
        assertEquals(numberOfMessagesExpected + 1, validationResult.getDetailMessages().size());
        assertEquals(baseListSize, messagesToAdd.size());
    }
    
    private static Stream<Arguments> addMessageParams() {
        return Stream.of(
                Arguments.of(buildMessageList(TEST_STRING_ONE), 1, 1),
                Arguments.of(buildMessageList(), 0, 0),
                Arguments.of(buildMessageList(TEST_STRING_ONE, TEST_STRING_ONE), 1, 2),
                Arguments.of(buildMessageList(TEST_STRING_ONE, TEST_STRING_TWO), 2, 2),
                Arguments.of(buildMessageList(TEST_STRING_ONE, TEST_STRING_TWO, TEST_STRING_ONE, TEST_STRING_THREE, TEST_STRING_THREE_UPPER_CASE), 3, 5)
                );
    }
    
    private static List<String> buildMessageList(String... messages) {
        List<String> messageList = new ArrayList<>();
        for (String message : messages) {
            messageList.add(message);
        }
        return messageList;
    }
    
    @Test
    public void verifyAddNullCollectionMessage() {
        validationResult.addErrorMessage(TEST_STRING_ONE);
        validationResult.addErrorMessages(null);
        assertEquals("There should only be one line", 1, validationResult.getErrorMessages().size());
    }

}
