package edu.cornell.kfs.concur.service;


public interface ConcurAccessTokenService {
    
    String getAccessToken();
    
    String getRefreshToken();
    
    String getConsumerKey();
    
    String getSecretKey();
    
    void refreshAccessToken();

}