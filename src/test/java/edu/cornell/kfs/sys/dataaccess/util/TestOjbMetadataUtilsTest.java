package edu.cornell.kfs.sys.dataaccess.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.sql.JDBCType;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.DescriptorRepository;
import org.apache.ojb.broker.metadata.FieldDescriptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.kuali.kfs.core.framework.persistence.ojb.conversion.OjbCharBooleanConversion;
import org.kuali.kfs.core.framework.persistence.ojb.conversion.OjbKualiEncryptDecryptFieldConversion;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.annotation.ForCodeDocumentationOnly;
import edu.cornell.kfs.sys.businessobject.TestOjbMaintenanceDocument;
import edu.cornell.kfs.sys.businessobject.TestOjbState;
import edu.cornell.kfs.sys.dataaccess.fixture.ClassDescriptorFixture;
import edu.cornell.kfs.sys.dataaccess.fixture.DescriptorRepositoryFixture;
import edu.cornell.kfs.sys.dataaccess.fixture.FieldDescriptorFixture;
import edu.cornell.kfs.sys.dataaccess.xml.TestDescriptorRepositoryDto;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import edu.cornell.kfs.sys.util.FixtureUtils;

@Execution(ExecutionMode.SAME_THREAD)
public class TestOjbMetadataUtilsTest {

    private static final String TEST_OJB_BASE_FILE_PATH = "classpath:edu/cornell/kfs/sys/ojb-utils/";

    private CUMarshalService cuMarshalService;



    @BeforeEach
    void setUp() throws Exception {
        cuMarshalService = new CUMarshalServiceImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        cuMarshalService = null;
    }



    @ForCodeDocumentationOnly
    private static @interface CombineExpectedItemsFromOtherTests {

    }

    enum LocalTestCase {

        @DescriptorRepositoryFixture(classDescriptors = {
                @ClassDescriptorFixture(mappedClass = TestOjbMaintenanceDocument.class, table = "TEST_MAINT_DOC_T",
                        fieldDescriptors = {
                                @FieldDescriptorFixture(name = "documentNumber", column = "DOC_HDR_ID",
                                        jdbcType = JDBCType.VARCHAR),
                                @FieldDescriptorFixture(name = "objectId", column = "OBJ_ID",
                                        jdbcType = JDBCType.VARCHAR),
                                @FieldDescriptorFixture(name = "versionNumber", column = "VER_NBR",
                                        jdbcType = JDBCType.BIGINT),
                                @FieldDescriptorFixture(name = "xmlDocumentContents", column = "DOC_CNTNT",
                                        jdbcType = JDBCType.CLOB,
                                        conversion = OjbKualiEncryptDecryptFieldConversion.class)
                        }        
                )
        })
        OJB_METADATA_FOR_TEST_MAINT_DOCUMENT("test-ojb-maintenance-doc.xml"),

        @DescriptorRepositoryFixture(classDescriptors = {
                @ClassDescriptorFixture(mappedClass = TestOjbState.class, table = "TEST_ST_T",
                        fieldDescriptors = {
                                @FieldDescriptorFixture(name = "countryCode", column = "POSTAL_CNTRY_CD",
                                        jdbcType = JDBCType.VARCHAR),
                                @FieldDescriptorFixture(name = "code", column = "POSTAL_STATE_CD",
                                        jdbcType = JDBCType.VARCHAR),
                                @FieldDescriptorFixture(name = "name", column = "POSTAL_STATE_NM",
                                        jdbcType = JDBCType.VARCHAR),
                                @FieldDescriptorFixture(name = "objectId", column = "OBJ_ID",
                                        jdbcType = JDBCType.VARCHAR),
                                @FieldDescriptorFixture(name = "versionNumber", column = "VER_NBR",
                                        jdbcType = JDBCType.BIGINT),
                                @FieldDescriptorFixture(name = "lastUpdatedTimestamp", column = "LAST_UPDT_TS",
                                        jdbcType = JDBCType.TIMESTAMP),
                                @FieldDescriptorFixture(name = "active", column = "ACTV_IND",
                                        jdbcType = JDBCType.VARCHAR, conversion = OjbCharBooleanConversion.class)
                        }        
                )
        })
        OJB_METADATA_FOR_TEST_STATE("test-ojb-state.xml"),

