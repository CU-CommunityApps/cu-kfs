package edu.cornell.kfs.sys.service.impl.fixture;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;

import edu.cornell.kfs.sys.CUKFSConstants;

public class AwsSecretPojo {

    private String static_string;
    private String changeable_string;
    private int number_test;
    private boolean boolean_test;

    @JsonFormat(pattern = CUKFSConstants.DATE_FORMAT_yyyy_MM_dd_T_HH_mm_ss_SSS)
    private Date update_date;

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

    public Date getUpdate_date() {
        return update_date;
    }

    public void setUpdate_date(Date update_date) {
        this.update_date = update_date;
    }

    public boolean isBoolean_test() {
        return boolean_test;
    }

    public void setBoolean_test(boolean boolean_test) {
        this.boolean_test = boolean_test;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }
}
