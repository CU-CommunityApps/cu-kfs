package org.kuali.kfs.sys.aws.batch;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.aws.AmazonSecretValidationInstance;
import org.kuali.kfs.sys.aws.AmazonSecretValidationShared;
import org.kuali.kfs.sys.batch.AbstractStep;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import edu.cornell.kfs.sys.service.AwsSecretService;

public class AwsSecretValidationStep extends AbstractStep {
    private static final String AWS_SECRET_NAME_VALIDATION_SHARED = "AWS_VALIDATION_SHARED";
    private static final String AWS_SECRET_NAME_VALIDATION_INSTANCE = "AWS_VALIDATION_INSTANCE";

    private static final String NEW_ACCESS_TOKEN = "NEW_ACCESS_TOKEN";
    private static final String NEW_REFRESH_TOKEN = "NEW_REFRESH_TOKEN";
    private static final String NEW_ACCESS_TOKEN_EXPIRATION_DATE = "NEW_ACCESS_TOKEN_EXPIRATION_DATE";

    private static final Logger LOG = LogManager.getLogger(AwsSecretValidationStep.class);

    protected AwsSecretService awsSecretService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        LOG.info("execute, starting job");
        awsSecretService.initializeCache(this.getClass());

        try {
            AmazonSecretValidationShared shared = awsSecretService.getPojoFromAwsSecret(this.getClass(),
                    AWS_SECRET_NAME_VALIDATION_SHARED, false, AmazonSecretValidationShared.class);
            AmazonSecretValidationInstance instance = awsSecretService.getPojoFromAwsSecret(this.getClass(),
                    AWS_SECRET_NAME_VALIDATION_INSTANCE, true, AmazonSecretValidationInstance.class);

            Map<String, Object> newSecretValues = buildNewSecretValue();

            instance.setAccess_token(newSecretValues.get(NEW_ACCESS_TOKEN).toString());
            instance.setRefresh_token(newSecretValues.get(NEW_REFRESH_TOKEN).toString());
            instance.setAccess_token_expiration_date((Date) newSecretValues.get(NEW_ACCESS_TOKEN_EXPIRATION_DATE));

            awsSecretService.updatePojo(this.getClass(), AWS_SECRET_NAME_VALIDATION_INSTANCE, true, instance);

            confirmInstanceSecetValues(newSecretValues, awsSecretService.getPojoFromAwsSecret(this.getClass(),
                    AWS_SECRET_NAME_VALIDATION_INSTANCE, true, AmazonSecretValidationInstance.class));

            LOG.info("excute, The values were retrieved from cache as expected");

            awsSecretService.clearCache(this.getClass());

            confirmInstanceSecetValues(newSecretValues, awsSecretService.getPojoFromAwsSecret(this.getClass(),
                    AWS_SECRET_NAME_VALIDATION_INSTANCE, true, AmazonSecretValidationInstance.class));

            LOG.info("excute, The values were retrieved from Amazon Secret Manager as expected");

        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        awsSecretService.clearCache(this.getClass());
        LOG.info("execute, starting ending job");
        return true;
    }

    private Map<String, Object> buildNewSecretValue() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put(NEW_ACCESS_TOKEN, UUID.randomUUID().toString());
        values.put(NEW_REFRESH_TOKEN, UUID.randomUUID().toString());
        values.put(NEW_ACCESS_TOKEN_EXPIRATION_DATE, Calendar.getInstance(Locale.US).getTime());
        return values;
    }

    protected void confirmInstanceSecetValues(Map<String, Object> expectedSecretValues,
            AmazonSecretValidationInstance instanceValues) {
        String errorMessage = StringUtils.EMPTY;
        if (!StringUtils.equals(instanceValues.getAccess_token(),
                expectedSecretValues.get(NEW_ACCESS_TOKEN).toString())) {
            errorMessage = errorMessage + "Did not update ACCESS token as expected.";
        }
        if (!StringUtils.equals(instanceValues.getRefresh_token(),
                expectedSecretValues.get(NEW_REFRESH_TOKEN).toString())) {
            errorMessage = errorMessage + " Did not update REFRESH token as expected.";
        }
        if (!instanceValues.getAccess_token_expiration_date()
                .equals((Date) expectedSecretValues.get(NEW_ACCESS_TOKEN_EXPIRATION_DATE))) {
            errorMessage = errorMessage + " Did not update access token EXPIRATION DATE as expected.";
        }

        if (StringUtils.isNotBlank(errorMessage)) {
            throw new RuntimeException(errorMessage);
        }
    }

    public void setAwsSecretService(AwsSecretService awsSecretService) {
        this.awsSecretService = awsSecretService;
    }

}
