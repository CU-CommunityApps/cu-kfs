package edu.cornell.kfs.tax.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.coreservice.api.parameter.EvaluationOperator;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.coreservice.impl.parameter.Parameter;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.Tax1042SParameterNames;
import edu.cornell.kfs.tax.util.TaxParameterUtils;

@Execution(ExecutionMode.SAME_THREAD)
public class TaxParameterServiceImplTest {

    private static final String NON_EXISTENT_PARM = "NON_EXISTENT_PARM";

    private ParameterService parameterService;
    private TaxParameterServiceImpl taxParameterService;

    @BeforeEach
    void setUp() throws Exception {
        parameterService = TaxParameterUtils.createUpdatableMockParameterServiceForTaxProcessing();
        taxParameterService = new TaxParameterServiceImpl();
        taxParameterService.setParameterService(parameterService);
    }

    @AfterEach
    void tearDown() throws Exception {
        taxParameterService = null;
        parameterService = null;
    }



    static Stream<Arguments> singleValueParameters() {
        return Stream.of(
                parms(parmFor1042S(Tax1042SParameterNames.STATE_NAME, "NY")),
                parms(
                        parmFor1042S(Tax1042SParameterNames.STATE_NAME, "NY"),
                        parmFor1042S(Tax1042SParameterNames.NON_REPORTABLE_INCOME_CODE, "XX")
                ),
                parms(parmFor1042S(Tax1042SParameterNames.STATE_NAME, "NY", EvaluationOperator.DISALLOW)),
                parms(parmFor1042S(Tax1042SParameterNames.STATE_NAME, null)),
                parms(parmFor1042S(Tax1042SParameterNames.STATE_NAME, KFSConstants.EMPTY_STRING)),
                parms(parmFor1042S(Tax1042SParameterNames.STATE_NAME, KFSConstants.BLANK_SPACE))
        ).map(Arguments::of);
    }

