package edu.cornell.kfs.concur.rest.jsonObjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConcurOauth2TokenResponseDTO {
    
    private int expires_in;
    private String scope;
    private String token_type;
    private String access_token;
    private String refresh_token;
    private int refresh_expires_in;
    private String geolocation;
    private String id_token;
    
    public int getExpires_in() {
        return expires_in;
    }
    
    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public String getToken_type() {
        return token_type;
    }
    
    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }
    
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
    
    public int getRefresh_expires_in() {
        return refresh_expires_in;
    }
    
    public void setRefresh_expires_in(int refresh_expires_in) {
        this.refresh_expires_in = refresh_expires_in;
    }
    
    public String getGeolocation() {
        return geolocation;
    }
    
    public void setGeolocation(String geolocation) {
        this.geolocation = geolocation;
    }
    
    public String getId_token() {
        return id_token;
    }
    
    public void setId_token(String id_token) {
        this.id_token = id_token;
    }
}
