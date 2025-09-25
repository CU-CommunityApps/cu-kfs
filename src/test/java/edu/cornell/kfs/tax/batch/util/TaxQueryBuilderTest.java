package edu.cornell.kfs.tax.batch.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.springframework.jdbc.core.SqlParameterValue;

import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.sys.util.TestSpringContextExtension;
import edu.cornell.kfs.sys.util.fixture.CuSqlQueryFixture;
import edu.cornell.kfs.sys.util.fixture.SqlParameterFixture;
import edu.cornell.kfs.tax.CuTaxTestConstants.TaxSpringBeans;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.batch.dto.NoteLite.NoteField;
import edu.cornell.kfs.tax.batch.dto.PrncSourceData.PrncSourceDataField;
import edu.cornell.kfs.tax.batch.dto.VendorDetailLite.VendorField;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;
import edu.cornell.kfs.tax.batch.service.TaxTableMetadataLookupService;
import edu.cornell.kfs.tax.batch.service.impl.TaxTableMetadataLookupServiceOjbImpl;
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.Criteria;
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.QuerySort;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.businessobject.TransactionDetail.TransactionDetailField;

@Execution(ExecutionMode.SAME_THREAD)
public class TaxQueryBuilderTest {

    @RegisterExtension
    static TestSpringContextExtension springContextExtension = TestSpringContextExtension.forClassPathSpringXmlFile(
            "edu/cornell/kfs/tax/batch/cu-spring-tax-query-builder-test.xml");

    private TaxTableMetadataLookupService metadataService;

