package edu.cornell.kfs.sys.service.mock;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coreservice.api.parameter.Parameter;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.vnd.VendorParameterConstants;

import java.util.Collection;

public class MockParameterServiceImpl implements ParameterService {

    private static final String VENDOR_DETAIL_NAME = "org.kuali.kfs.vnd.businessobject.VendorDetail";

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
        if (StringUtils.equals(componentClass.getName(), VENDOR_DETAIL_NAME) && StringUtils.equals(parameterName, VendorParameterConstants.DEFAULT_PHONE_NUMBER_DIGITS)) {
            return "10";
        } else {
            return null;
        }
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
        return null;
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
