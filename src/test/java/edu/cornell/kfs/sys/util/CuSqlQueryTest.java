package edu.cornell.kfs.sys.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.jdbc.core.SqlParameterValue;

import edu.cornell.kfs.sys.CUKFSConstants;

@Execution(ExecutionMode.SAME_THREAD)
public class CuSqlQueryTest {

    private static final String SELECT_1_FROM_DUAL = "SELECT 1 FROM DUAL";
    private static final String DOC_ID_123456 = "123456";
    private static final String PRINCIPAL_ID_1357531 = "1357531";

    @SuppressWarnings({"rawtypes", "unchecked"})
    private enum InConditionBuilder {
        STRING_IN(" IN ", " OR ", true,
                (columnName, values) -> CuSqlChunk.asSqlInCondition(columnName, (Collection) values)),
        
        STRING_NOT_IN(" NOT IN ", " AND ", true,
                (columnName, values) -> CuSqlChunk.asSqlInCondition(columnName, (Collection) values)),
        
        NUMERIC_IN(" IN ", " OR ", false,
                (columnName, values) -> CuSqlChunk.asSqlInCondition(columnName, Types.INTEGER, values)),
        
        NUMERIC_NOT_IN(" NOT IN ", " AND ", false,
                (columnName, values) -> CuSqlChunk.asSqlNotInCondition(columnName, Types.INTEGER, values));
        
        private final String conditionKeywords;
        private final String subConditionCombiner;
        private final boolean forStringCollection;
        private final BiFunction<String, Collection<?>, CuSqlChunk> builderFunction;
        
        private InConditionBuilder(String conditionKeywords, String subConditionCombiner, boolean forStringCollection,
                BiFunction<String, Collection<?>, CuSqlChunk> builderFunction) {
            this.conditionKeywords = conditionKeywords;
            this.subConditionCombiner = subConditionCombiner;
            this.forStringCollection = forStringCollection;
            this.builderFunction = builderFunction;
        }
    }

    @Test
    void testBuildQueryFromSingleChunk() throws Exception {
        CuSqlQuery sqlQuery = CuSqlQuery.of(SELECT_1_FROM_DUAL);
        String expectedSql = SELECT_1_FROM_DUAL;
        assertQueryWasGeneratedCorrectly(sqlQuery, expectedSql);
    }

    @Test
    void testBuildQueryFromMultipleStringChunks() throws Exception {
        CuSqlQuery sqlQuery = CuSqlQuery.of(
                "SELECT DOC_HDR_ID ",
                "FROM KFS.KREW_DOC_HDR_T ",
                "WHERE FNL_DT IS NULL");
        String expectedSql = "SELECT DOC_HDR_ID FROM KFS.KREW_DOC_HDR_T WHERE FNL_DT IS NULL";
        assertQueryWasGeneratedCorrectly(sqlQuery, expectedSql);
    }

    @Test
    void testBuildQueryWithSingleParameter() throws Exception {
        CuSqlQuery sqlQuery = CuSqlQuery.of(
                "SELECT * FROM KFS.KREW_DOC_HDR_T WHERE DOC_HDR_STAT_CD = ",
                CuSqlChunk.forParameter(KewApiConstants.ROUTE_HEADER_ENROUTE_CD));
        String expectedSql = "SELECT * FROM KFS.KREW_DOC_HDR_T WHERE DOC_HDR_STAT_CD = ?";
        assertQueryWasGeneratedCorrectly(sqlQuery, expectedSql,
                parm(KewApiConstants.ROUTE_HEADER_ENROUTE_CD));
    }

    @Test
    void testBuildQueryWithMultipleParameters() throws Exception {
        CuSqlQuery sqlQuery = CuSqlQuery.of(
                "SELECT * FROM KFS.KREW_DOC_HDR_T ",
                "WHERE DOC_HDR_STAT_CD = ", CuSqlChunk.forParameter(KewApiConstants.ROUTE_HEADER_SAVED_CD),
                " AND VER_NBR > ", CuSqlChunk.forParameter(Types.BIGINT, 2L));
        String expectedSql = "SELECT * FROM KFS.KREW_DOC_HDR_T WHERE DOC_HDR_STAT_CD = ? AND VER_NBR > ?";
        assertQueryWasGeneratedCorrectly(sqlQuery, expectedSql,
                parm(KewApiConstants.ROUTE_HEADER_SAVED_CD), parm(Types.BIGINT, 2L));
    }