        @CombineExpectedItemsFromOtherTests
        OJB_METDATA_FOR_TEST_MAINT_DOCUMENT_AND_STATE("test-ojb-maintenance-doc-and-state.xml",
                OJB_METADATA_FOR_TEST_MAINT_DOCUMENT, OJB_METADATA_FOR_TEST_STATE);

        private final String ojbFileName;
        private final List<LocalTestCase> testCasesToCombine;

        private LocalTestCase(final String ojbFileName, final LocalTestCase... testCasesToCombine) {
            this.ojbFileName = ojbFileName;
            this.testCasesToCombine = List.of(testCasesToCombine);
        }

        private List<ClassDescriptorFixture> getExpectedClassDescriptors() {
            if (testCasesToCombine.size() > 0) {
                final List<DescriptorRepositoryFixture> repositoryFixtures = getDescriptorRepositoriesToCombine();
                return repositoryFixtures.stream()
                        .map(DescriptorRepositoryFixture::classDescriptors)
                        .flatMap(Arrays::stream)
                        .collect(Collectors.toUnmodifiableList());
            } else {
                final DescriptorRepositoryFixture repositoryFixture = getDescriptorRepositoryFixture();
                return List.of(repositoryFixture.classDescriptors());
            }
        }

        private DescriptorRepository toOjbDescriptorRepository() {
            if (testCasesToCombine.size() > 0) {
                return DescriptorRepositoryFixture.Utils.toCombinedOjbDescriptorRepository(
                        getDescriptorRepositoriesToCombine());
            } else {
                return DescriptorRepositoryFixture.Utils.toOjbDescriptorRepository(getDescriptorRepositoryFixture());
            }
        }

        private List<DescriptorRepositoryFixture> getDescriptorRepositoriesToCombine() {
            return testCasesToCombine.stream()
                    .map(LocalTestCase::getDescriptorRepositoryFixture)
                    .collect(Collectors.toUnmodifiableList());
        }

        private DescriptorRepositoryFixture getDescriptorRepositoryFixture() {
            return FixtureUtils.getAnnotationBasedFixture(this, DescriptorRepositoryFixture.class);
        }
    }



    @ParameterizedTest
    @EnumSource(LocalTestCase.class)
    void testCreateAnnotationBasedDescriptorRepository(final LocalTestCase testCase) throws Exception {
        final DescriptorRepository mockRepository = testCase.toOjbDescriptorRepository();
        assertDescriptorRepositoryWasGeneratedCorrectly(testCase, mockRepository);
    }

    @ParameterizedTest
    @EnumSource(LocalTestCase.class)
    void testCreateJaxbBasedDescriptorRepository(final LocalTestCase testCase) throws Exception {
        final TestDescriptorRepositoryDto xmlRepository = getDescriptorRepositoryFromXml(testCase.ojbFileName);
        final DescriptorRepository mockRepository = xmlRepository.toOjbDescriptorRepository();
        assertDescriptorRepositoryWasGeneratedCorrectly(testCase, mockRepository);
    }

    private TestDescriptorRepositoryDto getDescriptorRepositoryFromXml(final String fileName) throws Exception {
        final String fullFileName = TEST_OJB_BASE_FILE_PATH + fileName;
        try (final InputStream fileStream = CuCoreUtilities.getResourceAsStream(fullFileName)) {
            return cuMarshalService.unmarshalStream(fileStream, TestDescriptorRepositoryDto.class);
        }
    }

