package edu.cornell.kfs.sys.util;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.annotation.XmlAttributeMatcher;
import edu.cornell.kfs.sys.annotation.XmlDocumentFilter;
import edu.cornell.kfs.sys.annotation.XmlElementFilter;

@Execution(ExecutionMode.SAME_THREAD)
@CreateTestDirectories(
        baseDirectory = CuXmlFiltererTest.TEST_XML_FILTERER_DIRECTORY,
        subDirectories = {
                CuXmlFiltererTest.TEST_XML_FILTERER_INPUT_DIRECTORY,
                CuXmlFiltererTest.TEST_XML_FILTERER_OUTPUT_DIRECTORY
        }
)
public class CuXmlFiltererTest {

    static final String EXPECTED_FILE_RESULTS_DIRECTORY = "classpath:edu/cornell/kfs/sys/xml-filterer/";
    static final String TEST_XML_FILTERER_DIRECTORY = "test/xml_filterer_utility/";
    static final String TEST_XML_FILTERER_INPUT_DIRECTORY = TEST_XML_FILTERER_DIRECTORY + "input/";
    static final String TEST_XML_FILTERER_OUTPUT_DIRECTORY = TEST_XML_FILTERER_DIRECTORY + "output/";

    private static final String OJB_SYS_XML_FILE = "ojb-sys.xml";
    private static final String OJB_SYS_XML_FILE_PATH = "classpath:org/kuali/kfs/sys/" + OJB_SYS_XML_FILE;
    private static final String FILTERED_OJB_SYS_XML_FILE_PATH = TEST_XML_FILTERER_OUTPUT_DIRECTORY + OJB_SYS_XML_FILE;



    enum LocalTestCase {

        @XmlDocumentFilter(rootElementName = "descriptor-repository", directChildElementsToKeep = {
                @XmlElementFilter(name = "class-descriptor", matchConditions = {
                        @XmlAttributeMatcher(name = "class", values = {
                                "org.kuali.kfs.sys.businessobject.DocumentHeader"
                        })
                })
        })
        DOCUMENT_HEADER_ENTRY_FILTER_BY_CLASSNAME("test-ojb-sys-filter-doc-header.xml"),

        @XmlDocumentFilter(rootElementName = "descriptor-repository", directChildElementsToKeep = {
                @XmlElementFilter(name = "class-descriptor", matchConditions = {
                        @XmlAttributeMatcher(name = "class", values = {
                                "org.kuali.kfs.coreservice.impl.component.Component"
                        })
                })
        })
        PARM_COMPONENT_ENTRY_FILTER_BY_CLASSNAME("test-ojb-sys-filter-component.xml"),

        @XmlDocumentFilter(rootElementName = "descriptor-repository", directChildElementsToKeep = {
                @XmlElementFilter(name = "class-descriptor", matchConditions = {
                        @XmlAttributeMatcher(name = "table", values = { "KRCR_CMPNT_T" })
                })
        })
        PARM_COMPONENT_ENTRY_FILTER_BY_TABLE("test-ojb-sys-filter-component.xml"),

        @XmlDocumentFilter(rootElementName = "descriptor-repository", directChildElementsToKeep = {
                @XmlElementFilter(name = "class-descriptor", matchConditions = {
                        @XmlAttributeMatcher(name = "class", values = {
                                "org.kuali.kfs.sys.businessobject.DocumentHeader"
                        }),
                        @XmlAttributeMatcher(name = "table", values = { "FS_DOC_HEADER_T" })
                })
        })
        DOCUMENT_HEADER_ENTRY_FILTER_BY_CLASSNAME_AND_TABLE("test-ojb-sys-filter-doc-header.xml"),

        @XmlDocumentFilter(rootElementName = "descriptor-repository", directChildElementsToKeep = {
                @XmlElementFilter(name = "class-descriptor", matchConditions = {
                        @XmlAttributeMatcher(name = "class", values = {
                                "org.kuali.kfs.sys.businessobject.DocumentHeader",
                                "org.kuali.kfs.coreservice.impl.component.Component"
                        })
                })
        })
        DOCUMENT_AND_COMPONENT_ENTRIES_FILTER_BY_CLASSNAME("test-ojb-sys-filter-doc-header-and-component.xml"),

