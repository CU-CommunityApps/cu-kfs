package edu.cornell.kfs.tax.batch.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.function.FailableSupplier;
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
import org.junit.jupiter.params.provider.ValueSource;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.exception.ParseException;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.util.CreateTestDirectories;
import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.sys.util.GlobalResourceLoaderUtils;
import edu.cornell.kfs.sys.util.TestSpringContextExtension;
import edu.cornell.kfs.tax.CuTaxTestConstants.TaxSpringBeans;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxOutputFieldType;
import edu.cornell.kfs.tax.batch.TaxOutputDefinitionV2FileType;
import edu.cornell.kfs.tax.fixture.TaxOutputDefinitionFixture;
import edu.cornell.kfs.tax.fixture.TaxOutputFieldFixture;
import edu.cornell.kfs.tax.fixture.TaxOutputSectionFixture;

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

        @TaxOutputDefinitionFixture(fieldSeparator = KFSConstants.COMMA, sections = {
                @TaxOutputSectionFixture(name = "Example_Section", fields = {
                        @TaxOutputFieldFixture(
                                name = "recipient_id", length = 25,
                                type = TaxOutputFieldType.STRING, key = "recipientId"
                        )
                })
        })
        SINGLE_SECTION_SINGLE_FIELD,

        @TaxOutputDefinitionFixture(fieldSeparator = "\t", includeQuotes = false, sections = {
                @TaxOutputSectionFixture(name = "Tax_Section", useExactFieldLengths = true, fields = {
                        @TaxOutputFieldFixture(
                                name = "recipient_id", length = 25,
                                type = TaxOutputFieldType.STRING, key = "recipientId"
                        ),
                        @TaxOutputFieldFixture(
                                name = "full_name", length = 90,
                                type = TaxOutputFieldType.STRING, key = "recipientName"
                        ),
                        @TaxOutputFieldFixture(
                                name = "tax_amount", length = 15,
                                type = TaxOutputFieldType.AMOUNT, key = "taxAmount"
                        ),
                        @TaxOutputFieldFixture(
                                name = "other_amount", length = 15,
                                type = TaxOutputFieldType.STATIC, value = "0.00"
                        )
                })
        })
        SINGLE_SECTION_MULTIPLE_FIELDS,

        @TaxOutputDefinitionFixture(fieldSeparator = KFSConstants.COMMA, includeQuotes = true,
                amountFormat = "#######.##", percentFormat = "00.00", sections = {
                @TaxOutputSectionFixture(name = "Biographic_Section", fields = {
                        @TaxOutputFieldFixture(
                                name = "recipient_id", length = 25,
                                type = TaxOutputFieldType.STRING, key = "recipientId"
                        ),
                        @TaxOutputFieldFixture(
                                name = "full_name", length = 90,
                                type = TaxOutputFieldType.STRING, key = "recipientName"
                        ),
                        @TaxOutputFieldFixture(
                                name = "primary_address", length = 120,
                                type = TaxOutputFieldType.STRING, key = "recipientAddress"
                        ),
                        @TaxOutputFieldFixture(
                                name = "extra_address", length = 120,
                                type = TaxOutputFieldType.STATIC, value = ""
                        ),
                        @TaxOutputFieldFixture(
                                name = "tax_id", length = 9,
                                type = TaxOutputFieldType.SENSITIVE_STRING, key = "taxId", mask="ZZZZZZZZZ"
                        )
                }),
                @TaxOutputSectionFixture(name = "Payment_Section", fields = {
                        @TaxOutputFieldFixture(
                                name = "recipient_id", length = 25,
                                type = TaxOutputFieldType.STRING, key = "recipientId"
                        ),
                        @TaxOutputFieldFixture(
                                name = "payment_source", length = 10,
                                type = TaxOutputFieldType.STATIC, value = "Cornell"
                        ),
                        @TaxOutputFieldFixture(
                                name = "tax_amount", length = 15,
                                type = TaxOutputFieldType.AMOUNT, key = "taxAmount"
                        )
                })
        })
        MULTIPLE_SECTIONS_MULTIPLE_FIELDS;

        public Named<TaxOutputDefinitionFixture> toNamedAnnotationFixturePayload() {
            return FixtureUtils.createNamedAnnotationFixturePayload(this, TaxOutputDefinitionFixture.class);
        }
    }

    static Stream<Arguments> validOutputDefinitions() {
        return Stream.of(
                Pair.of("good-file-single-section-single-field.xml", LocalTestCase.SINGLE_SECTION_SINGLE_FIELD),
                Pair.of("good-file-single-section-multiple-fields.xml", LocalTestCase.SINGLE_SECTION_MULTIPLE_FIELDS),
                Pair.of("good-file-multiple-sections-multiple-fields.xml",
                        LocalTestCase.MULTIPLE_SECTIONS_MULTIPLE_FIELDS)
        )
                .map(pair -> Arguments.of(pair.getLeft(), pair.getRight().toNamedAnnotationFixturePayload()));
    }



    @ParameterizedTest
    @MethodSource("validOutputDefinitions")
    void testLoadValidOutputDefinitionFiles(final String fileName, final TaxOutputDefinitionFixture expectedFixture)
            throws Exception {
        final TaxOutputDefinitionV2 expectedResult = TaxOutputDefinitionFixture.Utils.toDTO(expectedFixture);
        final byte[] fileContents = readFileContents(fileName);
        final TaxOutputDefinitionV2 actualResult = parseFileContents(fileContents);
        assertEquals(expectedResult, actualResult, "Wrong DTO structure");
    }

    private static byte[] readFileContents(final String fileName) throws IOException {
        try (final InputStream fileStream = CuCoreUtilities.getResourceAsStream(BASE_TEST_XML_PATH + fileName)) {
            return IOUtils.toByteArray(fileStream);
        }
    }

    private TaxOutputDefinitionV2 parseFileContents(final byte[] fileContents) {
        return GlobalResourceLoaderUtils.doWithResourceRetrievalDelegatedToKradResourceLoaderUtil(
                () -> taxOutputDefinitionV2FileType.parse(fileContents));
    }



    static Stream<Arguments> emptyXmlTestCases() {
        final Stream<Named<FailableSupplier<byte[], Exception>>> testCases = Stream.of(
                Named.of("null array", () -> null),
                Named.of("empty array", () -> new byte[0]),
                Named.of("empty file", () -> readFileContents("bad-file-empty.xml"))
        );
        return testCases.map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("emptyXmlTestCases")
    void testInvalidEmptyOutputDefinitionXmlContent(final FailableSupplier<byte[], Exception> testValueSupplier)
            throws Exception {
        final byte[] xmlContents = testValueSupplier.get();
        assertThrows(IllegalArgumentException.class, () -> parseFileContents(xmlContents),
                "The parsing of the empty Tax Output Definition XML should have aborted during setup");
    }



    @ParameterizedTest
    @ValueSource(strings = {
            "bad-file-malformed-xml.xml",
            "bad-file-misnamed-attribute.xml",
            "bad-file-misnamed-root.xml",
            "bad-file-no-fields.xml",
            "bad-file-no-sections.xml",
            "bad-file-invalid-boolean.xml",
            "bad-file-invalid-int.xml",
            "bad-file-missing-field-type.xml",
            "bad-file-missing-field-key-and-value.xml",
            "bad-file-field-with-key-and-value.xml",
            "bad-file-derived-field-with-static-value.xml",
            "bad-file-derived-field-with-blank-key.xml",
            "bad-file-static-field-with-derived-key.xml",
            "bad-file-static-field-with-missing-value-attribute.xml",
            "bad-file-sensitive-field-without-mask.xml",
            "bad-file-non-sensitive-field-with-mask.xml"
    })
    void testInvalidOutputDefinitionFiles(final String fileName) throws Exception {
        final byte[] fileContents = readFileContents(fileName);
        assertThrows(ParseException.class, () -> parseFileContents(fileContents),
                "The parsing of the Tax Output Definition file should have failed");
    }

}
