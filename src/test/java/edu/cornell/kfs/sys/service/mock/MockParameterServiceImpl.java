package edu.cornell.kfs.sys.service.mock;

import edu.cornell.kfs.fp.CuFPParameterConstants;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.vnd.VendorParameterConstants;
import org.kuali.rice.coreservice.api.parameter.Parameter;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MockParameterServiceImpl implements ParameterService {

    Map<String, String> parameters;

    public MockParameterServiceImpl() {
        parameters = new HashMap<>();
        parameters.put(CuFPParameterConstants.AdvanceDepositDocument.CHART, "IT");
        parameters.put(CuFPParameterConstants.AdvanceDepositDocument.OBJECT_CODE, "2240");
        parameters.put(CuFPParameterConstants.AdvanceDepositDocument.ACCOUNT, "G621060");
        parameters.put(VendorParameterConstants.DEFAULT_PHONE_NUMBER_DIGITS, "10");
        parameters.put(CuFPParameterConstants.AchIncome.ACH_INCOME_SUMMARY_FROM_EMAIL_ADDRESS, "achIncomeTest@cornell.edu");
        parameters.put(CuFPParameterConstants.AchIncome.ACH_INCOME_SUMMARY_EMAIL_SUBJECT, "Advance Deposit Service Test Summary Email");
        parameters.put(CuFPParameterConstants.AchIncome.ACH_INCOME_SUMMARY_TO_EMAIL_ADDRESSES, "achIncomeTest@cornell.edu");
    }

    @Override
    public Parameter createParameter(Parameter parameter) {
        return null;
    }

    @Override
    public Parameter updateParameter(Parameter parameter) {
        return null;
    }

    @Override
    public Boolean parameterExists(Class<?> componentClass, String parameterName) {
        return null;
    }

    @Override
    public Boolean parameterExists(String namespaceCode, String componentCode, String parameterName) {
        return null;
    }

    @Override
    public String getParameterValueAsString(Class<?> componentClass, String parameterName) {
        return parameters.get(parameterName);
    }

    @Override
    public String getParameterValueAsString(Class<?> componentClass, String parameterName, String defaultValue) {
        return null;
    }

    @Override
    public String getParameterValueAsString(String namespaceCode, String componentCode, String parameterName) {
        if (StringUtils.equals(namespaceCode, "KFS-FP") && StringUtils.equals(namespaceCode, "ProcurementCard") && StringUtils.equals(parameterName, "PCARD_UPLOAD_ERROR_EMAIL_ADDR")) {
            return "fake@cornell.edu";
        } else {
            return null;
        }
    }

    @Override
    public String getParameterValueAsString(String namespaceCode, String componentCode, String parameterName, String defaultValue) {
        return null;
    }

    @Override
    public Boolean getParameterValueAsBoolean(Class<?> componentClass, String parameterName) {
        return null;
    }

    @Override
    public Boolean getParameterValueAsBoolean(Class<?> componentClass, String parameterName, Boolean defaultValue) {
        return null;
    }

    @Override
    public Boolean getParameterValueAsBoolean(String namespaceCode, String componentCode, String parameterName) {
        return null;
    }

    @Override
    public Boolean getParameterValueAsBoolean(String namespaceCode, String componentCode, String parameterName, Boolean defaultValue) {
        return null;
    }

    @Override
    public Parameter getParameter(Class<?> componentClass, String parameterName) {
        return null;
    }

    @Override
    public Parameter getParameter(String namespaceCode, String componentCode, String parameterName) {
        return null;
    }

    @Override
    public Collection<String> getParameterValuesAsString(Class<?> componentClass, String parameterName) {
        String strValues = getParameterValueAsString(componentClass, parameterName);

        if (StringUtils.isBlank(strValues)) {
            return Collections.emptyList();
        }

        final Collection<String> values = new ArrayList<String>();
        for (String value : strValues.split(";")) {
            values.add(value.trim());
        }

        return Collections.unmodifiableCollection(values);

    }

    @Override
    public Collection<String> getParameterValuesAsString(String namespaceCode, String componentCode, String parameterName) {
        return null;
    }

    @Override
    public String getSubParameterValueAsString(Class<?> componentClass, String parameterName, String subParameterName) {
        return null;
    }

    @Override
    public String getSubParameterValueAsString(String namespaceCode, String componentCode, String parameterName, String subParameterName) {
        return null;
    }

    @Override
    public Collection<String> getSubParameterValuesAsString(Class<?> componentClass, String parameterName, String subParameterName) {
        return null;
    }

    @Override
    public Collection<String> getSubParameterValuesAsString(String namespaceCode, String componentCode, String parameterName, String subParameterName) {
        return null;
    }
}