    @Test
    void testBuildQueryFromManuallyConcatenatedChunk() throws Exception {
        CuSqlQuery sqlQuery = new CuSqlChunk()
                .append("SELECT * FROM KFS.KREW_DOC_HDR_T ", "WHERE DOC_HDR_STAT_CD = ")
                .appendAsParameter(KewApiConstants.ROUTE_HEADER_SAVED_CD)
                .append(" AND VER_NBR BETWEEN ")
                .appendAsParameter(Types.BIGINT, 1L)
                .append(" AND ")
                .append(new SqlParameterValue(Types.BIGINT, 5L))
                .toQuery();
        String expectedSql = "SELECT * FROM KFS.KREW_DOC_HDR_T WHERE DOC_HDR_STAT_CD = ? AND VER_NBR BETWEEN ? AND ?";
        assertQueryWasGeneratedCorrectly(sqlQuery, expectedSql,
                parm(KewApiConstants.ROUTE_HEADER_SAVED_CD), parm(Types.BIGINT, 1L), parm(Types.BIGINT, 5L));
    }

    @Test
    void testBuildQueryWithParameterList() throws Exception {
        List<String> routeHeaderStatuses = List.of(
                KewApiConstants.ROUTE_HEADER_DISAPPROVED_CD, KewApiConstants.ROUTE_HEADER_CANCEL_CD);
        CuSqlQuery sqlQuery = CuSqlQuery.of(
                "SELECT * FROM KFS.KREW_DOC_HDR_T ",
                "WHERE DOC_HDR_STAT_CD IN (", CuSqlChunk.forStringParameters(routeHeaderStatuses), ")");
        String expectedSql = "SELECT * FROM KFS.KREW_DOC_HDR_T WHERE DOC_HDR_STAT_CD IN (?, ?)";
        assertQueryWasGeneratedCorrectly(sqlQuery, expectedSql,
                parm(KewApiConstants.ROUTE_HEADER_DISAPPROVED_CD), parm(KewApiConstants.ROUTE_HEADER_CANCEL_CD));
    }

    @Test
    void testBuildQueryWithSingleParameterPlusParameterList() throws Exception {
        List<Long> versionNumbers = List.of(3L, 4L, 5L);
        CuSqlQuery sqlQuery = CuSqlQuery.of(
                "SELECT * FROM KFS.KREW_DOC_HDR_T ",
                "WHERE VER_NBR IN (", CuSqlChunk.forParameters(Types.BIGINT, versionNumbers), ")",
                " AND DOC_HDR_STAT_CD = ", CuSqlChunk.forParameter(KewApiConstants.ROUTE_HEADER_CANCEL_CD));
        String expectedSql = "SELECT * FROM KFS.KREW_DOC_HDR_T WHERE VER_NBR IN (?, ?, ?) AND DOC_HDR_STAT_CD = ?";
        assertQueryWasGeneratedCorrectly(sqlQuery, expectedSql,
                parm(Types.BIGINT, 3L), parm(Types.BIGINT, 4L), parm(Types.BIGINT, 5L),
                parm(KewApiConstants.ROUTE_HEADER_CANCEL_CD));
    }

    @Test
    void testBuildQueryWithSingletonParameterList() throws Exception {
        List<String> documentIds = List.of(DOC_ID_123456);
        CuSqlQuery sqlQuery = CuSqlQuery.of(
                "SELECT * FROM KFS.KREW_DOC_HDR_T ",
                "WHERE DOC_HDR_ID IN (", CuSqlChunk.forStringParameters(documentIds), ")");
        String expectedSql = "SELECT * FROM KFS.KREW_DOC_HDR_T WHERE DOC_HDR_ID IN (?)";
        assertQueryWasGeneratedCorrectly(sqlQuery, expectedSql, parm(DOC_ID_123456));
    }

    @Test
    void testBuildQueryContainingOtherChunks() throws Exception {
        CuSqlChunk subQuery = CuSqlChunk.of(
                "SELECT DISTINCT DOC_HDR_ID FROM KFS.KREW_ACTN_RQST_T ",
                "WHERE ROLE_NM IS NOT NULL");
        CuSqlQuery sqlQuery = CuSqlQuery.of(
                "SELECT * FROM KFS.KREW_DOC_HDR_T ",
                "WHERE DOC_HDR_ID IN (", subQuery, ")");
        String expectedSql = "SELECT * FROM KFS.KREW_DOC_HDR_T WHERE DOC_HDR_ID IN ("
                + "SELECT DISTINCT DOC_HDR_ID FROM KFS.KREW_ACTN_RQST_T WHERE ROLE_NM IS NOT NULL)";
        assertQueryWasGeneratedCorrectly(sqlQuery, expectedSql);
    }

