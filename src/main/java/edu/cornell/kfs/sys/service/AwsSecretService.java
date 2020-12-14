package edu.cornell.kfs.sys.service;

import java.util.Date;

public interface AwsSecretService {
    
    String getSingleStringValueFromAwsSecret(String awsKeyName);
    
    void updateSecretValue(String awsKeyName, String keyValue);
    
    Date getSingleDateValueFromAwsSecret(String awsKeyName);

    <T> T getPojoFromAwsSecret(String awsKeyName, Class<T> objectType);

}
