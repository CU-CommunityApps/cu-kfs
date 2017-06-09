package edu.cornell.kfs.concur.service;


public interface ConcurAccessTokenService {
    
    String getAccessToken();
    
    String getRefreshToken();
    
    String getConsumerKey();
    
    String getSecretKey();

    void requestNewAccessToken();

    void refreshAccessToken();

    void revokeAccessToken();

    boolean isCurrentAccessTokenRevoked();

}