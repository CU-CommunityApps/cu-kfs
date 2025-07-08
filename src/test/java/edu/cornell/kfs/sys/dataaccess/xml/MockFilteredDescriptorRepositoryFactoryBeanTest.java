package edu.cornell.kfs.sys.dataaccess.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.JDBCType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.DescriptorRepository;
import org.apache.ojb.broker.metadata.FieldDescriptor;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.core.framework.persistence.ojb.conversion.OjbCharBooleanConversion;
import org.kuali.kfs.core.framework.persistence.ojb.conversion.OjbKualiEncryptDecryptFieldConversion;
import org.kuali.kfs.core.framework.persistence.ojb.conversion.OjbKualiIntegerFieldConversion;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.businessobject.ISOFIPSCountryMap;
import edu.cornell.kfs.sys.businessobject.NoteExtendedAttribute;
import edu.cornell.kfs.sys.businessobject.WebServiceCredential;
import edu.cornell.kfs.sys.dataaccess.fixture.ClassDescriptorFixture;
import edu.cornell.kfs.sys.dataaccess.fixture.FieldDescriptorFixture;
import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.tax.businessobject.DvDisbursementView;
import edu.cornell.kfs.tax.businessobject.ObjectCodeBucketMapping;

@Execution(ExecutionMode.SAME_THREAD)
public class MockFilteredDescriptorRepositoryFactoryBeanTest {

    private static final String CU_OJB_SYS_XML_FILE = "classpath:edu/cornell/kfs/sys/cu-ojb-sys.xml";
    private static final String CU_OJB_TAX_XML_FILE = "classpath:edu/cornell/kfs/tax/cu-ojb-tax.xml";
    private static final String CU_OJB_VND_XML_FILE = "classpath:edu/cornell/kfs/vnd/cu-ojb-vnd.xml";

    enum TestClassDescriptor {
        @ClassDescriptorFixture(mappedClass = NoteExtendedAttribute.class, table = "KRNS_NTE_TX", fieldDescriptors = {
            @FieldDescriptorFixture(name = "noteIdentifier", column = "NTE_ID", jdbcType = JDBCType.BIGINT),
            @FieldDescriptorFixture(name = "objectId", column = "OBJ_ID", jdbcType = JDBCType.VARCHAR),
            @FieldDescriptorFixture(name = "versionNumber", column = "VER_NBR", jdbcType = JDBCType.BIGINT),
            @FieldDescriptorFixture(name = "copyNoteIndicator", column = "COPY_IND", jdbcType = JDBCType.VARCHAR,
                    conversion = OjbCharBooleanConversion.class)
        })
        NOTE_EXTENDED_ATTRIBUTE,

        @ClassDescriptorFixture(mappedClass = WebServiceCredential.class, table = "CU_WEB_SRVC_CRDNTLS_T", fieldDescriptors = {
            @FieldDescriptorFixture(name = "credentialGroupCode", column = "CRDNTL_GRP_CD", jdbcType = JDBCType.VARCHAR),
            @FieldDescriptorFixture(name = "credentialKey", column = "CRDNTL_KEY", jdbcType = JDBCType.VARCHAR),
            @FieldDescriptorFixture(name = "versionNumber", column = "VER_NBR", jdbcType = JDBCType.BIGINT),
            @FieldDescriptorFixture(name = "objectId", column = "OBJ_ID", jdbcType = JDBCType.VARCHAR),
            @FieldDescriptorFixture(name = "credentialValue", column = "CRDNTL_VAL", jdbcType = JDBCType.VARCHAR,
                    conversion = OjbKualiEncryptDecryptFieldConversion.class),
            @FieldDescriptorFixture(name = "active", column = "ACTV_IND", jdbcType = JDBCType.VARCHAR,
                    conversion = OjbCharBooleanConversion.class),
            @FieldDescriptorFixture(name = "lastUpdatedTimestamp", column = "LAST_UPDT_TS", jdbcType = JDBCType.TIMESTAMP)
        })
        WEB_SERVICE_CREDENTIAL,

