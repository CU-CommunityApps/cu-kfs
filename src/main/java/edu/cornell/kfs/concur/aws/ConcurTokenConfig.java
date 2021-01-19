package edu.cornell.kfs.concur.aws;

public class ConcurTokenConfig {
    private String access_token;
    private String refresh_token;
    private String access_token_expiration_date;

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

    public String getAccess_token_expiration_date() {
        return access_token_expiration_date;
    }

    public void setAccess_token_expiration_date(String access_token_expiration_date) {
        this.access_token_expiration_date = access_token_expiration_date;
    }
}
