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
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.springframework.jdbc.core.SqlParameterValue;

import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.sys.util.fixture.CuSqlQueryFixture;
import edu.cornell.kfs.sys.util.fixture.SqlParameterFixture;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.batch.dto.NoteLite.NoteField;
import edu.cornell.kfs.tax.batch.dto.VendorDetailLite.VendorField;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;
import edu.cornell.kfs.tax.batch.service.TaxTableMetadataLookupService;
import edu.cornell.kfs.tax.batch.service.impl.TaxTableMetadataLookupServiceDefaultImpl;
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.Criteria;
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.QuerySort;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.businessobject.TransactionDetail.TransactionDetailField;

@Execution(ExecutionMode.SAME_THREAD)
public class TaxQueryBuilderTest {

    private TaxTableMetadataLookupService metadataService;

    @BeforeEach
    void setUp() throws Exception {
        metadataService = new TaxTableMetadataLookupServiceDefaultImpl();
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

        @CuSqlQueryFixture(sql = "SELECT VEN0.VNDR_NM, VEN1.VNDR_TYP_CD FROM KFS.PUR_VNDR_DTL_T VEN0 "
                + "JOIN KFS.PUR_VNDR_HDR_T VEN1 ON VEN0.VNDR_HDR_GNRTD_ID = VEN1.VNDR_HDR_GNRTD_ID "
                + "WHERE VEN0.VNDR_HDR_GNRTD_ID IN (?, ?, ?)",
                parameters = {
                        @SqlParameterFixture(type = Types.INTEGER, value = "9753"),
                        @SqlParameterFixture(type = Types.INTEGER, value = "9754"),
                        @SqlParameterFixture(type = Types.INTEGER, value = "9755")
                })
        QUERY_WITH_JOIN_CONDITION(metadataService -> {
            return createEmptyBuilder(metadataService, VendorField.class)
                    .select(VendorField.vendorName, VendorField.vendorTypeCode)
                    .from(VendorDetail.class)
                    .join(VendorHeader.class,
                            Criteria.equal(VendorField.vendorHeaderGeneratedIdentifier_forDetail,
                                    VendorField.vendorHeaderGeneratedIdentifier_forHeader)
                    )
                    .where(
                            Criteria.in(VendorField.vendorHeaderGeneratedIdentifier_forDetail,
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
                    .select(VendorField.vendorHeaderGeneratedIdentifier_forHeader)
                    .from(VendorHeader.class)
                    .where(Criteria.equal(VendorField.vendorTaxNumber, "xxxxx1234"));

            return createEmptyBuilder(metadataService, VendorField.class)
                    .select(VendorField.vendorName)
                    .from(VendorDetail.class)
                    .where(Criteria.equal(VendorField.vendorHeaderGeneratedIdentifier_forDetail, subQuery))
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
                    .select(VendorField.vendorHeaderGeneratedIdentifier_forDetail)
                    .from(VendorDetail.class)
                    .join(null, Criteria.equal(VendorField.vendorHeaderGeneratedIdentifier_forDetail,
                            VendorField.vendorHeaderGeneratedIdentifier_forHeader));
        }),

        INVALID_JOIN_BO_CLASS_REFERENCE(metadataService -> {
            createEmptyBuilder(metadataService, VendorField.class)
                    .select(VendorField.vendorHeaderGeneratedIdentifier_forDetail)
                    .from(VendorDetail.class)
                    .join(Note.class, Criteria.equal(VendorField.vendorHeaderGeneratedIdentifier_forDetail,
                            VendorField.vendorHeaderGeneratedIdentifier_forHeader));
        }),

        NULL_JOIN_CRITERIA(metadataService -> {
            createEmptyBuilder(metadataService, VendorField.class)
                    .select(VendorField.vendorHeaderGeneratedIdentifier_forDetail)
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
