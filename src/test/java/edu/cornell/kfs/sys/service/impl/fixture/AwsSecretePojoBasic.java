package edu.cornell.kfs.sys.service.impl.fixture;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class AwsSecretePojoBasic {
    
    private String static_string;
    private String changeable_string;
    private int number_test;
    
    public String getChangeable_string() {
        return changeable_string;
    }

    public void setChangeable_string(String changeable_string) {
        this.changeable_string = changeable_string;
    }

    public int getNumber_test() {
        return number_test;
    }

    public String getStatic_string() {
        return static_string;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }
}