    static Stream<Arguments> simpleMultiValueParameters() {
        return Stream.of(
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.PDP_EXCLUDED_DOC_TYPES, "APQQ"),
                        List.of("APQQ")
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.PDP_EXCLUDED_DOC_TYPES, "APQQ;APXZ;APAP"),
                        List.of("APQQ", "APXZ", "APAP")
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.PDP_EXCLUDED_DOC_TYPES, null),
                        List.of()
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.PDP_EXCLUDED_DOC_TYPES, KFSConstants.EMPTY_STRING),
                        List.of()
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.PDP_EXCLUDED_DOC_TYPES, KFSConstants.BLANK_SPACE),
                        List.of()
                )
        );
    }

    static Stream<Arguments> multiValueParametersWithUniqueSubParameters() {
        return Stream.of(
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.CHAPTER3_EXEMPTION_CODES, "NotExempt=00"),
                        Map.of("NotExempt", "00")
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.CHAPTER3_EXEMPTION_CODES,
                                "NotExempt=00;TaxTreaty=01;ForeignSource=02"),
                        Map.ofEntries(
                                Map.entry("NotExempt", "00"),
                                Map.entry("TaxTreaty", "01"),
                                Map.entry("ForeignSource", "02")
                        )
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.CHAPTER3_EXEMPTION_CODES, null),
                        Map.of()
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.CHAPTER3_EXEMPTION_CODES, KFSConstants.EMPTY_STRING),
                        Map.of()
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.CHAPTER3_EXEMPTION_CODES, KFSConstants.BLANK_SPACE),
                        Map.of()
                )
        );
    }

    static Stream<Arguments> multiValueParametersWithMultiValueSubParametersAndInverseMapping() {
        return Stream.of(
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.INCOME_CLASS_CODE_VALID_OBJECT_CODES, "A=1234"),
                        Map.of("1234", Set.of("A"))
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.INCOME_CLASS_CODE_VALID_OBJECT_CODES,
                                "A=1234,5555,6767;B=4324;C=0506,1234"),
                        Map.ofEntries(
                                Map.entry("1234", Set.of("A", "C")),
                                Map.entry("5555", Set.of("A")),
                                Map.entry("6767", Set.of("A")),
                                Map.entry("4324", Set.of("B")),
                                Map.entry("0506", Set.of("C"))
                        )
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.INCOME_CLASS_CODE_VALID_OBJECT_CODES, null),
                        Map.of()
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.INCOME_CLASS_CODE_VALID_OBJECT_CODES,
                                KFSConstants.EMPTY_STRING),
                        Map.of()
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.INCOME_CLASS_CODE_VALID_OBJECT_CODES,
                                KFSConstants.BLANK_SPACE),
                        Map.of()
                )
        );
    }

    static Stream<Arguments> multiValueParametersWithDuplicateSubParameterKeys() {
        return Stream.of(
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.ROYALTIES_INCLUDED_OBJECT_CODE_CHART_ACCOUNT,
                                "3333=IT-1234567"),
                        Map.of("3333", Set.of("IT-1234567"))
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.ROYALTIES_INCLUDED_OBJECT_CODE_CHART_ACCOUNT,
                                "3333=IT-1234567;9876=MM-0505050;3333=IT-4444444"),
                        Map.ofEntries(
                                Map.entry("3333", Set.of("IT-1234567", "IT-4444444")),
                                Map.entry("9876", Set.of("MM-0505050"))
                        )
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.ROYALTIES_INCLUDED_OBJECT_CODE_CHART_ACCOUNT, null),
                        Map.of()
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.ROYALTIES_INCLUDED_OBJECT_CODE_CHART_ACCOUNT,
                                KFSConstants.EMPTY_STRING),
                        Map.of()
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.ROYALTIES_INCLUDED_OBJECT_CODE_CHART_ACCOUNT,
                                KFSConstants.BLANK_SPACE),
                        Map.of()
                )
        );
    }

    static Stream<Arguments> multiValueRegexParametersWithDuplicateSubParameterKeys() {
        return Stream.of(
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.ROYALTIES_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT,
                                "3333=Petty Cash"),
                        Map.of("3333", List.of("^Petty Cash$"))
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.ROYALTIES_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT,
                                "3333=Petty Cash;9876=%Lots_of_Money!$;3333=^Some % Pelf%"),
                        Map.ofEntries(
                                Map.entry("3333", List.of("^Petty Cash$", "^Some .* Pelf.*")),
                                Map.entry("9876", List.of(".*Lots.of.Money!$"))
                        )
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.ROYALTIES_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT,
                                null),
                        Map.of()
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.ROYALTIES_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT,
                                KFSConstants.EMPTY_STRING),
                        Map.of()
                ),
                Arguments.of(
                        parmFor1042S(Tax1042SParameterNames.ROYALTIES_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT,
                                KFSConstants.BLANK_SPACE),
                        Map.of()
                )
        );
    }

    private static List<Parameter> parms(final Parameter... parameters) {
        return List.of(parameters);
    }

    private static Parameter parmFor1042S(final String parameterName, final String value) {
        return TaxParameterUtils.create1042SParameter(parameterName, value);
    }

    private static Parameter parmFor1042S(final String parameterName, final String value,
            final EvaluationOperator evaluationOperator) {
        return TaxParameterUtils.create1042SParameter(parameterName, value, evaluationOperator);
    }



    @ParameterizedTest
    @MethodSource("singleValueParameters")
    void testSingleValueParameters(final List<Parameter> parameters) throws Exception {
        for (final Parameter parameter : parameters) {
            parameterService.createParameter(parameter);
        }

        for (final Parameter expectedParameter : parameters) {
            final Parameter actualParameter = taxParameterService.getParameter(
                    expectedParameter.getComponentCode(), expectedParameter.getName());
            final String actualValue = taxParameterService.getParameterValueAsString(
                    expectedParameter.getComponentCode(), expectedParameter.getName());
            assertNotNull(actualParameter, "Parameter not found: " + expectedParameter.getName());
            assertEquals(expectedParameter.getNamespaceCode(), actualParameter.getNamespaceCode(),
                    "Wrong parameter namespace");
            assertEquals(expectedParameter.getComponentCode(), actualParameter.getComponentCode(),
                    "Wrong parameter component");
            assertEquals(expectedParameter.getName(), actualParameter.getName(),
                    "Wrong parameter name");
            assertEquals(expectedParameter.getEvaluationOperator(), actualParameter.getEvaluationOperator(),
                    "Wrong parameter evaluation operator");
            assertEquals(expectedParameter.getValue(), actualParameter.getValue(),
                    "Wrong parameter value");
            assertEquals(expectedParameter.getValue(), actualValue,
                    "Wrong parameter value returned from getParameterValueAsString() call");
        }

        final Parameter missingParameter = taxParameterService.getParameter(
                CUTaxConstants.TAX_1042S_PARM_DETAIL, NON_EXISTENT_PARM);
        final String missingValue = taxParameterService.getParameterValueAsString(
                CUTaxConstants.TAX_1042S_PARM_DETAIL, NON_EXISTENT_PARM);
        assertNull(missingParameter, "Parameter should not have been found: " + NON_EXISTENT_PARM);
        assertTrue(StringUtils.isBlank(missingValue),
                "A value should not have been present for parameter: " + NON_EXISTENT_PARM);
    }

    @ParameterizedTest
    @MethodSource("simpleMultiValueParameters")
    void testSimpleMultiValueParameters(final Parameter parameter, final List<String> expectedValue) throws Exception {
        parameterService.createParameter(parameter);
        final Set<String> expectedValueAsSet = Set.copyOf(expectedValue);
        final Collection<String> actualValue = taxParameterService.getParameterValuesAsString(
                parameter.getComponentCode(), parameter.getName());
        final Set<String> actualValueAsSet = taxParameterService.getParameterValuesSetAsString(
                parameter.getComponentCode(), parameter.getName());
        assertNotNull(actualValue, "The list/collection should not have been null");
        assertNotNull(actualValueAsSet, "The Set-based result should not have been null");
        assertIterableEquals(expectedValue, actualValue, "Wrong list/collection result");
        assertEquals(expectedValueAsSet, actualValueAsSet, "Wrong Set-based result");
    }

    @ParameterizedTest
    @MethodSource("multiValueParametersWithUniqueSubParameters")
    void testMultiValueParametersWithUniqueSubParameters(final Parameter parameter,
            final Map<String, String> expectedValue) throws Exception {
        parameterService.createParameter(parameter);
        final Map<String, String> actualValue = taxParameterService.getSubParameters(
                parameter.getComponentCode(), parameter.getName());
        assertNotNull(actualValue, "The Map should not have been null");
        assertEquals(expectedValue, actualValue, "Wrong Map-based result");
    }

    @ParameterizedTest
    @MethodSource("multiValueParametersWithMultiValueSubParametersAndInverseMapping")
    void testMultiValueParametersWithMultiValueSubParametersAndInverseMapping(final Parameter parameter,
            final Map<String, Set<String>> expectedValue) throws Exception {
        parameterService.createParameter(parameter);
        final Map<String, Set<String>> actualValue = taxParameterService
                .getValueToKeysMapFromParameterContainingMultiValueEntries(
                        parameter.getComponentCode(), parameter.getName());
        assertNotNull(actualValue, "The Map should not have been null");
        assertEquals(expectedValue, actualValue, "Wrong Map-based result");
    }

}
