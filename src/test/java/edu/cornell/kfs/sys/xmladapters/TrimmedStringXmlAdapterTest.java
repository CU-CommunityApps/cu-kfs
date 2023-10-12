package edu.cornell.kfs.sys.xmladapters;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TrimmedStringXmlAdapterTest {
    private static final String FOO = "foo";
    private static final String ONE_TWO_THREE = "123";
    private static final String SPACES = "    ";
    
    private TrimmedStringXmlAdapter adapter;

    @BeforeEach
    void setUp() throws Exception {
        adapter = new TrimmedStringXmlAdapter();
    }

    @AfterEach
    void tearDown() throws Exception {
        adapter = null;
    }

    @ParameterizedTest
    @MethodSource("marshalTestCases")
    public void testUnmarshal(String value, String expectedValue) throws Exception {
        String actualValue = adapter.unmarshal(value);
        assertEquals(expectedValue, actualValue);
    }
    
    @ParameterizedTest
    @MethodSource("marshalTestCases")
    public void testMarshal(String value, String expectedValue) throws Exception {
        String actualValue = adapter.marshal(value);
        assertEquals(expectedValue, actualValue);
    }
    
    static Stream<Arguments> marshalTestCases() {
        return Stream.of(
                Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY),
                Arguments.of(StringUtils.SPACE, StringUtils.EMPTY),
                Arguments.of(SPACES, StringUtils.EMPTY),
                Arguments.of(null, null),
                Arguments.of(SPACES + FOO + SPACES, FOO),
                Arguments.of(FOO + SPACES, FOO),
                Arguments.of(ONE_TWO_THREE + SPACES, ONE_TWO_THREE),
                Arguments.of("   foo  bar     ", "foo  bar"),
                Arguments.of(FOO, FOO)
        );
    }

}