        @XmlDocumentFilter(rootElementName = "descriptor-repository", directChildElementsToKeep = {
                @XmlElementFilter(name = "class-descriptor", matchConditions = {
                        @XmlAttributeMatcher(name = "class", regex = "^.*?\\.(DocumentHeader|Component)$")
                })
        })
        DOCUMENT_AND_COMPONENT_ENTRIES_FILTER_BY_REGEX("test-ojb-sys-filter-doc-header-and-component.xml"),

        @XmlDocumentFilter(rootElementName = "descriptor-repository", directChildElementsToKeep = {
                @XmlElementFilter(name = "class-descriptor", matchConditions = {
                        @XmlAttributeMatcher(name = "class", values = {
                                "edu.cornell.kfs.sys.businessobject.UnknownObject"
                        })
                })
        })
        UNMATCHED_ENTRIES_FILTER_BY_CLASSNAME("test-ojb-sys-filter-empty-repo.xml"),

        @XmlDocumentFilter(rootElementName = "descriptor-repository", directChildElementsToKeep = {
                @XmlElementFilter(name = "class-descriptor", matchConditions = {
                        @XmlAttributeMatcher(name = "table", values = { "KRCR_BOGUS_T", "UNKNOWN_TABLE_T" })
                })
        })
        UNMATCHED_ENTRIES_FILTER_BY_TABLE("test-ojb-sys-filter-empty-repo.xml"),
        
        @XmlDocumentFilter(rootElementName = "descriptor-repository", directChildElementsToKeep = {
                @XmlElementFilter(name = "class-descriptor", matchConditions = {
                        @XmlAttributeMatcher(name = "table", values = { "KRCR_BOGUS_T", "UNKNOWN_TABLE_T" })
                })
        })
        UNMATCHED_ENTRIES_FILTER_BY_CLASSNAME_AND_TABLE("test-ojb-sys-filter-empty-repo.xml"),

        @XmlDocumentFilter(rootElementName = "descriptor-repository", directChildElementsToKeep = {
                @XmlElementFilter(name = "class-descriptor", matchConditions = {
                        @XmlAttributeMatcher(name = "class", regex = "^.*?\\.(Bogus01|UnknownObject)$")
                })
        })
        UNMATCHED_ENTRIES_FILTER_BY_REGEX("test-ojb-sys-filter-empty-repo.xml");

        private final String expectedResultsFileName;

        private LocalTestCase(final String expectedResultsFileName) {
            this.expectedResultsFileName = expectedResultsFileName;
        }

    }

    static Stream<Arguments> validOjbSysXmlFilters() {
        return Arrays.stream(LocalTestCase.values())
                .map(fixture -> Arguments.of(fixture.expectedResultsFileName, fixture));
    }



    @ParameterizedTest
    @MethodSource("validOjbSysXmlFilters")
    void testFilterOjbSysXml(final String expectedResultsFile, final LocalTestCase testCase) throws Exception {
        CuXMLUnitTestUtils.filterXml(
                OJB_SYS_XML_FILE_PATH, FILTERED_OJB_SYS_XML_FILE_PATH, testCase);
        assertXmlWasFilteredProperly(EXPECTED_FILE_RESULTS_DIRECTORY + expectedResultsFile,
                FILTERED_OJB_SYS_XML_FILE_PATH);
    }

    private void assertXmlWasFilteredProperly(final String expectedXmlFilePath, final String actualXmlFilePath)
            throws Exception {
        try (final InputStream expectedXmlFile = CuCoreUtilities.getResourceAsStream(expectedXmlFilePath)) {
            final File actualXmlFile = new File(actualXmlFilePath);
            CuXMLUnitTestUtils.compareXML(expectedXmlFile, actualXmlFile);
        }
    }

}