    @Test
    void testBuildQueryWithParameterizedSubChunks() throws Exception {
        CuSqlChunk subQuery = CuSqlChunk.of(
                "SELECT DISTINCT DOC_HDR_ID FROM KFS.KREW_ACTN_RQST_T ",
                "WHERE PRNCPL_ID = ", CuSqlChunk.forParameter(PRINCIPAL_ID_1357531));
        CuSqlQuery sqlQuery = CuSqlQuery.of(
                "SELECT * FROM KFS.KREW_DOC_HDR_T ",
                "WHERE DOC_HDR_STAT_CD = ", CuSqlChunk.forParameter(KewApiConstants.ROUTE_HEADER_FINAL_CD),
                " AND DOC_HDR_ID IN (", subQuery, ")",
                " AND VER_NBR <= ", CuSqlChunk.forParameter(Types.BIGINT, 20L));
        String expectedSql = "SELECT * FROM KFS.KREW_DOC_HDR_T WHERE DOC_HDR_STAT_CD = ? AND DOC_HDR_ID IN ("
                + "SELECT DISTINCT DOC_HDR_ID FROM KFS.KREW_ACTN_RQST_T WHERE PRNCPL_ID = ?) AND VER_NBR <= ?";
        assertQueryWasGeneratedCorrectly(sqlQuery, expectedSql,
                parm(KewApiConstants.ROUTE_HEADER_FINAL_CD), parm(PRINCIPAL_ID_1357531), parm(Types.BIGINT, 20L));
    }

    static Stream<Integer> sqlInListSizes() {
        return Stream.of(
                1,
                10,
                25,
                78,
                100,
                450,
                999,
                1000,
                1001,
                1100,
                2000,
                2050,
                4675
        );
    }

    @ParameterizedTest
    @MethodSource("sqlInListSizes")
    void testBuildQueryWithStringInCondition(int parameterCount) throws Exception {
        assertCanBuildQueryWithStringInCondition(parameterCount, InConditionBuilder.STRING_IN);
    }

    @ParameterizedTest
    @MethodSource("sqlInListSizes")
    void testBuildQueryWithStringNotInCondition(int parameterCount) throws Exception {
        assertCanBuildQueryWithStringInCondition(parameterCount, InConditionBuilder.STRING_NOT_IN);
    }

    private void assertCanBuildQueryWithStringInCondition(
            int parameterCount, InConditionBuilder inConditionBuilder) throws Exception {
        List<String> sampleRoleIds = buildSequenceOfValues(1000, parameterCount, index -> "RLE" + index);
        
        String expectedInCondition = buildExpectedInCondition("ROLE_ID", parameterCount, inConditionBuilder);
        String expectedSql = "SELECT NMSPC_CD, ROLE_NM FROM KFS.KRIM_ROLE_T WHERE " + expectedInCondition;
        SqlParameterValue[] expectedParameters = sampleRoleIds.stream()
                .map(roleId -> parm(roleId))
                .toArray(SqlParameterValue[]::new);
        
        CuSqlChunk actualInCondition = (inConditionBuilder == InConditionBuilder.STRING_IN)
                ? CuSqlChunk.asSqlInCondition("ROLE_ID", sampleRoleIds)
                : CuSqlChunk.asSqlNotInCondition("ROLE_ID", sampleRoleIds);
        
        CuSqlQuery sqlQuery = CuSqlQuery.of(
                "SELECT NMSPC_CD, ROLE_NM FROM KFS.KRIM_ROLE_T WHERE ", actualInCondition);
        
        assertQueryWasGeneratedCorrectly(sqlQuery, expectedSql, expectedParameters);
    }

    @ParameterizedTest
    @MethodSource("sqlInListSizes")
    void testBuildQueryWithNumericInCondition(int parameterCount) throws Exception {
        assertCanBuildQueryWithNumericInCondition(parameterCount, InConditionBuilder.NUMERIC_IN);
    }

    @ParameterizedTest
    @MethodSource("sqlInListSizes")
    void testBuildQueryWithNumericNotInCondition(int parameterCount) throws Exception {
        assertCanBuildQueryWithNumericInCondition(parameterCount, InConditionBuilder.NUMERIC_NOT_IN);
    }

