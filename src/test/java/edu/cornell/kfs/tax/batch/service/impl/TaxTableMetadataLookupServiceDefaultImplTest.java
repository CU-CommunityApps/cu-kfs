package edu.cornell.kfs.tax.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.batch.dto.NoteLite.NoteField;
import edu.cornell.kfs.tax.batch.dto.VendorDetailLite.VendorField;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;
import edu.cornell.kfs.tax.batch.metadata.fixture.TaxDtoDbMetadataFixture;
import edu.cornell.kfs.tax.batch.metadata.fixture.TaxFieldFixture;
import edu.cornell.kfs.tax.batch.metadata.fixture.TaxTableFixture;

/*
 * NOTE: This test and the related test for the OJB implementation are nearly identical.
 * If we decide to remove the "DefaultImpl" variant, then this test should be removed.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class TaxTableMetadataLookupServiceDefaultImplTest {

    private TaxTableMetadataLookupServiceDefaultImpl metadataService;

    @BeforeEach
    void setUp() throws Exception {
        metadataService = new TaxTableMetadataLookupServiceDefaultImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        metadataService = null;
    }



    enum LocalTestCase {

        @TaxDtoDbMetadataFixture(
                fieldEnumClass = NoteField.class,
                mappedTables = {
                        @TaxTableFixture(businessObjectClass = Note.class,
                                tableName = "KFS.KRNS_NTE_T", tableAlias = "NOT0")
                },
                mappedFields = {
                        @TaxFieldFixture(key = "noteIdentifier", column = "NOT0.NTE_ID", alias = "NTE_ID"),
                        @TaxFieldFixture(key = "remoteObjectIdentifier", column = "NOT0.RMT_OBJ_ID",
                                alias = "RMT_OBJ_ID"),
                        @TaxFieldFixture(key = "noteText", column = "NOT0.TXT", alias = "TXT")
                }
        )
        NOTE_METADATA,

        @TaxDtoDbMetadataFixture(
                fieldEnumClass = VendorField.class,
                mappedTables = {
                        @TaxTableFixture(businessObjectClass = VendorDetail.class,
                                tableName = "KFS.PUR_VNDR_DTL_T", tableAlias = "VEN0"),
                        @TaxTableFixture(businessObjectClass = VendorHeader.class,
                                tableName = "KFS.PUR_VNDR_HDR_T", tableAlias = "VEN1")
                },
                mappedFields = {
                        @TaxFieldFixture(key = "vendorHeaderGeneratedIdentifier",
                                column = "VEN1.VNDR_HDR_GNRTD_ID", alias = "VNDR_HDR_GNRTD_ID"),
                        @TaxFieldFixture(key = "vendorDetailVendorHeaderGeneratedIdentifier",
                                column = "VEN0.VNDR_HDR_GNRTD_ID", alias = "VEN0_VNDR_HDR_GNRTD_ID"),
                        @TaxFieldFixture(key = "vendorDetailAssignedIdentifier", column = "VEN0.VNDR_DTL_ASND_ID",
                                alias = "VNDR_DTL_ASND_ID"),
                        @TaxFieldFixture(key = "vendorParentIndicator", column = "VEN0.VNDR_PARENT_IND",
                                alias = "VNDR_PARENT_IND"),
                        @TaxFieldFixture(key = "vendorFirstLastNameIndicator", column = "VEN0.VNDR_1ST_LST_NM_IND",
                                alias = "VNDR_1ST_LST_NM_IND"),
                        @TaxFieldFixture(key = "vendorName", column = "VEN0.VNDR_NM", alias = "VNDR_NM"),
                        @TaxFieldFixture(key = "vendorTaxNumber", column = "VEN1.VNDR_US_TAX_NBR",
                                alias = "VNDR_US_TAX_NBR"),
                        @TaxFieldFixture(key = "vendorTypeCode", column = "VEN1.VNDR_TYP_CD", alias = "VNDR_TYP_CD"),
                        @TaxFieldFixture(key = "vendorOwnershipCode", column = "VEN1.VNDR_OWNR_CD",
                                alias = "VNDR_OWNR_CD"),
                        @TaxFieldFixture(key = "vendorOwnershipCategoryCode", column = "VEN1.VNDR_OWNR_CTGRY_CD",
                                alias = "VNDR_OWNR_CTGRY_CD"),
                        @TaxFieldFixture(key = "vendorForeignIndicator", column = "VEN1.VNDR_FRGN_IND",
                                alias = "VNDR_FRGN_IND"),
                        @TaxFieldFixture(key = "vendorGIIN", column = "VEN1.VNDR_GIIN", alias = "VNDR_GIIN"),
                        @TaxFieldFixture(key = "vendorChapter4StatusCode", column = "VEN1.VNDR_CHAP_4_STAT_CD",
                                alias = "VNDR_CHAP_4_STAT_CD")
                }
        )
        VENDOR_METADATA;

    }



    @ParameterizedTest
    @EnumSource(LocalTestCase.class)
    void testReadMetadata(final LocalTestCase testCase) throws Exception {
        final TaxDtoDbMetadataFixture fixture = FixtureUtils.getAnnotationBasedFixture(
                testCase, TaxDtoDbMetadataFixture.class);
        final TaxDtoDbMetadata metadata = metadataService.getDatabaseMappingMetadataForDto(
                fixture.fieldEnumClass());
        assertMetadataIsCorrect(fixture, metadata);
    }

    private void assertMetadataIsCorrect(final TaxDtoDbMetadataFixture fixture, final TaxDtoDbMetadata metadata) {
        assertNotNull(metadata, "The metadata object returned by the service should not have been null");
        assertEquals(fixture.mappedTables().length, metadata.getMappedTableCount(), "Wrong table mapping count");
        assertEquals(fixture.mappedFields().length, metadata.getMappedColumnCount(), "Wrong column mapping count");

        for (final TaxTableFixture tableFixture : fixture.mappedTables()) {
            final Class<? extends BusinessObject> boClass = tableFixture.businessObjectClass();
            final String actualTableName = metadata.getQualifiedTableName(boClass);
            final String actualTableAlias = metadata.getTableAlias(boClass);
            assertEquals(tableFixture.tableName(), actualTableName, "Wrong table name for " + boClass.getName());
            assertEquals(tableFixture.tableAlias(), actualTableAlias, "Wrong table alias for " + boClass.getName());
        }

        final TaxDtoFieldEnum[] enumConstants = fixture.fieldEnumClass().getEnumConstants();
        assertNotNull(enumConstants, "The fixture's field enum class should have represented an enum");

        for (int i = 0; i < enumConstants.length; i++) {
            final TaxDtoFieldEnum fieldKey = enumConstants[i];
            final TaxFieldFixture fieldFixture = fixture.mappedFields()[i];
            assertEquals(fieldKey.name(), fieldFixture.key(), "Wrong ordering of expected fields within the fixture; "
                    + "they should have matched the declaration order of the related enum");

            final String actualColumnLabel = metadata.getFullColumnLabel(fieldKey);
            assertEquals(fieldFixture.column(), actualColumnLabel, "Wrong column label");

            final String actualColumnAlias = metadata.getColumnAlias(fieldKey);
            assertEquals(fieldFixture.alias(), actualColumnAlias, "Wrong column alias");
        }
    }

}
