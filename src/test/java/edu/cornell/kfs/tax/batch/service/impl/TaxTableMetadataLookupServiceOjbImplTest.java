package edu.cornell.kfs.tax.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.JDBCType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.exception.ClassNotPersistableException;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import edu.cornell.kfs.sys.annotation.XmlAttributeMatcher;
import edu.cornell.kfs.sys.annotation.XmlDocumentFilter;
import edu.cornell.kfs.sys.annotation.XmlElementFilter;
import edu.cornell.kfs.sys.util.CreateTestDirectories;
import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.sys.util.TestSpringContextExtension;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.QueryTableAliases;
import edu.cornell.kfs.tax.batch.dto.DocumentHeaderLite;
import edu.cornell.kfs.tax.batch.dto.NoteLite;
import edu.cornell.kfs.tax.batch.dto.TestTaxObjectLite;
import edu.cornell.kfs.tax.batch.dto.VendorAddressLite;
import edu.cornell.kfs.tax.batch.dto.VendorDetailLite;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoFieldDefinition;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoMappingDefinition;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoTableDefinition;
import edu.cornell.kfs.tax.batch.metadata.fixture.TaxDtoFieldDefinitionFixture;
import edu.cornell.kfs.tax.batch.metadata.fixture.TaxDtoMappingDefinitionFixture;
import edu.cornell.kfs.tax.batch.metadata.fixture.TaxDtoTableDefinitionFixture;
import edu.cornell.kfs.tax.businessobject.TestTaxObject;

@Execution(ExecutionMode.SAME_THREAD)
@CreateTestDirectories(
        baseDirectory = TaxTableMetadataLookupServiceOjbImplTest.BASE_TEST_DIRECTORY,
        subDirectories = {
                TaxTableMetadataLookupServiceOjbImplTest.FILTERED_OJB_XML_FILES_DIRECTORY
        },
        createBeforeEachTest = false
)
@XmlDocumentFilter(rootElementName = "descriptor-repository", directChildElementsToKeep = {
        @XmlElementFilter(name = "class-descriptor", matchConditions = {
                @XmlAttributeMatcher(name = "table", values = {
                        "FS_DOC_HEADER_T", "PUR_VNDR_HDR_T", "PUR_VNDR_DTL_T", "TEST_TAX_OBJECT_T"
                })
        })
})
public class TaxTableMetadataLookupServiceOjbImplTest {

    public static final String BASE_TEST_DIRECTORY = "test/tax-metdata-lookup/";
    public static final String FILTERED_OJB_XML_FILES_DIRECTORY = BASE_TEST_DIRECTORY + "ojb-files/";

    private static final String LOOKUP_SERVICE_BEAN_NAME = "taxTableMetadataLookupService";

    private static final Map<Class<?>, Object> DEFAULT_FIELD_VALUES_FOR_TESTING = Map.ofEntries(
            Map.entry(boolean.class, false),
            Map.entry(int.class, 0),
            Map.entry(long.class, 0L));

    private static final Map<Class<?>, Object> NEW_FIELD_VALUES_FOR_TESTING = Map.ofEntries(
            Map.entry(boolean.class, true),
            Map.entry(Boolean.class, true),
            Map.entry(int.class, 100),
            Map.entry(Integer.class, 101),
            Map.entry(long.class, 5000L),
            Map.entry(Long.class, 5005L),
            Map.entry(KualiDecimal.class, new KualiDecimal(27999)),
            Map.entry(String.class, "This is a new value!"));

    @RegisterExtension
    static TestSpringContextExtension springContextExtension = TestSpringContextExtension.forClassPathSpringXmlFile(
            "edu/cornell/kfs/tax/batch/cu-spring-tax-metadata-lookup-test.xml");

    private TaxTableMetadataLookupServiceOjbImpl taxTableMetadataLookupService;

