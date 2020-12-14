package edu.cornell.kfs.sys.service;

import java.text.ParseException;
import java.util.Date;

public interface AwsSecretService {
    
    String getSingleStringValueFromAwsSecret(String awsKeyName);
    
    void updateSecretValue(String awsKeyName, String keyValue);
    
    Date getSingleDateValueFromAwsSecret(String awsKeyName) throws ParseException;
    
    void updateSecretDate(String awsKeyName, Date date);

    <T> T getPojoFromAwsSecret(String awsKeyName, Class<T> objectType);
    
    void updatePojo(String awsKeyName, Object pojo);

}
