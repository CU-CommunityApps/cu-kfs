package edu.cornell.kfs.sys.service.mock;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

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

    private Map<String, String> localSecrets;
    private AWSSecretsManager mockAWSSecretsManager;

    public MockAwsSecretServiceImpl() {
        super();
        this.localSecrets = new HashMap<>();
        this.mockAWSSecretsManager = buildMockAWSSecretsManager();
    }

    @SafeVarargs
    public final void setInitialSecrets(Map.Entry<String, String>... secrets) {
        for (Map.Entry<String, String> secret : secrets) {
            localSecrets.put(secret.getKey(), secret.getValue());
        }
    }

    // Overridden just to increase the method's visibility for unit testing convenience.
    @Override
    public String buildFullAwsKeyName(String awsKeyName, boolean useKfsInstanceNamespace) {
        return super.buildFullAwsKeyName(awsKeyName, useKfsInstanceNamespace);
    }

    @Override
    protected AWSSecretsManager buildAWSSecretsManager() {
        return mockAWSSecretsManager;
    }

    protected AWSSecretsManager buildMockAWSSecretsManager() {
        AWSSecretsManager secretsManager = Mockito.mock(AWSSecretsManager.class);
        Mockito.when(secretsManager.getSecretValue(Mockito.any()))
                .then(this::getLocalSecretValue);
        Mockito.when(secretsManager.updateSecret(Mockito.any()))
                .then(this::updateLocalSecret);
        return secretsManager;
    }

    protected GetSecretValueResult getLocalSecretValue(InvocationOnMock invocation) {
        GetSecretValueRequest request = invocation.getArgument(0);
        String secretValue = localSecrets.get(request.getSecretId());
        return new GetSecretValueResult()
                .withName(request.getSecretId())
                .withSecretString(secretValue);
    }

    protected UpdateSecretResult updateLocalSecret(InvocationOnMock invocation) {
        UpdateSecretRequest request = invocation.getArgument(0);
        localSecrets.put(request.getSecretId(), request.getSecretString());
        
        SdkHttpMetadata httpMetadata = Mockito.mock(SdkHttpMetadata.class);
        Mockito.when(httpMetadata.getHttpStatusCode())
                .thenReturn(Response.Status.OK.getStatusCode());
        
        UpdateSecretResult result = new UpdateSecretResult().withName(request.getSecretId());
        result.setSdkHttpMetadata(httpMetadata);
        return result;
    }

}
