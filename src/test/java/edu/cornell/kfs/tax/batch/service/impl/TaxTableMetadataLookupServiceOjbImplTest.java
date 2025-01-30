package edu.cornell.kfs.tax.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.JDBCType;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.annotation.XmlAttributeMatcher;
import edu.cornell.kfs.sys.annotation.XmlDocumentFilter;
import edu.cornell.kfs.sys.annotation.XmlElementFilter;
import edu.cornell.kfs.sys.util.CreateTestDirectories;
import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.sys.util.TestSpringContextExtension;
import edu.cornell.kfs.tax.batch.dto.TestTaxObjectLite;
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
        }
)
@XmlDocumentFilter(rootElementName = "descriptor-repository", directChildElementsToKeep = {
        @XmlElementFilter(name = "class-descriptor", matchConditions = {
                @XmlAttributeMatcher(name = "table", values = {
                        "PUR_VNDR_HDR_T", "PUR_VNDR_DTL_T", "TEST_TAX_OBJECT_T"
                })
        })
})
public class TaxTableMetadataLookupServiceOjbImplTest {

    public static final String BASE_TEST_DIRECTORY = "test/tax-metdata-lookup/";
    public static final String FILTERED_OJB_XML_FILES_DIRECTORY = BASE_TEST_DIRECTORY + "ojb-files/";

    private static final String LOOKUP_SERVICE_BEAN_NAME = "taxTableMetadataLookupService";

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
                        @TaxDtoFieldDefinitionFixture(propertyName = "count", fieldClass = int.class,
                                jdbcType = JDBCType.INTEGER, columnLabel = "CNT"),
                        @TaxDtoFieldDefinitionFixture(propertyName = "active", fieldClass = boolean.class,
                                jdbcType = JDBCType.VARCHAR, columnLabel = "ACTV_IND", hasFieldConverter = true)
                }
        )
        TEST_TAX_OBJECT_LITE;
        //NOTE_LITE,
        //VENDOR_DETAIL_LITE;
    }

    // TODO: Finish implementing this unit test class!

    @Test
    void testSomething() throws Exception {
        final TaxDtoMappingDefinitionFixture fixture = FixtureUtils.getAnnotationBasedFixture(
                ExpectedMappingDefinition.TEST_TAX_OBJECT_LITE, TaxDtoMappingDefinitionFixture.class);
        final TaxDtoMappingDefinition<TestTaxObjectLite> def1 = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(TestTaxObjectLite.class);
        assertDtoMappingDefinitionHasCorrectSetup(fixture, def1);
    }

    private <T> void assertDtoMappingDefinitionHasCorrectSetup(final TaxDtoMappingDefinitionFixture fixture,
            final TaxDtoMappingDefinition<T> mappingDefinition) {
        assertEquals(fixture.dtoClass(), mappingDefinition.getDtoClass(), "Wrong DTO type");

        final Supplier<T> dtoConstructor = mappingDefinition.getDtoConstructor();
        assertNotNull(dtoConstructor, "DTO constructor lambda should have been defined");

        final T dtoInstance = dtoConstructor.get();
        assertNotNull(dtoInstance, "DTO constructor lambda should have created a new DTO");

        final T dtoInstance2 = dtoConstructor.get();
        assertNotNull(dtoInstance2, "DTO constructor lambda should have created another DTO on the 2nd run");
        assertTrue(dtoInstance != dtoInstance2, "DTO constructor lambda should not be reusing DTO instances");

        assertBusinessObjectMappingsAreCorrect(fixture, mappingDefinition);
    }

    private void assertBusinessObjectMappingsAreCorrect(final TaxDtoMappingDefinitionFixture fixture,
            final TaxDtoMappingDefinition<?> mappingDefinition) {
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

}
