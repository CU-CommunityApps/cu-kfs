package edu.cornell.kfs.sys.service;

import java.text.ParseException;
import java.util.Date;

public interface AwsSecretService {
    
    String getSingleStringValueFromAwsSecret(String awsKeyName, boolean useKfsInstanceNamespace);
    
    void updateSecretValue(String awsKeyName, boolean useKfsInstanceNamespace, String keyValue);
    
    Date getSingleDateValueFromAwsSecret(String awsKeyName, boolean useKfsInstanceNamespace) throws ParseException;
    
    void updateSecretDate(String awsKeyName, boolean useKfsInstanceNamespace, Date date);
    
    boolean getSingleBooleanFromAwsSecret(String awsKeyName, boolean useKfsInstanceNamespace);
    
    void updateSecretBoolean(String awsKeyName, boolean useKfsInstanceNamespace,  boolean booleanValue);

    <T> T getPojoFromAwsSecret(String awsKeyName, boolean useKfsInstanceNamespace, Class<T> objectType);
    
    void updatePojo(String awsKeyName, boolean useKfsInstanceNamespace, Object pojo);

}
