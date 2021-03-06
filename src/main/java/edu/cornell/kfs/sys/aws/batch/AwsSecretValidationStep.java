package edu.cornell.kfs.sys.aws.batch;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.context.SpringContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import edu.cornell.kfs.sys.aws.AmazonSecretValidationInstance;
import edu.cornell.kfs.sys.aws.AmazonSecretValidationShared;
import edu.cornell.kfs.sys.service.AwsSecretService;

public class AwsSecretValidationStep extends AbstractStep {
    private static final String AWS_SECRET_NAME_VALIDATION_SHARED = "AWS_VALIDATION_SHARED";
    private static final String AWS_SECRET_NAME_VALIDATION_INSTANCE = "AWS_VALIDATION_INSTANCE";

    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String REFRESH_TOKEN = "REFRESH_TOKEN";
    private static final String ACCESS_TOKEN_EXPIRATION_DATE = "ACCESS_TOKEN_EXPIRATION_DATE";
    
    private static final String EXPECTED_SHARED_USER_NAME = "test user";
    private static final String EXPECTED_SHARED_PASSWORD = "test password";

    private static final Logger LOG = LogManager.getLogger();

    protected AwsSecretService awsSecretService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        LOG.info("execute, starting job");
        awsSecretService.clearCache();

        try {
            confirmAmazonSecretValidationShared(awsSecretService.getPojoFromAwsSecret(AWS_SECRET_NAME_VALIDATION_SHARED, 
                    false, AmazonSecretValidationShared.class));
            
            AmazonSecretValidationInstance instanceSpaceSecrets = awsSecretService.getPojoFromAwsSecret(AWS_SECRET_NAME_VALIDATION_INSTANCE, 
                    true, AmazonSecretValidationInstance.class);
            
            Map<String, Object> newSecretValues = buildNewSecretValue();

            updateAmazonSecretValidationInstance(instanceSpaceSecrets, newSecretValues);

            awsSecretService.updatePojo(AWS_SECRET_NAME_VALIDATION_INSTANCE, true, instanceSpaceSecrets);

            confirmInstanceSecretValues(newSecretValues, awsSecretService.getPojoFromAwsSecret(AWS_SECRET_NAME_VALIDATION_INSTANCE, 
                    true, AmazonSecretValidationInstance.class));

            LOG.info("execute, The values were retrieved from cache as expected");
            awsSecretService.logCacheStatus();
            awsSecretService.clearCache();
            
            waitForAwsSecretUpdateToFullyPost();

            AmazonSecretValidationInstance amazonInstanceSpaceSecretsAfterProcessing = awsSecretService.getPojoFromAwsSecret(
                    AWS_SECRET_NAME_VALIDATION_INSTANCE, true, AmazonSecretValidationInstance.class);
            confirmInstanceSecretValues(newSecretValues, amazonInstanceSpaceSecretsAfterProcessing);

            LOG.info("execute, The values were retrieved from Amazon Secret Manager as expected");
            awsSecretService.logCacheStatus();

        } catch (JsonMappingException e) {
            LOG.error("execute, had an error mapping AWS Secrets to POJO", e);
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            LOG.error("execute, had an error process AWS Secrets", e);
            throw new RuntimeException(e);
        }
        
        validateUsingNewAWSServiceFromSpring();

        awsSecretService.clearCache();
        LOG.info("execute, ending job");
        return true;
    }
    
    private void confirmAmazonSecretValidationShared(AmazonSecretValidationShared sharedSpaceSecrets) {
        String errorMessage = StringUtils.EMPTY;
        if (!StringUtils.equals(sharedSpaceSecrets.getLogin_password(), EXPECTED_SHARED_PASSWORD)) {
            errorMessage = errorMessage + "The expected password was not found.";
        }
        if (!StringUtils.equals(sharedSpaceSecrets.getLogin_username(), EXPECTED_SHARED_USER_NAME)) {
            errorMessage = errorMessage + " The expected user name was not found.";
        }

        if (StringUtils.isNotBlank(errorMessage)) {
            throw new RuntimeException(errorMessage);
        } else {
            LOG.info("confirmAmazonSecretValidationShared, the shared secrets contained the expected values");
        }
    }

    private Map<String, Object> buildNewSecretValue() {
        Map<String, Object> values = new HashMap<>();
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

    protected void confirmInstanceSecretValues(Map<String, Object> expectedSecretValues,
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
    
    protected void validateUsingNewAWSServiceFromSpring() {
        AwsSecretService service = SpringContext.getBean(AwsSecretService.class);
        try {
            AmazonSecretValidationShared sharedSpaceSecrets = service.getPojoFromAwsSecret(AWS_SECRET_NAME_VALIDATION_SHARED, 
                    false, AmazonSecretValidationShared.class);
            confirmAmazonSecretValidationShared(sharedSpaceSecrets);
            LOG.info("validateUsingNewAWSServiceFromSpring, confirmed that shared secrets were retrieved correctly using a new instance of AwsSecretService");
            service.logCacheStatus();
        } catch (JsonProcessingException e) {
            LOG.error("validateUsingNewAWSServiceFromSpring, had an error calling AWS Secret Service ", e);
            throw new RuntimeException(e);
        }
    }
    
    protected void waitForAwsSecretUpdateToFullyPost() {
        LOG.info("waitForAwsSecretUpdateToFullyPost, waiting 5 seconds");
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            LOG.error("waitForAwsSecretUpdateToFullyPost. had an error waiting", e);
            throw new RuntimeException(e);
        }
    }

    public void setAwsSecretService(AwsSecretService awsSecretService) {
        this.awsSecretService = awsSecretService;
    }

}
