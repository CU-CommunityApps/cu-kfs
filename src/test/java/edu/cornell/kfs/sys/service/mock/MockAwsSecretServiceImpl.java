package edu.cornell.kfs.sys.service.mock;

import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import com.amazonaws.http.SdkHttpMetadata;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.UpdateSecretRequest;
import com.amazonaws.services.secretsmanager.model.UpdateSecretResult;

import edu.cornell.kfs.sys.service.impl.AwsSecretServiceImpl;

public class MockAwsSecretServiceImpl extends AwsSecretServiceImpl {

    private Map<String, Map<String, String>> secretsByRegion;

    public MockAwsSecretServiceImpl() {
        super();
        this.secretsByRegion = new HashMap<>();
    }

    @SafeVarargs
    public final void configureInitialSecretsForRegion(String region,
            Map.Entry<String, String>... secrets) {
        Map<String, String> secretsForRegion = secretsByRegion.computeIfAbsent(
                region, this::buildEmptyMapForAnyKey);
        for (Map.Entry<String, String> secret : secrets) {
            secretsForRegion.put(secret.getKey(), secret.getValue());
        }
    }

    @Override
    protected AWSSecretsManager buildAWSSecretsManager() {
        String configuredAwsRegion = awsRegion;
        AWSSecretsManager secretsManager = Mockito.mock(AWSSecretsManager.class);
        Mockito.when(secretsManager.getSecretValue(Mockito.any()))
                .then(invocation -> getSecretValueForRegion(invocation, configuredAwsRegion));
        Mockito.when(secretsManager.updateSecret(Mockito.any()))
                .then(invocation -> updateSecretForRegion(invocation, configuredAwsRegion));
        return secretsManager;
    }

    protected GetSecretValueResult getSecretValueForRegion(InvocationOnMock invocation, String configuredAwsRegion) {
        GetSecretValueRequest request = invocation.getArgument(0);
        Map<String, String> secretsForRegion = secretsByRegion.computeIfAbsent(
                configuredAwsRegion, this::buildEmptyMapForAnyKey);
        String secretValue = secretsForRegion.get(request.getSecretId());
        return new GetSecretValueResult()
                .withName(request.getSecretId())
                .withSecretString(secretValue);
    }

    protected UpdateSecretResult updateSecretForRegion(InvocationOnMock invocation, String configuredAwsRegion) {
        UpdateSecretRequest request = invocation.getArgument(0);
        Map<String, String> secretsForRegion = secretsByRegion.computeIfAbsent(
                configuredAwsRegion, this::buildEmptyMapForAnyKey);
        secretsForRegion.put(request.getSecretId(), request.getSecretString());
        
        SdkHttpMetadata httpMetadata = Mockito.mock(SdkHttpMetadata.class);
        Mockito.when(httpMetadata.getHttpStatusCode())
                .thenReturn(200);
        
        UpdateSecretResult result = new UpdateSecretResult().withName(request.getSecretId());
        result.setSdkHttpMetadata(httpMetadata);
        return result;
    }

    protected Map<String, String> buildEmptyMapForAnyKey(String key) {
        return new HashMap<>();
    }

}