    private void assertDescriptorRepositoryWasGeneratedCorrectly(final LocalTestCase testCase,
            final DescriptorRepository mockRepository) {
        final List<ClassDescriptorFixture> expectedClassDescriptors = testCase.getExpectedClassDescriptors();
        assertEquals(expectedClassDescriptors.size(), mockRepository.getDescriptorTable().size(),
                "Wrong number of class descriptors");

        int i = 0;
        for (final Object classDescriptor : IteratorUtils.asIterable((Iterator<?>) mockRepository.iterator())) {
            final ClassDescriptorFixture classDescriptorFixture = expectedClassDescriptors.get(i);
            final ClassDescriptor mockClassDescriptor = (ClassDescriptor) classDescriptor;
            final Class<?> expectedObjectClass = classDescriptorFixture.mappedClass();

            assertEquals(expectedObjectClass, mockClassDescriptor.getClassOfObject(),
                    "Wrong persisted object class");
            assertEquals(expectedObjectClass.getName(), mockClassDescriptor.getClassNameOfObject(),
                    "Wrong persisted object classname");
            assertEquals(classDescriptorFixture.table(), mockClassDescriptor.getFullTableName(), "Wrong table name");
            assertTrue(mockClassDescriptor == mockRepository.getDescriptorFor(expectedObjectClass),
                    "Class-based lookup did not return the expected descriptor for " + expectedObjectClass);
            assertTrue(mockClassDescriptor == mockRepository.getDescriptorFor(expectedObjectClass.getName()),
                    "Classname-based lookup did not return the expected descriptor for " + expectedObjectClass);
            assertClassDescriptorHasCorrectFieldDescriptors(classDescriptorFixture, mockClassDescriptor);

            i++;
        }
    }

    private void assertClassDescriptorHasCorrectFieldDescriptors(final ClassDescriptorFixture classDescriptorFixture,
            final ClassDescriptor mockClassDescriptor) {
        final FieldDescriptorFixture[] expectedFieldDescriptors = classDescriptorFixture.fieldDescriptors();
        final FieldDescriptor[] mockFieldDescriptors = mockClassDescriptor.getFieldDescriptions();
        assertEquals(expectedFieldDescriptors.length, mockFieldDescriptors.length,
                "Wrong number of field descriptors");

        for (int i = 0; i < expectedFieldDescriptors.length; i++) {
            final FieldDescriptorFixture fieldDescriptorFixture = expectedFieldDescriptors[i];
            final FieldDescriptor mockFieldDescriptor = mockFieldDescriptors[i];
            final String expectedFieldName = fieldDescriptorFixture.name();

            assertEquals(expectedFieldName, mockFieldDescriptor.getAttributeName(), "Wrong field name");
            assertEquals(fieldDescriptorFixture.column(), mockFieldDescriptor.getColumnName(), "Wrong column name");
            assertEquals(fieldDescriptorFixture.jdbcType().name(), mockFieldDescriptor.getColumnType(),
                    "Wrong JDBC type string");
            assertEquals(fieldDescriptorFixture.jdbcType().getVendorTypeNumber(),
                    mockFieldDescriptor.getJdbcType().getType(), "Wrong JDBC type integer");

            if (OjbKualiEncryptDecryptFieldConversion.class.equals(fieldDescriptorFixture.conversion())) {
                assertEquals(TestOjbKualiEncryptDecryptFieldConversion.class,
                        mockFieldDescriptor.getFieldConversion().getClass(), "Wrong encrypt/decrypt conversion class");
            } else {
                assertEquals(fieldDescriptorFixture.conversion(), mockFieldDescriptor.getFieldConversion().getClass(),
                        "Wrong field conversion class");
            }

            assertTrue(mockClassDescriptor.getFieldDescriptorByName(expectedFieldName) == mockFieldDescriptor,
                    "Field-name-based lookup did not return the expected descriptor for " + expectedFieldName);
        }
    }

}