        @ClassDescriptorFixture(mappedClass = ISOFIPSCountryMap.class, table = "CU_ISO_FIPS_CNTRY_MAP_T", fieldDescriptors = {
            @FieldDescriptorFixture(name = "isoCountryCode", column = "ISO_POSTAL_CNTRY_CD", jdbcType = JDBCType.VARCHAR),
            @FieldDescriptorFixture(name = "fipsCountryCode", column = "FIPS_POSTAL_CNTRY_CD", jdbcType = JDBCType.VARCHAR),
            @FieldDescriptorFixture(name = "objectId", column = "OBJ_ID", jdbcType = JDBCType.VARCHAR),
            @FieldDescriptorFixture(name = "versionNumber", column = "VER_NBR", jdbcType = JDBCType.BIGINT),
            @FieldDescriptorFixture(name = "active", column = "ACTV_IND", jdbcType = JDBCType.VARCHAR,
                    conversion = OjbCharBooleanConversion.class),
            @FieldDescriptorFixture(name = "lastUpdatedTimestamp", column = "LAST_UPDT_TS", jdbcType = JDBCType.TIMESTAMP)
        })
        ISO_FIPS_COUNTRY_MAP,

        @ClassDescriptorFixture(mappedClass = ObjectCodeBucketMapping.class, table = "TX_OBJ_CODE_BUCKET_T", fieldDescriptors = {
            @FieldDescriptorFixture(name = "financialObjectCode", column = "FIN_OBJECT_CD", jdbcType = JDBCType.VARCHAR),
            @FieldDescriptorFixture(name = "dvPaymentReasonCode", column = "DV_PMT_REAS_CD", jdbcType = JDBCType.VARCHAR),
            @FieldDescriptorFixture(name = "boxNumber", column = "BOX_NBR", jdbcType = JDBCType.VARCHAR),
            @FieldDescriptorFixture(name = "objectId", column = "OBJ_ID", jdbcType = JDBCType.VARCHAR),
            @FieldDescriptorFixture(name = "versionNumber", column = "VER_NBR", jdbcType = JDBCType.BIGINT),
            @FieldDescriptorFixture(name = "active", column = "ACTV_IND", jdbcType = JDBCType.VARCHAR,
                    conversion = OjbCharBooleanConversion.class),
            @FieldDescriptorFixture(name = "lastUpdatedTimestamp", column = "LAST_UPDT_TS", jdbcType = JDBCType.TIMESTAMP),
            @FieldDescriptorFixture(name = "formType", column = "FORM_TYPE", jdbcType = JDBCType.VARCHAR)
        })
        OBJECT_CODE_BUCKET_MAPPING,

        @ClassDescriptorFixture(mappedClass = DvDisbursementView.class, table = "TX_DV_DISBURSEMENT_V", fieldDescriptors = {
            @FieldDescriptorFixture(name = "custPaymentDocNbr", column = "CUST_PMT_DOC_NBR", jdbcType = JDBCType.VARCHAR),
            @FieldDescriptorFixture(name = "disbursementNbr", column = "DISB_NBR", jdbcType = JDBCType.BIGINT,
                    conversion = OjbKualiIntegerFieldConversion.class),
            @FieldDescriptorFixture(name = "paymentStatusCode", column = "PMT_STAT_CD", jdbcType = JDBCType.VARCHAR),
            @FieldDescriptorFixture(name = "disbursementTypeCode", column = "DISB_TYP_CD", jdbcType = JDBCType.VARCHAR)
        })
        DV_DISBURSEMENT_VIEW;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface TestCaseData {
        String[] fileNames();
        TestClassDescriptor[] descriptorsToKeep();
        TestClassDescriptor[] expectedResultsOverride() default {};
        boolean expectEmptyResults() default false;
    }

