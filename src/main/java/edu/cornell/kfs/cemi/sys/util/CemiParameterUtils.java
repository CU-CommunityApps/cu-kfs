package edu.cornell.kfs.cemi.sys.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.CemiBaseParameterConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

/**
 * Resolves the CEMI extract parameters that can be overridden for all extracts at once
 * by parameters on the KFS-CEMI "All" component. A blank override means the job-specific
 * parameter on the extract's Step component is used instead.
 */
public final class CemiParameterUtils {

    private static final Logger LOG = LogManager.getLogger();

    private CemiParameterUtils() {
        throw new UnsupportedOperationException("do not call");
    }

    public static String getSensitiveDataMaskingSetting(final ParameterService parameterService,
            final Class<?> stepClass, final String jobParameterName) {
        final String overrideValue = getAllExtractsOverrideValue(parameterService,
                CemiBaseParameterConstants.CEMI_ALL_EXTRACTS_SENSITIVE_DATA_MASKING_OVERRIDE);
        if (StringUtils.isNotBlank(overrideValue)) {
            LOG.info("getSensitiveDataMaskingSetting, Using all-extracts override value {} for {}",
                    overrideValue, stepClass.getSimpleName());
            return overrideValue;
        }
        return parameterService.getParameterValueAsString(stepClass, jobParameterName);
    }

    public static boolean shouldCopyFileToOutboundFolder(final ParameterService parameterService,
            final Class<?> stepClass, final String jobParameterName) {
        final String overrideValue = getAllExtractsOverrideValue(parameterService,
                CemiBaseParameterConstants.CEMI_ALL_EXTRACTS_COPY_FILE_TO_OUTBOUND_FOLDER_OVERRIDE);
        if (StringUtils.isNotBlank(overrideValue)) {
            LOG.info("shouldCopyFileToOutboundFolder, Using all-extracts override value {} for {}",
                    overrideValue, stepClass.getSimpleName());
            return Strings.CI.equals(StringUtils.trim(overrideValue), CemiBaseConstants.YES);
        }
        return parameterService.getParameterValueAsBoolean(stepClass, jobParameterName);
    }

    private static String getAllExtractsOverrideValue(final ParameterService parameterService,
            final String parameterName) {
        return parameterService.getParameterValueAsString(CemiBaseConstants.CEMI_PARAMETER_NAMESPACE_CODE,
                CUKFSParameterKeyConstants.ALL_COMPONENTS, parameterName);
    }

}