    @BeforeEach
    void setUp() throws Exception {
        taxTableMetadataLookupService = springContextExtension.getBean(
                LOOKUP_SERVICE_BEAN_NAME, TaxTableMetadataLookupServiceOjbImpl.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        taxTableMetadataLookupService = null;
    }

    

    enum ExpectedMappingDefinition {

        @TaxDtoMappingDefinitionFixture(dtoClass = TestTaxObjectLite.class,
                businessObjectMappings = {
                        @TaxDtoTableDefinitionFixture(businessObjectClass = TestTaxObject.class,
                                tableName = "TEST_TAX_OBJECT_T")
                },
                fieldMappings = {
                        @TaxDtoFieldDefinitionFixture(propertyName = "ID", fieldClass = Long.class,
                                jdbcType = JDBCType.BIGINT, columnLabel = "TAX_OBJ_ID"),
                        @TaxDtoFieldDefinitionFixture(propertyName = "eInvoiceId", fieldClass = String.class,
                                jdbcType = JDBCType.VARCHAR, columnLabel = "EINVOICE_ID"),
                        @TaxDtoFieldDefinitionFixture(propertyName = "amount", fieldClass = KualiDecimal.class,
                                jdbcType = JDBCType.DECIMAL, columnLabel = "AMT", hasFieldConverter = true),
                        @TaxDtoFieldDefinitionFixture(propertyName = "secretValue", fieldClass = String.class,
                                jdbcType = JDBCType.VARCHAR, columnLabel = "SECRET_VAL", hasFieldConverter = true),
                        @TaxDtoFieldDefinitionFixture(propertyName = "computedLabel", fieldClass = String.class,
                                jdbcType = JDBCType.VARCHAR, columnLabel = "ITM_LBL"),
                        @TaxDtoFieldDefinitionFixture(propertyName = "count", fieldClass = int.class,
                                jdbcType = JDBCType.INTEGER, columnLabel = "CNT"),
                        @TaxDtoFieldDefinitionFixture(propertyName = "active", fieldClass = boolean.class,
                                jdbcType = JDBCType.VARCHAR, columnLabel = "ACTV_IND", hasFieldConverter = true)
                }
        )
        TEST_TAX_OBJECT_LITE,

        @TaxDtoMappingDefinitionFixture(dtoClass = DocumentHeaderLite.class,
                businessObjectMappings = {
                        @TaxDtoTableDefinitionFixture(businessObjectClass = DocumentHeader.class,
                                tableName = "FS_DOC_HEADER_T")
                },
                fieldMappings = {
                        @TaxDtoFieldDefinitionFixture(propertyName = "documentNumber", fieldClass = String.class,
                                jdbcType = JDBCType.VARCHAR, columnLabel = "FDOC_NBR"),
                        @TaxDtoFieldDefinitionFixture(propertyName = "objectId", fieldClass = String.class,
                            jdbcType = JDBCType.VARCHAR, columnLabel = "OBJ_ID")
                }
        )
        DOCUMENT_HEADER_LITE,

        @TaxDtoMappingDefinitionFixture(dtoClass = VendorDetailLite.class,
                businessObjectMappings = {
                        @TaxDtoTableDefinitionFixture(businessObjectClass = VendorHeader.class,
                                tableName = "PUR_VNDR_HDR_T", tableAliasForQuery = QueryTableAliases.VENDOR_HEADER),
                        @TaxDtoTableDefinitionFixture(businessObjectClass = VendorDetail.class,
                                tableName = "PUR_VNDR_DTL_T", tableAliasForQuery = QueryTableAliases.VENDOR_DETAIL)
                },
                fieldMappings = {
                        @TaxDtoFieldDefinitionFixture(propertyName = "vendorHeaderGeneratedIdentifier",
                                columnLabel = QueryTableAliases.VENDOR_DETAIL + ".VNDR_HDR_GNRTD_ID",
                                fieldClass = Integer.class, jdbcType = JDBCType.INTEGER),
                        @TaxDtoFieldDefinitionFixture(propertyName = "vendorDetailAssignedIdentifier",
                                columnLabel = QueryTableAliases.VENDOR_DETAIL + ".VNDR_DTL_ASND_ID",
                                fieldClass = Integer.class, jdbcType = JDBCType.INTEGER),
                        @TaxDtoFieldDefinitionFixture(propertyName = "vendorParentIndicator",
                                columnLabel = QueryTableAliases.VENDOR_DETAIL + ".VNDR_PARENT_IND",
                                fieldClass = boolean.class, jdbcType = JDBCType.VARCHAR, hasFieldConverter = true),
                        @TaxDtoFieldDefinitionFixture(propertyName = "vendorFirstLastNameIndicator",
                                columnLabel = QueryTableAliases.VENDOR_DETAIL + ".VNDR_1ST_LST_NM_IND",
                                fieldClass = boolean.class, jdbcType = JDBCType.VARCHAR, hasFieldConverter = true),
                        @TaxDtoFieldDefinitionFixture(propertyName = "vendorName",
                                columnLabel = QueryTableAliases.VENDOR_DETAIL + ".VNDR_NM",
                                fieldClass = String.class, jdbcType = JDBCType.VARCHAR),
                        @TaxDtoFieldDefinitionFixture(propertyName = "vendorTaxNumber",
                                columnLabel = QueryTableAliases.VENDOR_HEADER + ".VNDR_US_TAX_NBR",
                                fieldClass = String.class, jdbcType = JDBCType.VARCHAR, hasFieldConverter = true),
                        @TaxDtoFieldDefinitionFixture(propertyName = "vendorTypeCode",
                                columnLabel = QueryTableAliases.VENDOR_HEADER + ".VNDR_TYP_CD",
                                fieldClass = String.class, jdbcType = JDBCType.VARCHAR),
                        @TaxDtoFieldDefinitionFixture(propertyName = "vendorOwnershipCode",
                                columnLabel = QueryTableAliases.VENDOR_HEADER + ".VNDR_OWNR_CD",
                                fieldClass = String.class, jdbcType = JDBCType.VARCHAR),
                        @TaxDtoFieldDefinitionFixture(propertyName = "vendorOwnershipCategoryCode",
                                columnLabel = QueryTableAliases.VENDOR_HEADER + ".VNDR_OWNR_CTGRY_CD",
                                fieldClass = String.class, jdbcType = JDBCType.VARCHAR),
                        @TaxDtoFieldDefinitionFixture(propertyName = "vendorForeignIndicator",
                                columnLabel = QueryTableAliases.VENDOR_HEADER + ".VNDR_FRGN_IND",
                                fieldClass = boolean.class, jdbcType = JDBCType.VARCHAR, hasFieldConverter = true),
                        @TaxDtoFieldDefinitionFixture(propertyName = "vendorGIIN",
                                columnLabel = QueryTableAliases.VENDOR_HEADER + ".VNDR_GIIN",
                                fieldClass = String.class, jdbcType = JDBCType.VARCHAR),
                        @TaxDtoFieldDefinitionFixture(propertyName = "vendorChapter4StatusCode",
                                columnLabel = QueryTableAliases.VENDOR_HEADER + ".VNDR_CHAP_4_STAT_CD",
                                fieldClass = String.class, jdbcType = JDBCType.VARCHAR)
                }
        )
        VENDOR_DETAIL_LITE;

        private Arguments toNamedAnnotationFixtureArgument() {
            return FixtureUtils.createNamedAnnotationFixtureArgument(this, TaxDtoMappingDefinitionFixture.class);
        }
    }

    static Stream<Arguments> expectedDtoMappings() {
        return Arrays.stream(ExpectedMappingDefinition.values())
                .map(ExpectedMappingDefinition::toNamedAnnotationFixtureArgument);
    }



    @ParameterizedTest
    @MethodSource("expectedDtoMappings")
    void testCreationOfMetadataMappingDefinitions(final TaxDtoMappingDefinitionFixture fixture) throws Exception {
        final TaxDtoMappingDefinition<?> mappingDefinition = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(fixture.dtoClass());
        assertDtoMappingDefinitionHasCorrectSetup(fixture, mappingDefinition);
    }

    private <T> void assertDtoMappingDefinitionHasCorrectSetup(final TaxDtoMappingDefinitionFixture fixture,
            final TaxDtoMappingDefinition<T> mappingDefinition) throws Exception {
        assertEquals(fixture.dtoClass(), mappingDefinition.getDtoClass(), "Wrong DTO type");

        final Supplier<T> dtoConstructor = mappingDefinition.getDtoConstructor();
        assertNotNull(dtoConstructor, "DTO constructor lambda should have been defined");

        final T dtoInstance = dtoConstructor.get();
        assertNotNull(dtoInstance, "DTO constructor lambda should have created a new DTO");

        final T dtoInstance2 = dtoConstructor.get();
        assertNotNull(dtoInstance2, "DTO constructor lambda should have created another DTO on the 2nd run");
        assertTrue(dtoInstance != dtoInstance2, "DTO constructor lambda should not be reusing DTO instances");

        assertBusinessObjectMappingsAreCorrect(fixture, mappingDefinition);
        assertFieldMappingsAreCorrect(fixture, mappingDefinition, dtoInstance);
    }

    private void assertBusinessObjectMappingsAreCorrect(final TaxDtoMappingDefinitionFixture fixture,
            final TaxDtoMappingDefinition<?> mappingDefinition) throws Exception {
        final TaxDtoTableDefinitionFixture[] expectedMappings = fixture.businessObjectMappings();
        final List<TaxDtoTableDefinition> actualMappings = mappingDefinition.getBusinessObjectMappings();
        assertEquals(expectedMappings.length, actualMappings.size(), "Wrong number of business object mappings");

        for (int i = 0; i < expectedMappings.length; i++) {
            final TaxDtoTableDefinitionFixture expectedMapping = expectedMappings[i];
            final TaxDtoTableDefinition actualMapping = actualMappings.get(i);
            assertEquals(expectedMapping.businessObjectClass(), actualMapping.getBusinessObjectClass(),
                    "Wrong mapped business object class");
            assertEquals(expectedMapping.tableName(), actualMapping.getTableName(), "Wrong mapped table name");

            if (StringUtils.isNotBlank(expectedMapping.tableAliasForQuery())) {
                assertTrue(actualMapping.hasTableAliasForQuery(), "Mapping should have had a table alias defined");
                assertEquals(expectedMapping.tableAliasForQuery(), actualMapping.getTableAliasForQuery(),
                        "Wrong query table alias");
            } else {
                assertFalse(actualMapping.hasTableAliasForQuery(), "Mapping should not have defined a table alias");
            }
        }
    }

    private <T> void assertFieldMappingsAreCorrect(final TaxDtoMappingDefinitionFixture fixture,
            final TaxDtoMappingDefinition<T> mappingDefinition, final T dtoInstance) throws Exception {
        final TaxDtoFieldDefinitionFixture[] expectedMappings = Arrays.stream(fixture.fieldMappings())
                .sorted(Comparator.comparing(TaxDtoFieldDefinitionFixture::propertyName))
                .toArray(TaxDtoFieldDefinitionFixture[]::new);
        final List<TaxDtoFieldDefinition<T, ?>> actualMappings = mappingDefinition.getFieldMappings();
        assertEquals(expectedMappings.length, actualMappings.size(), "Wrong number of field mappings");

        final BeanWrapper wrappedDtoInstance = PropertyAccessorFactory.forBeanPropertyAccess(dtoInstance);

        for (int i = 0; i < expectedMappings.length; i++) {
            final TaxDtoFieldDefinitionFixture expectedMapping = expectedMappings[i];
            final TaxDtoFieldDefinition<T, ?> actualMapping = actualMappings.get(i);
            assertFieldMappingIsCorrect(expectedMapping, actualMapping, wrappedDtoInstance);
        }
    }

    private <T, U> void assertFieldMappingIsCorrect(final TaxDtoFieldDefinitionFixture expectedMapping,
            final TaxDtoFieldDefinition<T, U> actualMapping, final BeanWrapper wrappedDtoInstance) throws Exception {
        final String propertyName = expectedMapping.propertyName();
        assertEquals(propertyName, actualMapping.getPropertyName(), "Wrong property name");
        assertEquals(expectedMapping.fieldClass(), actualMapping.getFieldClass(), "Wrong field data type");
        assertEquals(expectedMapping.jdbcType(), actualMapping.getJdbcType(), "Wrong JDBC Type");
        assertEquals(expectedMapping.columnLabel(), actualMapping.getColumnLabel(), "Wrong column label");

        if (expectedMapping.hasFieldConverter()) {
            assertTrue(actualMapping.hasFieldConverter(), "Mapping should have been flagged as having a converter");
            assertNotNull(actualMapping.getFieldConverter(), "Mapping should have had a converter instance defined");
        } else {
            assertFalse(actualMapping.hasFieldConverter(), "Mapping should not have defined a converter");
        }

        assertGetterAndSetterLambdasAreCorrect(actualMapping, wrappedDtoInstance);
    }

    private <T, U> void assertGetterAndSetterLambdasAreCorrect(final TaxDtoFieldDefinition<T, U> fieldMapping,
            final BeanWrapper wrappedDtoInstance) throws Exception {
        final T dtoInstance = fieldMapping.getDtoClass().cast(wrappedDtoInstance.getWrappedInstance());
        final BiConsumer<T, U> propertySetter = fieldMapping.getPropertySetter();
        final PropertyDescriptor propertyDescriptor = wrappedDtoInstance.getPropertyDescriptor(
                fieldMapping.getPropertyName());
        final Method reflectionGetterMethod = propertyDescriptor.getReadMethod();

        final Class<U> fieldClass = fieldMapping.getFieldClass();
        final U expectedDefaultValue = getValueForTesting(DEFAULT_FIELD_VALUES_FOR_TESTING, fieldClass);
        final U newFieldValue = getValueForTesting(NEW_FIELD_VALUES_FOR_TESTING, fieldClass);

        assertFieldHasExpectedValue(fieldMapping, reflectionGetterMethod, dtoInstance, expectedDefaultValue);
        propertySetter.accept(dtoInstance, newFieldValue);
        assertFieldHasExpectedValue(fieldMapping, reflectionGetterMethod, dtoInstance, newFieldValue);
        propertySetter.accept(dtoInstance, expectedDefaultValue);
        assertFieldHasExpectedValue(fieldMapping, reflectionGetterMethod, dtoInstance, expectedDefaultValue);
    }

    @SuppressWarnings("unchecked")
    private <U> U getValueForTesting(final Map<Class<?>, Object> valuesMap, final Class<U> fieldClass) {
        final Object value = valuesMap.getOrDefault(fieldClass, null);
        if (fieldClass.isPrimitive()) {
            return (U) value;
        } else {
            return fieldClass.cast(value);
        }
    }

    private <T, U> void assertFieldHasExpectedValue(final TaxDtoFieldDefinition<T, U> fieldMapping,
            final Method reflectionGetterMethod, final T dtoInstance, final U expectedValue) throws Exception {
        final Function<T, U> propertyGetter = fieldMapping.getPropertyGetter();

        final Object reflectionValue = reflectionGetterMethod.invoke(dtoInstance);
        assertEquals(expectedValue, reflectionValue, "Wrong value returned by reflection-based getter call");

        final U lambdaValue = propertyGetter.apply(dtoInstance);
        assertEquals(expectedValue, lambdaValue, "Wrong value returned by lambda-based getter call");
    }



    @ParameterizedTest
    @ValueSource(classes = {
            Object.class,
            boolean.class,
            VendorHeader.class
    })
    void testCannotCreateMappingDefinitionsForUnmappedClasses(final Class<?> unmappedClass) throws Exception {
        assertThrows(RuntimeException.class,
                () -> taxTableMetadataLookupService.getDatabaseMappingMetadataForDto(unmappedClass),
                "A mapping definition should not have been created for a class that lacks the TaxDto annotation");
    }

    @ParameterizedTest
    @ValueSource(classes = {
            NoteLite.class,
            VendorAddressLite.class
    })
    void testCannotCreateMappingDefinitionsForMissingOjbMappings(final Class<?> dtoClass) throws Exception {
        assertThrows(ClassNotPersistableException.class,
                () -> taxTableMetadataLookupService.getDatabaseMappingMetadataForDto(dtoClass),
                "A mapping definition should not have been created for a DTO class whose business object(s) "
                        + "are not included in this test's local OJB repository");
    }

}
