package edu.cornell.kfs.sys.service.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.coreservice.impl.parameter.Parameter;
import org.kuali.kfs.vnd.VendorParameterConstants;

import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.pdp.CUPdpParameterConstants;
import edu.cornell.kfs.rass.RassConstants;
import edu.cornell.kfs.rass.RassParameterConstants;
import edu.cornell.kfs.rass.RassTestConstants;

public class MockParameterServiceImpl implements ParameterService {

    Map<String, String> parameters;

    public MockParameterServiceImpl() {
        parameters = new HashMap<>();
        parameters.put(VendorParameterConstants.DEFAULT_PHONE_NUMBER_DIGITS, "10");
        parameters.put(CUPdpParameterConstants.ACH_PERSONAL_CHECKING_TRANSACTION_CODE, "22PPD");
        parameters.put(CUPdpParameterConstants.ACH_PERSONAL_SAVINGS_TRANSACTION_CODE, "32PPD");
        parameters.put(CUPdpParameterConstants.ACH_DIRECT_DEPOSIT_TRANSACTION_TYPE, "PRAP");
        parameters.put(CUPdpParameterConstants.GENERATED_PAYEE_ACH_ACCOUNT_DOC_NOTE_TEXT, "Created from Workday ACH data extract.");
        parameters.put(CUPdpParameterConstants.NEW_PAYEE_ACH_ACCOUNT_EMAIL_SUBJECT, "New ACH Account in KFS.");
        parameters.put(CUPdpParameterConstants.NEW_PAYEE_ACH_ACCOUNT_EMAIL_BODY,
                "Payment for [payeeIdentifierTypeCode] of [payeeIdNumber] will go to [bankAccountTypeCode] account at [bankRouting.bankName].");
        parameters.put(RassConstants.RASS_DEFAULT_PROPOSAL_AWARD_TYPE_PARAMETER, RassTestConstants.DEFAULT_PROPOSAL_AWARD_TYPE);
        parameters.put(RassParameterConstants.DEFAULT_AWARD_ACCOUNT, RassTestConstants.DEFAULT_AWARD_ACCOUNT_PARAMETER_VALUE);
        parameters.put(RassParameterConstants.DEFAULT_PROJECT_DIRECTOR, RassTestConstants.DEFAULT_PROJECT_DIRECTOR_PRINCIPAL_ID);
        parameters.put(RassParameterConstants.DEFAULT_FUND_MANAGER, RassTestConstants.DEFAULT_FUND_MANAGER_PRINCIPAL_ID);

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
        return subParameterName;
    }

    @Override
    public Collection<String> getSubParameterValuesAsString(Class<?> componentClass, String parameterName, String subParameterName) {
        return null;
    }

    @Override
    public Collection<String> getSubParameterValuesAsString(String namespaceCode, String componentCode, String parameterName, String subParameterName) {
        return null;
    }

    @Override
    public void watchParameter(String namespaceCode, String componentCode, String parameterName,
            Consumer<Parameter> consumer) {
        // Do nothing.
    }
}
