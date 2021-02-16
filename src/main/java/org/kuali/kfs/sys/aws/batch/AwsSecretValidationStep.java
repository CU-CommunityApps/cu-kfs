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

    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String REFRESH_TOKEN = "REFRESH_TOKEN";
    private static final String ACCESS_TOKEN_EXPIRATION_DATE = "ACCESS_TOKEN_EXPIRATION_DATE";

    private static final Logger LOG = LogManager.getLogger(AwsSecretValidationStep.class);

    protected AwsSecretService awsSecretService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        LOG.info("execute, starting job");
        initializeAmazonSecretCache();

        try {
            AmazonSecretValidationShared sharedSpaceSecrets = awsSecretService.getPojoFromAwsSecret(this.getClass(),
                    AWS_SECRET_NAME_VALIDATION_SHARED, false, AmazonSecretValidationShared.class);
            LOG.info("execute, sharedSpaceSecrets: " + sharedSpaceSecrets);
            AmazonSecretValidationInstance instanceSpaceSecrets = awsSecretService.getPojoFromAwsSecret(this.getClass(),
                    AWS_SECRET_NAME_VALIDATION_INSTANCE, true, AmazonSecretValidationInstance.class);
            LOG.info("execute, instanceSpaceSecrets: " + instanceSpaceSecrets);
            AmazonSecretValidationInstance instanceDifferentCacheScope = awsSecretService.getPojoFromAwsSecret(AmazonSecretValidationInstance.class,
                    AWS_SECRET_NAME_VALIDATION_INSTANCE, true, AmazonSecretValidationInstance.class);
            LOG.info("execute, instanceDifferentCacheScope: " + instanceDifferentCacheScope);

            Map<String, Object> newSecretValues = buildNewSecretValue();

            updateAmazonSecretValidationInstance(instanceSpaceSecrets, newSecretValues);

            awsSecretService.updatePojo(this.getClass(), AWS_SECRET_NAME_VALIDATION_INSTANCE, true, instanceSpaceSecrets);

            confirmInstanceSecetValues(newSecretValues, awsSecretService.getPojoFromAwsSecret(this.getClass(),
                    AWS_SECRET_NAME_VALIDATION_INSTANCE, true, AmazonSecretValidationInstance.class));

            LOG.info("excute, The values were retrieved from cache as expected");

            awsSecretService.clearCache(this.getClass());

            confirmInstanceSecetValues(newSecretValues, awsSecretService.getPojoFromAwsSecret(this.getClass(),
                    AWS_SECRET_NAME_VALIDATION_INSTANCE, true, AmazonSecretValidationInstance.class));

            LOG.info("excute, The values were retrieved from Amazon Secret Manager as expected");

        } catch (JsonMappingException e) {
            LOG.error("execute, had an error mapping AWS Secrets to POJO", e);
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            LOG.error("execute, had an error process AWS Secrets", e);
            throw new RuntimeException(e);
        }

        awsSecretService.clearCache(this.getClass());
        LOG.info("execute, ending job");
        return true;
    }

    protected void initializeAmazonSecretCache() {
        awsSecretService.initializeCache(this.getClass());
        awsSecretService.initializeCache(AmazonSecretValidationInstance.class);
    }

    private Map<String, Object> buildNewSecretValue() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put(ACCESS_TOKEN, UUID.randomUUID().toString());
        values.put(REFRESH_TOKEN, UUID.randomUUID().toString());
        values.put(ACCESS_TOKEN_EXPIRATION_DATE, Calendar.getInstance(Locale.US).getTime());
        return values;
    }
    
    protected void updateAmazonSecretValidationInstance(AmazonSecretValidationInstance instance,
            Map<String, Object> newSecretValues) {
        instance.setAccess_token(newSecretValues.get(ACCESS_TOKEN).toString());
        instance.setRefresh_token(newSecretValues.get(REFRESH_TOKEN).toString());
        instance.setAccess_token_expiration_date((Date) newSecretValues.get(ACCESS_TOKEN_EXPIRATION_DATE));
    }

    protected void confirmInstanceSecetValues(Map<String, Object> expectedSecretValues,
            AmazonSecretValidationInstance instanceValues) {
        String errorMessage = StringUtils.EMPTY;
        if (!StringUtils.equals(instanceValues.getAccess_token(),
                expectedSecretValues.get(ACCESS_TOKEN).toString())) {
            errorMessage = errorMessage + "Did not update ACCESS token as expected.";
        }
        if (!StringUtils.equals(instanceValues.getRefresh_token(),
                expectedSecretValues.get(REFRESH_TOKEN).toString())) {
            errorMessage = errorMessage + " Did not update REFRESH token as expected.";
        }
        if (!instanceValues.getAccess_token_expiration_date()
                .equals((Date) expectedSecretValues.get(ACCESS_TOKEN_EXPIRATION_DATE))) {
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
