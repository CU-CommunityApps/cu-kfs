package edu.cornell.kfs.krad.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kuali.kfs.core.api.util.CoreUtilities;
import org.kuali.kfs.coreservice.impl.parameter.Parameter;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;
import org.kuali.kfs.sys.KFSPropertyConstants;

/**
 * This test class is based on its Cynergy counterpart, edu.cornell.cynergy.krad.service.impl.CynergyMaintainableXMLConversionServiceImplTest
 * and includes some of the same tests (with occasionally tweaked test files, along with some additional KFS-specific tests.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class CuMaintainableXMLConversionServiceImplTest {

    protected static final String CONVERSION_RULE_FILE = "classpath:edu/cornell/kfs/krad/config/MaintainableXMLUpgradeRules.xml";
    protected static final String BASE_TEST_FILE_PATH = "classpath:edu/cornell/kfs/krad/service/impl/";
    protected static final String OLD_DATA_ELEMENT = "oldData";
    protected static final String EXPECTED_RESULT_ELEMENT = "expectedResult";

    protected static final String MOVE_TO_PARENT_TEST_ELEMENT = "moveToParentTest";
    protected static final String MOVED_CHILD_TEST_ELEMENT = "movedChild";
    protected static final String UNMOVED_CHILD_TO_UPDATE_TEST_ELEMENT = "unmovedChildToUpdate";
    protected static final String UNMOVED_RENAMED_CHILD_TEST_ELEMENT = "unmovedRenamedChild";
    protected static final String MOVED_CHILD_TO_UPDATE_TEST_ELEMENT = "movedChildToUpdate";
    protected static final String MOVED_RENAMED_CHILD_TEST_ELEMENT = "movedRenamedChild";

    protected TestMaintainableXMLConversionServiceImpl conversionService;
    protected String oldData;
    protected String expectedResult;

    @BeforeEach
    void setUp() throws Exception {
        conversionService = new TestMaintainableXMLConversionServiceImpl();
        conversionService.setConversionRuleFile(CONVERSION_RULE_FILE);
        conversionService.afterPropertiesSet();
    }

    @AfterEach
    void tearDown() throws Exception {
        conversionService = null;
        oldData = null;
        expectedResult = null;
    } 

    @Test
    void testConversionOfSimpleBusinessObject() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("BasicConversionTest.xml");
    }

    @Test
    void testAdvancedRulesForConvertingTypedArrayList() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("TypedArrayListTest.xml");
    }

    @Test
    void testAdvancedRulesForConvertingAttributeSet() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("AttributeSetTest.xml");
    }

    @Test
    void testMoveNodesToParentWhenExcludedNodeDoesNotHaveCustomPropertyRules() throws Exception {
        conversionService.addEntryToRuleMap(CuMaintenanceXMLConverter.DEFAULT_PROPERTY_RULE_KEY,
                MOVE_TO_PARENT_TEST_ELEMENT, CuMaintenanceXMLConverter.MOVE_CHILD_NODES_TO_PARENT_INDICATOR);
        assertXMLFromTestFileConvertsAsExpected("MoveNodesToParentTest.xml");
    }

    @Test
    void testMoveMarkedNodesToParent() throws Exception {
        conversionService.addRulesToMap(
                ruleEntry(CuMaintenanceXMLConverter.DEFAULT_PROPERTY_RULE_KEY,
                        Map.entry(MOVE_TO_PARENT_TEST_ELEMENT, CuMaintenanceXMLConverter.MOVE_MARKED_NODES_TO_PARENT_INDICATOR)),
                ruleEntry(MOVE_TO_PARENT_TEST_ELEMENT,
                        Map.entry(MOVED_CHILD_TEST_ELEMENT, CuMaintenanceXMLConverter.MOVE_THIS_NODE_TO_PARENT_INDICATOR),
                        Map.entry(MOVED_CHILD_TO_UPDATE_TEST_ELEMENT, CuMaintenanceXMLConverter.MOVE_THIS_NODE_TO_PARENT_INDICATOR),
                        Map.entry(UNMOVED_CHILD_TO_UPDATE_TEST_ELEMENT, UNMOVED_RENAMED_CHILD_TEST_ELEMENT)),
                ruleEntry(MOVE_TO_PARENT_TEST_ELEMENT + CuMaintenanceXMLConverter.MOVED_NODES_CLASSNAME_SUFFIX,
                        Map.entry(MOVED_CHILD_TO_UPDATE_TEST_ELEMENT, MOVED_RENAMED_CHILD_TEST_ELEMENT)));

        assertXMLFromTestFileConvertsAsExpected("MoveMarkedNodesToParentTest.xml");
    }

    @Test
    void testAddWrapperElement() throws Exception {
        conversionService.addRulesToMap(
                ruleEntry(Parameter.class.getName(),
                        Map.entry(MOVED_CHILD_TEST_ELEMENT, CuMaintenanceXMLConverter.ADD_WRAPPER_ELEMENT_INDICATOR),
                        Map.entry(MOVED_CHILD_TEST_ELEMENT + CuMaintenanceXMLConverter.WRAPPER_NAME_INDICATOR_SUFFIX,
                                KFSPropertyConstants.EXTENSION),
                        Map.entry(MOVED_CHILD_TEST_ELEMENT + CuMaintenanceXMLConverter.WRAPPER_CLASS_INDICATOR_SUFFIX,
                                PersistableBusinessObjectExtension.class.getName())));
        
        assertXMLFromTestFileConvertsAsExpected("AddWrapperElementTest.xml");
    }

    @Test
    void testConversionOfLegacyNotesXML() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("LegacyNotesTest.xml");
    }

    @Test
    void testConversionOfRice2xNotesXML() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("Rice2xNotesTest.xml");
    }

    @Test
    void testConversionOfDateFields() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("DateFieldTest.xml");
    }

    @Test
    void testConversionOfAccount() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("AccountTest.xml");
    }

    @Test
    void testConversionOfAccountCustomAddress() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("AccountCustomAddressTest.xml");
    }

    @Test
    void testConversionOfAccountReversion() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("AccountReversionTest.xml");
    }

    @Test
    void testConversionOfNoteTypeReference() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("NoteTypeReferenceTest.xml");
    }

    @Test
    void testConversionOfObjectCodeGlobal() throws Exception {
        assertXMLFromTestFileConvertsAsExpected("ObjectCodeGlobalTest.xml");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "VendorTest.xml",
        "VendorWithoutHeaderExtensionTest.xml",
        "VendorLocaleTest.xml"
    })
    void testConversionOfVendors(String vendorTestFile) throws Exception {
        assertXMLFromTestFileConvertsAsExpected(vendorTestFile);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "PreKewUpgradeKfsParameterTest.xml",
        "LegacyParameterTest.xml",
        "RiceParameterTest.xml"
    })
    void testConversionOfParametersFromBeforeKewUpgrade(String parameterTestFile) throws Exception {
        assertXMLFromTestFileConvertsAsExpected(parameterTestFile);
    }

    @ParameterizedTest
    @ValueSource(strings = {"LegacyDocumentTypeTest.xml", "RiceDocumentTypeTest.xml"})
    void testConversionOfRiceDocumentTypeMaintenanceDocuments(String documentTypeTestFile) throws Exception {
        assertXMLFromTestFileConvertsAsExpected(documentTypeTestFile);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "LegacyCampusTest.xml",
        "RiceCampusTest.xml",
        "LegacyCountryTest.xml",
        "RiceCountryTest.xml",
        "LegacyCountyTest.xml",
        "RiceCountyTest.xml",
        "LegacyPostalCodeTest.xml",
        "RicePostalCodeTest.xml",
        "LegacyStateTest.xml",
        "RiceStateTest.xml"
    })
    void testConversionOfLocationMaintenanceDocuments(String locationTestFile) throws Exception {
        assertXMLFromTestFileConvertsAsExpected(locationTestFile);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "LegacyChartTest.xml",
        "ChartTest.xml",
        "Legacy3xIndirectCostRecoveryTypeTest.xml",
        "Legacy5xIndirectCostRecoveryTypeTest.xml",
        "LegacyAccountDelegateGlobalTest.xml",
        "LegacyHigherEducationFunctionTest.xml"
    })
    void testConversionOfVariousCOADocuments(String coaTestFile) throws Exception {
        assertXMLFromTestFileConvertsAsExpected(coaTestFile);
    }

    protected void assertXMLFromTestFileConvertsAsExpected(String fileLocalName) throws Exception {
        readTestFile(fileLocalName);
        String actualResult = conversionService.transformMaintainableXML(oldData);
        assertConversionResultsAreCorrect(expectedResult, actualResult);
    }

    protected void assertConversionResultsAreCorrect(String expectedXml, String actualXml) throws Exception {
        expectedXml = StringUtils.normalizeSpace(expectedXml);
        actualXml = StringUtils.normalizeSpace(actualXml);
        assertEquals(expectedXml, actualXml, "Wrong XML conversion result");
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
            reader = new InputStreamReader(fileStream, StandardCharsets.UTF_8);
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

    @SafeVarargs
    protected final Map.Entry<String, Map<String, String>> ruleEntry(
            String ruleClassname, Map.Entry<String, String>... conversions) {
        return Map.entry(ruleClassname, Map.ofEntries(conversions));
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

        @SafeVarargs
        public final void addRulesToMap(Map.Entry<String, Map<String, String>>... ruleEntries) {
            for (Map.Entry<String, Map<String, String>> ruleEntry : ruleEntries) {
                classPropertyRuleMaps.computeIfAbsent(ruleEntry.getKey(), key -> new HashMap<>())
                        .putAll(ruleEntry.getValue());
            }
        }
    }

}