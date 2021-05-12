package edu.cornell.kfs.krad.service.impl;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.core.api.util.CoreUtilities;

/**
 * This test class is based on its Cynergy counterpart, edu.cornell.cynergy.krad.service.impl.CynergyMaintainableXMLConversionServiceImplTest
 * and includes some of the same tests (with occasionally tweaked test files, along with some additional KFS-specific tests.
 */
public class CuMaintainableXMLConversionServiceImplTest {

    protected static final String CONVERSION_RULE_FILE = "classpath:edu/cornell/kfs/krad/config/MaintainableXMLUpgradeRules.xml";
    protected static final String BASE_TEST_FILE_PATH = "classpath:edu/cornell/kfs/krad/service/impl/";
    protected static final String OLD_DATA_ELEMENT = "oldData";
    protected static final String EXPECTED_RESULT_ELEMENT = "expectedResult";
    protected static final String MOVE_TO_PARENT_TEST_ELEMENT = "moveToParentTest";

    protected TestMaintainableXMLConversionServiceImpl conversionService;
    protected String oldData;
    protected String expectedResult;

    @Before
    public void setUp() throws Exception {
        conversionService = new TestMaintainableXMLConversionServiceImpl();
        conversionService.setConversionRuleFile(CONVERSION_RULE_FILE);
        conversionService.afterPropertiesSet();
    }

    @Test
    public void testConversionOfSimpleBusinessObject() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("BasicConversionTest.xml");
    }

    @Test
    public void testAdvancedRulesForConvertingTypedArrayList() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("TypedArrayListTest.xml");
    }

    @Test
    public void testAdvancedRulesForConvertingAttributeSet() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("AttributeSetTest.xml");
    }

    @Test
    public void testMoveNodesToParentWhenExcludedNodeDoesNotHaveCustomPropertyRules() throws Exception {
        conversionService.addEntryToRuleMap(CuMaintenanceXMLConverter.DEFAULT_PROPERTY_RULE_KEY,
                MOVE_TO_PARENT_TEST_ELEMENT, CuMaintenanceXMLConverter.MOVE_NODES_TO_PARENT_INDICATOR);
        assertXMLFromTestFileConvertsAsExpected("MoveNodesToParentTest.xml");
    }

    @Test
    public void testConversionOfLegacyNotesXML() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("LegacyNotesTest.xml");
    }

    @Test
    public void testConversionOfRice2xNotesXML() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("Rice2xNotesTest.xml");
    }

    @Test
    public void testConversionOfDateFields() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("DateFieldTest.xml");
    }

    @Test
    public void testConversionOfAccount() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("AccountTest.xml");
    }

    @Test
    public void testConversionOfAccountCustomAddress() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("AccountCustomAddressTest.xml");
    }

    @Test
    public void testConversionOfAccountReversion() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("AccountReversionTest.xml");
    }

    @Test
    public void testConversionOfNoteTypeReference() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("NoteTypeReferenceTest.xml");
    }

    @Test
    public void testConversionOfObjectCodeGlobal() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("ObjectCodeGlobalTest.xml");
    }

    @Test
    public void testConversionOfVendor() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("VendorTest.xml");
    }


    protected void assertXMLFromTestFileConvertsAsExpected(String fileLocalName) throws Exception {
        readTestFile(fileLocalName);
        String actualResult = conversionService.transformMaintainableXML(oldData);
        assertConversionResultsAreCorrect(expectedResult, actualResult);
    }

    protected void assertConversionResultsAreCorrect(String expectedXml, String actualXml) throws Exception {
        expectedXml = StringUtils.normalizeSpace(expectedXml);
        actualXml = StringUtils.normalizeSpace(actualXml);
        assertEquals("Wrong XML conversion result", expectedXml, actualXml);
    }

    /**
     * Reads test files that are in XML format, and whose contents have the following:
     *
     * [1] A root "xmlConversionTestCase" element.
     * [2] An "oldData" element containing the XML that should be passed to the conversion service.
     * [3] An "expectedResult" element containing the expected XML output from the conversion service.
     *
     * NOTE: For simplicity in comparing the expected and actual conversion results,
     * any whitespace will be normalized prior to comparison.
     */
    protected void readTestFile(String fileLocalName) throws Exception {
        InputStream fileStream = null;
        InputStreamReader reader = null;
        StringBuilderWriter writer = null;

        try {
            fileStream = CoreUtilities.getResourceAsStream(BASE_TEST_FILE_PATH + fileLocalName);
            reader = new InputStreamReader(fileStream);
            writer = new StringBuilderWriter();

            IOUtils.copy(reader, writer);
            StringBuilder fileContents = writer.getBuilder();
            oldData = getXMLContentSubstring(fileContents, OLD_DATA_ELEMENT);
            expectedResult = getXMLContentSubstring(fileContents, EXPECTED_RESULT_ELEMENT);
        } finally {
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(fileStream);
        }
    }

    protected String getXMLContentSubstring(StringBuilder fileContents, String tagName) {
        String openingTag = String.format("<%s>", tagName);
        String closingTag = String.format("</%s>", tagName);
        int openingTagIndex = fileContents.indexOf(openingTag);
        int closingTagIndex = fileContents.lastIndexOf(closingTag);
        int trimmedStartIndex = fileContents.indexOf("<", openingTagIndex + openingTag.length());
        int trimmedEndIndex = fileContents.lastIndexOf(">", closingTagIndex) + 1;
        return fileContents.substring(trimmedStartIndex, trimmedEndIndex);
    }

    /**
     * Testing-only conversion service sub-class that will re-throw conversion errors instead of logging them,
     * and also allows for manually updating the property rule maps.
     */
    protected static class TestMaintainableXMLConversionServiceImpl extends CuMaintainableXMLConversionServiceImpl {

        @Override
        protected void handleXMLStreamException(XMLStreamException e) {
            throw new RuntimeException(e);
        }

        public void addEntryToRuleMap(String ruleMapKey, String match, String replacement) {
            classPropertyRuleMaps.computeIfAbsent(ruleMapKey, (key) -> new HashMap<>())
                    .put(match, replacement);
        }
    }

}