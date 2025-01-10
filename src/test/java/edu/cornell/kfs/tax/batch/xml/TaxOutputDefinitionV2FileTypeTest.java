package edu.cornell.kfs.tax.batch.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.util.CreateTestDirectories;
import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.sys.util.GlobalResourceLoaderUtils;
import edu.cornell.kfs.sys.util.TestSpringContextExtension;
import edu.cornell.kfs.tax.CuTaxTestConstants.TaxSpringBeans;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxOutputFieldType;
import edu.cornell.kfs.tax.batch.TaxOutputDefinitionV2FileType;
import edu.cornell.kfs.tax.fixture.TaxOutputDefinition;
import edu.cornell.kfs.tax.fixture.TaxOutputField;
import edu.cornell.kfs.tax.fixture.TaxOutputSection;

@Execution(ExecutionMode.SAME_THREAD)
@CreateTestDirectories(
        baseDirectory = TaxOutputDefinitionV2FileTypeTest.TEST_TAX_DIRECTORY,
        subDirectories = {
                TaxOutputDefinitionV2FileTypeTest.TEST_TAX_REPORTS_DIRECTORY,
                TaxOutputDefinitionV2FileTypeTest.TEST_TAX_STAGING_DIRECTORY
        }
)
public class TaxOutputDefinitionV2FileTypeTest {

    static final String TEST_TAX_DIRECTORY = "test/tax_output_def/";
    static final String TEST_TAX_REPORTS_DIRECTORY = TEST_TAX_DIRECTORY + "reports/tax/";
    static final String TEST_TAX_STAGING_DIRECTORY = TEST_TAX_DIRECTORY + "staging/tax/";

    private static final String BASE_TEST_XML_PATH = "classpath:edu/cornell/kfs/tax/batch/output-definition-test/";

    @RegisterExtension
    static TestSpringContextExtension springContextExtension = TestSpringContextExtension.forClassPathSpringXmlFile(
            "edu/cornell/kfs/tax/batch/cu-spring-tax-output-definition-test.xml");

    private TaxOutputDefinitionV2FileType taxOutputDefinitionV2FileType;

    @BeforeEach
    void setUp() throws Exception {
        taxOutputDefinitionV2FileType = springContextExtension.getBean(
                TaxSpringBeans.TAX_OUTPUT_DEFINITION_V2_FILE_TYPE, TaxOutputDefinitionV2FileType.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        taxOutputDefinitionV2FileType = null;
    }



    enum LocalTestCase {

        @TaxOutputDefinition(fieldSeparator = KFSConstants.COMMA, sections = {
                @TaxOutputSection(name = "Example_Section", fields = {
                        @TaxOutputField(
                                name = "recipient_id", length = 25,
                                type = TaxOutputFieldType.DERIVED, key = "recipientId"
                        )
                })
        })
        SINGLE_SECTION_SINGLE_FIELD,

        @TaxOutputDefinition(fieldSeparator = "\t", sections = {
                @TaxOutputSection(name = "Tax_Section", hasHeaderRow = true, useExactFieldLengths = true, fields = {
                        @TaxOutputField(
                                name = "recipient_id", length = 25,
                                type = TaxOutputFieldType.DERIVED, key = "recipientId"
                        ),
                        @TaxOutputField(
                                name = "full_name", length = 90,
                                type = TaxOutputFieldType.DERIVED, key = "recipientName"
                        ),
                        @TaxOutputField(
                                name = "tax_amount", length = 15,
                                type = TaxOutputFieldType.DERIVED, key = "taxAmount"
                        ),
                        @TaxOutputField(
                                name = "other_amount", length = 15,
                                type = TaxOutputFieldType.STATIC, value = "0.00"
                        )
                })
        })
        SINGLE_SECTION_MULTIPLE_FIELDS,

        @TaxOutputDefinition(fieldSeparator = KFSConstants.COMMA, sections = {
                @TaxOutputSection(name = "Biographic_Section", hasHeaderRow = true, fields = {
                        @TaxOutputField(
                                name = "recipient_id", length = 25,
                                type = TaxOutputFieldType.DERIVED, key = "recipientId"
                        ),
                        @TaxOutputField(
                                name = "full_name", length = 90,
                                type = TaxOutputFieldType.DERIVED, key = "recipientName"
                        ),
                        @TaxOutputField(
                                name = "primary_address", length = 120,
                                type = TaxOutputFieldType.DERIVED, key = "recipientAddress"
                        ),
                        @TaxOutputField(
                                name = "extra_address", length = 120,
                                type = TaxOutputFieldType.STATIC, value = ""
                        )
                }),
                @TaxOutputSection(name = "Payment_Section", hasHeaderRow = true, fields = {
                        @TaxOutputField(
                                name = "recipient_id", length = 25,
                                type = TaxOutputFieldType.DERIVED, key = "recipientId"
                        ),
                        @TaxOutputField(
                                name = "payment_source", length = 5,
                                type = TaxOutputFieldType.STATIC, value = "Cornell"
                        ),
                        @TaxOutputField(
                                name = "tax_amount", length = 15,
                                type = TaxOutputFieldType.DERIVED, key = "taxAmount"
                        )
                })
        })
        MULTIPLE_SECTIONS_MULTIPLE_FIELDS;

        public Named<TaxOutputDefinition> toNamedAnnotationFixturePayload() {
            return FixtureUtils.createNamedAnnotationFixturePayload(this, TaxOutputDefinition.class);
        }
    }

    static Stream<Arguments> validOutputDefinitions() {
        return Stream.of(
                Pair.of("good-file-single-section-single-field.xml",
                        LocalTestCase.SINGLE_SECTION_SINGLE_FIELD)
        )
                .map(pair -> Arguments.of(pair.getLeft(), pair.getRight().toNamedAnnotationFixturePayload()));
    }



    @ParameterizedTest
    @MethodSource("validOutputDefinitions")
    void testLoadValidOutputDefinitionFiles(final String fileName, final TaxOutputDefinition expectedFixture)
            throws Exception {
        final TaxOutputDefinitionV2 expectedResult = TaxOutputDefinition.Utils.toDTO(expectedFixture);
        final byte[] fileContents = readFileContents(fileName);
        final TaxOutputDefinitionV2 actualResult = parseFileContents(fileContents);
        assertEquals(expectedResult, actualResult, "Wrong DTO structure");
    }

    private byte[] readFileContents(final String fileName) throws IOException {
        try (final InputStream fileStream = CuCoreUtilities.getResourceAsStream(BASE_TEST_XML_PATH + fileName)) {
            return IOUtils.toByteArray(fileStream);
        }
    }

    private TaxOutputDefinitionV2 parseFileContents(final byte[] fileContents) {
        return GlobalResourceLoaderUtils.doWithResourceRetrievalDelegatedToKradResourceLoaderUtil(
                () -> taxOutputDefinitionV2FileType.parse(fileContents));
    }

}
