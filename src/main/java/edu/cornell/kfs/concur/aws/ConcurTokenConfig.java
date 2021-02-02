package edu.cornell.kfs.concur.aws;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import edu.cornell.kfs.sys.CUKFSConstants;

public class ConcurTokenConfig {
    private String access_token;

    private String refresh_token;

    @JsonFormat(pattern = CUKFSConstants.DATE_FORMAT_mm_dd_yyyy_hh_mm_ss_am, locale = CUKFSConstants.US_LOCALE_STRING)
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
}