    private void assertCanBuildQueryWithNumericInCondition(
            int parameterCount, InConditionBuilder inConditionBuilder) throws Exception {
        List<Integer> sampleVendorHeaderIds = buildSequenceOfValues(5000, parameterCount, Integer::valueOf);
        
        String expectedInCondition = buildExpectedInCondition(
                "VND.VNDR_HDR_GNRTD_ID", parameterCount, inConditionBuilder);
        String expectedSql = "SELECT * FROM KFS.PUR_VNDR_HDR_T VND WHERE " + expectedInCondition;
        SqlParameterValue[] expectedParameters = sampleVendorHeaderIds.stream()
                .map(vendorHeaderId -> parm(Types.INTEGER, vendorHeaderId))
                .toArray(SqlParameterValue[]::new);
        
        CuSqlChunk actualInCondition = inConditionBuilder == InConditionBuilder.NUMERIC_IN
                ? CuSqlChunk.asSqlInCondition("VND.VNDR_HDR_GNRTD_ID", Types.INTEGER, sampleVendorHeaderIds)
                : CuSqlChunk.asSqlNotInCondition("VND.VNDR_HDR_GNRTD_ID", Types.INTEGER, sampleVendorHeaderIds);
        
        CuSqlQuery sqlQuery = CuSqlQuery.of(
                "SELECT * FROM KFS.PUR_VNDR_HDR_T VND WHERE ", actualInCondition);
        
        assertQueryWasGeneratedCorrectly(sqlQuery, expectedSql, expectedParameters);
    }

    static Stream<Arguments> invalidInConditionCollectionTestCases() {
        return Stream.of(
                null,
                List.of(),
                Set.of()
        ).flatMap(CuSqlQueryTest::toInConditionTestCases);
    }

    private static Stream<Arguments> toInConditionTestCases(Object testValue) {
        return Arrays.stream(InConditionBuilder.values())
                .map(builder -> Arguments.of(testValue, builder));
    }

