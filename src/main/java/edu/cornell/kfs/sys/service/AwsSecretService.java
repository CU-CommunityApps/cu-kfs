package edu.cornell.kfs.sys.service;

import java.sql.Date;

public interface AwsSecretService {

    <T> T getPojoFromAwsSecret(String awsKeyName, Class<T> objectType);

    String getSingleStringValueFromAwsSecret(String awsKeyName);

    Date getSingleDateValueFromAwsSecret(String awsKeyName);

}
