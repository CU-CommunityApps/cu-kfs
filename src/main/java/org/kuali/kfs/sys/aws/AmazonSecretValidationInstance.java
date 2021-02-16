package org.kuali.kfs.sys.aws;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonFormat;

import edu.cornell.kfs.sys.CUKFSConstants;

public class AmazonSecretValidationInstance {
    private String access_token;

    private String refresh_token;

    @JsonFormat(pattern = CUKFSConstants.DATE_FORMAT_yyyy_MM_dd_T_HH_mm_ss_SSS, locale = CUKFSConstants.US_LOCALE_STRING)
    private Date access_token_expiration_date;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public Date getAccess_token_expiration_date() {
        return access_token_expiration_date;
    }

    public void setAccess_token_expiration_date(Date access_token_expiration_date) {
        this.access_token_expiration_date = access_token_expiration_date;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }

}
