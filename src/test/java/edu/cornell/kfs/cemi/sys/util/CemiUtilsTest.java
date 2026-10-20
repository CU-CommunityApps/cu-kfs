package edu.cornell.kfs.cemi.sys.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.coreservice.impl.parameter.Parameter;
import org.mockito.Mockito;

/**
 * KFSPTS-39458: Verifies that logging system parameter values never throws,
 * whether the parameter exists or not.
 */
public class CemiUtilsTest {

    private static final String EXISTING_PARAMETER_NAME = "EXISTING_PARAMETER";
    private static final String MISSING_PARAMETER_NAME = "MISSING_PARAMETER";

    private ParameterService parameterService;

    @BeforeEach
    void setUp() {
        parameterService = Mockito.mock(ParameterService.class);
        final Parameter existingParameter = Mockito.mock(Parameter.class);
        Mockito.when(existingParameter.getValue()).thenReturn("Y");
        Mockito.when(parameterService.parameterExists(CemiUtilsTest.class, EXISTING_PARAMETER_NAME))
                .thenReturn(Boolean.TRUE);
        Mockito.when(parameterService.getParameter(CemiUtilsTest.class, EXISTING_PARAMETER_NAME))
                .thenReturn(existingParameter);
        Mockito.when(parameterService.parameterExists(CemiUtilsTest.class, MISSING_PARAMETER_NAME))
                .thenReturn(Boolean.FALSE);
    }

    @Test
    void testLogExistingParameterValue() {
        assertDoesNotThrow(() -> CemiUtils.logParameterValues(
                parameterService, CemiUtilsTest.class, List.of(EXISTING_PARAMETER_NAME)));
        Mockito.verify(parameterService).getParameter(CemiUtilsTest.class, EXISTING_PARAMETER_NAME);
    }

    @Test
    void testMissingParameterLogsWarningWithoutThrowing() {
        assertDoesNotThrow(() -> CemiUtils.logParameterValues(
                parameterService, CemiUtilsTest.class, List.of(MISSING_PARAMETER_NAME)));
        Mockito.verify(parameterService, Mockito.never())
                .getParameter(CemiUtilsTest.class, MISSING_PARAMETER_NAME);
    }

    @Test
    void testMixOfExistingAndMissingParameters() {
        assertDoesNotThrow(() -> CemiUtils.logParameterValues(
                parameterService, CemiUtilsTest.class,
                List.of(EXISTING_PARAMETER_NAME, MISSING_PARAMETER_NAME)));
    }
}