    enum LocalTestCase {
        @TestCaseData(fileNames = {}, descriptorsToKeep = {}, expectEmptyResults = true)
        EMPTY_FILE_LIST,

        @TestCaseData(
            fileNames = { CU_OJB_SYS_XML_FILE },
            descriptorsToKeep = { TestClassDescriptor.NOTE_EXTENDED_ATTRIBUTE }
        )
        SINGLE_FILE_SINGLE_DESCRIPTOR,

        @TestCaseData(
            fileNames = { CU_OJB_SYS_XML_FILE },
            descriptorsToKeep = {
                TestClassDescriptor.NOTE_EXTENDED_ATTRIBUTE,
                TestClassDescriptor.WEB_SERVICE_CREDENTIAL,
                TestClassDescriptor.ISO_FIPS_COUNTRY_MAP
            }
        )
        SINGLE_FILE_MULTIPLE_DESCRIPTORS,

        @TestCaseData(
            fileNames = { CU_OJB_SYS_XML_FILE, CU_OJB_TAX_XML_FILE },
            descriptorsToKeep = {
                TestClassDescriptor.NOTE_EXTENDED_ATTRIBUTE,
                TestClassDescriptor.WEB_SERVICE_CREDENTIAL,
                TestClassDescriptor.ISO_FIPS_COUNTRY_MAP,
                TestClassDescriptor.OBJECT_CODE_BUCKET_MAPPING,
                TestClassDescriptor.DV_DISBURSEMENT_VIEW
            }
        )
        MULTIPLE_FILES_MULTIPLE_DESCRIPTORS,

        @TestCaseData(
            fileNames = { CU_OJB_TAX_XML_FILE },
            descriptorsToKeep = {
                TestClassDescriptor.NOTE_EXTENDED_ATTRIBUTE,
                TestClassDescriptor.WEB_SERVICE_CREDENTIAL,
                TestClassDescriptor.ISO_FIPS_COUNTRY_MAP
            },
            expectEmptyResults = true
        )
        MISMATCHED_FILE_AND_DESCRIPTORS,

        @TestCaseData(
            fileNames = { CU_OJB_SYS_XML_FILE, CU_OJB_VND_XML_FILE },
            descriptorsToKeep = {
                TestClassDescriptor.WEB_SERVICE_CREDENTIAL,
                TestClassDescriptor.ISO_FIPS_COUNTRY_MAP,
                TestClassDescriptor.OBJECT_CODE_BUCKET_MAPPING,
                TestClassDescriptor.DV_DISBURSEMENT_VIEW
            },
            expectedResultsOverride = {
                TestClassDescriptor.WEB_SERVICE_CREDENTIAL,
                TestClassDescriptor.ISO_FIPS_COUNTRY_MAP
            }
        )
        PARTIAL_DESCRIPTOR_MATCH,

        @TestCaseData(
            fileNames = { CU_OJB_TAX_XML_FILE, CU_OJB_VND_XML_FILE },
            descriptorsToKeep = {
                TestClassDescriptor.WEB_SERVICE_CREDENTIAL,
                TestClassDescriptor.ISO_FIPS_COUNTRY_MAP,
                TestClassDescriptor.OBJECT_CODE_BUCKET_MAPPING,
                TestClassDescriptor.DV_DISBURSEMENT_VIEW
            },
            expectedResultsOverride = {
                TestClassDescriptor.OBJECT_CODE_BUCKET_MAPPING,
                TestClassDescriptor.DV_DISBURSEMENT_VIEW
            }
        )
        PARTIAL_DESCRIPTOR_MATCH_2ND_CASE;
    }