    @BeforeEach
    void setUp() throws Exception {
        metadataService = springContextExtension.getBean(
                TaxSpringBeans.TAX_TABLE_METADATA_LOOKUP_SERVICE, TaxTableMetadataLookupServiceOjbImpl.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        metadataService = null;
    }



    enum LocalTestCase {

        @CuSqlQueryFixture(sql = "SELECT NOT0.NTE_ID FROM KFS.KRNS_NTE_T NOT0", parameters = {})
        SIMPLE_QUERY_SELECT_ONE_FIELD(metadataService -> {
            return createEmptyBuilder(metadataService, NoteField.class)
                    .select(NoteField.noteIdentifier)
                    .from(Note.class)
                    .build();
        }),

        @CuSqlQueryFixture(sql = "SELECT NOT0.NTE_ID, NOT0.TXT FROM KFS.KRNS_NTE_T NOT0", parameters = {})
        SIMPLE_QUERY_SELECT_MULTIPLE_FIELDS(metadataService -> {
            return createEmptyBuilder(metadataService, NoteField.class)
                    .select(NoteField.noteIdentifier, NoteField.noteText)
                    .from(Note.class)
                    .build();
        }),

        @CuSqlQueryFixture(sql = "SELECT NOT0.NTE_ID, NOT0.RMT_OBJ_ID, NOT0.TXT FROM KFS.KRNS_NTE_T NOT0",
                parameters = {})
        SIMPLE_QUERY_SELECT_ALL_FIELDS(metadataService -> {
            return createEmptyBuilder(metadataService, NoteField.class)
                    .selectAllMappedFields()
                    .from(Note.class)
                    .build();
        }),

        @CuSqlQueryFixture(sql = "SELECT NOT0.NTE_ID FROM KFS.KRNS_NTE_T NOT0 WHERE NOT0.TXT = ?",
                parameters = { @SqlParameterFixture("Some Test Value") })
        SIMPLE_QUERY_SINGLE_CRITERION(metadataService -> {
            return createEmptyBuilder(metadataService, NoteField.class)
                    .select(NoteField.noteIdentifier)
                    .from(Note.class)
                    .where(Criteria.equal(NoteField.noteText, "Some Test Value"))
                    .build();
        }),

        @CuSqlQueryFixture(sql = "SELECT NOT0.NTE_ID FROM KFS.KRNS_NTE_T NOT0 WHERE NOT0.TXT = ? AND NOT0.NTE_ID <> ?",
                parameters = {
                        @SqlParameterFixture("Some Test Value"),
                        @SqlParameterFixture("12345")
                })
        SIMPLE_QUERY_MULTIPLE_CRITERIA(metadataService -> {
            return createEmptyBuilder(metadataService, NoteField.class)
                    .select(NoteField.noteIdentifier)
                    .from(Note.class)
                    .where(
                            Criteria.equal(NoteField.noteText, "Some Test Value"),
                            Criteria.notEqual(NoteField.noteIdentifier, "12345")
                    )
                    .build();
        }),

        @CuSqlQueryFixture(sql = "SELECT VEN0.VNDR_HDR_GNRTD_ID \"VEN0_VNDR_HDR_GNRTD_ID\", "
                + "VEN0.VNDR_NM, VEN1.VNDR_TYP_CD "
                + "FROM KFS.PUR_VNDR_DTL_T VEN0 "
                + "JOIN KFS.PUR_VNDR_HDR_T VEN1 ON VEN0.VNDR_HDR_GNRTD_ID = VEN1.VNDR_HDR_GNRTD_ID "
                + "WHERE VEN0.VNDR_HDR_GNRTD_ID IN (?, ?, ?)",
                parameters = {
                        @SqlParameterFixture(type = Types.INTEGER, value = "9753"),
                        @SqlParameterFixture(type = Types.INTEGER, value = "9754"),
                        @SqlParameterFixture(type = Types.INTEGER, value = "9755")
                })
        QUERY_WITH_JOIN_CONDITION(metadataService -> {
            return createEmptyBuilder(metadataService, VendorField.class)
                    .select(VendorField.vendorDetailVendorHeaderGeneratedIdentifier, VendorField.vendorName,
                            VendorField.vendorTypeCode)
                    .from(VendorDetail.class)
                    .join(VendorHeader.class,
                            Criteria.equal(VendorField.vendorDetailVendorHeaderGeneratedIdentifier,
                                    VendorField.vendorHeaderGeneratedIdentifier)
                    )
                    .where(
                            Criteria.in(VendorField.vendorDetailVendorHeaderGeneratedIdentifier,
                                    Types.INTEGER, List.of(9753, 9754, 9755))
                    )
                    .build();
        }),

        @CuSqlQueryFixture(sql = "SELECT VEN0.VNDR_NM FROM KFS.PUR_VNDR_DTL_T VEN0 "
                + "WHERE VEN0.VNDR_HDR_GNRTD_ID = ("
                        + "SELECT VEN1.VNDR_HDR_GNRTD_ID FROM KFS.PUR_VNDR_HDR_T VEN1 "
                        + "WHERE VEN1.VNDR_US_TAX_NBR = ?"
                + ")",
                parameters = {
                        @SqlParameterFixture(type = Types.VARCHAR, value = "xxxxx1234")
                })
        QUERY_WITH_SUBQUERY(metadataService -> {
            final TaxQueryBuilder subQuery = createEmptyBuilder(metadataService, VendorField.class)
                    .select(VendorField.vendorHeaderGeneratedIdentifier)
                    .from(VendorHeader.class)
                    .where(Criteria.equal(VendorField.vendorTaxNumber, "xxxxx1234"));

            return createEmptyBuilder(metadataService, VendorField.class)
                    .select(VendorField.vendorName)
                    .from(VendorDetail.class)
                    .where(Criteria.equal(VendorField.vendorDetailVendorHeaderGeneratedIdentifier, subQuery))
                    .build();
        }),

        @CuSqlQueryFixture(sql = "SELECT CUP0.FDOC_NBR \"CUP0_FDOC_NBR\", VEN4.VNDR_US_TAX_NBR "
                + "FROM KFS.AP_PMT_RQST_T CUP0 "
                + "JOIN ("
                        + "SELECT VEN6.VNDR_HDR_GNRTD_ID, VEN6.VNDR_US_TAX_NBR "
                        + "FROM KFS.PUR_VNDR_HDR_T VEN6 "
                        + "JOIN KFS.PUR_VNDR_DTL_T VEN5 ON VEN6.VNDR_HDR_GNRTD_ID = VEN5.VNDR_HDR_GNRTD_ID "
                        + "WHERE VEN5.VNDR_DTL_ASND_ID = ?"
                + ") VEN4 ON CUP0.VNDR_HDR_GNRTD_ID = VEN4.VNDR_HDR_GNRTD_ID "
                + "WHERE CUP0.FDOC_NBR <> ?",
                parameters = {
                        @SqlParameterFixture(type = Types.INTEGER, value = "0"),
                        @SqlParameterFixture(type = Types.VARCHAR, value = "22446688")
                })
        QUERY_WITH_SUBQUERY_JOIN(metadataService -> {
            final TaxDtoDbMetadata metadata = metadataService.getDatabaseMappingMetadataForDto(PrncSourceDataField.class);
            final TaxDtoDbMetadata subQueryMetadata = metadataService.getDatabaseMappingMetadataForDto(
                    VendorField.class, metadata.getMaximumAliasSuffix() + 1);

            final TaxQueryBuilder vendorSubQuery = new TaxQueryBuilder(subQueryMetadata)
                    .select(VendorField.vendorHeaderGeneratedIdentifier, VendorField.vendorTaxNumber)
                    .from(VendorHeader.class)
                    .join(VendorDetail.class,
                            Criteria.equal(VendorField.vendorHeaderGeneratedIdentifier,
                                    VendorField.vendorDetailVendorHeaderGeneratedIdentifier))
                    .where(
                            Criteria.equal(VendorField.vendorDetailAssignedIdentifier, Types.INTEGER, 0)
                    );
            
            return new TaxQueryBuilder(metadata)
                    .select(PrncSourceDataField.preqDocumentNumber, PrncSourceDataField.vendorTaxNumber)
                    .from(CuPaymentRequestDocument.class)
                    .join(vendorSubQuery, VendorHeader.class,
                            Criteria.equal(PrncSourceDataField.preqVendorHeaderGeneratedIdentifier,
                                    PrncSourceDataField.vendorHeaderGeneratedIdentifier))
                    .where(
                            Criteria.notEqual(PrncSourceDataField.preqDocumentNumber, "22446688")
                    )
                    .build();
        }),

        @CuSqlQueryFixture(sql = "SELECT VEN0.VNDR_HDR_GNRTD_ID \"VEN0_VNDR_HDR_GNRTD_ID\", "
                + "VEN0.VNDR_DTL_ASND_ID, VEN0.VNDR_PARENT_IND, VEN0.VNDR_1ST_LST_NM_IND, VEN0.VNDR_NM "
                + "FROM KFS.PUR_VNDR_DTL_T VEN0 "
                + "JOIN KFS.PUR_VNDR_HDR_T VEN1 ON VEN0.VNDR_HDR_GNRTD_ID = VEN1.VNDR_HDR_GNRTD_ID "
                + "WHERE VEN1.VNDR_FRGN_IND = ?",
                parameters = {
                        @SqlParameterFixture(type = Types.VARCHAR, value = KRADConstants.YES_INDICATOR_VALUE)
                })
        QUERY_WITH_SINGLE_BO_SELECT(metadataService -> {
            return createEmptyBuilder(metadataService, VendorField.class)
                    .selectAllFieldsMappedTo(VendorDetail.class)
                    .from(VendorDetail.class)
                    .join(VendorHeader.class,
                            Criteria.equal(VendorField.vendorDetailVendorHeaderGeneratedIdentifier,
                                    VendorField.vendorHeaderGeneratedIdentifier)
                    )
                    .where(
                            Criteria.equal(VendorField.vendorForeignIndicator, KRADConstants.YES_INDICATOR_VALUE)
                    )
                    .build();
        }),

        @CuSqlQueryFixture(sql = "SELECT VEN1.VNDR_HDR_GNRTD_ID, VEN0.VNDR_DTL_ASND_ID, VEN0.VNDR_NM "
                + "FROM KFS.PUR_VNDR_HDR_T VEN1 "
                + "LEFT JOIN KFS.PUR_VNDR_DTL_T VEN0 "
                        + "ON VEN1.VNDR_HDR_GNRTD_ID = VEN0.VNDR_HDR_GNRTD_ID AND VEN0.VNDR_DTL_ASND_ID = ? "
                + "WHERE VEN1.VNDR_FRGN_IND = ?",
                parameters = {
                        @SqlParameterFixture(type = Types.INTEGER, value = "1"),
                        @SqlParameterFixture(type = Types.VARCHAR, value = KRADConstants.YES_INDICATOR_VALUE)
                })
        QUERY_WITH_LEFT_JOIN(metadataService -> {
            return createEmptyBuilder(metadataService, VendorField.class)
                    .select(VendorField.vendorHeaderGeneratedIdentifier,
                            VendorField.vendorDetailAssignedIdentifier, VendorField.vendorName)
                    .from(VendorHeader.class)
                    .leftJoin(VendorDetail.class,
                            Criteria.equal(VendorField.vendorHeaderGeneratedIdentifier,
                                    VendorField.vendorDetailVendorHeaderGeneratedIdentifier),
                            Criteria.equal(VendorField.vendorDetailAssignedIdentifier, Types.INTEGER, 1)
                    )
                    .where(
                            Criteria.equal(VendorField.vendorForeignIndicator, KRADConstants.YES_INDICATOR_VALUE)
                    )
                    .build();
        }),

        /*
         * NOTE: Despite the alias mismatch on the vendor header ID selections,
         *       this SQL should still be acceptable because the first set's aliases
         *       will override those from the second set.
         */
        @CuSqlQueryFixture(sql = "SELECT VEN0.VNDR_HDR_GNRTD_ID \"VEN0_VNDR_HDR_GNRTD_ID\", "
                + "VEN0.VNDR_DTL_ASND_ID, VEN0.VNDR_NM "
                + "FROM KFS.PUR_VNDR_DTL_T VEN0 "
                + "WHERE VEN0.VNDR_NM LIKE ? "
                + "UNION ALL "
                + "SELECT VEN2.VNDR_HDR_GNRTD_ID \"VEN2_VNDR_HDR_GNRTD_ID\", VEN2.VNDR_DTL_ASND_ID, VEN2.VNDR_NM "
                + "FROM KFS.PUR_VNDR_DTL_T VEN2 "
                + "WHERE VEN2.VNDR_HDR_GNRTD_ID BETWEEN ? AND ?",
                parameters = {
                        @SqlParameterFixture(type = Types.VARCHAR, value = "Joe's %"),
                        @SqlParameterFixture(type = Types.INTEGER, value = "13579"),
                        @SqlParameterFixture(type = Types.INTEGER, value = "13679")
                })
        QUERY_WITH_UNION(metadataService -> {
            final TaxDtoDbMetadata metadata = metadataService.getDatabaseMappingMetadataForDto(VendorField.class);
            final TaxDtoDbMetadata unionMetadata = metadataService.getDatabaseMappingMetadataForDto(
                    VendorField.class, metadata.getMaximumAliasSuffix() + 1);

            return new TaxQueryBuilder(metadata)
                    .select(VendorField.vendorDetailVendorHeaderGeneratedIdentifier,
                            VendorField.vendorDetailAssignedIdentifier, VendorField.vendorName)
                    .from(VendorDetail.class)
                    .where(
                            Criteria.like(VendorField.vendorName, "Joe's %")
                    )
                    .unionAll(new TaxQueryBuilder(unionMetadata)
                            .select(VendorField.vendorDetailVendorHeaderGeneratedIdentifier,
                                    VendorField.vendorDetailAssignedIdentifier, VendorField.vendorName)
                            .from(VendorDetail.class)
                            .where(
                                    Criteria.between(VendorField.vendorDetailVendorHeaderGeneratedIdentifier,
                                            Types.INTEGER, 13579, 13679)
                            )
                    )
                    .build();
        }),

        @CuSqlQueryFixture(sql = "SELECT TRA0.IRS_1099_1042S_DETAIL_ID FROM KFS.TX_TRANSACTION_DETAIL_T TRA0 "
                + "WHERE TRA0.VNDR_FRGN_LN2_ADDR IS NULL AND ("
                        + "TRA0.NET_PMT_AMT = ? OR ("
                                + "TRA0.VNDR_LN2_ADDR IS NOT NULL AND TRA0.DV_CHK_STUB_TXT <> ?"
                        + ")"
                + ") "
                + "ORDER BY TRA0.FDOC_NBR ASC, TRA0.KFS_CHART DESC, TRA0.VNDR_LN2_ADDR ASC NULLS FIRST",
                parameters = {
                        @SqlParameterFixture(type = Types.DECIMAL, value = "799.95"),
                        @SqlParameterFixture(type = Types.VARCHAR, value = "Petty Cash")
                })
        QUERY_WITH_COMPLEX_CRITERIA(metadataService -> {
            return createEmptyBuilder(metadataService, TransactionDetailField.class)
                    .select(TransactionDetailField.transactionDetailId)
                    .from(TransactionDetail.class)
                    .where(
                            Criteria.isNull(TransactionDetailField.vendorForeignLine2Address),
                            Criteria.or(
                                    Criteria.equal(TransactionDetailField.netPaymentAmount,
                                            Types.DECIMAL, new BigDecimal("799.95")),
                                    Criteria.and(
                                            Criteria.isNotNull(TransactionDetailField.vendorLine2Address),
                                            Criteria.notEqual(TransactionDetailField.dvCheckStubText, "Petty Cash")
                                    )
                            )
                    )
                    .orderBy(
                            QuerySort.ascending(TransactionDetailField.documentNumber),
                            QuerySort.descending(TransactionDetailField.chartCode),
                            QuerySort.ascendingNullsFirst(TransactionDetailField.vendorLine2Address)
                    )
                    .build();
        });



        private final Function<TaxTableMetadataLookupService, CuSqlQuery> queryBuildingTask;

        private LocalTestCase(final Function<TaxTableMetadataLookupService, CuSqlQuery> queryBuildingTask) {
            this.queryBuildingTask = queryBuildingTask;
        }

        private CuSqlQueryFixture getExpectedQuery() {
            return FixtureUtils.getAnnotationBasedFixture(this, CuSqlQueryFixture.class);
        }
    }

    private static TaxQueryBuilder createEmptyBuilder(final TaxTableMetadataLookupService metadataService,
            final Class<? extends TaxDtoFieldEnum> fieldEnumClass) {
        final TaxDtoDbMetadata metadata = metadataService.getDatabaseMappingMetadataForDto(fieldEnumClass);
        return new TaxQueryBuilder(metadata);
    }

    @ParameterizedTest
    @EnumSource(LocalTestCase.class)
    void testBuildTaxQuery(final LocalTestCase testCase) throws Exception {
        final CuSqlQueryFixture expectedQuery = testCase.getExpectedQuery();
        final CuSqlQuery actualQuery = testCase.queryBuildingTask.apply(metadataService);
        assertQueryWasCreatedProperly(expectedQuery, actualQuery);
    }

    private void assertQueryWasCreatedProperly(final CuSqlQueryFixture expectedQuery, final CuSqlQuery actualQuery) {
        assertEquals(expectedQuery.sql(), actualQuery.getQueryString(), "Wrong SQL string");

        final SqlParameterFixture[] expectedParams = expectedQuery.parameters();
        List<SqlParameterValue> actualParams = actualQuery.getParameters();
        assertEquals(expectedParams.length, actualParams.size(), "Wrong query parameter count");

        for (int i = 0; i < expectedParams.length; i++) {
            final SqlParameterFixture expectedParam = expectedParams[i];
            final SqlParameterValue actualParam = actualParams.get(i);
            final Object expectedValue = SqlParameterFixture.Utils.getParsedValue(expectedParam);
            final Object actualValue = actualParam.getValue();
            assertEquals(expectedParam.type(), actualParam.getSqlType(), "Wrong query parameter type");
            assertEquals(expectedValue, actualValue, "Wrong query parameter value");
        }
    }



    enum FailureTestCase {

        NULL_METADATA(metadataService -> {
            new TaxQueryBuilder(null);
        }),

        EMPTY_QUERY(metadataService -> {
            createEmptyBuilder(metadataService, NoteField.class).build();
        }),

        NULL_FIELD_LIST(metadataService -> {
            createEmptyBuilder(metadataService, NoteField.class)
                    .select((TaxDtoFieldEnum[]) null);
        }),

        INVALID_FIELD_REFERENCE(metadataService -> {
            createEmptyBuilder(metadataService, NoteField.class)
                    .select(VendorField.vendorName);
        }),

        NULL_BO_CLASS_REFERENCE(metadataService -> {
            createEmptyBuilder(metadataService, NoteField.class)
                    .select(NoteField.noteIdentifier)
                    .from(null);
        }),

        INVALID_BO_CLASS_REFERENCE(metadataService -> {
            createEmptyBuilder(metadataService, NoteField.class)
                    .select(NoteField.noteIdentifier)
                    .from(VendorHeader.class);
        }),

        NULL_JOIN_BO_CLASS_REFERENCE(metadataService -> {
            createEmptyBuilder(metadataService, VendorField.class)
                    .select(VendorField.vendorDetailVendorHeaderGeneratedIdentifier)
                    .from(VendorDetail.class)
                    .join(null, Criteria.equal(VendorField.vendorDetailVendorHeaderGeneratedIdentifier,
                            VendorField.vendorHeaderGeneratedIdentifier));
        }),

        INVALID_JOIN_BO_CLASS_REFERENCE(metadataService -> {
            createEmptyBuilder(metadataService, VendorField.class)
                    .select(VendorField.vendorDetailVendorHeaderGeneratedIdentifier)
                    .from(VendorDetail.class)
                    .join(Note.class, Criteria.equal(VendorField.vendorDetailVendorHeaderGeneratedIdentifier,
                            VendorField.vendorHeaderGeneratedIdentifier));
        }),

        NULL_JOIN_CRITERIA(metadataService -> {
            createEmptyBuilder(metadataService, VendorField.class)
                    .select(VendorField.vendorDetailVendorHeaderGeneratedIdentifier)
                    .from(VendorDetail.class)
                    .join(VendorHeader.class, (Criteria[]) null);
        }),

        NULL_WHERE_CRITERIA(metadataService -> {
            createEmptyBuilder(metadataService, NoteField.class)
                    .select(NoteField.noteIdentifier)
                    .from(Note.class)
                    .where((Criteria[]) null);
        }),

        NULL_CRITERIA_FIELD_REFERENCE(metadataService -> {
            createEmptyBuilder(metadataService, NoteField.class)
                    .select(NoteField.noteIdentifier)
                    .from(Note.class)
                    .where(Criteria.equal(null, "My Message"));
        }),

        INVALID_CRITERIA_FIELD_REFERENCE(metadataService -> {
            createEmptyBuilder(metadataService, NoteField.class)
                    .select(NoteField.noteIdentifier)
                    .from(Note.class)
                    .where(Criteria.equal(VendorField.vendorName, "My Message"));
        });

        private final Consumer<TaxTableMetadataLookupService> failableQueryBuildingTask;

        private FailureTestCase(final Consumer<TaxTableMetadataLookupService> failableQueryBuildingTask) {
            this.failableQueryBuildingTask = failableQueryBuildingTask;
        }

    }

    @ParameterizedTest
    @EnumSource(FailureTestCase.class)
    void testFailureOfInvalidQueryBuildingTask(final FailureTestCase testCase) throws Exception {
        assertThrows(RuntimeException.class, () -> testCase.failableQueryBuildingTask.accept(metadataService),
                "The operation containing an invalid query-building task should have thrown an exception");
    }

}