    @ParameterizedTest
    @MethodSource("invalidInConditionCollectionTestCases")
    void testCannotBuildInConditionFromNullOrEmptyCollection(
            Collection<?> collection, InConditionBuilder inConditionBuilder) throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> inConditionBuilder.builderFunction.apply("ROLE_ID", collection),
                "The in-condition method should have properly detected the invalid collection");
    }

    static Stream<Arguments> invalidInConditionColumnNameTestCases() {
        return Stream.of(
                null,
                KFSConstants.EMPTY_STRING,
                KFSConstants.BLANK_SPACE,
                KFSConstants.NEWLINE
        ).flatMap(CuSqlQueryTest::toInConditionTestCases);
    }

    @ParameterizedTest
    @MethodSource("invalidInConditionColumnNameTestCases")
    void testCannotBuildInConditionFromBlankColumnName(
            String columnName, InConditionBuilder inConditionBuilder) throws Exception {
        Collection<?> validCollection = inConditionBuilder.forStringCollection
                ? List.of("MyValue") : List.of(1);
        assertThrows(IllegalArgumentException.class,
                () -> inConditionBuilder.builderFunction.apply(columnName, validCollection),
                "The in-condition method should have properly detected the invalid column name");
    }

    static Stream<Arguments> invalidCuSqlQueryConstructorArguments() {
        return Stream.of(
                Arguments.arguments(null, null),
                Arguments.arguments(KFSConstants.EMPTY_STRING, null),
                Arguments.arguments(KFSConstants.BLANK_SPACE, null),
                Arguments.arguments(null, Stream.empty()),
                Arguments.arguments(KFSConstants.EMPTY_STRING, Stream.empty()),
                Arguments.arguments(KFSConstants.BLANK_SPACE, Stream.empty()),
                Arguments.arguments(SELECT_1_FROM_DUAL, null));
    }

    @ParameterizedTest
    @MethodSource("invalidCuSqlQueryConstructorArguments")
    void testCannotBuildQueryFromInvalidConstructorArguments(
            String queryString, Stream<SqlParameterValue> parameters) throws Exception {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> new CuSqlQuery(queryString, parameters),
                "An exception should have been thrown when constructing a CuSqlQuery with invalid arguments");
        assertTrue(exception instanceof NullPointerException || exception instanceof IllegalArgumentException,
                "The exception type should have been NullPointerException or IllegalArgumentException, but was: "
                        + exception.getClass());
    }

    static Stream<Consumer<CuSqlChunk>> sqlChunkOperationsRequiringMutableState() {
        return Stream.of(
                sqlChunk -> sqlChunk.append(" WHERE ROLE_NM IS NULL"),
                sqlChunk -> sqlChunk.append(" WHERE ROLE_NM", " IS NULL"),
                sqlChunk -> sqlChunk.append(new CuSqlChunk()),
                sqlChunk -> sqlChunk.append(new SqlParameterValue(Types.VARCHAR, DOC_ID_123456)),
                sqlChunk -> sqlChunk.appendAsParameter(DOC_ID_123456),
                sqlChunk -> sqlChunk.appendAsParameter(Types.BIGINT, 2L),
                sqlChunk -> sqlChunk.toQuery());
    }

    @ParameterizedTest
    @MethodSource("sqlChunkOperationsRequiringMutableState")
    void testCannotChangeOrReconvertSqlChunkAfterConvertingToQuery(
            Consumer<CuSqlChunk> sqlChunkOperation) throws Exception {
        CuSqlChunk sqlChunk = CuSqlChunk.of("SELECT * FROM KFS.KREW_ACTN_RQST_T");
        sqlChunk.toQuery();
        assertThrows(IllegalStateException.class, () -> sqlChunkOperation.accept(sqlChunk),
                "The SQL chunk should not be editable or re-convertible after being converted to a CuSqlQuery");
    }

    @ParameterizedTest
    @MethodSource("sqlChunkOperationsRequiringMutableState")
    void testCannotChangeOrConvertSqlChunkAfterAppendingToParentChunk(
            Consumer<CuSqlChunk> sqlChunkOperation) throws Exception {
        CuSqlChunk sqlChunk = CuSqlChunk.of(" FROM KFS.KREW_ACTN_RQST_T");
        CuSqlChunk parentChunk = CuSqlChunk.of("SELECT *");
        parentChunk.append(sqlChunk);
        assertThrows(IllegalStateException.class, () -> sqlChunkOperation.accept(sqlChunk),
                "The SQL chunk should not be editable or convertible after being appended to another chunk");
    }

    private void assertQueryWasGeneratedCorrectly(CuSqlQuery sqlQuery, String expectedSql,
            SqlParameterValue... expectedParameters) {
        String actualSql = sqlQuery.getQueryString();
        List<SqlParameterValue> actualParameters = sqlQuery.getParameters();
        Object[] actualParametersArray = sqlQuery.getParametersArray();
        assertEquals(expectedSql, actualSql, "Wrong SQL string was generated");
        assertEquals(expectedParameters.length, actualParameters.size(), "Wrong number of query parameters");
        assertEquals(expectedParameters.length, actualParametersArray.length, "Wrong number of parameters in array");
        for (int i = 0; i < expectedParameters.length; i++) {
            SqlParameterValue expectedParameter = expectedParameters[i];
            SqlParameterValue actualParameter = actualParameters.get(i);
            assertEquals(expectedParameter.getSqlType(), actualParameter.getSqlType(),
                    "Wrong SQL type for parameter at index " + i);
            assertEquals(expectedParameter.getValue(), actualParameter.getValue(),
                    "Wrong data value for parameter at index " + i);
            assertTrue(actualParameter == actualParametersArray[i], "The parameter at index " + i
                    + " was not the same instance as the corresponding one from the generated parameters array");
        }
    }

    private <T> List<T> buildSequenceOfValues(int start, int count, IntFunction<T> valueGenerator) {
        return IntStream.range(start, start + count)
                .mapToObj(valueGenerator)
                .collect(Collectors.toUnmodifiableList());
    }

    private String buildExpectedInCondition(String columnName, int count, InConditionBuilder inConditionBuilder) {
        StringBuilder condition = new StringBuilder();
        
        for (int startIndex = 0; startIndex < count; startIndex += CuSqlChunk.MAX_IN_LIST_SIZE) {
            int subSectionSize = Math.min(count - startIndex, CuSqlChunk.MAX_IN_LIST_SIZE);
            String parameterList = StringUtils.repeat(
                    KFSConstants.QUESTION_MARK, CUKFSConstants.COMMA_AND_SPACE, subSectionSize);
            String subCondition = StringUtils.join(
                    columnName, inConditionBuilder.conditionKeywords,
                    CUKFSConstants.LEFT_PARENTHESIS, parameterList, CUKFSConstants.RIGHT_PARENTHESIS);
            
            if (startIndex > 0) {
                condition.append(inConditionBuilder.subConditionCombiner);
            }
            condition.append(subCondition);
        }
        
        if (count > CuSqlChunk.MAX_IN_LIST_SIZE) {
            condition.insert(0, CUKFSConstants.LEFT_PARENTHESIS)
                    .append(CUKFSConstants.RIGHT_PARENTHESIS);
        }
        
        return condition.toString();
    }

    private SqlParameterValue parm(String value) {
        return parm(Types.VARCHAR, value);
    }

    private SqlParameterValue parm(int sqlType, Object value) {
        return new SqlParameterValue(sqlType, value);
    }

}
