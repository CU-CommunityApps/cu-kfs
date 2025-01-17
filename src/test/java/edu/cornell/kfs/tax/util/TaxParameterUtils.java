package edu.cornell.kfs.tax.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.coreservice.api.parameter.EvaluationOperator;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.coreservice.impl.parameter.Parameter;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.CuMockBuilder;
import edu.cornell.kfs.tax.CUTaxConstants;

public final class TaxParameterUtils {

    public static Parameter create1042SParameter(final String parameterName, final String value) {
        return create1042SParameter(parameterName, value, EvaluationOperator.ALLOW);
    }

    public static Parameter create1042SParameter(final String parameterName, final String value,
            final EvaluationOperator evaluationOperator) {
        return createParameter(CUTaxConstants.TAX_1042S_PARM_DETAIL, parameterName, value, evaluationOperator);
    }

    public static Parameter createParameter(final String componentCode, final String parameterName,
            final String value) {
        return createParameter(componentCode, parameterName, value, EvaluationOperator.ALLOW);
    }

    public static Parameter createParameter(final String componentCode, final String parameterName,
            final String value, final EvaluationOperator evaluationOperator) {
        final Parameter parameter = new Parameter();
        parameter.setNamespaceCode(CUTaxConstants.TAX_NAMESPACE);
        parameter.setComponentCode(componentCode);
        parameter.setName(parameterName);
        parameter.setValue(value);
        parameter.setEvaluationOperatorCode(evaluationOperator.getCode());
        return parameter;
    }



    public static ParameterService createUpdatableMockParameterServiceForTaxProcessing() {
        final Map<Pair<String, String>, Parameter> parameters = new ConcurrentHashMap<>();
        return new CuMockBuilder<>(ParameterService.class)
                .withAnswer(
                        service -> service.createParameter(Mockito.any()),
                        invocation -> createOrUpdateParameter(parameters, invocation))
                .withAnswer(
                        service -> service.updateParameter(Mockito.any()),
                        invocation -> createOrUpdateParameter(parameters, invocation))
                .withAnswer(
                        service -> service.getParameter(
                                Mockito.eq(CUTaxConstants.TAX_NAMESPACE), Mockito.anyString(), Mockito.anyString()),
                        invocation -> getParameter(parameters, invocation))
                .withAnswer(
                        service -> service.getParameterValueAsString(
                                Mockito.eq(CUTaxConstants.TAX_NAMESPACE), Mockito.anyString(), Mockito.anyString()),
                        invocation -> getParameterValue(parameters, invocation))
                .withAnswer(
                        service -> service.getParameterValuesAsString(
                                Mockito.eq(CUTaxConstants.TAX_NAMESPACE), Mockito.anyString(), Mockito.anyString()),
                        invocation -> getParameterValues(parameters, invocation))
                .build();
    }

    private static Parameter createOrUpdateParameter(
            final Map<Pair<String, String>, Parameter> parameters, final InvocationOnMock invocation) {
        final Parameter parameter = invocation.getArgument(0);
        Validate.notNull(parameter, "parameter cannot be null");
        Validate.isTrue(StringUtils.equals(parameter.getNamespaceCode(), CUTaxConstants.TAX_NAMESPACE),
                "parameter doesn't have the expected tax namespace");
        parameters.put(Pair.of(parameter.getComponentCode(), parameter.getName()), parameter);
        return parameter;
    }

    private static Parameter getParameter(
            final Map<Pair<String, String>, Parameter> parameters, final InvocationOnMock invocation) {
        final String componentCode = invocation.getArgument(1);
        final String parameterName = invocation.getArgument(2);
        return parameters.get(Pair.of(componentCode, parameterName));
    }

    private static String getParameterValue(
            final Map<Pair<String, String>, Parameter> parameters, final InvocationOnMock invocation) {
        final Parameter parameter = getParameter(parameters, invocation);
        return parameter != null ? parameter.getValue() : null;
    }

    private static Collection<String> getParameterValues(
            final Map<Pair<String, String>, Parameter> parameters, final InvocationOnMock invocation) {
        final String parameterValue = getParameterValue(parameters, invocation);
        return StringUtils.isNotBlank(parameterValue)
                ? List.of(StringUtils.split(parameterValue, CUKFSConstants.SEMICOLON))
                : List.of();
    }

}