    static Stream<TestCaseData> testCases() {
        return Arrays.stream(LocalTestCase.values())
                .map(testCase -> FixtureUtils.getAnnotationBasedFixture(testCase, TestCaseData.class));
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testFilterOjbDescriptorsByClassname(final TestCaseData testCase) throws Exception {
        final Set<String> descriptorsToKeep = getDescriptorsToKeep(testCase,
                descriptor -> descriptor.mappedClass().getName());
        assertFactoryBeanFiltersDescriptorsProperly(testCase, descriptorsToKeep);
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testFilterOjbDescriptorsByTableName(final TestCaseData testCase) throws Exception {
        final Set<String> descriptorsToKeep = getDescriptorsToKeep(testCase,
                descriptor -> descriptor.table());
        assertFactoryBeanFiltersDescriptorsProperly(testCase, descriptorsToKeep);
    }

    private Set<String> getDescriptorsToKeep(final TestCaseData testCase,
            final Function<ClassDescriptorFixture, String> classDescriptorKeyProperty) {
        return Arrays.stream(testCase.descriptorsToKeep())
                .map(descriptor -> FixtureUtils.getAnnotationBasedFixture(descriptor, ClassDescriptorFixture.class))
                .map(classDescriptorKeyProperty)
                .collect(Collectors.toUnmodifiableSet());
    }

    private void assertFactoryBeanFiltersDescriptorsProperly(final TestCaseData testCase,
            final Set<String> descriptorsToKeep) throws Exception {
        final MockFilteredDescriptorRepositoryFactoryBean factoryBean = new MockFilteredDescriptorRepositoryFactoryBean();
        factoryBean.setOjbRepositoryFiles(List.of(testCase.fileNames()));
        factoryBean.setDescriptorsToKeep(descriptorsToKeep);
        factoryBean.afterPropertiesSet();

        final DescriptorRepository mockRepository = factoryBean.getObject();
        final List<ClassDescriptorFixture> expectedDescriptors = getExpectedClassDescriptors(testCase);
        assertClassDescriptorsAreCorrect(expectedDescriptors, mockRepository);
    }

    private List<ClassDescriptorFixture> getExpectedClassDescriptors(final TestCaseData testCase) {
        if (testCase.expectEmptyResults()) {
            return List.of();
        } else {
            final TestClassDescriptor[] descriptorFixtures = (testCase.expectedResultsOverride().length > 0)
                    ? testCase.expectedResultsOverride() : testCase.descriptorsToKeep();
            return Arrays.stream(descriptorFixtures)
                    .map(descriptor -> FixtureUtils.getAnnotationBasedFixture(descriptor, ClassDescriptorFixture.class))
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    @SuppressWarnings("unchecked")
    private void assertClassDescriptorsAreCorrect(final List<ClassDescriptorFixture> expectedDescriptors,
            final DescriptorRepository mockRepository) {
        final Map<?, ?> descriptorTable = mockRepository.getDescriptorTable();
        int index = 0;
        for (final Object classDescriptor : IteratorUtils.asIterable(mockRepository.iterator())) {
            final ClassDescriptorFixture expectedClassDescriptor = expectedDescriptors.get(index);
            assertBasicFieldsOnClassDescriptorAreCorrect(expectedClassDescriptor, classDescriptor, index);
            
            final Class<?> mappedClass = expectedClassDescriptor.mappedClass();
            final Object descriptorFromMap = descriptorTable.get(mappedClass.getName());
            final Object descriptorByClass = mockRepository.getDescriptorFor(mappedClass);
            final Object descriptorByClassname = mockRepository.getDescriptorFor(mappedClass.getName());
            assertBasicFieldsOnClassDescriptorAreCorrect(expectedClassDescriptor, descriptorFromMap, index);
            assertBasicFieldsOnClassDescriptorAreCorrect(expectedClassDescriptor, descriptorByClass, index);
            assertBasicFieldsOnClassDescriptorAreCorrect(expectedClassDescriptor, descriptorByClassname, index);

            assertFieldDescriptorsAreCorrect(expectedClassDescriptor, (ClassDescriptor) classDescriptor, index);

            index++;
        }

        assertEquals(expectedDescriptors.size(), index, "Wrong number of class descriptors returned by filter");
    }

    private void assertBasicFieldsOnClassDescriptorAreCorrect(final ClassDescriptorFixture expectedDescriptor,
            final Object classDescriptor, final int index) {
        assertNotNull(classDescriptor, "Found a null class descriptor at index " + index);
        assertTrue(classDescriptor instanceof ClassDescriptor,
                "Found a non-ClassDescriptor object at index " + index);

        final ClassDescriptor actualDescriptor = (ClassDescriptor) classDescriptor;
        assertEquals(expectedDescriptor.mappedClass(), actualDescriptor.getClassOfObject(),
                "Wrong mapped class for descriptor at index " + index);
        assertEquals(expectedDescriptor.mappedClass().getName(), actualDescriptor.getClassNameOfObject(),
                "Wrong mapped classname for descriptor at index " + index);
        assertEquals(expectedDescriptor.table(), actualDescriptor.getFullTableName(),
                "Wrong table name for descriptor at index " + index);
    }

    private void assertFieldDescriptorsAreCorrect(final ClassDescriptorFixture expectedClassDescriptor,
            final ClassDescriptor actualClassDescriptor, final int classDescriptorIndex) {
        final FieldDescriptorFixture[] expectedFields = expectedClassDescriptor.fieldDescriptors();
        final FieldDescriptor[] actualFields = actualClassDescriptor.getFieldDescriptions();
        assertNotNull(actualFields, "Null field array detected on class descriptor at index " + classDescriptorIndex);
        assertEquals(expectedFields.length, actualFields.length,
                "Wrong number of fields on class descriptor at index " + classDescriptorIndex);
        
        for (int fieldIndex = 0; fieldIndex < expectedFields.length; fieldIndex++) {
            final String indexToPrint = classDescriptorIndex + CUKFSConstants.COMMA_AND_SPACE + fieldIndex;
            final FieldDescriptorFixture expectedField = expectedFields[fieldIndex];
            final FieldDescriptor actualField = actualFields[fieldIndex];
            assertNotNull(actualField, "Null field detected at field index " + indexToPrint);
            assertFieldDescriptorIsCorrect(expectedField, actualField, indexToPrint);

            final FieldDescriptor fieldByName = actualClassDescriptor.getFieldDescriptorByName(expectedField.name());
            final FieldDescriptor fieldByIndex = actualClassDescriptor.getFieldDescriptorByIndex(fieldIndex);
            assertNotNull(fieldByName, "Null field-by-name retrieval detected at field index " + indexToPrint);
            assertNotNull(fieldByIndex, "Null field-by-index retrieval detected at field index " + indexToPrint);
            assertFieldDescriptorIsCorrect(expectedField, fieldByName, indexToPrint);
            assertFieldDescriptorIsCorrect(expectedField, fieldByIndex, indexToPrint);
        }
    }

    private void assertFieldDescriptorIsCorrect(final FieldDescriptorFixture expectedField,
            final FieldDescriptor actualField, final String fieldIndex) {
        assertEquals(expectedField.name(), actualField.getAttributeName(),
                "Wrong attribute name at field index " + fieldIndex);
        assertEquals(expectedField.column(), actualField.getColumnName(),
                "Wrong column name at field index " + fieldIndex);
        assertEquals(expectedField.jdbcType().getName(), actualField.getColumnType(),
                "Wrong column type at field index " + fieldIndex);
        assertNotNull(actualField.getJdbcType(), "Null JDBC Type detected at field index " + fieldIndex);
        assertEquals(expectedField.jdbcType().getVendorTypeNumber(), actualField.getJdbcType().getType(),
                "Wrong column JDBC type code/number at field index " + fieldIndex);
        assertNotNull(actualField.getFieldConversion(), "Null field conversion detected at field index " + fieldIndex);
        assertEquals(expectedField.conversion().getName(), actualField.getFieldConversion().getClass().getName(),
                "Wrong explicit or default field conversion implementation at field index " + fieldIndex);
    }

}
