package edu.cornell.kfs.sys.service.impl;

import java.sql.Date;

import edu.cornell.kfs.sys.service.AwsSecretService;

public class AwsSecretServiceImpl  implements AwsSecretService{

    @Override
    public <T> T getPojoFromAwsSecret(String awsKeyName, Class<T> objectType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSingleStringValueFromAwsSecret(String awsKeyName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getSingleDateValueFromAwsSecret(String awsKeyName) {
        // TODO Auto-generated method stub
        return null;
    }

}
