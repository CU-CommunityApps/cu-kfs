package edu.cornell.kfs.sys.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import com.amazonaws.http.SdkHttpMetadata;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.amazonaws.services.secretsmanager.model.UpdateSecretRequest;
import com.amazonaws.services.secretsmanager.model.UpdateSecretResult;

import edu.cornell.kfs.sys.CuSysTestConstants.MockAwsSecretServiceConstants;

public class MockAwsSecretServiceImpl extends AwsSecretServiceImpl {

    private Map<String, String> localSecrets;
    private AWSSecretsManager mockAWSSecretsManager;

    public MockAwsSecretServiceImpl() {
        this(MockAwsSecretServiceConstants.AWS_US_EAST_ONE_REGION,
                MockAwsSecretServiceConstants.KFS_LOCALDEV_INSTANCE_NAMESPACE,
                MockAwsSecretServiceConstants.KFS_SHARED_NAMESPACE,
                MockAwsSecretServiceConstants.AWS_SECRET_DEFAULT_UPDATE_RETRY_COUNT);
    }

    public MockAwsSecretServiceImpl(String awsRegion, String kfsInstanceNamespace,
            String kfsSharedNamespace, int retryCount) {
        super();
        this.awsRegion = awsRegion;
        this.kfsInstanceNamespace = kfsInstanceNamespace;
        this.kfsSharedNamespace = kfsSharedNamespace;
        this.retryCount = retryCount;
        this.localSecrets = new HashMap<>();
        this.mockAWSSecretsManager = buildMockAWSSecretsManager();
    }

    @SafeVarargs
    public final void overrideLocalSecrets(Map.Entry<String, String>... secrets) {
        for (Map.Entry<String, String> secret : secrets) {
            localSecrets.put(secret.getKey(), secret.getValue());
        }
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
        if (secretValue == null) {
            throw new ResourceNotFoundException("Local secret with key " + request.getSecretId()
                    + " could not be found; the real AWSSecretsManager would have thrown a similar exception");
        }
        return new GetSecretValueResult()
                .withName(request.getSecretId())
                .withSecretString(secretValue);
    }

    protected UpdateSecretResult updateLocalSecret(InvocationOnMock invocation) {
        UpdateSecretRequest request = invocation.getArgument(0);
        if (!localSecrets.containsKey(request.getSecretId())) {
            throw new ResourceNotFoundException("Local secret with key " + request.getSecretId()
                    + " could not be found; the real AWSSecretsManager would have thrown a similar exception");
        }
        localSecrets.put(request.getSecretId(), request.getSecretString());
        
        SdkHttpMetadata httpMetadata = Mockito.mock(SdkHttpMetadata.class);
        Mockito.when(httpMetadata.getHttpStatusCode())
                .thenReturn(Response.Status.OK.getStatusCode());
        
        UpdateSecretResult result = new UpdateSecretResult().withName(request.getSecretId());
        result.setSdkHttpMetadata(httpMetadata);
        return result;
    }

}
